package com.example.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SahNajApplication
import com.example.data.model.CommandEntity
import com.example.data.model.StructuredAction
import com.example.domain.personality.PersonalityResponses
import com.example.domain.validator.ValidationResult
import com.example.util.PipelineLatencyTracker
import com.example.util.TechSoundManager
import com.example.util.WakeLockHelper
import com.example.voice.SpeechState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class AssistantVoiceService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var stateCollectJob: Job? = null
    private var isPaused = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "[FOREGROUND SERVICE] AssistantVoiceService onCreate")
        createNotificationChannel()
        _isServiceActive.value = true
        _isListeningActive.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.d(TAG, "[FOREGROUND SERVICE] onStartCommand received action: $action")

        when (action) {
            ACTION_TOGGLE -> {
                if (isPaused) {
                    resumeListening()
                } else {
                    pauseListening()
                }
            }
            ACTION_PAUSE -> {
                pauseListening()
            }
            ACTION_RESUME -> {
                resumeListening()
            }
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                isPaused = false
                _isListeningActive.value = true
                startForegroundWithNotification(isPaused = false)
                startBackgroundListeningLoop()
            }
        }

        return START_STICKY
    }

    private fun pauseListening() {
        Log.d(TAG, "[FOREGROUND SERVICE] Pausing background wake-word listening")
        isPaused = true
        _isListeningActive.value = false
        val app = applicationContext as? SahNajApplication
        app?.speechRecognizerManager?.stopListening()
        app?.wakeWordEngine?.setListeningForWakeWord(false)
        startForegroundWithNotification(isPaused = true)
    }

    private fun resumeListening() {
        Log.d(TAG, "[FOREGROUND SERVICE] Resuming background wake-word listening")
        isPaused = false
        _isListeningActive.value = true
        startForegroundWithNotification(isPaused = false)
        startBackgroundListeningLoop()
    }

    private fun startForegroundWithNotification(isPaused: Boolean) {
        val notification = buildForegroundNotification(isPaused)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            Log.d(TAG, "[FOREGROUND SERVICE] Foreground elevation active. isPaused=$isPaused")
        } catch (e: Exception) {
            Log.e(TAG, "[FOREGROUND SERVICE] Failed to elevate foreground service", e)
        }
    }

    private val latencyTracker = PipelineLatencyTracker()

    private fun startBackgroundListeningLoop() {
        val app = applicationContext as? SahNajApplication ?: return
        val permStatus = app.permissionManager.getPermissionStatus()
        if (!permStatus.hasMic) {
            Log.w(TAG, "[FOREGROUND SERVICE] Microphone permission missing. Waiting for user grant.")
            return
        }

        if (!app.userPreferences.isWakeWordEnabled()) {
            Log.d(TAG, "[FOREGROUND SERVICE] Wake word disabled in preferences.")
            return
        }

        app.wakeWordEngine.setListeningForWakeWord(true)
        val langCode = when (app.userPreferences.getLanguage().lowercase()) {
            "english" -> "en-US"
            else -> "hi-IN"
        }

        // Fast partial listener to intercept wake word with zero latency
        app.speechRecognizerManager.onPartialWakeWordListener = { partialSpeech ->
            val assistantName = app.userPreferences.getAssistantName()
            if (app.wakeWordEngine.matchesWakeWord(partialSpeech, assistantName)) {
                Log.d(TAG, "[FOREGROUND SERVICE] ⚡ Instant wake-word match on partial transcript: '$partialSpeech'")
                serviceScope.launch {
                    handleBackgroundSpeech(partialSpeech)
                }
                true
            } else {
                false
            }
        }

        Log.d(TAG, "[FOREGROUND SERVICE] Starting continuous background recognizer with lang=$langCode")
        app.speechRecognizerManager.startListening(languageCode = langCode, continuous = true)

        // Observe speech state
        stateCollectJob?.cancel()
        stateCollectJob = serviceScope.launch {
            app.speechRecognizerManager.speechState.collect { speechState ->
                if (isPaused) return@collect

                when (speechState) {
                    is SpeechState.Result -> {
                        handleBackgroundSpeech(speechState.text)
                    }
                    is SpeechState.Error -> {
                        Log.d(TAG, "[FOREGROUND SERVICE] Speech error: ${speechState.message}")
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun handleBackgroundSpeech(rawSpeech: String) {
        val app = applicationContext as? SahNajApplication ?: return
        val clean = rawSpeech.trim()
        if (clean.isBlank()) return

        val assistantName = app.userPreferences.getAssistantName()
        val isMatched = app.wakeWordEngine.matchesWakeWord(clean, assistantName)

        Log.d(TAG, "[FOREGROUND SERVICE] Background speech: '$clean', wake-word match: $isMatched")

        if (!isMatched) return

        // 1. Voice Lock check: Verify speaker against enrolled profile if enabled
        if (app.userPreferences.isVoiceLockEnabled()) {
            val verificationResult = app.voiceBiometricsEngine.verifySpeaker(clean)
            if (!verificationResult.isMatch) {
                Log.d(TAG, "[VOICE AUTH DEBUG] अपरिचित आवाज़ - सक्रिय नहीं हुआ (Speaker confidence: ${verificationResult.confidenceScore})")
                return
            }
            Log.d(TAG, "[VOICE AUTH] Enrolled speaker matched with confidence ${verificationResult.confidenceScore}")
        }

        // 2. Wake word detected: Start pipeline latency tracking and acquire wakelock
        latencyTracker.startPipeline()
        latencyTracker.markWakeWordDetected()
        WakeLockHelper.acquireWakeLock(this, 15000L)

        // Play brief futuristic cyber activation ping
        serviceScope.launch {
            TechSoundManager.playWakeTriggerSound(applicationContext)
        }

        val residualCommand = app.wakeWordEngine.stripWakeWord(clean, assistantName)
        Log.d(TAG, "[FOREGROUND SERVICE] Wake word triggered! Command: '$residualCommand'")

        if (residualCommand.isBlank()) {
            // Speak wake greeting and immediately start listening for subsequent command
            val currentUserName = app.userPreferences.getUserDisplayName()
            val useNamePref = app.userPreferences.isUseNameWhenSpeaking()
            val greeting = PersonalityResponses.getRandomWakeWordGreeting(
                userName = currentUserName,
                useNameWhenSpeaking = useNamePref
            )
            latencyTracker.markTtsStarted()
            app.textToSpeechManager.speak(greeting, app.userPreferences.getSpeechRate()) {
                Log.d(TAG, "[FOREGROUND SERVICE] Greeting complete. Starting follow-up command listening...")
                startListeningForCommand()
            }
        } else {
            // Process command directly
            processBackgroundCommand(residualCommand)
        }
    }

    private fun startListeningForCommand() {
        val app = applicationContext as? SahNajApplication ?: run {
            WakeLockHelper.releaseWakeLock()
            return
        }
        val langCode = when (app.userPreferences.getLanguage().lowercase()) {
            "english" -> "en-US"
            else -> "hi-IN"
        }
        Log.d(TAG, "[FOREGROUND SERVICE] Listening for user command with lang=$langCode...")

        app.speechRecognizerManager.onPartialWakeWordListener = null
        app.speechRecognizerManager.startListening(languageCode = langCode, continuous = false)

        stateCollectJob?.cancel()
        stateCollectJob = serviceScope.launch {
            app.speechRecognizerManager.speechState.collect { speechState ->
                if (isPaused) return@collect
                when (speechState) {
                    is SpeechState.Result -> {
                        Log.d(TAG, "[FOREGROUND SERVICE] Command recognized: '${speechState.text}'")
                        processBackgroundCommand(speechState.text)
                    }
                    is SpeechState.Error -> {
                        Log.d(TAG, "[FOREGROUND SERVICE] Command listening ended/error: ${speechState.message}")
                        WakeLockHelper.releaseWakeLock()
                        if (!isPaused) {
                            startBackgroundListeningLoop()
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    private fun processBackgroundCommand(commandText: String) {
        val app = applicationContext as? SahNajApplication ?: run {
            WakeLockHelper.releaseWakeLock()
            return
        }

        latencyTracker.markSttReceived(commandText)

        serviceScope.launch {
            try {
                val assistantName = app.userPreferences.getAssistantName()

                // 1. Try local offline parser
                val localParsed = app.localCommandParser.parseLocally(commandText, assistantName)
                if (localParsed != null) {
                    latencyTracker.markParserComplete("Local Offline Parser")
                    executeBackgroundAction(localParsed, isGemini = false)
                    return@launch
                }

                // 2. Play subtle thinking feedback ping
                TechSoundManager.playThinkingChirp(applicationContext)

                // 3. Try Gemini AI (with only last 2 turns context for speed)
                val recent = app.commandHistoryRepository.getRecentHistory(2).firstOrNull() ?: emptyList()
                val contextInfo = recent.joinToString("; ") { "Turn: ${it.commandText} -> ${it.spokenResponse}" }

                val result = app.geminiRepository.parseCommand(commandText, assistantName, contextInfo)
                result.onSuccess { structuredAction ->
                    latencyTracker.markParserComplete("Gemini Flash AI")
                    executeBackgroundAction(structuredAction, isGemini = true)
                }.onFailure { error ->
                    latencyTracker.markParserComplete("Smart Human Engine (Zero-API)")
                    Log.w(TAG, "[FOREGROUND SERVICE] Gemini unavailable (${error.message}), seamlessly engaging Smart Human Engine...")
                    val smartReply = com.example.domain.personality.SmartHumanEngine.generateSmartReply(commandText, assistantName)
                    val fallbackAction = com.example.data.model.StructuredAction(
                        action = com.example.data.model.ActionType.SPEAK_TEXT,
                        target = "SMART_HUMAN_ENGINE",
                        spokenResponse = smartReply,
                        rawPrompt = commandText,
                        riskLevel = com.example.data.model.RiskLevel.LOW
                    )
                    executeBackgroundAction(fallbackAction, isGemini = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[FOREGROUND SERVICE] Error executing background command", e)
                WakeLockHelper.releaseWakeLock()
                if (!isPaused) startBackgroundListeningLoop()
            }
        }
    }

    private fun executeBackgroundAction(action: StructuredAction, isGemini: Boolean) {
        val app = applicationContext as? SahNajApplication ?: run {
            WakeLockHelper.releaseWakeLock()
            return
        }

        val validation = app.actionValidator.validate(action)
        if (validation is ValidationResult.Invalid) {
            latencyTracker.markTtsStarted()
            app.dualVoiceEngine.speakAssistantResponse(validation.reason) {
                WakeLockHelper.releaseWakeLock()
                if (!isPaused) startBackgroundListeningLoop()
            }
            return
        }

        serviceScope.launch {
            try {
                val result = app.actionExecutor.execute(action)
                latencyTracker.markActionExecuted(action.action.name)
                app.commandHistoryRepository.addCommand(
                    CommandEntity(
                        commandText = action.rawPrompt,
                        actionType = action.action.name,
                        target = action.target,
                        status = result.status.name,
                        spokenResponse = result.spokenResponse,
                        isGemini = isGemini
                    )
                )

                if (result.spokenResponse.isNotBlank()) {
                    latencyTracker.markTtsStarted()
                    app.dualVoiceEngine.speakAssistantResponse(result.spokenResponse) {
                        WakeLockHelper.releaseWakeLock()
                        if (!isPaused) startBackgroundListeningLoop()
                    }
                } else {
                    latencyTracker.markTtsStarted()
                    WakeLockHelper.releaseWakeLock()
                    if (!isPaused) startBackgroundListeningLoop()
                }
            } catch (e: Exception) {
                Log.e(TAG, "[FOREGROUND SERVICE] Action execution failed", e)
                WakeLockHelper.releaseWakeLock()
                if (!isPaused) startBackgroundListeningLoop()
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "[FOREGROUND SERVICE] AssistantVoiceService onDestroy")
        _isServiceActive.value = false
        _isListeningActive.value = false
        stateCollectJob?.cancel()
        serviceScope.cancel()
        WakeLockHelper.releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "सहनाज 24x7 Core AI Voice Service",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "24x7 Hands-free voice wake-word & automation engine"
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(isPaused: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, AssistantVoiceService::class.java).apply {
            action = ACTION_TOGGLE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleLabel = if (isPaused) "RESUME" else "PAUSE"
        val statusText = if (isPaused) {
            "सहनाज AI is paused"
        } else {
            "⚡ सहनाज AI 24x7 Active & Listening..."
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("सहनाज Autonomous JARVIS OS")
            .setContentText(statusText)
            .setSubText(if (isPaused) "Paused" else "Wake-Word Radar ON")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(0, toggleLabel, togglePendingIntent)
            .build()
    }

    companion object {
        private const val TAG = "SAHNAJ_SERVICE"
        private const val CHANNEL_ID = "sahnaj_voice_channel"
        private const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.action.START_SERVICE"
        const val ACTION_PAUSE = "com.example.action.PAUSE_LISTENING"
        const val ACTION_RESUME = "com.example.action.RESUME_LISTENING"
        const val ACTION_TOGGLE = "com.example.action.TOGGLE_LISTENING"
        const val ACTION_STOP = "com.example.action.STOP_SERVICE"

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        private val _isListeningActive = MutableStateFlow(false)
        val isListeningActive: StateFlow<Boolean> = _isListeningActive.asStateFlow()

        fun start(context: Context) {
            try {
                val intent = Intent(context, AssistantVoiceService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "AssistantVoiceService.start dispatched")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start AssistantVoiceService", e)
            }
        }

        fun toggle(context: Context) {
            try {
                val intent = Intent(context, AssistantVoiceService::class.java).apply {
                    action = ACTION_TOGGLE
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle AssistantVoiceService", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, AssistantVoiceService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
                Log.d(TAG, "AssistantVoiceService.stop dispatched")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop AssistantVoiceService", e)
            }
        }
    }
}

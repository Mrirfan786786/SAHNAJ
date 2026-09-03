package com.example.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * SahnajDaemonService:
 * Ultra-resilient 24x7 Background Foreground Service with START_STICKY,
 * continuous hotword detection ("Hi SAHNAJ" / "सहनाज"), Partial WakeLock,
 * haptic vibration acknowledgment, voice feedback ("Yes boss, I'm listening"),
 * and automatic overlay/voice prompt triggering over other apps.
 */
class SahnajDaemonService : Service(), TextToSpeech.OnInitListener {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val handler = Handler(Looper.getMainLooper())

    private var wakeLock: PowerManager.WakeLock? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var isListening = false
    private var isServiceActive = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SahnajDaemonService created")
        createNotificationChannel()
        acquireServiceWakeLock()
        initTTS()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand received action: $action")

        if (action == ACTION_STOP) {
            stopForegroundService()
            return START_NOT_STICKY
        }

        startForegroundWithNotification()
        isServiceActive = true
        _isRunningFlow.value = true

        startHotwordListener()

        // START_STICKY ensures Android OS restarts the service if terminated under memory pressure
        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SahnajDaemonService::class.java).apply {
                this.action = ACTION_STOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SAHNAJ AI Radar Active")
            .setContentText("Listening for 'Hi Sahnaj' // 24x7 Shield Active")
            .setSubText("Radar Active")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop Radar", stopIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } catch (e: Exception) {
                Log.w(TAG, "Starting foreground without specific type fallback", e)
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "SAHNAJ AI Radar Daemon",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "24x7 Persistent Hotword and Wake-Word Detection Radar"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireServiceWakeLock() {
        try {
            if (wakeLock == null) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "sahnaj:DaemonRadarWakeLock"
                )?.apply {
                    setReferenceCounted(false)
                }
            }
            wakeLock?.let {
                if (!it.isHeld) {
                    it.acquire()
                    Log.d(TAG, "Persistent Partial WakeLock acquired for 24x7 radar")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring WakeLock", e)
        }
    }

    private fun releaseServiceWakeLock() {
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "Partial WakeLock released")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing WakeLock", e)
        }
    }

    private fun initTTS() {
        try {
            tts = TextToSpeech(applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let {
                val result = it.setLanguage(Locale("hi", "IN"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    it.setLanguage(Locale.US)
                }
                it.setPitch(1.05f)
                it.setSpeechRate(1.0f)
                isTtsReady = true
                Log.d(TAG, "TTS initialized successfully in SahnajDaemonService")
            }
        }
    }

    private fun startHotwordListener() {
        if (!isServiceActive) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "RECORD_AUDIO permission missing; cannot start hotword listener")
            return
        }

        handler.post {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                        setRecognitionListener(createHotwordRecognitionListener())
                    }
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
                }

                speechRecognizer?.startListening(intent)
                isListening = true
                Log.d(TAG, "Hotword listener started listening")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognizer for hotword", e)
                scheduleListenerRestart(2000)
            }
        }
    }

    private fun createHotwordRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            isListening = false
        }

        override fun onError(error: Int) {
            isListening = false
            // Recoverable speech recognition timeouts and no-matches
            val delayMs = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> 300L
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> 800L
                else -> 1200L
            }
            if (isServiceActive) {
                scheduleListenerRestart(delayMs)
            }
        }

        override fun onResults(results: Bundle?) {
            isListening = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            processMatches(matches)
            if (isServiceActive) {
                scheduleListenerRestart(300)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val matched = processMatches(partialMatches)
            if (matched) {
                try {
                    speechRecognizer?.stopListening()
                } catch (_: Exception) {}
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processMatches(matches: ArrayList<String>?): Boolean {
        if (matches.isNullOrEmpty()) return false

        for (phrase in matches) {
            val clean = phrase.lowercase().trim()
            if (isHotwordTrigger(clean)) {
                Log.d(TAG, "Wake-Word TRIGGERED: '$clean'")
                onWakeWordTriggered()
                return true
            }
        }
        return false
    }

    private fun isHotwordTrigger(text: String): Boolean {
        val triggers = listOf(
            "hi sahnaj", "hey sahnaj", "sahnaj", "shahnaj", "sahnaz", "shahnaz",
            "sah naj", "shah naj", "हाय शहनाज", "सहनाज", "शहनाज", "हेलो शहनाज", "ok sahnaj", "sun sahnaj"
        )
        return triggers.any { text.contains(it) }
    }

    private fun onWakeWordTriggered() {
        serviceScope.launch(Dispatchers.Main) {
            // 1. Vibrate briefly to acknowledge
            performHapticFeedback()

            // 2. Speak via TTS
            speakResponse("Yes boss, I'm listening.")

            // 3. Trigger Glowing Quick-Action Voice Interface / Launch MainActivity with listening prompt
            launchVoiceInterface()
        }
    }

    private fun performHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 150), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 150), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 120, 80, 150), -1)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error performing vibration", e)
        }
    }

    private fun speakResponse(text: String) {
        try {
            if (isTtsReady && tts != null) {
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sahnaj_wake_ack")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking wake ack", e)
        }
    }

    private fun launchVoiceInterface() {
        try {
            // Launch MainActivity with listening trigger
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_START_LISTENING", true)
                putExtra("EXTRA_WAKE_TRIGGERED", true)
            }
            startActivity(intent)

            // Also ensure Floating Cyber Overlay is ready if enabled
            try {
                val overlayIntent = Intent(this, FloatingCyberOverlayService::class.java)
                startService(overlayIntent)
            } catch (_: Exception) {}

        } catch (e: Exception) {
            Log.e(TAG, "Error launching voice interface", e)
        }
    }

    private fun scheduleListenerRestart(delayMs: Long) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            if (isServiceActive) {
                startHotwordListener()
            }
        }, delayMs)
    }

    private fun stopForegroundService() {
        isServiceActive = false
        _isRunningFlow.value = false
        handler.removeCallbacksAndMessages(null)

        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w(TAG, "Error destroying speech recognizer", e)
        }

        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.w(TAG, "Error shutting down TTS", e)
        }

        releaseServiceWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopForegroundService()
        serviceScope.cancel()
        super.onDestroy()
        Log.d(TAG, "SahnajDaemonService destroyed cleanly")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "SahnajDaemonService"
        private const val CHANNEL_ID = "sahnaj_radar_daemon_channel"
        private const val NOTIFICATION_ID = 9921

        const val ACTION_START = "com.example.action.START_DAEMON_RADAR"
        const val ACTION_STOP = "com.example.action.STOP_DAEMON_RADAR"

        private val _isRunningFlow = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()

        fun start(context: Context) {
            try {
                val intent = Intent(context, SahnajDaemonService::class.java).apply {
                    action = ACTION_START
                }
                ContextCompat.startForegroundService(context, intent)
                Log.d(TAG, "Dispatched START_DAEMON_RADAR to SahnajDaemonService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SahnajDaemonService", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, SahnajDaemonService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
                Log.d(TAG, "Dispatched STOP_DAEMON_RADAR to SahnajDaemonService")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop SahnajDaemonService", e)
            }
        }
    }
}

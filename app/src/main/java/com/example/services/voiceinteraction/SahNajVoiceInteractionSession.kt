package com.example.services.voiceinteraction

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.util.Log
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.SahNajApplication
import com.example.data.model.ActionType
import com.example.data.model.CommandEntity
import com.example.data.model.ExecutionResult
import com.example.data.model.ResultStatus
import com.example.domain.personality.SmartHumanEngine
import com.example.data.model.RiskLevel
import com.example.data.model.StructuredAction
import com.example.domain.personality.PersonalityResponses
import com.example.domain.validator.ValidationResult
import com.example.presentation.components.HolographicVoiceOrb
import com.example.presentation.viewmodel.AssistantStatus
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.util.PipelineLatencyTracker
import com.example.util.TechSoundManager
import com.example.voice.SpeechState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class SahNajVoiceInteractionSession(context: Context) : VoiceInteractionSession(context) {

    companion object {
        private const val TAG = "SAHNAJ_VOICE_SESSION"
    }

    private val sessionScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val sessionLifecycleOwner = AssistantSessionLifecycleOwner()

    private val _sessionStatus = MutableStateFlow<AssistantStatus>(AssistantStatus.Listening)
    private val _transcription = MutableStateFlow("")
    private val _aiResponseText = MutableStateFlow("")
    private val _pendingAction = MutableStateFlow<StructuredAction?>(null)

    private var speechCollectJob: Job? = null
    private var pendingWhatsAppRecipient: String? = null
    private val latencyTracker = PipelineLatencyTracker()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SahNajVoiceInteractionSession onCreate")
        sessionLifecycleOwner.performRestore(null)
        sessionLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateContentView(): View {
        Log.d(TAG, "SahNajVoiceInteractionSession onCreateContentView")
        sessionLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        sessionLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setViewTreeLifecycleOwner(sessionLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(sessionLifecycleOwner)
            setViewTreeViewModelStoreOwner(sessionLifecycleOwner)
            setContent {
                SahNajSessionOverlayUi(
                    sessionStatusFlow = _sessionStatus,
                    transcriptionFlow = _transcription,
                    aiResponseFlow = _aiResponseText,
                    pendingActionFlow = _pendingAction,
                    onDismiss = { hideSession() },
                    onConfirmAction = { confirmPendingAction() },
                    onDeclineAction = { declinePendingAction() },
                    onMicClick = { startListening() }
                )
            }
        }

        return composeView
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        Log.d(TAG, "SahNajVoiceInteractionSession onShow. showFlags=$showFlags, args=$args")
        sessionLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        _sessionStatus.value = AssistantStatus.Listening
        _transcription.value = ""
        _aiResponseText.value = ""
        _pendingAction.value = null
        pendingWhatsAppRecipient = null

        // Play subtle assistant activation sound
        sessionScope.launch {
            TechSoundManager.playWakeTriggerSound(context)
        }

        // Start listening
        startListening()
    }

    override fun onHandleAssist(data: Bundle?, structure: AssistStructure?, content: AssistContent?) {
        super.onHandleAssist(data, structure, content)
        Log.d(TAG, "onHandleAssist received screen structure")
    }

    private fun startListening() {
        val app = context.applicationContext as? SahNajApplication ?: return
        val langCode = when (app.userPreferences.getLanguage().lowercase()) {
            "english" -> "en-US"
            "hindi" -> "hi-IN"
            else -> "hi-IN"
        }

        app.textToSpeechManager.stop()
        _sessionStatus.value = AssistantStatus.Listening
        _transcription.value = ""

        speechCollectJob?.cancel()
        speechCollectJob = sessionScope.launch {
            app.speechRecognizerManager.speechState.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _sessionStatus.value = AssistantStatus.Listening
                    }
                    is SpeechState.Partial -> {
                        _transcription.value = state.text
                    }
                    is SpeechState.Result -> {
                        _transcription.value = state.text
                        processUserVoiceQuery(state.text)
                    }
                    is SpeechState.Error -> {
                        Log.w(TAG, "SpeechRecognizer error: ${state.message}")
                        if (_sessionStatus.value is AssistantStatus.Listening) {
                            _sessionStatus.value = AssistantStatus.Error(state.message)
                        }
                    }
                    else -> {}
                }
            }
        }

        Log.d(TAG, "Starting SpeechRecognizer with langCode=$langCode")
        app.speechRecognizerManager.startListening(languageCode = langCode, continuous = false)
    }

    private fun processUserVoiceQuery(rawSpeech: String) {
        val cleanSpeech = rawSpeech.trim()
        if (cleanSpeech.isBlank()) {
            return
        }

        val app = context.applicationContext as? SahNajApplication ?: return
        val assistantName = app.userPreferences.getAssistantName()
        latencyTracker.startPipeline()
        latencyTracker.markSttReceived(cleanSpeech)

        // If awaiting WhatsApp message text follow-up
        val recipient = pendingWhatsAppRecipient
        if (!recipient.isNullOrBlank()) {
            pendingWhatsAppRecipient = null
            val action = StructuredAction(
                action = ActionType.SEND_WHATSAPP_MESSAGE,
                target = recipient,
                parameters = mapOf("message" to cleanSpeech, "send" to "true"),
                requiresConfirmation = true,
                spokenResponse = "$recipient ko '$cleanSpeech' bhejun?",
                rawPrompt = cleanSpeech,
                riskLevel = RiskLevel.HIGH
            )
            handleStructuredAction(action)
            return
        }

        // If awaiting confirmation
        if (app.confirmationManager.hasPendingAction) {
            val pending = app.confirmationManager.currentPendingAction!!
            if (app.confirmationManager.isConfirmationAffirmative(cleanSpeech)) {
                app.confirmationManager.clearPendingAction()
                executeAction(pending)
                return
            } else if (app.confirmationManager.isConfirmationNegative(cleanSpeech)) {
                app.confirmationManager.clearPendingAction()
                _pendingAction.value = null
                speakAndDisplay("Action cancel kar diya.") {
                    sessionScope.launch {
                        delay(1500)
                        hideSession()
                    }
                }
                return
            }
        }

        _sessionStatus.value = AssistantStatus.Thinking
        sessionScope.launch {
            // Local fast memory fact saving
            app.userMemoryRepository.extractAndSaveLocalFacts(cleanSpeech)

            // 1. Try local rule-based parser
            val localParsed = app.localCommandParser.parseLocally(cleanSpeech, assistantName)
            if (localParsed != null) {
                latencyTracker.markParserComplete("Local Rule Parser")
                handleStructuredAction(localParsed)
                return@launch
            }

            TechSoundManager.playThinkingChirp(context)

            // 2. Fallback to Gemini AI Core with contextual memories
            val memoryContext = app.userMemoryRepository.getMemoriesForPrompt()
            val recentList = app.commandHistoryRepository.getRecentHistory(2).firstOrNull() ?: emptyList()
            val shortRecentTurn = if (recentList.isNotEmpty()) {
                recentList.reversed().joinToString("; ") { item ->
                    "Turn: \"${item.commandText}\" -> \"${item.spokenResponse}\""
                }
            } else ""

            val combinedContext = buildString {
                if (memoryContext.isNotBlank()) append(memoryContext)
                if (shortRecentTurn.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("Recent Turns: ").append(shortRecentTurn)
                }
            }

            val result = app.geminiRepository.parseCommand(cleanSpeech, assistantName, combinedContext)
            result.onSuccess { action ->
                latencyTracker.markParserComplete("Gemini AI")
                if (action.extractedMemories.isNotEmpty()) {
                    app.userMemoryRepository.saveExtractedMemories(action.extractedMemories)
                }
                handleStructuredAction(action)
            }.onFailure { error ->
                latencyTracker.markParserComplete("Smart Human Engine (Zero-API)")
                Log.w(TAG, "Gemini unavailable (${error.message}), seamlessly engaging Smart Human Engine...")
                val smartReply = com.example.domain.personality.SmartHumanEngine.generateSmartReply(cleanSpeech, assistantName)
                val fallbackAction = StructuredAction(
                    action = ActionType.SPEAK_TEXT,
                    target = "SMART_HUMAN_ENGINE",
                    spokenResponse = smartReply,
                    rawPrompt = cleanSpeech,
                    riskLevel = RiskLevel.LOW
                )
                handleStructuredAction(fallbackAction)
            }
        }
    }

    private fun handleStructuredAction(action: StructuredAction) {
        val app = context.applicationContext as? SahNajApplication ?: return

        // If follow-up message text required
        if ((action.action == ActionType.SEND_WHATSAPP_MESSAGE || action.action == ActionType.WHATSAPP_MESSAGE) && action.parameters["prompt_text"] == "true") {
            pendingWhatsAppRecipient = action.target
            val promptMsg = "Kya likhna hai?"
            _pendingAction.value = null
            speakAndDisplay(promptMsg) {
                startListening()
            }
            return
        }

        // Validate
        val validation = app.actionValidator.validate(action)
        if (validation is ValidationResult.Invalid) {
            val isSensitiveCredential = validation.reason.contains("passwords", ignoreCase = true) ||
                    validation.reason.contains("financial secrets", ignoreCase = true) ||
                    validation.reason.contains("PIN", ignoreCase = false)
            if (isSensitiveCredential) {
                _sessionStatus.value = AssistantStatus.Error(validation.reason)
                speakAndDisplay(validation.reason) {
                    sessionScope.launch {
                        delay(2000)
                        hideSession()
                    }
                }
                return
            }

            // Conversational Route:
            // If any automation action is not an OS-level command, feed the query directly to Gemini (gemini-3.6-flash)
            val queryText = action.rawPrompt.ifBlank { action.spokenResponse }.ifBlank { action.target }
            if (action.spokenResponse.isNotBlank() && action.action != ActionType.UNKNOWN) {
                speakAndDisplay(action.spokenResponse)
            } else {
                sessionScope.launch {
                    val geminiResult = app.geminiRepository.answerConversational(queryText, "SAHNAJ")
                    val answer = geminiResult.getOrElse {
                        SmartHumanEngine.generateSmartReply(queryText, "boss")
                    }
                    speakAndDisplay(answer)
                }
            }
            return
        }

        // Confirmation required
        if (app.confirmationManager.requiresConfirmation(action)) {
            app.confirmationManager.setPendingAction(action)
            _pendingAction.value = action
            _sessionStatus.value = AssistantStatus.AwaitingConfirmation(action)
            val promptText = action.spokenResponse.ifEmpty { "Kya aap ${action.target} par aage badhna chahte hain?" }
            speakAndDisplay(promptText)
            return
        }

        // Execute immediately
        executeAction(action)
    }

    private fun executeAction(action: StructuredAction) {
        val app = context.applicationContext as? SahNajApplication ?: return
        _pendingAction.value = null

        sessionScope.launch {
            val result = app.actionExecutor.execute(action)
            _sessionStatus.value = AssistantStatus.ActionExecuted(result, action)
            val replyText = result.spokenResponse.ifEmpty { action.spokenResponse }
            
            // Save to command history
            app.commandHistoryRepository.addCommand(
                CommandEntity(
                    commandText = action.rawPrompt,
                    actionType = action.action.name,
                    target = action.target,
                    status = if (result.status == ResultStatus.SUCCESS) "SUCCESS" else "FAILED",
                    spokenResponse = replyText,
                    timestamp = System.currentTimeMillis()
                )
            )

            speakAndDisplay(replyText) {
                // Auto dismiss session after completing action unless user wants more
                sessionScope.launch {
                    delay(2000)
                    hideSession()
                }
            }
        }
    }

    private fun confirmPendingAction() {
        val app = context.applicationContext as? SahNajApplication ?: return
        val pending = app.confirmationManager.currentPendingAction ?: _pendingAction.value ?: return
        app.confirmationManager.clearPendingAction()
        executeAction(pending)
    }

    private fun declinePendingAction() {
        val app = context.applicationContext as? SahNajApplication ?: return
        app.confirmationManager.clearPendingAction()
        _pendingAction.value = null
        speakAndDisplay("Action cancel kar diya.") {
            sessionScope.launch {
                delay(1500)
                hideSession()
            }
        }
    }

    private fun speakAndDisplay(text: String, onComplete: (() -> Unit)? = null) {
        _aiResponseText.value = text
        _sessionStatus.value = AssistantStatus.Speaking(text)
        val app = context.applicationContext as? SahNajApplication
        app?.textToSpeechManager?.speak(text, app.userPreferences.getSpeechRate()) {
            onComplete?.invoke()
        }
    }

    private fun hideSession() {
        try {
            val app = context.applicationContext as? SahNajApplication
            app?.speechRecognizerManager?.stopListening()
            app?.textToSpeechManager?.stop()
            hide()
        } catch (e: Exception) {
            Log.e(TAG, "Error hiding session: ${e.message}")
        }
    }

    override fun onHide() {
        super.onHide()
        Log.d(TAG, "SahNajVoiceInteractionSession onHide")
        speechCollectJob?.cancel()
        val app = context.applicationContext as? SahNajApplication
        app?.speechRecognizerManager?.stopListening()
        app?.textToSpeechManager?.stop()
        app?.confirmationManager?.clearPendingAction()
        sessionLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        sessionLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "SahNajVoiceInteractionSession onDestroy")
        sessionLifecycleOwner.onDestroy()
        sessionScope.cancel()
    }
}

@Composable
fun SahNajSessionOverlayUi(
    sessionStatusFlow: MutableStateFlow<AssistantStatus>,
    transcriptionFlow: MutableStateFlow<String>,
    aiResponseFlow: MutableStateFlow<String>,
    pendingActionFlow: MutableStateFlow<StructuredAction?>,
    onDismiss: () -> Unit,
    onConfirmAction: () -> Unit,
    onDeclineAction: () -> Unit,
    onMicClick: () -> Unit
) {
    val status by sessionStatusFlow.collectAsState()
    val transcription by transcriptionFlow.collectAsState()
    val aiResponse by aiResponseFlow.collectAsState()
    val pendingAction by pendingActionFlow.collectAsState()

    val app = SahNajApplication.instance
    val rmsDb by app.speechRecognizerManager.rmsDb.collectAsState()
    val isSpeaking by app.textToSpeechManager.isSpeaking.collectAsState()

    // Full screen overlay with semi-transparent backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Futuristic Bottom Assistant Sheet
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent click through */ }
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140E1B)),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberRedBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Bar: Drag handle & Title & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CyberRedBright)
                        )
                        Text(
                            text = PersonalityResponses.ASSISTANT_NAME_DISPLAY.uppercase() + " AI",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberRedBright,
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Central Holographic Voice Orb
                HolographicVoiceOrb(
                    status = status,
                    rmsDb = rmsDb,
                    isSpeaking = isSpeaking,
                    onClick = onMicClick,
                    orbSize = 180.dp
                )

                // Status & Transcription display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val statusText = when (status) {
                        is AssistantStatus.Listening -> "Listening..."
                        is AssistantStatus.Thinking -> "Processing..."
                        is AssistantStatus.Speaking -> "Responding..."
                        is AssistantStatus.AwaitingConfirmation -> "Confirmation Required"
                        is AssistantStatus.ActionExecuted -> "Action Complete"
                        is AssistantStatus.Error -> "Attention"
                        else -> "Ready"
                    }
                    val statusColor = when (status) {
                        is AssistantStatus.Listening -> CyberRedBright
                        is AssistantStatus.Thinking -> CyberAmber
                        is AssistantStatus.Speaking -> CyberGreen
                        is AssistantStatus.AwaitingConfirmation -> CyberAmber
                        is AssistantStatus.Error -> CyberRed
                        else -> CyberTextSecondary
                    }

                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )

                    if (transcription.isNotBlank()) {
                        Text(
                            text = "\"$transcription\"",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberTextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    if (aiResponse.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F1429)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B1E4A)),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Text(
                                text = aiResponse,
                                fontSize = 14.5.sp,
                                color = CyberTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(14.dp).fillMaxWidth()
                            )
                        }
                    }
                }

                // Confirmation buttons if pending action
                if (pendingAction != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDeclineAction,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextPrimary)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onConfirmAction,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Confirm", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

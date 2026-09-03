package com.example.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.SahNajAccessibilityService
import com.example.presentation.components.ErrorBannerData
import com.example.presentation.components.CyberErrorBanner
import com.example.data.local.UserPreferences
import com.example.data.model.ActionType
import com.example.data.model.CommandEntity
import com.example.data.model.ExecutionResult
import com.example.data.model.ResultStatus
import com.example.data.model.RiskLevel
import com.example.data.model.StructuredAction
import com.example.data.repository.CommandHistoryRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.UserMemoryRepository
import com.example.domain.confirmation.ConfirmationManager
import com.example.domain.executor.ActionExecutor
import com.example.domain.parser.LocalCommandParser
import com.example.domain.personality.PersonalityResponses
import com.example.domain.personality.SmartHumanEngine
import com.example.domain.validator.ActionValidator
import com.example.domain.validator.ValidationResult
import com.example.services.AssistantVoiceService
import com.example.services.AssistantWakeLockManager
import com.example.permissions.PermissionManager
import com.example.util.PipelineLatencyTracker
import com.example.util.TechSoundManager
import com.example.voice.SpeechRecognizerManager
import com.example.voice.SpeechState
import com.example.voice.TextToSpeechManager
import com.example.voice.WakeWordEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class AssistantStatus {
    data object Idle : AssistantStatus()
    data object Listening : AssistantStatus()
    data object Thinking : AssistantStatus()
    data class Speaking(val text: String) : AssistantStatus()
    data class AwaitingConfirmation(val action: StructuredAction) : AssistantStatus()
    data class ActionExecuted(val result: ExecutionResult, val action: StructuredAction) : AssistantStatus()
    data class Error(val message: String) : AssistantStatus()
}

class AssistantViewModel(
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager,
    private val wakeWordEngine: WakeWordEngine,
    private val localCommandParser: LocalCommandParser,
    private val geminiRepository: GeminiRepository,
    private val actionValidator: ActionValidator,
    private val confirmationManager: ConfirmationManager,
    private val actionExecutor: ActionExecutor,
    private val commandHistoryRepository: CommandHistoryRepository,
    private val userMemoryRepository: UserMemoryRepository,
    private val userPreferences: UserPreferences,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _status = MutableStateFlow<AssistantStatus>(AssistantStatus.Idle)
    val status: StateFlow<AssistantStatus> = _status.asStateFlow()

    private val _lastUserSpeech = MutableStateFlow("")
    val lastUserSpeech: StateFlow<String> = _lastUserSpeech.asStateFlow()

    private val _lastAiResponse = MutableStateFlow("")
    val lastAiResponse: StateFlow<String> = _lastAiResponse.asStateFlow()

    private val _currentAction = MutableStateFlow<StructuredAction?>(null)
    val currentAction: StateFlow<StructuredAction?> = _currentAction.asStateFlow()

    private val _activeErrorBanner = MutableStateFlow<ErrorBannerData?>(null)
    val activeErrorBanner: StateFlow<ErrorBannerData?> = _activeErrorBanner.asStateFlow()

    val speechState: StateFlow<SpeechState> = speechRecognizerManager.speechState
    val rmsDb: StateFlow<Float> = speechRecognizerManager.rmsDb
    val partialText: StateFlow<String> = speechRecognizerManager.partialText
    val isSpeaking: StateFlow<Boolean> = textToSpeechManager.isSpeaking

    val isAccessibilityActive: StateFlow<Boolean> = SahNajAccessibilityService.isServiceActive
    val isBackgroundServiceActive: StateFlow<Boolean> = AssistantVoiceService.isServiceActive
    val isFloatingOverlayActive: StateFlow<Boolean> = com.example.services.FloatingCyberOverlayService.isOverlayActive
    val assistantName: StateFlow<String> = userPreferences.assistantName
    val isWakeWordActive: StateFlow<Boolean> = wakeWordEngine.isListeningForWakeWord
    val aiModel: StateFlow<String> = userPreferences.aiModel
    val providerKeysFlow: StateFlow<Map<String, String>> = userPreferences.providerKeysFlow

    fun toggleBackgroundService(context: android.content.Context) {
        if (isBackgroundServiceActive.value) {
            AssistantVoiceService.stop(context)
        } else {
            AssistantVoiceService.start(context)
        }
    }

    fun toggleFloatingOverlay(context: android.content.Context) {
        com.example.services.FloatingCyberOverlayService.toggle(context)
    }

    fun requestBatteryOptimizationExemption(context: android.content.Context): Boolean {
        return com.example.util.BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
    }

    fun setAiModel(model: String) {
        userPreferences.setAiModel(model)
    }

    private val latencyTracker = PipelineLatencyTracker()
    private var pendingWhatsAppRecipient: String? = null

    init {
        Log.d(TAG, "[STAGE 1: MIC / PERMISSION] AssistantViewModel initialized")
        viewModelScope.launch {
            speechRecognizerManager.speechState.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _status.value = AssistantStatus.Listening
                    }
                    is SpeechState.Partial -> {
                        _lastUserSpeech.value = state.text
                    }
                    is SpeechState.Processing -> {
                        _status.value = AssistantStatus.Thinking
                    }
                    is SpeechState.Result -> {
                        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Speech received: \"${state.text}\"")
                        handleSpeechResult(state.text)
                    }
                    is SpeechState.Error -> {
                        Log.w(TAG, "[STAGE 3: STT (Speech-to-Text)] STT Error: ${state.message} (code: ${state.errorCode})")
                        if (!wakeWordEngine.isListeningForWakeWord.value) {
                            _status.value = AssistantStatus.Error(state.message)
                            showErrorBanner(
                                message = state.message,
                                canRetry = true,
                                onRetry = { startListening() }
                            )
                        }
                    }
                    is SpeechState.Idle -> {
                        if (_status.value is AssistantStatus.Listening) {
                            _status.value = AssistantStatus.Idle
                        }
                    }
                }
            }
        }

        // Silent periodic background cleanup of old conversation summaries (> 30 days)
        viewModelScope.launch {
            userMemoryRepository.cleanupOldSummaries(30)
        }
    }

    /**
     * Starts active voice listening (User tapped mic).
     */
    fun startListening() {
        val permStatus = permissionManager.getPermissionStatus()
        Log.d(TAG, "[STAGE 1: MIC / PERMISSION] startListening invoked. hasMic=${permStatus.hasMic}")
        if (!permStatus.hasMic) {
            val errMsg = "Microphone permission is required for voice commands"
            Log.e(TAG, "[STAGE 1: MIC / PERMISSION] ERROR: $errMsg")
            _status.value = AssistantStatus.Error(errMsg)
            showErrorBanner(
                message = "माइक्रोफ़ोन अनुमति नहीं मिली। कृपया अनुमति दें।",
                canRetry = true,
                actionLabel = "अनुमति दें",
                onRetry = { startListening() }
            )
            return
        }

        wakeWordEngine.setListeningForWakeWord(false)
        textToSpeechManager.stop()
        val langCode = getLanguageCode()
        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Starting direct speech recognition with langCode=$langCode")
        speechRecognizerManager.startListening(languageCode = langCode, continuous = false)
    }

    /**
     * Starts continuous wake-word detection (e.g. listening for "Sahnaj").
     */
    fun startWakeWordListening() {
        val permStatus = permissionManager.getPermissionStatus()
        Log.d(TAG, "[STAGE 2: WAKE-WORD] startWakeWordListening invoked. hasMic=${permStatus.hasMic}")
        if (!permStatus.hasMic) {
            Log.w(TAG, "[STAGE 1: MIC / PERMISSION] Cannot start wake word without mic permission")
            return
        }

        wakeWordEngine.setListeningForWakeWord(true)
        val langCode = getLanguageCode()
        Log.d(TAG, "[STAGE 2: WAKE-WORD] Starting continuous listening for wake-word with langCode=$langCode")
        speechRecognizerManager.startListening(languageCode = langCode, continuous = true)
    }

    fun stopListening() {
        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Stopping speech recognizer")
        wakeWordEngine.setListeningForWakeWord(false)
        speechRecognizerManager.stopListening()
        _status.value = AssistantStatus.Idle
    }

    fun cancelAction() {
        Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Action cancelled by user")
        textToSpeechManager.stop()
        speechRecognizerManager.stopListening()
        confirmationManager.clearPendingAction()
        AssistantWakeLockManager.releaseWakeLock()
        _status.value = AssistantStatus.Idle
        _currentAction.value = null
    }

    fun confirmPendingAction() {
        val pending = confirmationManager.currentPendingAction ?: return
        Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Action confirmed by user: ${pending.action}")
        confirmationManager.clearPendingAction()
        executeStructuredAction(pending)
    }

    fun declinePendingAction() {
        Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Action declined by user")
        confirmationManager.clearPendingAction()
        _status.value = AssistantStatus.Idle
        _currentAction.value = null
        speakResponse("Theek hai, action cancel kar diya.")
    }

    fun processDirectTextCommand(text: String) {
        if (text.isBlank()) return
        Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] processDirectTextCommand: \"$text\"")
        wakeWordEngine.setListeningForWakeWord(false)
        handleSpeechResult(text)
    }

    private fun handleSpeechResult(rawSpeech: String) {
        val cleanSpeech = rawSpeech.trim()
        if (cleanSpeech.isBlank()) {
            _status.value = AssistantStatus.Idle
            return
        }

        val name = userPreferences.getAssistantName()
        val inWakeWordMode = wakeWordEngine.isListeningForWakeWord.value

        // Check wake word if continuous mode is enabled
        if (inWakeWordMode) {
            val isWakeMatched = wakeWordEngine.matchesWakeWord(cleanSpeech, name)
            Log.d(TAG, "[STAGE 2: WAKE-WORD] Testing speech \"$cleanSpeech\" against wake word \"$name\": matched=$isWakeMatched")
            if (!isWakeMatched) {
                // Ignore ambient noise/speech and continue listening
                return
            }

            // Wake word detected! Start latency tracker and wake lock
            latencyTracker.startPipeline()
            latencyTracker.markWakeWordDetected()
            AssistantWakeLockManager.acquireWakeLock(com.example.SahNajApplication.instance, 10000L)

            // Play quick audio feedback ping
            viewModelScope.launch {
                TechSoundManager.playWakeTriggerSound(com.example.SahNajApplication.instance)
            }

            val commandPortion = wakeWordEngine.stripWakeWord(cleanSpeech, name)
            Log.d(TAG, "[STAGE 2: WAKE-WORD] Wake-word detected! Residual command: \"$commandPortion\"")

            if (commandPortion.isBlank()) {
                // User just called "Sahnaj" -> Respond with friendly greeting and listen for follow-up command
                val currentUserName = userPreferences.getUserDisplayName()
                val useNamePref = userPreferences.isUseNameWhenSpeaking()
                val wakeGreeting = PersonalityResponses.getRandomWakeWordGreeting(
                    userName = currentUserName,
                    useNameWhenSpeaking = useNamePref
                )
                Log.d(TAG, "[STAGE 6: TTS (Text-to-Speech)] Speaking wake greeting: \"$wakeGreeting\"")
                _status.value = AssistantStatus.Speaking(wakeGreeting)
                _lastAiResponse.value = wakeGreeting
                latencyTracker.markTtsStarted()
                textToSpeechManager.speak(wakeGreeting, userPreferences.getSpeechRate()) {
                    // Start listening for the user's command immediately after speaking greeting
                    Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Wake greeting complete, starting command listening")
                    startListening()
                }
                return
            } else {
                // User spoke "Sahnaj WhatsApp kholo" in one phrase -> execute command portion
                wakeWordEngine.setListeningForWakeWord(false)
                executeParsedPipeline(commandPortion, name)
                return
            }
        }

        executeParsedPipeline(cleanSpeech, name)
    }

    private fun executeParsedPipeline(cleanSpeech: String, name: String) {
        _lastUserSpeech.value = cleanSpeech
        latencyTracker.markSttReceived(cleanSpeech)
        Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Executing pipeline for input: \"$cleanSpeech\"")

        // If awaiting WhatsApp message text
        val recipient = pendingWhatsAppRecipient
        if (!recipient.isNullOrBlank()) {
            pendingWhatsAppRecipient = null
            val action = StructuredAction(
                action = ActionType.WHATSAPP_MESSAGE,
                target = recipient,
                parameters = mapOf("message" to cleanSpeech, "send" to "true"),
                requiresConfirmation = true,
                spokenResponse = "$recipient ko '$cleanSpeech' bhejun?",
                rawPrompt = cleanSpeech,
                riskLevel = RiskLevel.HIGH
            )
            processStructuredAction(action, isGemini = false)
            return
        }

        // If currently awaiting confirmation
        if (confirmationManager.hasPendingAction) {
            val pending = confirmationManager.currentPendingAction!!
            if (confirmationManager.isConfirmationAffirmative(cleanSpeech)) {
                Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Affirmative confirmation detected for: ${pending.action}")
                confirmationManager.clearPendingAction()
                executeStructuredAction(pending)
                return
            } else if (confirmationManager.isConfirmationNegative(cleanSpeech)) {
                Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Negative confirmation detected for: ${pending.action}")
                confirmationManager.clearPendingAction()
                _status.value = AssistantStatus.Idle
                _currentAction.value = null
                speakResponse("Action cancel kar diya.")
                return
            }
        }

        _status.value = AssistantStatus.Thinking
        viewModelScope.launch {
            // Local fast fact/preference/name extraction in background (invisible)
            userMemoryRepository.extractAndSaveLocalFacts(cleanSpeech)

            // Autonomous In-App Visual & Web Grounding Engine Check
            if (com.example.presentation.components.VisualSearchEngine.isVisualOrGroundingQuery(cleanSpeech)) {
                Log.d(TAG, "[STAGE 4: VISUAL GROUNDING] Intercepting visual/web query: \"$cleanSpeech\"")
                val visualResult = com.example.presentation.components.VisualSearchEngine.performVisualSearch(
                    rawQuery = cleanSpeech,
                    context = com.example.SahNajApplication.instance
                )
                val spokenTts = if (visualResult.groundingType == com.example.presentation.components.GroundingType.VISUAL_IMAGE && !visualResult.imageUrl.isNullOrBlank()) {
                    "Boss, ${visualResult.title} ka verified visual aur details screen par render kar diya hai."
                } else {
                    "Boss, ${visualResult.title} ke verified facts aur summary screen par display kar diye hain."
                }

                _lastAiResponse.value = visualResult.snippet
                val action = StructuredAction(
                    action = ActionType.SPEAK_TEXT,
                    target = "VISUAL_GROUNDING_ENGINE",
                    spokenResponse = spokenTts,
                    rawPrompt = cleanSpeech,
                    riskLevel = RiskLevel.LOW
                )
                processStructuredAction(action, isGemini = false)
                return@launch
            }

            // 1. Try local rule-based parser first (fast & offline)
            val localParsed = localCommandParser.parseLocally(cleanSpeech, name)
            if (localParsed != null) {
                latencyTracker.markParserComplete("Local Rule Parser")
                Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] LocalCommandParser matched action: ${localParsed.action} target: ${localParsed.target}")
                processStructuredAction(localParsed, isGemini = false)
                return@launch
            }

            // Play fast thinking ping
            TechSoundManager.playThinkingChirp(com.example.SahNajApplication.instance)

            // 2. Fetch persistent invisible memory + recent context from Room Database
            val memoryContext = userMemoryRepository.getMemoriesForPrompt()
            val recentList = commandHistoryRepository.getRecentHistory(2).firstOrNull() ?: emptyList()
            val shortRecentTurn = if (recentList.isNotEmpty()) {
                recentList.reversed().joinToString("; ") { item ->
                    "Turn: \"${item.commandText}\" -> \"${item.spokenResponse}\""
                }
            } else ""

            val combinedContext = buildString {
                if (memoryContext.isNotBlank()) {
                    append(memoryContext)
                }
                if (shortRecentTurn.isNotBlank()) {
                    if (isNotEmpty()) append("\n")
                    append("Recent Turns: ").append(shortRecentTurn)
                }
            }

            // 3. Fallback to Gemini AI for natural language reasoning with contextual memory
            Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Forwarding query to Gemini AI Core with memory context...")
            val geminiResult = geminiRepository.parseCommand(cleanSpeech, name, combinedContext)
            geminiResult.onSuccess { structuredAction ->
                latencyTracker.markParserComplete("Gemini Flash AI")
                Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Gemini parsed action: ${structuredAction.action}, response: \"${structuredAction.spokenResponse}\"")
                
                // Silently persist any extracted memories in background (Room DB)
                if (structuredAction.extractedMemories.isNotEmpty()) {
                    userMemoryRepository.saveExtractedMemories(structuredAction.extractedMemories)
                }
                if (!structuredAction.conversationSummary.isNullOrBlank()) {
                    userMemoryRepository.saveConversationSummary(cleanSpeech, structuredAction.spokenResponse)
                }

                processStructuredAction(structuredAction, isGemini = true)
            }.onFailure { error ->
                latencyTracker.markParserComplete("Smart Human Engine (Zero-API)")
                Log.w(TAG, "[STAGE 4: PARSER / SMART HUMAN ENGINE] Gemini unavailable (${error.message}), seamlessly engaging Smart Human Engine fallback...")
                
                val smartReply = SmartHumanEngine.generateSmartReply(cleanSpeech, name)
                val fallbackAction = StructuredAction(
                    action = ActionType.SPEAK_TEXT,
                    target = "SMART_HUMAN_ENGINE",
                    spokenResponse = smartReply,
                    rawPrompt = cleanSpeech,
                    riskLevel = RiskLevel.LOW
                )
                processStructuredAction(fallbackAction, isGemini = false)
            }
        }
    }

    private fun processStructuredAction(action: StructuredAction, isGemini: Boolean) {
        _currentAction.value = action

        // If command requires follow-up message text from user (e.g. "WhatsApp par Mammi ko message karo")
        if ((action.action == ActionType.SEND_WHATSAPP_MESSAGE || action.action == ActionType.WHATSAPP_MESSAGE || action.action == ActionType.SEND_WHATSAPP) && action.parameters["prompt_text"] == "true") {
            pendingWhatsAppRecipient = action.target
            val promptQuestion = "Kya likhna hai?"
            _status.value = AssistantStatus.Speaking(promptQuestion)
            _lastAiResponse.value = promptQuestion
            textToSpeechManager.speak(promptQuestion, userPreferences.getSpeechRate()) {
                startListening()
            }
            return
        }

        // Validate action against whitelist & safety guardrails
        val validation = actionValidator.validate(action)
        Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Action validation: $validation")
        if (validation is ValidationResult.Invalid) {
            // Strictly preserve safety for typing sensitive passwords / bank PINs
            val isSensitiveCredential = validation.reason.contains("passwords", ignoreCase = true) ||
                    validation.reason.contains("financial secrets", ignoreCase = true) ||
                    validation.reason.contains("PIN", ignoreCase = false)
            if (isSensitiveCredential) {
                _status.value = AssistantStatus.Error(validation.reason)
                speakResponse(validation.reason)
                return
            }

            // Conversational Route:
            // If any automation action is not an OS-level command, feed the query directly to Gemini (gemini-3.6-flash)
            // so it answers intelligently instead of throwing safety prohibition errors.
            Log.d(TAG, "[CONVERSATIONAL ROUTE] Non-OS action '${action.action}' routed to Gemini conversational engine...")
            val queryText = action.rawPrompt.ifBlank { action.spokenResponse }.ifBlank { action.target }
            if (action.spokenResponse.isNotBlank() && action.action != ActionType.UNKNOWN) {
                _status.value = AssistantStatus.Idle
                _lastAiResponse.value = action.spokenResponse
                speakResponse(action.spokenResponse)
            } else {
                handleConversationalFallback(queryText)
            }
            return
        }

        // Check if confirmation is required (e.g. Phone Calls, SMS, High Risk actions)
        if (confirmationManager.requiresConfirmation(action)) {
            Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Action requires user confirmation: ${action.action}")
            confirmationManager.setPendingAction(action)
            _status.value = AssistantStatus.AwaitingConfirmation(action)
            val promptText = action.spokenResponse.ifEmpty { "Kya aap ${action.target} par aage badhna chahte hain?" }
            speakResponse(promptText)
            return
        }

        // Execute immediately for low risk actions
        executeStructuredAction(action, isGemini)
    }

    private fun executeStructuredAction(action: StructuredAction, isGemini: Boolean = false) {
        viewModelScope.launch {
            Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Executing action: ${action.action}, target: ${action.target}")
            val result = actionExecutor.execute(action)
            latencyTracker.markActionExecuted(action.action.name)
            Log.d(TAG, "[STAGE 5: ACTION EXECUTOR] Action execution result: ${result.status}, response: \"${result.spokenResponse}\"")
            _status.value = AssistantStatus.ActionExecuted(result, action)
            _lastAiResponse.value = result.spokenResponse

            // Save to Room Command History
            commandHistoryRepository.addCommand(
                CommandEntity(
                    commandText = action.rawPrompt,
                    actionType = action.action.name,
                    target = action.target,
                    status = result.status.name,
                    spokenResponse = result.spokenResponse,
                    isGemini = isGemini
                )
            )

            // Speak outcome response via TTS
            if (result.spokenResponse.isNotBlank()) {
                speakResponse(result.spokenResponse)
            }
        }
    }

    private fun speakResponse(text: String) {
        val cleanText = SmartHumanEngine.sanitizeResponse(text, _lastUserSpeech.value)
        Log.d(TAG, "[STAGE 6: TTS (Text-to-Speech)] speakResponse: \"$cleanText\"")
        _lastAiResponse.value = cleanText
        _status.value = AssistantStatus.Speaking(cleanText)
        latencyTracker.markTtsStarted()
        
        try {
            com.example.SahNajApplication.instance.dualVoiceEngine.speakAssistantResponse(cleanText) {
                AssistantWakeLockManager.releaseWakeLock()
                if (_status.value is AssistantStatus.Speaking) {
                    _status.value = AssistantStatus.Idle
                }
            }
        } catch (_: Exception) {
            textToSpeechManager.speak(cleanText, userPreferences.getSpeechRate()) {
                AssistantWakeLockManager.releaseWakeLock()
                if (_status.value is AssistantStatus.Speaking) {
                    _status.value = AssistantStatus.Idle
                }
            }
        }
    }

    private fun getLanguageCode(): String {
        return when (userPreferences.getLanguage().lowercase()) {
            "english" -> "en-US"
            "hindi" -> "hi-IN"
            else -> "hi-IN"
        }
    }

    fun showErrorBanner(
        message: String,
        canRetry: Boolean = false,
        actionLabel: String = "फिर कोशिश करें",
        onRetry: (() -> Unit)? = null
    ) {
        _activeErrorBanner.value = ErrorBannerData(
            message = message,
            canRetry = canRetry,
            actionLabel = actionLabel,
            onRetry = onRetry
        )
    }

    fun dismissErrorBanner() {
        _activeErrorBanner.value = null
    }

    fun retryLastCommand() {
        val lastCommand = _lastUserSpeech.value.trim()
        if (lastCommand.isNotBlank()) {
            processDirectTextCommand(lastCommand)
        } else {
            startListening()
        }
    }

    private fun handleConversationalFallback(query: String) {
        viewModelScope.launch {
            _status.value = AssistantStatus.Thinking
            val name = userPreferences.getUserDisplayName()
            val result = geminiRepository.answerConversational(query, name)
            result.onSuccess { reply ->
                _status.value = AssistantStatus.Idle
                _lastAiResponse.value = reply
                speakResponse(reply)
            }.onFailure { err ->
                Log.w(TAG, "[CONVERSATIONAL ROUTE] Gemini conversational call failed: ${err.message}, using SmartHumanEngine fallback")
                val fallback = SmartHumanEngine.generateSmartReply(query, name)
                _status.value = AssistantStatus.Idle
                _lastAiResponse.value = fallback
                speakResponse(fallback)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "[STAGE 1: MIC / PERMISSION] AssistantViewModel onCleared")
        speechRecognizerManager.stopListening()
        textToSpeechManager.stop()
    }

    companion object {
        private const val TAG = "SAHNAJ_VOICE"
    }
}


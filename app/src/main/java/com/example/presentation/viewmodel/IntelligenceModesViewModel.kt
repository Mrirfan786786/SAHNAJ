package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.SahNajAccessibilityService
import com.example.data.local.UserPreferences
import com.example.data.model.ResponseLatencyOptimization
import com.example.data.model.SahnajOperatingMode
import com.example.data.repository.GeminiRepository
import com.example.data.repository.UserMemoryRepository
import com.example.voice.DualVoiceEngine
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IntelligenceModesViewModel(
    private val userPreferences: UserPreferences,
    private val geminiRepository: GeminiRepository? = null,
    private val userMemoryRepository: UserMemoryRepository? = null,
    private val textToSpeechManager: TextToSpeechManager? = null,
    private val dualVoiceEngine: DualVoiceEngine? = null
) : ViewModel() {

    val operatingMode: StateFlow<SahnajOperatingMode> = userPreferences.operatingMode
    val screenContentReaderEnabled: StateFlow<Boolean> = userPreferences.screenContentReaderEnabled
    val autoSummarizeLongTextsEnabled: StateFlow<Boolean> = userPreferences.autoSummarizeLongTextsEnabled
    val contextMemoryEnabled: StateFlow<Boolean> = userPreferences.contextMemoryEnabled
    val responseLatencyOptimization: StateFlow<ResponseLatencyOptimization> = userPreferences.responseLatencyOptimization

    private val _isReadingDemo = MutableStateFlow(false)
    val isReadingDemo: StateFlow<Boolean> = _isReadingDemo.asStateFlow()

    private val _demoReadingText = MutableStateFlow<String?>(null)
    val demoReadingText: StateFlow<String?> = _demoReadingText.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun setOperatingMode(mode: SahnajOperatingMode) {
        userPreferences.setOperatingMode(mode)
        _statusMessage.value = "Mode switched to: ${mode.title}"
        
        // If switching to Stealth mode, immediately stop any speech
        if (mode == SahnajOperatingMode.STEALTH) {
            stopSpeaking()
        } else if (mode.ttsEnabled) {
            // Friendly voice confirmation of mode switch
            val confirmationText = when (mode) {
                SahnajOperatingMode.JARVIS -> "JARVIS Autonomous Mode active. Full voice and system diagnostics ready."
                SahnajOperatingMode.TECHNICIAN -> "Technician Mode engaged. Automotive and hardware diagnostic engine ready."
                SahnajOperatingMode.OFFLINE_CORE -> "Offline Core active. 100 percent on-device processing engaged."
                else -> ""
            }
            if (confirmationText.isNotBlank()) {
                speakPrompt(confirmationText)
            }
        }
    }

    fun toggleScreenContentReader(enabled: Boolean) {
        userPreferences.setScreenContentReaderEnabled(enabled)
        _statusMessage.value = if (enabled) "Screen Content Reader enabled" else "Screen Content Reader disabled"
    }

    fun toggleAutoSummarizeLongTexts(enabled: Boolean) {
        userPreferences.setAutoSummarizeLongTextsEnabled(enabled)
        _statusMessage.value = if (enabled) "Auto-Summarization enabled" else "Auto-Summarization disabled"
    }

    fun toggleContextMemory(enabled: Boolean) {
        userPreferences.setContextMemoryEnabled(enabled)
        _statusMessage.value = if (enabled) "Context Memory active" else "Context Memory paused"
    }

    fun setResponseLatencyOptimization(optimization: ResponseLatencyOptimization) {
        userPreferences.setResponseLatencyOptimization(optimization)
        _statusMessage.value = "Latency Profile: ${optimization.title} (${optimization.targetLatency})"
    }

    fun testScreenReading(context: Context) {
        val activeService = SahNajAccessibilityService.instance
        val screenText = activeService?.extractScreenText(1000)
        
        val textToRead = if (!screenText.isNullOrBlank()) {
            "Screen Content: $screenText"
        } else {
            "SAHNAJ Smart Reader: Currently reading active screen. Intelligent parsing, chat extraction, and hands-free text-to-speech are operational."
        }

        _demoReadingText.value = textToRead
        _isReadingDemo.value = true

        speakPrompt(textToRead) {
            _isReadingDemo.value = false
        }
    }

    fun testSummarizeAndRead() {
        val sampleLongArticle = "SAHNAJ Autonomous AI operates with dual neural pipelines. The on-device engine executes zero-latency system actions including WhatsApp messaging, contacts dialing, and sensor triggers. The cloud reasoning core delivers rich multimodal visual understanding and continuous context synthesis."
        
        val summaryToRead = if (userPreferences.isAutoSummarizeLongTextsEnabled()) {
            "Summary: SAHNAJ runs a dual-engine architecture with instant on-device actions and cloud multimodal reasoning."
        } else {
            sampleLongArticle
        }

        _demoReadingText.value = summaryToRead
        _isReadingDemo.value = true

        speakPrompt(summaryToRead) {
            _isReadingDemo.value = false
        }
    }

    fun stopSpeaking() {
        _isReadingDemo.value = false
        dualVoiceEngine?.stopPlayback()
        textToSpeechManager?.stop()
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun clearMemory(onComplete: () -> Unit) {
        viewModelScope.launch {
            userMemoryRepository?.clearAllMemories()
            _statusMessage.value = "All context memories wiped successfully"
            onComplete()
        }
    }

    private fun speakPrompt(text: String, onDone: (() -> Unit)? = null) {
        val currentMode = userPreferences.getOperatingMode()
        if (!currentMode.ttsEnabled) {
            onDone?.invoke()
            return
        }

        if (dualVoiceEngine != null) {
            dualVoiceEngine.speakAssistantResponse(text) {
                onDone?.invoke()
            }
        } else if (textToSpeechManager != null) {
            textToSpeechManager.speak(text, userPreferences.getSpeechRate()) {
                onDone?.invoke()
            }
        } else {
            onDone?.invoke()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopSpeaking()
    }
}

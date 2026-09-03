package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.repository.UserMemoryRepository
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val userMemoryRepository: UserMemoryRepository? = null,
    private val textToSpeechManager: TextToSpeechManager? = null
) : ViewModel() {

    val assistantName: StateFlow<String> = userPreferences.assistantName
    val wakeWordEnabled: StateFlow<Boolean> = userPreferences.wakeWordEnabled
    val speechRate: StateFlow<Float> = userPreferences.speechRate
    val language: StateFlow<String> = userPreferences.language
    val theme: StateFlow<String> = userPreferences.theme
    val confirmationMode: StateFlow<Boolean> = userPreferences.confirmationMode
    val callAssistantEnabled: StateFlow<Boolean> = userPreferences.callAssistantEnabled
    val chatNotificationsEnabled: StateFlow<Boolean> = userPreferences.chatNotificationsEnabled
    val operatingMode: StateFlow<com.example.data.model.SahnajOperatingMode> = userPreferences.operatingMode
    val screenContentReaderEnabled: StateFlow<Boolean> = userPreferences.screenContentReaderEnabled
    val autoSummarizeLongTextsEnabled: StateFlow<Boolean> = userPreferences.autoSummarizeLongTextsEnabled
    val contextMemoryEnabled: StateFlow<Boolean> = userPreferences.contextMemoryEnabled
    val responseLatencyOptimization: StateFlow<com.example.data.model.ResponseLatencyOptimization> = userPreferences.responseLatencyOptimization

    private val _memoryClearedStatus = MutableStateFlow<Boolean?>(null)
    val memoryClearedStatus: StateFlow<Boolean?> = _memoryClearedStatus.asStateFlow()

    fun getGeminiApiKey(): String = userPreferences.getGeminiApiKey()

    fun setGeminiApiKey(key: String) {
        userPreferences.setGeminiApiKey(key)
    }

    fun updateAssistantName(name: String) {
        userPreferences.setAssistantName(name.trim().uppercase())
    }

    fun updateLanguage(lang: String) {
        userPreferences.setLanguage(lang)
        textToSpeechManager?.setPreferredLanguage(lang)
    }

    fun updateTheme(themeStr: String) {
        userPreferences.setTheme(themeStr)
    }

    fun updateSpeechRate(rate: Float) {
        userPreferences.setSpeechRate(rate)
    }

    fun toggleWakeWord(enabled: Boolean) {
        userPreferences.setWakeWordEnabled(enabled)
    }

    fun toggleConfirmationMode(enabled: Boolean) {
        userPreferences.setConfirmationMode(enabled)
    }

    fun toggleCallAssistant(enabled: Boolean) {
        userPreferences.setCallAssistantEnabled(enabled)
    }

    fun toggleChatNotifications(enabled: Boolean) {
        userPreferences.setChatNotificationsEnabled(enabled)
    }

    fun setOperatingMode(mode: com.example.data.model.SahnajOperatingMode) {
        userPreferences.setOperatingMode(mode)
    }

    fun toggleScreenContentReader(enabled: Boolean) {
        userPreferences.setScreenContentReaderEnabled(enabled)
    }

    fun toggleAutoSummarizeLongTexts(enabled: Boolean) {
        userPreferences.setAutoSummarizeLongTextsEnabled(enabled)
    }

    fun toggleContextMemory(enabled: Boolean) {
        userPreferences.setContextMemoryEnabled(enabled)
    }

    fun setResponseLatencyOptimization(optimization: com.example.data.model.ResponseLatencyOptimization) {
        userPreferences.setResponseLatencyOptimization(optimization)
    }

    fun clearAllMemories(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            userMemoryRepository?.clearAllMemories()
            _memoryClearedStatus.value = true
            onComplete?.invoke()
        }
    }

    fun resetMemoryClearedStatus() {
        _memoryClearedStatus.value = null
    }

    fun previewFemaleVoice() {
        val currentLang = userPreferences.getLanguage().lowercase()
        val previewPhrase = when {
            currentLang == "english" -> "Hello! I am SahNaj, your friendly smart voice assistant!"
            currentLang == "hindi" -> "नमस्ते! मैं सहनाज हूँ, आपकी स्मार्ट वॉइस असिस्टेंट!"
            else -> "Haanji dost! Main SahNaj hoon, aapki energetic female voice assistant! 😊"
        }
        textToSpeechManager?.speak(previewPhrase, userPreferences.getSpeechRate())
    }
}

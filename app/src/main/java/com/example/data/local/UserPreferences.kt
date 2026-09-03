package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ResponseLatencyOptimization
import com.example.data.model.SahnajOperatingMode
import com.example.data.model.UserProfile
import com.example.data.model.VoiceProfile
import com.example.domain.personality.PersonalityResponses
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("sahnaj_prefs", Context.MODE_PRIVATE)
    val securePreferences: SecurePreferences = SecurePreferences(context)

    init {
        // Auto-migrate legacy plain text API key to EncryptedSharedPreferences if present
        val legacyKey = prefs.getString("custom_gemini_api_key", null)
        if (!legacyKey.isNullOrBlank()) {
            if (securePreferences.getGeminiApiKey().isBlank()) {
                securePreferences.setGeminiApiKey(legacyKey)
            }
            prefs.edit().remove("custom_gemini_api_key").apply()
        }
    }

    // AI Name is FIXED to SAHNAJ (Non-editable constant)
    val assistantName: StateFlow<String> = MutableStateFlow(PersonalityResponses.ASSISTANT_NAME).asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(isWakeWordEnabled())
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    private val _speechRate = MutableStateFlow(getSpeechRate())
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(getSpeechPitch())
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    private val _selectedVoiceName = MutableStateFlow(getTtsVoiceName())
    val selectedVoiceName: StateFlow<String> = _selectedVoiceName.asStateFlow()

    private val _activeVoiceId = MutableStateFlow(getActiveVoiceId())
    val activeVoiceFlow: StateFlow<String> = _activeVoiceId.asStateFlow()

    private val _aiModel = MutableStateFlow(getAiModel())
    val aiModel: StateFlow<String> = _aiModel.asStateFlow()

    private val _language = MutableStateFlow(getLanguage())
    val language: StateFlow<String> = _language.asStateFlow()

    private val _useNameWhenSpeaking = MutableStateFlow(isUseNameWhenSpeaking())
    val useNameWhenSpeaking: StateFlow<Boolean> = _useNameWhenSpeaking.asStateFlow()

    private val _userDisplayName = MutableStateFlow(getUserDisplayName())
    val userDisplayName: StateFlow<String> = _userDisplayName.asStateFlow()

    private val _theme = MutableStateFlow("DARK")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _confirmationMode = MutableStateFlow(isConfirmationModeEnabled())
    val confirmationMode: StateFlow<Boolean> = _confirmationMode.asStateFlow()

    private val _orbColor = MutableStateFlow(getOrbColor())
    val orbColor: StateFlow<Long> = _orbColor.asStateFlow()

    private val _orbScale = MutableStateFlow(getOrbScale())
    val orbScale: StateFlow<Float> = _orbScale.asStateFlow()

    private val _lastSyncedTimestamp = MutableStateFlow(getLastSyncedTimestamp())
    val lastSyncedTimestamp: StateFlow<Long> = _lastSyncedTimestamp.asStateFlow()

    private val _autoUpdateCheckEnabled = MutableStateFlow(isAutoUpdateCheckEnabled())
    val autoUpdateCheckEnabled: StateFlow<Boolean> = _autoUpdateCheckEnabled.asStateFlow()

    private val _voiceLockEnabled = MutableStateFlow(isVoiceLockEnabled())
    val voiceLockEnabled: StateFlow<Boolean> = _voiceLockEnabled.asStateFlow()

    private val _isLicensed = MutableStateFlow(isLicensed())
    val isLicensed: StateFlow<Boolean> = _isLicensed.asStateFlow()

    private val _triggersMasterEnabled = MutableStateFlow(isTriggersMasterEnabled())
    val triggersMasterEnabled: StateFlow<Boolean> = _triggersMasterEnabled.asStateFlow()

    private val _morningBriefingEnabled = MutableStateFlow(isMorningBriefingEnabled())
    val morningBriefingEnabled: StateFlow<Boolean> = _morningBriefingEnabled.asStateFlow()

    private val _nightAutomationEnabled = MutableStateFlow(isNightAutomationEnabled())
    val nightAutomationEnabled: StateFlow<Boolean> = _nightAutomationEnabled.asStateFlow()

    private val _callAssistantEnabled = MutableStateFlow(isCallAssistantEnabled())
    val callAssistantEnabled: StateFlow<Boolean> = _callAssistantEnabled.asStateFlow()

    private val _chatNotificationsEnabled = MutableStateFlow(isChatNotificationsEnabled())
    val chatNotificationsEnabled: StateFlow<Boolean> = _chatNotificationsEnabled.asStateFlow()

    private val _operatingMode = MutableStateFlow(getOperatingMode())
    val operatingMode: StateFlow<SahnajOperatingMode> = _operatingMode.asStateFlow()

    private val _screenContentReaderEnabled = MutableStateFlow(isScreenContentReaderEnabled())
    val screenContentReaderEnabled: StateFlow<Boolean> = _screenContentReaderEnabled.asStateFlow()

    private val _autoSummarizeLongTextsEnabled = MutableStateFlow(isAutoSummarizeLongTextsEnabled())
    val autoSummarizeLongTextsEnabled: StateFlow<Boolean> = _autoSummarizeLongTextsEnabled.asStateFlow()

    private val _contextMemoryEnabled = MutableStateFlow(isContextMemoryEnabled())
    val contextMemoryEnabled: StateFlow<Boolean> = _contextMemoryEnabled.asStateFlow()

    private val _responseLatencyOptimization = MutableStateFlow(getResponseLatencyOptimization())
    val responseLatencyOptimization: StateFlow<ResponseLatencyOptimization> = _responseLatencyOptimization.asStateFlow()

    val geminiApiKeyFlow: StateFlow<String> = securePreferences.apiKeyFlow
    val providerKeysFlow: StateFlow<Map<String, String>> = securePreferences.providerKeysFlow

    fun isTriggersMasterEnabled(): Boolean {
        return prefs.getBoolean("triggers_enabled_key", false)
    }

    fun setTriggersMasterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("triggers_enabled_key", enabled).apply()
        _triggersMasterEnabled.value = enabled
    }

    fun isMorningBriefingEnabled(): Boolean {
        return prefs.getBoolean("morning_briefing_key", false)
    }

    fun setMorningBriefingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("morning_briefing_key", enabled).apply()
        _morningBriefingEnabled.value = enabled
    }

    fun isNightAutomationEnabled(): Boolean {
        return prefs.getBoolean("night_automation_key", false)
    }

    fun setNightAutomationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("night_automation_key", enabled).apply()
        _nightAutomationEnabled.value = enabled
    }

    fun isCallAssistantEnabled(): Boolean {
        return prefs.getBoolean("call_assistant_enabled", false)
    }

    fun setCallAssistantEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("call_assistant_enabled", enabled).apply()
        _callAssistantEnabled.value = enabled
    }

    fun isChatNotificationsEnabled(): Boolean {
        return prefs.getBoolean("chat_notifications_enabled", false)
    }

    fun setChatNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("chat_notifications_enabled", enabled).apply()
        _chatNotificationsEnabled.value = enabled
    }

    // Operating Modes: JARVIS (Autonomous), TECHNICIAN, STEALTH, OFFLINE_CORE
    fun getOperatingMode(): SahnajOperatingMode {
        val raw = prefs.getString("sahnaj_operating_mode", SahnajOperatingMode.JARVIS.id)
        return SahnajOperatingMode.fromId(raw)
    }

    fun setOperatingMode(mode: SahnajOperatingMode) {
        prefs.edit().putString("sahnaj_operating_mode", mode.id).apply()
        _operatingMode.value = mode
    }

    // Smart Reading Engine: Screen Content Reader
    fun isScreenContentReaderEnabled(): Boolean {
        return prefs.getBoolean("screen_content_reader_enabled", false)
    }

    fun setScreenContentReaderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("screen_content_reader_enabled", enabled).apply()
        _screenContentReaderEnabled.value = enabled
    }

    // Smart Reading Engine: Auto-Summarize Long Texts
    fun isAutoSummarizeLongTextsEnabled(): Boolean {
        return prefs.getBoolean("auto_summarize_long_texts", true)
    }

    fun setAutoSummarizeLongTextsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_summarize_long_texts", enabled).apply()
        _autoSummarizeLongTextsEnabled.value = enabled
    }

    // Memory & Automation: Context Memory
    fun isContextMemoryEnabled(): Boolean {
        return prefs.getBoolean("context_memory_enabled", true)
    }

    fun setContextMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("context_memory_enabled", enabled).apply()
        _contextMemoryEnabled.value = enabled
    }

    // Memory & Automation: Response Latency Optimization (Aggressive, Balanced, Deep Thinking)
    fun getResponseLatencyOptimization(): ResponseLatencyOptimization {
        val level = prefs.getInt("response_latency_level", ResponseLatencyOptimization.BALANCED.level)
        return ResponseLatencyOptimization.fromLevel(level)
    }

    fun setResponseLatencyOptimization(optimization: ResponseLatencyOptimization) {
        prefs.edit().putInt("response_latency_level", optimization.level).apply()
        _responseLatencyOptimization.value = optimization
    }

    fun getCustomTriggersJson(): String {
        return prefs.getString("custom_triggers_list_json", "") ?: ""
    }

    fun saveCustomTriggersJson(json: String) {
        prefs.edit().putString("custom_triggers_list_json", json).apply()
    }

    fun getAssistantName(): String {
        return PersonalityResponses.ASSISTANT_NAME
    }

    // Deprecated / No-Op: Assistant Name is immutable "SAHNAJ"
    fun setAssistantName(name: String) {
        // Name is fixed to SAHNAJ
    }

    fun isWakeWordEnabled(): Boolean {
        return prefs.getBoolean("wake_word_enabled", true)
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("wake_word_enabled", enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    fun getSpeechRate(): Float {
        return prefs.getFloat("speech_rate", com.example.voice.TTSVoiceHelper.DEFAULT_SPEECH_RATE)
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat("speech_rate", rate).apply()
        _speechRate.value = rate
    }

    fun getSpeechPitch(): Float {
        return prefs.getFloat("speech_pitch", com.example.voice.TTSVoiceHelper.DEFAULT_FEMALE_PITCH)
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat("speech_pitch", pitch).apply()
        _speechPitch.value = pitch
    }

    fun getTtsVoiceName(): String {
        return prefs.getString("tts_voice_name", "") ?: ""
    }

    fun setTtsVoiceName(name: String) {
        prefs.edit().putString("tts_voice_name", name).apply()
        _selectedVoiceName.value = name
    }

    fun getActiveVoiceId(): String {
        return prefs.getString("sahnaj_active_voice", com.example.data.model.VoiceStudioCatalog.DEFAULT_VOICE_ID)
            ?: com.example.data.model.VoiceStudioCatalog.DEFAULT_VOICE_ID
    }

    fun setActiveVoiceId(voiceId: String) {
        val clean = voiceId.trim().ifEmpty { com.example.data.model.VoiceStudioCatalog.DEFAULT_VOICE_ID }
        prefs.edit().putString("sahnaj_active_voice", clean).apply()
        _activeVoiceId.value = clean
        val profile = com.example.data.model.VoiceStudioCatalog.findVoiceById(clean)
        setTtsVoiceName(profile.name)
        setSpeechPitch(profile.ttsPitch)
        setSpeechRate(profile.ttsSpeechRate)
    }

    fun getActiveVoiceProfile(): com.example.data.model.VoiceProfileItem {
        return com.example.data.model.VoiceStudioCatalog.findVoiceById(getActiveVoiceId())
    }

    fun getAiModel(): String {
        return prefs.getString("selected_ai_model", "gemini-flash") ?: "gemini-flash"
    }

    fun setAiModel(model: String) {
        prefs.edit().putString("selected_ai_model", model).apply()
        _aiModel.value = model
    }

    fun getLanguage(): String {
        return prefs.getString("app_language", "Hinglish") ?: "Hinglish"
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _language.value = lang
    }

    fun getTheme(): String {
        return "DARK"
    }

    fun setTheme(theme: String) {
        // Red-Black Dark theme is fixed
        _theme.value = "DARK"
    }

    fun isConfirmationModeEnabled(): Boolean {
        return prefs.getBoolean("confirmation_mode", true)
    }

    fun setConfirmationMode(enabled: Boolean) {
        prefs.edit().putBoolean("confirmation_mode", enabled).apply()
        _confirmationMode.value = enabled
    }

    fun getOrbColor(): Long {
        return prefs.getLong("orb_color", DEFAULT_ORB_COLOR)
    }

    fun setOrbColor(color: Long) {
        prefs.edit().putLong("orb_color", color).apply()
        _orbColor.value = color
    }

    fun getOrbScale(): Float {
        return prefs.getFloat("orb_scale", DEFAULT_ORB_SCALE)
    }

    fun setOrbScale(scale: Float) {
        prefs.edit().putFloat("orb_scale", scale).apply()
        _orbScale.value = scale
    }

    fun resetOrbCustomization() {
        prefs.edit()
            .putLong("orb_color", DEFAULT_ORB_COLOR)
            .putFloat("orb_scale", DEFAULT_ORB_SCALE)
            .apply()
        _orbColor.value = DEFAULT_ORB_COLOR
        _orbScale.value = DEFAULT_ORB_SCALE
    }

    companion object {
        const val DEFAULT_ORB_COLOR: Long = 0xFFE10600L
        const val DEFAULT_ORB_SCALE: Float = 1.0f
    }

    fun hasSeenOnboarding(): Boolean {
        return prefs.getBoolean("has_seen_onboarding", false)
    }

    fun setSeenOnboarding(seen: Boolean) {
        prefs.edit().putBoolean("has_seen_onboarding", seen).apply()
    }

    fun isSetupCompleted(): Boolean {
        return prefs.getBoolean("setup_completed", false)
    }

    fun setSetupCompleted(completed: Boolean) {
        prefs.edit().putBoolean("setup_completed", completed).apply()
    }

    fun getGeminiApiKey(): String {
        return securePreferences.getGeminiApiKey()
    }

    fun setGeminiApiKey(key: String) {
        securePreferences.setGeminiApiKey(key)
    }

    fun clearGeminiApiKey() {
        securePreferences.clearGeminiApiKey()
    }

    fun getProviderApiKey(providerId: String): String {
        return securePreferences.getProviderApiKey(providerId)
    }

    fun setProviderApiKey(providerId: String, key: String) {
        securePreferences.setProviderApiKey(providerId, key)
    }

    fun clearProviderApiKey(providerId: String) {
        securePreferences.clearProviderApiKey(providerId)
    }

    fun getLastSyncedTimestamp(): Long {
        return prefs.getLong("last_cloud_sync_timestamp", 0L)
    }

    fun setLastSyncedTimestamp(timestamp: Long) {
        prefs.edit().putLong("last_cloud_sync_timestamp", timestamp).apply()
        _lastSyncedTimestamp.value = timestamp
    }

    fun isAutoUpdateCheckEnabled(): Boolean {
        return prefs.getBoolean("auto_update_check_enabled", true)
    }

    fun setAutoUpdateCheckEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_update_check_enabled", enabled).apply()
        _autoUpdateCheckEnabled.value = enabled
    }

    fun getLastUpdateCheckTime(): Long {
        return prefs.getLong("last_update_check_time", 0L)
    }

    fun setLastUpdateCheckTime(timestamp: Long) {
        prefs.edit().putLong("last_update_check_time", timestamp).apply()
    }

    fun isVoiceLockEnabled(): Boolean {
        return prefs.getBoolean("voice_lock_enabled", false)
    }

    fun setVoiceLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("voice_lock_enabled", enabled).apply()
        _voiceLockEnabled.value = enabled
    }

    fun isLicensed(): Boolean {
        return prefs.getBoolean("is_app_licensed", false)
    }

    fun setLicensed(licensed: Boolean) {
        prefs.edit().putBoolean("is_app_licensed", licensed).apply()
        _isLicensed.value = licensed
    }

    fun getLicenseKey(): String {
        return prefs.getString("app_license_key", "") ?: ""
    }

    fun setLicenseKey(key: String) {
        prefs.edit().putString("app_license_key", key).apply()
    }

    fun getLicenseActivatedDate(): String {
        return prefs.getString("license_activated_date", "") ?: ""
    }

    fun setLicenseActivatedDate(dateStr: String) {
        prefs.edit().putString("license_activated_date", dateStr).apply()
    }

    fun getLicenseActivatedTimestamp(): Long {
        return prefs.getLong("license_activated_timestamp", 0L)
    }

    fun setLicenseActivatedTimestamp(timestamp: Long) {
        prefs.edit().putLong("license_activated_timestamp", timestamp).apply()
    }

    fun getSubscriptionPlan(): String {
        return prefs.getString("subscription_plan_id", "") ?: ""
    }

    fun setSubscriptionPlan(planId: String) {
        prefs.edit().putString("subscription_plan_id", planId).apply()
    }

    fun getSubscriptionPlanName(): String {
        return prefs.getString("subscription_plan_name", "") ?: ""
    }

    fun setSubscriptionPlanName(name: String) {
        prefs.edit().putString("subscription_plan_name", name).apply()
    }

    fun getSubscriptionExpiry(): String {
        return prefs.getString("subscription_expiry", "") ?: ""
    }

    fun setSubscriptionExpiry(expiry: String) {
        prefs.edit().putString("subscription_expiry", expiry).apply()
    }

    fun hasEnrolledVoiceProfile(): Boolean {
        val json = prefs.getString("voice_biometric_profile", "") ?: ""
        return json.isNotBlank()
    }

    fun getVoiceProfile(): VoiceProfile? {
        val json = prefs.getString("voice_biometric_profile", "") ?: ""
        if (json.isBlank()) return null
        return try {
            val obj = JSONObject(json)
            val sigArray = obj.optJSONArray("acousticSignature")
            val signature = mutableListOf<Float>()
            if (sigArray != null) {
                for (i in 0 until sigArray.length()) {
                    signature.add(sigArray.getDouble(i).toFloat())
                }
            }
            VoiceProfile(
                isEnrolled = obj.optBoolean("isEnrolled", true),
                enrolledDate = obj.optString("enrolledDate", ""),
                sampleCount = obj.optInt("sampleCount", 3),
                averageRmsDb = obj.optDouble("averageRmsDb", 0.0).toFloat(),
                averageDurationMs = obj.optLong("averageDurationMs", 0L),
                acousticSignature = signature,
                confidenceThreshold = obj.optDouble("confidenceThreshold", 0.50).toFloat()
            )
        } catch (_: Exception) {
            null
        }
    }

    fun saveVoiceProfile(profile: VoiceProfile) {
        try {
            val obj = JSONObject().apply {
                put("isEnrolled", profile.isEnrolled)
                put("enrolledDate", profile.enrolledDate)
                put("sampleCount", profile.sampleCount)
                put("averageRmsDb", profile.averageRmsDb.toDouble())
                put("averageDurationMs", profile.averageDurationMs)
                put("confidenceThreshold", profile.confidenceThreshold.toDouble())
                val arr = JSONArray()
                profile.acousticSignature.forEach { arr.put(it.toDouble()) }
                put("acousticSignature", arr)
            }
            prefs.edit().putString("voice_biometric_profile", obj.toString()).apply()
        } catch (_: Exception) {
        }
    }

    fun clearVoiceProfile() {
        prefs.edit().remove("voice_biometric_profile").apply()
        setVoiceLockEnabled(false)
    }

    fun isUseNameWhenSpeaking(): Boolean {
        return prefs.getBoolean("use_name_when_speaking", true)
    }

    fun setUseNameWhenSpeaking(enabled: Boolean) {
        prefs.edit().putBoolean("use_name_when_speaking", enabled).apply()
        _useNameWhenSpeaking.value = enabled
    }

    fun getUserDisplayName(): String {
        val offlineName = prefs.getString("OFFLINE_USER_NAME", null)?.trim()
        if (!offlineName.isNullOrBlank()) return offlineName
        return prefs.getString("user_display_name", "USER") ?: "USER"
    }

    fun setUserDisplayName(name: String) {
        val clean = name.trim().ifEmpty { "USER" }
        prefs.edit()
            .putString("user_display_name", clean)
            .putString("OFFLINE_USER_NAME", clean)
            .apply()
        _userDisplayName.value = clean
    }

    fun getOfflineUserName(): String? {
        val offlineName = prefs.getString("OFFLINE_USER_NAME", null)?.trim()
        if (!offlineName.isNullOrBlank()) return offlineName
        val legacy = prefs.getString("user_display_name", null)?.trim()
        return if (!legacy.isNullOrBlank() && !legacy.equals("USER", ignoreCase = true)) legacy else null
    }

    fun setOfflineUserName(name: String) {
        setUserDisplayName(name)
    }

    fun getUserProfile(): UserProfile {
        return UserProfile(
            uid = "local_device",
            displayName = getUserDisplayName(),
            email = "offline_mode@device.local",
            phoneNumber = "",
            photoUrl = "",
            assistantName = PersonalityResponses.ASSISTANT_NAME,
            language = getLanguage(),
            theme = "DARK",
            speechRate = getSpeechRate(),
            plan = "CYBER CORE",
            isActive = true
        )
    }

    fun cacheUserProfile(profile: UserProfile) {
        setUserDisplayName(profile.displayName)
        setLanguage(profile.language)
        setSpeechRate(profile.speechRate)
    }

    fun getCachedUserProfile(): UserProfile {
        return getUserProfile()
    }

    fun clearUserData() {
        securePreferences.clearGeminiApiKey()
        prefs.edit()
            .remove("user_display_name")
            .remove("assistant_name")
            .remove("app_language")
            .remove("use_name_when_speaking")
            .remove("speech_rate")
            .remove("wake_word_enabled")
            .remove("confirmation_mode")
            .remove("setup_completed")
            .remove("custom_gemini_api_key")
            .remove("last_cloud_sync_timestamp")
            .apply()
        _wakeWordEnabled.value = true
        _speechRate.value = 1.08f
        _language.value = "Hinglish"
        _useNameWhenSpeaking.value = true
        _userDisplayName.value = "USER"
        _theme.value = "DARK"
        _confirmationMode.value = true
        _lastSyncedTimestamp.value = 0L
    }
}

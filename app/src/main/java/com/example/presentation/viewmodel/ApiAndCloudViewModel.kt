package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.model.AiProvider
import com.example.data.model.AiProvidersConfig
import com.example.data.repository.AuthRepository
import com.example.data.repository.CloudSyncManager
import com.example.data.repository.GeminiRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

sealed class TestConnectionState {
    data object Idle : TestConnectionState()
    data object Testing : TestConnectionState()
    data class Success(val message: String = "Connected Successfully! (सफलतापूर्वक जुड़ गया)") : TestConnectionState()
    data class Error(val message: String = "Invalid API Key or Quota Exceeded (अमान्य कुंजी)") : TestConnectionState()
}

sealed class CloudSyncState {
    data object Idle : CloudSyncState()
    data object Syncing : CloudSyncState()
    data class Success(val message: String = "✅ Synced", val timestamp: Long) : CloudSyncState()
    data class Error(val message: String) : CloudSyncState()
}

class ApiAndCloudViewModel(
    private val userPreferences: UserPreferences,
    private val geminiRepository: GeminiRepository,
    private val authRepository: AuthRepository,
    private val cloudSyncManager: CloudSyncManager
) : ViewModel() {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val apiKey: StateFlow<String> = userPreferences.geminiApiKeyFlow
    val providerKeys: StateFlow<Map<String, String>> = userPreferences.providerKeysFlow
    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val lastSyncedTimestamp: StateFlow<Long> = userPreferences.lastSyncedTimestamp

    // Single Gemini legacy test state for backwards compatibility
    private val _testConnectionState = MutableStateFlow<TestConnectionState>(TestConnectionState.Idle)
    val testConnectionState: StateFlow<TestConnectionState> = _testConnectionState.asStateFlow()

    // Multi-provider per-card test states
    private val _providerTestStates = MutableStateFlow<Map<String, TestConnectionState>>(emptyMap())
    val providerTestStates: StateFlow<Map<String, TestConnectionState>> = _providerTestStates.asStateFlow()

    private val _cloudSyncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val cloudSyncState: StateFlow<CloudSyncState> = _cloudSyncState.asStateFlow()

    fun getProviderKey(providerId: String): String {
        return userPreferences.getProviderApiKey(providerId)
    }

    fun saveProviderKey(providerId: String, newKey: String) {
        val cleanKey = newKey.trim()
        userPreferences.setProviderApiKey(providerId, cleanKey)
        updateProviderTestState(providerId, TestConnectionState.Idle)
        if (providerId.equals("gemini", ignoreCase = true)) {
            _testConnectionState.value = TestConnectionState.Idle
        }
    }

    fun clearProviderKey(providerId: String) {
        userPreferences.clearProviderApiKey(providerId)
        updateProviderTestState(providerId, TestConnectionState.Idle)
        if (providerId.equals("gemini", ignoreCase = true)) {
            _testConnectionState.value = TestConnectionState.Idle
        }
    }

    fun saveApiKey(newKey: String) {
        saveProviderKey("gemini", newKey)
    }

    fun clearApiKey() {
        clearProviderKey("gemini")
    }

    private fun updateProviderTestState(providerId: String, state: TestConnectionState) {
        val current = _providerTestStates.value.toMutableMap()
        current[providerId.lowercase()] = state
        _providerTestStates.value = current
    }

    /**
     * Universal Connection Tester & Router for all 16 AI Providers
     */
    fun testProviderConnection(providerId: String, key: String) {
        val cleanId = providerId.lowercase().trim()
        val provider = AiProvidersConfig.getProviderById(cleanId)
        val keyToTest = key.trim().ifEmpty { getProviderKey(cleanId) }

        if (keyToTest.isBlank()) {
            val emptyError = TestConnectionState.Error("Key is empty. Please enter or configure an API key.")
            updateProviderTestState(cleanId, emptyError)
            if (cleanId == "gemini") _testConnectionState.value = emptyError
            return
        }

        updateProviderTestState(cleanId, TestConnectionState.Testing)
        if (cleanId == "gemini") _testConnectionState.value = TestConnectionState.Testing

        viewModelScope.launch {
            if (cleanId == "gemini") {
                val result = geminiRepository.testApiKey(keyToTest)
                result.onSuccess { msg ->
                    val success = TestConnectionState.Success("Connected Successfully! (सफलतापूर्वक जुड़ गया)")
                    updateProviderTestState(cleanId, success)
                    _testConnectionState.value = success
                }.onFailure { err ->
                    val errorMsg = when {
                        err.message?.contains("400") == true || err.message?.contains("403") == true || err.message?.contains("401") == true ->
                            "Invalid API Key or Quota Exceeded (अमान्य कुंजी)"
                        err.message?.contains("Network") == true || err.message?.contains("internet") == true ->
                            "Network Error: Check internet connection"
                        else -> err.message ?: "Invalid API Key or Quota Exceeded (अमान्य कुंजी)"
                    }
                    val error = TestConnectionState.Error(errorMsg)
                    updateProviderTestState(cleanId, error)
                    _testConnectionState.value = error
                }
            } else {
                val outcome = testGenericAiConnection(provider, cleanId, keyToTest)
                updateProviderTestState(cleanId, outcome)
            }
        }
    }

    private suspend fun testGenericAiConnection(
        provider: AiProvider?,
        providerId: String,
        apiKey: String
    ): TestConnectionState = withContext(Dispatchers.IO) {
        try {
            val targetUrl = provider?.testEndpoint ?: "https://api.openai.com/v1/models"
            val requestBuilder = Request.Builder().url(targetUrl)

            when (providerId) {
                "claude_sonnet", "claude_haiku", "anthropic" -> {
                    requestBuilder.addHeader("x-api-key", apiKey)
                    requestBuilder.addHeader("anthropic-version", "2023-06-01")
                }
                "cohere_command", "cohere" -> {
                    requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                    requestBuilder.addHeader("accept", "application/json")
                }
                "replicate_video", "replicate" -> {
                    requestBuilder.addHeader("Authorization", "Token $apiKey")
                }
                "runway" -> {
                    requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                    requestBuilder.addHeader("X-Runway-Version", "2024-09-13")
                }
                "stability" -> {
                    requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                    requestBuilder.addHeader("Accept", "application/json")
                }
                "elevenlabs", "voice", "voice_tts" -> {
                    requestBuilder.addHeader("xi-api-key", apiKey)
                    requestBuilder.addHeader("Accept", "application/json")
                }
                "flux" -> {
                    // Pollinations / Flux test
                    if (apiKey.isNotBlank() && apiKey != "DIRECT") {
                        requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                    }
                }
                else -> {
                    requestBuilder.addHeader("Authorization", "Bearer $apiKey")
                }
            }

            val request = requestBuilder.build()
            val response = okHttpClient.newCall(request).execute()
            val code = response.code

            response.close()

            if (code in 200..299 || code == 204) {
                TestConnectionState.Success("Connected Successfully! (सफलतापूर्वक जुड़ गया)")
            } else if (code == 401 || code == 403) {
                TestConnectionState.Error("Invalid API Key or Quota Exceeded (अमान्य कुंजी)")
            } else if (code == 404 || code == 400) {
                // Some model endpoints without query param return 400/404 but validated auth header
                TestConnectionState.Success("Connected (Auth Active) (सफलतापूर्वक जुड़ गया)")
            } else {
                TestConnectionState.Error("Invalid API Key or Quota Exceeded (अमान्य कुंजी)")
            }
        } catch (e: Exception) {
            TestConnectionState.Error("Network Error: Check internet connection")
        }
    }

    fun testConnection(key: String) {
        testProviderConnection("gemini", key)
    }

    fun resetTestState() {
        _testConnectionState.value = TestConnectionState.Idle
        _providerTestStates.value = emptyMap()
    }

    fun syncNow() {
        _cloudSyncState.value = CloudSyncState.Syncing
        viewModelScope.launch {
            val result = cloudSyncManager.performSync()
            result.onSuccess { timestamp ->
                _cloudSyncState.value = CloudSyncState.Success("✅ Synced", timestamp)
            }.onFailure { err ->
                _cloudSyncState.value = CloudSyncState.Error("❌ Sync failed: ${err.message}")
            }
        }
    }

    fun resetSyncState() {
        _cloudSyncState.value = CloudSyncState.Idle
    }

    fun getMaskedApiKey(key: String = apiKey.value): String {
        val clean = key.trim()
        if (clean.isBlank()) return "Not Configured (Secure Keystore Ready)"
        return if (clean.length > 8) {
            val prefix = clean.take(4)
            val suffix = clean.takeLast(4)
            val dots = "•".repeat((clean.length - 8).coerceIn(4, 16))
            "$prefix$dots$suffix"
        } else {
            "••••••••••••"
        }
    }
}

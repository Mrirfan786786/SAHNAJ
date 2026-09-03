package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SecurePreferences(private val context: Context) {

    companion object {
        private const val TAG = "SecurePreferences"
        private const val SECURE_PREFS_FILE = "sahnaj_secure_keystore_prefs"
        private const val KEY_GEMINI_API_KEY = "encrypted_gemini_api_key"
        private const val PREFIX_PROVIDER_KEY = "encrypted_provider_key_"
        private const val PREFIX_SAHNAJ_KEY = "sahnaj_"
    }

    private val securePrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                SECURE_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "EncryptedSharedPreferences initialization failed, falling back to private prefs", e)
            context.getSharedPreferences("${SECURE_PREFS_FILE}_fallback", Context.MODE_PRIVATE)
        }
    }

    private val _apiKeyFlow = MutableStateFlow(getGeminiApiKey())
    val apiKeyFlow: StateFlow<String> = _apiKeyFlow.asStateFlow()

    private val _providerKeysFlow = MutableStateFlow<Map<String, String>>(getAllProviderKeys())
    val providerKeysFlow: StateFlow<Map<String, String>> = _providerKeysFlow.asStateFlow()

    private fun normalizeProviderId(providerId: String): String {
        return providerId.lowercase().trim().removePrefix("sahnaj_").removeSuffix("_key")
    }

    fun getProviderApiKey(providerId: String): String {
        val cleanId = normalizeProviderId(providerId)
        if (cleanId == "gemini") {
            val geminiKey = securePrefs.getString(KEY_GEMINI_API_KEY, "")?.trim() ?: ""
            if (geminiKey.isNotBlank()) return geminiKey
        }
        return try {
            val byDirectKey = securePrefs.getString("sahnaj_${cleanId}_key", "")?.trim() ?: ""
            if (byDirectKey.isNotBlank()) return byDirectKey

            val byPrefixedKey = securePrefs.getString("$PREFIX_PROVIDER_KEY$cleanId", "")?.trim() ?: ""
            if (byPrefixedKey.isNotBlank()) return byPrefixedKey

            // Intelligent fallbacks for sub-models / family keys
            when (cleanId) {
                "openai_mini", "openai_reasoning", "openai_o1", "openai_o3" -> {
                    val baseKey = securePrefs.getString("sahnaj_openai_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}openai", "")?.trim() ?: ""
                }
                "claude_sonnet", "claude_haiku", "anthropic" -> {
                    val baseKey = securePrefs.getString("sahnaj_claude_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val anthropicKey = securePrefs.getString("sahnaj_anthropic_key", "")?.trim() ?: ""
                    if (anthropicKey.isNotBlank()) return anthropicKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}anthropic", "")?.trim() ?: ""
                }
                "deepseek_v3", "deepseek_r1" -> {
                    val baseKey = securePrefs.getString("sahnaj_deepseek_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}deepseek", "")?.trim() ?: ""
                }
                "groq_llama", "groq" -> {
                    val baseKey = securePrefs.getString("sahnaj_groq_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}groq", "")?.trim() ?: ""
                }
                "mistral_large", "mistral_codestral", "codestral" -> {
                    val baseKey = securePrefs.getString("sahnaj_mistral_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val codestralKey = securePrefs.getString("sahnaj_codestral_key", "")?.trim() ?: ""
                    if (codestralKey.isNotBlank()) return codestralKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}mistral", "")?.trim() ?: ""
                }
                "perplexity_sonar", "perplexity" -> {
                    val baseKey = securePrefs.getString("sahnaj_perplexity_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}perplexity", "")?.trim() ?: ""
                }
                "cohere_command", "cohere" -> {
                    val baseKey = securePrefs.getString("sahnaj_cohere_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}cohere", "")?.trim() ?: ""
                }
                "gemma_2", "gemma" -> {
                    val baseKey = securePrefs.getString("sahnaj_gemma_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    getGeminiApiKey()
                }
                "grok_2", "grok" -> {
                    val baseKey = securePrefs.getString("sahnaj_grok_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}grok", "")?.trim() ?: ""
                }
                "stability", "sd3" -> {
                    val baseKey = securePrefs.getString("sahnaj_stability_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val genericImg = securePrefs.getString("sahnaj_image_key", "")?.trim() ?: ""
                    if (genericImg.isNotBlank()) return genericImg
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}stability", "")?.trim() ?: ""
                }
                "flux", "pollinations" -> {
                    val baseKey = securePrefs.getString("sahnaj_flux_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val genericImg = securePrefs.getString("sahnaj_image_key", "")?.trim() ?: ""
                    if (genericImg.isNotBlank()) return genericImg
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}flux", "")?.trim() ?: ""
                }
                "dalle", "dall_e" -> {
                    val baseKey = securePrefs.getString("sahnaj_dalle_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val openaiKey = securePrefs.getString("sahnaj_openai_key", "")?.trim() ?: ""
                    if (openaiKey.isNotBlank()) return openaiKey
                    val genericImg = securePrefs.getString("sahnaj_image_key", "")?.trim() ?: ""
                    if (genericImg.isNotBlank()) return genericImg
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}dalle", "")?.trim() ?: ""
                }
                "universal_image", "image" -> {
                    val baseKey = securePrefs.getString("sahnaj_image_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}image", "")?.trim() ?: ""
                }
                "runway" -> {
                    val baseKey = securePrefs.getString("sahnaj_runway_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val genericVid = securePrefs.getString("sahnaj_video_key", "")?.trim() ?: ""
                    if (genericVid.isNotBlank()) return genericVid
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}runway", "")?.trim() ?: ""
                }
                "luma" -> {
                    val baseKey = securePrefs.getString("sahnaj_luma_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val genericVid = securePrefs.getString("sahnaj_video_key", "")?.trim() ?: ""
                    if (genericVid.isNotBlank()) return genericVid
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}luma", "")?.trim() ?: ""
                }
                "replicate_video", "replicate" -> {
                    val baseKey = securePrefs.getString("sahnaj_replicate_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val genericVid = securePrefs.getString("sahnaj_video_key", "")?.trim() ?: ""
                    if (genericVid.isNotBlank()) return genericVid
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}replicate", "")?.trim() ?: ""
                }
                "universal_video", "video" -> {
                    val baseKey = securePrefs.getString("sahnaj_video_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}video", "")?.trim() ?: ""
                }
                "elevenlabs", "eleven_labs", "voice", "voice_tts" -> {
                    val baseKey = securePrefs.getString("sahnaj_elevenlabs_key", "")?.trim() ?: ""
                    if (baseKey.isNotBlank()) return baseKey
                    val genericVoice = securePrefs.getString("sahnaj_voice_key", "")?.trim() ?: ""
                    if (genericVoice.isNotBlank()) return genericVoice
                    securePrefs.getString("${PREFIX_PROVIDER_KEY}elevenlabs", "")?.trim() ?: ""
                }
                else -> ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading encrypted API key for provider: $providerId", e)
            ""
        }
    }

    fun setProviderApiKey(providerId: String, apiKey: String) {
        val cleanId = normalizeProviderId(providerId)
        val cleanKey = apiKey.trim()
        try {
            val editor = securePrefs.edit()
            editor.putString("$PREFIX_PROVIDER_KEY$cleanId", cleanKey)
            editor.putString("sahnaj_${cleanId}_key", cleanKey)
            if (cleanId == "anthropic") {
                editor.putString("sahnaj_claude_key", cleanKey)
            }
            if (cleanId == "gemini") {
                editor.putString(KEY_GEMINI_API_KEY, cleanKey)
                _apiKeyFlow.value = cleanKey
            }
            editor.apply()
            _providerKeysFlow.value = getAllProviderKeys()
        } catch (e: Exception) {
            Log.e(TAG, "Error storing encrypted API key for provider: $providerId", e)
        }
    }

    fun clearProviderApiKey(providerId: String) {
        val cleanId = normalizeProviderId(providerId)
        try {
            val editor = securePrefs.edit()
            editor.remove("$PREFIX_PROVIDER_KEY$cleanId")
            editor.remove("sahnaj_${cleanId}_key")
            if (cleanId == "anthropic") {
                editor.remove("sahnaj_claude_key")
            }
            if (cleanId == "gemini") {
                editor.remove(KEY_GEMINI_API_KEY)
                _apiKeyFlow.value = ""
            }
            editor.apply()
            _providerKeysFlow.value = getAllProviderKeys()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing encrypted API key for provider: $providerId", e)
        }
    }

    fun getAllProviderKeys(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        // Gemini fallback
        val geminiKey = getGeminiApiKey()
        if (geminiKey.isNotBlank()) {
            map["gemini"] = geminiKey
        }
        try {
            val allEntries = securePrefs.all
            for ((key, value) in allEntries) {
                if (key.startsWith(PREFIX_PROVIDER_KEY) && value is String && value.isNotBlank()) {
                    val providerId = normalizeProviderId(key.removePrefix(PREFIX_PROVIDER_KEY))
                    map[providerId] = value.trim()
                } else if (key.startsWith(PREFIX_SAHNAJ_KEY) && key.endsWith("_key") && value is String && value.isNotBlank()) {
                    val providerId = normalizeProviderId(key)
                    map[providerId] = value.trim()
                }
            }
            // Ensure derived/family model keys are recognized
            val openaiKey = map["openai"] ?: securePrefs.getString("sahnaj_openai_key", "")?.trim() ?: ""
            if (openaiKey.isNotBlank()) {
                if (!map.containsKey("openai_mini")) map["openai_mini"] = openaiKey
                if (!map.containsKey("openai_reasoning")) map["openai_reasoning"] = openaiKey
            }
            val claudeKey = map["claude"] ?: map["claude_sonnet"] ?: securePrefs.getString("sahnaj_claude_key", "")?.trim() ?: ""
            if (claudeKey.isNotBlank()) {
                if (!map.containsKey("claude_sonnet")) map["claude_sonnet"] = claudeKey
                if (!map.containsKey("claude_haiku")) map["claude_haiku"] = claudeKey
            }
            val deepseekKey = map["deepseek"] ?: map["deepseek_v3"] ?: securePrefs.getString("sahnaj_deepseek_key", "")?.trim() ?: ""
            if (deepseekKey.isNotBlank()) {
                if (!map.containsKey("deepseek_v3")) map["deepseek_v3"] = deepseekKey
                if (!map.containsKey("deepseek_r1")) map["deepseek_r1"] = deepseekKey
            }
            val mistralKey = map["mistral"] ?: map["mistral_large"] ?: securePrefs.getString("sahnaj_mistral_key", "")?.trim() ?: ""
            if (mistralKey.isNotBlank()) {
                if (!map.containsKey("mistral_large")) map["mistral_large"] = mistralKey
                if (!map.containsKey("mistral_codestral")) map["mistral_codestral"] = mistralKey
            }
            val groqKey = map["groq"] ?: map["groq_llama"] ?: securePrefs.getString("sahnaj_groq_key", "")?.trim() ?: ""
            if (groqKey.isNotBlank()) {
                if (!map.containsKey("groq_llama")) map["groq_llama"] = groqKey
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading all provider keys", e)
        }
        return map
    }

    fun getGeminiApiKey(): String {
        return try {
            val primary = securePrefs.getString(KEY_GEMINI_API_KEY, "")?.trim() ?: ""
            if (primary.isNotBlank()) primary else (securePrefs.getString("${PREFIX_PROVIDER_KEY}gemini", "")?.trim() ?: "")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading encrypted Gemini API key", e)
            ""
        }
    }

    fun setGeminiApiKey(apiKey: String) {
        setProviderApiKey("gemini", apiKey)
    }

    fun clearGeminiApiKey() {
        clearProviderApiKey("gemini")
    }
}

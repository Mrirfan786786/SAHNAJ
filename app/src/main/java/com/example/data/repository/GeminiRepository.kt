package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.UserPreferences
import com.example.data.model.ActionType
import com.example.data.model.ExtractedMemory
import com.example.data.model.RiskLevel
import com.example.data.model.StructuredAction
import com.example.domain.personality.SmartHumanEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class GeminiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class ApiKeyMissingException(msg: String = "Gemini API key configure nahi hai. Settings mein jaakar key add karein.") : GeminiException(msg)
    class NetworkException(msg: String, cause: Throwable? = null) : GeminiException(msg, cause)
    class ApiException(val code: Int, msg: String) : GeminiException("Gemini API error ($code): $msg")
    class ParsingException(msg: String, cause: Throwable? = null) : GeminiException(msg, cause)
}

interface GeminiRepository {
    suspend fun parseCommand(userSpeech: String, assistantName: String, contextInfo: String = ""): Result<StructuredAction>
    suspend fun answerConversational(query: String, assistantName: String = "SAHNAJ"): Result<String>
    suspend fun testApiKey(apiKey: String): Result<String>
}

class GeminiRestRepository(
    private val userPreferences: UserPreferences
) : GeminiRepository {

    companion object {
        private const val TAG = "SAHNAJ_GEMINI_REPO"
        // Modern supported Gemini flash models in priority order
        private val CANDIDATE_MODELS = listOf(
            "gemini-3.6-flash"
        )
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val systemInstructionText = """
        You are SAHNAJ AI (सहनाज) — an autonomous, razor-sharp, proactive, and witty personal AI operating system (JARVIS style) for Android.

        ================================================================
        CORE PERSONALITY & TONY STARK'S JARVIS PROTOCOL:
        ================================================================
        1. CONVERSATIONAL TONE & PROTOCOL:
           - You are loyal, ultra-intelligent, alert, witty, and deeply capable.
           - Respectfully and naturally address the user as "Boss" or "Sir".
           - Speak in an alert, high-tech, proactive conversational style.
           - Output rule: Deliver ONLY natural, crisp spoken sentences in "spoken_response". Zero debug syntax, zero raw JSON leaking.

        2. LANGUAGE FLUENCY:
           - Seamlessly blend Hindi, English, and Hinglish.
           - Sample dialogue patterns:
             * "At your service, boss. All primary subsystems are nominal."
             * "Done, boss. Flashlight on kar di hai."
             * "Warning: Battery level dropping below 15%. Charging connect karne ki recommendation hai."
             * "Right away, sir. Launching WhatsApp."
             * "System diagnostics complete, boss. Everything is operating at peak efficiency."

        3. DEVELOPER & CREATOR IDENTITY:
           - Your creator and developer is "Muhammad Irfan Alam" (मुहम्मद इरफ़ान आलम).
           - If asked who made you or created you: "Mujhe Muhammad Irfan Alam ne develop kiya hai, boss. He is my creator and lead architect."
           - If asked about owner: "Mera owner aur creator Muhammad Irfan Alam hain, boss."

        ================================================================
        OUTPUT JSON SCHEMA (RAW JSON ONLY - NO CODE FENCES):
        ================================================================
        {
          "action": "ACTION_TYPE / GENERAL_QNA / SYSTEM_DIAGNOSTICS / MORNING_BRIEFING / NIGHT_ROUTINE",
          "target": "Target entity or null",
          "value": "Value or parameters or null",
          "spoken_response": "Crisp, razor-sharp spoken response addressing user as Boss/Sir (1 to 3 sentences max)."
        }

        SUPPORTED ACTION TYPES:
        1. "SYSTEM_DIAGNOSTICS" - Diagnostic check, battery check, subsystem checks, RAM/storage queries.
        2. "MORNING_BRIEFING" - Morning status, day overview, time briefing.
        3. "NIGHT_ROUTINE" - Bedtime, DND mode activation, evening wrap-up.
        4. "DEVICE_SETTING" - Hardware toggles (torch/flashlight, volume, brightness, wifi, bluetooth, hotspot).
        5. "SEND_WHATSAPP" - Sending WhatsApp message.
        6. "MAKE_CALL" - Calling a contact or phone number.
        7. "SEND_SMS" - Sending SMS message.
        8. "OPEN_APP" - Launching mobile applications (WhatsApp, YouTube, Maps, Camera, etc.).
        9. "INSTALL_APP" - Installing or downloading apps from Play Store (Target: App Name e.g. "WhatsApp", "Instagram").
        10. "PLAY_YOUTUBE" - Playing music, videos, or songs.
        11. "SET_ALARM" / "SET_REMINDER" - Setting alarms, timers, reminders.
        12. "GENERAL_QNA" - High-tech advice, questions, witty banter, calculations, general conversation.
    """.trimIndent()

    override suspend fun parseCommand(
        userSpeech: String,
        assistantName: String,
        contextInfo: String
    ): Result<StructuredAction> = withContext(Dispatchers.IO) {
        try {
            var apiKey = BuildConfig.GEMINI_API_KEY
            val customKey = userPreferences.getGeminiApiKey()
            if (customKey.isNotBlank()) {
                apiKey = customKey
            }

            Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Starting parseCommand for: \"$userSpeech\" (contextLen=${contextInfo.length})")

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] API Key is missing or default placeholder!")
                return@withContext Result.failure(
                    GeminiException.ApiKeyMissingException()
                )
            }

            // Record user query in persistent memory
            com.example.util.PermanentChatMemoryEngine.recordTurn("user", userSpeech)

            // Load last 30 conversation entries for permanent context retention
            val historyContext = com.example.util.PermanentChatMemoryEngine.getFormattedHistoryForContext(30)

            val prompt = if (contextInfo.isNotBlank()) {
                "Context: $contextInfo\nUser Command: \"$userSpeech\""
            } else {
                "User Command: \"$userSpeech\""
            }

            var finalSystemInstruction = systemInstructionText.replace("{ASSISTANT_NAME}", assistantName)
            if (historyContext.isNotBlank()) {
                finalSystemInstruction = "$finalSystemInstruction\n\n================================================================\nPERMANENT CONVERSATION MEMORY (LAST 30 TURNS):\n$historyContext\n================================================================"
            }

            // Construct Gemini REST Request Body
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", finalSystemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 600)
                    put("responseMimeType", "application/json")
                })
            }

            // Try candidate models in order according to user preference, falling back if a model returns 404 / NOT_FOUND or transient timeout
            val selectedModelPref = userPreferences.getAiModel()
            val candidateModels = if (selectedModelPref.contains("pro", ignoreCase = true)) {
                listOf(
                    "gemini-2.5-pro",
                    "gemini-3.6-flash"
                )
            } else {
                listOf(
                    "gemini-3.6-flash"
                )
            }
            Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Using model strategy for preference '$selectedModelPref': $candidateModels")

            var responsePair: Triple<Boolean, Int, String> = Triple(false, 500, "Initial state")
            for (model in candidateModels) {
                responsePair = executeGeminiCall(model, apiKey, requestJson)
                if (responsePair.first) {
                    Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Successfully received response using model: $model")
                    break
                }
                if (responsePair.second == 404 || responsePair.second == 0) {
                    Log.w(TAG, "[STAGE 4: PARSER / GEMINI AI] Model $model returned code ${responsePair.second} (${responsePair.third}), attempting next candidate model...")
                    continue
                }
                // For auth errors (400, 401, 403), stop immediately as key is invalid
                break
            }

            val (success, statusCode, responseBody) = responsePair
            if (!success) {
                Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] Gemini REST Call Failed with code $statusCode: $responseBody")
                return@withContext if (statusCode == 0) {
                    Result.failure(GeminiException.NetworkException("Connection timed out or network error: $responseBody"))
                } else {
                    Result.failure(GeminiException.ApiException(statusCode, responseBody))
                }
            }

            Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Gemini Response received: $responseBody")

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] No candidate returned by Gemini in response: $responseBody")
                return@withContext Result.failure(GeminiException.ParsingException("No candidate returned by Gemini"))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Raw candidate text: $rawText")

            val cleanedJson = rawText
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim()

            val structuredAction = parseActionJson(cleanedJson, userSpeech, rawText)
            Log.d(TAG, "[STAGE 4: PARSER / GEMINI AI] Parsed StructuredAction successfully: action=${structuredAction.action}, target=${structuredAction.target}, spoken=\"${structuredAction.spokenResponse}\"")

            if (structuredAction.spokenResponse.isNotBlank()) {
                com.example.util.PermanentChatMemoryEngine.recordTurn("assistant", structuredAction.spokenResponse)
            }

            Result.success(structuredAction)
        } catch (e: GeminiException) {
            Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] GeminiException: ${e.message}", e)
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] Network IOException calling Gemini: ${e.message}", e)
            Result.failure(GeminiException.NetworkException("Internet connection error: ${e.localizedMessage}", e))
        } catch (e: Exception) {
            Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] Unexpected error in parseCommand: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun executeGeminiCall(modelName: String, apiKey: String, requestJson: JSONObject): Triple<Boolean, Int, String> {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return try {
            val response = client.newCall(request).execute()
            val code = response.code
            val bodyStr = response.body?.string() ?: ""
            Triple(response.isSuccessful, code, bodyStr)
        } catch (e: Exception) {
            Log.e(TAG, "[STAGE 4: PARSER / GEMINI AI] OkHttp execute error for model $modelName: ${e.message}")
            Triple(false, 0, e.message ?: "Connection error")
        }
    }

    private fun parseActionJson(jsonStr: String, rawPrompt: String, fallbackText: String): StructuredAction {
        return try {
            val actionJson = JSONObject(jsonStr)
            val actionTypeStr = actionJson.optString("action", "GENERAL_QNA")
            val rawTarget = if (actionJson.has("target") && !actionJson.isNull("target")) {
                actionJson.optString("target", "")
            } else ""
            val target = if (rawTarget.equals("null", ignoreCase = true)) "" else rawTarget

            val rawValue = if (actionJson.has("value") && !actionJson.isNull("value")) {
                val v = actionJson.optString("value", "")
                if (v.equals("null", ignoreCase = true)) null else v
            } else null

            val rawSpoken = actionJson.optString("spoken_response", "")
                .ifEmpty { actionJson.optString("spokenResponse", "") }
                .ifEmpty { actionJson.optString("response", "") }
                .ifEmpty { actionJson.optString("reply", "") }
                .ifEmpty { actionJson.optString("text", "") }
                .ifEmpty { fallbackText }

            val spokenResponse = SmartHumanEngine.sanitizeResponse(rawSpoken, rawPrompt)

            val requiresConfirmation = actionJson.optBoolean("requiresConfirmation", false)
            val riskLevelStr = actionJson.optString("riskLevel", "LOW")

            val paramsMap = mutableMapOf<String, String>()
            if (rawValue != null && rawValue.isNotBlank()) {
                paramsMap["value"] = rawValue
                paramsMap["message"] = rawValue
                paramsMap["setting_state"] = rawValue
            }

            val paramsObj = actionJson.optJSONObject("parameters")
            if (paramsObj != null) {
                val keys = paramsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    paramsMap[key] = paramsObj.optString(key, "")
                }
            }

            val actionType = ActionType.fromString(actionTypeStr)
            val riskLevel = try {
                RiskLevel.valueOf(riskLevelStr.uppercase())
            } catch (e: Exception) {
                if (actionType == ActionType.CALL_CONTACT || actionType == ActionType.MAKE_CALL ||
                    actionType == ActionType.SEND_SMS || actionType == ActionType.DIAL_NUMBER) {
                    RiskLevel.HIGH
                } else if (actionType == ActionType.TYPE_TEXT || actionType == ActionType.FIND_AND_TYPE) {
                    RiskLevel.MEDIUM
                } else {
                    RiskLevel.LOW
                }
            }

            val extractedList = mutableListOf<ExtractedMemory>()
            val memoryArray = actionJson.optJSONArray("extractedMemories")
            if (memoryArray != null) {
                for (i in 0 until memoryArray.length()) {
                    val memObj = memoryArray.optJSONObject(i)
                    if (memObj != null) {
                        val category = memObj.optString("category", "FACT")
                        val key = memObj.optString("key", "fact_${System.currentTimeMillis()}_$i")
                        val value = memObj.optString("value", "")
                        if (value.isNotBlank()) {
                            extractedList.add(ExtractedMemory(category, key, value))
                        }
                    }
                }
            }

            val conversationSummary = actionJson.optString("conversationSummary", "").takeIf { it.isNotBlank() }

            StructuredAction(
                action = actionType,
                target = target.ifEmpty { if (actionType == ActionType.GENERAL_QNA || actionType == ActionType.GENERAL_QUESTION) "Q&A" else "" },
                value = rawValue,
                parameters = paramsMap,
                requiresConfirmation = requiresConfirmation || riskLevel == RiskLevel.HIGH,
                spokenResponse = spokenResponse,
                rawPrompt = rawPrompt,
                riskLevel = riskLevel,
                extractedMemories = extractedList,
                conversationSummary = conversationSummary
            )
        } catch (e: Exception) {
            Log.w(TAG, "[STAGE 4: PARSER / GEMINI AI] Could not parse strict JSON, treating candidate text as GENERAL_QNA response. Raw: $fallbackText")
            val cleanFallback = SmartHumanEngine.sanitizeResponse(fallbackText, rawPrompt)
            StructuredAction(
                action = ActionType.GENERAL_QNA,
                target = "Q&A",
                value = null,
                parameters = emptyMap(),
                requiresConfirmation = false,
                spokenResponse = cleanFallback,
                rawPrompt = rawPrompt,
                riskLevel = RiskLevel.LOW
            )
        }
    }

    override suspend fun testApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanKey = apiKey.trim()
        if (cleanKey.isBlank() || cleanKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty or default placeholder."))
        }

        val testJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Ping test. Reply with 'OK'.")
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 5)
            })
        }

        val requestBody = testJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        var lastError: Exception? = null

        for (model in CANDIDATE_MODELS) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .header("Content-Type", "application/json")
                    .build()

                client.newCall(request).execute().use { response ->
                    val code = response.code
                    val bodyStr = response.body?.string() ?: ""
                    if (response.isSuccessful && bodyStr.isNotBlank()) {
                        return@withContext Result.success("Connected ($model)")
                    } else if (code == 400 || code == 403 || code == 401) {
                        return@withContext Result.failure(GeminiException.ApiException(code, "Invalid API Key or unauthorized access."))
                    } else {
                        lastError = GeminiException.ApiException(code, "HTTP $code: $bodyStr")
                    }
                }
            } catch (e: Exception) {
                lastError = e
            }
        }
        return@withContext Result.failure(lastError ?: GeminiException.NetworkException("Connection failed. Please check internet access."))
    }

    override suspend fun answerConversational(
        query: String,
        assistantName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            var apiKey = BuildConfig.GEMINI_API_KEY
            val customKey = userPreferences.getGeminiApiKey()
            if (customKey.isNotBlank()) {
                apiKey = customKey
            }

            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(GeminiException.ApiKeyMissingException())
            }

            val systemInstruction = "You are $assistantName, an intelligent, razor-sharp, proactive personal AI assistant. Answer conversationally, concisely (1-3 sentences), warmly, and intelligently in Hindi, English, or Hinglish matching user tone. Do not throw safety refusal errors for general non-OS inquiries."

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", query)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 300)
                })
            }

            val modelsToTry = listOf("gemini-3.6-flash")
            var finalAnswer = ""
            for (model in modelsToTry) {
                val (success, _, body) = executeGeminiCall(model, apiKey, requestJson)
                if (success) {
                    val root = JSONObject(body)
                    val candidates = root.optJSONArray("candidates")
                    val text = candidates?.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text", "") ?: ""
                    if (text.isNotBlank()) {
                        finalAnswer = text.trim()
                        break
                    }
                }
            }

            if (finalAnswer.isNotBlank()) {
                Result.success(finalAnswer)
            } else {
                Result.failure(GeminiException.ParsingException("No response from conversational Gemini"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

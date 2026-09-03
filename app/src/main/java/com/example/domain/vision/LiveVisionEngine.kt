package com.example.domain.vision

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.SecurePreferences
import com.example.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class LiveVisionAnalysisResult(
    val answer: String,
    val detectedObjects: List<String> = emptyList(),
    val latencyMs: Long = 0L,
    val modelUsed: String = "gemini-3.5-flash"
)

class LiveVisionEngine(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val securePreferences: SecurePreferences
) {
    companion object {
        private const val TAG = "LiveVisionEngine"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        // Preferred Gemini Vision Models following system guidance
        private val MULTIMODAL_MODELS = listOf(
            "gemini-3.5-flash",
            "gemini-flash-latest",
            "gemini-2.5-flash",
            "gemini-3.1-pro-preview"
        )

        const val SYSTEM_DIRECTIVE = """You are SAHNAJ, a real-time multimodal visual companion. Observe the frame provided and give crisp, precise, and practical answers in Hindi/Hinglish.
Identify objects, mechanical parts, texts, gadgets, or UI elements instantly without verbose fluff.
Speak directly to the point like an expert technician and smart personal assistant."""
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    fun getGeminiApiKey(): String {
        val prefKey = userPreferences.getGeminiApiKey().trim()
        if (prefKey.isNotBlank()) return prefKey

        val secureKey = securePreferences.getProviderApiKey("gemini").trim()
        if (secureKey.isNotBlank()) return secureKey

        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Downscales and compresses a bitmap into a fast, lightweight JPEG byte array and Base64 string.
     */
    fun optimizeBitmapForVision(bitmap: Bitmap, maxDimension: Int = 800, quality: Int = 80): Pair<ByteArray, String> {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var scaled = bitmap

        if (originalWidth > maxDimension || originalHeight > maxDimension) {
            val scale = if (originalWidth > originalHeight) {
                maxDimension.toFloat() / originalWidth
            } else {
                maxDimension.toFloat() / originalHeight
            }
            val matrix = Matrix().apply { postScale(scale, scale) }
            scaled = Bitmap.createBitmap(bitmap, 0, 0, originalWidth, originalHeight, matrix, true)
        }

        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return Pair(byteArray, base64)
    }

    /**
     * Executes real-time multimodal visual analysis on the given frame and query prompt.
     */
    suspend fun analyzeFrame(
        bitmap: Bitmap,
        userPrompt: String,
        customContext: String = ""
    ): Result<LiveVisionAnalysisResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val apiKey = getGeminiApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API Key missing. Please set your Gemini key in API & Cloud Settings.")
                )
            }

            val (_, base64Data) = optimizeBitmapForVision(bitmap, maxDimension = 800, quality = 78)

            val effectivePrompt = if (userPrompt.isNotBlank()) {
                userPrompt
            } else {
                "Is frame ko dekhein aur batayein kya dikh raha hai (Identify the primary object, gadget, component, or screen element concisely in Hindi/Hinglish)."
            }

            val finalPrompt = if (customContext.isNotBlank()) {
                "Context: $customContext\nUser Query: $effectivePrompt"
            } else {
                effectivePrompt
            }

            var lastError: Exception? = null
            for (model in MULTIMODAL_MODELS) {
                try {
                    val result = callGeminiVision(apiKey, model, base64Data, finalPrompt)
                    if (result.isSuccess) {
                        val responseText = result.getOrThrow()
                        val latency = System.currentTimeMillis() - startTime
                        val extractedObjects = extractQuickKeywords(responseText)

                        return@withContext Result.success(
                            LiveVisionAnalysisResult(
                                answer = responseText,
                                detectedObjects = extractedObjects,
                                latencyMs = latency,
                                modelUsed = model
                            )
                        )
                    } else {
                        lastError = result.exceptionOrNull() as? Exception
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Model $model encountered error, falling back...", e)
                    lastError = e
                }
            }

            Result.failure(lastError ?: Exception("Unable to analyze vision frame with Gemini AI."))
        } catch (e: Exception) {
            Log.e(TAG, "Frame analysis failed", e)
            Result.failure(e)
        }
    }

    private suspend fun callGeminiVision(
        apiKey: String,
        model: String,
        base64Data: String,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$GEMINI_BASE_URL/$model:generateContent?key=$apiKey"

            val partsArray = JSONArray().apply {
                // Image frame inline data
                put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", "image/jpeg")
                        put("data", base64Data)
                    })
                })
                // User voice/text prompt
                put(JSONObject().apply {
                    put("text", prompt)
                })
            }

            val contentObject = JSONObject().apply {
                put("role", "user")
                put("parts", partsArray)
            }

            val systemInstructionObject = JSONObject().apply {
                put("role", "system")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", SYSTEM_DIRECTIVE)
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply { put(contentObject) })
                put("system_instruction", systemInstructionObject)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 600)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                response.close()
                return@withContext Result.failure(Exception("Gemini API error ${response.code}: $responseBody"))
            }
            response.close()

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) {
                        return@withContext Result.success(text.trim())
                    }
                }
            }

            Result.failure(Exception("No content returned from Gemini Vision model."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractQuickKeywords(text: String): List<String> {
        val words = text.split(" ", "\n", ",", ".")
            .map { it.trim().trim('"', '\'', '*', '#') }
            .filter { it.length > 3 && !it.equals("this", true) && !it.equals("that", true) && !it.equals("hai", true) && !it.equals("aur", true) }
            .distinct()
            .take(4)
        return words
    }
}

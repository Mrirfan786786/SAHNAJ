package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.SecurePreferences
import com.example.data.local.UserPreferences
import com.example.data.model.GeneratedPromptResult
import com.example.data.model.PromptAspectRatio
import com.example.data.model.PromptStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PromptStudioRepository(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val securePreferences: SecurePreferences
) {
    companion object {
        private const val TAG = "PromptStudioRepo"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

        val DEFAULT_NEGATIVE_PROMPTS = """
            blurry, low resolution, bad anatomy, deformed hands, extra fingers, missing limbs, fused fingers, distorted face, cross-eyed, bad proportions, unnatural skin texture, oversaturated, text errors, watermark, signature, jpeg artifacts, low quality, grainy, duplicate elements, out of frame, poorly drawn eyes
        """.trimIndent()
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _recentPrompts = MutableStateFlow<List<GeneratedPromptResult>>(emptyList())
    val recentPrompts: StateFlow<List<GeneratedPromptResult>> = _recentPrompts.asStateFlow()

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

    suspend fun generatePromptStudioPackage(
        subject: String,
        style: PromptStyle,
        aspectRatio: PromptAspectRatio,
        customLighting: String = ""
    ): Result<GeneratedPromptResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val apiKey = getGeminiApiKey()

            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                val systemPrompt = """
                    You are an elite Prompt Engineer for cutting-edge AI image generators: FLUX.1, Midjourney v6.1, and Stable Diffusion 3 / SDXL.
                    
                    TASK:
                    Given the user's Subject, Selected Visual Style, and Target Aspect Ratio, generate an optimized, award-winning Prompt Engineering Package.
                    
                    Subject: "$subject"
                    Style: ${style.title} (${style.description})
                    Keywords to incorporate: ${style.visualKeywords}
                    Aspect Ratio: ${aspectRatio.ratioValue} (${aspectRatio.midjourneyFlag})
                    ${if (customLighting.isNotBlank()) "Lighting preference: $customLighting" else ""}
                    
                    OUTPUT FORMAT (STRICT JSON ONLY - no markdown wrap):
                    {
                      "master_prompt": "A complete, highly detailed master descriptive prompt focusing on subject morphology, materials, textures, lighting, camera angle, and background ambiance.",
                      "flux_prompt": "Prompt tuned specifically for FLUX.1 (natural descriptive language, precise spatial relationships, photographic terms, depth of field, 8k resolution, photorealism).",
                      "midjourney_prompt": "Prompt formatted for Midjourney v6 with stylistic tokens, lighting parameters, and aspect ratio flags like ${aspectRatio.midjourneyFlag} --v 6.1 --style raw --stylize 250",
                      "sd_prompt": "Prompt formatted for Stable Diffusion 3 / SDXL (comma-separated quality boosters, masterwork tags, specific camera lenses like 85mm f/1.4, Octane Render, volumetric lighting).",
                      "negative_prompt": "A comprehensive list of negative prompt tokens preventing artifacts, bad anatomy, blur, text glitches, and poor composition.",
                      "lighting_and_camera": "1-2 sentences summarizing recommended key lights, rim lights, and camera lens angle."
                    }
                """.trimIndent()

                val apiResult = callGeminiPromptEngine(apiKey, systemPrompt)
                if (apiResult.isSuccess) {
                    val jsonStr = apiResult.getOrThrow()
                    val result = parseGeneratedJson(jsonStr, subject, style, aspectRatio, startTime)
                    if (result != null) {
                        _recentPrompts.value = listOf(result) + _recentPrompts.value.take(25)
                        return@withContext Result.success(result)
                    }
                }
            }

            // High Quality Offline Synthesis Fallback if API not configured or network failed
            val fallbackResult = generateHighQualityFallback(subject, style, aspectRatio, startTime)
            _recentPrompts.value = listOf(fallbackResult) + _recentPrompts.value.take(25)
            Result.success(fallbackResult)
        } catch (e: Exception) {
            Log.e(TAG, "Prompt generation failed, using local engine", e)
            val fallback = generateHighQualityFallback(subject, style, aspectRatio, startTime)
            Result.success(fallback)
        }
    }

    private suspend fun callGeminiPromptEngine(apiKey: String, prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val models = listOf("gemini-2.5-flash", "gemini-3.5-flash", "gemini-flash-latest", "gemini-3.1-pro-preview")

        for (model in models) {
            try {
                val url = "$GEMINI_BASE_URL/$model:generateContent?key=$apiKey"
                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.3)
                        put("maxOutputTokens", 2048)
                        put("responseMimeType", "application/json")
                    })
                }

                val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val body = response.body?.string() ?: ""
                response.close()

                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.getJSONObject(0)
                            ?.optString("text", "") ?: ""

                        if (text.isNotBlank()) {
                            return@withContext Result.success(text)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Model $model failed for prompt engine: ${e.message}")
            }
        }
        Result.failure(Exception("All prompt engine models failed"))
    }

    private fun parseGeneratedJson(
        jsonStr: String,
        subject: String,
        style: PromptStyle,
        aspectRatio: PromptAspectRatio,
        startTime: Long
    ): GeneratedPromptResult? {
        return try {
            val cleanJson = jsonStr.replace("```json", "").replace("```", "").trim()
            val obj = JSONObject(cleanJson)

            GeneratedPromptResult(
                subject = subject,
                style = style,
                aspectRatio = aspectRatio,
                masterPrompt = obj.optString("master_prompt", "").ifBlank { subject },
                fluxPrompt = obj.optString("flux_prompt", "").ifBlank { obj.optString("master_prompt", subject) },
                midjourneyPrompt = obj.optString("midjourney_prompt", "").ifBlank { "${obj.optString("master_prompt", subject)} ${aspectRatio.midjourneyFlag} --v 6.1" },
                sdPrompt = obj.optString("sd_prompt", "").ifBlank { obj.optString("master_prompt", subject) },
                negativePrompt = obj.optString("negative_prompt", "").ifBlank { DEFAULT_NEGATIVE_PROMPTS },
                lightingAndCameraNotes = obj.optString("lighting_and_camera", "Cinematic three-point studio lighting with anamorphic lens bokeh."),
                timestamp = System.currentTimeMillis(),
                durationMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Prompt JSON", e)
            null
        }
    }

    private fun generateHighQualityFallback(
        subject: String,
        style: PromptStyle,
        aspectRatio: PromptAspectRatio,
        startTime: Long
    ): GeneratedPromptResult {
        val master = "A stunning, masterfully crafted visual of $subject, ${style.visualKeywords}, highly detailed composition, flawless textures, rich atmosphere, 8K ultra-sharp resolution, cinematic color grading."

        val flux = "An ultra-detailed photograph of $subject. Rendered in ${style.title} style. Intricate fine textures, perfectly balanced specular highlights, natural depth of field with 50mm f/1.2 prime lens, soft atmospheric haze, volumetric rim lighting, 8k masterpiece render."

        val midjourney = "A captivating depiction of $subject, in the signature style of ${style.title}, dynamic composition, vibrant color harmony, photorealistic lighting, ray traced reflections, cinematic composition ${aspectRatio.midjourneyFlag} --v 6.1 --style raw --stylize 250"

        val sd = "$subject, ${style.visualKeywords}, octane render 8k, unreal engine 5, masterpiece, highly detailed, dramatic lighting, sharp focus, 85mm portrait photography, award-winning illustration"

        return GeneratedPromptResult(
            subject = subject,
            style = style,
            aspectRatio = aspectRatio,
            masterPrompt = master,
            fluxPrompt = flux,
            midjourneyPrompt = midjourney,
            sdPrompt = sd,
            negativePrompt = DEFAULT_NEGATIVE_PROMPTS,
            lightingAndCameraNotes = "Key light from 45° angle with saturated rim light. Shot on 85mm prime lens at f/1.8 with creamy bokeh background separation.",
            timestamp = System.currentTimeMillis(),
            durationMs = System.currentTimeMillis() - startTime
        )
    }
}

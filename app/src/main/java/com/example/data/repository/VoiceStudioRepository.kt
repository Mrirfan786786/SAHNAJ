package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.data.local.SecurePreferences
import com.example.data.model.ElevenLabsVoice
import com.example.data.model.GeneratedVoiceItem
import com.example.data.model.VoiceStudioPresets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit

class VoiceStudioRepository(
    private val context: Context,
    private val securePreferences: SecurePreferences
) {
    companion object {
        private const val TAG = "VoiceStudioRepo"
        private const val ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1"
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _recentVoices = MutableStateFlow<List<GeneratedVoiceItem>>(emptyList())
    val recentVoices: StateFlow<List<GeneratedVoiceItem>> = _recentVoices.asStateFlow()

    fun getApiKey(): String {
        val key = securePreferences.getProviderApiKey("elevenlabs").trim()
        if (key.isNotBlank()) return key
        val genericVoiceKey = securePreferences.getProviderApiKey("voice").trim()
        if (genericVoiceKey.isNotBlank()) return genericVoiceKey
        return ""
    }

    suspend fun generateVoice(
        text: String,
        voiceId: String,
        voiceName: String,
        modelId: String = "eleven_multilingual_v2",
        stability: Float = 0.5f,
        similarityBoost: Float = 0.75f,
        style: Float = 0.0f,
        useSpeakerBoost: Boolean = true,
        onProgressUpdate: (Float, String) -> Unit = { _, _ -> }
    ): Result<GeneratedVoiceItem> = withContext(Dispatchers.IO) {
        try {
            val cleanText = text.trim()
            if (cleanText.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Script text cannot be empty (कृपया टेक्स्ट या स्क्रिप्ट दर्ज करें)"))
            }

            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("ElevenLabs API Key is missing. Please configure your key in Settings > API & Cloud Settings.")
                )
            }

            val cleanVoiceId = voiceId.trim().ifBlank { "21m00Tcm4TlvDq8ikWAM" } // Default to Rachel if empty
            val normalizedScript = com.example.voice.TtsPhoneticNormalizer.normalizeForSpeech(cleanText).ifBlank { cleanText }

            onProgressUpdate(0.15f, "Connecting to ElevenLabs Neural Voice API...")

            val requestJson = JSONObject().apply {
                put("text", normalizedScript)
                put("model_id", modelId)
                val voiceSettings = JSONObject().apply {
                    put("stability", stability.toDouble())
                    put("similarity_boost", similarityBoost.toDouble())
                    put("style", style.toDouble())
                    put("use_speaker_boost", useSpeakerBoost)
                }
                put("voice_settings", voiceSettings)
            }

            val endpoint = "$ELEVENLABS_BASE_URL/text-to-speech/$cleanVoiceId?output_format=mp3_44100_128"

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "audio/mpeg")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            onProgressUpdate(0.40f, "Synthesizing multilingual speech ($voiceName)...")

            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                response.close()
                val message = parseElevenLabsError(response.code, errorBody)
                return@withContext Result.failure(Exception(message))
            }

            val responseBody = response.body ?: run {
                response.close()
                return@withContext Result.failure(Exception("Empty audio stream received from ElevenLabs"))
            }

            onProgressUpdate(0.75f, "Processing audio stream & generating waveform...")

            // Save audio to app cache directory
            val audioDir = File(context.cacheDir, "voice_studio").apply { mkdirs() }
            val audioFile = File(audioDir, "sahnaj_voice_${System.currentTimeMillis()}.mp3")

            val inputStream: InputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(audioFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            response.close()

            onProgressUpdate(0.92f, "Finalizing high-fidelity audio track...")

            // Extract duration
            val durationMs = extractAudioDuration(audioFile)

            val item = GeneratedVoiceItem(
                text = cleanText,
                voiceName = voiceName,
                voiceId = cleanVoiceId,
                modelId = modelId,
                audioFilePath = audioFile.absolutePath,
                durationMs = durationMs,
                timestamp = System.currentTimeMillis(),
                stability = stability,
                similarityBoost = similarityBoost,
                fileSizeBytes = audioFile.length()
            )

            // Update recent voices
            val updated = listOf(item) + _recentVoices.value.take(15)
            _recentVoices.value = updated

            onProgressUpdate(1.0f, "Voiceover Generation Complete! 🎙️")
            Result.success(item)
        } catch (e: Exception) {
            Log.e(TAG, "ElevenLabs Voice Generation Failed", e)
            Result.failure(Exception(e.message ?: "Voice synthesis failed. Check connection & API key."))
        }
    }

    private fun extractAudioDuration(file: File): Long {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            time?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun parseElevenLabsError(code: Int, body: String): String {
        return try {
            if (body.isNotBlank()) {
                val json = JSONObject(body)
                if (json.has("detail")) {
                    val detail = json.get("detail")
                    if (detail is JSONObject && detail.has("message")) {
                        return detail.getString("message")
                    } else if (detail is String) {
                        return detail
                    }
                }
                if (json.has("message")) {
                    return json.getString("message")
                }
            }
            when (code) {
                401 -> "Invalid ElevenLabs API Key. Please verify in Settings."
                429 -> "ElevenLabs quota/credits exceeded or rate limited."
                400 -> "Invalid voice parameters or Voice ID not found."
                else -> "ElevenLabs synthesis failed with HTTP status code $code."
            }
        } catch (_: Exception) {
            "ElevenLabs API returned HTTP error $code"
        }
    }

    suspend fun downloadMp3ToMediaStore(voiceItem: GeneratedVoiceItem): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(voiceItem.audioFilePath)
            if (!sourceFile.exists() || sourceFile.length() == 0L) {
                return@withContext Result.failure(Exception("Source audio file not found"))
            }

            val fileName = "SAHNAJ_Voice_${voiceItem.voiceName.replace(" ", "_")}_${System.currentTimeMillis()}.mp3"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/SAHNAJ_AI/VoiceStudio")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                    put(MediaStore.Audio.Media.ARTIST, "SAHNAJ AI Voice Studio")
                    put(MediaStore.Audio.Media.ALBUM, "SAHNAJ AI Creations")
                    put(MediaStore.Audio.Media.TITLE, "Voiceover by ${voiceItem.voiceName}")
                }

                val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                context.contentResolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)

                Result.success(uri)
            } else {
                val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "SAHNAJ_AI/VoiceStudio")
                if (!musicDir.exists()) musicDir.mkdirs()
                val destFile = File(musicDir, fileName)

                sourceFile.inputStream().use { input ->
                    FileOutputStream(destFile).use { out ->
                        input.copyTo(out)
                    }
                }

                val uri = Uri.fromFile(destFile)
                Result.success(uri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download MP3", e)
            Result.failure(Exception("Could not save MP3: ${e.message}"))
        }
    }
}

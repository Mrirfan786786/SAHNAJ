package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import com.example.data.local.SecurePreferences
import com.example.data.local.UserPreferences
import com.example.data.model.VoiceProfileItem
import com.example.data.model.VoiceStudioCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * DualVoiceEngine: The Core Voice Engine of SAHNAJ AI.
 *
 * Supports:
 * 1. Online Mode (ElevenLabs API):
 *    - Synthesizes using Multilingual v2 API with optimized stability (0.75) and similarity boost (0.85).
 *    - Automatic disk caching of audio blobs for instant zero-delay playback.
 * 2. Offline / Fallback Mode (Android Native TTS):
 *    - Dynamically maps each of the 50 voice profiles to the closest native Android TTS parameters
 *      (Pitch, SpeechRate, Locale: hi-IN, ur-PK, en-IN, en-US).
 *    - 100% uninterrupted voice responses regardless of connectivity.
 * 3. 2-Second Instant Voice Previews for all 50 voices.
 */
class DualVoiceEngine(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val securePreferences: SecurePreferences,
    private val textToSpeechManager: TextToSpeechManager
) {
    companion object {
        private const val TAG = "DualVoiceEngine"
        private const val ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1"
        const val OPTIMIZED_STABILITY = 0.75f
        const val OPTIMIZED_SIMILARITY_BOOST = 0.85f
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val cacheDir: File = File(context.cacheDir, "sahnaj_voice_blobs").apply { mkdirs() }

    private var mediaPlayer: MediaPlayer? = null
    private var activePreviewJob: Job? = null

    private val _currentlyPlayingVoiceId = MutableStateFlow<String?>(null)
    val currentlyPlayingVoiceId: StateFlow<String?> = _currentlyPlayingVoiceId.asStateFlow()

    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    private val _activeVoiceId = MutableStateFlow(userPreferences.getActiveVoiceId())
    val activeVoiceId: StateFlow<String> = _activeVoiceId.asStateFlow()

    init {
        scope.launch {
            userPreferences.activeVoiceFlow.collect { voiceId ->
                _activeVoiceId.value = voiceId
            }
        }
    }

    /**
     * Retrieves the ElevenLabs API Key if configured in Settings.
     */
    fun getElevenLabsApiKey(): String {
        val directKey = securePreferences.getProviderApiKey("elevenlabs").trim()
        if (directKey.isNotBlank()) return directKey
        val genericKey = securePreferences.getProviderApiKey("voice").trim()
        if (genericKey.isNotBlank()) return genericKey
        return ""
    }

    fun isOnlineNeuralAvailable(): Boolean {
        return getElevenLabsApiKey().isNotBlank()
    }

    /**
     * Sets the active voice for the entire application and persists it in `sahnaj_active_voice`.
     */
    fun selectActiveVoice(voiceId: String) {
        userPreferences.setActiveVoiceId(voiceId)
        _activeVoiceId.value = voiceId
        val profile = VoiceStudioCatalog.findVoiceById(voiceId)
        Log.d(TAG, "Selected active voice: ${profile.name} (${profile.gender}, ID: $voiceId)")
    }

    /**
     * Returns the currently active VoiceProfileItem.
     */
    fun getActiveVoiceProfile(): VoiceProfileItem {
        return VoiceStudioCatalog.findVoiceById(_activeVoiceId.value)
    }

    /**
     * Plays a high-speed 2-second voice preview for any voice profile.
     * Uses cached ElevenLabs audio if online, otherwise triggers native TTS preview instantly.
     */
    fun previewVoice(
        voice: VoiceProfileItem,
        customText: String? = null,
        onComplete: (() -> Unit)? = null
    ) {
        stopPlayback()
        val rawText = customText ?: voice.previewSampleText
        val phoneticText = TtsPhoneticNormalizer.normalizeForSpeech(rawText)

        _currentlyPlayingVoiceId.value = voice.id
        _isPlayingPreview.value = true

        activePreviewJob = scope.launch {
            val apiKey = getElevenLabsApiKey()
            var playedSuccessfully = false

            if (apiKey.isNotBlank()) {
                try {
                    val audioFile = getOrSynthesizeElevenLabsAudio(
                        text = phoneticText,
                        voiceId = voice.elevenLabsVoiceId,
                        apiKey = apiKey
                    )

                    if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                        withContext(Dispatchers.Main) {
                            playAudioFile(audioFile) {
                                _isPlayingPreview.value = false
                                _currentlyPlayingVoiceId.value = null
                                onComplete?.invoke()
                            }
                        }
                        playedSuccessfully = true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Online ElevenLabs preview failed for ${voice.name}: ${e.message}. Falling back to TTS.")
                }
            }

            if (!playedSuccessfully) {
                // Offline Native Android TTS Fallback
                withContext(Dispatchers.Main) {
                    textToSpeechManager.previewVoice(
                        voice = null,
                        pitch = voice.ttsPitch,
                        rate = voice.ttsSpeechRate,
                        sampleText = phoneticText
                    )
                    // Auto-release preview state after brief duration
                    scope.launch {
                        kotlinx.coroutines.delay(2800)
                        if (_currentlyPlayingVoiceId.value == voice.id) {
                            _isPlayingPreview.value = false
                            _currentlyPlayingVoiceId.value = null
                            onComplete?.invoke()
                        }
                    }
                }
            }
        }
    }

    /**
     * Speaks the assistant response using either online ElevenLabs audio (if key configured)
     * or instant offline Android TTS with the selected profile's acoustic tuning.
     */
    fun speakAssistantResponse(
        text: String,
        onComplete: (() -> Unit)? = null
    ) {
        val profile = getActiveVoiceProfile()
        val apiKey = getElevenLabsApiKey()
        val phoneticText = TtsPhoneticNormalizer.normalizeForSpeech(text)

        if (apiKey.isNotBlank()) {
            scope.launch {
                try {
                    val audioFile = getOrSynthesizeElevenLabsAudio(
                        text = phoneticText,
                        voiceId = profile.elevenLabsVoiceId,
                        apiKey = apiKey
                    )
                    if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                        withContext(Dispatchers.Main) {
                            playAudioFile(audioFile, onComplete)
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Online speech failed: ${e.message}. Using Native TTS fallback.")
                }

                // Fallback to TTS
                withContext(Dispatchers.Main) {
                    textToSpeechManager.speak(phoneticText, profile.ttsSpeechRate, onComplete)
                }
            }
        } else {
            // Offline Mode: Native TTS directly with configured pitch & speed
            textToSpeechManager.speak(phoneticText, profile.ttsSpeechRate, onComplete)
        }
    }

    /**
     * Synthesizes audio via ElevenLabs Multilingual v2 with caching to prevent duplicate API requests.
     */
    private suspend fun getOrSynthesizeElevenLabsAudio(
        text: String,
        voiceId: String,
        apiKey: String
    ): File? = withContext(Dispatchers.IO) {
        val cacheKey = hashString("$voiceId:$text")
        val cachedFile = File(cacheDir, "voice_$cacheKey.mp3")

        if (cachedFile.exists() && cachedFile.length() > 500) {
            Log.d(TAG, "Cache HIT for voiceId=$voiceId, text=\"${text.take(20)}...\"")
            return@withContext cachedFile
        }

        try {
            val requestJson = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", OPTIMIZED_STABILITY.toDouble())
                    put("similarity_boost", OPTIMIZED_SIMILARITY_BOOST.toDouble())
                    put("style", 0.0)
                    put("use_speaker_boost", true)
                }
                put("voice_settings", voiceSettings)
            }

            val endpoint = "$ELEVENLABS_BASE_URL/text-to-speech/$voiceId?output_format=mp3_44100_128"
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "audio/mpeg")
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: ""
                response.close()
                Log.w(TAG, "ElevenLabs API returned ${response.code}: $err")
                return@withContext null
            }

            val body = response.body ?: run {
                response.close()
                return@withContext null
            }

            val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}.mp3")
            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(tempFile)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            response.close()

            if (tempFile.exists() && tempFile.length() > 500) {
                tempFile.renameTo(cachedFile)
                return@withContext cachedFile
            }
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "ElevenLabs synthesis error: ${e.message}", e)
            return@withContext null
        }
    }

    private fun playAudioFile(file: File, onDone: (() -> Unit)?) {
        try {
            stopPlayback()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
                start()

                setOnCompletionListener {
                    _isPlayingPreview.value = false
                    _currentlyPlayingVoiceId.value = null
                    onDone?.invoke()
                    stopPlayback()
                }

                setOnErrorListener { _, _, _ ->
                    _isPlayingPreview.value = false
                    _currentlyPlayingVoiceId.value = null
                    onDone?.invoke()
                    stopPlayback()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "MediaPlayer error playing audio: ${e.message}", e)
            _isPlayingPreview.value = false
            _currentlyPlayingVoiceId.value = null
            onDone?.invoke()
        }
    }

    fun stopPlayback() {
        activePreviewJob?.cancel()
        activePreviewJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlayingPreview.value = false
        _currentlyPlayingVoiceId.value = null
    }

    private fun hashString(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

package com.example.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance Text-to-Speech Manager calibrated for SahNaj's young,
 * energetic, friendly female persona.
 *
 * Implements:
 * 1. Smart programmatic female voice ranking & selection across Google & OEM engines.
 * 2. Tuned pitch (1.10x natural young female, 1.14x fallback) & brisk speech rate (1.08x).
 * 3. Bilingual support (Hindi/Hinglish & English) with seamless per-utterance adaptation.
 * 4. Resilient fallback handling with automatic pitch elevation for generic voices.
 */
class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentLanguage: String = "Hinglish"

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val completionCallbacks = ConcurrentHashMap<String, () -> Unit>()
    private val pendingSpeechQueue = mutableListOf<PendingSpeech>()

    private data class PendingSpeech(val text: String, val rate: Float, val onComplete: (() -> Unit)?)

    init {
        try {
            Log.d(TAG, "[STAGE 6: TTS] Initializing Android TextToSpeech engine for young female voice...")
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "[STAGE 6: TTS] Failed to create TextToSpeech instance", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.d(TAG, "[STAGE 6: TTS] TTS engine initialization SUCCESS")
            isInitialized = true

            // Configure optimal young female voice & baseline acoustic parameters
            applyVoiceConfiguration(currentLanguage)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "[STAGE 6: TTS] Utterance started: $utteranceId")
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "[STAGE 6: TTS] Utterance completed: $utteranceId")
                    _isSpeaking.value = false
                    utteranceId?.let { id ->
                        completionCallbacks.remove(id)?.let { callback ->
                            mainHandler.post { callback.invoke() }
                        }
                    }
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "[STAGE 6: TTS] Utterance error on id: $utteranceId")
                    _isSpeaking.value = false
                    utteranceId?.let { id ->
                        completionCallbacks.remove(id)?.let { callback ->
                            mainHandler.post { callback.invoke() }
                        }
                    }
                }
            })

            // Process any queued speech requests
            synchronized(pendingSpeechQueue) {
                while (pendingSpeechQueue.isNotEmpty()) {
                    val next = pendingSpeechQueue.removeAt(0)
                    speak(next.text, next.rate, next.onComplete)
                }
            }
        } else {
            Log.e(TAG, "[STAGE 6: TTS] TTS engine initialization failed with status: $status")
            isInitialized = false
        }
    }

    /**
     * Reconfigures TTS engine for the requested language preference and selects
     * the optimal young female voice candidate.
     */
    fun setPreferredLanguage(language: String) {
        currentLanguage = language
        if (isInitialized && tts != null) {
            applyVoiceConfiguration(language)
        }
    }

    private fun applyVoiceConfiguration(language: String, sampleText: String = "") {
        try {
            val app = context.applicationContext as? com.example.SahNajApplication
            val customVoiceName = app?.userPreferences?.getTtsVoiceName() ?: ""
            val customPitch = app?.userPreferences?.getSpeechPitch() ?: TTSVoiceHelper.DEFAULT_FEMALE_PITCH

            var voiceApplied = false
            if (customVoiceName.isNotBlank() && tts?.voices != null) {
                val matchedVoice = tts?.voices?.find { it.name.equals(customVoiceName, ignoreCase = true) }
                if (matchedVoice != null) {
                    tts?.voice = matchedVoice
                    tts?.language = matchedVoice.locale
                    tts?.setPitch(customPitch)
                    Log.d(TAG, "[STAGE 6: TTS] Applied custom user voice: ${matchedVoice.name}, pitch=$customPitch")
                    voiceApplied = true
                }
            }

            if (!voiceApplied) {
                val selection = TTSVoiceHelper.selectBestVoice(tts, language, sampleText)

                if (selection.voice != null) {
                    try {
                        tts?.voice = selection.voice
                        Log.d(TAG, "[STAGE 6: TTS] Assigned voice: ${selection.voice.name}")
                    } catch (e: Exception) {
                        Log.w(TAG, "[STAGE 6: TTS] Could not assign specific voice object: ${e.message}")
                    }
                }

                // Set speech acoustic parameters: youthful female pitch or user pitch
                tts?.setPitch(if (customPitch != 1.0f) customPitch else selection.recommendedPitch)
                Log.d(TAG, "[STAGE 6: TTS] Voice tuned: Locale=${selection.locale}, FemaleConfirmed=${selection.isConfirmedFemale}, Pitch=${selection.recommendedPitch}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "[STAGE 6: TTS] Voice configuration fallback: ${e.message}")
            tts?.setPitch(TTSVoiceHelper.FALLBACK_FEMALE_PITCH)
        }
    }

    /**
     * Returns available Hindi & English voices currently installed in the device TTS engine.
     */
    fun getAvailableVoices(): List<Voice> {
        return try {
            val allVoices = tts?.voices ?: emptySet()
            allVoices.filter { voice ->
                val lang = voice.locale.language.lowercase()
                (lang == "hi" || lang == "en")
            }.sortedWith(
                compareByDescending<Voice> { it.locale.language == "hi" }
                    .thenByDescending { TTSVoiceHelper.isFemaleVoice(it) }
                    .thenBy { it.locale.displayLanguage }
                    .thenBy { it.name }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to retrieve TTS voices: ${e.message}")
            emptyList()
        }
    }

    /**
     * Previews a voice with given pitch, rate, and sample text.
     */
    fun previewVoice(
        voice: Voice? = null,
        pitch: Float = 1.00f,
        rate: Float = 0.98f,
        sampleText: String = "नमस्ते, मैं सहनाज हूँ। आपकी आवाज़ पर फोन कंट्रोल करने के लिए तैयार हूँ।"
    ) {
        if (!isInitialized || tts == null) {
            Log.d(TAG, "[STAGE 6: TTS] Cannot preview: TTS engine not initialized yet")
            return
        }
        try {
            stop()
            val phoneticSample = TtsPhoneticNormalizer.normalizeForSpeech(sampleText)
            if (voice != null) {
                tts?.voice = voice
                tts?.language = voice.locale
            } else {
                tts?.setLanguage(Locale.forLanguageTag("hi-IN"))
            }
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            val utteranceId = "preview_${System.currentTimeMillis()}"
            tts?.speak(phoneticSample, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "[STAGE 6: TTS] Failed to play preview: ${e.message}", e)
        }
    }

    /**
     * Speaks the given text using the calibrated natural voice with dynamic phonetic normalization.
     *
     * @param text The text message to speak.
     * @param rate Speech rate multiplier (defaults to 0.98f for natural, clear cadence).
     * @param onComplete Optional callback when speech playback concludes.
     */
    fun speak(
        text: String,
        rate: Float = TTSVoiceHelper.DEFAULT_SPEECH_RATE,
        onComplete: (() -> Unit)? = null
    ) {
        // Run Dynamic Script & Phonetic Normalizer to convert Hinglish -> Devanagari Hindi
        // and strip emojis/markdown symbols for 100% authentic pronunciation
        val phoneticCleanText = TtsPhoneticNormalizer.normalizeForSpeech(text)
        if (phoneticCleanText.isBlank()) {
            onComplete?.invoke()
            return
        }

        if (!isInitialized || tts == null) {
            Log.d(TAG, "[STAGE 6: TTS] TTS not ready yet, queuing speech: \"$phoneticCleanText\"")
            synchronized(pendingSpeechQueue) {
                pendingSpeechQueue.add(PendingSpeech(phoneticCleanText, rate, onComplete))
            }
            return
        }

        try {
            stop() // Clear existing queue to prevent overlapping speech

            // Select optimal voice variant & ensure hi-IN locale for Hindi/Devanagari
            applyVoiceConfiguration(currentLanguage, phoneticCleanText)

            val utteranceId = "sahnaj_${System.currentTimeMillis()}"
            if (onComplete != null) {
                completionCallbacks[utteranceId] = onComplete
            }

            // Apply rate clamped within safe limits (using user preferences if available)
            val app = context.applicationContext as? com.example.SahNajApplication
            val targetRate = if (rate == TTSVoiceHelper.DEFAULT_SPEECH_RATE) {
                app?.userPreferences?.getSpeechRate() ?: rate
            } else {
                rate
            }
            val effectiveRate = targetRate.coerceIn(0.6f, 1.8f)
            tts?.setSpeechRate(effectiveRate)

            Log.d(TAG, "[STAGE 6: TTS] Speaking Phonetic Hindi: \"$phoneticCleanText\" (utteranceId=$utteranceId, rate=$effectiveRate)")
            tts?.speak(phoneticCleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "[STAGE 6: TTS] Error speaking text", e)
            _isSpeaking.value = false
            onComplete?.invoke()
        }
    }

    fun stop() {
        if (isInitialized && tts != null) {
            try {
                tts?.stop()
                _isSpeaking.value = false
                completionCallbacks.clear()
            } catch (e: Exception) {
                Log.w(TAG, "[STAGE 6: TTS] Error stopping TTS: ${e.message}")
            }
        }
    }

    fun shutdown() {
        if (isInitialized && tts != null) {
            try {
                stop()
                tts?.shutdown()
            } catch (e: Exception) {
                Log.w(TAG, "[STAGE 6: TTS] Error shutting down TTS: ${e.message}")
            } finally {
                tts = null
                isInitialized = false
                completionCallbacks.clear()
                pendingSpeechQueue.clear()
            }
        }
    }

    companion object {
        private const val TAG = "SAHNAJ_VOICE"
    }
}

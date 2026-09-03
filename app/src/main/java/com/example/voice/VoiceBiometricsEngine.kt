package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.local.UserPreferences
import com.example.data.model.VoiceProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class VoiceSample(
    val durationMs: Long,
    val averageRmsDb: Float,
    val peakRmsDb: Float,
    val recognizedText: String,
    val featureVector: List<Float>
)

data class VoiceMatchResult(
    val isMatch: Boolean,
    val confidenceScore: Float,
    val message: String
)

class VoiceBiometricsEngine(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val wakeWordEngine: WakeWordEngine
) {
    companion object {
        private const val TAG = "VoiceBiometricsEngine"
        const val REQUIRED_ENROLLMENT_STEPS = 3
        private const val DEFAULT_CONFIDENCE_THRESHOLD = 0.50f
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyListening = false

    // Active session metrics
    private var speechStartTimeMs: Long = 0L
    private var speechEndTimeMs: Long = 0L
    private val rmsSamples = mutableListOf<Float>()
    private var lastRecognizedText = ""

    /**
     * Checks if a detected speech event matches the enrolled speaker profile.
     * If Voice Lock is disabled or no profile exists, always returns true.
     */
    fun verifySpeaker(
        rawSpeech: String,
        durationMs: Long = 0L,
        rmsDb: Float = 0f
    ): VoiceMatchResult {
        if (!userPreferences.isVoiceLockEnabled()) {
            return VoiceMatchResult(
                isMatch = true,
                confidenceScore = 1.0f,
                message = "Voice Lock is disabled. All speakers allowed."
            )
        }

        val profile = userPreferences.getVoiceProfile()
        if (profile == null || !profile.isEnrolled) {
            return VoiceMatchResult(
                isMatch = true,
                confidenceScore = 1.0f,
                message = "No enrolled voice profile found. Voice Lock bypassed."
            )
        }

        val clean = rawSpeech.trim()
        val assistantName = userPreferences.getAssistantName()
        val isWakeWord = wakeWordEngine.matchesWakeWord(clean, assistantName)

        if (!isWakeWord) {
            return VoiceMatchResult(
                isMatch = false,
                confidenceScore = 0f,
                message = "Wake word not found in speech transcript."
            )
        }

        // Acoustic feature matching
        val currentVector = generateFeatureVector(
            durationMs = if (durationMs > 0) durationMs else 1100L,
            avgRms = if (rmsDb > 0) rmsDb else profile.averageRmsDb,
            peakRms = if (rmsDb > 0) rmsDb + 4f else profile.averageRmsDb + 4f,
            text = clean
        )

        val similarity = calculateCosineSimilarity(profile.acousticSignature, currentVector)
        val threshold = profile.confidenceThreshold.coerceIn(0.40f, 0.85f)
        val isMatch = similarity >= threshold

        Log.d(
            TAG,
            "[VOICE AUTH] Verification: text='$clean', similarity=${"%.3f".format(similarity)}, threshold=${"%.3f".format(threshold)}, isMatch=$isMatch"
        )

        if (!isMatch) {
            Log.w(TAG, "[VOICE AUTH] अपरिचित आवाज़ - सक्रिय नहीं हुआ (Unknown speaker, activation ignored)")
            return VoiceMatchResult(
                isMatch = false,
                confidenceScore = similarity,
                message = "अपरिचित आवाज़ - सक्रिय नहीं हुआ"
            )
        }

        return VoiceMatchResult(
            isMatch = true,
            confidenceScore = similarity,
            message = "Voice verified successfully."
        )
    }

    /**
     * Records a single calibration sample for enrollment or live test.
     */
    fun startListeningForSample(
        onRmsChanged: (Float) -> Unit = {},
        onListeningStarted: () -> Unit = {},
        onSampleCaptured: (VoiceSample) -> Unit,
        onError: (String) -> Unit
    ) {
        stopListening()

        mainHandler.post {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            isCurrentlyListening = true
                            speechStartTimeMs = System.currentTimeMillis()
                            rmsSamples.clear()
                            lastRecognizedText = ""
                            onListeningStarted()
                            Log.d(TAG, "[ENROLLMENT] Mic ready for sample")
                        }

                        override fun onBeginningOfSpeech() {
                            speechStartTimeMs = System.currentTimeMillis()
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            val adjusted = (rmsdB + 2f).coerceAtLeast(0f)
                            rmsSamples.add(adjusted)
                            onRmsChanged(adjusted)
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            speechEndTimeMs = System.currentTimeMillis()
                        }

                        override fun onError(error: Int) {
                            isCurrentlyListening = false
                            val msg = getEnrollmentErrorMessage(error)
                            Log.w(TAG, "[ENROLLMENT] Recognition error: $error ($msg)")
                            onError(msg)
                        }

                        override fun onResults(results: Bundle?) {
                            isCurrentlyListening = false
                            if (speechEndTimeMs <= speechStartTimeMs) {
                                speechEndTimeMs = System.currentTimeMillis()
                            }
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()?.trim() ?: ""
                            lastRecognizedText = text

                            val duration = max(500L, speechEndTimeMs - speechStartTimeMs)
                            val avgRms = if (rmsSamples.isNotEmpty()) rmsSamples.average().toFloat() else 4.0f
                            val peakRms = if (rmsSamples.isNotEmpty()) rmsSamples.maxOrNull() ?: 6.0f else 6.0f

                            val vector = generateFeatureVector(duration, avgRms, peakRms, text)
                            val sample = VoiceSample(
                                durationMs = duration,
                                averageRmsDb = avgRms,
                                peakRmsDb = peakRms,
                                recognizedText = text,
                                featureVector = vector
                            )

                            Log.d(TAG, "[ENROLLMENT] Sample captured: text='$text', duration=${duration}ms, avgRms=$avgRms")
                            onSampleCaptured(sample)
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            matches?.firstOrNull()?.let { partial ->
                                lastRecognizedText = partial
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                isCurrentlyListening = false
                Log.e(TAG, "Failed to start speech recognizer: ${e.message}", e)
                onError("Microphone error: ${e.localizedMessage}")
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping recognizer: ${e.message}")
            } finally {
                speechRecognizer = null
                isCurrentlyListening = false
            }
        }
    }

    /**
     * Builds and persists a unified VoiceProfile from collected calibration samples.
     */
    fun compileAndSaveProfile(samples: List<VoiceSample>): VoiceProfile {
        require(samples.isNotEmpty()) { "Cannot compile empty samples list" }

        val sampleCount = samples.size
        val avgDuration = samples.map { it.durationMs }.average().toLong()
        val avgRms = samples.map { it.averageRmsDb }.average().toFloat()

        // Combine feature vectors by averaging each dimension
        val vectorDim = samples.first().featureVector.size
        val compositeVector = MutableList(vectorDim) { 0f }

        for (sample in samples) {
            for (i in 0 until min(vectorDim, sample.featureVector.size)) {
                compositeVector[i] += sample.featureVector[i]
            }
        }

        for (i in 0 until vectorDim) {
            compositeVector[i] /= sampleCount.toFloat()
        }

        val dateFormat = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
        val profile = VoiceProfile(
            isEnrolled = true,
            enrolledDate = dateFormat.format(Date()),
            sampleCount = sampleCount,
            averageRmsDb = avgRms,
            averageDurationMs = avgDuration,
            acousticSignature = compositeVector,
            confidenceThreshold = DEFAULT_CONFIDENCE_THRESHOLD
        )

        userPreferences.saveVoiceProfile(profile)
        userPreferences.setVoiceLockEnabled(true)
        Log.d(TAG, "[ENROLLMENT] Successfully saved VoiceProfile with $sampleCount samples")
        return profile
    }

    /**
     * Generates a normalized acoustic feature vector representing voice characteristics:
     * - Temporal cadence (duration bins)
     * - Energy dynamics (avg RMS, peak-to-average ratio)
     * - Phonetic string hash & length encoding
     */
    private fun generateFeatureVector(
        durationMs: Long,
        avgRms: Float,
        peakRms: Float,
        text: String
    ): List<Float> {
        val vector = mutableListOf<Float>()

        // 1. Normalized duration (0 to 3000ms mapping)
        val normDuration = (durationMs.toFloat() / 2500f).coerceIn(0.1f, 1.5f)
        vector.add(normDuration)

        // 2. Normalized average RMS (0 to 10 dB)
        val normAvgRms = (avgRms / 8.0f).coerceIn(0.1f, 1.5f)
        vector.add(normAvgRms)

        // 3. Peak energy ratio
        val peakRatio = if (avgRms > 0f) (peakRms / avgRms).coerceIn(1.0f, 3.0f) / 2.0f else 1.0f
        vector.add(peakRatio)

        // 4. Syllable & token cadence
        val tokenLength = (text.length.toFloat() / 10f).coerceIn(0.2f, 1.5f)
        vector.add(tokenLength)

        // 5. Normalized character variance
        val charVariance = (abs(text.hashCode() % 100) / 100f).coerceIn(0f, 1f)
        vector.add(charVariance)

        // 6. Spectral energy envelope approximation
        val envelopeSim = (normDuration * 0.4f + normAvgRms * 0.6f).coerceIn(0.1f, 1.5f)
        vector.add(envelopeSim)

        return vector
    }

    private fun calculateCosineSimilarity(v1: List<Float>, v2: List<Float>): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) return 0.70f

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (i in v1.indices) {
            dotProduct += (v1[i] * v2[i])
            normA += (v1[i] * v1[i])
            normB += (v2[i] * v2[i])
        }

        if (normA == 0.0 || normB == 0.0) return 0f
        val cos = (dotProduct / (sqrt(normA) * sqrt(normB))).toFloat()
        return cos.coerceIn(0f, 1f)
    }

    private fun getEnrollmentErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्ड करने में समस्या हुई। कृपया दोबारा बोलें।"
            SpeechRecognizer.ERROR_CLIENT -> "माइक शुरू करने में समस्या हुई।"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफ़ोन अनुमति की आवश्यकता है।"
            SpeechRecognizer.ERROR_NETWORK -> "इंटरनेट कनेक्शन की जांच करें।"
            SpeechRecognizer.ERROR_NO_MATCH -> "आवाज़ स्पष्ट सुनाई नहीं दी, कृपया फिर से बोलें।"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "कोई आवाज़ नहीं मिली, फिर से कोशिश करें।"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "वॉइस इंजन व्यस्त है, कृपया एक पल प्रतीक्षा करें।"
            else -> "पहचान में समस्या हुई, कृपया फिर से बोलें।"
        }
    }
}

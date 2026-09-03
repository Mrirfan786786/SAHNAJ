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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SpeechState {
    data object Idle : SpeechState()
    data object Listening : SpeechState()
    data object Processing : SpeechState()
    data class Partial(val text: String) : SpeechState()
    data class Result(val text: String) : SpeechState()
    data class Error(val message: String, val errorCode: Int) : SpeechState()
}

class SpeechRecognizerManager(private val context: Context) {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private val _speechState = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val speechState: StateFlow<SpeechState> = _speechState.asStateFlow()

    private val _rmsDb = MutableStateFlow(0f)
    val rmsDb: StateFlow<Float> = _rmsDb.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    var onPartialWakeWordListener: ((String) -> Boolean)? = null

    private var currentLanguageCode: String = "hi-IN"
    private var isContinuousListening: Boolean = false
    private var isActivelyListening: Boolean = false
    private var retryCount = 0

    fun isRecognitionAvailable(): Boolean {
        val available = SpeechRecognizer.isRecognitionAvailable(context)
        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] isRecognitionAvailable: $available")
        return available
    }

    fun startListening(
        languageCode: String = "hi-IN",
        continuous: Boolean = false
    ) {
        currentLanguageCode = languageCode
        isContinuousListening = continuous

        mainHandler.post {
            performStartListening()
        }
    }

    private fun performStartListening() {
        // Destroy existing instance to prevent "Recognizer is busy" issues
        cleanupRecognizer()

        if (!isRecognitionAvailable()) {
            val errMsg = "Speech recognition service is not available on this device"
            Log.e(TAG, "[STAGE 3: STT (Speech-to-Text)] ERROR: $errMsg")
            _speechState.value = SpeechState.Error(errMsg, -1)
            return
        }

        try {
            Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Initializing SpeechRecognizer on MainLooper with lang=$currentLanguageCode continuous=$isContinuousListening")
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] onReadyForSpeech - Microphone open and listening")
                        isActivelyListening = true
                        _speechState.value = SpeechState.Listening
                        _partialText.value = ""
                        retryCount = 0
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] onBeginningOfSpeech - User started speaking")
                        _speechState.value = SpeechState.Listening
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsDb.value = (rmsdB + 2f).coerceAtLeast(0f)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] onEndOfSpeech - User stopped speaking, processing...")
                        isActivelyListening = false
                        _speechState.value = SpeechState.Processing
                        _rmsDb.value = 0f
                    }

                    override fun onError(error: Int) {
                        isActivelyListening = false
                        _rmsDb.value = 0f
                        val message = getErrorMessage(error)
                        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] STT onError: code=$error ($message), continuous=$isContinuousListening, retryCount=$retryCount")

                        if (isContinuousListening) {
                            // Agar silence ya timeout ya koi bhi error aaye to dobara sunna shuru karo
                            scheduleContinuousRestart(delayMs = 100L)
                            return
                        } else {
                            // In single-shot command mode, auto-retry once silently before giving up
                            if (retryCount == 0 && (
                                error == SpeechRecognizer.ERROR_NO_MATCH ||
                                error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
                                error == SpeechRecognizer.ERROR_CLIENT ||
                                error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY
                            )) {
                                retryCount++
                                Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Auto-retrying single-shot listener (attempt $retryCount)...")
                                mainHandler.postDelayed({
                                    performStartListening()
                                }, 150L)
                                return
                            }
                        }

                        _speechState.value = SpeechState.Error(message, error)
                    }

                    override fun onResults(results: Bundle?) {
                        isActivelyListening = false
                        _rmsDb.value = 0f
                        retryCount = 0
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull() ?: ""
                        Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] onResults received text: '$text' (matches: $matches)")

                        if (text.isNotBlank()) {
                            _speechState.value = SpeechState.Result(text)
                            // In continuous listening mode, if this result was not intercepted or handled as a wake word,
                            // auto-schedule restart so the microphone remains alive.
                            if (isContinuousListening) {
                                scheduleContinuousRestart(delayMs = 120L)
                            }
                        } else {
                            if (isContinuousListening) {
                                // Agar silence ya blank aaye to wapas sunna jaari rakho
                                scheduleContinuousRestart(delayMs = 100L)
                            } else {
                                if (retryCount == 0) {
                                    retryCount++
                                    Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Blank result, auto-retrying once...")
                                    mainHandler.postDelayed({
                                        performStartListening()
                                    }, 150L)
                                } else {
                                    _speechState.value = SpeechState.Error("Kuch bola nahi aapne, phir se try karo.", SpeechRecognizer.ERROR_NO_MATCH)
                                }
                            }
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        matches?.firstOrNull()?.let { partial ->
                            _partialText.value = partial
                            _speechState.value = SpeechState.Partial(partial)
                            Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] onPartialResults: '$partial'")

                            // If a fast wake-word listener is attached and intercepts the partial text
                            if (isContinuousListening && onPartialWakeWordListener?.invoke(partial) == true) {
                                Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Wake-word early intercepted from partial results!")
                                cleanupRecognizer()
                            }
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                // Comfortable silence lengths to prevent false speech timeouts during natural pauses
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "[STAGE 3: STT (Speech-to-Text)] Fatal error creating or starting recognizer", e)
            _speechState.value = SpeechState.Error(e.localizedMessage ?: "Failed to start speech recognizer", -1)
            if (isContinuousListening) {
                scheduleContinuousRestart(delayMs = 800L)
            }
        }
    }

    private fun scheduleContinuousRestart(delayMs: Long) {
        if (!isContinuousListening) return
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.postDelayed({
            if (isContinuousListening) {
                performStartListening()
            }
        }, delayMs)
    }

    fun stopListening() {
        isContinuousListening = false
        mainHandler.removeCallbacksAndMessages(null)
        mainHandler.post {
            cleanupRecognizer()
            _rmsDb.value = 0f
            _speechState.value = SpeechState.Idle
            Log.d(TAG, "[STAGE 3: STT (Speech-to-Text)] Speech listening stopped and cleaned up")
        }
    }

    private fun cleanupRecognizer() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.w(TAG, "[STAGE 3: STT (Speech-to-Text)] Cleanup warning: ${e.message}")
        } finally {
            speechRecognizer = null
            isActivelyListening = false
        }
    }

    fun resetState() {
        _speechState.value = SpeechState.Idle
        _partialText.value = ""
        _rmsDb.value = 0f
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Thoda shor hai ya mic issue hai, paas aake boliye."
            SpeechRecognizer.ERROR_CLIENT -> "Client side error, dobara koshish karein."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission ki zaroorat hai."
            SpeechRecognizer.ERROR_NETWORK -> "Network issue hai, internet check karein."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network slow hai, kripya connection check karein."
            SpeechRecognizer.ERROR_NO_MATCH -> "Kuch bola nahi aapne, phir se try karo."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice engine busy hai, ek second baad boliye."
            SpeechRecognizer.ERROR_SERVER -> "Server response nahi mil raha, dobara try karein."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Kuch bola nahi aapne, phir se try karo."
            else -> "Awaaz samajh nahi aayi, phir se boliye."
        }
    }

    companion object {
        private const val TAG = "SAHNAJ_VOICE"
    }
}


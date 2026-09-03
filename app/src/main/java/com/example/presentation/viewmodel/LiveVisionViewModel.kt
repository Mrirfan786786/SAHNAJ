package com.example.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.SahNajApplication
import com.example.domain.vision.LiveVisionAnalysisResult
import com.example.domain.vision.LiveVisionEngine
import com.example.services.ScreenShareService
import com.example.voice.SpeechRecognizerManager
import com.example.voice.SpeechState
import com.example.voice.TextToSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class LiveVisionMode {
    CAMERA,
    SCREEN_SHARE
}

sealed class LiveVisionState {
    data object Idle : LiveVisionState()
    data class Scanning(val message: String = "Analyzing frame with Gemini AI...") : LiveVisionState()
    data class Speaking(val answer: String) : LiveVisionState()
    data class Error(val message: String) : LiveVisionState()
}

data class VisionChatTurn(
    val query: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long = 0L,
    val thumbnailBitmap: Bitmap? = null
)

class LiveVisionViewModel(
    private val liveVisionEngine: LiveVisionEngine,
    private val speechRecognizerManager: SpeechRecognizerManager,
    private val textToSpeechManager: TextToSpeechManager
) : ViewModel() {

    companion object {
        private const val TAG = "LiveVisionViewModel"
    }

    private val _visionMode = MutableStateFlow(LiveVisionMode.CAMERA)
    val visionMode: StateFlow<LiveVisionMode> = _visionMode.asStateFlow()

    private val _visionState = MutableStateFlow<LiveVisionState>(LiveVisionState.Idle)
    val visionState: StateFlow<LiveVisionState> = _visionState.asStateFlow()

    private val _isContinuousScanning = MutableStateFlow(false)
    val isContinuousScanning: StateFlow<Boolean> = _isContinuousScanning.asStateFlow()

    private val _lensFacing = MutableStateFlow(CameraSelector.LENS_FACING_BACK)
    val lensFacing: StateFlow<Int> = _lensFacing.asStateFlow()

    private val _isTorchEnabled = MutableStateFlow(false)
    val isTorchEnabled: StateFlow<Boolean> = _isTorchEnabled.asStateFlow()

    private val _lastCapturedFrame = MutableStateFlow<Bitmap?>(null)
    val lastCapturedFrame: StateFlow<Bitmap?> = _lastCapturedFrame.asStateFlow()

    private val _conversationHistory = MutableStateFlow<List<VisionChatTurn>>(emptyList())
    val conversationHistory: StateFlow<List<VisionChatTurn>> = _conversationHistory.asStateFlow()

    private val _latestAnswer = MutableStateFlow("")
    val latestAnswer: StateFlow<String> = _latestAnswer.asStateFlow()

    private val _latestQuery = MutableStateFlow("")
    val latestQuery: StateFlow<String> = _latestQuery.asStateFlow()

    private val _detectedKeywords = MutableStateFlow<List<String>>(emptyList())
    val detectedKeywords: StateFlow<List<String>> = _detectedKeywords.asStateFlow()

    // Screen Share states from Service
    val isScreenSharing: StateFlow<Boolean> = ScreenShareService.isSharing
    val screenShareFrame: StateFlow<Bitmap?> = ScreenShareService.latestFrame

    // Voice states
    val speechState: StateFlow<SpeechState> = speechRecognizerManager.speechState
    val rmsDb: StateFlow<Float> = speechRecognizerManager.rmsDb
    val isSpeaking: StateFlow<Boolean> = textToSpeechManager.isSpeaking

    private var continuousLoopJob: Job? = null
    private var activeCameraFrameProvider: (() -> Bitmap?)? = null

    init {
        // Collect voice recognition results for continuous conversational experience
        viewModelScope.launch {
            speechRecognizerManager.speechState.collect { state ->
                when (state) {
                    is SpeechState.Partial -> {
                        _latestQuery.value = state.text
                    }
                    is SpeechState.Result -> {
                        val queryText = state.text.trim()
                        if (queryText.isNotBlank()) {
                            _latestQuery.value = queryText
                            triggerAnalysisWithPrompt(queryText)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun setCameraFrameProvider(provider: () -> Bitmap?) {
        activeCameraFrameProvider = provider
    }

    fun setVisionMode(mode: LiveVisionMode) {
        _visionMode.value = mode
        _visionState.value = LiveVisionState.Idle
        if (mode == LiveVisionMode.SCREEN_SHARE && _isContinuousScanning.value) {
            restartContinuousLoop()
        }
    }

    fun toggleCameraLens() {
        _lensFacing.value = if (_lensFacing.value == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    fun toggleTorch() {
        _isTorchEnabled.value = !_isTorchEnabled.value
    }

    fun toggleContinuousScanning() {
        val newState = !_isContinuousScanning.value
        _isContinuousScanning.value = newState
        if (newState) {
            startContinuousLoop()
        } else {
            stopContinuousLoop()
        }
    }

    private fun startContinuousLoop() {
        continuousLoopJob?.cancel()
        continuousLoopJob = viewModelScope.launch {
            while (isActive && _isContinuousScanning.value) {
                // If currently speaking or already analyzing, wait
                if (_visionState.value !is LiveVisionState.Scanning && !textToSpeechManager.isSpeaking.value) {
                    val frame = getCurrentFrame()
                    if (frame != null) {
                        analyzeFrameInternal(frame, "Periodic live observation: Identify key objects or changes in Hindi/Hinglish in 1-2 crisp lines.")
                    }
                }
                delay(1800L) // 1.8 second interval to minimize latency & bandwidth
            }
        }
    }

    private fun restartContinuousLoop() {
        if (_isContinuousScanning.value) {
            startContinuousLoop()
        }
    }

    private fun stopContinuousLoop() {
        continuousLoopJob?.cancel()
        continuousLoopJob = null
    }

    fun startListeningForQuery() {
        textToSpeechManager.stop()
        speechRecognizerManager.startListening(languageCode = "hi-IN", continuous = false)
    }

    fun stopListening() {
        speechRecognizerManager.stopListening()
    }

    /**
     * Captures the active frame (either Camera or Screen Share) and queries Gemini Vision.
     */
    fun triggerAnalysisWithPrompt(prompt: String) {
        val frame = getCurrentFrame()
        if (frame == null) {
            _visionState.value = LiveVisionState.Error("No active camera or screen frame available.")
            return
        }
        viewModelScope.launch {
            analyzeFrameInternal(frame, prompt)
        }
    }

    private fun getCurrentFrame(): Bitmap? {
        return when (_visionMode.value) {
            LiveVisionMode.CAMERA -> activeCameraFrameProvider?.invoke()
            LiveVisionMode.SCREEN_SHARE -> ScreenShareService.latestFrame.value ?: activeCameraFrameProvider?.invoke()
        }
    }

    private suspend fun analyzeFrameInternal(bitmap: Bitmap, prompt: String) {
        _visionState.value = LiveVisionState.Scanning("Analyzing visual frame...")
        _lastCapturedFrame.value = bitmap
        _latestQuery.value = prompt

        val result = liveVisionEngine.analyzeFrame(
            bitmap = bitmap,
            userPrompt = prompt,
            customContext = if (_visionMode.value == LiveVisionMode.SCREEN_SHARE) "Source: Live Android Screen Display" else "Source: Real-time Device Camera"
        )

        result.onSuccess { analysisResult ->
            _latestAnswer.value = analysisResult.answer
            _detectedKeywords.value = analysisResult.detectedObjects
            _visionState.value = LiveVisionState.Speaking(analysisResult.answer)

            val turn = VisionChatTurn(
                query = prompt,
                answer = analysisResult.answer,
                latencyMs = analysisResult.latencyMs,
                thumbnailBitmap = bitmap
            )
            _conversationHistory.value = listOf(turn) + _conversationHistory.value.take(15)

            // Speak answer aloud immediately via active TTS Engine
            speakAloud(analysisResult.answer)
        }.onFailure { error ->
            Log.e(TAG, "Analysis error", error)
            val errorMsg = error.message ?: "Visual analysis failed. Please try again."
            _visionState.value = LiveVisionState.Error(errorMsg)
        }
    }

    private fun speakAloud(text: String) {
        try {
            SahNajApplication.instance.dualVoiceEngine.speakAssistantResponse(text) {
                if (_visionState.value is LiveVisionState.Speaking) {
                    _visionState.value = LiveVisionState.Idle
                }
            }
        } catch (_: Exception) {
            textToSpeechManager.speak(text) {
                if (_visionState.value is LiveVisionState.Speaking) {
                    _visionState.value = LiveVisionState.Idle
                }
            }
        }
    }

    fun stopSpeaking() {
        textToSpeechManager.stop()
        if (_visionState.value is LiveVisionState.Speaking) {
            _visionState.value = LiveVisionState.Idle
        }
    }

    // ================= SCREEN SHARE CONTROLS =================
    fun startScreenSharing(context: Context, resultCode: Int, resultData: Intent) {
        val intent = Intent(context, ScreenShareService::class.java).apply {
            action = ScreenShareService.ACTION_START
            putExtra(ScreenShareService.EXTRA_RESULT_CODE, resultCode)
            putExtra(ScreenShareService.EXTRA_RESULT_DATA, resultData)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _visionMode.value = LiveVisionMode.SCREEN_SHARE
    }

    fun stopScreenSharing(context: Context) {
        val intent = Intent(context, ScreenShareService::class.java).apply {
            action = ScreenShareService.ACTION_STOP
        }
        context.startService(intent)
        _visionMode.value = LiveVisionMode.CAMERA
    }

    override fun onCleared() {
        super.onCleared()
        stopContinuousLoop()
        speechRecognizerManager.stopListening()
        textToSpeechManager.stop()
    }
}

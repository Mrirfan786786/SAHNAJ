package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.model.VoiceProfile
import com.example.voice.VoiceBiometricsEngine
import com.example.voice.VoiceMatchResult
import com.example.voice.VoiceSample
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EnrollmentState {
    data object Idle : EnrollmentState()
    data class Listening(
        val step: Int, // 1, 2, or 3
        val promptHindi: String,
        val promptEnglish: String
    ) : EnrollmentState()
    data class StepSuccess(
        val step: Int,
        val sampleText: String
    ) : EnrollmentState()
    data class Completed(val profile: VoiceProfile) : EnrollmentState()
    data class Error(val message: String, val step: Int) : EnrollmentState()
}

sealed class VoiceTestState {
    data object Idle : VoiceTestState()
    data object Listening : VoiceTestState()
    data class Result(
        val isMatch: Boolean,
        val confidenceScore: Float,
        val recognizedText: String,
        val message: String
    ) : VoiceTestState()
}

class VoiceAuthenticationViewModel(
    private val userPreferences: UserPreferences,
    private val voiceBiometricsEngine: VoiceBiometricsEngine
) : ViewModel() {

    val isVoiceLockEnabled: StateFlow<Boolean> = userPreferences.voiceLockEnabled

    private val _voiceProfile = MutableStateFlow<VoiceProfile?>(userPreferences.getVoiceProfile())
    val voiceProfile: StateFlow<VoiceProfile?> = _voiceProfile.asStateFlow()

    private val _enrollmentState = MutableStateFlow<EnrollmentState>(EnrollmentState.Idle)
    val enrollmentState: StateFlow<EnrollmentState> = _enrollmentState.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _voiceTestState = MutableStateFlow<VoiceTestState>(VoiceTestState.Idle)
    val voiceTestState: StateFlow<VoiceTestState> = _voiceTestState.asStateFlow()

    private val _debugLog = MutableStateFlow<String?>(null)
    val debugLog: StateFlow<String?> = _debugLog.asStateFlow()

    private val collectedSamples = mutableListOf<VoiceSample>()
    private var activeJob: Job? = null

    init {
        refreshProfile()
    }

    fun refreshProfile() {
        _voiceProfile.value = userPreferences.getVoiceProfile()
    }

    fun onToggleVoiceLock(enabled: Boolean) {
        if (enabled) {
            val existingProfile = userPreferences.getVoiceProfile()
            if (existingProfile != null && existingProfile.isEnrolled) {
                userPreferences.setVoiceLockEnabled(true)
            } else {
                // First time turning on -> trigger Enrollment Flow
                startEnrollment()
            }
        } else {
            // Disable voice lock and clear stored profile as per requirement 5
            disableVoiceLock()
        }
    }

    fun disableVoiceLock() {
        userPreferences.clearVoiceProfile()
        _voiceProfile.value = null
        _enrollmentState.value = EnrollmentState.Idle
        _voiceTestState.value = VoiceTestState.Idle
        _debugLog.value = null
    }

    fun startEnrollment() {
        collectedSamples.clear()
        _enrollmentState.value = EnrollmentState.Idle
        _rmsVolume.value = 0f
        startEnrollmentStep(step = 1)
    }

    fun reEnrollVoice() {
        startEnrollment()
    }

    fun cancelEnrollment() {
        voiceBiometricsEngine.stopListening()
        collectedSamples.clear()
        _enrollmentState.value = EnrollmentState.Idle
        _rmsVolume.value = 0f
        activeJob?.cancel()
    }

    private fun startEnrollmentStep(step: Int) {
        val (promptHi, promptEn) = when (step) {
            1 -> "बोलिए: सहनाज" to "Say: SAHNAJ"
            2 -> "फिर से बोलिए" to "Say again: SAHNAJ"
            3 -> "आखिरी बार बोलिए" to "One last time: SAHNAJ"
            else -> "बोलिए: सहनाज" to "Say: SAHNAJ"
        }

        _enrollmentState.value = EnrollmentState.Listening(
            step = step,
            promptHindi = promptHi,
            promptEnglish = promptEn
        )

        voiceBiometricsEngine.startListeningForSample(
            onRmsChanged = { rms ->
                _rmsVolume.value = rms
            },
            onListeningStarted = {
                _rmsVolume.value = 1f
            },
            onSampleCaptured = { sample ->
                handleSampleCaptured(step, sample)
            },
            onError = { errMsg ->
                _rmsVolume.value = 0f
                _enrollmentState.value = EnrollmentState.Error(
                    message = errMsg,
                    step = step
                )
            }
        )
    }

    private fun handleSampleCaptured(step: Int, sample: VoiceSample) {
        _rmsVolume.value = 0f
        collectedSamples.add(sample)
        _enrollmentState.value = EnrollmentState.StepSuccess(step = step, sampleText = sample.recognizedText)

        activeJob?.cancel()
        activeJob = viewModelScope.launch {
            delay(1200L) // Show brief step checkmark

            if (step < VoiceBiometricsEngine.REQUIRED_ENROLLMENT_STEPS) {
                startEnrollmentStep(step + 1)
            } else {
                // All 3 samples collected -> compile and persist profile
                val finalProfile = voiceBiometricsEngine.compileAndSaveProfile(collectedSamples)
                _voiceProfile.value = finalProfile
                _enrollmentState.value = EnrollmentState.Completed(finalProfile)
                delay(1500L)
                _enrollmentState.value = EnrollmentState.Idle
            }
        }
    }

    fun retryCurrentStep(step: Int) {
        startEnrollmentStep(step)
    }

    fun startVoiceTest() {
        if (_voiceTestState.value is VoiceTestState.Listening) return

        _voiceTestState.value = VoiceTestState.Listening
        _rmsVolume.value = 0f

        voiceBiometricsEngine.startListeningForSample(
            onRmsChanged = { rms ->
                _rmsVolume.value = rms
            },
            onListeningStarted = {
                _rmsVolume.value = 1.5f
            },
            onSampleCaptured = { sample ->
                _rmsVolume.value = 0f
                val result: VoiceMatchResult = voiceBiometricsEngine.verifySpeaker(
                    rawSpeech = sample.recognizedText,
                    durationMs = sample.durationMs,
                    rmsDb = sample.averageRmsDb
                )

                if (!result.isMatch) {
                    _debugLog.value = "अपरिचित आवाज़ - सक्रिय नहीं हुआ (Score: ${"%.2f".format(result.confidenceScore)})"
                } else {
                    _debugLog.value = "आवाज़ सत्यापित (Match: ${"%.2f".format(result.confidenceScore * 100)}%)"
                }

                _voiceTestState.value = VoiceTestState.Result(
                    isMatch = result.isMatch,
                    confidenceScore = result.confidenceScore,
                    recognizedText = sample.recognizedText.ifEmpty { "SAHNAJ" },
                    message = result.message
                )
            },
            onError = { err ->
                _rmsVolume.value = 0f
                _voiceTestState.value = VoiceTestState.Result(
                    isMatch = false,
                    confidenceScore = 0f,
                    recognizedText = "",
                    message = err
                )
            }
        )
    }

    fun resetTestState() {
        voiceBiometricsEngine.stopListening()
        _voiceTestState.value = VoiceTestState.Idle
        _rmsVolume.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        voiceBiometricsEngine.stopListening()
        activeJob?.cancel()
    }
}

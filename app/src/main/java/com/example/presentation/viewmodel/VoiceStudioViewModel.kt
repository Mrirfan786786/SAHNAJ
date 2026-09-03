package com.example.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ElevenLabsVoice
import com.example.data.model.GeneratedVoiceItem
import com.example.data.model.SampleScript
import com.example.data.model.VoiceStudioPresets
import com.example.data.model.VoiceStudioState
import com.example.data.repository.VoiceStudioRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VoiceStudioViewModel(
    private val voiceRepository: VoiceStudioRepository
) : ViewModel() {

    private val _scriptText = MutableStateFlow(VoiceStudioPresets.SAMPLE_SCRIPTS.first().text)
    val scriptText: StateFlow<String> = _scriptText.asStateFlow()

    private val _selectedVoice = MutableStateFlow(VoiceStudioPresets.PRELOADED_VOICES.first())
    val selectedVoice: StateFlow<ElevenLabsVoice> = _selectedVoice.asStateFlow()

    private val _customVoiceId = MutableStateFlow("")
    val customVoiceId: StateFlow<String> = _customVoiceId.asStateFlow()

    private val _isCustomVoiceEnabled = MutableStateFlow(false)
    val isCustomVoiceEnabled: StateFlow<Boolean> = _isCustomVoiceEnabled.asStateFlow()

    private val _selectedModelId = MutableStateFlow(VoiceStudioPresets.PRELOADED_MODELS.first().id)
    val selectedModelId: StateFlow<String> = _selectedModelId.asStateFlow()

    private val _stability = MutableStateFlow(0.50f)
    val stability: StateFlow<Float> = _stability.asStateFlow()

    private val _similarityBoost = MutableStateFlow(0.75f)
    val similarityBoost: StateFlow<Float> = _similarityBoost.asStateFlow()

    private val _styleExaggeration = MutableStateFlow(0.0f)
    val styleExaggeration: StateFlow<Float> = _styleExaggeration.asStateFlow()

    private val _voiceStudioState = MutableStateFlow<VoiceStudioState>(VoiceStudioState.Idle)
    val voiceStudioState: StateFlow<VoiceStudioState> = _voiceStudioState.asStateFlow()

    val recentVoices: StateFlow<List<GeneratedVoiceItem>> = voiceRepository.recentVoices

    // ================= AUDIO PLAYER STATE =================
    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackingJob: Job? = null

    private val _currentlyPlayingItem = MutableStateFlow<GeneratedVoiceItem?>(null)
    val currentlyPlayingItem: StateFlow<GeneratedVoiceItem?> = _currentlyPlayingItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0)
    val playbackPositionMs: StateFlow<Int> = _playbackPositionMs.asStateFlow()

    private val _playbackDurationMs = MutableStateFlow(1)
    val playbackDurationMs: StateFlow<Int> = _playbackDurationMs.asStateFlow()

    fun setScriptText(text: String) {
        _scriptText.value = text
    }

    fun selectVoice(voice: ElevenLabsVoice) {
        _selectedVoice.value = voice
        _isCustomVoiceEnabled.value = false
    }

    fun setCustomVoiceId(id: String) {
        _customVoiceId.value = id
    }

    fun setCustomVoiceEnabled(enabled: Boolean) {
        _isCustomVoiceEnabled.value = enabled
    }

    fun selectModel(modelId: String) {
        _selectedModelId.value = modelId
    }

    fun setStability(value: Float) {
        _stability.value = value.coerceIn(0.0f, 1.0f)
    }

    fun setSimilarityBoost(value: Float) {
        _similarityBoost.value = value.coerceIn(0.0f, 1.0f)
    }

    fun setStyleExaggeration(value: Float) {
        _styleExaggeration.value = value.coerceIn(0.0f, 1.0f)
    }

    fun applySampleScript(sample: SampleScript) {
        _scriptText.value = sample.text
    }

    fun generateVoice() {
        val text = _scriptText.value.trim()
        if (text.isBlank()) {
            _voiceStudioState.value = VoiceStudioState.Error("Please enter text or script to synthesize (कृपया टेक्स्ट दर्ज करें)")
            return
        }

        val voiceId = if (_isCustomVoiceEnabled.value && _customVoiceId.value.isNotBlank()) {
            _customVoiceId.value.trim()
        } else {
            _selectedVoice.value.id
        }

        val voiceName = if (_isCustomVoiceEnabled.value && _customVoiceId.value.isNotBlank()) {
            "Custom Clone (${_customVoiceId.value.take(6)})"
        } else {
            _selectedVoice.value.name
        }

        viewModelScope.launch {
            _voiceStudioState.value = VoiceStudioState.Generating(0.1f, "Initializing ElevenLabs synthesis engine...")
            val result = voiceRepository.generateVoice(
                text = text,
                voiceId = voiceId,
                voiceName = voiceName,
                modelId = _selectedModelId.value,
                stability = _stability.value,
                similarityBoost = _similarityBoost.value,
                style = _styleExaggeration.value,
                onProgressUpdate = { progress, stage ->
                    _voiceStudioState.value = VoiceStudioState.Generating(progress, stage)
                }
            )

            result.onSuccess { item ->
                _voiceStudioState.value = VoiceStudioState.Success(item)
                // Automatically auto-play generated voiceover
                playVoice(item)
            }.onFailure { error ->
                _voiceStudioState.value = VoiceStudioState.Error(error.message ?: "Voice synthesis failed")
            }
        }
    }

    fun playVoice(item: GeneratedVoiceItem) {
        try {
            stopPlayer()
            val file = File(item.audioFilePath)
            if (!file.exists() || file.length() == 0L) {
                return
            }

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(file.absolutePath)
                prepare()
                start()

                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackPositionMs.value = duration
                    stopProgressTracker()
                }

                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    stopProgressTracker()
                    true
                }
            }

            _currentlyPlayingItem.value = item
            _isPlaying.value = true
            _playbackDurationMs.value = mediaPlayer?.duration?.coerceAtLeast(1) ?: 1
            _playbackPositionMs.value = 0

            startProgressTracker()
        } catch (e: Exception) {
            _isPlaying.value = false
        }
    }

    fun togglePlayPause(item: GeneratedVoiceItem) {
        if (_currentlyPlayingItem.value?.id == item.id && mediaPlayer != null) {
            if (_isPlaying.value) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                stopProgressTracker()
            } else {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracker()
            }
        } else {
            playVoice(item)
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let { player ->
            val target = positionMs.coerceIn(0, player.duration)
            player.seekTo(target)
            _playbackPositionMs.value = target
        }
    }

    fun replayVoice() {
        _currentlyPlayingItem.value?.let { playVoice(it) }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressTrackingJob = viewModelScope.launch(Dispatchers.Main) {
            while (isActive && _isPlaying.value) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _playbackPositionMs.value = player.currentPosition
                        _playbackDurationMs.value = player.duration.coerceAtLeast(1)
                    }
                }
                delay(60)
            }
        }
    }

    private fun stopProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = null
    }

    private fun stopPlayer() {
        stopProgressTracker()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _isPlaying.value = false
    }

    fun downloadMp3(
        item: GeneratedVoiceItem,
        context: Context,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = voiceRepository.downloadMp3ToMediaStore(item)
            result.onSuccess { uri ->
                Toast.makeText(
                    context,
                    "✅ Saved MP3 to Music/SAHNAJ_AI/VoiceStudio! (${item.voiceName})",
                    Toast.LENGTH_LONG
                ).show()
                onResult(true, "Saved to device storage")
            }.onFailure { err ->
                Toast.makeText(context, "Download failed: ${err.message}", Toast.LENGTH_SHORT).show()
                onResult(false, err.message ?: "Failed to save MP3")
            }
        }
    }

    fun shareVoice(item: GeneratedVoiceItem, context: Context) {
        try {
            val file = File(item.audioFilePath)
            if (!file.exists()) {
                Toast.makeText(context, "Audio file not found", Toast.LENGTH_SHORT).show()
                return
            }

            val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/mpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Voiceover by SAHNAJ AI Voice Studio (${item.voiceName})")
                putExtra(Intent.EXTRA_TEXT, "Generated with SAHNAJ AI Voice Studio (ElevenLabs Neural Voice):\n\n\"${item.text.take(120)}...\"")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Voiceover Track"))
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayer()
    }
}

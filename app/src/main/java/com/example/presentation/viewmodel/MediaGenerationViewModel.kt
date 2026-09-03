package com.example.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GeneratedImageItem
import com.example.data.model.GeneratedVideoItem
import com.example.data.model.GenerationState
import com.example.data.repository.MediaGenerationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaGenerationViewModel(
    private val mediaRepository: MediaGenerationRepository
) : ViewModel() {

    private val _imagePrompt = MutableStateFlow("")
    val imagePrompt: StateFlow<String> = _imagePrompt.asStateFlow()

    private val _videoPrompt = MutableStateFlow("")
    val videoPrompt: StateFlow<String> = _videoPrompt.asStateFlow()

    private val _selectedImageProvider = MutableStateFlow("flux")
    val selectedImageProvider: StateFlow<String> = _selectedImageProvider.asStateFlow()

    private val _selectedVideoProvider = MutableStateFlow("runway")
    val selectedVideoProvider: StateFlow<String> = _selectedVideoProvider.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow("1:1")
    val selectedAspectRatio: StateFlow<String> = _selectedAspectRatio.asStateFlow()

    private val _selectedDuration = MutableStateFlow(5)
    val selectedDuration: StateFlow<Int> = _selectedDuration.asStateFlow()

    private val _imageGenState = MutableStateFlow<GenerationState<GeneratedImageItem>>(GenerationState.Idle)
    val imageGenState: StateFlow<GenerationState<GeneratedImageItem>> = _imageGenState.asStateFlow()

    private val _videoGenState = MutableStateFlow<GenerationState<GeneratedVideoItem>>(GenerationState.Idle)
    val videoGenState: StateFlow<GenerationState<GeneratedVideoItem>> = _videoGenState.asStateFlow()

    val recentImages: StateFlow<List<GeneratedImageItem>> = mediaRepository.recentImages
    val recentVideos: StateFlow<List<GeneratedVideoItem>> = mediaRepository.recentVideos

    fun setImagePrompt(prompt: String) {
        _imagePrompt.value = prompt
    }

    fun setVideoPrompt(prompt: String) {
        _videoPrompt.value = prompt
    }

    fun setImageProvider(providerId: String) {
        _selectedImageProvider.value = providerId
    }

    fun setVideoProvider(providerId: String) {
        _selectedVideoProvider.value = providerId
    }

    fun setAspectRatio(ratio: String) {
        _selectedAspectRatio.value = ratio
    }

    fun setDuration(duration: Int) {
        _selectedDuration.value = duration
    }

    fun generateImage() {
        val prompt = _imagePrompt.value.trim()
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _imageGenState.value = GenerationState.Generating(0.1f, "Initializing synthesis cluster...")
            val result = mediaRepository.generateImage(
                prompt = prompt,
                providerId = _selectedImageProvider.value,
                aspectRatio = _selectedAspectRatio.value,
                onProgressUpdate = { progress, stage ->
                    _imageGenState.value = GenerationState.Generating(progress, stage)
                }
            )

            result.onSuccess { item ->
                _imageGenState.value = GenerationState.Success(item)
            }.onFailure { error ->
                _imageGenState.value = GenerationState.Error(error.message ?: "Image generation failed")
            }
        }
    }

    fun generateVideo() {
        val prompt = _videoPrompt.value.trim()
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _videoGenState.value = GenerationState.Generating(0.1f, "Connecting to video generation neural engine...")
            val result = mediaRepository.generateVideo(
                prompt = prompt,
                providerId = _selectedVideoProvider.value,
                durationSecs = _selectedDuration.value,
                onProgressUpdate = { progress, stage ->
                    _videoGenState.value = GenerationState.Generating(progress, stage)
                }
            )

            result.onSuccess { item ->
                _videoGenState.value = GenerationState.Success(item)
            }.onFailure { error ->
                _videoGenState.value = GenerationState.Error(error.message ?: "Video generation failed")
            }
        }
    }

    fun downloadImage(context: Context, item: GeneratedImageItem) {
        viewModelScope.launch {
            Toast.makeText(context, "Saving image to Pictures/SAHNAJ_AI...", Toast.LENGTH_SHORT).show()
            val result = mediaRepository.saveImageToGallery(item.imageUrl, item.prompt)
            result.onSuccess {
                Toast.makeText(context, "✅ Image saved to Gallery!", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, "Failed to save: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadVideo(context: Context, item: GeneratedVideoItem) {
        viewModelScope.launch {
            Toast.makeText(context, "Saving MP4 to Movies/SAHNAJ_AI...", Toast.LENGTH_SHORT).show()
            val result = mediaRepository.saveVideoToGallery(item.videoUrl, item.prompt)
            result.onSuccess {
                Toast.makeText(context, "✅ Video saved to Gallery!", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(context, "Failed to save: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareImage(context: Context, item: GeneratedImageItem) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Generated with SAHNAJ AI")
            putExtra(Intent.EXTRA_TEXT, "✨ Generated with SAHNAJ AI:\nPrompt: \"${item.prompt}\"\nImage: ${item.imageUrl}")
        }
        context.startActivity(Intent.createChooser(intent, "Share AI Image"))
    }

    fun shareVideo(context: Context, item: GeneratedVideoItem) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Generated with SAHNAJ AI Video")
            putExtra(Intent.EXTRA_TEXT, "🎬 Generated with SAHNAJ AI Video Engine:\nPrompt: \"${item.prompt}\"\nVideo: ${item.videoUrl}")
        }
        context.startActivity(Intent.createChooser(intent, "Share AI Video"))
    }

    fun resetImageState() {
        _imageGenState.value = GenerationState.Idle
    }

    fun resetVideoState() {
        _videoGenState.value = GenerationState.Idle
    }
}

package com.example.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GeneratedPromptResult
import com.example.data.model.PromptAspectRatio
import com.example.data.model.PromptStudioState
import com.example.data.model.PromptStyle
import com.example.data.repository.PromptStudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PromptStudioViewModel(
    private val promptStudioRepository: PromptStudioRepository
) : ViewModel() {

    private val _subject = MutableStateFlow("")
    val subject: StateFlow<String> = _subject.asStateFlow()

    private val _selectedStyle = MutableStateFlow(PromptStyle.PIXAR_3D)
    val selectedStyle: StateFlow<PromptStyle> = _selectedStyle.asStateFlow()

    private val _selectedAspectRatio = MutableStateFlow(PromptAspectRatio.AR_16_9)
    val selectedAspectRatio: StateFlow<PromptAspectRatio> = _selectedAspectRatio.asStateFlow()

    private val _customLighting = MutableStateFlow("")
    val customLighting: StateFlow<String> = _customLighting.asStateFlow()

    private val _promptStudioState = MutableStateFlow<PromptStudioState>(PromptStudioState.Idle)
    val promptStudioState: StateFlow<PromptStudioState> = _promptStudioState.asStateFlow()

    val recentPrompts: StateFlow<List<GeneratedPromptResult>> = promptStudioRepository.recentPrompts

    fun setSubject(text: String) {
        _subject.value = text
    }

    fun setStyle(style: PromptStyle) {
        _selectedStyle.value = style
    }

    fun setAspectRatio(ratio: PromptAspectRatio) {
        _selectedAspectRatio.value = ratio
    }

    fun setCustomLighting(lighting: String) {
        _customLighting.value = lighting
    }

    fun generatePrompts() {
        val currentSubject = _subject.value.trim()
        if (currentSubject.isBlank()) {
            _promptStudioState.value = PromptStudioState.Error("Please enter a topic or character description (कृपया कोई टॉपिक या विषय लिखें)")
            return
        }

        viewModelScope.launch {
            _promptStudioState.value = PromptStudioState.Generating("Synthesizing prompt engineering neural tokens...")

            val result = promptStudioRepository.generatePromptStudioPackage(
                subject = currentSubject,
                style = _selectedStyle.value,
                aspectRatio = _selectedAspectRatio.value,
                customLighting = _customLighting.value
            )

            result.onSuccess { promptPackage ->
                _promptStudioState.value = PromptStudioState.Success(promptPackage)
            }.onFailure { error ->
                _promptStudioState.value = PromptStudioState.Error(error.message ?: "Failed to generate prompts")
            }
        }
    }

    fun copyToClipboard(label: String, text: String, context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "✅ $label copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadPreset(presetSubject: String, presetStyle: PromptStyle, presetRatio: PromptAspectRatio) {
        _subject.value = presetSubject
        _selectedStyle.value = presetStyle
        _selectedAspectRatio.value = presetRatio
        generatePrompts()
    }
}

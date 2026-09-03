package com.example.presentation.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ScanMode
import com.example.data.model.ScannedDocumentItem
import com.example.data.model.TargetLanguage
import com.example.data.model.VisionScannerState
import com.example.data.repository.VisionScannerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisionScannerViewModel(
    private val visionScannerRepository: VisionScannerRepository
) : ViewModel() {

    private val _selectedScanMode = MutableStateFlow(ScanMode.AUTOMOBILE_PARTS)
    val selectedScanMode: StateFlow<ScanMode> = _selectedScanMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(TargetLanguage.HINDI)
    val selectedLanguage: StateFlow<TargetLanguage> = _selectedLanguage.asStateFlow()

    private val _customInstructions = MutableStateFlow("")
    val customInstructions: StateFlow<String> = _customInstructions.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _scannerState = MutableStateFlow<VisionScannerState>(VisionScannerState.Idle)
    val scannerState: StateFlow<VisionScannerState> = _scannerState.asStateFlow()

    val recentScans: StateFlow<List<ScannedDocumentItem>> = visionScannerRepository.recentScans

    private val _isZoomDialogOpen = MutableStateFlow(false)
    val isZoomDialogOpen: StateFlow<Boolean> = _isZoomDialogOpen.asStateFlow()

    fun setScanMode(mode: ScanMode) {
        _selectedScanMode.value = mode
    }

    fun setTargetLanguage(language: TargetLanguage) {
        _selectedLanguage.value = language
    }

    fun setCustomInstructions(text: String) {
        _customInstructions.value = text
    }

    fun setImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
        _selectedBitmap.value = null
        if (uri != null) {
            _scannerState.value = VisionScannerState.Idle
        }
    }

    fun setCapturedBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
        _selectedImageUri.value = null
        if (bitmap != null) {
            _scannerState.value = VisionScannerState.Idle
        }
    }

    fun clearMedia() {
        _selectedImageUri.value = null
        _selectedBitmap.value = null
        _scannerState.value = VisionScannerState.Idle
    }

    fun setZoomDialogOpen(open: Boolean) {
        _isZoomDialogOpen.value = open
    }

    fun scanDocument() {
        if (_selectedImageUri.value == null && _selectedBitmap.value == null) {
            _scannerState.value = VisionScannerState.Error("Please select or capture a document/image first (कृपया पहले फोटो लें या अपलोड करें)")
            return
        }

        viewModelScope.launch {
            _scannerState.value = VisionScannerState.Scanning(0.1f, "Initializing Gemini Vision Core...")

            val result = visionScannerRepository.scanDocument(
                imageUri = _selectedImageUri.value,
                bitmap = _selectedBitmap.value,
                scanMode = _selectedScanMode.value,
                targetLanguage = _selectedLanguage.value,
                customInstructions = _customInstructions.value,
                onProgressUpdate = { progress, stage ->
                    _scannerState.value = VisionScannerState.Scanning(progress, stage)
                }
            )

            result.onSuccess { item ->
                _scannerState.value = VisionScannerState.Success(item)
            }.onFailure { error ->
                _scannerState.value = VisionScannerState.Error(error.message ?: "OCR & Extraction failed.")
            }
        }
    }

    fun copyExtractedText(item: ScannedDocumentItem, context: Context) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SAHNAJ AI Scanned Text", item.markdownResult)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "✅ Extracted Markdown copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to copy: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAsPdf(item: ScannedDocumentItem, context: Context) {
        visionScannerRepository.printOrExportPdf(item, context)
    }

    fun shareViaWhatsApp(item: ScannedDocumentItem, context: Context) {
        visionScannerRepository.shareViaWhatsApp(item, context)
    }

    fun loadRecentScan(item: ScannedDocumentItem) {
        _selectedScanMode.value = item.scanMode
        _selectedLanguage.value = item.targetLanguage
        _scannerState.value = VisionScannerState.Success(item)
    }
}

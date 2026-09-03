package com.example.presentation.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.ScanMode
import com.example.data.model.ScannedDocumentItem
import com.example.data.model.TargetLanguage
import com.example.data.model.VisionScannerState
import com.example.presentation.viewmodel.VisionScannerViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisionScannerView(
    viewModel: VisionScannerViewModel,
    onNavigateToApiSettings: () -> Unit
) {
    val context = LocalContext.current
    val selectedScanMode by viewModel.selectedScanMode.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val customInstructions by viewModel.customInstructions.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val selectedBitmap by viewModel.selectedBitmap.collectAsState()
    val scannerState by viewModel.scannerState.collectAsState()
    val recentScans by viewModel.recentScans.collectAsState()
    val isZoomDialogOpen by viewModel.isZoomDialogOpen.collectAsState()

    var showModeDropdown by remember { mutableStateOf(false) }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.setCapturedBitmap(bitmap)
            Toast.makeText(context, "📸 Photo Captured Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // Document / Image Picker Launcher (Supports Images and PDF)
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setImageUri(uri)
            Toast.makeText(context, "📁 Document selected!", Toast.LENGTH_SHORT).show()
        }
    }

    val isScanning = scannerState is VisionScannerState.Scanning
    val hasMedia = selectedImageUri != null || selectedBitmap != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ================= HEADER CARD =================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberRedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberRedDark)
                                .border(BorderStroke(1.dp, CyberRedBright), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "AI VISION & OCR SCANNER",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberTextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "GEMINI 1.5/3.5 FLASH VISION CORE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E0E14),
                        border = BorderStroke(1.dp, CyberRedBorder),
                        modifier = Modifier.clickable { onNavigateToApiSettings() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Key",
                                tint = CyberRedBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "GEMINI KEY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "High-precision AI Vision OCR for Automobile Spare Parts Estimates, Receipts & Bills, Handwritten Notes, and Multi-language Document Translation.",
                    fontSize = 11.5.sp,
                    color = CyberTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // ================= CAMERA & UPLOAD CONTROLS =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Camera Button
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        takePictureLauncher.launch(null)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberRedDark,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, CyberRedBright),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("scanner_open_camera_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        tint = CyberRedBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "TAKE PHOTO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Upload / Pick File Button
            Button(
                onClick = {
                    mediaPickerLauncher.launch("*/*")
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberSurface,
                    contentColor = CyberTextPrimary
                ),
                border = BorderStroke(1.dp, CyberRedBorder),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("scanner_upload_device_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Upload Document",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "UPLOAD / PDF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ================= IMAGE PREVIEW BOX WITH NEON SCANNER =================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberBlack),
            border = BorderStroke(1.dp, if (hasMedia) CyberRedBright else CyberRedBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Captured Document",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { viewModel.setZoomDialogOpen(true) }
                    )
                } else if (selectedImageUri != null) {
                    val uri = selectedImageUri
                    val mime = uri?.let { context.contentResolver.getType(it) } ?: ""
                    val isPdf = mime.contains("pdf", ignoreCase = true) || (uri?.toString()?.endsWith(".pdf", ignoreCase = true) == true)
                    if (isPdf) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF Document",
                                tint = CyberRedBright,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "PDF Document Attached",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = uri?.toString()?.takeLast(32) ?: "",
                                fontSize = 10.sp,
                                color = CyberTextMuted
                            )
                        }
                    } else {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Uploaded Document",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { viewModel.setZoomDialogOpen(true) }
                        )
                    }
                } else {
                    // Empty Placeholder State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            tint = CyberTextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "NO DOCUMENT LOADED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Snap a photo of bills, parts lists, or prescriptions,\nor upload an image / PDF from device.",
                            fontSize = 11.sp,
                            color = CyberTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Media Action Overlay (Zoom & Clear)
                if (hasMedia) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xCC000000),
                            border = BorderStroke(1.dp, CyberRedBorder),
                            modifier = Modifier.clickable { viewModel.setZoomDialogOpen(true) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom",
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xCC000000),
                            border = BorderStroke(1.dp, CyberRedBorder),
                            modifier = Modifier.clickable { viewModel.clearMedia() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = CyberRedBright,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(18.dp)
                            )
                        }
                    }
                }

                // Active Laser Scanning Line Animation
                if (isScanning) {
                    NeonScanningLaserAnimation()
                }
            }
        }

        // ================= SCAN MODES SELECTOR =================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SCAN MODE // मोड चुनें",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            // Grid / List of Modes
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScanMode.values().forEach { mode ->
                    val isSelected = selectedScanMode == mode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyberRedDark else CyberCard,
                        border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setScanMode(mode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = mode.emoji,
                                fontSize = 22.sp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mode.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else CyberTextPrimary
                                )
                                Text(
                                    text = mode.description,
                                    fontSize = 10.sp,
                                    color = if (isSelected) CyberRedBright else CyberTextMuted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = CyberRedBright
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= TARGET LANGUAGE SELECTOR =================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRANSLATE OUTPUT // भाषा:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TargetLanguage.values().forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CyberRedDark else CyberSurface,
                        border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                        modifier = Modifier.clickable { viewModel.setTargetLanguage(lang) }
                    ) {
                        Text(
                            text = "${lang.flag} ${lang.displayName.split(" ").first()}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else CyberTextSecondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        // ================= SCAN & EXTRACT BUTTON =================
        Button(
            onClick = { viewModel.scanDocument() },
            enabled = !isScanning && hasMedia,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberRedBright,
                contentColor = Color.White,
                disabledContainerColor = CyberRedDark,
                disabledContentColor = CyberTextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("scan_extract_data_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "NEURAL VISION SCANNING...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "⚡ SCAN & EXTRACT DATA",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // ================= PROGRESS INDICATOR =================
        AnimatedVisibility(
            visible = isScanning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val scanState = scannerState as? VisionScannerState.Scanning
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = scanState?.stage ?: "Processing Gemini Vision inference...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { scanState?.progress ?: 0.5f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberRedBright,
                        trackColor = CyberSurface
                    )
                }
            }
        }

        // ================= ERROR BANNER =================
        if (scannerState is VisionScannerState.Error) {
            val errorMsg = (scannerState as VisionScannerState.Error).message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0D14)),
                border = BorderStroke(1.dp, CyberRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "VISION OCR FAILED // एरर",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberRedBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMsg,
                        fontSize = 11.5.sp,
                        color = Color.White
                    )

                    if (errorMsg.contains("API Key", ignoreCase = true) || errorMsg.contains("Settings", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToApiSettings,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedDark),
                            border = BorderStroke(1.dp, CyberRedBright)
                        ) {
                            Text("CONFIGURE GEMINI KEY IN SETTINGS", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // ================= STRUCTURED RESULTS CARD =================
        if (scannerState is VisionScannerState.Success) {
            val item = (scannerState as VisionScannerState.Success).item
            StructuredScanResultsCard(
                item = item,
                onCopy = { viewModel.copyExtractedText(item, context) },
                onExportPdf = { viewModel.exportAsPdf(item, context) },
                onShareWhatsApp = { viewModel.shareViaWhatsApp(item, context) }
            )
        }

        // ================= RECENT SCANS HISTORY =================
        if (recentScans.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RECENT SCANNED DOCUMENTS (${recentScans.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentScans.forEach { scan ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberCard,
                            border = BorderStroke(1.dp, CyberRedBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.loadRecentScan(scan) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = scan.scanMode.emoji,
                                        fontSize = 22.sp
                                    )

                                    Column {
                                        Text(
                                            text = scan.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextPrimary
                                        )
                                        Text(
                                            text = "${scan.targetLanguage.displayName} • ${scan.estimatedTotal ?: "Processed in ${scan.durationMs}ms"}",
                                            fontSize = 10.sp,
                                            color = CyberRedBright
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { viewModel.copyExtractedText(scan, context) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = CyberTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.shareViaWhatsApp(scan, context) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = CyberRedBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // ================= ZOOM PREVIEW DIALOG =================
    if (isZoomDialogOpen && hasMedia) {
        Dialog(onDismissRequest = { viewModel.setZoomDialogOpen(false) }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CyberBlack,
                border = BorderStroke(1.dp, CyberRedBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (selectedBitmap != null) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Full Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Full Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    IconButton(
                        onClick = { viewModel.setZoomDialogOpen(false) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color(0xCC000000), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ================= STRUCTURED SCAN RESULTS CARD COMPOSABLE =================
@Composable
fun StructuredScanResultsCard(
    item: ScannedDocumentItem,
    onCopy: () -> Unit,
    onExportPdf: () -> Unit,
    onShareWhatsApp: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, CyberRedBright),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.scanMode.emoji,
                        fontSize = 20.sp
                    )

                    Column {
                        Text(
                            text = "EXTRACTED DATA // ${item.scanMode.name}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberRedBright
                        )
                        Text(
                            text = "Language: ${item.targetLanguage.displayName} • Parsed with Gemini Vision",
                            fontSize = 9.5.sp,
                            color = CyberTextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B2A1E),
                    border = BorderStroke(1.dp, CyberGreen)
                ) {
                    Text(
                        text = "OCR VERIFIED ⚡",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Actions Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Copy Text
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberRedBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCopy() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = CyberRedBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("COPY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Export as PDF / Print
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberRedBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onExportPdf() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Print / PDF",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PRINT / PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Share WhatsApp
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F2618),
                    border = BorderStroke(1.dp, CyberGreen),
                    modifier = Modifier
                        .weight(1.1f)
                        .clickable { onShareWhatsApp() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "WhatsApp",
                            tint = CyberGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WHATSAPP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Formatted Markdown & Tabular Display Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyberBlack,
                border = BorderStroke(1.dp, CyberRedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    MarkdownDocumentRenderer(markdown = item.markdownResult)
                }
            }
        }
    }
}

// ================= CUSTOM MARKDOWN / TABLE RENDERER =================
@Composable
fun MarkdownDocumentRenderer(markdown: String) {
    val lines = markdown.lines()
    var inTable = false
    val tableLines = mutableListOf<String>()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                inTable = true
                tableLines.add(trimmed)
            } else {
                if (inTable && tableLines.isNotEmpty()) {
                    RenderMarkdownTable(tableLines.toList())
                    tableLines.clear()
                    inTable = false
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (trimmed.startsWith("# ")) {
                    Text(
                        text = trimmed.removePrefix("# "),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberRedBright,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else if (trimmed.startsWith("## ")) {
                    Text(
                        text = trimmed.removePrefix("## "),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (trimmed.startsWith("### ")) {
                    Text(
                        text = trimmed.removePrefix("### "),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("•", color = CyberRedBright, fontWeight = FontWeight.Bold)
                        Text(
                            text = trimmed.substring(2).replace("**", ""),
                            fontSize = 11.5.sp,
                            color = CyberTextPrimary,
                            lineHeight = 16.sp
                        )
                    }
                } else if (trimmed.isNotBlank()) {
                    Text(
                        text = trimmed.replace("**", ""),
                        fontSize = 11.5.sp,
                        color = CyberTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (inTable && tableLines.isNotEmpty()) {
            RenderMarkdownTable(tableLines.toList())
            tableLines.clear()
        }
    }
}

// ================= TABLE RENDERER COMPOSABLE =================
@Composable
fun RenderMarkdownTable(lines: List<String>) {
    val rows = lines.filterNot { it.contains("---") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, CyberRedBorder), RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
    ) {
        rows.forEachIndexed { index, row ->
            val isHeader = index == 0
            val cells = row.split("|").filter { it.isNotBlank() }.map { it.trim().replace("**", "") }

            Row(
                modifier = Modifier
                    .background(if (isHeader) CyberRedDark else if (index % 2 == 0) CyberCard else CyberBlack)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                cells.forEach { cell ->
                    Text(
                        text = cell,
                        fontSize = if (isHeader) 10.5.sp else 10.sp,
                        fontWeight = if (isHeader) FontWeight.Black else FontWeight.Normal,
                        color = if (isHeader) Color.White else CyberTextPrimary,
                        modifier = Modifier.width(110.dp)
                    )
                }
            }
        }
    }
}

// ================= NEON SCANNING LASER ANIMATION =================
@Composable
fun NeonScanningLaserAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val y = size.height * laserProgress

        // Laser glow gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    CyberRedBright.copy(alpha = 0.35f),
                    CyberRedBright.copy(alpha = 0.85f),
                    CyberRedBright.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                startY = (y - 20f).coerceAtLeast(0f),
                endY = (y + 20f).coerceAtMost(size.height)
            ),
            topLeft = Offset(0f, (y - 20f).coerceAtLeast(0f)),
            size = androidx.compose.ui.geometry.Size(size.width, 40f)
        )

        // Laser Center Line
        drawLine(
            color = Color.White,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 3.dp.toPx()
        )
    }
}

package com.example.presentation.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SahNajApplication
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.util.AutomotiveDiagnosticsEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomotiveDiagnosticsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as? SahNajApplication

    var queryText by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var diagnosticResult by remember { mutableStateOf<String?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val quickDtcCodes = listOf("P0300", "P0171", "P0420", "P0113", "P0128", "C0035", "U0100")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedImageBitmap = bitmap
            } catch (_: Exception) {}
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedImageBitmap = bitmap
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    fun runDiagnostics() {
        if (queryText.isBlank() && selectedImageBitmap == null) return
        isAnalyzing = true
        diagnosticResult = null

        scope.launch {
            if (selectedImageBitmap != null) {
                val result = AutomotiveDiagnosticsEngine.analyzeMechanicalImage(
                    bitmap = selectedImageBitmap!!,
                    contextPrompt = queryText,
                    userPreferences = app?.userPreferences
                )
                diagnosticResult = result
            } else {
                val result = AutomotiveDiagnosticsEngine.analyzeAutomotiveIssue(
                    query = queryText,
                    vehicleModel = vehicleModel,
                    userPreferences = app?.userPreferences
                )
                diagnosticResult = result
            }
            isAnalyzing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Automotive Diagnostics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "मैकेनिकल व इलेक्ट्रिकल फॉल्ट डायग्नोस्टिक्स",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CyberRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "OBD-II & Mechanical AI Core",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "DTC Fault Code lookup, engine misfire diagnostics & camera part scanning",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            // Quick DTC Code Chips
            Text(
                text = "Quick DTC / OBD-II Fault Codes:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyberTextMuted
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickDtcCodes.take(4).forEach { code ->
                    FilterChip(
                        selected = queryText.contains(code),
                        onClick = {
                            queryText = "$code fault code fix & symptoms"
                            runDiagnostics()
                        },
                        label = { Text(code, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberRed,
                            selectedLabelColor = CyberBlack
                        )
                    )
                }
            }

            // Vehicle Model & Query inputs
            OutlinedTextField(
                value = vehicleModel,
                onValueChange = { vehicleModel = it },
                label = { Text("Vehicle Make / Model (Optional, e.g., Hyundai i20, Royal Enfield 350)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberRedBright,
                    unfocusedBorderColor = CyberRedBorder
                )
            )

            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                label = { Text("Problem symptoms, noises, warning lights, or DTC error code") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberRedBright,
                    unfocusedBorderColor = CyberRedBorder
                )
            )

            // Multimodal Camera / Image Attachment Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = CyberRedBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Snap Part", color = CyberTextPrimary, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = CyberRedBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery", color = CyberTextPrimary, fontSize = 13.sp)
                }
            }

            // Display Attached Image Preview
            selectedImageBitmap?.let { bmp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberRedBright)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Selected Mechanical Part",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                        IconButton(
                            onClick = { selectedImageBitmap = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Clear", tint = CyberRed)
                        }
                    }
                }
            }

            // Analyze Action Button
            Button(
                onClick = { runDiagnostics() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isAnalyzing && (queryText.isNotBlank() || selectedImageBitmap != null),
                colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyberBlack, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Analyzing Mechanical Subsystems...", color = CyberBlack, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Build, contentDescription = null, tint = CyberBlack)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Run Master Diagnostics", color = CyberBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            // Diagnostic Results View
            AnimatedVisibility(visible = diagnosticResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberGreen)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = CyberGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Automotive Diagnostic Report",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = CyberRedBorder)
                        Spacer(modifier = Modifier.height(10.dp))
                        SelectionContainer {
                            Text(
                                text = diagnosticResult ?: "",
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = CyberTextPrimary,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                }
            }
        }
    }
}

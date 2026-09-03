package com.example.presentation.screens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.projection.MediaProjectionManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.permissions.PermissionManager
import com.example.presentation.viewmodel.LiveVisionMode
import com.example.presentation.viewmodel.LiveVisionState
import com.example.presentation.viewmodel.LiveVisionViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberRedGlow
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextDark
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.voice.SpeechState
import java.util.concurrent.Executors

@Composable
fun LiveVisionScreen(
    viewModel: LiveVisionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val focusManager = LocalFocusManager.current
    val permissionManager = remember { PermissionManager(context) }

    val visionMode by viewModel.visionMode.collectAsState()
    val visionState by viewModel.visionState.collectAsState()
    val isContinuousScanning by viewModel.isContinuousScanning.collectAsState()
    val lensFacing by viewModel.lensFacing.collectAsState()
    val isTorchEnabled by viewModel.isTorchEnabled.collectAsState()
    val latestAnswer by viewModel.latestAnswer.collectAsState()
    val latestQuery by viewModel.latestQuery.collectAsState()
    val isScreenSharing by viewModel.isScreenSharing.collectAsState()
    val screenShareFrame by viewModel.screenShareFrame.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val detectedKeywords by viewModel.detectedKeywords.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var showTextInput by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }

    // Screen capture launcher for MediaProjection
    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.startScreenSharing(context, result.resultCode, result.data!!)
            Toast.makeText(context, "🟢 Screen Share Mode Activated!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Screen share permission was declined.", Toast.LENGTH_SHORT).show()
            viewModel.setVisionMode(LiveVisionMode.CAMERA)
        }
    }

    // Camera permission launcher
    var hasCameraPermission by remember {
        mutableStateOf(permissionManager.hasPermission(Manifest.permission.CAMERA))
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required for Live Vision", Toast.LENGTH_SHORT).show()
        }
    }

    // Mic permission launcher
    var hasMicPermission by remember {
        mutableStateOf(permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO))
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    // Set frame provider callback to extract high-res bitmap from PreviewView
    DisposableEffect(previewViewRef) {
        viewModel.setCameraFrameProvider {
            previewViewRef?.bitmap
        }
        onDispose {
            viewModel.setCameraFrameProvider { null }
        }
    }

    // Handle torch changes
    LaunchedEffect(isTorchEnabled, cameraRef) {
        try {
            cameraRef?.cameraControl?.enableTorch(isTorchEnabled)
        } catch (_: Exception) {}
    }

    // Animated laser scanning line
    val infiniteTransition = rememberInfiniteTransition(label = "LaserScan")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserPosition"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        // ================= BACKGROUND / VIDEO VIEW =================
        if (visionMode == LiveVisionMode.CAMERA) {
            if (hasCameraPermission) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                        previewViewRef = previewView

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }

                            val cameraSelector = CameraSelector.Builder()
                                .requireLensFacing(lensFacing)
                                .build()

                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview
                                )
                                cameraRef = cam
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    update = { view ->
                        previewViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Camera permission prompt
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Camera Permission Required",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "SAHNAJ AI needs camera access to observe real-world objects and assist you in real-time.",
                            fontSize = 13.sp,
                            color = CyberTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Grant Camera Permission", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Screen Share View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0C0910)),
                contentAlignment = Alignment.Center
            ) {
                if (isScreenSharing && screenShareFrame != null) {
                    Image(
                        bitmap = screenShareFrame!!.asImageBitmap(),
                        contentDescription = "Live Screen Display",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1428))
                                .border(BorderStroke(1.5.dp, CyberRedBorder), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ScreenShare,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Text(
                            text = "Live Screen Share Vision",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "Share your Android screen with SAHNAJ to analyze apps, troubleshooting codes, documents, or UI layouts live.",
                            fontSize = 13.sp,
                            color = CyberTextMuted,
                            textAlign = TextAlign.Center
                        )

                        if (!isScreenSharing) {
                            Button(
                                onClick = {
                                    val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                                    if (mpm != null) {
                                        screenCaptureLauncher.launch(mpm.createScreenCaptureIntent())
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ScreenShare, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Screen Share", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.stopScreenSharing(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop Sharing", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ================= NEON VIEWFINDER HUD OVERLAY =================
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Viewfinder bounding box inset
            val padX = w * 0.08f
            val padY = h * 0.14f
            val boxW = w - (padX * 2)
            val boxH = h * 0.46f
            val cornerLen = 36.dp.toPx()

            val left = padX
            val top = padY
            val right = padX + boxW
            val bottom = padY + boxH

            // 4 Corner Brackets (Glowing Neon Red / Cyan)
            val cornerColor = if (visionState is LiveVisionState.Scanning) Color(0xFF00E5FF) else CyberRedBright
            val strokeW = 3.dp.toPx()

            // Top-Left
            drawLine(cornerColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
            drawLine(cornerColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)

            // Top-Right
            drawLine(cornerColor, Offset(right, top), Offset(right - cornerLen, top), strokeW)
            drawLine(cornerColor, Offset(right, top), Offset(right, top + cornerLen), strokeW)

            // Bottom-Left
            drawLine(cornerColor, Offset(left, bottom), Offset(left + cornerLen, bottom), strokeW)
            drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - cornerLen), strokeW)

            // Bottom-Right
            drawLine(cornerColor, Offset(right, bottom), Offset(right - cornerLen, bottom), strokeW)
            drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - cornerLen), strokeW)

            // Central Targeting Reticle
            val cx = left + boxW / 2
            val cy = top + boxH / 2
            val crossLen = 14.dp.toPx()
            drawLine(cornerColor.copy(alpha = 0.5f), Offset(cx - crossLen, cy), Offset(cx + crossLen, cy), 1.5.dp.toPx())
            drawLine(cornerColor.copy(alpha = 0.5f), Offset(cx, cy - crossLen), Offset(cx, cy + crossLen), 1.5.dp.toPx())
            drawCircle(cornerColor.copy(alpha = 0.35f), radius = 22.dp.toPx(), center = Offset(cx, cy), style = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx()))

            // Animated Laser Scanning Line
            val laserY = top + (boxH * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        cornerColor.copy(alpha = 0.85f),
                        Color.White,
                        cornerColor.copy(alpha = 0.85f),
                        Color.Transparent
                    )
                ),
                start = Offset(left, laserY),
                end = Offset(right, laserY),
                strokeWidth = 2.5.dp.toPx()
            )
        }

        // ================= TOP BAR CONTROLS =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xE6050308), Color(0x99050308), Color.Transparent)
                    )
                )
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1624))
                        .border(1.dp, Color(0xFF2E243A), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CyberTextPrimary
                    )
                }

                // Dual Mode Switch Pill (Camera / Screen Share)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF140E1C))
                        .border(1.dp, CyberRedBorder.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Camera Mode
                    ModePillItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Camera",
                        isSelected = visionMode == LiveVisionMode.CAMERA,
                        onClick = { viewModel.setVisionMode(LiveVisionMode.CAMERA) }
                    )

                    // Screen Share Mode
                    ModePillItem(
                        icon = Icons.Default.ScreenShare,
                        label = "Screen Share",
                        isSelected = visionMode == LiveVisionMode.SCREEN_SHARE,
                        onClick = {
                            if (!isScreenSharing) {
                                val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
                                if (mpm != null) {
                                    screenCaptureLauncher.launch(mpm.createScreenCaptureIntent())
                                }
                            } else {
                                viewModel.setVisionMode(LiveVisionMode.SCREEN_SHARE)
                            }
                        }
                    )
                }

                // Action controls (Torch & Lens switch)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (visionMode == LiveVisionMode.CAMERA) {
                        // Torch Toggle
                        IconButton(
                            onClick = { viewModel.toggleTorch() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isTorchEnabled) CyberRedBright else Color(0xFF1C1624))
                                .border(1.dp, Color(0xFF2E243A), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Torch",
                                tint = if (isTorchEnabled) Color.White else CyberTextMuted,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Lens Switch
                        IconButton(
                            onClick = { viewModel.toggleCameraLens() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1C1624))
                                .border(1.dp, Color(0xFF2E243A), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Camera",
                                tint = CyberTextPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Continuous Scanning Toggle & Live Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Status Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC181222))
                        .border(1.dp, if (visionState is LiveVisionState.Scanning) Color(0xFF00E5FF) else CyberRedBright, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (visionState is LiveVisionState.Scanning) Color(0xFF00E5FF) else Color(0xFF00FF9D))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (visionState) {
                            is LiveVisionState.Scanning -> "ANALYZING FRAME..."
                            is LiveVisionState.Speaking -> "SPEAKING ALOUD 🔊"
                            is LiveVisionState.Error -> "RETRYING"
                            else -> if (isContinuousScanning) "CONTINUOUS STREAM (1.8s)" else "SAHNAJ VISION LIVE"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                // Continuous Auto-Stream Toggle Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isContinuousScanning) CyberRedContainer else Color(0xCC181222))
                        .border(1.dp, if (isContinuousScanning) CyberRedBright else Color(0xFF332640), RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = { viewModel.toggleContinuousScanning() }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Auto Stream",
                        tint = if (isContinuousScanning) CyberRedBright else CyberTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isContinuousScanning) "Auto Stream: ON" else "Auto Stream: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isContinuousScanning) CyberRedBright else CyberTextMuted
                    )
                }
            }
        }

        // ================= BOTTOM CONVERSATIONAL HUD & CONTROLS =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC06040A), Color(0xFA06040A), CyberBlack)
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Live AI Speech Response Card
            AnimatedVisibility(
                visible = latestAnswer.isNotBlank() || visionState is LiveVisionState.Scanning || visionState is LiveVisionState.Error,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xF215101F)),
                    border = BorderStroke(1.dp, CyberRedBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header row with Query & TTS controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (latestQuery.isNotBlank()) "\"$latestQuery\"" else "Visual Analysis",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberRedBright,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // TTS Play/Stop Button
                            IconButton(
                                onClick = {
                                    if (isSpeaking) {
                                        viewModel.stopSpeaking()
                                    } else if (latestAnswer.isNotBlank()) {
                                        viewModel.triggerAnalysisWithPrompt(latestQuery.ifBlank { "Describe again" })
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Voice output",
                                    tint = if (isSpeaking) CyberRedBright else CyberTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Answer Text / Loading
                        when (val state = visionState) {
                            is LiveVisionState.Scanning -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFF00E5FF),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.message,
                                        fontSize = 13.5.sp,
                                        color = Color(0xFFE0F7FA)
                                    )
                                }
                            }
                            is LiveVisionState.Error -> {
                                Text(
                                    text = "⚠️ ${state.message}",
                                    fontSize = 13.5.sp,
                                    color = Color(0xFFFF8A80)
                                )
                            }
                            else -> {
                                Text(
                                    text = latestAnswer,
                                    fontSize = 14.sp,
                                    color = CyberTextPrimary,
                                    lineHeight = 20.sp,
                                    maxLines = 5,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Detected keywords / tag chips
                        if (detectedKeywords.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                detectedKeywords.forEach { tag ->
                                    Surface(
                                        color = Color(0xFF231A30),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCE93D8),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Prompt Chips (One-tap visual queries)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickPromptChip("🔍 Yeh kya hai?", "Yeh kya hai aur kaise kaam karta hai?") { prompt ->
                    viewModel.triggerAnalysisWithPrompt(prompt)
                }
                QuickPromptChip("⚙️ Identify Part", "Is mechanical/electronic part ka naam aur specification batayein.") { prompt ->
                    viewModel.triggerAnalysisWithPrompt(prompt)
                }
                QuickPromptChip("📝 Read & Translate", "Is frame mein jo text ya label hai use padho aur Hindi mein samjhao.") { prompt ->
                    viewModel.triggerAnalysisWithPrompt(prompt)
                }
                QuickPromptChip("🛠️ Diagnose Issue", "Isme kya kharabi ya problem dikh rahi hai? Solution batayein.") { prompt ->
                    viewModel.triggerAnalysisWithPrompt(prompt)
                }
                QuickPromptChip("📱 Screen UI Help", "Screen par jo UI element hai uska matlab samjhao.") { prompt ->
                    viewModel.triggerAnalysisWithPrompt(prompt)
                }
            }

            // Optional Manual Query Input Field
            AnimatedVisibility(visible = showTextInput) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Type visual question...", color = CyberTextMuted, fontSize = 13.5.sp) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val q = textInput.trim()
                                    textInput = ""
                                    focusManager.clearFocus()
                                    viewModel.triggerAnalysisWithPrompt(q)
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = CyberRedBright)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                val q = textInput.trim()
                                textInput = ""
                                focusManager.clearFocus()
                                viewModel.triggerAnalysisWithPrompt(q)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF140F1E),
                        unfocusedContainerColor = Color(0xFF140F1E),
                        focusedBorderColor = CyberRedBorder,
                        unfocusedBorderColor = Color(0xFF2C2038),
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        cursorColor = CyberRedBright
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(bottom = 8.dp)
                )
            }

            // Primary Interactive Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle Type / Text Input Button
                IconButton(
                    onClick = { showTextInput = !showTextInput },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (showTextInput) CyberRedBright else Color(0xFF181224))
                        .border(1.dp, Color(0xFF2E2440), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Type Query",
                        tint = if (showTextInput) Color.White else CyberTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Central Shutter / Ask Action Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(54.dp)
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(CyberRedDark, CyberRedBright, CyberRed)
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                viewModel.triggerAnalysisWithPrompt("Describe this scene, identify main objects, and explain them concisely in Hindi/Hinglish.")
                            }
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask SAHNAJ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Live Mic Button (Voice Query on Camera)
                val isListening = speechState is SpeechState.Listening
                IconButton(
                    onClick = {
                        if (hasMicPermission) {
                            if (isListening) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListeningForQuery()
                            }
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isListening) CyberRedBright else Color(0xFF181224))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isListening) Color.White else CyberRedBorder
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Voice Query",
                        tint = if (isListening) Color.White else CyberRedBright,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModePillItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) CyberRedBright else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else CyberTextMuted,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else CyberTextMuted
        )
    }
}

@Composable
private fun QuickPromptChip(
    label: String,
    prompt: String,
    onClick: (String) -> Unit
) {
    Surface(
        color = Color(0xE61A1424),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF332742)),
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = true),
            onClick = { onClick(prompt) }
        )
    ) {
        Text(
            text = label,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = CyberTextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

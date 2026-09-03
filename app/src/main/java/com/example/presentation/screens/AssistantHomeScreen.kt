package com.example.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import com.example.data.model.AiProvidersConfig
import com.example.presentation.components.media.ImageGeneratorView
import com.example.presentation.components.media.VideoGeneratorView
import com.example.presentation.viewmodel.MediaGenerationViewModel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.personality.PersonalityResponses
import com.example.presentation.components.ConfirmationDialog
import com.example.presentation.components.CyberErrorBanner
import com.example.presentation.components.HolographicVoiceOrb
import com.example.presentation.components.ReactiveFuturisticBackground
import com.example.presentation.components.SahNajBottomBar
import com.example.presentation.components.VoiceOrb
import com.example.presentation.navigation.Screen
import com.example.presentation.components.VoiceStudioView
import com.example.presentation.components.VisionScannerView
import com.example.presentation.components.PromptStudioView
import com.example.presentation.viewmodel.AssistantStatus
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.presentation.viewmodel.VoiceStudioViewModel
import com.example.presentation.viewmodel.VisionScannerViewModel
import com.example.presentation.viewmodel.PromptStudioViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

enum class AssistantMode(val title: String, val emoji: String, val subtitle: String) {
    CHAT("CHAT", "💬", "Voice & LLM Assistant"),
    PROMPT_STUDIO("PROMPTS", "🪄", "3D Avatar & Thumbnail Studio"),
    VISION_SCANNER("SCANNER", "🔍", "AI Vision & Part OCR"),
    VOICE_STUDIO("VOICE", "🎙️", "ElevenLabs Dubbing"),
    IMAGE_GEN("IMAGE", "🎨", "Neural Diffusion AI"),
    VIDEO_GEN("VIDEO", "🎬", "Motion Video AI")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantHomeScreen(
    assistantViewModel: AssistantViewModel,
    mediaGenerationViewModel: MediaGenerationViewModel? = null,
    voiceStudioViewModel: VoiceStudioViewModel? = null,
    visionScannerViewModel: VisionScannerViewModel? = null,
    promptStudioViewModel: PromptStudioViewModel? = null,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAccessibilitySetup: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSubscription: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var currentMode by remember { mutableStateOf(AssistantMode.CHAT) }

    val status by assistantViewModel.status.collectAsState()
    val rmsDb by assistantViewModel.rmsDb.collectAsState()
    val partialText by assistantViewModel.partialText.collectAsState()
    val isSpeaking by assistantViewModel.isSpeaking.collectAsState()
    val isAccessibilityActive by assistantViewModel.isAccessibilityActive.collectAsState()
    val lastUserSpeech by assistantViewModel.lastUserSpeech.collectAsState()
    val lastAiResponse by assistantViewModel.lastAiResponse.collectAsState()
    val currentAction by assistantViewModel.currentAction.collectAsState()
    val activeErrorBanner by assistantViewModel.activeErrorBanner.collectAsState()
    val visualResult by com.example.presentation.components.VisualSearchEngine.currentResult.collectAsState()
    val isVisualLoading by com.example.presentation.components.VisualSearchEngine.isLoading.collectAsState()

    val currentAiModel by assistantViewModel.aiModel.collectAsState()
    val providerKeys by assistantViewModel.providerKeysFlow.collectAsState()
    var isModelMenuExpanded by remember { mutableStateOf(false) }

    var manualTextInput by remember { mutableStateOf("") }

    // Auto-navigate to subscription paywall when OPEN_PAYWALL or SUBSCRIPTION_QUERY action is received
    androidx.compose.runtime.LaunchedEffect(currentAction) {
        val act = currentAction?.action
        if (act == com.example.data.model.ActionType.OPEN_PAYWALL || act == com.example.data.model.ActionType.SUBSCRIPTION_QUERY) {
            kotlinx.coroutines.delay(1200) // Allow voice to announce before opening paywall
            onNavigateToSubscription()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistantViewModel.startListening()
        }
    }

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "BACK TO DASHBOARD",
                            tint = CyberRedBright
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberRedContainer)
                                .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(6.dp))
                        ) {
                            Text(
                                text = "SN",
                                fontFamily = FontFamily.Monospace,
                                color = CyberRedBright,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column {
                            Text(
                                text = "${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} // VOICE CONSOLE",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "NEURAL MATRIX ONLINE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "SETTINGS",
                            tint = CyberRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                )
            )
        },
        bottomBar = {
            SahNajBottomBar(
                currentRoute = Screen.Assistant.route,
                onNavigate = { route ->
                    when (route) {
                        Screen.Dashboard.route -> onNavigateToDashboard()
                        Screen.Settings.route -> onNavigateToSettings()
                        else -> {}
                    }
                },
                onOrbClick = {
                    if (status is AssistantStatus.Listening) {
                        assistantViewModel.stopListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Smooth, battery-efficient reactive ambient background
            ReactiveFuturisticBackground(
                status = status,
                rmsDb = rmsDb,
                isSpeaking = isSpeaking
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // ================= MODE SWITCHER [ CHAT | PROMPTS | SCANNER | VOICE | IMAGE | VIDEO ] =================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCard)
                        .border(BorderStroke(1.dp, CyberRedBorder), RoundedCornerShape(12.dp))
                        .horizontalScroll(rememberScrollState())
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AssistantMode.values().forEach { mode ->
                        val isSelected = currentMode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberRedDark else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, CyberRedBright) else null,
                            modifier = Modifier
                                .clickable { currentMode = mode }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(mode.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mode.title,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else CyberTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (currentMode) {
                    AssistantMode.PROMPT_STUDIO -> {
                        promptStudioViewModel?.let { promptVm ->
                            PromptStudioView(
                                viewModel = promptVm,
                                onNavigateToApiSettings = onNavigateToSettings,
                                onDirectGenerateImage = { prompt, ratio ->
                                    mediaGenerationViewModel?.let { mediaVm ->
                                        mediaVm.setImagePrompt(prompt)
                                        mediaVm.setAspectRatio(ratio)
                                        currentMode = AssistantMode.IMAGE_GEN
                                        mediaVm.generateImage()
                                    }
                                }
                            )
                        } ?: run {
                            Text("Prompt Studio Module Initializing...", color = CyberTextMuted, fontSize = 12.sp)
                        }
                    }
                    AssistantMode.VISION_SCANNER -> {
                        visionScannerViewModel?.let { scannerVm ->
                            VisionScannerView(
                                viewModel = scannerVm,
                                onNavigateToApiSettings = onNavigateToSettings
                            )
                        } ?: run {
                            Text("Vision Scanner Module Initializing...", color = CyberTextMuted, fontSize = 12.sp)
                        }
                    }
                    AssistantMode.VOICE_STUDIO -> {
                        voiceStudioViewModel?.let { voiceVm ->
                            VoiceStudioView(
                                viewModel = voiceVm,
                                onNavigateToApiSettings = onNavigateToSettings
                            )
                        } ?: run {
                            Text("Voice Studio Module Initializing...", color = CyberTextMuted, fontSize = 12.sp)
                        }
                    }
                    AssistantMode.IMAGE_GEN -> {
                        mediaGenerationViewModel?.let { mediaVm ->
                            ImageGeneratorView(
                                viewModel = mediaVm,
                                onNavigateToApiSettings = onNavigateToSettings
                            )
                        } ?: run {
                            Text("Media Generation Module Initializing...", color = CyberTextMuted, fontSize = 12.sp)
                        }
                    }
                    AssistantMode.VIDEO_GEN -> {
                        mediaGenerationViewModel?.let { mediaVm ->
                            VideoGeneratorView(
                                viewModel = mediaVm,
                                onNavigateToApiSettings = onNavigateToSettings
                            )
                        } ?: run {
                            Text("Video Generation Module Initializing...", color = CyberTextMuted, fontSize = 12.sp)
                        }
                    }
                    AssistantMode.CHAT -> {
                        // Cyber Status Badges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CyberMiniBadge(
                                label = if (status is AssistantStatus.Listening) "LISTENING" else "VOICE CORE READY",
                                color = if (status is AssistantStatus.Listening) CyberGreen else CyberRedBright
                            )
                            val displayModelName = remember(currentAiModel) {
                                when {
                                    currentAiModel.contains("pro", ignoreCase = true) -> "GEMINI PRO"
                                    currentAiModel.contains("flash", ignoreCase = true) || currentAiModel.isBlank() -> "GEMINI FLASH"
                                    else -> {
                                        val prov = AiProvidersConfig.getProviderById(currentAiModel)
                                        prov?.name?.uppercase() ?: currentAiModel.uppercase()
                                    }
                                }
                            }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCard)
                                        .border(BorderStroke(1.dp, CyberRedBright.copy(alpha = 0.8f)), RoundedCornerShape(4.dp))
                                        .clickable { isModelMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "MODEL: $displayModelName",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CyberRedBright
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select AI Model",
                                            tint = CyberRedBright,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isModelMenuExpanded,
                                    onDismissRequest = { isModelMenuExpanded = false },
                                    modifier = Modifier.background(CyberCard).border(1.dp, CyberRedBorder, RoundedCornerShape(8.dp))
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("✨ Google Gemini Flash", color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Fast responses & system tools", color = CyberTextMuted, fontSize = 10.sp)
                                            }
                                        },
                                        onClick = {
                                            assistantViewModel.setAiModel("gemini-flash")
                                            isModelMenuExpanded = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text("🌟 Google Gemini Pro", color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("High reasoning & detailed planning", color = CyberTextMuted, fontSize = 10.sp)
                                            }
                                        },
                                        onClick = {
                                            assistantViewModel.setAiModel("gemini-pro")
                                            isModelMenuExpanded = false
                                        }
                                    )

                                    val activeProviders = AiProvidersConfig.ALL_PROVIDERS.filter { p ->
                                        p.id != "gemini" && (providerKeys[p.id]?.isNotBlank() == true || providerKeys[p.localStorageKey]?.isNotBlank() == true)
                                    }

                                    if (activeProviders.isNotEmpty()) {
                                        androidx.compose.material3.HorizontalDivider(color = CyberSurface, modifier = Modifier.padding(vertical = 4.dp))
                                        activeProviders.forEach { prov ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            Text(prov.accentEmoji, fontSize = 12.sp)
                                                            Text(prov.name, color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        }
                                                        Text("${prov.badge} • ${prov.useCase}", color = CyberGreen, fontSize = 9.5.sp)
                                                    }
                                                },
                                                onClick = {
                                                    assistantViewModel.setAiModel(prov.id)
                                                    isModelMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            CyberMiniBadge(
                                label = if (isAccessibilityActive) "ACCESSIBILITY: ACTIVE" else "ACCESSIBILITY: OFF",
                                color = if (isAccessibilityActive) CyberGreen else CyberAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Live 17-year-old Cyber Girl Avatar
                        SahnajCyberGirlAvatar(
                            status = status,
                            rmsDb = rmsDb,
                            isSpeaking = isSpeaking,
                            onClick = {
                                if (status is AssistantStatus.Listening) {
                                    assistantViewModel.stopListening()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // State & Instruction Label (ALL CAPS)
                        val statusLabel = when (status) {
                            is AssistantStatus.Listening -> "LISTENING... SPEAK COMMAND"
                            is AssistantStatus.Thinking -> "THINKING // ANALYZING INTENT..."
                            is AssistantStatus.Speaking -> "${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} IS SPEAKING..."
                            is AssistantStatus.AwaitingConfirmation -> "CONFIRMATION REQUIRED"
                            is AssistantStatus.ActionExecuted -> "COMMAND EXECUTED SUCCESSFULLY"
                            is AssistantStatus.Error -> "ERROR // PLEASE TRY AGAIN"
                            is AssistantStatus.Idle -> "TAP MIC OR SAY '${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY}' TO TRIGGER"
                        }

                        Text(
                            text = statusLabel,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = when (status) {
                                is AssistantStatus.Listening -> CyberGreen
                                is AssistantStatus.Thinking -> CyberAmber
                                is AssistantStatus.Speaking -> CyberRedBright
                                else -> CyberTextPrimary
                            },
                            textAlign = TextAlign.Center
                        )

                        // Live partial recognition transcript
                        if (partialText.isNotBlank() && status is AssistantStatus.Listening) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "> \"${partialText.uppercase()}\"",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Pending Confirmation Dialog (For High Risk Actions)
                        if (status is AssistantStatus.AwaitingConfirmation) {
                            val action = (status as AssistantStatus.AwaitingConfirmation).action
                            Spacer(modifier = Modifier.height(16.dp))
                            ConfirmationDialog(
                                action = action,
                                onConfirm = { assistantViewModel.confirmPendingAction() },
                                onDecline = { assistantViewModel.declinePendingAction() }
                            )
                        }

                        // In-Memory Active Session Activity Box (Only current interaction, no history list)
                        if (lastUserSpeech.isNotBlank() || lastAiResponse.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                border = BorderStroke(1.dp, CyberRedBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (lastUserSpeech.isNotBlank()) {
                                        Column {
                                            Text(
                                                text = "> USER INPUT:",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyberRedBright
                                            )
                                            Text(
                                                text = lastUserSpeech.uppercase(),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyberTextPrimary
                                            )
                                        }
                                    }

                                    val action = currentAction?.action
                                    val showActionChip = action != null &&
                                        action != com.example.data.model.ActionType.SPEAK_TEXT &&
                                        action != com.example.data.model.ActionType.GENERAL_QNA &&
                                        action != com.example.data.model.ActionType.GENERAL_QUESTION &&
                                        action != com.example.data.model.ActionType.UNKNOWN &&
                                        currentAction?.target != "SMART_HUMAN_ENGINE"

                                    if (showActionChip && currentAction != null) {
                                        val actionName = currentAction!!.action.name
                                        val target = currentAction!!.target
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CyberRedContainer)
                                                .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Terminal,
                                                contentDescription = null,
                                                tint = CyberRedBright,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (target.isNotBlank()) "DEVICE ACTION: $actionName // $target".uppercase() else "DEVICE ACTION: $actionName".uppercase(),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = CyberRedBright
                                            )
                                        }
                                    }

                                    val cleanDisplayResponse = remember(lastAiResponse) {
                                        com.example.domain.personality.SmartHumanEngine.sanitizeResponse(lastAiResponse)
                                    }

                                    if (cleanDisplayResponse.isNotBlank()) {
                                        Column {
                                            Text(
                                                text = "> ${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} RESPONSE:",
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CyberGreen
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = cleanDisplayResponse,
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Normal,
                                                lineHeight = 19.sp,
                                                color = CyberTextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Autonomous In-App Visual & Web Grounding Feed Card
                        if (isVisualLoading) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = CyberCard),
                                border = BorderStroke(1.dp, CyberRedBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        color = CyberRedBright,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "GROUNDING // RETRIEVING VISUAL & WEB REPOSITORY...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberRedBright
                                    )
                                }
                            }
                        }

                        if (visualResult != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            com.example.presentation.components.VisualGroundingCyberCard(
                                result = visualResult!!,
                                onDismiss = { com.example.presentation.components.VisualSearchEngine.clear() }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Cyber Voice Prompt Suggestions
                        Text(
                            text = "COMMAND SAMPLES",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberRedBright,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val suggestions = listOf(
                                "TAJ MAHAL PHOTO",
                                "PM KA PHOTO DIKHAO",
                                "ENGINE PARTS",
                                "CHANDRAYAAN-3 DETAILS",
                                "WHATSAPP KHOLO",
                                "OPEN YOUTUBE",
                                "INDIA KI CAPITAL KYA HAI?",
                                "CALL CONTACT",
                                "OPEN SETTINGS"
                            )

                            for (cmd in suggestions) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberCard)
                                        .border(BorderStroke(1.dp, CyberRedBorder), RoundedCornerShape(4.dp))
                                        .clickable {
                                            assistantViewModel.processDirectTextCommand(cmd)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cmd,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Direct Terminal Input Field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = manualTextInput,
                                onValueChange = { manualTextInput = it.uppercase() },
                                placeholder = {
                                    Text(
                                        text = "TYPE COMMAND OR QUERY...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = CyberTextMuted
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (manualTextInput.isNotBlank()) {
                                            focusManager.clearFocus()
                                            assistantViewModel.processDirectTextCommand(manualTextInput)
                                            manualTextInput = ""
                                        }
                                    }
                                ),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(6.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberRedBright,
                                    unfocusedBorderColor = CyberRedBorder,
                                    focusedTextColor = CyberTextPrimary,
                                    unfocusedTextColor = CyberTextPrimary,
                                    focusedContainerColor = CyberCard,
                                    unfocusedContainerColor = CyberCard
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberRedBright)
                                    .clickable {
                                        if (manualTextInput.isNotBlank()) {
                                            focusManager.clearFocus()
                                            assistantViewModel.processDirectTextCommand(manualTextInput)
                                            manualTextInput = ""
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "EXECUTE COMMAND",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Error Banner Overlay
            CyberErrorBanner(
                errorData = activeErrorBanner,
                onDismiss = { assistantViewModel.dismissErrorBanner() },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CyberMiniBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(CyberCard)
            .border(BorderStroke(1.dp, color.copy(alpha = 0.5f)), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}

// =========================================================================
// LIVE SAHNAJ CYBER GIRL AVATAR
// =========================================================================

@Composable
fun SahnajCyberGirlAvatar(
    modifier: Modifier = Modifier,
    status: AssistantStatus = AssistantStatus.Idle,
    rmsDb: Float = 0f,
    isSpeaking: Boolean = false,
    onClick: () -> Unit
) {
    HolographicVoiceOrb(
        status = status,
        rmsDb = rmsDb,
        isSpeaking = isSpeaking,
        onClick = onClick,
        modifier = modifier,
        orbSize = 220.dp
    )
}


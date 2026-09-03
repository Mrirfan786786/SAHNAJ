package com.example.presentation.screens

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.personality.PersonalityResponses
import com.example.permissions.PermissionManager
import com.example.presentation.components.CyberErrorBanner
import com.example.presentation.components.HolographicVoiceOrb
import com.example.presentation.components.SahNajBottomBar
import com.example.presentation.navigation.Screen
import com.example.presentation.viewmodel.AssistantStatus
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.presentation.viewmodel.HistoryViewModel
import com.example.presentation.viewmodel.ProfileUiState
import com.example.presentation.viewmodel.ProfileViewModel
import com.example.presentation.viewmodel.SettingsViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    assistantViewModel: AssistantViewModel,
    settingsViewModel: SettingsViewModel,
    historyViewModel: HistoryViewModel,
    profileViewModel: ProfileViewModel,
    onNavigateToAssistant: () -> Unit,
    onNavigateToLiveVision: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToTriggers: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAutomotive: () -> Unit = {},
    onNavigateToSecurityShield: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val permissionManager = remember { PermissionManager(context) }

    val assistantStatus by assistantViewModel.status.collectAsState()
    val rmsDb by assistantViewModel.rmsDb.collectAsState()
    val isSpeaking by assistantViewModel.isSpeaking.collectAsState()
    val activeErrorBanner by assistantViewModel.activeErrorBanner.collectAsState()
    val historyItems by historyViewModel.historyItems.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()

    var textInput by remember { mutableStateOf("") }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistantViewModel.startListening()
            onNavigateToAssistant()
        }
    }

    // Resolve user's actual display name
    val userName = when (val state = profileState) {
        is ProfileUiState.Content -> {
            val name = state.profile.displayName
            if (name.isNotBlank() && name != "USER") name else "Md Irfan Alam"
        }
        else -> "Md Irfan Alam"
    }

    Scaffold(
        containerColor = CyberBlack,
        bottomBar = {
            SahNajBottomBar(
                currentRoute = Screen.Dashboard.route,
                onNavigate = { route ->
                    when (route) {
                        Screen.Chat.route -> onNavigateToChat()
                        Screen.Triggers.route -> onNavigateToTriggers()
                        Screen.Settings.route -> onNavigateToSettings()
                        else -> {}
                    }
                },
                onOrbClick = {
                    if (permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                        assistantViewModel.startListening()
                        onNavigateToAssistant()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0B10),
                            Color(0xFF09060B),
                            CyberBlack
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Header Row: User Greeting & Notification Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hello, $userName",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "How can I assist you today?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = CyberTextMuted
                        )
                    }

                    // Notification Bell Icon in circular dark badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1A24))
                            .border(BorderStroke(1.dp, Color(0xFF2C2536)), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, radius = 23.dp),
                                onClick = onNavigateToProfile
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications & Profile",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Large Interactive Holographic Voice Orb with orbiting cosmic particles
                HolographicVoiceOrb(
                    status = assistantStatus,
                    rmsDb = rmsDb,
                    isSpeaking = isSpeaking,
                    onClick = {
                        if (permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                            if (assistantStatus is AssistantStatus.Listening) {
                                assistantViewModel.stopListening()
                            } else {
                                assistantViewModel.startListening()
                                onNavigateToAssistant()
                            }
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Command / Query Input Pill
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Ask ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} anything...",
                            color = CyberTextMuted,
                            fontSize = 14.5.sp
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    val prompt = textInput.trim()
                                    textInput = ""
                                    focusManager.clearFocus()
                                    assistantViewModel.processDirectTextCommand(prompt)
                                    onNavigateToAssistant()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = CyberRedBright
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (textInput.isNotBlank()) {
                                val prompt = textInput.trim()
                                textInput = ""
                                focusManager.clearFocus()
                                assistantViewModel.processDirectTextCommand(prompt)
                                onNavigateToAssistant()
                            }
                        }
                    ),
                    shape = RoundedCornerShape(26.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF14101A),
                        unfocusedContainerColor = Color(0xFF120E17),
                        focusedBorderColor = CyberRedBorder,
                        unfocusedBorderColor = Color(0xFF282030),
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary,
                        cursorColor = CyberRedBright
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3.5 Live Multimodal Vision Button & Hub
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = onNavigateToLiveVision
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF191122)),
                    border = BorderStroke(1.5.dp, CyberRedBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(CyberRedDark, CyberRedBright)
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Live Vision",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Live Vision AI",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF00FF9D))
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Real-time Multimodal (Camera & Screen)",
                                    fontSize = 12.sp,
                                    color = CyberTextMuted
                                )
                            }
                        }

                        // Toggle icons (Camera & Screen Share badges)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF261936))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera Mode",
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF261936))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ScreenShare,
                                    contentDescription = "Screen Share Mode",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. 2x2 Feature Grid Cards (Voice Mode, Neural Lens, AMOLED Map, Aura Control)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Voice Mode
                    HomeFeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.GraphicEq,
                        title = "Voice Mode",
                        subtitle = "Sahnaj Avatar",
                        onClick = {
                            if (permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                                assistantViewModel.startListening()
                                onNavigateToAssistant()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    )

                    // Neural Lens
                    HomeFeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CameraAlt,
                        title = "Neural Lens",
                        subtitle = "Live Object Scan",
                        onClick = onNavigateToLiveVision
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Automotive Diagnostics Card
                    HomeFeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Build,
                        title = "Auto Diagnostics",
                        subtitle = "OBD-II & Repairs",
                        onClick = onNavigateToAutomotive
                    )

                    // Security Shield Card
                    HomeFeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Security,
                        title = "Security Shield",
                        subtitle = "SOS & Call Bridge",
                        onClick = onNavigateToSecurityShield
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // AMOLED Map
                    HomeFeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Map,
                        title = "AMOLED Map",
                        subtitle = "Vector Guide",
                        onClick = {
                            assistantViewModel.processDirectTextCommand("Open Google Maps")
                            onNavigateToAssistant()
                        }
                    )

                    // Aura Control
                    HomeFeatureCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Tune,
                        title = "Aura Control",
                        subtitle = "Settings",
                        onClick = onNavigateToSettings
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Large "Mission Mode" Card with Constellation / Circuit Background Line Art
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = true),
                            onClick = {
                                assistantViewModel.processDirectTextCommand("Mission Mode activated: Run phone diagnostics and system automation.")
                                onNavigateToAssistant()
                            }
                        ),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
                    border = BorderStroke(1.dp, Color(0xFF2C1C28))
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background Cybernetic Grid / Constellation Canvas
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw subtle circuit and node lines (as seen in MYRA reference)
                            val p1 = Offset(w * 0.32f, h * 0.42f)
                            val p2 = Offset(w * 0.50f, h * 0.65f)
                            val p3 = Offset(w * 0.70f, h * 0.40f)
                            val p4 = Offset(w * 0.88f, h * 0.75f)

                            drawLine(Color(0xFF382234), Offset(0f, h * 0.35f), p1, strokeWidth = 1.2f)
                            drawLine(Color(0xFF382234), p1, p2, strokeWidth = 1.2f)
                            drawLine(Color(0xFF382234), p2, p3, strokeWidth = 1.2f)
                            drawLine(Color(0xFF382234), p3, p4, strokeWidth = 1.2f)

                            drawCircle(CyberRedBright.copy(alpha = 0.5f), radius = 3.5f, center = p1)
                            drawCircle(Color(0xFF00FF9D).copy(alpha = 0.6f), radius = 3.5f, center = p2)
                            drawCircle(CyberRedBright.copy(alpha = 0.5f), radius = 3.5f, center = p3)
                            drawCircle(Color(0xFF7A6BFF).copy(alpha = 0.6f), radius = 3.5f, center = p4)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RocketLaunch,
                                contentDescription = "Mission Mode",
                                tint = CyberRedBright,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Mission Mode",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Give ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} a goal to run autonomously",
                                fontSize = 12.sp,
                                color = CyberTextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // 6. "Quick Directives" Section (Horizontally scrollable)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Quick Directives",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickDirectiveItem(
                            icon = Icons.Default.AutoAwesome,
                            title = "Deep Research",
                            subtitle = "Analyze local weather",
                            onClick = {
                                assistantViewModel.processDirectTextCommand("Aaj ka mausam kaisa hai?")
                                onNavigateToAssistant()
                            }
                        )

                        QuickDirectiveItem(
                            icon = Icons.Default.Image,
                            title = "Image Search",
                            subtitle = "Search aesthetic spaces",
                            onClick = {
                                assistantViewModel.processDirectTextCommand("Search images of futuristic aesthetic spaces")
                                onNavigateToAssistant()
                            }
                        )

                        QuickDirectiveItem(
                            icon = Icons.Default.Code,
                            title = "Coding Cores",
                            subtitle = "Write some Kotlin",
                            onClick = {
                                assistantViewModel.processDirectTextCommand("Write a Kotlin function for sorting list")
                                onNavigateToAssistant()
                            }
                        )

                        QuickDirectiveItem(
                            icon = Icons.Default.Info,
                            title = "${PersonalityResponses.ASSISTANT_NAME_DISPLAY} Specs",
                            subtitle = "Who built ${PersonalityResponses.ASSISTANT_NAME_DISPLAY}?",
                            onClick = {
                                assistantViewModel.processDirectTextCommand("Who built SahNaj AI and what are your specifications?")
                                onNavigateToAssistant()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 7. "Neural Stream" Section (Recent chat/command history preview)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Neural Stream",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "New Thread",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyberRedBright,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onNavigateToChat
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Stream container card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onNavigateToChat),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF140F18)),
                        border = BorderStroke(1.dp, CyberRedBorder.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (historyItems.isEmpty()) {
                                Text(
                                    text = userName,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberRedBright
                                )
                                Text(
                                    text = "Hello SahNaj! Ready to assist you anytime.",
                                    fontSize = 12.5.sp,
                                    color = CyberTextSecondary
                                )
                                Text(
                                    text = "Search aesthetic spaces",
                                    fontSize = 12.5.sp,
                                    color = CyberTextMuted
                                )
                            } else {
                                val recentItems = historyItems.take(3)
                                recentItems.forEach { item ->
                                    Column {
                                        Text(
                                            text = userName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyberRedBright
                                        )
                                        Text(
                                            text = item.commandText,
                                            fontSize = 12.5.sp,
                                            color = CyberTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Active Error Banner Overlay
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
private fun HomeFeatureCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
        border = BorderStroke(1.dp, Color(0xFF2C1C28))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyberRedBright,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = CyberTextMuted
            )
        }
    }
}

@Composable
private fun QuickDirectiveItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(185.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
        border = BorderStroke(1.dp, Color(0xFF2C1C28))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = CyberRedBright,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.5.sp,
                color = CyberTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

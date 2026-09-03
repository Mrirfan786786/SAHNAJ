package com.example.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import com.example.ui.theme.CyberGreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.personality.PersonalityResponses
import com.example.permissions.PermissionManager
import com.example.presentation.components.SahNajBottomBar
import com.example.presentation.navigation.Screen
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.presentation.viewmodel.HistoryViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun ChatsScreen(
    assistantViewModel: AssistantViewModel,
    historyViewModel: HistoryViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToTriggers: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    val historyItems by historyViewModel.historyItems.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistantViewModel.startListening()
            onNavigateToAssistant()
        }
    }

    Scaffold(
        containerColor = CyberBlack,
        bottomBar = {
            SahNajBottomBar(
                currentRoute = Screen.Chat.route,
                onNavigate = { route ->
                    when (route) {
                        Screen.Dashboard.route -> onNavigateToHome()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberBlack)
        ) {
            // 1. Top Header Row: "Chats" and Action Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chats",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "API Keys",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 16.dp),
                                onClick = onNavigateToSettings
                            )
                    )
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Web Network",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Community",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Chat Threads List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Primary AI Assistant Thread
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onNavigateToAssistant
                            )
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Futuristic AI Avatar with Cosmic Glow
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFF2A4D),
                                            Color(0xFF8A1836),
                                            Color(0xFF26050C)
                                        )
                                    )
                                )
                                .border(1.5.dp, CyberRedBright.copy(alpha = 0.8f), CircleShape)
                        ) {
                            Text(
                                text = "SN",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = PersonalityResponses.ASSISTANT_NAME_DISPLAY,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CyberRed)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "AI CORE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            val activeModel by assistantViewModel.aiModel.collectAsState()
                            val activeModelLabel = remember(activeModel) {
                                when {
                                    activeModel.contains("pro", ignoreCase = true) -> "Gemini Pro"
                                    activeModel.contains("flash", ignoreCase = true) || activeModel.isBlank() -> "Gemini Flash"
                                    else -> {
                                        com.example.data.model.AiProvidersConfig.getProviderById(activeModel)?.name ?: activeModel
                                    }
                                }
                            }
                            Text(
                                text = "Active Engine: $activeModelLabel • Tap to open console",
                                fontSize = 12.5.sp,
                                color = CyberGreen
                            )
                        }
                    }
                }

                // Community Channel 1
                item {
                    CommunityChatItem(
                        title = "${PersonalityResponses.ASSISTANT_NAME_DISPLAY} Community",
                        time = "9:27 am",
                        subtitle = "Automation directives & discussions",
                        avatarColor = Color(0xFF57121C),
                        onClick = onNavigateToAssistant
                    )
                }

                // Community Channel 2
                item {
                    CommunityChatItem(
                        title = "${PersonalityResponses.ASSISTANT_NAME_DISPLAY} Dev Core",
                        time = "9:27 am",
                        subtitle = "Autonomous routines & system triggers",
                        avatarColor = Color(0xFF252033),
                        onClick = onNavigateToAssistant
                    )
                }

                // Dynamic Recent Threads
                items(historyItems.take(5)) { historyItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onNavigateToAssistant
                            )
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1724))
                                .border(1.dp, Color(0xFF2D2336), CircleShape)
                        ) {
                            Text(
                                text = historyItem.commandText.take(1).uppercase().ifEmpty { "C" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = historyItem.commandText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val cleanResponse = remember(historyItem.spokenResponse) {
                                com.example.domain.personality.SmartHumanEngine.sanitizeResponse(
                                    historyItem.spokenResponse,
                                    historyItem.commandText
                                )
                            }
                            Text(
                                text = cleanResponse.ifBlank { "Executed command successfully" },
                                fontSize = 12.5.sp,
                                color = CyberTextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityChatItem(
    title: String,
    time: String,
    subtitle: String,
    avatarColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(avatarColor)
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Community",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
                Text(
                    text = time,
                    fontSize = 11.5.sp,
                    color = CyberTextMuted
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = CyberTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

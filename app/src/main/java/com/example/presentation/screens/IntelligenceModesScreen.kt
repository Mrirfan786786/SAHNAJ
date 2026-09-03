package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ResponseLatencyOptimization
import com.example.data.model.SahnajOperatingMode
import com.example.presentation.viewmodel.IntelligenceModesViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCyan
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntelligenceModesScreen(
    viewModel: IntelligenceModesViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentMode by viewModel.operatingMode.collectAsState()
    val screenReaderEnabled by viewModel.screenContentReaderEnabled.collectAsState()
    val autoSummarizeEnabled by viewModel.autoSummarizeLongTextsEnabled.collectAsState()
    val contextMemoryEnabled by viewModel.contextMemoryEnabled.collectAsState()
    val latencyOptimization by viewModel.responseLatencyOptimization.collectAsState()
    val isReadingDemo by viewModel.isReadingDemo.collectAsState()
    val demoReadingText by viewModel.demoReadingText.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var showClearMemoryDialog by remember { mutableStateOf(false) }
    var sliderPosition by remember(latencyOptimization) {
        mutableFloatStateOf(latencyOptimization.level.toFloat())
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Intelligence & Modes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "इंटेलिजेंस व ऑपरेटिंग मोड्स",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopSpeaking()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberRedContainer)
                            .border(1.dp, CyberRedBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentMode.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyberRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBlack
                )
            )
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Top Hero Banner with Active Intelligence Overview
            HeroActiveModeBanner(
                currentMode = currentMode,
                latencyOptimization = latencyOptimization
            )

            // ==========================================
            // 1. OPERATING MODES SECTION
            // ==========================================
            SectionHeader(
                title = "Operating Modes",
                subtitle = "ऑपरेटिंग मोड्स • Choose the primary intelligence personality & execution pipeline",
                badge = "SINGLE CHOICE"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SahnajOperatingMode.entries.forEach { mode ->
                    val isSelected = (mode == currentMode)
                    OperatingModeCard(
                        mode = mode,
                        isSelected = isSelected,
                        onSelect = { viewModel.setOperatingMode(mode) }
                    )
                }
            }

            // ==========================================
            // 2. SMART READING ENGINE SECTION
            // ==========================================
            SectionHeader(
                title = "Smart Reading Engine",
                subtitle = "स्मार्ट रीडिंग इंजन • Read screen content, chat bubbles, and documents aloud",
                badge = "NEURAL TTS"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Toggle 1: Screen Content Reader
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1728))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Screen Content Reader",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "स्क्रीन कंटेंट रीडर • Floating trigger to read aloud currently displayed text & chats",
                                    fontSize = 11.sp,
                                    color = CyberTextMuted
                                )
                            }
                        }
                        Switch(
                            checked = screenReaderEnabled,
                            onCheckedChange = { viewModel.toggleScreenContentReader(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF262626)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // Toggle 2: Auto-Summarize Long Texts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1728))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Auto-Summarize Long Texts",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "ऑटो समराइज़ • Summarize lengthy articles or long chats before reading aloud",
                                    fontSize = 11.sp,
                                    color = CyberTextMuted
                                )
                            }
                        }
                        Switch(
                            checked = autoSummarizeEnabled,
                            onCheckedChange = { viewModel.toggleAutoSummarizeLongTexts(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF262626)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // Demo & Test Controls
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF141118))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Interactive Reader Test",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextSecondary
                            )

                            if (isReadingDemo) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = "Playing",
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Reading Aloud...",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberRedBright
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (isReadingDemo) {
                                        viewModel.stopSpeaking()
                                    } else {
                                        viewModel.testScreenReading(context)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isReadingDemo) CyberRedContainer else Color(0xFF241C2C)
                                ),
                                border = BorderStroke(1.dp, if (isReadingDemo) CyberRedBright else CyberRedBorder),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isReadingDemo) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isReadingDemo) CyberRedBright else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isReadingDemo) "Stop Audio" else "Read Screen",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReadingDemo) CyberRedBright else Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    if (isReadingDemo) {
                                        viewModel.stopSpeaking()
                                    } else {
                                        viewModel.testSummarizeAndRead()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CyberAmber
                                ),
                                border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyberAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Demo Summary",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberAmber
                                )
                            }
                        }

                        // Demo Text Box
                        AnimatedVisibility(visible = demoReadingText != null) {
                            demoReadingText?.let { text ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0F0F12))
                                        .border(1.dp, Color(0xFF33223E), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = text,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberTextSecondary,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. MEMORY & AUTOMATION INTELLIGENCE SECTION
            // ==========================================
            SectionHeader(
                title = "Memory & Automation Intelligence",
                subtitle = "मेमोरी व ऑटोमेशन • Continuous context learning & reasoning latency control",
                badge = "COGNITIVE ENGINE"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Toggle: Context Memory
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E1728))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = CyberGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Context Memory",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "कॉन्टेक्स्ट मेमोरी • Allow SAHNAJ to remember user facts & preferences across sessions",
                                    fontSize = 11.sp,
                                    color = CyberTextMuted
                                )
                            }
                        }
                        Switch(
                            checked = contextMemoryEnabled,
                            onCheckedChange = { viewModel.toggleContextMemory(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF262626)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // Slider: Response Speed / Latency Optimization
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Latency & Reasoning Optimization",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberRedContainer)
                                    .border(1.dp, CyberRedBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = latencyOptimization.targetLatency,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberRedBright
                                )
                            }
                        }

                        Text(
                            text = "रिस्पांस स्पीड • Balance between fastest token streaming vs deep technical cognition",
                            fontSize = 11.sp,
                            color = CyberTextMuted
                        )

                        // 3 Option Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ResponseLatencyOptimization.entries.forEach { opt ->
                                val isSelected = (opt == latencyOptimization)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        sliderPosition = opt.level.toFloat()
                                        viewModel.setResponseLatencyOptimization(opt)
                                    },
                                    label = {
                                        Text(
                                            text = "${opt.badge} ${opt.title}",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberRedBright,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF201A26),
                                        labelColor = CyberTextSecondary
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CyberRedBright else Color(0xFF33263E)
                                    )
                                )
                            }
                        }

                        // Slider
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                                val rounded = it.toInt().coerceIn(0, 2)
                                val opt = ResponseLatencyOptimization.fromLevel(rounded)
                                if (opt != latencyOptimization) {
                                    viewModel.setResponseLatencyOptimization(opt)
                                }
                            },
                            valueRange = 0f..2f,
                            steps = 1,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberRedBright,
                                activeTrackColor = CyberRedBright,
                                inactiveTrackColor = Color(0xFF33263E)
                            )
                        )

                        // Latency Description Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF141118))
                                .border(1.dp, CyberRedBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "${latencyOptimization.badge} ${latencyOptimization.title}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )
                                    Text(
                                        text = "Target: ${latencyOptimization.targetLatency}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberCyan
                                    )
                                }
                                Text(
                                    text = latencyOptimization.description,
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    // Wipe Memory Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Clear Saved Context Memory",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Remove all learned user facts and conversation history from local database",
                                fontSize = 11.sp,
                                color = CyberTextMuted
                            )
                        }

                        OutlinedButton(
                            onClick = { showClearMemoryDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CyberRedBright
                            ),
                            border = BorderStroke(1.dp, CyberRedBorder),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 4. AUTOMOTIVE & SECURITY INTEGRATIONS
            // ==========================================
            SectionHeader(
                title = "Automotive & Security Modules",
                subtitle = "ऑटोमोटिव व सुरक्षा मॉड्यूल्स • Diagnostics, SOS broadcast, and hands-free call bridge",
                badge = "JARVIS RADAR"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1728))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Automotive & DTC Engine",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "OBD-II trouble codes, engine misfire diagnostics & camera part scanning active",
                                fontSize = 11.sp,
                                color = CyberTextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2A2A2A))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E1728))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Emergency SOS & Call Announcer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Voice incoming caller announcement, decoy shutdown & emergency location dispatch",
                                fontSize = 11.sp,
                                color = CyberTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Confirmation Alert Dialog for Clearing Memory
    if (showClearMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearMemoryDialog = false },
            title = {
                Text(
                    text = "Clear Context Memory?",
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will erase all learned personal facts, extracted memory snippets, and recent conversation summaries from your device. This action cannot be undone.",
                    fontSize = 13.sp,
                    color = CyberTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearMemory {
                            showClearMemoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedDark),
                    border = BorderStroke(1.dp, CyberRedBright),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Erase All", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearMemoryDialog = false }) {
                    Text("Cancel", color = CyberTextMuted)
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun HeroActiveModeBanner(
    currentMode: SahnajOperatingMode,
    latencyOptimization: ResponseLatencyOptimization
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1222)),
        border = BorderStroke(1.5.dp, CyberRedBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF26142E), Color(0xFF130E1C))
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(CyberGreen)
                        )
                        Text(
                            text = "ACTIVE INTELLIGENCE PROTOCOL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyberGreen
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2A1B36))
                            .border(1.dp, CyberRedBright.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = latencyOptimization.targetLatency,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(CyberRedDark, CyberRedBright)
                                )
                            )
                    ) {
                        Icon(
                            imageVector = getModeIcon(currentMode),
                            contentDescription = currentMode.title,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentMode.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentMode.hindiSubtitle,
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                }

                Text(
                    text = currentMode.description,
                    fontSize = 12.sp,
                    color = CyberTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun OperatingModeCard(
    mode: SahnajOperatingMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) CyberRedBright else CyberRedBorder,
        animationSpec = tween(250),
        label = "modeBorderColor"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF241424) else CyberCard,
        animationSpec = tween(250),
        label = "modeContainerColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onSelect
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Icon Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CyberRedDark else Color(0xFF201826))
                    .border(
                        1.dp,
                        if (isSelected) CyberRedBright else Color(0xFF33223E),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = getModeIcon(mode),
                    contentDescription = mode.title,
                    tint = if (isSelected) Color.White else CyberRedBright,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else CyberTextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) CyberRedContainer else Color(0xFF1E1E24))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = mode.badge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) CyberRedBright else CyberTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = mode.subtitle,
                    fontSize = 12.sp,
                    color = CyberTextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Capabilities Pills (TTS Status, Cloud Status)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CapabilityPill(
                        label = if (mode.ttsEnabled) "Voice TTS Active" else "Voice Muted (Text-Only)",
                        icon = if (mode.ttsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        color = if (mode.ttsEnabled) CyberGreen else CyberAmber
                    )

                    CapabilityPill(
                        label = if (mode.cloudAllowed) "Cloud Hybrid" else "100% Local Offline",
                        icon = if (mode.cloudAllowed) Icons.Default.Cloud else Icons.Default.CloudOff,
                        color = if (mode.cloudAllowed) CyberCyan else CyberRedBright
                    )
                }
            }

            // Radio Indicator / Checkmark
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CyberRedBright else Color(0xFF26202E))
                    .border(
                        1.5.dp,
                        if (isSelected) CyberRedBright else Color(0xFF4A3E54),
                        CircleShape
                    )
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CapabilityPill(
    label: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    badge: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = CyberRedBright,
                letterSpacing = 1.sp
            )

            badge?.let {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberRedContainer)
                        .border(0.8.dp, CyberRedBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = it,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberRedBright
                    )
                }
            }
        }

        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = CyberTextMuted
        )
    }
}

private fun getModeIcon(mode: SahnajOperatingMode): ImageVector {
    return when (mode) {
        SahnajOperatingMode.JARVIS -> Icons.Default.Psychology
        SahnajOperatingMode.TECHNICIAN -> Icons.Default.Build
        SahnajOperatingMode.STEALTH -> Icons.Default.VisibilityOff
        SahnajOperatingMode.OFFLINE_CORE -> Icons.Default.CloudOff
    }
}

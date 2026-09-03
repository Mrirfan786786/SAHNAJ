package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SahNajApplication
import com.example.data.model.VoiceProfileItem
import com.example.data.model.VoiceStudioCatalog
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
import com.example.ui.theme.CyberTextSecondary
import java.util.Locale

enum class VoiceFilterCategory(val label: String) {
    ALL("All (50)"),
    FEMALE("Female (25)"),
    MALE("Male (25)"),
    REGIONAL("Hindi & Urdu"),
    GLOBAL("Global English")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VoiceAndAiModelsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as SahNajApplication }
    val userPreferences = remember { app.userPreferences }
    val dualVoiceEngine = remember { app.dualVoiceEngine }

    val activeVoiceId by dualVoiceEngine.activeVoiceId.collectAsState()
    val playingVoiceId by dualVoiceEngine.currentlyPlayingVoiceId.collectAsState()
    val isPlayingPreview by dualVoiceEngine.isPlayingPreview.collectAsState()

    val savedSpeechRate by userPreferences.speechRate.collectAsState()
    val savedSpeechPitch by userPreferences.speechPitch.collectAsState()
    val savedAiModel by userPreferences.aiModel.collectAsState()

    var speechRate by remember(savedSpeechRate) { mutableFloatStateOf(savedSpeechRate) }
    var speechPitch by remember(savedSpeechPitch) { mutableFloatStateOf(savedSpeechPitch) }
    var selectedModel by remember(savedAiModel) { mutableStateOf(savedAiModel) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(VoiceFilterCategory.ALL) }

    val activeProfile = remember(activeVoiceId) {
        VoiceStudioCatalog.findVoiceById(activeVoiceId)
    }

    val isOnlineAvailable = remember { dualVoiceEngine.isOnlineNeuralAvailable() }

    // Filter voices based on search and selected category
    val filteredVoices = remember(searchQuery, selectedFilter) {
        val query = searchQuery.trim().lowercase()
        VoiceStudioCatalog.ALL_50_VOICES.filter { voice ->
            val matchesCategory = when (selectedFilter) {
                VoiceFilterCategory.ALL -> true
                VoiceFilterCategory.FEMALE -> voice.gender.equals("Female", ignoreCase = true)
                VoiceFilterCategory.MALE -> voice.gender.equals("Male", ignoreCase = true)
                VoiceFilterCategory.REGIONAL -> voice.category.contains("Regional", ignoreCase = true)
                VoiceFilterCategory.GLOBAL -> voice.category.contains("Global", ignoreCase = true)
            }
            val matchesSearch = query.isEmpty() ||
                voice.name.lowercase().contains(query) ||
                voice.tagline.lowercase().contains(query) ||
                voice.languageOptimization.lowercase().contains(query) ||
                voice.gender.lowercase().contains(query)

            matchesCategory && matchesSearch
        }
    }

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "50-Voice Studio Engine",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "25 Female + 25 Male Neural Voices",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        dualVoiceEngine.stopPlayback()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= SECTION 1: ACTIVE VOICE STATUS BANNER =================
            ActiveVoiceHeroBanner(
                activeProfile = activeProfile,
                isOnlineAvailable = isOnlineAvailable,
                isPlaying = isPlayingPreview && playingVoiceId == activeProfile.id,
                onTestActiveVoice = {
                    dualVoiceEngine.previewVoice(activeProfile)
                }
            )

            // ================= SECTION 2: SEARCH & GENDER MATRIX FILTERS =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search voice by name, tone, language...", fontSize = 13.sp, color = CyberTextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberRedBright, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CyberTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = Color(0xFF332038),
                            focusedContainerColor = Color(0xFF130E18),
                            unfocusedContainerColor = Color(0xFF130E18),
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        )
                    )

                    // Gender & Category Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VoiceFilterCategory.entries.forEach { category ->
                            val isSelected = selectedFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = category },
                                label = {
                                    Text(
                                        text = category.label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (category) {
                                        VoiceFilterCategory.FEMALE -> Color(0xFFFF2D55)
                                        VoiceFilterCategory.MALE -> Color(0xFF00C6FF)
                                        else -> CyberRedBright
                                    },
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF201828),
                                    labelColor = CyberTextSecondary
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color.Transparent else Color(0xFF382644)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // ================= SECTION 3: 50 VOICE MATRIX CATALOG =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "VOICE MATRIX (${filteredVoices.size})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }

                Text(
                    text = "Tap Play to Preview • Tap Card to Select",
                    fontSize = 10.5.sp,
                    color = CyberTextMuted
                )
            }

            // Voice Items List
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredVoices.forEach { voice ->
                    val isSelected = activeVoiceId.equals(voice.id, ignoreCase = true) ||
                        activeVoiceId.equals(voice.name, ignoreCase = true)
                    val isCurrentlyPlaying = isPlayingPreview && playingVoiceId == voice.id

                    VoiceMatrixCard(
                        voice = voice,
                        isSelected = isSelected,
                        isPlaying = isCurrentlyPlaying,
                        onSelect = {
                            dualVoiceEngine.selectActiveVoice(voice.id)
                        },
                        onTogglePreview = {
                            if (isCurrentlyPlaying) {
                                dualVoiceEngine.stopPlayback()
                            } else {
                                dualVoiceEngine.previewVoice(voice)
                            }
                        }
                    )
                }

                if (filteredVoices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No voices match \"$searchQuery\"",
                            fontSize = 13.sp,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ================= SECTION 4: ACOUSTIC FINE-TUNING SLIDERS =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "ACOUSTIC FINE-TUNING",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )

                    // 1. Speech Rate Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Speech Rate (बोलने की गति)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberTextPrimary
                                )
                            }
                            Text(
                                text = "${String.format(Locale.US, "%.2f", speechRate)}x",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }

                        Slider(
                            value = speechRate,
                            onValueChange = { newValue ->
                                speechRate = newValue
                                userPreferences.setSpeechRate(newValue)
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberRedBright,
                                activeTrackColor = CyberRed,
                                inactiveTrackColor = CyberSurface
                            )
                        )
                    }

                    // 2. Pitch Slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Pitch (आवाज़ का सुर / टोन)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberTextPrimary
                                )
                            }
                            Text(
                                text = String.format(Locale.US, "%.2f", speechPitch),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }

                        Slider(
                            value = speechPitch,
                            onValueChange = { newValue ->
                                speechPitch = newValue
                                userPreferences.setSpeechPitch(newValue)
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberRedBright,
                                activeTrackColor = CyberRed,
                                inactiveTrackColor = CyberSurface
                            )
                        )
                    }
                }
            }

            // ================= SECTION 5: AI REASONING MODELS =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF00C6FF),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "PRIMARY AI REASONING ENGINE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00C6FF)
                        )
                    }

                    listOf(
                        Triple("gemini-2.5-flash", "Gemini 2.5 Flash (Recommended)", "Ultra-low latency reasoning with instant voice streaming"),
                        Triple("gemini-1.5-pro", "Gemini 1.5 Pro (Deep Reasoning)", "High-intelligence deep analysis for complex commands"),
                        Triple("smart-human-offline", "Smart Human Engine (Offline / Local)", "Autonomous zero-network fallback with high emotional warmth")
                    ).forEach { (id, title, desc) ->
                        val isChosen = selectedModel == id || (id == "gemini-2.5-flash" && selectedModel.contains("flash"))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isChosen) Color(0xFF1E162B) else Color(0xFF140F1D))
                                .border(
                                    1.dp,
                                    if (isChosen) CyberRedBright else Color(0xFF281C33),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    selectedModel = id
                                    userPreferences.setAiModel(id)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                                Text(desc, fontSize = 11.sp, color = CyberTextMuted)
                            }
                            if (isChosen) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = CyberRedBright, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActiveVoiceHeroBanner(
    activeProfile: VoiceProfileItem,
    isOnlineAvailable: Boolean,
    isPlaying: Boolean,
    onTestActiveVoice: () -> Unit
) {
    val isFemale = activeProfile.gender.equals("Female", ignoreCase = true)
    val genderColor = if (isFemale) Color(0xFFFF2D55) else Color(0xFF00C6FF)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF191122)),
        border = BorderStroke(1.5.dp, CyberRedBright)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(genderColor.copy(alpha = 0.2f))
                            .border(2.dp, genderColor, CircleShape)
                    ) {
                        Text(text = activeProfile.avatarEmoji, fontSize = 24.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = activeProfile.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberTextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(genderColor.copy(alpha = 0.25f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeProfile.gender.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = genderColor
                                )
                            }
                        }

                        Text(
                            text = activeProfile.tagline,
                            fontSize = 11.5.sp,
                            color = CyberTextSecondary
                        )
                    }
                }

                // Test Voice Button
                Button(
                    onClick = onTestActiveVoice,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedContainer),
                    border = BorderStroke(1.dp, CyberRedBright)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPlaying) "Stop" else "Test",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF2E1F3B))

            // Engine Mode & Language tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isOnlineAvailable) Icons.Default.Cloud else Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (isOnlineAvailable) CyberGreen else Color(0xFFFF9F0A),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (isOnlineAvailable) "ElevenLabs Neural v2 (Online)" else "High-Quality Android TTS (Offline)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOnlineAvailable) CyberGreen else Color(0xFFFF9F0A)
                    )
                }

                Text(
                    text = activeProfile.languageOptimization,
                    fontSize = 10.5.sp,
                    color = CyberTextMuted
                )
            }
        }
    }
}

@Composable
private fun VoiceMatrixCard(
    voice: VoiceProfileItem,
    isSelected: Boolean,
    isPlaying: Boolean,
    onSelect: () -> Unit,
    onTogglePreview: () -> Unit
) {
    val isFemale = voice.gender.equals("Female", ignoreCase = true)
    val genderColor = if (isFemale) Color(0xFFFF2D55) else Color(0xFF00C6FF)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onSelect
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF22162E) else Color(0xFF140F1B)
        ),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) CyberRedBright else Color(0xFF271A2F)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Emoji Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(genderColor.copy(alpha = 0.16f))
                    .border(1.dp, genderColor.copy(alpha = 0.6f), CircleShape)
            ) {
                Text(text = voice.avatarEmoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Voice Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = voice.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CyberRedBright else CyberTextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(genderColor.copy(alpha = 0.2f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = voice.gender.uppercase(),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = genderColor
                        )
                    }

                    if (voice.category.contains("Regional", ignoreCase = true)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF3B2516))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "HINDI/URDU",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9F0A)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = voice.tagline,
                    fontSize = 11.sp,
                    color = CyberTextSecondary,
                    maxLines = 1
                )

                Text(
                    text = "${voice.languageOptimization} • Pitch: ${voice.ttsPitch}x",
                    fontSize = 10.sp,
                    color = CyberTextMuted
                )
            }

            // Play / Preview Button
            IconButton(
                onClick = onTogglePreview,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) CyberRedBright else Color(0xFF251A31))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = "Preview Voice",
                    tint = if (isPlaying) Color.White else CyberRedBright,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Selection Check Indicator
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) CyberRedBright else Color(0xFF1E1527))
                    .border(1.dp, if (isSelected) CyberRedBright else Color(0xFF382645), CircleShape)
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

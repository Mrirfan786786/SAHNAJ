package com.example.presentation.components

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ElevenLabsVoice
import com.example.data.model.GeneratedVoiceItem
import com.example.data.model.VoiceStudioPresets
import com.example.data.model.VoiceStudioState
import com.example.presentation.viewmodel.VoiceStudioViewModel
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
import java.util.Locale
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceStudioView(
    viewModel: VoiceStudioViewModel,
    onNavigateToApiSettings: () -> Unit
) {
    val context = LocalContext.current
    val scriptText by viewModel.scriptText.collectAsState()
    val selectedVoice by viewModel.selectedVoice.collectAsState()
    val customVoiceId by viewModel.customVoiceId.collectAsState()
    val isCustomVoiceEnabled by viewModel.isCustomVoiceEnabled.collectAsState()
    val selectedModelId by viewModel.selectedModelId.collectAsState()
    val stability by viewModel.stability.collectAsState()
    val similarityBoost by viewModel.similarityBoost.collectAsState()
    val voiceStudioState by viewModel.voiceStudioState.collectAsState()
    val recentVoices by viewModel.recentVoices.collectAsState()

    val currentlyPlayingItem by viewModel.currentlyPlayingItem.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPositionMs by viewModel.playbackPositionMs.collectAsState()
    val playbackDurationMs by viewModel.playbackDurationMs.collectAsState()

    var showVoiceSettings by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // ================= STUDIO HEADER CARD =================
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
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "AI VOICE STUDIO & DUBBING",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberTextPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "POWERED BY ELEVENLABS NEURAL API",
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
                                text = "API KEY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Generate ultra-realistic human-like voiceovers in Hindi (हिन्दी), Urdu (اردو), Hinglish, and English with pitch-perfect emotion, clarity, and pacing.",
                    fontSize = 11.5.sp,
                    color = CyberTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // ================= SAMPLE SCRIPT PRESETS =================
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "QUICK SCRIPT PRESETS // तत्पर स्क्रिप्ट्स",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceStudioPresets.SAMPLE_SCRIPTS.forEach { sample ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CyberSurface,
                        border = BorderStroke(1.dp, CyberRedBorder),
                        modifier = Modifier.clickable {
                            viewModel.applySampleScript(sample)
                            Toast.makeText(context, "Loaded: ${sample.label}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = sample.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ================= TEXT / SCRIPT INPUT AREA =================
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberRedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VOICEOVER SCRIPT // संवाद स्क्रिप्ट",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary,
                        letterSpacing = 0.5.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = clipboard.primaryClip
                                if (clipData != null && clipData.itemCount > 0) {
                                    val pasted = clipData.getItemAt(0).text?.toString() ?: ""
                                    if (pasted.isNotBlank()) {
                                        viewModel.setScriptText(pasted)
                                        Toast.makeText(context, "Script pasted!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = CyberTextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (scriptText.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.setScriptText("") },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = CyberTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = scriptText,
                    onValueChange = { viewModel.setScriptText(it) },
                    placeholder = {
                        Text(
                            text = "Enter dialogue or script here in Hindi, Urdu, or English...\n\nउदाहरण: \"नमस्ते! मैं शहनाज़ एआई हूँ। आपके सभी वॉइस प्रोजेक्ट्स के लिए तैयार हूँ।\"",
                            color = CyberTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    },
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = CyberTextPrimary,
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack,
                        cursorColor = CyberRedBright,
                        focusedBorderColor = CyberRedBright,
                        unfocusedBorderColor = CyberRedBorder
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_studio_script_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Characters: ${scriptText.length} • Words: ${if (scriptText.isBlank()) 0 else scriptText.trim().split(Regex("\\s+")).size}",
                        fontSize = 10.sp,
                        color = CyberTextMuted
                    )

                    Text(
                        text = "Multilingual v2 Compatible",
                        fontSize = 10.sp,
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ================= VOICE SELECTOR CAROUSEL =================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT ELEVENLABS VOICE // आवाज़ चुनें",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isCustomVoiceEnabled) CyberRedDark else CyberSurface,
                    border = BorderStroke(1.dp, if (isCustomVoiceEnabled) CyberRedBright else CyberRedBorder),
                    modifier = Modifier.clickable {
                        viewModel.setCustomVoiceEnabled(!isCustomVoiceEnabled)
                    }
                ) {
                    Text(
                        text = if (isCustomVoiceEnabled) "CUSTOM ID ACTIVE ⚡" else "+ CUSTOM CLONE ID",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCustomVoiceEnabled) Color.White else CyberRedBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isCustomVoiceEnabled) {
                OutlinedTextField(
                    value = customVoiceId,
                    onValueChange = { viewModel.setCustomVoiceId(it) },
                    label = { Text("Custom ElevenLabs Voice ID", fontSize = 11.sp, color = CyberRedBright) },
                    placeholder = { Text("e.g., 21m00Tcm4TlvDq8ikWAM or cloned ID", fontSize = 11.sp, color = CyberTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = CyberTextPrimary,
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack,
                        cursorColor = CyberRedBright,
                        focusedBorderColor = CyberRedBright,
                        unfocusedBorderColor = CyberRedBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceStudioPresets.PRELOADED_VOICES.forEach { voice ->
                        val isSelected = selectedVoice.id == voice.id && !isCustomVoiceEnabled

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CyberRedDark else CyberCard,
                            border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                            modifier = Modifier
                                .width(135.dp)
                                .clickable { viewModel.selectVoice(voice) }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = voice.avatarEmoji,
                                    fontSize = 24.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = voice.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else CyberTextPrimary
                                )

                                Text(
                                    text = voice.previewTrait,
                                    fontSize = 9.sp,
                                    color = if (isSelected) CyberRedBright else CyberTextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0x33000000)
                                ) {
                                    Text(
                                        text = "${voice.gender} • ${voice.category}",
                                        fontSize = 8.5.sp,
                                        color = CyberTextSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ================= MODEL SELECTOR =================
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "NEURAL MODEL // मॉडल चुनें",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceStudioPresets.PRELOADED_MODELS.forEach { model ->
                    val isSelected = selectedModelId == model.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyberRedDark else CyberCard,
                        border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectModel(model.id) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = model.name.split(" ").first(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CyberTextPrimary
                            )
                            Text(
                                text = model.badge,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CyberRedBright else CyberTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // ================= ADVANCED VOICE TUNING EXPANDABLE =================
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberRedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showVoiceSettings = !showVoiceSettings },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "VOICE STABILITY & CLARITY SETTINGS",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    Text(
                        text = if (showVoiceSettings) "HIDE ▲" else "FINE-TUNE ▼",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }

                AnimatedVisibility(visible = showVoiceSettings) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Stability Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Voice Stability (स्थिरता):",
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f", stability),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright
                                )
                            }
                            Slider(
                                value = stability,
                                onValueChange = { viewModel.setStability(it) },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberRedBright,
                                    activeTrackColor = CyberRedBright,
                                    inactiveTrackColor = CyberSurface
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Variable / Expressive", fontSize = 9.sp, color = CyberTextMuted)
                                Text("Consistent / Steady", fontSize = 9.sp, color = CyberTextMuted)
                            }
                        }

                        // Similarity / Clarity Boost Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Clarity & Similarity Boost (स्पष्टता):",
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f", similarityBoost),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright
                                )
                            }
                            Slider(
                                value = similarityBoost,
                                onValueChange = { viewModel.setSimilarityBoost(it) },
                                valueRange = 0.0f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberCyan,
                                    activeTrackColor = CyberCyan,
                                    inactiveTrackColor = CyberSurface
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Natural Softness", fontSize = 9.sp, color = CyberTextMuted)
                                Text("High Clarity / Crisp", fontSize = 9.sp, color = CyberTextMuted)
                            }
                        }
                    }
                }
            }
        }

        // ================= GENERATE ACTION BUTTON =================
        val isGenerating = voiceStudioState is VoiceStudioState.Generating
        Button(
            onClick = { viewModel.generateVoice() },
            enabled = !isGenerating && scriptText.isNotBlank(),
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
                .testTag("generate_voice_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "SYNTHESIZING DUBBED AUDIO...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "⚡ GENERATE DUBBED VOICE",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // ================= DYNAMIC LOADING / PROGRESS STATE =================
        AnimatedVisibility(
            visible = isGenerating,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val genState = voiceStudioState as? VoiceStudioState.Generating
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
                        text = genState?.stage ?: "Processing ElevenLabs synthesis...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Wave animation
                    DynamicAudioWaveAnimation(isPlaying = true)

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { genState?.progress ?: 0.5f },
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
        if (voiceStudioState is VoiceStudioState.Error) {
            val errorMsg = (voiceStudioState as VoiceStudioState.Error).message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0D14)),
                border = BorderStroke(1.dp, CyberRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SYNTHESIS FAILED // एरर",
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
                            Text("CONFIGURE ELEVENLABS KEY IN SETTINGS", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // ================= REAL-TIME AUDIO PLAYER CARD =================
        val activeItem = currentlyPlayingItem ?: (voiceStudioState as? VoiceStudioState.Success)?.item
        if (activeItem != null) {
            AudioPlayerCard(
                item = activeItem,
                isPlaying = isPlaying && (currentlyPlayingItem?.id == activeItem.id),
                playbackPositionMs = if (currentlyPlayingItem?.id == activeItem.id) playbackPositionMs else 0,
                playbackDurationMs = if (currentlyPlayingItem?.id == activeItem.id) playbackDurationMs else (activeItem.durationMs.toInt().coerceAtLeast(1)),
                onTogglePlay = { viewModel.togglePlayPause(activeItem) },
                onSeek = { viewModel.seekTo(it) },
                onReplay = { viewModel.replayVoice() },
                onDownload = { viewModel.downloadMp3(activeItem, context) },
                onShare = { viewModel.shareVoice(activeItem, context) }
            )
        }

        // ================= RECENT DUBBED CLIPS STRIP =================
        if (recentVoices.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RECENT VOICEOVER CLIPS (${recentVoices.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentVoices.forEach { clip ->
                        val isThisPlaying = isPlaying && (currentlyPlayingItem?.id == clip.id)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberCard,
                            border = BorderStroke(1.dp, if (isThisPlaying) CyberRedBright else CyberRedBorder),
                            modifier = Modifier.fillMaxWidth()
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
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isThisPlaying) CyberRedBright else CyberRedDark)
                                            .clickable { viewModel.togglePlayPause(clip) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "${clip.voiceName} • ${clip.modelId.replace("eleven_", "")}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextPrimary
                                        )
                                        Text(
                                            text = clip.text,
                                            fontSize = 10.5.sp,
                                            color = CyberTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { viewModel.downloadMp3(clip, context) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = "Download",
                                            tint = CyberRedBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.shareVoice(clip, context) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = CyberTextMuted,
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
}

// ================= CUSTOM STYLED AUDIO PLAYER COMPOSABLE =================
@Composable
fun AudioPlayerCard(
    item: GeneratedVoiceItem,
    isPlaying: Boolean,
    playbackPositionMs: Int,
    playbackDurationMs: Int,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit,
    onReplay: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, CyberRedBright),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Info
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(CyberRedDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "VOICEOVER READY // ${item.voiceName.uppercase()}",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberRedBright
                        )
                        Text(
                            text = "Model: ${item.modelId} • MP3 44.1kHz",
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
                        text = "SYNTHESIZED ⚡",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Script Quote Preview
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyberBlack,
                border = BorderStroke(1.dp, CyberSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${item.text}\"",
                    fontSize = 11.5.sp,
                    color = CyberTextSecondary,
                    modifier = Modifier.padding(10.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Audio Waveform
            DynamicAudioWaveAnimation(isPlaying = isPlaying)

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Slider
            Slider(
                value = playbackPositionMs.toFloat().coerceIn(0f, playbackDurationMs.toFloat().coerceAtLeast(1f)),
                onValueChange = { onSeek(it.toInt()) },
                valueRange = 0f..playbackDurationMs.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = CyberRedBright,
                    activeTrackColor = CyberRedBright,
                    inactiveTrackColor = CyberSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Timestamps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(playbackPositionMs),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = CyberRedBright
                )
                Text(
                    text = formatMs(playbackDurationMs),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberTextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Controls & Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause / Replay Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(CyberRedBright, CyberRedDark)
                                )
                            )
                            .border(BorderStroke(1.5.dp, CyberRedBright), CircleShape)
                            .clickable { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay",
                            tint = CyberTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 1-Click Download MP3 & Share
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onDownload,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRedDark),
                        border = BorderStroke(1.dp, CyberRedBright),
                        modifier = Modifier.testTag("download_mp3_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "DOWNLOAD MP3",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberSurface)
                            .border(BorderStroke(1.dp, CyberRedBorder), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = CyberTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ================= DYNAMIC AUDIO WAVE ANIMATION =================
@Composable
fun DynamicAudioWaveAnimation(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(CyberBlack)
            .border(BorderStroke(1.dp, CyberSurface), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val barCount = 28
        val spacing = size.width / barCount
        val barWidth = spacing * 0.55f
        val centerY = size.height / 2f

        for (i in 0 until barCount) {
            val normalizedX = i.toFloat() / barCount
            val waveHeightFactor = if (isPlaying) {
                (0.3f + 0.7f * ((sin(normalizedX * 12.0f + phase) + 1f) / 2f))
            } else {
                0.25f + 0.1f * sin(normalizedX * 6.0f)
            }

            val barHeight = (size.height * 0.85f * waveHeightFactor).coerceAtLeast(4f)
            val left = i * spacing + (spacing - barWidth) / 2f
            val top = centerY - barHeight / 2f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        CyberRedBright,
                        CyberRed,
                        CyberRedDark
                    )
                ),
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

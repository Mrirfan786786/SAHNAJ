package com.example.presentation.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GeneratedPromptResult
import com.example.data.model.PromptAspectRatio
import com.example.data.model.PromptStudioState
import com.example.data.model.PromptStyle
import com.example.presentation.viewmodel.PromptStudioViewModel
import com.example.ui.theme.CyberAmber
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PromptStudioView(
    viewModel: PromptStudioViewModel,
    onNavigateToApiSettings: () -> Unit,
    onDirectGenerateImage: (prompt: String, aspectRatio: String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val subject by viewModel.subject.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedAspectRatio by viewModel.selectedAspectRatio.collectAsState()
    val promptStudioState by viewModel.promptStudioState.collectAsState()
    val recentPrompts by viewModel.recentPrompts.collectAsState()

    val isGenerating = promptStudioState is PromptStudioState.Generating

    val presetIdeas = remember {
        listOf(
            Triple("Tech YouTuber with neon setup and floating holographic stats", PromptStyle.YOUTUBE_THUMBNAIL, PromptAspectRatio.AR_16_9),
            Triple("3D Animated cute fruit character with expressive eyes holding a glowing orb", PromptStyle.PIXAR_3D, PromptAspectRatio.AR_1_1),
            Triple("Luxury supercar repair master in cyber mechanic garage with sparks", PromptStyle.UNREAL_8K, PromptAspectRatio.AR_16_9),
            Triple("Futuristic AI Assistant mascot female cyborg with neon red visor", PromptStyle.CYBERPUNK_NEON, PromptAspectRatio.AR_9_16),
            Triple("Minimalist geometric cyber skull gaming brand logo", PromptStyle.MINIMALIST_LOGO, PromptAspectRatio.AR_1_1),
            Triple("Extreme reaction fitness athlete breaking workout record", PromptStyle.YOUTUBE_THUMBNAIL, PromptAspectRatio.AR_16_9)
        )
    }

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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "3D AVATAR & THUMBNAIL STUDIO",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberTextPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "FLUX.1 • MIDJOURNEY V6 • SD3 PROMPT CORE",
                                fontSize = 9.5.sp,
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
                                text = "AI KEY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Craft high-CTR YouTube thumbnails, 3D Pixar avatars, and photorealistic 8K visuals with precision lighting, camera lenses, and auto-negative prompt tokens.",
                    fontSize = 11.5.sp,
                    color = CyberTextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // ================= TOPIC / SUBJECT INPUT FIELD =================
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
                        text = "TOPIC / SUBJECT // विषय:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextMuted,
                        letterSpacing = 0.5.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    viewModel.setSubject(clip)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = CyberCyan,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (subject.isNotBlank()) {
                            IconButton(
                                onClick = { viewModel.setSubject("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { viewModel.setSubject(it) },
                    placeholder = {
                        Text(
                            text = "e.g., Tech YouTuber with neon setup, 3D Animated fruit character, Luxury car repair master...",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .testTag("prompt_studio_subject_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CyberBlack,
                        unfocusedContainerColor = CyberBlack,
                        focusedBorderColor = CyberRedBright,
                        unfocusedBorderColor = CyberRedBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = CyberTextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Inspiration Chips
                Text(
                    text = "QUICK PRESETS // तुरंत चुनें:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetIdeas.forEach { (presetSubject, presetStyle, presetRatio) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyberSurface,
                            border = BorderStroke(1.dp, CyberRedBorder),
                            modifier = Modifier.clickable {
                                viewModel.loadPreset(presetSubject, presetStyle, presetRatio)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(presetStyle.emoji, fontSize = 11.sp)
                                Text(
                                    text = presetSubject.take(24) + "...",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = CyberTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ================= STYLE SELECTOR =================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "VISUAL ART STYLE // स्टाइल चुनें",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PromptStyle.values().forEach { style ->
                    val isSelected = selectedStyle == style
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyberRedDark else CyberCard,
                        border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setStyle(style) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(style.emoji, fontSize = 22.sp)

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else CyberTextPrimary
                                )
                                Text(
                                    text = style.description,
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
                                        text = "SELECTED",
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

        // ================= ASPECT RATIO SELECTOR =================
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "ASPECT RATIO // आकार:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PromptAspectRatio.values().forEach { ratio ->
                    val isSelected = selectedAspectRatio == ratio
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyberRedDark else CyberCard,
                        border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setAspectRatio(ratio) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(ratio.emoji, fontSize = 14.sp)
                                Text(
                                    text = ratio.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else CyberTextPrimary
                                )
                            }
                            Text(
                                text = ratio.subtitle,
                                fontSize = 9.5.sp,
                                color = if (isSelected) CyberRedBright else CyberTextMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // ================= GENERATE BUTTON =================
        Button(
            onClick = { viewModel.generatePrompts() },
            enabled = !isGenerating && subject.isNotBlank(),
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
                .testTag("generate_master_prompts_button")
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
                        text = "SYNTHESIZING PROMPT TOKENS...",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "⚡ GENERATE MASTER PROMPTS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // ================= ERROR BANNER =================
        if (promptStudioState is PromptStudioState.Error) {
            val errorMsg = (promptStudioState as PromptStudioState.Error).message
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0D14)),
                border = BorderStroke(1.dp, CyberRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "PROMPT GENERATION NOTICE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberRedBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMsg,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        // ================= GENERATED PROMPT RESULT PACKAGE =================
        if (promptStudioState is PromptStudioState.Success) {
            val result = (promptStudioState as PromptStudioState.Success).result

            GeneratedPromptPackageCard(
                result = result,
                onCopy = { label, text -> viewModel.copyToClipboard(label, text, context) },
                onDirectGenerate = { prompt, ratio -> onDirectGenerateImage(prompt, ratio) },
                onShare = { sharePromptPackage(result, context) }
            )
        }

        // ================= RECENT PROMPTS HISTORY =================
        if (recentPrompts.isNotEmpty() && promptStudioState !is PromptStudioState.Success) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "SAVED PROMPT PACKAGES (${recentPrompts.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted,
                    letterSpacing = 0.5.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentPrompts.take(5).forEach { item ->
                        GeneratedPromptPackageCard(
                            result = item,
                            onCopy = { label, text -> viewModel.copyToClipboard(label, text, context) },
                            onDirectGenerate = { prompt, ratio -> onDirectGenerateImage(prompt, ratio) },
                            onShare = { sharePromptPackage(item, context) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ================= GENERATED PROMPT PACKAGE CARD =================
@Composable
fun GeneratedPromptPackageCard(
    result: GeneratedPromptResult,
    onCopy: (label: String, text: String) -> Unit,
    onDirectGenerate: (prompt: String, aspectRatio: String) -> Unit,
    onShare: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: FLUX.1, 1: Midjourney v6, 2: Stable Diffusion 3, 3: Negative Prompt

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
                    Text(result.style.emoji, fontSize = 20.sp)
                    Column {
                        Text(
                            text = result.subject.take(30) + if (result.subject.length > 30) "..." else "",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${result.style.title} • Ratio ${result.aspectRatio.title}",
                            fontSize = 10.sp,
                            color = CyberRedBright
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1B2A1E),
                    border = BorderStroke(1.dp, CyberGreen)
                ) {
                    Text(
                        text = "8K READY ⚡",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Engine Tabs: FLUX.1 | Midjourney v6 | Stable Diffusion 3 | Negative Prompt
            val tabs = listOf(
                Pair("FLUX.1", CyberRedBright),
                Pair("MIDJOURNEY", CyberCyan),
                Pair("SD3 / SDXL", CyberAmber),
                Pair("NEGATIVE", CyberTextMuted)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEachIndexed { index, (tabName, tabColor) ->
                    val isTabSelected = activeTab == index
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isTabSelected) CyberRedDark else CyberSurface,
                        border = BorderStroke(1.dp, if (isTabSelected) tabColor else CyberRedBorder),
                        modifier = Modifier.clickable { activeTab = index }
                    ) {
                        Text(
                            text = tabName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTabSelected) Color.White else CyberTextMuted,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Display Active Prompt Text
            val (currentLabel, currentText) = when (activeTab) {
                0 -> Pair("FLUX.1 Master Prompt", result.fluxPrompt)
                1 -> Pair("Midjourney v6.1 Prompt", result.midjourneyPrompt)
                2 -> Pair("Stable Diffusion 3 Prompt", result.sdPrompt)
                else -> Pair("Negative Prompt Tokens", result.negativePrompt)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyberBlack,
                border = BorderStroke(1.dp, CyberRedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentLabel.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberRedBright,
                            letterSpacing = 0.5.sp
                        )

                        Text(
                            text = "${currentText.length} chars",
                            fontSize = 9.sp,
                            color = CyberTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = currentText,
                        fontSize = 11.5.sp,
                        color = CyberTextPrimary,
                        lineHeight = 16.5.sp
                    )
                }
            }

            // Lighting & Lens Notes
            if (result.lightingAndCameraNotes.isNotBlank() && activeTab != 3) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF141923),
                    border = BorderStroke(1.dp, Color(0xFF22354D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = result.lightingAndCameraNotes,
                            fontSize = 10.sp,
                            color = CyberCyan,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Copy Prompt | Direct Generate | Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy Prompt Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberSurface,
                    border = BorderStroke(1.dp, CyberRedBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCopy(currentLabel, currentText) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = CyberRedBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "COPY PROMPT",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Direct Generate Image Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberRedBright,
                    border = BorderStroke(1.dp, CyberRedBright),
                    modifier = Modifier
                        .weight(1.3f)
                        .clickable {
                            val promptToSend = if (activeTab == 3) result.fluxPrompt else currentText
                            onDirectGenerate(promptToSend, result.aspectRatio.ratioValue)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Direct Generate",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡ DIRECT GENERATE",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Share Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F2618),
                    border = BorderStroke(1.dp, CyberGreen),
                    modifier = Modifier
                        .weight(0.7f)
                        .clickable { onShare() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = CyberGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SHARE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    }
                }
            }
        }
    }
}

private fun sharePromptPackage(result: GeneratedPromptResult, context: Context) {
    try {
        val shareText = """
            🎨 *SAHNAJ AI - 3D Avatar & Thumbnail Prompt Package*
            
            *Subject:* ${result.subject}
            *Style:* ${result.style.title} (${result.aspectRatio.title})
            
            🚀 *FLUX.1 Prompt:*
            ${result.fluxPrompt}
            
            🎨 *Midjourney v6.1 Prompt:*
            ${result.midjourneyPrompt}
            
            ⚡ *Stable Diffusion 3 Prompt:*
            ${result.sdPrompt}
            
            🛡️ *Negative Prompt:*
            ${result.negativePrompt}
            
            💡 *Lighting & Camera Direction:*
            ${result.lightingAndCameraNotes}
            
            _Crafted by SAHNAJ AI Prompt Studio_
        """.trimIndent()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "SAHNAJ AI Master Prompt: ${result.subject}")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Master Prompt Package"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

package com.example.presentation.components.media

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.AiProvidersConfig
import com.example.data.model.GeneratedImageItem
import com.example.data.model.GenerationState
import com.example.presentation.viewmodel.MediaGenerationViewModel
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

@Composable
fun ImageGeneratorView(
    viewModel: MediaGenerationViewModel,
    modifier: Modifier = Modifier,
    onNavigateToApiSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val clipboardManager = LocalClipboardManager.current

    val prompt by viewModel.imagePrompt.collectAsState()
    val selectedProvider by viewModel.selectedImageProvider.collectAsState()
    val selectedRatio by viewModel.selectedAspectRatio.collectAsState()
    val genState by viewModel.imageGenState.collectAsState()
    val recentImages by viewModel.recentImages.collectAsState()

    var fullscreenImageUrl by remember { mutableStateOf<String?>(null) }

    val samplePrompts = remember {
        listOf(
            "Cyberpunk Samurai with glowing katana in rainy neo Tokyo, 8k octane render",
            "Futuristic hypercar speeding through neon canyon, volumetric lights, cinematic",
            "Bioluminescent lotus floating in cybernetic zen temple, ultra detailed",
            "Hyper-realistic astronaut standing on crystalline alien planet with purple nebula",
            "Mecha guardian with glowing red energy core in destroyed cyberpunk metropolis"
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Engine Selector Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberRedBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberRedContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎨", fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                text = "IMAGE GENERATION ENGINE",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberRedBright,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "User API Key & Neural Diffusion",
                                fontSize = 11.sp,
                                color = CyberTextMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E0E14),
                        border = BorderStroke(1.dp, Color(0xFF5A1523)),
                        modifier = Modifier.clickable { onNavigateToApiSettings() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "API KEYS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }
                    }
                }

                // Provider chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val providers = listOf(
                        Triple("flux", "Pollinations / FLUX.1", "✨ Real-Time"),
                        Triple("stability", "Stability SD 3.5", "🎨 4K Pro"),
                        Triple("dalle", "OpenAI DALL-E 3", "🖼️ Creative HD"),
                        Triple("universal_image", "Universal Key", "🌌 Custom")
                    )

                    providers.forEach { (id, name, badge) ->
                        val isSelected = selectedProvider == id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) CyberRedDark else CyberSurface,
                            border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                            modifier = Modifier.clickable { viewModel.setImageProvider(id) }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else CyberTextPrimary
                                )
                                Text(
                                    text = badge,
                                    fontSize = 10.sp,
                                    color = if (isSelected) CyberAmber else CyberTextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Aspect Ratio Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ASPECT RATIO:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberTextMuted,
                letterSpacing = 0.5.sp
            )

            val ratios = listOf("1:1" to "Square", "16:9" to "Cinema", "9:16" to "Story", "4:3" to "Classic")
            ratios.forEach { (ratio, label) ->
                val isSelected = selectedRatio == ratio
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CyberRedContainer else CyberCard,
                    border = BorderStroke(1.dp, if (isSelected) CyberRedBright else Color(0xFF2A1015)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setAspectRatio(ratio) }
                ) {
                    Text(
                        text = ratio,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CyberRedBright else CyberTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }

        // Prompt Input Field
        OutlinedTextField(
            value = prompt,
            onValueChange = { viewModel.setImagePrompt(it) },
            placeholder = {
                Text(
                    text = "Describe your image (e.g. Cyberpunk samurai with neon katana...)",
                    fontSize = 13.5.sp,
                    color = CyberTextMuted
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("image_prompt_input"),
            shape = RoundedCornerShape(14.dp),
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberSurface,
                unfocusedContainerColor = CyberSurface,
                focusedBorderColor = CyberRedBright,
                unfocusedBorderColor = CyberRedBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = CyberRedBright
            ),
            trailingIcon = {
                if (prompt.isNotBlank()) {
                    IconButton(onClick = { viewModel.setImagePrompt("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = CyberTextMuted)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                if (prompt.isNotBlank()) viewModel.generateImage()
            })
        )

        // Inspiration Prompts Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            samplePrompts.forEach { sample ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF160A10),
                    border = BorderStroke(1.dp, Color(0xFF3B121A)),
                    modifier = Modifier.clickable { viewModel.setImagePrompt(sample) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("💡", fontSize = 11.sp)
                        Text(
                            text = sample.take(28) + "...",
                            fontSize = 11.sp,
                            color = CyberTextSecondary
                        )
                    }
                }
            }
        }

        // Generate Action Button
        val isGenerating = genState is GenerationState.Generating
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.generateImage()
            },
            enabled = prompt.isNotBlank() && !isGenerating,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyberRedBright,
                disabledContainerColor = CyberRedDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_image_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp
                    )
                    Text(
                        text = "SYNTHESIZING IMAGE...",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "GENERATE IMAGE (चित्र बनाएं)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Loading Progress Indicator
        if (genState is GenerationState.Generating) {
            val state = genState as GenerationState.Generating
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "glow"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBright.copy(alpha = glowAlpha)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { state.progress },
                        color = CyberRedBright,
                        trackColor = CyberRedDark,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = state.stageText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary,
                        textAlign = TextAlign.Center
                    )
                    LinearProgressIndicator(
                        progress = { state.progress },
                        color = CyberRedBright,
                        trackColor = Color(0xFF260D12),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            }
        }

        // Error Banner
        if (genState is GenerationState.Error) {
            val error = (genState as GenerationState.Error).errorMessage
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A0A0E)),
                border = BorderStroke(1.dp, CyberRedBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CyberRedBright)
                    Text(
                        text = error,
                        fontSize = 12.5.sp,
                        color = CyberRedBright,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.resetImageState() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = CyberTextMuted)
                    }
                }
            }
        }

        // Rendered Generated Image Card
        if (genState is GenerationState.Success) {
            val item = (genState as GenerationState.Success<GeneratedImageItem>).data
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.5.dp, CyberRedBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with model tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyberRedContainer,
                            border = BorderStroke(1.dp, CyberRed)
                        ) {
                            Text(
                                text = "🎨 ${item.providerId.uppercase()} • ${item.aspectRatio}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(onClick = { fullscreenImageUrl = item.imageUrl }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = CyberRedBright)
                        }
                    }

                    // Async Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(
                                when (item.aspectRatio) {
                                    "16:9" -> 16f / 9f
                                    "9:16" -> 9f / 16f
                                    "4:3" -> 4f / 3f
                                    else -> 1f
                                }
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .clickable { fullscreenImageUrl = item.imageUrl },
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.prompt,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = CyberRedBright, strokeWidth = 3.dp)
                                }
                            },
                            error = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = CyberRedBright)
                                    Text("Failed to render image stream", fontSize = 11.sp, color = CyberTextMuted)
                                }
                            }
                        )
                    }

                    // Prompt description
                    Text(
                        text = "\"${item.prompt}\"",
                        fontSize = 12.5.sp,
                        color = CyberTextSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.downloadImage(context, item) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("download_image_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("DOWNLOAD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { viewModel.shareImage(context, item) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                            border = BorderStroke(1.dp, CyberRedBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = CyberTextPrimary, modifier = Modifier.size(16.dp))
                                Text("SHARE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                            }
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(item.prompt))
                                Toast.makeText(context, "Prompt copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", tint = CyberTextMuted)
                        }
                    }
                }
            }
        }

        // Recent Generations Gallery
        if (recentImages.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RECENT IMAGE GENERATIONS (${recentImages.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted,
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentImages) { item ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, CyberRedBorder), RoundedCornerShape(10.dp))
                                .background(Color.Black)
                                .clickable { fullscreenImageUrl = item.imageUrl }
                        ) {
                            SubcomposeAsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.prompt,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Dialog on Tap
    fullscreenImageUrl?.let { url ->
        Dialog(onDismissRequest = { fullscreenImageUrl = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { fullscreenImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                SubcomposeAsyncImage(
                    model = url,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
        }
    }
}

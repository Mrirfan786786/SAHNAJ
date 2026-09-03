package com.example.presentation.screens

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SahNajApplication
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberRedGlow
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.util.TechSoundManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val TARGET_WORD = "WELCOME"
private const val SCRAMBLE_CHARS = "0101XYZΩΔ7#@%8943"

@Composable
fun SplashScreen(
    isLoggedIn: Boolean = false,
    isSetupCompleted: Boolean,
    hasSeenOnboarding: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToAssistant: () -> Unit
) {
    val context = LocalContext.current
    var isNavigated by remember { mutableStateOf(false) }

    // Glitch Decode State
    var displayedWord by remember { mutableStateOf("") }
    var currentLogStatus by remember { mutableStateOf("> INITIALIZING KERNEL...") }
    val screenFadeAlpha = remember { Animatable(1f) }

    // 5-Second Orchestration Sequence + Female Voice TTS + Cyber-AI Chime Sound FX
    LaunchedEffect(Unit) {
        // 1. Futuristic 3-second Cyber-AI initialization chime
        launch {
            TechSoundManager.playCyberStartupChime(context)
        }

        // 2. Clear, polite Female Voice TTS Welcome intro
        launch {
            delay(600L) // Harmonize with the sub-bass rise before female voice speaks
            try {
                val app = context.applicationContext as? SahNajApplication
                val femaleVoiceManager = app?.textToSpeechManager
                femaleVoiceManager?.speak(
                    "Welcome! Sahnaj AI core is fully initialized and ready for you.",
                    rate = 1.0f
                )
            } catch (_: Exception) { }
        }

        // 3. Fast Hacker Log & Letter Scramble Animation (Total timeline = ~4.6s + 0.4s fade = 5.0s)
        currentLogStatus = "> INITIALIZING KERNEL..."
        delay(350L)

        currentLogStatus = "> BYPASSING SECURITY PROTOCOLS..."
        delay(400L)

        currentLogStatus = "> SYNCHRONIZING NEURAL MATRIX..."

        // Glitch Scramble Decoding of "WELCOME"
        val chars = CharArray(TARGET_WORD.length) { ' ' }
        for (index in TARGET_WORD.indices) {
            // Scramble for 3 fast iterations before locking
            for (step in 0..2) {
                chars[index] = SCRAMBLE_CHARS[Random.nextInt(SCRAMBLE_CHARS.length)]
                displayedWord = String(chars).trimEnd()
                delay(65L)
            }
            chars[index] = TARGET_WORD[index]
            displayedWord = String(chars).trimEnd()
            delay(120L)
        }

        currentLogStatus = "> CALIBRATING HOLOGRAPHIC CORE..."
        delay(550L)

        currentLogStatus = "> FEMALE AI VOICE SYNTHESIZER READY"
        delay(650L)

        currentLogStatus = "> SYSTEM ONLINE // SAHNAJ AI CORE"
        delay(850L)

        // Smooth fade out transition to total exactly 5.0 seconds
        screenFadeAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )

        if (!isNavigated) {
            isNavigated = true
            if (!isLoggedIn) {
                onNavigateToLogin()
            } else if (!hasSeenOnboarding) {
                onNavigateToOnboarding()
            } else if (!isSetupCompleted) {
                onNavigateToSetup()
            } else {
                onNavigateToAssistant()
            }
        }
    }

    // Infinite Animation Transitions for Hologram, Scanlines, and Neon Glow
    val infiniteTransition = rememberInfiniteTransition(label = "cyber_hacker_effects")

    val hologramRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hologram_rot"
    )

    val audioWavePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "audio_wave_pulse"
    )

    val neonGlowShimmer by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_shimmer"
    )

    val cursorBlink by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_blink"
    )

    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanline_beam"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenFadeAlpha.value)
            .background(CyberBlack)
    ) {
        // 1. Matrix Cyber Grid & Flowing Binary Rain Canvas
        MatrixCyberCanvas(scanlineOffset = scanlineY)

        // 2. Foreground Content Structure
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Status Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF14070A),
                border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CyberGreen)
                    )
                    Text(
                        text = "SECURE PROTOCOL • ENCRYPTED 256-BIT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextSecondary,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Center Hologram Sphere & Hacker Terminal Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Pulsing 3D Cyber Hologram Sphere with Live Reacting Audio Wave Rings
                HolographicAssistantCore(
                    rotationDegrees = hologramRotation,
                    wavePulse = audioWavePulse,
                    neonShimmer = neonGlowShimmer
                )

                // Neon Bordered Terminal Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardElevated.copy(alpha = 0.92f))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(CyberRedBright, CyberRedDark, CyberRedBright)
                                )
                            ),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 22.dp, vertical = 18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Top Terminal Tag
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CyberRedBright)
                            )
                            Text(
                                text = "TERMINAL // NEURAL_OVERRIDE_SUCCESS",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp,
                                color = CyberRedBright
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Big Glitch Typewriter "WELCOME" Text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = displayedWord,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 5.sp,
                                color = CyberTextPrimary
                            )

                            if (displayedWord.length < TARGET_WORD.length || cursorBlink > 0.5f) {
                                Text(
                                    text = "█",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright.copy(alpha = cursorBlink)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Fast Cycling Hacker Logs Status
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0D0305),
                            border = BorderStroke(1.dp, CyberRedContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currentLogStatus,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberGreen,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // 3. Unique Bottom Branding Animation: "सहनाज AI CORE • मुहम्मद इरफ़ान आलम"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF140407))
                    .border(
                        BorderStroke(
                            1.dp,
                            CyberRedBright.copy(alpha = neonGlowShimmer)
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberRedBright.copy(alpha = neonGlowShimmer))
                    )
                    Text(
                        text = "सहनाज AI CORE • मुहम्मद इरफ़ान आलम",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CyberRedBright.copy(alpha = neonGlowShimmer))
                    )
                }
            }
        }
    }
}

/**
 * Holographic 3D-Style AI Sphere Core with rotating audio wave rings
 */
@Composable
private fun HolographicAssistantCore(
    rotationDegrees: Float,
    wavePulse: Float,
    neonShimmer: Float
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(150.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 4.2f

            // Outer Audio Wave Ring 3 (Farthest pulsing ring)
            drawCircle(
                color = CyberRed.copy(alpha = 0.18f * neonShimmer),
                radius = baseRadius * 2.2f * wavePulse,
                center = center,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
            )

            // Outer Audio Wave Ring 2
            drawCircle(
                color = CyberRedBright.copy(alpha = 0.35f * neonShimmer),
                radius = baseRadius * 1.75f * wavePulse,
                center = center,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), rotationDegrees)
                )
            )

            // Rotating Cyber HUD Ring 1
            rotate(rotationDegrees, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            CyberRedBright,
                            CyberGreen.copy(alpha = 0.6f),
                            CyberRed,
                            Color.Transparent,
                            CyberRedBright
                        ),
                        center = center
                    ),
                    radius = baseRadius * 1.35f,
                    center = center,
                    style = Stroke(
                        width = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 18f), 0f)
                    )
                )
            }

            // Reverse Rotating Orbit Ring
            rotate(-rotationDegrees * 1.4f, pivot = center) {
                drawCircle(
                    color = CyberGreen.copy(alpha = 0.45f),
                    radius = baseRadius * 1.15f,
                    center = center,
                    style = Stroke(
                        width = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 20f), 0f)
                    )
                )
            }

            // Glowing 3D Hologram Sphere Center Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        CyberRedBright,
                        CyberRedDark,
                        CyberBlack.copy(alpha = 0.9f)
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius * (0.92f + 0.08f * wavePulse),
                center = center
            )

            // Inner Core Cyber Grid cross-lines
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(center.x - baseRadius * 0.7f, center.y),
                end = Offset(center.x + baseRadius * 0.7f, center.y),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - baseRadius * 0.7f),
                end = Offset(center.x, center.y + baseRadius * 0.7f),
                strokeWidth = 1.5f
            )
        }
    }
}

/**
 * Matrix Grid & Binary Code Stream Canvas
 */
@Composable
private fun MatrixCyberCanvas(scanlineOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Subtle Dark Matrix Grid
        val gridSize = 36.dp.toPx()
        var x = 0f
        while (x < width) {
            drawLine(
                color = CyberRedDark.copy(alpha = 0.12f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += gridSize
        }
        var y = 0f
        while (y < height) {
            drawLine(
                color = CyberRedDark.copy(alpha = 0.12f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }

        // 2. Falling Binary Stream Particles (Green & Red Matrix Code Columns)
        val columnSpacing = 50.dp.toPx()
        val numColumns = (width / columnSpacing).toInt() + 1
        for (col in 0 until numColumns) {
            val colX = col * columnSpacing + 12f
            val speedFactor = 0.8f + (col % 4) * 0.35f
            val streamY = ((scanlineOffset * speedFactor + col * 120f) % (height + 300f)) - 100f

            // Draw glowing particle nodes along stream
            for (p in 0..4) {
                val particleY = streamY - p * 24f
                if (particleY in 0f..height) {
                    val isGreen = (col + p) % 3 == 0
                    val alpha = (1f - (p / 5f)) * 0.45f
                    drawCircle(
                        color = if (isGreen) CyberGreen.copy(alpha = alpha) else CyberRedBright.copy(alpha = alpha),
                        radius = if (p == 0) 2.5f else 1.5f,
                        center = Offset(colX, particleY)
                    )
                }
            }
        }

        // 3. Laser Scanline Moving Beam
        val scanY = (scanlineOffset % (height + 250f)) - 100f
        if (scanY in 0f..height) {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        CyberRedBright.copy(alpha = 0.25f),
                        CyberRedBright.copy(alpha = 0.6f),
                        CyberGreen.copy(alpha = 0.4f),
                        CyberRedBright.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                ),
                start = Offset(0f, scanY),
                end = Offset(width, scanY),
                strokeWidth = 2.5f
            )
        }
    }
}

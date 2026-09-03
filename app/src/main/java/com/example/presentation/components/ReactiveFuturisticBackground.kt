package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.presentation.viewmodel.AssistantStatus
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberRedGlow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modern, battery-friendly ambient background animation with:
 * 1. Continuous smooth looping soft glowing gradient orbs & floating aura.
 * 2. Real-time reactive waveform & glowing pulse that activates when
 *    the assistant is listening (reacts to voice RMS volume), speaking, or thinking.
 * 3. High-performance single-canvas rendering with zero frame drops.
 */
@Composable
fun ReactiveFuturisticBackground(
    status: AssistantStatus = AssistantStatus.Idle,
    rmsDb: Float = 0f,
    isSpeaking: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isListening = status is AssistantStatus.Listening
    val isThinking = status is AssistantStatus.Thinking
    val isError = status is AssistantStatus.Error
    val isActive = isListening || isSpeaking || isThinking

    val infiniteTransition = rememberInfiniteTransition(label = "FuturisticBgTransition")

    // Slow ambient orb movement (horizontal & vertical orbits)
    val orbPhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPhase1"
    )

    val orbPhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbPhase2"
    )

    // Waveform continuous drift phase
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 3000 else 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // Ambient pulse scale
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 1000 else 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseFactor"
    )

    // Reactive amplitude calculation based on RMS dB audio volume
    val normalizedRms = if (isListening) (rmsDb.coerceIn(0f, 15f) / 15f) else 0f
    val baseWaveAmplitude = when {
        isListening -> 35f + (normalizedRms * 55f)
        isSpeaking -> 40f * pulseFactor
        isThinking -> 25f * pulseFactor
        isError -> 15f
        else -> 14f * pulseFactor
    }

    // Path objects reused across redraws
    val wavePath1 = remember { Path() }
    val wavePath2 = remember { Path() }
    val wavePath3 = remember { Path() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Pure dark base
        drawRect(color = CyberBlack)

        // 2. Soft Ambient Glowing Orbs (Top-Right & Bottom-Left)
        val orb1CenterX = width * 0.75f + (cos(orbPhase1) * width * 0.15f)
        val orb1CenterY = height * 0.22f + (sin(orbPhase1) * height * 0.10f)
        val orb1Radius = (width * 0.55f) * (if (isActive) pulseFactor * 1.08f else pulseFactor)

        val glowColor1 = when {
            isListening -> CyberRedBright.copy(alpha = 0.22f)
            isSpeaking -> CyberRedGlow.copy(alpha = 0.26f)
            isThinking -> CyberRed.copy(alpha = 0.20f)
            else -> CyberRedDark.copy(alpha = 0.14f)
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor1,
                    CyberRedContainer.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(orb1CenterX, orb1CenterY),
                radius = orb1Radius.coerceAtLeast(10f)
            ),
            center = Offset(orb1CenterX, orb1CenterY),
            radius = orb1Radius.coerceAtLeast(10f)
        )

        val orb2CenterX = width * 0.25f + (sin(orbPhase2) * width * 0.18f)
        val orb2CenterY = height * 0.72f + (cos(orbPhase2) * height * 0.12f)
        val orb2Radius = (width * 0.65f) * pulseFactor

        val glowColor2 = when {
            isListening -> CyberRedGlow.copy(alpha = 0.18f)
            isSpeaking -> CyberRedBright.copy(alpha = 0.22f)
            isThinking -> CyberRedDark.copy(alpha = 0.18f)
            else -> CyberRedContainer.copy(alpha = 0.12f)
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor2,
                    CyberRedDark.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(orb2CenterX, orb2CenterY),
                radius = orb2Radius.coerceAtLeast(10f)
            ),
            center = Offset(orb2CenterX, orb2CenterY),
            radius = orb2Radius.coerceAtLeast(10f)
        )

        // 3. Central Ambient Reactive Waveforms (flowing across lower-middle of the screen)
        val waveBaseY = height * 0.68f

        drawWaveLayer(
            path = wavePath1,
            width = width,
            baseY = waveBaseY,
            amplitude = baseWaveAmplitude,
            frequency = 0.007f,
            phase = wavePhase,
            strokeColor = if (isActive) CyberRedBright.copy(alpha = 0.35f) else CyberRed.copy(alpha = 0.18f),
            fillBrush = Brush.verticalGradient(
                colors = listOf(
                    if (isActive) CyberRedBright.copy(alpha = 0.12f) else CyberRedDark.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                startY = waveBaseY - baseWaveAmplitude,
                endY = height
            ),
            height = height
        )

        drawWaveLayer(
            path = wavePath2,
            width = width,
            baseY = waveBaseY + 12f,
            amplitude = baseWaveAmplitude * 0.75f,
            frequency = 0.010f,
            phase = wavePhase + 1.8f,
            strokeColor = if (isActive) CyberRedGlow.copy(alpha = 0.28f) else CyberRedDark.copy(alpha = 0.14f),
            fillBrush = null,
            height = height
        )

        if (isActive) {
            drawWaveLayer(
                path = wavePath3,
                width = width,
                baseY = waveBaseY - 8f,
                amplitude = baseWaveAmplitude * 1.15f,
                frequency = 0.005f,
                phase = wavePhase * 1.3f + 0.8f,
                strokeColor = CyberRedBright.copy(alpha = 0.45f),
                fillBrush = null,
                height = height
            )
        }

        // 4. Subtle Ambient Floating Grid Points (High-tech cyber aesthetic)
        val gridStep = 70f
        val startX = (gridStep * 0.5f)
        var x = startX
        var yIndex = 0
        while (x < width) {
            var y = 50f
            while (y < height) {
                val dotAlpha = 0.025f + (sin(orbPhase1 + (x * 0.01f) + (y * 0.01f)) * 0.015f).toFloat()
                drawCircle(
                    color = CyberRedBright.copy(alpha = dotAlpha.coerceIn(0.01f, 0.06f)),
                    radius = 1.2f,
                    center = Offset(x, y)
                )
                y += gridStep
            }
            x += gridStep
            yIndex++
        }
    }
}

private fun DrawScope.drawWaveLayer(
    path: Path,
    width: Float,
    baseY: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    strokeColor: Color,
    fillBrush: Brush?,
    height: Float
) {
    path.reset()
    path.moveTo(0f, baseY)

    val step = 14f
    var currX = 0f
    while (currX <= width + step) {
        val sinVal = sin((currX * frequency) + phase)
        val cosVal = cos((currX * frequency * 0.5f) + (phase * 0.7f))
        val y = baseY + (sinVal * amplitude) + (cosVal * amplitude * 0.35f)
        path.lineTo(currX, y)
        currX += step
    }

    // Draw wave contour line
    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(width = 2.dp.toPx())
    )

    // Draw translucent gradient fill if specified
    fillBrush?.let { brush ->
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = brush,
            style = Fill
        )
    }
}

package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.SahNajApplication
import com.example.presentation.viewmodel.AssistantStatus
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRedBright

/**
 * 17-Year-Old Cybernetic Girl Avatar Themes
 */
data class AvatarTheme(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val subtitleEn: String,
    val hexString: String,
    val colorLong: Long,
    val imageUrl: String,
    val accentColor: Color,
    val secondaryColor: Color
)

val AVATAR_THEMES = listOf(
    AvatarTheme(
        id = "crimson_valkyrie",
        nameEn = "Crimson Valkyrie",
        nameHi = "क्रिमसन वाल्किरी (मूल)",
        subtitleEn = "Red/Black Cyber Samurai Girl aesthetic",
        hexString = "#E10600",
        colorLong = 0xFFE10600L,
        imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
        accentColor = Color(0xFFFF1744),
        secondaryColor = Color(0xFFFF8A80)
    ),
    AvatarTheme(
        id = "neon_cyberpunk",
        nameEn = "Neon Cyberpunk",
        nameHi = "नियॉन साइबरपंक",
        subtitleEn = "Purple/Magenta high-tech Visor Girl",
        hexString = "#9B51E0",
        colorLong = 0xFF9B51E0L,
        imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&auto=format&fit=crop&q=80",
        accentColor = Color(0xFFD946EF),
        secondaryColor = Color(0xFFE879F9)
    ),
    AvatarTheme(
        id = "quantum_matrix",
        nameEn = "Quantum Matrix",
        nameHi = "क्वांटम मैट्रिक्स",
        subtitleEn = "Cyan/Blue Hologram Girl",
        hexString = "#00B0FF",
        colorLong = 0xFF00B0FFL,
        imageUrl = "https://images.unsplash.com/photo-1563089145-599997674d42?w=600&auto=format&fit=crop&q=80",
        accentColor = Color(0xFF00E5FF),
        secondaryColor = Color(0xFF80D8FF)
    ),
    AvatarTheme(
        id = "emerald_shadow",
        nameEn = "Emerald Shadow",
        nameHi = "एमराल्ड शैडो",
        subtitleEn = "Matrix Green Tactical Girl",
        hexString = "#00FF9D",
        colorLong = 0xFF00FF9DL,
        imageUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80",
        accentColor = Color(0xFF00E676),
        secondaryColor = Color(0xFFB9F6CA)
    )
)

fun getAvatarThemeForColor(colorLong: Long): AvatarTheme {
    return AVATAR_THEMES.find { it.colorLong == colorLong } ?: AVATAR_THEMES.first()
}

/**
 * Animated 17-year-old Cybernetic Girl Avatar (Sahnaj) with glowing hair highlights,
 * cyber visor/headset, pulsating holographic aura rings, dynamic lip-sync, and eye pulse.
 * Replaces the static red holographic mic orb throughout the app.
 */
@Composable
fun HolographicVoiceOrb(
    status: AssistantStatus,
    rmsDb: Float = 0f,
    isSpeaking: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    customColor: Color? = null,
    orbSize: Dp? = null
) {
    val context = LocalContext.current
    val app = context.applicationContext as? SahNajApplication
    val savedColorLong by (app?.userPreferences?.orbColor?.collectAsState() ?: remember { mutableStateOf(0xFFE10600L) })
    val savedScale by (app?.userPreferences?.orbScale?.collectAsState() ?: remember { mutableFloatStateOf(1.0f) })

    val activeTheme = remember(savedColorLong) { getAvatarThemeForColor(savedColorLong) }
    val baseColor = customColor ?: Color(savedColorLong)
    val effectiveSize = orbSize ?: size ?: (230.dp * savedScale)

    val isListening = status is AssistantStatus.Listening
    val isThinking = status is AssistantStatus.Thinking
    val isSpeakingActive = isSpeaking || status is AssistantStatus.Speaking

    val infiniteTransition = rememberInfiniteTransition(label = "SahnajCyberGirlAvatarMotion")

    // 1. Natural breathing & floating vertical offset
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_float"
    )

    // 2. Halo alpha pulsation
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_halo_pulse"
    )

    // 3. Ring scale pulsation
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_ring_scale"
    )

    // 4. Orbital rotation for cyber HUD segments
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatar_ring_rot"
    )

    val ringRotationRev by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatar_ring_rot_rev"
    )

    // 5. Lip-sync mouth expansion when active vocal speaking
    val mouthMelt by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouth_sync"
    )

    // 6. Glowing eye pulse when listening or speaking
    val eyePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eye_pulse"
    )

    // Audio reactivity scale
    val dynamicAudioScale = if (isListening) {
        1.0f + (rmsDb.coerceIn(0f, 15f) / 20f)
    } else if (isThinking || isSpeakingActive) {
        1.04f
    } else {
        1.0f
    }

    val activeHaloColor = when {
        isListening -> CyberGreen
        isSpeakingActive -> activeTheme.accentColor
        else -> baseColor
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(effectiveSize)
            .scale(dynamicAudioScale)
            .offset(y = floatOffset.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = effectiveSize / 2),
                onClick = onClick
            )
    ) {
        // A. Pulsating Holographic Cyber Rings & Futuristic Orbit Nodes
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val baseRadius = canvasSize.minDimension / 2f

            // Atmospheric ambient radial energy pulse
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        activeHaloColor.copy(alpha = if (isSpeakingActive || isListening) 0.55f * haloPulse else 0.30f * haloPulse),
                        activeHaloColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = center
            )

            // Outer Dashed Pulsing Holographic Ring
            drawCircle(
                color = activeHaloColor.copy(alpha = (0.75f * haloPulse).coerceIn(0.2f, 1f)),
                radius = (baseRadius * 0.94f) * ringScale,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f), 0f)
                ),
                center = center
            )

            // Orbiting Segment 1
            rotate(ringRotation, pivot = center) {
                drawArc(
                    color = activeHaloColor,
                    startAngle = 10f,
                    sweepAngle = 75f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.85f, center.y - baseRadius * 0.85f),
                    size = Size(baseRadius * 1.70f, baseRadius * 1.70f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = Color.White.copy(alpha = 0.8f),
                    startAngle = 180f,
                    sweepAngle = 60f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.85f, center.y - baseRadius * 0.85f),
                    size = Size(baseRadius * 1.70f, baseRadius * 1.70f),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
                // Orbiting satellite tech dot
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = Offset(center.x + baseRadius * 0.85f, center.y)
                )
            }

            // Orbiting Segment 2 (Reverse)
            rotate(ringRotationRev, pivot = center) {
                drawArc(
                    color = activeTheme.secondaryColor.copy(alpha = 0.65f),
                    startAngle = 50f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 0.74f, center.y - baseRadius * 0.74f),
                    size = Size(baseRadius * 1.48f, baseRadius * 1.48f),
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )
            }
        }

        // B. Cyber Girl Avatar Center Container
        val avatarSize = effectiveSize * 0.62f
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(avatarSize)
                .shadow(
                    elevation = if (isSpeakingActive || isListening) 28.dp else 16.dp,
                    shape = CircleShape,
                    ambientColor = activeHaloColor,
                    spotColor = activeHaloColor
                )
                .clip(CircleShape)
                .background(Color(0xFF0D0312))
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            activeHaloColor,
                            activeTheme.secondaryColor,
                            Color.White,
                            activeHaloColor
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            // Clean Dark Cybernetic Backdrop
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1E0E22),
                                Color(0xFF0E0612),
                                Color(0xFF06020A)
                            )
                        )
                    )
            )

            // High-Tech 17-Year-Old Cyber Girl Avatar Graphic (clean character silhouette & facial details)
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "SAHNAJ 17-Year-Old Cyber Girl Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.25f)
            )

            // High-tech Cybernetic HUD Overlay on Avatar (Visor tint, glowing eyes, and lip-sync)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasSize = this.size
                val cx = canvasSize.width / 2f
                val cy = canvasSize.height / 2f
                val s = canvasSize.minDimension / 140f

                // Cyber-hair glow sheen over top and sides
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            activeHaloColor.copy(alpha = if (isSpeakingActive || isListening) 0.45f else 0.25f)
                        ),
                        center = Offset(cx, cy * 0.65f),
                        radius = canvasSize.minDimension * 0.55f
                    ),
                    radius = canvasSize.minDimension * 0.5f,
                    center = Offset(cx, cy)
                )

                // Glowing Neon Headset / Cyber Visor Clip on temple
                drawRoundRect(
                    color = activeHaloColor,
                    topLeft = Offset(cx + 42f * s, cy - 14f * s),
                    size = Size(9f * s, 26f * s),
                    cornerRadius = CornerRadius(3f * s)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.2f * s,
                    center = Offset(cx + 46.5f * s, cy - 8f * s)
                )

                // Left Visor Ear Comms Node
                drawRoundRect(
                    color = activeHaloColor,
                    topLeft = Offset(cx - 51f * s, cy - 14f * s),
                    size = Size(9f * s, 26f * s),
                    cornerRadius = CornerRadius(3f * s)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.2f * s,
                    center = Offset(cx - 46.5f * s, cy - 8f * s)
                )

                // Glowing Cyber HUD Cheek Tattoos
                val leftMarking = Path().apply {
                    moveTo(cx - 30f * s, cy + 12f * s)
                    lineTo(cx - 36f * s, cy + 18f * s)
                    lineTo(cx - 28f * s, cy + 24f * s)
                }
                drawPath(
                    path = leftMarking,
                    color = activeHaloColor,
                    style = Stroke(width = 1.8f * s, cap = StrokeCap.Round)
                )
                drawCircle(
                    color = activeHaloColor,
                    radius = 1.8f * s,
                    center = Offset(cx - 28f * s, cy + 24f * s)
                )

                // Glowing Eye Pulse when speaking or listening
                if (isSpeakingActive || isListening) {
                    val eyeColor = if (isListening) CyberGreen else activeHaloColor
                    val leftEyeCenter = Offset(cx - 16f * s, cy - 4f * s)
                    val rightEyeCenter = Offset(cx + 16f * s, cy - 4f * s)
                    val eyeRadius = 3.5f * s * (0.8f + 0.3f * eyePulse)

                    // Outer Eye Aura
                    drawCircle(
                        color = eyeColor.copy(alpha = 0.5f * eyePulse),
                        radius = eyeRadius * 1.8f,
                        center = leftEyeCenter
                    )
                    drawCircle(
                        color = eyeColor.copy(alpha = 0.5f * eyePulse),
                        radius = eyeRadius * 1.8f,
                        center = rightEyeCenter
                    )

                    // Cyber Eye Iris
                    drawCircle(
                        color = eyeColor,
                        radius = eyeRadius,
                        center = leftEyeCenter
                    )
                    drawCircle(
                        color = eyeColor,
                        radius = eyeRadius,
                        center = rightEyeCenter
                    )

                    // White pupil reflection
                    drawCircle(
                        color = Color.White,
                        radius = 1.4f * s,
                        center = Offset(leftEyeCenter.x + 0.8f * s, leftEyeCenter.y - 0.8f * s)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.4f * s,
                        center = Offset(rightEyeCenter.x + 0.8f * s, rightEyeCenter.y - 0.8f * s)
                    )
                }

                // Responsive Lip-sync Mouth Indicator when speaking
                if (isSpeakingActive) {
                    val mouthW = (12f + 8f * mouthMelt) * s
                    val mouthH = (4f + 9f * mouthMelt) * s
                    drawOval(
                        color = Color(0xFF4A0817),
                        topLeft = Offset(cx - mouthW / 2f, cy + 30f * s - mouthH / 2f),
                        size = Size(mouthW, mouthH)
                    )
                    drawOval(
                        color = activeHaloColor,
                        topLeft = Offset(cx - mouthW / 2f, cy + 30f * s - mouthH / 2f),
                        size = Size(mouthW, mouthH),
                        style = Stroke(width = 1.8f * s)
                    )
                }
            }
        }
    }
}

package com.example.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.example.SahNajApplication
import com.example.presentation.navigation.Screen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberRedGlow
import com.example.ui.theme.CyberTextMuted

/**
 * High-end Bottom Navigation matching MYRA reference screenshots.
 * Features 5 items: Home, Chat, Floating Cosmic Orb (Center), Triggers, Settings.
 */
@Composable
fun SahNajBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onOrbClick: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as? SahNajApplication
    val savedColorLong by (app?.userPreferences?.orbColor?.collectAsState() ?: remember { mutableStateOf(0xFFE10600L) })
    val baseOrbColor = Color(savedColorLong)

    val brightOrbColor = baseOrbColor
    val darkOrbColor = Color(
        red = (baseOrbColor.red * 0.35f).coerceIn(0f, 1f),
        green = (baseOrbColor.green * 0.35f).coerceIn(0f, 1f),
        blue = (baseOrbColor.blue * 0.35f).coerceIn(0f, 1f)
    )
    val lightGlow = Color(
        red = (baseOrbColor.red * 0.7f + 0.3f).coerceIn(0f, 1f),
        green = (baseOrbColor.green * 0.7f + 0.3f).coerceIn(0f, 1f),
        blue = (baseOrbColor.blue * 0.7f + 0.3f).coerceIn(0f, 1f)
    )

    val infiniteTransition = rememberInfiniteTransition(label = "BottomOrbGlow")
    val orbGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orbGlowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Base Navigation Bar Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF16161D),
                            Color(0xFF0D0D12),
                            CyberBlack
                        )
                    )
                )
                .drawBehind {
                    // Top subtle divider line
                    drawLine(
                        color = Color(0xFF2B2B36),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                },
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentRoute == Screen.Dashboard.route,
                onClick = { onNavigate(Screen.Dashboard.route) }
            )

            // 2. Chat
            BottomNavItem(
                icon = Icons.Default.ChatBubbleOutline,
                label = "Chat",
                isSelected = currentRoute == Screen.Chat.route,
                onClick = { onNavigate(Screen.Chat.route) }
            )

            // Placeholder Spacer for elevated center orb
            Spacer(modifier = Modifier.size(56.dp))

            // 4. Triggers
            BottomNavItem(
                icon = Icons.Default.Bolt,
                label = "Triggers",
                isSelected = currentRoute == Screen.Triggers.route,
                onClick = { onNavigate(Screen.Triggers.route) }
            )

            // 5. Settings
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) }
            )
        }

        // Center Elevated Glowing Floating Holographic Orb Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(62.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = brightOrbColor,
                    spotColor = baseOrbColor
                )
                .clip(CircleShape)
                .background(Color(0xFF0F0B12))
                .border(
                    width = 2.dp,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            brightOrbColor.copy(alpha = orbGlowAlpha),
                            baseOrbColor,
                            darkOrbColor
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, radius = 30.dp),
                    onClick = onOrbClick
                )
        ) {
            // Cosmic gradient sphere inside the button (matching reference)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                lightGlow,
                                brightOrbColor,
                                baseOrbColor,
                                darkOrbColor,
                                Color(0xFF08020D)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = if (isSelected) CyberRedBright else CyberTextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 28.dp),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = activeColor,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = activeColor
        )
    }
}

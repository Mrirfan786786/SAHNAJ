package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pure Red-Black Theme Scheme
private val RedBlackColorScheme = darkColorScheme(
    primary = CyberRedBright,
    onPrimary = Color.White,
    primaryContainer = CyberRedContainer,
    onPrimaryContainer = Color(0xFFFFDADA),
    secondary = CyberRed,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF380505),
    onSecondaryContainer = Color(0xFFFFD1D1),
    tertiary = CyberRedGlow,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2A0000),
    onTertiaryContainer = Color(0xFFFFCCCC),
    background = CyberBlack,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberRedBorder,
    outlineVariant = Color(0xFF330808),
    error = CyberError,
    onError = Color.White
)

@Composable
fun SahNajAITheme(
    themeMode: String = "DARK",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // Red-Black Cyberpunk theme is the primary unified design system
    MaterialTheme(
        colorScheme = RedBlackColorScheme,
        typography = Typography,
        content = content
    )
}

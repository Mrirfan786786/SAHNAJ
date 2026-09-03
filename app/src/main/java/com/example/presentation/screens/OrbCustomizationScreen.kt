package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.SahNajApplication
import com.example.presentation.components.AVATAR_THEMES
import com.example.presentation.components.HolographicVoiceOrb
import com.example.presentation.components.ReactiveFuturisticBackground
import com.example.presentation.components.getAvatarThemeForColor
import com.example.presentation.viewmodel.AssistantStatus
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SizeOption(
    val labelEn: String,
    val labelHi: String,
    val scale: Float,
    val baseDp: Int
)

val PRESET_SIZE_OPTIONS = listOf(
    SizeOption("Small", "छोटा", 0.78f, 185),
    SizeOption("Medium", "मध्यम", 1.0f, 240),
    SizeOption("Large", "बड़ा", 1.18f, 280),
    SizeOption("Extra Large", "अति बड़ा", 1.35f, 320)
)

/**
 * Avatar Customization Screen (formerly Orb Customization).
 * Allows users to choose between 4 distinct 17-year-old Cyber Girl Avatar styles:
 * - Crimson Valkyrie (Default)
 * - Neon Cyberpunk
 * - Quantum Matrix
 * - Emerald Shadow
 * Each theme card features a mini avatar preview, and the live preview renders the chosen
 * animated cyber girl avatar with reactive floating, lip-sync, and cyber rings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrbCustomizationScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as? SahNajApplication
    val userPreferences = remember { app?.userPreferences ?: com.example.data.local.UserPreferences(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val savedColorLong by userPreferences.orbColor.collectAsState()
    val savedScale by userPreferences.orbScale.collectAsState()

    var activeColorLong by remember(savedColorLong) { mutableStateOf(savedColorLong) }
    var activeScale by remember(savedScale) { mutableFloatStateOf(savedScale) }
    var showCustomColorPicker by remember { mutableStateOf(false) }

    // Live preview interactive state
    var isPreviewActive by remember { mutableStateOf(false) }

    val activeTheme = remember(activeColorLong) { getAvatarThemeForColor(activeColorLong) }

    // HSV representation for custom color picker
    val initialColor = Color(activeColorLong)
    val hsv = remember(activeColorLong) {
        val hsvArr = FloatArray(3)
        android.graphics.Color.colorToHSV(initialColor.toArgb(), hsvArr)
        hsvArr
    }
    var customHue by remember { mutableFloatStateOf(hsv[0]) }
    var customSaturation by remember { mutableFloatStateOf(hsv[1]) }
    var customValue by remember { mutableFloatStateOf(hsv[2]) }

    val currentColor = Color(activeColorLong)

    Scaffold(
        containerColor = CyberBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Avatar Customization",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(activeTheme.accentColor)
                            )
                        }
                        Text(
                            text = "अवतार कस्टमाइज़ेशन • 4 शैलियाँ और साइज़",
                            fontSize = 12.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "BACK",
                            tint = CyberRedBright
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            userPreferences.resetOrbCustomization()
                            activeColorLong = com.example.data.local.UserPreferences.DEFAULT_ORB_COLOR
                            activeScale = com.example.data.local.UserPreferences.DEFAULT_ORB_SCALE
                            showCustomColorPicker = false
                            scope.launch {
                                snackbarHostState.showSnackbar("डिफ़ॉल्ट अवतार रीसेट हो गया (Reset to Crimson Valkyrie)")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "RESET TO DEFAULT",
                            tint = CyberTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface,
                    titleContentColor = CyberTextPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ReactiveFuturisticBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Face,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "SAHNAJ CYBER GIRL AVATAR ENGINE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberRedBright
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "सहनाज के 17-वर्षीय साइबर गर्ल अवतार की शैली, होलोग्राफिक ऑरा और आकार कस्टमाइज़ करें। यह बदलाव होम स्क्रीन और सभी वॉइस इंटरैक्शन में तुरंत दिखाई देगा।",
                            fontSize = 12.5.sp,
                            color = CyberTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }

                // Live Preview Stage Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0910)),
                    border = BorderStroke(1.5.dp, activeTheme.accentColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE AVATAR PREVIEW // अवतार पूर्वावलोकन",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextMuted
                            )
                            Surface(
                                color = activeTheme.accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, activeTheme.accentColor.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(activeTheme.accentColor)
                                    )
                                    Text(
                                        text = activeTheme.nameEn.uppercase(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeTheme.accentColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Live Holographic Cyber Girl Avatar Preview
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .height(260.dp)
                                .fillMaxWidth()
                        ) {
                            HolographicVoiceOrb(
                                status = if (isPreviewActive) AssistantStatus.Listening else AssistantStatus.Idle,
                                rmsDb = if (isPreviewActive) 8.5f else 0f,
                                isSpeaking = isPreviewActive,
                                customColor = currentColor,
                                orbSize = (230.dp * activeScale),
                                onClick = {
                                    isPreviewActive = !isPreviewActive
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isPreviewActive) "▶ एक्टिव वॉइस व लिप-सिंक पूर्वावलोकन (Tap to Stop)" else "अवतार पर टैप करके वॉइस एनीमेशन टेस्ट करें (Tap to Test Voice)",
                            fontSize = 12.sp,
                            color = if (isPreviewActive) CyberGreen else CyberTextMuted,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Section 1: Avatar Themes (4 Cyber Girl Presets)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "SECTION 1 — AVATAR THEMES // अवतार शैलियाँ",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "सहनाज की 17-वर्षीय साइबर शैली चुनें",
                                    fontSize = 12.sp,
                                    color = CyberRedBright
                                )
                            }
                        }

                        HorizontalDivider(color = CyberSurface, thickness = 1.dp)

                        Text(
                            text = "AVATAR THEMES // अवतार शैलियाँ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextMuted
                        )

                        // 4 Preset Cards with Mini Avatar Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AVATAR_THEMES.forEach { theme ->
                                val isSelected = (activeColorLong == theme.colorLong) && !showCustomColorPicker
                                val themeColor = theme.accentColor

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(160.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) CyberRedContainer.copy(alpha = 0.45f) else Color(0xFF14101A))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) themeColor else Color(0xFF2B2238),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true),
                                            onClick = {
                                                activeColorLong = theme.colorLong
                                                showCustomColorPicker = false
                                                userPreferences.setOrbColor(theme.colorLong)
                                            }
                                        )
                                        .padding(12.dp)
                                ) {
                                    // Mini Avatar Preview with theme styling
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(62.dp)
                                            .shadow(
                                                elevation = if (isSelected) 12.dp else 4.dp,
                                                shape = CircleShape,
                                                ambientColor = themeColor,
                                                spotColor = themeColor
                                            )
                                            .clip(CircleShape)
                                            .border(
                                                BorderStroke(
                                                    width = if (isSelected) 2.5.dp else 1.5.dp,
                                                    color = if (isSelected) Color.White else themeColor
                                                ),
                                                CircleShape
                                            )
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                            contentDescription = theme.nameEn,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .scale(1.25f)
                                        )
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color.Black.copy(alpha = 0.35f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = theme.nameEn,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CyberTextPrimary else CyberTextSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = theme.nameHi,
                                        fontSize = 10.5.sp,
                                        color = if (isSelected) themeColor else CyberTextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = theme.subtitleEn,
                                        fontSize = 9.5.sp,
                                        color = CyberTextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 12.sp,
                                        maxLines = 2
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Custom Color Tuner Toggle
                        OutlinedButton(
                            onClick = {
                                showCustomColorPicker = !showCustomColorPicker
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (showCustomColorPicker) CyberRedBright else CyberRedBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showCustomColorPicker) CyberRedContainer.copy(alpha = 0.25f) else Color(0xFF14101A)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (showCustomColorPicker) "कस्टम कलर पिकर छिपाएं (Hide Custom Tuner)" else "कस्टम कलर पिकर खोलें (Custom HSV Tuner)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                            }
                        }

                        // Expandable Custom HSV Color Picker
                        AnimatedVisibility(
                            visible = showCustomColorPicker,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0B15)),
                                border = BorderStroke(1.dp, Color(0xFF332042)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "HSV COLOR TUNER // फाइन कलर ट्यूनर",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberRedBright
                                    )

                                    // 1. Hue Slider (0 - 360)
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Hue (रंग टोन)", fontSize = 12.sp, color = CyberTextSecondary)
                                            Text("${customHue.roundToInt()}°", fontSize = 12.sp, color = CyberRedBright, fontFamily = FontFamily.Monospace)
                                        }
                                        Slider(
                                            value = customHue,
                                            onValueChange = { hue ->
                                                customHue = hue
                                                val argb = android.graphics.Color.HSVToColor(floatArrayOf(customHue, customSaturation, customValue))
                                                val colorL = argb.toLong() and 0xFFFFFFFFL
                                                activeColorLong = colorL
                                                userPreferences.setOrbColor(colorL)
                                            },
                                            valueRange = 0f..360f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberRedBright,
                                                activeTrackColor = CyberRed,
                                                inactiveTrackColor = Color(0xFF282030)
                                            )
                                        )
                                    }

                                    // 2. Saturation Slider (0 - 1)
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Saturation (तीव्रता)", fontSize = 12.sp, color = CyberTextSecondary)
                                            Text("${(customSaturation * 100).roundToInt()}%", fontSize = 12.sp, color = CyberRedBright, fontFamily = FontFamily.Monospace)
                                        }
                                        Slider(
                                            value = customSaturation,
                                            onValueChange = { sat ->
                                                customSaturation = sat
                                                val argb = android.graphics.Color.HSVToColor(floatArrayOf(customHue, customSaturation, customValue))
                                                val colorL = argb.toLong() and 0xFFFFFFFFL
                                                activeColorLong = colorL
                                                userPreferences.setOrbColor(colorL)
                                            },
                                            valueRange = 0.1f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberRedBright,
                                                activeTrackColor = CyberRed,
                                                inactiveTrackColor = Color(0xFF282030)
                                            )
                                        )
                                    }

                                    // 3. Brightness / Value Slider (0 - 1)
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Brightness / Value (चमक)", fontSize = 12.sp, color = CyberTextSecondary)
                                            Text("${(customValue * 100).roundToInt()}%", fontSize = 12.sp, color = CyberRedBright, fontFamily = FontFamily.Monospace)
                                        }
                                        Slider(
                                            value = customValue,
                                            onValueChange = { v ->
                                                customValue = v
                                                val argb = android.graphics.Color.HSVToColor(floatArrayOf(customHue, customSaturation, customValue))
                                                val colorL = argb.toLong() and 0xFFFFFFFFL
                                                activeColorLong = colorL
                                                userPreferences.setOrbColor(colorL)
                                            },
                                            valueRange = 0.2f..1.0f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = CyberRedBright,
                                                activeTrackColor = CyberRed,
                                                inactiveTrackColor = Color(0xFF282030)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Avatar Size (आकार)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "SECTION 2 — AVATAR SIZE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "अवतार का साइज़ (आकार) सेट करें",
                                    fontSize = 12.sp,
                                    color = CyberRedBright
                                )
                            }
                        }

                        HorizontalDivider(color = CyberSurface, thickness = 1.dp)

                        // Quick Chips: Small, Medium, Large, Extra Large
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PRESET_SIZE_OPTIONS.forEach { sizeOpt ->
                                val isSelected = kotlin.math.abs(activeScale - sizeOpt.scale) < 0.06f

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        activeScale = sizeOpt.scale
                                        userPreferences.setOrbScale(sizeOpt.scale)
                                    },
                                    label = {
                                        Text(
                                            text = sizeOpt.labelEn,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberRedContainer,
                                        selectedLabelColor = CyberRedBright,
                                        containerColor = Color(0xFF14101A),
                                        labelColor = CyberTextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) CyberRedBright else Color(0xFF282030),
                                        selectedBorderColor = CyberRedBright
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Continuous Scale Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Continuous Scale (कस्टम स्केल)",
                                    fontSize = 12.sp,
                                    color = CyberTextSecondary
                                )
                                Text(
                                    text = "${(activeScale * 100).roundToInt()}% (${(240 * activeScale).roundToInt()} dp)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Slider(
                                value = activeScale,
                                onValueChange = { newScale ->
                                    activeScale = newScale
                                    userPreferences.setOrbScale(newScale)
                                },
                                valueRange = 0.75f..1.35f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberRedBright,
                                    activeTrackColor = CyberRed,
                                    inactiveTrackColor = Color(0xFF282030)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Small (75%)", fontSize = 10.sp, color = CyberTextMuted)
                                Text("Default (100%)", fontSize = 10.sp, color = CyberTextMuted)
                                Text("XL (135%)", fontSize = 10.sp, color = CyberTextMuted)
                            }
                        }
                    }
                }

                // Reset to Default Button
                Button(
                    onClick = {
                        userPreferences.resetOrbCustomization()
                        activeColorLong = com.example.data.local.UserPreferences.DEFAULT_ORB_COLOR
                        activeScale = com.example.data.local.UserPreferences.DEFAULT_ORB_SCALE
                        showCustomColorPicker = false
                        scope.launch {
                            snackbarHostState.showSnackbar("डिफ़ॉल्ट क्रिमसन वाल्किरी अवतार व 100% साइज़ रीसेट कर दिया गया")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1422)),
                    border = BorderStroke(1.dp, CyberRedBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Reset to Default // मूल स्थिति में रीसेट करें",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CyberRedBright
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

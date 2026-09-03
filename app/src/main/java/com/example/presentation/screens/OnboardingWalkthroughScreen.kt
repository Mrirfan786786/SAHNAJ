package com.example.presentation.screens

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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.personality.PersonalityResponses
import com.example.presentation.viewmodel.SetupViewModel
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

@Composable
fun OnboardingWalkthroughScreen(
    setupViewModel: SetupViewModel,
    onNavigateToPermissionSetup: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val onCompleteOrSkip = {
        setupViewModel.completeOnboarding()
        onNavigateToPermissionSetup()
    }

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberRedContainer)
                        .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ONBOARDING // ${pagerState.currentPage + 1}/4",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )
                }

                // Skip button for experienced users
                TextButton(
                    onClick = onCompleteOrSkip,
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = "SKIP / छोड़ें",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextMuted
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) CyberRedBright else CyberRedBorder.copy(alpha = 0.5f))
                        )
                    }
                }

                // Primary Navigation Buttons Row
                if (pagerState.currentPage < 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CyberRedBorder),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(
                                    text = "पीछे",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextSecondary
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "अगला",
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Final Slide CTA: "आगे बढ़ें" (Takes user to Permission Setup)
                    Button(
                        onClick = onCompleteOrSkip,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "आगे बढ़ें (अनुमतियाँ सेटअप करें)",
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            when (page) {
                0 -> Slide1Intro()
                1 -> Slide2VoiceControl()
                2 -> Slide3AskAnything()
                3 -> Slide4GetStarted()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 1: "मैं सहनाज हूं" — Introduction
// ─────────────────────────────────────────────────────────────
@Composable
private fun Slide1Intro() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_slide1")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing Center Avatar / Illustration Box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(CyberRedContainer)
                .border(BorderStroke(2.5.dp, CyberRedBright), CircleShape)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(105.dp)
                    .clip(CircleShape)
                    .background(CyberCard)
                    .border(BorderStroke(1.5.dp, CyberRed), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = CyberRedBright,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "नमस्ते! मैं सहनाज हूं",
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = CyberTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "SAHNAJ AI • YOUR SMART VOICE COMPANION",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberRedBright,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberRedBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "आपकी पर्सनल स्मार्ट वॉइस असिस्टेंट जो आपके एक आवाज़ देने पर फोन के काम करती है।",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyberTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Text(
                    text = "बस बोलिए 'सहनाज' या माइक बटन दबाएं!",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 2: "बोलकर फोन चलाओ" — Voice Control & Device Actions
// ─────────────────────────────────────────────────────────────
@Composable
private fun Slide2VoiceControl() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberRedContainer)
                .border(BorderStroke(2.dp, CyberRedBright), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Smartphone,
                contentDescription = null,
                tint = CyberRedBright,
                modifier = Modifier.size(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "बोलकर फोन चलाओ",
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = CyberTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "HANDS-FREE DEVICE AUTOMATION",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberRedBright,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "बिना स्क्रीन छुए कॉल करें, ऐप्स खोलें और सेटिंग्स बदलें:",
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            color = CyberTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Example Voice Commands Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExampleVoiceCommandCard(
                icon = Icons.Default.ChatBubble,
                commandText = "“सहनाज, WhatsApp खोलो”",
                subtitle = "कोई भी ऐप तुरंत ओपन करें"
            )
            ExampleVoiceCommandCard(
                icon = Icons.Default.Call,
                commandText = "“राहुल को कॉल लगाओ”",
                subtitle = "कॉन्टैक्ट्स से सीधा वॉइस कॉल"
            )
            ExampleVoiceCommandCard(
                icon = Icons.Default.Settings,
                commandText = "“Bluetooth और Wi-Fi ऑन करो”",
                subtitle = "फोन सेटिंग्स और कंट्रोल्स"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 3: "मुझसे कुछ भी पूछो" — General Knowledge & Gemini AI
// ─────────────────────────────────────────────────────────────
@Composable
private fun Slide3AskAnything() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CyberRedContainer)
                .border(BorderStroke(2.dp, CyberRedBright), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = CyberRedBright,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "मुझसे कुछ भी पूछो",
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = CyberTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "POWERED BY GOOGLE GEMINI AI",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberRedBright,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "जनरल नॉलेज, गणित, मौसम और रोज़मर्रा के सवाल हिंदी और हिंग्लिश में पूछें:",
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            color = CyberTextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExampleVoiceCommandCard(
                icon = Icons.AutoMirrored.Filled.Help,
                commandText = "“भारत की राजधानी क्या है?”",
                subtitle = "ज्ञान और तथ्य तुरंत जानें"
            )
            ExampleVoiceCommandCard(
                icon = Icons.Default.AutoAwesome,
                commandText = "“आज का मौसम कैसा रहेगा?”",
                subtitle = "लाइव जानकारी और अपडेट्स"
            )
            ExampleVoiceCommandCard(
                icon = Icons.Default.CheckCircle,
                commandText = "“15 * 24 कितना होता है?”",
                subtitle = "कैलकुलेशन और सवाल-जवाब"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SLIDE 4: "शुरू करते हैं" — Get Started & Permission Prep
// ─────────────────────────────────────────────────────────────
@Composable
private fun Slide4GetStarted() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(CyberRedContainer)
                .border(BorderStroke(2.dp, CyberRedBright), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = CyberRedBright,
                modifier = Modifier.size(46.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "शुरू करते हैं!",
            fontFamily = FontFamily.SansSerif,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = CyberTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "READY FOR SETUP & ACTIVATION",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = CyberRedBright,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = CyberCard),
            border = BorderStroke(1.dp, CyberRedBorder)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "सहनाज को आपके फोन से कनेक्ट करने के लिए कुछ ज़रूरी अनुमतियों (Permissions) की आवश्यकता होगी।",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.5.sp,
                    color = CyberTextSecondary,
                    lineHeight = 19.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "100% सुरक्षित और ऑन-डिवाइस प्राइवेसी",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "कोई पर्सनल डेटा स्टोर नहीं होता",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun ExampleVoiceCommandCard(
    icon: ImageVector,
    commandText: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberRedBorder.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberRedContainer)
                    .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(4.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberRedBright,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commandText,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
                Text(
                    text = subtitle,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = CyberTextMuted
                )
            }
        }
    }
}

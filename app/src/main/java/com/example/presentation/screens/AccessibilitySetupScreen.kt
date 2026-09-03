package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.SetupViewModel
import com.example.ui.theme.CyberAmber
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

@Composable
fun AccessibilitySetupScreen(
    setupViewModel: SetupViewModel,
    onCompleteSetup: () -> Unit
) {
    val context = LocalContext.current
    val permissionStatus by setupViewModel.permissions.collectAsState()

    LaunchedEffect(Unit) {
        setupViewModel.refreshPermissions()
    }

    Scaffold(
        containerColor = CyberBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Cyber Shield Icon Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberRedContainer)
                    .border(BorderStroke(1.5.dp, CyberRedBright), RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.AccessibilityNew,
                    contentDescription = null,
                    tint = CyberRedBright,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = "ACCESSIBILITY AUTOMATION BUS",
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = CyberTextPrimary
            )

            Text(
                text = "${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} USES ANDROID ACCESSIBILITY TO AUTOMATE SCREEN ACTIONS (TAP, SCROLL, APP LAUNCH) SEAMLESSLY VIA VOICE.",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                color = CyberTextSecondary
            )

            // Setup Steps Card
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ACTIVATION PROTOCOL",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberRedBright
                    )

                    CyberSetupStep(step = "01", text = "TAP 'OPEN ACCESSIBILITY SETTINGS' BELOW.")
                    CyberSetupStep(step = "02", text = "FIND '${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} ASSISTANT' IN DOWNLOADED/INSTALLED APPS LIST.")
                    CyberSetupStep(step = "03", text = "TOGGLE SWITCH ON AND CONFIRM SYSTEM PERMISSION.")
                }
            }

            // Android 13+ Restricted Settings Helper Banner / Tooltip Guide
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = CyberAmber.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, CyberAmber),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "RESTRICTED SETTINGS HELPER (ANDROID 13+)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberAmber
                        )
                    }

                    Text(
                        text = "Agar '${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} ASSISTANT' accessibility list mein GREY / RESTRICTED dikhe aur tap na ho paye, to ye 3 simple steps follow karein:",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = CyberTextPrimary,
                        lineHeight = 15.sp
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyberBlack.copy(alpha = 0.6f))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "1. Phone Settings → Apps → ${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} par jayein (ya neeche button tap karein).",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 10.sp,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = "2. Top right corner mein 3-dots menu (⋮) par tap karein.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = CyberTextSecondary
                        )
                        Text(
                            text = "3. 'Allow restricted settings' par tap karein, phir wapas aakar Accessibility on karein.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberAmber
                        )
                    }

                    // Direct App Info button for convenience
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, CyberAmber),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = CyberAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OPEN APP INFO (FOR 3-DOT UNLOCK)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberAmber
                        )
                    }
                }
            }

            // Security Notice Card
            Card(
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberRedBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CyberGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "PRIVACY & SECURITY ASSURANCE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                        Text(
                            text = "ALL ACTIONS ARE EXECUTED ON-DEVICE. ZERO PERSONAL DATA LEAKS.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Action Buttons
            Button(
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = "OPEN ACCESSIBILITY SETTINGS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            OutlinedButton(
                onClick = onCompleteSetup,
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, CyberRedBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "PROCEED TO DASHBOARD",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
            }
        }
    }
}

@Composable
private fun CyberSetupStep(step: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CyberRedContainer)
                .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(4.dp))
        ) {
            Text(
                text = step,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = CyberRedBright
            )
        }
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = CyberTextSecondary
        )
    }
}

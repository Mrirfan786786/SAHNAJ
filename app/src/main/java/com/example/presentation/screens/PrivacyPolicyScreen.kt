package com.example.presentation.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PRIVACY & SECURITY MATRIX",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = CyberTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "BACK",
                            tint = CyberRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                )
            )
        },
        containerColor = CyberBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Security Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.5.dp, CyberRed)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberRedContainer)
                            .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(14.dp))
                    Column {
                        Text(
                            text = "SECURITY-FIRST ARCHITECTURE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "ON-DEVICE EXECUTION WITH STRICT ACCESS CONTROLS.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            CyberPrivacySectionCard(
                icon = Icons.Default.Lock,
                title = "ACCESSIBILITY BUS INTEGRITY",
                content = "${com.example.domain.personality.PersonalityResponses.ASSISTANT_NAME_DISPLAY} USES ANDROID ACCESSIBILITY STRICTLY TO EXECUTE USER-REQUESTED AUTOMATION (BACK, HOME, RECENTS, SCROLL, AND LAUNCH). NEVER LOGS PASSWORDS, BANK DETAILS, OR SNOOPS PRIVATE SCREEN DATA."
            )

            CyberPrivacySectionCard(
                icon = Icons.Default.Security,
                title = "HIGH-RISK CONFIRMATION GUARDRAILS",
                content = "OUTGOING CALLS, SMS DISPATCH, AND SYSTEM SETTINGS ALTERATIONS STRICTLY REQUIRE USER VERBAL CONFIRMATION BEFORE EXECUTION."
            )

            CyberPrivacySectionCard(
                icon = Icons.Default.CheckCircle,
                title = "100% LOCAL DEVICE PRIVACY",
                content = "ZERO CLOUD ACCOUNT OBLIGATIONS. PREFERENCES AND PROFILES REMAIN ISOLATED ON-DEVICE IN SECURE ENCRYPTED LOCAL STORAGE."
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CyberPrivacySectionCard(
    icon: ImageVector,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberRedBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CyberRedBright,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = CyberTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = CyberTextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

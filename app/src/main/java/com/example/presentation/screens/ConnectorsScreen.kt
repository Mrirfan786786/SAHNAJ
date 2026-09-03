package com.example.presentation.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
fun ConnectorsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isWhatsAppInstalled by remember { mutableStateOf(checkIsWhatsAppInstalled(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Connectors",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "कनेक्टर्स • App & Voice Integrations",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("connectors_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
            // Header Info Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connectors_info_card"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B101D),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cable,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "App Connectors",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "सहनाज को अन्य ऐप्स और सेवाओं के साथ वॉइस कमांड से जोड़ें।",
                            fontSize = 11.5.sp,
                            color = CyberTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // 1. WhatsApp Connector
            WhatsAppConnectorCard(
                isInstalled = isWhatsAppInstalled,
                onInstallClick = {
                    try {
                        val playStoreIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")
                        )
                        context.startActivity(playStoreIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Play Store नहीं खुल सका", Toast.LENGTH_SHORT).show()
                    }
                },
                onCardClick = {
                    if (isWhatsAppInstalled) {
                        Toast.makeText(
                            context,
                            "WhatsApp कनेक्टर सक्रिय है। आप 'WhatsApp पर मैसेज भेजो' बोल सकते हैं।",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "WhatsApp इंस्टॉल नहीं है। कृपया Play Store से इंस्टॉल करें।",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            // 2. Phone / Contacts Connector
            ConnectorItemCard(
                icon = Icons.Default.Call,
                iconTint = Color(0xFF30D158),
                iconBg = Color(0xFF0E2E18),
                title = "Phone / Contacts",
                hindiTitle = "फोन और कॉन्टैक्ट्स",
                description = "Make calls and manage contacts",
                isConnected = true,
                statusLabel = "Connected",
                badgeColor = CyberGreen,
                onClick = {
                    Toast.makeText(
                        context,
                        "Phone / Contacts कनेक्टर सक्रिय है। (कॉल और संपर्क प्रबंधित करें)",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                testTag = "connector_phone_contacts_card"
            )

            // 3. Calendar Connector (Placeholder for future)
            ConnectorItemCard(
                icon = Icons.Default.DateRange,
                iconTint = Color(0xFF007AFF),
                iconBg = Color(0xFF0C2442),
                title = "Calendar",
                hindiTitle = "कैलेंडर",
                description = "Create and check events via voice",
                isConnected = false,
                statusLabel = "Not Connected",
                badgeColor = CyberTextMuted,
                onClick = {
                    Toast.makeText(context, "यह फीचर जल्द आ रहा है (Coming Soon)", Toast.LENGTH_SHORT).show()
                },
                testTag = "connector_calendar_card"
            )

            // 4. Notes / Reminders Connector (Placeholder for future)
            ConnectorItemCard(
                icon = Icons.Default.Edit,
                iconTint = Color(0xFFFF9F0A),
                iconBg = Color(0xFF332007),
                title = "Notes & Reminders",
                hindiTitle = "नोट्स और रिमाइंडर",
                description = "Save quick notes and reminders via voice",
                isConnected = false,
                statusLabel = "Not Connected",
                badgeColor = CyberTextMuted,
                onClick = {
                    Toast.makeText(context, "यह फीचर जल्द आ रहा है (Coming Soon)", Toast.LENGTH_SHORT).show()
                },
                testTag = "connector_notes_reminders_card"
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WhatsAppConnectorCard(
    isInstalled: Boolean,
    onInstallClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("connector_whatsapp_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(
            1.dp,
            if (isInstalled) CyberRedBorder else CyberRedBright
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F311C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Message,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "WhatsApp",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "व्हाट्सऐप",
                                fontSize = 11.5.sp,
                                color = CyberRedBright
                            )
                        }
                        Text(
                            text = "Send and read messages via voice commands",
                            fontSize = 12.sp,
                            color = CyberTextSecondary
                        )
                    }
                }

                // Status Badge
                StatusBadge(
                    text = if (isInstalled) "Connected" else "Not Installed",
                    badgeColor = if (isInstalled) CyberGreen else CyberRedBright,
                    isSuccess = isInstalled
                )
            }

            if (!isInstalled) {
                HorizontalDivider(color = Color(0xFF2A1C28))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "WhatsApp इंस्टॉल नहीं है",
                        fontSize = 11.5.sp,
                        color = CyberRedBright
                    )

                    Button(
                        onClick = onInstallClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("whatsapp_install_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Install",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectorItemCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    hindiTitle: String,
    description: String,
    isConnected: Boolean,
    statusLabel: String,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.dp, CyberRedBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = hindiTitle,
                            fontSize = 11.sp,
                            color = CyberTextMuted
                        )
                    }
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )
                }
            }

            // Status Badge
            StatusBadge(
                text = statusLabel,
                badgeColor = badgeColor,
                isSuccess = isConnected
            )
        }
    }
}

@Composable
private fun StatusBadge(
    text: String,
    badgeColor: Color,
    isSuccess: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
            )
            Text(
                text = text,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}

private fun checkIsWhatsAppInstalled(context: Context): Boolean {
    val pm = context.packageManager
    val packages = listOf("com.whatsapp", "com.whatsapp.w4b")
    for (pkg in packages) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }
            return true
        } catch (_: PackageManager.NameNotFoundException) {
        }
    }
    return false
}

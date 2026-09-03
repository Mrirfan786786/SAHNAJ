package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.viewmodel.PcConnectViewModel
import com.example.services.pcconnect.PcCommandLog
import com.example.services.pcconnect.PcConnectionStatus
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
fun PcConnectScreen(
    onNavigateBack: () -> Unit,
    viewModel: PcConnectViewModel = viewModel()
) {
    val context = LocalContext.current
    val pairingCode by viewModel.pairingCode.collectAsState()
    val localIp by viewModel.localIp.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val isServerRunning by viewModel.isServerRunning.collectAsState()
    val connectedClient by viewModel.connectedClient.collectAsState()
    val commandLogs by viewModel.commandLogs.collectAsState()
    val port = viewModel.port

    val serverUrl = if (localIp != null) "http://$localIp:$port" else "Local IP not detected"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PC Connect",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "पीसी कनेक्ट • Local Wi-Fi Bridge",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("pc_connect_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberRedBright
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.refreshNetwork()
                            viewModel.refreshPairingCode()
                            Toast.makeText(context, "Network & Code Refreshed", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("pc_connect_refresh_button")
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Explanation Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pc_connect_explanation_card"),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(CyberRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "PC Remote Control",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "अपने फोन को PC से जोड़कर सहनाज को PC से भी control करें (लोकल नेटवर्क पर)।",
                            fontSize = 12.sp,
                            color = CyberTextSecondary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // 2. Wi-Fi Requirement Note
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pc_connect_wifi_note_card"),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF22111A),
                border = BorderStroke(1.dp, Color(0xFF4A1A2A))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFFF9F0A),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "यह feature सिर्फ same Wi-Fi network पर काम करता है।",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFD180)
                    )
                }
            }

            // 3. 6-Digit Pairing Code Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pc_connect_pairing_code_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.5.dp, CyberRedBright)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "PAIRING CODE / पेयरिंग कोड",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = CyberRedBright
                    )

                    // Big 6-digit text with letter spacing
                    val formattedCode = pairingCode.chunked(1).joinToString("  ")
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF140810),
                        border = BorderStroke(1.dp, CyberRedBorder),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = formattedCode,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                                .testTag("pc_connect_code_display"),
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "PC पर यह 6-अंकों का कोड दर्ज करें",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                val newCode = viewModel.pairingCode.value
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("SahNaj Pairing Code", newCode)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Pairing code copied: $newCode", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRedBright),
                            border = BorderStroke(1.dp, CyberRedBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Code", fontSize = 11.5.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.refreshPairingCode()
                                Toast.makeText(context, "New code generated", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextPrimary),
                            border = BorderStroke(1.dp, Color(0xFF3E2234)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Code", fontSize = 11.5.sp)
                        }
                    }
                }
            }

            // 4. Connection Status & Local Server Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pc_connect_status_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (connectedClient != null) Color(0xFF0F2416) else CyberCard
                ),
                border = BorderStroke(
                    1.dp,
                    if (connectedClient != null) CyberGreen else CyberRedBorder
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
                        Text(
                            text = "STATUS / स्थिति",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (connectedClient != null) CyberGreen else CyberTextSecondary
                        )

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (connectedClient != null) CyberGreen.copy(alpha = 0.15f) else Color(0xFFFF9500).copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                if (connectedClient != null) CyberGreen else Color(0xFFFF9500)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (connectedClient != null) CyberGreen else Color(0xFFFF9500))
                                )
                                Text(
                                    text = if (connectedClient != null) "Connected" else "Waiting for PC",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (connectedClient != null) CyberGreen else Color(0xFFFF9500)
                                )
                            }
                        }
                    }

                    if (connectedClient != null) {
                        val client = connectedClient!!
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "✅ Connected to ${client.clientName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Client IP: ${client.clientIp} • Connected at ${client.connectedAt}",
                                fontSize = 12.sp,
                                color = CyberTextSecondary
                            )
                        }

                        // Disconnect Button
                        Button(
                            onClick = {
                                viewModel.disconnectClient()
                                Toast.makeText(context, "PC disconnected", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("pc_connect_disconnect_button")
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disconnect", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "प्रतीक्षा में... (Waiting for PC)",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD180)
                            )
                            Text(
                                text = "अपने PC के ब्राउज़र में खोलें या PC client से कनेक्ट करें:",
                                fontSize = 12.sp,
                                color = CyberTextSecondary
                            )
                        }

                        // Server Address Display Box
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0F050B),
                            border = BorderStroke(1.dp, Color(0xFF331627))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (localIp != null) Icons.Default.Wifi else Icons.Default.WifiOff,
                                        contentDescription = null,
                                        tint = if (localIp != null) CyberGreen else CyberRedBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = serverUrl,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberTextPrimary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (localIp != null) {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("SahNaj PC Bridge URL", serverUrl)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "URL Copied: $serverUrl", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy URL",
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Activity Log / Live Command Stream Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pc_connect_activity_log_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "LIVE COMMAND STREAM / कमांड स्ट्रीम",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        }

                        if (commandLogs.isNotEmpty()) {
                            Text(
                                text = "${commandLogs.size} events",
                                fontSize = 10.5.sp,
                                color = CyberTextMuted
                            )
                        }
                    }

                    if (commandLogs.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF080206),
                            border = BorderStroke(1.dp, Color(0xFF22111E)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "> Server listening on port $port...",
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberGreen
                                )
                                Text(
                                    text = "> Waiting for pairing and commands from PC.",
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = CyberTextMuted
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            commandLogs.take(8).forEach { log ->
                                LogEntryItem(log)
                            }
                        }
                    }
                }
            }

            // 6. Quick Instructions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pc_connect_instructions_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF130911)),
                border = BorderStroke(1.dp, Color(0xFF2A1524))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "HOW TO CONNECT / कनेक्ट कैसे करें:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRedBright
                    )

                    InstructionStep(number = "1", text = "Ensure phone and PC are connected to the same Wi-Fi.")
                    InstructionStep(number = "2", text = "Open browser on PC and visit $serverUrl")
                    InstructionStep(number = "3", text = "Enter the 6-digit code '$pairingCode' and click Connect.")
                    InstructionStep(number = "4", text = "Type commands on PC (e.g. 'open youtube') to execute on phone.")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LogEntryItem(log: PcCommandLog) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0D050B),
        border = BorderStroke(1.dp, if (log.success) Color(0xFF1B3D23) else Color(0xFF4A1A24)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PC > ${log.command}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64D2FF)
                )
                Text(
                    text = log.timestamp,
                    fontSize = 10.sp,
                    color = CyberTextMuted
                )
            }

            Text(
                text = "Phone > ${log.response}",
                fontSize = 11.5.sp,
                fontFamily = FontFamily.Monospace,
                color = if (log.success) CyberGreen else CyberRedBright
            )
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = CyberRedContainer,
            modifier = Modifier.size(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright
                )
            }
        }
        Text(
            text = text,
            fontSize = 11.5.sp,
            color = CyberTextSecondary,
            lineHeight = 16.sp
        )
    }
}

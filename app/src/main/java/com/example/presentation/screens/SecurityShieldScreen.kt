package com.example.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SahNajApplication
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
import com.example.util.SecurityShieldManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityShieldScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as? SahNajApplication

    val isFakeShutdownActive by SecurityShieldManager.isFakeShutdownActive.collectAsState()
    val isSosActive by SecurityShieldManager.isSosActive.collectAsState()

    var emergencyContactNumber by remember { mutableStateOf("112") }
    var sosResultStatus by remember { mutableStateOf<String?>(null) }
    var isTriggeringSos by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions
    }

    if (isFakeShutdownActive) {
        // Full Fake Decoy Shutdown Blackout Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Screen Power Down (Stealth Protocol)",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { SecurityShieldManager.deactivateFakeShutdown() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color(0xFF222222))
                ) {
                    Text("Deactivate Stealth Decoy (Boss PIN)", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Security Shield & Emergency SOS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "सुरक्षा शील्ड, आपातकालीन एसओएस व कॉल ब्रिज",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(CyberRedContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Active Security Radar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "Autonomous intrusion protection, voice call announcer & emergency SOS broadcast",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            // 1. EMERGENCY SOS SECTION
            Text(
                text = "1. Emergency SOS Location Dispatch",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRedBright
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = emergencyContactNumber,
                        onValueChange = { emergencyContactNumber = it },
                        label = { Text("Emergency Contact Number or Police Helpline (Default: 112)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = CyberRedBorder
                        )
                    )

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.SEND_SMS
                                )
                            )
                            isTriggeringSos = true
                            scope.launch {
                                val result = SecurityShieldManager.triggerEmergencySos(
                                    context = context,
                                    emergencyNumber = emergencyContactNumber,
                                    userPreferences = app?.userPreferences
                                )
                                sosResultStatus = result
                                isTriggeringSos = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isTriggeringSos) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyberBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Broadcasting GPS SOS Coordinates...", color = CyberBlack, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = CyberBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DISPATCH EMERGENCY SOS NOW", color = CyberBlack, fontWeight = FontWeight.Bold)
                        }
                    }

                    AnimatedVisibility(visible = sosResultStatus != null) {
                        Text(
                            text = sosResultStatus ?: "",
                            fontSize = 13.sp,
                            color = CyberGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // 2. FAKE SHUTDOWN / INTRUSION DECOY
            Text(
                text = "2. Decoy / Fake Shutdown Mode",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRedBright
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Simulates an instant phone power off while SAHNAJ AI remains silently awake in the background, listening for wake-word authentication.",
                        fontSize = 13.sp,
                        color = CyberTextMuted
                    )

                    Button(
                        onClick = { SecurityShieldManager.activateFakeShutdown() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        border = BorderStroke(1.dp, CyberRedBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VisibilityOff, contentDescription = null, tint = CyberRedBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Engage Fake Stealth Shutdown", color = CyberTextPrimary)
                    }
                }
            }

            // 3. HANDS-FREE VOICE CALL ANNOUNCER
            Text(
                text = "3. Hands-Free Voice Call & WhatsApp Bridge",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRedBright
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Incoming Caller Voice Announcer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Speaks caller identity aloud through TTS and awaits voice reply ('Answer call' or 'Reject call')",
                                fontSize = 12.sp,
                                color = CyberTextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = CyberRedBorder)

                    Text(
                        text = "Supported Voice Commands:\n• 'Answer call' / 'Call pick karo' -> Picks up incoming call\n• 'Reject call' / 'Call cut karo' -> Disconnects incoming call\n• 'WhatsApp message to [Name] bolo [Message]' -> Types & dispatches hands-free",
                        fontSize = 12.sp,
                        color = CyberAmber,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

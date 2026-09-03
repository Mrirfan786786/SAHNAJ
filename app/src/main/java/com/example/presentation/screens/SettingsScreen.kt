package com.example.presentation.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.services.SahnajNotificationService
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.example.BuildConfig
import com.example.R
import com.example.domain.personality.PersonalityResponses
import com.example.permissions.PermissionManager
import com.example.presentation.components.SahNajBottomBar
import com.example.presentation.navigation.Screen
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
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
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    assistantViewModel: AssistantViewModel? = null,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = onNavigateBack,
    onNavigateToChat: () -> Unit = {},
    onNavigateToTriggers: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToIntelligenceModes: () -> Unit = {},
    onNavigateToVoiceAndAiModels: () -> Unit = {},
    onNavigateToOrbCustomization: () -> Unit = {},
    onNavigateToApiAndCloudSettings: () -> Unit = {},
    onNavigateToConnectors: () -> Unit = {},
    onNavigateToPcConnect: () -> Unit = {},
    onNavigateToLicenseActivation: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToVoiceAuthentication: () -> Unit = {},
    onNavigateToBatchUpdate: () -> Unit = {},
    onNavigateToVoiceConsole: () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }

    val wakeWordEnabled by settingsViewModel.wakeWordEnabled.collectAsState()
    val speechRate by settingsViewModel.speechRate.collectAsState()
    val language by settingsViewModel.language.collectAsState()
    val confirmationMode by settingsViewModel.confirmationMode.collectAsState()
    val callAssistantEnabled by settingsViewModel.callAssistantEnabled.collectAsState()
    val chatNotificationsEnabled by settingsViewModel.chatNotificationsEnabled.collectAsState()

    var showVoiceDialog by remember { mutableStateOf(false) }
    var showOrbDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showConnectorsDialog by remember { mutableStateOf(false) }
    var showIntelligenceDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }

    var tempApiKey by remember { mutableStateOf(settingsViewModel.getGeminiApiKey()) }
    var currentSliderRate by remember(speechRate) { mutableFloatStateOf(speechRate) }

    var isNotificationAccessGranted by remember {
        mutableStateOf(SahnajNotificationService.isNotificationAccessGranted(context))
    }

    // Refresh notification access status on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isNotificationAccessGranted = SahnajNotificationService.isNotificationAccessGranted(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistantViewModel?.startListening()
            onNavigateToHome()
        }
    }

    // Modal: Voice & AI Models Dialog
    if (showVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showVoiceDialog = false },
            title = {
                Text("Voice & AI Models", fontWeight = FontWeight.Bold, color = CyberTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Primary AI Language", fontSize = 13.sp, color = CyberTextMuted)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("HINGLISH", "HINDI", "ENGLISH").forEach { lang ->
                            val isSelected = language.equals(lang, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { settingsViewModel.updateLanguage(lang) },
                                label = { Text(lang, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberRedBright,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF231C2C),
                                    labelColor = CyberTextSecondary
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF32263D))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Voice Persona", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                            Text("Young Female (1.10x Pitch)", fontSize = 11.sp, color = CyberRedBright)
                        }
                        Button(
                            onClick = { settingsViewModel.previewFemaleVoice() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedContainer),
                            border = BorderStroke(1.dp, CyberRedBright),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Test Voice", fontSize = 11.sp, color = CyberRedBright)
                        }
                    }

                    Text("Speech Rate (${String.format("%.2fx", currentSliderRate)})", fontSize = 12.sp, color = CyberTextMuted)
                    Slider(
                        value = currentSliderRate,
                        onValueChange = { currentSliderRate = it },
                        onValueChangeFinished = { settingsViewModel.updateSpeechRate(currentSliderRate) },
                        valueRange = 0.6f..1.6f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberRedBright,
                            activeTrackColor = CyberRedBright,
                            inactiveTrackColor = Color(0xFF282030)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showVoiceDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("Done", color = Color.White)
                }
            },
            containerColor = Color(0xFF17111E)
        )
    }

    // Modal: API & Cloud Settings Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text("API & Cloud Settings", fontWeight = FontWeight.Bold, color = CyberTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your Google Gemini API key to power ${PersonalityResponses.ASSISTANT_NAME_DISPLAY}'s deep reasoning engine.",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it.trim() },
                        placeholder = { Text("AIzaSy...", color = CyberTextMuted) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = Color(0xFF38263A),
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.setGeminiApiKey(tempApiKey)
                        showApiKeyDialog = false
                        Toast.makeText(context, "API Key saved successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("Save Key", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel", color = CyberTextMuted)
                }
            },
            containerColor = Color(0xFF17111E)
        )
    }

    // Modal: Account & Clear Data Dialog
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = {
                Text("Account Options", fontWeight = FontWeight.Bold, color = CyberRedBright)
            },
            text = {
                Text(
                    text = "Manage your session or wipe AI memory & remembered preferences.",
                    fontSize = 12.5.sp,
                    color = CyberTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.clearAllMemories()
                        showAccountDialog = false
                        Toast.makeText(context, "AI Memory cleared!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("Clear AI Memory", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Cancel", color = CyberTextMuted)
                }
            },
            containerColor = Color(0xFF17111E)
        )
    }

    // Modal: Avatar Customization
    if (showOrbDialog) {
        AlertDialog(
            onDismissRequest = { showOrbDialog = false },
            title = { Text("Avatar Customization", fontWeight = FontWeight.Bold, color = CyberTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("17-Year-Old Cyber Girl Avatar: Sahnaj", fontSize = 12.5.sp, color = CyberRedBright)
                    Text("Your Avatar is calibrated with dynamic breathing, glowing eye pulse, lip-sync, and cyber holographic aura rings.", fontSize = 12.sp, color = CyberTextMuted)
                }
            },
            confirmButton = {
                Button(onClick = { showOrbDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = CyberRed)) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = Color(0xFF17111E)
        )
    }

    Scaffold(
        containerColor = CyberBlack,
        bottomBar = {
            SahNajBottomBar(
                currentRoute = Screen.Settings.route,
                onNavigate = { route ->
                    when (route) {
                        Screen.Dashboard.route -> onNavigateToHome()
                        Screen.Chat.route -> onNavigateToChat()
                        Screen.Triggers.route -> onNavigateToTriggers()
                        else -> {}
                    }
                },
                onOrbClick = {
                    if (permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                        assistantViewModel?.startListening()
                        onNavigateToHome()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Header Banner matching MYRA reference
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = PersonalityResponses.ASSISTANT_NAME_DISPLAY.uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberRedBright,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Your Intelligent AI Assistant",
                        fontSize = 13.5.sp,
                        color = CyberTextMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                    Text(
                        text = "Customize your ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} experience",
                        fontSize = 12.sp,
                        color = CyberTextMuted
                    )
                }

                // Assistant Character Avatar Hero Image / Graphic on top right
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF2A4D),
                                    Color(0xFF7A0F26),
                                    Color(0xFF1E040B)
                                )
                            )
                        )
                        .border(2.dp, CyberRedBright, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.creator_photo),
                        contentDescription = "Assistant Persona",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Categorized List of Settings matching Screenshots
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 0. Default Digital Assistant (System Level Integration)
                SettingCategoryItem(
                    icon = Icons.Default.AutoAwesome,
                    badgeColor = CyberRedBright,
                    title = "Set as Default Assistant",
                    subtitle = "Home button long-press ya gesture par SahNaj ko direct activate karne ke liye yahan se select karein",
                    onClick = {
                        openDefaultAssistantSettings(context)
                    }
                )

                // 1. Voice & AI Models
                SettingCategoryItem(
                    icon = Icons.Default.AutoAwesome,
                    badgeColor = Color(0xFF9B51E0),
                    title = "50-Voice Studio Engine",
                    subtitle = "50 वॉइस स्टूडियो • 25 Female + 25 Male Neural Voices with Instant Preview & Pitch Tuning",
                    onClick = onNavigateToVoiceAndAiModels
                )

                // 2. Avatar Customization
                SettingCategoryItem(
                    icon = Icons.Default.Palette,
                    badgeColor = Color(0xFFFF2D55),
                    title = "Avatar Customization",
                    subtitle = "अवतार कस्टमाइज़ेशन • 17-year-old Cyber Girl themes, holographic aura & size",
                    onClick = onNavigateToOrbCustomization
                )

                // 3. API & Cloud Settings
                SettingCategoryItem(
                    icon = Icons.Default.Cloud,
                    badgeColor = Color(0xFF00C6FF),
                    title = "API & Cloud Settings",
                    subtitle = "API और क्लाउड सेटिंग्स • Gemini API Key, Firebase Cloud Sync & Security",
                    onClick = onNavigateToApiAndCloudSettings
                )

                // 3.1 Autonomous Offline Voice Console
                SettingCategoryItem(
                    icon = Icons.Default.Mic,
                    badgeColor = Color(0xFF00E5FF),
                    title = "Offline Voice Console",
                    subtitle = "ऑफलाइन वॉइस कंसोल • Zero-API offline commands, local memory & device intents",
                    onClick = onNavigateToVoiceConsole
                )

                // 4. Connectors
                SettingCategoryItem(
                    icon = Icons.Default.Cable,
                    badgeColor = Color(0xFFFF9F0A),
                    title = "Connectors",
                    subtitle = "कनेक्टर्स • Connect ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} with WhatsApp, Phone, & Apps",
                    onClick = onNavigateToConnectors
                )

                // 5. Permissions
                SettingCategoryItem(
                    icon = Icons.Default.Security,
                    badgeColor = Color(0xFF00FF9D),
                    title = "Permissions",
                    subtitle = "अनुमतियाँ • View and enable all required permissions",
                    onClick = onNavigateToPermissions
                )

                // 6. Voice Authentication & Voice Guardian
                SettingCategoryItem(
                    icon = Icons.Default.Fingerprint,
                    badgeColor = CyberRedBright,
                    title = "Voice Guardian",
                    subtitle = "बायोमेट्रिक वॉइस शील्ड • Master Voice Lock, Away Guard, & Biometrics",
                    onClick = onNavigateToVoiceAuthentication
                )

                // 7. Wake Word
                SettingCategoryItem(
                    icon = Icons.Default.Mic,
                    badgeColor = Color(0xFFFF3B30),
                    title = "Wake Word",
                    subtitle = "Customize how you start ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} (Trigger: '${PersonalityResponses.ASSISTANT_NAME_DISPLAY}')",
                    trailing = {
                        Switch(
                            checked = wakeWordEnabled,
                            onCheckedChange = { settingsViewModel.toggleWakeWord(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright
                            )
                        )
                    },
                    onClick = { settingsViewModel.toggleWakeWord(!wakeWordEnabled) }
                )

                // 8. Intelligence & Modes
                SettingCategoryItem(
                    icon = Icons.Default.Psychology,
                    badgeColor = Color(0xFF007AFF),
                    title = "Intelligence & Modes",
                    subtitle = "ऑपरेटिंग मोड्स • JARVIS Autonomous, Technician, Stealth & Screen Reader",
                    onClick = onNavigateToIntelligenceModes
                )

                // 9. Call Assistant
                SettingCategoryItem(
                    icon = Icons.Default.Call,
                    badgeColor = Color(0xFF30D158),
                    title = "Call Assistant",
                    subtitle = "Announce incoming calls and control them using your voice",
                    trailing = {
                        Switch(
                            checked = callAssistantEnabled,
                            onCheckedChange = { settingsViewModel.toggleCallAssistant(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright
                            )
                        )
                    },
                    onClick = { settingsViewModel.toggleCallAssistant(!callAssistantEnabled) }
                )

                // 10. Chat Notifications
                SettingCategoryItem(
                    icon = Icons.Default.Notifications,
                    badgeColor = Color(0xFFAF52DE),
                    title = "Chat Notifications",
                    subtitle = "Get notified about new chat messages",
                    trailing = {
                        Switch(
                            checked = chatNotificationsEnabled,
                            onCheckedChange = { settingsViewModel.toggleChatNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright
                            )
                        )
                    },
                    onClick = { settingsViewModel.toggleChatNotifications(!chatNotificationsEnabled) }
                )

                // 11. Notification Access (WhatsApp, Calls & Alerts)
                SettingCategoryItem(
                    icon = Icons.Default.NotificationsActive,
                    badgeColor = if (isNotificationAccessGranted) Color(0xFF34C759) else Color(0xFFFF2D55),
                    title = if (isNotificationAccessGranted) "Notification Access (Active)" else "Enable Notification Access",
                    subtitle = if (isNotificationAccessGranted) "सूचना एक्सेस सक्रिय • WhatsApp & Call notifications will be processed by SAHNAJ AI" else "सूचना एक्सेस चालू करें • Read & respond to WhatsApp messages, Calls & Alerts",
                    trailing = {
                        Button(
                            onClick = {
                                openNotificationAccessSettings(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNotificationAccessGranted) Color(0xFF1B3820) else CyberRedContainer
                            ),
                            border = BorderStroke(1.dp, if (isNotificationAccessGranted) Color(0xFF34C759) else CyberRedBright),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isNotificationAccessGranted) "Active" else "Enable",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNotificationAccessGranted) Color(0xFF34C759) else CyberRedBright
                            )
                        }
                    },
                    onClick = {
                        openNotificationAccessSettings(context)
                    }
                )

                // 12. PC Connect
                SettingCategoryItem(
                    icon = Icons.Default.Computer,
                    badgeColor = Color(0xFFFF9500),
                    title = "PC Connect",
                    subtitle = "पीसी कनेक्ट • Remote voice & command bridge on local Wi-Fi",
                    onClick = onNavigateToPcConnect
                )

                // 12. License Activation
                SettingCategoryItem(
                    icon = Icons.Default.Key,
                    badgeColor = Color(0xFFFFCC00),
                    title = "License Activation",
                    subtitle = "लाइसेंस एक्टिवेशन • Enter a license key bought from the website",
                    onClick = onNavigateToLicenseActivation
                )

                // 13. Subscription
                SettingCategoryItem(
                    icon = Icons.Default.Star,
                    badgeColor = Color(0xFFFF375F),
                    title = "Subscription",
                    subtitle = "सब्सक्रिप्शन • Plans & pricing",
                    onClick = onNavigateToSubscription
                )

                // 14. User Profile
                SettingCategoryItem(
                    icon = Icons.Default.AccountCircle,
                    badgeColor = Color(0xFF64D2FF),
                    title = "User Profile",
                    subtitle = "यूज़र प्रोफ़ाइल • Profile info, language & name preferences",
                    onClick = onNavigateToProfile
                )

                // 15. Batch / Update
                SettingCategoryItem(
                    icon = Icons.Default.Sync,
                    badgeColor = Color(0xFF34C759),
                    title = "Batch / Update",
                    subtitle = "App Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                    onClick = onNavigateToBatchUpdate
                )

                // 16. Account (Logout)
                SettingCategoryItem(
                    icon = Icons.Default.Logout,
                    badgeColor = Color(0xFFFF453A),
                    title = "Account",
                    subtitle = "Log out or clear AI memory",
                    onClick = { showAccountDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Bottom Floating Pill: "Double Tap" action capsule (as in screenshot)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF221A28))
                        .border(1.dp, Color(0xFF362840), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Double Tap",
                            tint = CyberRedBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Double Tap any item for quick assist",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Founder & Chief Architect Branding Card
            BrandingCard()

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun BrandingCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF140E18)),
        border = BorderStroke(1.dp, Color(0xFFFF1E27))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MUHAMMAD IRFAN ALAM",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "FOUNDER & CHIEF ARCHITECT // SAHNAJ AI CORE",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                color = Color(0xFFFF3344),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingCategoryItem(
    icon: ImageVector,
    badgeColor: Color,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
        border = BorderStroke(1.dp, Color(0xFF261824))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Colored Badge Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = CyberTextMuted,
                    lineHeight = 15.sp
                )
            }

            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF4A3E54),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

fun openDefaultAssistantSettings(context: Context) {
    val intents = listOf(
        Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
        Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
        Intent("android.settings.VOICE_INPUT_SETTINGS"),
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    )

    for (intent in intents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (_: Exception) {}
    }
    Toast.makeText(context, "Settings > Apps > Default apps > Digital assistant app mein SahNaj select karein", Toast.LENGTH_LONG).show()
}

fun openNotificationAccessSettings(context: Context) {
    try {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "Please enable Notification Access in Android Settings", Toast.LENGTH_LONG).show()
        }
    }
}


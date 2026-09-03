package com.example.presentation.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.permissions.PermissionManager
import com.example.permissions.PermissionStatus
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
fun PermissionsSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val permissionManager = remember { PermissionManager(context) }
    var permissionStatus by remember { mutableStateOf(permissionManager.getPermissionStatus()) }

    fun refreshStatus() {
        permissionStatus = permissionManager.getPermissionStatus()
    }

    // Auto-refresh permission status whenever user returns to screen (onResume)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val singlePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        refreshStatus()
    }

    val multiplePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshStatus()
    }

    val totalCount = 9
    var grantedCount = 0
    if (permissionStatus.hasMic) grantedCount++
    if (permissionStatus.hasContacts) grantedCount++
    if (permissionStatus.hasPhone) grantedCount++
    if (permissionStatus.hasSms) grantedCount++
    if (permissionStatus.hasNotifications) grantedCount++
    if (permissionStatus.hasAccessibility) grantedCount++
    if (permissionStatus.hasBatteryOptimization) grantedCount++
    if (permissionStatus.hasOverlay) grantedCount++
    if (permissionStatus.hasNotificationListener) grantedCount++

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Permissions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "अनुमतियाँ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = CyberTextMuted
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
                actions = {
                    IconButton(onClick = { refreshStatus() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CyberRedBright
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Note Banner Card at the top
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberRedContainer)
                                    .border(BorderStroke(1.dp, CyberRed), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "SYSTEM INTEGRITY",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRedBright
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (grantedCount == totalCount) CyberGreen.copy(alpha = 0.2f) else CyberRedContainer)
                                .border(
                                    BorderStroke(1.dp, if (grantedCount == totalCount) CyberGreen else CyberRedBorder),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (grantedCount == totalCount) "ALL GRANTED" else "$grantedCount / $totalCount GRANTED",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (grantedCount == totalCount) CyberGreen else CyberRedBright
                            )
                        }
                    }

                    Text(
                        text = "SAHNAJ को सही तरीके से काम करने के लिए ये सभी permissions ज़रूरी हैं।",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberTextPrimary,
                        lineHeight = 19.sp
                    )
                }
            }

            // 1. Microphone
            PermissionDetailCard(
                icon = Icons.Default.Mic,
                title = "Microphone",
                hindiTitle = "माइक्रोफ़ोन",
                description = "आपकी आवाज़ सुनकर कमांड्स और प्रश्नों को समझने के लिए।",
                isGranted = permissionStatus.hasMic,
                onEnable = {
                    singlePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )

            // 2. Contacts
            PermissionDetailCard(
                icon = Icons.Default.Contacts,
                title = "Contacts",
                hindiTitle = "कॉन्टैक्ट्स",
                description = "नाम बोलकर तुरंत कॉल लगाने या मैसेज भेजने के लिए।",
                isGranted = permissionStatus.hasContacts,
                onEnable = {
                    singlePermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            )

            // 3. Phone / Call Logs
            PermissionDetailCard(
                icon = Icons.Default.Call,
                title = "Phone / Call Logs",
                hindiTitle = "फ़ोन व कॉल लॉग्स",
                description = "डायरेक्ट वॉइस कमांड से कॉल कनेक्ट करने के लिए।",
                isGranted = permissionStatus.hasPhone,
                onEnable = {
                    singlePermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                }
            )

            // 4. SMS Messages
            PermissionDetailCard(
                icon = Icons.AutoMirrored.Filled.Message,
                title = "SMS",
                hindiTitle = "एसएमएस संदेश",
                description = "आवाज़ से सीधे टेक्स्ट और एसएमएस संदेश भेजने के लिए।",
                isGranted = permissionStatus.hasSms,
                onEnable = {
                    singlePermissionLauncher.launch(Manifest.permission.SEND_SMS)
                }
            )

            // 5. Notifications
            PermissionDetailCard(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                hindiTitle = "नोटिफिकेशन",
                description = "बैकग्राउंड वॉइस लिसनर और जरूरी अलर्ट्स हमेशा सक्रिय रखने के लिए।",
                isGranted = permissionStatus.hasNotifications,
                onEnable = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        singlePermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        try {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            openAppSettings(context)
                        }
                    }
                }
            )

            // 6. Accessibility Service
            PermissionDetailCard(
                icon = Icons.Default.TouchApp,
                title = "Accessibility Service",
                hindiTitle = "एक्सेसिबिलिटी सर्विस",
                description = "आपकी आवाज़ पर स्क्रीन टच, स्वाइप, बैक, होम जाना और ऐप्स ऑटोमेट करने के लिए।",
                instructionText = "Accessibility Settings में 'Downloaded apps' या 'Installed services' में 'SAHNAJ' को खोजें और चालू करें।",
                isGranted = permissionStatus.hasAccessibility,
                onEnable = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Accessibility settings open karein", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // 7. Battery Optimization Exemption
            PermissionDetailCard(
                icon = Icons.Default.BatteryChargingFull,
                title = "Battery Optimization Exemption",
                hindiTitle = "बैटरी ऑप्टिमाइज़ेशन छूट",
                description = "बैकग्राउंड में बिना रुके हमेशा सक्रिय रहने और वेकवर्ड डिटेक्ट करने के लिए।",
                instructionText = "सहनाज को बिना किसी रुकावट के चलाने के लिए बैटरी ऑप्टिमाइज़ेशन से छूट दें।",
                isGranted = permissionStatus.hasBatteryOptimization,
                onEnable = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(fallbackIntent)
                        } catch (_: Exception) {
                            openAppSettings(context)
                        }
                    }
                }
            )

            // 8. Display Over Other Apps
            PermissionDetailCard(
                icon = Icons.Default.Layers,
                title = "Display Over Other Apps",
                hindiTitle = "अन्य ऐप्स के ऊपर डिस्प्ले",
                description = "किसी भी स्क्रीन पर फ्लोटिंग असिस्टेंट और वॉइस ऑर्ब पॉपअप करने के लिए।",
                instructionText = "फ्लोटिंग असिस्टेंट और वॉइस इंटरफ़ेस प्रदर्शित करने के लिए इस अनुमति को चालू करें।",
                isGranted = permissionStatus.hasOverlay,
                onEnable = {
                    try {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        openAppSettings(context)
                    }
                }
            )

            // 9. Notification Listener Access (WhatsApp, Calls & Alerts)
            PermissionDetailCard(
                icon = Icons.Default.Notifications,
                title = "Notification Access",
                hindiTitle = "सूचना एक्सेस (व्हाट्सएप/कॉल/अलर्ट्स)",
                description = "व्हाट्सएप संदेश, इनकमिंग कॉल्स और ज़रूरी अलर्ट्स को पढ़कर वॉइस असिस्टेंट द्वारा सुनाने के लिए।",
                instructionText = "Notification Access Settings में 'SAHNAJ AI' को ढूंढें और एक्सेस की अनुमति दें।",
                isGranted = permissionStatus.hasNotificationListener,
                onEnable = {
                    com.example.services.SahnajNotificationService.openNotificationAccessSettings(context)
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // App Settings Shortcut
            OutlinedButton(
                onClick = { openAppSettings(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberRedBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextPrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Open App Settings (ऐप सेटिंग्स खोलें)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PermissionDetailCard(
    icon: ImageVector,
    title: String,
    hindiTitle: String,
    description: String,
    instructionText: String? = null,
    isGranted: Boolean,
    onEnable: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) CyberCard else CyberCard
        ),
        border = BorderStroke(
            1.dp,
            if (isGranted) CyberGreen.copy(alpha = 0.5f) else CyberRedBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Icon + Title + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isGranted) CyberGreen.copy(alpha = 0.15f) else CyberRedContainer)
                        .border(
                            BorderStroke(1.dp, if (isGranted) CyberGreen else CyberRed),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) CyberGreen else CyberRedBright,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                    Text(
                        text = hindiTitle,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = CyberTextMuted
                    )
                }

                // Current Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGranted) CyberGreen.copy(alpha = 0.15f) else CyberRedContainer)
                        .border(
                            BorderStroke(1.dp, if (isGranted) CyberGreen.copy(alpha = 0.6f) else CyberRedBorder),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (isGranted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Granted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Not Granted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright
                        )
                    }
                }
            }

            // Description
            Text(
                text = description,
                fontSize = 12.5.sp,
                color = CyberTextSecondary,
                lineHeight = 17.sp
            )

            // If not granted, show instruction text (if available) + Enable button
            if (!isGranted) {
                if (!instructionText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF220808))
                            .border(BorderStroke(1.dp, CyberRedBorder.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = instructionText,
                            fontSize = 11.5.sp,
                            color = CyberTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Button(
                    onClick = onEnable,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRedBright
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Enable / चालू करें",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun openAppSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "App settings open karein", Toast.LENGTH_SHORT).show()
    }
}

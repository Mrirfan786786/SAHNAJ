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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SahNajApplication
import com.example.data.local.UserPreferences
import com.example.domain.personality.PersonalityResponses
import com.example.permissions.PermissionManager
import com.example.presentation.components.SahNajBottomBar
import com.example.presentation.navigation.Screen
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import org.json.JSONArray
import org.json.JSONObject

data class TriggerItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val isEnabled: Boolean = false
)

private fun loadCustomTriggers(userPreferences: UserPreferences): List<TriggerItem> {
    val json = userPreferences.getCustomTriggersJson()
    if (json.isBlank()) return emptyList()
    return try {
        val array = JSONArray(json)
        val list = mutableListOf<TriggerItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                TriggerItem(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    title = obj.optString("title", ""),
                    description = obj.optString("description", ""),
                    isEnabled = obj.optBoolean("isEnabled", false)
                )
            )
        }
        list
    } catch (_: Exception) {
        emptyList()
    }
}

private fun saveCustomTriggers(userPreferences: UserPreferences, list: List<TriggerItem>) {
    try {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("description", item.description)
                put("isEnabled", item.isEnabled)
            }
            array.put(obj)
        }
        userPreferences.saveCustomTriggersJson(array.toString())
    } catch (_: Exception) {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggersScreen(
    assistantViewModel: AssistantViewModel,
    onNavigateToHome: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToAssistant: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToBatterySetup: () -> Unit,
    onNavigateToAccessibilitySetup: () -> Unit
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as SahNajApplication }
    val userPreferences = remember { app.userPreferences }
    val permissionManager = remember { PermissionManager(context) }

    val triggersMasterEnabled by userPreferences.triggersMasterEnabled.collectAsState()
    val morningBriefingEnabled by userPreferences.morningBriefingEnabled.collectAsState()
    val nightAutomationEnabled by userPreferences.nightAutomationEnabled.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newTriggerName by remember { mutableStateOf("") }
    var newTriggerAction by remember { mutableStateOf("") }

    val customTriggers = remember {
        mutableStateListOf<TriggerItem>().apply {
            addAll(loadCustomTriggers(userPreferences))
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            assistantViewModel.startListening()
            onNavigateToAssistant()
        }
    }

    // Check battery optimization status
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    val isIgnoringBattery = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateToHome) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "My Triggers",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "Automation that works for you",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )
                    }
                },
                actions = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1A24))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Trigger Tips",
                            tint = CyberAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        bottomBar = {
            SahNajBottomBar(
                currentRoute = Screen.Triggers.route,
                onNavigate = { route ->
                    when (route) {
                        Screen.Dashboard.route -> onNavigateToHome()
                        Screen.Chat.route -> onNavigateToChat()
                        Screen.Settings.route -> onNavigateToSettings()
                        else -> {}
                    }
                },
                onOrbClick = {
                    if (permissionManager.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                        assistantViewModel.startListening()
                        onNavigateToAssistant()
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
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Enable Triggers Master Switch Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
                border = BorderStroke(1.dp, Color(0xFF2C1C28))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF281E10))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Triggers",
                            tint = CyberAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Triggers",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Allow ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} to automatically respond to your custom triggers",
                            fontSize = 12.sp,
                            color = CyberTextMuted,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = triggersMasterEnabled,
                        onCheckedChange = { isChecked ->
                            userPreferences.setTriggersMasterEnabled(isChecked)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberRedBright,
                            uncheckedThumbColor = CyberTextMuted,
                            uncheckedTrackColor = Color(0xFF25202E)
                        )
                    )
                }
            }

            // 2. System Permissions Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "System Permissions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A1C16))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (isIgnoringBattery) "Active" else "Action Required",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isIgnoringBattery) Color(0xFF00FF9D) else CyberAmber
                        )
                    }
                }

                // Default Digital Assistant App
                PermissionFixItem(
                    title = "Default Digital Assistant",
                    subtitle = "Set SahNaj as default assistant for Home button / Assistant gesture",
                    onFixClick = {
                        openDefaultAssistantSettings(context)
                    }
                )

                // Battery Optimization
                PermissionFixItem(
                    title = "Battery Optimization",
                    subtitle = "${PersonalityResponses.ASSISTANT_NAME_DISPLAY} needs to run in background",
                    onFixClick = onNavigateToBatterySetup
                )

                // Notification Access
                PermissionFixItem(
                    title = "Notification Access",
                    subtitle = "Required for app-based triggers",
                    onFixClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Opening Notification Settings", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Exact Alarm
                PermissionFixItem(
                    title = "Exact Alarm",
                    subtitle = "Required for scheduled triggers",
                    onFixClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Alarm permission granted", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Alarm permission already available", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // 3. Built-in & Custom Automations Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Active Automations",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )

                // Automation 1: Morning Briefing
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
                    border = BorderStroke(1.dp, Color(0xFF2C1C28))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF2E2412))
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Morning Briefing",
                                tint = CyberAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Morning Briefing",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "When phone is unplugged, announce weather and battery",
                                fontSize = 12.sp,
                                color = CyberTextMuted
                            )
                        }
                        Switch(
                            checked = morningBriefingEnabled && triggersMasterEnabled,
                            enabled = triggersMasterEnabled,
                            onCheckedChange = { isChecked ->
                                userPreferences.setMorningBriefingEnabled(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF25202E)
                            )
                        )
                    }
                }

                // Automation 2: Night Automation
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
                    border = BorderStroke(1.dp, Color(0xFF2C1C28))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1F1C2E))
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Night Automation",
                                tint = Color(0xFF9D86E0),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Night Automation",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "At 11:00 PM, set phone to DND and summarize reminders",
                                fontSize = 12.sp,
                                color = CyberTextMuted
                            )
                        }
                        Switch(
                            checked = nightAutomationEnabled && triggersMasterEnabled,
                            enabled = triggersMasterEnabled,
                            onCheckedChange = { isChecked ->
                                userPreferences.setNightAutomationEnabled(isChecked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF25202E)
                            )
                        )
                    }
                }

                // Custom User Automations
                customTriggers.forEachIndexed { index, trigger ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
                        border = BorderStroke(1.dp, Color(0xFF2C1C28))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = trigger.title,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = trigger.description,
                                    fontSize = 12.sp,
                                    color = CyberTextMuted
                                )
                            }
                            IconButton(
                                onClick = {
                                    customTriggers.removeAt(index)
                                    saveCustomTriggers(userPreferences, customTriggers)
                                    Toast.makeText(context, "Trigger removed", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Trigger",
                                    tint = CyberTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = trigger.isEnabled && triggersMasterEnabled,
                                enabled = triggersMasterEnabled,
                                onCheckedChange = { isChecked ->
                                    customTriggers[index] = trigger.copy(isEnabled = isChecked)
                                    saveCustomTriggers(userPreferences, customTriggers)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CyberRedBright,
                                    uncheckedThumbColor = CyberTextMuted,
                                    uncheckedTrackColor = Color(0xFF25202E)
                                )
                            )
                        }
                    }
                }
            }

            // 4. "Add New Trigger" Dashed Card
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F0B13))
                    .drawBehind {
                        val stroke = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
                        )
                        drawRoundRect(
                            color = Color(0xFF38263A),
                            size = size,
                            cornerRadius = CornerRadius(16.dp.toPx()),
                            style = stroke
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { showAddDialog = true }
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF231A29))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Add New Trigger",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                    Text(
                        text = "Create a custom trigger for ${PersonalityResponses.ASSISTANT_NAME_DISPLAY}",
                        fontSize = 12.sp,
                        color = CyberTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal to create custom trigger
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "New Custom Trigger",
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTriggerName,
                        onValueChange = { newTriggerName = it },
                        label = { Text("Trigger Event (e.g. Battery Low)", color = CyberTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = Color(0xFF38263A),
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTriggerAction,
                        onValueChange = { newTriggerAction = it },
                        label = { Text("Assistant Action", color = CyberTextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = Color(0xFF38263A),
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTriggerName.isNotBlank()) {
                            val newTrigger = TriggerItem(
                                title = newTriggerName.trim(),
                                description = newTriggerAction.ifBlank { "Execute automated directive" },
                                isEnabled = false
                            )
                            customTriggers.add(newTrigger)
                            saveCustomTriggers(userPreferences, customTriggers)
                            newTriggerName = ""
                            newTriggerAction = ""
                            showAddDialog = false
                            Toast.makeText(context, "Custom trigger created and saved!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed)
                ) {
                    Text("Save Trigger", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = CyberTextMuted)
                }
            },
            containerColor = Color(0xFF16101D)
        )
    }
}

@Composable
private fun PermissionFixItem(
    title: String,
    subtitle: String,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF15101A)),
        border = BorderStroke(1.dp, Color(0xFF2C1C28))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF261914))
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = CyberAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = CyberTextMuted
                )
            }

            Button(
                onClick = onFixClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33202E)),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text(
                    text = "Fix",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberRedBright
                )
            }
        }
    }
}

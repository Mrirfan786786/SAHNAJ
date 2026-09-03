package com.example.presentation.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiProvider
import com.example.data.model.AiProvidersConfig
import com.example.presentation.components.ReactiveFuturisticBackground
import com.example.presentation.viewmodel.ApiAndCloudViewModel
import com.example.presentation.viewmodel.CloudSyncState
import com.example.presentation.viewmodel.TestConnectionState
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextDark
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiAndCloudSettingsScreen(
    viewModel: ApiAndCloudViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val providerKeys by viewModel.providerKeys.collectAsState()
    val providerTestStates by viewModel.providerTestStates.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val lastSyncedTimestamp by viewModel.lastSyncedTimestamp.collectAsState()
    val syncState by viewModel.cloudSyncState.collectAsState()

    var activeEditingProvider by remember { mutableStateOf<AiProvider?>(null) }
    var editingKeyText by remember { mutableStateOf("") }
    var isKeyVisible by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<com.example.data.model.ProviderCategory?>(null) }

    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Providers & Cloud Matrix",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "16 AI प्रोवाइडर्स एवं एन्क्रिप्टेड क्लाउड सेटिंग्स",
                            fontSize = 12.sp,
                            color = CyberRedBright,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("api_cloud_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBlack
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ReactiveFuturisticBackground(
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Info Hero Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(CyberRedDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MULTI-PROVIDER AI MATRIX",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberRedBright,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "LLM Engines • Image Synthesis • Video Generation • AES-256 Vault",
                                fontSize = 11.5.sp,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }

                // Category Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allSelected = selectedCategoryFilter == null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (allSelected) CyberRedDark else CyberCard,
                        border = BorderStroke(1.dp, if (allSelected) CyberRedBright else CyberRedBorder),
                        modifier = Modifier.weight(1f).clickable { selectedCategoryFilter = null }
                    ) {
                        Text(
                            text = "⚡ ALL (${AiProvidersConfig.ALL_PROVIDERS.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (allSelected) Color.White else CyberTextMuted,
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    com.example.data.model.ProviderCategory.values().forEach { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        val count = AiProvidersConfig.ALL_PROVIDERS.count { it.category == cat }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CyberRedDark else CyberCard,
                            border = BorderStroke(1.dp, if (isSelected) CyberRedBright else CyberRedBorder),
                            modifier = Modifier.weight(1f).clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = "${cat.emoji} ${cat.name.take(3)} ($count)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CyberTextMuted,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                // Section Title: AI Providers
                val displayedProviders = remember(selectedCategoryFilter) {
                    if (selectedCategoryFilter == null) {
                        AiProvidersConfig.ALL_PROVIDERS
                    } else {
                        AiProvidersConfig.ALL_PROVIDERS.filter { it.category == selectedCategoryFilter }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedCategoryFilter) {
                            com.example.data.model.ProviderCategory.VOICE_TTS -> "VOICE STUDIO & DUBBING ENGINES (${displayedProviders.size})"
                            com.example.data.model.ProviderCategory.IMAGE_GEN -> "IMAGE GENERATION ENGINES (${displayedProviders.size})"
                            com.example.data.model.ProviderCategory.VIDEO_GEN -> "VIDEO GENERATION ENGINES (${displayedProviders.size})"
                            com.example.data.model.ProviderCategory.LLM -> "LLM CHAT & REASONING ENGINES (${displayedProviders.size})"
                            else -> "ALL CONFIGURED AI PROVIDERS (${displayedProviders.size})"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextMuted,
                        letterSpacing = 1.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E0E14),
                        border = BorderStroke(1.dp, Color(0xFF5A1523))
                    ) {
                        Text(
                            text = "AES-256 VAULT",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                // ================= DYNAMIC MULTI-PROVIDER CARDS LOOP =================
                displayedProviders.forEach { provider ->
                    val currentKey = providerKeys[provider.id.lowercase()] ?: ""
                    val testState = providerTestStates[provider.id.lowercase()] ?: TestConnectionState.Idle

                    AiProviderCard(
                        provider = provider,
                        currentKey = currentKey,
                        testState = testState,
                        onEditKey = {
                            activeEditingProvider = provider
                            editingKeyText = currentKey
                            isKeyVisible = false
                        },
                        onTestConnection = {
                            viewModel.testProviderConnection(provider.id, currentKey)
                        },
                        onClearKey = {
                            viewModel.clearProviderKey(provider.id)
                            Toast.makeText(context, "${provider.name} key cleared", Toast.LENGTH_SHORT).show()
                        },
                        maskedKeyText = viewModel.getMaskedApiKey(currentKey)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ================= SECTION: CLOUD SYNC STATUS =================
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(CyberRedDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cloud Sync Status",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )
                                    Text(
                                        text = "क्लाउड सिंक स्थिति • Firebase Realtime Storage",
                                        fontSize = 11.5.sp,
                                        color = CyberTextMuted
                                    )
                                }
                            }

                            // Active / Connected Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (currentUser != null) Color(0xFF0D2A1A) else Color(0xFF1E1405),
                                border = BorderStroke(
                                    1.dp,
                                    if (currentUser != null) CyberGreen else Color(0xFFFF9500)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (currentUser != null) CyberGreen else Color(0xFFFF9500))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (currentUser != null) "CONNECTED" else "LOCAL DEVICE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentUser != null) CyberGreen else Color(0xFFFF9500)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CyberSurface, thickness = 1.dp)

                        // Account Details Container
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CyberBlack,
                            border = BorderStroke(1.dp, CyberSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "ACCOUNT IDENTIFIER",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextMuted,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = currentUser?.email ?: (currentUser?.displayName ?: "technologygyan0786@gmail.com (Local / Offline Session)"),
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CyberTextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF1E1E26), thickness = 0.8.dp)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = CyberTextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "LAST SYNCED",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextMuted,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = formatTimestampText(lastSyncedTimestamp),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (lastSyncedTimestamp > 0) CyberTextSecondary else CyberTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Sync Now Button
                        Button(
                            onClick = {
                                viewModel.syncNow()
                            },
                            enabled = syncState !is CloudSyncState.Syncing,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A1A24),
                                disabledContainerColor = CyberBlack
                            ),
                            border = BorderStroke(1.dp, CyberRedBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("sync_now_button")
                        ) {
                            if (syncState is CloudSyncState.Syncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyberRedBright,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Syncing with Cloud...",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sync Now (अभी सिंक करें)",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright
                                )
                            }
                        }

                        // Sync Confirmation Feedback Banner
                        AnimatedVisibility(
                            visible = syncState is CloudSyncState.Success,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0D2A1A),
                                border = BorderStroke(1.dp, CyberGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "✅ Synced successfully with Firebase Cloud",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen
                                    )
                                }
                            }
                        }
                    }
                }

                // ================= SECTION: DATA USAGE & PRIVACY NOTE =================
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, Color(0xFF331018)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22080E)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Data Usage & Privacy",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "डेटा उपयोग और गोपनीयता",
                                    fontSize = 11.sp,
                                    color = CyberTextMuted
                                )
                            }
                        }

                        HorizontalDivider(color = CyberSurface, thickness = 0.8.dp)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CyberBlack,
                            border = BorderStroke(1.dp, Color(0xFF251016)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "आपका डेटा सुरक्षित रूप से Firebase Cloud पर सेव होता है और सिर्फ आपके अकाउंट से जुड़ा रहता है।",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyberTextPrimary,
                                        lineHeight = 19.sp
                                    )
                                    Text(
                                        text = "All 16 AI Provider keys are protected with Hardware-backed AES-256 encryption.",
                                        fontSize = 11.5.sp,
                                        color = CyberTextMuted,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ================= DYNAMIC EDIT API KEY DIALOG =================
    activeEditingProvider?.let { provider ->
        AlertDialog(
            onDismissRequest = { activeEditingProvider = null },
            containerColor = CyberCard,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Edit ${provider.name} Key",
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary,
                            fontSize = 17.sp
                        )
                        Text(
                            text = provider.hindi,
                            color = CyberRedBright,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Enter or paste your API key for ${provider.name}. Supported models: ${provider.defaultModels}.",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    OutlinedTextField(
                        value = editingKeyText,
                        onValueChange = { editingKeyText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input_${provider.id}"),
                        placeholder = {
                            Text(provider.keyPlaceholder, color = CyberTextMuted, fontSize = 13.sp)
                        },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Hide Key" else "Show Key",
                                    tint = CyberTextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary,
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = CyberSurface,
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                        })
                    )

                    // Quick Actions: Paste from Clipboard / Clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                val clip = clipboardManager?.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val text = clip.getItemAt(0).text?.toString() ?: ""
                                    if (text.isNotBlank()) {
                                        editingKeyText = text.trim()
                                        Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = CyberRedBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Paste Clipboard",
                                color = CyberRedBright,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (editingKeyText.isNotBlank()) {
                            TextButton(
                                onClick = { editingKeyText = "" }
                            ) {
                                Text(
                                    text = "Clear Text",
                                    color = CyberTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProviderKey(provider.id, editingKeyText.trim())
                        activeEditingProvider = null
                        Toast.makeText(context, "${provider.name} Key saved securely in Keystore", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                    modifier = Modifier.testTag("save_api_key_${provider.id}")
                ) {
                    Text("Save Key", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeEditingProvider = null }) {
                    Text("Cancel", color = CyberTextMuted)
                }
            }
        )
    }
}

/**
 * Reusable AI Provider Card matching the Gemini API Card layout specification
 */
@Composable
fun AiProviderCard(
    provider: AiProvider,
    currentKey: String,
    testState: TestConnectionState,
    onEditKey: () -> Unit,
    onTestConnection: () -> Unit,
    onClearKey: () -> Unit,
    maskedKeyText: String
) {
    val isConfigured = currentKey.isNotBlank()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(
            1.dp,
            if (isConfigured) CyberRedBorder else Color(0xFF33161C)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("provider_card_${provider.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card Header: Provider Name + Badge + Neon Accent
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isConfigured) CyberRedDark else Color(0xFF220A10))
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isConfigured) CyberRedBright.copy(alpha = 0.6f) else Color(0xFF4A1420)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = provider.accentEmoji,
                            fontSize = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = provider.name,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${provider.hindi} • ${provider.useCase}",
                            fontSize = 11.5.sp,
                            color = CyberTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Provider Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isConfigured) Color(0xFF1E0E14) else Color(0xFF180A0E),
                    border = BorderStroke(
                        1.dp,
                        if (isConfigured) CyberRedBright.copy(alpha = 0.8f) else Color(0xFF4A101C)
                    )
                ) {
                    Text(
                        text = provider.badge,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConfigured) CyberRedBright else CyberTextMuted,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                    )
                }
            }

            HorizontalDivider(color = CyberSurface, thickness = 1.dp)

            // Configured Key Status Field (Masked key or 'Not Configured' with Edit/Add Key Button)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CyberBlack,
                border = BorderStroke(
                    1.dp,
                    if (isConfigured) Color(0xFF2E1A22) else Color(0xFF221115)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isConfigured) CyberGreen else Color(0xFFE53935))
                            )
                            Text(
                                text = if (isConfigured) "CONFIGURED KEY (ACTIVE)" else "CONFIGURED KEY STATUS",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConfigured) CyberGreen else CyberTextMuted,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = if (isConfigured) maskedKeyText else "Not Configured",
                            fontSize = 13.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isConfigured) CyberTextPrimary else Color(0xFFE57373),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Key: ${provider.localStorageKey}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onEditKey,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberRedBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyberRedBright
                        ),
                        modifier = Modifier.testTag("edit_key_button_${provider.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (isConfigured) "Edit Key" else "Add Key",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isConfigured) "Edit" else "Add Key",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Test Connection Button & Clear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTestConnection,
                    enabled = testState !is TestConnectionState.Testing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberRedBright,
                        disabledContainerColor = CyberRedDark
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("test_connection_button_${provider.id}")
                ) {
                    if (testState is TestConnectionState.Testing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Connection", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isConfigured) {
                    OutlinedButton(
                        onClick = onClearKey,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF4A101C)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberTextMuted),
                        modifier = Modifier.testTag("clear_key_button_${provider.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Key",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Test State Feedback Banner
            AnimatedVisibility(
                visible = testState !is TestConnectionState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                when (testState) {
                    is TestConnectionState.Success -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0D2A1A),
                            border = BorderStroke(1.dp, CyberGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CyberGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = testState.message,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberGreen
                                )
                            }
                        }
                    }
                    is TestConnectionState.Error -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF2A0A10),
                            border = BorderStroke(1.dp, CyberRedBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = testState.message,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFF8599)
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

private fun formatTimestampText(timestamp: Long): String {
    if (timestamp <= 0L) return "Never synced (सिंक नहीं हुआ)"
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    if (diff < 60_000L) return "Just now (अभी-अभी)"
    if (diff < 3600_000L) {
        val minutes = (diff / 60_000L).coerceAtLeast(1)
        return "$minutes min ago ($minutes मिनट पहले)"
    }
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

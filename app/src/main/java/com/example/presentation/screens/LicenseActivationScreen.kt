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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.viewmodel.LicenseViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberError
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
fun LicenseActivationScreen(
    viewModel: LicenseViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var keyInput by remember { mutableStateOf("") }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "License Activation",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "लाइसेंस एक्टिवेशन",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberRedBright
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncCloudStatus() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Status",
                            tint = CyberTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBlack
                )
            )
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // 1. Hero Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardElevated),
                border = BorderStroke(
                    1.dp,
                    if (uiState.isLicensed) CyberGreen.copy(alpha = 0.5f) else CyberRedBorder
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = if (uiState.isLicensed) {
                                    listOf(CyberGreen.copy(alpha = 0.12f), CyberCardElevated)
                                } else {
                                    listOf(CyberRedContainer.copy(alpha = 0.4f), CyberCardElevated)
                                }
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isLicensed) CyberGreen.copy(alpha = 0.18f)
                                    else CyberRedContainer
                                )
                                .border(
                                    1.dp,
                                    if (uiState.isLicensed) CyberGreen else CyberRedBright,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (uiState.isLicensed) Icons.Default.Verified else Icons.Default.Key,
                                contentDescription = "License Status Icon",
                                tint = if (uiState.isLicensed) CyberGreen else CyberRedBright,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uiState.isLicensed) "PRO LICENSE ACTIVE" else "SAHNAJ PRO ACTIVATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isLicensed) CyberGreen else CyberRedBright,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (uiState.isLicensed) {
                                    "सहनाज एआई प्रो फीचर्स सक्रिय हैं"
                                } else {
                                    "वेबसाइट से खरीदी गई की (Key) से ऐप एक्टिवेट करें"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberTextPrimary
                            )
                        }
                    }
                }
            }

            // 2. Main Body: Input Field OR Activated State Display
            if (uiState.isLicensed) {
                // ==========================================
                // REQUIREMENT 4: ALREADY LICENSED DISPLAY
                // "✅ आपका ऐप activated है।" with activation date
                // ==========================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("activated_license_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(CyberGreen.copy(alpha = 0.15f))
                                .border(1.5.dp, CyberGreen, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Check",
                                tint = CyberGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Requirement 4 exact text
                        Text(
                            text = "✅ आपका ऐप activated है।",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen,
                            textAlign = TextAlign.Center
                        )

                        // Activation Date
                        if (uiState.activationDate.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CyberSurface,
                                border = BorderStroke(1.dp, Color(0xFF263326))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "एक्टिवेशन दिनांक: ${uiState.activationDate}",
                                        fontSize = 13.sp,
                                        color = CyberTextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (uiState.licenseKey.isNotBlank()) {
                            // Masked key display
                            val maskedKey = if (uiState.licenseKey.length > 8) {
                                "${uiState.licenseKey.take(4)}-****-****-${uiState.licenseKey.takeLast(4)}"
                            } else {
                                uiState.licenseKey
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberBlack)
                                    .border(1.dp, Color(0xFF222222), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "LICENSE KEY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextMuted
                                    )
                                    Text(
                                        text = maskedKey,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyberTextPrimary
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("License Key", uiState.licenseKey)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Key",
                                        tint = CyberTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = Color(0xFF261824),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        // Feature badges unlocked
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            LicenseBenefitChip(icon = Icons.Default.Star, text = "Full AI Engine")
                            LicenseBenefitChip(icon = Icons.Default.Shield, text = "Cloud Sync")
                            LicenseBenefitChip(icon = Icons.Default.Lock, text = "Voice Lock")
                        }
                    }
                }
            } else {
                // ==========================================
                // REQUIREMENT 2 & 3: INPUT FIELD & ACTIVATE BUTTON
                // Placeholder: "यहाँ अपनी license key डालें"
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Enter License Key",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )

                        Text(
                            text = "कृपया 16-अंकीय या पोर्टल से मिली लाइसेंस की दर्ज करें।",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )

                        // Text Field for entering key
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = {
                                keyInput = it
                                if (uiState.errorMessage != null) {
                                    viewModel.clearError()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("license_key_input"),
                            placeholder = {
                                Text(
                                    text = "यहाँ अपनी license key डालें",
                                    color = CyberTextMuted,
                                    fontSize = 13.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Key Icon",
                                    tint = if (keyInput.isNotBlank()) CyberRedBright else CyberTextMuted
                                )
                            },
                            trailingIcon = {
                                if (keyInput.isNotBlank()) {
                                    IconButton(onClick = { keyInput = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = CyberTextMuted
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clipData = clipboard.primaryClip
                                            if (clipData != null && clipData.itemCount > 0) {
                                                val pasteText = clipData.getItemAt(0).text?.toString() ?: ""
                                                if (pasteText.isNotBlank()) {
                                                    keyInput = pasteText.trim()
                                                    Toast.makeText(context, "Key pasted", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = CyberTextMuted
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (keyInput.isNotBlank()) {
                                        viewModel.activateLicense(keyInput)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CyberBlack,
                                unfocusedContainerColor = CyberBlack,
                                focusedBorderColor = CyberRedBright,
                                unfocusedBorderColor = if (uiState.errorMessage != null) CyberError else Color(0xFF38263A),
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary
                            )
                        )

                        // Error Banner (Requirement 3: Specific error messages)
                        AnimatedVisibility(
                            visible = uiState.errorMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            uiState.errorMessage?.let { errorText ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = CyberError.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, CyberError.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = "Error",
                                            tint = CyberError,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = errorText,
                                            fontSize = 13.sp,
                                            color = CyberError,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.testTag("license_error_message")
                                        )
                                    }
                                }
                            }
                        }

                        // Activate Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.activateLicense(keyInput)
                            },
                            enabled = keyInput.isNotBlank() && !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("activate_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberRed,
                                disabledContainerColor = CyberRed.copy(alpha = 0.35f)
                            )
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "वेरिफाई किया जा रहा है...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Activate",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 3. Information & Verification Guidelines Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                border = BorderStroke(1.dp, Color(0xFF261824))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Help",
                            tint = CyberAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "लाइसेंस संबंधी जानकारी (License Guidelines)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    Text(
                        text = "• प्रत्येक license key एकल (1) डिवाइस/अकाउंट के लिए मान्य है।\n" +
                               "• एक्टिवेशन के दौरान इंटरनेट कनेक्शन आवश्यक है (Firebase Firestore Verification)।\n" +
                               "• यदि आपने पोर्टल से की प्राप्त की है और एरर आ रहा है, तो स्पेलिंग व हाइफ़न जांचें।",
                        fontSize = 12.sp,
                        color = CyberTextMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LicenseBenefitChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = CyberSurface,
        border = BorderStroke(1.dp, Color(0xFF2E2433))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberRedBright,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = CyberTextSecondary
            )
        }
    }
}

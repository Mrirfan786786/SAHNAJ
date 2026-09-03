package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VpnKey
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.personality.PersonalityResponses
import com.example.presentation.viewmodel.ProfileUiState
import com.example.presentation.viewmodel.ProfileViewModel
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
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by profileViewModel.uiState.collectAsState()
    val isSaving by profileViewModel.isSaving.collectAsState()
    val feedbackMessage by profileViewModel.feedbackMessage.collectAsState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            profileViewModel.clearFeedbackMessage()
        }
    }

    // Modal: Edit Display Name
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EDIT DISPLAY NAME",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = CyberRedBright
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Set the name ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} will use when addressing you (e.g. \"हाँ [Name], बताइए\").",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name / नाम") },
                        placeholder = { Text("Enter your name...") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberRedBright,
                            unfocusedBorderColor = CyberRedBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary,
                            focusedContainerColor = CyberCard,
                            unfocusedContainerColor = CyberCard
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.updateDisplayName(tempName)
                        showEditNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("save_name_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "SAVE NAME",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = FontFamily.Monospace,
                        color = CyberTextMuted
                    )
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Modal: Sign Out Confirmation
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "DISCONNECT TERMINAL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = CyberRedBright
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out from your account?",
                    fontSize = 12.sp,
                    color = CyberTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        profileViewModel.signOut {
                            onSignOut()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SIGN OUT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = FontFamily.Monospace,
                        color = CyberTextMuted
                    )
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Modal: Reset Local Preferences
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "RESET LOCAL CONFIGURATION",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = CyberRedBright
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to reset all local cached preferences to factory values?",
                    fontSize = 12.sp,
                    color = CyberTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        profileViewModel.resetAppData()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CONFIRM RESET",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = FontFamily.Monospace,
                        color = CyberTextMuted
                    )
                }
            },
            containerColor = CyberSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "User Profile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "यूज़र प्रोफ़ाइल • Account & Preferences",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
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
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyberRedBright)
                }
            }
            is ProfileUiState.Content -> {
                val profile = state.profile
                val firebaseUser = state.firebaseUser

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ==========================================
                    // SECTION 1 — PROFILE INFO
                    // ==========================================
                    SectionHeader(
                        icon = Icons.Default.Person,
                        title = "Profile Info",
                        hindiTitle = "प्रोफ़ाइल जानकारी"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_info_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Avatar + Name + Email Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Profile Photo / Placeholder Avatar
                                val photoUrl = firebaseUser?.photoUrl?.toString() ?: profile.photoUrl
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color(0xFFFF2A4D),
                                                    Color(0xFF7A0F26),
                                                    Color(0xFF1E040B)
                                                )
                                            )
                                        )
                                        .border(2.dp, CyberRedBright, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (photoUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(photoUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = "User Profile Photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        // Default stylized initials/avatar
                                        val initial = profile.displayName.takeIf { it.isNotBlank() }?.take(1)?.uppercase() ?: "U"
                                        Text(
                                            text = initial,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Name & Email Text
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = profile.displayName.ifBlank { "User" },
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )

                                    val userEmail = firebaseUser?.email ?: profile.email.takeIf { it.isNotBlank() } ?: "Local Device Account"
                                    Text(
                                        text = userEmail,
                                        fontSize = 12.sp,
                                        color = CyberTextMuted
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Assistant addressing preview badge
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = CyberRedContainer,
                                        border = BorderStroke(0.5.dp, CyberRedBright.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "Assistant calls you: \"${profile.displayName.ifBlank { "User" }}\"",
                                            fontSize = 10.5.sp,
                                            color = CyberRedBright,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF2A1C28))

                            // Display Name Edit Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Display Name",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = CyberTextPrimary
                                    )
                                    Text(
                                        text = "Custom name used when ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} speaks to you",
                                        fontSize = 11.sp,
                                        color = CyberTextMuted
                                    )
                                }

                                Button(
                                    onClick = {
                                        tempName = profile.displayName
                                        showEditNameDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedContainer),
                                    border = BorderStroke(1.dp, CyberRedBright),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("edit_name_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Edit Name",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberRedBright
                                    )
                                }
                            }

                            // Google Account Name Info (if different)
                            if (firebaseUser?.displayName != null && firebaseUser.displayName != profile.displayName) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF140E1A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = CyberTextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Google Account: ${firebaseUser.displayName}",
                                            fontSize = 11.5.sp,
                                            color = CyberTextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 2 — PREFERENCES
                    // ==========================================
                    SectionHeader(
                        icon = Icons.Default.Tune,
                        title = "Preferences",
                        hindiTitle = "प्राथमिकताएं"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preferences_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 1. Preferred Language Selection
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Preferred Language (पसंदीदा भाषा)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )
                                }
                                Text(
                                    text = "Choose the language ${PersonalityResponses.ASSISTANT_NAME_DISPLAY} uses for its responses (persisted in DataStore & Firestore):",
                                    fontSize = 11.sp,
                                    color = CyberTextMuted
                                )

                                val languages = listOf(
                                    Triple("Hinglish", "हिंग्लिश", "Natural conversational Hindi + English mix"),
                                    Triple("Hindi", "हिंदी", "शुद्ध और स्पष्ट हिंदी में जवाब"),
                                    Triple("English", "English", "Fluent English conversational responses")
                                )

                                languages.forEach { (code, nativeLabel, desc) ->
                                    val isSelected = state.preferredLanguage.equals(code, ignoreCase = true)
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                profileViewModel.updatePreferredLanguage(code)
                                            }
                                            .testTag("language_option_$code"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CyberRedContainer else Color(0xFF16101C),
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) CyberRedBright else Color(0xFF2A1C28)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { profileViewModel.updatePreferredLanguage(code) },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = CyberRedBright,
                                                    unselectedColor = CyberTextMuted
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = code,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) CyberRedBright else CyberTextPrimary
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "($nativeLabel)",
                                                        fontSize = 11.5.sp,
                                                        color = CyberTextSecondary
                                                    )
                                                }
                                                Text(
                                                    text = desc,
                                                    fontSize = 10.5.sp,
                                                    color = CyberTextMuted
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = CyberRedBright,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFF2A1C28))

                            // 2. Use My Name When Speaking Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("use_name_row"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Use my name when speaking",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CyberTextPrimary
                                        )
                                        Text(
                                            text = "बातचीत में मेरा नाम इस्तेमाल करें (e.g. \"हाँ ${profile.displayName.ifBlank { "User" }}, बताइए\")",
                                            fontSize = 11.sp,
                                            color = CyberTextMuted
                                        )
                                    }
                                }

                                Switch(
                                    checked = state.useNameWhenSpeaking,
                                    onCheckedChange = { profileViewModel.updateUseNameWhenSpeaking(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CyberRedBright,
                                        uncheckedThumbColor = CyberTextMuted,
                                        uncheckedTrackColor = Color(0xFF251B2A)
                                    ),
                                    modifier = Modifier.testTag("use_name_switch")
                                )
                            }
                        }
                    }

                    // ==========================================
                    // SECTION 3 — ACCOUNT INFO (READ-ONLY)
                    // ==========================================
                    SectionHeader(
                        icon = Icons.Default.Security,
                        title = "Account Info",
                        hindiTitle = "अकाउंट जानकारी (Read-Only)"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("account_info_card"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCard),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Account Creation Date
                            AccountInfoRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Account Created",
                                value = state.accountCreationDate
                            )

                            HorizontalDivider(color = Color(0xFF2A1C28))

                            // Sign-In Method
                            AccountInfoRow(
                                icon = Icons.Default.VpnKey,
                                label = "Sign-In Method",
                                value = state.signInProvider
                            )

                            HorizontalDivider(color = Color(0xFF2A1C28))

                            // Email Address
                            AccountInfoRow(
                                icon = Icons.Default.Email,
                                label = "Email Address",
                                value = firebaseUser?.email ?: "Offline Local Profile"
                            )

                            HorizontalDivider(color = Color(0xFF2A1C28))

                            // User UID
                            AccountInfoRow(
                                icon = Icons.Default.Fingerprint,
                                label = "Account UID",
                                value = firebaseUser?.uid ?: "local_device"
                            )

                            HorizontalDivider(color = Color(0xFF2A1C28))

                            // Cloud Status
                            AccountInfoRow(
                                icon = Icons.Default.Security,
                                label = "Protection & Sync",
                                value = if (firebaseUser != null) "Active & Firebase Cloud Synced" else "Local Offline Engine"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ==========================================
                    // ACTIONS & UTILITIES
                    // ==========================================
                    OutlinedButton(
                        onClick = onNavigateToPrivacyPolicy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("privacy_policy_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Policy, contentDescription = null, tint = CyberRedBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PRIVACY POLICY & DATA GUARANTEES",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_preferences_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = CyberRedBright)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RESET LOCAL PREFERENCES",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    Button(
                        onClick = { showSignOutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("sign_out_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIGN OUT / DISCONNECT ACCOUNT",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = CyberRedBright,
                        fontSize = 14.sp
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    hindiTitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyberRedBright,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = CyberTextPrimary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "• $hindiTitle",
            fontSize = 12.sp,
            color = CyberRedBright,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AccountInfoRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyberRedBright,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = CyberTextMuted,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyberTextPrimary
        )
    }
}

package com.example.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.presentation.viewmodel.EnrollmentState
import com.example.presentation.viewmodel.VoiceAuthenticationViewModel
import com.example.presentation.viewmodel.VoiceTestState
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
fun VoiceAuthenticationScreen(
    viewModel: VoiceAuthenticationViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isVoiceLockEnabled by viewModel.isVoiceLockEnabled.collectAsState()
    val voiceProfile by viewModel.voiceProfile.collectAsState()
    val enrollmentState by viewModel.enrollmentState.collectAsState()
    val rmsVolume by viewModel.rmsVolume.collectAsState()
    val voiceTestState by viewModel.voiceTestState.collectAsState()
    val debugLog by viewModel.debugLog.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onToggleVoiceLock(true)
        } else {
            Toast.makeText(context, "वॉइस लॉक के लिए माइक्रोफ़ोन अनुमति आवश्यक है", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleToggle(targetState: Boolean) {
        if (targetState) {
            val hasPerm = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPerm) {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                viewModel.onToggleVoiceLock(true)
            }
        } else {
            viewModel.onToggleVoiceLock(false)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Voice Authentication",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "वॉइस ऑथेंटिकेशन • Biometric Speaker Lock",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("voice_auth_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==========================================
            // EXPLANATORY NOTE IN HINDI (शीर्ष विवरण)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_auth_explanatory_card"),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1F0D13),
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
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = "यह feature सिर्फ आपकी आवाज़ पर सहनाज को चालू करेगा, ताकि कोई और व्यक्ति आपका असिस्टेंट इस्तेमाल न कर सके।",
                        fontSize = 12.5.sp,
                        color = CyberTextSecondary,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ==========================================
            // SECTION 1 — VOICE LOCK TOGGLE (वॉइस लॉक)
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_lock_toggle_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(
                    1.dp,
                    if (isVoiceLockEnabled) CyberRedBright else CyberRedBorder
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
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isVoiceLockEnabled) CyberRedContainer else Color(0xFF221A26)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isVoiceLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (isVoiceLockEnabled) CyberRedBright else CyberTextMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "Enable Voice Lock",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "वॉइस लॉक चालू करें",
                                    fontSize = 12.sp,
                                    color = CyberRedBright,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Switch(
                            checked = isVoiceLockEnabled,
                            onCheckedChange = { handleToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF251B2A)
                            ),
                            modifier = Modifier.testTag("enable_voice_lock_switch")
                        )
                    }

                    HorizontalDivider(color = Color(0xFF2A1C28))

                    // Status Text
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceLockEnabled) CyberGreen else CyberTextMuted)
                        )
                        Text(
                            text = if (isVoiceLockEnabled) {
                                "सक्रिय: केवल आपकी आवाज़ से सहनाज चालू होगी"
                            } else {
                                "निष्क्रिय: कोई भी आवाज़ वेक-वर्ड चालू कर सकती है (Default)"
                            },
                            fontSize = 11.5.sp,
                            color = if (isVoiceLockEnabled) CyberGreen else CyberTextMuted,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ==========================================
            // ENROLLMENT WIZARD / LIVE CALIBRATION CARD
            // ==========================================
            AnimatedVisibility(
                visible = enrollmentState !is EnrollmentState.Idle,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EnrollmentWizardCard(
                    enrollmentState = enrollmentState,
                    rmsVolume = rmsVolume,
                    onRetry = { step -> viewModel.retryCurrentStep(step) },
                    onCancel = { viewModel.cancelEnrollment() }
                )
            }

            // ==========================================
            // SECTION 2 — ENROLLED VOICE PROFILE DETAILS
            // ==========================================
            if (voiceProfile != null && voiceProfile!!.isEnrolled && enrollmentState is EnrollmentState.Idle) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("enrolled_profile_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Enrolled Voice Profile",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "रजिस्टर्ड वॉइस प्रोफ़ाइल",
                                    fontSize = 11.5.sp,
                                    color = CyberRedBright
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFF2A1C28))

                        // Stats
                        ProfileInfoRow(
                            label = "Enrollment Status",
                            value = "Calibrated & Protected (सुरक्षित)"
                        )
                        ProfileInfoRow(
                            label = "Samples Recorded",
                            value = "${voiceProfile!!.sampleCount} / 3 Samples (सहनाज)"
                        )
                        ProfileInfoRow(
                            label = "Enrolled On",
                            value = voiceProfile!!.enrolledDate.ifEmpty { "Recently" }
                        )
                        ProfileInfoRow(
                            label = "Storage Location",
                            value = "Local Device KeyStore (No Cloud)"
                        )

                        HorizontalDivider(color = Color(0xFF2A1C28))

                        // Action Buttons: Re-enroll & Disable
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.reEnrollVoice() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("re_enroll_voice_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Re-enroll Voice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.disableVoiceLock() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CyberRedBright
                                ),
                                border = BorderStroke(1.dp, CyberRedBorder),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("disable_voice_lock_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = CyberRedBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Disable Lock",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 3 — LIVE VOICE MATCH TEST (आवाज़ टेस्ट करें)
            // ==========================================
            if (voiceProfile != null && voiceProfile!!.isEnrolled && enrollmentState is EnrollmentState.Idle) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_match_test_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCard),
                    border = BorderStroke(1.dp, CyberRedBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "Test Voice Recognition",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = "अपनी आवाज़ का मिलान टेस्ट करें",
                                    fontSize = 11.5.sp,
                                    color = CyberTextSecondary
                                )
                            }
                        }

                        Text(
                            text = "नीचे बटन दबाएं और बोलें 'सहनाज'। सिस्टम आपकी आवाज़ और रजिस्टर्ड प्रोफ़ाइल का मिलान चेक करेगा।",
                            fontSize = 12.sp,
                            color = CyberTextSecondary,
                            lineHeight = 17.sp
                        )

                        // Testing feedback
                        when (val testState = voiceTestState) {
                            is VoiceTestState.Listening -> {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyberRedContainer,
                                    border = BorderStroke(1.dp, CyberRedBright)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        PulsingMicIcon(rmsVolume = rmsVolume)
                                        Column {
                                            Text(
                                                text = "सुन रहा है... बोलिए 'सहनाज'",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Listening for test utterance...",
                                                fontSize = 11.sp,
                                                color = CyberRedBright
                                            )
                                        }
                                    }
                                }
                            }
                            is VoiceTestState.Result -> {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (testState.isMatch) Color(0xFF0F2618) else Color(0xFF261018),
                                    border = BorderStroke(
                                        1.dp,
                                        if (testState.isMatch) CyberGreen else CyberRedBright
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (testState.isMatch) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (testState.isMatch) CyberGreen else CyberRedBright,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (testState.isMatch) "✅ आवाज़ सत्यापित (Voice Verified)" else "❌ अपरिचित आवाज़ (Unknown Voice)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (testState.isMatch) {
                                                    "Match confidence: ${"%.1f".format(testState.confidenceScore * 100)}% • Assistant will activate"
                                                } else {
                                                    "अपरिचित आवाज़ - सक्रिय नहीं हुआ (${testState.message})"
                                                },
                                                fontSize = 11.5.sp,
                                                color = CyberTextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                            is VoiceTestState.Idle -> Unit
                        }

                        Button(
                            onClick = { viewModel.startVoiceTest() },
                            enabled = voiceTestState !is VoiceTestState.Listening,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("start_voice_test_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (voiceTestState is VoiceTestState.Listening) "LISTENING..." else "TEST VOICE MATCH (सहनाज)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ==========================================
            // DEBUG / DEV MODE LOG (अपरिचित आवाज़ लॉग)
            // ==========================================
            if (debugLog != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_auth_debug_log"),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF140D18),
                    border = BorderStroke(1.dp, Color(0xFF3B1E38))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Debug Log: $debugLog",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun EnrollmentWizardCard(
    enrollmentState: EnrollmentState,
    rmsVolume: Float,
    onRetry: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("enrollment_wizard_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, CyberRedBright)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Voice Lock Enrollment",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = CyberTextMuted
                    )
                }
            }

            // Step Progress Indicator (1 of 3, 2 of 3, 3 of 3)
            val currentStep = when (enrollmentState) {
                is EnrollmentState.Listening -> enrollmentState.step
                is EnrollmentState.StepSuccess -> enrollmentState.step
                is EnrollmentState.Error -> enrollmentState.step
                is EnrollmentState.Completed -> 3
                else -> 1
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    StepBadge(
                        stepNumber = i,
                        isCurrent = i == currentStep && enrollmentState !is EnrollmentState.Completed,
                        isCompleted = i < currentStep || (i == 3 && enrollmentState is EnrollmentState.Completed)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFF2A1C28))

            // Main Prompt & Animation Body
            when (enrollmentState) {
                is EnrollmentState.Listening -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PulsingMicOrb(rmsVolume = rmsVolume)

                        Text(
                            text = enrollmentState.promptHindi,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberRedBright,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "स्पष्ट आवाज़ में बोलिए: \"सहनाज\"",
                            fontSize = 13.sp,
                            color = CyberTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF24141B),
                            border = BorderStroke(1.dp, CyberRedBorder)
                        ) {
                            Text(
                                text = "चरण ${enrollmentState.step} / 3: ${enrollmentState.promptEnglish}",
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = CyberTextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                is EnrollmentState.StepSuccess -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F2618))
                                .border(2.dp, CyberGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Text(
                            text = "चरण ${enrollmentState.step} रिकॉर्ड हो गया!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                        Text(
                            text = "Sample captured: \"${enrollmentState.sampleText.ifEmpty { "सहनाज" }}\"",
                            fontSize = 12.sp,
                            color = CyberTextSecondary
                        )
                    }
                }

                is EnrollmentState.Completed -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F2618))
                                .border(2.dp, CyberGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "✅ आवाज़ सफलतापूर्वक रजिस्टर हो गई!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberGreen,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "वॉइस लॉक चालू कर दिया गया है। अब केवल आपकी आवाज़ पर सहनाज सक्रिय होगी।",
                            fontSize = 12.sp,
                            color = CyberTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is EnrollmentState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2B1015))
                                .border(2.dp, CyberRedBright, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Text(
                            text = enrollmentState.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { onRetry(enrollmentState.step) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "फिर से कोशिश करें (Retry)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun StepBadge(
    stepNumber: Int,
    isCurrent: Boolean,
    isCompleted: Boolean
) {
    val bgColor = when {
        isCompleted -> Color(0xFF0F2618)
        isCurrent -> CyberRedContainer
        else -> Color(0xFF1F1A24)
    }
    val borderColor = when {
        isCompleted -> CyberGreen
        isCurrent -> CyberRedBright
        else -> Color(0xFF332A3A)
    }
    val textColor = when {
        isCompleted -> CyberGreen
        isCurrent -> CyberRedBright
        else -> CyberTextMuted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(1.5.dp, borderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CyberGreen,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = "$stepNumber",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
        Text(
            text = "Step $stepNumber",
            fontSize = 11.5.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) CyberTextPrimary else CyberTextMuted
        )
    }
}

@Composable
private fun PulsingMicOrb(rmsVolume: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val dynamicScale = (1f + (rmsVolume / 15f)).coerceIn(1f, 1.4f) * pulseScale

    Box(
        modifier = Modifier
            .size(90.dp)
            .scale(dynamicScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(CyberRedBright, CyberRedContainer, Color.Transparent)
                )
            )
            .border(2.dp, CyberRedBright, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "Listening Mic",
            tint = Color.White,
            modifier = Modifier.size(38.dp)
        )
    }
}

@Composable
private fun PulsingMicIcon(rmsVolume: Float) {
    val scale = (1f + (rmsVolume / 20f)).coerceIn(1f, 1.3f)
    Box(
        modifier = Modifier
            .size(36.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(CyberRedBright),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = CyberTextMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyberTextPrimary
        )
    }
}

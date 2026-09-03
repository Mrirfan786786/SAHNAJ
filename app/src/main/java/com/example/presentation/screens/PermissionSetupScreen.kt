package com.example.presentation.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.personality.PersonalityResponses
import com.example.presentation.viewmodel.SetupViewModel
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

@Composable
fun PermissionSetupScreen(
    setupViewModel: SetupViewModel,
    onNavigateToBatterySetup: () -> Unit,
    onSkipToHome: () -> Unit
) {
    val permissionStatus by setupViewModel.permissions.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        setupViewModel.refreshPermissions()
    }

    val totalSteps = 6
    var grantedCount = 0
    if (permissionStatus.hasMic) grantedCount++
    if (permissionStatus.hasContacts) grantedCount++
    if (permissionStatus.hasPhone) grantedCount++
    if (permissionStatus.hasSms) grantedCount++
    if (permissionStatus.hasNotifications) grantedCount++
    if (permissionStatus.hasAccessibility) grantedCount++

    val progressFraction = grantedCount.toFloat() / totalSteps.toFloat()

    Scaffold(
        containerColor = CyberBlack,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberSurface)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "चरण $grantedCount / $totalSteps • अनुमतियाँ सेटअप",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (grantedCount == totalSteps) CyberGreen.copy(alpha = 0.2f) else CyberRedContainer)
                            .border(BorderStroke(1.dp, if (grantedCount == totalSteps) CyberGreen else CyberRed), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (grantedCount == totalSteps) "ALL GRANTED" else "$grantedCount/$totalSteps COMPLETED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (grantedCount == totalSteps) CyberGreen else CyberRedBright
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (grantedCount == totalSteps) CyberGreen else CyberRedBright,
                    trackColor = CyberRedBorder.copy(alpha = 0.4f)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CyberRedContainer)
                            .border(BorderStroke(1.dp, CyberRedBright), RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberRedBright,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "अनुमति क्यों जरूरी हैं?",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "सहनाज आपके निर्देशों पर सही काम कर सके, इसलिए हर अनुमति का कारण नीचे विस्तार से दिया गया है।",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            color = CyberTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // 1. MICROPHONE WHY CARD (Step 1)
            PermissionWhyCard(
                stepIndex = 1,
                totalSteps = totalSteps,
                icon = Icons.Default.Mic,
                heading = "माइक्रोफ़ोन क्यों चाहिए?",
                explanation = "ताकि मैं आपकी आवाज़ सुनकर आपके आदेश समझ सकूं और तुरंत एक्शन ले सकूं।",
                isGranted = permissionStatus.hasMic,
                onRequest = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
                }
            )

            // 2. CONTACTS WHY CARD (Step 2)
            PermissionWhyCard(
                stepIndex = 2,
                totalSteps = totalSteps,
                icon = Icons.Default.Contacts,
                heading = "कॉन्टैक्ट्स अनुमति क्यों चाहिए?",
                explanation = "ताकि आप बोलकर किसी का भी नाम लें (जैसे 'राहुल को कॉल लगाओ') और सहनाज तुरंत उन्हें ढूंढ सके।",
                isGranted = permissionStatus.hasContacts,
                onRequest = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS))
                }
            )

            // 3. PHONE CALLS WHY CARD (Step 3)
            PermissionWhyCard(
                stepIndex = 3,
                totalSteps = totalSteps,
                icon = Icons.Default.Call,
                heading = "फ़ोन कॉल अनुमति क्यों चाहिए?",
                explanation = "ताकि बिना डायल पैड खोले सहनाज सीधे आपके वॉइस कमांड से फोन कॉल कनेक्ट कर सके।",
                isGranted = permissionStatus.hasPhone,
                onRequest = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.CALL_PHONE))
                }
            )

            // 4. SMS WHY CARD (Step 4)
            PermissionWhyCard(
                stepIndex = 4,
                totalSteps = totalSteps,
                icon = Icons.AutoMirrored.Filled.Message,
                heading = "SMS संदेश अनुमति क्यों चाहिए?",
                explanation = "ताकि आप बोलकर सीधे मैसेज भेज सकें। अगर यह अनुमति न दें, तो सहनाज केवल SMS ऐप खोल देगी।",
                isGranted = permissionStatus.hasSms,
                onRequest = {
                    permissionLauncher.launch(arrayOf(Manifest.permission.SEND_SMS))
                }
            )

            // 5. NOTIFICATIONS WHY CARD (Step 5)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                PermissionWhyCard(
                    stepIndex = 5,
                    totalSteps = totalSteps,
                    icon = Icons.Default.Notifications,
                    heading = "नोटिफिकेशन अनुमति क्यों चाहिए?",
                    explanation = "ताकि बैकग्राउंड में वॉइस लिसनर सक्रिय रहे और जरूरी कमांड स्टेटस आपको हमेशा दिखता रहे।",
                    isGranted = permissionStatus.hasNotifications,
                    onRequest = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
                    }
                )
            }

            // 6. ACCESSIBILITY SERVICE WHY CARD (Step 6)
            PermissionWhyCard(
                stepIndex = 6,
                totalSteps = totalSteps,
                icon = Icons.Default.TouchApp,
                heading = "एक्सेसिबिलिटी सर्विस क्यों चाहिए?",
                explanation = "ताकि सहनाज आपकी आवाज़ पर स्क्रीन स्वाइप, बैक, होम जाना और ऐप्स ऑटोमेट कर सके।",
                isGranted = permissionStatus.hasAccessibility,
                onRequest = {
                    try {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Bulk Grant Essential Permissions Button (if some essential are missing)
            val hasMissingEssential = !permissionStatus.hasMic || !permissionStatus.hasContacts || !permissionStatus.hasPhone
            if (hasMissingEssential) {
                Button(
                    onClick = {
                        val list = mutableListOf(
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CALL_PHONE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            list.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(list.toTypedArray())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright)
                ) {
                    Text(
                        text = "सभी मुख्य अनुमतियाँ एक साथ दें",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Next Step: Battery Setup Matrix
            OutlinedButton(
                onClick = onNavigateToBatterySetup,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "आगे बढ़ें (बैटरी ऑप्टिमाइज़ेशन)",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = CyberRedBright,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionWhyCard(
    stepIndex: Int,
    totalSteps: Int,
    icon: ImageVector,
    heading: String,
    explanation: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) CyberCard.copy(alpha = 0.7f) else CyberCard
        ),
        border = BorderStroke(1.dp, if (isGranted) CyberGreen.copy(alpha = 0.5f) else CyberRedBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row with Step indicator & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "चरण $stepIndex / $totalSteps",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextMuted
                )

                if (isGranted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "GRANTED",
                            tint = CyberGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "अनुमति प्राप्त (GRANTED)",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberGreen
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberRedContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "लंबित / PENDING",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberRedBright
                        )
                    }
                }
            }

            // Main Content Row (Icon + Heading + 1-2 line explanation)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGranted) CyberGreen.copy(alpha = 0.15f) else CyberRedContainer)
                        .border(BorderStroke(1.dp, if (isGranted) CyberGreen else CyberRed), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) CyberGreen else CyberRedBright,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = heading,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberTextPrimary
                    )
                    Text(
                        text = explanation,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = CyberTextSecondary,
                        lineHeight = 16.5.sp
                    )
                }
            }

            // "अनुमति दें" Action Button (if not granted)
            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Text(
                        text = "अनुमति दें / GRANT PERMISSION",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

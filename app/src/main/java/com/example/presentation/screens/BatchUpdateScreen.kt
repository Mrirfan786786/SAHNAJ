package com.example.presentation.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.presentation.viewmodel.BatchUpdateViewModel
import com.example.presentation.viewmodel.UpdateUiState
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
fun BatchUpdateScreen(
    viewModel: BatchUpdateViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isAutoUpdateEnabled by viewModel.isAutoUpdateEnabled.collectAsState()
    val lastCheckedTime by viewModel.lastCheckedTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Batch / Update",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "अपडेट और बैच सेटिंग्स • Version & Distribution",
                            fontSize = 11.sp,
                            color = CyberRedBright
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("batch_update_back_button")
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
            // SECTION 1 — APP INFO (ऐप जानकारी)
            // ==========================================
            SectionHeader(
                icon = Icons.Default.Info,
                title = "App Info",
                hindiTitle = "ऐप जानकारी"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_info_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with Icon & Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberRedContainer)
                                .border(1.5.dp, CyberRedBright, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = CyberRedBright,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SahNaj AI Assistant",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberRedBright
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2A1C28))

                    // Detail items
                    InfoRow(
                        label = "App Version",
                        value = "v${BuildConfig.VERSION_NAME}"
                    )

                    HorizontalDivider(color = Color(0xFF2A1C28))

                    InfoRow(
                        label = "Build Number",
                        value = "${BuildConfig.VERSION_CODE} (Universal Release)"
                    )

                    HorizontalDivider(color = Color(0xFF2A1C28))

                    InfoRow(
                        label = "Last updated on",
                        value = "29 August 2026"
                    )

                    HorizontalDivider(color = Color(0xFF2A1C28))

                    InfoRow(
                        label = "Package Identifier",
                        value = BuildConfig.APPLICATION_ID
                    )

                    HorizontalDivider(color = Color(0xFF2A1C28))

                    InfoRow(
                        label = "AI Engine Core",
                        value = "Gemini AI & Local Rule Core"
                    )
                }
            }

            // ==========================================
            // SECTION 2 — CHECK FOR UPDATES (अपडेट चेक करें)
            // ==========================================
            SectionHeader(
                icon = Icons.Default.Sync,
                title = "Check for Updates",
                hindiTitle = "अपडेट चेक करें"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("check_updates_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Verify whether a newer build or hotfix release of SahNaj AI is available from the distribution channel.",
                        fontSize = 12.sp,
                        color = CyberTextSecondary
                    )

                    // Action Button: Check for Update
                    Button(
                        onClick = { viewModel.checkForUpdates(isManual = true) },
                        enabled = uiState !is UpdateUiState.Checking,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("check_for_update_button")
                    ) {
                        if (uiState is UpdateUiState.Checking) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CHECKING UPDATE SERVER...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHECK FOR UPDATE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    // Last Checked Text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last checked:",
                            fontSize = 11.5.sp,
                            color = CyberTextMuted
                        )
                        Text(
                            text = lastCheckedTime,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextSecondary
                        )
                    }

                    // Result Panels
                    AnimatedVisibility(
                        visible = uiState is UpdateUiState.UpToDate,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (uiState is UpdateUiState.UpToDate) {
                            val state = uiState as UpdateUiState.UpToDate
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("up_to_date_banner"),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F2618),
                                border = BorderStroke(1.dp, CyberGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "✅ आपके पास सबसे नया version है।",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "You are running the latest version (v${state.currentVersionName}, Build ${state.currentVersionCode}). No updates required.",
                                            fontSize = 11.5.sp,
                                            color = CyberTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState is UpdateUiState.UpdateAvailable,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (uiState is UpdateUiState.UpdateAvailable) {
                            val info = (uiState as UpdateUiState.UpdateAvailable).updateInfo
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("update_available_card"),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF261018),
                                border = BorderStroke(1.5.dp, CyberRedBright)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NewReleases,
                                            contentDescription = null,
                                            tint = CyberRedBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "नया अपडेट उपलब्ध है (v${info.versionName})",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Black,
                                            color = CyberRedBright
                                        )
                                    }

                                    Text(
                                        text = "Release Date: ${info.releaseDate}",
                                        fontSize = 11.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = CyberTextMuted
                                    )

                                    HorizontalDivider(color = Color(0xFF4A1A28))

                                    Text(
                                        text = "क्या नया है (Release Notes):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberTextPrimary
                                    )

                                    Text(
                                        text = info.releaseNotes,
                                        fontSize = 12.sp,
                                        color = CyberTextSecondary,
                                        lineHeight = 18.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.downloadUrl))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Could not open download link: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("download_update_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "DOWNLOAD UPDATE (v${info.versionName})",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = uiState is UpdateUiState.Error,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (uiState is UpdateUiState.Error) {
                            val errorMsg = (uiState as UpdateUiState.Error).message
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF261014),
                                border = BorderStroke(1.dp, CyberRed)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = errorMsg,
                                        fontSize = 11.5.sp,
                                        color = CyberTextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 3 — AUTO-UPDATE CHECK (स्वचालित अपडेट जांच)
            // ==========================================
            SectionHeader(
                icon = Icons.Default.SettingsSuggest,
                title = "Auto-Update Check",
                hindiTitle = "स्वचालित अपडेट जांच"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_update_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_update_switch_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ऐप खोलते समय अपडेट चेक करें",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Automatically check for new updates silently in the background when the app is launched.",
                                fontSize = 11.sp,
                                color = CyberTextMuted
                            )
                        }

                        Switch(
                            checked = isAutoUpdateEnabled,
                            onCheckedChange = { viewModel.setAutoUpdateCheckEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberRedBright,
                                uncheckedThumbColor = CyberTextMuted,
                                uncheckedTrackColor = Color(0xFF251B2A)
                            ),
                            modifier = Modifier.testTag("auto_update_switch")
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 4 — BATCH DETAILS & CHANNEL (बैच विवरण)
            // ==========================================
            SectionHeader(
                icon = Icons.Default.AutoAwesome,
                title = "Batch & Channel Details",
                hindiTitle = "बैच और चैनल विवरण"
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoRow(label = "Distribution Channel", value = "Production / Direct OTA")
                    HorizontalDivider(color = Color(0xFF2A1C28))
                    InfoRow(label = "Target Android SDK", value = "Android 15 (API 36)")
                    HorizontalDivider(color = Color(0xFF2A1C28))
                    InfoRow(label = "Architecture", value = "ARM64-v8a / x86_64")
                    HorizontalDivider(color = Color(0xFF2A1C28))
                    InfoRow(label = "Signing Certificate", value = "Verified AI Studio Signature")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
private fun InfoRow(
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

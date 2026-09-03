package com.example.presentation.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineMemoryStore
import com.example.domain.dispatcher.CommandDispatcher
import com.example.presentation.components.HolographicVoiceOrb
import com.example.presentation.viewmodel.AssistantStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// High-Tech Cyber Color Palette
private val ConsoleDarkBg = Color(0xFF070B11)
private val ConsoleCardBg = Color(0xFF0F1722)
private val ConsoleAccentCyan = Color(0xFF00E5FF)
private val ConsoleAccentRed = Color(0xFFFF1744)
private val ConsoleAccentGreen = Color(0xFF00E676)
private val ConsoleAccentOrange = Color(0xFFFF9100)
private val ConsoleTextPrimary = Color(0xFFF1F5F9)
private val ConsoleTextSecondary = Color(0xFF94A3B8)
private val ConsoleBorderColor = Color(0xFF1E293B)

data class ConsoleLogEntry(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()),
    val userInput: String,
    val spokenResponse: String,
    val actionType: String,
    val detail: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConsoleScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var savedOfflineName by remember { mutableStateOf(OfflineMemoryStore.getOfflineUserName(context)) }
    var inputQuery by remember { mutableStateOf("") }
    val consoleLogs = remember {
        mutableStateListOf(
            ConsoleLogEntry(
                userInput = "SYSTEM_INITIALIZE",
                spokenResponse = "Autonomous Offline Response & Memory Engine initialized. Ready for offline commands.",
                actionType = "ENGINE_BOOT",
                detail = "Local SharedPreferences storage active"
            )
        )
    }

    // Local Text-to-Speech Engine
    var isSpeaking by remember { mutableStateOf(false) }
    var lastSpokenText by remember { mutableStateOf("") }
    var ttsInstance by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        val tts = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale("hi", "IN")
            }
        }
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeaking = true
            }
            override fun onDone(utteranceId: String?) {
                isSpeaking = false
            }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeaking = false
            }
        })
        ttsInstance = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(text: String) {
        try {
            lastSpokenText = text
            isSpeaking = true
            val params = android.os.Bundle()
            ttsInstance?.speak(text, android.speech.tts.TextToSpeech.QUEUE_FLUSH, params, "console_tts")
        } catch (_: Exception) {
            isSpeaking = false
        }
    }

    fun executeQuery(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return

        val result = CommandDispatcher.dispatch(clean, context)
        // Refresh local memory display
        savedOfflineName = OfflineMemoryStore.getOfflineUserName(context)

        consoleLogs.add(
            ConsoleLogEntry(
                userInput = clean,
                spokenResponse = result.spokenResponse,
                actionType = result.actionType,
                detail = result.detail
            )
        )

        // Speak aloud through TTS
        speakText(result.spokenResponse)

        coroutineScope.launch {
            if (consoleLogs.isNotEmpty()) {
                listState.animateScrollToItem(consoleLogs.size - 1)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ConsoleDarkBg),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VOICE CONSOLE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ConsoleTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(ConsoleAccentGreen.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .border(1.dp, ConsoleAccentGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "OFFLINE ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ConsoleAccentGreen,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "Zero-API Autonomous Memory & Intent Engine",
                            fontSize = 11.sp,
                            color = ConsoleTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ConsoleTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ConsoleCardBg)
            )
        },
        bottomBar = {
            Surface(
                color = ConsoleCardBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_console_input"),
                        placeholder = {
                            Text(
                                "Enter voice / text command...",
                                fontSize = 13.sp,
                                color = ConsoleTextSecondary
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = ConsoleTextPrimary,
                            unfocusedTextColor = ConsoleTextPrimary,
                            focusedBorderColor = ConsoleAccentCyan,
                            unfocusedBorderColor = ConsoleBorderColor,
                            cursorColor = ConsoleAccentCyan,
                            focusedContainerColor = ConsoleDarkBg,
                            unfocusedContainerColor = ConsoleDarkBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputQuery.isNotBlank()) {
                                val q = inputQuery
                                inputQuery = ""
                                executeQuery(q)
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                val q = inputQuery
                                inputQuery = ""
                                executeQuery(q)
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(ConsoleAccentCyan, Color(0xFF0091EA)))
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ConsoleDarkBg)
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Central Circular Voice Console Orb (17-Year-Old Cyber Girl Avatar)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                HolographicVoiceOrb(
                    status = if (isSpeaking) AssistantStatus.Speaking(lastSpokenText) else AssistantStatus.Idle,
                    rmsDb = if (isSpeaking) 55f else 0f,
                    isSpeaking = isSpeaking,
                    onClick = {
                        executeQuery("SYSTEM_DIAGNOSTICS")
                    },
                    orbSize = 130.dp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 1. SharedPreferences Memory HUD Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ConsoleCardBg),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ConsoleBorderColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ConsoleAccentCyan.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Memory",
                                    tint = ConsoleAccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "OFFLINE_USER_NAME MEMORY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = ConsoleAccentCyan
                                )
                                Text(
                                    text = if (!savedOfflineName.isNullOrBlank()) "Saved: \"$savedOfflineName\"" else "Status: Not Registered (Offline Prompt Active)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (!savedOfflineName.isNullOrBlank()) ConsoleAccentGreen else ConsoleAccentOrange
                                )
                            }
                        }

                        if (!savedOfflineName.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    OfflineMemoryStore.clearOfflineUserName(context)
                                    savedOfflineName = null
                                    consoleLogs.add(
                                        ConsoleLogEntry(
                                            userInput = "CLEAR_OFFLINE_USER_NAME",
                                            spokenResponse = "Offline user name memory cleared from SharedPreferences.",
                                            actionType = "MEMORY_RESET",
                                            detail = "Cleared ${OfflineMemoryStore.KEY_OFFLINE_USER_NAME}"
                                        )
                                    )
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Name",
                                    tint = ConsoleAccentRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Quick Command Chips Row
            Text(
                text = "TEST OFFLINE COMMANDS (TAP TO EXECUTE):",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ConsoleTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            val quickCommands = listOf(
                "SYSTEM_DIAGNOSTICS" to ConsoleAccentCyan,
                "Mission Mode" to ConsoleAccentRed,
                "Mera naam Rahul hai" to ConsoleAccentCyan,
                "Mera naam kya hai" to ConsoleAccentGreen,
                "Main kaun hoon" to ConsoleAccentGreen,
                "Who am I" to ConsoleAccentGreen,
                "Flashlight on" to ConsoleAccentOrange,
                "Flashlight off" to ConsoleAccentOrange,
                "Battery status" to ConsoleAccentCyan,
                "Time kya hua hai" to ConsoleAccentCyan,
                "Play Store kholo" to ConsoleAccentCyan,
                "Open YouTube" to ConsoleAccentCyan,
                "Tell me a long sci-fi story" to ConsoleAccentRed
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickCommands.forEach { (cmd, color) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ConsoleCardBg)
                            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { executeQuery(cmd) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cmd,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Autonomous Execution Logs Console
            Text(
                text = "EXECUTION & RESPONSE STREAM:",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = ConsoleTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConsoleCardBg)
                    .border(1.dp, ConsoleBorderColor, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(consoleLogs, key = { it.id }) { log ->
                    ConsoleLogCard(log = log, onReplayTts = { speakText(log.spokenResponse) })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ConsoleLogCard(
    log: ConsoleLogEntry,
    onReplayTts: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ConsoleDarkBg),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, ConsoleBorderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(ConsoleAccentCyan.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = log.actionType,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = ConsoleAccentCyan
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.timestamp,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = ConsoleTextSecondary
                    )
                }

                IconButton(
                    onClick = onReplayTts,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Speak Aloud",
                        tint = ConsoleAccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "> User: ${log.userInput}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ConsoleTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "⚡ TTS: \"${log.spokenResponse}\"",
                fontSize = 12.sp,
                color = if (log.actionType == "OFFLINE_FALLBACK") ConsoleAccentOrange else ConsoleAccentGreen
            )

            if (log.detail.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Detail: ${log.detail}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ConsoleTextSecondary
                )
            }
        }
    }
}

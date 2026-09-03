package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.presentation.navigation.SahNajNavGraph
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.HistoryViewModel
import com.example.presentation.viewmodel.ProfileViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.presentation.viewmodel.SetupViewModel
import com.example.ui.theme.SahNajAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as SahNajApplication

        setContent {
            val assistantViewModel = remember {
                AssistantViewModel(
                    speechRecognizerManager = app.speechRecognizerManager,
                    textToSpeechManager = app.textToSpeechManager,
                    wakeWordEngine = app.wakeWordEngine,
                    localCommandParser = app.localCommandParser,
                    geminiRepository = app.geminiRepository,
                    actionValidator = app.actionValidator,
                    confirmationManager = app.confirmationManager,
                    actionExecutor = app.actionExecutor,
                    commandHistoryRepository = app.commandHistoryRepository,
                    userMemoryRepository = app.userMemoryRepository,
                    userPreferences = app.userPreferences,
                    permissionManager = app.permissionManager
                )
            }
            val authViewModel = remember {
                AuthViewModel(app.authRepository, app.userPreferences)
            }
            val settingsViewModel = remember {
                SettingsViewModel(app.userPreferences, app.userMemoryRepository, app.textToSpeechManager)
            }
            val profileViewModel = remember {
                ProfileViewModel(app.userPreferences, app.authRepository)
            }
            val historyViewModel = remember {
                HistoryViewModel(app.commandHistoryRepository)
            }
            val setupViewModel = remember {
                SetupViewModel(app.permissionManager, app.userPreferences)
            }

            val themeMode by settingsViewModel.theme.collectAsState()
            val navController = rememberNavController()

            SahNajAITheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SahNajNavGraph(
                        navController = navController,
                        app = app,
                        authViewModel = authViewModel,
                        assistantViewModel = assistantViewModel,
                        settingsViewModel = settingsViewModel,
                        profileViewModel = profileViewModel,
                        historyViewModel = historyViewModel,
                        setupViewModel = setupViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        handleWakeIntent(intent)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWakeIntent(intent)
    }

    private fun handleWakeIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("EXTRA_START_LISTENING", false) == true) {
            val app = application as SahNajApplication
            if (app.permissionManager.getPermissionStatus().hasMic) {
                app.speechRecognizerManager.startListening()
            }
        }
    }
}



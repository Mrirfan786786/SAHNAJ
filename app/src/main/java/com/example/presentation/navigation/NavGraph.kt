package com.example.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.SahNajApplication
import com.example.presentation.screens.AccessibilitySetupScreen
import com.example.presentation.screens.AssistantHomeScreen
import com.example.presentation.screens.BatterySetupScreen
import com.example.presentation.screens.ChatsScreen
import com.example.presentation.screens.CommandHistoryScreen
import com.example.presentation.screens.DashboardScreen
import com.example.presentation.screens.LiveVisionScreen
import com.example.presentation.screens.LoginScreen
import com.example.presentation.screens.OnboardingWalkthroughScreen
import com.example.presentation.screens.PermissionSetupScreen
import com.example.presentation.screens.PermissionsSettingsScreen
import com.example.presentation.screens.IntelligenceModesScreen
import com.example.presentation.screens.PrivacyPolicyScreen
import com.example.presentation.screens.ProfileScreen
import com.example.presentation.screens.SettingsScreen
import com.example.presentation.screens.SplashScreen
import com.example.presentation.screens.TriggersScreen
import com.example.presentation.screens.VoiceAndAiModelsScreen
import com.example.presentation.screens.OrbCustomizationScreen
import com.example.presentation.screens.ApiAndCloudSettingsScreen
import com.example.presentation.screens.ConnectorsScreen
import com.example.presentation.screens.PcConnectScreen
import com.example.presentation.screens.VoiceAuthenticationScreen
import com.example.presentation.screens.VoiceGuardianScreen
import com.example.presentation.screens.BatchUpdateScreen
import com.example.presentation.screens.LicenseActivationScreen
import com.example.presentation.screens.SubscriptionScreen
import com.example.presentation.viewmodel.ApiAndCloudViewModel
import com.example.presentation.viewmodel.VoiceAuthenticationViewModel
import com.example.presentation.viewmodel.BatchUpdateViewModel
import com.example.presentation.viewmodel.LicenseViewModel
import com.example.presentation.viewmodel.VisionScannerViewModel
import com.example.presentation.viewmodel.PromptStudioViewModel
import com.example.presentation.viewmodel.VoiceStudioViewModel
import com.example.presentation.viewmodel.MediaGenerationViewModel
import com.example.presentation.viewmodel.LiveVisionViewModel
import com.example.presentation.viewmodel.IntelligenceModesViewModel
import com.example.presentation.viewmodel.SubscriptionViewModel
import com.example.presentation.viewmodel.AssistantViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.HistoryViewModel
import com.example.presentation.viewmodel.ProfileViewModel
import com.example.presentation.viewmodel.SettingsViewModel
import com.example.presentation.viewmodel.SetupViewModel
import androidx.compose.runtime.remember

@Composable
fun SahNajNavGraph(
    navController: NavHostController,
    app: SahNajApplication,
    authViewModel: AuthViewModel,
    assistantViewModel: AssistantViewModel,
    settingsViewModel: SettingsViewModel,
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    setupViewModel: SetupViewModel
) {
    val apiAndCloudViewModel = remember {
        ApiAndCloudViewModel(
            userPreferences = app.userPreferences,
            geminiRepository = app.geminiRepository,
            authRepository = app.authRepository,
            cloudSyncManager = app.cloudSyncManager
        )
    }

    val batchUpdateViewModel = remember {
        BatchUpdateViewModel(
            userPreferences = app.userPreferences
        )
    }

    val voiceAuthenticationViewModel = remember {
        VoiceAuthenticationViewModel(
            userPreferences = app.userPreferences,
            voiceBiometricsEngine = app.voiceBiometricsEngine
        )
    }

    val licenseViewModel = remember {
        LicenseViewModel(
            userPreferences = app.userPreferences
        )
    }

    val subscriptionViewModel = remember {
        SubscriptionViewModel(
            userPreferences = app.userPreferences
        )
    }

    val mediaGenerationViewModel = remember {
        MediaGenerationViewModel(
            mediaRepository = app.mediaGenerationRepository
        )
    }

    val voiceStudioViewModel = remember {
        VoiceStudioViewModel(
            voiceRepository = app.voiceStudioRepository
        )
    }

    val visionScannerViewModel = remember {
        VisionScannerViewModel(
            visionScannerRepository = app.visionScannerRepository
        )
    }

    val promptStudioViewModel = remember {
        PromptStudioViewModel(
            promptStudioRepository = app.promptStudioRepository
        )
    }

    val liveVisionViewModel = remember {
        LiveVisionViewModel(
            liveVisionEngine = app.liveVisionEngine,
            speechRecognizerManager = app.speechRecognizerManager,
            textToSpeechManager = app.textToSpeechManager
        )
    }

    val intelligenceModesViewModel = remember {
        IntelligenceModesViewModel(
            userPreferences = app.userPreferences,
            geminiRepository = app.geminiRepository,
            userMemoryRepository = app.userMemoryRepository,
            textToSpeechManager = app.textToSpeechManager,
            dualVoiceEngine = app.dualVoiceEngine
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                isLoggedIn = authViewModel.isLoggedIn,
                isSetupCompleted = setupViewModel.isSetupCompleted(),
                hasSeenOnboarding = setupViewModel.hasSeenOnboarding(),
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.OnboardingWalkthrough.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToSetup = {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAssistant = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Login & Firebase Auth (Email/Pass + Google Sign-In + Phone OTP)
        composable(Screen.Login.route) {
            val navigatePostAuth = {
                if (!setupViewModel.hasSeenOnboarding()) {
                    navController.navigate(Screen.OnboardingWalkthrough.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else if (!setupViewModel.isSetupCompleted()) {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            LoginScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    profileViewModel.loadProfile()
                    navigatePostAuth()
                }
            )
        }

        // Onboarding Walkthrough (First launch only)
        composable(Screen.OnboardingWalkthrough.route) {
            OnboardingWalkthroughScreen(
                setupViewModel = setupViewModel,
                onNavigateToPermissionSetup = {
                    navController.navigate(Screen.PermissionSetup.route) {
                        popUpTo(Screen.OnboardingWalkthrough.route) { inclusive = true }
                    }
                }
            )
        }

        // Permission Setup Wizard
        composable(Screen.PermissionSetup.route) {
            PermissionSetupScreen(
                setupViewModel = setupViewModel,
                onNavigateToBatterySetup = {
                    navController.navigate(Screen.BatterySetup.route)
                },
                onSkipToHome = {
                    setupViewModel.completeSetup()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Battery Optimization & Autostart Setup
        composable(Screen.BatterySetup.route) {
            BatterySetupScreen(
                setupViewModel = setupViewModel,
                onNavigateToAccessibility = {
                    navController.navigate(Screen.AccessibilitySetup.route)
                },
                onSkipToDashboard = {
                    setupViewModel.completeSetup()
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Accessibility Setup Guide
        composable(Screen.AccessibilitySetup.route) {
            AccessibilitySetupScreen(
                setupViewModel = setupViewModel,
                onCompleteSetup = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.PermissionSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Central Dashboard Screen (Home)
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                assistantViewModel = assistantViewModel,
                settingsViewModel = settingsViewModel,
                historyViewModel = historyViewModel,
                profileViewModel = profileViewModel,
                onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                onNavigateToLiveVision = { navController.navigate(Screen.LiveVision.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToTriggers = { navController.navigate(Screen.Triggers.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToAutomotive = { navController.navigate(Screen.AutomotiveDiagnostics.route) },
                onNavigateToSecurityShield = { navController.navigate(Screen.SecurityShield.route) }
            )
        }

        // Live Multimodal Vision Assistant (Camera & Screen Share)
        composable(Screen.LiveVision.route) {
            LiveVisionScreen(
                viewModel = liveVisionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Chats Screen
        composable(Screen.Chat.route) {
            ChatsScreen(
                assistantViewModel = assistantViewModel,
                historyViewModel = historyViewModel,
                onNavigateToHome = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                onNavigateToTriggers = { navController.navigate(Screen.Triggers.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // Triggers Screen
        composable(Screen.Triggers.route) {
            TriggersScreen(
                assistantViewModel = assistantViewModel,
                onNavigateToHome = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToBatterySetup = { navController.navigate(Screen.BatterySetup.route) },
                onNavigateToAccessibilitySetup = { navController.navigate(Screen.AccessibilitySetup.route) }
            )
        }

        // Main Assistant Voice Screen
        composable(Screen.Assistant.route) {
            AssistantHomeScreen(
                assistantViewModel = assistantViewModel,
                mediaGenerationViewModel = mediaGenerationViewModel,
                voiceStudioViewModel = voiceStudioViewModel,
                visionScannerViewModel = visionScannerViewModel,
                promptStudioViewModel = promptStudioViewModel,
                onNavigateToHistory = { navController.navigate(Screen.CommandHistory.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToAccessibilitySetup = { navController.navigate(Screen.AccessibilitySetup.route) },
                onNavigateToDashboard = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) }
            )
        }

        // Command History Screen
        composable(Screen.CommandHistory.route) {
            CommandHistoryScreen(
                historyViewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // User Profile Screen
        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                assistantViewModel = assistantViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate(Screen.Dashboard.route) },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                onNavigateToTriggers = { navController.navigate(Screen.Triggers.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToPermissions = { navController.navigate(Screen.Permissions.route) },
                onNavigateToIntelligenceModes = { navController.navigate(Screen.IntelligenceModes.route) },
                onNavigateToVoiceAndAiModels = { navController.navigate(Screen.VoiceAndAiModels.route) },
                onNavigateToOrbCustomization = { navController.navigate(Screen.OrbCustomization.route) },
                onNavigateToApiAndCloudSettings = { navController.navigate(Screen.ApiAndCloudSettings.route) },
                onNavigateToConnectors = { navController.navigate(Screen.Connectors.route) },
                onNavigateToPcConnect = { navController.navigate(Screen.PcConnect.route) },
                onNavigateToLicenseActivation = { navController.navigate(Screen.LicenseActivation.route) },
                onNavigateToSubscription = { navController.navigate(Screen.Subscription.route) },
                onNavigateToVoiceAuthentication = { navController.navigate(Screen.VoiceAuthentication.route) },
                onNavigateToBatchUpdate = { navController.navigate(Screen.BatchUpdate.route) },
                onNavigateToVoiceConsole = { navController.navigate(Screen.VoiceConsole.route) }
            )
        }

        // Permissions Management Screen
        composable(Screen.Permissions.route) {
            PermissionsSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Intelligence & Modes Screen
        composable(Screen.IntelligenceModes.route) {
            IntelligenceModesScreen(
                viewModel = intelligenceModesViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Voice & AI Models Screen
        composable(Screen.VoiceAndAiModels.route) {
            VoiceAndAiModelsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Orb Customization Screen
        composable(Screen.OrbCustomization.route) {
            OrbCustomizationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // API & Cloud Settings Screen
        composable(Screen.ApiAndCloudSettings.route) {
            ApiAndCloudSettingsScreen(
                viewModel = apiAndCloudViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Connectors Screen
        composable(Screen.Connectors.route) {
            ConnectorsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // PC Connect Screen
        composable(Screen.PcConnect.route) {
            PcConnectScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // License Activation Screen
        composable(Screen.LicenseActivation.route) {
            LicenseActivationScreen(
                viewModel = licenseViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Subscription Screen
        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                viewModel = subscriptionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Voice Authentication & Voice Guardian Screen
        composable(Screen.VoiceAuthentication.route) {
            VoiceGuardianScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.VoiceGuardian.route) {
            VoiceGuardianScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Batch / Update Screen
        composable(Screen.BatchUpdate.route) {
            BatchUpdateScreen(
                viewModel = batchUpdateViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Privacy Policy & Security
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Automotive & Mechanical Diagnostics Screen
        composable(Screen.AutomotiveDiagnostics.route) {
            com.example.presentation.screens.AutomotiveDiagnosticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Security Shield & Emergency SOS Screen
        composable(Screen.SecurityShield.route) {
            com.example.presentation.screens.SecurityShieldScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Autonomous Offline Voice Console Screen
        composable(Screen.VoiceConsole.route) {
            com.example.presentation.screens.VoiceConsoleScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.repository.AuthRepository
import com.example.data.repository.CommandHistoryRepository
import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.GeminiRestRepository
import com.example.data.repository.RoomCommandHistoryRepository
import com.example.data.repository.RoomUserMemoryRepository
import com.example.data.repository.UserMemoryRepository
import com.example.domain.confirmation.ConfirmationManager
import com.example.domain.executor.ActionExecutor
import com.example.domain.parser.LocalCommandParser
import com.example.domain.resolvers.AppResolver
import com.example.domain.resolvers.ContactResolver
import com.example.domain.resolvers.SettingsNavigator
import com.example.domain.validator.ActionValidator
import com.example.permissions.PermissionManager
import com.example.voice.SpeechRecognizerManager
import com.example.voice.TextToSpeechManager
import com.example.voice.WakeWordEngine

class SahNajApplication : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var userPreferences: UserPreferences
        private set
    lateinit var commandHistoryRepository: CommandHistoryRepository
        private set
    lateinit var userMemoryRepository: UserMemoryRepository
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var geminiRepository: GeminiRepository
        private set
    lateinit var cloudSyncManager: com.example.data.repository.CloudSyncManager
        private set
    lateinit var mediaGenerationRepository: com.example.data.repository.MediaGenerationRepository
        private set
    lateinit var voiceStudioRepository: com.example.data.repository.VoiceStudioRepository
        private set
    lateinit var visionScannerRepository: com.example.data.repository.VisionScannerRepository
        private set
    lateinit var promptStudioRepository: com.example.data.repository.PromptStudioRepository
        private set
    lateinit var liveVisionEngine: com.example.domain.vision.LiveVisionEngine
        private set

    lateinit var localCommandParser: LocalCommandParser
        private set
    lateinit var actionValidator: ActionValidator
        private set
    lateinit var confirmationManager: ConfirmationManager
        private set
    lateinit var appResolver: AppResolver
        private set
    lateinit var contactResolver: ContactResolver
        private set
    lateinit var settingsNavigator: SettingsNavigator
        private set
    lateinit var actionExecutor: ActionExecutor
        private set

    lateinit var speechRecognizerManager: SpeechRecognizerManager
        private set
    lateinit var textToSpeechManager: TextToSpeechManager
        private set
    lateinit var dualVoiceEngine: com.example.voice.DualVoiceEngine
        private set
    lateinit var wakeWordEngine: WakeWordEngine
        private set
    lateinit var voiceBiometricsEngine: com.example.voice.VoiceBiometricsEngine
        private set
    lateinit var permissionManager: PermissionManager
        private set
    lateinit var handsFreeCallManager: com.example.services.HandsFreeCallManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        userPreferences = UserPreferences(this)

        if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
            try {
                com.google.firebase.FirebaseApp.initializeApp(this)
            } catch (_: Exception) {
                try {
                    com.google.firebase.FirebaseApp.initializeApp(
                        this,
                        com.google.firebase.FirebaseOptions.Builder()
                            .setApplicationId("1:329101568335:android:app")
                            .setApiKey("mockApiKey12345")
                            .setProjectId("sahnaj-ai")
                            .build()
                    )
                } catch (_: Exception) {}
            }
        }
        
        commandHistoryRepository = RoomCommandHistoryRepository(database.commandDao())
        userMemoryRepository = RoomUserMemoryRepository(database.userMemoryDao(), userPreferences)
        authRepository = FirebaseAuthRepository()
        geminiRepository = GeminiRestRepository(userPreferences)
        cloudSyncManager = com.example.data.repository.CloudSyncManager(userPreferences, database.userMemoryDao())
        mediaGenerationRepository = com.example.data.repository.MediaGenerationRepository(
            this,
            com.example.data.local.SecurePreferences(this)
        )
        voiceStudioRepository = com.example.data.repository.VoiceStudioRepository(
            this,
            com.example.data.local.SecurePreferences(this)
        )
        visionScannerRepository = com.example.data.repository.VisionScannerRepository(
            this,
            userPreferences,
            com.example.data.local.SecurePreferences(this)
        )
        promptStudioRepository = com.example.data.repository.PromptStudioRepository(
            this,
            userPreferences,
            com.example.data.local.SecurePreferences(this)
        )
        liveVisionEngine = com.example.domain.vision.LiveVisionEngine(
            this,
            userPreferences,
            com.example.data.local.SecurePreferences(this)
        )

        localCommandParser = LocalCommandParser()
        actionValidator = ActionValidator()
        confirmationManager = ConfirmationManager()
        appResolver = AppResolver(this)
        contactResolver = ContactResolver(this)
        settingsNavigator = SettingsNavigator(this)
        actionExecutor = ActionExecutor(this, appResolver, contactResolver, settingsNavigator)

        speechRecognizerManager = SpeechRecognizerManager(this)
        textToSpeechManager = TextToSpeechManager(this)
        dualVoiceEngine = com.example.voice.DualVoiceEngine(
            this,
            userPreferences,
            com.example.data.local.SecurePreferences(this),
            textToSpeechManager
        )
        wakeWordEngine = WakeWordEngine()
        voiceBiometricsEngine = com.example.voice.VoiceBiometricsEngine(this, userPreferences, wakeWordEngine)
        permissionManager = PermissionManager(this)
        com.example.util.PermanentChatMemoryEngine.init(this)
        handsFreeCallManager = com.example.services.HandsFreeCallManager(this).apply {
            registerCallListener()
        }
    }

    companion object {
        lateinit var instance: SahNajApplication
            private set
    }
}


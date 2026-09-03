package com.example.domain.executor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.example.accessibility.SahNajAccessibilityService
import com.example.data.model.ActionType
import com.example.data.model.ExecutionResult
import com.example.data.model.ResultStatus
import com.example.data.model.StructuredAction
import com.example.domain.automation.AppControlHub
import com.example.domain.automation.WhatsAppAutomation
import com.example.domain.resolvers.AppResolver
import com.example.domain.resolvers.ContactResolver
import com.example.domain.resolvers.SettingsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.provider.AlarmClock
import java.util.Calendar

class ActionExecutor(
    private val context: Context,
    private val appResolver: AppResolver,
    private val contactResolver: ContactResolver,
    private val settingsNavigator: SettingsNavigator,
    private val whatsAppAutomation: WhatsAppAutomation = WhatsAppAutomation(context, appResolver, contactResolver),
    private val appControlHub: AppControlHub = AppControlHub(context, appResolver)
) {

    suspend fun execute(action: StructuredAction): ExecutionResult = withContext(Dispatchers.Main) {
        when (action.action) {
            ActionType.SYSTEM_DIAGNOSTICS -> ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = com.example.util.SystemDiagnosticsHelper.buildCyberpunkDiagnosticsReport(context),
                detail = "Real-time system diagnostics generated"
            )
            ActionType.MORNING_BRIEFING -> ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = com.example.util.SystemDiagnosticsHelper.buildMorningBriefing(context),
                detail = "Morning briefing generated"
            )
            ActionType.NIGHT_ROUTINE -> ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = com.example.util.SystemDiagnosticsHelper.buildNightRoutine(context),
                detail = "Night routine activated"
            )
            ActionType.OPEN_APP -> executeOpenApp(action)
            ActionType.OPEN_SETTINGS -> executeOpenSettings(action)
            ActionType.DEVICE_SETTING -> executeDeviceSetting(action)
            ActionType.CALL_CONTACT -> executeCallContact(action)
            ActionType.DIAL_NUMBER -> executeDialNumber(action)
            ActionType.MAKE_CALL -> executeMakeCall(action)
            ActionType.SEND_SMS -> executeSendSms(action)
            ActionType.SEND_WHATSAPP_MESSAGE, ActionType.WHATSAPP_MESSAGE, ActionType.SEND_WHATSAPP -> executeWhatsAppMessage(action)
            ActionType.YOUTUBE_SEARCH, ActionType.PLAY_YOUTUBE -> executeYouTubeSearch(action)
            ActionType.WEB_SEARCH -> executeWebSearch(action)
            ActionType.SET_ALARM -> executeSetAlarm(action)
            ActionType.SET_REMINDER -> executeSetReminder(action)
            ActionType.FIND_AND_TAP -> executeFindAndTap(action)
            ActionType.FIND_AND_TYPE -> executeFindAndType(action)
            ActionType.GO_HOME -> executeAccessibilityAction(action) { it.doHome() }
            ActionType.GO_BACK -> executeAccessibilityAction(action) { it.doBack() }
            ActionType.OPEN_RECENTS -> executeAccessibilityAction(action) { it.doRecents() }
            ActionType.SCROLL_UP -> executeAccessibilityAction(action) { it.doScrollUp() }
            ActionType.SCROLL_DOWN -> executeAccessibilityAction(action) { it.doScrollDown() }
            ActionType.SWIPE_LEFT -> executeAccessibilityAction(action) { it.doSwipe(SahNajAccessibilityService.SwipeDirection.LEFT) }
            ActionType.SWIPE_RIGHT -> executeAccessibilityAction(action) { it.doSwipe(SahNajAccessibilityService.SwipeDirection.RIGHT) }
            ActionType.TAP_TEXT -> executeFindAndTap(action)
            ActionType.LONG_PRESS -> executeAccessibilityAction(action) { it.findAndLongPress(action.target) }
            ActionType.TYPE_TEXT -> executeFindAndType(action)
            ActionType.ANSWER_CALL -> {
                val app = context.applicationContext as? com.example.SahNajApplication
                val answered = app?.handsFreeCallManager?.answerCall() ?: false
                if (answered) {
                    ExecutionResult(
                        status = ResultStatus.SUCCESS,
                        spokenResponse = "Call accept kar li hai, boss.",
                        detail = "Accepted incoming call via TelecomManager"
                    )
                } else {
                    ExecutionResult(
                        status = ResultStatus.FAILURE,
                        spokenResponse = "Call accept karne ke liye phone call permission ya active call ki zaroorat hai.",
                        detail = "No ringing call or permission missing"
                    )
                }
            }
            ActionType.REJECT_CALL -> {
                val app = context.applicationContext as? com.example.SahNajApplication
                val rejected = app?.handsFreeCallManager?.rejectCall() ?: false
                if (rejected) {
                    ExecutionResult(
                        status = ResultStatus.SUCCESS,
                        spokenResponse = "Call cut kar di hai, boss.",
                        detail = "Declined incoming call"
                    )
                } else {
                    ExecutionResult(
                        status = ResultStatus.FAILURE,
                        spokenResponse = "Call reject nahi ho saki ya koi active ringing call nahi hai.",
                        detail = "Reject call failed"
                    )
                }
            }
            ActionType.EMERGENCY_SOS -> {
                val app = context.applicationContext as? com.example.SahNajApplication
                val sosReport = com.example.util.SecurityShieldManager.triggerEmergencySos(context, userPreferences = app?.userPreferences)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "Emergency alert trigger kar diya hai, boss. Location broadcast ho rahi hai.",
                    detail = sosReport
                )
            }
            ActionType.FAKE_SHUTDOWN -> {
                com.example.util.SecurityShieldManager.activateFakeShutdown()
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "Stealth shutdown engaged. System running in silent undercover mode.",
                    detail = "Fake shutdown activated"
                )
            }
            ActionType.AUTOMOTIVE_DIAGNOSTICS -> {
                val app = context.applicationContext as? com.example.SahNajApplication
                val diagResult = com.example.util.AutomotiveDiagnosticsEngine.analyzeAutomotiveIssue(
                    query = action.target.ifBlank { action.rawPrompt },
                    userPreferences = app?.userPreferences
                )
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = diagResult.take(300),
                    detail = diagResult
                )
            }
            ActionType.INSTALL_APP -> executeInstallApp(action)
            ActionType.READ_SCREEN_TEXT -> {
                val service = SahNajAccessibilityService.instance
                if (service != null) {
                    val text = service.extractScreenText()
                    if (text.isNotBlank()) {
                        ExecutionResult(
                            status = ResultStatus.SUCCESS,
                            spokenResponse = "Boss, screen content ye hai: $text",
                            detail = text
                        )
                    } else {
                        ExecutionResult(
                            status = ResultStatus.SUCCESS,
                            spokenResponse = "Boss, current screen par koi text detect nahi hua.",
                            detail = "No text found on active screen"
                        )
                    }
                } else {
                    ExecutionResult(
                        status = ResultStatus.FAILURE,
                        spokenResponse = "Boss, screen reading ke liye Accessibility permission enable karni hogi."
                    )
                }
            }
            ActionType.OPEN_NOTIFICATION_PANEL -> executeAccessibilityAction(action) { it.doNotifications() }
            ActionType.OPEN_QUICK_SETTINGS -> executeAccessibilityAction(action) { it.doQuickSettings() }
            ActionType.STOP_ACTION -> ExecutionResult(
                status = ResultStatus.CANCELLED,
                spokenResponse = "Action cancel kar diya."
            )
            ActionType.ASK_CONFIRMATION -> ExecutionResult(
                status = ResultStatus.REQUIRES_CONFIRMATION,
                spokenResponse = action.spokenResponse.ifEmpty { "Kya aap aage badhna chahte hain?" },
                pendingAction = action
            )
            ActionType.SPEAK_TEXT -> ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = action.spokenResponse,
                detail = "Spoken conversational response: ${action.spokenResponse}"
            )
            ActionType.GENERAL_QUESTION, ActionType.GENERAL_QNA -> ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = action.spokenResponse,
                detail = "General Q&A answer: ${action.spokenResponse}"
            )
            ActionType.OPEN_PAYWALL, ActionType.SUBSCRIPTION_QUERY -> ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = action.spokenResponse.ifEmpty { "Hamare paas 1 Month (Rs 99), 3 Months (Rs 279), 1 Year (Rs 999) aur Lifetime VIP (Rs 1499) plans hain. Main payment screen open kar rahi hoon." },
                detail = "Opening Subscription & Paywall screen"
            )
            ActionType.UNKNOWN -> ExecutionResult(
                status = ResultStatus.NOT_SUPPORTED,
                spokenResponse = action.spokenResponse.ifEmpty { "Maaf kijiye, main yeh command samajh nahi paayi. Kripya dobara boliye." }
            )
        }
    }

    private fun executeMakeCall(action: StructuredAction): ExecutionResult {
        val hasDigits = action.target.any { it.isDigit() }
        return if (hasDigits) {
            executeDialNumber(action)
        } else {
            executeCallContact(action)
        }
    }

    private fun executeDeviceSetting(action: StructuredAction): ExecutionResult {
        val targetLower = action.target.lowercase().trim()
        val valueStr = (action.value ?: action.parameters["setting_state"] ?: action.parameters["value"] ?: "").uppercase().trim()

        // 1. Torch / Flashlight toggle
        if (targetLower.contains("torch") || targetLower.contains("flashlight") || targetLower.contains("flash")) {
            return try {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                    val chars = cameraManager.getCameraCharacteristics(id)
                    chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                } ?: cameraManager.cameraIdList.firstOrNull()

                if (cameraId != null) {
                    val turnOn = when {
                        valueStr == "ON" || valueStr.contains("ON") || valueStr == "TRUE" -> true
                        valueStr == "OFF" || valueStr.contains("OFF") || valueStr == "FALSE" -> false
                        else -> !isTorchCurrentlyOn
                    }
                    cameraManager.setTorchMode(cameraId, turnOn)
                    isTorchCurrentlyOn = turnOn
                    val resp = if (turnOn) "Done, boss. Flashlight on kar di hai." else "Done, boss. Flashlight off kar di hai."
                    ExecutionResult(
                        status = ResultStatus.SUCCESS,
                        spokenResponse = action.spokenResponse.ifEmpty { resp },
                        detail = "Torch set to $turnOn"
                    )
                } else {
                    executeOpenSettings(action)
                }
            } catch (e: Exception) {
                executeOpenSettings(action)
            }
        }

        // 2. Volume control
        if (targetLower.contains("volume") || targetLower.contains("awaz") || targetLower.contains("sound")) {
            return try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val targetVol = when {
                    valueStr.contains("100") || valueStr.contains("FULL") || valueStr.contains("MAX") -> maxVol
                    valueStr.contains("0") || valueStr.contains("MUTE") || valueStr.contains("SILENT") -> 0
                    valueStr.contains("50") || valueStr.contains("HALF") -> maxVol / 2
                    valueStr.contains("UP") || valueStr.contains("BADHAO") -> (currentVol + (maxVol / 5)).coerceAtMost(maxVol)
                    valueStr.contains("DOWN") || valueStr.contains("KAM") -> (currentVol - (maxVol / 5)).coerceAtLeast(0)
                    else -> {
                        val num = valueStr.filter { it.isDigit() }.toIntOrNull()
                        if (num != null) (num * maxVol) / 100 else maxVol
                    }
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
                val pct = if (maxVol > 0) (targetVol * 100) / maxVol else 0
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = action.spokenResponse.ifEmpty { "Confirmed, sir. Volume set to $pct%." },
                    detail = "Volume set to $targetVol/$maxVol"
                )
            } catch (e: Exception) {
                executeOpenSettings(action)
            }
        }

        // 3. Brightness & Display Settings
        if (targetLower.contains("brightness") || targetLower.contains("display") || targetLower.contains("screen")) {
            return executeOpenSettings(action.copy(target = "DISPLAY"))
        }

        // 4. Wi-Fi & Bluetooth
        if (targetLower.contains("wifi") || targetLower.contains("wi-fi")) {
            return executeOpenSettings(action.copy(target = "WIFI"))
        }
        if (targetLower.contains("bluetooth") || targetLower.contains("bt")) {
            return executeOpenSettings(action.copy(target = "BLUETOOTH"))
        }

        // Fallback to opening relevant Android settings screen
        return executeOpenSettings(action)
    }

    companion object {
        private var isTorchCurrentlyOn = false
    }

    private fun executeSetAlarm(action: StructuredAction): ExecutionResult {
        return try {
            val rawTime = action.target.ifEmpty { action.value ?: "" }
            var hour = 6
            var minute = 0
            var isPm = rawTime.contains("PM", ignoreCase = true) || rawTime.contains("shaam", ignoreCase = true) || rawTime.contains("raat", ignoreCase = true)

            val digits = rawTime.filter { it.isDigit() || it == ':' }
            if (digits.contains(":")) {
                val parts = digits.split(":")
                hour = parts.getOrNull(0)?.toIntOrNull() ?: 6
                minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            } else {
                val num = digits.toIntOrNull()
                if (num != null) {
                    hour = num
                }
            }

            if (isPm && hour < 12) hour += 12
            if (!isPm && hour == 12 && rawTime.contains("AM", ignoreCase = true)) hour = 0

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, "SAHNAJ AI Alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = action.spokenResponse.ifEmpty { "Alarm set kar diya gaya hai." },
                detail = "Alarm set for %02d:%02d".format(hour, minute)
            )
        } catch (e: Exception) {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Alarm set karne mein problem aayi.",
                detail = e.localizedMessage
            )
        }
    }

    private fun executeSetReminder(action: StructuredAction): ExecutionResult {
        return try {
            val raw = action.target.ifEmpty { action.value ?: "" }
            val minutes = raw.filter { it.isDigit() }.toIntOrNull() ?: 5
            val seconds = minutes * 60

            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, "SAHNAJ AI Timer")
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = action.spokenResponse.ifEmpty { "Timer set kar diya." },
                detail = "Timer set for $minutes minutes"
            )
        } catch (e: Exception) {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Timer set karne mein dikkat aayi.",
                detail = e.localizedMessage
            )
        }
    }

    private suspend fun executeWhatsAppMessage(action: StructuredAction): ExecutionResult {
        val contact = action.target
        val message = action.parameters["message"] ?: ""
        val autoSend = action.parameters["send"]?.toBoolean() ?: true
        return whatsAppAutomation.executeWhatsAppMessagePipeline(
            contactName = contact,
            messageText = message,
            shouldSendImmediately = autoSend
        )
    }

    private suspend fun executeYouTubeSearch(action: StructuredAction): ExecutionResult {
        val query = action.target
        return appControlHub.executeYouTubeSearch(query)
    }

    private suspend fun executeWebSearch(action: StructuredAction): ExecutionResult {
        val query = action.target
        return appControlHub.executeWebSearch(query)
    }

    private suspend fun executeFindAndTap(action: StructuredAction): ExecutionResult {
        val target = action.target
        return appControlHub.executeGenericTap(target)
    }

    private suspend fun executeFindAndType(action: StructuredAction): ExecutionResult {
        val textToType = action.parameters["text"] ?: action.target
        val fieldHint = action.parameters["hint"]
        return appControlHub.executeGenericType(textToType, fieldHint)
    }

    private fun executeOpenApp(action: StructuredAction): ExecutionResult {
        val app = appResolver.findApp(action.target)
        return if (app != null) {
            val launched = appResolver.launchApp(app.packageName)
            if (launched) {
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "Ji, ${app.appName} open ho gaya.",
                    detail = "Launched ${app.appName} (${app.packageName})"
                )
            } else {
                ExecutionResult(
                    status = ResultStatus.FAILURE,
                    spokenResponse = "${app.appName} launch karne mein samasya aayi.",
                    detail = "Failed to launch ${app.packageName}"
                )
            }
        } else {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "${action.target} aapke phone mein installed nahi mila.",
                detail = "Application '${action.target}' not found on device"
            )
        }
    }

    private fun executeOpenSettings(action: StructuredAction): ExecutionResult {
        val settingsType = settingsNavigator.parseSettingsType(action.target)
        val opened = settingsNavigator.openSettings(settingsType)
        return if (opened) {
            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "${action.target} settings khol diya hai.",
                detail = "Opened $settingsType settings"
            )
        } else {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Settings open karne mein problem aayi.",
                detail = "Could not open settings for ${action.target}"
            )
        }
    }

    private fun executeCallContact(action: StructuredAction): ExecutionResult {
        val contacts = contactResolver.findContactsByName(action.target)
        if (contacts.isEmpty()) {
            return ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "${action.target} naam ka koi contact nahi mila.",
                detail = "No contact found matching '${action.target}'"
            )
        }

        if (contacts.size > 1) {
            val contactNames = contacts.take(3).joinToString(", ") { "${it.name} (${it.phoneNumber})" }
            return ExecutionResult(
                status = ResultStatus.REQUIRES_CONFIRMATION,
                spokenResponse = "Multiple contacts mile: $contactNames. Kisko call lagana hai?",
                detail = "Multiple contacts found: $contactNames"
            )
        }

        val targetContact = contacts.first()
        return makePhoneCall(targetContact.phoneNumber, targetContact.name)
    }

    private fun executeDialNumber(action: StructuredAction): ExecutionResult {
        val digits = action.target.filter { it.isDigit() || it == '+' }
        if (digits.length < 3) {
            return ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Phone number sahi nahi hai.",
                detail = "Invalid phone number: ${action.target}"
            )
        }
        return makePhoneCall(digits, digits)
    }

    private fun makePhoneCall(number: String, displayName: String): ExecutionResult {
        val hasCallPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        return try {
            val intent = if (hasCallPermission) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "$displayName ko call lagaya ja raha hai.",
                detail = "Initiated call to $number"
            )
        } catch (e: Exception) {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Call lagane mein truti hui.",
                detail = e.localizedMessage
            )
        }
    }

    private fun executeSendSms(action: StructuredAction): ExecutionResult {
        val message = action.parameters["message"] ?: ""
        if (message.isBlank()) {
            return ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "SMS ka message text khali hai.",
                detail = "Empty SMS body"
            )
        }

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        // Resolve phone number if contact name provided
        var recipientNumber = action.target
        var recipientDisplayName = action.target
        if (!action.target.any { it.isDigit() }) {
            val contacts = contactResolver.findContactsByName(action.target)
            if (contacts.isNotEmpty()) {
                recipientNumber = contacts.first().phoneNumber
                recipientDisplayName = contacts.first().name
            }
        }

        return try {
            if (hasSmsPermission) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(recipientNumber, null, message, null, null)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "$recipientDisplayName ko SMS bhej diya gaya.",
                    detail = "SMS sent to $recipientNumber"
                )
            } else {
                // Graceful fallback when SMS permission is denied: Open default SMS app pre-filled
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:$recipientNumber")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "SMS permission nahi hai, isliye main SMS app open kar rahi hoon, aap wahan se bhej sakte hain.",
                    detail = "Fallback: Opened SMS app prefilled for $recipientNumber"
                )
            }
        } catch (e: Exception) {
            // Further fallback to open SMS composer if direct intent fails
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$recipientNumber")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "SMS app open kar diya hai, aap bhej dijiye.",
                    detail = "Fallback to generic SMS VIEW intent"
                )
            } catch (fallbackError: Exception) {
                ExecutionResult(
                    status = ResultStatus.FAILURE,
                    spokenResponse = "SMS bhejne mein samasya hui.",
                    detail = e.localizedMessage ?: fallbackError.localizedMessage
                )
            }
        }
    }

    private fun executeAccessibilityAction(
        action: StructuredAction,
        serviceAction: (SahNajAccessibilityService) -> Boolean
    ): ExecutionResult {
        val service = SahNajAccessibilityService.instance
        if (service == null) {
            return ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "SahNaj accessibility service chalu nahi hai. Kripya settings mein jakar on karein.",
                detail = "Accessibility Service is not running"
            )
        }

        val success = serviceAction(service)
        return if (success) {
            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "Action poora ho gaya.",
                detail = "Executed accessibility action: ${action.action}"
            )
        } else {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Action poora nahi ho saka.",
                detail = "Accessibility action failed to execute"
            )
        }
    }

    private fun executeInstallApp(action: StructuredAction): ExecutionResult {
        val appName = action.target.ifBlank { action.parameters["app_name"] ?: "" }.trim()
        
        return try {
            val intent = if (appName.isBlank() || appName.equals("Play Store", ignoreCase = true)) {
                // Open Play Store Home
                context.packageManager.getLaunchIntentForPackage("com.android.vending")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            } else {
                // Open Play Store search or direct app page
                val query = Uri.encode(appName)
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/search?q=$query&c=apps")
                    setPackage("com.android.vending")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback without restricting package to com.android.vending
                val fallbackQuery = if (appName.isNotBlank() && !appName.equals("Play Store", ignoreCase = true)) {
                    Uri.encode(appName)
                } else {
                    ""
                }
                val webUrl = if (fallbackQuery.isNotBlank()) {
                    "https://play.google.com/store/search?q=$fallbackQuery&c=apps"
                } else {
                    "https://play.google.com/store/apps"
                }
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
            }

            // Safe accessibility helper invocation with try-catch
            try {
                SahNajAccessibilityService.instance?.scheduleAutoInstallClick()
            } catch (_: Exception) {
                // Ignored - accessibility is optional
            }

            val spoken = if (appName.isNotBlank() && !appName.equals("Play Store", ignoreCase = true)) {
                "Play Store par $appName khol diya hai, boss."
            } else {
                "Google Play Store khol diya hai, boss."
            }

            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = action.spokenResponse.ifBlank { spoken },
                detail = "Play Store intent launched successfully for target: $appName"
            )
        } catch (e: Exception) {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Play Store open nahi ho saka.",
                detail = "Failed to launch Play Store intent: ${e.message}"
            )
        }
    }
}

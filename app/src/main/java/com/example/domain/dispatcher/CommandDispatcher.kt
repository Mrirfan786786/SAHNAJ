package com.example.domain.dispatcher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import com.example.data.local.OfflineMemoryStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Autonomous Offline Command Dispatcher & Memory Engine.
 * Operates 100% locally on-device without requiring any external Gemini API key or internet connection.
 * Handles:
 * 1. SharedPreferences offline memory ("OFFLINE_USER_NAME")
 * 2. Identity recall ("Main kaun hoon", "Mera naam kya hai", "Who am I")
 * 3. Local Android device intents (Flashlight, Play Store, Launch apps, Battery, Time/Date)
 * 4. Polite offline mode guidance for arbitrary complex chat queries.
 */
object CommandDispatcher {

    const val OFFLINE_FALLBACK_MESSAGE = "Main abhi offline mode mein hoon. AI se lambi baat karne ke liye Settings mein Gemini API Key enter karein."
    const val NAME_SAVED_MESSAGE = "Theek hai, maine aapka naam save kar liya hai."
    const val NAME_UNKNOWN_PROMPT = "Mujhe abhi aapka naam nahi pata. Aap boliye 'Mera naam [Aapka Naam] hai' aur main save kar lungi."

    // In-memory toggle state tracker for flashlight
    private var isFlashlightOn: Boolean = false

    data class DispatchResult(
        val handled: Boolean,
        val spokenResponse: String,
        val actionType: String,
        val detail: String = ""
    )

    /**
     * Autonomous offline intent dispatch entry point.
     */
    fun dispatch(rawSpeech: String, context: Context): DispatchResult {
        val trimmed = rawSpeech.trim()
        if (trimmed.isBlank()) {
            return DispatchResult(
                handled = true,
                spokenResponse = "Ji boss, kahiye. Main sun rahi hoon.",
                actionType = "WAKE"
            )
        }

        val cleanSpeech = sanitizeSpeech(trimmed)
        val lower = cleanSpeech.lowercase(Locale.ROOT)

        // 1. Check: Offline Name Saving ("mera naam ... hai", "my name is ...")
        val savedName = parseAndSaveName(cleanSpeech, context)
        if (savedName != null) {
            return DispatchResult(
                handled = true,
                spokenResponse = NAME_SAVED_MESSAGE,
                actionType = "SAVE_OFFLINE_USER_NAME",
                detail = "Saved name: $savedName under ${OfflineMemoryStore.KEY_OFFLINE_USER_NAME}"
            )
        }

        // 2. Check: User Identity Query ("Main kaun hoon", "Mera naam kya hai", "Who am I")
        if (isIdentityQuery(lower)) {
            val identityReply = getIdentityResponse(context)
            return DispatchResult(
                handled = true,
                spokenResponse = identityReply,
                actionType = "QUERY_USER_IDENTITY",
                detail = "Recalled offline name"
            )
        }

        // 2.5 Check: SYSTEM_DIAGNOSTICS Action
        if (isDiagnosticsCommand(lower) || trimmed.equals("SYSTEM_DIAGNOSTICS", ignoreCase = true)) {
            val report = com.example.util.SystemDiagnosticsHelper.buildCyberpunkDiagnosticsReport(context)
            return DispatchResult(
                handled = true,
                spokenResponse = report,
                actionType = "SYSTEM_DIAGNOSTICS",
                detail = "Cyberpunk system diagnostics scan complete"
            )
        }

        // 3. Check: Flashlight / Torch toggle
        if (isFlashlightCommand(lower)) {
            val forceOn = when {
                lower.contains("chalu") || lower.contains("on") || lower.contains("start") || lower.contains("jalao") -> true
                lower.contains("band") || lower.contains("off") || lower.contains("stop") || lower.contains("bujhao") -> false
                else -> null // toggle
            }
            val (success, state) = toggleFlashlight(context, forceOn)
            val msg = if (success) {
                if (state) "Flashlight on kar di hai." else "Flashlight off kar di hai."
            } else {
                "Flashlight control karne mein dikkat aayi."
            }
            return DispatchResult(
                handled = true,
                spokenResponse = msg,
                actionType = "FLASHLIGHT_TOGGLE",
                detail = "Flashlight state: $state"
            )
        }

        // 4. Check: Battery status
        if (isBatteryCommand(lower)) {
            val batteryReport = getBatteryStatus(context)
            return DispatchResult(
                handled = true,
                spokenResponse = batteryReport,
                actionType = "BATTERY_STATUS",
                detail = "Checked battery status"
            )
        }

        // 5. Check: Time or Date
        if (isTimeOrDateCommand(lower)) {
            val isTime = !lower.contains("date") && !lower.contains("taarikh") && !lower.contains("din")
            val report = getTimeOrDate(context, isTime)
            return DispatchResult(
                handled = true,
                spokenResponse = report,
                actionType = if (isTime) "GET_TIME" else "GET_DATE",
                detail = report
            )
        }

        // 6. Check: Open Play Store
        if (isPlayStoreCommand(lower)) {
            val appTarget = extractPlayStoreAppQuery(cleanSpeech)
            val success = openPlayStore(context, appTarget)
            val msg = if (success) {
                if (appTarget.isNotBlank()) "Play Store par $appTarget khol diya hai." else "Play Store open kar diya hai."
            } else {
                "Play Store open nahi ho saka."
            }
            return DispatchResult(
                handled = true,
                spokenResponse = msg,
                actionType = "OPEN_PLAY_STORE",
                detail = "Target: $appTarget"
            )
        }

        // 7. Check: Launch installed apps ("Open [App]", "[App] kholo")
        val appNameToLaunch = extractAppLaunchTarget(cleanSpeech, lower)
        if (!appNameToLaunch.isNullOrBlank()) {
            val launched = launchApp(context, appNameToLaunch)
            if (launched) {
                return DispatchResult(
                    handled = true,
                    spokenResponse = "$appNameToLaunch open kar diya hai.",
                    actionType = "LAUNCH_APP",
                    detail = "Launched $appNameToLaunch"
                )
            }
        }

        // 8. Polite Offline Fallback for unknown chat queries
        return DispatchResult(
            handled = false,
            spokenResponse = OFFLINE_FALLBACK_MESSAGE,
            actionType = "OFFLINE_FALLBACK",
            detail = "Unknown offline query fallback engaged"
        )
    }

    /**
     * Parses and permanently stores the user's name if the speech matches
     * "mera naam ... hai" or "my name is ...".
     */
    fun parseAndSaveName(rawSpeech: String, context: Context): String? {
        val clean = sanitizeSpeech(rawSpeech)
        val name = extractNameFromSpeech(clean)
        if (name != null) {
            OfflineMemoryStore.saveOfflineUserName(context, name)
            return name
        }
        return null
    }

    /**
     * Checks whether the speech contains a name declaration.
     */
    fun isNameRegistration(rawSpeech: String): Boolean {
        val clean = sanitizeSpeech(rawSpeech)
        return extractNameFromSpeech(clean) != null
    }

    /**
     * Extracts name component using regex patterns for Hindi, English, and Hinglish.
     */
    private fun extractNameFromSpeech(clean: String): String? {
        // Pattern 1: mera naam [Name] hai / he / h
        val p1 = Regex("""(?:mera\s+naam|mera\s+name|my\s+name)(?:\s+hai)?\s+([a-zA-Z\u0900-\u097F\s]+?)(?:\s+(?:hai|he|h))?$""", RegexOption.IGNORE_CASE)
        val m1 = p1.find(clean)
        if (m1 != null) {
            val candidate = cleanNameString(m1.groupValues[1])
            if (isValidNameCandidate(candidate)) return candidate
        }

        // Pattern 2: my name is [Name]
        val p2 = Regex("""my\s+name\s+is\s+([a-zA-Z\u0900-\u097F\s]+)$""", RegexOption.IGNORE_CASE)
        val m2 = p2.find(clean)
        if (m2 != null) {
            val candidate = cleanNameString(m2.groupValues[1])
            if (isValidNameCandidate(candidate)) return candidate
        }

        // Pattern 3: mera naam [Name] (without trailing hai)
        val p3 = Regex("""(?:mera\s+naam|mera\s+name)\s+([a-zA-Z\u0900-\u097F\s]+)$""", RegexOption.IGNORE_CASE)
        val m3 = p3.find(clean)
        if (m3 != null) {
            val candidate = cleanNameString(m3.groupValues[1])
            if (isValidNameCandidate(candidate)) return candidate
        }

        return null
    }

    private fun cleanNameString(raw: String): String {
        return raw.replace(Regex("""(?i)\b(hai|he|h|sahnaj|shahnaz|assistant|bhai|ji|sir|boss)\b"""), "")
            .replace(Regex("""[.,!?:;]+"""), "")
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }
    }

    private fun isValidNameCandidate(name: String): Boolean {
        if (name.length < 2 || name.length > 30) return false
        val lower = name.lowercase()
        val blacklisted = setOf("kya", "kaun", "kuch", "nothing", "what", "who", "user", "admin", "null", "undefined")
        return !blacklisted.contains(lower)
    }

    /**
     * Checks if the user is asking "Main kaun hoon", "Mera naam kya hai", or "Who am I".
     */
    fun isIdentityQuery(lowerSpeech: String): Boolean {
        val patterns = listOf(
            "main kaun hoon",
            "main kaun hu",
            "mai kaun hu",
            "mai kaun hoon",
            "who am i",
            "mera naam kya hai",
            "mera naam kya he",
            "mera name kya hai",
            "mera name kya he",
            "what is my name",
            "kya aapko mera naam pata hai",
            "mera kya naam hai",
            "batao mera naam kya hai",
            "batao main kaun hoon"
        )
        return patterns.any { lowerSpeech.contains(it) }
    }

    /**
     * Returns the identity response based on "OFFLINE_USER_NAME".
     */
    fun getIdentityResponse(context: Context): String {
        val savedName = OfflineMemoryStore.getOfflineUserName(context)
        return if (!savedName.isNullOrBlank()) {
            "Aapka naam $savedName hai."
        } else {
            NAME_UNKNOWN_PROMPT
        }
    }

    /**
     * System Diagnostics check.
     */
    fun isDiagnosticsCommand(lower: String): Boolean {
        return lower == "system_diagnostics" ||
                lower == "system diagnostics" ||
                lower == "diagnostics" ||
                lower.contains("mission mode") ||
                lower.contains("mission_mode") ||
                lower.contains("system diagnostic") ||
                lower.contains("system scan") ||
                lower.contains("subsystem check") ||
                lower.contains("system status") ||
                lower.contains("device status") ||
                lower.contains("run diagnostics") ||
                lower.contains("diagnostic check") ||
                lower.contains("hardware status") ||
                lower.contains("system check")
    }

    /**
     * Flashlight check.
     */
    fun isFlashlightCommand(lower: String): Boolean {
        return (lower.contains("torch") || lower.contains("flashlight") || lower.contains("flash light") || lower.contains("flash")) &&
                (lower.contains("on") || lower.contains("off") || lower.contains("chalu") || lower.contains("band") ||
                        lower.contains("toggle") || lower.contains("jalao") || lower.contains("bujhao") || lower.contains("kholo"))
    }

    /**
     * Toggles or sets device torch mode using Android CameraManager.
     */
    fun toggleFlashlight(context: Context, forceState: Boolean? = null): Pair<Boolean, Boolean> {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (cameraManager == null) return Pair(false, false)

            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return Pair(false, false)
            val newState = forceState ?: !isFlashlightOn
            cameraManager.setTorchMode(cameraId, newState)
            isFlashlightOn = newState
            Pair(true, newState)
        } catch (e: Exception) {
            Pair(false, false)
        }
    }

    /**
     * Battery check.
     */
    fun isBatteryCommand(lower: String): Boolean {
        return lower.contains("battery") || lower.contains("battery status") ||
                lower.contains("battery kitni hai") || lower.contains("charge kitna hai") ||
                lower.contains("battery percentage") || lower.contains("battery percent")
    }

    /**
     * Reads battery status using Android BatteryManager system API.
     */
    fun getBatteryStatus(context: Context): String {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, intentFilter)
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) (level * 100) / scale else 50
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            val stateText = if (isCharging) "charging par hai" else "battery par chal raha hai"
            "Battery status, boss: Level $pct% hai aur abhi $stateText."
        } catch (e: Exception) {
            "Battery level read karne mein samasya hui."
        }
    }

    /**
     * Time and date check.
     */
    fun isTimeOrDateCommand(lower: String): Boolean {
        return lower.contains("time kya hai") || lower.contains("time kya hua") ||
                lower.contains("what time is it") || lower.contains("kya samay hua") ||
                lower.contains("samay kya hai") || lower.contains("aaj ki date") ||
                lower.contains("taarikh kya hai") || lower.contains("what is the date") ||
                lower.contains("aaj kaun sa din hai") || lower.contains("today's date")
    }

    /**
     * Formats current time or date from system clock.
     */
    fun getTimeOrDate(context: Context, isTime: Boolean): String {
        val now = Date()
        return if (isTime) {
            val timeFmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
            "Abhi samay ${timeFmt.format(now)} hua hai."
        } else {
            val dateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            "Aaj ki taarikh ${dateFmt.format(now)} hai."
        }
    }

    /**
     * Play Store check.
     */
    fun isPlayStoreCommand(lower: String): Boolean {
        return lower.contains("play store") || lower.contains("playstore") ||
                lower.contains("install app") || lower.contains("app store")
    }

    private fun extractPlayStoreAppQuery(raw: String): String {
        var query = raw.replace(Regex("""(?i)\b(open|kholo|chalu karo|search karo|par|pe|play store|playstore|install karo|download karo)\b"""), "").trim()
        return query.replace(Regex("""[.,!?:;]+"""), "").trim()
    }

    /**
     * Launches Play Store home or search page using Android Intents.
     */
    fun openPlayStore(context: Context, appQuery: String = ""): Boolean {
        return try {
            val intent = if (appQuery.isBlank()) {
                context.packageManager.getLaunchIntentForPackage("com.android.vending")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps"))
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=${Uri.encode(appQuery)}&c=apps")).apply {
                    setPackage("com.android.vending")
                }
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            // Fallback to web browser
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Extracts app target name from phrases like "open whatsapp", "youtube kholo".
     */
    fun extractAppLaunchTarget(raw: String, lower: String): String? {
        val triggers = listOf("open ", "kholo ", "launch ", "chalu karo ")
        for (trig in triggers) {
            if (lower.startsWith(trig)) {
                val app = raw.substring(trig.length).trim()
                return cleanAppTarget(app)
            }
        }
        if (lower.endsWith(" kholo") || lower.endsWith(" open")) {
            val app = raw.replace(Regex("""(?i)\b(kholo|open|chalu karo)\b"""), "").trim()
            return cleanAppTarget(app)
        }
        return null
    }

    private fun cleanAppTarget(raw: String): String {
        return raw.replace(Regex("""[.,!?:;]+"""), "").trim()
    }

    /**
     * Launches an installed application by resolving package matching the target name.
     */
    fun launchApp(context: Context, appName: String): Boolean {
        val clean = appName.lowercase().trim()
        val pm = context.packageManager

        // Fast mapping for popular packages
        val directPackage = when {
            clean.contains("whatsapp") -> "com.whatsapp"
            clean.contains("youtube") -> "com.google.android.youtube"
            clean.contains("chrome") -> "com.android.chrome"
            clean.contains("play store") || clean.contains("playstore") -> "com.android.vending"
            clean.contains("settings") -> "com.android.settings"
            clean.contains("maps") -> "com.google.android.apps.maps"
            clean.contains("gmail") -> "com.google.android.gm"
            clean.contains("camera") -> {
                val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                    return true
                } catch (_: Exception) {
                    null
                }
            }
            else -> null
        }

        if (directPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(directPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return true
            }
        }

        // Generic search through installed applications
        try {
            val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in installedApps) {
                val label = pm.getApplicationLabel(appInfo).toString().lowercase()
                if (label.contains(clean) || clean.contains(label)) {
                    val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                        return true
                    }
                }
            }
        } catch (_: Exception) { }

        return false
    }

    private fun sanitizeSpeech(input: String): String {
        return input.replace("?", "")
            .replace("!", "")
            .replace(",", " ")
            .trim()
    }
}

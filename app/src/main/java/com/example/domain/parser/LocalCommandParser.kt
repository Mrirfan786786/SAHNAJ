package com.example.domain.parser

import com.example.data.model.ActionType
import com.example.data.model.RiskLevel
import com.example.data.model.StructuredAction
import com.example.domain.personality.PersonalityResponses

class LocalCommandParser {

    /**
     * Attempts to parse user speech locally using lightweight pattern matching
     * in Hindi, English, and Hinglish. Returns null if command is complex or unrecognized.
     */
    fun parseLocally(rawSpeech: String, assistantName: String): StructuredAction? {
        val trimmed = rawSpeech.trim()
        if (trimmed.isEmpty()) return null

        val nameLower = assistantName.lowercase()

        // If user only spoke the assistant name or greeting invocation
        if (trimmed.equals(assistantName, ignoreCase = true) ||
            trimmed.equals("hey $nameLower", ignoreCase = true) ||
            trimmed.equals("ok $nameLower", ignoreCase = true) ||
            trimmed.equals("hello $nameLower", ignoreCase = true) ||
            trimmed.equals("hi $nameLower", ignoreCase = true)
        ) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "WAKE_RESPONSE",
                spokenResponse = PersonalityResponses.getRandomWakeWordGreeting(),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // Strip assistant name if spoken at start
        val cleanSpeech = trimmed
            .removePrefix(assistantName)
            .removePrefix(assistantName.lowercase())
            .removePrefix(assistantName.uppercase())
            .trim()
            .removePrefix(",")
            .trim()

        if (cleanSpeech.isEmpty()) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "WAKE_RESPONSE",
                spokenResponse = PersonalityResponses.getRandomWakeWordGreeting(),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        val lower = cleanSpeech.lowercase()

        // 0.01 Offline User Name Saving ("mera naam ... hai", "my name is ...")
        val appCtx = com.example.SahNajApplication.instance
        val savedName = com.example.domain.dispatcher.CommandDispatcher.parseAndSaveName(cleanSpeech, appCtx)
        if (savedName != null) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "OFFLINE_USER_NAME",
                spokenResponse = com.example.domain.dispatcher.CommandDispatcher.NAME_SAVED_MESSAGE,
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.02 Offline User Identity Query ("Main kaun hoon", "Mera naam kya hai", "Who am I")
        if (com.example.domain.dispatcher.CommandDispatcher.isIdentityQuery(lower) || com.example.domain.dispatcher.CommandDispatcher.isIdentityQuery(trimmed.lowercase())) {
            val identityResponse = com.example.domain.dispatcher.CommandDispatcher.getIdentityResponse(appCtx)
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "USER_IDENTITY",
                spokenResponse = identityResponse,
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.03 Offline Battery Status
        if (com.example.domain.dispatcher.CommandDispatcher.isBatteryCommand(lower)) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "BATTERY_STATUS",
                spokenResponse = com.example.domain.dispatcher.CommandDispatcher.getBatteryStatus(appCtx),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.04 Offline Time or Date
        if (com.example.domain.dispatcher.CommandDispatcher.isTimeOrDateCommand(lower)) {
            val isTime = !lower.contains("date") && !lower.contains("taarikh") && !lower.contains("din")
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = if (isTime) "TIME" else "DATE",
                spokenResponse = com.example.domain.dispatcher.CommandDispatcher.getTimeOrDate(appCtx, isTime),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0. Owner Change / Modification Questions
        if (isOwnerChangeQuestion(lower) || isOwnerChangeQuestion(trimmed.lowercase())) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "OWNER_CHANGE",
                spokenResponse = PersonalityResponses.OWNER_CANNOT_CHANGE_RESPONSE,
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.1 Owner Identity Questions
        if (isOwnerQuestion(lower) || isOwnerQuestion(trimmed.lowercase())) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = PersonalityResponses.CREATOR_NAME,
                spokenResponse = PersonalityResponses.OWNER_NAME_RESPONSE,
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.2 Creator Identity Questions
        if (isCreatorQuestion(lower) || isCreatorQuestion(trimmed.lowercase())) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = PersonalityResponses.CREATOR_NAME,
                spokenResponse = PersonalityResponses.getRandomCreatorResponse(),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.3 Quick Local Math Calculation (e.g. "1 aur 1 kitna hota hai", "2 + 2", "5 * 10")
        val mathResult = parseQuickMath(lower, rawSpeech)
        if (mathResult != null) {
            return mathResult
        }

        // 0.35 System Diagnostics & Mission Mode
        if (lower.contains("diagnostics") || lower.contains("system status") || lower.contains("diagnostics check") ||
            lower.contains("subsystem status") || lower.contains("hardware status") || lower.contains("system_diagnostics") ||
            lower.contains("mission mode") || lower.contains("mission_mode") || lower.contains("system scan") ||
            lower.contains("subsystem check")) {
            return StructuredAction(
                action = ActionType.SYSTEM_DIAGNOSTICS,
                target = "DIAGNOSTICS",
                spokenResponse = "",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.36 Morning Briefing
        if (lower in listOf("morning briefing", "daily briefing", "briefing do", "good morning", "subah ho gayi", "morning briefing do", "briefing karo")) {
            return StructuredAction(
                action = ActionType.MORNING_BRIEFING,
                target = "BRIEFING",
                spokenResponse = "",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.37 Night Routine
        if (lower in listOf("night routine", "good night", "bedtime", "bedtime routine", "dnd on karo", "dnd mode", "night mode")) {
            return StructuredAction(
                action = ActionType.NIGHT_ROUTINE,
                target = "NIGHT_ROUTINE",
                spokenResponse = "",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.38 Torch / Flashlight Quick Hardware Toggle
        val torchMatch = parseTorchCommand(lower, rawSpeech)
        if (torchMatch != null) {
            return torchMatch
        }

        // 0.39 Volume Quick Hardware Control
        val volumeMatch = parseVolumeCommand(lower, rawSpeech)
        if (volumeMatch != null) {
            return volumeMatch
        }

        // 0.394 App Store Download / Install Action
        val installMatch = parseInstallAppCommand(lower, rawSpeech)
        if (installMatch != null) {
            return installMatch
        }

        // 0.395 Screen Reader / Inspection Command
        if (lower.contains("screen read") || lower.contains("screen padho") || lower.contains("screen text") || lower.contains("read screen") || lower.contains("screen inspect") || lower.contains("inspect screen") || lower == "screen par kya hai") {
            return StructuredAction(
                action = ActionType.READ_SCREEN_TEXT,
                target = "SCREEN",
                spokenResponse = "",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.396 Voice Call Handling: Answer / Reject Call
        if (lower in listOf("answer call", "call pick karo", "call uthao", "call receive karo", "accept call", "pick up", "answer", "ha pick karo", "call utha lo")) {
            return StructuredAction(
                action = ActionType.ANSWER_CALL,
                target = "CALL",
                spokenResponse = "Call accept kar rahi hoon, boss.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }
        if (lower in listOf("reject call", "call cut karo", "call decline karo", "call disconnect karo", "cut call", "decline call", "reject", "call kaat do", "nahi cut karo")) {
            return StructuredAction(
                action = ActionType.REJECT_CALL,
                target = "CALL",
                spokenResponse = "Call reject kar di hai, boss.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.397 Emergency SOS Location Broadcast
        if (lower in listOf("emergency sos", "sos alert", "send sos", "bachao", "emergency location send karo", "send emergency location", "help sos", "emergency alert")) {
            return StructuredAction(
                action = ActionType.EMERGENCY_SOS,
                target = "EMERGENCY",
                spokenResponse = "",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.HIGH
            )
        }

        // 0.398 Fake / Decoy Shutdown Security Protocol
        if (lower in listOf("fake shutdown", "stealth shutdown", "decoy shutdown", "fake switch off", "screen off stealth")) {
            return StructuredAction(
                action = ActionType.FAKE_SHUTDOWN,
                target = "SECURITY",
                spokenResponse = "Decoy shutdown protocol engaged, boss. Screen dark ho rahi hai but stealth listening active rahegi.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.MEDIUM
            )
        }

        // 0.399 Automotive & DTC Diagnostics Quick Match
        if (lower.contains("dtc code") || lower.contains("obd code") || lower.contains("engine misfire") || lower.startsWith("p0") || lower.startsWith("c0") || lower.startsWith("u0") || lower.contains("car problem") || lower.contains("bike problem") || lower.contains("mechanic check")) {
            return StructuredAction(
                action = ActionType.AUTOMOTIVE_DIAGNOSTICS,
                target = rawSpeech,
                spokenResponse = "",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 0.4 Friendly Greetings, Jokes, Jaanu/Love, and Casual Chit-Chat via SmartHumanEngine
        val smartHumanReply = com.example.domain.personality.SmartHumanEngine.generateSmartReply(cleanSpeech, assistantName)
        if (isHumanChatOrGreeting(lower) || isHumanChatOrGreeting(trimmed.lowercase())) {
            return StructuredAction(
                action = ActionType.SPEAK_TEXT,
                target = "CASUAL_CHAT",
                spokenResponse = smartHumanReply,
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 1. Cancellation / Stop commands
        if (isStopCommand(lower)) {
            return StructuredAction(
                action = ActionType.STOP_ACTION,
                spokenResponse = PersonalityResponses.getRandomCancelResponse(),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 2. Navigation: Back
        if (lower in listOf("back", "back jao", "go back", "piche jao", "wapas jao", "back karo")) {
            return StructuredAction(
                action = ActionType.GO_BACK,
                spokenResponse = PersonalityResponses.getNavResponse("back"),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 3. Navigation: Home
        if (lower in listOf("home", "go home", "home jao", "home screen", "home par jao", "home par chalo")) {
            return StructuredAction(
                action = ActionType.GO_HOME,
                spokenResponse = PersonalityResponses.getNavResponse("home"),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 4. Navigation: Recents
        if (lower in listOf("recents", "recent apps", "recent apps kholo", "recent apps dikhao", "open recents", "recent")) {
            return StructuredAction(
                action = ActionType.OPEN_RECENTS,
                spokenResponse = PersonalityResponses.getNavResponse("recents"),
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 5. Gestures: Scroll Down
        if (lower in listOf("scroll down", "niche scroll karo", "niche jao", "scroll down karo", "niche karo")) {
            return StructuredAction(
                action = ActionType.SCROLL_DOWN,
                spokenResponse = "Niche scroll kar raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 6. Gestures: Scroll Up
        if (lower in listOf("scroll up", "upar scroll karo", "upar jao", "scroll up karo", "upar karo")) {
            return StructuredAction(
                action = ActionType.SCROLL_UP,
                spokenResponse = "Upar scroll kar raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 7. Gestures: Swipe Left / Right
        if (lower in listOf("swipe left", "left swipe karo", "left jao", "left swipe")) {
            return StructuredAction(
                action = ActionType.SWIPE_LEFT,
                spokenResponse = "Left swipe kar raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }
        if (lower in listOf("swipe right", "right swipe karo", "right jao", "right swipe")) {
            return StructuredAction(
                action = ActionType.SWIPE_RIGHT,
                spokenResponse = "Right swipe kar raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 8. Notifications / Quick Settings
        if (lower in listOf("open notifications", "notification panel kholo", "notifications dikhao", "notification bar kholo")) {
            return StructuredAction(
                action = ActionType.OPEN_NOTIFICATION_PANEL,
                spokenResponse = "Notification panel khol raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }
        if (lower in listOf("open quick settings", "quick settings kholo", "control center kholo", "quick settings")) {
            return StructuredAction(
                action = ActionType.OPEN_QUICK_SETTINGS,
                spokenResponse = "Quick settings khol raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // 9. Open Settings specifically
        val settingsMatch = parseSettingsCommand(lower, rawSpeech)
        if (settingsMatch != null) {
            return settingsMatch
        }

        // 10. WhatsApp deep message automation (Must be before generic open app)
        val waMatch = parseWhatsAppCommand(lower, rawSpeech)
        if (waMatch != null) {
            return waMatch
        }

        // 11. YouTube search & playback
        val ytMatch = parseYouTubeCommand(lower, rawSpeech)
        if (ytMatch != null) {
            return ytMatch
        }

        // 12. Chrome & Web Search
        val webMatch = parseWebSearchCommand(lower, rawSpeech)
        if (webMatch != null) {
            return webMatch
        }

        // 13. SMS Sending
        val smsMatch = parseSmsCommand(lower, rawSpeech)
        if (smsMatch != null) {
            return smsMatch
        }

        // 14. Phone Call ("call Rahul", "Rahul ko call karo", "Ammi ko phone lagao", "dial 9876543210")
        val callMatch = parseCallCommand(lower, rawSpeech)
        if (callMatch != null) {
            return callMatch
        }

        // 15. Generic Find & Tap ("tap submit", "next button dabao", "search par tap karo")
        val tapMatch = parseTapCommand(lower, rawSpeech)
        if (tapMatch != null) {
            return tapMatch
        }

        // 16. Generic Type Text
        val typeMatch = parseTypeCommand(lower, rawSpeech)
        if (typeMatch != null) {
            return typeMatch
        }

        // 17. Open App ("open whatsapp", "whatsapp kholo", "launch youtube", "chrome start karo")
        val appMatch = parseOpenAppCommand(lower, rawSpeech)
        if (appMatch != null) {
            return appMatch
        }

        return null
    }

    private fun isOwnerChangeQuestion(lower: String): Boolean {
        val clean = lower.replace("?", "").replace("!", "").trim()
        val phrases = listOf(
            "owner ka naam change kar sakte ho",
            "owner ka naam badal sakte ho",
            "owner change kar sakte ho",
            "owner ka naam change karo",
            "owner change karo",
            "owner badal do",
            "owner ka naam badlo",
            "can you change owner",
            "can you change owner name",
            "change owner name",
            "change your owner",
            "change owner",
            "owner badlo"
        )
        return phrases.any { clean == it || clean.contains(it) }
    }

    private fun isOwnerQuestion(lower: String): Boolean {
        val clean = lower.replace("?", "").replace("!", "").trim()
        val phrases = listOf(
            "owner kaun hai",
            "tumhara owner kaun hai",
            "who is your owner",
            "who is the owner",
            "owner ka naam",
            "owner ka naam kya hai",
            "tumhara owner kaun he",
            "owner name",
            "owner kaun he"
        )
        return phrases.any { clean == it || clean.contains(it) }
    }

    private fun parseQuickMath(lower: String, rawSpeech: String): StructuredAction? {
        val clean = lower.replace("?", "").replace("!", "").trim()

        // "1 aur 1 kitna hota hai", "1 aur 1", "ek aur ek kitna hota hai"
        val regexPlusHinglish = Regex("""^(\d+|ek|do|teen|chaar|paanch)\s+(?:aur|\+|\bplus\b)\s+(\d+|ek|do|teen|chaar|paanch)(?:\s+kitna\s+hota\s+hai|\s+kitna\s+hai)?$""")
        val matchPlus = regexPlusHinglish.find(clean)
        if (matchPlus != null) {
            val num1 = wordToNum(matchPlus.groupValues[1])
            val num2 = wordToNum(matchPlus.groupValues[2])
            if (num1 != null && num2 != null) {
                val sum = num1 + num2
                return StructuredAction(
                    action = ActionType.GENERAL_QUESTION,
                    target = "Q&A",
                    spokenResponse = "$num1 aur $num2 barabar $sum hota hai.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        // Basic "X + Y", "X - Y", "X * Y", "X / Y"
        val basicMathRegex = Regex("""^(\d+)\s*([\+\-\*\/]|plus|minus|into|multiplied by|divided by)\s*(\d+)(?:\s+kitna\s+hota\s+hai|\s+kitna\s+hai)?$""")
        val matchBasic = basicMathRegex.find(clean)
        if (matchBasic != null) {
            val a = matchBasic.groupValues[1].toLongOrNull()
            val op = matchBasic.groupValues[2].trim()
            val b = matchBasic.groupValues[3].toLongOrNull()
            if (a != null && b != null) {
                val (res, opName) = when (op) {
                    "+", "plus" -> Pair(a + b, "plus")
                    "-", "minus" -> Pair(a - b, "minus")
                    "*", "into", "multiplied by" -> Pair(a * b, "into")
                    "/", "divided by" -> if (b != 0L) Pair(a / b, "divided by") else Pair(null, "divided by")
                    else -> Pair(null, "")
                }
                if (res != null) {
                    return StructuredAction(
                        action = ActionType.GENERAL_QUESTION,
                        target = "Q&A",
                        spokenResponse = "$a $opName $b barabar $res hota hai.",
                        rawPrompt = rawSpeech,
                        riskLevel = RiskLevel.LOW
                    )
                }
            }
        }
        return null
    }

    private fun wordToNum(w: String): Long? {
        return when (w) {
            "ek", "1" -> 1L
            "do", "2" -> 2L
            "teen", "3" -> 3L
            "chaar", "4" -> 4L
            "paanch", "5" -> 5L
            else -> w.toLongOrNull()
        }
    }

    private fun isCreatorQuestion(lower: String): Boolean {
        val clean = lower.replace("?", "").replace("!", "").trim()
        val creatorPhrases = listOf(
            "who made you",
            "who created you",
            "who is your creator",
            "who is your developer",
            "who developed you",
            "who is your maker",
            "who build you",
            "who built you",
            "who designed you",
            "tumhe kisne banaya",
            "tumhe kisne banaya hai",
            "tumhe kisne create kiya",
            "tumhe kisne create kiya hai",
            "tumhe kisne develop kiya",
            "tumhe kisne banaya he",
            "tumhara creator kaun hai",
            "tumhara developer kaun hai",
            "tumhara creator kaun he",
            "tumhara developer kaun he",
            "tumhara malik kaun hai",
            "tumhara boss kaun hai",
            "tumhe kisne program kiya",
            "kisne banaya tumhe",
            "creator kaun hai",
            "apne creator ke baare mein batao",
            "who made this app",
            "is app ko kisne banaya"
        )
        return creatorPhrases.any { clean == it || clean.contains(it) }
    }

    private fun isHumanChatOrGreeting(lower: String): Boolean {
        val clean = lower.replace("?", "").replace("!", "").replace(".", "").replace(",", "").trim()
        val chatKeywords = listOf(
            "hello", "hi", "hey", "namaste", "pranam", "salam", "adaab", "good morning", "good evening", "good night",
            "kaise ho", "kaisi ho", "kya haal", "kya chal raha", "kya kar rahi", "kya kar rahe", "kya scene",
            "aur batao", "aur sunao", "sun rahe ho", "suno na", "suno", "jaanu", "janu", "jaan", "baby", "shona",
            "sweetheart", "darling", "meri jaan", "i love you", "love you", "pyar", "girlfriend", "joke", "chutkula",
            "hasao", "boring", "bore", "shayari", "khana khaya", "thak gaya", "thak gayi", "sad", "udaas", "mood",
            "happy", "smart", "pyari", "khubsurat", "time kya", "kitne baje", "date kya", "tarikh kya", "mausam"
        )
        return chatKeywords.any { clean == it || clean.contains(it) || clean == "$it sahnaj" || clean == "$it sahaj" }
    }

    private fun isGreeting(lower: String): Boolean {
        val clean = lower.replace("?", "").replace("!", "").trim()
        val greetings = listOf(
            "hello", "hi", "hey", "namaste", "pranam", "salam",
            "kaise ho", "kya haal hai", "kya chal raha hai", "sun rahe ho"
        )
        return greetings.any { clean == it || clean == "$it sahnaj" || clean == "$it sahaj" }
    }

    private fun isStopCommand(lower: String): Boolean {
        return lower in listOf("stop", "cancel", "ruko", "ruk jao", "bas", "rok do", "band karo", "abort")
    }

    private fun parseSettingsCommand(lower: String, rawSpeech: String): StructuredAction? {
        val settingsMap = mapOf(
            "wifi" to "WIFI",
            "wi-fi" to "WIFI",
            "bluetooth" to "BLUETOOTH",
            "display" to "DISPLAY",
            "screen" to "DISPLAY",
            "sound" to "SOUND",
            "volume" to "SOUND",
            "battery" to "BATTERY",
            "location" to "LOCATION",
            "gps" to "LOCATION",
            "accessibility" to "ACCESSIBILITY",
            "security" to "SECURITY",
            "apps" to "APPS",
            "settings" to "GENERAL"
        )

        for ((keyword, targetType) in settingsMap) {
            if (lower.contains("$keyword settings") ||
                lower.contains("$keyword setting") ||
                lower.startsWith("open $keyword") ||
                lower.endsWith("$keyword kholo") ||
                lower.contains("$keyword ki setting")
            ) {
                return StructuredAction(
                    action = ActionType.OPEN_SETTINGS,
                    target = targetType,
                    spokenResponse = "$keyword settings khol raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        if (lower in listOf("settings", "open settings", "settings kholo", "setting kholo", "settings open karo")) {
            return StructuredAction(
                action = ActionType.OPEN_SETTINGS,
                target = "GENERAL",
                spokenResponse = "Settings khol raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        return null
    }

    private fun parseOpenAppCommand(lower: String, rawSpeech: String): StructuredAction? {
        val commonApps = listOf(
            "whatsapp", "youtube", "chrome", "instagram", "facebook", "camera", "gallery",
            "maps", "gmail", "calculator", "clock", "telegram", "spotify", "twitter", "x",
            "linkedin", "snapchat", "contacts", "messages", "phone", "play store", "files",
            "drive", "calendar", "notes"
        )

        for (app in commonApps) {
            if (lower == "open $app" ||
                lower == "launch $app" ||
                lower == "$app kholo" ||
                lower == "$app open karo" ||
                lower == "$app chalao" ||
                lower == "$app start karo" ||
                lower.startsWith("open $app") ||
                lower.endsWith("$app kholo")
            ) {
                val formattedName = app.replaceFirstChar { it.uppercase() }
                return StructuredAction(
                    action = ActionType.OPEN_APP,
                    target = formattedName,
                    spokenResponse = "Ji, $formattedName khol raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        val openPrefixes = listOf("open ", "launch ", "start ")
        for (prefix in openPrefixes) {
            if (lower.startsWith(prefix)) {
                val targetApp = lower.removePrefix(prefix).trim().replaceFirstChar { it.uppercase() }
                if (targetApp.isNotEmpty() && !targetApp.contains("settings")) {
                    return StructuredAction(
                        action = ActionType.OPEN_APP,
                        target = targetApp,
                        spokenResponse = "$targetApp khol raha hoon.",
                        rawPrompt = rawSpeech,
                        riskLevel = RiskLevel.LOW
                    )
                }
            }
        }

        if (lower.endsWith(" kholo") || lower.endsWith(" open karo") || lower.endsWith(" chalao")) {
            val appPart = lower.removeSuffix(" kholo").removeSuffix(" open karo").removeSuffix(" chalao").trim()
            if (appPart.isNotEmpty() && !appPart.contains("settings") && !appPart.contains("setting")) {
                val formatted = appPart.replaceFirstChar { it.uppercase() }
                return StructuredAction(
                    action = ActionType.OPEN_APP,
                    target = formatted,
                    spokenResponse = "$formatted khol raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        return null
    }

    private fun parseCallCommand(lower: String, rawSpeech: String): StructuredAction? {
        // Dial number
        val digits = lower.filter { it.isDigit() }
        if (digits.length in 3..15 && (lower.contains("dial") || lower.contains("call") || lower.contains("phone"))) {
            return StructuredAction(
                action = ActionType.DIAL_NUMBER,
                target = digits,
                requiresConfirmation = true,
                spokenResponse = "$digits par call karun?",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.HIGH
            )
        }

        // Call contact ("call Rahul", "Rahul ko call karo", "Ammi ko phone karo")
        if (lower.startsWith("call ")) {
            val contact = lower.removePrefix("call ").trim().replaceFirstChar { it.uppercase() }
            if (contact.isNotEmpty()) {
                return StructuredAction(
                    action = ActionType.CALL_CONTACT,
                    target = contact,
                    requiresConfirmation = true,
                    spokenResponse = "$contact ko call karun?",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.HIGH
                )
            }
        }

        val hindiSuffixes = listOf(" ko call karo", " ko phone karo", " ko call lagao", " ko phone lagao")
        for (suffix in hindiSuffixes) {
            if (lower.endsWith(suffix)) {
                val contact = lower.removeSuffix(suffix).trim().replaceFirstChar { it.uppercase() }
                if (contact.isNotEmpty()) {
                    return StructuredAction(
                        action = ActionType.CALL_CONTACT,
                        target = contact,
                        requiresConfirmation = true,
                        spokenResponse = "$contact ko call karun?",
                        rawPrompt = rawSpeech,
                        riskLevel = RiskLevel.HIGH
                    )
                }
            }
        }

        return null
    }

    private fun parseWhatsAppCommand(lower: String, rawSpeech: String): StructuredAction? {
        if (!lower.contains("whatsapp") && !lower.contains("whats app")) return null

        val isSendExplicit = lower.contains("bhejo") || lower.contains("bhej do") ||
                lower.contains("send karo") || lower.contains("send kar do") ||
                lower.contains("send")

        // Helper regex for contact name or phone number: allows letters, digits, +, and spaces between words
        // e.g. "Mammi", "Rahul Sharma", "+919876543210", "9876543210"

        // Pattern 1: "whatsapp (kholo aur / par) [contact/number] ko message karo/bhejo [-—:] [message]"
        // Example: "WhatsApp kholo aur 9876543210 ko message bhejo — main late hoon"
        // Example: "WhatsApp par Rahul ko message bhejo main aa raha hoon"
        val regex1 = Regex("""(?:whatsapp\s+(?:kholo\s+aur|open\s+karo\s+aur|par|pe|me|mein)\s+)([\+0-9a-zA-Z\u0900-\u097F]+(?:\s+[\+0-9a-zA-Z\u0900-\u097F]+)?)\s+ko\s+message\s+(?:karo|bhejo|kar\s+do|bhej\s+do)(?:\s*[-—:]\s*|\s+)(.+)""", RegexOption.IGNORE_CASE)
        val match1 = regex1.find(lower)
        if (match1 != null) {
            val contact = match1.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val message = match1.groupValues[2].trim()
            if (message.isNotBlank() && !message.equals("bhejo", ignoreCase = true) && !message.equals("karo", ignoreCase = true)) {
                return buildWhatsAppAction(contact, message, isSendExplicit, rawSpeech)
            }
        }

        // Pattern 2: "[contact/number] ko whatsapp (par/pe) message karo/bhejo [-—:] [message]"
        val regex2 = Regex("""([\+0-9a-zA-Z\u0900-\u097F]+(?:\s+[\+0-9a-zA-Z\u0900-\u097F]+)?)\s+ko\s+whatsapp\s*(?:par|pe|me|mein)?\s*message\s+(?:karo|bhejo|kar\s+do|bhej\s+do)(?:\s*[-—:]\s*|\s+)(.+)""", RegexOption.IGNORE_CASE)
        val match2 = regex2.find(lower)
        if (match2 != null) {
            val contact = match2.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val message = match2.groupValues[2].trim()
            if (message.isNotBlank() && !message.equals("bhejo", ignoreCase = true) && !message.equals("karo", ignoreCase = true)) {
                return buildWhatsAppAction(contact, message, isSendExplicit, rawSpeech)
            }
        }

        // Pattern 3: "whatsapp message to [contact/number] [message]"
        val regex3 = Regex("""(?:whatsapp\s+message\s+to\s+|send\s+whatsapp\s+(?:message\s+)?to\s+)([\+0-9a-zA-Z\u0900-\u097F]+(?:\s+[\+0-9a-zA-Z\u0900-\u097F]+)?)(?:\s*[-—:]\s*|\s+)(.+)""", RegexOption.IGNORE_CASE)
        val match3 = regex3.find(lower)
        if (match3 != null) {
            val contact = match3.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val message = match3.groupValues[2].trim()
            return buildWhatsAppAction(contact, message, isSendExplicit, rawSpeech)
        }

        // Pattern 4: "whatsapp kholo aur [contact/number] ko message bhejo / karo" (without message body -> prompt user)
        val regexPrompt1 = Regex("""(?:whatsapp\s+(?:kholo\s+aur|open\s+karo\s+aur|par|pe|me|mein)\s+)([\+0-9a-zA-Z\u0900-\u097F]+(?:\s+[\+0-9a-zA-Z\u0900-\u097F]+)?)\s+ko\s*(?:message\s+)?(?:karo|bhejo|kar\s+do|bhej\s+do)$""", RegexOption.IGNORE_CASE)
        val matchPrompt1 = regexPrompt1.find(lower)
        if (matchPrompt1 != null) {
            val contact = matchPrompt1.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            if (contact.isNotBlank() && !contact.equals("whatsapp", ignoreCase = true)) {
                return buildWhatsAppAction(contact, "", isSendExplicit, rawSpeech)
            }
        }

        // Pattern 5: "[contact/number] ko whatsapp par message karo / bhejo" (without message body -> prompt user)
        val regexPrompt2 = Regex("""([\+0-9a-zA-Z\u0900-\u097F]+(?:\s+[\+0-9a-zA-Z\u0900-\u097F]+)?)\s+ko\s+whatsapp\s*(?:par|pe|me|mein)?\s*(?:message\s+)?(?:karo|bhejo|kar\s+do|bhej\s+do)$""", RegexOption.IGNORE_CASE)
        val matchPrompt2 = regexPrompt2.find(lower)
        if (matchPrompt2 != null) {
            val contact = matchPrompt2.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            if (contact.isNotBlank() && !contact.equals("whatsapp", ignoreCase = true)) {
                return buildWhatsAppAction(contact, "", isSendExplicit, rawSpeech)
            }
        }

        return null
    }

    private fun buildWhatsAppAction(
        contact: String,
        message: String,
        isSendExplicit: Boolean,
        rawSpeech: String
    ): StructuredAction {
        if (message.isBlank()) {
            return StructuredAction(
                action = ActionType.SEND_WHATSAPP_MESSAGE,
                target = contact,
                parameters = mapOf("message" to "", "send" to "false", "prompt_text" to "true"),
                requiresConfirmation = false,
                spokenResponse = "Kya likhna hai?",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        return if (isSendExplicit) {
            StructuredAction(
                action = ActionType.SEND_WHATSAPP_MESSAGE,
                target = contact,
                parameters = mapOf("message" to message, "send" to "true"),
                requiresConfirmation = true,
                spokenResponse = "$contact ko '$message' bhejun?",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.HIGH
            )
        } else {
            StructuredAction(
                action = ActionType.SEND_WHATSAPP_MESSAGE,
                target = contact,
                parameters = mapOf("message" to message, "send" to "false"),
                requiresConfirmation = false,
                spokenResponse = "WhatsApp par $contact ke liye message likh diya hai.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }
    }

    private fun parseYouTubeCommand(lower: String, rawSpeech: String): StructuredAction? {
        if (!lower.contains("youtube") && !lower.startsWith("yt ")) return null

        // "youtube kholo aur [query] search karo"
        val regex1 = Regex("""(?:youtube\s+(?:kholo|open\s+karo|chalao)\s+aur\s+|youtube\s+(?:par|pe|me|mein)\s+|youtube\s+search\s+)(.+)$""", RegexOption.IGNORE_CASE)
        val match1 = regex1.find(lower)
        if (match1 != null) {
            val query = match1.groupValues[1]
                .removePrefix("search ")
                .removeSuffix(" search karo")
                .removeSuffix(" search")
                .removeSuffix(" chalao")
                .removeSuffix(" dikhao")
                .trim()
            if (query.isNotBlank() && query != "kholo" && query != "open") {
                return StructuredAction(
                    action = ActionType.YOUTUBE_SEARCH,
                    target = query,
                    spokenResponse = "Ji, YouTube par '$query' search kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        // "search [query] on youtube"
        val regex2 = Regex("""search\s+(.+?)\s+(?:on|in)\s+youtube""", RegexOption.IGNORE_CASE)
        val match2 = regex2.find(lower)
        if (match2 != null) {
            val query = match2.groupValues[1].trim()
            if (query.isNotBlank()) {
                return StructuredAction(
                    action = ActionType.YOUTUBE_SEARCH,
                    target = query,
                    spokenResponse = "YouTube par '$query' search kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        return null
    }

    private fun parseWebSearchCommand(lower: String, rawSpeech: String): StructuredAction? {
        if (!lower.contains("chrome") && !lower.contains("google search") && !lower.startsWith("search ")) return null

        // "chrome kholo aur [query] search karo" / "chrome par search karo [query]"
        val regex1 = Regex("""(?:chrome\s+(?:kholo|open\s+karo)\s+aur\s+|chrome\s+(?:par|pe|me|mein)\s+|google\s+(?:par|pe)\s+search\s+karo\s+|search\s+in\s+chrome\s+|search\s+on\s+google\s+)(.+)$""", RegexOption.IGNORE_CASE)
        val match1 = regex1.find(lower)
        if (match1 != null) {
            val query = match1.groupValues[1]
                .removePrefix("search ")
                .removeSuffix(" search karo")
                .removeSuffix(" search")
                .removeSuffix(" dhoondho")
                .trim()
            if (query.isNotBlank() && query != "kholo" && query != "open") {
                return StructuredAction(
                    action = ActionType.WEB_SEARCH,
                    target = query,
                    spokenResponse = "Chrome par '$query' search kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        return null
    }

    private fun parseSmsCommand(lower: String, rawSpeech: String): StructuredAction? {
        if (!lower.contains("sms") && !lower.contains("message")) return null
        if (lower.contains("whatsapp")) return null // WhatsApp handled separately

        val regex1 = Regex("""([a-zA-Z0-9\u0900-\u097F]+)\s+ko\s+sms\s+(?:bhejo|karo|bhej\s+do)(?:\s*[-—:]\s*|\s+)(.*)""", RegexOption.IGNORE_CASE)
        val match1 = regex1.find(lower)
        if (match1 != null) {
            val contact = match1.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val message = match1.groupValues[2].trim()
            if (message.isNotBlank()) {
                return StructuredAction(
                    action = ActionType.SEND_SMS,
                    target = contact,
                    parameters = mapOf("message" to message),
                    requiresConfirmation = true,
                    spokenResponse = "$contact ko SMS bhejun: '$message'?",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.HIGH
                )
            }
        }

        val regex2 = Regex("""(?:send\s+sms\s+to\s+|sms\s+to\s+)([a-zA-Z0-9\u0900-\u097F]+)(?:\s*[-—:]\s*|\s+)(.*)""", RegexOption.IGNORE_CASE)
        val match2 = regex2.find(lower)
        if (match2 != null) {
            val contact = match2.groupValues[1].trim().replaceFirstChar { it.uppercase() }
            val message = match2.groupValues[2].trim()
            if (message.isNotBlank()) {
                return StructuredAction(
                    action = ActionType.SEND_SMS,
                    target = contact,
                    parameters = mapOf("message" to message),
                    requiresConfirmation = true,
                    spokenResponse = "$contact ko SMS bhejun: '$message'?",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.HIGH
                )
            }
        }

        return null
    }

    private fun parseTapCommand(lower: String, rawSpeech: String): StructuredAction? {
        if (lower.startsWith("tap on ") || lower.startsWith("tap ")) {
            val target = lower.removePrefix("tap on ").removePrefix("tap ").trim()
            if (target.isNotEmpty()) {
                return StructuredAction(
                    action = ActionType.FIND_AND_TAP,
                    target = target,
                    spokenResponse = "$target par tap kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        if (lower.startsWith("click on ") || lower.startsWith("click ")) {
            val target = lower.removePrefix("click on ").removePrefix("click ").trim()
            if (target.isNotEmpty()) {
                return StructuredAction(
                    action = ActionType.FIND_AND_TAP,
                    target = target,
                    spokenResponse = "$target par click kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        if (lower.endsWith(" par tap karo") || lower.endsWith(" dabao") || lower.endsWith(" click karo") || lower.endsWith(" par click karo")) {
            val target = lower
                .removeSuffix(" par tap karo")
                .removeSuffix(" par click karo")
                .removeSuffix(" dabao")
                .removeSuffix(" click karo")
                .removeSuffix(" button")
                .trim()
            if (target.isNotEmpty()) {
                return StructuredAction(
                    action = ActionType.FIND_AND_TAP,
                    target = target,
                    spokenResponse = "$target par tap kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        return null
    }

    private fun parseTypeCommand(lower: String, rawSpeech: String): StructuredAction? {
        // "type [text] in [field]"
        val regex1 = Regex("""type\s+(.+?)\s+(?:in|into)\s+(.+)""", RegexOption.IGNORE_CASE)
        val match1 = regex1.find(lower)
        if (match1 != null) {
            val text = match1.groupValues[1].trim()
            val field = match1.groupValues[2].trim()
            return StructuredAction(
                action = ActionType.FIND_AND_TYPE,
                target = text,
                parameters = mapOf("text" to text, "hint" to field),
                spokenResponse = "$field mein '$text' type kar raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // "[field] mein [text] type karo / likho"
        val regex2 = Regex("""(.+?)\s+(?:mein|me|field\s+mein|field\s+me)\s+(.+?)\s+(?:type\s+karo|likho)""", RegexOption.IGNORE_CASE)
        val match2 = regex2.find(lower)
        if (match2 != null) {
            val field = match2.groupValues[1].trim()
            val text = match2.groupValues[2].trim()
            return StructuredAction(
                action = ActionType.FIND_AND_TYPE,
                target = text,
                parameters = mapOf("text" to text, "hint" to field),
                spokenResponse = "$field mein '$text' likh raha hoon.",
                rawPrompt = rawSpeech,
                riskLevel = RiskLevel.LOW
            )
        }

        // "[text] type karo / likho"
        if (lower.endsWith(" type karo") || lower.endsWith(" likho")) {
            val text = lower.removeSuffix(" type karo").removeSuffix(" likho").trim()
            if (text.isNotEmpty()) {
                return StructuredAction(
                    action = ActionType.FIND_AND_TYPE,
                    target = text,
                    parameters = mapOf("text" to text),
                    spokenResponse = "'$text' type kar raha hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        return null
    }

    private fun parseTorchCommand(lower: String, rawSpeech: String): StructuredAction? {
        val clean = lower.replace("?", "").replace("!", "").trim()
        val isTorchWord = clean.contains("torch") || clean.contains("flashlight") || clean.contains("flash light") || clean.contains("flash")
        if (!isTorchWord) return null

        val isOn = clean.contains("on") || clean.contains("chalu") || clean.contains("jalao") || clean.contains("start")
        val isOff = clean.contains("off") || clean.contains("band") || clean.contains("bujhao") || clean.contains("stop")

        val stateValue = when {
            isOn -> "ON"
            isOff -> "OFF"
            else -> "TOGGLE"
        }

        return StructuredAction(
            action = ActionType.DEVICE_SETTING,
            target = "TORCH",
            value = stateValue,
            parameters = mapOf("setting_state" to stateValue),
            spokenResponse = "",
            rawPrompt = rawSpeech,
            riskLevel = RiskLevel.LOW
        )
    }

    private fun parseVolumeCommand(lower: String, rawSpeech: String): StructuredAction? {
        val clean = lower.replace("?", "").replace("!", "").trim()
        val isVolumeWord = clean.contains("volume") || clean.contains("awaz") || clean.contains("sound")
        if (!isVolumeWord) return null

        val isUp = clean.contains("up") || clean.contains("badhao") || clean.contains("tez") || clean.contains("increase")
        val isDown = clean.contains("down") || clean.contains("kam") || clean.contains("ghatao") || clean.contains("decrease")
        val isMute = clean.contains("mute") || clean.contains("silent") || clean.contains("shant")
        val isMax = clean.contains("max") || clean.contains("full") || clean.contains("100")

        val digits = clean.filter { it.isDigit() }
        val valueStr = when {
            isMax -> "MAX"
            isMute -> "MUTE"
            isUp -> "UP"
            isDown -> "DOWN"
            digits.isNotBlank() -> digits
            else -> "UP"
        }

        return StructuredAction(
            action = ActionType.DEVICE_SETTING,
            target = "VOLUME",
            value = valueStr,
            parameters = mapOf("setting_state" to valueStr),
            spokenResponse = "",
            rawPrompt = rawSpeech,
            riskLevel = RiskLevel.LOW
        )
    }

    private fun parseInstallAppCommand(lower: String, rawSpeech: String): StructuredAction? {
        val clean = lower.replace("?", "").replace("!", "").trim()

        val hasInstallVerb = clean.contains("install") || clean.contains("download") || clean.contains("डाउनलोड") || clean.contains("इन्स्टॉल") || clean.contains("इंस्टॉल")
        if (!hasInstallVerb) return null

        // 1. "play store kholo aur [app] install/download karo" or "play store se [app] install karo"
        val regexPlayStore = Regex("""(?:open\s+)?play\s*store\s*(?:kholo\s+aur|se|me|par|khol\s+ke)?\s+(.+?)\s*(?:ko\s+)?(?:install|download|dalao|dal do|load)\s*(?:karo|karna|kar do|kijiye)?""", RegexOption.IGNORE_CASE)
        val matchPlayStore = regexPlayStore.find(clean)
        if (matchPlayStore != null) {
            val app = matchPlayStore.groupValues[1].replace("app", "").trim()
            if (app.isNotBlank() && app != "kholo" && app != "open") {
                val formattedName = app.replaceFirstChar { it.uppercase() }
                return StructuredAction(
                    action = ActionType.INSTALL_APP,
                    target = formattedName,
                    parameters = mapOf("app_name" to formattedName),
                    spokenResponse = "Play Store se $formattedName install kar rahi hoon, boss.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        // 2. "install [app] from play store" / "download [app] from play store"
        val regexFromPlayStore = Regex("""(?:install|download)\s+(.+?)\s+(?:from|in|on)\s+play\s*store""", RegexOption.IGNORE_CASE)
        val matchFromPlayStore = regexFromPlayStore.find(clean)
        if (matchFromPlayStore != null) {
            val app = matchFromPlayStore.groupValues[1].replace("app", "").trim()
            if (app.isNotBlank()) {
                val formattedName = app.replaceFirstChar { it.uppercase() }
                return StructuredAction(
                    action = ActionType.INSTALL_APP,
                    target = formattedName,
                    parameters = mapOf("app_name" to formattedName),
                    spokenResponse = "Play Store se $formattedName download aur install kar rahi hoon, boss.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        // 3. "install [app]" / "download [app]"
        val regexDirectInstall = Regex("""^(?:install|download)\s+(.+)$""", RegexOption.IGNORE_CASE)
        val matchDirect = regexDirectInstall.find(clean)
        if (matchDirect != null) {
            val app = matchDirect.groupValues[1].replace("app", "").trim()
            if (app.isNotBlank()) {
                val formattedName = app.replaceFirstChar { it.uppercase() }
                return StructuredAction(
                    action = ActionType.INSTALL_APP,
                    target = formattedName,
                    parameters = mapOf("app_name" to formattedName),
                    spokenResponse = "Play Store par $formattedName open karke install kar rahi hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        // 4. "[app] install karo" / "[app] download karo" / "[app] install kar do"
        val regexHindiInstall = Regex("""^(.+?)\s+(?:app\s+)?(?:install|download)\s+(?:karo|kar do|kijiye|karna hai)$""", RegexOption.IGNORE_CASE)
        val matchHindi = regexHindiInstall.find(clean)
        if (matchHindi != null) {
            var app = matchHindi.groupValues[1].trim()
            app = app.removePrefix("play store se ").removePrefix("play store par ").removePrefix("play store me ").trim()
            if (app.isNotBlank()) {
                val formattedName = app.replaceFirstChar { it.uppercase() }
                return StructuredAction(
                    action = ActionType.INSTALL_APP,
                    target = formattedName,
                    parameters = mapOf("app_name" to formattedName),
                    spokenResponse = "Play Store se $formattedName install kar rahi hoon.",
                    rawPrompt = rawSpeech,
                    riskLevel = RiskLevel.LOW
                )
            }
        }

        return null
    }
}

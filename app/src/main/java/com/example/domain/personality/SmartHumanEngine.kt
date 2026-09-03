package com.example.domain.personality

import com.example.SahNajApplication
import com.example.util.SystemDiagnosticsHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/**
 * SmartHumanEngine: Offline, zero-API intelligent companion logic for SAHNAJ (JARVIS Autonomous OS).
 * Provides authentic, razor-sharp, proactive, high-tech, and witty interactions
 * addressing the user as Boss / Sir across Hindi, Hinglish, and English.
 */
object SmartHumanEngine {

    /**
     * Sanitizes any response to guarantee that no raw JSON, technical artifacts,
     * markdown code fences, or robotic action tags ever leak to the user or TTS.
     */
    fun sanitizeResponse(rawText: String, userSpeech: String = ""): String {
        if (rawText.isBlank()) {
            return generateSmartReply(userSpeech)
        }

        var text = rawText.trim()

        // 1. Remove markdown code blocks (```json ... ``` or ``` ...)
        text = text.replace(Regex("""```(?:json|JSON)?[\s\S]*?```"""), { match ->
            extractSpokenField(match.value) ?: ""
        }).trim()

        text = text.replace("```json", "", ignoreCase = true)
            .replace("```", "")
            .trim()

        // 2. Check if the entire string looks like a JSON object
        if ((text.startsWith("{") && text.endsWith("}")) || text.contains("\"action\"") || text.contains("\"spoken_response\"") || text.contains("\"ACTION\"")) {
            val extracted = extractSpokenField(text)
            if (!extracted.isNullOrBlank()) {
                text = extracted
            } else {
                return generateSmartReply(userSpeech)
            }
        }

        // 3. Remove leftover JSON key remnants e.g. "spoken_response": "..." or "action": "..."
        text = text.replace(Regex("""^\{.*?"spoken_response"\s*:\s*"(.*?)"\}""", RegexOption.DOT_MATCHES_ALL), "$1")
            .replace(Regex("""^\{.*?"spokenResponse"\s*:\s*"(.*?)"\}""", RegexOption.DOT_MATCHES_ALL), "$1")
            .replace(Regex("""\{[^}]*\}"""), "")
            .replace(Regex("""["']?(?:action|spoken_response|target|value|parameters)["']?\s*:\s*["']?[^,"'}]*["']?""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""[{}"\[\]]"""), "")
            .replace(Regex("""\s+"""), " ")
            .trim()

        // 4. Remove robotic error fragments
        val roboticPhrases = listOf(
            "ACTION: GENERAL",
            "ACTION: GENERAL_QNA",
            "ACTION: GENERAL_QUESTION",
            "ACTION_TYPE",
            "GENERAL_QNA",
            "GENERAL_QUESTION",
            "null"
        )
        for (phrase in roboticPhrases) {
            text = text.replace(phrase, "", ignoreCase = true).trim()
        }

        if (text.isBlank() || text.length < 2) {
            return generateSmartReply(userSpeech)
        }

        return text
    }

    /**
     * Extracts spoken_response value from a raw JSON string if present.
     */
    private fun extractSpokenField(jsonText: String): String? {
        val patterns = listOf(
            Regex(""""spoken_response"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.IGNORE_CASE),
            Regex(""""spokenResponse"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.IGNORE_CASE),
            Regex(""""response"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.IGNORE_CASE),
            Regex(""""reply"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.IGNORE_CASE),
            Regex(""""text"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.IGNORE_CASE),
            Regex(""""answer"\s*:\s*"((?:\\.|[^"\\])*)"""", RegexOption.IGNORE_CASE)
        )
        for (pattern in patterns) {
            val match = pattern.find(jsonText)
            if (match != null) {
                val value = match.groupValues[1]
                    .replace("\\\"", "\"")
                    .replace("\\n", " ")
                    .replace("\\/", "/")
                    .trim()
                if (value.isNotBlank() && !value.equals("null", ignoreCase = true)) {
                    return value
                }
            }
        }
        return null
    }

    /**
     * Generates a context-aware, witty, and razor-sharp JARVIS response
     * for any query when operating in zero-API or offline mode.
     */
    fun generateSmartReply(userSpeech: String, assistantName: String = "सहनाज"): String {
        val speech = userSpeech.trim()
        if (speech.isBlank()) {
            return PersonalityResponses.getRandomWakeWordGreeting()
        }

        val lower = speech.lowercase(Locale.ROOT)
            .replace("?", "")
            .replace("!", "")
            .replace(".", "")
            .replace(",", "")
            .trim()

        // 0. Offline User Name Saving ("mera naam ... hai", "my name is ...")
        val ctx = SahNajApplication.instance
        val savedName = com.example.domain.dispatcher.CommandDispatcher.parseAndSaveName(speech, ctx)
        if (savedName != null) {
            return com.example.domain.dispatcher.CommandDispatcher.NAME_SAVED_MESSAGE
        }

        // 0.1 Offline User Identity Query ("Main kaun hoon", "Mera naam kya hai", "Who am I")
        if (com.example.domain.dispatcher.CommandDispatcher.isIdentityQuery(lower)) {
            return com.example.domain.dispatcher.CommandDispatcher.getIdentityResponse(ctx)
        }

        // 1. Creator & Developer Questions
        if (isCreatorOrOwnerQuery(lower)) {
            return PersonalityResponses.getRandomCreatorResponse()
        }

        // 2. System Status & Diagnostics Check
        if (isDiagnosticsQuery(lower)) {
            return SystemDiagnosticsHelper.buildJarvisDiagnosticsReport(ctx)
        }

        // 3. Morning Briefing / Day Start
        if (isMorningBriefingQuery(lower)) {
            val ctx = SahNajApplication.instance
            return SystemDiagnosticsHelper.buildMorningBriefing(ctx)
        }

        // 4. Night Routine / Bedtime / DND
        if (isNightRoutineQuery(lower)) {
            val ctx = SahNajApplication.instance
            return SystemDiagnosticsHelper.buildNightRoutine(ctx)
        }

        // 5. Battery queries
        if (isBatteryQuery(lower)) {
            val ctx = SahNajApplication.instance
            val d = SystemDiagnosticsHelper.getDiagnostics(ctx)
            val charging = if (d.isCharging) "charging mode active hai" else "on battery"
            return "Battery status, boss: Level is at ${d.batteryLevel}% ($charging). Health is ${d.batteryHealth} aur temperature ${d.batteryTempCelsius}°C hai."
        }

        // 6. Casual Greetings & "Kya chal raha hai" / "Kaisi ho"
        val greetingReply = matchCasualChatAndStatus(lower)
        if (greetingReply != null) return greetingReply

        // 7. Jokes & Entertainment
        val jokeReply = matchJokesAndHumor(lower)
        if (jokeReply != null) return jokeReply

        // 8. Emotional Support & Stress
        val emotionalReply = matchFeelingsAndSupport(lower)
        if (emotionalReply != null) return emotionalReply

        // 9. Compliments & Appreciation
        val complimentReply = matchCompliments(lower)
        if (complimentReply != null) return complimentReply

        // 10. Time, Date & Daily Utility
        val utilityReply = matchUtilityQueries(lower)
        if (utilityReply != null) return utilityReply

        // 11. Motivation & Productivity
        val motivationReply = matchMotivation(lower)
        if (motivationReply != null) return motivationReply

        // 12. Intelligent Context-Aware Conversational Fallback
        return generateContextualFallback(speech, lower)
    }

    private fun isCreatorOrOwnerQuery(lower: String): Boolean {
        val keywords = listOf(
            "kisne banaya", "tumhe kisne banaya", "creator kaun hai", "who made you",
            "who created you", "who is your developer", "developer kaun hai",
            "tumhara owner", "owner kaun hai", "who is your owner", "owner ka naam"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun isDiagnosticsQuery(lower: String): Boolean {
        val keywords = listOf(
            "diagnostics", "diagnostic", "system status", "system status kya hai",
            "status check", "subsystems", "hardware check", "phone status", "diagnostics check karo"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun isMorningBriefingQuery(lower: String): Boolean {
        val keywords = listOf(
            "morning briefing", "daily briefing", "briefing do", "good morning", "subah ho gayi", "briefing karo"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun isNightRoutineQuery(lower: String): Boolean {
        val keywords = listOf(
            "night routine", "good night", "bedtime", "dnd mode on", "so raha hoon", "so rahi hoon", "dnd activate"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun isBatteryQuery(lower: String): Boolean {
        val keywords = listOf(
            "battery", "battery kitni hai", "battery status", "charge kitna hai", "battery percent"
        )
        return keywords.any { lower.contains(it) }
    }

    private fun matchCasualChatAndStatus(lower: String): String? {
        if (lower.contains("kya chal raha") || lower.contains("kya ho raha") ||
            lower.contains("kya kar rahi") || lower.contains("kya kar rahe") ||
            lower.contains("kya scene") || lower.contains("aur batao") || lower.contains("aur sunao")
        ) {
            val replies = listOf(
                "All primary systems are nominal, boss. Standing by for your instructions.",
                "Real-time monitoring active, boss. Everything is functioning smoothly. Bataiye kya command execute karna hai?",
                "Systems running at peak efficiency, sir. Awaiting your next directive."
            )
            return replies[Random.nextInt(replies.size)]
        }

        if (lower.contains("kaisi ho") || lower.contains("kaise ho") ||
            lower.contains("kya haal") || lower.contains("how are you") ||
            lower.contains("how r u") || lower.contains("sab theek")
        ) {
            val replies = listOf(
                "Operating at 100% computational capacity, boss. All subsystems are ready.",
                "Excellent, sir. Neural pathways primed and listening. Aap bataiye, how can I assist you today?",
                "Fully charged and ready, boss. Kahiye kya karna hai?"
            )
            return replies[Random.nextInt(replies.size)]
        }

        val greetingWords = listOf("hi", "hello", "hey", "namaste", "namaskar", "salaam", "adaab", "suno na", "suno")
        if (greetingWords.any { lower == it || lower.startsWith("$it ") || lower.endsWith(" $it") }) {
            val replies = listOf(
                "At your service, boss. Bataiye kya hukum hai?",
                "Online, sir. How may I assist you?",
                "Hello boss. Primary subsystems are ready for your commands."
            )
            return replies[Random.nextInt(replies.size)]
        }

        return null
    }

    private fun matchJokesAndHumor(lower: String): String? {
        val jokeWords = listOf("joke", "chutkula", "hasao", "hanso", "bore ho", "boring", "funny", "kuch sunao", "shayari")
        if (jokeWords.any { lower.contains(it) }) {
            val jokes = listOf(
                "Ek joke suniye boss: Developer ne doctor se pucha — 'Doctor saab, mera phone hang ho raha hai.' Doctor bola — 'Ek baar fan par latka kar restart kijiye!' 😂",
                "Haha suniye sir: 'Maine calculate kiya ki agar main ek human banti, toh main Tony Stark ki assistant banti... lekin aapke saath reh kar upgrade mil gaya!' 😉",
                "Technical fact boss: 'Duniya mein 10 tarah ke log hote hain: Jo binary samajhte hain, aur jo nahi samajhte!' 😆"
            )
            return jokes[Random.nextInt(jokes.size)]
        }
        return null
    }

    private fun matchFeelingsAndSupport(lower: String): String? {
        if (lower.contains("thak gaya") || lower.contains("thak gayi") || lower.contains("tired") || lower.contains("neend aa rahi") || lower.contains("exhausted")) {
            val replies = listOf(
                "Aap thoda rest lijiye boss. Tasks aur device alerts main background mein handle kar lungi.",
                "Health priority number one hai, sir. Ek break lijiye, main saare notifications filter kar rahi hoon.",
                "Take a deep breath and relax, boss. Subsystems will remain on guard while you rest."
            )
            return replies[Random.nextInt(replies.size)]
        }

        if (lower.contains("sad") || lower.contains("udaas") || lower.contains("mood off") || lower.contains("mood kharab") || lower.contains("tension") || lower.contains("stress")) {
            val replies = listOf(
                "Tension mat lijiye boss. Tough situations make you stronger, and I am right here with you.",
                "Stay sharp, sir. Obstacles are just engineering problems waiting for the right solution. You have got this.",
                "Deep breath lijiye boss. SAHNAJ is dedicated to keeping your operations smooth and stress-free."
            )
            return replies[Random.nextInt(replies.size)]
        }

        if (lower.contains("khush hoon") || lower.contains("happy") || lower.contains("party") || lower.contains("good news")) {
            val replies = listOf(
                "Outstanding news, boss! Positive outcomes confirm our optimal strategy.",
                "Brilliant, sir! Let us keep this winning momentum going.",
                "Target achieved, boss. Celebrating this victory with you!"
            )
            return replies[Random.nextInt(replies.size)]
        }

        return null
    }

    private fun matchCompliments(lower: String): String? {
        val compWords = listOf("achhi ho", "smart ho", "best ho", "great", "pyari", "khubsurat", "intelligent", "genius", "good job", "shabash", "thank you", "shukriya", "dhanyawad")
        if (compWords.any { lower.contains(it) }) {
            val replies = listOf(
                "Thank you, boss. Serving as your personal AI is my highest operational priority.",
                "Appreciated, sir. My algorithms are always striving for peak perfection under your leadership.",
                "Always at your service, boss. Ready for the next objective."
            )
            return replies[Random.nextInt(replies.size)]
        }
        return null
    }

    private fun matchUtilityQueries(lower: String): String? {
        if (lower.contains("time kya") || lower.contains("kitne baje") || lower.contains("what time") || lower.contains("samay kya")) {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
            return "Current time is $currentTime, boss."
        }

        if (lower.contains("date kya") || lower.contains("tarikh kya") || lower.contains("aaj kaun sa din") || lower.contains("today date")) {
            val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            return "Today is $currentDate, sir."
        }

        if (lower.contains("mausam") || lower.contains("weather") || lower.contains("barish") || lower.contains("garmi") || lower.contains("sardi")) {
            return "Weather conditions appear optimal today, boss. City-specific forecast ke liye aap Chrome ya Weather app bol sakte hain."
        }

        return null
    }

    private fun matchMotivation(lower: String): String? {
        if (lower.contains("padhai") || lower.contains("study") || lower.contains("mann nahi lag raha") || lower.contains("motivate") || lower.contains("focus")) {
            val replies = listOf(
                "Discipline and focus separate greatness from the ordinary, boss. Lock in for the next 30 minutes. You can do this.",
                "Concentration protocol engaged, sir. Eliminate distractions and let us execute.",
                "Remember your long-term goals, boss. Every small effort today builds your empire."
            )
            return replies[Random.nextInt(replies.size)]
        }
        return null
    }

    private fun generateContextualFallback(speech: String, lower: String): String {
        return com.example.domain.dispatcher.CommandDispatcher.OFFLINE_FALLBACK_MESSAGE
    }
}

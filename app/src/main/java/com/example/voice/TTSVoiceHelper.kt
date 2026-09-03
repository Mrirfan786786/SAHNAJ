package com.example.voice

import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

/**
 * Helper utility for selecting and tuning Android Text-to-Speech (TTS) voices.
 * Configured specifically for a young, lively, friendly female voice persona (SahNaj).
 */
object TTSVoiceHelper {

    private const val TAG = "SAHNAJ_TTS_VOICE"

    // Calibrated Voice Parameters for SahNaj's natural, warm persona
    const val DEFAULT_FEMALE_PITCH = 1.00f      // 1.00x natural human pitch
    const val FALLBACK_FEMALE_PITCH = 1.05f     // Subtle lift for generic fallback voices
    const val DEFAULT_SPEECH_RATE = 0.98f       // 0.98x for clear, non-rushed, natural human cadence
    const val MIN_SPEECH_RATE = 0.6f
    const val MAX_SPEECH_RATE = 1.6f

    // Supported Locales
    val LOCALE_HINDI: Locale = Locale.forLanguageTag("hi-IN")
    val LOCALE_INDIAN_ENGLISH: Locale = Locale.forLanguageTag("en-IN")
    val LOCALE_US_ENGLISH: Locale = Locale.US

    // High Quality Google Hindi Voice Signatures (Ranked by naturalness)
    private val HIGH_QUALITY_GOOGLE_HINDI_VOICES = listOf(
        "hi-in-x-hie-local", "hi-in-x-hie-network", "hi-in-x-hie",
        "hi-in-x-hid-local", "hi-in-x-hid-network", "hi-in-x-hid",
        "hi-in-x-hia-local", "hi-in-x-hia-network", "hi-in-x-hia",
        "hi-in-x-hic-local", "hi-in-x-hic-network", "hi-in-x-hic",
        "hi-in-x-hif-local", "hi-in-x-hif-network", "hi-in-x-hif"
    )

    // Known Young Female Voice Signatures (Google TTS, Samsung TTS, Xiaomi, AOSP)
    private val KNOWN_FEMALE_SIGNATURES = listOf(
        // Google TTS Hindi female voices (highest quality young female variants)
        "hi-in-x-hie", "hi-in-x-hid", "hi-in-x-hic", "hi-in-x-hif", "hi-in-x-hia",
        // Google TTS Indian English female voices
        "en-in-x-end", "en-in-x-enc", "en-in-x-ene", "en-in-x-ena", "en-in-x-cxx",
        // Google TTS US English female voices
        "en-us-x-sfg", "en-us-x-tpd", "en-us-x-iol", "en-us-x-iob", "en-us-x-tpc", "en-us-x-tpf",
        // General tokens & OEM female markers
        "female", "#female", "_female", "-female", "woman", "girl", "fem", "f01", "f02", "f03",
        "voice-f", "voice_f", "variant-f", "w-local", "c-local", "d-local", "e-local"
    )

    // Known Male Voice Signatures
    private val KNOWN_MALE_SIGNATURES = listOf(
        "male", "#male", "_male", "-male", "man", "boy", "m01", "m02", "m03",
        "hi-in-x-hib-local", "hi-in-x-hib-network", "hi-in-x-hib", "en-in-x-enb", "en-us-x-sfd", "en-us-x-tpb", "en-us-x-iom"
    )

    data class VoiceSelectionResult(
        val voice: Voice?,
        val locale: Locale,
        val isConfirmedFemale: Boolean,
        val recommendedPitch: Float
    )

    /**
     * Finds the best matching female voice for the given target language and text content.
     */
    fun selectBestVoice(
        tts: TextToSpeech?,
        preferredLanguage: String,
        sampleText: String = ""
    ): VoiceSelectionResult {
        if (tts == null) {
            return VoiceSelectionResult(
                voice = null,
                locale = LOCALE_HINDI,
                isConfirmedFemale = false,
                recommendedPitch = FALLBACK_FEMALE_PITCH
            )
        }

        // Determine effective target locale based on user setting & text script
        val targetLocale = resolveTargetLocale(preferredLanguage, sampleText)

        // Try setting the language first
        var activeLocale = targetLocale
        val langResult = tts.setLanguage(targetLocale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Locale $targetLocale not supported. Falling back to alternative locale.")
            if (targetLocale == LOCALE_HINDI) {
                // Fallback to Indian English
                val enInResult = tts.setLanguage(LOCALE_INDIAN_ENGLISH)
                if (enInResult != TextToSpeech.LANG_MISSING_DATA && enInResult != TextToSpeech.LANG_NOT_SUPPORTED) {
                    activeLocale = LOCALE_INDIAN_ENGLISH
                } else {
                    tts.setLanguage(LOCALE_US_ENGLISH)
                    activeLocale = LOCALE_US_ENGLISH
                }
            } else if (targetLocale == LOCALE_INDIAN_ENGLISH) {
                tts.setLanguage(LOCALE_US_ENGLISH)
                activeLocale = LOCALE_US_ENGLISH
            }
        }

        // Search among available voices in the TTS engine
        try {
            val availableVoices = tts.voices
            if (!availableVoices.isNullOrEmpty()) {
                val candidate = evaluateAndRankVoices(availableVoices, activeLocale)
                if (candidate != null) {
                    val isFemale = isFemaleVoice(candidate)
                    val pitch = if (isFemale) DEFAULT_FEMALE_PITCH else FALLBACK_FEMALE_PITCH
                    Log.d(TAG, "Selected Voice: '${candidate.name}' for locale: ${candidate.locale} (isFemale=$isFemale, pitch=$pitch)")
                    return VoiceSelectionResult(
                        voice = candidate,
                        locale = candidate.locale,
                        isConfirmedFemale = isFemale,
                        recommendedPitch = pitch
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying TTS available voices: ${e.message}")
        }

        // Fallback: No specific Voice object selected, engine will use activeLocale default
        Log.d(TAG, "Fallback to default voice for locale: $activeLocale with pitch $FALLBACK_FEMALE_PITCH")
        return VoiceSelectionResult(
            voice = null,
            locale = activeLocale,
            isConfirmedFemale = false,
            recommendedPitch = FALLBACK_FEMALE_PITCH
        )
    }

    /**
     * Resolves the appropriate locale given the user preference and optional utterance text.
     */
    fun resolveTargetLocale(preferredLanguage: String, text: String = ""): Locale {
        // If text contains Devanagari characters, always prefer Hindi locale for accurate phonetics
        val containsDevanagari = text.any { it in '\u0900'..'\u097F' }
        if (containsDevanagari) {
            return LOCALE_HINDI
        }

        return when (preferredLanguage.trim().lowercase()) {
            "hindi", "hinglish" -> LOCALE_HINDI
            "english" -> LOCALE_INDIAN_ENGLISH
            else -> LOCALE_HINDI
        }
    }

    /**
     * Evaluates all voices and scores them based on locale match, female markers,
     * quality, and local availability.
     */
    private fun evaluateAndRankVoices(voices: Set<Voice>, targetLocale: Locale): Voice? {
        val scoredVoices = voices.mapNotNull { voice ->
            val score = calculateVoiceScore(voice, targetLocale)
            if (score > -100) Pair(voice, score) else null
        }.sortedByDescending { it.second }

        if (scoredVoices.isNotEmpty()) {
            val topChoice = scoredVoices.first().first
            Log.d(TAG, "Top voice candidate: ${topChoice.name} (Score: ${scoredVoices.first().second})")
            return topChoice
        }

        return null
    }

    /**
     * Calculates a numerical score for a voice based on quality, gender match, and locale.
     */
    private fun calculateVoiceScore(voice: Voice, targetLocale: Locale): Int {
        var score = 0
        val nameLower = voice.name.lowercase()
        val voiceLang = voice.locale.language.lowercase()
        val voiceCountry = voice.locale.country.lowercase()
        val targetLang = targetLocale.language.lowercase()
        val targetCountry = targetLocale.country.lowercase()

        // 1. Feature Check: Penalize if not installed
        val features = voice.features ?: emptySet()
        if (features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) || features.contains("notInstalled")) {
            score -= 500
        }

        // 2. Language Matching
        if (voiceLang == targetLang) {
            score += 100
            if (voiceCountry == targetCountry) {
                score += 50
            }
        } else if ((targetLang == "hi" && voiceLang == "en" && voiceCountry == "in") ||
                   (targetLang == "en" && voiceLang == "hi")) {
            // Compatible Indian multilingual fallbacks
            score += 30
        } else {
            // Different language
            score -= 100
        }

        // 3. High-Quality Google Hindi Neural Voice Recognition
        for (hqSig in HIGH_QUALITY_GOOGLE_HINDI_VOICES) {
            if (nameLower.contains(hqSig)) {
                score += 250
                break
            }
        }

        // 4. Gender Scoring
        var hasFemaleSignature = false
        for (sig in KNOWN_FEMALE_SIGNATURES) {
            if (nameLower.contains(sig)) {
                score += 150
                hasFemaleSignature = true
                break
            }
        }

        for (maleSig in KNOWN_MALE_SIGNATURES) {
            if (nameLower.contains(maleSig)) {
                score -= 100
                break
            }
        }

        // 5. Quality & Latency bonus
        if (voice.quality == Voice.QUALITY_VERY_HIGH) {
            score += 40
        } else if (voice.quality == Voice.QUALITY_HIGH) {
            score += 25
        }

        // 5. Offline preference (Local voices are more reliable and have lower latency)
        if (!voice.isNetworkConnectionRequired) {
            score += 30
        }

        return score
    }

    /**
     * Checks if a voice matches known female patterns.
     */
    fun isFemaleVoice(voice: Voice?): Boolean {
        if (voice == null) return false
        val nameLower = voice.name.lowercase()
        for (sig in KNOWN_FEMALE_SIGNATURES) {
            if (nameLower.contains(sig)) return true
        }
        return false
    }

    /**
     * Determines whether text appears to be primarily English or Hindi/Hinglish.
     */
    fun isTextHindiOrHinglish(text: String): Boolean {
        if (text.any { it in '\u0900'..'\u097F' }) return true
        val lower = text.lowercase()
        val hinglishMarkers = listOf(
            "karo", "khol", "kholo", "batao", "banao", "call", "lagao", "hai", "hain", "hoon",
            "kya", "kaise", "suno", "sun", "yaar", "dost", "mera", "meri", "mere", "aap", "tum",
            "kar diya", "ho gaya", "bolo", "kahiye", "shukriya", "madad", "band"
        )
        return hinglishMarkers.any { lower.contains(it) }
    }

    /**
     * Strips all Unicode emojis, symbols, pictographs, variation selectors,
     * skin-tone modifiers, and emoticons from text before sending to Text-to-Speech engine.
     * Ensures the TTS never pronounces emoji names (e.g. "smiling face", "sparkles", etc.)
     * while completely preserving Hindi (Devanagari), English, numbers, currency symbols, and punctuation.
     */
    fun stripEmojis(text: String): String {
        if (text.isEmpty()) return ""

        val sb = StringBuilder(text.length)
        var i = 0
        val len = text.length
        while (i < len) {
            val codePoint = text.codePointAt(i)
            val charCount = Character.charCount(codePoint)

            val isEmoji = isEmojiCodePoint(codePoint)

            if (!isEmoji) {
                sb.appendCodePoint(codePoint)
            } else {
                sb.append(' ')
            }
            i += charCount
        }

        return sb.toString()
            .replace(Regex("[\\s\\u00A0\\u2000-\\u200B]+"), " ")
            .trim()
    }

    /**
     * Checks if a Unicode code point is an emoji, pictograph, or emoji-modifier.
     */
    private fun isEmojiCodePoint(codePoint: Int): Boolean {
        // 1. Emoji & Pictograph Specific Unicode Blocks
        if (codePoint in 0x1F600..0x1F64F) return true // Emoticons (Faces)
        if (codePoint in 0x1F300..0x1F5FF) return true // Misc Symbols & Pictographs
        if (codePoint in 0x1F680..0x1F6FF) return true // Transport & Map
        if (codePoint in 0x1F700..0x1F77F) return true // Alchemical Symbols
        if (codePoint in 0x1F780..0x1F7FF) return true // Geometric Shapes Extended
        if (codePoint in 0x1F800..0x1F8FF) return true // Supplemental Arrows-C
        if (codePoint in 0x1F900..0x1F9FF) return true // Supplemental Symbols & Pictographs
        if (codePoint in 0x1FA00..0x1FA6F) return true // Chess Symbols
        if (codePoint in 0x1FA70..0x1FAFF) return true // Symbols & Pictographs Extended-A
        if (codePoint in 0x1F1E6..0x1F1FF) return true // Regional Indicator Symbols (Flags)
        if (codePoint in 0x2600..0x26FF) return true   // Miscellaneous Symbols (e.g. ☀️, ☁️, ⚡, ☕, ⚠️)
        if (codePoint in 0x2700..0x27BF) return true   // Dingbats (e.g. ✨, ✂️, ✈️, ✉️, ✌️, ✔️)
        if (codePoint in 0x2300..0x23FF) return true   // Miscellaneous Technical (e.g. ⌛, ⌚, ⌨️)
        if (codePoint in 0x2B50..0x2B55) return true   // Star, Circles (⭐, ⭕, etc.)
        if (codePoint in 0x2934..0x2935) return true   // Arrows
        if (codePoint in 0x25AA..0x25FE) return true   // Geometric shapes
        if (codePoint in 0xFE00..0xFE0F) return true   // Variation Selectors (VS-1 to VS-16)
        if (codePoint == 0x200D) return true           // Zero Width Joiner (ZWJ)
        if (codePoint in 0xE0020..0xE007F) return true // Tag characters

        // 2. Character Categories: Other Symbol (So) and Modifier Symbol (Sk) that aren't currency or math
        val type = Character.getType(codePoint)
        if (type == Character.OTHER_SYMBOL.toInt() || type == Character.MODIFIER_SYMBOL.toInt()) {
            val charStr = String(Character.toChars(codePoint))
            val isAllowedSymbol = charStr in listOf("₹", "$", "€", "£", "¥", "¢", "%", "+", "-", "=", "<", ">", "/", "*", "°", "±", "×", "÷")
            if (!isAllowedSymbol) {
                return true
            }
        }

        return false
    }
}

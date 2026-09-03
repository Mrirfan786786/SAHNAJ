package com.example.voice

import java.util.Locale
import java.util.regex.Pattern

/**
 * TtsPhoneticNormalizer:
 * Pre-TTS dynamic script and phonetic processing engine for SAHNAJ AI.
 *
 * Solves:
 * 1. Mispronunciations of Hinglish / Roman Urdu words (e.g. "Sunkar jawab dungi" -> "सुनकर जवाब दूंगी").
 * 2. Unnatural foreign English accents on Indian sentences when sent to the speech synthesizer.
 * 3. Reading aloud of Markdown asterisks, backticks, hashes, bullet points, and Unicode emojis.
 * 4. Syllable stretching and unnatural cadence in both Android native TTS and ElevenLabs Multilingual v2.
 */
object TtsPhoneticNormalizer {

    private val EMOJI_AND_SYMBOLS_REGEX = Regex("[\\p{So}\\p{Sk}\\p{Cn}\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+")
    private val MARKDOWN_BOLD_REGEX = Regex("\\*\\*(.*?)\\*\\*|__(.*?)__")
    private val MARKDOWN_ITALIC_REGEX = Regex("\\*(.*?)\\*|_(.*?)_")
    private val MARKDOWN_CODE_REGEX = Regex("`{1,3}(.*?)`{1,3}")
    private val MARKDOWN_HEADERS_REGEX = Regex("(?m)^#{1,6}\\s*")
    private val MARKDOWN_BULLETS_REGEX = Regex("(?m)^[\\*\\-\\+]\\s+")
    private val MARKDOWN_LINKS_REGEX = Regex("\\[(.*?)\\]\\((.*?)\\)")
    private val MULTIPLE_SPACES_REGEX = Regex("\\s+")

    // Comprehensive Hinglish & Common Word Dictionary for high-accuracy Devanagari mapping
    private val HINGLISH_TO_DEVANAGARI_MAP: Map<String, String> = mapOf(
        // Assistant Identity & Persona
        "sahnaj" to "शहनाज़",
        "shahnaz" to "शहनाज़",
        "sehnaj" to "शहनाज़",
        "sahnaz" to "शहनाज़",
        "aisha" to "आयशा",
        "ananya" to "अनन्या",
        "priya" to "प्रिया",
        "zara" to "ज़ारा",
        "riya" to "रिया",
        "meera" to "मीरा",
        "kavya" to "काव्या",
        "simran" to "सिमरन",
        "fatima" to "फ़ातिमा",
        "aryan" to "आर्यन",
        "kabir" to "कबीर",
        "rohan" to "रोहन",
        "rahul" to "राहुल",
        "zaid" to "ज़ैद",
        "vikram" to "विक्रम",
        "aman" to "अमन",
        "dev" to "देव",
        "sameer" to "समीर",
        "farhan" to "फ़रहान",

        // Conversational Core & Problem Words
        "sunkar" to "सुनकर",
        "sun kar" to "सुन कर",
        "jawab" to "जवाब",
        "jawaab" to "जवाब",
        "dungi" to "दूंगी",
        "doongi" to "दूंगी",
        "dunga" to "दूँगा",
        "doonga" to "दूँगा",
        "dungi." to "दूंगी।",
        "dunga." to "दूँगा।",
        "suno" to "सुनो",
        "suna" to "सुना",
        "suniye" to "सुनिए",
        "sunaye" to "सुनाए",
        "sunao" to "सुनाओ",
        "sun rahi hoon" to "सुन रही हूँ",
        "sun raha hoon" to "सुन रहा हूँ",
        "bolo" to "बोलो",
        "boliye" to "बोलिए",
        "bol" to "बोल",
        "bol rahi hoon" to "बोल रही हूँ",
        "bol raha hoon" to "बोल रहा हूँ",
        "batao" to "बताओ",
        "bataiye" to "बताइए",
        "bata" to "बता",
        "bata rahi hoon" to "बता रही हूँ",
        "bata raha hoon" to "बता रहा हूँ",
        "kaho" to "कहो",
        "kahiye" to "कहिए",
        "samajh" to "समझ",
        "samajha" to "समझा",
        "samajh gayi" to "समझ गई",
        "samajh gaya" to "समझ गया",
        "samajh sakti hoon" to "समझ सकती हूँ",
        "samajh sakta hoon" to "समझ सकता हूँ",

        // Greetings & Adab
        "namaste" to "नमस्ते",
        "namaskar" to "नमस्कार",
        "pranam" to "प्रणाम",
        "salaam" to "सलाम",
        "adaab" to "आदाब",
        "adab" to "आदाब",
        "shukriya" to "शुक्रिया",
        "dhanyawad" to "धन्यवाद",
        "dhanyavaad" to "धन्यवाद",
        "alvida" to "अलविदा",
        "khudahafez" to "खुदा हाफ़िज़",
        "khuda hafiz" to "खुदा हाफ़िज़",
        "kripya" to "कृपया",
        "meherbani" to "मेहरबानी",
        "hukum" to "हुक्म",
        "hukam" to "हुक्म",
        "hukum kijiye" to "हुक्म कीजिए",
        "farmaye" to "फ़रमाइए",
        "farmaiye" to "फ़रमाइए",
        "hazir" to "हाज़िर",
        "haazir" to "हाज़िर",
        "haazir hoon" to "हाज़िर हूँ",

        // Affirmations & Status
        "haan" to "हाँ",
        "ha" to "हाँ",
        "haanji" to "हाँजी",
        "ji haan" to "जी हाँ",
        "nahi" to "नहीं",
        "nahin" to "नहीं",
        "ji nahi" to "जी नहीं",
        "thik" to "ठीक",
        "theek" to "ठीक",
        "thik hai" to "ठीक है",
        "theek hai" to "ठीक है",
        "achha" to "अच्छा",
        "acha" to "अच्छा",
        "achhi" to "अच्छी",
        "achi" to "अच्छी",
        "achhe" to "अच्छे",
        "bilkul" to "बिल्कुल",
        "zaroor" to "ज़रूर",
        "jarur" to "ज़रूर",
        "sahi" to "सही",
        "galat" to "गलत",
        "shandar" to "शानदार",
        "zabardast" to "ज़बरदस्त",
        "badhiya" to "बढ़िया",
        "badiya" to "बढ़िया",
        "mast" to "मस्त",

        // Actions & Confirmations
        "kar diya" to "कर दिया",
        "kar diya hai" to "कर दिया है",
        "ho gaya" to "हो गया",
        "ho gaya hai" to "हो गया है",
        "kar rahi hoon" to "कर रही हूँ",
        "kar raha hoon" to "कर रहा हूँ",
        "karta hoon" to "करता हूँ",
        "karti hoon" to "करती हूँ",
        "karenge" to "करेंगे",
        "karo" to "करो",
        "kijiye" to "कीजिए",
        "karna" to "करना",
        "karne" to "करने",
        "khol" to "खोल",
        "kholo" to "खोलो",
        "kholiye" to "खोलिए",
        "khol diya" to "खोल दिया",
        "khol rahi hoon" to "खोल रही हूँ",
        "khol raha hoon" to "खोल रहा हूँ",
        "band" to "बंद",
        "band karo" to "बंद करो",
        "band kijiye" to "बंद कीजिए",
        "band kar diya" to "बंद कर दिया",
        "chalu" to "चालू",
        "chalu karo" to "चालू करो",
        "chalu kijiye" to "चालू कीजिए",
        "chalu kar diya" to "चालू कर दिया",
        "rok" to "रोक",
        "roko" to "रोको",
        "rokiye" to "रोकिए",
        "rok diya" to "रोक दिया",
        "chalao" to "चलाओ",
        "chalaiye" to "चलाइए",
        "chala diya" to "चला दिया",
        "bhejo" to "भेजो",
        "bhejiye" to "भेजिए",
        "bhej diya" to "भेज दिया",
        "lagao" to "लगाओ",
        "lagaiye" to "लगाइए",
        "laga diya" to "लगा दिया",
        "hatao" to "हटाओ",
        "hataiye" to "हटाइए",
        "hata diya" to "हटा दिया",
        "badhao" to "बढ़ाओ",
        "badhaiye" to "बढ़ाइए",
        "ghatao" to "घटाओ",
        "ghataiye" to "घटाइए",
        "dhoondho" to "ढूँढो",
        "dhoondhiye" to "ढूँढिए",
        "dhoondh rahi hoon" to "ढूँढ रही हूँ",
        "mil gaya" to "मिल गया",
        "nahi mila" to "नहीं मिला",

        // Question Words & Pronouns
        "kya" to "क्या",
        "kaise" to "कैसे",
        "kaisi" to "कैसी",
        "kaisa" to "कैसा",
        "kyun" to "क्यों",
        "kyu" to "क्यों",
        "kyon" to "क्यों",
        "kab" to "कब",
        "kahan" to "कहाँ",
        "kaha" to "कहाँ",
        "kidhar" to "किधर",
        "kaun" to "कौन",
        "kiska" to "किसका",
        "kiski" to "किसकी",
        "kisko" to "किसको",
        "kitna" to "कितना",
        "kitni" to "कितनी",
        "kitne" to "कितने",
        "aap" to "आप",
        "aapka" to "आपका",
        "aapki" to "आपकी",
        "aapke" to "आपके",
        "aapko" to "आपको",
        "tum" to "तुम",
        "tumhara" to "तुम्हारा",
        "tumhari" to "तुम्हारी",
        "tumhare" to "तुम्हारे",
        "tumhe" to "तुम्हें",
        "main" to "मैं",
        "mai" to "मैं",
        "mera" to "मेरा",
        "meri" to "मेरी",
        "mere" to "मेरे",
        "mujhe" to "मुझे",
        "mujhko" to "मुझको",
        "hum" to "हम",
        "hamara" to "हमारा",
        "hamari" to "हमारी",
        "hamare" to "हमारे",
        "hume" to "हमें",
        "yeh" to "यह",
        "ye" to "ये",
        "is" to "इस",
        "iska" to "इसका",
        "iski" to "इसकी",
        "iske" to "इसके",
        "isko" to "इसको",
        "woh" to "वह",
        "wo" to "वो",
        "us" to "उस",
        "uska" to "उसका",
        "uski" to "उसकी",
        "uske" to "उसके",
        "usko" to "उसको",
        "yaar" to "यार",
        "dost" to "दोस्त",
        "bhai" to "भाई",
        "sahab" to "साहब",
        "sir" to "सर",
        "madam" to "मैडम",

        // Auxiliary & Prepositions
        "hai" to "है",
        "hain" to "हैं",
        "hoon" to "हूँ",
        "hun" to "हूँ",
        "ho" to "हो",
        "tha" to "था",
        "thi" to "थी",
        "the" to "थे",
        "hoga" to "होगा",
        "hogi" to "होगी",
        "honge" to "होंगे",
        "ka" to "का",
        "ki" to "की",
        "ke" to "के",
        "ko" to "को",
        "se" to "से",
        "mein" to "में",
        "me" to "में",
        "par" to "पर",
        "tak" to "तक",
        "liye" to "लिए",
        "saath" to "साथ",
        "bhi" to "भी",
        "aur" to "और",
        "ya" to "या",
        "lekin" to "लेकिन",
        "magar" to "मगर",
        "parantu" to "परन्तु",
        "kyunki" to "क्योंकि",
        "kyuki" to "क्योंकि",
        "agar" to "अगर",
        "yadi" to "यदि",
        "toh" to "तो",
        "to" to "तो",
        "ab" to "अब",
        "abhi" to "अभी",
        "tab" to "तब",
        "jab" to "जब",
        "aaj" to "आज",
        "kal" to "कल",
        "parso" to "परसों",
        "subah" to "सुबह",
        "dopahar" to "दोपहर",
        "shaam" to "शाम",
        "raat" to "रात",
        "din" to "दिन",
        "waqt" to "वक़्त",
        "samay" to "समय",
        "minute" to "मिनट",
        "second" to "सेकंड",
        "ghanta" to "घंटा",
        "ghante" to "घंटे",

        // Technical, App, System & Device loanwords with accurate phonetic Devanagari
        "whatsapp" to "व्हाट्सएप",
        "what's app" to "व्हाट्सएप",
        "youtube" to "यूट्यूब",
        "you tube" to "यूट्यूब",
        "google" to "गूगल",
        "chrome" to "क्रोम",
        "instagram" to "इंस्टाग्राम",
        "facebook" to "फ़ेसबुक",
        "twitter" to "ट्विटर",
        "telegram" to "टेलीग्राम",
        "spotify" to "स्पॉटिफ़ाई",
        "settings" to "सेटिंग्स",
        "setting" to "सेटिंग",
        "camera" to "कैमरा",
        "flashlight" to "फ्लैशलाइट",
        "torch" to "टॉर्च",
        "bluetooth" to "ब्लूटूथ",
        "wifi" to "वाई-फ़ाई",
        "wi-fi" to "वाई-फ़ाई",
        "hotspot" to "हॉटस्पॉट",
        "battery" to "बैटरी",
        "volume" to "वॉल्यूम",
        "brightness" to "ब्राइटनेस",
        "screen" to "स्क्रीन",
        "calculator" to "कैलकुलेटर",
        "alarm" to "अलार्म",
        "timer" to "टाइमर",
        "stopwatch" to "स्टॉपवॉच",
        "gallery" to "गैलरी",
        "photos" to "फ़ोटो",
        "photo" to "फ़ोटो",
        "music" to "म्यूज़िक",
        "song" to "गाना",
        "songs" to "गाने",
        "video" to "वीडियो",
        "videos" to "वीडियो",
        "call" to "कॉल",
        "calling" to "कॉलिंग",
        "dial" to "डायल",
        "message" to "मैसेज",
        "sms" to "एसएमएस",
        "contact" to "कॉन्टैक्ट",
        "contacts" to "कॉन्टैक्ट्स",
        "notification" to "नोटिफिकेशन",
        "notifications" to "नोटिफिकेशन्स",
        "lock" to "लॉक",
        "unlock" to "अनलॉक",
        "restart" to "रीस्टार्ट",
        "reboot" to "रीबूट",
        "battery level" to "बैटरी लेवल",
        "percent" to "प्रतिशत",
        "percentage" to "प्रतिशत",
        "internet" to "इंटरनेट",
        "network" to "नेटवर्क",
        "airplane mode" to "एरोप्लेन मोड",
        "flight mode" to "फ़्लाइट मोड",
        "do not disturb" to "डू नॉट डिस्टर्ब",
        "silent" to "साइलेंट",
        "vibrate" to "वाइब्रेट",
        "ring" to "रिंग",
        "ringtone" to "रिंगटोन"
    )

    /**
     * Normalizes text specifically for Text-to-Speech synthesis (both Android Native TTS and ElevenLabs).
     *
     * 1. Strips emojis and weird Unicode symbols (avoids TTS announcing emoji titles).
     * 2. Cleans Markdown headers, bold/italic asterisks, backticks, bullets, URLs.
     * 3. Transliterates Roman Hinglish into authentic Devanagari script so native Indian TTS voices
     *    pronounce it with authentic human intonation, natural vowels, and zero foreign distortion.
     */
    fun normalizeForSpeech(rawText: String): String {
        if (rawText.isBlank()) return ""

        // Step 1: Strip Emojis and Symbols
        var clean = TTSVoiceHelper.stripEmojis(rawText)
        clean = EMOJI_AND_SYMBOLS_REGEX.replace(clean, " ")

        // Step 2: Strip Markdown Syntax
        clean = MARKDOWN_LINKS_REGEX.replace(clean) { it.groupValues[1] }
        clean = MARKDOWN_BOLD_REGEX.replace(clean) { it.groupValues[1].ifEmpty { it.groupValues[2] } }
        clean = MARKDOWN_ITALIC_REGEX.replace(clean) { it.groupValues[1].ifEmpty { it.groupValues[2] } }
        clean = MARKDOWN_CODE_REGEX.replace(clean) { it.groupValues[1] }
        clean = MARKDOWN_HEADERS_REGEX.replace(clean, "")
        clean = MARKDOWN_BULLETS_REGEX.replace(clean, "")
        clean = clean.replace(Regex("[#*_`~>|\\[\\]\\{\\}]"), " ")

        // Step 3: Normalize whitespace
        clean = MULTIPLE_SPACES_REGEX.replace(clean, " ").trim()

        // Step 4: If text is predominantly Roman Hinglish, convert to authentic Devanagari
        val devanagariTransliterated = convertHinglishToDevanagariPhonetic(clean)

        return devanagariTransliterated
    }

    /**
     * Converts a Roman Hinglish sentence into authentic Devanagari Hindi text.
     * Preserves existing Devanagari text, numbers, punctuation, and converts Roman words.
     */
    fun convertHinglishToDevanagariPhonetic(text: String): String {
        if (text.isBlank()) return ""

        // Check if text already has significant Devanagari characters
        val devanagariCharCount = text.count { it in '\u0900'..'\u097F' }
        val totalLetters = text.count { it.isLetter() }

        // If it's already mostly Devanagari, just apply standard phonetic word fixes
        if (totalLetters > 0 && (devanagariCharCount.toFloat() / totalLetters) > 0.6f) {
            return polishExistingDevanagari(text)
        }

        // Tokenize and replace known multi-word phrases first (e.g. "sunkar jawab dungi")
        var processed = text
        val multiWordEntries = HINGLISH_TO_DEVANAGARI_MAP.entries
            .filter { it.key.contains(" ") }
            .sortedByDescending { it.key.length }

        for ((phrase, devanagari) in multiWordEntries) {
            val pattern = Pattern.compile("(?i)\\b" + Pattern.quote(phrase) + "\\b")
            processed = pattern.matcher(processed).replaceAll(devanagari)
        }

        // Tokenize remaining words and replace single Hinglish words
        val words = processed.split(Regex("(?<=\\s)|(?=\\s)|(?<=[.,!?;:()।])|(?=[.,!?;:()।])"))
        val sb = StringBuilder()

        for (token in words) {
            val trimmed = token.trim()
            if (trimmed.isEmpty() || trimmed.all { !it.isLetter() }) {
                sb.append(token)
                continue
            }

            val lower = trimmed.lowercase()
            val mapped = HINGLISH_TO_DEVANAGARI_MAP[lower]

            if (mapped != null) {
                sb.append(mapped)
            } else if (trimmed.any { it in '\u0900'..'\u097F' }) {
                sb.append(trimmed)
            } else {
                // If it's an unmapped English word or name, retain it cleanly or apply light phonetic rule
                val fallbackTranslit = transliterateSingleWordIfHinglish(lower)
                sb.append(fallbackTranslit ?: trimmed)
            }
        }

        return sb.toString()
            .replace(Regex("\\s+"), " ")
            .replace(" .", "।")
            .replace(".", "।")
            .trim()
    }

    /**
     * Polishes existing Devanagari text for optimal speech synthesis.
     */
    private fun polishExistingDevanagari(text: String): String {
        return text
            .replace("हुकुम", "हुक्म")
            .replace("जवाब दूंगी", "जवाब दूंगी।")
            .replace("कर दिया", "कर दिया।")
            .replace(" .", "।")
            .trim()
    }

    /**
     * Fallback phonetic transliterator for unmapped Roman Hinglish words.
     */
    private fun transliterateSingleWordIfHinglish(word: String): String? {
        val w = word.lowercase()

        // Common suffixes and patterns in Hinglish
        return when {
            w.endsWith("karo") -> w.removeSuffix("karo") + " करो"
            w.endsWith("dungi") -> "दूंगी"
            w.endsWith("dunga") -> "दूँगा"
            w.endsWith("wala") -> "वाला"
            w.endsWith("wali") -> "वाली"
            w.endsWith("wale") -> "वाले"
            w.endsWith("gaya") -> "गया"
            w.endsWith("gayi") -> "गई"
            w.endsWith("rahi") -> "रही"
            w.endsWith("raha") -> "रहा"
            w.endsWith("hoga") -> "होगा"
            w.endsWith("hogi") -> "होगी"
            w == "pls" || w == "plz" -> "कृपया"
            w == "thx" || w == "tq" -> "धन्यवाद"
            w == "sunkar" -> "सुनकर"
            w == "jawab" -> "जवाब"
            w == "dungi" -> "दूंगी"
            w == "dungii" -> "दूंगी"
            w == "dunga" -> "दूँगा"
            else -> null
        }
    }
}

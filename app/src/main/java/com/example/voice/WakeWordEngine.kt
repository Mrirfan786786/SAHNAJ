package com.example.voice

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WakeWordEngine {

    private val _isListeningForWakeWord = MutableStateFlow(false)
    val isListeningForWakeWord: StateFlow<Boolean> = _isListeningForWakeWord.asStateFlow()

    private val defaultVariants = hashSetOf(
        "sahnaj", "sahnaz", "sanaj", "sanaz", "shahnaz", "shahnaaj", "sahnaaj",
        "sahnaji", "sahnaj ji", "shahnaj", "shehnaz", "sahnaj ai", "sahna",
        "sahnajj", "shahnajj", "sahnaaz", "shana", "shahnaz", "sehnaaz", "sehnaz",
        "sahanaj", "sahanaz", "shahanaj", "shahanaz", "sana", "shana", "shahnazz",
        "सहनाज", "सहनाज़", "शहनाज", "शहनाज़", "सहनज", "सहानाज़", "शाहनाज़"
    )

    private val prefixWords = listOf("hey", "ok", "okay", "hello", "hi", "suno", "arre", "bolo", "ji", "aey", "सुनो", "अरे", "बोलो", "नमस्ते")

    // Precompiled regex for stripping
    private val stripPrefixRegex = Regex(
        pattern = "(?i)^(hey|ok|okay|hello|hi|suno|arre|bolo|ji|aey|सुनो|अरे|बोलो|नमस्ते)\\s+(sahnaj|sahnaz|sanaj|sanaz|shahnaz|shahnaaj|sahnaaj|sahnaji|shahnaj|shehnaz|sahnaaz|sehnaaz|sehnaz|sahanaj|sahanaz|shahanaj|shahanaz|sana|shana|सहनाज|सहनाज़|शहनाज|शहनाज़|सहनज|सहानाज़|शाहनाज़)[,.]?\\s*",
        options = setOf(RegexOption.IGNORE_CASE)
    )
    private val stripDirectRegex = Regex(
        pattern = "(?i)^(sahnaj|sahnaz|sanaj|sanaz|shahnaz|shahnaaj|sahnaaj|sahnaji|shahnaj|shehnaz|sahnaaz|sehnaaz|sehnaz|sahanaj|sahanaz|shahanaj|shahanaz|sana|shana|सहनाज|सहनाज़|शहनाज|शहनाज़|सहनज|सहानाज़|शाहनाज़)[,.]?\\s*",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    fun setListeningForWakeWord(isListening: Boolean) {
        _isListeningForWakeWord.value = isListening
        Log.d(TAG, "[STAGE 2: WAKE-WORD] Listening state updated: isListening=$isListening")
    }

    /**
     * Ultra-fast on-device wake-word matcher running in sub-millisecond time.
     */
    fun matchesWakeWord(speech: String, wakeWord: String): Boolean {
        val cleanSpeech = speech.trim().lowercase()
        if (cleanSpeech.isBlank()) return false
        val cleanWakeWord = wakeWord.trim().lowercase()

        // 1. Direct prefix / contains match
        if (cleanWakeWord.isNotBlank() && (cleanSpeech.startsWith(cleanWakeWord) || cleanSpeech.contains(cleanWakeWord))) {
            Log.d(TAG, "[STAGE 2: WAKE-WORD] Fast direct match for '$wakeWord' in '$speech'")
            return true
        }

        // 2. Tokenized word check against O(1) set
        val words = cleanSpeech.split("\\s+".toRegex())
        for (i in words.indices) {
            val word = words[i].replace("[^a-zA-Z0-9]".toRegex(), "")
            if (word.isBlank()) continue

            if (word == cleanWakeWord || defaultVariants.contains(word)) {
                Log.d(TAG, "[STAGE 2: WAKE-WORD] Token match '$word' in '$speech'")
                return true
            }

            // Check 2-word combinations e.g. "hey sahnaj"
            if (i < words.size - 1) {
                val nextWord = words[i + 1].replace("[^a-zA-Z0-9]".toRegex(), "")
                if (prefixWords.contains(word) && (nextWord == cleanWakeWord || defaultVariants.contains(nextWord))) {
                    Log.d(TAG, "[STAGE 2: WAKE-WORD] 2-Token match '$word $nextWord' in '$speech'")
                    return true
                }
            }
        }

        // 3. Fallback substring scan for variants
        for (variant in defaultVariants) {
            if (cleanSpeech.contains(variant)) {
                Log.d(TAG, "[STAGE 2: WAKE-WORD] Substring variant match '$variant' in '$speech'")
                return true
            }
        }

        return false
    }

    fun stripWakeWord(speech: String, wakeWord: String): String {
        var cleanSpeech = speech.trim()
        val customClean = wakeWord.trim().lowercase()

        if (customClean.isNotBlank() && customClean != "sahnaj") {
            for (p in prefixWords) {
                val customPattern = "(?i)^$p\\s+$customClean[,.]?\\s*"
                cleanSpeech = cleanSpeech.replace(Regex(customPattern), "").trim()
            }
            val customDirect = "(?i)^$customClean[,.]?\\s*"
            cleanSpeech = cleanSpeech.replace(Regex(customDirect), "").trim()
        }

        cleanSpeech = cleanSpeech.replace(stripPrefixRegex, "").trim()
        cleanSpeech = cleanSpeech.replace(stripDirectRegex, "").trim()

        return cleanSpeech
    }

    companion object {
        private const val TAG = "SAHNAJ_VOICE"
    }
}


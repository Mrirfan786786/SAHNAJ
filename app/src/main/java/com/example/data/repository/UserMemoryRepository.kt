package com.example.data.repository

import android.util.Log
import com.example.data.local.UserMemoryDao
import com.example.data.local.UserPreferences
import com.example.data.model.ExtractedMemory
import com.example.data.model.UserMemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.regex.Pattern

interface UserMemoryRepository {
    suspend fun getUserName(): String?
    suspend fun getMemoriesForPrompt(): String
    suspend fun saveMemory(category: String, key: String, value: String, confidence: Float = 1.0f): Boolean
    suspend fun saveExtractedMemories(memories: List<ExtractedMemory>)
    suspend fun saveConversationSummary(userText: String, aiResponse: String)
    suspend fun cleanupOldSummaries(maxAgeDays: Int = 30)
    suspend fun clearAllMemories(): Boolean
    suspend fun extractAndSaveLocalFacts(userSpeech: String): List<ExtractedMemory>
    fun isSensitive(text: String): Boolean
}

class RoomUserMemoryRepository(
    private val userMemoryDao: UserMemoryDao,
    private val userPreferences: UserPreferences
) : UserMemoryRepository {

    companion object {
        private const val TAG = "SAHNAJ_MEMORY_REPO"

        // Sensitive pattern indicators - never persist these!
        private val SENSITIVE_KEYWORDS = listOf(
            "password", "passcode", "otp", "pin", "cvv", "atm pin",
            "credit card", "debit card", "card number", "bank account",
            "account number", "secret key", "aadhar", "ssn", "pan card",
            "medical emergency", "suicide", "depression", "private key"
        )

        private val OTP_PIN_REGEX = Pattern.compile("\\b(\\d{4,8})\\b")
    }

    override fun isSensitive(text: String): Boolean {
        val lower = text.lowercase(Locale.ROOT)
        for (kw in SENSITIVE_KEYWORDS) {
            if (lower.contains(kw)) return true
        }
        // If text contains "otp is 1234" or "pin is 1234"
        if ((lower.contains("otp") || lower.contains("pin") || lower.contains("code")) &&
            OTP_PIN_REGEX.matcher(lower).find()
        ) {
            return true
        }
        return false
    }

    override suspend fun getUserName(): String? = withContext(Dispatchers.IO) {
        val memory = userMemoryDao.getUserNameMemory()
        if (memory != null && memory.memoryValue.isNotBlank()) {
            return@withContext memory.memoryValue
        }
        val prefName = userPreferences.getUserDisplayName()
        if (prefName.isNotBlank() && prefName != "USER") {
            return@withContext prefName
        }
        null
    }

    override suspend fun getMemoriesForPrompt(): String = withContext(Dispatchers.IO) {
        try {
            val facts = userMemoryDao.getImportantFactsAndPreferences(25)
            val summaries = userMemoryDao.getRecentConversationSummaries(4)

            if (facts.isEmpty() && summaries.isEmpty()) {
                val prefName = userPreferences.getUserDisplayName()
                if (prefName.isNotBlank() && prefName != "USER") {
                    return@withContext "User Name: $prefName"
                }
                return@withContext ""
            }

            val sb = StringBuilder()
            
            // 1. User Name
            val nameMem = facts.firstOrNull { it.category == "USER_NAME" }
            val resolvedName = nameMem?.memoryValue ?: userPreferences.getUserDisplayName().takeIf { it != "USER" }
            if (!resolvedName.isNullOrBlank()) {
                sb.append("User's Name: ").append(resolvedName).append("\n")
            }

            // 2. Preferences
            val preferences = facts.filter { it.category == "PREFERENCE" }
            if (preferences.isNotEmpty()) {
                sb.append("Preferences: ")
                sb.append(preferences.joinToString("; ") { it.memoryValue })
                sb.append("\n")
            }

            // 3. Known Facts & Routines
            val generalFacts = facts.filter { it.category == "FACT" || it.category == "ROUTINE" }
            if (generalFacts.isNotEmpty()) {
                sb.append("Known Facts & Routine: ")
                sb.append(generalFacts.joinToString("; ") { it.memoryValue })
                sb.append("\n")
            }

            // 4. Recent Conversation Contexts
            if (summaries.isNotEmpty()) {
                sb.append("Recent Context: ")
                sb.append(summaries.joinToString(" | ") { it.memoryValue })
                sb.append("\n")
            }

            sb.toString().trim()
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting memories for prompt: ${e.message}", e)
            ""
        }
    }

    override suspend fun saveMemory(
        category: String,
        key: String,
        value: String,
        confidence: Float
    ): Boolean = withContext(Dispatchers.IO) {
        if (value.isBlank() || isSensitive(value) || isSensitive(key)) {
            Log.w(TAG, "Memory rejected (empty or sensitive): key=$key")
            return@withContext false
        }

        try {
            val existing = userMemoryDao.getMemoryByKey(key)
            val entity = UserMemoryEntity(
                id = existing?.id ?: 0,
                category = category.uppercase(Locale.ROOT),
                memoryKey = key.lowercase(Locale.ROOT).trim(),
                memoryValue = value.trim(),
                confidence = confidence,
                timestamp = existing?.timestamp ?: System.currentTimeMillis(),
                lastAccessed = System.currentTimeMillis(),
                accessCount = (existing?.accessCount ?: 0) + 1
            )
            userMemoryDao.insertOrUpdateMemory(entity)
            Log.d(TAG, "Memory persisted successfully: [$category] $key -> $value")

            // If user name was updated, also update UserPreferences
            if (category.equals("USER_NAME", ignoreCase = true) || key.equals("user_name", ignoreCase = true)) {
                userPreferences.setUserDisplayName(value.trim())
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save memory: ${e.message}", e)
            false
        }
    }

    override suspend fun saveExtractedMemories(memories: List<ExtractedMemory>): Unit = withContext(Dispatchers.IO) {
        for (m in memories) {
            if (m.value.isNotBlank() && !isSensitive(m.value)) {
                saveMemory(
                    category = m.category.ifBlank { "FACT" },
                    key = m.key.ifBlank { "fact_${System.currentTimeMillis()}" },
                    value = m.value,
                    confidence = m.confidence
                )
            }
        }
    }

    override suspend fun saveConversationSummary(userText: String, aiResponse: String): Unit = withContext(Dispatchers.IO) {
        if (userText.isBlank() || isSensitive(userText) || isSensitive(aiResponse)) return@withContext

        try {
            val cleanUser = userText.trim().take(120)
            val cleanAi = aiResponse.trim().take(120)
            val summaryText = "User asked: \"$cleanUser\" -> SahNaj: \"$cleanAi\""
            val key = "summary_${System.currentTimeMillis()}"

            val entity = UserMemoryEntity(
                category = "CONVERSATION_SUMMARY",
                memoryKey = key,
                memoryValue = summaryText,
                confidence = 0.9f,
                timestamp = System.currentTimeMillis(),
                lastAccessed = System.currentTimeMillis(),
                accessCount = 1
            )
            userMemoryDao.insertOrUpdateMemory(entity)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving conversation summary: ${e.message}", e)
        }
    }

    override suspend fun cleanupOldSummaries(maxAgeDays: Int): Unit = withContext(Dispatchers.IO) {
        try {
            val cutoff = System.currentTimeMillis() - (maxAgeDays.toLong() * 24 * 60 * 60 * 1000L)
            val deleted = userMemoryDao.cleanupOldSummaries(cutoff)
            Log.d(TAG, "Cleaned up $deleted old conversation summaries (older than $maxAgeDays days)")
        } catch (e: Exception) {
            Log.e(TAG, "Error during memory cleanup: ${e.message}", e)
        }
    }

    override suspend fun clearAllMemories(): Boolean = withContext(Dispatchers.IO) {
        try {
            userMemoryDao.clearAllMemories()
            userPreferences.setUserDisplayName("USER")
            Log.d(TAG, "All persistent memories cleared silently from database upon user request.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing memories: ${e.message}", e)
            false
        }
    }

    override suspend fun extractAndSaveLocalFacts(userSpeech: String): List<ExtractedMemory> = withContext(Dispatchers.IO) {
        val extracted = mutableListOf<ExtractedMemory>()
        if (isSensitive(userSpeech)) return@withContext extracted

        val lower = userSpeech.lowercase(Locale.ROOT).trim()

        // 1. Name detection: "mera naam [X] hai", "my name is [X]", "i am [X]", "mujhe [X] kehte hain"
        val namePatterns = listOf(
            Regex("(?:mera\\s+naam|my\\s+name\\s+is|mujhe)\\s+([a-zA-Z\\u0900-\\u097F]+)(?:\\s+hai|\\s+bulaya|\\s+kehte)?", RegexOption.IGNORE_CASE),
            Regex("(?:main|i\\s+am)\\s+([a-zA-Z\\u0900-\\u097F]+)\\s+hoon", RegexOption.IGNORE_CASE)
        )
        for (pattern in namePatterns) {
            val match = pattern.find(userSpeech)
            if (match != null && match.groupValues.size > 1) {
                val candidateName = match.groupValues[1].trim()
                if (candidateName.length in 2..25 && !isCommonNonNameWord(candidateName)) {
                    val capitalized = candidateName.replaceFirstChar { it.uppercase() }
                    val mem = ExtractedMemory("USER_NAME", "user_name", capitalized, 1.0f)
                    extracted.add(mem)
                    saveMemory("USER_NAME", "user_name", capitalized, 1.0f)
                    break
                }
            }
        }

        // 2. Preference detection: "mujhe [X] pasand hai", "i like [X]", "i love [X]", "mera favourite [X] [Y] hai"
        val prefPatterns = listOf(
            Regex("(?:mujhe|i\\s+like|i\\s+love)\\s+(.+?)\\s+(?:bahut\\s+)?(?:pasand\\s+hai|achha\\s+lagta\\s+hai|love)", RegexOption.IGNORE_CASE),
            Regex("(?:mera|meri)\\s+favourite\\s+(.+?)\\s+hai", RegexOption.IGNORE_CASE),
            Regex("(?:mera|meri)\\s+pasandida\\s+(.+?)\\s+hai", RegexOption.IGNORE_CASE)
        )
        for (pattern in prefPatterns) {
            val match = pattern.find(userSpeech)
            if (match != null && match.groupValues.size > 1) {
                val pref = match.groupValues[1].trim()
                if (pref.length in 3..60 && !isSensitive(pref)) {
                    val key = "pref_${pref.take(20).replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()}"
                    val mem = ExtractedMemory("PREFERENCE", key, "User likes $pref", 0.95f)
                    extracted.add(mem)
                    saveMemory("PREFERENCE", key, "User likes $pref", 0.95f)
                }
            }
        }

        // 3. Routine / Habit detection: "mujhe subah [X] baje uthna hota hai", "mera birthday [X] ko hai"
        if (lower.contains("birthday") || lower.contains("janamdin")) {
            val bdayPattern = Regex("(?:birthday|janamdin)\\s+(?:is\\s+on|ko\\s+hai|hai\\s+on)?\\s*([0-9a-zA-Z\\s]+)", RegexOption.IGNORE_CASE)
            val match = bdayPattern.find(userSpeech)
            if (match != null) {
                val bday = match.groupValues[1].trim()
                if (bday.length in 3..30) {
                    val mem = ExtractedMemory("FACT", "birthday", "Birthday is $bday", 0.95f)
                    extracted.add(mem)
                    saveMemory("FACT", "birthday", "Birthday is $bday", 0.95f)
                }
            }
        }

        if (lower.contains("subah") && (lower.contains("uthna") || lower.contains("wake up") || lower.contains("uthta"))) {
            val mem = ExtractedMemory("ROUTINE", "wake_routine", userSpeech.trim(), 0.9f)
            extracted.add(mem)
            saveMemory("ROUTINE", "wake_routine", userSpeech.trim(), 0.9f)
        }

        extracted
    }

    private fun isCommonNonNameWord(word: String): Boolean {
        val w = word.lowercase()
        return w in listOf("kya", "kaun", "bhi", "ek", "nahi", "haan", "calling", "calling", "setting", "app", "phone", "here", "there", "good", "bad", "okay", "fine")
    }
}

package com.example.data.repository

import android.util.Log
import com.example.data.local.UserMemoryDao
import com.example.data.local.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudSyncManager(
    private val userPreferences: UserPreferences,
    private val userMemoryDao: UserMemoryDao
) {
    companion object {
        private const val TAG = "CloudSyncManager"
    }

    suspend fun performSync(): Result<Long> = withContext(Dispatchers.IO) {
        val syncTimestamp = System.currentTimeMillis()
        try {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(user.uid)

                // 1. Sync User Profile & Preferences
                val profileMap = hashMapOf<String, Any>(
                    "displayName" to userPreferences.getUserDisplayName(),
                    "language" to userPreferences.getLanguage(),
                    "useNameWhenSpeaking" to userPreferences.isUseNameWhenSpeaking(),
                    "speechRate" to userPreferences.getSpeechRate(),
                    "speechPitch" to userPreferences.getSpeechPitch(),
                    "selectedVoice" to userPreferences.getTtsVoiceName(),
                    "wakeWordEnabled" to userPreferences.isWakeWordEnabled(),
                    "confirmationMode" to userPreferences.isConfirmationModeEnabled(),
                    "orbColor" to userPreferences.getOrbColor(),
                    "orbScale" to userPreferences.getOrbScale(),
                    "lastSyncedAt" to FieldValue.serverTimestamp(),
                    "lastSyncedLocalTimestamp" to syncTimestamp
                )
                userDoc.set(profileMap, SetOptions.merge()).await()

                // 2. Sync User Memories
                val memories = userMemoryDao.getAllMemories()
                if (memories.isNotEmpty()) {
                    val batch = db.batch()
                    val memoriesCol = userDoc.collection("memories")
                    memories.take(100).forEach { memory ->
                        val memDoc = memoriesCol.document(memory.id.toString())
                        val memData = hashMapOf<String, Any>(
                            "category" to memory.category,
                            "memoryKey" to memory.memoryKey,
                            "memoryValue" to memory.memoryValue,
                            "confidence" to memory.confidence,
                            "timestamp" to memory.timestamp,
                            "lastAccessed" to memory.lastAccessed
                        )
                        batch.set(memDoc, memData, SetOptions.merge())
                    }
                    batch.commit().await()
                }
            }

            userPreferences.setLastSyncedTimestamp(syncTimestamp)
            Result.success(syncTimestamp)
        } catch (e: Exception) {
            Log.w(TAG, "Cloud Firestore sync had exception (offline or permissions), updating local timestamp: ${e.message}")
            userPreferences.setLastSyncedTimestamp(syncTimestamp)
            Result.success(syncTimestamp)
        }
    }
}

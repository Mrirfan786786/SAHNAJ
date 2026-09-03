package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_memory",
    indices = [Index(value = ["category", "memoryKey"], unique = true)]
)
data class UserMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "USER_NAME", "PREFERENCE", "FACT", "ROUTINE", "CONVERSATION_SUMMARY"
    val memoryKey: String, // Unique identifier e.g. "user_name", "fav_sport", "routine_wakeup"
    val memoryValue: String, // The remembered statement/fact/summary
    val confidence: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis(),
    val accessCount: Int = 1
)

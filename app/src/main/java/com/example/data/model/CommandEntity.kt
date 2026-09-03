package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "command_history")
data class CommandEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val commandText: String,
    val actionType: String,
    val target: String,
    val status: String,
    val spokenResponse: String,
    val isGemini: Boolean = false
)

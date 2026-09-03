package com.example.data.model

data class ExtractedMemory(
    val category: String, // "USER_NAME", "PREFERENCE", "FACT", "ROUTINE", "CONVERSATION_SUMMARY"
    val key: String,
    val value: String,
    val confidence: Float = 1.0f
)

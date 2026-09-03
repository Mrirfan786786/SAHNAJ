package com.example.data.model

data class VoiceProfile(
    val isEnrolled: Boolean = false,
    val enrolledDate: String = "",
    val sampleCount: Int = 0,
    val averageRmsDb: Float = 0f,
    val averageDurationMs: Long = 0L,
    val acousticSignature: List<Float> = emptyList(),
    val confidenceThreshold: Float = 0.50f
)

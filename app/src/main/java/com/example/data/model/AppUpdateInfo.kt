package com.example.data.model

data class AppUpdateInfo(
    val versionCode: Long = 16L,
    val versionName: String = "1.16",
    val releaseNotes: String = "• AI Core latency optimizations\n• Voice and Profile customization improvements\n• Bug fixes and battery efficiency updates",
    val downloadUrl: String = "https://github.com/aistudio/sahnaj-ai/releases",
    val releaseDate: String = "August 2026",
    val isMandatory: Boolean = false
)

package com.example.data.model

import java.util.UUID

data class GeneratedImageItem(
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val providerId: String,
    val imageUrl: String,
    val localUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val aspectRatio: String = "1:1",
    val style: String = "Cinematic Cyberpunk"
)

data class GeneratedVideoItem(
    val id: String = UUID.randomUUID().toString(),
    val prompt: String,
    val providerId: String,
    val videoUrl: String,
    val localUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSecs: Int = 5,
    val motionStyle: String = "Cinematic Dynamic"
)

sealed interface GenerationState<out T> {
    data object Idle : GenerationState<Nothing>
    data class Generating(val progress: Float = 0.5f, val stageText: String = "Synthesizing AI Neural Latents...") : GenerationState<Nothing>
    data class Success<T>(val data: T) : GenerationState<T>
    data class Error(val errorMessage: String) : GenerationState<Nothing>
}

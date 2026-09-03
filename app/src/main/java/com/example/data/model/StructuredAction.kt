package com.example.data.model

data class StructuredAction(
    val action: ActionType,
    val target: String = "",
    val value: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val spokenResponse: String = "",
    val rawPrompt: String = "",
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val extractedMemories: List<ExtractedMemory> = emptyList(),
    val conversationSummary: String? = null
)

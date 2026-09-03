package com.example.data.model

import java.util.UUID

data class ElevenLabsVoice(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val previewTrait: String,
    val gender: String,
    val avatarEmoji: String = "🎙️",
    val isCloned: Boolean = false
)

data class ElevenLabsModel(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val recommendedFor: String
)

data class GeneratedVoiceItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val voiceName: String,
    val voiceId: String,
    val modelId: String,
    val audioFilePath: String,
    val durationMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val stability: Float = 0.5f,
    val similarityBoost: Float = 0.75f,
    val fileSizeBytes: Long = 0L
)

sealed class VoiceStudioState {
    data object Idle : VoiceStudioState()
    data class Generating(val progress: Float, val stage: String) : VoiceStudioState()
    data class Success(val item: GeneratedVoiceItem) : VoiceStudioState()
    data class Error(val message: String) : VoiceStudioState()
}

object VoiceStudioPresets {

    val PRELOADED_VOICES: List<ElevenLabsVoice> = VoiceStudioCatalog.ALL_50_VOICES.map { profile ->
        ElevenLabsVoice(
            id = profile.elevenLabsVoiceId,
            name = profile.name,
            category = profile.category,
            description = "${profile.tagline} • Optimizations for ${profile.languageOptimization}",
            previewTrait = profile.tagline,
            gender = profile.gender,
            avatarEmoji = profile.avatarEmoji
        )
    }

    val PRELOADED_MODELS = listOf(
        ElevenLabsModel(
            id = "eleven_multilingual_v2",
            name = "Multilingual v2 (Flagship)",
            badge = "BEST FOR HINDI / URDU",
            description = "Cutting-edge multilingual neural synthesis with 29+ languages and deep emotional cadence.",
            recommendedFor = "Hindi, Urdu, Hinglish, Arabic, English & Multi-lingual Dubbing"
        ),
        ElevenLabsModel(
            id = "eleven_turbo_v2_5",
            name = "Turbo v2.5 (Low Latency)",
            badge = "ULTRA FAST",
            description = "Near instant text-to-speech with high quality multilingual capabilities.",
            recommendedFor = "Real-time Dialogue & Quick Audio Generation"
        ),
        ElevenLabsModel(
            id = "eleven_turbo_v2",
            name = "Turbo v2",
            badge = "BALANCED",
            description = "Fast generation model optimized for high throughput voice synthesis.",
            recommendedFor = "General Audio & Fast Narration"
        )
    )

    val SAMPLE_SCRIPTS = listOf(
        SampleScript(
            label = "🇮🇳 Hindi Dialogue",
            category = "Hindi",
            text = "नमस्ते! मैं शहनाज़ एआई हूँ। आपके सभी डिजिटल कार्यों और आवाज़ से जुड़े प्रोजेक्ट्स के लिए मैं हमेशा तैयार हूँ। आज आप क्या रचना चाहते हैं?"
        ),
        SampleScript(
            label = "🇵🇰 Urdu Poetry",
            category = "Urdu",
            text = "خودی کو کر بلند اتنا کہ ہر تقدیر سے پہلے، خدا بندے سے خود پوچھے بتا تیری رضا کیا ہے۔ شہناز اے آئی کے وائس سٹوڈیو میں آپ کا استقبال ہے۔"
        ),
        SampleScript(
            label = "🤖 Cyber Matrix",
            category = "Sci-Fi",
            text = "SAHNAJ neural core operational. Neural audio synthesis layer linked at forty-four kilohertz. Ready to broadcast high-fidelity voice stream across cyber matrix."
        ),
        SampleScript(
            label = "🎙️ Podcast Intro",
            category = "English",
            text = "Welcome back to the future of AI. In today's episode, we explore how next-generation voice synthesis is transforming storytelling and digital communication."
        )
    )
}

data class SampleScript(
    val label: String,
    val category: String,
    val text: String
)

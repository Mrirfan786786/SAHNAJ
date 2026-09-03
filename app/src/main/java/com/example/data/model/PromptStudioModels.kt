package com.example.data.model

import java.util.UUID

enum class PromptStyle(
    val title: String,
    val hindiTitle: String,
    val emoji: String,
    val description: String,
    val visualKeywords: String
) {
    PIXAR_3D(
        title = "3D Pixar Animation",
        hindiTitle = "3D पिक्सर / डिज़्नी एनिमेशन",
        emoji = "🎨",
        description = "Vibrant 3D animated character style, Disney/Pixar aesthetic, smooth subsurface scattering, friendly expressive lighting.",
        visualKeywords = "3D Pixar animation style, Disney character render, soft studio lighting, subsurface scattering, expressive big eyes, stylized smooth textures, Octane 3D render, cute vibrant proportions, raytraced clay and cloth shaders"
    ),

    UNREAL_8K(
        title = "Hyper-Realistic 8K Unreal Engine",
        hindiTitle = "8K अनरियल इंजन रियलिस्टिक",
        emoji = "⚡",
        description = "Unreal Engine 5 render, cinematic ray tracing, 8K octane render, photorealistic textures, hyper-detailed.",
        visualKeywords = "Hyper-realistic, Unreal Engine 5 render, Octane render 8k, cinematic ray-tracing, photorealistic volumetric lighting, intricate skin pore textures, dynamic depth of field, anamorphic lens flare, masterwork photography"
    ),

    CYBERPUNK_NEON(
        title = "Cyberpunk Neon",
        hindiTitle = "साइबरपंक नियॉन एस्थेटिक",
        emoji = "🌆",
        description = "Neon-drenched cyberpunk aesthetic, volumetric fog, holographic glow, high-tech futuristic dystopian vibes.",
        visualKeywords = "Cyberpunk aesthetic, neon magenta and cyan rim lighting, futuristic HUD elements, holographic reflections, wet rainy asphalt reflections, techwear apparel, hyper-detailed mechanical details, dark dystopian moody atmosphere"
    ),

    MINIMALIST_LOGO(
        title = "Minimalist Vector Logo",
        hindiTitle = "मिनिमलिस्ट वेक्टर लोगो एवं आइकन",
        emoji = "💎",
        description = "Clean minimalist vector art, flat logo design, geometric lines, modern brand identity, SVG aesthetic.",
        visualKeywords = "Minimalist modern vector icon, flat graphic design, clean bold silhouette, Swiss design style, elegant geometric typography, high contrast, smooth gradients, vector logo suitable for app store or branding, white or clean dark background"
    ),

    YOUTUBE_THUMBNAIL(
        title = "YouTube High-CTR Thumbnail",
        hindiTitle = "यूट्यूब हाई-सीटीआर थंबनेल",
        emoji = "🔥",
        description = "High CTR YouTube thumbnail style, dramatic rim lighting, expressive face, high contrast, vibrant saturated colors, bold focal point.",
        visualKeywords = "High CTR YouTube thumbnail art, ultra-dramatic colored rim lighting, ultra-expressive shocking facial reaction, high saturation, sharp focal contrast, bold 3D subject pop-out, clean background separation, click-magnet lighting, professional studio key light"
    )
}

enum class PromptAspectRatio(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val ratioValue: String,
    val midjourneyFlag: String
) {
    AR_16_9(
        title = "16:9",
        subtitle = "YouTube & Desktop",
        emoji = "🖥️",
        ratioValue = "16:9",
        midjourneyFlag = "--ar 16:9"
    ),

    AR_9_16(
        title = "9:16",
        subtitle = "Reels, Shorts & Stories",
        emoji = "📱",
        ratioValue = "9:16",
        midjourneyFlag = "--ar 9:16"
    ),

    AR_1_1(
        title = "1:1",
        subtitle = "Profile Avatar & Square",
        emoji = "👤",
        ratioValue = "1:1",
        midjourneyFlag = "--ar 1:1"
    )
}

data class GeneratedPromptResult(
    val id: String = UUID.randomUUID().toString(),
    val subject: String,
    val style: PromptStyle,
    val aspectRatio: PromptAspectRatio,
    val fluxPrompt: String,
    val midjourneyPrompt: String,
    val sdPrompt: String,
    val masterPrompt: String,
    val negativePrompt: String,
    val lightingAndCameraNotes: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L
)

sealed class PromptStudioState {
    data object Idle : PromptStudioState()
    data class Generating(val stage: String) : PromptStudioState()
    data class Success(val result: GeneratedPromptResult) : PromptStudioState()
    data class Error(val message: String) : PromptStudioState()
}

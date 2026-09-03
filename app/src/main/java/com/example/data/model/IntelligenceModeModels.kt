package com.example.data.model

enum class SahnajOperatingMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val hindiSubtitle: String,
    val badge: String,
    val description: String,
    val ttsEnabled: Boolean,
    val cloudAllowed: Boolean
) {
    JARVIS(
        id = "JARVIS",
        title = "JARVIS Autonomous Mode",
        subtitle = "Proactive alerts, system diagnostics, and conversational voice responses.",
        hindiSubtitle = "स्वायत्त जार्विस मोड • सक्रिय अलर्ट और प्राकृतिक आवाज़ प्रतिक्रियाएँ",
        badge = "AUTONOMOUS CORE",
        description = "Full conversational intelligence with background alerts, deep voice synthesis, proactive reminders, and autonomous multi-step execution.",
        ttsEnabled = true,
        cloudAllowed = true
    ),
    TECHNICIAN(
        id = "TECHNICIAN",
        title = "Technician Mode",
        subtitle = "Specialized for automotive/hardware diagnostics, mechanics guidance, and direct technical answers.",
        hindiSubtitle = "तकनीशियन मोड • हार्डवेयर, ऑटोमोबाइल व मशीनरी डायग्नोस्टिक्स",
        badge = "HARDWARE & AUTO",
        description = "Direct, crisp, engineering-grade diagnostic assistance for automotive troubleshooting, circuit schematics, tool identification, and mechanics steps.",
        ttsEnabled = true,
        cloudAllowed = true
    ),
    STEALTH(
        id = "STEALTH",
        title = "Stealth Mode",
        subtitle = "Text-only responses, disables TTS voice output for quiet environments.",
        hindiSubtitle = "साइलेंट स्टील्थ मोड • केवल टेक्स्ट रिस्पांस, आवाज़ बंद",
        badge = "SILENT PROTOCOL",
        description = "Disables all voice synthesis (TTS) and audio chimes. SAHNAJ processes everything visually and communicates purely through text and cards on screen.",
        ttsEnabled = false,
        cloudAllowed = true
    ),
    OFFLINE_CORE(
        id = "OFFLINE_CORE",
        title = "Offline Core Only",
        subtitle = "Force local Android TTS and local rules engine, disables cloud API calls.",
        hindiSubtitle = "ऑफ़लाइन कोर मोड • 100% ऑन-डिवाइस, बिना इंटरनेट",
        badge = "ZERO-CLOUD LOCAL",
        description = "Strictly disables external cloud network calls. All commands are processed through on-device rule engines, local contacts/apps matching, and native offline TTS.",
        ttsEnabled = true,
        cloudAllowed = false
    );

    companion object {
        fun fromId(id: String?): SahnajOperatingMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: JARVIS
        }
    }
}

enum class ResponseLatencyOptimization(
    val level: Int,
    val title: String,
    val subtitle: String,
    val badge: String,
    val targetLatency: String,
    val description: String
) {
    AGGRESSIVE(
        level = 0,
        title = "Aggressive",
        subtitle = "Low Latency & High Speed",
        badge = "⚡ TURBO",
        targetLatency = "~250ms - 450ms",
        description = "Short, direct, concise responses with ultra-fast streaming tokens. Perfect for rapid hands-free device actions."
    ),
    BALANCED(
        level = 1,
        title = "Balanced",
        subtitle = "Recommended Default",
        badge = "✨ OPTIMAL",
        targetLatency = "~600ms - 900ms",
        description = "Optimal balance between conversational nuance, reasoning depth, voice naturalness, and prompt latency."
    ),
    DEEP_THINKING(
        level = 2,
        title = "Deep Thinking",
        subtitle = "Maximum Reasoning Depth",
        badge = "🧠 DEEP COGNITION",
        targetLatency = "~1.2s - 2.0s",
        description = "Comprehensive multi-step thinking, detailed technical explanations, structured tables, and exhaustive diagnostics."
    );

    companion object {
        fun fromLevel(level: Int): ResponseLatencyOptimization {
            return entries.firstOrNull { it.level == level } ?: BALANCED
        }
    }
}

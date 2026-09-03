package com.example.data.model

enum class ProviderCategory(val title: String, val emoji: String) {
    LLM("LLM Engines", "🧠"),
    VOICE_TTS("Voice Studio & Dubbing", "🎙️"),
    IMAGE_GEN("Image AI", "🎨"),
    VIDEO_GEN("Video AI", "🎬")
}

data class AiProvider(
    val id: String,
    val name: String,
    val storageKey: String,
    val localStorageKey: String,
    val hindi: String,
    val defaultModels: String,
    val useCase: String,
    val testEndpoint: String,
    val keyPlaceholder: String = "Paste key here...",
    val badge: String = "PRO LLM",
    val accentEmoji: String = "⚡",
    val category: ProviderCategory = ProviderCategory.LLM
)

object AiProvidersConfig {
    val VOICE_PROVIDERS = listOf(
        AiProvider(
            id = "elevenlabs",
            name = "ElevenLabs Voice AI",
            storageKey = "ELEVENLABS_API_KEY",
            localStorageKey = "sahnaj_elevenlabs_key",
            hindi = "इलेवनलैब्स वॉयस क्लोनिंग एवं डबिंग स्टूडियो",
            defaultModels = "eleven_multilingual_v2, eleven_turbo_v2_5",
            useCase = "Ultra-Realistic Hindi, Urdu & Multilingual Dubbing & Voice Cloning",
            testEndpoint = "https://api.elevenlabs.io/v1/voices",
            keyPlaceholder = "sk_... or xi-api-key",
            badge = "VOICE STUDIO PRO",
            accentEmoji = "🎙️",
            category = ProviderCategory.VOICE_TTS
        )
    )

    val LLM_PROVIDERS = listOf(
        AiProvider(
            id = "gemini",
            name = "Google Gemini",
            storageKey = "GEMINI_API_KEY",
            localStorageKey = "sahnaj_gemini_key",
            hindi = "गूगल जेमिनी एआई",
            defaultModels = "gemini-3.6-flash, gemini-2.5-pro",
            useCase = "Chat, Multimodal & System Automation",
            testEndpoint = "https://generativelanguage.googleapis.com",
            keyPlaceholder = "AIzaSy...",
            badge = "PRIMARY ENGINE",
            accentEmoji = "✨",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "openai",
            name = "OpenAI GPT-4o",
            storageKey = "OPENAI_API_KEY",
            localStorageKey = "sahnaj_openai_key",
            hindi = "ओपनएआई जीपीटी-4o",
            defaultModels = "gpt-4o",
            useCase = "Flagship Multimodal & Logic Reasoning",
            testEndpoint = "https://api.openai.com/v1/models",
            keyPlaceholder = "sk-proj-...",
            badge = "FLAGSHIP LLM",
            accentEmoji = "🟢",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "openai_mini",
            name = "OpenAI GPT-4o Mini",
            storageKey = "OPENAI_MINI_API_KEY",
            localStorageKey = "sahnaj_openai_mini_key",
            hindi = "ओपनएआई जीपीटी-4o मिनी",
            defaultModels = "gpt-4o-mini",
            useCase = "Fast, Low-Cost Everyday Chat & Tasks",
            testEndpoint = "https://api.openai.com/v1/models",
            keyPlaceholder = "sk-proj-...",
            badge = "FAST LLM",
            accentEmoji = "⚡",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "openai_reasoning",
            name = "OpenAI o1 / o3 Reasoning",
            storageKey = "OPENAI_REASONING_API_KEY",
            localStorageKey = "sahnaj_openai_reasoning_key",
            hindi = "ओपनएआई o1 / o3 रीजनिंग",
            defaultModels = "o1, o1-preview, o3-mini",
            useCase = "Deep Thought, Complex Math & Logic",
            testEndpoint = "https://api.openai.com/v1/models",
            keyPlaceholder = "sk-proj-...",
            badge = "DEEP THINKER",
            accentEmoji = "🧠",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "claude_sonnet",
            name = "Claude 3.5 Sonnet",
            storageKey = "CLAUDE_SONNET_API_KEY",
            localStorageKey = "sahnaj_claude_key",
            hindi = "क्लॉड 3.5 सॉनेट",
            defaultModels = "claude-3-5-sonnet-latest",
            useCase = "Code Master & System Architecture",
            testEndpoint = "https://api.anthropic.com/v1/models",
            keyPlaceholder = "sk-ant-...",
            badge = "CODE MASTER",
            accentEmoji = "💻",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "claude_haiku",
            name = "Claude 3.5 Haiku",
            storageKey = "CLAUDE_HAIKU_API_KEY",
            localStorageKey = "sahnaj_claude_haiku_key",
            hindi = "क्लॉड 3.5 हाइकू",
            defaultModels = "claude-3-5-haiku-latest",
            useCase = "Ultra-Fast & Responsive Coding",
            testEndpoint = "https://api.anthropic.com/v1/models",
            keyPlaceholder = "sk-ant-...",
            badge = "SPEED & CODE",
            accentEmoji = "🚀",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "deepseek_v3",
            name = "DeepSeek V3",
            storageKey = "DEEPSEEK_API_KEY",
            localStorageKey = "sahnaj_deepseek_key",
            hindi = "डीपसीक वी3 जनरल चैट",
            defaultModels = "deepseek-chat",
            useCase = "Open Weights Frontier General Chat",
            testEndpoint = "https://api.deepseek.com/models",
            keyPlaceholder = "sk-...",
            badge = "GENERAL LLM",
            accentEmoji = "🔍",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "deepseek_r1",
            name = "DeepSeek R1",
            storageKey = "DEEPSEEK_R1_API_KEY",
            localStorageKey = "sahnaj_deepseek_r1_key",
            hindi = "डीपसीक आर1 रीजनिंग",
            defaultModels = "deepseek-reasoner",
            useCase = "Advanced Chain-of-Thought Reasoning",
            testEndpoint = "https://api.deepseek.com/models",
            keyPlaceholder = "sk-...",
            badge = "REASONING R1",
            accentEmoji = "🧩",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "groq_llama",
            name = "Meta Llama 3.3 70B (Groq)",
            storageKey = "GROQ_API_KEY",
            localStorageKey = "sahnaj_groq_key",
            hindi = "मेटा लामा 3.3 70B (ग्रॉक)",
            defaultModels = "llama-3.3-70b-versatile",
            useCase = "Ultra-Fast 500 T/s Open Inference",
            testEndpoint = "https://api.groq.com/openai/v1/models",
            keyPlaceholder = "gsk_...",
            badge = "LIGHTNING 500 T/s",
            accentEmoji = "⚡",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "mistral_large",
            name = "Mistral Large 2",
            storageKey = "MISTRAL_API_KEY",
            localStorageKey = "sahnaj_mistral_key",
            hindi = "मिस्ट्रल लार्ज 2",
            defaultModels = "mistral-large-latest",
            useCase = "Multilingual Frontier Reasoning",
            testEndpoint = "https://api.mistral.ai/v1/models",
            keyPlaceholder = "mistral_key...",
            badge = "FRONTIER LLM",
            accentEmoji = "🌪️",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "mistral_codestral",
            name = "Mistral Codestral",
            storageKey = "CODESTRAL_API_KEY",
            localStorageKey = "sahnaj_codestral_key",
            hindi = "मिस्ट्रल कोडस्ट्रल",
            defaultModels = "codestral-latest",
            useCase = "Specialized Code Completion & Debug",
            testEndpoint = "https://api.mistral.ai/v1/models",
            keyPlaceholder = "mistral_key...",
            badge = "DEV SPECIALIST",
            accentEmoji = "⌨️",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "perplexity_sonar",
            name = "Perplexity Sonar",
            storageKey = "PERPLEXITY_API_KEY",
            localStorageKey = "sahnaj_perplexity_key",
            hindi = "पर्प्लेक्सिटी सोनार सर्च",
            defaultModels = "sonar, sonar-pro, sonar-reasoning",
            useCase = "Live Grounded Web Search LLM",
            testEndpoint = "https://api.perplexity.ai/models",
            keyPlaceholder = "pplx-...",
            badge = "LIVE WEB SEARCH",
            accentEmoji = "🌐",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "cohere_command",
            name = "Cohere Command R+",
            storageKey = "COHERE_API_KEY",
            localStorageKey = "sahnaj_cohere_key",
            hindi = "कोहेरे कमांड आर+",
            defaultModels = "command-r-plus, command-r",
            useCase = "Enterprise RAG, Tool Use & Grounding",
            testEndpoint = "https://api.cohere.ai/v1/models",
            keyPlaceholder = "cohere_key...",
            badge = "ENTERPRISE RAG",
            accentEmoji = "🔷",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "qwen",
            name = "Qwen 2.5",
            storageKey = "QWEN_API_KEY",
            localStorageKey = "sahnaj_qwen_key",
            hindi = "क्वेन 2.5 मॉडल",
            defaultModels = "qwen-2.5-72b-instruct, qwen-2.5-coder",
            useCase = "Top Open Weights Multi-Task & Math",
            testEndpoint = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/models",
            keyPlaceholder = "sk-...",
            badge = "OPEN WEIGHTS",
            accentEmoji = "🏮",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "gemma_2",
            name = "Google Gemma 2",
            storageKey = "GEMMA_API_KEY",
            localStorageKey = "sahnaj_gemma_key",
            hindi = "गूगल जेम्मा 2",
            defaultModels = "gemma-2-27b-it, gemma-2-9b-it",
            useCase = "Lightweight Open Foundation Model",
            testEndpoint = "https://generativelanguage.googleapis.com",
            keyPlaceholder = "AIzaSy... / gsk_...",
            badge = "GOOGLE OPEN",
            accentEmoji = "💎",
            category = ProviderCategory.LLM
        ),
        AiProvider(
            id = "grok_2",
            name = "xAI Grok 2",
            storageKey = "GROK_API_KEY",
            localStorageKey = "sahnaj_grok_key",
            hindi = "ग्रोक 2 चैट",
            defaultModels = "grok-2, grok-2-mini",
            useCase = "Real-Time X Knowledge & Chat",
            testEndpoint = "https://api.x.ai/v1/models",
            keyPlaceholder = "xai-...",
            badge = "REAL-TIME X",
            accentEmoji = "✖️",
            category = ProviderCategory.LLM
        )
    )

    val IMAGE_PROVIDERS = listOf(
        AiProvider(
            id = "flux",
            name = "Pollinations / FLUX.1",
            storageKey = "FLUX_API_KEY",
            localStorageKey = "sahnaj_flux_key",
            hindi = "पोलिनेशन्स / फ्लक्स.1 इमेज",
            defaultModels = "flux, flux-realism, turbo",
            useCase = "Real-Time Fast FLUX Synthesis & Photorealism",
            testEndpoint = "https://image.pollinations.ai",
            keyPlaceholder = "Direct URL or Custom Key...",
            badge = "FLUX.1 ENGINE",
            accentEmoji = "✨",
            category = ProviderCategory.IMAGE_GEN
        ),
        AiProvider(
            id = "stability",
            name = "Stability AI / SD 3.5",
            storageKey = "STABILITY_API_KEY",
            localStorageKey = "sahnaj_stability_key",
            hindi = "स्टैबिलिटी एसडी 3.5 इमेज",
            defaultModels = "sd3.5-large, stable-image-ultra, core",
            useCase = "Photorealistic 4K Image Generation & Diffusion",
            testEndpoint = "https://api.stability.ai/v1/user/account",
            keyPlaceholder = "sk-...",
            badge = "SD 3.5 DIFFUSION",
            accentEmoji = "🎨",
            category = ProviderCategory.IMAGE_GEN
        ),
        AiProvider(
            id = "dalle",
            name = "OpenAI DALL-E 3",
            storageKey = "DALLE_API_KEY",
            localStorageKey = "sahnaj_dalle_key",
            hindi = "ओपनएआई डॉल-ई 3 इमेज",
            defaultModels = "dall-e-3, dall-e-2",
            useCase = "Creative HD Prompt Understanding & Art",
            testEndpoint = "https://api.openai.com/v1/models",
            keyPlaceholder = "sk-proj-...",
            badge = "DALL-E 3 HD",
            accentEmoji = "🖼️",
            category = ProviderCategory.IMAGE_GEN
        ),
        AiProvider(
            id = "universal_image",
            name = "Universal Image Key",
            storageKey = "IMAGE_API_KEY",
            localStorageKey = "sahnaj_image_key",
            hindi = "यूनिवर्सल इमेज जनरेशन कुंजी",
            defaultModels = "custom-flux-sd3",
            useCase = "Fallback Key For All Image Generation Calls",
            testEndpoint = "https://api.stability.ai/v1/user/account",
            keyPlaceholder = "sahnaj_image_key...",
            badge = "UNIVERSAL KEY",
            accentEmoji = "🌌",
            category = ProviderCategory.IMAGE_GEN
        )
    )

    val VIDEO_PROVIDERS = listOf(
        AiProvider(
            id = "runway",
            name = "Runway Gen-3 Alpha",
            storageKey = "RUNWAY_API_KEY",
            localStorageKey = "sahnaj_runway_key",
            hindi = "रनवे जेन-3 अल्फा वीडियो",
            defaultModels = "gen3a_turbo, gen-2",
            useCase = "Cinematic 4K Realistic Motion Video",
            testEndpoint = "https://api.dev.runwayml.com/v1/models",
            keyPlaceholder = "key_...",
            badge = "GEN-3 ALPHA",
            accentEmoji = "🎬",
            category = ProviderCategory.VIDEO_GEN
        ),
        AiProvider(
            id = "luma",
            name = "Luma Dream Machine",
            storageKey = "LUMA_API_KEY",
            localStorageKey = "sahnaj_luma_key",
            hindi = "लूमा ड्रीम मशीन वीडियो",
            defaultModels = "ray-1-6, dream-machine",
            useCase = "Physics-Accurate Dynamic Video",
            testEndpoint = "https://api.lumalabs.ai/dream-machine/v1/generations",
            keyPlaceholder = "luma-...",
            badge = "DREAM MACHINE",
            accentEmoji = "🎥",
            category = ProviderCategory.VIDEO_GEN
        ),
        AiProvider(
            id = "replicate_video",
            name = "Replicate Video (Minimax/CogVideo)",
            storageKey = "REPLICATE_API_KEY",
            localStorageKey = "sahnaj_replicate_key",
            hindi = "रेप्लिकेट वीडियो मॉडल",
            defaultModels = "minimax/video-01, cogvideox-5b",
            useCase = "Serverless Cloud High-Motion Video",
            testEndpoint = "https://api.replicate.com/v1/models",
            keyPlaceholder = "r8_...",
            badge = "CLOUD VIDEO",
            accentEmoji = "⚡",
            category = ProviderCategory.VIDEO_GEN
        ),
        AiProvider(
            id = "universal_video",
            name = "Universal Video Key",
            storageKey = "VIDEO_API_KEY",
            localStorageKey = "sahnaj_video_key",
            hindi = "यूनिवर्सल वीडियो जनरेशन कुंजी",
            defaultModels = "custom-video-engine",
            useCase = "Fallback Key For All Video Generation Calls",
            testEndpoint = "https://api.replicate.com/v1/models",
            keyPlaceholder = "sahnaj_video_key...",
            badge = "UNIVERSAL KEY",
            accentEmoji = "📹",
            category = ProviderCategory.VIDEO_GEN
        )
    )

    val ALL_PROVIDERS: List<AiProvider> = LLM_PROVIDERS + VOICE_PROVIDERS + IMAGE_PROVIDERS + VIDEO_PROVIDERS

    fun getProviderById(id: String): AiProvider? {
        return ALL_PROVIDERS.find { it.id.equals(id, ignoreCase = true) }
    }
}


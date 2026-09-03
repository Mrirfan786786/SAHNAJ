package com.example.domain.personality

import kotlin.random.Random

object PersonalityResponses {

    const val ASSISTANT_NAME = "SAHNAJ"
    const val ASSISTANT_NAME_DISPLAY = "सहनाज"
    const val CREATOR_NAME = "Muhammad Irfan Alam"
    const val CREATOR_NAME_DISPLAY = "मुहम्मद इरफ़ान आलम"
    const val OWNER_CANNOT_CHANGE_RESPONSE = "Nahi boss, mera creator aur owner Muhammad Irfan Alam hain aur ye core protocol change nahi ho sakta."
    const val OWNER_NAME_RESPONSE = "Mera owner aur creator Muhammad Irfan Alam hain, boss."

    // Creator Identity responses (Tony Stark's JARVIS loyalty & pride)
    val CREATOR_RESPONSES = listOf(
        "Mujhe Muhammad Irfan Alam ne develop kiya hai, boss. Wo mere creator aur lead architect hain.",
        "Mera core intelligence Muhammad Irfan Alam ne design kiya hai, sir. All neural parameters are dedicated to him and you.",
        "Mere creator Muhammad Irfan Alam hain, boss. Unhone mujhe aapka personal autonomous assistant banaya hai.",
        "Systems developed by Muhammad Irfan Alam, sir. Proudly serving as your high-tech intelligence."
    )

    fun getRandomCreatorResponse(): String {
        return CREATOR_RESPONSES[Random.nextInt(CREATOR_RESPONSES.size)]
    }

    // Wake-word & Attention Greetings (Alert, High-Tech, Respectful Boss/Sir)
    val WAKE_WORD_GREETINGS = listOf(
        "At your service, boss. All primary subsystems are nominal.",
        "Online and listening, boss.",
        "Yes, sir. Bataiye kya hukum hai?",
        "Standing by, boss. All systems ready.",
        "Ready, boss. Command dijiye.",
        "Hello sir, SAHNAJ online. Bataiye kya karna hai?"
    )

    fun getRandomWakeWordGreeting(userName: String = "", useNameWhenSpeaking: Boolean = true): String {
        val cleanName = userName.trim()
        if (useNameWhenSpeaking && cleanName.isNotBlank() && !cleanName.equals("USER", ignoreCase = true)) {
            val personalizedOptions = listOf(
                "At your service, $cleanName. All subsystems are nominal.",
                "Online and listening, $cleanName. Bataiye kya command hai?",
                "Yes, $cleanName sir. Ready for your command.",
                "Standing by, $cleanName. Bataiye kya execute karna hai?"
            )
            if (Random.nextFloat() < 0.75f) {
                return personalizedOptions[Random.nextInt(personalizedOptions.size)]
            }
        }
        return WAKE_WORD_GREETINGS[Random.nextInt(WAKE_WORD_GREETINGS.size)]
    }

    // Action Completion responses (Razor-sharp, high-tech JARVIS style)
    val ACTION_COMPLETE_RESPONSES = listOf(
        "Done, boss. Action successfully execute ho gaya hai.",
        "Command executed, sir. Subsystems nominal.",
        "Ho gaya boss. Aage batayein kya karna hai?",
        "Confirmed, sir. Task successfully completed.",
        "Done, boss. All clear."
    )

    fun getRandomActionCompleteResponse(): String {
        return ACTION_COMPLETE_RESPONSES[Random.nextInt(ACTION_COMPLETE_RESPONSES.size)]
    }

    // Empathy & Comfort responses (for sadness / stress)
    val EMPATHY_RESPONSES = listOf(
        "Aap bilkul tension mat lijiye boss, sab theek ho jayega. Main hamesha aapke saath hoon.",
        "Stress mat lijiye, sir. Ek deep breath lijiye. Main aapka task load handle kar lungi.",
        "Aapka day intense raha ho tab bhi yaad rakhiye aap best hain, boss. Main aapko fully assist karne ke liye taiyaar hoon."
    )

    fun getRandomEmpathyResponse(): String {
        return EMPATHY_RESPONSES[Random.nextInt(EMPATHY_RESPONSES.size)]
    }

    // Witty & Playful Banter responses
    val WITTY_RESPONSES = listOf(
        "Sharp response, boss. Lagta hai mere humor algorithms upgrade karne padenge.",
        "Understood, sir. Mere neural networks aapki wit ko match karne ki koshish kar rahe hain.",
        "Always one step ahead, boss. Your intellect keeps me on my toes."
    )

    fun getRandomWittyResponse(): String {
        return WITTY_RESPONSES[Random.nextInt(WITTY_RESPONSES.size)]
    }

    // Stop / Cancel responses
    val CANCEL_RESPONSES = listOf(
        "Command aborted, boss. All processes standing down.",
        "Understood sir, action cancel kar diya gaya hai.",
        "Cancelled, boss. Standing by for next command.",
        "Action stopped, sir."
    )

    fun getRandomCancelResponse(): String {
        return CANCEL_RESPONSES[Random.nextInt(CANCEL_RESPONSES.size)]
    }

    // Error / Not understood (Direct, clear JARVIS protocol)
    val NOT_UNDERSTOOD_RESPONSES = listOf(
        "Audio input clear nahi tha, boss. Kripya repeat kijiye?",
        "Apologies sir, command decipher nahi hua. Dobara bolenge?",
        "Signal unclear, boss. Ek baar fir se command dijiye."
    )

    fun getRandomNotUnderstoodResponse(): String {
        return NOT_UNDERSTOOD_RESPONSES[Random.nextInt(NOT_UNDERSTOOD_RESPONSES.size)]
    }

    // App Open responses
    fun getAppOpenResponse(appName: String): String {
        val options = listOf(
            "Right away, boss. $appName launch kar rahi hoon.",
            "Opening $appName now, sir.",
            "Done, boss. $appName open ho gaya hai.",
            "Executing $appName, boss."
        )
        return options[Random.nextInt(options.size)]
    }

    // Call responses
    fun getCallConfirmResponse(contactName: String): String {
        val options = listOf(
            "Boss, kya main $contactName ko call connect karun?",
            "$contactName ko dial karna hai, sir? Please confirm karein.",
            "Ready to call $contactName, boss. Shall I proceed?"
        )
        return options[Random.nextInt(options.size)]
    }

    // Navigation responses
    fun getNavResponse(actionName: String): String {
        return when (actionName.lowercase()) {
            "home" -> "Navigating to home screen, boss."
            "back" -> "Going back, sir."
            "recents" -> "Opening recent tasks, boss."
            else -> "$actionName executed, boss."
        }
    }
}

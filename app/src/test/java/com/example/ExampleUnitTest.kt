package com.example

import com.example.data.model.ActionType
import com.example.data.model.RiskLevel
import com.example.domain.automation.UniversalNodeFinder
import com.example.domain.parser.LocalCommandParser
import com.example.voice.TTSVoiceHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun tts_stripEmojis_removesEmojisCompletely() {
        val input1 = "Hello, main sun rahi hoon! 😊"
        val expected1 = "Hello, main sun rahi hoon!"
        assertEquals(expected1, TTSVoiceHelper.stripEmojis(input1))

        val input2 = "Done yaar! Kaam complete kar diya maine. 😊✨🚀❤️👍"
        val expected2 = "Done yaar! Kaam complete kar diya maine."
        assertEquals(expected2, TTSVoiceHelper.stripEmojis(input2))

        val input3 = "नमस्ते! मैं सहज हूँ 🙏🤖 आपका क्या हुक्म है?"
        val expected3 = "नमस्ते! मैं सहज हूँ आपका क्या हुक्म है?"
        assertEquals(expected3, TTSVoiceHelper.stripEmojis(input3))
    }

    @Test
    fun testWhatsAppCommandParsing() {
        val parser = LocalCommandParser()

        // 1. WhatsApp with contact and message
        val action1 = parser.parseLocally("WhatsApp kholo aur Mammi ko message karo — main late hoon", "SahNaj")
        assertNotNull(action1)
        assertEquals(ActionType.SEND_WHATSAPP_MESSAGE, action1?.action)
        assertEquals("Mammi", action1?.target)
        assertEquals("main late hoon", action1?.parameters?.get("message"))

        // 2. WhatsApp with send explicit
        val action2 = parser.parseLocally("WhatsApp par Rahul ko message bhejo main aa raha hoon", "SahNaj")
        assertNotNull(action2)
        assertEquals(ActionType.SEND_WHATSAPP_MESSAGE, action2?.action)
        assertEquals("Rahul", action2?.target)
        assertEquals("true", action2?.parameters?.get("send"))
        assertEquals(RiskLevel.HIGH, action2?.riskLevel)

        // 3. WhatsApp without message text
        val action3 = parser.parseLocally("WhatsApp par Mammi ko message karo", "SahNaj")
        assertNotNull(action3)
        assertEquals(ActionType.SEND_WHATSAPP_MESSAGE, action3?.action)
        assertEquals("Mammi", action3?.target)
        assertEquals("true", action3?.parameters?.get("prompt_text"))

        // 4. WhatsApp with raw phone number: "WhatsApp kholo aur 9876543210 ko message bhejo"
        val action4 = parser.parseLocally("WhatsApp kholo aur 9876543210 ko message bhejo", "SahNaj")
        assertNotNull(action4)
        assertEquals(ActionType.SEND_WHATSAPP_MESSAGE, action4?.action)
        assertEquals("9876543210", action4?.target)
        assertEquals("true", action4?.parameters?.get("prompt_text"))
    }

    @Test
    fun testYouTubeAndChromeParsing() {
        val parser = LocalCommandParser()

        // YouTube search
        val ytAction = parser.parseLocally("YouTube kholo aur Arijit Singh ke gaane search karo", "SahNaj")
        assertNotNull(ytAction)
        assertEquals(ActionType.YOUTUBE_SEARCH, ytAction?.action)
        assertTrue(ytAction?.target?.contains("Arijit Singh ke gaane", ignoreCase = true) == true)

        // Chrome search
        val chromeAction = parser.parseLocally("Chrome kholo aur weather in Mumbai search karo", "SahNaj")
        assertNotNull(chromeAction)
        assertEquals(ActionType.WEB_SEARCH, chromeAction?.action)
        assertTrue(chromeAction?.target?.contains("weather in Mumbai", ignoreCase = true) == true)
    }

    @Test
    fun testGenericTapAndTypeParsing() {
        val parser = LocalCommandParser()

        // Tap
        val tapAction = parser.parseLocally("Login par tap karo", "SahNaj")
        assertNotNull(tapAction)
        assertEquals(ActionType.FIND_AND_TAP, tapAction?.action)
        assertTrue(tapAction?.target.equals("Login", ignoreCase = true))

        // Type
        val typeAction = parser.parseLocally("email field mein irfan@test.com type karo", "SahNaj")
        assertNotNull(typeAction)
        assertEquals(ActionType.FIND_AND_TYPE, typeAction?.action)
    }

    @Test
    fun testUniversalNodeFinderSimilarityAndSecurity() {
        val similarity = UniversalNodeFinder.calculateSimilarity("Submit", "Submit Now")
        assertTrue(similarity > 0.5)

        val blockedScan = UniversalNodeFinder.findTargetNode(null, "enter password")
        assertEquals(UniversalNodeFinder.MatchType.BLOCKED_SENSITIVE, blockedScan.matchType)
    }
}

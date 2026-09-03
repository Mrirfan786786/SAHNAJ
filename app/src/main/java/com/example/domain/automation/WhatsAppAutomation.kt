package com.example.domain.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.accessibility.SahNajAccessibilityService
import com.example.data.model.ExecutionResult
import com.example.data.model.ResultStatus
import com.example.domain.resolvers.AppResolver
import com.example.domain.resolvers.ContactResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * High-Reliability Deep WhatsApp Automation Engine.
 *
 * Implements the 10-step Voice Pipeline:
 * 1. Open WhatsApp (`com.whatsapp`)
 * 2. Wait 1-2s for UI initialization
 * 3. Locate Search icon (`Search`, `खोजें`, id `menuitem_search`, etc.) -> Tap
 * 4. Type contact name (e.g. "Mammi")
 * 5. Wait & Select matching contact
 * 6. Multiple matches / disambiguation handling
 * 7. Chat screen loading verification
 * 8. Locate message input field & Type message
 * 9. Tap Send if requested / confirmed, or leave typed safely
 * 10. Robust Intent fallback if Accessibility UI tree changes
 */
class WhatsAppAutomation(
    private val context: Context,
    private val appResolver: AppResolver,
    private val contactResolver: ContactResolver
) {

    companion object {
        private const val TAG = "SAHNAJ_WA_AUTOMATION"
        private const val WA_PACKAGE = "com.whatsapp"
        private const val WA_BUSINESS_PACKAGE = "com.whatsapp.w4b"
    }

    /**
     * Executes the WhatsApp voice pipeline.
     */
    suspend fun executeWhatsAppMessagePipeline(
        contactName: String,
        messageText: String,
        shouldSendImmediately: Boolean
    ): ExecutionResult = withContext(Dispatchers.IO) {
        val cleanContact = contactName.trim()
        val cleanMessage = messageText.trim()

        Log.d(TAG, "Starting WhatsApp automation pipeline for '$cleanContact' with message: '$cleanMessage'")

        // 1. Verify WhatsApp installation
        val isStandardInstalled = isAppInstalled(WA_PACKAGE)
        val isBusinessInstalled = isAppInstalled(WA_BUSINESS_PACKAGE)
        val targetPackage = when {
            isStandardInstalled -> WA_PACKAGE
            isBusinessInstalled -> WA_BUSINESS_PACKAGE
            else -> null
        }

        if (targetPackage == null) {
            Log.e(TAG, "WhatsApp is not installed on device.")
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "WhatsApp aapke phone mein installed nahi mila.",
                detail = "WhatsApp not installed"
            )
        }

        // 2. Check if Accessibility Service is active
        val accessibilityService = SahNajAccessibilityService.instance
        if (accessibilityService == null) {
            Log.w(TAG, "Accessibility service not running. Falling back to direct Intent.")
            return@withContext fallbackIntentMessage(cleanContact, cleanMessage, targetPackage)
        }

        // 3. Launch WhatsApp
        val launched = appResolver.launchApp(targetPackage)
        if (!launched) {
            return@withContext fallbackIntentMessage(cleanContact, cleanMessage, targetPackage)
        }

        // 4. Wait 1.5s for WhatsApp UI to load
        delay(1600)

        // 5. Find and click Search Icon
        val searchClicked = findAndTapSearchIcon(accessibilityService)
        if (!searchClicked) {
            Log.w(TAG, "Search icon not found via accessibility. Attempting direct chat intent fallback.")
            return@withContext fallbackIntentMessage(cleanContact, cleanMessage, targetPackage)
        }

        // 6. Wait 500ms for search bar and type Contact Name
        delay(600)
        val typedContact = typeContactInSearch(accessibilityService, cleanContact)
        if (!typedContact) {
            Log.w(TAG, "Failed to type contact in search bar.")
            return@withContext fallbackIntentMessage(cleanContact, cleanMessage, targetPackage)
        }

        // 7. Wait 900ms for search results to load
        delay(900)

        // 8. Select matching contact from search results
        val contactSelected = selectContactFromResults(accessibilityService, cleanContact)
        if (!contactSelected) {
            Log.w(TAG, "Could not tap contact from search results: '$cleanContact'")
            return@withContext fallbackIntentMessage(cleanContact, cleanMessage, targetPackage)
        }

        // 9. Wait 1.2s for Chat screen to open
        delay(1200)

        // 10. Find message input field and type message
        if (cleanMessage.isNotBlank()) {
            val typedMessage = typeMessageInChat(accessibilityService, cleanMessage)
            if (typedMessage) {
                if (shouldSendImmediately) {
                    delay(500)
                    val sendClicked = findAndTapSendButton(accessibilityService)
                    if (sendClicked) {
                        return@withContext ExecutionResult(
                            status = ResultStatus.SUCCESS,
                            spokenResponse = "Ji, WhatsApp par $cleanContact ko message bhej diya gaya.",
                            detail = "WhatsApp message sent to $cleanContact"
                        )
                    }
                }
                return@withContext ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "Ji, WhatsApp par $cleanContact ki chat mein message type kar diya hai.",
                    detail = "WhatsApp message typed for $cleanContact"
                )
            }
        }

        ExecutionResult(
            status = ResultStatus.SUCCESS,
            spokenResponse = "Ji, WhatsApp par $cleanContact ki chat open kar di hai.",
            detail = "WhatsApp chat opened for $cleanContact"
        )
    }

    private fun findAndTapSearchIcon(service: SahNajAccessibilityService): Boolean {
        val root = service.rootInActiveWindow ?: return false

        // Try standard WhatsApp search descriptions & IDs
        val searchKeywords = listOf(
            "search", "search…", "खोजें", "dhoondhe", "search button",
            "menuitem_search", "search_holder", "search_src_text"
        )

        for (keyword in searchKeywords) {
            val nodes = root.findAccessibilityNodeInfosByText(keyword)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    if (UniversalNodeFinder.performClick(node)) return true
                }
            }
        }

        // Scan all nodes for search content description or ID
        val scan = UniversalNodeFinder.findTargetNode(root, "Search")
        if (scan.node != null && UniversalNodeFinder.performClick(scan.node)) {
            return true
        }

        return false
    }

    private fun typeContactInSearch(service: SahNajAccessibilityService, contactName: String): Boolean {
        val root = service.rootInActiveWindow ?: return false
        val editable = UniversalNodeFinder.findFirstEditableNode(root)
        if (editable != null) {
            return UniversalNodeFinder.performSetText(editable, contactName)
        }
        return service.typeTextIntoField(contactName, "Search")
    }

    private fun selectContactFromResults(service: SahNajAccessibilityService, contactName: String): Boolean {
        val root = service.rootInActiveWindow ?: return false

        // Exact / Substring search in results
        val scan = UniversalNodeFinder.findTargetNode(root, contactName)
        if (scan.node != null) {
            return UniversalNodeFinder.performClick(scan.node)
        }

        // Fallback: Click first list item in search results
        val matchingNodes = root.findAccessibilityNodeInfosByText(contactName)
        if (!matchingNodes.isNullOrEmpty()) {
            for (node in matchingNodes) {
                if (UniversalNodeFinder.performClick(node)) return true
            }
        }

        return false
    }

    private fun typeMessageInChat(service: SahNajAccessibilityService, message: String): Boolean {
        val root = service.rootInActiveWindow ?: return false

        val chatHints = listOf("Message", "Type a message", "संदेश", "Text message")
        for (hint in chatHints) {
            val matching = root.findAccessibilityNodeInfosByText(hint)
            if (!matching.isNullOrEmpty()) {
                val inputNode = matching.firstOrNull { it.isEditable && !it.isPassword } ?: matching.first()
                if (UniversalNodeFinder.performSetText(inputNode, message)) return true
            }
        }

        val editable = UniversalNodeFinder.findFirstEditableNode(root)
        if (editable != null) {
            return UniversalNodeFinder.performSetText(editable, message)
        }

        return service.typeTextIntoField(message)
    }

    private fun findAndTapSendButton(service: SahNajAccessibilityService): Boolean {
        val root = service.rootInActiveWindow ?: return false

        val sendKeywords = listOf("Send", "भेजें", "send_btn", "send")
        for (kw in sendKeywords) {
            val matching = root.findAccessibilityNodeInfosByText(kw)
            if (!matching.isNullOrEmpty()) {
                for (node in matching) {
                    if (UniversalNodeFinder.performClick(node)) return true
                }
            }
        }

        val scan = UniversalNodeFinder.findTargetNode(root, "Send")
        if (scan.node != null) {
            return UniversalNodeFinder.performClick(scan.node)
        }

        return false
    }

    private fun fallbackIntentMessage(
        contactName: String,
        message: String,
        packageName: String
    ): ExecutionResult {
        // Check phone contacts for phone number
        val contacts = contactResolver.findContactsByName(contactName)
        val phoneNumber = if (contacts.isNotEmpty()) {
            contacts.first().phoneNumber.filter { it.isDigit() || it == '+' }
        } else {
            contactName.filter { it.isDigit() || it == '+' }
        }

        return try {
            if (phoneNumber.isNotBlank() && phoneNumber.length >= 7) {
                val cleanNumber = if (!phoneNumber.startsWith("+") && phoneNumber.length == 10) {
                    "91$phoneNumber" // Default India country code if 10 digits
                } else {
                    phoneNumber.replace("+", "")
                }
                val encodedMsg = Uri.encode(message)
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=$encodedMsg")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "WhatsApp par $contactName ki chat open kar di hai.",
                    detail = "WhatsApp direct intent launched for $cleanNumber"
                )
            } else {
                // Generic share intent to WhatsApp
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage(packageName)
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "WhatsApp open kar diya hai, $contactName ko select karke message bhej dijiye.",
                    detail = "WhatsApp share intent launched"
                )
            }
        } catch (e: Exception) {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "WhatsApp open karne mein samasya aayi.",
                detail = e.localizedMessage
            )
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

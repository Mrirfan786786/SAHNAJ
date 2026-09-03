package com.example.domain.automation

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.math.min

/**
 * Universal Accessibility Node Inspector and Interaction Engine.
 * Scans on-screen UI nodes, calculates Levenshtein similarity,
 * provides closest text suggestions, and safely interacts with UI elements.
 */
object UniversalNodeFinder {

    private const val TAG = "SAHNAJ_NODE_FINDER"

    private val SENSITIVE_KEYWORDS = listOf(
        "password", "pin", "otp", "cvv", "credit card", "debit card", "upi pin",
        "passcode", "secret", "netbanking", "cvv2", "atm pin", "passkey"
    )

    data class ScanResult(
        val node: AccessibilityNodeInfo?,
        val matchedText: String?,
        val matchType: MatchType,
        val suggestedAlternatives: List<String> = emptyList()
    )

    enum class MatchType {
        EXACT,
        CONTAINS,
        FUZZY_SUGGESTION,
        NOT_FOUND,
        BLOCKED_SENSITIVE
    }

    /**
     * Scans the active window node hierarchy for a given target label or text.
     */
    fun findTargetNode(root: AccessibilityNodeInfo?, targetQuery: String): ScanResult {
        if (targetQuery.isBlank()) {
            return ScanResult(null, null, MatchType.NOT_FOUND)
        }

        val cleanQuery = targetQuery.trim().lowercase()

        // 1. Check if user is trying to target a password or sensitive field
        for (sensitive in SENSITIVE_KEYWORDS) {
            if (cleanQuery.contains(sensitive)) {
                Log.w(TAG, "Target query contains sensitive keyword: $sensitive. Blocking for user safety.")
                return ScanResult(null, null, MatchType.BLOCKED_SENSITIVE)
            }
        }

        if (root == null) {
            return ScanResult(null, null, MatchType.NOT_FOUND)
        }

        val allVisibleNodes = mutableListOf<NodeTextItem>()
        collectAllVisibleTextNodes(root, allVisibleNodes)

        if (allVisibleNodes.isEmpty()) {
            return ScanResult(null, null, MatchType.NOT_FOUND)
        }

        // 2. Exact Match (case-insensitive)
        val exactMatch = allVisibleNodes.find { it.text.equals(cleanQuery, ignoreCase = true) }
        if (exactMatch != null && !isNodeSensitive(exactMatch.node)) {
            return ScanResult(exactMatch.node, exactMatch.text, MatchType.EXACT)
        }

        // 3. Contains / Substring Match
        val containsMatch = allVisibleNodes.find {
            val lowerText = it.text.lowercase()
            lowerText.contains(cleanQuery) || cleanQuery.contains(lowerText)
        }
        if (containsMatch != null && !isNodeSensitive(containsMatch.node)) {
            return ScanResult(containsMatch.node, containsMatch.text, MatchType.CONTAINS)
        }

        // 4. Fuzzy / Levenshtein Distance Match (Finding closest suggestion)
        val suggestions = mutableListOf<Pair<String, Double>>()
        for (item in allVisibleNodes) {
            if (isNodeSensitive(item.node)) continue
            val similarity = calculateSimilarity(cleanQuery, item.text.lowercase())
            if (similarity >= 0.45) {
                suggestions.add(Pair(item.text, similarity))
            }
        }

        suggestions.sortByDescending { it.second }
        val topSuggestions = suggestions.map { it.first }.distinct().take(3)

        if (topSuggestions.isNotEmpty()) {
            val best = suggestions.first()
            if (best.second >= 0.70) {
                val bestNode = allVisibleNodes.find { it.text.equals(best.first, ignoreCase = true) }
                return ScanResult(bestNode?.node, best.first, MatchType.FUZZY_SUGGESTION, topSuggestions)
            }
            return ScanResult(null, null, MatchType.FUZZY_SUGGESTION, topSuggestions)
        }

        return ScanResult(null, null, MatchType.NOT_FOUND)
    }

    /**
     * Safely clicks a node, walking up the parent chain to find a clickable container if needed.
     */
    fun performClick(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (isNodeSensitive(node)) return false

        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable) {
                val success = current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (success) return true
            }
            current = current.parent
        }

        // Fallback: try click on original node anyway
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    /**
     * Safely types text into an editable node.
     * STRICT GUARD: Blocks if the node is a password field or sensitive.
     */
    fun performSetText(node: AccessibilityNodeInfo?, textToType: String): Boolean {
        if (node == null) return false

        // CRITICAL SECURITY GUARD: Never type into password fields
        if (isNodeSensitive(node)) {
            Log.e(TAG, "Attempted automated text entry into sensitive or password node. BLOCKED.")
            return false
        }

        val arguments = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    /**
     * Finds the first available editable text input node on the active screen.
     */
    fun findFirstEditableNode(root: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (root == null) return null
        if (root.isEditable && !isNodeSensitive(root)) return root

        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            val found = findFirstEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    /**
     * Checks if an accessibility node is sensitive (Password, PIN, OTP, etc.)
     */
    fun isNodeSensitive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        if (node.isPassword) return true

        val text = (node.text?.toString() ?: "").lowercase()
        val desc = (node.contentDescription?.toString() ?: "").lowercase()
        val viewId = (node.viewIdResourceName ?: "").lowercase()

        for (keyword in SENSITIVE_KEYWORDS) {
            if (text.contains(keyword) || desc.contains(keyword) || viewId.contains(keyword)) {
                return true
            }
        }
        return false
    }

    private data class NodeTextItem(
        val node: AccessibilityNodeInfo,
        val text: String
    )

    private fun collectAllVisibleTextNodes(node: AccessibilityNodeInfo, outList: MutableList<NodeTextItem>) {
        if (!node.isVisibleToUser) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrBlank()) {
            outList.add(NodeTextItem(node, text))
        }
        if (!desc.isNullOrBlank() && desc != text) {
            outList.add(NodeTextItem(node, desc))
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectAllVisibleTextNodes(child, outList)
        }
    }

    /**
     * Calculates normalized similarity score [0.0, 1.0] using Levenshtein distance.
     */
    fun calculateSimilarity(s1: String, s2: String): Double {
        val longer = if (s1.length >= s2.length) s1 else s2
        val shorter = if (s1.length >= s2.length) s2 else s1
        if (longer.isEmpty()) return 1.0
        val distance = levenshteinDistance(longer, shorter)
        return (longer.length - distance).toDouble() / longer.length.toDouble()
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}

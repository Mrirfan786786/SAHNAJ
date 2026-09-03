package com.example.domain.automation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.accessibility.SahNajAccessibilityService
import com.example.data.model.ExecutionResult
import com.example.data.model.ResultStatus
import com.example.domain.resolvers.AppResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Universal App Control & Automation Dispatcher.
 * Covers YouTube search, Chrome search, and Generic Find & Tap / Type engines.
 */
class AppControlHub(
    private val context: Context,
    private val appResolver: AppResolver
) {

    companion object {
        private const val TAG = "SAHNAJ_APP_HUB"
        private const val YOUTUBE_PKG = "com.google.android.youtube"
        private const val CHROME_PKG = "com.android.chrome"
    }

    /**
     * YouTube Voice Automation:
     * "YouTube kholo aur [query] search karo"
     */
    suspend fun executeYouTubeSearch(query: String): ExecutionResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        Log.d(TAG, "Executing YouTube search for: \"$cleanQuery\"")

        // 1. Direct Intent Search (Most reliable and fastest)
        try {
            val intent = Intent(Intent.ACTION_SEARCH).apply {
                setPackage(YOUTUBE_PKG)
                putExtra("query", cleanQuery)
                putExtra(SearchManager.QUERY, cleanQuery)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return@withContext ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "Ji, YouTube par '$cleanQuery' search kar diya hai.",
                detail = "YouTube search intent executed for $cleanQuery"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Direct YouTube search intent failed, trying URI intent: ${e.message}")
        }

        // 2. Fallback URI Intent
        try {
            val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(cleanQuery)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return@withContext ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "Ji, YouTube par '$cleanQuery' search kar diya hai.",
                detail = "YouTube URL intent executed for $cleanQuery"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Fallback URI search failed, falling back to accessibility automation.")
        }

        // 3. Accessibility Fallback Pipeline
        val service = SahNajAccessibilityService.instance
        if (service != null) {
            appResolver.launchApp(YOUTUBE_PKG)
            delay(1500)
            val root = service.rootInActiveWindow
            val searchIcon = UniversalNodeFinder.findTargetNode(root, "Search")
            if (searchIcon.node != null) {
                UniversalNodeFinder.performClick(searchIcon.node)
                delay(600)
                val editable = UniversalNodeFinder.findFirstEditableNode(service.rootInActiveWindow)
                if (editable != null) {
                    UniversalNodeFinder.performSetText(editable, cleanQuery)
                }
            }
            return@withContext ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "YouTube par '$cleanQuery' search khol diya hai.",
                detail = "YouTube accessibility automation executed"
            )
        }

        ExecutionResult(
            status = ResultStatus.FAILURE,
            spokenResponse = "YouTube search open karne mein samasya aayi.",
            detail = "Could not execute YouTube search"
        )
    }

    /**
     * Chrome / Browser Voice Automation:
     * "Chrome kholo aur [query] search karo"
     */
    suspend fun executeWebSearch(query: String): ExecutionResult = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        Log.d(TAG, "Executing Web search for: \"$cleanQuery\"")

        try {
            val searchUrl = "https://www.google.com/search?q=${Uri.encode(cleanQuery)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Prefer Chrome if installed
                if (isPackageInstalled(CHROME_PKG)) {
                    setPackage(CHROME_PKG)
                }
            }
            context.startActivity(intent)
            return@withContext ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "Chrome par '$cleanQuery' search kar diya hai.",
                detail = "Web search executed: $searchUrl"
            )
        } catch (e: Exception) {
            // General Web Search Intent fallback
            try {
                val genericIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                    putExtra(SearchManager.QUERY, cleanQuery)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(genericIntent)
                return@withContext ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = "Internet par '$cleanQuery' search kar diya hai.",
                    detail = "ACTION_WEB_SEARCH executed"
                )
            } catch (err: Exception) {
                return@withContext ExecutionResult(
                    status = ResultStatus.FAILURE,
                    spokenResponse = "Search open karne mein samasya aayi.",
                    detail = err.localizedMessage
                )
            }
        }
    }

    /**
     * Generic "Find and Tap" Engine for ANY third-party application screen.
     * Scans on-screen nodes, matches text or provides intelligent suggestion if closest match found.
     */
    suspend fun executeGenericTap(targetText: String): ExecutionResult = withContext(Dispatchers.Main) {
        val cleanTarget = targetText.trim()
        val service = SahNajAccessibilityService.instance
        if (service == null) {
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "SahNaj accessibility service chalu nahi hai. Kripya phone settings mein jakar on karein.",
                detail = "Accessibility Service is inactive"
            )
        }

        val root = service.rootInActiveWindow
        if (root == null) {
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Screen par koi UI element nahi dikh raha.",
                detail = "Active window root node is null"
            )
        }

        val result = UniversalNodeFinder.findTargetNode(root, cleanTarget)
        when (result.matchType) {
            UniversalNodeFinder.MatchType.BLOCKED_SENSITIVE -> {
                ExecutionResult(
                    status = ResultStatus.FAILURE,
                    spokenResponse = "Suraksha ke liye password, PIN ya financial fields par tap karna allow nahi hai.",
                    detail = "Target element blocked for privacy/security"
                )
            }
            UniversalNodeFinder.MatchType.EXACT, UniversalNodeFinder.MatchType.CONTAINS -> {
                val clicked = UniversalNodeFinder.performClick(result.node)
                if (clicked) {
                    ExecutionResult(
                        status = ResultStatus.SUCCESS,
                        spokenResponse = "'${result.matchedText ?: cleanTarget}' par tap kar diya.",
                        detail = "Clicked element with text: ${result.matchedText}"
                    )
                } else {
                    ExecutionResult(
                        status = ResultStatus.FAILURE,
                        spokenResponse = "'$cleanTarget' mila lekin uspar tap nahi ho paya.",
                        detail = "Element found but click action returned false"
                    )
                }
            }
            UniversalNodeFinder.MatchType.FUZZY_SUGGESTION -> {
                if (result.node != null) {
                    val clicked = UniversalNodeFinder.performClick(result.node)
                    if (clicked) {
                        return@withContext ExecutionResult(
                            status = ResultStatus.SUCCESS,
                            spokenResponse = "'${result.matchedText}' par tap kar diya.",
                            detail = "Fuzzy matched and clicked: ${result.matchedText}"
                        )
                    }
                }
                val suggestionList = result.suggestedAlternatives.joinToString(", ")
                ExecutionResult(
                    status = ResultStatus.FAILURE,
                    spokenResponse = "Exact '$cleanTarget' nahi mila. Kya aapka matlab '$suggestionList' se tha?",
                    detail = "Did you mean suggestions: $suggestionList"
                )
            }
            UniversalNodeFinder.MatchType.NOT_FOUND -> {
                ExecutionResult(
                    status = ResultStatus.FAILURE,
                    spokenResponse = "Screen par '$cleanTarget' naam ka koi button ya text nahi mila.",
                    detail = "Element '$cleanTarget' not found on current screen"
                )
            }
        }
    }

    /**
     * Generic "Type Text" Engine into current screen's editable text field.
     */
    suspend fun executeGenericType(textToType: String, fieldHint: String?): ExecutionResult = withContext(Dispatchers.Main) {
        val service = SahNajAccessibilityService.instance
        if (service == null) {
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "SahNaj accessibility service chalu nahi hai. Kripya settings mein jakar on karein.",
                detail = "Accessibility Service is inactive"
            )
        }

        val root = service.rootInActiveWindow
        if (root == null) {
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Screen par koi input box nahi mila.",
                detail = "Active window root node is null"
            )
        }

        val targetNode = if (!fieldHint.isNullOrBlank()) {
            val scan = UniversalNodeFinder.findTargetNode(root, fieldHint)
            scan.node?.takeIf { it.isEditable && !UniversalNodeFinder.isNodeSensitive(it) }
                ?: UniversalNodeFinder.findFirstEditableNode(root)
        } else {
            UniversalNodeFinder.findFirstEditableNode(root)
        }

        if (targetNode == null) {
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Screen par koi editable text field nahi mila.",
                detail = "No editable node found on screen"
            )
        }

        if (UniversalNodeFinder.isNodeSensitive(targetNode)) {
            return@withContext ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Suraksha ke liye password ya sensitive field mein automatic typing allow nahi hai.",
                detail = "Sensitive node blocked"
            )
        }

        val typed = UniversalNodeFinder.performSetText(targetNode, textToType)
        if (typed) {
            ExecutionResult(
                status = ResultStatus.SUCCESS,
                spokenResponse = "Text type kar diya hai.",
                detail = "Typed text successfully"
            )
        } else {
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = "Text type karne mein problem aayi.",
                detail = "performSetText returned false"
            )
        }
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

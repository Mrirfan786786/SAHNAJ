package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SahNajAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceActive.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Only lightweight handling, no continuous background recording or scraping
    }

    override fun onInterrupt() {
        // Handle interruption
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isServiceActive.value = false
        }
    }

    fun doBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun doHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun doRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    fun doNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    fun doQuickSettings(): Boolean = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

    fun doScrollDown(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollable = findFirstScrollableNode(root)
        val result = scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        if (!result && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return performSwipeGesture(SwipeDirection.UP) // Dragging up scrolls down
        }
        return result
    }

    fun doScrollUp(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollable = findFirstScrollableNode(root)
        val result = scrollable?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
        if (!result && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return performSwipeGesture(SwipeDirection.DOWN) // Dragging down scrolls up
        }
        return result
    }

    fun doSwipe(direction: SwipeDirection): Boolean {
        return performSwipeGesture(direction)
    }

    fun findAndClick(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)
        if (nodes.isNullOrEmpty()) return false

        for (node in nodes) {
            var clickableNode: AccessibilityNodeInfo? = node
            while (clickableNode != null && !clickableNode.isClickable) {
                clickableNode = clickableNode.parent
            }
            if (clickableNode != null && clickableNode.isClickable) {
                val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                if (success) return true
            }
        }
        return false
    }

    /**
     * Periodically monitors the active window to detect and auto-click "Install" / "इन्स्टॉल" on Google Play Store.
     */
    fun scheduleAutoInstallClick() {
        try {
            serviceScope.launch {
                // Retry for up to 8 seconds (16 attempts every 500ms)
                for (i in 1..16) {
                    try {
                        delay(500)
                        val clicked = findAndClickInstallButton()
                        if (clicked) {
                            android.util.Log.d("SahNajAccessibility", "Auto-clicked Play Store install button on attempt $i")
                            break
                        }
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Inspects active window nodes for "Install", "इन्स्टॉल करें", or "Update" buttons on app stores and clicks them.
     */
    fun findAndClickInstallButton(): Boolean {
        return try {
            val root = rootInActiveWindow ?: return false
            val installKeywords = listOf(
                "Install", "इन्स्टॉल करें", "इन्स्टॉल", "इंस्टॉल", "डाउनलोड करें", "Download", "Update", "अपडेट करें", "Install now"
            )
            for (keyword in installKeywords) {
                val nodes = root.findAccessibilityNodeInfosByText(keyword)
                if (!nodes.isNullOrEmpty()) {
                    for (node in nodes) {
                        var clickableNode: AccessibilityNodeInfo? = node
                        while (clickableNode != null && !clickableNode.isClickable) {
                            clickableNode = clickableNode.parent
                        }
                        if (clickableNode != null && clickableNode.isClickable) {
                            val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            if (success) return true
                        }
                    }
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    fun findAndLongPress(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)
        if (nodes.isNullOrEmpty()) return false

        for (node in nodes) {
            var clickableNode: AccessibilityNodeInfo? = node
            while (clickableNode != null && !clickableNode.isLongClickable) {
                clickableNode = clickableNode.parent
            }
            if (clickableNode != null && clickableNode.isLongClickable) {
                val success = clickableNode.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
                if (success) return true
            }
        }
        return false
    }

    fun typeTextIntoField(textToType: String, fieldHint: String? = null): Boolean {
        val root = rootInActiveWindow ?: return false
        val targetNode: AccessibilityNodeInfo? = if (!fieldHint.isNullOrBlank()) {
            val matching = root.findAccessibilityNodeInfosByText(fieldHint)
            matching?.firstOrNull { it.isEditable && !it.isPassword } ?: findEditableNode(root)
        } else {
            findFocusedOrEditableNode(root)
        }

        if (targetNode != null) {
            // Guardrail: Never type into password fields
            if (targetNode.isPassword) {
                return false
            }
            val arguments = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            }
            return targetNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return false
    }

    private fun findFocusedOrEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable && !node.isPassword) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFocusedOrEditableNode(child)
            if (found != null) return found
        }
        return findEditableNode(node)
    }

    private fun findEditableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isEditable && !node.isPassword) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findEditableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun findFirstScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findFirstScrollableNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun performSwipeGesture(direction: SwipeDirection): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false

        val displayMetrics = resources.displayMetrics
        val width = displayMetrics.widthPixels.toFloat()
        val height = displayMetrics.heightPixels.toFloat()

        val startX: Float
        val startY: Float
        val endX: Float
        val endY: Float

        when (direction) {
            SwipeDirection.UP -> {
                startX = width / 2
                startY = height * 0.75f
                endX = width / 2
                endY = height * 0.25f
            }
            SwipeDirection.DOWN -> {
                startX = width / 2
                startY = height * 0.25f
                endX = width / 2
                endY = height * 0.75f
            }
            SwipeDirection.LEFT -> {
                startX = width * 0.85f
                startY = height / 2
                endX = width * 0.15f
                endY = height / 2
            }
            SwipeDirection.RIGHT -> {
                startX = width * 0.15f
                startY = height / 2
                endX = width * 0.85f
                endY = height / 2
            }
        }

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT
    }

    /**
     * Extracts readable text content from currently displayed accessibility window nodes.
     */
    fun extractScreenText(maxChars: Int = 4000): String {
        val root = rootInActiveWindow ?: return ""
        val textList = mutableListOf<String>()
        collectNodeTexts(root, textList)
        val combined = textList.filter { it.isNotBlank() }.distinct().joinToString(" • ")
        return if (combined.length > maxChars) combined.take(maxChars) + "..." else combined
    }

    private fun collectNodeTexts(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        if (!node.text.isNullOrBlank()) {
            list.add(node.text.toString().trim())
        } else if (!node.contentDescription.isNullOrBlank()) {
            list.add(node.contentDescription.toString().trim())
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectNodeTexts(child, list)
        }
    }

    companion object {
        var instance: SahNajAccessibilityService? = null
            private set

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()

        fun isAccessibilityEnabled(context: Context): Boolean {
            if (instance != null) return true
            val expectedServiceName = "${context.packageName}/${SahNajAccessibilityService::class.java.name}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            return enabledServices.split(':').any {
                it.equals(expectedServiceName, ignoreCase = true) ||
                        it.contains(SahNajAccessibilityService::class.java.simpleName)
            }
        }
    }
}

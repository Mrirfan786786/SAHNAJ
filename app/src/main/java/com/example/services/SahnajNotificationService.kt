package com.example.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.SahNajApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * SahnajNotificationService:
 * NotificationListenerService for SAHNAJ AI to read and process incoming notifications
 * (WhatsApp messages, incoming calls, SMS alerts, and app notifications).
 */
class SahnajNotificationService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    data class NotificationItem(
        val id: Int,
        val packageName: String,
        val appName: String,
        val title: String,
        val content: String,
        val postTime: Long,
        val isCall: Boolean = false,
        val isWhatsApp: Boolean = false,
        val isSms: Boolean = false
    )

    override fun onCreate() {
        super.onCreate()
        instance = this
        _isServiceConnected.value = true
        Log.d(TAG, "SahnajNotificationService created and running")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceConnected.value = false
        Log.d(TAG, "SahnajNotificationService destroyed")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        _isServiceConnected.value = true
        Log.d(TAG, "SahnajNotificationService connected successfully")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        _isServiceConnected.value = false
        Log.d(TAG, "SahnajNotificationService disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return

        // Ignore our own notifications to prevent feedback loops
        if (packageName == applicationContext.packageName) return

        try {
            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return

            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: extras.getString(Notification.EXTRA_TITLE)
                ?: ""

            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
                ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
                ?: extras.getString(Notification.EXTRA_TEXT)
                ?: ""

            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

            // Skip empty/blank notifications
            if (title.isBlank() && text.isBlank()) return

            val isWhatsApp = packageName.contains("whatsapp", ignoreCase = true)
            val isCall = packageName.contains("dialer", ignoreCase = true) ||
                    packageName.contains("telecom", ignoreCase = true) ||
                    packageName.contains("incallui", ignoreCase = true) ||
                    notification.category == Notification.CATEGORY_CALL

            val isSms = packageName.contains("messaging", ignoreCase = true) ||
                    packageName.contains("mms", ignoreCase = true) ||
                    notification.category == Notification.CATEGORY_MESSAGE

            val app = applicationContext as? SahNajApplication
            val prefs = app?.userPreferences
            val isCallAssistant = prefs?.isCallAssistantEnabled() ?: false
            val isChatNotifications = prefs?.isChatNotificationsEnabled() ?: false

            if (isCall && !isCallAssistant) {
                Log.d(TAG, "Call received but Call Assistant is disabled in settings; skipping broadcast.")
                return
            }
            if ((isWhatsApp || isSms) && !isChatNotifications) {
                Log.d(TAG, "Message notification received but Chat Notifications is disabled; skipping broadcast.")
                return
            }

            val appLabel = getAppNameFromPackage(packageName)

            val fullBody = if (subText.isNotBlank() && text.isNotBlank()) "$subText: $text" else text.ifBlank { subText }

            val item = NotificationItem(
                id = sbn.id,
                packageName = packageName,
                appName = appLabel,
                title = title,
                content = fullBody,
                postTime = sbn.postTime,
                isCall = isCall,
                isWhatsApp = isWhatsApp,
                isSms = isSms
            )

            Log.d(TAG, "Incoming Notification -> App: $appLabel ($packageName), Title: $title, Text: $fullBody")

            _latestNotification.value = item
            serviceScope.launch {
                _notificationEvents.emit(item)

                // JARVIS Smart Notification Radar Proactive Voice Alert
                if (isWhatsApp || isSms || isCall) {
                    val senderName = title.ifBlank { "Unknown Contact" }
                    val proactiveText = when {
                        isCall -> "Boss, incoming call from $senderName."
                        isWhatsApp -> "Boss, incoming WhatsApp message from $senderName. Kya main read karun ya reply bhejun?"
                        else -> "Boss, incoming message from $senderName."
                    }
                    try {
                        val tts = app?.textToSpeechManager
                        tts?.speak(proactiveText)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to speak proactive radar alert", e)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing incoming notification from $packageName", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        Log.d(TAG, "Notification removed: ${sbn.packageName} (ID: ${sbn.id})")
    }

    private fun getAppNameFromPackage(pkg: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            when {
                pkg.contains("whatsapp") -> "WhatsApp"
                pkg.contains("dialer") -> "Phone Call"
                pkg.contains("messaging") -> "Messages"
                pkg.contains("instagram") -> "Instagram"
                pkg.contains("telegram") -> "Telegram"
                pkg.contains("youtube") -> "YouTube"
                else -> pkg.substringAfterLast('.')
            }
        }
    }

    companion object {
        private const val TAG = "SahnajNotification"

        var instance: SahnajNotificationService? = null
            private set

        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        private val _latestNotification = MutableStateFlow<NotificationItem?>(null)
        val latestNotification: StateFlow<NotificationItem?> = _latestNotification.asStateFlow()

        private val _notificationEvents = MutableSharedFlow<NotificationItem>(extraBufferCapacity = 64)
        val notificationEvents: SharedFlow<NotificationItem> = _notificationEvents.asSharedFlow()

        /**
         * Checks if SAHNAJ AI has been granted Notification Listener Access in Android Settings.
         */
        fun isNotificationAccessGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (flat.isNullOrBlank()) return false

            val names = flat.split(":")
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && cn.packageName == packageName) {
                    return true
                }
            }
            return false
        }

        /**
         * Direct intent to open Android's Notification Listener Settings screen.
         */
        fun openNotificationAccessSettings(context: Context) {
            try {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(fallbackIntent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open settings: ${e.message}", e)
                }
            }
        }
    }
}

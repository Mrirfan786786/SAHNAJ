package com.example.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.example.accessibility.SahNajAccessibilityService
import com.example.services.SahnajNotificationService

data class PermissionStatus(
    val hasMic: Boolean,
    val hasContacts: Boolean,
    val hasPhone: Boolean,
    val hasSms: Boolean,
    val hasNotifications: Boolean,
    val hasAccessibility: Boolean,
    val hasBatteryOptimization: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotificationListener: Boolean = false
) {
    val isCoreReady: Boolean
        get() = hasMic

    val allGranted: Boolean
        get() = hasMic && hasContacts && hasPhone && hasSms && hasNotifications && hasAccessibility && hasBatteryOptimization && hasOverlay && hasNotificationListener
}

class PermissionManager(private val context: Context) {

    fun getPermissionStatus(): PermissionStatus {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val hasContacts = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val hasPhone = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED

        val hasSms = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        val hasAccessibility = SahNajAccessibilityService.isAccessibilityEnabled(context)
        val hasBattery = isIgnoringBatteryOptimizations()
        val hasOverlay = canDrawOverlays()
        val hasNotificationListener = SahnajNotificationService.isNotificationAccessGranted(context)

        return PermissionStatus(
            hasMic = hasMic,
            hasContacts = hasContacts,
            hasPhone = hasPhone,
            hasSms = hasSms,
            hasNotifications = hasNotifications,
            hasAccessibility = hasAccessibility,
            hasBatteryOptimization = hasBattery,
            hasOverlay = hasOverlay,
            hasNotificationListener = hasNotificationListener
        )
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun isAccessibilityEnabled(): Boolean {
        return SahNajAccessibilityService.isAccessibilityEnabled(context)
    }

    fun isNotificationAccessGranted(): Boolean {
        return SahnajNotificationService.isNotificationAccessGranted(context)
    }
}


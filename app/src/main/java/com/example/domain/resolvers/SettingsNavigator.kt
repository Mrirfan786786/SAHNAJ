package com.example.domain.resolvers

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.example.data.model.SettingsType

class SettingsNavigator(private val context: Context) {

    fun openSettings(type: SettingsType): Boolean {
        val action = when (type) {
            SettingsType.WIFI -> Settings.ACTION_WIFI_SETTINGS
            SettingsType.BLUETOOTH -> Settings.ACTION_BLUETOOTH_SETTINGS
            SettingsType.DISPLAY -> Settings.ACTION_DISPLAY_SETTINGS
            SettingsType.SOUND -> Settings.ACTION_SOUND_SETTINGS
            SettingsType.BATTERY -> Settings.ACTION_BATTERY_SAVER_SETTINGS
            SettingsType.LOCATION -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            SettingsType.APPS -> Settings.ACTION_APPLICATION_SETTINGS
            SettingsType.ACCESSIBILITY -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            SettingsType.SECURITY -> Settings.ACTION_SECURITY_SETTINGS
            SettingsType.DATE_TIME -> Settings.ACTION_DATE_SETTINGS
            SettingsType.GENERAL -> Settings.ACTION_SETTINGS
        }

        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    fun parseSettingsType(target: String): SettingsType {
        val upper = target.trim().uppercase()
        return try {
            SettingsType.valueOf(upper)
        } catch (e: Exception) {
            when {
                upper.contains("WIFI") || upper.contains("WI-FI") -> SettingsType.WIFI
                upper.contains("BLUETOOTH") -> SettingsType.BLUETOOTH
                upper.contains("DISPLAY") || upper.contains("SCREEN") -> SettingsType.DISPLAY
                upper.contains("SOUND") || upper.contains("VOLUME") -> SettingsType.SOUND
                upper.contains("BATTERY") -> SettingsType.BATTERY
                upper.contains("LOCATION") || upper.contains("GPS") -> SettingsType.LOCATION
                upper.contains("APP") -> SettingsType.APPS
                upper.contains("ACCESSIBILITY") -> SettingsType.ACCESSIBILITY
                upper.contains("SECURITY") -> SettingsType.SECURITY
                else -> SettingsType.GENERAL
            }
        }
    }
}

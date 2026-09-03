package com.example.domain.resolvers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.model.AppInfo

class AppResolver(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    private val appAliases = mapOf(
        "whatsapp" to listOf("whatsapp", "whatsapp messenger", "wa"),
        "youtube" to listOf("youtube", "yt"),
        "chrome" to listOf("chrome", "google chrome", "browser"),
        "instagram" to listOf("instagram", "insta", "ig"),
        "facebook" to listOf("facebook", "fb"),
        "camera" to listOf("camera", "cam"),
        "gallery" to listOf("gallery", "photos", "google photos"),
        "maps" to listOf("maps", "google maps", "navigation"),
        "gmail" to listOf("gmail", "email", "mail"),
        "calculator" to listOf("calculator", "calc"),
        "clock" to listOf("clock", "alarm", "timer"),
        "spotify" to listOf("spotify", "music"),
        "telegram" to listOf("telegram", "tg"),
        "settings" to listOf("settings", "system settings")
    )

    fun getInstalledLaunchableApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        return resolveInfos.mapNotNull { resolveInfo ->
            val appName = resolveInfo.loadLabel(packageManager).toString()
            val packageName = resolveInfo.activityInfo.packageName
            val className = resolveInfo.activityInfo.name
            AppInfo(appName = appName, packageName = packageName, className = className)
        }
    }

    fun findApp(targetName: String): AppInfo? {
        val cleanTarget = targetName.trim().lowercase()
        val installedApps = getInstalledLaunchableApps()

        // 1. Direct match on installed app label
        val exactMatch = installedApps.find { it.appName.equals(cleanTarget, ignoreCase = true) }
        if (exactMatch != null) return exactMatch

        // 2. Contains match
        val containsMatch = installedApps.find { it.appName.lowercase().contains(cleanTarget) }
        if (containsMatch != null) return containsMatch

        // 3. Match against known aliases
        for ((key, aliases) in appAliases) {
            if (aliases.any { cleanTarget.contains(it) || it.contains(cleanTarget) }) {
                val matchedApp = installedApps.find {
                    it.appName.lowercase().contains(key) || it.packageName.lowercase().contains(key)
                }
                if (matchedApp != null) return matchedApp
            }
        }

        // 4. Match against package name
        val packageMatch = installedApps.find { it.packageName.lowercase().contains(cleanTarget) }
        if (packageMatch != null) return packageMatch

        return null
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}

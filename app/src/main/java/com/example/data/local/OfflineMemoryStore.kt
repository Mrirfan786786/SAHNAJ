package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Autonomous Offline Key-Value Storage for SAHNAJ.
 * Manages zero-API persistent user memory directly via Android SharedPreferences.
 * Stores offline user identity under the key "OFFLINE_USER_NAME".
 */
object OfflineMemoryStore {
    const val PREFS_NAME = "sahnaj_offline_memory"
    const val KEY_OFFLINE_USER_NAME = "OFFLINE_USER_NAME"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Reads the saved user name permanently stored under "OFFLINE_USER_NAME".
     */
    fun getOfflineUserName(context: Context): String? {
        val direct = getPrefs(context).getString(KEY_OFFLINE_USER_NAME, null)?.trim()
        if (!direct.isNullOrBlank()) return direct

        // Also check primary app prefs under "OFFLINE_USER_NAME"
        val mainPrefs = context.getSharedPreferences("sahnaj_prefs", Context.MODE_PRIVATE)
        val mainOffline = mainPrefs.getString(KEY_OFFLINE_USER_NAME, null)?.trim()
        if (!mainOffline.isNullOrBlank()) return mainOffline

        // Fallback to legacy user_display_name if non-default
        val legacy = mainPrefs.getString("user_display_name", null)?.trim()
        return if (!legacy.isNullOrBlank() && !legacy.equals("USER", ignoreCase = true)) legacy else null
    }

    /**
     * Permanently saves the user's name under "OFFLINE_USER_NAME" and syncs across preference stores.
     */
    fun saveOfflineUserName(context: Context, name: String) {
        val clean = name.trim().replace(Regex("""^[.,!?:;]+|[.,!?:;]+$"""), "")
        if (clean.isBlank()) return

        getPrefs(context).edit().putString(KEY_OFFLINE_USER_NAME, clean).apply()

        // Synchronize with main application preferences
        val mainPrefs = context.getSharedPreferences("sahnaj_prefs", Context.MODE_PRIVATE)
        mainPrefs.edit()
            .putString(KEY_OFFLINE_USER_NAME, clean)
            .putString("user_display_name", clean)
            .apply()
    }

    /**
     * Clears the saved offline user name.
     */
    fun clearOfflineUserName(context: Context) {
        getPrefs(context).edit().remove(KEY_OFFLINE_USER_NAME).apply()
        val mainPrefs = context.getSharedPreferences("sahnaj_prefs", Context.MODE_PRIVATE)
        mainPrefs.edit().remove(KEY_OFFLINE_USER_NAME).apply()
    }

    /**
     * Checks whether an offline user name is currently registered.
     */
    fun hasOfflineUserName(context: Context): Boolean {
        return !getOfflineUserName(context).isNullOrBlank()
    }
}

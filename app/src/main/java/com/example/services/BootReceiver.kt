package com.example.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.local.UserPreferences
import com.example.permissions.PermissionManager

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        Log.d(TAG, "BootReceiver triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val userPreferences = UserPreferences(context)
            val permissionManager = PermissionManager(context)
            val permStatus = permissionManager.getPermissionStatus()

            val isSetupDone = userPreferences.isSetupCompleted()
            val isWakeWordEnabled = userPreferences.isWakeWordEnabled()

            Log.d(
                TAG,
                "Boot conditions: isSetupDone=$isSetupDone, isWakeWordEnabled=$isWakeWordEnabled, hasMic=${permStatus.hasMic}"
            )

            if (isSetupDone && isWakeWordEnabled && permStatus.hasMic) {
                Log.d(TAG, "Auto-starting SAHNAJ Foreground Voice Service after device boot...")
                AssistantVoiceService.start(context)
                SahnajDaemonService.start(context)
            } else {
                Log.d(TAG, "Skipping auto-start: Wake-word or mic permission not active")
            }
        }
    }

    companion object {
        private const val TAG = "SAHNAJ_BOOT"
    }
}

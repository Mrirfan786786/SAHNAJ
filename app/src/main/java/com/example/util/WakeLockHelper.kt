package com.example.util

import android.content.Context
import android.os.PowerManager
import android.util.Log

object WakeLockHelper {
    private const val TAG = "SAHNAJ_WAKELOCK"
    private const val WAKE_LOCK_TAG = "sahnaj:voice_assistant_wakelock"
    private var wakeLock: PowerManager.WakeLock? = null

    @Synchronized
    fun acquireWakeLock(context: Context, timeoutMs: Long = 15000L) {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager == null) {
                Log.w(TAG, "PowerManager not available")
                return
            }

            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    WAKE_LOCK_TAG
                ).apply {
                    setReferenceCounted(false)
                }
            }

            if (wakeLock?.isHeld != true) {
                wakeLock?.acquire(timeoutMs)
                Log.d(TAG, "Partial WakeLock ACQUIRED for ${timeoutMs}ms (Prevents CPU sleep during voice processing)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire Partial WakeLock", e)
        }
    }

    @Synchronized
    fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
                Log.d(TAG, "Partial WakeLock RELEASED (CPU power saved)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release Partial WakeLock", e)
        }
    }
}

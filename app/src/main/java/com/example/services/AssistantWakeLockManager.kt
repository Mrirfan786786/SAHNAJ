package com.example.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.util.Log

object AssistantWakeLockManager {
    private const val TAG = "AssistantWakeLock"
    private const val WAKE_LOCK_TAG = "sahnaj:AssistantProcessingWakeLock"

    private var wakeLock: PowerManager.WakeLock? = null
    private val lock = Any()

    /**
     * Acquires a temporary partial wake lock for the specified max duration (defaults to 10 seconds).
     * This keeps the CPU awake during wake-word detection, speech-to-text processing, and action execution.
     */
    @SuppressLint("WakelockTimeout")
    fun acquireWakeLock(context: Context, timeoutMs: Long = 10000L) {
        synchronized(lock) {
            try {
                if (wakeLock == null) {
                    val powerManager = context.applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                    wakeLock = powerManager?.newWakeLock(
                        PowerManager.PARTIAL_WAKE_LOCK,
                        WAKE_LOCK_TAG
                    )?.apply {
                        setReferenceCounted(false)
                    }
                }

                wakeLock?.let {
                    if (it.isHeld) {
                        it.release()
                    }
                    it.acquire(timeoutMs)
                    Log.d(TAG, "Partial WakeLock ACQUIRED with timeout ${timeoutMs}ms")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to acquire WakeLock", e)
            }
        }
    }

    /**
     * Releases the partial wake lock immediately once the command pipeline or TTS finishes.
     */
    fun releaseWakeLock() {
        synchronized(lock) {
            try {
                wakeLock?.let {
                    if (it.isHeld) {
                        it.release()
                        Log.d(TAG, "Partial WakeLock RELEASED cleanly")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing WakeLock", e)
            }
        }
    }
}

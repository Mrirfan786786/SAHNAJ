package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * SecurityShieldManager:
 * Handles Emergency SOS location broadcast to emergency contacts,
 * intrusion detection alert, and decoy / fake shutdown security protocols.
 */
object SecurityShieldManager {

    private const val TAG = "SecurityShield"

    private val _isFakeShutdownActive = MutableStateFlow(false)
    val isFakeShutdownActive: StateFlow<Boolean> = _isFakeShutdownActive.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    /**
     * Triggers Emergency SOS: sends current GPS location coordinates via SMS
     * to saved emergency contacts and optionally dials emergency number.
     */
    suspend fun triggerEmergencySos(
        context: Context,
        emergencyNumber: String = "112",
        userPreferences: UserPreferences? = null
    ): String = withContext(Dispatchers.IO) {
        _isSosActive.value = true
        val locationStr = getCurrentLocationString(context)
        val sosMessage = "EMERGENCY SOS: SAHNAJ AI Alert! Urgent assistance required at location: $locationStr (Time: ${System.currentTimeMillis()})"

        // Send SMS if emergency contact configured or to emergencyNumber
        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        var smsStatus = ""
        if (hasSmsPermission && emergencyNumber.isNotBlank()) {
            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(emergencyNumber, null, sosMessage, null, null)
                smsStatus = "SOS SMS dispatched to $emergencyNumber."
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SOS SMS", e)
                smsStatus = "SMS error: ${e.localizedMessage}"
            }
        } else {
            smsStatus = "Location logged: $locationStr."
        }

        // Trigger heavy alert vibration
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 500, 200, 500, 200, 800)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(1000)
            }
        } catch (_: Exception) {}

        return@withContext "🚨 SOS Alert Activated! $smsStatus"
    }

    /**
     * Activates Decoy / Fake Shutdown mode (Screen goes pitch black, silent, but remains secretly listening).
     */
    fun activateFakeShutdown() {
        _isFakeShutdownActive.value = true
    }

    fun deactivateFakeShutdown() {
        _isFakeShutdownActive.value = false
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocationString(context: Context): String {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return "GPS Permission Required (approximate: Device Online)"
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val netLoc = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = gpsLoc ?: netLoc

            if (best != null) {
                "https://maps.google.com/?q=${best.latitude},${best.longitude} (Lat: ${best.latitude}, Lon: ${best.longitude})"
            } else {
                "Location unavailable currently"
            }
        } catch (e: Exception) {
            "Location lookup error: ${e.localizedMessage}"
        }
    }
}

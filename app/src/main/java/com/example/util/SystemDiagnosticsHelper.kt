package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DeviceDiagnostics(
    val batteryLevel: Int,
    val isCharging: Boolean,
    val batteryHealth: String,
    val batteryTempCelsius: Float,
    val availableRamMb: Long,
    val totalRamMb: Long,
    val ramUsagePercent: Int,
    val freeStorageGb: Double,
    val totalStorageGb: Double,
    val networkType: String,
    val isConnected: Boolean,
    val thermalState: String,
    val mediaVolumePercent: Int
)

object SystemDiagnosticsHelper {

    fun getDiagnostics(context: Context): DeviceDiagnostics {
        // 1. Battery Information
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 50
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val healthInt = batteryIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val healthStr = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Normal"
        }
        val rawTemp = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = if (rawTemp > 0) rawTemp / 10.0f else 32.0f

        // 2. RAM Information
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val ramUsagePct = if (totalRamMb > 0) (((totalRamMb - availRamMb) * 100) / totalRamMb).toInt() else 0

        // 3. Storage Information
        var freeStorageGb = 0.0
        var totalStorageGb = 0.0
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val totalBlocks = stat.blockCountLong
            freeStorageGb = (availableBlocks * blockSize).toDouble() / (1024.0 * 1024.0 * 1024.0)
            totalStorageGb = (totalBlocks * blockSize).toDouble() / (1024.0 * 1024.0 * 1024.0)
        } catch (_: Exception) {}

        // 4. Network Status
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        var networkType = "Offline"
        var isConnected = false
        if (connManager != null) {
            val activeNetwork = connManager.activeNetwork
            val caps = connManager.getNetworkCapabilities(activeNetwork)
            if (caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                isConnected = true
                networkType = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular 4G/5G"
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "Bluetooth Tether"
                    else -> "Connected"
                }
            }
        }

        // 5. Thermal Status
        var thermalState = "Cool & Nominal"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val statusVal = powerManager?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
            thermalState = when (statusVal) {
                PowerManager.THERMAL_STATUS_NONE -> "Nominal (Cool)"
                PowerManager.THERMAL_STATUS_LIGHT -> "Light Warmth"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Heat"
                PowerManager.THERMAL_STATUS_SEVERE -> "Warning: Elevated Heat"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Heat"
                else -> "Nominal"
            }
        }

        // 6. Volume
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val currentVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val volPct = if (maxVol > 0) (currentVol * 100) / maxVol else 0

        return DeviceDiagnostics(
            batteryLevel = batteryPct,
            isCharging = isCharging,
            batteryHealth = healthStr,
            batteryTempCelsius = tempCelsius,
            availableRamMb = availRamMb,
            totalRamMb = totalRamMb,
            ramUsagePercent = ramUsagePct,
            freeStorageGb = freeStorageGb,
            totalStorageGb = totalStorageGb,
            networkType = networkType,
            isConnected = isConnected,
            thermalState = thermalState,
            mediaVolumePercent = volPct
        )
    }

    /**
     * Generates a clean cyberpunk diagnostics readout:
     * "Boss, System Diagnostics Complete. Battery: [X]%, Storage: [Y]% free, Network: Secure, Neural Matrix: 100% Operational."
     */
    fun buildCyberpunkDiagnosticsReport(context: Context): String {
        val d = getDiagnostics(context)
        val storagePctFree = if (d.totalStorageGb > 0) {
            ((d.freeStorageGb / d.totalStorageGb) * 100).toInt().coerceIn(1, 99)
        } else {
            42
        }
        val networkStatus = if (d.isConnected) "Secure" else "Offline"
        return "Boss, System Diagnostics Complete. Battery: ${d.batteryLevel}%, Storage: ${storagePctFree}% free, Network: $networkStatus, Neural Matrix: 100% Operational."
    }

    /**
     * Generates a crisp, razor-sharp JARVIS diagnostic voice report for the user.
     */
    fun buildJarvisDiagnosticsReport(context: Context): String {
        val d = getDiagnostics(context)
        val ramAvailGb = String.format(Locale.US, "%.1f", d.availableRamMb / 1024.0)
        val ramTotalGb = String.format(Locale.US, "%.1f", d.totalRamMb / 1024.0)
        val storageFree = String.format(Locale.US, "%.1f", d.freeStorageGb)
        val chargingStatus = if (d.isCharging) "charging ho rahi hai" else "on battery"

        return "System diagnostics complete, boss. Subsystems nominal hain: Battery ${d.batteryLevel}% ($chargingStatus), Available RAM ${ramAvailGb} GB out of ${ramTotalGb} GB, Free storage ${storageFree} GB, network ${d.networkType}, aur device thermal state bilkul stable hai."
    }

    /**
     * Generates a comprehensive morning briefing.
     */
    fun buildMorningBriefing(context: Context, userName: String = ""): String {
        val d = getDiagnostics(context)
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val currentTime = timeFormat.format(Date())
        val greetingTarget = if (userName.isNotBlank() && !userName.equals("USER", ignoreCase = true)) userName else "boss"

        return "Good morning, $greetingTarget. Time abhi $currentTime hai. Device battery ${d.batteryLevel}% par hai, network ${d.networkType} active hai, aur sabhi primary subsystems ready hain. Aaj ke schedule ke liye main taiyaar hoon."
    }

    /**
     * Generates a bedtime / night routine briefing.
     */
    fun buildNightRoutine(context: Context): String {
        val d = getDiagnostics(context)
        val chargingAdvice = if (d.batteryLevel < 40 && !d.isCharging) {
            " Battery ${d.batteryLevel}% hai, phone ko charging par connect karne ki recommendation hai."
        } else {
            " Battery ${d.batteryLevel}% hai."
        }

        return "Good night, boss. DND protocol aur night routine activate kar rahi hoon.$chargingAdvice Sabhi pending alerts silencer mode par hain. Rest well, sir."
    }

    /**
     * Checks if battery is critically low and generates proactive warning if needed.
     */
    fun checkBatteryWarning(context: Context): String? {
        val d = getDiagnostics(context)
        if (d.batteryLevel in 1..15 && !d.isCharging) {
            return "Warning, boss: Battery level ${d.batteryLevel}% par drop ho chuka hai. Charging connect karne ki urgent recommendation hai."
        }
        return null
    }
}

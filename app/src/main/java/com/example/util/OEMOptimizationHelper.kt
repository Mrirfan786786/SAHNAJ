package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

data class OEMInstruction(
    val manufacturer: String,
    val title: String,
    val description: String,
    val steps: List<String>,
    val buttonLabel: String
)

object OEMOptimizationHelper {
    private const val TAG = "SAHNAJ_OEM"

    fun getManufacturer(): String {
        return Build.MANUFACTURER.uppercase()
    }

    fun isRestrictedOEM(): Boolean {
        val m = Build.MANUFACTURER.lowercase()
        return m.contains("xiaomi") ||
                m.contains("redmi") ||
                m.contains("poco") ||
                m.contains("oppo") ||
                m.contains("vivo") ||
                m.contains("realme") ||
                m.contains("samsung") ||
                m.contains("oneplus") ||
                m.contains("huawei") ||
                m.contains("honor")
    }

    fun getOEMInstructions(): OEMInstruction {
        val m = Build.MANUFACTURER.lowercase()
        return when {
            m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> {
                OEMInstruction(
                    manufacturer = "XIAOMI / MIUI / HYPEROS",
                    title = "ENABLE AUTOSTART & UNRESTRICTED BATTERY",
                    description = "MIUI / HyperOS stops background services unless explicitly permitted in Autostart.",
                    steps = listOf(
                        "1. Enable 'Autostart' toggle for सहनाज AI.",
                        "2. Go to 'Battery Saver' -> Select 'No Restrictions'.",
                        "3. Lock सहनाज in Recent Apps tray if needed."
                    ),
                    buttonLabel = "OPEN MIUI AUTOSTART SETTINGS"
                )
            }
            m.contains("oppo") || m.contains("realme") -> {
                OEMInstruction(
                    manufacturer = "OPPO / REALME / COLOROS",
                    title = "ALLOW BACKGROUND STARTUP",
                    description = "ColorOS / Realme UI requires background launch permission for voice wake-up.",
                    steps = listOf(
                        "1. Enable 'Allow Auto-startup' and 'Allow Background Startup'.",
                        "2. In Battery settings, disable 'Freeze in Background'.",
                        "3. Set Power Consumption Protection to 'Don't Optimize'."
                    ),
                    buttonLabel = "OPEN STARTUP MANAGER"
                )
            }
            m.contains("vivo") || m.contains("iqoo") -> {
                OEMInstruction(
                    manufacturer = "VIVO / IQOO / FUNTOUCH OS",
                    title = "ALLOW HIGH BACKGROUND POWER USAGE",
                    description = "Funtouch OS / Origin OS puts background services to sleep unless allowed.",
                    steps = listOf(
                        "1. Open 'Background App Management' / 'Autostart'.",
                        "2. Allow सहनाज AI in 'High Background Power Consumption'.",
                        "3. Keep सहनाज active in RAM."
                    ),
                    buttonLabel = "OPEN VIVO POWER SETTINGS"
                )
            }
            m.contains("samsung") -> {
                OEMInstruction(
                    manufacturer = "SAMSUNG / ONE UI",
                    title = "EXCLUDE FROM NEVER SLEEPING APPS",
                    description = "One UI automatically puts unused background apps into deep sleep.",
                    steps = listOf(
                        "1. Go to 'Battery' -> 'Background usage limits'.",
                        "2. Add सहनाज AI to 'Never Sleeping Apps'.",
                        "3. Set Battery mode to 'Unrestricted'."
                    ),
                    buttonLabel = "OPEN ONE UI BATTERY SETTINGS"
                )
            }
            m.contains("oneplus") -> {
                OEMInstruction(
                    manufacturer = "ONEPLUS / OXYGENOS",
                    title = "ALLOW AUTO-LAUNCH & RUN IN BACKGROUND",
                    description = "OxygenOS optimizes background apps unless auto-launch is granted.",
                    steps = listOf(
                        "1. Go to App Management -> सहनाज AI -> Auto-launch.",
                        "2. Turn on 'Allow background activities'.",
                        "3. Set Battery Optimization to 'Don't Optimize'."
                    ),
                    buttonLabel = "OPEN AUTO-LAUNCH SETTINGS"
                )
            }
            m.contains("huawei") || m.contains("honor") -> {
                OEMInstruction(
                    manufacturer = "HUAWEI / HONOR / EMUI",
                    title = "MANUAL APP LAUNCH MANAGEMENT",
                    description = "EMUI manages apps automatically unless set to manual management.",
                    steps = listOf(
                        "1. Go to 'App Launch' / 'Battery'.",
                        "2. Find सहनाज AI and toggle off 'Manage Automatically'.",
                        "3. Enable: Auto-launch, Secondary launch, and Run in background."
                    ),
                    buttonLabel = "OPEN APP LAUNCH SETTINGS"
                )
            }
            else -> {
                OEMInstruction(
                    manufacturer = getManufacturer(),
                    title = "ENABLE UNRESTRICTED BACKGROUND EXECUTION",
                    description = "Ensure Android does not throttle or kill सहनाज in the background.",
                    steps = listOf(
                        "1. Set Battery Usage to 'Unrestricted' / 'Don't Optimize'.",
                        "2. Allow background data and microphone access.",
                        "3. Ensure battery saver does not restrict background services."
                    ),
                    buttonLabel = "OPEN APP INFO SETTINGS"
                )
            }
        }
    }

    /**
     * Attempts to open OEM-specific Autostart / Battery manager settings screens.
     */
    fun openOEMAutostartSettings(context: Context): Boolean {
        val intents = listOf(
            // Xiaomi
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.powercenter.PowerSettings")),
            // Oppo / Realme
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            Intent().setComponent(ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelga设置.PowerConsumptionActivity")),
            // Vivo / iQOO
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.MainGuideActivity")),
            // Samsung
            Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            Intent().setComponent(ComponentName("com.samsung.android.sm", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            // OnePlus
            Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListAct")),
            // Huawei / Honor
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity"))
        )

        for (intent in intents) {
            try {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Log.d(TAG, "Successfully launched OEM intent: ${intent.component}")
                return true
            } catch (e: Exception) {
                // Ignore and try next
            }
        }

        // Fallback to app details or battery optimization settings
        return try {
            val appInfoIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appInfoIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed fallback to app info settings", e)
            false
        }
    }
}

package com.example.util

import android.content.Context
import android.graphics.Bitmap
import com.example.BuildConfig
import com.example.data.local.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * AutomotiveDiagnosticsEngine:
 * Specialized automotive mechanical, electrical, DTC/OBD-II fault code analyzer,
 * and multimodal camera diagnostics engine.
 */
object AutomotiveDiagnosticsEngine {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val COMMON_DTC_DATABASE = mapOf(
        "P0300" to "Random/Multiple Cylinder Misfire Detected. Spark plugs, ignition coils, fuel injector clog ya vacuum leak check karein.",
        "P0301" to "Cylinder 1 Misfire Detected. Spark plug 1, coil pack 1, ya fuel injector 1 inspect karein.",
        "P0302" to "Cylinder 2 Misfire Detected. Spark plug 2, coil pack 2, ya compression check karein.",
        "P0303" to "Cylinder 3 Misfire Detected. Cylinder 3 ignition coil, spark plug check karein.",
        "P0304" to "Cylinder 4 Misfire Detected. Cylinder 4 ignition coil, spark plug ya fuel delivery check karein.",
        "P0171" to "System Too Lean (Bank 1). MAF (Mass Air Flow) sensor ganda ho sakta hai, vacuum leak ya fuel pump weak ho sakta hai.",
        "P0172" to "System Too Rich (Bank 1). Leaking fuel injector, faulty O2 sensor, ya dirty air filter.",
        "P0420" to "Catalytic Converter System Efficiency Below Threshold (Bank 1). O2 oxygen sensors ya catalytic converter degrade ho chuka hai.",
        "P0113" to "Intake Air Temperature (IAT) Sensor Circuit High. Sensor wire loose ya sensor faulty hai.",
        "P0128" to "Coolant Thermostat (Coolant Temp Below Regulating Temp). Thermostat valve open stuck hai ya coolant temperature sensor faulty hai.",
        "P0500" to "Vehicle Speed Sensor (VSS) Malfunction. Speedometer reading issue, wiring harness check karein.",
        "P0700" to "Transmission Control System Malfunction. TCM fault code ya transmission fluid level check karein.",
        "U0100" to "Lost Communication With ECM/PCM. CAN bus wiring, fuse, ya battery voltage issue.",
        "C0035" to "Left Front Wheel Speed Circuit (ABS). ABS wheel speed sensor ya magnetic ring damaged ho sakti hai."
    )

    /**
     * Looks up OBD-II / DTC code locally first for immediate 0ms answer.
     */
    fun lookupLocalDtcCode(code: String): String? {
        val cleanCode = code.trim().uppercase()
        return COMMON_DTC_DATABASE[cleanCode]
    }

    /**
     * Deep Diagnostic Analyzer for any vehicle issue or fault code.
     */
    suspend fun analyzeAutomotiveIssue(
        query: String,
        vehicleModel: String = "",
        userPreferences: UserPreferences? = null
    ): String = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()

        // Check local DTC database first
        val dtcRegex = Regex("\\b([PBCU][0-9]{4})\\b", RegexOption.IGNORE_CASE)
        val match = dtcRegex.find(cleanQuery)
        val localDtcDesc = match?.let { lookupLocalDtcCode(it.value) }

        val keyFromPref = userPreferences?.securePreferences?.getGeminiApiKey() ?: ""
        val apiKey = if (keyFromPref.isNotBlank()) keyFromPref else BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext if (localDtcDesc != null) {
                "🚗 **DTC Fault Analysis (${match!!.value.uppercase()})**\n\n$localDtcDesc\n\n*(Note: Cloud AI key configure karein for full step-by-step repair guide)*"
            } else {
                "🚗 **Automotive Troubleshooting**\n\nProblem: $cleanQuery\n\nPrathmik jaanch:\n1. Battery terminal aur voltage check karein (12.4V - 12.8V).\n2. Engine oil, coolant aur brake fluid level inspect karein.\n3. Fuse box aur OBD-II scanner se fault codes read karein."
            }
        }

        val prompt = """
            You are SAHNAJ Automotive & Mechanical Master Diagnostics Specialist.
            Vehicle Model/Type: ${vehicleModel.ifBlank { "Generic Automobile / Motorcycle" }}
            User Query / Symptoms / Fault Code: "$cleanQuery"

            Provide a master technician diagnostic report formatted in clean Markdown with:
            1. 🔧 **Problem Summary & Probable Root Cause**
            2. 📋 **DTC / Error Code Interpretation** (if fault code mentioned)
            3. 🛠️ **Step-by-Step Diagnostic & Repair Procedure** (Tools needed, component inspection, safety precautions)
            4. ⚠️ **Urgency & Driving Risk** (Safe to drive vs. Immediate stop)
            5. 💡 **Cost Estimate & Recommended Parts**
            
            Keep the tone professional, razor-sharp, and clear in easy-to-understand Hinglish/English.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val respJson = JSONObject(respStr)
                val candidates = respJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: ""
                if (text.isNotBlank()) return@withContext text
            }
        } catch (e: Exception) {
            android.util.Log.e("AutoDiagnostics", "AI analysis failed", e)
        }

        return@withContext if (localDtcDesc != null) {
            "🚗 **DTC Fault Analysis (${match!!.value.uppercase()})**\n\n$localDtcDesc"
        } else {
            "🚗 **Automotive Diagnostics Engine**\n\nSymptom: $cleanQuery\n\n• Spark plugs, ignition coils aur fuel delivery check karein.\n• Battery voltage aur wiring harness terminals verify karein.\n• OBD-II port se live sensor telemetry scan karein."
        }
    }

    /**
     * Multimodal Image Analysis for Engine Bay, Mechanical Parts, Wiring, or OBD Scanners.
     */
    suspend fun analyzeMechanicalImage(
        bitmap: Bitmap,
        contextPrompt: String = "",
        userPreferences: UserPreferences? = null
    ): String = withContext(Dispatchers.IO) {
        val keyFromPref = userPreferences?.securePreferences?.getGeminiApiKey() ?: ""
        val apiKey = if (keyFromPref.isNotBlank()) keyFromPref else BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank()) {
            return@withContext "⚠️ Gemini API Key required for multimodal visual automotive diagnostics. Settings mein key save karein."
        }

        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
        val base64Image = android.util.Base64.encodeToString(bos.toByteArray(), android.util.Base64.NO_WRAP)

        val prompt = """
            You are SAHNAJ Automotive & Mechanical Master AI Specialist.
            Analyze this automotive/mechanical photo (engine bay, mechanical part, electrical wiring, tire/brake assembly, or OBD-II scanner display).
            ${if (contextPrompt.isNotBlank()) "User Note: $contextPrompt" else ""}

            Provide an expert visual diagnostic breakdown in structured Markdown:
            1. 🔍 **Visual Component Identification**: Identify exact part, component, assembly, or fault code visible.
            2. 🛠️ **Condition Assessment**: Identify wear, corrosion, leakages (oil/coolant), loose connectors, broken belts, or abnormal burn marks.
            3. 📋 **Actionable Repair / Replacement Steps**: Step-by-step mechanical guide to test, remove, or replace the part.
            4. ⚠️ **Safety & Tools Required**: PPE, wrench sizes, torque specifications (if standard), and safety warnings.
            
            Keep the response authoritative, structured, and easy to follow.
        """.trimIndent()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inline_data", JSONObject().apply {
                                    put("mime_type", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val respJson = JSONObject(respStr)
                val candidates = respJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text") ?: ""
                if (text.isNotBlank()) return@withContext text
            }
        } catch (e: Exception) {
            android.util.Log.e("AutoDiagnostics", "Visual mechanical analysis failed", e)
            return@withContext "❌ Mechanical visual scan failed: ${e.localizedMessage}"
        }

        return@withContext "⚠️ Visual diagnostics completed without details. Please retake a clear photo with proper lighting."
    }
}

package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Base64
import android.util.Log
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.BuildConfig
import com.example.data.local.SecurePreferences
import com.example.data.local.UserPreferences
import com.example.data.model.ScanMode
import com.example.data.model.ScannedDocumentItem
import com.example.data.model.TargetLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class VisionScannerRepository(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val securePreferences: SecurePreferences
) {
    companion object {
        private const val TAG = "VisionScannerRepo"
        private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"
        
        // Priority list of Gemini vision models
        private val VISION_MODELS = listOf(
            "gemini-3.5-flash",
            "gemini-flash-latest",
            "gemini-2.5-flash",
            "gemini-3.1-pro-preview"
        )
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private val _recentScans = MutableStateFlow<List<ScannedDocumentItem>>(emptyList())
    val recentScans: StateFlow<List<ScannedDocumentItem>> = _recentScans.asStateFlow()

    fun getGeminiApiKey(): String {
        val prefKey = userPreferences.getGeminiApiKey().trim()
        if (prefKey.isNotBlank()) return prefKey

        val secureKey = securePreferences.getProviderApiKey("gemini").trim()
        if (secureKey.isNotBlank()) return secureKey

        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
    }

    suspend fun scanDocument(
        imageUri: Uri?,
        bitmap: Bitmap?,
        scanMode: ScanMode,
        targetLanguage: TargetLanguage = TargetLanguage.HINDI,
        customInstructions: String = "",
        onProgressUpdate: (Float, String) -> Unit = { _, _ -> }
    ): Result<ScannedDocumentItem> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val apiKey = getGeminiApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API Key is missing. Please configure your key in Settings > API & Cloud Settings.")
                )
            }

            onProgressUpdate(0.15f, "Preparing document & optimizing image quality...")

            // Convert image or PDF to base64 & MIME type
            val (base64Data, mimeType, cachedFilePath) = prepareImageData(imageUri, bitmap)
                ?: return@withContext Result.failure(IllegalArgumentException("No valid image or document provided for scanning."))

            onProgressUpdate(0.35f, "Formulating neural prompt for ${scanMode.title}...")

            val systemPrompt = buildScanPrompt(scanMode, targetLanguage, customInstructions)

            onProgressUpdate(0.55f, "Sending payload to Gemini Vision AI Core...")

            var lastError: Exception? = null
            for (model in VISION_MODELS) {
                try {
                    val result = callGeminiVisionApi(apiKey, model, mimeType, base64Data, systemPrompt)
                    if (result.isSuccess) {
                        val markdownText = result.getOrThrow()
                        onProgressUpdate(0.90f, "Structuring Markdown tables & extracting line items...")

                        val lineCount = countExtractedItems(markdownText)
                        val total = extractEstimatedTotal(markdownText)

                        val scannedItem = ScannedDocumentItem(
                            scanMode = scanMode,
                            title = "${scanMode.title} - ${formatTime(startTime)}",
                            imagePath = cachedFilePath,
                            markdownResult = markdownText,
                            targetLanguage = targetLanguage,
                            timestamp = System.currentTimeMillis(),
                            durationMs = System.currentTimeMillis() - startTime,
                            estimatedTotal = total,
                            lineItemCount = lineCount
                        )

                        // Update in-memory history
                        val updated = listOf(scannedItem) + _recentScans.value.take(20)
                        _recentScans.value = updated

                        onProgressUpdate(1.0f, "Scan Complete! ⚡")
                        return@withContext Result.success(scannedItem)
                    } else {
                        lastError = result.exceptionOrNull() as? Exception
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Model $model failed, attempting fallback...", e)
                    lastError = e
                }
            }

            Result.failure(lastError ?: Exception("Gemini Vision failed to process the document. Please verify your API key and image clarity."))
        } catch (e: Exception) {
            Log.e(TAG, "Vision scan failed", e)
            Result.failure(Exception(e.message ?: "Document OCR & Extraction failed."))
        }
    }

    private fun prepareImageData(imageUri: Uri?, bitmap: Bitmap?): Triple<String, String, String?>? {
        try {
            if (bitmap != null) {
                val optimizedBitmap = scaleBitmapDown(bitmap, 1600)
                val stream = ByteArrayOutputStream()
                optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val bytes = stream.toByteArray()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                val cacheFile = saveBitmapToCache(optimizedBitmap)
                return Triple(base64, "image/jpeg", cacheFile?.absolutePath)
            }

            if (imageUri != null) {
                val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"

                if (mimeType == "application/pdf") {
                    val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    return Triple(base64, "application/pdf", null)
                } else {
                    // Image Uri
                    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (originalBitmap != null) {
                        val optimizedBitmap = scaleBitmapDown(originalBitmap, 1600)
                        val stream = ByteArrayOutputStream()
                        optimizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                        val bytes = stream.toByteArray()
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                        val cacheFile = saveBitmapToCache(optimizedBitmap)
                        return Triple(base64, "image/jpeg", cacheFile?.absolutePath)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare image data", e)
        }
        return null
    }

    private fun scaleBitmapDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        var resizedWidth = originalWidth
        var resizedHeight = originalHeight

        if (originalHeight > maxDimension || originalWidth > maxDimension) {
            if (originalHeight > originalWidth) {
                resizedHeight = maxDimension
                resizedWidth = ((resizedHeight.toFloat() / originalHeight.toFloat()) * originalWidth).toInt()
            } else {
                resizedWidth = maxDimension
                resizedHeight = ((resizedWidth.toFloat() / originalWidth.toFloat()) * originalHeight).toInt()
            }
            return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, true)
        }
        return bitmap
    }

    private fun saveBitmapToCache(bitmap: Bitmap): File? {
        return try {
            val dir = File(context.cacheDir, "scanned_docs").apply { mkdirs() }
            val file = File(dir, "doc_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun buildScanPrompt(
        scanMode: ScanMode,
        targetLanguage: TargetLanguage,
        customInstructions: String
    ): String {
        return """
            You are SAHNAJ AI's specialized Vision OCR & Document Intelligence Core.
            Target Language for explanation / translation: ${targetLanguage.displayName} (${targetLanguage.promptName}).
            
            ${scanMode.promptInstruction}
            
            ${if (customInstructions.isNotBlank()) "USER CUSTOM NOTE: $customInstructions" else ""}
            
            CRITICAL FORMATTING INSTRUCTIONS:
            - Provide clear, high-contrast Markdown formatting.
            - Always use neat Markdown tables with pipe dividers for itemized or tabular data.
            - Highlight financial totals, taxes, part codes, and important dates with bold text (**₹XX,XXX**).
            - If text in the document is in Hindi, Urdu, or English, retain exact numbers and nomenclature while providing clean bilingual understanding where beneficial.
            - Do not invent non-existent details; if something is faded or illegible, mark it as [Unclear / अस्पष्ट].
        """.trimIndent()
    }

    private suspend fun callGeminiVisionApi(
        apiKey: String,
        model: String,
        mimeType: String,
        base64Data: String,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "$GEMINI_BASE_URL/$model:generateContent?key=$apiKey"

            val partsArray = JSONArray().apply {
                // Inline Image / PDF Part
                put(JSONObject().apply {
                    put("inline_data", JSONObject().apply {
                        put("mime_type", mimeType)
                        put("data", base64Data)
                    })
                })
                // Text prompt part
                put(JSONObject().apply {
                    put("text", prompt)
                })
            }

            val contentObject = JSONObject().apply {
                put("role", "user")
                put("parts", partsArray)
            }

            val contentsArray = JSONArray().apply {
                put(contentObject)
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.15)
                    put("maxOutputTokens", 4096)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                response.close()
                return@withContext Result.failure(Exception("Gemini API Error (${response.code}): $responseBody"))
            }
            response.close()

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val extractedText = parts.getJSONObject(0).optString("text", "")
                    if (extractedText.isNotBlank()) {
                        return@withContext Result.success(extractedText)
                    }
                }
            }

            Result.failure(Exception("No content generated in Gemini Vision response"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun countExtractedItems(markdown: String): Int {
        val lines = markdown.lines()
        var tableRowCount = 0
        lines.forEach { line ->
            if (line.trim().startsWith("|") && !line.contains("---") && !line.contains("S.No", ignoreCase = true) && !line.contains("Item", ignoreCase = true)) {
                tableRowCount++
            }
        }
        return tableRowCount.coerceAtLeast(1)
    }

    private fun extractEstimatedTotal(markdown: String): String? {
        val regex = Regex("(?i)(?:Grand Total|Total Amount|Total Estimate|Total|कुल योग|Grand Total:?)\\s*[:\\-]?\\s*([₹$€£]?\\s*[0-9,]+(?:\\.[0-9]{2})?)")
        val match = regex.find(markdown)
        return match?.groupValues?.get(1)?.trim()
    }

    private fun formatTime(timeMs: Long): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a, dd MMM", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timeMs))
    }

    // ================= EXPORT AS PDF / PRINT =================
    fun printOrExportPdf(item: ScannedDocumentItem, activityContext: Context) {
        try {
            val htmlContent = convertMarkdownToHtml(item)
            val webView = WebView(activityContext)
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = activityContext.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val printAdapter = webView.createPrintDocumentAdapter("SAHNAJ_Scan_${item.scanMode.name}_${System.currentTimeMillis()}")
                    printManager?.print(
                        "SAHNAJ AI Document - ${item.scanMode.title}",
                        printAdapter,
                        PrintAttributes.Builder().build()
                    )
                }

                override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                    try {
                        view?.destroy()
                    } catch (_: Exception) {}
                    return true
                }
            }
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Log.e(TAG, "Print failed", e)
        }
    }

    // ================= SHARE VIA WHATSAPP =================
    fun shareViaWhatsApp(item: ScannedDocumentItem, context: Context) {
        try {
            val shareText = """
                *📄 SAHNAJ AI Vision Scanner - ${item.scanMode.title}*
                
                ${item.markdownResult}
                
                _Extracted seamlessly by SAHNAJ AI Vision Core_
            """.trimIndent()

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            try {
                context.startActivity(whatsappIntent)
            } catch (_: Exception) {
                // Fallback to normal share chooser if WhatsApp not installed
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_SUBJECT, "SAHNAJ AI Scanned Document - ${item.scanMode.title}")
                }
                context.startActivity(Intent.createChooser(generalIntent, "Share Scanned Document"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Share via WhatsApp failed", e)
        }
    }

    private fun convertMarkdownToHtml(item: ScannedDocumentItem): String {
        val lines = item.markdownResult.lines()
        val htmlBody = StringBuilder()

        htmlBody.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 24px; color: #111; line-height: 1.6; }
                    h1 { color: #D32F2F; border-bottom: 2px solid #D32F2F; padding-bottom: 8px; font-size: 22px; }
                    h2, h3 { color: #333; margin-top: 18px; font-size: 16px; }
                    table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 13px; }
                    th, td { border: 1px solid #ccc; padding: 8px 12px; text-align: left; }
                    th { background-color: #f5f5f5; font-weight: bold; color: #222; }
                    tr:nth-child(even) { background-color: #fafafa; }
                    .badge { background: #fee2e2; color: #991b1b; padding: 4px 8px; border-radius: 4px; font-weight: bold; font-size: 12px; display: inline-block; }
                    .footer { margin-top: 30px; font-size: 11px; color: #777; border-top: 1px solid #eee; padding-top: 10px; }
                </style>
            </head>
            <body>
                <span class="badge">SAHNAJ AI VISION EXTRACT</span>
                <h1>${item.scanMode.title}</h1>
                <p><strong>Scan Mode:</strong> ${item.scanMode.title} | <strong>Language:</strong> ${item.targetLanguage.displayName}</p>
        """.trimIndent())

        var inTable = false
        var isHeaderRow = true

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                if (!inTable) {
                    htmlBody.append("<table>")
                    inTable = true
                    isHeaderRow = true
                }

                if (trimmed.contains("---")) {
                    isHeaderRow = false
                    continue
                }

                val cols = trimmed.split("|").filter { it.isNotBlank() }.map { it.trim() }
                htmlBody.append("<tr>")
                for (col in cols) {
                    if (isHeaderRow) {
                        htmlBody.append("<th>").append(col).append("</th>")
                    } else {
                        htmlBody.append("<td>").append(col).append("</td>")
                    }
                }
                htmlBody.append("</tr>")
            } else {
                if (inTable) {
                    htmlBody.append("</table>")
                    inTable = false
                }
                if (trimmed.startsWith("# ")) {
                    htmlBody.append("<h1>").append(trimmed.removePrefix("# ")).append("</h1>")
                } else if (trimmed.startsWith("## ")) {
                    htmlBody.append("<h2>").append(trimmed.removePrefix("## ")).append("</h2>")
                } else if (trimmed.startsWith("### ")) {
                    htmlBody.append("<h3>").append(trimmed.removePrefix("### ")).append("</h3>")
                } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                    htmlBody.append("<li>").append(trimmed.substring(2)).append("</li>")
                } else if (trimmed.isNotBlank()) {
                    htmlBody.append("<p>").append(trimmed).append("</p>")
                }
            }
        }
        if (inTable) {
            htmlBody.append("</table>")
        }

        htmlBody.append("""
                <div class="footer">
                    Generated by SAHNAJ AI Neural Vision & OCR Studio • Powered by Gemini AI
                </div>
            </body>
            </html>
        """.trimIndent())

        return htmlBody.toString()
    }
}

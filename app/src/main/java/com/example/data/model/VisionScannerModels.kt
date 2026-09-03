package com.example.data.model

import java.util.UUID

enum class ScanMode(
    val title: String,
    val hindiTitle: String,
    val emoji: String,
    val description: String,
    val promptInstruction: String
) {
    AUTOMOBILE_PARTS(
        title = "Automobile Parts & Estimate",
        hindiTitle = "ऑटोमोबाइल स्पेयर पार्ट्स एवं एस्टीमेट",
        emoji = "🚗",
        description = "Extracts part names, part numbers, OEM codes, quantity, unit rates, and estimated total prices into a clean structured table.",
        promptInstruction = """
            Analyze this image of automobile parts, spare parts catalogue, or mechanic estimate.
            Extract all parts and billing details with extreme precision.
            
            Format your response strictly using:
            1. An executive summary header with Vehicle Make/Model (if detectable) and Estimate Total.
            2. A Markdown Table with columns:
               | S.No | Part Name / Item Description | Part / OEM Number | Qty | Unit Price | Total Price | Notes |
            3. A summary breakdown listing Subtotal, Estimated Labor/Fitting charges, Taxes (GST/VAT), and Grand Total.
            4. Critical mechanics notes or safety recommendations if applicable.
        """.trimIndent()
    ),

    BILL_RECEIPT(
        title = "Bill & Receipt OCR",
        hindiTitle = "बिल एवं रसीद ओसीआर",
        emoji = "🧾",
        description = "Extracts vendor name, invoice number, date, line items, taxes, discounts, and final total amount.",
        promptInstruction = """
            Analyze this bill, invoice, tax receipt, or payment voucher with high OCR precision.
            
            Format your response strictly using:
            1. Invoice Header block: Vendor/Store Name, Invoice/Bill #, Date, GSTIN/Tax ID, Payment Mode.
            2. A Markdown Table for all purchased items:
               | S.No | Item Description | HSN/SKU | Qty | Rate | Amount |
            3. Tax & Financial Breakdown: Subtotal, Discount, CGST/SGST/Tax, and Grand Total Amount.
            4. Key notes or payment status (Paid / Pending).
        """.trimIndent()
    ),

    HANDWRITING_NOTES(
        title = "Handwriting & Notes to Text",
        hindiTitle = "हस्तलिखित नोट्स से डिजिटल टेक्स्ट",
        emoji = "✍️",
        description = "Digitizes handwritten prescriptions, student notes, diagrams, meeting scribbles, and sketches into clean text.",
        promptInstruction = """
            Transcribe all handwritten notes, scribbles, cursive text, or chalkboard/whiteboard contents in this image.
            
            Format your response strictly using:
            1. Clean, digitized Markdown formatting with logical headings, bullet points, and organized paragraphs.
            2. If medical prescription: list Doctor details, Patient name/date, Medicines (Dosage, Frequency, Timing - Before/After food), and Advice.
            3. If study notes: organize key concepts, definitions, and equations.
            4. Note any words that were partially obscured or ambiguous with [ambiguous: ...].
        """.trimIndent()
    ),

    TRANSLATE_SUMMARIZE(
        title = "Translate & Summarize",
        hindiTitle = "दस्तावेज़ अनुवाद एवं सार संक्षेप",
        emoji = "🌐",
        description = "Extracts text from documents and provides translations and summaries in Hindi, Urdu, and English.",
        promptInstruction = """
            Analyze this document, sign, letter, notice, or screenshot.
            
            Format your response strictly using:
            1. Executive 3-Bullet Summary (Key Takeaways).
            2. Full Extracted Text Translation in the requested target language (Hindi / Urdu / English).
            3. Original Transcribed Text.
            4. Important Action Items / Deadlines mentioned in the document.
        """.trimIndent()
    )
}

enum class TargetLanguage(val displayName: String, val promptName: String, val flag: String) {
    HINDI("Hindi (हिन्दी)", "Hindi", "🇮🇳"),
    URDU("Urdu (اردو)", "Urdu", "🇵🇰"),
    ENGLISH("English", "English", "🌐"),
    HINGLISH("Hinglish", "conversational Hinglish", "⚡")
}

data class ScannedDocumentItem(
    val id: String = UUID.randomUUID().toString(),
    val scanMode: ScanMode,
    val title: String,
    val imagePath: String? = null,
    val markdownResult: String,
    val targetLanguage: TargetLanguage = TargetLanguage.HINDI,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val estimatedTotal: String? = null,
    val lineItemCount: Int = 0
)

sealed class VisionScannerState {
    data object Idle : VisionScannerState()
    data class Scanning(val progress: Float, val stage: String) : VisionScannerState()
    data class Success(val item: ScannedDocumentItem) : VisionScannerState()
    data class Error(val message: String) : VisionScannerState()
}

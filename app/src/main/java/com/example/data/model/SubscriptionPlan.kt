package com.example.data.model

data class SubscriptionPlan(
    val planId: String,
    val displayName: String,
    val price: Long,
    val currency: String = "INR",
    val durationDays: Int, // 30, 90, 365, -1
    val tagline: String = "",
    val features: List<String> = emptyList(),
    val isActive: Boolean = true
) {
    val isLifetime: Boolean get() = durationDays == -1 || planId.lowercase().contains("lifetime")
    val isYearly: Boolean get() = durationDays == 365 || planId.lowercase().contains("year")
    val isQuarterly: Boolean get() = durationDays == 90 || planId.lowercase().contains("3month")
    val isMonthly: Boolean get() = durationDays in 28..31 || planId.lowercase().contains("month")

    val priceDisplay: String get() {
        val formattedNumber = "%,d".format(price)
        return when {
            isLifetime -> "₹$formattedNumber one-time"
            isYearly -> "₹$formattedNumber/year"
            isQuarterly -> "₹$formattedNumber/3 months"
            isMonthly -> "₹$formattedNumber/month"
            else -> "₹$formattedNumber for $durationDays days"
        }
    }

    val billingCycleText: String get() = when {
        isLifetime -> "One-time payment • Lifetime VIP Access"
        isYearly -> "Billed annually • 365 days unlimited access"
        isQuarterly -> "Billed quarterly • 90 days access (Save 5%)"
        isMonthly -> "Billed monthly • 30 days access"
        else -> "Billed per cycle • $durationDays days"
    }

    companion object {
        const val RAZORPAY_KEY = "rzp_live_TWhiUSGnL0dHu3"
        const val RAZORPAY_KEY_ID = RAZORPAY_KEY

        val DEFAULT_PLANS = listOf(
            SubscriptionPlan(
                planId = "1month",
                displayName = "1 Month",
                price = 99L,
                currency = "INR",
                durationDays = 30,
                tagline = "Basic Plan",
                features = listOf(
                    "Natural Voice Wake-word ('सहनाज')",
                    "WhatsApp, Calls & SMS Automation",
                    "Full App Control & System Settings",
                    "High-speed Gemini AI responses"
                ),
                isActive = true
            ),
            SubscriptionPlan(
                planId = "3months",
                displayName = "3 Months",
                price = 279L,
                currency = "INR",
                durationDays = 90,
                tagline = "Quarterly Plan - Save 5%",
                features = listOf(
                    "Everything in 1 Month Plan",
                    "Save 5% on Quarterly billing",
                    "Background continuous listening",
                    "Priority voice processing"
                ),
                isActive = true
            ),
            SubscriptionPlan(
                planId = "1year",
                displayName = "1 Year",
                price = 999L,
                currency = "INR",
                durationDays = 365,
                tagline = "Super Saver - Best Value",
                features = listOf(
                    "Everything in 3 Months Plan",
                    "Biggest annual savings",
                    "Priority Gemini Pro AI reasoning",
                    "Continuous updates & cloud backup"
                ),
                isActive = true
            ),
            SubscriptionPlan(
                planId = "lifetime",
                displayName = "Lifetime VIP",
                price = 1499L,
                currency = "INR",
                durationDays = -1,
                tagline = "VIP Access - One Time",
                features = listOf(
                    "One-time payment, VIP forever",
                    "All future updates & AI models",
                    "No renewals or recurring charges",
                    "Instant VIP priority badge & support"
                ),
                isActive = true
            )
        )
    }
}

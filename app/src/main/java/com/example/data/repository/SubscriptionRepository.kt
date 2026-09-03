package com.example.data.repository

import android.util.Log
import com.example.data.local.UserPreferences
import com.example.data.model.SubscriptionPlan
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserSubscription(
    val isSubscribed: Boolean = false,
    val planId: String = "",
    val planDisplayName: String = "",
    val expiryDate: String = "",
    val isLifetime: Boolean = false
)

sealed interface PaymentRequestResult {
    data class Success(val message: String, val requestId: String) : PaymentRequestResult
    data class Error(val message: String) : PaymentRequestResult
}

class SubscriptionRepository(
    private val userPreferences: UserPreferences,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "SubscriptionRepo"
        private const val COLLECTION_PLANS = "subscription_plans"
        private const val COLLECTION_PAYMENT_REQUESTS = "payment_requests"
        private const val COLLECTION_USERS = "users"
    }

    suspend fun getSubscriptionPlans(): List<SubscriptionPlan> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection(COLLECTION_PLANS).get().await()
            if (!snapshot.isEmpty) {
                val fetchedPlans = snapshot.documents.mapNotNull { doc ->
                    try {
                        val planId = doc.getString("planId") ?: doc.id
                        val displayName = doc.getString("displayName") ?: planId.replaceFirstChar { it.uppercase() }
                        val priceNum = doc.get("price")
                        val price = when (priceNum) {
                            is Number -> priceNum.toLong()
                            is String -> priceNum.toLongOrNull() ?: 0L
                            else -> 0L
                        }
                        val currency = doc.getString("currency") ?: "INR"
                        val durationDaysNum = doc.get("durationDays")
                        val durationDays = when (durationDaysNum) {
                            is Number -> durationDaysNum.toInt()
                            is String -> durationDaysNum.toIntOrNull() ?: 30
                            else -> 30
                        }
                        val featuresList = (doc.get("features") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                        val isActive = doc.getBoolean("isActive") ?: true

                        if (isActive) {
                            SubscriptionPlan(
                                planId = planId,
                                displayName = displayName,
                                price = price,
                                currency = currency,
                                durationDays = durationDays,
                                features = featuresList,
                                isActive = isActive
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.w(TAG, "Error parsing plan doc: ${doc.id}", e)
                        null
                    }
                }

                if (fetchedPlans.isNotEmpty()) {
                    // Sort so monthly -> yearly -> lifetime, or by price ascending
                    return@withContext fetchedPlans.sortedBy { it.price }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load plans from Firestore, using default seeds", e)
        }

        // Return default seed plans
        return@withContext SubscriptionPlan.DEFAULT_PLANS
    }

    suspend fun checkUserSubscription(): UserSubscription = withContext(Dispatchers.IO) {
        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid

        if (!userId.isNullOrBlank()) {
            try {
                val userDoc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
                if (userDoc.exists()) {
                    val isLicensed = userDoc.getBoolean("isLicensed") == true
                    val subscriptionPlan = userDoc.getString("subscriptionPlan") ?: ""
                    val subscriptionPlanName = userDoc.getString("subscriptionPlanName") ?: ""
                    val subscriptionExpiryField = userDoc.get("subscriptionExpiry")
                    val isSubscribed = isLicensed || subscriptionPlan.isNotBlank()

                    if (isSubscribed) {
                        val isLifetime = subscriptionPlan.lowercase() == "lifetime" ||
                                (userDoc.get("isLifetime") == true) ||
                                (subscriptionExpiryField == null && isLicensed)

                        val expiryDateStr = when {
                            isLifetime -> "Lifetime — कभी खत्म नहीं होगा"
                            subscriptionExpiryField is Timestamp -> {
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(subscriptionExpiryField.toDate())
                            }
                            subscriptionExpiryField is Date -> {
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(subscriptionExpiryField)
                            }
                            subscriptionExpiryField is String -> subscriptionExpiryField
                            else -> "Active"
                        }

                        val resolvedPlanName = when {
                            subscriptionPlanName.isNotBlank() -> subscriptionPlanName
                            subscriptionPlan.isNotBlank() -> subscriptionPlan.replaceFirstChar { it.uppercase() }
                            isLifetime -> "Lifetime Pro"
                            else -> "SahNaj AI Pro"
                        }

                        // Save local preferences
                        userPreferences.setLicensed(true)
                        userPreferences.setSubscriptionPlan(subscriptionPlan.ifBlank { "lifetime" })
                        userPreferences.setSubscriptionPlanName(resolvedPlanName)
                        userPreferences.setSubscriptionExpiry(expiryDateStr)

                        return@withContext UserSubscription(
                            isSubscribed = true,
                            planId = subscriptionPlan.ifBlank { "lifetime" },
                            planDisplayName = resolvedPlanName,
                            expiryDate = expiryDateStr,
                            isLifetime = isLifetime
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to check subscription in Firestore", e)
            }
        }

        // Check local cache
        val localLicensed = userPreferences.isLicensed()
        val localPlan = userPreferences.getSubscriptionPlan()
        val localPlanName = userPreferences.getSubscriptionPlanName()
        val localExpiry = userPreferences.getSubscriptionExpiry()

        if (localLicensed || localPlan.isNotBlank()) {
            val isLifetime = localPlan.lowercase() == "lifetime" || localExpiry.contains("lifetime", ignoreCase = true)
            return@withContext UserSubscription(
                isSubscribed = true,
                planId = localPlan.ifBlank { "pro" },
                planDisplayName = localPlanName.ifBlank { if (isLifetime) "Lifetime Pro" else "SahNaj AI Pro" },
                expiryDate = localExpiry.ifBlank { if (isLifetime) "Lifetime — कभी खत्म नहीं होगा" else "Active" },
                isLifetime = isLifetime
            )
        }

        return@withContext UserSubscription(isSubscribed = false)
    }

    suspend fun submitPaymentRequest(
        plan: SubscriptionPlan,
        transactionRef: String
    ): PaymentRequestResult = withContext(Dispatchers.IO) {
        val trimmedRef = transactionRef.trim()
        if (trimmedRef.isBlank()) {
            return@withContext PaymentRequestResult.Error("कृपया सही UPI Transaction / UTR Reference नंबर दर्ज करें।")
        }

        val currentUser = firebaseAuth.currentUser
        val userId = currentUser?.uid ?: "user_${userPreferences.getUserDisplayName().ifBlank { "device_client" }}"
        val userEmail = currentUser?.email ?: ""
        val userName = currentUser?.displayName ?: userPreferences.getUserDisplayName()

        try {
            val paymentData = hashMapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "userName" to userName,
                "planId" to plan.planId,
                "planDisplayName" to plan.displayName,
                "amountPaid" to plan.price,
                "currency" to plan.currency,
                "transactionRef" to trimmedRef,
                "status" to "pending",
                "requestedAt" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection(COLLECTION_PAYMENT_REQUESTS).add(paymentData).await()
            Log.i(TAG, "Payment request successfully logged with doc id: ${docRef.id}")

            PaymentRequestResult.Success(
                message = "आपका payment request भेज दिया गया है। 24 घंटे के अंदर verify होकर plan activate हो जाएगा।",
                requestId = docRef.id
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to submit payment request to Firestore", e)
            PaymentRequestResult.Error("रिक्वेस्ट भेजने में त्रुटि हुई: ${e.localizedMessage ?: "इंटरनेट कनेक्शन चेक करें।"}")
        }
    }

    suspend fun activateSubscriptionFromRazorpay(
        plan: SubscriptionPlan,
        paymentId: String,
        orderId: String = "",
        signature: String = ""
    ): Result<UserSubscription> = withContext(Dispatchers.IO) {
        try {
            val isLifetime = plan.isLifetime
            val expiryDateStr = if (isLifetime) {
                "Lifetime VIP — कभी खत्म नहीं होगा"
            } else {
                val expiryMillis = System.currentTimeMillis() + (plan.durationDays.toLong() * 24L * 60L * 60L * 1000L)
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryMillis))
            }

            // Save in local preferences
            userPreferences.setLicensed(true)
            userPreferences.setSubscriptionPlan(plan.planId)
            userPreferences.setSubscriptionPlanName(plan.displayName)
            userPreferences.setSubscriptionExpiry(expiryDateStr)
            userPreferences.setWakeWordEnabled(true)

            val currentUser = firebaseAuth.currentUser
            val userId = currentUser?.uid
            if (!userId.isNullOrBlank()) {
                val userUpdates = hashMapOf<String, Any>(
                    "isLicensed" to true,
                    "isPremium" to true,
                    "subscriptionPlan" to plan.planId,
                    "subscriptionPlanName" to plan.displayName,
                    "subscriptionExpiry" to expiryDateStr,
                    "isLifetime" to isLifetime,
                    "lastPaymentId" to paymentId,
                    "lastPaymentAmount" to plan.price,
                    "lastPaymentAt" to FieldValue.serverTimestamp()
                )
                firestore.collection(COLLECTION_USERS).document(userId).set(userUpdates, com.google.firebase.firestore.SetOptions.merge())

                // Also log to transactions
                val paymentLog = hashMapOf(
                    "userId" to userId,
                    "paymentId" to paymentId,
                    "orderId" to orderId,
                    "signature" to signature,
                    "planId" to plan.planId,
                    "planDisplayName" to plan.displayName,
                    "amount" to plan.price,
                    "amountPaise" to (plan.price * 100),
                    "status" to "SUCCESS",
                    "timestamp" to FieldValue.serverTimestamp()
                )
                firestore.collection("razorpay_transactions").add(paymentLog)
            }

            Result.success(
                UserSubscription(
                    isSubscribed = true,
                    planId = plan.planId,
                    planDisplayName = plan.displayName,
                    expiryDate = expiryDateStr,
                    isLifetime = isLifetime
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error activating subscription from Razorpay", e)
            // Even if network fails, ensure local persistence is preserved
            val isLifetime = plan.isLifetime
            val expiryDateStr = if (isLifetime) {
                "Lifetime VIP — कभी खत्म नहीं होगा"
            } else {
                val expiryMillis = System.currentTimeMillis() + (plan.durationDays.toLong() * 24L * 60L * 60L * 1000L)
                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(expiryMillis))
            }
            userPreferences.setLicensed(true)
            userPreferences.setSubscriptionPlan(plan.planId)
            userPreferences.setSubscriptionPlanName(plan.displayName)
            userPreferences.setSubscriptionExpiry(expiryDateStr)
            userPreferences.setWakeWordEnabled(true)

            Result.success(
                UserSubscription(
                    isSubscribed = true,
                    planId = plan.planId,
                    planDisplayName = plan.displayName,
                    expiryDate = expiryDateStr,
                    isLifetime = isLifetime
                )
            )
        }
    }
}

package com.example.data.repository

import android.util.Log
import com.example.data.local.UserPreferences
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface LicenseResult {
    data class Success(val key: String, val activationDate: String, val message: String) : LicenseResult
    data class Error(val message: String) : LicenseResult
}

class LicenseRepository(
    private val userPreferences: UserPreferences,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    companion object {
        private const val TAG = "LicenseRepository"
        private const val COLLECTION_LICENSE_KEYS = "license_keys"
        private const val COLLECTION_USERS = "users"
    }

    suspend fun activateLicense(inputKey: String): LicenseResult = withContext(Dispatchers.IO) {
        val trimmedKey = inputKey.trim()
        if (trimmedKey.isBlank()) {
            return@withContext LicenseResult.Error("कृपया वैध license key दर्ज करें।")
        }

        try {
            val col = firestore.collection(COLLECTION_LICENSE_KEYS)
            
            // 1. Search for document by "key" field or document ID
            var docSnapshot = col.whereEqualTo("key", trimmedKey).limit(1).get().await().documents.firstOrNull()
            
            if (docSnapshot == null) {
                // Try uppercase variation
                docSnapshot = col.whereEqualTo("key", trimmedKey.uppercase()).limit(1).get().await().documents.firstOrNull()
            }
            
            if (docSnapshot == null) {
                // Try direct doc ID
                val directDoc = col.document(trimmedKey).get().await()
                if (directDoc.exists()) {
                    docSnapshot = directDoc
                } else {
                    val directDocUpper = col.document(trimmedKey.uppercase()).get().await()
                    if (directDocUpper.exists()) {
                        docSnapshot = directDocUpper
                    }
                }
            }

            // 2. If key doesn't exist
            if (docSnapshot == null || !docSnapshot.exists()) {
                Log.w(TAG, "License key not found: $trimmedKey")
                return@withContext LicenseResult.Error("अमान्य license key। कृपया दोबारा जांचें।")
            }

            val docData = docSnapshot.data ?: emptyMap<String, Any>()
            val keyString = docSnapshot.getString("key") ?: trimmedKey
            val isUsed = docSnapshot.getBoolean("isUsed") ?: (docData["isUsed"] == true)
            val usedByUserId = docSnapshot.getString("usedByUserId")

            // 3. Check expiryDate
            val expiryTimeMillis: Long? = when (val exp = docData["expiryDate"]) {
                is Timestamp -> exp.toDate().time
                is Date -> exp.time
                is Number -> {
                    val rawNum = exp.toLong()
                    if (rawNum < 100_000_000_000L) rawNum * 1000L else rawNum
                }
                else -> null
            }

            if (expiryTimeMillis != null && expiryTimeMillis < System.currentTimeMillis()) {
                Log.w(TAG, "License key expired on: $expiryTimeMillis")
                return@withContext LicenseResult.Error("इस key की समय सीमा समाप्त हो चुकी है।")
            }

            val currentUserId = firebaseAuth.currentUser?.uid ?: "device_user_${userPreferences.getUserDisplayName()}"

            // 4. Check if already used
            if (isUsed) {
                // If it's already assigned to the current user, refresh status
                if (!usedByUserId.isNullOrBlank() && usedByUserId == currentUserId) {
                    val dateStr = userPreferences.getLicenseActivatedDate().ifBlank {
                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
                    }
                    userPreferences.setLicensed(true)
                    userPreferences.setLicenseKey(keyString)
                    userPreferences.setLicenseActivatedDate(dateStr)
                    return@withContext LicenseResult.Success(
                        key = keyString,
                        activationDate = dateStr,
                        message = "आपका लाइसेंस पहले से एक्टिवेटेड है।"
                    )
                }
                return@withContext LicenseResult.Error("यह key पहले से इस्तेमाल हो चुकी है।")
            }

            // 5. Mark key as used in Firestore
            val updateKeyMap = hashMapOf<String, Any>(
                "isUsed" to true,
                "usedByUserId" to currentUserId,
                "activatedAt" to FieldValue.serverTimestamp()
            )
            docSnapshot.reference.update(updateKeyMap).await()

            // 6. Update user's own document if logged in
            val loggedInUid = firebaseAuth.currentUser?.uid
            if (!loggedInUid.isNullOrBlank()) {
                val userDocRef = firestore.collection(COLLECTION_USERS).document(loggedInUid)
                val userLicenseMap = hashMapOf<String, Any>(
                    "isLicensed" to true,
                    "licenseKey" to keyString,
                    "licenseActivatedAt" to FieldValue.serverTimestamp()
                )
                userDocRef.set(userLicenseMap, SetOptions.merge()).await()
            }

            // 7. Save local preferences
            val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
            userPreferences.setLicensed(true)
            userPreferences.setLicenseKey(keyString)
            userPreferences.setLicenseActivatedDate(formattedDate)
            userPreferences.setLicenseActivatedTimestamp(System.currentTimeMillis())

            Log.i(TAG, "License key successfully activated for user: $currentUserId")
            LicenseResult.Success(
                key = keyString,
                activationDate = formattedDate,
                message = "लाइसेंस सफलतापूर्वक एक्टिवेट हो गया!"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error activating license key", e)
            LicenseResult.Error("एक्टिवेशन विफल: ${e.localizedMessage ?: "इंटरनेट कनेक्शन जांचें।"}")
        }
    }

    suspend fun syncLicenseStatusFromCloud(): Boolean = withContext(Dispatchers.IO) {
        val user = firebaseAuth.currentUser ?: return@withContext userPreferences.isLicensed()
        try {
            val userDoc = firestore.collection(COLLECTION_USERS).document(user.uid).get().await()
            if (userDoc.exists()) {
                val cloudIsLicensed = userDoc.getBoolean("isLicensed") == true
                if (cloudIsLicensed) {
                    userPreferences.setLicensed(true)
                    val key = userDoc.getString("licenseKey")
                    if (!key.isNullOrBlank()) {
                        userPreferences.setLicenseKey(key)
                    }
                    val actTimestamp = userDoc.getTimestamp("licenseActivatedAt")
                    if (actTimestamp != null && userPreferences.getLicenseActivatedDate().isBlank()) {
                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(actTimestamp.toDate())
                        userPreferences.setLicenseActivatedDate(dateStr)
                        userPreferences.setLicenseActivatedTimestamp(actTimestamp.toDate().time)
                    }
                    return@withContext true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not sync license status from cloud", e)
        }
        return@withContext userPreferences.isLicensed()
    }
}

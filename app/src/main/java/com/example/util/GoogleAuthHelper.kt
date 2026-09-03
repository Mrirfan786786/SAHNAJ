package com.example.util

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

object GoogleAuthHelper {
    private const val TAG = "GoogleAuthHelper"
    
    // Web Client ID from google-services.json (client_type: 3)
    const val DEFAULT_WEB_CLIENT_ID = "623431647609-apfum4oi0ouitc6fks9b4c9iaiut2ckv.apps.googleusercontent.com"

    fun resolveWebClientId(context: Context): String {
        return try {
            val stringId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (stringId != 0) {
                val clientId = context.getString(stringId)
                if (clientId.isNotBlank()) return clientId
            }
            val appStringId = context.resources.getIdentifier("google_web_client_id", "string", context.packageName)
            if (appStringId != 0) {
                val clientId = context.getString(appStringId)
                if (clientId.isNotBlank()) return clientId
            }
            DEFAULT_WEB_CLIENT_ID
        } catch (e: Exception) {
            DEFAULT_WEB_CLIENT_ID
        }
    }

    suspend fun launchGoogleSignIn(context: Context): Result<String> {
        val serverClientId = resolveWebClientId(context)
        Log.d(TAG, "Initiating Google Sign-In via CredentialManager with Web Client ID: $serverClientId")
        return try {
            val credentialManager = CredentialManager.create(context)

            // Use GetGoogleIdOption for native bottom sheet account picker
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = context,
                request = request
            )

            when (val credential = result.credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        Log.d(TAG, "Google ID token retrieved successfully from CredentialManager")
                        Result.success(idToken)
                    } else {
                        Result.failure(Exception("Unsupported credential type: ${credential.type}"))
                    }
                }
                else -> {
                    Result.failure(Exception("Unknown credential format received"))
                }
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In canceled by user")
            Result.failure(Exception("Sign-in canceled"))
        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.e(TAG, "No Google credentials available on device: ${e.message}", e)
            Result.failure(Exception("NO_GOOGLE_ACCOUNT_ON_DEVICE: No Google accounts found on this device or emulator. Please sign in with Email & Password or add a Google account."))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "CredentialManager error [${e.type}]: ${e.message}", e)
            val msg = if (e.type.contains("TYPE_NO_CREDENTIAL", ignoreCase = true) || e.message?.contains("No credentials", ignoreCase = true) == true) {
                "NO_GOOGLE_ACCOUNT_ON_DEVICE: No Google accounts found on this device or emulator. Please sign in with Email & Password."
            } else {
                e.localizedMessage ?: "Google Sign-In failed"
            }
            Result.failure(Exception(msg))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}

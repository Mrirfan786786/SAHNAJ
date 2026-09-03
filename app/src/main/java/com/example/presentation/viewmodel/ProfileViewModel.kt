package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Content(
        val profile: UserProfile,
        val firebaseUser: FirebaseUser?,
        val preferredLanguage: String,
        val useNameWhenSpeaking: Boolean,
        val accountCreationDate: String,
        val signInProvider: String
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
    data object ResetDone : ProfileUiState()
    data object SignedOut : ProfileUiState()
}

class ProfileViewModel(
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    init {
        loadProfile()
    }

    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }

    fun loadProfile() {
        val baseProfile = userPreferences.getUserProfile()
        val fbUser = authRepository?.currentUser?.value ?: FirebaseAuth.getInstance().currentUser

        val resolvedDisplayName = userPreferences.getUserDisplayName().takeIf { it.isNotBlank() && it != "USER" }
            ?: fbUser?.displayName?.takeIf { it.isNotBlank() }
            ?: "USER"

        val mergedProfile = if (fbUser != null) {
            baseProfile.copy(
                uid = fbUser.uid,
                displayName = resolvedDisplayName,
                email = fbUser.email ?: baseProfile.email,
                phoneNumber = fbUser.phoneNumber ?: "",
                photoUrl = fbUser.photoUrl?.toString() ?: ""
            )
        } else {
            baseProfile.copy(displayName = resolvedDisplayName)
        }

        val creationDate = formatCreationDate(fbUser)
        val signInProvider = resolveSignInProvider(fbUser)

        _uiState.value = ProfileUiState.Content(
            profile = mergedProfile,
            firebaseUser = fbUser,
            preferredLanguage = userPreferences.getLanguage(),
            useNameWhenSpeaking = userPreferences.isUseNameWhenSpeaking(),
            accountCreationDate = creationDate,
            signInProvider = signInProvider
        )
    }

    private fun formatCreationDate(user: FirebaseUser?): String {
        val timestamp = user?.metadata?.creationTimestamp
        return if (timestamp != null && timestamp > 0L) {
            try {
                val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
                sdf.format(Date(timestamp))
            } catch (e: Exception) {
                "Recent"
            }
        } else {
            "Local Session Active"
        }
    }

    private fun resolveSignInProvider(user: FirebaseUser?): String {
        if (user == null) return "Local / Guest Session"
        val providers = user.providerData
        for (provider in providers) {
            when (provider.providerId) {
                "google.com" -> return "Google Sign-In"
                "password" -> return "Email & Password"
                "phone" -> return "Phone Verification"
                "anonymous" -> return "Anonymous Guest"
            }
        }
        return "Google / Firebase Authentication"
    }

    fun updateDisplayName(name: String) {
        val clean = name.trim().ifEmpty { "USER" }
        userPreferences.setUserDisplayName(clean)
        _isSaving.value = true

        viewModelScope.launch {
            try {
                val fbUser = authRepository?.currentUser?.value ?: FirebaseAuth.getInstance().currentUser
                if (fbUser != null) {
                    // Update Firebase Auth display name
                    try {
                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(clean)
                            .build()
                        fbUser.updateProfile(update).await()
                    } catch (e: Exception) {
                        // ignore if auth profile update fails
                    }

                    // Update Firestore document under users/{uid}
                    try {
                        withContext(Dispatchers.IO) {
                            val db = FirebaseFirestore.getInstance()
                            val userDoc = db.collection("users").document(fbUser.uid)
                            val data = hashMapOf<String, Any>(
                                "displayName" to clean,
                                "nameUpdatedAt" to FieldValue.serverTimestamp()
                            )
                            userDoc.set(data, SetOptions.merge()).await()
                        }
                    } catch (e: Exception) {
                        // ignore offline firestore exceptions
                    }
                }
                _feedbackMessage.value = "Name updated successfully!"
            } catch (e: Exception) {
                _feedbackMessage.value = "Name saved locally."
            } finally {
                _isSaving.value = false
                loadProfile()
            }
        }
    }

    fun updatePreferredLanguage(lang: String) {
        userPreferences.setLanguage(lang)
        viewModelScope.launch {
            try {
                val fbUser = authRepository?.currentUser?.value ?: FirebaseAuth.getInstance().currentUser
                if (fbUser != null) {
                    withContext(Dispatchers.IO) {
                        val db = FirebaseFirestore.getInstance()
                        val userDoc = db.collection("users").document(fbUser.uid)
                        val data = hashMapOf<String, Any>(
                            "language" to lang,
                            "lastUpdated" to FieldValue.serverTimestamp()
                        )
                        userDoc.set(data, SetOptions.merge()).await()
                    }
                }
                _feedbackMessage.value = "Language set to $lang"
            } catch (e: Exception) {
                _feedbackMessage.value = "Language saved locally."
            } finally {
                loadProfile()
            }
        }
    }

    fun updateUseNameWhenSpeaking(enabled: Boolean) {
        userPreferences.setUseNameWhenSpeaking(enabled)
        viewModelScope.launch {
            try {
                val fbUser = authRepository?.currentUser?.value ?: FirebaseAuth.getInstance().currentUser
                if (fbUser != null) {
                    withContext(Dispatchers.IO) {
                        val db = FirebaseFirestore.getInstance()
                        val userDoc = db.collection("users").document(fbUser.uid)
                        val data = hashMapOf<String, Any>(
                            "useNameWhenSpeaking" to enabled,
                            "lastUpdated" to FieldValue.serverTimestamp()
                        )
                        userDoc.set(data, SetOptions.merge()).await()
                    }
                }
            } catch (e: Exception) {
                // local preference is already saved
            } finally {
                loadProfile()
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        authRepository?.signOut()
        _uiState.value = ProfileUiState.SignedOut
        onSignedOut()
    }

    fun resetAppData() {
        userPreferences.clearUserData()
        _uiState.value = ProfileUiState.ResetDone
        loadProfile()
    }
}



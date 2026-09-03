package com.example.presentation.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.repository.AuthRepository
import com.example.util.GoogleAuthHelper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    REGISTER,
    PHONE_OTP,
    FORGOT_PASSWORD
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data class Loading(val message: String = "AUTHENTICATING...") : AuthUiState()
    data class OtpSent(val verificationId: String, val phoneNumber: String) : AuthUiState()
    data class PasswordResetSent(val email: String, val message: String = "Password reset link sent to your email. Please check your inbox.") : AuthUiState()
    data class Success(val user: FirebaseUser, val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
    val isLoggedIn: Boolean get() = authRepository.isLoggedIn

    // Form inputs
    var emailInput = MutableStateFlow("")
    var passwordInput = MutableStateFlow("")
    var confirmPasswordInput = MutableStateFlow("")
    var nameInput = MutableStateFlow("")
    var phoneInput = MutableStateFlow("")
    var otpInput = MutableStateFlow("")

    private var currentVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val _resendCountdown = MutableStateFlow(0)
    val resendCountdown: StateFlow<Int> = _resendCountdown.asStateFlow()
    private var timerJob: Job? = null

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        _uiState.value = AuthUiState.Idle
    }

    fun clearError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }

    fun signInWithEmail() {
        val email = emailInput.value.trim().lowercase()
        val pass = passwordInput.value

        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }
        if (pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your password.")
            return
        }

        _uiState.value = AuthUiState.Loading("AUTHENTICATING CREDENTIALS...")
        viewModelScope.launch {
            authRepository.signInWithEmail(email, pass).onSuccess { user ->
                syncUserProfile(user)
                _uiState.value = AuthUiState.Success(user, "Welcome back, ${user.displayName ?: "User"}!")
            }.onFailure { err ->
                Log.e(TAG, "Email sign-in failed", err)
                val rawMsg = err.localizedMessage ?: ""
                val friendlyMessage = when {
                    err is FirebaseAuthInvalidCredentialsException ||
                    err is FirebaseAuthInvalidUserException ||
                    rawMsg.contains("invalid credential", ignoreCase = true) ||
                    rawMsg.contains("supplied auth credential", ignoreCase = true) ||
                    rawMsg.contains("wrong password", ignoreCase = true) ||
                    rawMsg.contains("user not found", ignoreCase = true) ||
                    rawMsg.contains("no user record", ignoreCase = true) ->
                        "Invalid email or password. Please verify your credentials or create a new account."
                    rawMsg.contains("network", ignoreCase = true) ->
                        "Network error. Please check your internet connection and try again."
                    rawMsg.contains("too many requests", ignoreCase = true) ||
                    rawMsg.contains("blocked", ignoreCase = true) ->
                        "Too many unsuccessful attempts. Please try again later or reset your password."
                    else -> rawMsg.ifBlank { "Invalid email or password." }
                }
                _uiState.value = AuthUiState.Error(friendlyMessage)
            }
        }
    }

    fun signUpWithEmail() {
        val name = nameInput.value.trim()
        val email = emailInput.value.trim().lowercase()
        val pass = passwordInput.value
        val confirmPass = confirmPasswordInput.value

        if (name.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your full name.")
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Passwords do not match.")
            return
        }

        _uiState.value = AuthUiState.Loading("CREATING SECURE ACCOUNT...")
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, pass, name).onSuccess { user ->
                syncUserProfile(user, fallbackName = name)
                _uiState.value = AuthUiState.Success(user, "Account created successfully!")
            }.onFailure { err ->
                Log.e(TAG, "Email signup failed", err)
                val rawMsg = err.localizedMessage ?: ""
                val friendlyMessage = when {
                    err is FirebaseAuthUserCollisionException ||
                    rawMsg.contains("already in use", ignoreCase = true) ||
                    rawMsg.contains("email address is already", ignoreCase = true) ->
                        "This email is already registered. Please Sign In or use Reset Password."
                    err is FirebaseAuthWeakPasswordException ||
                    rawMsg.contains("weak", ignoreCase = true) ->
                        "Password is too weak. Please use at least 6 characters."
                    err is FirebaseAuthInvalidCredentialsException ||
                    rawMsg.contains("badly formatted", ignoreCase = true) ->
                        "Invalid email address format."
                    rawMsg.contains("network", ignoreCase = true) ->
                        "Network error. Please check your connection and try again."
                    else -> rawMsg.ifBlank { "Registration failed. Please try again." }
                }
                _uiState.value = AuthUiState.Error(friendlyMessage)
            }
        }
    }

    fun sendPasswordReset() {
        val email = emailInput.value.trim().lowercase()
        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = AuthUiState.Error("Please enter your registered email address.")
            return
        }

        _uiState.value = AuthUiState.Loading("SENDING RESET EMAIL...")
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).onSuccess {
                Log.d(TAG, "Firebase password reset email sent successfully to $email")
                _uiState.value = AuthUiState.PasswordResetSent(
                    email = email,
                    message = "Password reset link sent to your email. Please check your inbox."
                )
            }.onFailure { err ->
                Log.e(TAG, "Firebase password reset failed", err)
                val rawMsg = err.localizedMessage ?: ""
                val friendlyMessage = when {
                    err is FirebaseAuthInvalidUserException ||
                    rawMsg.contains("user not found", ignoreCase = true) ||
                    rawMsg.contains("no user record", ignoreCase = true) ->
                        "No account found for this email address. Please check the spelling or sign up."
                    rawMsg.contains("invalid email", ignoreCase = true) ->
                        "Invalid email address format."
                    rawMsg.contains("network", ignoreCase = true) ->
                        "Network error. Please check your internet connection."
                    else -> rawMsg.ifBlank { "Failed to send reset email. Please try again." }
                }
                _uiState.value = AuthUiState.Error(friendlyMessage)
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _uiState.value = AuthUiState.Loading("CONNECTING WITH GOOGLE...")
        viewModelScope.launch {
            val tokenResult = GoogleAuthHelper.launchGoogleSignIn(context)
            tokenResult.onSuccess { idToken ->
                authRepository.signInWithGoogle(idToken).onSuccess { user ->
                    syncUserProfile(user)
                    _uiState.value = AuthUiState.Success(user, "Google Sign-In successful!")
                }.onFailure { err ->
                    Log.e(TAG, "Firebase Google credential exchange failed", err)
                    _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Google authentication failed.")
                }
            }.onFailure { err ->
                if (err.message != "Sign-in canceled") {
                    val rawMsg = err.localizedMessage ?: ""
                    val friendlyMsg = when {
                        rawMsg.contains("NO_GOOGLE_ACCOUNT_ON_DEVICE", ignoreCase = true) ||
                        rawMsg.contains("No credentials", ignoreCase = true) ||
                        rawMsg.contains("16", ignoreCase = true) ->
                            "No Google account found on this device. Please use Email & Password to sign in."
                        else -> rawMsg.ifBlank { "Google Sign-In failed. Please use Email & Password." }
                    }
                    _uiState.value = AuthUiState.Error(friendlyMsg)
                } else {
                    _uiState.value = AuthUiState.Idle
                }
            }
        }
    }

    fun sendPhoneOtp(activity: Activity) {
        var rawPhone = phoneInput.value.trim()
        if (rawPhone.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter a valid phone number.")
            return
        }
        if (!rawPhone.startsWith("+")) {
            rawPhone = "+91$rawPhone"
        }

        _uiState.value = AuthUiState.Loading("SENDING OTP CODE...")
        startCountdownTimer()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Phone auto-verification completed")
                viewModelScope.launch {
                    authRepository.signInWithPhoneCredential(credential).onSuccess { user ->
                        syncUserProfile(user)
                        _uiState.value = AuthUiState.Success(user, "Phone verified successfully!")
                    }.onFailure { err ->
                        _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Verification failed.")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Phone verification failed", e)
                val rawMsg = e.localizedMessage ?: ""
                val friendlyMessage = when {
                    rawMsg.contains("BILLING_NOT_ENABLED", ignoreCase = true) || rawMsg.contains("17499", ignoreCase = true) || rawMsg.contains("billing", ignoreCase = true) ->
                        "Firebase SMS service requires Blaze plan. Please use Email / Password or Google Sign-In."
                    rawMsg.contains("operation is not allowed", ignoreCase = true) || rawMsg.contains("sign-in provider is disabled", ignoreCase = true) ->
                        "Phone Auth is not enabled in Firebase Console. Please use Email / Password."
                    rawMsg.contains("invalid", ignoreCase = true) ->
                        "Invalid phone number format. Please enter country code (e.g. +91 9876543210)."
                    else -> rawMsg.ifBlank { "SMS OTP verification failed. Please try Email login." }
                }
                _uiState.value = AuthUiState.Error(friendlyMessage)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "OTP Code sent: $verificationId")
                currentVerificationId = verificationId
                resendToken = token
                _uiState.value = AuthUiState.OtpSent(verificationId, rawPhone)
            }
        }

        authRepository.verifyPhoneNumber(rawPhone, activity, callbacks, resendToken)
    }

    fun verifyOtp() {
        val verificationId = currentVerificationId
        val code = otpInput.value.trim()

        if (verificationId.isNullOrBlank()) {
            _uiState.value = AuthUiState.Error("Please request an OTP code first.")
            return
        }
        if (code.length < 6) {
            _uiState.value = AuthUiState.Error("Please enter the 6-digit SMS OTP.")
            return
        }

        _uiState.value = AuthUiState.Loading("VERIFYING OTP...")
        viewModelScope.launch {
            authRepository.signInWithPhoneOtp(verificationId, code).onSuccess { user ->
                syncUserProfile(user)
                _uiState.value = AuthUiState.Success(user, "Phone login verified!")
            }.onFailure { err ->
                Log.e(TAG, "OTP verification failed", err)
                _uiState.value = AuthUiState.Error(err.localizedMessage ?: "Invalid OTP code.")
            }
        }
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        _resendCountdown.value = 60
        timerJob = viewModelScope.launch {
            while (_resendCountdown.value > 0) {
                delay(1000L)
                _resendCountdown.value -= 1
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        userPreferences.setUserDisplayName("")
        _uiState.value = AuthUiState.Idle
    }

    private fun syncUserProfile(user: FirebaseUser, fallbackName: String = "") {
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: fallbackName.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "USER"
        userPreferences.setUserDisplayName(name)
    }
}


package com.example.presentation.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.personality.PersonalityResponses
import androidx.activity.compose.BackHandler
import com.example.presentation.viewmodel.AuthMode
import com.example.presentation.viewmodel.AuthUiState
import com.example.presentation.viewmodel.AuthViewModel
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val uiState by authViewModel.uiState.collectAsState()
    val authMode by authViewModel.authMode.collectAsState()
    val countdown by authViewModel.resendCountdown.collectAsState()

    val email by authViewModel.emailInput.collectAsState()
    val password by authViewModel.passwordInput.collectAsState()
    val confirmPassword by authViewModel.confirmPasswordInput.collectAsState()
    val name by authViewModel.nameInput.collectAsState()
    val phone by authViewModel.phoneInput.collectAsState()
    val otp by authViewModel.otpInput.collectAsState()

    var showPassword by remember { mutableStateOf(false) }

    // Strict Auth Rule: In Reset/Register/Phone modes, back press returns to Login tab rather than killing app
    BackHandler {
        if (authMode != AuthMode.LOGIN) {
            authViewModel.clearError()
            authViewModel.setAuthMode(AuthMode.LOGIN)
        } else {
            (context as? Activity)?.finish()
        }
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is AuthUiState.Success -> {
                onAuthSuccess()
            }
            is AuthUiState.PasswordResetSent -> {
                android.widget.Toast.makeText(
                    context,
                    "Password reset link sent to your email. Please check your inbox.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
            else -> Unit
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CyberBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // AI Logo Badge
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberRedContainer)
                    .border(BorderStroke(2.dp, CyberRedBright), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "AI Auth",
                    tint = CyberRedBright,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${PersonalityResponses.ASSISTANT_NAME} AI CORE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = CyberTextPrimary
            )

            Text(
                text = "NEURAL VOICE ASSISTANT // AUTH PORTAL",
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberRedBright,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mode Tabs (Sign In / Create Account / Phone OTP)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val isSignInTab = authMode == AuthMode.LOGIN || authMode == AuthMode.FORGOT_PASSWORD
                Button(
                    onClick = {
                        authViewModel.clearError()
                        authViewModel.setAuthMode(AuthMode.LOGIN)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSignInTab) CyberRedBright else Color.Transparent
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "SIGN IN",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSignInTab) Color.White else CyberTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                val isRegisterTab = authMode == AuthMode.REGISTER
                Button(
                    onClick = {
                        authViewModel.clearError()
                        authViewModel.setAuthMode(AuthMode.REGISTER)
                    },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRegisterTab) CyberRedBright else Color.Transparent
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "CREATE ACCOUNT",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRegisterTab) Color.White else CyberTextMuted
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                val isPhoneTab = authMode == AuthMode.PHONE_OTP
                Button(
                    onClick = {
                        authViewModel.clearError()
                        authViewModel.setAuthMode(AuthMode.PHONE_OTP)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPhoneTab) CyberRedBright else Color.Transparent
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "PHONE OTP",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPhoneTab) Color.White else CyberTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Input Container Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCard),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Success / Notification Banner (e.g. Password Reset Sent or OTP Sent)
                    if (uiState is AuthUiState.PasswordResetSent) {
                        val resetState = uiState as AuthUiState.PasswordResetSent
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberGreen.copy(alpha = 0.15f))
                                .border(BorderStroke(1.dp, CyberGreen), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✓ ${resetState.message}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Error Banner with Quick Action Shortcuts
                    if (uiState is AuthUiState.Error) {
                        val errMsg = (uiState as AuthUiState.Error).message
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberRed.copy(alpha = 0.2f))
                                .border(BorderStroke(1.dp, CyberRedBright), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = errMsg,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyberRedBright,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Contextual Action Shortcuts based on error and mode
                            Spacer(modifier = Modifier.height(10.dp))
                            when (authMode) {
                                AuthMode.LOGIN -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.setAuthMode(AuthMode.REGISTER)
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("CREATE ACCOUNT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.setAuthMode(AuthMode.FORGOT_PASSWORD)
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("RESET PASSWORD", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberBlack)
                                        }
                                    }
                                }
                                AuthMode.REGISTER -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.setAuthMode(AuthMode.LOGIN)
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("SIGN IN NOW", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.setAuthMode(AuthMode.FORGOT_PASSWORD)
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                                            border = BorderStroke(1.dp, CyberRedBorder),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("RESET PASS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                                        }
                                    }
                                }
                                else -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.setAuthMode(AuthMode.LOGIN)
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("SIGN IN", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        Button(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.signInWithGoogle(context)
                                            },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurface),
                                            border = BorderStroke(1.dp, CyberRedBorder),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("GOOGLE SIGN-IN", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = CyberTextPrimary)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Success Banner (e.g. Forgot Password reset sent)
                    if (uiState is AuthUiState.Success && authMode == AuthMode.FORGOT_PASSWORD) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberGreen.copy(alpha = 0.2f))
                                .border(BorderStroke(1.dp, CyberGreen), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = (uiState as AuthUiState.Success).message,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyberGreen,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    when (authMode) {
                        AuthMode.LOGIN -> {
                            Text(
                                text = "ACCOUNT SIGN IN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            CyberTextField(
                                value = email,
                                onValueChange = { authViewModel.emailInput.value = it },
                                label = "EMAIL ADDRESS",
                                icon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            CyberTextField(
                                value = password,
                                onValueChange = { authViewModel.passwordInput.value = it },
                                label = "PASSWORD",
                                icon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = showPassword,
                                onTogglePassword = { showPassword = !showPassword },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                onImeAction = {
                                    focusManager.clearFocus()
                                    authViewModel.signInWithEmail()
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { authViewModel.setAuthMode(AuthMode.FORGOT_PASSWORD) }) {
                                    Text(
                                        text = "Forgot Password?",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = CyberRedBright
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    authViewModel.signInWithEmail()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = uiState !is AuthUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                if (uiState is AuthUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = "SIGN IN",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = { authViewModel.setAuthMode(AuthMode.REGISTER) }) {
                                Text(
                                    text = "Don't have an account? CREATE ONE",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary
                                )
                            }
                        }

                        AuthMode.REGISTER -> {
                            Text(
                                text = "REGISTER IDENTITY",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            CyberTextField(
                                value = name,
                                onValueChange = { authViewModel.nameInput.value = it },
                                label = "YOUR FULL NAME",
                                icon = Icons.Default.Person,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            CyberTextField(
                                value = email,
                                onValueChange = { authViewModel.emailInput.value = it },
                                label = "EMAIL ADDRESS",
                                icon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            CyberTextField(
                                value = password,
                                onValueChange = { authViewModel.passwordInput.value = it },
                                label = "PASSWORD (MIN 6 CHARS)",
                                icon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = showPassword,
                                onTogglePassword = { showPassword = !showPassword },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            CyberTextField(
                                value = confirmPassword,
                                onValueChange = { authViewModel.confirmPasswordInput.value = it },
                                label = "CONFIRM PASSWORD",
                                icon = Icons.Default.Key,
                                isPassword = true,
                                passwordVisible = showPassword,
                                onTogglePassword = { showPassword = !showPassword },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                onImeAction = {
                                    focusManager.clearFocus()
                                    authViewModel.signUpWithEmail()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    authViewModel.signUpWithEmail()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = uiState !is AuthUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                if (uiState is AuthUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = "CREATE ACCOUNT",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = { authViewModel.setAuthMode(AuthMode.LOGIN) }) {
                                Text(
                                    text = "Already have an account? SIGN IN",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyberTextSecondary
                                )
                            }
                        }

                        AuthMode.FORGOT_PASSWORD -> {
                            Text(
                                text = "PASSWORD RECOVERY",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter your registered email below to receive a secure password reset link.",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = CyberTextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            CyberTextField(
                                value = email,
                                onValueChange = { authViewModel.emailInput.value = it },
                                label = "REGISTERED EMAIL",
                                icon = Icons.Default.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done,
                                onImeAction = {
                                    focusManager.clearFocus()
                                    authViewModel.sendPasswordReset()
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    authViewModel.sendPasswordReset()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = uiState !is AuthUiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                if (uiState is AuthUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = "SEND RESET EMAIL",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            TextButton(onClick = {
                                authViewModel.clearError()
                                authViewModel.setAuthMode(AuthMode.LOGIN)
                            }) {
                                Text(
                                    text = "← BACK TO SIGN IN",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberRedBright
                                )
                            }
                        }

                        AuthMode.PHONE_OTP -> {
                            Text(
                                text = "MOBILE OTP LOGIN",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CyberTextPrimary
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            CyberTextField(
                                value = phone,
                                onValueChange = { authViewModel.phoneInput.value = it },
                                label = "PHONE NUMBER (e.g. +91 9876543210)",
                                icon = Icons.Default.Phone,
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val activity = context as? Activity

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    if (activity != null) {
                                        authViewModel.sendPhoneOtp(activity)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                enabled = uiState !is AuthUiState.Loading && countdown == 0,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberRedBright),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                if (uiState is AuthUiState.Loading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                } else {
                                    Text(
                                        text = if (countdown > 0) "RESEND IN ${countdown}s" else "SEND OTP CODE",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberSurface)
                                    .border(BorderStroke(1.dp, CyberRedBorder.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                                    .padding(10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "💡 Real SMS OTP requires Firebase Blaze plan or Firebase Console test numbers.\nUse Email / Pass or Google Sign-In for instant login:",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = CyberTextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.setAuthMode(AuthMode.LOGIN)
                                            },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            border = BorderStroke(1.dp, CyberRedBright),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("USE EMAIL", fontSize = 10.sp, color = CyberRedBright, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                authViewModel.clearError()
                                                authViewModel.signInWithGoogle(context)
                                            },
                                            modifier = Modifier.weight(1f).height(34.dp),
                                            border = BorderStroke(1.dp, CyberRedBorder),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("USE GOOGLE", fontSize = 10.sp, color = CyberTextPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            if (uiState is AuthUiState.OtpSent) {
                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = CyberRedBorder)
                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "ENTER 6-DIGIT OTP",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberGreen
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                CyberTextField(
                                    value = otp,
                                    onValueChange = { if (it.length <= 6) authViewModel.otpInput.value = it },
                                    label = "6-DIGIT CODE",
                                    icon = Icons.Default.Key,
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done,
                                    onImeAction = {
                                        focusManager.clearFocus()
                                        authViewModel.verifyOtp()
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        authViewModel.verifyOtp()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    enabled = uiState !is AuthUiState.Loading && otp.length == 6,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "VERIFY & LOGIN",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        color = CyberBlack
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider OR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = CyberRedBorder)
                Text(
                    text = " OR ",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = CyberTextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = CyberRedBorder)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Google Sign In Button
            OutlinedButton(
                onClick = { authViewModel.signInWithGoogle(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.5.dp, CyberRedBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = CyberSurface)
            ) {
                // Google "G" representation
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "G",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color(0xFF4285F4)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "CONTINUE WITH GOOGLE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CyberTextPrimary,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CyberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = CyberTextMuted
            )
        },
        leadingIcon = {
            Icon(imageVector = icon, contentDescription = null, tint = CyberRedBright, modifier = Modifier.size(18.dp))
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Password",
                        tint = CyberTextMuted
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction?.invoke() },
            onNext = { onImeAction?.invoke() }
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberRedBright,
            unfocusedBorderColor = CyberRedBorder,
            focusedTextColor = CyberTextPrimary,
            unfocusedTextColor = CyberTextPrimary,
            focusedContainerColor = CyberSurface,
            unfocusedContainerColor = CyberSurface
        )
    )
}

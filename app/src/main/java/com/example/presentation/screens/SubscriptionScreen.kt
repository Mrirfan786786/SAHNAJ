package com.example.presentation.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SubscriptionPlan
import com.example.presentation.viewmodel.SubscriptionViewModel
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberError
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberRedBorder
import com.example.ui.theme.CyberRedBright
import com.example.ui.theme.CyberRedContainer
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

private const val PLACEHOLDER_UPI_ID = "sahnaj.ai@upi"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SAHNAJ AI Premium",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                        Text(
                            text = "सब्सक्रिप्शन • Time-based VIP Plans",
                            fontSize = 12.sp,
                            color = CyberTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberRedBright
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = CyberTextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CyberBlack)
            )
        },
        containerColor = CyberBlack
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // 1. Hero Header Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardElevated),
                    border = BorderStroke(
                        1.dp,
                        if (uiState.userSubscription.isSubscribed) CyberGreen.copy(alpha = 0.5f) else CyberRedBorder
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = if (uiState.userSubscription.isSubscribed) {
                                        listOf(CyberGreen.copy(alpha = 0.15f), CyberCardElevated)
                                    } else {
                                        listOf(CyberRedContainer.copy(alpha = 0.45f), CyberCardElevated)
                                    }
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.userSubscription.isSubscribed) CyberGreen.copy(alpha = 0.15f)
                                        else CyberRedContainer
                                    )
                                    .border(
                                        1.dp,
                                        if (uiState.userSubscription.isSubscribed) CyberGreen else CyberRedBright,
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (uiState.userSubscription.isSubscribed) Icons.Default.Verified else Icons.Default.Star,
                                    contentDescription = "Subscription Header Icon",
                                    tint = if (uiState.userSubscription.isSubscribed) CyberGreen else CyberRedBright,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.userSubscription.isSubscribed) "VIP Active / Pro Member" else "SAHNAJ AI PRO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (uiState.userSubscription.isSubscribed) CyberGreen else CyberRedBright,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (uiState.userSubscription.isSubscribed) {
                                        "आपका सब्सक्रिप्शन एक्टिव है — सभी फीचर्स अनलॉक्ड"
                                    } else {
                                        "अनलिमिटेड 'सहनाज' वॉयस वेक-वर्ड व एडवांस्ड फीचर्स अनलॉक करें"
                                    },
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyberTextPrimary
                                )
                            }
                        }
                    }
                }

                // 2. Active Subscription Card (if already subscribed)
                if (uiState.userSubscription.isSubscribed) {
                    ActiveSubscriptionCard(
                        subscription = uiState.userSubscription,
                        showAllPlans = uiState.showAllPlans,
                        onTogglePlans = { viewModel.toggleShowAllPlans() }
                    )
                }

                // 3. Plan Cards List (shown if not subscribed OR user tapped "Change Plan")
                if (!uiState.userSubscription.isSubscribed || uiState.showAllPlans) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.userSubscription.isSubscribed) "AVAILABLE PLANS" else "CHOOSE YOUR PLAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextMuted,
                            letterSpacing = 1.sp
                        )

                        if (uiState.userSubscription.isSubscribed) {
                            TextButton(onClick = { viewModel.toggleShowAllPlans() }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Hide plans",
                                        fontSize = 12.sp,
                                        color = CyberRedBright
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ExpandLess,
                                        contentDescription = null,
                                        tint = CyberRedBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CyberRedBright,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        uiState.plans.forEach { plan ->
                            PlanCard(
                                plan = plan,
                                isCurrentPlan = uiState.userSubscription.isSubscribed &&
                                        uiState.userSubscription.planId.equals(plan.planId, ignoreCase = true),
                                onSubscribe = { viewModel.startRazorpayPayment(plan) }
                            )
                        }
                    }
                }

                // 4. Feature Highlights Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    border = BorderStroke(1.dp, Color(0xFF261824))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = CyberAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Premium Features & Razorpay Security",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                        }

                        Text(
                            text = "• Instant Razorpay Checkout (UPI, Cards, NetBanking, Wallets)\n" +
                                   "• Automatic activation upon successful payment verification\n" +
                                   "• Unlocks Voice Wake-Word ('सहनाज') and 24/7 background listener\n" +
                                   "• 100% Encrypted & Safe 256-bit SSL transaction gateway",
                            fontSize = 12.sp,
                            color = CyberTextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Error Message Card if any
                AnimatedVisibility(visible = uiState.errorMessage != null) {
                    uiState.errorMessage?.let { err ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = CyberError.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, CyberError.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = CyberError,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = err,
                                    fontSize = 13.sp,
                                    color = CyberError,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearErrorMessage() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = CyberError,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // 5. Success Dialog when payment succeeds
            if (uiState.confirmationMessage != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.clearConfirmationMessage() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = CyberGreen,
                            modifier = Modifier.size(44.dp)
                        )
                    },
                    title = {
                        Text(
                            text = "Payment & Activation Successful!",
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = uiState.confirmationMessage ?: "",
                                fontSize = 14.sp,
                                color = CyberTextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyberGreen.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = CyberGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Voice Wake-word 'सहनाज' Activated",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberGreen
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.clearConfirmationMessage()
                                viewModel.loadData()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("शुरू करें (Continue)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = Color(0xFF1B1422)
                )
            }

            // 6. Razorpay Checkout Modal Dialog
            if (uiState.showRazorpayModal && uiState.selectedPlanForRazorpay != null) {
                RazorpayCheckoutDialog(
                    plan = uiState.selectedPlanForRazorpay!!,
                    onPaymentSuccess = { paymentId, orderId, sig ->
                        viewModel.onRazorpaySuccess(paymentId, orderId, sig)
                    },
                    onPaymentError = { code, desc ->
                        viewModel.onRazorpayError(code, desc)
                    },
                    onDismiss = {
                        viewModel.dismissRazorpayModal()
                    }
                )
            }

            // Loading overlay during payment confirmation
            if (uiState.isSubmittingPayment) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardElevated),
                        border = BorderStroke(1.dp, CyberRedBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                color = CyberRedBright,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Activating Membership...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextPrimary
                            )
                            Text(
                                text = "Please do not close the app",
                                fontSize = 12.sp,
                                color = CyberTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionCard(
    subscription: com.example.data.repository.UserSubscription,
    showAllPlans: Boolean,
    onTogglePlans: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_subscription_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(1.5.dp, CyberGreen.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = CyberGreen,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Current Active Plan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = CyberGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "VIP ACTIVE / PRO MEMBER",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = subscription.planDisplayName,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyberTextPrimary
            )

            // Expiry Date or Lifetime text
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CyberSurface,
                border = BorderStroke(1.dp, Color(0xFF263326)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (subscription.isLifetime) Icons.Default.AutoAwesome else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (subscription.isLifetime) CyberAmber else CyberGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "वैधता (Validity):",
                            fontSize = 11.sp,
                            color = CyberTextMuted
                        )
                        Text(
                            text = subscription.expiryDate,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subscription.isLifetime) CyberAmber else CyberTextPrimary
                        )
                    }
                }
            }

            HorizontalDivider(
                color = Color(0xFF261824),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Change Plan / View All Plans Button
            OutlinedButton(
                onClick = onTogglePlans,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CyberRedBright
                ),
                border = BorderStroke(1.dp, CyberRedBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (showAllPlans) "Hide Plans" else "Change Plan / View all plans",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (showAllPlans) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    plan: SubscriptionPlan,
    isCurrentPlan: Boolean,
    onSubscribe: () -> Unit
) {
    val isBestValue = plan.isYearly
    val isLifetime = plan.isLifetime
    val isQuarterly = plan.isQuarterly

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plan_card_${plan.planId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        border = BorderStroke(
            if (isBestValue || isLifetime) 2.dp else 1.dp,
            when {
                isBestValue -> CyberRedBright
                isLifetime -> CyberAmber
                else -> CyberRedBorder
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Plan Title & Best Value Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.displayName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberTextPrimary
                    )
                    if (plan.tagline.isNotBlank()) {
                        Text(
                            text = plan.tagline,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = when {
                                isBestValue -> CyberRedBright
                                isLifetime -> CyberAmber
                                isQuarterly -> CyberGreen
                                else -> CyberTextMuted
                            }
                        )
                    }
                }

                if (isBestValue) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CyberRedContainer,
                        border = BorderStroke(1.dp, CyberRedBright)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Best Value",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "BEST VALUE",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else if (isLifetime) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF2A200B),
                        border = BorderStroke(1.dp, CyberAmber)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "VIP",
                                tint = CyberAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "LIFETIME VIP",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberAmber
                            )
                        }
                    }
                }
            }

            // Price Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "₹${"%,d".format(plan.price)}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        isBestValue -> CyberRedBright
                        isLifetime -> CyberAmber
                        else -> CyberTextPrimary
                    }
                )
                Text(
                    text = when {
                        plan.isLifetime -> " one-time"
                        plan.isYearly -> " / year"
                        plan.isQuarterly -> " / 3 months"
                        plan.isMonthly -> " / month"
                        else -> " / ${plan.durationDays} days"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyberTextMuted,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = plan.billingCycleText,
                fontSize = 11.5.sp,
                color = CyberTextMuted
            )

            HorizontalDivider(
                color = Color(0xFF261824),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Features Checklist
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plan.features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(CyberGreen.copy(alpha = 0.15f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = CyberGreen,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Text(
                            text = feature,
                            fontSize = 13.sp,
                            color = CyberTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subscribe Button (Razorpay Checkout Trigger)
            Button(
                onClick = onSubscribe,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("subscribe_button_${plan.planId}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isBestValue -> CyberRed
                        isLifetime -> Color(0xFFB8860B)
                        else -> Color(0xFF381520)
                    }
                ),
                border = if (!isBestValue && !isLifetime) BorderStroke(1.dp, CyberRedBorder) else null
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isCurrentPlan) "Extend Plan • ₹${"%,d".format(plan.price)}" else "Subscribe • ₹${"%,d".format(plan.price)}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Android WebView Checkout Dialog embedding Razorpay checkout.js script
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RazorpayCheckoutDialog(
    plan: SubscriptionPlan,
    onPaymentSuccess: (paymentId: String, orderId: String, signature: String) -> Unit,
    onPaymentError: (code: Int, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isWebViewLoading by remember { mutableStateOf(true) }

    val amountInPaise = plan.price * 100
    val keyId = SubscriptionPlan.RAZORPAY_KEY_ID

    val checkoutHtml = remember(plan) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; }
                body {
                    margin: 0;
                    padding: 16px;
                    background-color: #0F0914;
                    color: #FFFFFF;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    min-height: 100vh;
                }
                .container {
                    text-align: center;
                    max-width: 360px;
                    width: 100%;
                }
                .badge {
                    display: inline-block;
                    padding: 4px 12px;
                    border-radius: 12px;
                    font-size: 11px;
                    font-weight: 800;
                    letter-spacing: 0.5px;
                    margin-bottom: 10px;
                    background: rgba(255, 30, 66, 0.15);
                    border: 1px solid #ff1e42;
                    color: #ff1e42;
                    text-transform: uppercase;
                }
                .badge.vip-active {
                    background: rgba(0, 255, 102, 0.15);
                    border: 1px solid #00FF66;
                    color: #00FF66;
                    box-shadow: 0 0 10px rgba(0, 255, 102, 0.3);
                }
                .spinner {
                    width: 44px;
                    height: 44px;
                    border: 3px solid rgba(255, 30, 66, 0.2);
                    border-top-color: #ff1e42;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                    margin: 0 auto 16px;
                }
                @keyframes spin { to { transform: rotate(360deg); } }
                h3 { margin: 0 0 6px; color: #FFFFFF; font-size: 18px; font-weight: 800; letter-spacing: 0.5px; }
                p { margin: 0; color: #8E7C93; font-size: 13px; line-height: 1.4; }
                .amount-pill {
                    margin: 14px 0 18px;
                    display: inline-block;
                    background: rgba(255, 30, 66, 0.12);
                    border: 1px solid #ff1e42;
                    color: #ff1e42;
                    padding: 8px 18px;
                    border-radius: 20px;
                    font-weight: bold;
                    font-size: 14px;
                    transition: all 0.3s ease;
                }
                .pro-features-box {
                    display: none;
                    text-align: left;
                    background: rgba(0, 255, 102, 0.06);
                    border: 1px solid rgba(0, 255, 102, 0.3);
                    border-radius: 12px;
                    padding: 12px 14px;
                    margin: 12px 0;
                    font-size: 12px;
                    color: #E2E8F0;
                }
                .pro-feature-item {
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin: 6px 0;
                }
                .plan-buttons {
                    display: flex;
                    flex-direction: column;
                    gap: 10px;
                    margin-top: 14px;
                    width: 100%;
                }
                .plan-btn {
                    width: 100%;
                    background: #1B1422;
                    color: #FFFFFF;
                    border: 1px solid #ff1e42;
                    padding: 12px 14px;
                    border-radius: 10px;
                    font-size: 14px;
                    font-weight: bold;
                    cursor: pointer;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    transition: background 0.2s;
                }
                .plan-btn:active {
                    background: #ff1e42;
                }
                .plan-btn.primary {
                    background: #ff1e42;
                    border-color: #ff1e42;
                }
            </style>
            <script src="https://checkout.razorpay.com/v1/checkout.js"></script>
        </head>
        <body>
            <div class="container">
                <div class="badge" id="vipBadge">Free Member</div>
                <div class="spinner" id="spinner"></div>
                <h3>SAHNAJ AI SECURE GATEWAY</h3>
                <p id="statusMsg">Opening Razorpay Checkout for ${plan.displayName}</p>
                <div class="amount-pill" id="amountPill">Payable: ₹${plan.price}</div>

                <div class="pro-features-box" id="proFeaturesBox">
                    <div style="font-weight:bold; color:#00FF66; margin-bottom:6px;">PRO VIP FEATURES ACTIVATED:</div>
                    <div class="pro-feature-item">🎙️ <span>24/7 Voice Wake-Word ('सहनाज') Active</span></div>
                    <div class="pro-feature-item">💬 <span>WhatsApp, Calls & SMS Automation Unlocked</span></div>
                    <div class="pro-feature-item">⚡ <span>Priority Gemini AI Model & High-Speed Reasoning</span></div>
                </div>

                <div class="plan-buttons" id="planButtons">
                    <button class="plan-btn" id="btn-1month" onclick="startSubscriptionPayment('1 Month', 99)">
                        <span>1 Month Plan</span>
                        <span>₹99</span>
                    </button>
                    <button class="plan-btn" id="btn-3months" onclick="startSubscriptionPayment('3 Months', 279)">
                        <span>3 Months Plan</span>
                        <span>₹279</span>
                    </button>
                    <button class="plan-btn" id="btn-1year" onclick="startSubscriptionPayment('1 Year', 999)">
                        <span>1 Year Plan (Best Value)</span>
                        <span>₹999</span>
                    </button>
                    <button class="plan-btn primary" id="btn-lifetime" onclick="startSubscriptionPayment('Lifetime VIP', 1499)">
                        <span>Lifetime VIP Access</span>
                        <span>₹1499</span>
                    </button>
                </div>
            </div>

            <script>
                const RAZORPAY_KEY = 'rzp_live_TWhiUSGnL0dHu3';
                let activePlanName = '${plan.displayName}';

                function activateProFeatures(planName, paymentId) {
                    console.log('Activating SAHNAJ AI VIP Pro features for: ' + planName);

                    // Save VIP subscription details in localStorage
                    localStorage.setItem('sahnaj_vip_status', 'active');
                    if (planName) localStorage.setItem('sahnaj_plan', planName);
                    if (paymentId) localStorage.setItem('sahnaj_payment_id', paymentId);

                    // Update UI badge to "VIP Active / Pro Member"
                    const badgeElem = document.getElementById('vipBadge');
                    if (badgeElem) {
                        badgeElem.innerText = 'VIP Active / Pro Member';
                        badgeElem.className = 'badge vip-active';
                    }

                    // Unlock 24/7 background voice listener & natural voice wake-word ('सहनाज')
                    console.log("Unlocked: 24/7 Background Voice Listener & Natural Wake-Word ('सहनाज')");

                    // Grant full access to WhatsApp/Calls/SMS automation and priority Gemini AI features
                    console.log("Granted: Full WhatsApp/Calls/SMS Automation & Priority Gemini AI");

                    const statusElem = document.getElementById('statusMsg');
                    const pillElem = document.getElementById('amountPill');
                    const spinnerElem = document.getElementById('spinner');
                    const proFeaturesBox = document.getElementById('proFeaturesBox');

                    if (spinnerElem) spinnerElem.style.display = 'none';
                    if (statusElem) {
                        statusElem.innerHTML = '<b style="color:#00FF66;">✓ VIP Active / Pro Member</b><br><span style="font-size:12px;color:#A090A8;">' + (planName || 'VIP') + ' Access Unlocked</span>';
                    }
                    if (pillElem) {
                        pillElem.innerText = 'VIP MEMBERSHIP ACTIVE';
                        pillElem.style.background = 'rgba(0, 255, 102, 0.15)';
                        pillElem.style.borderColor = '#00FF66';
                        pillElem.style.color = '#00FF66';
                    }
                    if (proFeaturesBox) {
                        proFeaturesBox.style.display = 'block';
                    }
                }

                function startSubscriptionPayment(planName, amountInRupees) {
                    try {
                        activePlanName = planName;
                        const statusElem = document.getElementById('statusMsg');
                        const pillElem = document.getElementById('amountPill');
                        const spinnerElem = document.getElementById('spinner');

                        if (statusElem) statusElem.innerText = 'Processing ' + planName + ' VIP Access...';
                        if (pillElem) {
                            pillElem.innerText = 'Payable: ₹' + amountInRupees;
                            pillElem.style.background = 'rgba(255, 30, 66, 0.12)';
                            pillElem.style.borderColor = '#ff1e42';
                            pillElem.style.color = '#ff1e42';
                        }
                        if (spinnerElem) spinnerElem.style.display = 'block';

                        var options = {
                            "key": RAZORPAY_KEY,
                            "amount": amountInRupees * 100,
                            "currency": "INR",
                            "name": "SAHNAJ AI",
                            "description": planName + ' VIP Access',
                            "image": "https://cdn-icons-png.flaticon.com/512/4712/4712109.png",
                            "theme": {
                                "color": "#ff1e42"
                            },
                            "modal": {
                                "ondismiss": function() {
                                    if (spinnerElem) spinnerElem.style.display = 'none';
                                    if (statusElem && localStorage.getItem('sahnaj_vip_status') !== 'active') {
                                        statusElem.innerText = 'Choose a plan or re-open checkout';
                                    }
                                    if (window.AndroidInterface) {
                                        window.AndroidInterface.onPaymentCancelled();
                                    }
                                }
                            },
                            "handler": function (response) {
                                var paymentId = (response && response.razorpay_payment_id) ? response.razorpay_payment_id : '';
                                
                                // 1. Display success confirmation alert showing payment ID
                                alert('Payment Successful! Payment ID: ' + paymentId);

                                // 2. Save VIP subscription details in localStorage & 3. Activate Pro Features
                                activateProFeatures(planName, paymentId);

                                // Notify Android Interface
                                if (window.AndroidInterface) {
                                    window.AndroidInterface.onPaymentSuccess(
                                        paymentId,
                                        response.razorpay_order_id || "",
                                        response.razorpay_signature || ""
                                    );
                                }
                            }
                        };

                        var rzp = new Razorpay(options);
                        rzp.on('payment.failed', function (response) {
                            if (spinnerElem) spinnerElem.style.display = 'none';
                            if (window.AndroidInterface) {
                                window.AndroidInterface.onPaymentError(
                                    (response.error && response.error.code) || 0,
                                    (response.error && response.error.description) || "Payment failed"
                                );
                            }
                        });
                        rzp.open();
                    } catch(e) {
                        console.error('Error starting Razorpay checkout:', e);
                        var spinner = document.getElementById('spinner');
                        if (spinner) spinner.style.display = 'none';
                    }
                }

                // Automatically runs on app launch (DOMContentLoaded / onload) if VIP is active
                document.addEventListener('DOMContentLoaded', function() {
                    if (localStorage.getItem('sahnaj_vip_status') === 'active') {
                        var savedPlan = localStorage.getItem('sahnaj_plan') || 'VIP Access';
                        var savedPaymentId = localStorage.getItem('sahnaj_payment_id') || '';
                        activateProFeatures(savedPlan, savedPaymentId);
                    }
                });

                window.onload = function() {
                    if (localStorage.getItem('sahnaj_vip_status') === 'active') {
                        var savedPlan = localStorage.getItem('sahnaj_plan') || 'VIP Access';
                        var savedPaymentId = localStorage.getItem('sahnaj_payment_id') || '';
                        activateProFeatures(savedPlan, savedPaymentId);
                    } else {
                        setTimeout(function() {
                            startSubscriptionPayment('${plan.displayName}', ${plan.price});
                        }, 400);
                    }
                };
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F0914),
            border = BorderStroke(1.dp, CyberRedBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberBlack)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Razorpay Checkout • ₹${plan.price}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = CyberTextMuted
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF261824))

                // Embedded Android WebView
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                }
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isWebViewLoading = false
                                    }

                                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                        return false
                                    }

                                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                                        try {
                                            view?.destroy()
                                        } catch (_: Exception) {}
                                        isWebViewLoading = false
                                        return true
                                    }
                                }

                                addJavascriptInterface(
                                    object {
                                        @JavascriptInterface
                                        fun onPaymentSuccess(paymentId: String, orderId: String, signature: String) {
                                            post {
                                                onPaymentSuccess(paymentId, orderId, signature)
                                            }
                                        }

                                        @JavascriptInterface
                                        fun onPaymentError(code: Int, description: String) {
                                            post {
                                                onPaymentError(code, description)
                                            }
                                        }

                                        @JavascriptInterface
                                        fun onPaymentCancelled() {
                                            post {
                                                onDismiss()
                                            }
                                        }
                                    },
                                    "AndroidInterface"
                                )

                                loadDataWithBaseURL("https://checkout.razorpay.com", checkoutHtml, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isWebViewLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = CyberRedBright,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferences
import com.example.data.model.SubscriptionPlan
import com.example.data.repository.PaymentRequestResult
import com.example.data.repository.SubscriptionRepository
import com.example.data.repository.UserSubscription
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubscriptionUiState(
    val plans: List<SubscriptionPlan> = SubscriptionPlan.DEFAULT_PLANS,
    val userSubscription: UserSubscription = UserSubscription(),
    val isLoading: Boolean = false,
    val isSubmittingPayment: Boolean = false,
    val selectedPlanForPayment: SubscriptionPlan? = null,
    val showRazorpayModal: Boolean = false,
    val selectedPlanForRazorpay: SubscriptionPlan? = null,
    val showAllPlans: Boolean = false,
    val errorMessage: String? = null,
    val confirmationMessage: String? = null,
    val isPaymentSuccessCelebration: Boolean = false
)

class SubscriptionViewModel(
    private val userPreferences: UserPreferences,
    private val subscriptionRepository: SubscriptionRepository = SubscriptionRepository(userPreferences)
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SubscriptionUiState(
            plans = SubscriptionPlan.DEFAULT_PLANS
        )
    )
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val plans = subscriptionRepository.getSubscriptionPlans()
            val userSub = subscriptionRepository.checkUserSubscription()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    plans = if (plans.isNotEmpty()) plans else SubscriptionPlan.DEFAULT_PLANS,
                    userSubscription = userSub,
                    showAllPlans = !userSub.isSubscribed
                )
            }
        }
    }

    fun toggleShowAllPlans() {
        _uiState.update { it.copy(showAllPlans = !it.showAllPlans) }
    }

    /**
     * Trigger Razorpay Checkout for the selected plan
     */
    fun startRazorpayPayment(plan: SubscriptionPlan) {
        _uiState.update {
            it.copy(
                selectedPlanForRazorpay = plan,
                showRazorpayModal = true,
                errorMessage = null,
                confirmationMessage = null
            )
        }
    }

    fun dismissRazorpayModal() {
        _uiState.update {
            it.copy(
                showRazorpayModal = false,
                selectedPlanForRazorpay = null
            )
        }
    }

    fun onRazorpaySuccess(paymentId: String, orderId: String = "", signature: String = "") {
        val selectedPlan = _uiState.value.selectedPlanForRazorpay ?: _uiState.value.plans.firstOrNull() ?: return
        _uiState.update {
            it.copy(
                showRazorpayModal = false,
                isSubmittingPayment = true
            )
        }

        viewModelScope.launch {
            val result = subscriptionRepository.activateSubscriptionFromRazorpay(
                plan = selectedPlan,
                paymentId = paymentId,
                orderId = orderId,
                signature = signature
            )

            result.onSuccess { updatedSub ->
                _uiState.update {
                    it.copy(
                        isSubmittingPayment = false,
                        userSubscription = updatedSub,
                        selectedPlanForRazorpay = null,
                        isPaymentSuccessCelebration = true,
                        confirmationMessage = "बधाई हो! आपका ${updatedSub.planDisplayName} प्लान सफलतापूर्वक एक्टिवेट हो गया है। सहनाज वॉयस वेक-वर्ड और सभी प्रीमियम फीचर्स अनलॉक कर दिए गए हैं।",
                        errorMessage = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isSubmittingPayment = false,
                        errorMessage = "Payment सफल हुआ लेकिन एक्टिवेशन में देरी: ${err.localizedMessage ?: "कृपया सपोर्ट से संपर्क करें।"}"
                    )
                }
            }
        }
    }

    fun onRazorpayError(errorCode: Int, description: String) {
        _uiState.update {
            it.copy(
                showRazorpayModal = false,
                errorMessage = "Payment असफल (Code $errorCode): $description"
            )
        }
    }

    fun selectPlanForPayment(plan: SubscriptionPlan) {
        startRazorpayPayment(plan)
    }

    fun dismissPaymentSheet() {
        _uiState.update {
            it.copy(
                selectedPlanForPayment = null,
                showRazorpayModal = false,
                selectedPlanForRazorpay = null,
                errorMessage = null
            )
        }
    }

    fun submitPayment(transactionRef: String) {
        val selectedPlan = _uiState.value.selectedPlanForPayment ?: return
        val trimmed = transactionRef.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "कृपया UPI Transaction Reference / UTR Number दर्ज करें।") }
            return
        }

        _uiState.update { it.copy(isSubmittingPayment = true, errorMessage = null) }

        viewModelScope.launch {
            val result = subscriptionRepository.submitPaymentRequest(selectedPlan, trimmed)
            when (result) {
                is PaymentRequestResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingPayment = false,
                            selectedPlanForPayment = null,
                            confirmationMessage = result.message,
                            errorMessage = null
                        )
                    }
                }
                is PaymentRequestResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingPayment = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearConfirmationMessage() {
        _uiState.update { it.copy(confirmationMessage = null, isPaymentSuccessCelebration = false) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}


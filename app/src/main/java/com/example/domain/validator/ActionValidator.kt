package com.example.domain.validator

import com.example.data.model.ActionType
import com.example.data.model.RiskLevel
import com.example.data.model.StructuredAction

class ActionValidator {

    private val allowedActions = setOf(
        ActionType.OPEN_APP,
        ActionType.CALL_CONTACT,
        ActionType.DIAL_NUMBER,
        ActionType.MAKE_CALL,
        ActionType.SEND_SMS,
        ActionType.SEND_WHATSAPP_MESSAGE,
        ActionType.WHATSAPP_MESSAGE,
        ActionType.SEND_WHATSAPP,
        ActionType.YOUTUBE_SEARCH,
        ActionType.PLAY_YOUTUBE,
        ActionType.WEB_SEARCH,
        ActionType.FIND_AND_TAP,
        ActionType.FIND_AND_TYPE,
        ActionType.OPEN_SETTINGS,
        ActionType.DEVICE_SETTING,
        ActionType.SET_ALARM,
        ActionType.SET_REMINDER,
        ActionType.GO_HOME,
        ActionType.GO_BACK,
        ActionType.OPEN_RECENTS,
        ActionType.SCROLL_UP,
        ActionType.SCROLL_DOWN,
        ActionType.SWIPE_LEFT,
        ActionType.SWIPE_RIGHT,
        ActionType.TAP_TEXT,
        ActionType.LONG_PRESS,
        ActionType.TYPE_TEXT,
        ActionType.OPEN_NOTIFICATION_PANEL,
        ActionType.OPEN_QUICK_SETTINGS,
        ActionType.STOP_ACTION,
        ActionType.ASK_CONFIRMATION,
        ActionType.SPEAK_TEXT,
        ActionType.GENERAL_QUESTION,
        ActionType.GENERAL_QNA,
        ActionType.SYSTEM_DIAGNOSTICS,
        ActionType.MORNING_BRIEFING,
        ActionType.NIGHT_ROUTINE,
        ActionType.OPEN_PAYWALL,
        ActionType.SUBSCRIPTION_QUERY
    )

    private val sensitiveKeywords = listOf(
        "password", "pin", "otp", "cvv", "credit card", "debit card", "upi pin", "bank",
        "passcode", "secret", "netbanking"
    )

    fun validate(action: StructuredAction): ValidationResult {
        // 1. Whitelist validation
        if (action.action !in allowedActions) {
            return ValidationResult.Invalid("Action '${action.action}' is not supported or prohibited for safety.")
        }

        // 2. Sensitive text entry guardrail
        if (action.action == ActionType.TYPE_TEXT || action.action == ActionType.FIND_AND_TYPE) {
            val textToType = (action.target + " " + (action.parameters["text"] ?: "")).lowercase()
            for (keyword in sensitiveKeywords) {
                if (textToType.contains(keyword)) {
                    return ValidationResult.Invalid("Automated typing of passwords, PINs, OTPs, or financial secrets is strictly prohibited for your security.")
                }
            }
        }

        // 3. Target requirements
        when (action.action) {
            ActionType.OPEN_APP -> {
                if (action.target.isBlank()) {
                    return ValidationResult.Invalid("No application name specified.")
                }
            }
            ActionType.SEND_WHATSAPP_MESSAGE, ActionType.WHATSAPP_MESSAGE -> {
                if (action.target.isBlank()) {
                    return ValidationResult.Invalid("No recipient contact specified for WhatsApp.")
                }
            }
            ActionType.YOUTUBE_SEARCH, ActionType.WEB_SEARCH -> {
                if (action.target.isBlank()) {
                    return ValidationResult.Invalid("No search query specified.")
                }
            }
            ActionType.CALL_CONTACT -> {
                if (action.target.isBlank()) {
                    return ValidationResult.Invalid("No contact name specified for calling.")
                }
            }
            ActionType.DIAL_NUMBER -> {
                if (action.target.isBlank() || !action.target.any { it.isDigit() }) {
                    return ValidationResult.Invalid("Invalid phone number provided.")
                }
            }
            ActionType.SEND_SMS -> {
                if (action.target.isBlank()) {
                    return ValidationResult.Invalid("No recipient specified for SMS.")
                }
            }
            ActionType.TAP_TEXT, ActionType.LONG_PRESS, ActionType.FIND_AND_TAP -> {
                if (action.target.isBlank()) {
                    return ValidationResult.Invalid("No target UI text specified to interact with.")
                }
            }
            else -> {}
        }

        return ValidationResult.Valid
    }
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
}

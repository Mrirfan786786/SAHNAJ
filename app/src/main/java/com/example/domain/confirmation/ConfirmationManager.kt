package com.example.domain.confirmation

import com.example.data.model.ActionType
import com.example.data.model.RiskLevel
import com.example.data.model.StructuredAction

class ConfirmationManager {

    private var pendingAction: StructuredAction? = null

    val hasPendingAction: Boolean
        get() = pendingAction != null

    val currentPendingAction: StructuredAction?
        get() = pendingAction

    fun requiresConfirmation(action: StructuredAction): Boolean {
        if (action.action == ActionType.SEND_WHATSAPP_MESSAGE || action.action == ActionType.WHATSAPP_MESSAGE) {
            val autoSend = action.parameters["send"]?.toBoolean() ?: false
            return action.requiresConfirmation || autoSend || action.riskLevel == RiskLevel.HIGH
        }
        return action.requiresConfirmation || action.riskLevel == RiskLevel.HIGH ||
                action.action == ActionType.CALL_CONTACT ||
                action.action == ActionType.DIAL_NUMBER ||
                action.action == ActionType.SEND_SMS
    }

    fun setPendingAction(action: StructuredAction) {
        pendingAction = action
    }

    fun clearPendingAction() {
        pendingAction = null
    }

    fun isConfirmationAffirmative(speech: String): Boolean {
        val lower = speech.trim().lowercase()
        val affirmatives = listOf(
            "yes", "haan", "ha", "haa", "haanji", "kar do", "bhejo", "bhej do", "send karo",
            "send kar do", "call karo", "proceed", "confirm", "sahi hai", "theek hai",
            "ok", "okay", "sure", "yep", "dial karo", "bilkul", "zarur", "chalao"
        )
        return affirmatives.any { lower.contains(it) }
    }

    fun isConfirmationNegative(speech: String): Boolean {
        val lower = speech.trim().lowercase()
        val negatives = listOf(
            "no", "nahi", "na", "mat karo", "mat bhejo", "cancel", "stop", "ruko", "rok do",
            "don't", "abort", "nope", "nahi karna", "mat send karo", "rehne do"
        )
        return negatives.any { lower.contains(it) }
    }
}

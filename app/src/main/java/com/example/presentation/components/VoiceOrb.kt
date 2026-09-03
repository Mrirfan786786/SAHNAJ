package com.example.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.example.presentation.viewmodel.AssistantStatus

/**
 * Animated Cyber Girl Voice Avatar component (formerly VoiceOrb).
 * Seamlessly delegates to HolographicVoiceOrb to provide live 17-year-old Cyber Girl Avatar,
 * theme customization, breathing animation, holographic aura rings, and lip-sync.
 */
@Composable
fun VoiceOrb(
    status: AssistantStatus,
    rmsDb: Float,
    isSpeaking: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    customColor: Color? = null,
    orbSize: Dp? = null
) {
    HolographicVoiceOrb(
        status = status,
        rmsDb = rmsDb,
        isSpeaking = isSpeaking,
        onClick = onClick,
        modifier = modifier,
        size = size,
        customColor = customColor,
        orbSize = orbSize
    )
}

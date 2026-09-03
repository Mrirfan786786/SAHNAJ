package com.example.services.voiceinteraction

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.util.Log

class SahNajVoiceInteractionService : VoiceInteractionService() {

    companion object {
        private const val TAG = "SAHNAJ_VOICE_SERVICE"

        fun isAssistantActive(context: Context): Boolean {
            return isActiveService(
                context,
                ComponentName(context, SahNajVoiceInteractionService::class.java)
            )
        }
    }

    override fun onReady() {
        super.onReady()
        Log.d(TAG, "SahNajVoiceInteractionService is ready. Is active assistant: ${isAssistantActive(this)}")
    }

    override fun onShutdown() {
        super.onShutdown()
        Log.d(TAG, "SahNajVoiceInteractionService is shutting down")
    }

    /**
     * Helper to show the voice session on demand
     */
    fun triggerVoiceSession(args: Bundle = Bundle()) {
        try {
            showSession(
                args,
                VoiceInteractionSession.SHOW_WITH_ASSIST or VoiceInteractionSession.SHOW_WITH_SCREENSHOT
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering voice session: ${e.message}", e)
        }
    }
}

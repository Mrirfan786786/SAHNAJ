package com.example.services.voiceinteraction

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log

class SahNajVoiceInteractionSessionService : VoiceInteractionSessionService() {

    companion object {
        private const val TAG = "SAHNAJ_SESSION_SERVICE"
    }

    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        Log.d(TAG, "onNewSession created with args: $args")
        return SahNajVoiceInteractionSession(this)
    }
}

package com.example.services.voiceinteraction

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionService
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.SahNajApplication

class SahNajRecognitionService : RecognitionService() {

    companion object {
        private const val TAG = "SAHNAJ_RECOGNITION"
    }

    private var activeCallback: Callback? = null

    override fun onStartListening(recognizerIntent: Intent?, listener: Callback?) {
        Log.d(TAG, "onStartListening invoked by system/client")
        activeCallback = listener
        listener?.readyForSpeech(Bundle())

        val app = applicationContext as? SahNajApplication
        if (app != null) {
            app.speechRecognizerManager.startListening(continuous = false)
        }
    }

    override fun onStopListening(listener: Callback?) {
        Log.d(TAG, "onStopListening invoked")
        val app = applicationContext as? SahNajApplication
        app?.speechRecognizerManager?.stopListening()
        listener?.endOfSpeech()
    }

    override fun onCancel(listener: Callback?) {
        Log.d(TAG, "onCancel invoked")
        val app = applicationContext as? SahNajApplication
        app?.speechRecognizerManager?.stopListening()
        activeCallback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        activeCallback = null
    }
}

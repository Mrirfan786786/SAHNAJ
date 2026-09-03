package com.example.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.SahNajApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * HandsFreeCallManager:
 * Intercepts incoming phone calls, announces caller names aloud via TTS,
 * and allows accepting/rejecting calls via hands-free voice commands.
 */
class HandsFreeCallManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _incomingCaller = MutableStateFlow<String?>(null)
    val incomingCaller: StateFlow<String?> = _incomingCaller.asStateFlow()

    private val _isCallRinging = MutableStateFlow(false)
    val isCallRinging: StateFlow<Boolean> = _isCallRinging.asStateFlow()

    private var telephonyReceiver: BroadcastReceiver? = null

    fun registerCallListener() {
        if (telephonyReceiver != null) return

        telephonyReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
                    val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                    val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                    when (stateStr) {
                        TelephonyManager.EXTRA_STATE_RINGING -> {
                            _isCallRinging.value = true
                            val callerDisplay = incomingNumber ?: "Incoming Call"
                            _incomingCaller.value = callerDisplay
                            announceIncomingCall(callerDisplay)
                        }
                        TelephonyManager.EXTRA_STATE_OFFHOOK,
                        TelephonyManager.EXTRA_STATE_IDLE -> {
                            _isCallRinging.value = false
                            _incomingCaller.value = null
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
        try {
            context.registerReceiver(telephonyReceiver, filter)
            Log.d(TAG, "TelephonyReceiver successfully registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register TelephonyReceiver", e)
        }
    }

    fun unregisterCallListener() {
        telephonyReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) {}
        }
        telephonyReceiver = null
    }

    private fun announceIncomingCall(caller: String) {
        val app = context.applicationContext as? SahNajApplication ?: return
        scope.launch {
            try {
                val contactName = app.contactResolver.resolveCallerName(caller)
                val displayName = contactName ?: caller
                val speech = "Boss, incoming call from $displayName. Bolen, call pick karun ya cut karun?"
                app.textToSpeechManager.speak(speech)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to announce incoming call", e)
            }
        }
    }

    /**
     * Answers the currently ringing incoming phone call.
     */
    @SuppressLint("MissingPermission")
    fun answerCall(): Boolean {
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telecomManager?.acceptRingingCall()
                _isCallRinging.value = false
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to accept call", e)
            false
        }
    }

    /**
     * Rejects/Ends the currently ringing phone call.
     */
    @SuppressLint("MissingPermission")
    fun rejectCall(): Boolean {
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                telecomManager?.endCall()
                _isCallRinging.value = false
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reject call", e)
            false
        }
    }

    companion object {
        private const val TAG = "HandsFreeCallManager"
    }
}

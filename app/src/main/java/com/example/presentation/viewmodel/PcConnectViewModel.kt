package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.services.pcconnect.PcClientInfo
import com.example.services.pcconnect.PcCommandLog
import com.example.services.pcconnect.PcConnectServer
import com.example.services.pcconnect.PcConnectionStatus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PcConnectViewModel(
    application: Application
) : AndroidViewModel(application) {

    val server = PcConnectServer(application)

    val pairingCode: StateFlow<String> = server.pairingCode
    val localIp: StateFlow<String?> = server.localIp
    val connectionStatus: StateFlow<PcConnectionStatus> = server.connectionStatus
    val isServerRunning: StateFlow<Boolean> = server.isServerRunning
    val connectedClient: StateFlow<PcClientInfo?> = server.connectedClient
    val commandLogs: StateFlow<List<PcCommandLog>> = server.commandLogs
    val port: Int = server.port

    init {
        // Start server and refresh IP on init
        startServer()
    }

    fun startServer() {
        server.refreshIpAddress()
        server.refreshPairingCode()
        server.startServer()
    }

    fun stopServer() {
        server.stopServer()
    }

    fun disconnectClient() {
        server.disconnectClient()
    }

    fun refreshPairingCode() {
        server.refreshPairingCode()
    }

    fun refreshNetwork() {
        server.refreshIpAddress()
    }

    override fun onCleared() {
        super.onCleared()
        // Stop server when ViewModel is cleared
        server.stopServer()
    }
}

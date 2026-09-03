package com.example.services.pcconnect

data class PcClientInfo(
    val clientName: String,
    val clientIp: String,
    val connectedAt: String
)

data class PcCommandLog(
    val id: String,
    val timestamp: String,
    val command: String,
    val response: String,
    val success: Boolean
)

sealed class PcConnectionStatus {
    data object Disconnected : PcConnectionStatus()
    data class WaitingForPairing(val ipAddress: String?, val port: Int, val pairingCode: String) : PcConnectionStatus()
    data class Connected(val client: PcClientInfo) : PcConnectionStatus()
}

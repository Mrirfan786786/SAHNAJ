package com.example.services.pcconnect

import android.content.Context
import android.util.Log
import com.example.SahNajApplication
import com.example.data.model.ExecutionResult
import com.example.data.model.ResultStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class PcConnectServer(
    private val context: Context,
    val port: Int = 8765
) {
    private val TAG = "PcConnectServer"

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _pairingCode = MutableStateFlow(generateRandomPairingCode())
    val pairingCode: StateFlow<String> = _pairingCode.asStateFlow()

    private val _localIp = MutableStateFlow<String?>(null)
    val localIp: StateFlow<String?> = _localIp.asStateFlow()

    private val _connectionStatus = MutableStateFlow<PcConnectionStatus>(PcConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<PcConnectionStatus> = _connectionStatus.asStateFlow()

    private val _isServerRunning = MutableStateFlow(false)
    val isServerRunning: StateFlow<Boolean> = _isServerRunning.asStateFlow()

    private val _connectedClient = MutableStateFlow<PcClientInfo?>(null)
    val connectedClient: StateFlow<PcClientInfo?> = _connectedClient.asStateFlow()

    private val _commandLogs = MutableStateFlow<List<PcCommandLog>>(emptyList())
    val commandLogs: StateFlow<List<PcCommandLog>> = _commandLogs.asStateFlow()

    fun refreshPairingCode(): String {
        val newCode = generateRandomPairingCode()
        _pairingCode.value = newCode
        if (_isServerRunning.value && _connectedClient.value == null) {
            _connectionStatus.value = PcConnectionStatus.WaitingForPairing(_localIp.value, port, newCode)
        }
        return newCode
    }

    fun startServer() {
        if (_isServerRunning.value) return

        refreshIpAddress()
        val code = _pairingCode.value
        _connectionStatus.value = PcConnectionStatus.WaitingForPairing(_localIp.value, port, code)

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(port)
                _isServerRunning.value = true
                Log.d(TAG, "PC Connect server started on port $port, IP: ${_localIp.value}, pairing code: $code")

                while (_isServerRunning.value && serverSocket?.isClosed == false) {
                    try {
                        val clientSocket = serverSocket?.accept() ?: break
                        scope.launch {
                            handleClientSocket(clientSocket)
                        }
                    } catch (e: Exception) {
                        if (!_isServerRunning.value || serverSocket?.isClosed == true) {
                            break
                        }
                        Log.e(TAG, "Socket accept error", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start server on port $port", e)
                _isServerRunning.value = false
                _connectionStatus.value = PcConnectionStatus.Disconnected
            }
        }
    }

    fun stopServer() {
        _isServerRunning.value = false
        _connectedClient.value = null
        _connectionStatus.value = PcConnectionStatus.Disconnected
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing server socket", e)
        }
        serverSocket = null
        serverJob?.cancel()
        serverJob = null
        Log.d(TAG, "PC Connect server stopped")
    }

    fun disconnectClient() {
        val client = _connectedClient.value
        _connectedClient.value = null
        _connectionStatus.value = PcConnectionStatus.WaitingForPairing(_localIp.value, port, _pairingCode.value)
        addLog(
            command = "DISCONNECT",
            response = "PC session ended (${client?.clientName ?: "PC"})",
            success = true
        )
    }

    fun refreshIpAddress() {
        _localIp.value = getDeviceWifiIp()
    }

    private fun handleClientSocket(socket: Socket) {
        val clientIp = socket.inetAddress?.hostAddress ?: "Unknown IP"
        try {
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val outputStream = socket.getOutputStream()

            val requestLine = reader.readLine() ?: run {
                socket.close()
                return
            }

            val parts = requestLine.split(" ")
            if (parts.size < 2) {
                socket.close()
                return
            }

            val method = parts[0].uppercase(Locale.ROOT)
            val fullPath = parts[1]

            // Read headers to check content-length
            var contentLength = 0
            var headerLine: String?
            while (reader.readLine().also { headerLine = it } != null) {
                if (headerLine.isNullOrBlank()) break
                if (headerLine!!.lowercase(Locale.ROOT).startsWith("content-length:")) {
                    contentLength = headerLine!!.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            // Read body if POST
            val body = if (contentLength > 0) {
                val buffer = CharArray(contentLength)
                var bytesRead = 0
                while (bytesRead < contentLength) {
                    val read = reader.read(buffer, bytesRead, contentLength - bytesRead)
                    if (read == -1) break
                    bytesRead += read
                }
                String(buffer, 0, bytesRead)
            } else ""

            // Handle OPTIONS for CORS
            if (method == "OPTIONS") {
                sendHttpResponse(outputStream, 200, "text/plain", "OK")
                socket.close()
                return
            }

            val path = if (fullPath.contains("?")) fullPath.substringBefore("?") else fullPath
            val queryParams = parseQueryParams(fullPath)

            when (path) {
                "/", "/index.html" -> {
                    val html = buildWebDashboardHtml(_localIp.value ?: "127.0.0.1", port)
                    sendHttpResponse(outputStream, 200, "text/html; charset=UTF-8", html)
                }

                "/status" -> {
                    val isConnected = _connectedClient.value != null
                    val json = JSONObject().apply {
                        put("running", _isServerRunning.value)
                        put("connected", isConnected)
                        put("client", _connectedClient.value?.clientName ?: "")
                        put("clientIp", _connectedClient.value?.clientIp ?: "")
                        put("serverIp", _localIp.value ?: "")
                        put("port", port)
                    }
                    sendHttpResponse(outputStream, 200, "application/json", json.toString())
                }

                "/pair" -> {
                    var providedCode = queryParams["code"] ?: ""
                    var clientName = queryParams["client"] ?: queryParams["name"] ?: "PC Client ($clientIp)"

                    if (body.isNotBlank()) {
                        try {
                            val jsonBody = JSONObject(body)
                            if (jsonBody.has("code")) providedCode = jsonBody.getString("code")
                            if (jsonBody.has("client")) clientName = jsonBody.getString("client")
                            if (jsonBody.has("name")) clientName = jsonBody.getString("name")
                        } catch (_: Exception) {}
                    }

                    if (providedCode.trim() == _pairingCode.value.trim()) {
                        val clientInfo = PcClientInfo(
                            clientName = clientName,
                            clientIp = clientIp,
                            connectedAt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        _connectedClient.value = clientInfo
                        _connectionStatus.value = PcConnectionStatus.Connected(clientInfo)

                        addLog(
                            command = "PAIR_REQUEST",
                            response = "Successfully paired with $clientName ($clientIp)",
                            success = true
                        )

                        val resp = JSONObject().apply {
                            put("success", true)
                            put("message", "✅ Paired successfully with SahNaj Phone Assistant")
                            put("client", clientName)
                            put("device", "SahNaj AI Phone")
                        }
                        sendHttpResponse(outputStream, 200, "application/json", resp.toString())
                    } else {
                        val resp = JSONObject().apply {
                            put("success", false)
                            put("message", "❌ Invalid pairing code. Please enter the 6-digit code shown on the phone.")
                        }
                        sendHttpResponse(outputStream, 401, "application/json", resp.toString())
                    }
                }

                "/command" -> {
                    var cmdText = queryParams["cmd"] ?: queryParams["text"] ?: queryParams["command"] ?: ""
                    var providedCode = queryParams["code"] ?: ""

                    if (body.isNotBlank()) {
                        try {
                            val jsonBody = JSONObject(body)
                            if (jsonBody.has("command")) cmdText = jsonBody.getString("command")
                            if (jsonBody.has("cmd")) cmdText = jsonBody.getString("cmd")
                            if (jsonBody.has("text")) cmdText = jsonBody.getString("text")
                            if (jsonBody.has("code")) providedCode = jsonBody.getString("code")
                        } catch (_: Exception) {}
                    }

                    val isAuthorized = _connectedClient.value != null || (providedCode.isNotBlank() && providedCode.trim() == _pairingCode.value.trim())

                    if (!isAuthorized) {
                        val resp = JSONObject().apply {
                            put("success", false)
                            put("message", "Unauthorized. Please pair with the phone first.")
                        }
                        sendHttpResponse(outputStream, 401, "application/json", resp.toString())
                    } else if (cmdText.isBlank()) {
                        val resp = JSONObject().apply {
                            put("success", false)
                            put("message", "No command text provided.")
                        }
                        sendHttpResponse(outputStream, 400, "application/json", resp.toString())
                    } else {
                        // Process the command on phone
                        scope.launch {
                            val executionResult = executePhoneCommand(cmdText)
                            addLog(
                                command = cmdText,
                                response = executionResult.spokenResponse,
                                success = executionResult.status == ResultStatus.SUCCESS
                            )

                            val resp = JSONObject().apply {
                                put("success", true)
                                put("command", cmdText)
                                put("response", executionResult.spokenResponse)
                                put("status", executionResult.status.name)
                            }
                            sendHttpResponse(outputStream, 200, "application/json", resp.toString())
                        }
                        return
                    }
                }

                "/disconnect" -> {
                    disconnectClient()
                    val resp = JSONObject().apply {
                        put("success", true)
                        put("message", "Disconnected successfully.")
                    }
                    sendHttpResponse(outputStream, 200, "application/json", resp.toString())
                }

                else -> {
                    sendHttpResponse(outputStream, 404, "application/json", "{\"error\": \"Not Found\"}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling client connection", e)
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private suspend fun executePhoneCommand(commandText: String): ExecutionResult = withContext(Dispatchers.Main) {
        val app = SahNajApplication.instance
        val assistantName = app.userPreferences.getAssistantName()

        // 1. Try local command parser
        val parsedAction = app.localCommandParser.parseLocally(commandText, assistantName)
        if (parsedAction != null) {
            val result = app.actionExecutor.execute(parsedAction)
            // Speak response if available
            if (result.spokenResponse.isNotBlank()) {
                app.textToSpeechManager.speak(result.spokenResponse)
            }
            return@withContext result
        }

        // 2. Try Gemini AI or fallback
        return@withContext try {
            val geminiResult = app.geminiRepository.parseCommand(commandText, assistantName)
            if (geminiResult.isSuccess) {
                val action = geminiResult.getOrThrow()
                val execResult = app.actionExecutor.execute(action)
                val responseToSpeak = if (execResult.spokenResponse.isNotBlank()) execResult.spokenResponse else action.spokenResponse
                if (responseToSpeak.isNotBlank()) {
                    app.textToSpeechManager.speak(responseToSpeak)
                }
                ExecutionResult(
                    status = execResult.status,
                    spokenResponse = responseToSpeak,
                    detail = execResult.detail
                )
            } else {
                val fallback = "कमांड प्रोसेस की गई: \"$commandText\""
                app.textToSpeechManager.speak(fallback)
                ExecutionResult(
                    status = ResultStatus.SUCCESS,
                    spokenResponse = fallback
                )
            }
        } catch (e: Exception) {
            val errText = "कमांड पूरी करने में समस्या आई: ${e.message}"
            ExecutionResult(
                status = ResultStatus.FAILURE,
                spokenResponse = errText,
                detail = e.message
            )
        }
    }

    private fun addLog(command: String, response: String, success: Boolean) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val newLog = PcCommandLog(
            id = UUID.randomUUID().toString(),
            timestamp = timestamp,
            command = command,
            response = response,
            success = success
        )
        _commandLogs.value = listOf(newLog) + _commandLogs.value.take(49)
    }

    private fun parseQueryParams(path: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (!path.contains("?")) return map
        val queryString = path.substringAfter("?")
        for (param in queryString.split("&")) {
            val pair = param.split("=")
            if (pair.isNotEmpty()) {
                val key = URLDecoder.decode(pair[0], "UTF-8")
                val value = if (pair.size > 1) URLDecoder.decode(pair[1], "UTF-8") else ""
                map[key] = value
            }
        }
        return map
    }

    private fun sendHttpResponse(out: OutputStream, statusCode: Int, contentType: String, content: String) {
        val bytes = content.toByteArray(Charsets.UTF_8)
        val statusText = when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            404 -> "Not Found"
            else -> "Error"
        }
        val header = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: $contentType\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
                "Access-Control-Allow-Headers: Content-Type, Authorization\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    private fun generateRandomPairingCode(): String {
        return (Random.nextInt(100000, 999999)).toString()
    }

    private fun getDeviceWifiIp(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        val host = addr.hostAddress
                        if (host != null && !host.startsWith("127.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting IP", e)
        }
        return null
    }

    private fun buildWebDashboardHtml(ip: String, port: Int): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SahNaj PC Connect Terminal</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, monospace; }
        body { background: #0D0509; color: #FFFFFF; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 100vh; }
        .container { width: 100%; max-width: 640px; background: #180C14; border: 1px solid #FF1744; border-radius: 14px; padding: 24px; box-shadow: 0 0 25px rgba(255, 23, 68, 0.25); }
        .header { text-align: center; margin-bottom: 20px; border-bottom: 1px solid #2B1624; padding-bottom: 14px; }
        .title { color: #FF1744; font-size: 22px; font-weight: bold; letter-spacing: 1px; }
        .subtitle { color: #A0909A; font-size: 13px; margin-top: 4px; }
        .status-box { display: flex; align-items: center; justify-content: space-between; background: #22101D; padding: 12px 16px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #FF1744; }
        .status-dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; margin-right: 8px; background: #FF9500; }
        .status-dot.connected { background: #30D158; box-shadow: 0 0 8px #30D158; }
        .input-group { margin-bottom: 16px; }
        label { display: block; font-size: 12px; color: #FF7597; margin-bottom: 6px; font-weight: bold; }
        input[type="text"] { width: 100%; padding: 12px; background: #0D0509; border: 1px solid #3E1E34; border-radius: 8px; color: #FFF; font-size: 15px; outline: none; }
        input[type="text"]:focus { border-color: #FF1744; box-shadow: 0 0 8px rgba(255, 23, 68, 0.4); }
        .btn { width: 100%; padding: 12px; background: #FF1744; border: none; border-radius: 8px; color: #FFF; font-size: 14px; font-weight: bold; cursor: pointer; transition: 0.2s; margin-top: 8px; }
        .btn:hover { background: #D50000; box-shadow: 0 0 12px #FF1744; }
        .btn-outline { background: transparent; border: 1px solid #FF1744; color: #FF7597; }
        .btn-outline:hover { background: #2B1220; }
        .chips { display: flex; flex-wrap: wrap; gap: 8px; margin: 12px 0; }
        .chip { background: #261120; border: 1px solid #3E1E34; color: #E0D0DA; padding: 6px 12px; border-radius: 16px; font-size: 12px; cursor: pointer; }
        .chip:hover { border-color: #FF1744; color: #FFF; }
        .terminal { background: #070205; border: 1px solid #2B1624; border-radius: 8px; padding: 14px; height: 180px; overflow-y: auto; font-family: monospace; font-size: 12px; color: #30D158; margin-top: 16px; }
        .log-entry { margin-bottom: 6px; line-height: 1.4; }
        .log-user { color: #64D2FF; }
        .log-phone { color: #30D158; }
        .log-err { color: #FF453A; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div class="title">⚡ SAHNAJ PC CONNECT</div>
            <div class="subtitle">Control your AI Voice Assistant from PC on Local Wi-Fi</div>
        </div>

        <div class="status-box">
            <div>
                <span id="statusDot" class="status-dot"></span>
                <span id="statusText">Checking status...</span>
            </div>
            <span style="font-size: 11px; color: #887780;" id="ipBadge">$ip:$port</span>
        </div>

        <div id="pairSection">
            <div class="input-group">
                <label>ENTER 6-DIGIT PAIRING CODE (FROM PHONE SCREEN):</label>
                <input type="text" id="pairCodeInput" placeholder="e.g. 123456" maxlength="6" style="letter-spacing: 4px; font-weight: bold; text-align: center; font-size: 18px;">
            </div>
            <button class="btn" onclick="pairWithPhone()">🔗 Connect to Phone</button>
        </div>

        <div id="controlSection" style="display:none;">
            <div class="input-group">
                <label>SEND VOICE/TEXT COMMAND TO PHONE:</label>
                <input type="text" id="commandInput" placeholder="e.g. open youtube, send message to Rahul, what is the weather?" onkeydown="if(event.key==='Enter') sendCommand()">
            </div>
            <button class="btn" onclick="sendCommand()">🚀 Send Command</button>

            <div class="chips">
                <div class="chip" onclick="quickSend('open youtube')">▶️ Open YouTube</div>
                <div class="chip" onclick="quickSend('open whatsapp')">💬 Open WhatsApp</div>
                <div class="chip" onclick="quickSend('open settings')">⚙️ Settings</div>
                <div class="chip" onclick="quickSend('what time is it?')">⏰ Current Time</div>
                <div class="chip" onclick="quickSend('tell me a joke')">😄 Tell a Joke</div>
            </div>

            <button class="btn btn-outline" onclick="disconnectPhone()" style="margin-top: 10px;">🔌 Disconnect</button>
        </div>

        <div class="terminal" id="terminal">
            <div class="log-entry">> Connected to local SahNaj HTTP bridge on $ip:$port</div>
            <div class="log-entry">> Waiting for pairing authentication...</div>
        </div>
    </div>

    <script>
        let currentCode = localStorage.getItem('sahnaj_pair_code') || '';

        function log(msg, type='info') {
            const term = document.getElementById('terminal');
            const d = document.createElement('div');
            d.className = 'log-entry ' + (type === 'user' ? 'log-user' : type === 'err' ? 'log-err' : 'log-phone');
            const time = new Date().toLocaleTimeString();
            d.textContent = '[' + time + '] ' + msg;
            term.appendChild(d);
            term.scrollTop = term.scrollHeight;
        }

        async function checkStatus() {
            try {
                const res = await fetch('/status');
                const data = await res.json();
                if (data.connected) {
                    document.getElementById('statusDot').className = 'status-dot connected';
                    document.getElementById('statusText').textContent = '✅ Connected (' + (data.client || 'PC') + ')';
                    document.getElementById('pairSection').style.display = 'none';
                    document.getElementById('controlSection').style.display = 'block';
                } else {
                    document.getElementById('statusDot').className = 'status-dot';
                    document.getElementById('statusText').textContent = '⏳ Waiting for Pairing';
                    document.getElementById('pairSection').style.display = 'block';
                    document.getElementById('controlSection').style.display = 'none';
                }
            } catch(e) {
                document.getElementById('statusText').textContent = '❌ Cannot reach phone';
            }
        }

        async function pairWithPhone() {
            const code = document.getElementById('pairCodeInput').value.trim();
            if (!code || code.length !== 6) {
                alert('Please enter a valid 6-digit code');
                return;
            }
            log('Sending pairing request with code: ' + code, 'user');
            try {
                const res = await fetch('/pair', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ code: code, client: 'PC Web Browser' })
                });
                const data = await res.json();
                if (data.success) {
                    currentCode = code;
                    localStorage.setItem('sahnaj_pair_code', code);
                    log('✅ ' + data.message, 'phone');
                    checkStatus();
                } else {
                    log('❌ ' + data.message, 'err');
                    alert(data.message);
                }
            } catch(e) {
                log('❌ Connection failed: ' + e.message, 'err');
            }
        }

        async function sendCommand() {
            const input = document.getElementById('commandInput');
            const cmd = input.value.trim();
            if (!cmd) return;
            input.value = '';
            quickSend(cmd);
        }

        async function quickSend(cmd) {
            log('PC > ' + cmd, 'user');
            try {
                const res = await fetch('/command', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({ code: currentCode, command: cmd })
                });
                const data = await res.json();
                if (data.success) {
                    log('SahNaj > ' + data.response, 'phone');
                } else {
                    log('Error > ' + data.message, 'err');
                }
            } catch(e) {
                log('Failed to send command: ' + e.message, 'err');
            }
        }

        async function disconnectPhone() {
            await fetch('/disconnect', {method: 'POST'});
            localStorage.removeItem('sahnaj_pair_code');
            currentCode = '';
            log('Disconnected session.', 'user');
            checkStatus();
        }

        checkStatus();
        setInterval(checkStatus, 4000);
    </script>
</body>
</html>
        """.trimIndent()
    }
}

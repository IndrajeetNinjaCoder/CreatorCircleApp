package com.cc.creatorcircle.data.websocket


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString
import android.util.Log

class WebSocketService {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    // WebSocket connection states
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // Message flow
    private val _messages = MutableStateFlow<String?>(null)
    val messages: StateFlow<String?> = _messages

    // Error flow
    private val _errors = MutableStateFlow<String?>(null)
    val errors: StateFlow<String?> = _errors

    enum class ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        ERROR
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connection opened")
            _connectionState.value = ConnectionState.CONNECTED
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "Message received: $text")
            _messages.value = text
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            Log.d("WebSocket", "Binary message received")
            _messages.value = bytes.utf8()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Connection closing: $code / $reason")
            _connectionState.value = ConnectionState.DISCONNECTED
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Connection closed: $code / $reason")
            _connectionState.value = ConnectionState.DISCONNECTED
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Connection failed", t)
            _connectionState.value = ConnectionState.ERROR
            _errors.value = t.message
        }
    }

    fun connect(token: String) {
        if (webSocket != null) {
            disconnect()
        }

        _connectionState.value = ConnectionState.CONNECTING

        val request = Request.Builder()
            .url("wss://creatorcircle.in/ws") // Adjust URL as needed
            .addHeader("Authorization", token)
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendMessage(message: String): Boolean {
        return webSocket?.send(message) ?: false
    }

    fun sendMessage(bytes: ByteString): Boolean {
        return webSocket?.send(bytes) ?: false
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }

    fun isConnected(): Boolean {
        return _connectionState.value == ConnectionState.CONNECTED
    }
}
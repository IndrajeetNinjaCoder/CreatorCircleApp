package com.cc.creatorcircle.data.socket

import android.util.Log
import okhttp3.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for WebSocket
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .pingInterval(30, TimeUnit.SECONDS) // Keep connection alive
        .build()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private var userId: Int = 3
    private var sessionId: Int = 188

    companion object {
        private const val TAG = "WebSocketManager"
        private const val WS_URL = "https://creatorcircle.in/" // Add correct path if needed
    }

    fun connect(url: String = WS_URL) {
        Log.d(TAG, "Attempting to connect to: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket Connected: ${response.message}")
                _connectionState.value = ConnectionState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message received: $text")
                handleIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}", t)
                Log.e(TAG, "Response: ${response?.message}")
                _connectionState.value = ConnectionState.ERROR
            }
        })
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val json = JSONObject(text)
            Log.d(TAG, "Parsing JSON: $json")

            when {
                // Handle ai_chat_chunk events
                json.has("content") && json.has("assistant_message_id") -> {
                    val content = json.getString("content")
                    val messageId = json.getString("assistant_message_id")

                    Log.d(TAG, "Chunk received - ID: $messageId, Content: $content")

                    // Append chunk to existing assistant message or create new one
                    val currentMessages = _messages.value.toMutableList()
                    val existingIndex = currentMessages.indexOfLast {
                        it.isAssistant && it.id == messageId
                    }

                    if (existingIndex != -1) {
                        currentMessages[existingIndex] = currentMessages[existingIndex].copy(
                            content = currentMessages[existingIndex].content + content
                        )
                    } else {
                        currentMessages.add(
                            ChatMessage(
                                id = messageId,
                                content = content,
                                isAssistant = true,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    _messages.value = currentMessages
                }

                // Handle ai_chat_complete event
                json.has("status") && json.getString("status") == "complete" -> {
                    Log.d(TAG, "Message complete")
                    // Message is complete, no action needed as chunks already handled
                }

                // Handle ai_chat_response event
                json.has("message") && json.has("assistant_message_id") -> {
                    val fullMessage = json.getString("message")
                    val messageId = json.getString("assistant_message_id")

                    Log.d(TAG, "Full message received - ID: $messageId")

                    val currentMessages = _messages.value.toMutableList()
                    val existingIndex = currentMessages.indexOfLast {
                        it.isAssistant && it.id == messageId
                    }

                    if (existingIndex != -1) {
                        currentMessages[existingIndex] = currentMessages[existingIndex].copy(
                            content = fullMessage
                        )
                    } else {
                        currentMessages.add(
                            ChatMessage(
                                id = messageId,
                                content = fullMessage,
                                isAssistant = true,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    _messages.value = currentMessages
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message: ${e.message}", e)
        }
    }

    fun sendMessage(message: String) {
        val payload = JSONObject().apply {
            put("userId", userId)
            put("session_id", sessionId)
            put("message", message)
            put("event_type", "new_user_message")
        }

        val success = webSocket?.send(payload.toString()) ?: false
        Log.d(TAG, "Message sent: $success - $payload")

        if (success) {
            // Add user message to list
            val currentMessages = _messages.value.toMutableList()
            currentMessages.add(
                ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    content = message,
                    isAssistant = false,
                    timestamp = System.currentTimeMillis()
                )
            )
            _messages.value = currentMessages
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        Log.d(TAG, "Disconnected")
    }
}

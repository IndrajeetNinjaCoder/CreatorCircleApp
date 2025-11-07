package com.cc.creatorcircle.data.socket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject
import java.net.URISyntaxException

class SocketIOManager {
    private var socket: Socket? = null

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    // NEW: StateFlow to emit new session ID
    private val _newSessionId = MutableStateFlow<Int?>(null)
    val newSessionId: StateFlow<Int?> = _newSessionId

    private var userId: Int? = null
    private var sessionId: Int? = null

    companion object {
        private const val TAG = "SocketIOManager"
        private const val SERVER_URL = "https://creatorcircle.in"
    }

    fun connect(userId: Int, sessionId: Int, url: String = SERVER_URL) {
        this.userId = userId
        this.sessionId = sessionId

        try {
            Log.d(TAG, "Attempting to connect to: $url with userId: $userId, sessionId: $sessionId")

            val options = IO.Options().apply {
                transports = arrayOf("websocket", "polling")
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 20000
                forceNew = true
                path = "/socket.io/"
            }

            socket = IO.socket(url, options)

            socket?.apply {
                on(Socket.EVENT_CONNECT, onConnect)
                on(Socket.EVENT_DISCONNECT, onDisconnect)
                on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                on("ai_chat_chunk", onChatChunk)
                on("ai_chat_complete", onChatComplete)
                on("ai_chat_response", onChatResponse)

                connect()
            }
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Invalid URL: ${e.message}", e)
            _connectionState.value = ConnectionState.ERROR
        } catch (e: Exception) {
            Log.e(TAG, "Connection error: ${e.message}", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }

    private val onConnect = Emitter.Listener {
        Log.d(TAG, "Socket.IO Connected successfully!")
        Log.d(TAG, "Socket ID: ${socket?.id()}")
        _connectionState.value = ConnectionState.CONNECTED
    }

    private val onDisconnect = Emitter.Listener { args ->
        Log.d(TAG, "Socket.IO Disconnected: ${args.firstOrNull()}")
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = args.firstOrNull()
        Log.e(TAG, "Socket.IO Connection Error: $error")
        Log.e(TAG, "Error type: ${error?.javaClass?.name}")
        _connectionState.value = ConnectionState.ERROR
    }

    private val onChatChunk = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            Log.d(TAG, "Chat chunk received: $data")

            val content = data.getString("content")
            val messageId = data.getString("assistant_message_id")

            // Check for session_id in chunks (just in case)
            if (data.has("session_id")) {
                val receivedSessionId = data.getInt("session_id")
                Log.d(TAG, "✅ Session ID found in chat chunk: $receivedSessionId")

                if (this.sessionId == null || this.sessionId == -1 || this.sessionId != receivedSessionId) {
                    Log.d(TAG, "✅ Emitting new session ID from chunk: $receivedSessionId")
                    _newSessionId.value = receivedSessionId
                    this.sessionId = receivedSessionId
                }
            }

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
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling chat chunk: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
    }

    fun clearNewSessionId() {
        _newSessionId.value = null
    }

    private val onChatComplete = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            Log.d(TAG, "Chat complete: $data")

            // CRITICAL: Check for session_id in the response
            if (data.has("session_id")) {
                val receivedSessionId = data.getInt("session_id")
                Log.d(TAG, "✅ Session ID found in chat complete: $receivedSessionId")
                Log.d(TAG, "Current sessionId before update: ${this.sessionId}")

                // Only emit if we don't already have a valid session ID (or if it's -1)
                if (this.sessionId == null || this.sessionId == -1 || this.sessionId != receivedSessionId) {
                    Log.d(TAG, "✅ Emitting new session ID to UI: $receivedSessionId")
                    _newSessionId.value = receivedSessionId
                    this.sessionId = receivedSessionId
                } else {
                    Log.d(TAG, "⚠️ Session ID already set to: ${this.sessionId}, not emitting")
                }
            } else {
                Log.w(TAG, "⚠️ No session_id found in chat complete response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling chat complete: ${e.message}", e)
            e.printStackTrace()
        }
    }

    private val onChatResponse = Emitter.Listener { args ->
        try {
            val data = args[0] as JSONObject
            Log.d(TAG, "Chat response received: $data")

            val fullMessage = data.getString("message")
            val messageId = data.getString("assistant_message_id")

            // Check for session_id
            if (data.has("session_id")) {
                val receivedSessionId = data.getInt("session_id")
                Log.d(TAG, "✅ Session ID found in chat response: $receivedSessionId")

                if (this.sessionId == null || this.sessionId == -1 || this.sessionId != receivedSessionId) {
                    Log.d(TAG, "✅ Emitting new session ID from response: $receivedSessionId")
                    _newSessionId.value = receivedSessionId
                    this.sessionId = receivedSessionId
                }
            }

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
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling chat response: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun sendMessage(userId: Int, sessionId: Int, message: String) {
        if (socket?.connected() != true) {
            Log.e(TAG, "Cannot send message: Socket not connected")
            return
        }

        val payload = JSONObject().apply {
            put("userid", userId)
            put("session_id", sessionId)
            put("message", message)
            put("event_type", "new_user_message")
        }

        Log.d(TAG, "Sending message with payload: $payload")
        socket?.emit("ai_chat", payload)

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

    fun createNewMessage(userId: Int, profileId: Int, message: String) {
        if (socket?.connected() != true) {
            Log.e(TAG, "Cannot create new message: Socket not connected")
            return
        }

        val payload = JSONObject().apply {
            put("userid", userId)
            put("profile_id", profileId)
            put("message", message)
            put("event_type", "new_user_message")
        }

        Log.d(TAG, "Creating new message with payload: $payload")
        socket?.emit("ai_chat", payload)

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

    fun disconnect() {
        socket?.apply {
            off(Socket.EVENT_CONNECT, onConnect)
            off(Socket.EVENT_DISCONNECT, onDisconnect)
            off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            off("ai_chat_chunk", onChatChunk)
            off("ai_chat_complete", onChatComplete)
            off("ai_chat_response", onChatResponse)
            disconnect()
        }
        socket = null
        _connectionState.value = ConnectionState.DISCONNECTED
        userId = null
        sessionId = null
        Log.d(TAG, "Disconnected")
    }

    fun reconnect(userId: Int, sessionId: Int) {
        disconnect()
        Thread.sleep(300)
        connect(userId, sessionId)
    }
}

data class ChatMessage(
    val id: String,
    val content: String,
    val isAssistant: Boolean,
    val timestamp: Long
)

enum class ConnectionState {
    CONNECTING, CONNECTED, DISCONNECTED, ERROR
}



















//package com.cc.creatorcircle.data.socket
//
//import android.util.Log
//import io.socket.client.IO
//import io.socket.client.Socket
//import io.socket.emitter.Emitter
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import org.json.JSONObject
//import java.net.URISyntaxException
//
//class SocketIOManager {
//    private var socket: Socket? = null
//
//    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
//    val messages: StateFlow<List<ChatMessage>> = _messages
//
//    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
//    val connectionState: StateFlow<ConnectionState> = _connectionState
//
//    // NEW: StateFlow to emit new session ID
//    private val _newSessionId = MutableStateFlow<Int?>(null)
//    val newSessionId: StateFlow<Int?> = _newSessionId
//
//    private var userId: Int? = null
//    private var sessionId: Int? = null
//
//    companion object {
//        private const val TAG = "SocketIOManager"
//        private const val SERVER_URL = "https://creatorcircle.in"
//    }
//
//    fun connect(userId: Int, sessionId: Int, url: String = SERVER_URL) {
//        this.userId = userId
//        this.sessionId = sessionId
//
//        try {
//            Log.d(TAG, "Attempting to connect to: $url with userId: $userId, sessionId: $sessionId")
//
//            val options = IO.Options().apply {
//                transports = arrayOf("websocket", "polling")
//                reconnection = true
//                reconnectionAttempts = 5
//                reconnectionDelay = 1000
//                timeout = 20000
//                forceNew = true
//                path = "/socket.io/"
//            }
//
//            socket = IO.socket(url, options)
//
//            socket?.apply {
//                on(Socket.EVENT_CONNECT, onConnect)
//                on(Socket.EVENT_DISCONNECT, onDisconnect)
//                on(Socket.EVENT_CONNECT_ERROR, onConnectError)
//                on("ai_chat_chunk", onChatChunk)
//                on("ai_chat_complete", onChatComplete)
//                on("ai_chat_response", onChatResponse)
//                on("session_created", onSessionCreated) // NEW LISTENER
//
//                connect()
//            }
//        } catch (e: URISyntaxException) {
//            Log.e(TAG, "Invalid URL: ${e.message}", e)
//            _connectionState.value = ConnectionState.ERROR
//        } catch (e: Exception) {
//            Log.e(TAG, "Connection error: ${e.message}", e)
//            _connectionState.value = ConnectionState.ERROR
//        }
//    }
//
//    private val onConnect = Emitter.Listener {
//        Log.d(TAG, "Socket.IO Connected successfully!")
//        Log.d(TAG, "Socket ID: ${socket?.id()}")
//        _connectionState.value = ConnectionState.CONNECTED
//    }
//
//    private val onDisconnect = Emitter.Listener { args ->
//        Log.d(TAG, "Socket.IO Disconnected: ${args.firstOrNull()}")
//        _connectionState.value = ConnectionState.DISCONNECTED
//    }
//
//    private val onConnectError = Emitter.Listener { args ->
//        val error = args.firstOrNull()
//        Log.e(TAG, "Socket.IO Connection Error: $error")
//        Log.e(TAG, "Error type: ${error?.javaClass?.name}")
//        _connectionState.value = ConnectionState.ERROR
//    }
//
//    // NEW: Listener for session creation
//    private val onSessionCreated = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Session created response: $data")
//
//            val sessionId = data.getInt("session_id")
//            Log.d(TAG, "New session ID received: $sessionId")
//
//            // Emit the new session ID
//            _newSessionId.value = sessionId
//
//            // Update current session ID
//            this.sessionId = sessionId
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling session created: ${e.message}", e)
//        }
//    }
//
//    private val onChatChunk = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Chat chunk received: $data")
//
//            val content = data.getString("content")
//            val messageId = data.getString("assistant_message_id")
//
//            val currentMessages = _messages.value.toMutableList()
//            val existingIndex = currentMessages.indexOfLast {
//                it.isAssistant && it.id == messageId
//            }
//
//            if (existingIndex != -1) {
//                currentMessages[existingIndex] = currentMessages[existingIndex].copy(
//                    content = currentMessages[existingIndex].content + content
//                )
//            } else {
//                currentMessages.add(
//                    ChatMessage(
//                        id = messageId,
//                        content = content,
//                        isAssistant = true,
//                        timestamp = System.currentTimeMillis()
//                    )
//                )
//            }
//            _messages.value = currentMessages
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling chat chunk: ${e.message}", e)
//        }
//    }
//
//    fun clearMessages() {
//        _messages.value = emptyList()
//    }
//
//    fun clearNewSessionId() {
//        _newSessionId.value = null
//    }
//
//    private val onChatComplete = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Chat complete: $data")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling chat complete: ${e.message}", e)
//        }
//    }
//
//    private val onChatResponse = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Chat response received: $data")
//
//            val fullMessage = data.getString("message")
//            val messageId = data.getString("assistant_message_id")
//
//            val currentMessages = _messages.value.toMutableList()
//            val existingIndex = currentMessages.indexOfLast {
//                it.isAssistant && it.id == messageId
//            }
//
//            if (existingIndex != -1) {
//                currentMessages[existingIndex] = currentMessages[existingIndex].copy(
//                    content = fullMessage
//                )
//            } else {
//                currentMessages.add(
//                    ChatMessage(
//                        id = messageId,
//                        content = fullMessage,
//                        isAssistant = true,
//                        timestamp = System.currentTimeMillis()
//                    )
//                )
//            }
//            _messages.value = currentMessages
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling chat response: ${e.message}", e)
//        }
//    }
//
//    fun sendMessage(userId: Int, sessionId: Int, message: String) {
//        if (socket?.connected() != true) {
//            Log.e(TAG, "Cannot send message: Socket not connected")
//            return
//        }
//
//        val payload = JSONObject().apply {
//            put("userid", userId)
//            put("session_id", sessionId)
//            put("message", message)
//            put("event_type", "new_user_message")
//        }
//
//        Log.d(TAG, "Sending message with payload: $payload")
//        socket?.emit("ai_chat", payload)
//
//        val currentMessages = _messages.value.toMutableList()
//        currentMessages.add(
//            ChatMessage(
//                id = System.currentTimeMillis().toString(),
//                content = message,
//                isAssistant = false,
//                timestamp = System.currentTimeMillis()
//            )
//        )
//        _messages.value = currentMessages
//    }
//
//    fun createNewMessage(userId: Int, profileId: Int, message: String) {
//        if (socket?.connected() != true) {
//            Log.e(TAG, "Cannot create new message: Socket not connected")
//            return
//        }
//
//        val payload = JSONObject().apply {
//            put("userid", userId)
//            put("profile_id", profileId)
//            put("message", message)
//            put("event_type", "new_user_message")
//        }
//
//        Log.d(TAG, "Creating new message with payload: $payload")
//        socket?.emit("ai_chat", payload)
//
//        val currentMessages = _messages.value.toMutableList()
//        currentMessages.add(
//            ChatMessage(
//                id = System.currentTimeMillis().toString(),
//                content = message,
//                isAssistant = false,
//                timestamp = System.currentTimeMillis()
//            )
//        )
//        _messages.value = currentMessages
//    }
//
//    fun disconnect() {
//        socket?.apply {
//            off(Socket.EVENT_CONNECT, onConnect)
//            off(Socket.EVENT_DISCONNECT, onDisconnect)
//            off(Socket.EVENT_CONNECT_ERROR, onConnectError)
//            off("ai_chat_chunk", onChatChunk)
//            off("ai_chat_complete", onChatComplete)
//            off("ai_chat_response", onChatResponse)
//            off("session_created", onSessionCreated)
//            disconnect()
//        }
//        socket = null
//        _connectionState.value = ConnectionState.DISCONNECTED
//        userId = null
//        sessionId = null
//        Log.d(TAG, "Disconnected")
//    }
//
//    fun reconnect(userId: Int, sessionId: Int) {
//        disconnect()
//        Thread.sleep(300)
//        connect(userId, sessionId)
//    }
//}
//
//data class ChatMessage(
//    val id: String,
//    val content: String,
//    val isAssistant: Boolean,
//    val timestamp: Long
//)
//
//enum class ConnectionState {
//    CONNECTING, CONNECTED, DISCONNECTED, ERROR
//}
//












//package com.cc.creatorcircle.data.socket
//
//import android.util.Log
//import io.socket.client.IO
//import io.socket.client.Socket
//import io.socket.emitter.Emitter
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import org.json.JSONObject
//import java.net.URISyntaxException
//
//class SocketIOManager {
//    private var socket: Socket? = null
//
//    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
//    val messages: StateFlow<List<ChatMessage>> = _messages
//
//    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
//    val connectionState: StateFlow<ConnectionState> = _connectionState
//
//    private var userId: Int? = null
//    private var sessionId: Int? = null
//
//    companion object {
//        private const val TAG = "SocketIOManager"
//        private const val SERVER_URL = "https://creatorcircle.in"
//    }
//
//    fun connect(userId: Int, sessionId: Int, url: String = SERVER_URL) {
//        this.userId = userId
//        this.sessionId = sessionId
//
//        try {
//            Log.d(TAG, "Attempting to connect to: $url with userId: $userId, sessionId: $sessionId")
//
//            val options = IO.Options().apply {
//                transports = arrayOf("websocket", "polling")
//                reconnection = true
//                reconnectionAttempts = 5
//                reconnectionDelay = 1000
//                timeout = 20000
//                forceNew = true
//                path = "/socket.io/"
//            }
//
//            socket = IO.socket(url, options)
//
//            socket?.apply {
//                on(Socket.EVENT_CONNECT, onConnect)
//                on(Socket.EVENT_DISCONNECT, onDisconnect)
//                on(Socket.EVENT_CONNECT_ERROR, onConnectError)
//                on("ai_chat_chunk", onChatChunk)
//                on("ai_chat_complete", onChatComplete)
//                on("ai_chat_response", onChatResponse)
//
//                connect()
//            }
//        } catch (e: URISyntaxException) {
//            Log.e(TAG, "Invalid URL: ${e.message}", e)
//            _connectionState.value = ConnectionState.ERROR
//        } catch (e: Exception) {
//            Log.e(TAG, "Connection error: ${e.message}", e)
//            _connectionState.value = ConnectionState.ERROR
//        }
//    }
//
//    private val onConnect = Emitter.Listener {
//        Log.d(TAG, "Socket.IO Connected successfully!")
//        Log.d(TAG, "Socket ID: ${socket?.id()}")
//        _connectionState.value = ConnectionState.CONNECTED
//    }
//
//    private val onDisconnect = Emitter.Listener { args ->
//        Log.d(TAG, "Socket.IO Disconnected: ${args.firstOrNull()}")
//        _connectionState.value = ConnectionState.DISCONNECTED
//    }
//
//    private val onConnectError = Emitter.Listener { args ->
//        val error = args.firstOrNull()
//        Log.e(TAG, "Socket.IO Connection Error: $error")
//        Log.e(TAG, "Error type: ${error?.javaClass?.name}")
//        _connectionState.value = ConnectionState.ERROR
//    }
//
//    private val onChatChunk = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Chat chunk received: $data")
//
//            val content = data.getString("content")
//            val messageId = data.getString("assistant_message_id")
//
//            val currentMessages = _messages.value.toMutableList()
//            val existingIndex = currentMessages.indexOfLast {
//                it.isAssistant && it.id == messageId
//            }
//
//            if (existingIndex != -1) {
//                currentMessages[existingIndex] = currentMessages[existingIndex].copy(
//                    content = currentMessages[existingIndex].content + content
//                )
//            } else {
//                currentMessages.add(
//                    ChatMessage(
//                        id = messageId,
//                        content = content,
//                        isAssistant = true,
//                        timestamp = System.currentTimeMillis()
//                    )
//                )
//            }
//            _messages.value = currentMessages
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling chat chunk: ${e.message}", e)
//        }
//    }
//
//    fun clearMessages() {
//        _messages.value = emptyList()
//    }
//
//    private val onChatComplete = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Chat complete: $data")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling chat complete: ${e.message}", e)
//        }
//    }
//
//    private val onChatResponse = Emitter.Listener { args ->
//        try {
//            val data = args[0] as JSONObject
//            Log.d(TAG, "Chat response received: $data")
//
//            val fullMessage = data.getString("message")
//            val messageId = data.getString("assistant_message_id")
//
//            val currentMessages = _messages.value.toMutableList()
//            val existingIndex = currentMessages.indexOfLast {
//                it.isAssistant && it.id == messageId
//            }
//
//            if (existingIndex != -1) {
//                currentMessages[existingIndex] = currentMessages[existingIndex].copy(
//                    content = fullMessage
//                )
//            } else {
//                currentMessages.add(
//                    ChatMessage(
//                        id = messageId,
//                        content = fullMessage,
//                        isAssistant = true,
//                        timestamp = System.currentTimeMillis()
//                    )
//                )
//            }
//            _messages.value = currentMessages
//        } catch (e: Exception) {
//            Log.e(TAG, "Error handling chat response: ${e.message}", e)
//        }
//    }
//
//    // Existing function: Send message with session_id
//    fun sendMessage(userId: Int, sessionId: Int, message: String) {
//        if (socket?.connected() != true) {
//            Log.e(TAG, "Cannot send message: Socket not connected")
//            return
//        }
//
//        val payload = JSONObject().apply {
//            put("userid", userId)
//            put("session_id", sessionId)
//            put("message", message)
//            put("event_type", "new_user_message")
//        }
//
//        Log.d(TAG, "Sending message with payload: $payload")
//        socket?.emit("ai_chat", payload)
//
//        val currentMessages = _messages.value.toMutableList()
//        currentMessages.add(
//            ChatMessage(
//                id = System.currentTimeMillis().toString(),
//                content = message,
//                isAssistant = false,
//                timestamp = System.currentTimeMillis()
//            )
//        )
//        _messages.value = currentMessages
//    }
//
//    // New function: Create new message with profile_id
//    fun createNewMessage(userId: Int, profileId: Int, message: String) {
//        if (socket?.connected() != true) {
//            Log.e(TAG, "Cannot create new message: Socket not connected")
//            return
//        }
//
//        val payload = JSONObject().apply {
//            put("userid", userId)
//            put("profile_id", profileId)
//            put("message", message)
//            put("event_type", "new_user_message")
//        }
//
//        Log.d(TAG, "Creating new message with payload: $payload")
//        socket?.emit("ai_chat", payload)
//
//        val currentMessages = _messages.value.toMutableList()
//        currentMessages.add(
//            ChatMessage(
//                id = System.currentTimeMillis().toString(),
//                content = message,
//                isAssistant = false,
//                timestamp = System.currentTimeMillis()
//            )
//        )
//        _messages.value = currentMessages
//    }
//
//    fun disconnect() {
//        socket?.apply {
//            off(Socket.EVENT_CONNECT, onConnect)
//            off(Socket.EVENT_DISCONNECT, onDisconnect)
//            off(Socket.EVENT_CONNECT_ERROR, onConnectError)
//            off("ai_chat_chunk", onChatChunk)
//            off("ai_chat_complete", onChatComplete)
//            off("ai_chat_response", onChatResponse)
//            disconnect()
//        }
//        socket = null
//        _connectionState.value = ConnectionState.DISCONNECTED
//        userId = null
//        sessionId = null
//        Log.d(TAG, "Disconnected")
//    }
//
//    fun reconnect(userId: Int, sessionId: Int) {
//        disconnect()
//        Thread.sleep(300) // Give it time to disconnect
//        connect(userId, sessionId)
//    }
//
//}
//
//data class ChatMessage(
//    val id: String,
//    val content: String,
//    val isAssistant: Boolean,
//    val timestamp: Long
//)
//
//enum class ConnectionState {
//    CONNECTING, CONNECTED, DISCONNECTED, ERROR
//}
//
//

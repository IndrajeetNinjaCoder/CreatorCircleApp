package com.cc.creatorcircle.data.websocket

import com.cc.creatorcircle.data.models.websocket.IncomingMessage
import com.cc.creatorcircle.data.models.websocket.OutgoingMessage
import com.cc.creatorcircle.data.models.websocket.StreamingMessage
import com.cc.creatorcircle.data.models.websocket.extractMessageObject
import com.cc.creatorcircle.data.models.websocket.extractMessageString
import com.cc.creatorcircle.data.models.websocket.json
import okhttp3.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement

class WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _messages = MutableStateFlow<List<IncomingMessage>>(emptyList())
    val messages: StateFlow<List<IncomingMessage>> = _messages.asStateFlow()

    // Simplified streaming messages
    private val _streamingMessages = MutableStateFlow<Map<String, StreamingMessage>>(emptyMap())
    val streamingMessages: StateFlow<Map<String, StreamingMessage>> = _streamingMessages.asStateFlow()

    // Track current AI response
    private var currentAIResponseId: String? = null
    private var waitingForAIResponse = false

    enum class ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED, ERROR
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            _connectionState.value = ConnectionState.CONNECTED
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            println("=== RAW WebSocket message: $text")

            try {
                val message = json.decodeFromString<IncomingMessage>(text)
                println("=== Parsed - Type: ${message.type}, Status: ${message.status}")

                _messages.value = _messages.value + message
                handleMessage(message, text)

            } catch (e: Exception) {
                println("=== Error parsing, treating as simple text: $text")
                // If JSON parsing fails, treat as simple text (individual word)
                handleSimpleTextMessage(text)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            _connectionState.value = ConnectionState.ERROR
            println("WebSocket error: ${t.message}")
        }
    }

    private fun handleMessage(message: IncomingMessage, rawText: String) {
        val currentMessages = _streamingMessages.value.toMutableMap()

        when {
            // Handle user messages
            message.type == "new_user_message" -> {
                try {
                    val messageObj = extractMessageObject(message.message)

                    if (messageObj != null) {
                        val userMessage = StreamingMessage(
                            id = messageObj.id,
                            type = "user_message",
                            role = "user",
                            content = messageObj.content,
                            timestamp = messageObj.timestamp,
                            isComplete = true
                        )
                        currentMessages[userMessage.id] = userMessage

                        // Prepare for AI response
                        waitingForAIResponse = true
                        currentAIResponseId = "ai_${System.currentTimeMillis()}"

                        println("=== Added user message: ${messageObj.content}")
                    }
                } catch (e: Exception) {
                    println("=== Error parsing user message: ${e.message}")
                }
            }

            // Handle status messages
            message.status == "processing" -> {
                println("=== AI is processing...")
                waitingForAIResponse = true
                if (currentAIResponseId == null) {
                    currentAIResponseId = "ai_${System.currentTimeMillis()}"
                }
            }

            message.status == "complete" -> {
                println("=== AI response complete")
                currentAIResponseId?.let { id ->
                    currentMessages[id]?.let { aiMessage ->
                        currentMessages[id] = aiMessage.copy(isComplete = true)
                    }
                }
                waitingForAIResponse = false
            }

            // Handle individual words (messages without type/status)
            message.type == null && message.status == null -> {
                val content = extractMessageContent(message, rawText)
                if (content.isNotEmpty() && waitingForAIResponse) {
                    addToAIResponse(currentMessages, content)
                    println("=== Added to AI response: '$content'")
                }
            }
        }

        _streamingMessages.value = currentMessages
    }

    private fun handleSimpleTextMessage(text: String) {
        if (waitingForAIResponse && text.isNotEmpty()) {
            val currentMessages = _streamingMessages.value.toMutableMap()
            val cleanText = text.trim().removeSurrounding("\"")
            addToAIResponse(currentMessages, cleanText)
            _streamingMessages.value = currentMessages
            println("=== Added simple text to AI response: '$cleanText'")
        }
    }


    private fun addToAIResponse(currentMessages: MutableMap<String, StreamingMessage>, content: String) {
        if (currentAIResponseId == null) {
            currentAIResponseId = "ai_${System.currentTimeMillis()}"
        }

        val responseId = currentAIResponseId!!
        val existingMessage = currentMessages[responseId]

        // Create consistent timestamp format
        val currentTime = System.currentTimeMillis()
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(currentTime))

        if (existingMessage != null) {
            val separator = if (existingMessage.content.isEmpty()) "" else " "
            currentMessages[responseId] = existingMessage.copy(
                content = existingMessage.content + separator + content,
                timestamp = timestamp  // Use consistent format
            )
        } else {
            currentMessages[responseId] = StreamingMessage(
                id = responseId,
                type = "ai_response",
                role = "assistant",
                content = content,
                timestamp = timestamp,  // Use consistent format
                isComplete = false
            )
        }
    }
//    private fun addToAIResponse(currentMessages: MutableMap<String, StreamingMessage>, content: String) {
//        if (currentAIResponseId == null) {
//            currentAIResponseId = "ai_${System.currentTimeMillis()}"
//        }
//
//        val responseId = currentAIResponseId!!
//        val existingMessage = currentMessages[responseId]
//
//        if (existingMessage != null) {
//            val separator = if (existingMessage.content.isEmpty()) "" else " "
//            currentMessages[responseId] = existingMessage.copy(
//                content = existingMessage.content + separator + content,
//                timestamp = System.currentTimeMillis().toString()
//            )
//        } else {
//            currentMessages[responseId] = StreamingMessage(
//                id = responseId,
//                type = "ai_response",
//                role = "assistant",
//                content = content,
//                timestamp = System.currentTimeMillis().toString(),
//                isComplete = false
//            )
//        }
//    }

    private fun extractMessageContent(message: IncomingMessage, rawText: String): String {
        return try {
            when {
                message.message != null -> {
                    val msgStr = message.message.toString()
                    when {
                        msgStr.startsWith("\"") && msgStr.endsWith("\"") ->
                            msgStr.removeSurrounding("\"")
                        msgStr.startsWith("{") -> ""  // Skip object messages
                        else -> msgStr
                    }
                }
                else -> ""
            }
        } catch (e: Exception) {
            rawText.trim().removeSurrounding("\"")
        }
    }

    fun connect(url: String) {
        _connectionState.value = ConnectionState.CONNECTING
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendMessage(userId: Int, message: String) {
        val outgoingMessage = OutgoingMessage(userid = userId, message = message)
        val jsonMessage = json.encodeToString(outgoingMessage)
        webSocket?.send(jsonMessage)
        println("=== Sent message: $jsonMessage")
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun clearMessages() {
        _messages.value = emptyList()
        _streamingMessages.value = emptyMap()
        currentAIResponseId = null
        waitingForAIResponse = false
    }
}
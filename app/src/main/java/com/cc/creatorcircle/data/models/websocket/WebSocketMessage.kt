package com.cc.creatorcircle.data.models.websocket

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.decodeFromJsonElement

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}

@Serializable
data class OutgoingMessage(
    val userid: Int,
    val message: String
)

@Serializable
data class IncomingMessage(
    val type: String? = null,
    val status: String? = null,
    val message: JsonElement? = null, // Can be string or object
    @SerialName("session_id")
    val sessionId: Int? = null,
    val timestamp: String? = null
)

// For when message is an object (new_user_message type)
@Serializable
data class MessageObject(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: String
)

// New data class for streaming messages
data class StreamingMessage(
    val id: String,
    val type: String,
    val role: String,
    val content: String,
    val timestamp: String,
    val isComplete: Boolean = false
)

// Usage in your WebSocketManager:
fun parseMessage(jsonString: String): IncomingMessage? {
    return try {
        json.decodeFromString<IncomingMessage>(jsonString)
    } catch (e: Exception) {
        println("Error parsing message: $jsonString, Error: ${e.message}")
        null
    }
}

// Helper functions to extract message content
fun extractMessageString(message: JsonElement?): String? {
    return try {
        message?.let {
            val messageStr = it.toString()
            when {
                messageStr.startsWith("\"") && messageStr.endsWith("\"") -> {
                    // It's a quoted string, remove quotes
                    messageStr.removeSurrounding("\"")
                }
                messageStr.startsWith("{") -> {
                    // It's an object, try to parse it
                    val messageObj = json.decodeFromJsonElement<MessageObject>(it)
                    messageObj.content
                }
                else -> {
                    // It might be a simple string without quotes
                    messageStr
                }
            }
        }
    } catch (e: Exception) {
        // If parsing fails, try to get the raw string value
        message?.toString()?.removeSurrounding("\"")
    }
}

fun extractMessageObject(message: JsonElement?): MessageObject? {
    return try {
        message?.let { json.decodeFromJsonElement<MessageObject>(it) }
    } catch (e: Exception) {
        null
    }
}

// Helper function to convert IncomingMessage to StreamingMessage
fun IncomingMessage.toStreamingMessage(): StreamingMessage? {
    val messageObj = extractMessageObject(this.message)
    val messageString = extractMessageString(this.message)

    return when {
        messageObj != null -> {
            StreamingMessage(
                id = messageObj.id,
                type = this.type ?: "unknown",
                role = messageObj.role,
                content = messageObj.content,
                timestamp = messageObj.timestamp,
                isComplete = this.status == "complete"
            )
        }
        messageString != null && this.type != null -> {
            // Generate a unique ID based on session and timestamp
            val messageId = this.sessionId?.toString() ?:
            "${this.type}_${System.currentTimeMillis()}"

            // Determine role based on message type
            val role = when (this.type) {
                "new_user_message" -> "user"
                "ai_response" -> "assistant"
                else -> "assistant" // Default to assistant for streaming responses
            }

            StreamingMessage(
                id = messageId,
                type = this.type,
                role = role,
                content = messageString,
                timestamp = this.timestamp ?: System.currentTimeMillis().toString(),
                isComplete = this.status == "complete"
            )
        }
        else -> null
    }
}
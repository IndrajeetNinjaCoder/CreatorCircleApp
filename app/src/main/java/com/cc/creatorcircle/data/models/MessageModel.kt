package com.cc.creatorcircle.data.models



data class SendMessageRequest(
    val receiver_id: Int,
    val content: String,
    val message_type: String
)

data class SendMessageResponse(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val content: String,
    val message_type: String,
    val is_read: Boolean,
    val created_at: String
)

data class ConversationMessage(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val content: String,
    val message_type: String,
    val is_read: Boolean,
    val created_at: String
)

data class DeleteMessageResponse(
    val success: Boolean
)

data class MessageDeleteState(
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null
)
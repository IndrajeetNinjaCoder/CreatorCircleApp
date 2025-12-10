package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("subtype") val subtype: String?,
    @SerializedName("message") val message: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("post_id") val postId: String? = null
) {
    // Helper properties for UI
    val isUnread: Boolean get() = !isRead
    val timestamp: String get() = createdAt

    // Check notification types
    val isBooking: Boolean get() = type == "booking"
    val isMessage: Boolean get() = type == "message"
    val isFriendRequest: Boolean get() = type == "friend_request"
    val isPostInteraction: Boolean get() = type == "post_interaction"
    val isMutualFriend: Boolean get() = type == "mutual_friend"
}

// API returns direct array, so we use typealias
typealias NotificationResponse = List<Notification>

// If you need the wrapped response format for other endpoints in the future, use this:
data class NotificationResponseWrapper(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("count") val count: Int,
    @SerializedName("data") val data: List<Notification>
)


data class MarkAllReadResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int
)

data class UnreadCountResponse(
    @SerializedName("unread_count") val unreadCount: Int
)

data class MarkNotificationReadResponse(
    @SerializedName("message") val message: String
)

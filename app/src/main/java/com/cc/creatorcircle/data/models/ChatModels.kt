package com.cc.creatorcircle.data.models


import com.google.gson.annotations.SerializedName



data class ChatUserProfile(
    @SerializedName("id")
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("platform_link")
    val platformLink: String?, // Make this nullable
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("is_active")
    val isActive: Boolean
)

// Also update ProfileData to be nullable
data class ProfileData(
    val id: Int,
    val username: String,
    val platform: String,
    val platform_link: String?, // Make this nullable
    val is_active: Boolean
)

// Since the API returns a direct array, you can use this type alias for clarity
typealias ChatUserProfilesResponse = List<ChatUserProfile>


// Request model for creating new chat session
data class CreateChatSessionRequest(
    @SerializedName("userid")
    val userId: Int
)

// Response model for creating new chat session
data class CreateChatSessionResponse(
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("profile_id")
    val profileId: Int?,
    @SerializedName("title")
    val title: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)
// Model for chat session in history
data class ChatSession(
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("profile")
    val profile: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// Response model for chat history - direct array
typealias ChatHistoryResponse = List<ChatSession>



// Model for chat message
// Your ChatMessage model is correct
data class ChatMessage(
    @SerializedName("id")
    val id: String,
    @SerializedName("role")
    val role: String, // "user" or "assistant"
    @SerializedName("content")
    val content: String,
    @SerializedName("timestamp")
    val timestamp: String
)

// Change this - use a type alias for the direct array response
typealias ChatMessagesResponse = List<ChatMessage>

// Complete model for chat session details
data class ChatSessionDetail(
    @SerializedName("session_id")
    val sessionId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("profile_id")
    val profileId: Int?,
    @SerializedName("profile")
    val profile: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("messages")
    val messages: List<ChatMessage>
)

// Response model for chat session
data class ChatSessionResponse(
    @SerializedName("data")
    val data: ChatSessionDetail,
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String? = null
)


// Set profile active
// Request Model
data class SetProfileActiveRequest(
    val user_id: Int
)

// Response Models
data class SetProfileActiveResponse(
    val message: String,
    val profile: ProfileData
)




// Data classes for the new API methods
data class AddChatProfileRequest(
    val user_id: Int,
    val platform: String,
    val username: String
)

data class AddChatProfileResponse(
    val id: Int,
    val user_id: Int,
    val platform: String,
    val username: String,
    val platform_link: String?,
    val last_updated: String,
    val is_active: Boolean
)



// Response model for delete Insta profile API
data class DeleteChatProfileResponse(
    val message: String,
    val profile_id: Int
)


// Response model for delete chat session API
data class DeleteChatSessionResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("session_id")
    val sessionId: Int
)

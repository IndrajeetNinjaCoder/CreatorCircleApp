package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("content") val content: String,
    @SerializedName("created_timestamp") val createdTimestamp: String,
    @SerializedName("updated_timestamp") val updatedTimestamp: String,
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("user_has_liked") val userHasLiked: Boolean = false,
    @SerializedName("author") val author: Author,
    @SerializedName("parent_id") val parentId: Int? = null,
    @SerializedName("isAuthor") val isAuthor: Boolean = false,
    @SerializedName("replies") val replies: List<Comment>? = null
) {
    // Helper properties for UI
    val authorName: String get() = author.name ?: author.role ?: "Anonymous"
    val authorAvatar: String? get() = author.avatar
    val likesCount: Int get() = likes
    val isLiked: Boolean get() = userHasLiked
    val createdAt: String get() = createdTimestamp
    val timestamp: String get() = createdTimestamp
}

// Response for GET comments (returns array)
data class CommentResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("count") val count: Int,
    @SerializedName("data") val data: List<Comment>,
    @SerializedName("nested") val nested: Boolean
)

// Response for POST comment (returns single comment object)
// This matches your API response exactly
data class AddCommentResponse(
    @SerializedName("message") val message: String, // "Comment created"
    @SerializedName("status") val status: Int,      // 201
    @SerializedName("data") val data: Comment       // Single comment object
)

data class AddReplyResponse(
    @SerializedName("message") val message: String, // "Comment created"
    @SerializedName("status") val status: Int,      // 201
    @SerializedName("data") val data: Comment       // Single comment object
)



data class CommentLikeResponse(
    val message: String,
    val status: Int,
    val isLiked: Boolean,
    val like_count: Int
)
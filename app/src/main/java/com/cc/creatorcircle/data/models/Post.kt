package com.cc.creatorcircle.data.models


import com.google.gson.annotations.SerializedName
import java.io.File

data class PostsResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: List<Post>
)

data class Post(
    @SerializedName("id") val id: String,
    @SerializedName("author") val author: Author,
    @SerializedName("content") val content: String,
    @SerializedName("media") val media: List<String>,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("likes") val likes: Int,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("comments") val comments: Int,
    @SerializedName("reposts") val reposts: Int,
    @SerializedName("isAuthor") val isAuthor: Boolean,
    @SerializedName("likersPreview")val likers_preview: List<LikerPreview>?
)

data class Author(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("role") val role: String?
)

data class LikeResponse(
    val message: String,
    val status: Int,
    val isLiked: Boolean,
    val like_count: Int
)


data class LikerPreview(
    val id: Int,
    val name: String?,
    val avatar: String?,
    val username: String
)







// Models to create a post
data class PostResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("post_id") val postId: String,
    @SerializedName("media_urls") val mediaUrls: List<String>?
)

data class PostRequest(
    val content: String,
    val mediaFiles: List<File> = emptyList()
)

sealed class PostCreationState {
    object Idle : PostCreationState()
    object Loading : PostCreationState()
    data class Success(val response: PostResponse) : PostCreationState()
    data class Error(val message: String) : PostCreationState()
}


// Models for delete post
data class DeletePostResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("notifications_deleted") val notificationsDeleted: Int
)

sealed class PostDeletionState {
    object Idle : PostDeletionState()
    object Loading : PostDeletionState()
    data class Success(val response: DeletePostResponse) : PostDeletionState()
    data class Error(val message: String) : PostDeletionState()
}



// Models for update post
data class UpdatePostResponse(
    @SerializedName("message") val message: String,
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: Post
)

data class UpdatePostRequest(
    val content: String,
    val existing_media_urls: List<String> = emptyList(),
    val mediaFiles: List<File> = emptyList()
)

sealed class PostUpdateState {
    object Idle : PostUpdateState()
    object Loading : PostUpdateState()
    data class Success(val response: UpdatePostResponse) : PostUpdateState()
    data class Error(val message: String) : PostUpdateState()
}
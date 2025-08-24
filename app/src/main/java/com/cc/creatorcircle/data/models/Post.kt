package com.cc.creatorcircle.data.models


import com.google.gson.annotations.SerializedName

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
    @SerializedName("isAuthor") val isAuthor: Boolean
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
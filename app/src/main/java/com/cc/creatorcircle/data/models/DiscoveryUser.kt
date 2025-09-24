package com.cc.creatorcircle.data.models


import com.google.gson.annotations.SerializedName

// Main response model for user discovery API
data class UserDiscoveryResponse(
    @SerializedName("users")
    val users: List<UserWithScore>,

    @SerializedName("total_count")
    val totalCount: Int,

    @SerializedName("next_offset")
    val nextOffset: Int,

    @SerializedName("has_more")
    val hasMore: Boolean
)

// Model for individual user with scoring information
data class UserWithScore(
    @SerializedName("user")
    val user: DiscoveryUser,

    @SerializedName("total_score")
    val totalScore: Int,

    @SerializedName("breakdown")
    val breakdown: ScoreBreakdown
)

// User model specifically for discovery API
data class DiscoveryUser(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("username")
    val username: String?,

    @SerializedName("full_name")
    val fullName: String?,

    @SerializedName("profile_pic")
    val profilePic: String?,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("platform_followers")
    val platformFollowers: Map<String, Any>?,

    @SerializedName("follower_count")
    val followerCount: Int,

    @SerializedName("categories")
    val categories: List<String>,

    @SerializedName("total_social_media_followers")
    val totalSocialMediaFollowers: Int,

    @SerializedName("is_follower")
    val isFollower: Boolean,

    @SerializedName("is_following")
    val isFollowing: Boolean,

    @SerializedName("is_connected")
    val isConnected: Boolean,

    @SerializedName("connection_status")
    val connectionStatus: String?,

    @SerializedName("connection_id")
    val connectionId: Int?
)

// Score breakdown model
data class ScoreBreakdown(
    @SerializedName("mutual_score")
    val mutualScore: Int,

    @SerializedName("bio_score")
    val bioScore: Int,

    @SerializedName("category_score")
    val categoryScore: Int
)

// Model for platform followers (Instagram example)
//data class PlatformFollower(
//    @SerializedName("followers")
//    val followers: Int,
//
//    @SerializedName("is_primary")
//    val isPrimary: Boolean
//)
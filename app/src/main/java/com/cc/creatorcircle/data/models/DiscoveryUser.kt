package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

// Main response wrapper
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

// User with recommendation score
data class UserWithScore(
    @SerializedName("user_id")
    val userId: Int? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("profile_pic")
    val profilePic: String? = null,

    @SerializedName("bio")
    val bio: String? = null,

    @SerializedName("platform_followers")
    val platformFollowers: Map<String, Any>? = null,

    @SerializedName("follower_count")
    val followerCount: Int? = 0,

    @SerializedName("categories")
    val categories: List<String>? = emptyList(),

    @SerializedName("total_social_media_followers")
    val totalSocialMediaFollowers: Int? = 0,

    @SerializedName("is_follower")
    val isFollower: Boolean = false,

    @SerializedName("is_following")
    val isFollowing: Boolean = false,

    @SerializedName("is_connected")
    val isConnected: Boolean = false,

    @SerializedName("connection_status")
    val connectionStatus: String? = null,

    @SerializedName("connection_id")
    val connectionId: Int? = null,

    @SerializedName("is_brand")
    val isBrand: Boolean? = null,

    @SerializedName("total_score")
    val totalScore: Int = 0,

    @SerializedName("breakdown")
    val breakdown: ScoreBreakdown? = null
) {
    // Convert to DiscoveryUser for compatibility
    val user: DiscoveryUser
        get() = DiscoveryUser(
            userId = userId ?: 0,
            username = username,
            fullName = fullName,
            profilePic = profilePic,
            bio = bio,
            platformFollowers = platformFollowers,
            followerCount = followerCount ?: 0,
            categories = categories ?: emptyList(),
            totalSocialMediaFollowers = totalSocialMediaFollowers ?: 0,
            isFollower = isFollower,
            isFollowing = isFollowing,
            isConnected = isConnected,
            connectionStatus = connectionStatus,
            connectionId = connectionId
        )
}

data class ScoreBreakdown(
    @SerializedName("mutual_score")
    val mutualScore: Int = 0,

    @SerializedName("bio_score")
    val bioScore: Int = 0,

    @SerializedName("category_score")
    val categoryScore: Int = 0
)

// Discovery User model (for compatibility with existing code)
data class DiscoveryUser(
    val userId: Int,
    val username: String?,
    val fullName: String?,
    val profilePic: String?,
    val bio: String?,
    val platformFollowers: Map<String, Any>?,
    val followerCount: Int,
    val categories: List<String>,
    val totalSocialMediaFollowers: Int,
    val isFollower: Boolean,
    val isFollowing: Boolean,
    val isConnected: Boolean,
    val connectionStatus: String?,
    val connectionId: Int?
)








//package com.cc.creatorcircle.data.models
//
//
//import com.google.gson.annotations.SerializedName
//
//// Main response model for user discovery API
//data class UserDiscoveryResponse(
//    @SerializedName("users")
//    val users: List<UserWithScore>,
//
//    @SerializedName("total_count")
//    val totalCount: Int,
//
//    @SerializedName("next_offset")
//    val nextOffset: Int,
//
//    @SerializedName("has_more")
//    val hasMore: Boolean
//)
//
//// Model for individual user with scoring information
//data class UserWithScore(
//    @SerializedName("user")
//    val user: DiscoveryUser,
//
//    @SerializedName("total_score")
//    val totalScore: Int,
//
//    @SerializedName("breakdown")
//    val breakdown: ScoreBreakdown
//)
//
//// User model specifically for discovery API
//data class DiscoveryUser(
//    @SerializedName("user_id")
//    val userId: Int,
//
//    @SerializedName("username")
//    val username: String?,
//
//    @SerializedName("full_name")
//    val fullName: String?,
//
//    @SerializedName("profile_pic")
//    val profilePic: String?,
//
//    @SerializedName("bio")
//    val bio: String?,
//
//    @SerializedName("platform_followers")
//    val platformFollowers: Map<String, Any>?,
//
//    @SerializedName("follower_count")
//    val followerCount: Int,
//
//    @SerializedName("categories")
//    val categories: List<String>,
//
//    @SerializedName("total_social_media_followers")
//    val totalSocialMediaFollowers: Int,
//
//    @SerializedName("is_follower")
//    val isFollower: Boolean,
//
//    @SerializedName("is_following")
//    val isFollowing: Boolean,
//
//    @SerializedName("is_connected")
//    val isConnected: Boolean,
//
//    @SerializedName("connection_status")
//    val connectionStatus: String?,
//
//    @SerializedName("connection_id")
//    val connectionId: Int?
//)
//
//// Score breakdown model
//data class ScoreBreakdown(
//    @SerializedName("mutual_score")
//    val mutualScore: Int,
//
//    @SerializedName("bio_score")
//    val bioScore: Int,
//
//    @SerializedName("category_score")
//    val categoryScore: Int
//)
//
//// Model for platform followers (Instagram example)
////data class PlatformFollower(
////    @SerializedName("followers")
////    val followers: Int,
////
////    @SerializedName("is_primary")
////    val isPrimary: Boolean
////)
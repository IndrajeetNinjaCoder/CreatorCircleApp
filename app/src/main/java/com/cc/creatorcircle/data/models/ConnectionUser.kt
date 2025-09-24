package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

// Main response model
data class SocialMediaResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Int,

    @SerializedName("growth_suggestions")
    val growthSuggestions: List<ConnectionUser>,

    @SerializedName("pending_connections")
    val pendingConnections: PendingConnections
)

// Connection-specific user model with additional connection fields
data class ConnectionUser(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("username")
    val username: String?,

    @SerializedName("full_name")
    val fullName: String?,

    @SerializedName("profile_pic")
    val profilePic: String?,

    @SerializedName("platform_followers")
    val platformFollowers: Map<String, List<PlatformFollower>>,

    @SerializedName("profile_link")
    val profileLink: String,

    @SerializedName("followers_count")
    val followersCount: Int,

    @SerializedName("following_count")
    val followingCount: Int,

    @SerializedName("is_following")
    val isFollowing: Boolean,

    @SerializedName("is_follower")
    val isFollower: Boolean,

    @SerializedName("is_connected")
    val isConnected: Boolean,

    @SerializedName("source")
    val source: String,

    @SerializedName("connection_id")
    val connectionId: Int? = null // Only present in sent connections
)

// Platform follower model (e.g., Instagram followers)
//data class PlatformFollower(
//    @SerializedName("followers")
//    val followers: Int,
//
//    @SerializedName("is_primary")
//    val isPrimary: Boolean
//)

// Pending connections container
data class PendingConnections(
    @SerializedName("to_accept")
    val toAccept: ConnectionGroup,

    @SerializedName("sent")
    val sent: ConnectionGroup
)

// Connection group with count and users
data class ConnectionGroup(
    @SerializedName("count")
    val count: Int,

    @SerializedName("users")
    val users: List<ConnectionUser>
)

// Extension functions for convenience
fun ConnectionUser.hasProfilePicture(): Boolean = !profilePic.isNullOrBlank()

fun ConnectionUser.hasInstagramFollowers(): Boolean =
    platformFollowers["instagram"]?.isNotEmpty() == true

fun ConnectionUser.getInstagramFollowersCount(): Int =
    platformFollowers["instagram"]?.firstOrNull()?.followers ?: 0

fun SocialMediaResponse.getTotalGrowthSuggestions(): Int = growthSuggestions.size

fun SocialMediaResponse.getTotalPendingConnections(): Int =
    pendingConnections.toAccept.count + pendingConnections.sent.count

// Helper function to filter users by criteria
fun List<ConnectionUser>.filterByFollowersCount(minFollowers: Int): List<ConnectionUser> =
    filter { it.followersCount >= minFollowers }

fun List<ConnectionUser>.filterWithProfilePic(): List<ConnectionUser> =
    filter { it.hasProfilePicture() }

fun List<ConnectionUser>.filterByPlatform(platform: String): List<ConnectionUser> =
    filter { it.platformFollowers.containsKey(platform) }
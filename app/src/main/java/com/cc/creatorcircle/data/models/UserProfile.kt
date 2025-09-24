package com.cc.creatorcircle.data.models

data class UserProfile(
    val id: Int,
    val email: String,
    val username: String,
    val profile_pic: String?,
    val full_name: String?,
    val bio: String?,
    val mobile_number: String?,
    val is_active: Boolean,
    // Fixed: Changed from Map<String, Int> to Map<String, Any>
    val platform_followers: Map<String, Any>?,
    // Fixed: Changed from Map<String, String> to Map<String, Any>
    val social_media_links: Map<String, Any>?,
    val categories: List<String>?,
    val age: Int,
    val onboarding_status: Boolean,
    val is_connected: Boolean,
    val is_follower: Boolean,
    val is_following: Boolean,
    val followers: UserList,
    val following: UserList,
    val accepted_connections: UserList,
    val pending_connections: UserList,
    val posts: Any?, // Can be null or specific type based on your posts structure
    val reels: Any?, // Can be null or specific type based on your reels structure
    val connection_status: String?
)

data class UserList(
    val count: Int,
    val users: List<UserInfo>
)

data class UserInfo(
    val user_id: Int,
    val username: String,
    val full_name: String?,
    val profile_pic: String?,
    val bio: String?,
    val platform_followers: Map<String, Any>?,
    val follower_count: Int?,
    val categories: List<String>?,
    val total_social_media_followers: Int?,
    val is_follower: Boolean,
    val is_following: Boolean,
    val is_connected: Boolean,
    val connection_status: String?,
    val connection_id: Int?
)



















package com.cc.creatorcircleapp.data.models

data class SignUpRequest(
    val email: String,
    val username: String,
    val password: String
)

data class SignUpResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val user: User
)





// LoginRequest.kt
data class LoginRequest(
    val username: String,
    val password: String
)

// LoginResponse.kt
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val user: User
)



data class User(
    val id: Int,
    val email: String,
    val username: String,
    val profile_pic: String?,
    val full_name: String?,
    val bio: String?,
    val mobile_number: String?,
    val is_active: Boolean,
    val platform_followers: Map<String, Any>?,
    val social_media_links: Map<String, Any>?,
    val categories: List<String>?,
    val age: Int,
    val onboarding_status: Boolean,
    val is_connected: Boolean,
    val is_follower: Boolean,
    val is_following: Boolean
)

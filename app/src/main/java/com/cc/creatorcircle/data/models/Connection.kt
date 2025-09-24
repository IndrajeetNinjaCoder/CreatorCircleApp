package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

data class ConnectionResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Int,

    @SerializedName("connection")
    val connection: Connection
)

data class ConnectionActionResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Int,
)

data class RemoveConnectionResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Int
)

data class CancelConnectionResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: Int
)







data class Connection(
    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("full_name")
    val fullName: String?,

    @SerializedName("profile_pic")
    val profilePic: String?,

    @SerializedName("platform_followers")
    val platformFollowers: Map<String, Any>,

    @SerializedName("profile_link")
    val profileLink: String,

    @SerializedName("connection_id")
    val connectionId: Int,

    @SerializedName("status")
    val status: String
)
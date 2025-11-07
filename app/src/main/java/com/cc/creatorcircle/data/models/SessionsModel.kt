package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

// Request Models
//data class SessionsRequest(
//    @SerializedName("statuses") val statuses: List<String>,
//    @SerializedName("date_range") val dateRange: DateRange,
//    @SerializedName("session_type") val sessionType: String
//)





data class SessionsRequest(
    @SerializedName("statuses") val statuses: List<String>,
    @SerializedName("date_range") val dateRange: DateRange? = null,  // Now optional
    @SerializedName("session_type") val sessionType: String? = null  // Now optional
)









data class DateRange(
    @SerializedName("from_date") val fromDate: String,
    @SerializedName("to_date") val toDate: String
)

// Response Models
data class SessionsResponse(
    @SerializedName("sessions") val sessions: List<Session>
)

data class Session(
    @SerializedName("slot_id") val slotId: Int,
    @SerializedName("date") val date: String,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("status") val status: String,
    @SerializedName("description") val description: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("google_meet_link") val googleMeetLink: String,
    @SerializedName("current_user_role") val currentUserRole: String,
    @SerializedName("seeker") val seeker: SessionUser,
    @SerializedName("provider") val provider: SessionUser
)

data class SessionUser(
    @SerializedName("id") val id: Int,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("profile_pic") val profilePic: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("categories") val categories: List<String>,
    @SerializedName("platform_followers") val platformFollowers: PlatformFollowers,
    @SerializedName("is_current_user") val isCurrentUser: Boolean
)

data class PlatformFollowers(
    @SerializedName("instagram") val instagram: List<InstagramAccount>? = null,
    @SerializedName("youtube") val youtube: List<YoutubeAccount>? = null,
    @SerializedName("twitter") val twitter: List<TwitterAccount>? = null
)

data class InstagramAccount(
    @SerializedName("username") val username: String? = null,
    @SerializedName("followers") val followers: Any, // Can be String or Int
    @SerializedName("is_primary") val isPrimary: Boolean
)

data class YoutubeAccount(
    @SerializedName("username") val username: String? = null,
    @SerializedName("followers") val followers: Any,
    @SerializedName("is_primary") val isPrimary: Boolean
)

data class TwitterAccount(
    @SerializedName("username") val username: String? = null,
    @SerializedName("followers") val followers: Any,
    @SerializedName("is_primary") val isPrimary: Boolean
)

// State Management
sealed class SessionsState {
    object Idle : SessionsState()
    object Loading : SessionsState()
    data class Success(val response: SessionsResponse) : SessionsState()
    data class Error(val message: String) : SessionsState()
}

// Helper Extensions
fun Session.isUpcoming(): Boolean = status == "upcoming"
fun Session.isCompleted(): Boolean = status == "completed"
fun Session.isCancelled(): Boolean = status == "cancelled"
fun Session.isPending(): Boolean = status == "pending"

fun SessionUser.getPrimaryInstagramFollowers(): Int? {
    return platformFollowers.instagram
        ?.firstOrNull { it.isPrimary }
        ?.followers
        ?.let { 
            when (it) {
                is Int -> it
                is String -> it.toIntOrNull()
                else -> null
            }
        }
}

fun SessionUser.getPrimaryInstagramUsername(): String? {
    return platformFollowers.instagram
        ?.firstOrNull { it.isPrimary }
        ?.username
}

fun SessionUser.getTotalInstagramFollowers(): Int {
    return platformFollowers.instagram
        ?.sumOf { account ->
            when (val followers = account.followers) {
                is Int -> followers
                is String -> followers.toIntOrNull() ?: 0
                else -> 0
            }
        } ?: 0
}
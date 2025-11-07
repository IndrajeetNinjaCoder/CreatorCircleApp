package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.SocialMediaResponse
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File

// Data classes for nested structures
data class PlatformFollower(
    val followers: String,
    val is_primary: Boolean = true
)

data class SocialMediaLink(
    val link: String,
    val is_primary: Boolean = true
)

data class PlatformFollowers(
    val instagram: List<PlatformFollower>? = null,
    val youtube: List<PlatformFollower>? = null,
    val tiktok: List<PlatformFollower>? = null,
    val twitter: List<PlatformFollower>? = null,
    val facebook: List<PlatformFollower>? = null
)

data class SocialMediaLinks(
    val instagram: List<SocialMediaLink>? = null,
    val youtube: List<SocialMediaLink>? = null,
    val tiktok: List<SocialMediaLink>? = null,
    val twitter: List<SocialMediaLink>? = null,
    val facebook: List<SocialMediaLink>? = null
)

data class UserUpdateRequest(
    val fullName: String? = null,
    val password: String? = null,
    val mobileNumber: String? = null,
    val platformFollowers: PlatformFollowers? = null,
    val username: String? = null,
    val socialMediaLinks: SocialMediaLinks? = null,
    val onboardingStatus: Boolean? = false,
    val categories: String? = null,
    val bio: String? = null,
    val age: Int? = null,
    val profilePicFile: File? = null
)

class UserRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    suspend fun getUserProfile(): Response<UserProfile> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getUserProfile("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    suspend fun updateUser(updateRequest: UserUpdateRequest): Result<SocialMediaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get token from TokenManager
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                // Convert nested objects to JSON strings if they exist
                val platformFollowersJson = updateRequest.platformFollowers?.let {
                    convertPlatformFollowersToJson(it)
                }
                val socialMediaLinksJson = updateRequest.socialMediaLinks?.let {
                    convertSocialMediaLinksToJson(it)
                }

                // Call API with converted JSON strings
                val response = apiService.updateUser(
                    token = "Bearer $token",
                    fullName = updateRequest.fullName,
                    password = updateRequest.password,
                    mobileNumber = updateRequest.mobileNumber,
                    platformFollowers = platformFollowersJson,
                    username = updateRequest.username,
                    socialMediaLinks = socialMediaLinksJson,
                    onboardingStatus = updateRequest.onboardingStatus,
                    categories = updateRequest.categories,
                    bio = updateRequest.bio,
                    age = updateRequest.age
                )

                if (response.isSuccessful) {
                    response.body()?.let { updateResponse ->
                        Log.d("UserRepository", "User updated successfully")
                        Result.success(updateResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("UserRepository", "API Error: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to update user: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Exception in updateUser", e)
                Result.failure(e)
            }
        }
    }

    // Helper function to convert PlatformFollowers to JSON string
    private fun convertPlatformFollowersToJson(platformFollowers: PlatformFollowers): String {
        val jsonBuilder = StringBuilder("{")
        var isFirst = true

        platformFollowers.instagram?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"instagram\":[")
            list.forEachIndexed { index, follower ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"followers\":\"${follower.followers}\",\"is_primary\":${follower.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        platformFollowers.youtube?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"youtube\":[")
            list.forEachIndexed { index, follower ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"followers\":\"${follower.followers}\",\"is_primary\":${follower.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        platformFollowers.tiktok?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"tiktok\":[")
            list.forEachIndexed { index, follower ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"followers\":\"${follower.followers}\",\"is_primary\":${follower.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        platformFollowers.twitter?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"twitter\":[")
            list.forEachIndexed { index, follower ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"followers\":\"${follower.followers}\",\"is_primary\":${follower.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        platformFollowers.facebook?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"facebook\":[")
            list.forEachIndexed { index, follower ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"followers\":\"${follower.followers}\",\"is_primary\":${follower.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        jsonBuilder.append("}")
        return jsonBuilder.toString()
    }

    // Helper function to convert SocialMediaLinks to JSON string
    private fun convertSocialMediaLinksToJson(socialMediaLinks: SocialMediaLinks): String {
        val jsonBuilder = StringBuilder("{")
        var isFirst = true

        socialMediaLinks.instagram?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"instagram\":[")
            list.forEachIndexed { index, link ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"link\":\"${link.link}\",\"is_primary\":${link.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        socialMediaLinks.youtube?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"youtube\":[")
            list.forEachIndexed { index, link ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"link\":\"${link.link}\",\"is_primary\":${link.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        socialMediaLinks.tiktok?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"tiktok\":[")
            list.forEachIndexed { index, link ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"link\":\"${link.link}\",\"is_primary\":${link.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        socialMediaLinks.twitter?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"twitter\":[")
            list.forEachIndexed { index, link ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"link\":\"${link.link}\",\"is_primary\":${link.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        socialMediaLinks.facebook?.let { list ->
            if (!isFirst) jsonBuilder.append(",")
            jsonBuilder.append("\"facebook\":[")
            list.forEachIndexed { index, link ->
                if (index > 0) jsonBuilder.append(",")
                jsonBuilder.append("{\"link\":\"${link.link}\",\"is_primary\":${link.is_primary}}")
            }
            jsonBuilder.append("]")
            isFirst = false
        }

        jsonBuilder.append("}")
        return jsonBuilder.toString()
    }
}






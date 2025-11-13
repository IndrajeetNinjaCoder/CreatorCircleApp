package com.cc.creatorcircle.data.repository

import android.content.Context
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.AddCommentResponse
import com.cc.creatorcircle.data.models.AddReplyResponse
import com.cc.creatorcircle.data.models.CommentLikeResponse
import com.cc.creatorcircle.data.models.CommentResponse
import com.cc.creatorcircle.data.models.LikeResponse
import com.cc.creatorcircle.data.models.PostRequest
import com.cc.creatorcircle.data.models.PostsResponse
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.data.models.PostResponse
import com.cc.creatorcircle.utils.TokenManager
import retrofit2.Response

import android.util.Log
import com.cc.creatorcircle.data.models.DeletePostResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class PostsRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    suspend fun getPosts(post_type: String): Response<PostsResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getPosts("Bearer $token", postType=post_type)
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    suspend fun getAllPosts(): Response<PostsResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getAllPosts("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }



    suspend fun toggleLike(
        token: String,
        postId: String
    ): Response<LikeResponse> {
        return apiService.toggleLike("Bearer $token", postId)
    }

    suspend fun toggleCommentLike(
        token: String,
        commentId: Int
    ): Response<CommentLikeResponse> {
        return apiService.toggleCommentLike("Bearer $token", commentId)
    }

    suspend fun getComments(
        token: String,
        postId: String
    ): Response<CommentResponse> {
        return apiService.getComments("Bearer $token", postId)
    }

    suspend fun addComment(
        token: String,
        postId: String,
        content: String
    ): Response<AddCommentResponse> {
        return apiService.addComment("Bearer $token", postId, content)
    }

    suspend fun addReply(
        token: String,
        postId: String,
        content: String,
        parentId: Int
    ): Response<AddReplyResponse> {
        return apiService.addReply("Bearer $token", postId, content, parentId)
    }

    suspend fun getUserProfile(): Response<UserProfile> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getUserProfile("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    suspend fun getUserProfileById(userId: Int): Response<UserProfile> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getUserProfileById("Bearer $token", userId)
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    suspend fun createPost(postRequest: PostRequest, postType: String): Result<PostResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get token from TokenManager
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val contentRequestBody = postRequest.content.toRequestBody("text/plain".toMediaTypeOrNull())

                val mediaFiles = postRequest.mediaFiles.mapNotNull { file ->
                    if (file.exists()) {
                        val mimeType = when (file.extension.lowercase()) {
                            "jpg", "jpeg" -> "image/jpeg"
                            "png" -> "image/png"
                            "gif" -> "image/gif"
                            "mp4" -> "video/mp4"
                            "mov" -> "video/quicktime"
                            "avi" -> "video/x-msvideo"
                            else -> "application/octet-stream"
                        }

                        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
                        MultipartBody.Part.createFormData("media_files", file.name, requestFile)
                    } else {
                        Log.e("PostRepository", "File does not exist: ${file.absolutePath}")
                        null
                    }
                }


                val response = if (postType == "feed") {
                    apiService.createPost(
                        "Bearer $token",
                        contentRequestBody,
                        mediaFiles.ifEmpty { null }
                    )
                } else {
                    apiService.createPostResource(  // Different endpoint for resources
                        "Bearer $token",
                        contentRequestBody,
                        mediaFiles.ifEmpty { null }
                    )
                }

                if (response.isSuccessful) {
                    response.body()?.let { postResponse ->
                        Result.success(postResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("PostRepository", "API Error: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to create post: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "Exception in createPost", e)
                Result.failure(e)
            }
        }
    }


    suspend fun getUserPosts(targetUserId: Int): Response<PostsResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getUserPosts(targetUserId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    suspend fun deletePost(postId: String): Response<DeletePostResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.deletePost(postId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }


}


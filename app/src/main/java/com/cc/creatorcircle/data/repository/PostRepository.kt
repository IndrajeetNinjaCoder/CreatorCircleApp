package com.cc.creatorcircle.data.repository

import android.content.Context
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.LikeResponse
import com.cc.creatorcircle.data.models.PostsResponse
import com.cc.creatorcircle.utils.TokenManager
import retrofit2.Response

class PostsRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context) // Use TokenManager

    suspend fun getPosts(limit: Int = 50, offset: Int = 0): Response<PostsResponse> {
        val token = tokenManager.getToken() // Use consistent token source
        return if (token.isNotEmpty()) {
            apiService.getPosts("Bearer $token", limit, offset)
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
}
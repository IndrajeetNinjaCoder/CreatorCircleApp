package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.LiveSessionAvailabilityResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class LiveSessionAvailabilityRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get live session availability by session ID
     */
    suspend fun getLiveSessionAvailability(sessionId: Int): Result<LiveSessionAvailabilityResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure<LiveSessionAvailabilityResponse>(
                        Exception("Access token not found. Please log in again.")
                    )
                }

                val response = apiService.getLiveSessionAvailability(sessionId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { availability ->
                        Log.d("LiveSessionAvailabilityRepository", "Successfully fetched availability for session $sessionId")
                        Result.success(availability)
                    } ?: Result.failure<LiveSessionAvailabilityResponse>(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("LiveSessionAvailabilityRepository", "API Error in getLiveSessionAvailability: ${response.code()} - $errorMessage")
                    Result.failure<LiveSessionAvailabilityResponse>(
                        Exception("Failed to get live session availability: ${response.code()} - $errorMessage")
                    )
                }
            } catch (e: Exception) {
                Log.e("LiveSessionAvailabilityRepository", "Exception in getLiveSessionAvailability", e)
                Result.failure<LiveSessionAvailabilityResponse>(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Get live session availability (Response version)
     */
    suspend fun getLiveSessionAvailabilityResponse(sessionId: Int): Response<LiveSessionAvailabilityResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getLiveSessionAvailability(sessionId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }
}
package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.AvailabilityResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AvailabilityRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get mentor availability by user ID
     */
    suspend fun getMentorAvailability(userId: Int): Result<AvailabilityResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure<AvailabilityResponse>(
                        Exception("Access token not found. Please log in again.")
                    )
                }

                val response = apiService.getMentorAvailability(userId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { availability ->
                        Log.d("AvailabilityRepository", "Successfully fetched availability for mentor $userId")
                        Result.success(availability)
                    } ?: Result.failure<AvailabilityResponse>(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("AvailabilityRepository", "API Error in getMentorAvailability: ${response.code()} - $errorMessage")
                    Result.failure<AvailabilityResponse>(
                        Exception("Failed to get mentor availability: ${response.code()} - $errorMessage")
                    )
                }
            } catch (e: Exception) {
                Log.e("AvailabilityRepository", "Exception in getMentorAvailability", e)
                Result.failure<AvailabilityResponse>(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Get mentor availability (Response version)
     */
    suspend fun getMentorAvailabilityResponse(userId: Int): Response<AvailabilityResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getMentorAvailability(userId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }
}
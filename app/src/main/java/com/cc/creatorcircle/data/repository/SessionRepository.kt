package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.Session
import com.cc.creatorcircle.data.models.SessionsRequest
import com.cc.creatorcircle.data.models.SessionsResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SessionRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get sessions based on filters
     * Note: API returns List<Session> directly, not wrapped in an object
     */
    suspend fun getSessions(request: SessionsRequest): Result<SessionsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                Log.d("SessionRepository", "Fetching sessions with token: ${token.take(10)}...")
                Log.d("SessionRepository", "Request: $request")

                // API returns List<Session> directly
                val response = apiService.getSessions(request, "Bearer $token")

                Log.d("SessionRepository", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val sessions = response.body() ?: emptyList()
                    Log.d("SessionRepository", "Successfully received ${sessions.size} sessions")

                    // Wrap the list in SessionsResponse for consistency with ViewModel
                    Result.success(SessionsResponse(sessions = sessions))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("SessionRepository", "API Error in getSessions: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get sessions: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("SessionRepository", "Exception in getSessions", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Get sessions (Response version)
     */
    suspend fun getSessionsResponse(request: SessionsRequest): Response<List<Session>> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getSessions(request, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }
}
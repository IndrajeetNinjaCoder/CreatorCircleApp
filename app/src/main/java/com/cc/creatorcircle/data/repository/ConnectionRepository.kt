package com.cc.creatorcircle.data.repository


import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.CancelConnectionResponse
import com.cc.creatorcircle.data.models.ConnectionActionResponse
import com.cc.creatorcircle.data.models.ConnectionResponse
import com.cc.creatorcircle.data.models.RemoveConnectionResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ConnectionRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    suspend fun sendConnectionRequest(userId: Int): Result<ConnectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get token from TokenManager
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.sendConnectionRequest("Bearer $token", userId)

                if (response.isSuccessful) {
                    response.body()?.let { connectionResponse ->
                        Log.d(
                            "ConnectionRepository",
                            "Connection request sent successfully: ${connectionResponse.message}"
                        )
                        Result.success(connectionResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ConnectionRepository", "API Error: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to send connection request: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Exception in sendConnectionRequest", e)
                Result.failure(e)
            }
        }
    }


    suspend fun removeConnection(userId: Int): Result<RemoveConnectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get token from TokenManager
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.removeConnection("Bearer $token", userId)

                if (response.isSuccessful) {
                    response.body()?.let { removeConnectionResponse ->
                        Log.d(
                            "ConnectionRepository",
                            "Connection request sent successfully: ${removeConnectionResponse.message}"
                        )
                        Result.success(removeConnectionResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ConnectionRepository", "API Error: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to send connection request: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Exception in sendConnectionRequest", e)
                Result.failure(e)
            }
        }
    }

    suspend fun cancelConnection(userId: Int): Result<CancelConnectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get token from TokenManager
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.cancelConnectionRequest("Bearer $token", userId)

                if (response.isSuccessful) {
                    response.body()?.let { cancelConnectionResponse ->
                        Log.d(
                            "ConnectionRepository",
                            "Connection request sent successfully: ${cancelConnectionResponse.message}"
                        )
                        Result.success(cancelConnectionResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ConnectionRepository", "API Error: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to send connection request: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Exception in sendConnectionRequest", e)
                Result.failure(e)
            }
        }
    }


    suspend fun RespondConnectionRequest(connectionId: Int, action: String): Result<ConnectionActionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                // Get token from TokenManager
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.RespondConnectionRequest(
                    token = "Bearer $token",
                    connectionId = connectionId,
                    action = action
                )

                if (response.isSuccessful) {
                    response.body()?.let { connectionActionResponse ->
                        Log.d(
                            "ConnectionRepository",
                            "Connection request responded successfully: ${connectionActionResponse.message}"  // Updated message
                        )
                        Result.success(connectionActionResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ConnectionRepository", "API Error: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to respond to connection request: ${response.code()} - $errorMessage"))  // Updated message
                }
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Exception in RespondConnectionRequest", e)  // Updated method name in log
                Result.failure(e)
            }
        }
    }




//    suspend fun RespondConnectionRequest(connectionId: Int, action: String): Result<ConnectionActionResponse> {
//        return withContext(Dispatchers.IO) {
//            try {
//                // Get token from TokenManager
//                val token = tokenManager.getToken()
//                if (token.isEmpty()) {
//                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
//                }
//
//                val response = apiService.RespondConnectionRequest("Bearer $token", connectionId = connectionId, action = action)
//
//                if (response.isSuccessful) {
//                    response.body()?.let { connectionActionResponse ->
//                        Log.d(
//                            "ConnectionRepository",
//                            "Connection request sent successfully: ${connectionActionResponse.message}"
//                        )
//                        Result.success(connectionActionResponse)
//                    } ?: Result.failure(Exception("Empty response body"))
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
//                    Log.e("ConnectionRepository", "API Error: ${response.code()} - $errorMessage")
//                    Result.failure(Exception("Failed to send connection request: ${response.code()} - $errorMessage"))
//                }
//            } catch (e: Exception) {
//                Log.e("ConnectionRepository", "Exception in sendConnectionRequest", e)
//                Result.failure(e)
//            }
//        }
//    }
//


    // Alternative method with manual token parameter (similar to PostsRepository style)
    suspend fun sendConnectionRequest(
        token: String,
        userId: Int
    ): Response<ConnectionResponse> {
        return apiService.sendConnectionRequest("Bearer $token", userId)
    }

    // Method to get connection status or details if needed in future
    suspend fun getConnectionStatus(userId: Int): Result<ConnectionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                // Note: This would require a GET endpoint for connection status
                // For now, this is a placeholder structure
                Result.failure(Exception("Get connection status endpoint not implemented"))
            } catch (e: Exception) {
                Log.e("ConnectionRepository", "Exception in getConnectionStatus", e)
                Result.failure(e)
            }
        }
    }
}
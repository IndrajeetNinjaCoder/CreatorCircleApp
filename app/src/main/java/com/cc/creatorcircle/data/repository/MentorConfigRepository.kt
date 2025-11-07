package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.MentorConfigurationResponse
import com.cc.creatorcircle.data.models.UpdateMentorConfigurationRequest
import com.cc.creatorcircle.data.models.UpdateMentorConfigurationResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class MentorConfigRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get mentor guidance configuration
     */
    suspend fun getGuidanceConfiguration(): Result<MentorConfigurationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                Log.d("MentorConfigRepository", "Fetching guidance configuration with token: ${token.take(10)}...")

                val response = apiService.getGuidanceConfiguration("Bearer $token")

                Log.d("MentorConfigRepository", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val configuration = response.body()
                    if (configuration != null) {
                        Log.d("MentorConfigRepository", "Successfully received configuration: ID=${configuration.configurationId}")
                        Result.success(configuration)
                    } else {
                        Log.e("MentorConfigRepository", "Response body is null")
                        Result.failure(Exception("Configuration data is null"))
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("MentorConfigRepository", "API Error in getGuidanceConfiguration: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get configuration: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("MentorConfigRepository", "Exception in getGuidanceConfiguration", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Update mentor guidance configuration
     */
    suspend fun updateGuidanceConfiguration(
        configId: Int,
        request: UpdateMentorConfigurationRequest
    ): Result<UpdateMentorConfigurationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                Log.d("MentorConfigRepository", "Updating guidance configuration ID: $configId with token: ${token.take(10)}...")

                val response = apiService.updateGuidanceConfiguration(
                    configId = configId,
                    token = "Bearer $token",
                    request = request
                )

                Log.d("MentorConfigRepository", "Update response code: ${response.code()}")

                if (response.isSuccessful) {
                    val updateResponse = response.body()
                    if (updateResponse != null) {
                        Log.d("MentorConfigRepository", "Successfully updated configuration: ${updateResponse.message}")
                        Result.success(updateResponse)
                    } else {
                        Log.e("MentorConfigRepository", "Update response body is null")
                        Result.failure(Exception("Update response data is null"))
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("MentorConfigRepository", "API Error in updateGuidanceConfiguration: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to update configuration: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("MentorConfigRepository", "Exception in updateGuidanceConfiguration", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Get guidance configuration (Response version)
     */
    suspend fun getGuidanceConfigurationResponse(): Response<MentorConfigurationResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getGuidanceConfiguration("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Update guidance configuration (Response version)
     */
    suspend fun updateGuidanceConfigurationResponse(
        configId: Int,
        request: UpdateMentorConfigurationRequest
    ): Response<UpdateMentorConfigurationResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.updateGuidanceConfiguration(configId, "Bearer $token", request)
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }
}





//package com.cc.creatorcircle.data.repository
//
//import android.content.Context
//import android.util.Log
//import com.cc.creatorcircle.data.api.RetrofitInstance
//import com.cc.creatorcircle.data.models.MentorConfigurationResponse
//import com.cc.creatorcircle.utils.TokenManager
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import retrofit2.Response
//
//class MentorConfigRepository(private val context: Context) {
//
//    private val apiService = RetrofitInstance.api
//    private val tokenManager = TokenManager(context)
//
//    /**
//     * Get mentor guidance configuration
//     */
//    suspend fun getGuidanceConfiguration(): Result<MentorConfigurationResponse> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val token = tokenManager.getToken()
//                if (token.isEmpty()) {
//                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
//                }
//
//                Log.d("MentorConfigRepository", "Fetching guidance configuration with token: ${token.take(10)}...")
//
//                val response = apiService.getGuidanceConfiguration("Bearer $token")
//
//                Log.d("MentorConfigRepository", "Response code: ${response.code()}")
//
//                if (response.isSuccessful) {
//                    val configuration = response.body()
//                    if (configuration != null) {
//                        Log.d("MentorConfigRepository", "Successfully received configuration: ID=${configuration.configurationId}")
//                        Result.success(configuration)
//                    } else {
//                        Log.e("MentorConfigRepository", "Response body is null")
//                        Result.failure(Exception("Configuration data is null"))
//                    }
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
//                    Log.e("MentorConfigRepository", "API Error in getGuidanceConfiguration: ${response.code()} - $errorMessage")
//                    Result.failure(Exception("Failed to get configuration: ${response.code()} - $errorMessage"))
//                }
//            } catch (e: Exception) {
//                Log.e("MentorConfigRepository", "Exception in getGuidanceConfiguration", e)
//                Result.failure(e)
//            }
//        }
//    }
//
//    /**
//     * Alternative method using Response<T> pattern
//     * Get guidance configuration (Response version)
//     */
//    suspend fun getGuidanceConfigurationResponse(): Response<MentorConfigurationResponse> {
//        val token = tokenManager.getToken()
//        return if (token.isNotEmpty()) {
//            apiService.getGuidanceConfiguration("Bearer $token")
//        } else {
//            throw Exception("Access token not found. Please log in again.")
//        }
//    }
//}
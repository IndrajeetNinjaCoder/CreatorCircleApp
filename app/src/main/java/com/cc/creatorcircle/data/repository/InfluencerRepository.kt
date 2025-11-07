package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.InfluencerFilterRequest
import com.cc.creatorcircle.data.models.Influencers
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class InfluencerRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get suggested influencers
     */
    suspend fun getSuggestedInfluencers(): Result<List<Influencers>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getSuggestedInfluencers("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { influencersList ->
                        Log.d("InfluencerRepository", "Successfully fetched ${influencersList.size} influencers")
                        Result.success(influencersList)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("InfluencerRepository", "API Error in getSuggestedInfluencers: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get suggested influencers: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("InfluencerRepository", "Exception in getSuggestedInfluencers", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     */
    suspend fun getSuggestedInfluencersResponse(): Response<List<Influencers>> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getSuggestedInfluencers("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    suspend fun getFilteredInfluencers(filterRequest: InfluencerFilterRequest): Result<List<Influencers>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                Log.d("InfluencerRepository", "Applying filters: $filterRequest")
                val response = apiService.getFilteredInfluencers("Bearer $token", filterRequest)

                if (response.isSuccessful) {
                    response.body()?.let { influencersList ->
                        Log.d("InfluencerRepository", "Successfully fetched ${influencersList.size} filtered influencers")
                        Result.success(influencersList)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("InfluencerRepository", "API Error in getFilteredInfluencers: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get filtered influencers: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("InfluencerRepository", "Exception in getFilteredInfluencers", e)
                Result.failure(e)
            }
        }
    }
}
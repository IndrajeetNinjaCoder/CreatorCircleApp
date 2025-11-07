package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.utils.TokenManager
import com.example.creatorcircle.models.ApplyToDealRequest
import com.example.creatorcircle.models.ApplyToDealResponse
import com.example.creatorcircle.models.BrandsResponse
import com.example.creatorcircle.models.DealsResponse
import com.example.creatorcircle.models.GenerateEmailRequest
import com.example.creatorcircle.models.GenerateEmailResponse
import com.example.creatorcircle.models.SaveDefaultFiltersRequest
import com.example.creatorcircle.models.SaveDefaultFiltersResponse
import com.example.creatorcircle.models.SavedFilters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class DealsRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get deals with optional filters
     */
    suspend fun getDeals(
        category: String? = null,
        collaborationType: String? = null,
        location: String? = null,
        minAmount: String? = null,
        maxAmount: String? = null,
        status: String? = null,
        brandId: Int? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<DealsResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken()
            if (token.isEmpty()) {
                return@withContext kotlin.Result.failure(
                    Exception("Access token not found. Please log in again.")
                )
            }

            val response = apiService.getDeals(
                token = "Bearer $token",
                category = category,
                collaborationType = collaborationType,
                location = location,
                minAmount = minAmount,
                maxAmount = maxAmount,
                status = status,
                brandId = brandId
            )

            if (response.isSuccessful) {
                response.body()?.let { dealsResponse ->
                    Log.d("DealsRepository", "Successfully fetched ${dealsResponse.deals.size} deals")
                    kotlin.Result.success(dealsResponse)
                } ?: kotlin.Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Log.e("DealsRepository", "API Error in getDeals: ${response.code()} - $errorMessage")
                kotlin.Result.failure(
                    Exception("Failed to get deals: ${response.code()} - $errorMessage")
                )
            }
        } catch (e: Exception) {
            Log.e("DealsRepository", "Exception in getDeals", e)
            kotlin.Result.failure(e)
        }
    }

    /**
     * Get active deals only
     */
    suspend fun getActiveDeals(
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<DealsResponse> {
        return getDeals(
            status = "active",
            limit = limit,
            offset = offset
        )
    }

    /**
     * Get deals by category
     */
    suspend fun getDealsByCategory(
        category: String,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<DealsResponse> {
        return getDeals(
            category = category,
            limit = limit,
            offset = offset
        )
    }

    /**
     * Get deals by collaboration type
     */
    suspend fun getDealsByCollaborationType(
        collaborationType: String,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<DealsResponse> {
        return getDeals(
            collaborationType = collaborationType,
            limit = limit,
            offset = offset
        )
    }

    /**
     * Alternative method using Response<T> pattern
     * Get deals (Response version)
     */
    suspend fun getDealsResponse(
        category: String? = null,
        collaborationType: String? = null,
        location: String? = null,
        minAmount: String? = null,
        maxAmount: String? = null,
        status: String? = null,
        brandId: Int? = null
    ): Response<DealsResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getDeals(
                token = "Bearer $token",
                category = category,
                collaborationType = collaborationType,
                location = location,
                minAmount = minAmount,
                maxAmount = maxAmount,
                status = status,
                brandId = brandId
            )
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Get brands with optional filters
     */
    suspend fun getBrands(
        category: String? = null,
        states: List<String>? = null,
        country: String? = null,
        profileId: Int? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<BrandsResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken()
            if (token.isEmpty()) {
                return@withContext kotlin.Result.failure(
                    Exception("Access token not found. Please log in again.")
                )
            }

            val response = apiService.getBrands(
                token = "Bearer $token",
                category = category,
                states = states,
                country = country,
                profileId = profileId,
                limit = limit,
                offset = offset
            )

            if (response.isSuccessful) {
                response.body()?.let { brandsResponse ->
                    Log.d("DealsRepository", "Successfully fetched ${brandsResponse.data.size} brands")
                    kotlin.Result.success(brandsResponse)
                } ?: kotlin.Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Log.e("DealsRepository", "API Error in getBrands: ${response.code()} - $errorMessage")
                kotlin.Result.failure(
                    Exception("Failed to get brands: ${response.code()} - $errorMessage")
                )
            }
        } catch (e: Exception) {
            Log.e("DealsRepository", "Exception in getBrands", e)
            kotlin.Result.failure(e)
        }
    }

    /**
     * Get brands by category
     */
    suspend fun getBrandsByCategory(
        category: String,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<BrandsResponse> {
        return getBrands(
            category = category,
            limit = limit,
            offset = offset
        )
    }

    /**
     * Get brands by country
     */
    suspend fun getBrandsByCountry(
        country: String,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<BrandsResponse> {
        return getBrands(
            country = country,
            limit = limit,
            offset = offset
        )
    }

    /**
     * Get brands by states
     */
    suspend fun getBrandsByStates(
        states: List<String>,
        country: String? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ): kotlin.Result<BrandsResponse> {
        return getBrands(
            states = states,
            country = country,
            limit = limit,
            offset = offset
        )
    }

    /**
     * Alternative method using Response<T> pattern
     * Get brands (Response version)
     */
    suspend fun getBrandsResponse(
        category: String? = null,
        states: List<String>? = null,
        country: String? = null,
        profileId: Int? = null,
        limit: Int? = 20,
        offset: Int? = 0
    ): Response<BrandsResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getBrands(
                token = "Bearer $token",
                category = category,
                states = states,
                country = country,
                profileId = profileId,
                limit = limit,
                offset = offset
            )
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Save default filters for a profile
     */
    suspend fun saveDefaultFilters(
        profileId: Int,
        preferredCategories: List<String>,
        preferredLocations: List<String>
    ): kotlin.Result<SaveDefaultFiltersResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken()
            if (token.isEmpty()) {
                return@withContext kotlin.Result.failure(
                    Exception("Access token not found. Please log in again.")
                )
            }

            val request = SaveDefaultFiltersRequest(
                profileId = profileId,
                preferredCategories = preferredCategories,
                preferredLocations = preferredLocations
            )

            val response = apiService.saveDefaultFilters(
                request = request,
                token = "Bearer $token"
            )

            if (response.isSuccessful) {
                response.body()?.let { filtersResponse ->
                    Log.d("DealsRepository", "Successfully saved default filters for profile $profileId")
                    kotlin.Result.success(filtersResponse)
                } ?: kotlin.Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Log.e("DealsRepository", "API Error in saveDefaultFilters: ${response.code()} - $errorMessage")
                kotlin.Result.failure(
                    Exception("Failed to save default filters: ${response.code()} - $errorMessage")
                )
            }
        } catch (e: Exception) {
            Log.e("DealsRepository", "Exception in saveDefaultFilters", e)
            kotlin.Result.failure(e)
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Save default filters (Response version)
     */
    suspend fun saveDefaultFiltersResponse(
        profileId: Int,
        preferredCategories: List<String>,
        preferredLocations: List<String>
    ): Response<SaveDefaultFiltersResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            val request = SaveDefaultFiltersRequest(
                profileId = profileId,
                preferredCategories = preferredCategories,
                preferredLocations = preferredLocations
            )
            apiService.saveDefaultFilters(
                request = request,
                token = "Bearer $token"
            )
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }


    suspend fun generateEmail(
        brandId: Int,
        profileId: Int
    ): kotlin.Result<GenerateEmailResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken()
            if (token.isEmpty()) {
                return@withContext kotlin.Result.failure(
                    Exception("Access token not found. Please log in again.")
                )
            }

            val request = GenerateEmailRequest(
                brandId = brandId,
                profileId = profileId
            )

            val response = apiService.generateEmail(
                request = request,
                token = "Bearer $token"
            )

            if (response.isSuccessful) {
                response.body()?.let { emailResponse ->
                    Log.d("DealsRepository", "Successfully generated email for brand $brandId")
                    kotlin.Result.success(emailResponse)
                } ?: kotlin.Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Log.e("DealsRepository", "API Error in generateEmail: ${response.code()} - $errorMessage")
                kotlin.Result.failure(
                    Exception("Failed to generate email: ${response.code()} - $errorMessage")
                )
            }
        } catch (e: Exception) {
            Log.e("DealsRepository", "Exception in generateEmail", e)
            kotlin.Result.failure(e)
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Generate email (Response version)
     */
    suspend fun generateEmailResponse(
        brandId: Int,
        profileId: Int
    ): Response<GenerateEmailResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            val request = GenerateEmailRequest(
                brandId = brandId,
                profileId = profileId
            )
            apiService.generateEmail(
                request = request,
                token = "Bearer $token"
            )
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }



    /**
     * Apply to a deal
     */
    suspend fun applyToDeal(
        dealId: Int,
        phoneNumber: String,
        instagramUsername: String,
        location: String
    ): kotlin.Result<ApplyToDealResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getToken()
            if (token.isEmpty()) {
                return@withContext kotlin.Result.failure(
                    Exception("Access token not found. Please log in again.")
                )
            }

            val request = ApplyToDealRequest(
                phoneNumber = phoneNumber,
                instagramUsername = instagramUsername,
                location = location
            )

            val response = apiService.applyToDeal(
                dealId = dealId,
                request = request,
                token = "Bearer $token"
            )

            if (response.isSuccessful) {
                response.body()?.let { applyResponse ->
                    Log.d("DealsRepository", "Successfully applied to deal $dealId")
                    kotlin.Result.success(applyResponse)
                } ?: kotlin.Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Log.e("DealsRepository", "API Error in applyToDeal: ${response.code()} - $errorMessage")
                kotlin.Result.failure(
                    Exception("Failed to apply to deal: ${response.code()} - $errorMessage")
                )
            }
        } catch (e: Exception) {
            Log.e("DealsRepository", "Exception in applyToDeal", e)
            kotlin.Result.failure(e)
        }
    }

}









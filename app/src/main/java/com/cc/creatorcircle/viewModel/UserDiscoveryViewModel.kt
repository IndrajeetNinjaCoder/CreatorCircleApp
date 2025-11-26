package com.cc.creatorcircle.viewModel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.UserDiscoveryResponse
import com.cc.creatorcircle.data.models.UserWithScore
import kotlinx.coroutines.launch

class UserDiscoveryViewModel : ViewModel() {

    // UI States using mutableStateOf for Compose
    var discoveryState by mutableStateOf<Result<UserDiscoveryResponse>?>(null)
        private set

    var recommendedUsers by mutableStateOf<List<UserWithScore>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // UI State data
    var totalCount by mutableStateOf(0)
        private set

    var nextOffset by mutableStateOf(0)
        private set

    var hasMore by mutableStateOf(true)
        private set

    // Pagination states
    private var currentOffset = 0
    private val limit = 50

    fun loadRecommendations(context: Context, refresh: Boolean = false) {
        if (isLoading) return

        viewModelScope.launch {
            try {
                discoveryState = null
                isLoading = true
                errorMessage = null

                if (refresh) {
                    currentOffset = 0
                    hasMore = true
                    recommendedUsers = emptyList()
                }

                val token = getAccessToken(context)
                if (token.isNullOrEmpty()) {
                    errorMessage = "Authentication token not found"
                    discoveryState = Result.failure(Exception("Authentication token not found"))
                    return@launch
                }

                println("UserDiscoveryViewModel: Making API call with token")
                val response = RetrofitInstance.api.getRecommendations(
                    token = "Bearer $token",
                    nested = limit
                )

                println("UserDiscoveryViewModel: Response - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val discoveryResponse = response.body()!!
                    println("UserDiscoveryViewModel: Received ${discoveryResponse.users.size} users")

                    // Debug: Print first user
                    if (discoveryResponse.users.isNotEmpty()) {
                        val firstUser = discoveryResponse.users.first()
                        println("UserDiscoveryViewModel: First user - ID: ${firstUser.userId}, Name: ${firstUser.username}")
                    }

                    val currentUsers = if (refresh) emptyList() else recommendedUsers
                    val newUsers = currentUsers + discoveryResponse.users

                    recommendedUsers = newUsers
                    totalCount = discoveryResponse.totalCount
                    nextOffset = discoveryResponse.nextOffset
                    hasMore = discoveryResponse.hasMore
                    currentOffset = discoveryResponse.nextOffset

                    println("UserDiscoveryViewModel: Total users now: ${recommendedUsers.size}")
                    println("UserDiscoveryViewModel: Has more: $hasMore")

                    discoveryState = Result.success(discoveryResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("UserDiscoveryViewModel: API Error - Code: ${response.code()}")
                    println("UserDiscoveryViewModel: Error body: $errorBody")
                    errorMessage = "Failed to load recommendations: ${response.code()}"
                    discoveryState = Result.failure(Exception("Failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("UserDiscoveryViewModel: Exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error: ${e.message}"
                discoveryState = Result.failure(e)
            } finally {
                isLoading = false
                println("UserDiscoveryViewModel: Loading finished. Users count: ${recommendedUsers.size}")
            }
        }
    }

    fun loadMoreRecommendations(context: Context) {
        if (!hasMore || isLoading) {
            println("UserDiscoveryViewModel: Cannot load more - hasMore: $hasMore, isLoading: $isLoading")
            return
        }
        println("UserDiscoveryViewModel: Loading more recommendations...")
        loadRecommendations(context, refresh = false)
    }

    fun refreshRecommendations(context: Context) {
        println("UserDiscoveryViewModel: Refreshing recommendations...")
        loadRecommendations(context, refresh = true)
    }

    fun clearError() {
        errorMessage = null
    }

    fun getUserById(userId: Int): UserWithScore? {
        return recommendedUsers.find { it.userId == userId }
    }

    fun getUsersByCategory(category: String): List<UserWithScore> {
        return recommendedUsers.filter { userWithScore ->
            userWithScore.categories?.contains(category) == true
        }
    }

    fun getTopScoredUsers(count: Int = 10): List<UserWithScore> {
        return recommendedUsers
            .sortedByDescending { it.totalScore }
            .take(count)
    }

    fun searchUsers(query: String): List<UserWithScore> {
        return recommendedUsers.filter { userWithScore ->
            userWithScore.username?.contains(query, ignoreCase = true) == true ||
                    userWithScore.fullName?.contains(query, ignoreCase = true) == true ||
                    userWithScore.bio?.contains(query, ignoreCase = true) == true ||
                    userWithScore.categories?.any { it.contains(query, ignoreCase = true) } == true
        }
    }

    fun sendConnectionRequest(context: Context, userId: Int) {
        viewModelScope.launch {
            try {
                val token = getAccessToken(context)
                if (token.isNullOrEmpty()) {
                    errorMessage = "Authentication token not found"
                    return@launch
                }

                println("UserDiscoveryViewModel: Sending connection request to user: $userId")
                val response = RetrofitInstance.api.sendConnectionRequest(
                    token = "Bearer $token",
                    userId = userId
                )

                if (response.isSuccessful) {
                    // Update the user's connection status locally
                    val updatedUsers = recommendedUsers.map { userWithScore ->
                        if (userWithScore.userId == userId) {
                            userWithScore.copy(
                                isConnected = true,
                                connectionStatus = "pending"
                            )
                        } else {
                            userWithScore
                        }
                    }
                    recommendedUsers = updatedUsers
                    println("UserDiscoveryViewModel: Connection request sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("UserDiscoveryViewModel: Connection request failed - ${response.code()}")
                    errorMessage = "Failed to send connection request"
                }
            } catch (e: Exception) {
                println("UserDiscoveryViewModel: Connection request error: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error sending connection request"
            }
        }
    }

    private fun getAccessToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("access_token", null)
    }

    fun isUserLoggedIn(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        val hasAccessToken = !sharedPrefs.getString("access_token", null).isNullOrEmpty()
        return isLoggedIn && hasAccessToken
    }

    fun getAllCategories(): List<String> {
        return recommendedUsers
            .flatMap { it.categories ?: emptyList() }
            .distinct()
            .sorted()
    }

    fun getCategoryStats(): Map<String, Int> {
        return recommendedUsers
            .flatMap { userWithScore ->
                userWithScore.categories?.map { category -> category to userWithScore } ?: emptyList()
            }
            .groupBy { it.first }
            .mapValues { it.value.size }
    }

    fun debugRecommendations() {
        println("=== DEBUGGING RECOMMENDATIONS ===")
        println("Total users: ${recommendedUsers.size}")
        println("Has more: $hasMore")
        println("Current offset: $currentOffset")
        println("Total count: $totalCount")
        println("Is loading: $isLoading")
        println("Error: $errorMessage")
        if (recommendedUsers.isNotEmpty()) {
            println("First user: ${recommendedUsers.first().username}")
        }
        println("=================================")
    }
}


















//package com.cc.creatorcircle.viewModel
//
//import android.content.Context
//import androidx.compose.runtime.*
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cc.creatorcircle.data.api.RetrofitInstance
//import com.cc.creatorcircle.data.models.DiscoveryUser
//import com.cc.creatorcircle.data.models.UserDiscoveryResponse
//import com.cc.creatorcircle.data.models.UserWithScore
//import kotlinx.coroutines.launch
//import kotlin.Result
//
//class UserDiscoveryViewModel : ViewModel() {
//
//    // UI States using mutableStateOf for Compose
//    var discoveryState by mutableStateOf<Result<UserDiscoveryResponse>?>(null)
//        private set
//
//    var recommendedUsers by mutableStateOf<List<UserWithScore>>(emptyList())
//        private set
//
//    var isLoading by mutableStateOf(false)
//        private set
//
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    // UI State data
//    var totalCount by mutableStateOf(0)
//        private set
//
//    var nextOffset by mutableStateOf(0)
//        private set
//
//    var hasMore by mutableStateOf(true)
//        private set
//
//    // Pagination states
//    private var currentOffset = 0
//    private val limit = 1000000
//
//    fun loadRecommendations(context: Context, refresh: Boolean = false) {
//        if (isLoading) return
//
//        viewModelScope.launch {
//            try {
//                discoveryState = null
//                isLoading = true
//                errorMessage = null
//
//                if (refresh) {
//                    currentOffset = 0
//                    hasMore = true
//                    recommendedUsers = emptyList()
//                }
//
//                val token = getAccessToken(context)
//                if (token.isNullOrEmpty()) {
//                    errorMessage = "Authentication token not found"
//                    discoveryState = Result.failure(Exception("Authentication token not found"))
//                    return@launch
//                }
//
//                println("UserDiscoveryViewModel: Making API call to get recommendations with token: Bearer $token")
//                val response = RetrofitInstance.api.getRecommendations(
//                    token = "Bearer $token",
////                    limit = limit,
////                    offset = currentOffset
//                )
//
//                println("UserDiscoveryViewModel: API response received - Success: ${response.isSuccessful}, Code: ${response.code()}")
//
//                if (response.isSuccessful && response.body() != null) {
//                    val discoveryResponse = response.body()!!
//                    println("UserDiscoveryViewModel: Raw API Response Body: $discoveryResponse")
//
//                    val currentUsers = if (refresh) emptyList() else recommendedUsers
//                    val newUsers = currentUsers + discoveryResponse.users
//
//                    recommendedUsers = newUsers
//                    totalCount = discoveryResponse.totalCount
//                    nextOffset = discoveryResponse.nextOffset
//                    hasMore = discoveryResponse.hasMore
//
//                    currentOffset = discoveryResponse.nextOffset
//
//                    println("UserDiscoveryViewModel: Successfully loaded ${discoveryResponse.users.size} users")
//                    println("UserDiscoveryViewModel: Total users in list: ${recommendedUsers.size}")
//                    println("UserDiscoveryViewModel: Has more: $hasMore")
//
//                    discoveryState = Result.success(discoveryResponse)
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    println("UserDiscoveryViewModel: Loading recommendations failed - Code: ${response.code()}, Error: $errorBody")
//                    errorMessage = "Failed to load recommendations: ${response.code()} - $errorBody"
//                    discoveryState = Result.failure(Exception("Failed to load recommendations: ${response.code()} - $errorBody"))
//                }
//            } catch (e: Exception) {
//                println("UserDiscoveryViewModel: Loading recommendations error: ${e.message}")
//                e.printStackTrace()
//                errorMessage = "Error loading recommendations: ${e.message}"
//                discoveryState = Result.failure(e)
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    fun loadMoreRecommendations(context: Context) {
//        if (!hasMore || isLoading) {
//            println("UserDiscoveryViewModel: Cannot load more - hasMore: $hasMore, isLoading: $isLoading")
//            return
//        }
//        println("UserDiscoveryViewModel: Loading more recommendations...")
//        loadRecommendations(context, refresh = false)
//    }
//
//    fun refreshRecommendations(context: Context) {
//        println("UserDiscoveryViewModel: Refreshing recommendations...")
//        loadRecommendations(context, refresh = true)
//    }
//
//    fun clearError() {
//        errorMessage = null
//    }
//
//    fun getUserById(userId: Int): DiscoveryUser? {
//        return recommendedUsers.find { it.user.userId == userId }?.user
//    }
//
//    fun getUsersByCategory(category: String): List<UserWithScore> {
//        return recommendedUsers.filter { userWithScore ->
//            userWithScore.user.categories.contains(category)
//        }
//    }
//
//    fun getTopScoredUsers(count: Int = 10): List<UserWithScore> {
//        return recommendedUsers
//            .sortedByDescending { it.totalScore }
//            .take(count)
//    }
//
//    fun searchUsers(query: String): List<UserWithScore> {
//        return recommendedUsers.filter { userWithScore ->
//            val user = userWithScore.user
//            user.username?.contains(query, ignoreCase = true) == true ||
//                    user.fullName?.contains(query, ignoreCase = true) == true ||
//                    user.bio?.contains(query, ignoreCase = true) == true ||
//                    user.categories.any { it.contains(query, ignoreCase = true) }
//        }
//    }
//
//    // Function to handle connection requests
//    fun sendConnectionRequest(context: Context, userId: Int) {
//        viewModelScope.launch {
//            try {
//                val token = getAccessToken(context)
//                if (token.isNullOrEmpty()) {
//                    errorMessage = "Authentication token not found"
//                    return@launch
//                }
//
//                println("UserDiscoveryViewModel: Sending connection request to user: $userId")
//                val response = RetrofitInstance.api.sendConnectionRequest(
//                    token = "Bearer $token",
//                    userId = userId
//                )
//
//                if (response.isSuccessful) {
//                    // Update the user's connection status locally
//                    val updatedUsers = recommendedUsers.map { userWithScore ->
//                        if (userWithScore.user.userId == userId) {
//                            userWithScore.copy(
//                                user = userWithScore.user.copy(
//                                    isConnected = true,
//                                    connectionStatus = "pending"
//                                )
//                            )
//                        } else {
//                            userWithScore
//                        }
//                    }
//                    recommendedUsers = updatedUsers
//                    println("UserDiscoveryViewModel: Connection request sent successfully")
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    println("UserDiscoveryViewModel: Connection request failed - Code: ${response.code()}, Error: $errorBody")
//                    errorMessage = "Failed to send connection request: ${response.code()} - $errorBody"
//                }
//            } catch (e: Exception) {
//                println("UserDiscoveryViewModel: Connection request error: ${e.message}")
//                e.printStackTrace()
//                errorMessage = "Error sending connection request: ${e.message}"
//            }
//        }
//    }
//
//
//
//
//    // Helper function to get saved access token
//    private fun getAccessToken(context: Context): String? {
//        val sharedPrefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//        return sharedPrefs.getString("access_token", null)
//    }
//
//    // Helper function to check if user is logged in
//    fun isUserLoggedIn(context: Context): Boolean {
//        val sharedPrefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
//        val hasAccessToken = !sharedPrefs.getString("access_token", null).isNullOrEmpty()
//        println("UserDiscoveryViewModel: User logged in status - isLoggedIn: $isLoggedIn, hasAccessToken: $hasAccessToken")
//        return isLoggedIn && hasAccessToken
//    }
//
//    // Helper function to get categories from all users
//    fun getAllCategories(): List<String> {
//        return recommendedUsers
//            .flatMap { it.user.categories }
//            .distinct()
//            .sorted()
//    }
//
//    // Helper function to get users count by category
//    fun getCategoryStats(): Map<String, Int> {
//        return recommendedUsers
//            .flatMap { userWithScore ->
//                userWithScore.user.categories.map { category -> category to userWithScore }
//            }
//            .groupBy { it.first }
//            .mapValues { it.value.size }
//    }
//
//    // Debug function
//    fun debugRecommendations() {
//        println("UserDiscoveryViewModel: === DEBUGGING RECOMMENDATIONS DATA ===")
//        println("UserDiscoveryViewModel: Total users loaded: ${recommendedUsers.size}")
//        println("UserDiscoveryViewModel: Has more users: $hasMore")
//        println("UserDiscoveryViewModel: Current offset: $currentOffset")
//        println("UserDiscoveryViewModel: Total count from API: $totalCount")
//        println("UserDiscoveryViewModel: Is loading: $isLoading")
//        println("UserDiscoveryViewModel: Error message: $errorMessage")
//
//        if (recommendedUsers.isNotEmpty()) {
//            println("UserDiscoveryViewModel: Sample user: ${recommendedUsers.first().user.username}")
//            println("UserDiscoveryViewModel: Sample user categories: ${recommendedUsers.first().user.categories}")
//            println("UserDiscoveryViewModel: Sample user score: ${recommendedUsers.first().totalScore}")
//        }
//        println("UserDiscoveryViewModel: ================================================")
//    }
//}
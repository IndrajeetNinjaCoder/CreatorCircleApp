package com.cc.creatorcircle.viewModel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.*
import kotlinx.coroutines.launch
import kotlin.Result

// Filter options for connections
data class ConnectionsFilter(
    val minFollowers: Int = 0,
    val requireProfilePic: Boolean = false,
    val platform: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.ALL
)

enum class ConnectionStatus {
    ALL, FOLLOWING, FOLLOWERS, CONNECTED, NOT_CONNECTED
}

class UserConnectionsViewModel : ViewModel() {

    // UI States using mutableStateOf for Compose
    var connectionsState by mutableStateOf<Result<SocialMediaResponse>?>(null)
        private set

    var growthSuggestions by mutableStateOf<List<ConnectionUser>>(emptyList())
        private set

    var pendingToAccept by mutableStateOf<List<ConnectionUser>>(emptyList())
        private set

    var sentConnections by mutableStateOf<List<ConnectionUser>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Filter state
    var currentFilter by mutableStateOf(ConnectionsFilter())
        private set

    // Stats
    var totalGrowthSuggestions by mutableStateOf(0)
        private set

    var totalPendingConnections by mutableStateOf(0)
        private set

    var pendingToAcceptCount by mutableStateOf(0)
        private set

    var sentRequestsCount by mutableStateOf(0)
        private set

    // Cache original data to avoid API calls when filtering
    private var originalData: SocialMediaResponse? = null

    fun loadUserConnections(context: Context, forceRefresh: Boolean = false) {
        if (isLoading && !forceRefresh) return

        viewModelScope.launch {
            try {
                connectionsState = null
                if (forceRefresh) {
                    isRefreshing = true
                } else {
                    isLoading = true
                }
                errorMessage = null

                val token = getAccessToken(context)
                if (token.isNullOrEmpty()) {
                    errorMessage = "Authentication token not found"
                    connectionsState = Result.failure(Exception("Authentication token not found"))
                    return@launch
                }

                println("UserConnectionsViewModel: Making API call to get connections with token: Bearer $token")
                val response = RetrofitInstance.api.getUserConnections("Bearer $token")

                println("UserConnectionsViewModel: API response received - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val connectionsResponse = response.body()!!
                    println("UserConnectionsViewModel: Raw API Response Body: $connectionsResponse")

                    // Cache original data
                    originalData = connectionsResponse

                    // Update all states with null safety
                    updateConnectionsData(connectionsResponse)

                    println("UserConnectionsViewModel: Successfully loaded connections")
                    println("UserConnectionsViewModel: Growth suggestions: ${growthSuggestions.size}")
                    println("UserConnectionsViewModel: Pending to accept: ${pendingToAccept.size}")
                    println("UserConnectionsViewModel: Sent requests: ${sentConnections.size}")

                    connectionsState = Result.success(connectionsResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("UserConnectionsViewModel: Loading connections failed - Code: ${response.code()}, Error: $errorBody")
                    val errorMsg = when (response.code()) {
                        401 -> "Authentication failed. Please login again."
                        403 -> "Access denied."
                        404 -> "Connections not found."
                        429 -> "Too many requests. Please try again later."
                        500 -> "Server error. Please try again later."
                        else -> "Failed to load connections: ${response.code()} - $errorBody"
                    }
                    errorMessage = errorMsg
                    connectionsState = Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                println("UserConnectionsViewModel: Loading connections error: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error loading connections: ${e.message}"
                connectionsState = Result.failure(e)
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun refreshConnections(context: Context) {
        println("UserConnectionsViewModel: Refreshing connections...")
        loadUserConnections(context, forceRefresh = true)
    }

    fun applyFilter(filter: ConnectionsFilter) {
        currentFilter = filter
        println("UserConnectionsViewModel: Applying filter - minFollowers: ${filter.minFollowers}, requireProfilePic: ${filter.requireProfilePic}, platform: ${filter.platform}, status: ${filter.connectionStatus}")

        originalData?.let { data ->
            // Handle null growthSuggestions
            val filteredGrowthSuggestions = filterConnectionUsers(data.growthSuggestions ?: emptyList(), filter)
            val filteredToAccept = filterConnectionUsers(data.pendingConnections.toAccept.users, filter)
            val filteredSent = filterConnectionUsers(data.pendingConnections.sent.users, filter)

            // Update filtered data
            growthSuggestions = filteredGrowthSuggestions
            pendingToAccept = filteredToAccept
            sentConnections = filteredSent

            // Update counts
            pendingToAcceptCount = filteredToAccept.size
            sentRequestsCount = filteredSent.size
            totalGrowthSuggestions = filteredGrowthSuggestions.size
            totalPendingConnections = filteredToAccept.size + filteredSent.size

            println("UserConnectionsViewModel: Filter applied - Growth suggestions: ${filteredGrowthSuggestions.size}")
        }
    }

    fun clearFilters() {
        println("UserConnectionsViewModel: Clearing all filters...")
        currentFilter = ConnectionsFilter()
        originalData?.let { updateConnectionsData(it) }
    }

    fun searchUsers(query: String): List<ConnectionUser> {
        val allUsers = growthSuggestions + pendingToAccept + sentConnections

        return allUsers.filter { user ->
            user.username?.contains(query, ignoreCase = true) == true ||
                    user.fullName?.contains(query, ignoreCase = true) == true
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

                println("UserConnectionsViewModel: Sending connection request to user: $userId")
                val response = RetrofitInstance.api.sendConnectionRequest(
                    token = "Bearer $token",
                    userId = userId
                )

                if (response.isSuccessful) {
                    // Update the user's connection status locally
                    val updatedGrowthSuggestions = growthSuggestions.map { user ->
                        if (user.userId == userId) {
                            user.copy(isConnected = true)
                        } else {
                            user
                        }
                    }
                    growthSuggestions = updatedGrowthSuggestions

                    // Also update original data
                    originalData = originalData?.copy(
                        growthSuggestions = originalData!!.growthSuggestions?.map { user ->
                            if (user.userId == userId) {
                                user.copy(isConnected = true)
                            } else {
                                user
                            }
                        }
                    )

                    println("UserConnectionsViewModel: Connection request sent successfully")
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("UserConnectionsViewModel: Connection request failed - Code: ${response.code()}, Error: $errorBody")
                    errorMessage = "Failed to send connection request: ${response.code()} - $errorBody"
                }
            } catch (e: Exception) {
                println("UserConnectionsViewModel: Connection request error: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error sending connection request: ${e.message}"
            }
        }
    }

    fun acceptConnectionRequest(context: Context, connectionId: Int) {
        viewModelScope.launch {
            try {
                val token = getAccessToken(context)
                if (token.isNullOrEmpty()) {
                    errorMessage = "Authentication token not found"
                    return@launch
                }

                println("UserConnectionsViewModel: Accepting connection request: $connectionId")
                // You'll need to implement this API endpoint
                // val response = RetrofitInstance.api.acceptConnectionRequest("Bearer $token", connectionId)

                // For now, update locally
                val updatedPendingToAccept = pendingToAccept.filter { it.connectionId != connectionId }
                pendingToAccept = updatedPendingToAccept
                pendingToAcceptCount = updatedPendingToAccept.size

                println("UserConnectionsViewModel: Connection request accepted successfully")
            } catch (e: Exception) {
                println("UserConnectionsViewModel: Accept connection error: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error accepting connection request: ${e.message}"
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun getUserById(userId: Int): ConnectionUser? {
        val allUsers = growthSuggestions + pendingToAccept + sentConnections
        return allUsers.find { it.userId == userId }
    }

    fun getUsersByFollowersRange(minFollowers: Int, maxFollowers: Int): List<ConnectionUser> {
        return growthSuggestions.filter { user ->
            user.followersCount in minFollowers..maxFollowers
        }
    }

    fun getUsersWithInstagram(): List<ConnectionUser> {
        return growthSuggestions.filter { it.hasInstagramFollowers() }
    }

    fun getTopFollowersUsers(count: Int = 10): List<ConnectionUser> {
        return growthSuggestions
            .sortedByDescending { it.followersCount }
            .take(count)
    }

    // Private helper functions
    private fun updateConnectionsData(data: SocialMediaResponse) {
        // Handle null growthSuggestions safely
        growthSuggestions = data.growthSuggestions ?: emptyList()
        pendingToAccept = data.pendingConnections.toAccept.users
        sentConnections = data.pendingConnections.sent.users

        totalGrowthSuggestions = data.getTotalGrowthSuggestions()
        totalPendingConnections = data.getTotalPendingConnections()
        pendingToAcceptCount = data.pendingConnections.toAccept.count
        sentRequestsCount = data.pendingConnections.sent.count
    }

    private fun filterConnectionUsers(
        users: List<ConnectionUser>,
        filter: ConnectionsFilter
    ): List<ConnectionUser> {
        return users
            .let { if (filter.minFollowers > 0) it.filterByFollowersCount(filter.minFollowers) else it }
            .let { if (filter.requireProfilePic) it.filterWithProfilePic() else it }
            .let { if (filter.platform != null) it.filterByPlatform(filter.platform) else it }
            .let { filterByConnectionStatus(it, filter.connectionStatus) }
    }

    private fun filterByConnectionStatus(
        users: List<ConnectionUser>,
        status: ConnectionStatus
    ): List<ConnectionUser> {
        return when (status) {
            ConnectionStatus.ALL -> users
            ConnectionStatus.FOLLOWING -> users.filter { it.isFollowing }
            ConnectionStatus.FOLLOWERS -> users.filter { it.isFollower }
            ConnectionStatus.CONNECTED -> users.filter { it.isConnected }
            ConnectionStatus.NOT_CONNECTED -> users.filter { !it.isConnected }
        }
    }

    // Helper function to get saved access token
    private fun getAccessToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("access_token", null)
    }

    // Helper function to check if user is logged in
    fun isUserLoggedIn(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        val hasAccessToken = !sharedPrefs.getString("access_token", null).isNullOrEmpty()
        println("UserConnectionsViewModel: User logged in status - isLoggedIn: $isLoggedIn, hasAccessToken: $hasAccessToken")
        return isLoggedIn && hasAccessToken
    }

    // Debug function
    fun debugConnections() {
        println("UserConnectionsViewModel: === DEBUGGING CONNECTIONS DATA ===")
        println("UserConnectionsViewModel: Growth suggestions: ${growthSuggestions.size}")
        println("UserConnectionsViewModel: Pending to accept: ${pendingToAccept.size}")
        println("UserConnectionsViewModel: Sent requests: ${sentConnections.size}")
        println("UserConnectionsViewModel: Total pending connections: $totalPendingConnections")
        println("UserConnectionsViewModel: Is loading: $isLoading")
        println("UserConnectionsViewModel: Is refreshing: $isRefreshing")
        println("UserConnectionsViewModel: Error message: $errorMessage")
        println("UserConnectionsViewModel: Current filter: $currentFilter")

        if (growthSuggestions.isNotEmpty()) {
            println("UserConnectionsViewModel: Sample growth suggestion: ${growthSuggestions.first().username}")
            println("UserConnectionsViewModel: Sample user followers: ${growthSuggestions.first().followersCount}")
        }
        println("UserConnectionsViewModel: ================================================")
    }
}

// Data class for connection statistics
data class ConnectionStats(
    val totalGrowthSuggestions: Int,
    val totalPendingConnections: Int,
    val pendingToAccept: Int,
    val sentRequests: Int,
    val usersWithProfilePics: Int,
    val usersWithInstagram: Int
)
















//package com.cc.creatorcircle.viewModel
//
//import android.content.Context
//import androidx.compose.runtime.*
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cc.creatorcircle.data.api.RetrofitInstance
//import com.cc.creatorcircle.data.models.*
//import kotlinx.coroutines.launch
//import kotlin.Result
//
//// Filter options for connections
//data class ConnectionsFilter(
//    val minFollowers: Int = 0,
//    val requireProfilePic: Boolean = false,
//    val platform: String? = null,
//    val connectionStatus: ConnectionStatus = ConnectionStatus.ALL
//)
//
//enum class ConnectionStatus {
//    ALL, FOLLOWING, FOLLOWERS, CONNECTED, NOT_CONNECTED
//}
//
//class UserConnectionsViewModel : ViewModel() {
//
//    // UI States using mutableStateOf for Compose
//    var connectionsState by mutableStateOf<Result<SocialMediaResponse>?>(null)
//        private set
//
//    var growthSuggestions by mutableStateOf<List<ConnectionUser>>(emptyList())
//        private set
//
//    var pendingToAccept by mutableStateOf<List<ConnectionUser>>(emptyList())
//        private set
//
//    var sentConnections by mutableStateOf<List<ConnectionUser>>(emptyList())
//        private set
//
//    var isLoading by mutableStateOf(false)
//        private set
//
//    var isRefreshing by mutableStateOf(false)
//        private set
//
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    // Filter state
//    var currentFilter by mutableStateOf(ConnectionsFilter())
//        private set
//
//    // Stats
//    var totalGrowthSuggestions by mutableStateOf(0)
//        private set
//
//    var totalPendingConnections by mutableStateOf(0)
//        private set
//
//    var pendingToAcceptCount by mutableStateOf(0)
//        private set
//
//    var sentRequestsCount by mutableStateOf(0)
//        private set
//
//    // Cache original data to avoid API calls when filtering
//    private var originalData: SocialMediaResponse? = null
//
//    fun loadUserConnections(context: Context, forceRefresh: Boolean = false) {
//        if (isLoading && !forceRefresh) return
//
//        viewModelScope.launch {
//            try {
//                connectionsState = null
//                if (forceRefresh) {
//                    isRefreshing = true
//                } else {
//                    isLoading = true
//                }
//                errorMessage = null
//
//                val token = getAccessToken(context)
//                if (token.isNullOrEmpty()) {
//                    errorMessage = "Authentication token not found"
//                    connectionsState = Result.failure(Exception("Authentication token not found"))
//                    return@launch
//                }
//
//                println("UserConnectionsViewModel: Making API call to get connections with token: Bearer $token")
//                val response = RetrofitInstance.api.getUserConnections("Bearer $token")
//
//                println("UserConnectionsViewModel: API response received - Success: ${response.isSuccessful}, Code: ${response.code()}")
//
//                if (response.isSuccessful && response.body() != null) {
//                    val connectionsResponse = response.body()!!
//                    println("UserConnectionsViewModel: Raw API Response Body: $connectionsResponse")
//
//                    // Cache original data
//                    originalData = connectionsResponse
//
//                    // Update all states
//                    updateConnectionsData(connectionsResponse)
//
//                    println("UserConnectionsViewModel: Successfully loaded connections")
//                    println("UserConnectionsViewModel: Growth suggestions: ${growthSuggestions.size}")
//                    println("UserConnectionsViewModel: Pending to accept: ${pendingToAccept.size}")
//                    println("UserConnectionsViewModel: Sent requests: ${sentConnections.size}")
//
//                    connectionsState = Result.success(connectionsResponse)
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    println("UserConnectionsViewModel: Loading connections failed - Code: ${response.code()}, Error: $errorBody")
//                    val errorMsg = when (response.code()) {
//                        401 -> "Authentication failed. Please login again."
//                        403 -> "Access denied."
//                        404 -> "Connections not found."
//                        429 -> "Too many requests. Please try again later."
//                        500 -> "Server error. Please try again later."
//                        else -> "Failed to load connections: ${response.code()} - $errorBody"
//                    }
//                    errorMessage = errorMsg
//                    connectionsState = Result.failure(Exception(errorMsg))
//                }
//            } catch (e: Exception) {
//                println("UserConnectionsViewModel: Loading connections error: ${e.message}")
//                e.printStackTrace()
//                errorMessage = "Error loading connections: ${e.message}"
//                connectionsState = Result.failure(e)
//            } finally {
//                isLoading = false
//                isRefreshing = false
//            }
//        }
//    }
//
//    fun refreshConnections(context: Context) {
//        println("UserConnectionsViewModel: Refreshing connections...")
//        loadUserConnections(context, forceRefresh = true)
//    }
//
//    fun applyFilter(filter: ConnectionsFilter) {
//        currentFilter = filter
//        println("UserConnectionsViewModel: Applying filter - minFollowers: ${filter.minFollowers}, requireProfilePic: ${filter.requireProfilePic}, platform: ${filter.platform}, status: ${filter.connectionStatus}")
//
//        originalData?.let { data ->
//            val filteredGrowthSuggestions = filterConnectionUsers(data.growthSuggestions, filter)
//            val filteredToAccept = filterConnectionUsers(data.pendingConnections.toAccept.users, filter)
//            val filteredSent = filterConnectionUsers(data.pendingConnections.sent.users, filter)
//
//            // Update filtered data
//            growthSuggestions = filteredGrowthSuggestions
//            pendingToAccept = filteredToAccept
//            sentConnections = filteredSent
//
//            // Update counts
//            pendingToAcceptCount = filteredToAccept.size
//            sentRequestsCount = filteredSent.size
//            totalGrowthSuggestions = filteredGrowthSuggestions.size
//            totalPendingConnections = filteredToAccept.size + filteredSent.size
//
//            println("UserConnectionsViewModel: Filter applied - Growth suggestions: ${filteredGrowthSuggestions.size}")
//        }
//    }
//
//    fun clearFilters() {
//        println("UserConnectionsViewModel: Clearing all filters...")
//        currentFilter = ConnectionsFilter()
//        originalData?.let { updateConnectionsData(it) }
//    }
//
//    fun searchUsers(query: String): List<ConnectionUser> {
//        val allUsers = growthSuggestions + pendingToAccept + sentConnections
//
//        return allUsers.filter { user ->
//            user.username?.contains(query, ignoreCase = true) == true ||
//                    user.fullName?.contains(query, ignoreCase = true) == true
//        }
//    }
//
//    fun sendConnectionRequest(context: Context, userId: Int) {
//        viewModelScope.launch {
//            try {
//                val token = getAccessToken(context)
//                if (token.isNullOrEmpty()) {
//                    errorMessage = "Authentication token not found"
//                    return@launch
//                }
//
//                println("UserConnectionsViewModel: Sending connection request to user: $userId")
//                val response = RetrofitInstance.api.sendConnectionRequest(
//                    token = "Bearer $token",
//                    userId = userId
//                )
//
//                if (response.isSuccessful) {
//                    // Update the user's connection status locally
//                    val updatedGrowthSuggestions = growthSuggestions.map { user ->
//                        if (user.userId == userId) {
//                            user.copy(isConnected = true)
//                        } else {
//                            user
//                        }
//                    }
//                    growthSuggestions = updatedGrowthSuggestions
//
//                    // Also update original data
//                    originalData = originalData?.copy(
//                        growthSuggestions = originalData!!.growthSuggestions.map { user ->
//                            if (user.userId == userId) {
//                                user.copy(isConnected = true)
//                            } else {
//                                user
//                            }
//                        }
//                    )
//
//                    println("UserConnectionsViewModel: Connection request sent successfully")
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    println("UserConnectionsViewModel: Connection request failed - Code: ${response.code()}, Error: $errorBody")
//                    errorMessage = "Failed to send connection request: ${response.code()} - $errorBody"
//                }
//            } catch (e: Exception) {
//                println("UserConnectionsViewModel: Connection request error: ${e.message}")
//                e.printStackTrace()
//                errorMessage = "Error sending connection request: ${e.message}"
//            }
//        }
//    }
//
//    fun acceptConnectionRequest(context: Context, connectionId: Int) {
//        viewModelScope.launch {
//            try {
//                val token = getAccessToken(context)
//                if (token.isNullOrEmpty()) {
//                    errorMessage = "Authentication token not found"
//                    return@launch
//                }
//
//                println("UserConnectionsViewModel: Accepting connection request: $connectionId")
//                // You'll need to implement this API endpoint
//                // val response = RetrofitInstance.api.acceptConnectionRequest("Bearer $token", connectionId)
//
//                // For now, update locally
//                val updatedPendingToAccept = pendingToAccept.filter { it.connectionId != connectionId }
//                pendingToAccept = updatedPendingToAccept
//                pendingToAcceptCount = updatedPendingToAccept.size
//
//                println("UserConnectionsViewModel: Connection request accepted successfully")
//            } catch (e: Exception) {
//                println("UserConnectionsViewModel: Accept connection error: ${e.message}")
//                e.printStackTrace()
//                errorMessage = "Error accepting connection request: ${e.message}"
//            }
//        }
//    }
//
//    fun clearError() {
//        errorMessage = null
//    }
//
//    fun getUserById(userId: Int): ConnectionUser? {
//        val allUsers = growthSuggestions + pendingToAccept + sentConnections
//        return allUsers.find { it.userId == userId }
//    }
//
//    fun getUsersByFollowersRange(minFollowers: Int, maxFollowers: Int): List<ConnectionUser> {
//        return growthSuggestions.filter { user ->
//            user.followersCount in minFollowers..maxFollowers
//        }
//    }
//
//    fun getUsersWithInstagram(): List<ConnectionUser> {
//        return growthSuggestions.filter { it.hasInstagramFollowers() }
//    }
//
//    fun getTopFollowersUsers(count: Int = 10): List<ConnectionUser> {
//        return growthSuggestions
//            .sortedByDescending { it.followersCount }
//            .take(count)
//    }
//
//    // Private helper functions
//    private fun updateConnectionsData(data: SocialMediaResponse) {
//        growthSuggestions = data.growthSuggestions
//        pendingToAccept = data.pendingConnections.toAccept.users
//        sentConnections = data.pendingConnections.sent.users
//
//        totalGrowthSuggestions = data.getTotalGrowthSuggestions()
//        totalPendingConnections = data.getTotalPendingConnections()
//        pendingToAcceptCount = data.pendingConnections.toAccept.count
//        sentRequestsCount = data.pendingConnections.sent.count
//    }
//
//    private fun filterConnectionUsers(
//        users: List<ConnectionUser>,
//        filter: ConnectionsFilter
//    ): List<ConnectionUser> {
//        return users
//            .let { if (filter.minFollowers > 0) it.filterByFollowersCount(filter.minFollowers) else it }
//            .let { if (filter.requireProfilePic) it.filterWithProfilePic() else it }
//            .let { if (filter.platform != null) it.filterByPlatform(filter.platform) else it }
//            .let { filterByConnectionStatus(it, filter.connectionStatus) }
//    }
//
//    private fun filterByConnectionStatus(
//        users: List<ConnectionUser>,
//        status: ConnectionStatus
//    ): List<ConnectionUser> {
//        return when (status) {
//            ConnectionStatus.ALL -> users
//            ConnectionStatus.FOLLOWING -> users.filter { it.isFollowing }
//            ConnectionStatus.FOLLOWERS -> users.filter { it.isFollower }
//            ConnectionStatus.CONNECTED -> users.filter { it.isConnected }
//            ConnectionStatus.NOT_CONNECTED -> users.filter { !it.isConnected }
//        }
//    }
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
//        println("UserConnectionsViewModel: User logged in status - isLoggedIn: $isLoggedIn, hasAccessToken: $hasAccessToken")
//        return isLoggedIn && hasAccessToken
//    }
//
//    // Debug function
//    fun debugConnections() {
//        println("UserConnectionsViewModel: === DEBUGGING CONNECTIONS DATA ===")
//        println("UserConnectionsViewModel: Growth suggestions: ${growthSuggestions.size}")
//        println("UserConnectionsViewModel: Pending to accept: ${pendingToAccept.size}")
//        println("UserConnectionsViewModel: Sent requests: ${sentConnections.size}")
//        println("UserConnectionsViewModel: Total pending connections: $totalPendingConnections")
//        println("UserConnectionsViewModel: Is loading: $isLoading")
//        println("UserConnectionsViewModel: Is refreshing: $isRefreshing")
//        println("UserConnectionsViewModel: Error message: $errorMessage")
//        println("UserConnectionsViewModel: Current filter: $currentFilter")
//
//        if (growthSuggestions.isNotEmpty()) {
//            println("UserConnectionsViewModel: Sample growth suggestion: ${growthSuggestions.first().username}")
//            println("UserConnectionsViewModel: Sample user followers: ${growthSuggestions.first().followersCount}")
//        }
//        println("UserConnectionsViewModel: ================================================")
//    }
//}
//
//// Data class for connection statistics
//data class ConnectionStats(
//    val totalGrowthSuggestions: Int,
//    val totalPendingConnections: Int,
//    val pendingToAccept: Int,
//    val sentRequests: Int,
//    val usersWithProfilePics: Int,
//    val usersWithInstagram: Int
//)
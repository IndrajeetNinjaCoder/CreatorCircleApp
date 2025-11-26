package com.example.app

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.TopBarHome
import com.cc.creatorcircle.ui.screens.connections.connected.AcceptedConnectionsContent
import com.cc.creatorcircle.ui.screens.connections.pending.PendingConnectionsContent
import com.cc.creatorcircle.ui.screens.connections.recommendation.RecommendationContent
import com.cc.creatorcircle.ui.screens.connections.sent.SentRequestContent
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserConnectionsViewModel
import com.cc.creatorcircle.viewModel.UserDiscoveryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ConnectionTab {
    RECOMMENDATION, SENT_REQUEST, PENDING_REQUEST, CONNECTIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    navController: NavController,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )
    val discoveryViewModel: UserDiscoveryViewModel = viewModel()
    val userConnectionsViewModel: UserConnectionsViewModel = viewModel()
    val connectionsViewModel: ConnectionViewModel = viewModel()

    var selectedTab by remember { mutableStateOf(ConnectionTab.RECOMMENDATION) }

    // Collect states from PostsViewModel (for pending/accepted connections)
    val userProfile by postsViewModel.userProfile.collectAsState()
    val profileLoading by postsViewModel.profileLoading.collectAsState()
    val profileError by postsViewModel.profileError.collectAsState()

    // Discovery ViewModel states (for recommendations)
    val recommendedUsers = discoveryViewModel.recommendedUsers
    val isDiscoveryLoading = discoveryViewModel.isLoading
    val discoveryError = discoveryViewModel.errorMessage

    // Connections ViewModel states (for sent connections)
    val sentConnections = userConnectionsViewModel.sentConnections
    val isConnectionsLoading = userConnectionsViewModel.isLoading
    val connectionsError = userConnectionsViewModel.errorMessage

    // Log sent connections when they change
    LaunchedEffect(sentConnections) {
        Log.d("ConnectionsScreen", "Sent connections updated: ${sentConnections.size}")
        sentConnections.forEach { user ->
            Log.d("ConnectionsScreen", "User: ${user.username}, ID: ${user.userId}")
        }
    }

    // Function to refresh all data
    val refreshAllData = {
        Log.d("ConnectionsScreen", "Refreshing all data...")
        postsViewModel.fetchUserProfile()
        discoveryViewModel.refreshRecommendations(context)
        userConnectionsViewModel.refreshConnections(context)
    }

    // Function to refresh with delay
    val refreshWithDelay = {
        coroutineScope.launch {
            delay(1000)
            refreshAllData()
        }
    }

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        Log.d("ConnectionsScreen", "Initial data load")
        postsViewModel.fetchUserProfile()

        if (recommendedUsers.isEmpty() && !isDiscoveryLoading) {
            discoveryViewModel.loadRecommendations(context)
        }

        // Always load user connections on screen load
        userConnectionsViewModel.loadUserConnections(context)
    }

    Scaffold(
        topBar = {
            TopBarHome(
                tabs = listOf("Feed", "Resources", "Connections"),
                selectedTab = "Connections",
                navController = navController,
                onTabSelected = { tab ->
                    when (tab) {
                        "Feed" -> navController.navigate("home")
                        "Resources" -> navController.navigate("resourcehub")
                        "Connections" -> { /* Already here */ }
                    }
                },
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Tab Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(4.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                if (selectedTab == ConnectionTab.RECOMMENDATION) {
                    GradientButton("Recommendation") { selectedTab = ConnectionTab.RECOMMENDATION }
                } else {
                    CustomOutlinedButton("Recommendation") {
                        selectedTab = ConnectionTab.RECOMMENDATION
                    }
                }

                if (selectedTab == ConnectionTab.SENT_REQUEST) {
                    GradientButton("Sent Request") { selectedTab = ConnectionTab.SENT_REQUEST }
                } else {
                    CustomOutlinedButton("Sent Request") {
                        selectedTab = ConnectionTab.SENT_REQUEST
                    }
                }

                if (selectedTab == ConnectionTab.PENDING_REQUEST) {
                    GradientButton("Pending Request") {
                        selectedTab = ConnectionTab.PENDING_REQUEST
                    }
                } else {
                    CustomOutlinedButton("Pending Request") {
                        selectedTab = ConnectionTab.PENDING_REQUEST
                    }
                }

                if (selectedTab == ConnectionTab.CONNECTIONS) {
                    GradientButton("Connections") { selectedTab = ConnectionTab.CONNECTIONS }
                } else {
                    CustomOutlinedButton("Connections") { selectedTab = ConnectionTab.CONNECTIONS }
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                ConnectionTab.RECOMMENDATION -> {
                    Log.d("ConnectionsScreen", "Showing recommendations: ${recommendedUsers.size}")
                    RecommendationContent(
                        navController = navController,
                        recommendedUsers = recommendedUsers,
                        isLoading = isDiscoveryLoading,
                        error = discoveryError,
                        onConnectClick = { userId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.sendConnectionRequest(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error sending connection request", e)
                                }
                            }
                        },
                        onRefresh = {
                            discoveryViewModel.refreshRecommendations(context)
                        }
                    )
                }

                ConnectionTab.SENT_REQUEST -> {
                    Log.d("ConnectionsScreen", "Showing sent requests: ${sentConnections.size}")
                    SentRequestContent(
                        navController = navController,
                        sentConnections = sentConnections,
                        isLoading = isConnectionsLoading,
                        error = connectionsError,
                        onRefresh = {
                            Log.d("ConnectionsScreen", "Refreshing sent connections...")
                            userConnectionsViewModel.refreshConnections(context)
                        },
                        onCancelConnectionClick = { userId ->
                            coroutineScope.launch {
                                try {
                                    Log.d("ConnectionsScreen", "Canceling connection: $userId")
                                    connectionsViewModel.cancelConnection(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error canceling connection", e)
                                }
                            }
                        }
                    )
                }

                ConnectionTab.PENDING_REQUEST -> {
                    PendingConnectionsContent(
                        navController = navController,
                        pendingConnections = userProfile?.pending_connections?.users ?: emptyList(),
                        isLoading = profileLoading,
                        error = profileError,
                        onAcceptClick = { connectionId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.RespondConnectionRequest(connectionId, "accept")
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error accepting connection", e)
                                }
                            }
                        },
                        onRejectClick = { connectionId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.RespondConnectionRequest(connectionId, "reject")
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error rejecting connection", e)
                                }
                            }
                        }
                    )
                }

                ConnectionTab.CONNECTIONS -> {
                    AcceptedConnectionsContent(
                        navController = navController,
                        acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList(),
                        isLoading = profileLoading,
                        error = profileError,
                        onRemoveConnectionClick = { userId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.removeConnection(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error removing connection", e)
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}












//package com.example.app
//
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.horizontalScroll
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.CustomOutlinedButton
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.ui.components.TopBarHome
//import com.cc.creatorcircle.ui.screens.connections.connected.AcceptedConnectionsContent
//import com.cc.creatorcircle.ui.screens.connections.pending.PendingConnectionsContent
//import com.cc.creatorcircle.ui.screens.connections.recommendation.RecommendationContent
//import com.cc.creatorcircle.ui.screens.connections.sent.SentRequestContent
//import com.cc.creatorcircle.viewModel.ConnectionViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//import com.cc.creatorcircle.viewModel.UserConnectionsViewModel
//import com.cc.creatorcircle.viewModel.UserDiscoveryViewModel
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//enum class ConnectionTab {
//    RECOMMENDATION, SENT_REQUEST, PENDING_REQUEST, CONNECTIONS
//}
//
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ConnectionsScreen(
//    navController: NavController,
//    onBackClick: () -> Unit = {}
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope() // Add this
//
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//    val discoveryViewModel: UserDiscoveryViewModel = viewModel()
//    val userConnectionsViewModel: UserConnectionsViewModel = viewModel()
//    val connectionsViewModel: ConnectionViewModel = viewModel()
//    var selectedTab by remember { mutableStateOf(ConnectionTab.RECOMMENDATION) }
//
//    // Collect states from PostsViewModel (for pending/accepted connections)
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val profileLoading by postsViewModel.profileLoading.collectAsState()
//    val profileError by postsViewModel.profileError.collectAsState()
//
//    // Discovery ViewModel states (for recommendations)
//    val recommendedUsers = discoveryViewModel.recommendedUsers
//    val isDiscoveryLoading = discoveryViewModel.isLoading
//    val discoveryError = discoveryViewModel.errorMessage
//
//    // Connections ViewModel states (for sent connections)
//    val sentConnections = userConnectionsViewModel.sentConnections
//    val isConnectionsLoading = userConnectionsViewModel.isLoading
//    val connectionsError = userConnectionsViewModel.errorMessage
//
//    // Function to refresh all data
//    val refreshAllData = {
//        postsViewModel.fetchUserProfile()
//        discoveryViewModel.refreshRecommendations(context)
//        userConnectionsViewModel.refreshConnections(context)
//    }
//
//    // Function to refresh with delay (fallback solution)
//    val refreshWithDelay = {
//        coroutineScope.launch {
//            delay(1000) // Wait 1 second for backend to process
//            refreshAllData()
//        }
//    }
//
//    // Fetch data on first composition
//    LaunchedEffect(Unit) {
//        postsViewModel.fetchUserProfile()
//        if (recommendedUsers.isEmpty() && !isDiscoveryLoading) {
//            discoveryViewModel.loadRecommendations(context)
//        }
//        userConnectionsViewModel.loadUserConnections(context)
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarHome(
//                tabs = listOf("Feed", "Resources", "Connections"),
//                selectedTab = "Connections", // This should be "Resources" since you're in ResourceHub
//                navController = navController,
//                onTabSelected = { tab ->
//                    // Make sure this doesn't try to navigate to "feed"
//                    when (tab) {
//                        "Feed" -> navController.navigate("home") // NOT "feed"
//                        "Resources" -> navController.navigate("resourcehub")
//                        "Connections" -> { /* Already here */
//                        }
//
//                    }
//                },
//            )
//        },
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(Color.White)
//        ) {
//
//            // Tab Row
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                modifier = Modifier
//                    .padding(4.dp)
//                    .horizontalScroll(rememberScrollState())
//            ) {
//                if (selectedTab == ConnectionTab.RECOMMENDATION) {
//                    GradientButton("Recommendation") { selectedTab = ConnectionTab.RECOMMENDATION }
//                } else {
//                    CustomOutlinedButton("Recommendation") {
//                        selectedTab = ConnectionTab.RECOMMENDATION
//                    }
//                }
//
//                if (selectedTab == ConnectionTab.SENT_REQUEST) {
//                    GradientButton("Sent Request") { selectedTab = ConnectionTab.SENT_REQUEST }
//                } else {
//                    CustomOutlinedButton("Sent Request") {
//                        selectedTab = ConnectionTab.SENT_REQUEST
//                    }
//                }
//
//                if (selectedTab == ConnectionTab.PENDING_REQUEST) {
//                    GradientButton("Pending Request") {
//                        selectedTab = ConnectionTab.PENDING_REQUEST
//                    }
//                } else {
//                    CustomOutlinedButton("Pending Request") {
//                        selectedTab = ConnectionTab.PENDING_REQUEST
//                    }
//                }
//
//                if (selectedTab == ConnectionTab.CONNECTIONS) {
//                    GradientButton("Connections") { selectedTab = ConnectionTab.CONNECTIONS }
//                } else {
//                    CustomOutlinedButton("Connections") { selectedTab = ConnectionTab.CONNECTIONS }
//                }
//            }
//
//            // Content based on selected tab
//            when (selectedTab) {
//                ConnectionTab.RECOMMENDATION -> {
//                    Log.d("Recommended_Users", "ConnectionsScreen: " + recommendedUsers)
//                    RecommendationContent(
//                        navController = navController,
//                        recommendedUsers = recommendedUsers,
//                        isLoading = isDiscoveryLoading,
//                        error = discoveryError,
//                        onConnectClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.sendConnectionRequest(userId)
//                                    // Wait a bit then refresh
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    // Handle error
//                                }
//                            }
//                        },
//                        onRefresh = {
//                            discoveryViewModel.refreshRecommendations(context)
//                        }
//                    )
//                }
//
//                ConnectionTab.SENT_REQUEST -> {
//                    SentRequestContent(
//                        navController = navController,
//                        sentConnections = sentConnections,
//                        isLoading = isConnectionsLoading,
//                        error = connectionsError,
//                        onRefresh = {
//                            userConnectionsViewModel.refreshConnections(context)
//                        },
//                        onCancelConnectionClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.cancelConnection(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    // Handle error
//                                }
//                            }
//                        }
//                    )
//                }
//
//                ConnectionTab.PENDING_REQUEST -> {
//                    PendingConnectionsContent(
//                        navController = navController,
//                        pendingConnections = userProfile?.pending_connections?.users ?: emptyList(),
//                        isLoading = profileLoading,
//                        error = profileError,
//                        onAcceptClick = { connectionId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.RespondConnectionRequest(connectionId, "accept")
//                                    delay(500) // Wait for backend to process
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    // Handle error
//                                }
//                            }
//                        },
//                        onRejectClick = { connectionId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.RespondConnectionRequest(connectionId, "reject")
//                                    delay(500) // Wait for backend to process
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    // Handle error
//                                }
//                            }
//                        }
//                    )
//                }
//
//                ConnectionTab.CONNECTIONS -> {
//                    AcceptedConnectionsContent(
//                        navController = navController,
//                        acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList(),
//                        isLoading = profileLoading,
//                        error = profileError,
//                        onRemoveConnectionClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.removeConnection(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    // Handle error
//                                }
//                            }
//                        },
//                    )
//                }
//            }
//        }
//    }
//}

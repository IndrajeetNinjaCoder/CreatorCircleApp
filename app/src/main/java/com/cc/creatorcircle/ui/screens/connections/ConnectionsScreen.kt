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
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
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

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("ConnectionsScreen", "ConnectionsScreen")
    }

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
        FirebaseAnalyticsHelper.logScreenView("ConnectionsScreen", "connections_tab")
        postsViewModel.fetchUserProfile()

        if (recommendedUsers.isEmpty() && !isDiscoveryLoading) {
            discoveryViewModel.loadRecommendations(context)
            FirebaseAnalyticsHelper.logFeatureUsed("load_recommendations")
        }

        // Always load user connections on screen load
        userConnectionsViewModel.loadUserConnections(context)
        FirebaseAnalyticsHelper.logFeatureUsed("load_sent_connections")
    }

    Scaffold(
        topBar = {
            TopBarHome(
                tabs = listOf("Feed", "Resources", "Connections"),
                selectedTab = "Connections",
                navController = navController,
                onTabSelected = { tab ->
                    FirebaseAnalyticsHelper.logTabSelected(tab)
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
                    GradientButton("Recommendation") {
                        FirebaseAnalyticsHelper.logTabSelected("Recommendation")
                        selectedTab = ConnectionTab.RECOMMENDATION
                    }
                } else {
                    CustomOutlinedButton("Recommendation") {
                        FirebaseAnalyticsHelper.logTabSelected("Recommendation")
                        selectedTab = ConnectionTab.RECOMMENDATION
                    }
                }

                if (selectedTab == ConnectionTab.SENT_REQUEST) {
                    GradientButton("Sent Request") {
                        FirebaseAnalyticsHelper.logTabSelected("Sent Request")
                        selectedTab = ConnectionTab.SENT_REQUEST
                    }
                } else {
                    CustomOutlinedButton("Sent Request") {
                        FirebaseAnalyticsHelper.logTabSelected("Sent Request")
                        selectedTab = ConnectionTab.SENT_REQUEST
                    }
                }

                if (selectedTab == ConnectionTab.PENDING_REQUEST) {
                    GradientButton("Pending Request") {
                        FirebaseAnalyticsHelper.logTabSelected("Pending Request")
                        selectedTab = ConnectionTab.PENDING_REQUEST
                    }
                } else {
                    CustomOutlinedButton("Pending Request") {
                        FirebaseAnalyticsHelper.logTabSelected("Pending Request")
                        selectedTab = ConnectionTab.PENDING_REQUEST
                    }
                }

                if (selectedTab == ConnectionTab.CONNECTIONS) {
                    GradientButton("Connections") {
                        FirebaseAnalyticsHelper.logTabSelected("Connections")
                        selectedTab = ConnectionTab.CONNECTIONS
                    }
                } else {
                    CustomOutlinedButton("Connections") {
                        FirebaseAnalyticsHelper.logTabSelected("Connections")
                        selectedTab = ConnectionTab.CONNECTIONS
                    }
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                ConnectionTab.RECOMMENDATION -> {
                    Log.d("ConnectionsScreen", "Showing recommendations: ${recommendedUsers.size}")
                    FirebaseAnalyticsHelper.logScreenView("RecommendationTab", "recommendation_content")
                    RecommendationContent(
                        navController = navController,
                        recommendedUsers = recommendedUsers,
                        isLoading = isDiscoveryLoading,
                        error = discoveryError,
                        onConnectClick = { userId ->
                            coroutineScope.launch {
                                try {
                                    FirebaseAnalyticsHelper.logConnectionRequestSent(
                                        targetUserId = userId,
                                        source = "recommendation_tab"
                                    )
                                    connectionsViewModel.sendConnectionRequest(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error sending connection request", e)
                                    FirebaseAnalyticsHelper.logError(
                                        errorType = "connection_request_error",
                                        errorMessage = e.message ?: "Unknown error",
                                        context = "ConnectionsScreen_recommendation"
                                    )
                                }
                            }
                        },
                        onRefresh = {
                            FirebaseAnalyticsHelper.logFeatureUsed("refresh_recommendations")
                            discoveryViewModel.refreshRecommendations(context)
                        }
                    )
                }

                ConnectionTab.SENT_REQUEST -> {
                    Log.d("ConnectionsScreen", "Showing sent requests: ${sentConnections.size}")
                    FirebaseAnalyticsHelper.logScreenView("SentRequestTab", "sent_request_content")
                    SentRequestContent(
                        navController = navController,
                        sentConnections = sentConnections,
                        isLoading = isConnectionsLoading,
                        error = connectionsError,
                        onRefresh = {
                            Log.d("ConnectionsScreen", "Refreshing sent connections...")
                            FirebaseAnalyticsHelper.logFeatureUsed("refresh_sent_connections")
                            userConnectionsViewModel.refreshConnections(context)
                        },
                        onCancelConnectionClick = { userId: Int ->
                            coroutineScope.launch {
                                try {
                                    Log.d("ConnectionsScreen", "Canceling connection: $userId")
                                    FirebaseAnalyticsHelper.logConnectionRequestCancelled(
                                        targetUserId = userId,
                                        source = "sent_requests_tab"
                                    )
                                    connectionsViewModel.cancelConnection(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error canceling connection", e)
                                    FirebaseAnalyticsHelper.logError(
                                        errorType = "cancel_connection_error",
                                        errorMessage = e.message ?: "Unknown error",
                                        context = "ConnectionsScreen_sent_requests"
                                    )
                                }
                            }
                        }
                    )
                }

                ConnectionTab.PENDING_REQUEST -> {
                    FirebaseAnalyticsHelper.logScreenView("PendingRequestTab", "pending_request_content")
                    PendingConnectionsContent(
                        navController = navController,
                        pendingConnections = userProfile?.pending_connections?.users ?: emptyList(),
                        isLoading = profileLoading,
                        error = profileError,
                        onAcceptClick = { connectionId: Int ->
                            coroutineScope.launch {
                                try {
                                    FirebaseAnalyticsHelper.logConnectionRequestAccepted(
                                        connectionId = connectionId.toString(),
                                        source = "pending_requests_tab"
                                    )
                                    connectionsViewModel.RespondConnectionRequest(connectionId, "accept")
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error accepting connection", e)
                                    FirebaseAnalyticsHelper.logError(
                                        errorType = "accept_connection_error",
                                        errorMessage = e.message ?: "Unknown error",
                                        context = "ConnectionsScreen_pending"
                                    )
                                }
                            }
                        },
                        onRejectClick = { connectionId: Int ->
                            coroutineScope.launch {
                                try {
                                    FirebaseAnalyticsHelper.logConnectionRequestRejected(
                                        connectionId = connectionId.toString(),
                                        source = "pending_requests_tab"
                                    )
                                    connectionsViewModel.RespondConnectionRequest(connectionId, "reject")
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error rejecting connection", e)
                                    FirebaseAnalyticsHelper.logError(
                                        errorType = "reject_connection_error",
                                        errorMessage = e.message ?: "Unknown error",
                                        context = "ConnectionsScreen_pending"
                                    )
                                }
                            }
                        }
                    )
                }

                ConnectionTab.CONNECTIONS -> {
                    FirebaseAnalyticsHelper.logScreenView("ConnectionsTab", "accepted_connections_content")
                    AcceptedConnectionsContent(
                        navController = navController,
                        acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList(),
                        isLoading = profileLoading,
                        error = profileError,
                        onRemoveConnectionClick = { userId: Int ->
                            coroutineScope.launch {
                                try {
                                    FirebaseAnalyticsHelper.logConnectionRemoved(
                                        targetUserId = userId,
                                        source = "accepted_connections_tab"
                                    )
                                    connectionsViewModel.removeConnection(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    Log.e("ConnectionsScreen", "Error removing connection", e)
                                    FirebaseAnalyticsHelper.logError(
                                        errorType = "remove_connection_error",
                                        errorMessage = e.message ?: "Unknown error",
                                        context = "ConnectionsScreen_accepted"
                                    )
                                }
                            }
                        }
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
//import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
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
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ConnectionsScreen(
//    navController: NavController,
//    onBackClick: () -> Unit = {}
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//    val discoveryViewModel: UserDiscoveryViewModel = viewModel()
//    val userConnectionsViewModel: UserConnectionsViewModel = viewModel()
//    val connectionsViewModel: ConnectionViewModel = viewModel()
//
//    var selectedTab by remember { mutableStateOf(ConnectionTab.RECOMMENDATION) }
//
//    // Track screen view
//    LaunchedEffect(Unit) {
//        FirebaseAnalyticsHelper.logScreenView("ConnectionsScreen", "ConnectionsScreen")
//    }
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
//    // Log sent connections when they change
//    LaunchedEffect(sentConnections) {
//        Log.d("ConnectionsScreen", "Sent connections updated: ${sentConnections.size}")
//        sentConnections.forEach { user ->
//            Log.d("ConnectionsScreen", "User: ${user.username}, ID: ${user.userId}")
//        }
//    }
//
//    // Function to refresh all data
//    val refreshAllData = {
//        Log.d("ConnectionsScreen", "Refreshing all data...")
//        postsViewModel.fetchUserProfile()
//        discoveryViewModel.refreshRecommendations(context)
//        userConnectionsViewModel.refreshConnections(context)
//    }
//
//    // Function to refresh with delay
//    val refreshWithDelay = {
//        coroutineScope.launch {
//            delay(1000)
//            refreshAllData()
//        }
//    }
//
//    // Fetch data on first composition
//    LaunchedEffect(Unit) {
//        Log.d("ConnectionsScreen", "Initial data load")
//        FirebaseAnalyticsHelper.logScreenView("ConnectionsScreen", "connections_tab")
//        postsViewModel.fetchUserProfile()
//
//        if (recommendedUsers.isEmpty() && !isDiscoveryLoading) {
//            discoveryViewModel.loadRecommendations(context)
//            FirebaseAnalyticsHelper.logFeatureUsed("load_recommendations")
//        }
//
//        // Always load user connections on screen load
//        userConnectionsViewModel.loadUserConnections(context)
//        FirebaseAnalyticsHelper.logFeatureUsed("load_sent_connections")
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarHome(
//                tabs = listOf("Feed", "Resources", "Connections"),
//                selectedTab = "Connections",
//                navController = navController,
//                onTabSelected = { tab ->
//                    FirebaseAnalyticsHelper.logTabSelected(tab)
//                    when (tab) {
//                        "Feed" -> navController.navigate("home")
//                        "Resources" -> navController.navigate("resourcehub")
//                        "Connections" -> { /* Already here */ }
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
//            // Tab Row
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                modifier = Modifier
//                    .padding(4.dp)
//                    .horizontalScroll(rememberScrollState())
//            ) {
//                if (selectedTab == ConnectionTab.RECOMMENDATION) {
//                    GradientButton("Recommendation") {
//                        FirebaseAnalyticsHelper.logTabSelected("Recommendation")
//                        selectedTab = ConnectionTab.RECOMMENDATION
//                    }
//                } else {
//                    CustomOutlinedButton("Recommendation") {
//                        FirebaseAnalyticsHelper.logTabSelected("Recommendation")
//                        selectedTab = ConnectionTab.RECOMMENDATION
//                    }
//                }
//
//                if (selectedTab == ConnectionTab.SENT_REQUEST) {
//                    GradientButton("Sent Request") {
//                        FirebaseAnalyticsHelper.logTabSelected("Sent Request")
//                        selectedTab = ConnectionTab.SENT_REQUEST
//                    }
//                } else {
//                    CustomOutlinedButton("Sent Request") {
//                        FirebaseAnalyticsHelper.logTabSelected("Sent Request")
//                        selectedTab = ConnectionTab.SENT_REQUEST
//                    }
//                }
//
//                if (selectedTab == ConnectionTab.PENDING_REQUEST) {
//                    GradientButton("Pending Request") {
//                        FirebaseAnalyticsHelper.logTabSelected("Pending Request")
//                        selectedTab = ConnectionTab.PENDING_REQUEST
//                    }
//                } else {
//                    CustomOutlinedButton("Pending Request") {
//                        FirebaseAnalyticsHelper.logTabSelected("Pending Request")
//                        selectedTab = ConnectionTab.PENDING_REQUEST
//                    }
//                }
//
//                if (selectedTab == ConnectionTab.CONNECTIONS) {
//                    GradientButton("Connections") {
//                        FirebaseAnalyticsHelper.logTabSelected("Connections")
//                        selectedTab = ConnectionTab.CONNECTIONS
//                    }
//                } else {
//                    CustomOutlinedButton("Connections") {
//                        FirebaseAnalyticsHelper.logTabSelected("Connections")
//                        selectedTab = ConnectionTab.CONNECTIONS
//                    }
//                }
//            }
//
//            // Content based on selected tab
//            when (selectedTab) {
//                ConnectionTab.RECOMMENDATION -> {
//                    Log.d("ConnectionsScreen", "Showing recommendations: ${recommendedUsers.size}")
//                    FirebaseAnalyticsHelper.logScreenView("RecommendationTab", "recommendation_content")
//                    RecommendationContent(
//                        navController = navController,
//                        recommendedUsers = recommendedUsers,
//                        isLoading = isDiscoveryLoading,
//                        error = discoveryError,
//                        onConnectClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    FirebaseAnalyticsHelper.logConnectionRequestSent(
//                                        targetUserId = userId,
//                                        source = "recommendation_tab"
//                                    )
//                                    connectionsViewModel.sendConnectionRequest(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error sending connection request", e)
//                                    FirebaseAnalyticsHelper.logError(
//                                        errorType = "connection_request_error",
//                                        errorMessage = e.message ?: "Unknown error",
//                                        context = "ConnectionsScreen_recommendation"
//                                    )
//                                }
//                            }
//                        },
//                        onRefresh = {
//                            FirebaseAnalyticsHelper.logFeatureUsed("refresh_recommendations")
//                            discoveryViewModel.refreshRecommendations(context)
//                        },
//                        onUserProfileClick = { userId ->
//                            FirebaseAnalyticsHelper.logProfileClicked(
//                                userId = userId,
//                                source = "recommendation_card"
//                            )
//                        }
//                    )
//                }
//
//                ConnectionTab.SENT_REQUEST -> {
//                    Log.d("ConnectionsScreen", "Showing sent requests: ${sentConnections.size}")
//                    FirebaseAnalyticsHelper.logScreenView("SentRequestTab", "sent_request_content")
//                    SentRequestContent(
//                        navController = navController,
//                        sentConnections = sentConnections,
//                        isLoading = isConnectionsLoading,
//                        error = connectionsError,
//                        onRefresh = {
//                            Log.d("ConnectionsScreen", "Refreshing sent connections...")
//                            FirebaseAnalyticsHelper.logFeatureUsed("refresh_sent_connections")
//                            userConnectionsViewModel.refreshConnections(context)
//                        },
//                        onCancelConnectionClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    Log.d("ConnectionsScreen", "Canceling connection: $userId")
//                                    FirebaseAnalyticsHelper.logConnectionRequestCancelled(
//                                        targetUserId = userId,
//                                        source = "sent_requests_tab"
//                                    )
//                                    connectionsViewModel.cancelConnection(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error canceling connection", e)
//                                    FirebaseAnalyticsHelper.logError(
//                                        errorType = "cancel_connection_error",
//                                        errorMessage = e.message ?: "Unknown error",
//                                        context = "ConnectionsScreen_sent_requests"
//                                    )
//                                }
//                            }
//                        },
//                        onUserProfileClick = { userId ->
//                            FirebaseAnalyticsHelper.logProfileClicked(
//                                userId = userId,
//                                source = "sent_request_card"
//                            )
//                        }
//                    )
//                }
//
//                ConnectionTab.PENDING_REQUEST -> {
//                    FirebaseAnalyticsHelper.logScreenView("PendingRequestTab", "pending_request_content")
//                    PendingConnectionsContent(
//                        navController = navController,
//                        pendingConnections = userProfile?.pending_connections?.users ?: emptyList(),
//                        isLoading = profileLoading,
//                        error = profileError,
//                        onAcceptClick = { connectionId ->
//                            coroutineScope.launch {
//                                try {
//                                    FirebaseAnalyticsHelper.logConnectionRequestAccepted(
//                                        connectionId = connectionId,
//                                        source = "pending_requests_tab"
//                                    )
//                                    connectionsViewModel.RespondConnectionRequest(connectionId, "accept")
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error accepting connection", e)
//                                    FirebaseAnalyticsHelper.logError(
//                                        errorType = "accept_connection_error",
//                                        errorMessage = e.message ?: "Unknown error",
//                                        context = "ConnectionsScreen_pending"
//                                    )
//                                }
//                            }
//                        },
//                        onRejectClick = { connectionId ->
//                            coroutineScope.launch {
//                                try {
//                                    FirebaseAnalyticsHelper.logConnectionRequestRejected(
//                                        connectionId = connectionId,
//                                        source = "pending_requests_tab"
//                                    )
//                                    connectionsViewModel.RespondConnectionRequest(connectionId, "reject")
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error rejecting connection", e)
//                                    FirebaseAnalyticsHelper.logError(
//                                        errorType = "reject_connection_error",
//                                        errorMessage = e.message ?: "Unknown error",
//                                        context = "ConnectionsScreen_pending"
//                                    )
//                                }
//                            }
//                        },
//                        onUserProfileClick = { userId ->
//                            FirebaseAnalyticsHelper.logProfileClicked(
//                                userId = userId,
//                                source = "pending_request_card"
//                            )
//                        }
//                    )
//                }
//
//                ConnectionTab.CONNECTIONS -> {
//                    FirebaseAnalyticsHelper.logScreenView("ConnectionsTab", "accepted_connections_content")
//                    AcceptedConnectionsContent(
//                        navController = navController,
//                        acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList(),
//                        isLoading = profileLoading,
//                        error = profileError,
//                        onRemoveConnectionClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    FirebaseAnalyticsHelper.logConnectionRemoved(
//                                        targetUserId = userId,
//                                        source = "accepted_connections_tab"
//                                    )
//                                    connectionsViewModel.removeConnection(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error removing connection", e)
//                                    FirebaseAnalyticsHelper.logError(
//                                        errorType = "remove_connection_error",
//                                        errorMessage = e.message ?: "Unknown error",
//                                        context = "ConnectionsScreen_accepted"
//                                    )
//                                }
//                            }
//                        },
//                        onUserProfileClick = { userId ->
//                            FirebaseAnalyticsHelper.logProfileClicked(
//                                userId = userId,
//                                source = "accepted_connection_card"
//                            )
//                        }
//                    )
//                }
//            }
//        }
//    }
//}














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
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ConnectionsScreen(
//    navController: NavController,
//    onBackClick: () -> Unit = {}
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//    val discoveryViewModel: UserDiscoveryViewModel = viewModel()
//    val userConnectionsViewModel: UserConnectionsViewModel = viewModel()
//    val connectionsViewModel: ConnectionViewModel = viewModel()
//
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
//    // Log sent connections when they change
//    LaunchedEffect(sentConnections) {
//        Log.d("ConnectionsScreen", "Sent connections updated: ${sentConnections.size}")
//        sentConnections.forEach { user ->
//            Log.d("ConnectionsScreen", "User: ${user.username}, ID: ${user.userId}")
//        }
//    }
//
//    // Function to refresh all data
//    val refreshAllData = {
//        Log.d("ConnectionsScreen", "Refreshing all data...")
//        postsViewModel.fetchUserProfile()
//        discoveryViewModel.refreshRecommendations(context)
//        userConnectionsViewModel.refreshConnections(context)
//    }
//
//    // Function to refresh with delay
//    val refreshWithDelay = {
//        coroutineScope.launch {
//            delay(1000)
//            refreshAllData()
//        }
//    }
//
//    // Fetch data on first composition
//    LaunchedEffect(Unit) {
//        Log.d("ConnectionsScreen", "Initial data load")
//        postsViewModel.fetchUserProfile()
//
//        if (recommendedUsers.isEmpty() && !isDiscoveryLoading) {
//            discoveryViewModel.loadRecommendations(context)
//        }
//
//        // Always load user connections on screen load
//        userConnectionsViewModel.loadUserConnections(context)
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarHome(
//                tabs = listOf("Feed", "Resources", "Connections"),
//                selectedTab = "Connections",
//                navController = navController,
//                onTabSelected = { tab ->
//                    when (tab) {
//                        "Feed" -> navController.navigate("home")
//                        "Resources" -> navController.navigate("resourcehub")
//                        "Connections" -> { /* Already here */ }
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
//                    Log.d("ConnectionsScreen", "Showing recommendations: ${recommendedUsers.size}")
//                    RecommendationContent(
//                        navController = navController,
//                        recommendedUsers = recommendedUsers,
//                        isLoading = isDiscoveryLoading,
//                        error = discoveryError,
//                        onConnectClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.sendConnectionRequest(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error sending connection request", e)
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
//                    Log.d("ConnectionsScreen", "Showing sent requests: ${sentConnections.size}")
//                    SentRequestContent(
//                        navController = navController,
//                        sentConnections = sentConnections,
//                        isLoading = isConnectionsLoading,
//                        error = connectionsError,
//                        onRefresh = {
//                            Log.d("ConnectionsScreen", "Refreshing sent connections...")
//                            userConnectionsViewModel.refreshConnections(context)
//                        },
//                        onCancelConnectionClick = { userId ->
//                            coroutineScope.launch {
//                                try {
//                                    Log.d("ConnectionsScreen", "Canceling connection: $userId")
//                                    connectionsViewModel.cancelConnection(userId)
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error canceling connection", e)
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
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error accepting connection", e)
//                                }
//                            }
//                        },
//                        onRejectClick = { connectionId ->
//                            coroutineScope.launch {
//                                try {
//                                    connectionsViewModel.RespondConnectionRequest(connectionId, "reject")
//                                    delay(500)
//                                    refreshAllData()
//                                } catch (e: Exception) {
//                                    Log.e("ConnectionsScreen", "Error rejecting connection", e)
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
//                                    Log.e("ConnectionsScreen", "Error removing connection", e)
//                                }
//                            }
//                        },
//                    )
//                }
//            }
//        }
//    }
//}
//

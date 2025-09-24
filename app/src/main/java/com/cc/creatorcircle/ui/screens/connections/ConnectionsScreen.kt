package com.example.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.UserInfo
import com.cc.creatorcircle.data.models.DiscoveryUser
import com.cc.creatorcircle.data.models.UserWithScore
import com.cc.creatorcircle.data.models.ConnectionUser
import com.cc.creatorcircle.data.models.PlatformFollower
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.ui.screens.connections.connected.AcceptedConnectionsContent
import com.cc.creatorcircle.ui.screens.connections.pending.PendingConnectionsContent
import com.cc.creatorcircle.ui.screens.connections.recommendation.RecommendationContent
import com.cc.creatorcircle.ui.screens.connections.sent.SentRequestContent
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserDiscoveryViewModel
import com.cc.creatorcircle.viewModel.UserConnectionsViewModel
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
    val coroutineScope = rememberCoroutineScope() // Add this

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

    // Function to refresh all data
    val refreshAllData = {
        postsViewModel.fetchUserProfile()
        discoveryViewModel.refreshRecommendations(context)
        userConnectionsViewModel.refreshConnections(context)
    }

    // Function to refresh with delay (fallback solution)
    val refreshWithDelay = {
        coroutineScope.launch {
            delay(1000) // Wait 1 second for backend to process
            refreshAllData()
        }
    }

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        postsViewModel.fetchUserProfile()
        if (recommendedUsers.isEmpty() && !isDiscoveryLoading) {
            discoveryViewModel.loadRecommendations(context)
        }
        userConnectionsViewModel.loadUserConnections(context)
    }

    Scaffold(
        topBar = { TopBar(title = "Connections", navController) },
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
                    RecommendationContent(
                        navController = navController,
                        recommendedUsers = recommendedUsers,
                        isLoading = isDiscoveryLoading,
                        error = discoveryError,
                        onConnectClick = { userId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.sendConnectionRequest(userId)
                                    // Wait a bit then refresh
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        },
                        onRefresh = {
                            discoveryViewModel.refreshRecommendations(context)
                        }
                    )
                }

                ConnectionTab.SENT_REQUEST -> {
                    SentRequestContent(
                        navController = navController,
                        sentConnections = sentConnections,
                        isLoading = isConnectionsLoading,
                        error = connectionsError,
                        onRefresh = {
                            userConnectionsViewModel.refreshConnections(context)
                        },
                        onCancelConnectionClick = { userId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.cancelConnection(userId)
                                    delay(500)
                                    refreshAllData()
                                } catch (e: Exception) {
                                    // Handle error
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
                                    delay(500) // Wait for backend to process
                                    refreshAllData()
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            }
                        },
                        onRejectClick = { connectionId ->
                            coroutineScope.launch {
                                try {
                                    connectionsViewModel.RespondConnectionRequest(connectionId, "reject")
                                    delay(500) // Wait for backend to process
                                    refreshAllData()
                                } catch (e: Exception) {
                                    // Handle error
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
                                    // Handle error
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}






//
//
//@Composable
//fun RecommendationContent(
//    navController: NavController,
//    recommendedUsers: List<UserWithScore>,
//    isLoading: Boolean,
//    error: String?,
//    onConnectClick: (Int) -> Unit = {},
//    onRefresh: () -> Unit = {}
//) {
//    when {
//        isLoading -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//
//        error != null -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Text(
//                        text = "Error: $error",
//                        color = Color.Red,
//                        textAlign = TextAlign.Center
//                    )
//                    Button(onClick = onRefresh) {
//                        Text("Retry")
//                    }
//                }
//            }
//        }
//
//        recommendedUsers.isEmpty() -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Text(
//                        text = "No recommendations found",
//                        color = Color.Gray,
//                        textAlign = TextAlign.Center
//                    )
//                    Button(onClick = onRefresh) {
//                        Text("Refresh")
//                    }
//                }
//            }
//        }
//
//        else -> {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(2),
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(recommendedUsers) { userWithScore ->
//                    RecommendationProfileCard(
//                        navController = navController,
//                        userWithScore = userWithScore,
//                        onConnectClick = { onConnectClick(userWithScore.user.userId) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun RecommendationProfileCard(
//    navController: NavController,
//    userWithScore: UserWithScore,
//    onConnectClick: () -> Unit = {},
//    onViewProfileClick: () -> Unit = {}
//) {
//    val user = userWithScore.user
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(
//                        enabled = true,
//                        onClick = {
//                            navController.navigate("userprofile/${user.userId}")
//                        }
//                    ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image - Made smaller for grid layout
//                Box(
//                    modifier = Modifier
//                        .size(80.dp)
//                        .clip(CircleShape)
////                        .clickable(
////                            onClick = onViewProfileClick,
////                            interactionSource = remember { MutableInteractionSource() },
////                            indication = null // Remove ripple entirely, or use LocalIndication.current for default
////                        )
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFFE91E63), // Pink
//                                    Color(0xFF9C27B0), // Purple
//                                    Color(0xFF673AB7)  // Deep Purple
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(76.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF8E24AA)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (!user.profilePic.isNullOrEmpty()) {
//                            AsyncImage(
//                                model = user.profilePic,
//                                contentDescription = "Profile",
//                                modifier = Modifier
//                                    .size(76.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop,
//                                placeholder = painterResource(id = R.drawable.ic_profile),
//                                error = painterResource(id = R.drawable.ic_profile)
//                            )
//                        } else {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = "Profile",
//                                modifier = Modifier.size(40.dp),
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Name/Username
//                Text(
//                    text = user.fullName ?: user.username ?: "Unknown User",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.height(6.dp))
//
//            // Categories - Show only first 2 for space
//            if (user.categories.isNotEmpty()) {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    items(user.categories.take(2)) { category ->
//                        Surface(
//                            modifier = Modifier,
//                            shape = RoundedCornerShape(8.dp),
//                            color = Color(0xFFE3F2FD)
//                        ) {
//                            Text(
//                                text = category,
//                                fontSize = 8.sp,
//                                color = Color(0xFF1976D2),
//                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(8.dp))
//            }
//
//            // Bio/Description - Reduced lines for grid layout
//            Text(
//                text = user.bio ?: "No bio available",
//                fontSize = 12.sp,
//                color = Color.Gray,
//                textAlign = TextAlign.Center,
//                lineHeight = 16.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Button - Full width, smaller text
//            val buttonText = when {
//                user.isConnected -> "Connected"
//                user.connectionStatus == "pending" -> "Pending"
//                else -> "Connect"
//            }
//
//            val isButtonEnabled = !user.isConnected && user.connectionStatus != "pending"
//
//            if (isButtonEnabled) {
//                GradientButton(
//                    buttonText,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(36.dp)
//                ) {
//                    onConnectClick()
//                }
//            } else {
//                Button(
//                    onClick = { },
//                    enabled = false,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(36.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
//                    )
//                ) {
//                    Text(
//                        text = buttonText,
//                        fontSize = 12.sp
//                    )
//                }
//            }
//        }
//    }
//}


// Updated SentRequestContent to show actual sent connections
//@Composable
//fun SentRequestContent(
//    navController: NavController,
//    sentConnections: List<ConnectionUser>,
//    isLoading: Boolean,
//    error: String?,
//    onRefresh: () -> Unit = {},
//    onCancelConnectionClick: (userId: Int) -> Unit = {}
//) {
//    when {
//        isLoading -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//
//        error != null -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Text(
//                        text = "Error: $error",
//                        color = Color.Red,
//                        textAlign = TextAlign.Center
//                    )
//                    Button(onClick = onRefresh) {
//                        Text("Retry")
//                    }
//                }
//            }
//        }
//
//        sentConnections.isEmpty() -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Text(
//                        text = "No sent requests",
//                        color = Color.Gray,
//                        textAlign = TextAlign.Center
//                    )
//                    Button(onClick = onRefresh) {
//                        Text("Refresh")
//                    }
//                }
//            }
//        }
//
//        else -> {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(2),
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(sentConnections) { connectionUser ->
//                    SentConnectionCard(
//                        navController = navController,
//                        connectionUser = connectionUser,
//                        onViewProfileClick = { /* Handle view profile */ },
//                        onCancelConnectionClick = { onCancelConnectionClick(connectionUser.userId) }
//
//                    )
//                }
//            }
//        }
//    }
//}
//
//// New component for sent connection cards
//@Composable
//fun SentConnectionCard(
//    navController: NavController,
//    connectionUser: ConnectionUser,
//    onViewProfileClick: () -> Unit = {},
//    onCancelConnectionClick: (userId: Int) -> Unit = {}
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(
//                        enabled = true,
//                        onClick = {
//                            navController.navigate("userprofile/${connectionUser.userId}")
//                        }
//                    ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .clip(CircleShape)
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFFE91E63), // Pink
//                                    Color(0xFF9C27B0), // Purple
//                                    Color(0xFF673AB7)  // Deep Purple
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(116.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF8E24AA)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (!connectionUser.profilePic.isNullOrEmpty()) {
//                            AsyncImage(
//                                model = connectionUser.profilePic,
//                                contentDescription = "Profile",
//                                modifier = Modifier
//                                    .size(116.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop,
//                                placeholder = painterResource(id = R.drawable.ic_profile),
//                                error = painterResource(id = R.drawable.ic_profile)
//                            )
//                        } else {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = "Profile",
//                                modifier = Modifier.size(60.dp),
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Name/Username
//                Text(
//                    text = connectionUser.fullName ?: connectionUser.username ?: "Unknown User",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Social media stats
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Platform followers
//                connectionUser.platformFollowers.let { platformFollowers ->
//                    val instagramFollowers =
//                        getInstagramFollowersFromConnectionUser(platformFollowers)
//                    if (instagramFollowers != null && instagramFollowers > 0) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(2.dp)
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_instagram),
//                                contentDescription = "Instagram",
//                                modifier = Modifier.size(20.dp),
//                                contentScale = ContentScale.Crop
//                            )
//                            Text(
//                                text = formatFollowerCount(instagramFollowers),
//                                fontSize = 12.sp,
//                                color = Color.Gray,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//
//                // Total followers
////                if (connectionUser.followersCount > 0) {
////                    Text(
////                        text = "${connectionUser.followersCount} followers",
////                        fontSize = 12.sp,
////                        color = Color.Gray,
////                        fontWeight = FontWeight.Medium
////                    )
////                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Status indicator
////            Surface(
////                modifier = Modifier,
////                shape = RoundedCornerShape(12.dp),
////                color = Color(0xFFFFF3E0)
////            ) {
////                Text(
////                    text = "Request Sent",
////                    fontSize = 12.sp,
////                    color = Color(0xFFFF9800),
////                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
////                    fontWeight = FontWeight.Medium
////                )
////            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            GradientButton("Cancel", modifier = Modifier.fillMaxWidth()) {
//                onCancelConnectionClick(connectionUser.userId)
//            }
//
//            // View Profile Button
////            CustomOutlinedButton("View Profile", modifier = Modifier.fillMaxWidth()) {
////                onViewProfileClick()
////            }
//        }
//    }
//}

// Rest of the existing components remain the same...
//@Composable
//fun AcceptedConnectionsContent(
//    navController: NavController,
//    acceptedConnections: List<UserInfo>,
//    isLoading: Boolean,
//    error: String?,
//    onRemoveConnectionClick: (userId: Int) -> Unit = {}
//) {
//    when {
//        isLoading -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//
//        error != null -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "Error: $error",
//                    color = Color.Red,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//
//        acceptedConnections.isEmpty() -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No connections found",
//                    color = Color.Gray,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//
//        else -> {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(2),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(acceptedConnections) { user ->
//                    UserConnectionCard(
//                        navController = navController,
//                        userInfo = user,
//                        showConnectButton = false,
//                        buttonText = "Connected",
//                        onRemoveConnectionClick = { onRemoveConnectionClick(user.user_id) }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PendingConnectionsContent(
//    navController: NavController,
//    pendingConnections: List<UserInfo>,
//    isLoading: Boolean,
//    error: String?,
//    onAcceptClick: (connectionId: Int) -> Unit,
//    onRejectClick: (connectionId: Int) -> Unit,
//) {
//
//
//    when {
//        isLoading -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//
//        error != null -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "Error: $error",
//                    color = Color.Red,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//
//        pendingConnections.isEmpty() -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No pending connections",
//                    color = Color.Gray,
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//
//        else -> {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(2),
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                items(pendingConnections) { user ->
//                    PendingConnectionCard(
//                        navController = navController,
//                        userInfo = user,
//                        showConnectButton = true,
//                        buttonText = "Accept",
//                        onRejectClick = {
//                            onRejectClick(user?.connection_id ?: -1)
//                        },
//                        onAcceptClick = {
//                            onAcceptClick(user?.connection_id ?: -1)
//                        },
//                    )
//                }
//            }
//        }
//    }
//}

//@Composable
//fun PendingConnectionCard(
//    navController: NavController,
//    userInfo: UserInfo,
//    showConnectButton: Boolean = true,
//    buttonText: String = "Connect",
//    onRejectClick: () -> Unit = {},
//    onAcceptClick: () -> Unit = {},
//    onViewProfileClick: () -> Unit = {}
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(
//                        enabled = true,
//                        onClick = {
//                            navController.navigate("userprofile/${userInfo.user_id}")
//                        }
//                    ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .clip(CircleShape)
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFFE91E63), // Pink
//                                    Color(0xFF9C27B0), // Purple
//                                    Color(0xFF673AB7)  // Deep Purple
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(116.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF8E24AA)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (userInfo.profile_pic != null) {
//                            AsyncImage(
//                                model = userInfo.profile_pic,
//                                contentDescription = "Profile",
//                                modifier = Modifier
//                                    .size(116.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop,
//                                placeholder = painterResource(id = R.drawable.ic_profile),
//                                error = painterResource(id = R.drawable.ic_profile)
//                            )
//                        } else {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = "Profile",
//                                modifier = Modifier.size(60.dp),
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Name/Username
//                Text(
//                    text = userInfo.full_name ?: userInfo.username ?: "Unknown User",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Social media stats
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                // Show platform followers if available
//                userInfo.platform_followers?.let { platformFollowers ->
//                    val instagramFollowers = getInstagramFollowers(platformFollowers)
//                    if (instagramFollowers != null) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(2.dp)
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_instagram),
//                                contentDescription = "Instagram",
//                                modifier = Modifier.size(20.dp),
//                                contentScale = ContentScale.Crop
//                            )
//                            Text(
//                                text = formatFollowerCount(instagramFollowers),
//                                fontSize = 12.sp,
//                                color = Color.Gray,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Bio/Description
//            Text(
//                text = userInfo.bio ?: "No bio available",
//                fontSize = 14.sp,
//                color = Color.Gray,
//                textAlign = TextAlign.Center,
//                lineHeight = 18.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Accept/Reject Icons
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Reject Icon
//                IconButton(
//                    onClick = { onRejectClick() },
//                    modifier = Modifier
//                        .size(48.dp)
//                        .background(
//                            color = Color(0xFFFF5252),
//                            shape = CircleShape
//                        )
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "Reject",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//
//
//
//
//                // Accept Icon
//                IconButton(
//                    onClick = { onAcceptClick() },
//                    modifier = Modifier
//                        .size(48.dp)
//                        .background(
//                            color = Color(0xFF4CAF50),
//                            shape = CircleShape
//                        )
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Check,
//                        contentDescription = "Accept",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//            }
//        }
//    }
//}


//
//@Composable
//fun UserConnectionCard(
//    navController: NavController,
//    userInfo: UserInfo,
//    showConnectButton: Boolean = true,
//    buttonText: String = "Connect",
//    onRemoveConnectionClick: () -> Unit = {},
//    onViewProfileClick: () -> Unit = {}
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(4.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(
//                        enabled = true,
//                        onClick = {
//                            navController.navigate("userprofile/${userInfo.user_id}")
//                        }
//                    ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .clip(CircleShape)
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFFE91E63), // Pink
//                                    Color(0xFF9C27B0), // Purple
//                                    Color(0xFF673AB7)  // Deep Purple
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(116.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFF8E24AA)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (userInfo.profile_pic != null) {
//                            AsyncImage(
//                                model = userInfo.profile_pic,
//                                contentDescription = "Profile",
//                                modifier = Modifier
//                                    .size(116.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop,
//                                placeholder = painterResource(id = R.drawable.ic_profile),
//                                error = painterResource(id = R.drawable.ic_profile)
//                            )
//                        } else {
//                            Icon(
//                                imageVector = Icons.Default.Person,
//                                contentDescription = "Profile",
//                                modifier = Modifier.size(60.dp),
//                                tint = Color.White
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Name/Username
//                Text(
//                    text = userInfo.full_name ?: userInfo.username ?: "Unknown User",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Social media stats
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                // Show platform followers if available
//                userInfo.platform_followers?.let { platformFollowers ->
//                    val instagramFollowers = getInstagramFollowers(platformFollowers)
//                    if (instagramFollowers != null) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(2.dp)
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.ic_instagram),
//                                contentDescription = "Instagram",
//                                modifier = Modifier.size(20.dp),
//                                contentScale = ContentScale.Crop
//                            )
//                            Text(
//                                text = formatFollowerCount(instagramFollowers),
//                                fontSize = 12.sp,
//                                color = Color.Gray,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Bio/Description
//            Text(
//                text = userInfo.bio ?: "No bio available",
//                fontSize = 14.sp,
//                color = Color.Gray,
//                textAlign = TextAlign.Center,
//                lineHeight = 18.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // Buttons
//
//            GradientButton("Remove", modifier = Modifier.fillMaxWidth()) {
//                onRemoveConnectionClick()
//            }
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                horizontalArrangement = Arrangement.spacedBy(12.dp)
////            ) {
////
////                CustomOutlinedButton("View Profile", modifier = Modifier.weight(1f)) {
////                    onViewProfileClick()
////                }
////            }
//        }
//    }
//}


//// Helper function to extract Instagram followers from platform_followers map
//fun getInstagramFollowers(platformFollowers: Map<String, Any>): Int? {
//    return try {
//        val instagramData = platformFollowers["instagram"] as? List<*>
//        val firstEntry = instagramData?.firstOrNull() as? Map<*, *>
//        firstEntry?.get("followers") as? Int
//    } catch (e: Exception) {
//        null
//    }
//}
//
//// Helper function to extract Instagram followers from ConnectionUser's platformFollowers
//fun getInstagramFollowersFromConnectionUser(platformFollowers: Map<String, List<PlatformFollower>>): Int? {
//    return try {
//        platformFollowers["instagram"]?.firstOrNull()?.followers
//    } catch (e: Exception) {
//        null
//    }
//}
//
//// Helper function to format follower count
//fun formatFollowerCount(count: Int): String {
//    return when {
//        count >= 1_000_000 -> "${count / 1_000_000}M"
//        count >= 1_000 -> "${count / 1_000}K"
//        else -> count.toString()
//    }
//}









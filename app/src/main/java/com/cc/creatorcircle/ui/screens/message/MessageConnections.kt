package com.cc.creatorcircle.ui.screens.message

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageConnections(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onMessageClick: (Int, String) -> Unit = { _, _ -> } // userId and userName
) {
    val context = LocalContext.current
    val firebaseAnalytics = remember { Firebase.analytics }

    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Collect states from PostsViewModel
    val userProfile by postsViewModel.userProfile.collectAsState()
    val profileLoading by postsViewModel.profileLoading.collectAsState()
    val profileError by postsViewModel.profileError.collectAsState()

    // Get accepted connections with detailed logging
    val acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList()

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("MessageConnections", "MessageConnections")
        Log.d("MessageConnections", "LaunchedEffect: Fetching user profile")
        postsViewModel.fetchUserProfile()
    }

    // Log connection data whenever it changes
    LaunchedEffect(acceptedConnections) {
        Log.d("MessageConnections", "=== Connections Update ===")
        Log.d("MessageConnections", "Total connections count: ${acceptedConnections.size}")

        // Track connections loaded
        FirebaseAnalyticsHelper.logEvent(
            "message_connections_loaded",
            mapOf("connection_count" to acceptedConnections.size.toString())
        )

        acceptedConnections.forEachIndexed { index, connection ->
            Log.d("MessageConnections", "Connection $index: userId=${connection.user_id}, username=${connection.username}, name=${connection.full_name}")
        }
    }

    Scaffold(
        topBar = { TopBar(title = "Messages", navController) },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                profileLoading -> {
                    // Loading state
                    Log.d("MessageConnections", "Showing loading state")
                    FirebaseAnalyticsHelper.logEvent("message_connections_loading")

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                profileError != null -> {
                    // Error state
                    Log.e("MessageConnections", "Error state: $profileError")

                    FirebaseAnalyticsHelper.logEvent(
                        "message_connections_error",
                        mapOf("error" to (profileError ?: "Unknown error"))
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Failed to load connections",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = profileError ?: "Unknown error",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                Log.d("MessageConnections", "Retry button clicked")
                                FirebaseAnalyticsHelper.logFeatureUsed("message_connections_retry")
                                postsViewModel.fetchUserProfile()
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                acceptedConnections.isEmpty() -> {
                    // Empty state
                    Log.d("MessageConnections", "Showing empty state")
                    FirebaseAnalyticsHelper.logEvent("message_connections_empty_state")

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No connections yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start connecting with creators to chat",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    // List of connections
                    Log.d("MessageConnections", "Rendering ${acceptedConnections.size} connections")

                    FirebaseAnalyticsHelper.logEvent(
                        "message_connections_displayed",
                        mapOf("connection_count" to acceptedConnections.size.toString())
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = acceptedConnections,
                            key = { connection -> connection.user_id }
                        ) { connection ->
                            Log.d("MessageConnections", "Rendering item: userId=${connection.user_id}, username=${connection.username}")
                            MessageItemRow(
                                userId = connection.user_id,
                                name = connection.full_name ?: connection.username ?: "Unknown User",
                                username = connection.username ?: "",
                                profileImageUrl = connection.profile_pic,
                                onClick = {
                                    Log.d("MessageConnections", "Clicked on user: ${connection.user_id}")

                                    // Track message click
                                    FirebaseAnalyticsHelper.logFeatureUsed("message_connection_clicked")
                                    FirebaseAnalyticsHelper.logEvent(
                                        "message_connection_opened",
                                        mapOf(
                                            "user_id" to connection.user_id.toString(),
                                            "username" to (connection.username ?: "unknown"),
                                            "has_profile_pic" to (connection.profile_pic != null).toString()
                                        )
                                    )

                                    navController.navigate(
                                        Screen.MessageScreen.createRoute(
                                            userId = connection.user_id,
                                            userName = connection.username ?: "",
                                            profilePic = connection.profile_pic
                                        )
                                    )
                                    onMessageClick(connection.user_id, connection.username ?: "")
                                }
                            )

                            // Add divider between items
                            if (connection != acceptedConnections.last()) {
                                Divider(
                                    modifier = Modifier.padding(start = 88.dp),
                                    color = Color(0xFFE0E0E0),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItemRow(
    userId: Int,
    name: String,
    username: String,
    profileImageUrl: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (!profileImageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile picture of $name",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback to first letter if no image
                Text(
                    text = name.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name and Username
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2B2B2B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (username.isNotEmpty()) {
                Text(
                    text = "@$username",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        // Online indicator (optional - you can customize this)
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)) // Green for online
        )
    }
}











//package com.cc.creatorcircle.ui.screens.message
//
//import android.util.Log
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.TopBar
//import com.cc.creatorcircle.ui.navigation.Screen
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MessageConnections(
//    navController: NavController,
//    onBackClick: () -> Unit = {},
//    onMessageClick: (Int, String) -> Unit = { _, _ -> } // userId and userName
//) {
//    val context = LocalContext.current
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    // Collect states from PostsViewModel
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val profileLoading by postsViewModel.profileLoading.collectAsState()
//    val profileError by postsViewModel.profileError.collectAsState()
//
//    // Get accepted connections with detailed logging
//    val acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList()
//
//    // Fetch user profile on first composition
//    LaunchedEffect(Unit) {
//        Log.d("MessageConnections", "LaunchedEffect: Fetching user profile")
//        postsViewModel.fetchUserProfile()
//    }
//
//    // Log connection data whenever it changes
//    LaunchedEffect(acceptedConnections) {
//        Log.d("MessageConnections", "=== Connections Update ===")
//        Log.d("MessageConnections", "Total connections count: ${acceptedConnections.size}")
//        acceptedConnections.forEachIndexed { index, connection ->
//            Log.d("MessageConnections", "Connection $index: userId=${connection.user_id}, username=${connection.username}, name=${connection.full_name}")
//        }
//    }
//
//    Scaffold(
//        topBar = { TopBar(title = "Messages", navController) },
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            when {
//                profileLoading -> {
//                    // Loading state
//                    Log.d("MessageConnections", "Showing loading state")
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//                profileError != null -> {
//                    // Error state
//                    Log.e("MessageConnections", "Error state: $profileError")
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            Text(
//                                text = "Failed to load connections",
//                                fontSize = 16.sp,
//                                color = Color.Gray
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = profileError ?: "Unknown error",
//                                fontSize = 12.sp,
//                                color = Color.Gray
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Button(onClick = {
//                                Log.d("MessageConnections", "Retry button clicked")
//                                postsViewModel.fetchUserProfile()
//                            }) {
//                                Text("Retry")
//                            }
//                        }
//                    }
//                }
//                acceptedConnections.isEmpty() -> {
//                    // Empty state
//                    Log.d("MessageConnections", "Showing empty state")
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            Text(
//                                text = "No connections yet",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color.Gray
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = "Start connecting with creators to chat",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                }
//                else -> {
//                    // List of connections
//                    Log.d("MessageConnections", "Rendering ${acceptedConnections.size} connections")
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(vertical = 8.dp)
//                    ) {
//                        items(
//                            items = acceptedConnections,
//                            key = { connection -> connection.user_id }
//                        ) { connection ->
//                            Log.d("MessageConnections", "Rendering item: userId=${connection.user_id}, username=${connection.username}")
//                            MessageItemRow(
//                                userId = connection.user_id,
//                                name = connection.full_name ?: connection.username ?: "Unknown User",
//                                username = connection.username ?: "",
//                                profileImageUrl = connection.profile_pic,
//                                onClick = {
//                                    Log.d("MessageConnections", "Clicked on user: ${connection.user_id}")
//                                    navController.navigate(
//                                        Screen.MessageScreen.createRoute(
//                                            userId = connection.user_id,
//                                            userName = connection.username ?: "",
//                                            profilePic = connection.profile_pic
//                                        )
//                                    )
//                                    onMessageClick(connection.user_id, connection.username ?: "")
//                                }
//                            )
//
//                            // Add divider between items
//                            if (connection != acceptedConnections.last()) {
//                                Divider(
//                                    modifier = Modifier.padding(start = 88.dp),
//                                    color = Color(0xFFE0E0E0),
//                                    thickness = 0.5.dp
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun MessageItemRow(
//    userId: Int,
//    name: String,
//    username: String,
//    profileImageUrl: String?,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(horizontal = 20.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Profile Image
//        Box(
//            modifier = Modifier
//                .size(56.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFE0E0E0)),
//            contentAlignment = Alignment.Center
//        ) {
//            if (!profileImageUrl.isNullOrEmpty()) {
//                AsyncImage(
//                    model = profileImageUrl,
//                    contentDescription = "Profile picture of $name",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(CircleShape),
//                    contentScale = ContentScale.Crop
//                )
//            } else {
//                // Fallback to first letter if no image
//                Text(
//                    text = name.firstOrNull()?.uppercase() ?: "?",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.White
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        // Name and Username
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = name,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF2B2B2B)
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            if (username.isNotEmpty()) {
//                Text(
//                    text = "@$username",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Normal,
//                    color = Color(0xFF9E9E9E)
//                )
//            }
//        }
//
//        // Online indicator (optional - you can customize this)
//        Box(
//            modifier = Modifier
//                .size(10.dp)
//                .clip(CircleShape)
//                .background(Color(0xFF4CAF50)) // Green for online
//        )
//    }
//}
//

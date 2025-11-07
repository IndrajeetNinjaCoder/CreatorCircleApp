package com.cc.creatorcircle.ui.screens.message

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
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageConnections(
    navController: NavController,
    onBackClick: () -> Unit = {},
    onMessageClick: (Int, String) -> Unit = { _, _ -> } // userId and userName
) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Collect states from PostsViewModel
    val userProfile by postsViewModel.userProfile.collectAsState()
    val profileLoading by postsViewModel.profileLoading.collectAsState()
    val profileError by postsViewModel.profileError.collectAsState()

    // Get accepted connections
    val acceptedConnections = userProfile?.accepted_connections?.users ?: emptyList()

    // Fetch user profile on first composition
    LaunchedEffect(Unit) {
        postsViewModel.fetchUserProfile()
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                profileError != null -> {
                    // Error state
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
                            Button(onClick = { postsViewModel.fetchUserProfile() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                acceptedConnections.isEmpty() -> {
                    // Empty state
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(acceptedConnections) { connection ->
                            MessageItemRow(
                                userId = connection.user_id,
                                name = connection.full_name ?: "",
                                username = connection.username ?: "",
                                profileImageUrl = connection.profile_pic,
                                onClick = {
                                    navController.navigate(
                                        Screen.MessageScreen.createRoute(
                                            userId = connection.user_id,
                                            userName = connection.username ?: "",
                                            profilePic = connection.profile_pic
                                        )
                                    )
                                    onMessageClick(connection.user_id, connection.username)
                                }
                            )
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
                    text = "@$username $userId",
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
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.TopBar
//import com.cc.creatorcircle.ui.navigation.Screen
//
//data class MessageItem(
//    val name: String,
//    val message: String,
//    val time: String,
//    val profileImage: Int // Resource ID
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MessageConnections(
//    navController: NavController,
//    onBackClick: () -> Unit = {},
//    onMessageClick: (MessageItem) -> Unit = {}
//) {
//    // Sample data - replace with your actual data
//    val messages = List(10) {
//        MessageItem(
//            name = "Nisha",
//            message = "Sent an attachment",
//            time = "1hr ago",
//            profileImage = 0 // Replace with actual drawable resource
//        )
//    }
//
//    Scaffold(
//        topBar = { TopBar(title = "Messages", navController) },
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues),
//            contentPadding = PaddingValues(vertical = 8.dp)
//        ) {
//            items(messages) { message ->
//                MessageItemRow(
//                    message = message,
//                    onClick = { onMessageClick(message) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun MessageItemRow(
//    message: MessageItem,
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
//            // Placeholder - replace with actual image loading
//            // You can use Coil or Glide for image loading:
//            // AsyncImage(model = message.profileImage, contentDescription = null)
//            Text(
//                text = message.name.first().toString(),
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.White
//            )
//        }
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        // Name and Message
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = message.name,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF2B2B2B)
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = message.message,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Normal,
//                color = Color(0xFF9E9E9E)
//            )
//        }
//
//        // Time and Unread Indicator
//        Column(
//            horizontalAlignment = Alignment.End
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(6.dp)
//                        .clip(CircleShape)
//                        .background(Color(0xFF9E9E9E))
//                )
//                Spacer(modifier = Modifier.width(6.dp))
//                Text(
//                    text = message.time,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Normal,
//                    color = Color(0xFF9E9E9E)
//                )
//            }
//        }
//    }
//}
//

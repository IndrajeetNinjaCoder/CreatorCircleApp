package com.cc.creatorcircle.ui.screens.connections.sent

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.ConnectionUser
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.screens.connections.formatFollowerCount
import com.cc.creatorcircle.ui.screens.connections.getInstagramFollowersFromConnectionUser
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper

@Composable
fun SentRequestContent(
    navController: NavController,
    sentConnections: List<ConnectionUser>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit = {},
    onCancelConnectionClick: (userId: Int) -> Unit = {},
    onUserProfileClick: (userId: Int) -> Unit = {}
) {
    // Add logging to debug
    Log.d("SentRequestContent", "Sent connections count: ${sentConnections.size}")
    Log.d("SentRequestContent", "Is loading: $isLoading")
    Log.d("SentRequestContent", "Error: $error")

    // Track sent connections loaded
    LaunchedEffect(sentConnections.size) {
        if (sentConnections.isNotEmpty()) {
            FirebaseAnalyticsHelper.logFeatureUsed("sent_connections_loaded")
        }
    }

    // Track errors
    LaunchedEffect(error) {
        error?.let {
            FirebaseAnalyticsHelper.logError(
                errorType = "load_sent_connections_error",
                errorMessage = it,
                context = "SentRequestContent"
            )
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        error != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("retry_sent_connections")
                        onRefresh()
                    }) {
                        Text("Retry")
                    }
                }
            }
        }

        sentConnections.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No sent requests",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("refresh_empty_sent_requests")
                        onRefresh()
                    }) {
                        Text("Refresh")
                    }
                }
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sentConnections) { connectionUser ->
                    // Track sent connection viewed
                    LaunchedEffect(connectionUser.userId) {
                        FirebaseAnalyticsHelper.logSentConnectionViewed(
                            targetUserId = connectionUser.userId,
                            username = connectionUser.username ?: ""
                        )
                    }

                    Log.d("SentRequestContent", "Rendering user: ${connectionUser.username}")
                    SentConnectionCard(
                        navController = navController,
                        connectionUser = connectionUser,
                        onCancelConnectionClick = onCancelConnectionClick,
                        onUserProfileClick = onUserProfileClick
                    )
                }
            }
        }
    }
}

@Composable
fun SentConnectionCard(
    navController: NavController,
    connectionUser: ConnectionUser,
    onCancelConnectionClick: (userId: Int) -> Unit = {},
    onUserProfileClick: (userId: Int) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clickable profile section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseAnalyticsHelper.logProfileClicked(
                            userId = connectionUser.userId,
                            source = "sent_request_card"
                        )
                        onUserProfileClick(connectionUser.userId)
                        navController.navigate("userprofile/${connectionUser.userId}")
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!connectionUser.profilePic.isNullOrEmpty()) {
                        AsyncImage(
                            model = connectionUser.profilePic,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_profile1),
                            error = painterResource(id = R.drawable.ic_profile1)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(70.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name/Username
                Text(
                    text = connectionUser.fullName ?: connectionUser.username ?: "Unknown User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Username if full name exists
                if (!connectionUser.fullName.isNullOrEmpty() && !connectionUser.username.isNullOrEmpty()) {
                    Text(
                        text = "@${connectionUser.username}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Social media stats
            connectionUser.platformFollowers["instagram"]?.let { instagramAccounts ->
                if (instagramAccounts.isNotEmpty()) {
                    val primaryAccount = instagramAccounts.firstOrNull { it.isPrimary } ?: instagramAccounts.first()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_instagram),
                            contentDescription = "Instagram",
                            modifier = Modifier.size(16.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = formatFollowerCount(primaryAccount.followers),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Categories (if available)
            connectionUser.categories?.let { categories ->
                if (categories.isNotEmpty()) {
                    Text(
                        text = categories.take(2).joinToString(", "),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Cancel button
            GradientButton(
                text = "Cancel Request",
                modifier = Modifier.fillMaxWidth()
            ) {
                FirebaseAnalyticsHelper.logConnectionRequestCancelled(
                    targetUserId = connectionUser.userId,
                    source = "sent_request_card_button"
                )
                onCancelConnectionClick(connectionUser.userId)
            }
        }
    }
}













//package com.cc.creatorcircle.ui.screens.connections.sent
//
//import android.util.Log
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.Button
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.data.models.ConnectionUser
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.ui.screens.connections.formatFollowerCount
//import com.cc.creatorcircle.ui.screens.connections.getInstagramFollowersFromConnectionUser
//
//@Composable
//fun SentRequestContent(
//    navController: NavController,
//    sentConnections: List<ConnectionUser>,
//    isLoading: Boolean,
//    error: String?,
//    onRefresh: () -> Unit = {},
//    onCancelConnectionClick: (userId: Int) -> Unit = {}
//) {
//    // Add logging to debug
//    Log.d("SentRequestContent", "Sent connections count: ${sentConnections.size}")
//    Log.d("SentRequestContent", "Is loading: $isLoading")
//    Log.d("SentRequestContent", "Error: $error")
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
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    Text(
//                        text = "Error: $error",
//                        color = Color.Red,
//                        textAlign = TextAlign.Center,
//                        modifier = Modifier.padding(16.dp)
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
//                        fontSize = 16.sp,
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
//                items(sentConnections) { connectionUser ->
//                    Log.d("SentRequestContent", "Rendering user: ${connectionUser.username}")
//                    SentConnectionCard(
//                        navController = navController,
//                        connectionUser = connectionUser,
//                        onCancelConnectionClick = onCancelConnectionClick
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SentConnectionCard(
//    navController: NavController,
//    connectionUser: ConnectionUser,
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
//            // Clickable profile section
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        navController.navigate("userprofile/${connectionUser.userId}")
//                    },
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image
//                Box(
//                    modifier = Modifier
//                        .size(100.dp)
//                        .clip(CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (!connectionUser.profilePic.isNullOrEmpty()) {
//                        AsyncImage(
//                            model = connectionUser.profilePic,
//                            contentDescription = "Profile",
//                            modifier = Modifier
//                                .size(100.dp)
//                                .clip(CircleShape),
//                            contentScale = ContentScale.Crop,
//                            placeholder = painterResource(id = R.drawable.ic_profile1),
//                            error = painterResource(id = R.drawable.ic_profile1)
//                        )
//                    } else {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Profile",
//                            modifier = Modifier.size(70.dp),
//                            tint = Color.Gray.copy(alpha = 0.5f)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Name/Username
//                Text(
//                    text = connectionUser.fullName ?: connectionUser.username ?: "Unknown User",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    textAlign = TextAlign.Center,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier.padding(horizontal = 4.dp)
//                )
//
//                Spacer(modifier = Modifier.height(4.dp))
//
//                // Username if full name exists
//                if (!connectionUser.fullName.isNullOrEmpty() && !connectionUser.username.isNullOrEmpty()) {
//                    Text(
//                        text = "@${connectionUser.username}",
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Social media stats
//            connectionUser.platformFollowers["instagram"]?.let { instagramAccounts ->
//                if (instagramAccounts.isNotEmpty()) {
//                    val primaryAccount = instagramAccounts.firstOrNull { it.isPrimary } ?: instagramAccounts.first()
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center,
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.ic_instagram),
//                            contentDescription = "Instagram",
//                            modifier = Modifier.size(16.dp),
//                            contentScale = ContentScale.Crop
//                        )
//                        Spacer(modifier = Modifier.size(4.dp))
//                        Text(
//                            text = formatFollowerCount(primaryAccount.followers),
//                            fontSize = 12.sp,
//                            color = Color.Gray,
//                            fontWeight = FontWeight.Medium
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//            }
//
//            // Categories (if available)
//            connectionUser.categories?.let { categories ->
//                if (categories.isNotEmpty()) {
//                    Text(
//                        text = categories.take(2).joinToString(", "),
//                        fontSize = 11.sp,
//                        color = Color.Gray,
//                        textAlign = TextAlign.Center,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        modifier = Modifier.padding(horizontal = 4.dp)
//                    )
//                    Spacer(modifier = Modifier.height(12.dp))
//                }
//            }
//
//            // Cancel button
//            GradientButton(
//                text = "Cancel Request",
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                onCancelConnectionClick(connectionUser.userId)
//            }
//        }
//    }
//}

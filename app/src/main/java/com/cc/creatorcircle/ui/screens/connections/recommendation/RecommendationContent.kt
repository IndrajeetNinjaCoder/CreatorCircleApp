package com.cc.creatorcircle.ui.screens.connections.recommendation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
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
import com.cc.creatorcircle.data.models.UserWithScore
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper

@Composable
fun RecommendationContent(
    navController: NavController,
    recommendedUsers: List<UserWithScore>,
    isLoading: Boolean,
    error: String?,
    onConnectClick: (Int) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    // Filter out users with null userId
    val validUsers = recommendedUsers.filter { it.userId != null && it.userId > 0 }

    // Track recommendations loaded
    LaunchedEffect(validUsers.size) {
        if (validUsers.isNotEmpty()) {
            FirebaseAnalyticsHelper.logFeatureUsed("recommendations_loaded")
        }
    }

    // Track errors
    LaunchedEffect(error) {
        error?.let {
            FirebaseAnalyticsHelper.logError(
                errorType = "load_recommendations_error",
                errorMessage = it,
                context = "RecommendationContent"
            )
        }
    }

    when {
        isLoading && validUsers.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(text = "Loading recommendations...")
                }
            }
        }

        error != null && validUsers.isEmpty() -> {
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
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("retry_recommendations")
                        onRefresh()
                    }) {
                        Text("Retry")
                    }
                }
            }
        }

        validUsers.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "No recommendations found",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("refresh_empty_recommendations")
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
                items(
                    items = validUsers,
                    key = { it.userId ?: System.nanoTime() }
                ) { userWithScore ->
                    RecommendationProfileCard(
                        navController = navController,
                        userWithScore = userWithScore,
                        onConnectClick = {
                            userWithScore.userId?.let { onConnectClick(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendationProfileCard(
    navController: NavController,
    userWithScore: UserWithScore,
    onConnectClick: () -> Unit = {}
) {
    // Return early if userId is null
    if (userWithScore.userId == null) {
        return
    }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseAnalyticsHelper.logProfileClicked(
                            userId = userWithScore.userId,
                            source = "recommendation_card"
                        )
                        navController.navigate("userprofile/${userWithScore.userId}")
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!userWithScore.profilePic.isNullOrEmpty()) {
                        AsyncImage(
                            model = userWithScore.profilePic,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(116.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_profile1),
                            error = painterResource(id = R.drawable.ic_profile1)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(90.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name/Username
                Text(
                    text = userWithScore.fullName?.takeIf { it.isNotEmpty() }
                        ?: userWithScore.username?.takeIf { it.isNotEmpty() }
                        ?: "Unknown User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Categories
            if (!userWithScore.categories.isNullOrEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(userWithScore.categories.take(2)) { category ->
                        Surface(
                            modifier = Modifier,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = category,
                                fontSize = 8.sp,
                                color = Color(0xFF1976D2),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bio
            Text(
                text = userWithScore.bio?.takeIf { it.isNotEmpty() } ?: "No bio available",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connect Button
            val buttonText = when {
                userWithScore.isConnected -> "Connected"
                userWithScore.connectionStatus == "pending" -> "Pending"
                else -> "Connect"
            }

            val isButtonEnabled = !userWithScore.isConnected &&
                    userWithScore.connectionStatus != "pending"

            if (isButtonEnabled) {
                GradientButton(
                    buttonText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    FirebaseAnalyticsHelper.logConnectionRequestInitiated(
                        targetUserId = userWithScore.userId,
                        source = "recommendation_card_connect"
                    )
                    onConnectClick()
                }
            } else {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}














//package com.cc.creatorcircle.ui.screens.connections.recommendation
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
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
//import com.cc.creatorcircle.data.models.UserWithScore
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
//
//@Composable
//fun RecommendationContent(
//    navController: NavController,
//    recommendedUsers: List<UserWithScore>,
//    isLoading: Boolean,
//    error: String?,
//    onConnectClick: (Int) -> Unit = {},
//    onRefresh: () -> Unit = {},
//    onUserProfileClick: (Int) -> Unit = {}
//) {
//    // Filter out users with null userId
//    val validUsers = recommendedUsers.filter { it.userId != null && it.userId > 0 }
//
//    // Track recommendations loaded
//    LaunchedEffect(validUsers.size) {
//        if (validUsers.isNotEmpty()) {
//            FirebaseAnalyticsHelper.logFeatureUsed("recommendations_loaded")
//        }
//    }
//
//    // Track errors
//    LaunchedEffect(error) {
//        error?.let {
//            FirebaseAnalyticsHelper.logError(
//                errorType = "load_recommendations_error",
//                errorMessage = it,
//                context = "RecommendationContent"
//            )
//        }
//    }
//
//    when {
//        isLoading && validUsers.isEmpty() -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    CircularProgressIndicator()
//                    Text(text = "Loading recommendations...")
//                }
//            }
//        }
//
//        error != null && validUsers.isEmpty() -> {
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
//                    Button(onClick = {
//                        FirebaseAnalyticsHelper.logFeatureUsed("retry_recommendations")
//                        onRefresh()
//                    }) {
//                        Text("Retry")
//                    }
//                }
//            }
//        }
//
//        validUsers.isEmpty() -> {
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
//                    Button(onClick = {
//                        FirebaseAnalyticsHelper.logFeatureUsed("refresh_empty_recommendations")
//                        onRefresh()
//                    }) {
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
//                items(
//                    items = validUsers,
//                    key = { it.userId ?: System.nanoTime() }
//                ) { userWithScore ->
//                    // Track recommendation viewed
//                    LaunchedEffect(userWithScore.userId) {
//                        userWithScore.userId?.let {
//                            FirebaseAnalyticsHelper.logRecommendationViewed(
//                                userId = it,
//                                score = userWithScore.compatibilityScore ?: 0f
//                            )
//                        }
//                    }
//
//                    RecommendationProfileCard(
//                        navController = navController,
//                        userWithScore = userWithScore,
//                        onConnectClick = {
//                            userWithScore.userId?.let { onConnectClick(it) }
//                        },
//                        onUserProfileClick = {
//                            userWithScore.userId?.let { onUserProfileClick(it) }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RecommendationProfileCard(
//    navController: NavController,
//    userWithScore: UserWithScore,
//    onConnectClick: () -> Unit = {},
//    onUserProfileClick: () -> Unit = {}
//) {
//    // Return early if userId is null
//    if (userWithScore.userId == null) {
//        return
//    }
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
//                    .clickable {
//                        FirebaseAnalyticsHelper.logProfileClicked(
//                            userId = userWithScore.userId,
//                            source = "recommendation_card"
//                        )
//                        onUserProfileClick()
//                        navController.navigate("userprofile/${userWithScore.userId}")
//                    },
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image
//                Box(
//                    modifier = Modifier
//                        .size(116.dp)
//                        .clip(CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (!userWithScore.profilePic.isNullOrEmpty()) {
//                        AsyncImage(
//                            model = userWithScore.profilePic,
//                            contentDescription = "Profile",
//                            modifier = Modifier
//                                .size(116.dp)
//                                .clip(CircleShape),
//                            contentScale = ContentScale.Crop,
//                            placeholder = painterResource(id = R.drawable.ic_profile1),
//                            error = painterResource(id = R.drawable.ic_profile1)
//                        )
//                    } else {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Profile",
//                            modifier = Modifier.size(90.dp),
//                            tint = Color.Gray.copy(alpha = 0.5f)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Name/Username
//                Text(
//                    text = userWithScore.fullName?.takeIf { it.isNotEmpty() }
//                        ?: userWithScore.username?.takeIf { it.isNotEmpty() }
//                        ?: "Unknown User",
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
//            // Categories
//            if (!userWithScore.categories.isNullOrEmpty()) {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    items(userWithScore.categories.take(2)) { category ->
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
//            // Bio
//            Text(
//                text = userWithScore.bio?.takeIf { it.isNotEmpty() } ?: "No bio available",
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
//            // Connect Button
//            val buttonText = when {
//                userWithScore.isConnected -> "Connected"
//                userWithScore.connectionStatus == "pending" -> "Pending"
//                else -> "Connect"
//            }
//
//            val isButtonEnabled = !userWithScore.isConnected &&
//                    userWithScore.connectionStatus != "pending"
//
//            if (isButtonEnabled) {
//                GradientButton(
//                    buttonText,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(36.dp)
//                ) {
//                    FirebaseAnalyticsHelper.logConnectionRequestInitiated(
//                        targetUserId = userWithScore.userId,
//                        source = "recommendation_card_connect"
//                    )
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














//package com.cc.creatorcircle.ui.screens.connections.recommendation
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
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
//import com.cc.creatorcircle.data.models.UserWithScore
//import com.cc.creatorcircle.ui.components.GradientButton
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
//    // Filter out users with null userId
//    val validUsers = recommendedUsers.filter { it.userId != null && it.userId > 0 }
//
//    when {
//        isLoading && validUsers.isEmpty() -> {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(16.dp)
//                ) {
//                    CircularProgressIndicator()
//                    Text(text = "Loading recommendations...")
//                }
//            }
//        }
//
//        error != null && validUsers.isEmpty() -> {
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
//        validUsers.isEmpty() -> {
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
//                items(
//                    items = validUsers,
//                    key = { it.userId ?: System.nanoTime() }
//                ) { userWithScore ->
//                    RecommendationProfileCard(
//                        navController = navController,
//                        userWithScore = userWithScore,
//                        onConnectClick = {
//                            userWithScore.userId?.let { onConnectClick(it) }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RecommendationProfileCard(
//    navController: NavController,
//    userWithScore: UserWithScore,
//    onConnectClick: () -> Unit = {}
//) {
//    // Return early if userId is null
//    if (userWithScore.userId == null) {
//        return
//    }
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
//                    .clickable {
//                        navController.navigate("userprofile/${userWithScore.userId}")
//                    },
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // Profile Image
//                Box(
//                    modifier = Modifier
//                        .size(116.dp)
//                        .clip(CircleShape),
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (!userWithScore.profilePic.isNullOrEmpty()) {
//                        AsyncImage(
//                            model = userWithScore.profilePic,
//                            contentDescription = "Profile",
//                            modifier = Modifier
//                                .size(116.dp)
//                                .clip(CircleShape),
//                            contentScale = ContentScale.Crop,
//                            placeholder = painterResource(id = R.drawable.ic_profile1),
//                            error = painterResource(id = R.drawable.ic_profile1)
//                        )
//                    } else {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Profile",
//                            modifier = Modifier.size(90.dp),
//                            tint = Color.Gray.copy(alpha = 0.5f)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Name/Username
//                Text(
//                    text = userWithScore.fullName?.takeIf { it.isNotEmpty() }
//                        ?: userWithScore.username?.takeIf { it.isNotEmpty() }
//                        ?: "Unknown User",
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
//            // Categories
//            if (!userWithScore.categories.isNullOrEmpty()) {
//                LazyRow(
//                    horizontalArrangement = Arrangement.spacedBy(4.dp),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    items(userWithScore.categories.take(2)) { category ->
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
//            // Bio
//            Text(
//                text = userWithScore.bio?.takeIf { it.isNotEmpty() } ?: "No bio available",
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
//            // Connect Button
//            val buttonText = when {
//                userWithScore.isConnected -> "Connected"
//                userWithScore.connectionStatus == "pending" -> "Pending"
//                else -> "Connect"
//            }
//
//            val isButtonEnabled = !userWithScore.isConnected &&
//                    userWithScore.connectionStatus != "pending"
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

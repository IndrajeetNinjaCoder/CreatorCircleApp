package com.cc.creatorcircle.ui.screens.connections.connected


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
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserDiscoveryViewModel
import com.cc.creatorcircle.viewModel.UserConnectionsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RecommendationContent(
    navController: NavController,
    recommendedUsers: List<UserWithScore>,
    isLoading: Boolean,
    error: String?,
    onConnectClick: (Int) -> Unit = {},
    onRefresh: () -> Unit = {}
) {
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
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onRefresh) {
                        Text("Retry")
                    }
                }
            }
        }

        recommendedUsers.isEmpty() -> {
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
                    Button(onClick = onRefresh) {
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
                items(recommendedUsers) { userWithScore ->
                    RecommendationProfileCard(
                        navController = navController,
                        userWithScore = userWithScore,
                        onConnectClick = { onConnectClick(userWithScore.user.userId) }
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
    onConnectClick: () -> Unit = {},
    onViewProfileClick: () -> Unit = {}
) {
    val user = userWithScore.user

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
                    .clickable(
                        enabled = true,
                        onClick = {
                            navController.navigate("userprofile/${user.userId}")
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image - Made smaller for grid layout
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
//                        .clickable(
//                            onClick = onViewProfileClick,
//                            interactionSource = remember { MutableInteractionSource() },
//                            indication = null // Remove ripple entirely, or use LocalIndication.current for default
//                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE91E63), // Pink
                                    Color(0xFF9C27B0), // Purple
                                    Color(0xFF673AB7)  // Deep Purple
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8E24AA)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!user.profilePic.isNullOrEmpty()) {
                            AsyncImage(
                                model = user.profilePic,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_profile),
                                error = painterResource(id = R.drawable.ic_profile)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name/Username
                Text(
                    text = user.fullName ?: user.username ?: "Unknown User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Categories - Show only first 2 for space
            if (user.categories.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(user.categories.take(2)) { category ->
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

            // Bio/Description - Reduced lines for grid layout
            Text(
                text = user.bio ?: "No bio available",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Button - Full width, smaller text
            val buttonText = when {
                user.isConnected -> "Connected"
                user.connectionStatus == "pending" -> "Pending"
                else -> "Connect"
            }

            val isButtonEnabled = !user.isConnected && user.connectionStatus != "pending"

            if (isButtonEnabled) {
                GradientButton(
                    buttonText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
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

package com.cc.creatorcircle.ui.screens.connections.pending


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.cc.creatorcircle.data.models.UserInfo
import com.cc.creatorcircle.ui.screens.connections.formatFollowerCount
import com.cc.creatorcircle.ui.screens.connections.getInstagramFollowers


@Composable
fun PendingConnectionsContent(
    navController: NavController,
    pendingConnections: List<UserInfo>,
    isLoading: Boolean,
    error: String?,
    onAcceptClick: (connectionId: Int) -> Unit,
    onRejectClick: (connectionId: Int) -> Unit,
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
                Text(
                    text = "Error: $error",
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

        pendingConnections.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No pending connections",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingConnections) { user ->
                    PendingConnectionCard(
                        navController = navController,
                        userInfo = user,
                        showConnectButton = true,
                        buttonText = "Accept",
                        onRejectClick = {
                            onRejectClick(user?.connection_id ?: -1)
                        },
                        onAcceptClick = {
                            onAcceptClick(user?.connection_id ?: -1)
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun PendingConnectionCard(
    navController: NavController,
    userInfo: UserInfo,
    showConnectButton: Boolean = true,
    buttonText: String = "Connect",
    onRejectClick: () -> Unit = {},
    onAcceptClick: () -> Unit = {},
    onViewProfileClick: () -> Unit = {}
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = true,
                        onClick = {
                            navController.navigate("userprofile/${userInfo.user_id}")
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
//                        .background(
//                            brush = Brush.radialGradient(
//                                colors = listOf(
//                                    Color(0xFFE91E63), // Pink
//                                    Color(0xFF9C27B0), // Purple
//                                    Color(0xFF673AB7)  // Deep Purple
//                                )
//                            )
//                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userInfo.profile_pic != null) {
                            AsyncImage(
                                model = userInfo.profile_pic,
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
                                modifier = Modifier.size(90.dp),
                                tint = Color.Gray.copy(alpha = 0.5f) // Optional: make icon semi-transparent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name/Username
                Text(
                    text = userInfo.full_name ?: userInfo.username ?: "Unknown User",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Social media stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Show platform followers if available
                userInfo.platform_followers?.let { platformFollowers ->
                    val instagramFollowers = getInstagramFollowers(platformFollowers)
                    if (instagramFollowers != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_instagram),
                                contentDescription = "Instagram",
                                modifier = Modifier.size(20.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = formatFollowerCount(instagramFollowers),
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bio/Description
            Text(
                text = userInfo.bio ?: "No bio available",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Accept/Reject Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reject Icon
                IconButton(
                    onClick = { onRejectClick() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFFF5252),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Reject",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }




                // Accept Icon
                IconButton(
                    onClick = { onAcceptClick() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFF4CAF50),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

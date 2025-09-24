package com.cc.creatorcircle.ui.screens.connections.connected


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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.screens.connections.formatFollowerCount
import com.cc.creatorcircle.ui.screens.connections.getInstagramFollowers


@Composable
fun AcceptedConnectionsContent(
    navController: NavController,
    acceptedConnections: List<UserInfo>,
    isLoading: Boolean,
    error: String?,
    onRemoveConnectionClick: (userId: Int) -> Unit = {}
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

        acceptedConnections.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No connections found",
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(acceptedConnections) { user ->
                    UserConnectionCard(
                        navController = navController,
                        userInfo = user,
                        showConnectButton = false,
                        buttonText = "Connected",
                        onRemoveConnectionClick = { onRemoveConnectionClick(user.user_id) }
                    )
                }
            }
        }
    }
}


@Composable
fun UserConnectionCard(
    navController: NavController,
    userInfo: UserInfo,
    showConnectButton: Boolean = true,
    buttonText: String = "Connect",
    onRemoveConnectionClick: () -> Unit = {},
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
                        .size(116.dp)
                        .clip(CircleShape),
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
                                    .size(116.dp)
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

            // Buttons

            GradientButton("Remove", modifier = Modifier.fillMaxWidth()) {
                onRemoveConnectionClick()
            }
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//
//                CustomOutlinedButton("View Profile", modifier = Modifier.weight(1f)) {
//                    onViewProfileClick()
//                }
//            }
        }
    }
}

package com.cc.creatorcircle.ui.screens.connections.sent


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


@Composable
fun SentRequestContent(
    navController: NavController,
    sentConnections: List<ConnectionUser>,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit = {},
    onCancelConnectionClick: (userId: Int) -> Unit = {}
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sentConnections) { connectionUser ->
                    SentConnectionCard(
                        navController = navController,
                        connectionUser = connectionUser,
                        onViewProfileClick = { /* Handle view profile */ },
                        onCancelConnectionClick = { onCancelConnectionClick(connectionUser.userId) }

                    )
                }
            }
        }
    }
}

// New component for sent connection cards
@Composable
fun SentConnectionCard(
    navController: NavController,
    connectionUser: ConnectionUser,
    onViewProfileClick: () -> Unit = {},
    onCancelConnectionClick: (userId: Int) -> Unit = {}
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
                            navController.navigate("userprofile/${connectionUser.userId}")
                        }
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
//                        .background(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!connectionUser.profilePic.isNullOrEmpty()) {
                            AsyncImage(
                                model = connectionUser.profilePic,
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
                                tint = Color.Gray.copy(alpha = 0.5f) // Optional: make icon semi-transparent
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name/Username
                Text(
                    text = connectionUser.fullName ?: connectionUser.username ?: "Unknown User",
                    fontSize = 20.sp,
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Platform followers
                connectionUser.platformFollowers.let { platformFollowers ->
                    val instagramFollowers =
                        getInstagramFollowersFromConnectionUser(platformFollowers)
                    if (instagramFollowers != null && instagramFollowers > 0) {
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


            Spacer(modifier = Modifier.height(24.dp))

            GradientButton("Cancel", modifier = Modifier.fillMaxWidth()) {
                onCancelConnectionClick(connectionUser.userId)
            }

        }
    }
}

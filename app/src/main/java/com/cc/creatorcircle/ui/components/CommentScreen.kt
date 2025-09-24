@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cc.creatorcircle.R
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager

data class Comment(
    val id: Int,
    val username: String,
    val text: String,
    val profileImage: Int,
    val likes: Int = 0,
    val hasMoreReplies: Boolean = false,
    val replyCount: Int = 0
)

data class Post(
    val username: String,
    val handle: String,
    val timeAgo: String,
    val content: String,
    val postImage: Int,
    val profileImage: Int
)

@Composable
fun CommentScreen() {
    val context = LocalContext.current
    val userDataManager = remember { UserDataManager(context) }
    var userData by remember { mutableStateOf(UserData()) }

    var newComment by remember { mutableStateOf("") }
    var comments by remember {
        mutableStateOf(listOf(
            Comment(1, "Priya", "Gorgeous picture !!", R.drawable.ic_profile, 0, true, 3),
            Comment(2, "Priya", "Gorgeous picture !!", R.drawable.ic_profile, 0),
            Comment(3, "Priya", "Gorgeous picture !!", R.drawable.ic_profile, 0)
        ))
    }

    LaunchedEffect(Unit) {
        userData = userDataManager.getUserData()
    }

    val post = Post(
        username = "Nisha__119",
        handle = "@sonu",
        timeAgo = "1 day ago",
        content = "has done a great performance on the dance show",
        postImage = R.drawable.ic_post_image,
        profileImage = R.drawable.ic_profile
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Top Navigation Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App Logo
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Color(0xFFE91E63),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "oo",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            actions = {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBA68C8)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Feed", color = Color.White)
                    }
                    TextButton(onClick = { }) {
                        Text("Resources", color = Color.Gray)
                    }
                    TextButton(onClick = { }) {
                        Text("Explore", color = Color.Gray)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Main Content
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            // User Profile Section
            item {
                UserProfileSection()
            }

            // Post Section
            item {
                PostSection(post = post)
            }

            // Comments Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Comments",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // Comments List
            items(comments) { comment ->
                CommentItem(
                    comment = comment,
                    onLike = { /* Handle like */ }
                )
            }

            // Hide replies option for first comment
            if (comments.isNotEmpty() && comments[0].hasMoreReplies) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(start = 72.dp, top = 8.dp, bottom = 16.dp)
                    ) {
                        TextButton(onClick = { }) {
                            Text(
                                text = "View ${comments[0].replyCount} more replies",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(start = 72.dp, bottom = 16.dp)
                    ) {
                        TextButton(onClick = { }) {
                            Text(
                                text = "Hide replies",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Comment Input Section
        CommentInputSection(
            value = newComment,
            onValueChange = { newComment = it },
            onSend = {
                if (newComment.isNotBlank()) {
                    comments = comments + Comment(
                        id = comments.size + 1,
                        username = "You",
                        text = newComment,
                        profileImage = R.drawable.ic_profile
                    )
                    newComment = ""
                }
            }
        )
    }
}

@Composable
fun UserProfileSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile Image
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Nisha",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Post Input
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Share your thoughts", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBA68C8),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                trailingIcon = {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBA68C8)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Post", color = Color.White)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Media Options
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Image",
                        tint = Color(0xFFBA68C8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Image", color = Color(0xFFBA68C8))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VideoLibrary,
                        contentDescription = "Video",
                        tint = Color(0xFFBA68C8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Video", color = Color(0xFFBA68C8))
                }
            }
        }
    }
}

@Composable
fun PostSection(post: Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = post.username,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Text(
                            text = post.timeAgo,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE1BEE7)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Connect", color = Color(0xFF7B1FA2))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${post.handle} ${post.content}",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Post Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onLike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = comment.text,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Reply",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onLike,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CommentInputSection(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Add a comment for Nisha_99", color = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFBA68C8),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(25.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFFBA68C8),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SocialMediaCommentsScreenPreview() {
    MaterialTheme {
        CommentScreen()
    }
}

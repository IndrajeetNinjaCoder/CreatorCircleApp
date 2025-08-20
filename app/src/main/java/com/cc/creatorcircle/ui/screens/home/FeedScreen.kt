@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cc.creatorcircle.ui.components.TopBar

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.draw.shadow
import com.cc.creatorcircle.R

//@Composable
//fun FeedScreen() {
//    Box(modifier = Modifier.fillMaxSize()) {
//        Column {
//            TopBar() {}
//
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .weight(1f)
//            ) {
//                item {
//                    PostInputSection()
//                }
//
//
//                item {
//                    PostCardSection()
//                }
//            }
//        }
////        CreatePostButton(modifier = Modifier
////            .align(Alignment.BottomEnd)
////            .padding(16.dp))
//
//        Spacer(modifier = Modifier.width(22.dp))
//        BottomNavBar(modifier = Modifier.align(Alignment.BottomCenter))
//    }
//}


//
//@Composable
//fun FeedScreen() {
//    Scaffold(
//        topBar = { TopBar() },
//        bottomBar = { BottomNavBar() },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues), // This handles the top and bottom spacing
//            contentPadding = PaddingValues(bottom = 16.dp), // Extra padding at bottom if needed
////            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            item {
//                Spacer(modifier = Modifier.height(22.dp))
//                PostInputSection()
//                Spacer(modifier = Modifier.height(22.dp))
//            }
//
//            item {
//                PostCardSection()
//                Spacer(modifier = Modifier.height(22.dp))
//            }
//
////            // Add more items as needed
////            items(10) { // Example of multiple posts
////                PostCardSection()
////            }
//        }
//    }
//}






//@Composable
//fun FeedScreen() {
//    Scaffold(
//        topBar = { TopBar() },
//        bottomBar = { BottomNavBar() },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {
//                    // Handle create post action here
//                    // You can navigate to create post screen or show a dialog
//                },
//                containerColor = MaterialTheme.colorScheme.primary,
//                contentColor = MaterialTheme.colorScheme.onPrimary,
//                modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
//            ) {
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Create Post"
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text(
//                        text = "Create Post",
//                        style = MaterialTheme.typography.labelMedium
//                    )
//                }
//            }
//        },
//        floatingActionButtonPosition = FabPosition.End,
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues), // This handles the top and bottom spacing
//            contentPadding = PaddingValues(bottom = 16.dp), // Extra padding at bottom if needed
////            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            item {
//                Spacer(modifier = Modifier.height(22.dp))
//                PostInputSection()
//                Spacer(modifier = Modifier.height(22.dp))
//            }
//
//            item {
//                PostCardSection()
//                Spacer(modifier = Modifier.height(22.dp))
//            }
//
////            // Add more items as needed
////            items(10) { // Example of multiple posts
////                PostCardSection()
////            }
//        }
//    }
//}











@Composable
fun FeedScreen() {
    var showNotification by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavBar() },
        floatingActionButton = {
            GradientCreatePostButton(
                onClick = {
                    // TODO: Add action
                },
                modifier = Modifier.padding(bottom = 2.dp, end = 2.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues), // This handles the top and bottom spacing
                contentPadding = PaddingValues(bottom = 16.dp), // Extra padding at bottom if needed
//                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    PostInputSection()
                    Spacer(modifier = Modifier.height(22.dp))
                }

                item {
                    PostCardSection()
                    Spacer(modifier = Modifier.height(22.dp))
                }

//                // Add more items as needed
//                items(10) { // Example of multiple posts
//                    PostCardSection()
//                }
            }

            // Good Morning Notification
            if (showNotification) {
                GoodMorningNotification(
                    onDismiss = { showNotification = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 140.dp)
                )
            }
        }
    }
}

@Composable
fun GoodMorningNotification(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentWidth()
            .widthIn(max = 220.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Good morning ❤️".let { text ->
                        if (text.length > 16) {
                            text.take(13) + "..."
                        } else {
                            text
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(end = 4.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Profile image
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = Color.Gray,
                            shape = CircleShape
                        )
                ) {
                    // You can replace this with actual profile image
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile), // Replace with actual profile image
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Close button
//            IconButton(
//                onClick = onDismiss,
//                modifier = Modifier.size(20.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(14.dp)
//                )
//            }
        }

        // Action buttons row
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reply button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Handle reply */ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reply), // Replace with reply icon
                    contentDescription = "Reply",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reply",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Share button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Handle share */ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share), // Replace with share icon
                    contentDescription = "Share",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Share",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}



@Composable
fun GradientCreatePostButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63)),
                    startX = 0f,
                    endX = 1000f
                ),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Create post", color = Color.White)
        }
    }
}



@Composable
fun TopBarSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_cc_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Feed",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Resources", color = Color(0xFF9A76E2), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Explore", color = Color(0xFF9A76E2), fontWeight = FontWeight.Medium)
        }
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Messages",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(
            imageVector = Icons.Filled.NotificationsNone,
            contentDescription = "Notifications",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}


@Composable
fun PostInputSection() {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
            )
            Text("Nisha", fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp) // Set minimum height to match design
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            // Custom placeholder at top-left
            if (text.isEmpty()) {
                Text(
                    text = "Share your thoughts",
                    color = Color(0xFFAAAAAA),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            // BasicTextField for better control
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (text.isEmpty()) 20.dp else 0.dp), // Space below placeholder
                maxLines = 4,
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp
                )
            )

            // Post button positioned at bottom-end
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { /* Handle post action */ }
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Post",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentButton(
                icon = R.drawable.ic_image,
                text = "Image"
            )
            Spacer(modifier = Modifier.width(12.dp))
            AttachmentButton(
                icon = R.drawable.ic_video,
                text = "Video"
            )
        }
    }

    Spacer(modifier = Modifier.width(12.dp))
}


@Composable
fun AttachmentButton(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
//            .border(
//                width = 1.dp,
//                color = Color(0xFFE0C0F5),
//                shape = RoundedCornerShape(12.dp)
//            )
            .padding(start = 12.dp)
            .clickable { }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = Color(0xFFB726FF),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color(0xFFB726FF),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

//@Composable
//fun IconTextButton(iconRes: Int, text: String) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier.clickable { }
//    ) {
//        Image(
//            painter = painterResource(id = iconRes),
//            contentDescription = text,
//            modifier = Modifier.size(20.dp)
//        )
//        Spacer(modifier = Modifier.width(4.dp))
//        Text(text, color = Color(0xFFB726FF), fontSize = 14.sp)
//    }
//}

@Composable
fun PostCardSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),

        ) {
        Row(
//            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Name and description
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nisha__119",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• 1 day ago",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "@sonu has done a great performance on the dance show",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        lineHeight = 16.sp // reduce line spacing (try 13–15.sp for 12.sp font)
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Connect Button
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEDE1FF)
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(32.dp)
                    .wrapContentWidth()
                    .padding(end = 8.dp),
                border = BorderStroke(1.dp, Color(0xFF8B5CF6))
            ) {
                Text(
                    text = "Connect",
                    color = Color(0xFF8B5CF6),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with profile, name, and connect button


                Spacer(modifier = Modifier.height(12.dp))

                // Post Image
                Image(
                    painter = painterResource(id = R.drawable.ic_post_image),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Likes and Comments count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "David warner & 76 others",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "55 Comments",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ActionButton(
                        icon = Icons.Default.FavoriteBorder,
                        text = "Like",
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = Icons.Default.ChatBubbleOutline,
                        text = "Comment",
                        modifier = Modifier.weight(1f)
                    )
                    ActionButton(
                        icon = Icons.Default.Share,
                        text = "Share",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Alternative version using drawable resources (if you prefer your existing icons)
@Composable
fun ActionButtonWithDrawable(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Color.Gray)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// If you want to use your existing IconTextButton, update it like this:
@Composable
fun IconTextButton(iconRes: Int, text: String) {
    Row(
        modifier = Modifier
            .clickable { }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(Color.Gray)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

//@Composable
//fun CreatePostButton(modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier
//            .background(
//                brush = Brush.horizontalGradient(
//                    listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
//                ),
//                shape = RoundedCornerShape(50)
//            )
//            .clickable {}
//            .padding(horizontal = 20.dp, vertical = 12.dp)
//    ) {
//        Text("+ Create post", color = Color.White, fontWeight = FontWeight.Bold)
//    }
//}


@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(R.drawable.ic_home, "Home", Color(0xFFB726FF))
        BottomNavItem(R.drawable.ic_connections, "Connections", Color.Gray)
        BottomNavItem(R.drawable.ic_sabo_ai, "Sabo AI", Color(0xFFFF6600))
        BottomNavItem(R.drawable.ic_video, "Live session", Color.Gray)

        // For profile icon in circle
        BottomNavProfileItem(R.drawable.ic_profile, "You", Color.Gray)
    }
}

@Composable
fun BottomNavItem(iconRes: Int, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Text(label, color = tint, fontSize = 12.sp)
    }
}

@Composable
fun BottomNavProfileItem(iconRes: Int, label: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ProfileIcon(iconRes)
        Text(label, color = tint, fontSize = 12.sp)
    }
}

@Composable
fun ProfileIcon(iconRes: Int) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = "Profile",
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .border(1.dp, Color.LightGray, CircleShape)
    )
}

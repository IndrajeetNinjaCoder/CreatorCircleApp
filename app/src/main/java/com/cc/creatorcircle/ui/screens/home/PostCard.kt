package com.cc.creatorcircle.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.PostDeletionState
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel

@Composable
fun PostCardSection(
    post: Post,
    userProfile: UserProfile?,
    connectionViewModel: ConnectionViewModel,
    postsViewModel: PostsViewModel,
    onLikeClick: (String) -> Unit = {},
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Post) -> Unit = {},
    onConnectClick: (Int) -> Unit = {},
    onEditClick: (Post) -> Unit = {},
    onViewPostClick: (Post) -> Unit = {},
    onDeleteClick: (Post) -> Unit = {}  // Add this parameter
) {


    // State to track if full content is shown
    var isExpanded by remember { mutableStateOf(false) }

    // State for dropdown menu
    var showDropdownMenu by remember { mutableStateOf(false) }

    // State for delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Observe post deletion state
    val postDeletionState by postsViewModel.postDeletionState.collectAsState()

    // Check if user is already connected
    val isAlreadyConnected = userProfile?.accepted_connections?.users?.any {
        it.user_id == post.author.id
    } ?: false

    // Check if user is following the post author
    val isFollowing = userProfile?.following?.users?.any {
        it.user_id == post.author.id
    } ?: false

    // Check if connection request is already sent
    val isConnectionSent = connectionViewModel.isConnectionAlreadySent(post.author.id)

    // Get connection loading state for this specific user
    val connectionLoading by connectionViewModel.isLoading.observeAsState(false)

    // Get the connection for this user to check status
    val connection = connectionViewModel.getConnectionByUserId(post.author.id)

    // Extract links from post content
    val links = extractLinksFromText(post.content)

    // Handle delete dialog result
    LaunchedEffect(postDeletionState) {
        when (postDeletionState) {
            is PostDeletionState.Success -> {
                // Show success message (optional)
                showDeleteDialog = false
                postsViewModel.clearDeletionState()
            }

            is PostDeletionState.Error -> {
                // Error is handled in the dialog
            }

            else -> {}
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (postDeletionState !is PostDeletionState.Loading) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text(
                    text = "Delete Post",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this post? This action cannot be undone.",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    // Show error if deletion failed
                    if (postDeletionState is PostDeletionState.Error) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = (postDeletionState as PostDeletionState.Error).message,
                            fontSize = 12.sp,
                            color = Color.Red
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        postsViewModel.deletePost(post.id)
                    },
                    enabled = postDeletionState !is PostDeletionState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    if (postDeletionState is PostDeletionState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Delete", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        postsViewModel.clearDeletionState()
                    },
                    enabled = postDeletionState !is PostDeletionState.Loading
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFB726FF).copy(alpha = 0.5f),
                        Color(0xFFFB3D91).copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    )
    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            // Profile Image
            if (post.author.avatar != null) {
                AsyncImage(
                    model = post.author.avatar,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile1),
                    error = painterResource(id = R.drawable.ic_profile1)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name and description
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.author.name ?: post.author.role ?: "Anonymous",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${post.timestamp}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
                if (post.author.role != null && post.author.role != post.author.name) {
                    Text(
                        text = "@${post.author.role}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Three-dot menu (only show if user is author)
            if (post.isAuthor) {
                Box {
                    androidx.compose.material3.IconButton(
                        onClick = { showDropdownMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    androidx.compose.material3.DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Edit",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                onEditClick(post)
                            }
                        )

                        androidx.compose.material3.DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Delete",
                                        fontSize = 14.sp,
                                        color = Color.Red
                                    )
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                onDeleteClick(post)  // Just call the callback
                            }
                        )

//                        androidx.compose.material3.DropdownMenuItem(
//                            text = {
//                                Row(verticalAlignment = Alignment.CenterVertically) {
//                                    Icon(
//                                        imageVector = Icons.Default.Visibility,
//                                        contentDescription = "View Post",
//                                        tint = Color.Black,
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(
//                                        text = "View Post",
//                                        fontSize = 14.sp,
//                                        color = Color.Black
//                                    )
//                                }
//                            },
//                            onClick = {
//                                showDropdownMenu = false
//                                onViewPostClick(post)
//                            }
//                        )
//
                    }
                }
            } else if (!post.isAuthor && !isAlreadyConnected) {
                // Connect Button (only show if not author and not already connected)
                Button(
                    onClick = {
                        if (!isConnectionSent && !connectionLoading && !isFollowing) {
                            onConnectClick(post.author.id)
                        }
                    },
                    enabled = !connectionLoading && !isConnectionSent && !isFollowing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isConnectionSent || isFollowing -> Color(0xFFE0E0E0)
                            else -> Color(0xFFEDE1FF)
                        },
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .wrapContentWidth()
                        .padding(end = 8.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isConnectionSent || isFollowing -> Color.Gray
                            else -> Color(0xFF8B5CF6)
                        }
                    )
                ) {
                    if (connectionLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF8B5CF6)
                        )
                    } else {
                        Text(
                            text = when {
                                isConnectionSent -> when (connection?.status) {
                                    "pending" -> "Pending"
                                    "accepted" -> "Connected"
                                    "rejected" -> "Rejected"
                                    else -> "Sent"
                                }

                                isFollowing -> "Pending"
                                else -> "Connect"
                            },
                            color = when {
                                isConnectionSent || isFollowing -> Color.Gray
                                else -> Color(0xFF8B5CF6)
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // Post Content
                if (post.content.isNotEmpty()) {
                    Column {
                        var showButton by remember { mutableStateOf(false) }

                        Box(modifier = Modifier.height(0.dp)) {
                            Text(
                                text = post.content,
                                fontSize = 12.sp,
                                maxLines = 4,
                                color = Color.Transparent,
                                lineHeight = 20.sp,
                                onTextLayout = { textLayoutResult ->
                                    showButton = textLayoutResult.hasVisualOverflow
                                }
                            )
                        }

                        Text(
                            text = post.content,
                            fontSize = 12.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                            overflow = TextOverflow.Ellipsis,
                            color = Color(0xFF4B5563),
                            lineHeight = 20.sp
                        )

                        if (showButton) {
                            Text(
                                text = if (isExpanded) "Show less" else "Show more",
                                fontSize = 14.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.clickable {
                                    isExpanded = !isExpanded
                                }
                            )
                        }
                    }
                }

                // Display Links if any
                if (links.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(links) { index, link ->
                            LinkChip(
                                link = link,
                                linkNumber = index + 1
                            )
                        }
                    }
                }

                // Post Media (Images and Videos)
                if (post.media.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    post.media.forEachIndexed { index, mediaUrl ->
                        val isVideo = isVideoUrl(mediaUrl)

                        if (isVideo) {
                            VideoPlayer(
                                videoUrl = mediaUrl,
                                videoId = "${post.id}_media_$index",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            var imageLoadState by remember {
                                mutableStateOf<ImageLoadState>(ImageLoadState.Loading)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f / 5f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (imageLoadState is ImageLoadState.Error)
                                            Color(0xFFE0E0E0)
                                        else
                                            Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (imageLoadState) {
                                    is ImageLoadState.Loading, is ImageLoadState.Success -> {
                                        AsyncImage(
                                            model = mediaUrl,
                                            contentDescription = "Post Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            onSuccess = { imageLoadState = ImageLoadState.Success },
                                            onError = { imageLoadState = ImageLoadState.Error }
                                        )
                                    }

                                    is ImageLoadState.Error -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_cross),
                                                contentDescription = "Failed to load",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Unable to load media",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (index < post.media.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // Likes and Comments count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (post.likes > 0) "${post.likes} likes" else "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (post.comments > 0) "${post.comments} Comments" else "",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LikeButton(
                        post = post,
                        text = "Like",
                        onClick = { onLikeClick(post.id) }
                    )

                    ActionButton(
                        iconRes = R.drawable.ic_comment,
                        text = "Comment",
                        onClick = { onCommentClick(post) }
                    )
                    ActionButton(
                        iconRes = R.drawable.ic_share,
                        text = "Share",
                        onClick = { onShareClick(post) }
                    )
                }
            }
        }
    }
}


@Composable
fun LikeButton(
    post: Post,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(
                    id = if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                ),
                contentDescription = "Heart",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}


@Composable
fun ActionButton(
    iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.Gray,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                color = tint,
                fontSize = 12.sp
            )
        }
    }
}




// Improved video URL detection function
fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf(".mp4", ".mov", ".avi", ".mkv", ".webm", ".3gp", ".m4v")
    val lowerUrl = url.lowercase()

    // Check file extension
    if (videoExtensions.any { lowerUrl.contains(it) }) {
        return true
    }

    // Check common video hosting patterns
    if (lowerUrl.contains("video") ||
        lowerUrl.contains(".mp4") ||
        lowerUrl.contains("cloudinary.com/video")
    ) {
        return true
    }

    return false
}

@Composable
fun LinkChip(
    link: String,
    linkNumber: Int
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Card(
        modifier = Modifier
            .wrapContentSize()
            .border(
                width = 1.dp,
                color = Color(0xFFB787F5),
                shape = RoundedCornerShape(8.dp) // Match the card shape
            )
            .clickable {
                try {
                    uriHandler.openUri(link)
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open link", Toast.LENGTH_SHORT).show()
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF7EBFD) // Light purple background
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_link2),
                contentDescription = "Link",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFB787F5) // Purple color
            )
            Text(
                text = "Link $linkNumber",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB787F5)
            )
        }
    }
}


// Helper function to extract links from text
fun extractLinksFromText(text: String): List<String> {
    val urlPattern = Regex(
        """https?://(?:[-\w.])+(?::\d+)?(?:/(?:[\w/_.])*(?:\?(?:[\w&=%.])*)?(?:#(?:\w*))?)?"""
    )
    return urlPattern.findAll(text).map { it.value }.toList()
}


sealed class ImageLoadState {
    object Loading : ImageLoadState()
    object Success : ImageLoadState()
    object Error : ImageLoadState()
}

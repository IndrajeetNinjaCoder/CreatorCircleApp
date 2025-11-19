package com.cc.creatorcircle.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Comment
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.viewModel.PostsViewModel

@Composable
fun CommentBottomSheetContent(
    post: Post,
    comments: List<Comment>,
    isLoading: Boolean,
    error: String?,
    viewModel: PostsViewModel,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var expandedComments by remember { mutableStateOf(setOf<Int>()) }
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // ✅ Input Section fixed at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            )
            {
                // Profile Image
                Image(
                    painter = painterResource(id = R.drawable.ic_profile1),
                    contentDescription = "Your Profile",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Comment Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (commentText.isEmpty()) {
                        Text(
                            text = if (replyingToComment != null) {
                                "Reply to ${replyingToComment!!.authorName}..."
                            } else {
//                                "Add a comment for ${post.author.name ?: "user"}..."
                                "Add a comment..."
                            },
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    BasicTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                Icon(
                    painter = painterResource(id = R.drawable.ic_send),
                    contentDescription = "Send Comment",
                    tint = Color(0x80A24DAF),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            if (commentText.isNotBlank()) {
                                if (replyingToComment != null) {
                                    // Add reply with parent_id
                                    viewModel.addReply(
                                        postId = post.id,
                                        content = commentText,
                                        parentId = replyingToComment!!.id
                                    )
                                } else {
                                    // Add regular comment
                                    viewModel.addComment(post.id, commentText)
                                }
                                commentText = ""
                                replyingToComment = null
                            }
                        }
                )
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (error != null) {
                    TextButton(onClick = onRefresh) {
                        Text("Retry", color = Color(0xFF8B5CF6))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reply indicator
            if (replyingToComment != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Replying to ${replyingToComment!!.authorName}",
                            fontSize = 14.sp,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel reply",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable {
                                    replyingToComment = null
                                }
                        )
                    }
                }
            }

            // Content with LazyColumn scroll
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8B5CF6))
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = error, color = Color.Red)
                    }
                }

                comments.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 80.dp), // Padding to avoid overlap with input
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                expandedComments = expandedComments,
                                onExpandToggle = { commentId ->
                                    expandedComments = if (expandedComments.contains(commentId)) {
                                        expandedComments - commentId
                                    } else {
                                        expandedComments + commentId
                                    }
                                },
                                onLikeClick = { commentId ->
                                    viewModel.toggleCommentLike(commentId)
                                },
                                onReplyClick = { comment ->
                                    replyingToComment = comment
                                },
                                nestingLevel = 0
                            )
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No comments yet", color = Color.Gray)
                    }
                }
            }
        }


    }
}

// Updated CommentItem - Now properly handles nested replies with individual expand states
@Composable
fun CommentItem(
    comment: Comment,
    expandedComments: Set<Int>,
    onExpandToggle: (Int) -> Unit,
    onLikeClick: (Int) -> Unit,
    onReplyClick: (Comment) -> Unit,
    nestingLevel: Int = 0
) {
    // Safe access to replies list
    val repliesList = comment.replies ?: emptyList()
    val isExpanded = expandedComments.contains(comment.id)

    // Calculate indentation based on nesting level (max 5 levels to prevent excessive indentation)
    val indentation = (minOf(nestingLevel, 5) * 24).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indentation)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Profile Image - smaller for nested replies
            val profileSize = if (nestingLevel > 0) 32.dp else 40.dp

            if (comment.authorAvatar != null) {
                AsyncImage(
                    model = comment.authorAvatar,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(profileSize)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile1),
                    error = painterResource(id = R.drawable.ic_profile1)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(profileSize)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile1),
                        contentDescription = "Profile",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Author name and timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (nestingLevel > 0) 14.sp else 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${comment.timestamp}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Comment content
                Text(
                    text = comment.content,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Comment actions
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reply button - Available for all comments including nested replies
                    Text(
                        text = "Reply",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.clickable {
                            onReplyClick(comment)
                        }
                    )

                    // Show replies toggle for any comment that has replies
                    if (repliesList.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(
                            text = if (isExpanded) {
                                "Hide replies"
                            } else {
                                if (repliesList.size == 1) {
                                    "View 1 reply"
                                } else {
                                    "View ${repliesList.size} replies"
                                }
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF8B5CF6),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                onExpandToggle(comment.id)
                            }
                        )
                    }
                }
            }

            // Heart icon with like count below it
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (comment.isLiked) Color.Red else Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(if (nestingLevel > 0) 20.dp else 24.dp)
                        .clickable {
                            onLikeClick(comment.id)
                        }
                )

                // Like count below the heart icon
                if (comment.likes > 0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${comment.likes}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Show replies if expanded - recursively render nested replies
        if (isExpanded && repliesList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repliesList.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        expandedComments = expandedComments, // Pass the same expanded state
                        onExpandToggle = onExpandToggle, // Pass through the toggle function
                        onLikeClick = onLikeClick, // Pass the like function to replies too
                        onReplyClick = onReplyClick, // Allows replying to replies
                        nestingLevel = nestingLevel + 1 // Increase nesting level
                    )
                }
            }
        }
    }
}


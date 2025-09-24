@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Comment
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.PostCreationState
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
import com.cc.creatorcircle.ui.components.TopBarHome
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.utils.createFileFromUri
import com.cc.creatorcircle.viewModel.ConnectionRequestState
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory

import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Add ConnectionViewModel
    val connectionViewModel: ConnectionViewModel = viewModel()

    // Add UserDataManager
    val userDataManager = remember { UserDataManager(context) }
    var userData by remember { mutableStateOf(userDataManager.getUserData()) }

    // Add the missing declarations
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var showNotification by remember { mutableStateOf(true) }
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Comment states
    val comments by viewModel.comments.collectAsState()
    val commentsLoading by viewModel.commentsLoading.collectAsState()
    val commentsError by viewModel.commentsError.collectAsState()

    // User profile states
    val userProfile by viewModel.userProfile.collectAsState()
    val profileLoading by viewModel.profileLoading.collectAsState()

    // Connection states
    val connectionRequestState by connectionViewModel.connectionRequestState.observeAsState()
    val connectionResponse by connectionViewModel.connectionResponse.observeAsState()
    val connectionLoading by connectionViewModel.isLoading.observeAsState()
    val connectionError by connectionViewModel.errorMessage.observeAsState()

    // Bottom sheet state for comments
    val commentBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    // Bottom sheet state for share
    val shareBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var selectedPostForShare by remember { mutableStateOf<Post?>(null) }

    // Handle connection success
    LaunchedEffect(connectionRequestState) {
        when (connectionRequestState) {
            is ConnectionRequestState.Success -> {
                // Show success message or handle UI update
                // You can add a snackbar or toast here
            }

            is ConnectionRequestState.Error -> {
                // Handle error - could show a snackbar
            }

            else -> {}
        }
    }

    // Update userData when userProfile changes and save to SharedPreferences

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val userId = profile.id ?: -1  // Assuming UserProfile has an id field
            val username = profile.full_name?.takeIf { it.isNotEmpty() }
                ?: profile.username
                ?: ""
            val profilePic = profile.profile_pic ?: ""

            // Save to SharedPreferences
            userDataManager.saveUserData(userId, username, profilePic)

            // Update local state
            userData = UserData(userId, username, profilePic)
        }
    }


//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            val username = profile.full_name?.takeIf { it.isNotEmpty() }
//                ?: profile.username
//                ?: ""
//            val profilePic = profile.profile_pic ?: ""
//
//            // Save to SharedPreferences
//            userDataManager.saveUserData(username, profilePic)
//
//            // Update local state
//            userData = UserData(username, profilePic)
//        }
//    }

    // FIXED: Single LaunchedEffect for initial data loading
    LaunchedEffect(Unit) {
        // Fetch feed posts (default postType is "feed")
        viewModel.fetchPosts(postType = "feed")

        // Fetch user profile only if userData is empty
        if (userData.username.isEmpty()) {
            viewModel.fetchUserProfile()
        }
    }

    // Get auth token using TokenManager
    val tokenManager = remember { TokenManager(context) }
    val token = remember { tokenManager.getToken() }

    Scaffold(
        topBar = {
            TopBarHome(
                tabs = listOf("Feed", "Resources"),
                selectedTab = "Feed",
                navController = navController,
                onTabSelected = { tab ->
                    when (tab) {
                        "Feed" -> { /* Already here */
                        }

                        "Resources" -> navController.navigate("resourcehub")
                    }
                },
                modifier = Modifier.background(Color.White)

            )

        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            GradientCreatePostButton(
                viewModel = viewModel,
                modifier = Modifier.padding(16.dp),
                username = userData.username.ifEmpty { "Loading..." },
                profilePic = userData.profilePic.takeIf { it?.isNotEmpty() == true }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // FIXED: Add pull-to-refresh functionality
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isLoading,
                onRefresh = {
                    viewModel.fetchPosts(postType = "feed")
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    item {
                        Spacer(modifier = Modifier.height(22.dp))
                        PostInputSection(
                            viewModel = viewModel,
                            focusRequester = focusRequester,
                            username = userData.username.ifEmpty { "Loading..." },
                            profilePic = userData.profilePic
                        )
                        Spacer(modifier = Modifier.height(22.dp))
                    }

                    // FIXED: Better loading state handling
                    if (isLoading && posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF8B5CF6)
                                )
                            }
                        }
                    }

                    // FIXED: Better error handling
                    if (error != null && posts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = error ?: "Unknown error",
                                        color = Color.Red,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.fetchPosts(postType = "feed")
                                        }
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }

                    // FIXED: Optimized posts rendering
                    if (posts.isNotEmpty()) {
                        items(
                            items = posts,
                            key = { post -> post.id } // Add key for better performance
                        ) { post ->
                            PostCardSection(
                                post = post,
                                userProfile = userProfile,
                                connectionViewModel = connectionViewModel,
                                onLikeClick = { postId ->
                                    viewModel.toggleLike(postId)
                                },
                                onCommentClick = { clickedPost ->
                                    selectedPost = clickedPost
                                    viewModel.fetchComments(clickedPost.id)
                                    showCommentBottomSheet = true
                                },
                                onShareClick = { clickedPost ->
                                    selectedPostForShare = clickedPost
                                    showShareBottomSheet = true
                                },
                                onConnectClick = { userId ->
                                    connectionViewModel.sendConnectionRequest(userId)
                                }
                            )
                        }
                    } else if (!isLoading && error == null) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No posts available",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.fetchPosts(postType = "feed")
                                        }
                                    ) {
                                        Text("Refresh")
                                    }
                                }
                            }
                        }
                    }
                }

                // Pull refresh indicator
                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF8B5CF6)
                )
            }
        }

        // Comment Bottom Sheet
        if (showCommentBottomSheet && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showCommentBottomSheet = false
                    selectedPost = null
                    viewModel.clearComments()
                },
                sheetState = commentBottomSheetState,
                modifier = Modifier.fillMaxSize()
            ) {
                CommentBottomSheetContent(
                    post = selectedPost!!,
                    comments = comments,
                    isLoading = commentsLoading,
                    error = commentsError,
                    viewModel = viewModel,
                    onDismiss = {
                        showCommentBottomSheet = false
                        selectedPost = null
                        viewModel.clearComments()
                    },
                    onRefresh = {
                        selectedPost?.let { post ->
                            viewModel.fetchComments(post.id)
                        }
                    }
                )
            }
        }

        // Share Bottom Sheet
        selectedPostForShare?.let { post ->
            if (showShareBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showShareBottomSheet = false
                        selectedPostForShare = null
                    },
                    sheetState = shareBottomSheetState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ShareBottomSheetContent(
                        post = post,
                        context = context,
                        onDismiss = {
                            showShareBottomSheet = false
                            selectedPostForShare = null
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun PostCardSection(
    post: Post,
    userProfile: UserProfile?,
    connectionViewModel: ConnectionViewModel,
    onLikeClick: (String) -> Unit = {},
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Post) -> Unit = {},
    onConnectClick: (Int) -> Unit = {}
) {

    // State to track if full content is shown
    var isExpanded by remember { mutableStateOf(false) }

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

    // Determine button text and state based on connection status
    val buttonText = when {
        isConnectionSent -> when (connection?.status) {
            "pending" -> "Pending"
            "accepted" -> "Connected"
            "rejected" -> "Rejected"
            else -> "Sent"
        }

        isFollowing -> "Pending"  // Show "Pending" if following but not connected
        else -> "Connect"
    }

    val buttonEnabled = !connectionLoading && !isConnectionSent && !isFollowing

    // Extract links from post content
    val links = extractLinksFromText(post.content)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
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
                    placeholder = painterResource(id = R.drawable.ic_profile),
                    error = painterResource(id = R.drawable.ic_profile)
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
//                Spacer(modifier = Modifier.height(2.dp))
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

            // Connect Button (only show if not author and not already connected)
            if (!post.isAuthor && !isAlreadyConnected) {
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
            modifier = Modifier
                .fillMaxWidth(),
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
                        Text(
                            text = post.content,
                            fontSize = 14.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.Black,
                            lineHeight = 20.sp
                        )

                        // Check if text actually overflows 2 lines by measuring
                        var showButton by remember { mutableStateOf(false) }

                        Text(
                            text = post.content,
                            fontSize = 14.sp,
                            maxLines = 2,
                            color = Color.Transparent,
                            lineHeight = 20.sp,
                            onTextLayout = { textLayoutResult ->
                                showButton = textLayoutResult.hasVisualOverflow
                            }
                        )

                        if (showButton) {
                            Text(
                                text = if (isExpanded) "Show less" else "Show more",
                                fontSize = 14.sp,
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
//                                    .padding(top = 4.dp)
                                    .clickable {
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
                                // Create unique videoId using post ID and media index
                                videoId = "${post.id}_media_$index",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(9f / 16f) // 9:16 aspect ratio
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = "Post Image",
                                modifier = Modifier
                                    .fillMaxWidth()
//                                    .aspectRatio(9f / 16f) // Changed from fixed height to 9:16 aspect ratio
                                    .aspectRatio(4f / 5f) // Changed from fixed height to 9:16 aspect ratio
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.ic_post_image),
                                error = painterResource(id = R.drawable.ic_post_image)
                            )
                        }

                        if (index < post.media.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

//                Spacer(modifier = Modifier.height(12.dp))

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

//                Spacer(modifier = Modifier.height(12.dp))

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

                    // Updated ActionButton calls
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

// Alternative regex pattern that's more comprehensive
fun extractLinksFromTextAdvanced(text: String): List<String> {
    val patterns = listOf(
        // HTTP/HTTPS URLs
        """https?://[^\s<>"{}|\\^`\[\]]+""",
        // www URLs
        """www\.[^\s<>"{}|\\^`\[\]]+""",
        // Domain.com URLs
        """[a-zA-Z0-9][a-zA-Z0-9-]{1,61}[a-zA-Z0-9]\.[a-zA-Z]{2,}"""
    )

    val allLinks = mutableSetOf<String>()

    patterns.forEach { pattern ->
        val regex = Regex(pattern)
        regex.findAll(text).forEach { match ->
            var link = match.value.trim()
            // Add protocol if missing
            if (!link.startsWith("http://") && !link.startsWith("https://")) {
                link = "https://$link"
            }
            allLinks.add(link)
        }
    }

    return allLinks.toList()
}


@Composable
fun IconButton(
    icon: ImageVector,
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
                imageVector = icon,
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


@Composable
fun HeartIcon(
    isLiked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(Color(0xCC7921A4), Color(0xFFD6559D)),
        start = Offset(0f, 0f),
        end = Offset(100f, 100f)
    )

    if (isLiked) {
        Icon(
            painter = painterResource(id = R.drawable.ic_heart_filled),
            contentDescription = "Unlike",
            tint = Color.Unspecified,
            modifier = modifier
                .size(18.dp)
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = gradientBrush,
                        blendMode = BlendMode.SrcIn
                    )
                }
                .clickable { onClick() }
        )
    } else {
        Icon(
            painter = painterResource(id = R.drawable.ic_heart),
            contentDescription = "Like",
            tint = Color.Gray,
            modifier = modifier
                .size(18.dp)
                .clickable { onClick() }
        )
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


// Usage example for Instagram-like feed
@Composable
fun VideoFeedItem(
    videoUrl: String,
    isVisible: Boolean = true,
    onLike: () -> Unit = {},
    onComment: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    var isMuted by remember { mutableStateOf(true) }
    var isLiked by remember { mutableStateOf(false) }

    VideoPlayer(
        videoUrl = videoUrl,
        autoPlay = isVisible,
        isMuted = isMuted,
        onMuteToggle = { isMuted = it },
        onDoubleTap = {
            isLiked = true
            onLike()
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f) // Instagram story/reel ratio
    )
}

// Helper function to determine if URL is a video
private fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf("mp4", "avi", "mov", "mkv", "3gp", "webm", "m4v", "wmv", "flv")
    val extension = url.substringAfterLast('.', "").lowercase()
    return videoExtensions.contains(extension)
}


@Composable
fun ShareBottomSheetContent(
    post: Post,
    context: Context,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Mock contact data - replace with actual contact data
    val contacts = remember {
        listOf(
            Contact("Nisha___119", R.drawable.ic_profile),
            Contact("Nisha___119", R.drawable.ic_profile),
            Contact("Nisha___119", R.drawable.ic_profile),
            Contact("Nisha___119", R.drawable.ic_profile),
            Contact("Nisha___119", R.drawable.ic_profile),
            Contact("Nisha___119", R.drawable.ic_profile)
        )
    }

    val filteredContacts = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            contacts
        } else {
            contacts.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        Color.Gray.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }

        // Title
        Text(
            text = "Share",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search", color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color(0xFFF5F5F5),
                unfocusedContainerColor = Color(0xFFF5F5F5)
            ),
            singleLine = true
        )

        // Contacts Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 16.dp)
        ) {
            items(filteredContacts) { contact ->
                ContactItem(
                    contact = contact,
                    onContactClick = { selectedContact ->
                        // Handle sharing to specific contact
                        shareToContact(context, post, selectedContact.name)
                        onDismiss()
                    }
                )
            }
        }

        // External Sharing Options
        LazyRow(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            item {
                ExternalShareOption(
                    icon = R.drawable.ic_whatsapp, // You'll need to add this drawable
                    label = "WhatsApp",
                    backgroundColor = Color(0xFF25D366),
                    onClick = {
                        shareToWhatsApp(context, post)
                        onDismiss()
                    }
                )
            }
            item { Spacer(modifier = Modifier.width(24.dp)) }
            item {
                ExternalShareOption(
                    icon = R.drawable.ic_gmail, // You'll need to add this drawable
                    label = "Mail",
                    backgroundColor = Color(0xFFEA4335),
                    onClick = {
                        shareToEmail(context, post)
                        onDismiss()
                    }
                )
            }
            item { Spacer(modifier = Modifier.width(24.dp)) }
            item {
                ExternalShareOption(
                    icon = Icons.Default.Share,
                    label = "Share",
                    backgroundColor = Color.Gray,
                    onClick = {
                        shareToOtherApps(context, post)
                        onDismiss()
                    }
                )
            }
            item { Spacer(modifier = Modifier.width(24.dp)) }
            item {
                ExternalShareOption(
                    icon = Icons.Default.Link,
                    label = "Copy link",
                    backgroundColor = Color.Gray,
                    onClick = {
                        copyLinkToClipboard(context, post)
                        onDismiss()
                    }
                )
            }
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (index == 0) Color(0xFF8B5CF6) else Color.Gray.copy(alpha = 0.3f),
                            CircleShape
                        )
                )
                if (index < 3) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onContactClick: (Contact) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onContactClick(contact) }
            .padding(8.dp)
    ) {
        AsyncImage(
            model = contact.avatarRes, // Use the resource ID
            contentDescription = contact.name,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.ic_profile),
            error = painterResource(id = R.drawable.ic_profile)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = contact.name,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ExternalShareOption(
    icon: Any, // Can be ImageVector or Int (drawable resource)
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                is Int -> {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

// Data class for contacts
data class Contact(
    val name: String,
    val avatarRes: Int // Resource ID for avatar
)


// Sharing functions
private fun shareToContact(context: Context, post: Post, contactName: String) {
    // Implement sharing to specific contact
    val shareText = "Check out this post: ${post.content}"
    Toast.makeText(context, "Sharing to $contactName", Toast.LENGTH_SHORT).show()
}

private fun shareToWhatsApp(context: Context, post: Post) {
    try {
        val shareText = "Check out this post: ${post.content}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.whatsapp")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        shareToOtherApps(context, post)
    }
}

private fun shareToEmail(context: Context, post: Post) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Shared Post")
        putExtra(Intent.EXTRA_TEXT, "Check out this post: ${post.content}")
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Send email"))
    } catch (e: Exception) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}

private fun shareToOtherApps(context: Context, post: Post) {
    val shareText = "Check out this post: ${post.content}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Share via"))
    } catch (e: Exception) {
        Toast.makeText(context, "No apps available for sharing", Toast.LENGTH_SHORT).show()
    }
}

private fun copyLinkToClipboard(context: Context, post: Post) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Post Link", "https://yourapp.com/post/${post.id}")
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
}


// Updated CommentBottomSheetContent
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
                    painter = painterResource(id = R.drawable.ic_profile),
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
                    placeholder = painterResource(id = R.drawable.ic_profile),
                    error = painterResource(id = R.drawable.ic_profile)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(profileSize)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_profile),
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
fun GradientCreatePostButton(
    modifier: Modifier = Modifier,
    viewModel: PostsViewModel,
    username: String,
    profilePic: String?
) {
    var showPopup by remember { mutableStateOf(false) }

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
            .clickable { showPopup = true }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Create post", color = Color.White)
        }
    }

    // Show popup when showPopup is true
    if (showPopup) {
        PostInputPopupDialog(
            viewModel = viewModel,
            username = username,
            profilePic = profilePic,
            onDismiss = { showPopup = false }
        )
    }
}

@Composable
fun PostInputSection(
    viewModel: PostsViewModel,
    focusRequester: FocusRequester = remember { FocusRequester() },
    username: String,
    profilePic: String?
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    val postCreationState by viewModel.postCreationState.collectAsState()
    val selectedMediaFiles by viewModel.selectedMediaFiles.collectAsState()

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Convert URI to File
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addMediaFile(it) }
        }
    }

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // Convert URI to File
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addMediaFile(it) }
        }
    }

    // Handle post creation state
    LaunchedEffect(postCreationState) {
        when (postCreationState) {
            is PostCreationState.Success -> {
                text = "" // Clear text on success
                showPopup = false // Close popup on success
            }

            is PostCreationState.Error -> {
                // Handle error (show toast, snackbar, etc.)
            }

            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row {
            // Show user profile picture or default image
            if (profilePic?.isNotEmpty() == true) {
                AsyncImage(
                    model = profilePic,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.ic_profile),
                    error = painterResource(id = R.drawable.ic_profile),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            }

            // Show username passed from parent
            Text(
                text = username,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clickable box that opens popup instead of being editable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
                .clickable {
                    showPopup = true
                    println("Clicked - showPopup: $showPopup") // Debug log
                } // Open popup on click
        ) {
            Text(
                text = "Share your thoughts",
                color = Color(0xFFAAAAAA),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }

        // Show selected media files with proper image/video preview
        if (selectedMediaFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(selectedMediaFiles.size) { index ->
                    val file = selectedMediaFiles[index]
                    Card(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(80.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Show preview based on file type
                            when {
                                file.extension.lowercase() in listOf(
                                    "jpg",
                                    "jpeg",
                                    "png",
                                    "gif"
                                ) -> {
                                    // Image preview
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                file.extension.lowercase() in listOf(
                                    "mp4",
                                    "avi",
                                    "mov",
                                    "mkv",
                                    "3gp",
                                    "webm"
                                ) -> {
                                    // Video preview with play icon
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // You can use a video thumbnail library like Glide or Coil for better video thumbnails
                                        // For now, showing a play icon
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play), // You'll need this icon
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }

                                else -> {
                                    // Show file extension for other files
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = file.extension.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Close button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                                    .clickable { viewModel.removeMediaFile(file) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AttachmentButton(
                icon = R.drawable.ic_image,
                text = "Image",
                onClick = {
                    imagePickerLauncher.launch("image/*")
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            AttachmentButton(
                icon = R.drawable.ic_video,
                text = "Video",
                onClick = {
                    videoPickerLauncher.launch("video/*")
                }
            )
        }

        // Show error message if any
        val currentState = postCreationState
        if (currentState is PostCreationState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentState.message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.clickable { viewModel.clearError() }
            )
        }
    }

    // Show popup when showPopup is true
    if (showPopup) {
        PostInputPopupDialog(
            viewModel = viewModel,
            username = username,
            profilePic = profilePic,
            onDismiss = { showPopup = false }
        )
    }
}


@Composable
fun PostInputPopupDialog(
    viewModel: PostsViewModel,
    username: String,
    profilePic: String?,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenHeight * 4f / 5f) // 2/3 of screen height
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with user info and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color(0xFFE0E0E0),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // You can replace this with actual user image
                            AsyncImage(
                                model = profilePic,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = username, // Replace with actual user name
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                text = "Member",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    // Close button
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFFF5F5F5),
                                CircleShape
                            )
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF666666)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Use the updated PostInputPopup composable

                PostInputPopup(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )


            }
        }
    }
}


@Composable
fun PostInputPopup(
    viewModel: PostsViewModel,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val postCreationState by viewModel.postCreationState.collectAsState()
    val selectedMediaFiles by viewModel.selectedMediaFiles.collectAsState()

    // Auto-focus when popup opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addMediaFile(it) }
        }
    }

    // Video picker launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addMediaFile(it) }
        }
    }

    // Handle post creation state
    LaunchedEffect(postCreationState) {
        when (postCreationState) {
            is PostCreationState.Success -> {
                text = ""
            }

            is PostCreationState.Error -> {
                // Handle error
            }

            else -> {}
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Text input area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 100.dp)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(8.dp)

        ) {
            if (text.isEmpty()) {
                Text(
                    text = "What do you want to talk about?",
                    color = Color(0xFFAAAAAA),
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )
        }

        // Show selected media files
        if (selectedMediaFiles.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedMediaFiles.size) { index ->
                    val file = selectedMediaFiles[index]
                    Card(
                        modifier = Modifier.size(80.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            when {
                                file.extension.lowercase() in listOf(
                                    "jpg",
                                    "jpeg",
                                    "png",
                                    "gif"
                                ) -> {
                                    AsyncImage(
                                        model = file,
                                        contentDescription = "Selected image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                file.extension.lowercase() in listOf(
                                    "mp4",
                                    "avi",
                                    "mov",
                                    "mkv",
                                    "3gp",
                                    "webm"
                                ) -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF000000)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_play),
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFF5F5F5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = file.extension.uppercase(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // Remove button
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        CircleShape
                                    )
                                    .clickable { viewModel.removeMediaFile(file) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "×",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bottom section with attachments and post button
        Column {
            // Attachment buttons and Post button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Attachment buttons
                Row(
                    horizontalArrangement = Arrangement.Start
                ) {
                    AttachmentButton(
                        icon = R.drawable.ic_image,
                        text = "Photo",
                        iconTint = Color(0xFF1DA1F2),
                        hasTextInput = text.isNotBlank(),
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    AttachmentButton(
                        icon = R.drawable.ic_video,
                        text = "Video",
                        iconTint = Color(0xFF00C851),
                        hasTextInput = text.isNotBlank(),
                        onClick = {
                            videoPickerLauncher.launch("video/*")
                        }
                    )
                }

                // Right side - Post button
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            enabled = postCreationState !is PostCreationState.Loading &&
                                    (text.isNotBlank() || selectedMediaFiles.isNotEmpty())
                        ) {
                            if (text.isNotBlank() || selectedMediaFiles.isNotEmpty()) {
                                viewModel.createPost(text, postType = "feed")
                            }
                        }
                ) {
                    if (postCreationState is PostCreationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Post",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Show error message if any
        val currentState = postCreationState
        if (currentState is PostCreationState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentState.message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.clickable { viewModel.clearError() }
            )
        }
    }
}


@Composable
fun AttachmentButton(
    icon: Int,
    text: String,
    iconTint: Color = Color.Gray,
    hasTextInput: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .background(
                color = if (hasTextInput) Color(0xFFF0F8FF) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp),
//            .padding(horizontal = if (hasTextInput) 12.dp else 0.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (hasTextInput) Color(0xFF333333) else Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    viewModel.fetchPosts()

    // Add ConnectionViewModel
    val connectionViewModel: ConnectionViewModel = viewModel()

    // Add UserDataManager
    val userDataManager = remember { UserDataManager(context) }
    var userData by remember { mutableStateOf(userDataManager.getUserData()) }

    // Add the missing declarations
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var showNotification by remember { mutableStateOf(true) }
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Comment states
    val comments by viewModel.comments.collectAsState()
    val commentsLoading by viewModel.commentsLoading.collectAsState()
    val commentsError by viewModel.commentsError.collectAsState()

    // User profile states
    val userProfile by viewModel.userProfile.collectAsState()
    val profileLoading by viewModel.profileLoading.collectAsState()

    // Connection states
    val connectionRequestState by connectionViewModel.connectionRequestState.observeAsState()
    val connectionResponse by connectionViewModel.connectionResponse.observeAsState()
    val connectionLoading by connectionViewModel.isLoading.observeAsState()
    val connectionError by connectionViewModel.errorMessage.observeAsState()

    // Bottom sheet state for comments
    val commentBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    // Bottom sheet state for share
    val shareBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var selectedPostForShare by remember { mutableStateOf<Post?>(null) }

    // Handle connection success
    LaunchedEffect(connectionRequestState) {
        when (connectionRequestState) {
            is ConnectionRequestState.Success -> {
                // Show success message or handle UI update
                // You can add a snackbar or toast here
            }
            is ConnectionRequestState.Error -> {
                // Handle error - could show a snackbar
            }
            else -> {}
        }
    }

    // Update userData when userProfile changes and save to SharedPreferences
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val username = profile.full_name?.takeIf { it.isNotEmpty() }
                ?: profile.username
                ?: ""
            val profilePic = profile.profile_pic ?: ""

            // Save to SharedPreferences
            userDataManager.saveUserData(username, profilePic)

            // Update local state
            userData = UserData(username, profilePic)
        }
    }

    // Fetch user profile when HomeScreen is first created (only if userData is empty)
    LaunchedEffect(Unit) {
        if (userData.username.isEmpty()) {
            viewModel.fetchUserProfile()
        }
    }

    // Get auth token using TokenManager
    val tokenManager = remember { TokenManager(context) }
    val token = remember { tokenManager.getToken() }

    Scaffold(
        topBar = { TopBarHome(tabs = listOf("Feed", "Resources"), selectedTab = "Feed", navController) },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            GradientCreatePostButton(
                viewModel = viewModel,
                modifier = Modifier.padding(16.dp),
                username = userData.username.ifEmpty { "Loading..." },
                profilePic = userData.profilePic.takeIf { it?.isNotEmpty() == true }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                item {
                    Spacer(modifier = Modifier.height(22.dp))
                    PostInputSection(
                        viewModel = viewModel,
                        focusRequester = focusRequester,
                        username = userData.username.ifEmpty { "Loading..." },
                        profilePic = userData.profilePic
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                }

                // Loading state
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // Error state
                if (error != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = error ?: "Unknown error",
                                    color = Color.Red,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.refreshPosts()
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                // Posts with like, share, and connection functionality
                if (posts.isNotEmpty()) {
                    items(posts) { post ->
                        PostCardSection(
                            post = post,
                            userProfile = userProfile,
                            connectionViewModel = connectionViewModel,
                            onLikeClick = { postId ->
                                viewModel.toggleLike(postId)
                            },
                            onCommentClick = { clickedPost ->
                                selectedPost = clickedPost
                                viewModel.fetchComments(clickedPost.id)
                                showCommentBottomSheet = true
                            },
                            onShareClick = { clickedPost ->
                                selectedPostForShare = clickedPost
                                showShareBottomSheet = true
                            },
                            onConnectClick = { userId ->
                                connectionViewModel.sendConnectionRequest(userId)
                            }
                        )
                    }
                } else if (!isLoading && error == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts available",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Comment Bottom Sheet
        if (showCommentBottomSheet && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showCommentBottomSheet = false
                    selectedPost = null
                    viewModel.clearComments()
                },
                sheetState = commentBottomSheetState,
                modifier = Modifier.fillMaxSize()
            ) {
                CommentBottomSheetContent(
                    post = selectedPost!!,
                    comments = comments,
                    isLoading = commentsLoading,
                    error = commentsError,
                    viewModel = viewModel,
                    onDismiss = {
                        showCommentBottomSheet = false
                        selectedPost = null
                        viewModel.clearComments()
                    },
                    onRefresh = {
                        selectedPost?.let { post ->
                            viewModel.fetchComments(post.id)
                        }
                    }
                )
            }
        }

        // Share Bottom Sheet
        selectedPostForShare?.let { post ->
            if (showShareBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showShareBottomSheet = false
                        selectedPostForShare = null
                    },
                    sheetState = shareBottomSheetState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ShareBottomSheetContent(
                        post = post,
                        context = context,
                        onDismiss = {
                            showShareBottomSheet = false
                            selectedPostForShare = null
                        }
                    )
                }
            }
        }
    }
}
*/


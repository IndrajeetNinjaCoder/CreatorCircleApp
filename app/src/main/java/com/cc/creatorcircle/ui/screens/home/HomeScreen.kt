@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.PostDeletionState
import com.cc.creatorcircle.data.models.PostUpdateState
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
import com.cc.creatorcircle.ui.components.TopBarHome
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.viewModel.ConnectionRequestState
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("HomeScreen", "HomeScreen")
    }

    val connectionViewModel: ConnectionViewModel = viewModel()
    val userDataManager = remember { UserDataManager(context) }
    var userData by remember { mutableStateOf(userDataManager.getUserData()) }

    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    // Track scroll depth for analytics
    LaunchedEffect(lazyListState.firstVisibleItemIndex) {
        if (lazyListState.firstVisibleItemIndex > 0 && lazyListState.firstVisibleItemIndex % 5 == 0) {
            FirebaseAnalyticsHelper.logFeedScrolled(lazyListState.firstVisibleItemIndex)
        }
    }

    var showNotification by remember { mutableStateOf(true) }
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val comments by viewModel.comments.collectAsState()
    val commentsLoading by viewModel.commentsLoading.collectAsState()
    val commentsError by viewModel.commentsError.collectAsState()

    val userProfile by viewModel.userProfile.collectAsState()
    val profileLoading by viewModel.profileLoading.collectAsState()

    val connectionRequestState by connectionViewModel.connectionRequestState.observeAsState()
    val connectionResponse by connectionViewModel.connectionResponse.observeAsState()
    val connectionLoading by connectionViewModel.isLoading.observeAsState()
    val connectionError by connectionViewModel.errorMessage.observeAsState()

    val commentBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    val shareBottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var selectedPostForShare by remember { mutableStateOf<Post?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }

    val postUpdateState by viewModel.postUpdateState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    val postDeletionState by viewModel.postDeletionState.collectAsState()

    // Handle post update state with Firebase tracking
    LaunchedEffect(postUpdateState) {
        when (postUpdateState) {
            is PostUpdateState.Success -> {
                postToEdit?.let { post ->
                    FirebaseAnalyticsHelper.logPostUpdated(
                        postId = post.id,
                        fieldsUpdated = "content_and_media"
                    )
                }
                showEditDialog = false
                postToEdit = null
                viewModel.resetUpdateState()
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
            }
            is PostUpdateState.Error -> {
                (postUpdateState as? PostUpdateState.Error)?.let { errorState ->
                    FirebaseAnalyticsHelper.logError(
                        errorType = "post_update_error",
                        errorMessage = errorState.message,
                        context = "HomeScreen"
                    )
                }
            }
            else -> {}
        }
    }

    // Handle post deletion state with Firebase tracking
    LaunchedEffect(postDeletionState) {
        when (postDeletionState) {
            is PostDeletionState.Success -> {
                postToDelete?.let { post ->
                    FirebaseAnalyticsHelper.logPostDeleted(
                        postId = post.id,
                        authorId = post.author.id
                    )
                }
                showDeleteDialog = false
                postToDelete = null
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
            }
            is PostDeletionState.Error -> {
                (postDeletionState as? PostDeletionState.Error)?.let { errorState ->
                    FirebaseAnalyticsHelper.logError(
                        errorType = "post_deletion_error",
                        errorMessage = errorState.message,
                        context = "HomeScreen"
                    )
                }
            }
            else -> {}
        }
    }

    // Handle connection success with Firebase tracking
    LaunchedEffect(connectionRequestState) {
        when (val state = connectionRequestState) {
            is ConnectionRequestState.Success -> {
                FirebaseAnalyticsHelper.logConnectionRequestSuccess(
                    targetUserId = state.connectionResponse.connection.userId
                )
            }
            is ConnectionRequestState.Error -> {
                FirebaseAnalyticsHelper.logConnectionRequestError(
                    targetUserId = -1,
                    errorMessage = state.message
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val userId = profile.id ?: -1
            val username = profile.full_name?.takeIf { it.isNotEmpty() }
                ?: profile.username
                ?: ""
            val profilePic = profile.profile_pic ?: ""

            userDataManager.saveUserData(userId, username, profilePic)
            userData = UserData(userId, username, profilePic)

            // Set user properties for Firebase
            FirebaseAnalyticsHelper.setUserId(userId.toString())
            FirebaseAnalyticsHelper.setUserProperty("username", username)
        }
    }

    // Track feed loading time
    val feedLoadStartTime = remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        feedLoadStartTime.value = System.currentTimeMillis()
        viewModel.fetchPosts(postType = "feed")

        if (userData.username.isEmpty()) {
            viewModel.fetchUserProfile()
        }
    }

    // Track successful feed load
    LaunchedEffect(posts, isLoading) {
        if (!isLoading && posts.isNotEmpty()) {
            val loadTime = System.currentTimeMillis() - feedLoadStartTime.value
            FirebaseAnalyticsHelper.logFeedLoaded(
                postCount = posts.size,
                loadTimeMs = loadTime
            )
        }
    }

    // Track feed errors
    LaunchedEffect(error) {
        error?.let {
            FirebaseAnalyticsHelper.logFeedError(it)
        }
    }

    val tokenManager = remember { TokenManager(context) }
    val token = remember { tokenManager.getToken() }

    Scaffold(
        topBar = {
            TopBarHome(
                tabs = listOf("Feed", "Resources", "Connections"),
                selectedTab = "Feed",
                navController = navController,
                onTabSelected = { tab ->
                    FirebaseAnalyticsHelper.logTabSelected(tab)
                    when (tab) {
                        "Feed" -> { /* Already here */ }
                        "Resources" -> navController.navigate("resourcehub")
                        "Connections" -> navController.navigate("connections")
                    }
                },
                modifier = Modifier.background(Color.White)
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF893BCF),
                                    Color(0xFFEA3BA1)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            FirebaseAnalyticsHelper.logMessageIconClicked()
                            navController.navigate(Screen.MessageConnections.route)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Messages",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                GradientCreatePostButton(
                    viewModel = viewModel,
                    modifier = Modifier,
                    username = userData.username.ifEmpty { "Loading..." },
                    profilePic = userData.profilePic.takeIf { it?.isNotEmpty() == true }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = isLoading,
                onRefresh = {
                    FirebaseAnalyticsHelper.logFeedRefreshed(manual = true)
                    feedLoadStartTime.value = System.currentTimeMillis()
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
                                            FirebaseAnalyticsHelper.logFeatureUsed("retry_feed_load")
                                            viewModel.fetchPosts(postType = "feed")
                                        }
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }

                    if (posts.isNotEmpty()) {
                        items(
                            items = posts,
                            key = { post -> post.id }
                        ) { post ->
                            // Track post view
                            LaunchedEffect(post.id) {
                                FirebaseAnalyticsHelper.logPostViewed(
                                    postId = post.id,
                                    authorId = post.author.id,
                                    postType = "feed"
                                )
                            }

                            PostCardSection(
                                post = post,
                                userProfile = userProfile,
                                connectionViewModel = connectionViewModel,
                                postsViewModel = viewModel,
                                onLikeClick = { postId ->
                                    viewModel.toggleLike(postId)
                                },
                                onCommentClick = { clickedPost ->
                                    FirebaseAnalyticsHelper.logCommentClick(
                                        postId = clickedPost.id,
                                        authorId = clickedPost.author.id,
                                        commentCount = clickedPost.comments
                                    )
                                    selectedPost = clickedPost
                                    viewModel.fetchComments(clickedPost.id)
                                    showCommentBottomSheet = true
                                },
                                onShareClick = { clickedPost ->
                                    FirebaseAnalyticsHelper.logShareClick(
                                        postId = clickedPost.id,
                                        authorId = clickedPost.author.id
                                    )
                                    selectedPostForShare = clickedPost
                                    showShareBottomSheet = true
                                },
                                onConnectClick = { userId ->
                                    FirebaseAnalyticsHelper.logConnectionRequestSent(
                                        targetUserId = userId,
                                        source = "post"
                                    )
                                    connectionViewModel.sendConnectionRequest(userId)
                                },
                                onEditClick = { post ->
                                    FirebaseAnalyticsHelper.logDialogOpened("edit_post", post.id)
                                    postToEdit = post
                                    viewModel.initializePostForEdit(post.media)
                                    showEditDialog = true
                                },
                                onViewPostClick = { post ->
                                    FirebaseAnalyticsHelper.logFeatureUsed("view_post_detail", "home_feed")
                                },
                                onDeleteClick = { post ->
                                    FirebaseAnalyticsHelper.logDialogOpened("delete_post", post.id)
                                    postToDelete = post
                                    showDeleteDialog = true
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
                                            FirebaseAnalyticsHelper.logFeatureUsed("refresh_empty_feed")
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

                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF8B5CF6)
                )
            }
        }

        // Comment Bottom Sheet with Firebase tracking
        if (showCommentBottomSheet && selectedPost != null) {
            LaunchedEffect(Unit) {
                FirebaseAnalyticsHelper.logDialogOpened("comment_sheet", selectedPost?.id)
            }

            ModalBottomSheet(
                onDismissRequest = {
                    FirebaseAnalyticsHelper.logDialogClosed("comment_sheet", "dismissed")
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
                        FirebaseAnalyticsHelper.logDialogClosed("comment_sheet", "closed")
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

        // Share Bottom Sheet with Firebase tracking
        selectedPostForShare?.let { post ->
            if (showShareBottomSheet) {
                LaunchedEffect(Unit) {
                    FirebaseAnalyticsHelper.logDialogOpened("share_sheet", post.id)
                }

                ModalBottomSheet(
                    onDismissRequest = {
                        FirebaseAnalyticsHelper.logDialogClosed("share_sheet", "dismissed")
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
                            FirebaseAnalyticsHelper.logDialogClosed("share_sheet", "closed")
                            showShareBottomSheet = false
                            selectedPostForShare = null
                        }
                    )
                }
            }
        }

        // Edit Post Dialog with Firebase tracking
        if (showEditDialog && postToEdit != null) {
            EditPostDialog(
                post = postToEdit!!,
                viewModel = viewModel,
                username = userData.username.ifEmpty { "Loading..." },
                profilePic = userData.profilePic,
                onDismiss = {
                    FirebaseAnalyticsHelper.logDialogClosed("edit_post", "cancelled")
                    showEditDialog = false
                    postToEdit = null
                    viewModel.resetUpdateState()
                }
            )
        }

        // Delete Dialog with Firebase tracking
        if (showDeleteDialog && postToDelete != null) {
            LaunchedEffect(Unit) {
                FirebaseAnalyticsHelper.logDialogOpened("delete_confirmation", postToDelete?.id)
            }

            // Your existing delete dialog code here
            // Add Firebase tracking for dialog close with appropriate action
        }
    }
}



















//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.cc.creatorcircle.ui.screens.home
//
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.aspectRatio
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.material.AlertDialog
//import androidx.compose.material.ExperimentalMaterialApi
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.FavoriteBorder
//import androidx.compose.material.icons.filled.Message
//import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material.pullrefresh.PullRefreshIndicator
//import androidx.compose.material.pullrefresh.pullRefresh
//import androidx.compose.material.pullrefresh.rememberPullRefreshState
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FabPosition
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.rememberModalBottomSheetState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalUriHandler
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.data.models.Comment
//import com.cc.creatorcircle.data.models.Post
//import com.cc.creatorcircle.data.models.PostCreationState
//import com.cc.creatorcircle.data.models.PostDeletionState
//import com.cc.creatorcircle.data.models.PostUpdateState
//import com.cc.creatorcircle.data.models.UserProfile
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
//import com.cc.creatorcircle.ui.components.TopBarHome
//import com.cc.creatorcircle.ui.navigation.Screen
//import com.cc.creatorcircle.utils.TokenManager
//import com.cc.creatorcircle.utils.UserData
//import com.cc.creatorcircle.utils.UserDataManager
//import com.cc.creatorcircle.utils.createFileFromUri
//import com.cc.creatorcircle.viewModel.ConnectionRequestState
//import com.cc.creatorcircle.viewModel.ConnectionViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
//@Composable
//fun HomeScreen(
//    navController: NavController
//) {
//    val context = LocalContext.current
//    val viewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    // Add ConnectionViewModel
//    val connectionViewModel: ConnectionViewModel = viewModel()
//
//    // Add UserDataManager
//    val userDataManager = remember { UserDataManager(context) }
//    var userData by remember { mutableStateOf(userDataManager.getUserData()) }
//
//    // Add the missing declarations
//    val focusRequester = remember { FocusRequester() }
//    val coroutineScope = rememberCoroutineScope()
//    val lazyListState = rememberLazyListState()
//
//    var showNotification by remember { mutableStateOf(true) }
//    val posts by viewModel.posts.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//
//    // Comment states
//    val comments by viewModel.comments.collectAsState()
//    val commentsLoading by viewModel.commentsLoading.collectAsState()
//    val commentsError by viewModel.commentsError.collectAsState()
//
//    // User profile states
//    val userProfile by viewModel.userProfile.collectAsState()
//    val profileLoading by viewModel.profileLoading.collectAsState()
//
//    // Connection states
//    val connectionRequestState by connectionViewModel.connectionRequestState.observeAsState()
//    val connectionResponse by connectionViewModel.connectionResponse.observeAsState()
//    val connectionLoading by connectionViewModel.isLoading.observeAsState()
//    val connectionError by connectionViewModel.errorMessage.observeAsState()
//
//    // Bottom sheet state for comments
//    val commentBottomSheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = false
//    )
//    var showCommentBottomSheet by remember { mutableStateOf(false) }
//    var selectedPost by remember { mutableStateOf<Post?>(null) }
//
//    // Bottom sheet state for share
//    val shareBottomSheetState = rememberModalBottomSheetState(
//        skipPartiallyExpanded = true
//    )
//    var showShareBottomSheet by remember { mutableStateOf(false) }
//    var selectedPostForShare by remember { mutableStateOf<Post?>(null) }
//
//
//    // Add this state in HomeScreen composable (inside HomeScreen function)
//    var showEditDialog by remember { mutableStateOf(false) }
//    var postToEdit by remember { mutableStateOf<Post?>(null) }
//
//// Observe post update state
//    val postUpdateState by viewModel.postUpdateState.collectAsState()
//
//
//    // DELETE DIALOG STATE - Add this
//    var showDeleteDialog by remember { mutableStateOf(false) }
//    var postToDelete by remember { mutableStateOf<Post?>(null) }
//
//    // Observe post deletion state
//    val postDeletionState by viewModel.postDeletionState.collectAsState()
//
//    // Handle post update state
//    LaunchedEffect(postUpdateState) {
//        when (postUpdateState) {
//            is PostUpdateState.Success -> {
//                showEditDialog = false
//                postToEdit = null
//                viewModel.resetUpdateState()
//                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
//            }
//
//            is PostUpdateState.Error -> {
//                // Error is shown in the dialog
//            }
//
//            else -> {}
//        }
//    }
//
//    // Handle connection success
//    LaunchedEffect(connectionRequestState) {
//        when (connectionRequestState) {
//            is ConnectionRequestState.Success -> {
//                // Show success message or handle UI update
//                // You can add a snackbar or toast here
//            }
//
//            is ConnectionRequestState.Error -> {
//                // Handle error - could show a snackbar
//            }
//
//            else -> {}
//        }
//    }
//
//    // Update userData when userProfile changes and save to SharedPreferences
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            val userId = profile.id ?: -1  // Assuming UserProfile has an id field
//            val username = profile.full_name?.takeIf { it.isNotEmpty() }
//                ?: profile.username
//                ?: ""
//            val profilePic = profile.profile_pic ?: ""
//
//            // Save to SharedPreferences
//            userDataManager.saveUserData(userId, username, profilePic)
//
//            // Update local state
//            userData = UserData(userId, username, profilePic)
//        }
//    }
//
//
//    // FIXED: Single LaunchedEffect for initial data loading
//    LaunchedEffect(Unit) {
//        // Fetch feed posts (default postType is "feed")
//        viewModel.fetchPosts(postType = "feed")
//
//        // Fetch user profile only if userData is empty
//        if (userData.username.isEmpty()) {
//            viewModel.fetchUserProfile()
//        }
//    }
//
//    // Get auth token using TokenManager
//    val tokenManager = remember { TokenManager(context) }
//    val token = remember { tokenManager.getToken() }
//
//    Scaffold(
//        topBar = {
//            TopBarHome(
//                tabs = listOf("Feed", "Resources", "Connections"),
//                selectedTab = "Feed",
//                navController = navController,
//                onTabSelected = { tab ->
//                    when (tab) {
//                        "Feed" -> { /* Already here */
//                        }
//
//                        "Resources" -> navController.navigate("resourcehub")
//
//                        "Connections" -> navController.navigate("connections")
//                    }
//                },
//                modifier = Modifier.background(Color.White)
//
//            )
//
//        },
//        bottomBar = {
//            BottomNavBar(navController = navController)
//        },
////        floatingActionButton = {
////            GradientCreatePostButton(
////                viewModel = viewModel,
////                modifier = Modifier.padding(16.dp),
////                username = userData.username.ifEmpty { "Loading..." },
////                profilePic = userData.profilePic.takeIf { it?.isNotEmpty() == true }
////            )
////        },
////        floatingActionButtonPosition = FabPosition.End,
////        modifier = Modifier
////            .fillMaxSize()
//
//
//        floatingActionButton = {
//            Column(
//                horizontalAlignment = Alignment.End,
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Message Icon FAB
////                FloatingActionButton(
////                    onClick = {
////                        navController.navigate(Screen.MessageConnections.route)
////                    },
//////                    containerColor = Color(0xFF8B5CF6),
////                    contentColor = Color.White,
////                    modifier = Modifier.size(56.dp)
////                ) {
////                    Icon(
////                        imageVector = Icons.Default.Message,
////                        contentDescription = "Messages",
////                        modifier = Modifier.size(24.dp)
////                    )
////                }
//
////                FloatingActionButton(
////                    onClick = {
////                        navController.navigate(Screen.MessageConnections.route)
////                    },
//////                    containerColor = Color.Transparent,
////                    contentColor = Color.White,
////                    modifier = Modifier
////                        .size(56.dp)
////                        .background(
////                            brush = Brush.verticalGradient(
////                                colors = listOf(
////                                    Color(0xFF893BCF), // Bright magenta/pink
////                                    Color(0xFFEA3BA1)  // Pink
////                                )
////                            ),
////                            shape = CircleShape
////                        )
////                ) {
////                    Icon(
////                        imageVector = Icons.Default.Message,
////                        contentDescription = "Messages",
////                        modifier = Modifier.size(24.dp)
////                    )
////                }
//
//
//                Box(
//                    modifier = Modifier
//                        .size(56.dp)
//                        .background(
//                            brush = Brush.verticalGradient(
//                                colors = listOf(
//                                    Color(0xFF893BCF),
//                                    Color(0xFFEA3BA1)
//                                )
//                            ),
//                            shape = CircleShape
//                        )
//                        .clickable {
//                            navController.navigate(Screen.MessageConnections.route)
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Message,
//                        contentDescription = "Messages",
//                        tint = Color.White,
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//
//
//                // Create Post Button
//                GradientCreatePostButton(
//                    viewModel = viewModel,
//                    modifier = Modifier,
//                    username = userData.username.ifEmpty { "Loading..." },
//                    profilePic = userData.profilePic.takeIf { it?.isNotEmpty() == true }
//                )
//            }
//        },
//        floatingActionButtonPosition = FabPosition.End,
//
//
//        ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White)
//        ) {
//            // FIXED: Add pull-to-refresh functionality
//            val pullRefreshState = rememberPullRefreshState(
//                refreshing = isLoading,
//                onRefresh = {
//                    viewModel.fetchPosts(postType = "feed")
//                }
//            )
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .pullRefresh(pullRefreshState)
//            ) {
//                LazyColumn(
//                    state = lazyListState,
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues),
//                    contentPadding = PaddingValues(bottom = 16.dp),
//                ) {
//                    item {
//                        Spacer(modifier = Modifier.height(22.dp))
//                        PostInputSection(
//                            viewModel = viewModel,
//                            focusRequester = focusRequester,
//                            username = userData.username.ifEmpty { "Loading..." },
//                            profilePic = userData.profilePic
//                        )
//                        Spacer(modifier = Modifier.height(22.dp))
//                    }
//
//                    // FIXED: Better loading state handling
//                    if (isLoading && posts.isEmpty()) {
//                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(32.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                CircularProgressIndicator(
//                                    color = Color(0xFF8B5CF6)
//                                )
//                            }
//                        }
//                    }
//
//                    // FIXED: Better error handling
//                    if (error != null && posts.isEmpty()) {
//                        item {
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(16.dp),
//                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
//                            ) {
//                                Column(
//                                    modifier = Modifier.padding(16.dp),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Text(
//                                        text = error ?: "Unknown error",
//                                        color = Color.Red,
//                                        textAlign = TextAlign.Center
//                                    )
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                    Button(
//                                        onClick = {
//                                            viewModel.fetchPosts(postType = "feed")
//                                        }
//                                    ) {
//                                        Text("Retry")
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // FIXED: Optimized posts rendering
//                    if (posts.isNotEmpty()) {
//                        items(
//                            items = posts,
//                            key = { post -> post.id } // Add key for better performance
//                        ) { post ->
////                            PostCardSection(
////                                post = post,
////                                userProfile = userProfile,
////                                connectionViewModel = connectionViewModel,
////                                onLikeClick = { postId ->
////                                    viewModel.toggleLike(postId)
////                                },
////                                onCommentClick = { clickedPost ->
////                                    selectedPost = clickedPost
////                                    viewModel.fetchComments(clickedPost.id)
////                                    showCommentBottomSheet = true
////                                },
////                                onShareClick = { clickedPost ->
////                                    selectedPostForShare = clickedPost
////                                    showShareBottomSheet = true
////                                },
////                                onConnectClick = { userId ->
////                                    connectionViewModel.sendConnectionRequest(userId)
////                                }
////                            )
//
////                            PostCardSection(
////                                post = post,
////                                userProfile = userProfile,
////                                connectionViewModel = connectionViewModel,
////                                postsViewModel = viewModel,  // Add this line
////                                onLikeClick = { postId ->
////                                    viewModel.toggleLike(postId)
////                                },
////                                onCommentClick = { clickedPost ->
////                                    selectedPost = clickedPost
////                                    viewModel.fetchComments(clickedPost.id)
////                                    showCommentBottomSheet = true
////                                },
////                                onShareClick = { clickedPost ->
////                                    selectedPostForShare = clickedPost
////                                    showShareBottomSheet = true
////                                },
////                                onConnectClick = { userId ->
////                                    connectionViewModel.sendConnectionRequest(userId)
////                                },
////                                onEditClick = { post ->
////                                    // Handle edit
////                                },
////                                onViewPostClick = { post ->
////                                    // Handle view post
////                                }
////                            )
//
//                            // Update PostCardSection onEditClick parameter:
//                            PostCardSection(
//                                post = post,
//                                userProfile = userProfile,
//                                connectionViewModel = connectionViewModel,
//                                postsViewModel = viewModel,
//                                onLikeClick = { postId ->
//                                    viewModel.toggleLike(postId)
//                                },
//                                onCommentClick = { clickedPost ->
//                                    selectedPost = clickedPost
//                                    viewModel.fetchComments(clickedPost.id)
//                                    showCommentBottomSheet = true
//                                },
//                                onShareClick = { clickedPost ->
//                                    selectedPostForShare = clickedPost
//                                    showShareBottomSheet = true
//                                },
//                                onConnectClick = { userId ->
//                                    connectionViewModel.sendConnectionRequest(userId)
//                                },
//                                onEditClick = { post ->
//                                    postToEdit = post
//                                    viewModel.initializePostForEdit(post.media)
//                                    showEditDialog = true
//                                },
//                                onViewPostClick = { post ->
//                                    // Handle view post
//                                },
//                                onDeleteClick = { post ->
//                                    showDeleteDialog = true
//                                }
//                            )
//                        }
//                    } else if (!isLoading && error == null) {
//                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(32.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Column(
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Text(
//                                        text = "No posts available",
//                                        color = Color.Gray,
//                                        fontSize = 16.sp
//                                    )
//                                    Spacer(modifier = Modifier.height(8.dp))
//                                    Button(
//                                        onClick = {
//                                            viewModel.fetchPosts(postType = "feed")
//                                        }
//                                    ) {
//                                        Text("Refresh")
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // Pull refresh indicator
//                PullRefreshIndicator(
//                    refreshing = isLoading,
//                    state = pullRefreshState,
//                    modifier = Modifier.align(Alignment.TopCenter),
//                    backgroundColor = Color.White,
//                    contentColor = Color(0xFF8B5CF6)
//                )
//            }
//        }
//
//        // Comment Bottom Sheet
//        if (showCommentBottomSheet && selectedPost != null) {
//            ModalBottomSheet(
//                onDismissRequest = {
//                    showCommentBottomSheet = false
//                    selectedPost = null
//                    viewModel.clearComments()
//                },
//                sheetState = commentBottomSheetState,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                CommentBottomSheetContent(
//                    post = selectedPost!!,
//                    comments = comments,
//                    isLoading = commentsLoading,
//                    error = commentsError,
//                    viewModel = viewModel,
//                    onDismiss = {
//                        showCommentBottomSheet = false
//                        selectedPost = null
//                        viewModel.clearComments()
//                    },
//                    onRefresh = {
//                        selectedPost?.let { post ->
//                            viewModel.fetchComments(post.id)
//                        }
//                    }
//                )
//            }
//        }
//
//        // Share Bottom Sheet
//        selectedPostForShare?.let { post ->
//            if (showShareBottomSheet) {
//                ModalBottomSheet(
//                    onDismissRequest = {
//                        showShareBottomSheet = false
//                        selectedPostForShare = null
//                    },
//                    sheetState = shareBottomSheetState,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    ShareBottomSheetContent(
//                        post = post,
//                        context = context,
//                        onDismiss = {
//                            showShareBottomSheet = false
//                            selectedPostForShare = null
//                        }
//                    )
//                }
//            }
//        }
//
//        // Edit Post Dialog
//        if (showEditDialog && postToEdit != null) {
//            EditPostDialog(
//                post = postToEdit!!,
//                viewModel = viewModel,
//                username = userData.username.ifEmpty { "Loading..." },
//                profilePic = userData.profilePic,
//                onDismiss = {
//                    showEditDialog = false
//                    postToEdit = null
//                    viewModel.resetUpdateState()
//                }
//            )
//        }
//    }
//}

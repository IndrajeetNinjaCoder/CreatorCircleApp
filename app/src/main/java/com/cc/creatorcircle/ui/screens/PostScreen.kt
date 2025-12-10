@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.ui.screens.home.CommentBottomSheetContent
import com.cc.creatorcircle.ui.screens.home.EditPostDialog
import com.cc.creatorcircle.ui.screens.home.PostCardSection
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun PostScreen(navController: NavController, postId: String) {
    val context = LocalContext.current
    val viewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )
    val connectionViewModel: ConnectionViewModel = viewModel()
    val userDataManager = remember { UserDataManager(context) }
    val userData by remember { mutableStateOf(userDataManager.getUserData()) }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("PostScreen", "PostScreen")
        FirebaseAnalyticsHelper.logFeatureUsed("view_single_post", postId)
    }

    // State for single post
    val singlePost by viewModel.singlePost.collectAsState()
    val singlePostLoading by viewModel.singlePostLoading.collectAsState()
    val singlePostError by viewModel.singlePostError.collectAsState()

    // State for comments
    val comments by viewModel.comments.collectAsState()
    val commentsLoading by viewModel.commentsLoading.collectAsState()
    val commentsError by viewModel.commentsError.collectAsState()

    // State for user profile
    val userProfile by viewModel.userProfile.collectAsState()

    // Bottom sheet states
    val commentBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    val shareBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var selectedPostForShare by remember { mutableStateOf<Post?>(null) }

    // Edit dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }
    val postUpdateState by viewModel.postUpdateState.collectAsState()

    // Delete dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }
    val postDeletionState by viewModel.postDeletionState.collectAsState()

    // Handle post update state
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
                // Refresh the post
                viewModel.fetchPostById(postId)
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()
            }
            is PostUpdateState.Error -> {
                (postUpdateState as? PostUpdateState.Error)?.let { errorState ->
                    FirebaseAnalyticsHelper.logError(
                        errorType = "post_update_error",
                        errorMessage = errorState.message,
                        context = "PostScreen"
                    )
                }
            }
            else -> {}
        }
    }

    // Handle post deletion state
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
                // Navigate back after deletion
                navController.popBackStack()
            }
            is PostDeletionState.Error -> {
                (postDeletionState as? PostDeletionState.Error)?.let { errorState ->
                    FirebaseAnalyticsHelper.logError(
                        errorType = "post_deletion_error",
                        errorMessage = errorState.message,
                        context = "PostScreen"
                    )
                }
            }
            else -> {}
        }
    }

    // Fetch post and user profile on launch
    LaunchedEffect(postId) {
        viewModel.fetchPostById(postId)
        if (userData.username.isEmpty()) {
            viewModel.fetchUserProfile()
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = "Post",
                navController = navController
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
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
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = singlePostLoading,
                onRefresh = {
                    FirebaseAnalyticsHelper.logFeatureUsed("refresh_single_post", postId)
                    viewModel.fetchPostById(postId)
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    when {
                        singlePostLoading && singlePost == null -> {
                            // Loading state
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF8B5CF6)
                                )
                            }
                        }
                        singlePostError != null && singlePost == null -> {
                            // Error state
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFFFEBEE)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = singlePostError ?: "Failed to load post",
                                            color = Color.Red,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                FirebaseAnalyticsHelper.logFeatureUsed("retry_post_load")
                                                viewModel.fetchPostById(postId)
                                            }
                                        ) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                        }
                        singlePost != null -> {
                            // Post loaded successfully
                            PostCardSection(
                                post = singlePost!!,
                                userProfile = userProfile,
                                connectionViewModel = connectionViewModel,
                                postsViewModel = viewModel,
                                onLikeClick = { postId ->
                                    viewModel.toggleLike(postId)
                                    // Refresh the post to get updated like count
                                    viewModel.fetchPostById(postId)
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
                                        source = "post_screen"
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
                                    FirebaseAnalyticsHelper.logFeatureUsed("view_post_detail", "post_screen")
                                },
                                onDeleteClick = { post ->
                                    FirebaseAnalyticsHelper.logDialogOpened("delete_post", post.id)
                                    postToDelete = post
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = singlePostLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF8B5CF6)
                )
            }
        }

        // Comment Bottom Sheet
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
                        // Refresh the post to get updated comment count
                        viewModel.fetchPostById(postId)
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

        // Edit Post Dialog
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

        // Delete Dialog
        if (showDeleteDialog && postToDelete != null) {
            LaunchedEffect(Unit) {
                FirebaseAnalyticsHelper.logDialogOpened("delete_confirmation", postToDelete?.id)
            }

            androidx.compose.material.AlertDialog(
                onDismissRequest = {
                    if (postDeletionState !is PostDeletionState.Loading) {
                        FirebaseAnalyticsHelper.logDialogClosed("delete_confirmation", "dismissed")
                        showDeleteDialog = false
                    }
                },
                title = {
                    Text(
                        text = "Delete Post",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                            FirebaseAnalyticsHelper.logDialogClosed("delete_confirmation", "confirmed")
                            postToDelete?.let { viewModel.deletePost(it.id) }
                        },
                        enabled = postDeletionState !is PostDeletionState.Loading,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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
                    androidx.compose.material3.TextButton(
                        onClick = {
                            FirebaseAnalyticsHelper.logDialogClosed("delete_confirmation", "cancelled")
                            showDeleteDialog = false
                            postToDelete = null
                            viewModel.clearDeletionState()
                        },
                        enabled = postDeletionState !is PostDeletionState.Loading
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}










//package com.cc.creatorcircle.ui.screens
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.navigation.NavController
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.TopBar
//
//@Composable
//fun PostScreen(postId: String, navController: NavController) {
//    Scaffold(
//        topBar = { TopBar(title = "Notifications", navController) },
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//
//    }
//}
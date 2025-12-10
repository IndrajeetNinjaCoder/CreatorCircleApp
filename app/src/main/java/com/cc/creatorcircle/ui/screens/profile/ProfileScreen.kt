@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens.profile

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.PostDeletionState
import com.cc.creatorcircle.data.models.PostUpdateState
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.ui.screens.home.CommentBottomSheetContent
import com.cc.creatorcircle.ui.screens.home.EditPostDialog
import com.cc.creatorcircle.ui.screens.home.PostCardSection
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserViewModelFactory


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context)
    )

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("ProfileScreen", "ProfileScreen")
    }

    // Collect states from ViewModel
    val userProfile by postsViewModel.userProfile.collectAsState()
    val isLoading by postsViewModel.profileLoading.collectAsState()
    val error by postsViewModel.profileError.collectAsState()

    // Add user posts states - observing the Map
    val userPostsMap by postsViewModel.userPosts.collectAsState()
    val userPostsLoadingSet by postsViewModel.userPostsLoading.collectAsState()
    val userPostsErrorMap by postsViewModel.userPostsError.collectAsState()

    // Extract current user's data from the maps
    val currentUserId = userProfile?.id
    val currentUserPosts = currentUserId?.let { userPostsMap[it] } ?: emptyList()
    val currentUserPostsLoading = currentUserId?.let { userPostsLoadingSet.contains(it) } ?: false
    val currentUserPostsError = currentUserId?.let { userPostsErrorMap[it] }

    // State to control Account screen visibility
    var showAccount by remember { mutableStateOf(false) }

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logProfileLoadStarted()
        postsViewModel.fetchUserProfile()
    }

    // Fetch user posts when profile is loaded
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            FirebaseAnalyticsHelper.logUserPostsLoadStarted(userId)
            postsViewModel.fetchUserPosts(userId)
        }
    }

    // Track successful profile load
    LaunchedEffect(userProfile, isLoading) {
        if (!isLoading && userProfile != null) {
            FirebaseAnalyticsHelper.logProfileLoaded(
                userId = userProfile!!.id ?: -1,
                postCount = currentUserPosts.size,
                connectionCount = userProfile!!.accepted_connections.count
            )
        }
    }

    // Track profile load error
    LaunchedEffect(error) {
        error?.let {
            FirebaseAnalyticsHelper.logProfileError(it)
        }
    }

    // Track user posts load error
    LaunchedEffect(currentUserPostsError) {
        currentUserPostsError?.let {
            FirebaseAnalyticsHelper.logUserPostsError(it)
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF8F8))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFB388FF)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                FirebaseAnalyticsHelper.logFeatureUsed("retry_profile_load")
                                postsViewModel.fetchUserProfile()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB388FF)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                userProfile != null -> {
                    ProfileContent(
                        navController = navController,
                        profile = userProfile!!,
                        onMenuClick = {
                            FirebaseAnalyticsHelper.logProfileMenuOpened(userProfile!!.id ?: -1)
                            showAccount = true
                        },
                        postsViewModel = postsViewModel,
                        userPosts = currentUserPosts,
                        userPostsLoading = currentUserPostsLoading,
                        userPostsError = currentUserPostsError,
                        userViewModel = userViewModel
                    )
                }

                else -> {
                    Text(
                        text = "No profile data available",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                }
            }

            // Show Account Screen as overlay
            if (showAccount) {
                Account(
                    navController = navController,
                    userViewModel = userViewModel,
                    onDismiss = {
                        FirebaseAnalyticsHelper.logDialogClosed("account_menu", "dismissed")
                        showAccount = false
                    }
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ProfileContent(
    navController: NavController,
    profile: com.cc.creatorcircle.data.models.UserProfile,
    onMenuClick: () -> Unit,
    postsViewModel: PostsViewModel,
    userPosts: List<com.cc.creatorcircle.data.models.Post>,
    userPostsLoading: Boolean,
    userPostsError: String?,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf("Posts") }

    // Add ConnectionViewModel
    val connectionViewModel: ConnectionViewModel = viewModel()

    // Simply use the passed parameters - they should already be observed in the parent
    val displayPosts = userPosts
    val displayLoading = userPostsLoading
    val displayError = userPostsError

    // Comment states
    val comments by postsViewModel.comments.collectAsState()
    val commentsLoading by postsViewModel.commentsLoading.collectAsState()
    val commentsError by postsViewModel.commentsError.collectAsState()

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

    // DELETE DIALOG STATE
    var showDeleteDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    // EDIT DIALOG STATE
    var showEditDialog by remember { mutableStateOf(false) }
    var postToEdit by remember { mutableStateOf<Post?>(null) }

    // Observe post deletion state
    val postDeletionState by postsViewModel.postDeletionState.collectAsState()

    // Observe post update state
    val postUpdateState by postsViewModel.postUpdateState.collectAsState()

    // Handle delete dialog result
    LaunchedEffect(postDeletionState) {
        when (postDeletionState) {
            is PostDeletionState.Success -> {
                showDeleteDialog = false
                val deletedPostId = postToDelete?.id
                postToDelete = null
                postsViewModel.clearDeletionState()

                // Track successful deletion
                FirebaseAnalyticsHelper.logFeatureUsed("post_deleted")
                deletedPostId?.let {
                    FirebaseAnalyticsHelper.logEvent(
                        "post_deletion_success",
                        mapOf("post_id" to it.toString())
                    )
                }

                // Refresh user posts after successful deletion
                profile.id?.let { userId ->
                    postsViewModel.refreshUserPosts(userId)

                    if (deletedPostId != null) {
                        postsViewModel.removePostFromUserPosts(userId, deletedPostId)
                    }
                }
            }

            is PostDeletionState.Error -> {
                // Track deletion error
                FirebaseAnalyticsHelper.logEvent(
                    "post_deletion_error",
                    mapOf("error" to (postDeletionState as PostDeletionState.Error).message)
                )
            }

            else -> {}
        }
    }

    // Handle post update state
    LaunchedEffect(postUpdateState) {
        when (postUpdateState) {
            is PostUpdateState.Success -> {
                showEditDialog = false
                postToEdit = null
                postsViewModel.resetUpdateState()
                Toast.makeText(context, "Post updated successfully", Toast.LENGTH_SHORT).show()

                // Track successful update
                FirebaseAnalyticsHelper.logFeatureUsed("post_updated")
                FirebaseAnalyticsHelper.logEvent("post_update_success")

                // Refresh user posts after successful update
                profile.id?.let { userId ->
                    postsViewModel.refreshUserPosts(userId)
                }
            }
            is PostUpdateState.Error -> {
                // Track update error
                FirebaseAnalyticsHelper.logEvent(
                    "post_update_error",
                    mapOf("error" to (postUpdateState as PostUpdateState.Error).message)
                )
            }
            else -> {}
        }
    }

    // Track tab changes
    LaunchedEffect(selectedTab) {
        FirebaseAnalyticsHelper.logEvent(
            "profile_tab_selected",
            mapOf("tab_name" to selectedTab)
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && postToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (postDeletionState !is PostDeletionState.Loading) {
                    FirebaseAnalyticsHelper.logDialogClosed("delete_post_dialog", "dismissed")
                    showDeleteDialog = false
                    postToDelete = null
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
                        postToDelete?.let { post ->
                            FirebaseAnalyticsHelper.logFeatureUsed("confirm_post_delete")
                            FirebaseAnalyticsHelper.logEvent(
                                "post_delete_initiated",
                                mapOf("post_id" to post.id.toString())
                            )
                            postsViewModel.deletePost(post.id)
                        }
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
                        FirebaseAnalyticsHelper.logDialogClosed("delete_post_dialog", "cancelled")
                        showDeleteDialog = false
                        postToDelete = null
                        postsViewModel.clearDeletionState()
                    },
                    enabled = postDeletionState !is PostDeletionState.Loading
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Edit Post Dialog
    if (showEditDialog && postToEdit != null) {
        EditPostDialog(
            post = postToEdit!!,
            viewModel = postsViewModel,
            username = profile.full_name?.takeIf { it.isNotEmpty() } ?: profile.username ?: "Loading...",
            profilePic = profile.profile_pic,
            onDismiss = {
                FirebaseAnalyticsHelper.logDialogClosed("edit_post_dialog", "dismissed")
                showEditDialog = false
                postToEdit = null
                postsViewModel.resetUpdateState()
            }
        )
    }

    // Extract social media follower counts
    val instagramFollowers = try {
        val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
        val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
        (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error parsing Instagram followers", e)
        0
    }

    val youtubeFollowers = try {
        val platformFollowers = profile.platform_followers?.get("youtube") as? List<*>
        val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
        (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error parsing YouTube followers", e)
        0
    }

    val facebookFollowers = try {
        val platformFollowers = profile.platform_followers?.get("facebook") as? List<*>
        val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
        (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error parsing Facebook followers", e)
        0
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF8F8)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // --- Top Bar ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_left_arrow),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(26.dp)
                            .clickable {
                                FirebaseAnalyticsHelper.logFeatureUsed("profile_back_button")
                                navController.popBackStack()
                            }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "More options",
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onMenuClick() }
                    )
                }

                // --- Profile Info Row ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    // Profile Image
                    if (profile.profile_pic != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profile.profile_pic),
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.LightGray, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0))
                                .border(1.dp, Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = "Default profile",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = profile.full_name ?: profile.username,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Posts count - use actual count from displayPosts
                            ProfileStat("${displayPosts.size}", "Posts")
                            Spacer(modifier = Modifier.width(20.dp))
                            ProfileStat("${profile.accepted_connections.count}", "Connection")
                        }
                    }
                }

                // --- Bio ---
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Categories
                    if (!profile.categories.isNullOrEmpty()) {
                        Text(
                            text = profile.categories.firstOrNull() ?: "",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Bio
                    Text(
                        text = profile.bio ?: "No bio available",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )
                }

                // --- Social Icons ---
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (youtubeFollowers > 0) {
                        SocialIcon(R.drawable.ic_youtube, formatFollowers(youtubeFollowers))
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (instagramFollowers > 0) {
                        SocialIcon(R.drawable.ic_instagram, formatFollowers(instagramFollowers))
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    if (facebookFollowers > 0) {
                        SocialIcon(R.drawable.ic_facebook, formatFollowers(facebookFollowers))
                    }
                }

                // --- Buttons ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val buttonColor = Color(0xFFB388FF)



                    OutlinedButton(
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("edit_profile_button")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_edit_initiated",
                                mapOf("user_id" to (profile.id?.toString() ?: "unknown"))
                            )
                            navController.navigate(Screen.YourProfileScreen.route)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(25),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = buttonColor
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 1.dp,
                            brush = androidx.compose.ui.graphics.SolidColor(buttonColor)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = buttonColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Edit profile",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))


                }

                // --- Tabs ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                )
                {
                    Text(
                        "Posts",
                        color = if (selectedTab == "Posts") Color(0xFF9C27B0) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            selectedTab = "Posts"
                        }
                    )
                    Text(
                        "Reels",
                        color = if (selectedTab == "Reels") Color(0xFF9C27B0) else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable {
                            selectedTab = "Reels"
                        }
                    )
                }
                Divider(color = Color(0xFFECECEC), thickness = 1.dp)
            }

            // --- Posts Section ---
            if (selectedTab == "Posts") {
                // Loading state
                if (displayLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFB388FF)
                            )
                        }
                    }
                }

                // Error state
                if (displayError != null) {
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
                                    text = displayError,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        FirebaseAnalyticsHelper.logFeatureUsed("retry_user_posts")
                                        profile.id?.let { postsViewModel.refreshUserPosts(it) }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFB388FF)
                                    )
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                // Empty state
                if (displayPosts.isEmpty() && !displayLoading && displayError == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts yet",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Posts list
                items(
                    count = displayPosts.size,
                    key = { index -> displayPosts[index].id }
                ) { index ->
                    val post = displayPosts[index]
                    PostCardSection(
                        post = post,
                        userProfile = profile,
                        connectionViewModel = connectionViewModel,
                        postsViewModel = postsViewModel,
                        onLikeClick = { postId ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_like_from_profile")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_post_liked",
                                mapOf(
                                    "post_id" to postId.toString(),
                                    "user_id" to (profile.id?.toString() ?: "unknown")
                                )
                            )
                            postsViewModel.toggleLike(postId)
                            profile.id?.let { userId ->
                                postsViewModel.updateUserPostLike(userId, postId)
                            }
                        },
                        onCommentClick = { clickedPost ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_comment_from_profile")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_post_comment_opened",
                                mapOf(
                                    "post_id" to clickedPost.id.toString(),
                                    "comment_count" to clickedPost.comments.toString()
                                )
                            )
                            selectedPost = clickedPost
                            postsViewModel.fetchComments(clickedPost.id)
                            showCommentBottomSheet = true
                        },
                        onShareClick = { clickedPost ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_share_from_profile")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_post_share_opened",
                                mapOf("post_id" to clickedPost.id.toString())
                            )
                            selectedPostForShare = clickedPost
                            showShareBottomSheet = true
                        },
                        onConnectClick = { userId ->
                            // Handle connect click - not needed for own profile
                        },
                        onEditClick = { post ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_edit_initiated")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_post_edit_clicked",
                                mapOf("post_id" to post.id.toString())
                            )
                            postToEdit = post
                            postsViewModel.initializePostForEdit(post.media)
                            showEditDialog = true
                        },
                        onViewPostClick = { post ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_view_from_profile")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_post_viewed",
                                mapOf("post_id" to post.id.toString())
                            )
                            // TODO: Navigate to post detail screen
                        },
                        onDeleteClick = { post ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_delete_clicked")
                            FirebaseAnalyticsHelper.logEvent(
                                "profile_post_delete_clicked",
                                mapOf("post_id" to post.id.toString())
                            )
                            postToDelete = post
                            showDeleteDialog = true
                        }
                    )
                }
            } else {
                // Reels tab - Show "No reels" message
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No reels",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Comment Bottom Sheet
        if (showCommentBottomSheet && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    FirebaseAnalyticsHelper.logDialogClosed("comment_bottom_sheet", "dismissed")
                    showCommentBottomSheet = false
                    selectedPost = null
                    postsViewModel.clearComments()
                },
                sheetState = commentBottomSheetState,
                modifier = Modifier.fillMaxSize()
            ) {
                CommentBottomSheetContent(
                    post = selectedPost!!,
                    comments = comments,
                    isLoading = commentsLoading,
                    error = commentsError,
                    viewModel = postsViewModel,
                    onDismiss = {
                        FirebaseAnalyticsHelper.logDialogClosed("comment_bottom_sheet", "closed")
                        showCommentBottomSheet = false
                        selectedPost = null
                        postsViewModel.clearComments()
                    },
                    onRefresh = {
                        selectedPost?.let { post ->
                            FirebaseAnalyticsHelper.logFeatureUsed("comments_refresh")
                            postsViewModel.fetchComments(post.id)
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
                        FirebaseAnalyticsHelper.logDialogClosed("share_bottom_sheet", "dismissed")
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
                            FirebaseAnalyticsHelper.logDialogClosed("share_bottom_sheet", "closed")
                            showShareBottomSheet = false
                            selectedPostForShare = null
                        }
                    )
                }
            }
        }
    }
}


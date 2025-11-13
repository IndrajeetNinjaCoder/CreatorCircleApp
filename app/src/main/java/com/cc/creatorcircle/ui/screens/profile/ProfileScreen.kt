@file:OptIn(ExperimentalMaterial3Api::class)
package com.cc.creatorcircle.ui.screens.profile

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.cc.creatorcircle.R
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.ui.screens.home.CommentBottomSheetContent
import com.cc.creatorcircle.ui.screens.home.PostCardSection
import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.viewModel.ConnectionViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

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
    var showAccountScreen by remember { mutableStateOf(false) }

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        postsViewModel.fetchUserProfile()
    }

    // Fetch user posts when profile is loaded
    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            postsViewModel.fetchUserPosts(userId)
        }
    }

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
                        onClick = { postsViewModel.fetchUserProfile() },
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
                    onMenuClick = { showAccountScreen = true },
                    postsViewModel = postsViewModel,
                    userPosts = currentUserPosts,
                    userPostsLoading = currentUserPostsLoading,
                    userPostsError = currentUserPostsError
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
        if (showAccountScreen) {
            AccountScreen(
                onDismiss = { showAccountScreen = false },
                navController = navController
            )
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
    userPostsError: String?
) {
    val context = LocalContext.current

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
                            .clickable { navController.popBackStack() }
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
                ) {
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
                            Spacer(modifier = Modifier.width(20.dp))
                            ProfileStat("${profile.following.count}", "Following")
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
                        onClick = { /* Navigate to edit profile */ },
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

                    OutlinedButton(
                        onClick = { /* Share profile */ },
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
                            painter = painterResource(id = R.drawable.ic_share),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = buttonColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Share profile",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // --- Tabs ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        "Posts",
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Reels",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Divider(color = Color(0xFFECECEC), thickness = 1.dp)
            }

            // --- Posts Section ---
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

            // Posts list - using items() correctly with optimized like handling
            items(
                count = displayPosts.size,
                key = { index -> displayPosts[index].id }
            ) { index ->
                val post = displayPosts[index]
                PostCardSection(
                    post = post,
                    userProfile = profile,
                    connectionViewModel = connectionViewModel,
                    onLikeClick = { postId ->
                        // Toggle like in ViewModel
                        postsViewModel.toggleLike(postId)

                        // Also update the user posts to reflect the change immediately
                        profile.id?.let { userId ->
                            postsViewModel.updateUserPostLike(userId, postId)
                        }
                    },
                    onCommentClick = { clickedPost ->
                        selectedPost = clickedPost
                        postsViewModel.fetchComments(clickedPost.id)
                        showCommentBottomSheet = true
                    },
                    onShareClick = { clickedPost ->
                        selectedPostForShare = clickedPost
                        showShareBottomSheet = true
                    },
                    onConnectClick = { userId ->
                        // Handle connect click - not needed for own profile
                    }
                )
            }
        }

        // Comment Bottom Sheet
        if (showCommentBottomSheet && selectedPost != null) {
            ModalBottomSheet(
                onDismissRequest = {
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
                        showCommentBottomSheet = false
                        selectedPost = null
                        postsViewModel.clearComments()
                    },
                    onRefresh = {
                        selectedPost?.let { post ->
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
fun AccountScreen(
    onDismiss: () -> Unit,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_left_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onDismiss() },
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            AccountMenuItem(
                icon = R.drawable.ic_profile_icon,
                text = "Your profile",
                onClick = { navController.navigate(Screen.YourProfileScreen.route) }
            )

            AccountMenuItem(
                icon = R.drawable.ic_edit,
                text = "Personal Information",
                onClick = { /* Navigate to personal info */ }
            )

            AccountMenuItem(
                icon = R.drawable.ic_lock,
                text = "Account privacy",
                onClick = { /* Navigate to privacy */ }
            )

            AccountMenuItem(
                icon = R.drawable.ic_security,
                text = "Password & security",
                onClick = { /* Navigate to security */ }
            )

            AccountMenuItem(
                icon = R.drawable.ic_block,
                text = "Blocked",
                onClick = { /* Navigate to blocked users */ }
            )

            AccountMenuItem(
                icon = R.drawable.ic_info,
                text = "About",
                onClick = { /* Navigate to about */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Item (Different styling)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Handle logout */ }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Logout",
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFFE53935)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AccountMenuItem(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(28.dp),
            tint = Color(0xFF9E9E9E)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_right_arrow),
            contentDescription = "Navigate",
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF9E9E9E)
        )
    }
}











@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(label, fontSize = 13.sp, color = Color.Black)
    }
}

@Composable
fun SocialIcon(iconRes: Int, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(count, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun IconWithLabel(iconRes: Int, label: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = tint)
    }
}

// Helper function to format follower counts
fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}


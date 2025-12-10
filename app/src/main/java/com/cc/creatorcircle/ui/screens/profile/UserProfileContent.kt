package com.cc.creatorcircle.ui.screens.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
import com.cc.creatorcircle.ui.screens.home.CommentBottomSheetContent
import com.cc.creatorcircle.ui.screens.home.PostCardSection
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import android.util.Log
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun UserProfileContent(
    navController: NavController,
    profile: com.cc.creatorcircle.data.models.UserProfile,
    loggedInUserId: Int?, // Add this to identify logged-in user
    postsViewModel: PostsViewModel,
    userPosts: List<Post>,
    userPostsLoading: Boolean,
    userPostsError: String?
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Posts") }
    val connectionViewModel: ConnectionViewModel = viewModel()

    val displayPosts = userPosts
    val displayLoading = userPostsLoading
    val displayError = userPostsError

    // Comment states
    val comments by postsViewModel.comments.collectAsState()
    val commentsLoading by postsViewModel.commentsLoading.collectAsState()
    val commentsError by postsViewModel.commentsError.collectAsState()

    // Bottom sheet states
    val commentBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showCommentBottomSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    val shareBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showShareBottomSheet by remember { mutableStateOf(false) }
    var selectedPostForShare by remember { mutableStateOf<Post?>(null) }

    // Check if this is the logged-in user's profile
    val isOwnProfile = profile.id == loggedInUserId

    // Track tab changes
    LaunchedEffect(selectedTab) {
        FirebaseAnalyticsHelper.logEvent(
            "profile_tab_selected",
            mapOf("tab_name" to selectedTab, "is_own_profile" to isOwnProfile.toString())
        )
    }

    // Extract social media followers
    val socialMediaCounts = remember(profile) {
        parseSocialMediaFollowers(profile)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAF8F8)),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Top Bar
                ProfileTopBar(
                    onBackClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("user_profile_back_button")
                        navController.popBackStack()
                    },
                    showMenuIcon = false // Don't show menu for other users
                )

                // Profile Info
                UserProfileInfoSection(
                    profile = profile,
                    postCount = displayPosts.size,
                    isOwnProfile = isOwnProfile
                )

                // Bio
                ProfileBioSection(
                    categories = profile.categories,
                    bio = profile.bio
                )

                // Social Icons
                if (socialMediaCounts.hasAnyFollowers()) {
                    SocialMediaRow(
                        instagramFollowers = socialMediaCounts.instagram,
                        youtubeFollowers = socialMediaCounts.youtube,
                        facebookFollowers = socialMediaCounts.facebook
                    )
                }

                // Action Buttons - Different for own profile vs others
                UserProfileActionButtons(
                    navController = navController,
                    profile = profile,
                    isOwnProfile = isOwnProfile,
                    connectionViewModel = connectionViewModel,
                    onMessageClick = {
                        // TODO: Navigate to messages
                        FirebaseAnalyticsHelper.logFeatureUsed("message_user_from_profile")
                    }
                )

                // Tabs
                ProfileTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                
                Divider(color = Color(0xFFECECEC), thickness = 1.dp)
            }

            // Posts Section
            if (selectedTab == "Posts") {
                if (displayLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFFB388FF))
                        }
                    }
                }

                if (displayError != null) {
                    item {
                        ErrorStateWithRetry(
                            error = displayError,
                            onRetry = {
                                FirebaseAnalyticsHelper.logFeatureUsed("retry_user_posts")
                                profile.id?.let { postsViewModel.refreshUserPosts(it) }
                            }
                        )
                    }
                }

                if (displayPosts.isEmpty() && !displayLoading && displayError == null) {
                    item {
                        EmptyState(message = "No posts yet")
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
                            FirebaseAnalyticsHelper.logFeatureUsed("post_like_from_user_profile")
//                            postsViewModel.toggleLike(postId)

                            postsViewModel.toggleLike(postId)
                            profile.id?.let { userId ->
                                postsViewModel.updateUserPostLike(userId, postId)
                            }
                        },
                        onCommentClick = { clickedPost ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_comment_from_user_profile")
                            selectedPost = clickedPost
                            postsViewModel.fetchComments(clickedPost.id)
                            showCommentBottomSheet = true
                        },
                        onShareClick = { clickedPost ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_share_from_user_profile")
                            selectedPostForShare = clickedPost
                            showShareBottomSheet = true
                        },
                        onConnectClick = { userId ->
                            FirebaseAnalyticsHelper.logFeatureUsed("connect_from_user_profile")
                            connectionViewModel.sendConnectionRequest(userId)
                        },
                        onEditClick = {  }, // Hide edit for other users
                        onViewPostClick = { post ->
                            FirebaseAnalyticsHelper.logFeatureUsed("post_view_from_user_profile")
                            // TODO: Navigate to post detail
                        },
                        onDeleteClick = {  } // Hide delete for other users
                    )
                }
            } else {
                item {
                    EmptyState(message = "No reels")
                }
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

// Helper composables
@Composable
private fun ProfileTopBar(
    onBackClick: () -> Unit,
    showMenuIcon: Boolean = false,
    onMenuClick: () -> Unit = {}
) {
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
                .clickable { onBackClick() }
        )
        Spacer(modifier = Modifier.weight(1f))
        if (showMenuIcon) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "More options",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onMenuClick() }
            )
        }
    }
}

@Composable
private fun UserProfileInfoSection(
    profile: com.cc.creatorcircle.data.models.UserProfile,
    postCount: Int,
    isOwnProfile: Boolean
) {
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
                text = profile.full_name ?: profile.username ?: "User",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileStat("$postCount", "Posts")
                Spacer(modifier = Modifier.width(20.dp))
                ProfileStat("${profile.accepted_connections.count}", "Connection")
            }
        }
    }
}

@Composable
private fun UserProfileActionButtons(
    navController: NavController,
    profile: com.cc.creatorcircle.data.models.UserProfile,
    isOwnProfile: Boolean,
    connectionViewModel: ConnectionViewModel,
    onMessageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (isOwnProfile) {
            // Show nothing or edit button for own profile
            // This shouldn't happen as own profile uses ProfileContent
        } else {
            // Connect/Message buttons for other users
            val buttonColor = Color(0xFFB388FF)

//            OutlinedButton(
//                onClick = {
//                    FirebaseAnalyticsHelper.logFeatureUsed("message_button_clicked")
//                    onMessageClick()
//                },
//                modifier = Modifier.weight(1f),
//                shape = RoundedCornerShape(25),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonColor),
//                border = ButtonDefaults.outlinedButtonBorder.copy(
//                    width = 1.dp,
//                    brush = androidx.compose.ui.graphics.SolidColor(buttonColor)
//                )
//            ) {
//                Text("Message", fontSize = 14.sp, fontWeight = FontWeight.Medium)
//            }

//                GradientButton("Book Now") { }
//


            CustomOutlinedButton("Message", Modifier.weight(1f)) {
                FirebaseAnalyticsHelper.logFeatureUsed("message_button_clicked")
                navController.navigate(
                    Screen.MessageScreen.createRoute(
                        userId = profile.id,
                        userName = profile.username,
                        profilePic = profile.profile_pic
                    )
                )
            }

            GradientButton("Book Now", Modifier.weight(1f)) {
                FirebaseAnalyticsHelper.logFeatureUsed("book_now_button_clicked")
                navController.navigate(
                    Screen.BookingSlot.createRoute(
                        profile.id,
                        influencerName = profile.full_name.toString()
                    )
                )
            }

            
//            Button(
//                onClick = {
//                    FirebaseAnalyticsHelper.logFeatureUsed("connect_button_clicked")
//                    profile.id?.let { connectionViewModel.sendConnectionRequest(it) }
//                },
//                modifier = Modifier.weight(1f),
//                shape = RoundedCornerShape(25),
//                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
//            ) {
//                Text("Connect", fontSize = 14.sp, fontWeight = FontWeight.Medium)
//            }


        }
    }
}

@Composable
private fun ProfileTabSelector(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            "Posts",
            color = if (selectedTab == "Posts") Color(0xFF9C27B0) else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.clickable { onTabSelected("Posts") }
        )
        Text(
            "Reels",
            color = if (selectedTab == "Reels") Color(0xFF9C27B0) else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.clickable { onTabSelected("Reels") }
        )
    }
}

@Composable
private fun ErrorStateWithRetry(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB388FF))
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, fontSize = 16.sp)
    }
}
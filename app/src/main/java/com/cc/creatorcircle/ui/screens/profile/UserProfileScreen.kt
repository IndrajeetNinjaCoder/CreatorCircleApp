package com.cc.creatorcircle.ui.screens.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserViewModelFactory





@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavController, InfluencerId: Int) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context)
    )

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("UserProfileScreen", "UserProfileScreen")
    }

    // *** CHANGE: Collect from otherUserProfiles map ***
    val otherUserProfiles by postsViewModel.otherUserProfiles.collectAsState()
    val otherUserLoading by postsViewModel.otherUserLoading.collectAsState()
    val otherUserError by postsViewModel.otherUserError.collectAsState()

    // Get the specific influencer's data
    val influencerProfile = otherUserProfiles[InfluencerId]
    val isLoading = otherUserLoading.contains(InfluencerId)
    val error = otherUserError[InfluencerId]

    // Get logged-in user ID for comparison
    val loggedInUserProfile by userViewModel.userProfile.collectAsState()
    val loggedInUserId = loggedInUserProfile?.id

    // User posts states
    val userPostsMap by postsViewModel.userPosts.collectAsState()
    val userPostsLoadingSet by postsViewModel.userPostsLoading.collectAsState()
    val userPostsErrorMap by postsViewModel.userPostsError.collectAsState()

    // Extract influencer's posts
    val influencerPosts = userPostsMap[InfluencerId] ?: emptyList()
    val influencerPostsLoading = userPostsLoadingSet.contains(InfluencerId)
    val influencerPostsError = userPostsErrorMap[InfluencerId]

    // Fetch logged-in user profile for comparison
    LaunchedEffect(Unit) {
        userViewModel.fetchUserProfile()
    }

    // Fetch influencer profile when screen loads
    LaunchedEffect(InfluencerId) {
        FirebaseAnalyticsHelper.logProfileLoadStarted()
        postsViewModel.fetchUserProfileById(InfluencerId)
    }

    // Fetch influencer posts when profile is loaded
    LaunchedEffect(InfluencerId) {
        FirebaseAnalyticsHelper.logUserPostsLoadStarted(InfluencerId)
        postsViewModel.fetchUserPosts(InfluencerId)
    }

    // Track successful profile load
    LaunchedEffect(influencerProfile, isLoading) {
        if (!isLoading && influencerProfile != null) {
            FirebaseAnalyticsHelper.logProfileLoaded(
                userId = influencerProfile.id,
                postCount = influencerPosts.size,
                connectionCount = influencerProfile.accepted_connections.count
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
    LaunchedEffect(influencerPostsError) {
        influencerPostsError?.let {
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
                            text = error,
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                FirebaseAnalyticsHelper.logFeatureUsed("retry_profile_load")
                                postsViewModel.fetchUserProfileById(InfluencerId)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB388FF)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                influencerProfile != null -> {
                    UserProfileContent(
                        navController = navController,
                        profile = influencerProfile,
                        loggedInUserId = loggedInUserId,
                        postsViewModel = postsViewModel,
                        userPosts = influencerPosts,
                        userPostsLoading = influencerPostsLoading,
                        userPostsError = influencerPostsError
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
        }
    }
}


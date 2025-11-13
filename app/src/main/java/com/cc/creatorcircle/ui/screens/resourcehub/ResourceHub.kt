package com.cc.creatorcircle.ui.screens.resourcehub

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.PostCreationState
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
import com.cc.creatorcircle.ui.components.TopBarHome
import com.cc.creatorcircle.ui.screens.home.CommentBottomSheetContent
import com.cc.creatorcircle.ui.screens.home.PostCardSection
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.utils.createFileFromUri
import com.cc.creatorcircle.viewModel.ConnectionRequestState
import com.cc.creatorcircle.viewModel.ConnectionViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import java.io.File


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ResourceHub(
//    navController: NavController,
//    onCommentClick: (Post) -> Unit = {},
//    onShareClick: (Post) -> Unit = {}
//) {
//    val context = LocalContext.current
//    val viewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    var showPopup by remember { mutableStateOf(false) }
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
//
//    // Get auth token using TokenManager
//    val tokenManager = remember { TokenManager(context) }
//    val token = remember { tokenManager.getToken() }
//
//    // Fetch resource posts and user profile when the composable is first launched
//    LaunchedEffect(Unit) {
//        // Fetch resource posts specifically
//        viewModel.fetchPosts(postType = "resource")
//
//        // Fetch user profile if userData is empty
//        if (userData.username.isEmpty()) {
//            viewModel.fetchUserProfile()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarHome(
//                tabs = listOf("Feed", "Resources", "Connections"),
//                selectedTab = "Resources", // This should be "Resources" since you're in ResourceHub
//                navController = navController,
//                onTabSelected = { tab ->
//                    // Make sure this doesn't try to navigate to "feed"
//                    when (tab) {
//                        "Feed" -> navController.navigate("home") // NOT "feed"
//                        "Resources" -> { /* Already here */
//                        }
//                        "Connections" -> navController.navigate("connections")
//                    }
//                },
//            )
//        },
//        bottomBar = {
//            BottomNavBar(navController = navController)
//        }
//    ) { paddingValues -> // Fixed: Added paddingValues parameter
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues) // Fixed: Apply scaffold padding
//                .background(Color.White)
//        ) {
//
//            // Profile Card Component
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(12.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color.White
//                ),
//                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Profile Image
//                    Card(
//                        modifier = Modifier.size(48.dp),
//                        shape = CircleShape,
//                        colors = CardDefaults.cardColors(
//                            containerColor = Color.Gray
//                        )
//                    ) {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            if (userData.profilePic?.isNotEmpty() == true) {
//                                AsyncImage(
//                                    model = userData.profilePic,
//                                    contentDescription = "Profile Picture",
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .clip(CircleShape),
//                                    contentScale = ContentScale.Crop,
//                                    fallback = painterResource(id = R.drawable.ic_profile1),
//                                    error = painterResource(id = R.drawable.ic_profile1)
//                                )
//                            } else {
//                                // Placeholder for profile image
//                                Icon(
//                                    imageVector = Icons.Default.Person,
//                                    contentDescription = "Profile",
//                                    tint = Color.White,
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    // User Info
//                    Column(
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text(
//                            text = userData.username.ifEmpty { "Loading..." },
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color.Black
//                        )
//                    }
//
//                    GradientButton(
//                        text = "Share your post",
//                        onClick = {
//                            showPopup = true
//                        }
//                    )
//                }
//            }
//
//            // Posts Content
//            when {
//                isLoading -> {
//                    // Loading state
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator(
//                            color = Color(0xFF8B5CF6)
//                        )
//                    }
//                }
//
//                error != null -> {
//                    // Error state
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = error!!,
//                            color = Color.Red,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                }
//
//                posts.isEmpty() -> {
//                    // Empty state
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No resource posts available",
//                            color = Color.Gray,
//                            fontSize = 16.sp
//                        )
//                    }
//                }
//
//                else -> {
//                    // Posts list
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        state = lazyListState // Fixed: Use the declared lazyListState
//                    ) {
//                        items(posts) { post ->
//                            PostCardSection(
//                                post = post,
//                                userProfile = userProfile,
//                                connectionViewModel = connectionViewModel,
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
//                                }
//                            )
//                        }
//                    }
//                }
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
//        // Post Creation Popup
//        if (showPopup) {
//            PostInputPopupDialogResource(
//                viewModel = viewModel,
//                username = userData.username.ifEmpty { "Loading..." },
//                profilePic = userData.profilePic,
//                onDismiss = { showPopup = false }
//            )
//        }
//    }
//}
//



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceHub(
    navController: NavController,
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Post) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    var showPopup by remember { mutableStateOf(false) }

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

    // Get auth token using TokenManager
    val tokenManager = remember { TokenManager(context) }
    val token = remember { tokenManager.getToken() }

    // Fetch resource posts and user profile when the composable is first launched
    LaunchedEffect(Unit) {
        // Fetch resource posts specifically
        viewModel.fetchPosts(postType = "resource")

        // Fetch user profile if userData is empty
        if (userData.username.isEmpty()) {
            viewModel.fetchUserProfile()
        }
    }

    Scaffold(
        topBar = {
            TopBarHome(
                tabs = listOf("Feed", "Resources", "Connections"),
                selectedTab = "Resources", // This should be "Resources" since you're in ResourceHub
                navController = navController,
                onTabSelected = { tab ->
                    // Make sure this doesn't try to navigate to "feed"
                    when (tab) {
                        "Feed" -> navController.navigate("home") // NOT "feed"
                        "Resources" -> { /* Already here */
                        }
                        "Connections" -> navController.navigate("connections")
                    }
                },
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {

            // Profile Card Component
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image
                    Card(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userData.profilePic?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = userData.profilePic,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    fallback = painterResource(id = R.drawable.ic_profile1),
                                    error = painterResource(id = R.drawable.ic_profile1)
                                )
                            } else {
                                // Placeholder for profile image
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // User Info
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = userData.username.ifEmpty { "Loading..." },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                    GradientButton(
                        text = "Share your post",
                        onClick = {
                            showPopup = true
                        }
                    )
                }
            }

            // Posts Content
            when {
                isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }

                error != null -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error!!,
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.fetchPosts(postType = "resource")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8B5CF6)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                posts.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No resource posts available",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.fetchPosts(postType = "resource")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8B5CF6)
                                )
                            ) {
                                Text("Refresh")
                            }
                        }
                    }
                }

                else -> {
                    // Posts list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState
                    ) {
                        items(
                            items = posts,
                            key = { post -> post.id } // Add key for better performance
                        ) { post ->
                            PostCardSection(
                                post = post,
                                userProfile = userProfile,
                                connectionViewModel = connectionViewModel,
                                postsViewModel = viewModel,  // Add PostsViewModel parameter
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
                                },
                                onEditClick = { post ->
                                    // TODO: Navigate to edit post screen or show edit dialog
                                },
                                onViewPostClick = { post ->
                                    // TODO: Navigate to post detail screen
                                }
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

        // Post Creation Popup
        if (showPopup) {
            PostInputPopupDialogResource(
                viewModel = viewModel,
                username = userData.username.ifEmpty { "Loading..." },
                profilePic = userData.profilePic,
                onDismiss = { showPopup = false }
            )
        }
    }
}



//
//@Composable
//fun PostInputPopupDialogResource(
//    viewModel: PostsViewModel,
//    username: String,
//    profilePic: String?,
//    onDismiss: () -> Unit
//) {
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(screenHeight * 4f / 5f) // 2/3 of screen height
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(20.dp)
//            ) {
//                // Header with user info and close button
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // User Avatar
//                        Box(
//                            modifier = Modifier
//                                .size(40.dp)
//                                .background(
//                                    Color(0xFFE0E0E0),
//                                    CircleShape
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            // You can replace this with actual user image
//                            AsyncImage(
//                                model = profilePic,
//                                contentDescription = "User Avatar",
//                                modifier = Modifier
//                                    .size(40.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(12.dp))
//
//                        Column {
//                            Text(
//                                text = username, // Replace with actual user name
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.Black
//                            )
//                            Text(
//                                text = "Member",
//                                fontSize = 12.sp,
//                                color = Color(0xFF666666)
//                            )
//                        }
//                    }
//
//                    // Close button
//                    Box(
//                        modifier = Modifier
//                            .size(32.dp)
//                            .background(
//                                Color(0xFFF5F5F5),
//                                CircleShape
//                            )
//                            .clickable { onDismiss() },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "×",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF666666)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//
//                PostInputPopupResource(
//                    viewModel = viewModel,
//                    modifier = Modifier.weight(1f),
//                    onPostCreated = onDismiss
//                )
//
//
//            }
//        }
//    }
//}



@Composable
fun PostInputPopupDialogResource(
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
                .height(screenHeight * 6f / 7f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp) // Increased from 20dp
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
                                text = username,
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

                Spacer(modifier = Modifier.height(24.dp)) // Increased from 20dp

                PostInputPopupResource(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                    onPostCreated = onDismiss
                )
            }
        }
    }
}

@Composable
fun PostInputPopupResource(
    viewModel: PostsViewModel,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onPostCreated: () -> Unit
) {
    val context = LocalContext.current

    // State for dynamic input fields
    var inputFields by remember {
        mutableStateOf(
            listOf(
                InputFieldData(
                    id = "topic",
                    label = "Topic",
                    text = "",
                    placeholder = "Write your topic"
                ),
                InputFieldData(
                    id = "description",
                    label = "Description",
                    text = "",
                    placeholder = "Write your description"
                ),
                InputFieldData(
                    id = "link",
                    label = "Link",
                    text = "",
                    placeholder = "Add link here"
                )
            )
        )
    }

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

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val file = createFileFromUri(context, selectedUri)
            file?.let { viewModel.addMediaFile(it) }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Handle camera result
            // You'll need to implement camera file handling
        }
    }

    // Document picker launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
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
                // Reset all fields
                inputFields = inputFields.map { it.copy(text = "") }
                // Refresh the posts to show the new one
                viewModel.fetchPosts(postType = "resource")
                // Close the popup
                onPostCreated()
            }

            is PostCreationState.Error -> {
                // Handle error - could show a snackbar or toast
            }

            else -> {}
        }
    }

    // Function to update input field text
    fun updateInputField(id: String, newText: String) {
        inputFields = inputFields.map { field ->
            if (field.id == id) {
                field.copy(text = newText)
            } else field
        }

        // Add new dotted input field if user typed in the last field and it's not empty
        val lastField = inputFields.lastOrNull()
        if (lastField != null && lastField.text.isNotEmpty() && lastField.id == id) {
            val newFieldId = "field_${System.currentTimeMillis()}"
            inputFields = inputFields + InputFieldData(
                id = newFieldId,
                label = "Additional Info",
                text = "",
                placeholder = "Add more information"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Dynamic input fields
        inputFields.forEachIndexed { index, field ->
            DynamicInputField(
                label = field.label,
                text = field.text,
                onTextChange = { newText -> updateInputField(field.id, newText) },
                placeholder = field.placeholder,
                focusRequester = if (index == 0) focusRequester else null,
                singleLine = field.id != "description",
                minHeight = if (field.id == "description") 120.dp else null,
                shouldUseDottedBorder = field.id == "link" || field.id.startsWith("field_")
            )

            // Add spacing between input fields
            if (index < inputFields.size - 1) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Space after input fields

        // Show selected media files
        if (selectedMediaFiles.isNotEmpty()) {
            MediaFilesSection(
                selectedMediaFiles = selectedMediaFiles,
                onRemoveFile = { file -> viewModel.removeMediaFile(file) }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom section with attachments and share button
        BottomActionsSection(
            topicText = inputFields.find { it.id == "topic" }?.text ?: "",
            descriptionText = inputFields.find { it.id == "description" }?.text ?: "",
            linkText = inputFields.find { it.id == "link" }?.text ?: "",
            additionalFields = inputFields.filter { it.id.startsWith("field_") },
            selectedMediaFiles = selectedMediaFiles,
            postCreationState = postCreationState,
            onClick = {},
            onImageClick = { imagePickerLauncher.launch("image/*") },
            onVideoClick = { videoPickerLauncher.launch("video/*") },
            onPdfClick = { pdfPickerLauncher.launch("application/pdf") },
            onShareClick = { topic, description, link, additionalFields ->
                // Create content by combining all fields
                val contentBuilder = StringBuilder()

                if (topic.isNotBlank()) {
                    contentBuilder.append("$topic\n\n")
                }

                if (description.isNotBlank()) {
                    contentBuilder.append("$description\n\n")
                }

                if (link.isNotBlank()) {
                    contentBuilder.append("$link\n\n")
                }

                // Add additional fields
                additionalFields.forEach { field ->
                    if (field.text.isNotBlank()) {
                        contentBuilder.append("${field.label}: ${field.text}\n\n")
                    }
                }

                val finalContent = contentBuilder.toString().trim()

                // Create post with combined content and resource type
                viewModel.createPost(finalContent, "resource")
            }
        )

        // Show error message if any
        ErrorMessage(
            postCreationState = postCreationState,
            onClearError = { viewModel.clearError() }
        )
    }
}

// Data class for input field
data class InputFieldData(
    val id: String,
    val label: String,
    val text: String,
    val placeholder: String
)

@Composable
fun DynamicInputField(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    singleLine: Boolean = true,
    minHeight: Dp? = null,
    shouldUseDottedBorder: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    // Determine if field should have dotted border
    val useDottedBorder = shouldUseDottedBorder && text.isEmpty() && !isFocused

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (minHeight != null) {
                        Modifier.heightIn(min = minHeight)
                    } else Modifier
                )
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
                .then(
                    focusRequester?.let {
                        Modifier.focusRequester(it)
                    } ?: Modifier
                )
                .drawBehind {
                    // Draw dotted border directly on the TextField
                    if (useDottedBorder) {
                        val strokeWidth = 2.dp.toPx()
                        val dashLength = 8.dp.toPx()
                        val gapLength = 4.dp.toPx()

                        val pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashLength, gapLength),
                            0f
                        )

                        drawRoundRect(
                            color = Color.Gray.copy(alpha = 0.5f),
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = pathEffect
                            ),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                    }
                },
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (useDottedBorder)
                    Color.Transparent else MaterialTheme.colorScheme.outline,
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MediaFilesSection(
    selectedMediaFiles: List<File>,
    onRemoveFile: (File) -> Unit
) {
    Column {
        Text(
            text = "${selectedMediaFiles.size} files uploaded...",
            fontSize = 12.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp) // Increased from 8dp
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp) // Increased from 8dp
        ) {
            items(selectedMediaFiles.size) { index ->
                val file = selectedMediaFiles[index]
                MediaFileItem(
                    file = file,
                    onRemove = { onRemoveFile(file) }
                )
            }
        }
    }
}

@Composable
private fun MediaFileItem(
    file: File,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.size(64.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp") -> {
                    AsyncImage(
                        model = file,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                file.extension.lowercase() in listOf("mp4", "avi", "mov", "mkv", "3gp", "webm") -> {
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
                            modifier = Modifier.size(20.dp)
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
                    .size(18.dp)
                    .background(
                        Color.Black.copy(alpha = 0.7f),
                        CircleShape
                    )
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BottomActionsSection(
    topicText: String,
    descriptionText: String,
    linkText: String,
    additionalFields: List<InputFieldData>,
    selectedMediaFiles: List<File>,
    postCreationState: PostCreationState,
    onClick: () -> Unit,
    onImageClick: () -> Unit,
    onVideoClick: () -> Unit,
    onPdfClick: () -> Unit,
    onShareClick: (String, String, String, List<InputFieldData>) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Left side - Media buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Increased from 16dp
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaButton(
                    icon = R.drawable.ic_plus,
                    text = "Add media",
                    iconTint = Color(0xFFB787F5),
                    onClick = onClick
                )

                MediaButton(
                    icon = R.drawable.ic_image,
                    text = "",
                    iconTint = Color(0xFFB787F5),
                    onClick = onImageClick,
                    showText = false
                )

                MediaButton(
                    icon = R.drawable.ic_video,
                    text = "",
                    iconTint = Color(0xFFB787F5),
                    onClick = onVideoClick,
                    showText = false
                )

                MediaButton(
                    icon = R.drawable.ic_pdf,
                    text = "",
                    iconTint = Color(0xFFB787F5),
                    onClick = onPdfClick,
                    showText = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Increased from 20dp

        // Share button
        val isEnabled = postCreationState !is PostCreationState.Loading &&
                (topicText.isNotBlank() || descriptionText.isNotBlank() ||
                        linkText.isNotBlank() || selectedMediaFiles.isNotEmpty() ||
                        additionalFields.any { it.text.isNotBlank() })

        GradientButton(
            text = if (postCreationState is PostCreationState.Loading) "Sharing..." else "Share",
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled
        ) {
            if (isEnabled) {
                onShareClick(topicText, descriptionText, linkText, additionalFields)
            }
        }
    }
}

@Composable
private fun MediaButton(
    icon: Int,
    text: String,
    iconTint: Color,
    onClick: () -> Unit,
    showText: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp) // Increased from 4dp for better touch target
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )

        if (showText && text.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp)) // Increased from 6dp
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color(0xFFB787F5)
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    postCreationState: PostCreationState,
    onClearError: () -> Unit
) {
    if (postCreationState is PostCreationState.Error) {
        Spacer(modifier = Modifier.height(12.dp)) // Increased from 8dp
        Text(
            text = postCreationState.message,
            color = Color.Red,
            fontSize = 12.sp,
            modifier = Modifier.clickable { onClearError() }
        )
    }
}



//
//@Composable
//fun PostInputPopupResource(
//    viewModel: PostsViewModel,
//    modifier: Modifier = Modifier,
//    focusRequester: FocusRequester = remember { FocusRequester() },
//    onPostCreated: () -> Unit
//) {
//    val context = LocalContext.current
//
//    // State for dynamic input fields
//    var inputFields by remember {
//        mutableStateOf(
//            listOf(
//                InputFieldData(
//                    id = "topic",
//                    label = "Topic",
//                    text = "",
//                    placeholder = "Write your topic"
//                ),
//                InputFieldData(
//                    id = "description",
//                    label = "Description",
//                    text = "",
//                    placeholder = "Write your description"
//                ),
//                InputFieldData(
//                    id = "link",
//                    label = "Link",
//                    text = "",
//                    placeholder = "Add link here"
//                )
//            )
//        )
//    }
//
//    val postCreationState by viewModel.postCreationState.collectAsState()
//    val selectedMediaFiles by viewModel.selectedMediaFiles.collectAsState()
//
//    // Auto-focus when popup opens
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
//
//    // Image picker launcher
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//    // Video picker launcher
//    val videoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//    val pdfPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//
//    // Camera launcher
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success) {
//            // Handle camera result
//            // You'll need to implement camera file handling
//        }
//    }
//
//    // Document picker launcher
//    val documentPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//    // Handle post creation state
//    LaunchedEffect(postCreationState) {
//        when (postCreationState) {
//            is PostCreationState.Success -> {
//                // Reset all fields
//                inputFields = inputFields.map { it.copy(text = "") }
//                // Refresh the posts to show the new one
//                viewModel.fetchPosts(postType = "resource")
//                // Close the popup
//                onPostCreated()
//            }
//
//            is PostCreationState.Error -> {
//                // Handle error - could show a snackbar or toast
//            }
//
//            else -> {}
//        }
//    }
//
//    // Function to update input field text
//    fun updateInputField(id: String, newText: String) {
//        inputFields = inputFields.map { field ->
//            if (field.id == id) {
//                field.copy(text = newText)
//            } else field
//        }
//
//        // Add new dotted input field if user typed in the last field and it's not empty
//        val lastField = inputFields.lastOrNull()
//        if (lastField != null && lastField.text.isNotEmpty() && lastField.id == id) {
//            val newFieldId = "field_${System.currentTimeMillis()}"
//            inputFields = inputFields + InputFieldData(
//                id = newFieldId,
//                label = "Additional Info",
//                text = "",
//                placeholder = "Add more information"
//            )
//        }
//    }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//    ) {
//        // Dynamic input fields
//        inputFields.forEachIndexed { index, field ->
//            DynamicInputField(
//                label = field.label,
//                text = field.text,
//                onTextChange = { newText -> updateInputField(field.id, newText) },
//                placeholder = field.placeholder,
//                focusRequester = if (index == 0) focusRequester else null,
//                singleLine = field.id != "description",
//                minHeight = if (field.id == "description") 120.dp else null,
//                shouldUseDottedBorder = field.id == "link" || field.id.startsWith("field_") // Only link and additional fields get dotted border
//            )
//
////            Spacer(modifier = Modifier.height(16.dp))
//        }
//
//
////         Show selected media files
//        if (selectedMediaFiles.isNotEmpty()) {
//            MediaFilesSection(
//                selectedMediaFiles = selectedMediaFiles,
//                onRemoveFile = { file -> viewModel.removeMediaFile(file) }
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//        }
//
//        // Bottom section with attachments and share button
//        BottomActionsSection(
//            topicText = inputFields.find { it.id == "topic" }?.text ?: "",
//            descriptionText = inputFields.find { it.id == "description" }?.text ?: "",
//            linkText = inputFields.find { it.id == "link" }?.text ?: "",
//            additionalFields = inputFields.filter { it.id.startsWith("field_") },
//            selectedMediaFiles = selectedMediaFiles,
//            postCreationState = postCreationState,
//            onClick = {},
//            onImageClick = { imagePickerLauncher.launch("image/*") },
//            onVideoClick = { videoPickerLauncher.launch("video/*") },
//            onPdfClick = { pdfPickerLauncher.launch("application/pdf") },
//            onShareClick = { topic, description, link, additionalFields ->
//                // Create content by combining all fields
//                val contentBuilder = StringBuilder()
//
//                if (topic.isNotBlank()) {
//                    contentBuilder.append("$topic\n\n")
//                }
//
//                if (description.isNotBlank()) {
//                    contentBuilder.append("$description\n\n")
//                }
//
//                if (link.isNotBlank()) {
//                    contentBuilder.append("$link\n\n")
//                }
//
//                // Add additional fields
//                additionalFields.forEach { field ->
//                    if (field.text.isNotBlank()) {
//                        contentBuilder.append("${field.label}: ${field.text}\n\n")
//                    }
//                }
//
//                val finalContent = contentBuilder.toString().trim()
//
//                // Create post with combined content and resource type
//                viewModel.createPost(finalContent, "resource")
//            }
//        )
//
//        // Show error message if any
//        ErrorMessage(
//            postCreationState = postCreationState,
//            onClearError = { viewModel.clearError() }
//        )
//    }
//}
//
//
//// Data class for input field
//data class InputFieldData(
//    val id: String,
//    val label: String,
//    val text: String,
//    val placeholder: String
//)
//
//
//@Composable
//fun DynamicInputField(
//    label: String,
//    text: String,
//    onTextChange: (String) -> Unit,
//    placeholder: String,
//    modifier: Modifier = Modifier,
//    focusRequester: FocusRequester? = null,
//    singleLine: Boolean = true,
//    minHeight: Dp? = null,
//    shouldUseDottedBorder: Boolean = false
//) {
//    val focusManager = LocalFocusManager.current
//    var isFocused by remember { mutableStateOf(false) }
//
//    // Determine if field should have dotted border
//    val useDottedBorder = shouldUseDottedBorder && text.isEmpty() && !isFocused
//
//    Column(modifier = modifier) {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurface,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        OutlinedTextField(
//            value = text,
//            onValueChange = onTextChange,
//            placeholder = {
//                Text(
//                    text = placeholder,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .then(
//                    if (minHeight != null) {
//                        Modifier.heightIn(min = minHeight)
//                    } else Modifier
//                )
//                .onFocusChanged { focusState ->
//                    isFocused = focusState.isFocused
//                }
//                .then(
//                    focusRequester?.let {
//                        Modifier.focusRequester(it)
//                    } ?: Modifier
//                )
//                .drawBehind {
//                    // Draw dotted border directly on the TextField
//                    if (useDottedBorder) {
//                        val strokeWidth = 2.dp.toPx()
//                        val dashLength = 8.dp.toPx()
//                        val gapLength = 4.dp.toPx()
//
//                        val pathEffect = PathEffect.dashPathEffect(
//                            floatArrayOf(dashLength, gapLength),
//                            0f
//                        )
//
//                        drawRoundRect(
//                            color = Color.Gray.copy(alpha = 0.5f),
//                            style = Stroke(
//                                width = strokeWidth,
//                                pathEffect = pathEffect
//                            ),
//                            cornerRadius = CornerRadius(8.dp.toPx())
//                        )
//                    }
//                },
//            singleLine = singleLine,
//            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = MaterialTheme.colorScheme.primary,
//                unfocusedBorderColor = if (useDottedBorder)
//                    Color.Transparent else MaterialTheme.colorScheme.outline,
//            ),
//            shape = RoundedCornerShape(8.dp),
//            textStyle = MaterialTheme.typography.bodyMedium
//        )
//
//        // Remove the entire if (useDottedBorder) block - no longer needed!
//    }
//}
//
//
//@Composable
//private fun MediaFilesSection(
//    selectedMediaFiles: List<File>,
//    onRemoveFile: (File) -> Unit
//) {
//    Column {
//        Text(
//            text = "${selectedMediaFiles.size} files uploaded...",
//            fontSize = 12.sp,
//            color = Color(0xFF666666),
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        LazyRow(
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(selectedMediaFiles.size) { index ->
//                val file = selectedMediaFiles[index]
//                MediaFileItem(
//                    file = file,
//                    onRemove = { onRemoveFile(file) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun MediaFileItem(
//    file: File,
//    onRemove: () -> Unit
//) {
//    Card(
//        modifier = Modifier.size(64.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp") -> {
//                    AsyncImage(
//                        model = file,
//                        contentDescription = "Selected image",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//
//                file.extension.lowercase() in listOf("mp4", "avi", "mov", "mkv", "3gp", "webm") -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0xFF000000)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_play),
//                            contentDescription = "Video",
//                            tint = Color.White,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//
//                else -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0xFFF5F5F5)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = file.extension.uppercase(),
//                            fontSize = 10.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//
//            // Remove button
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .size(18.dp)
//                    .background(
//                        Color.Black.copy(alpha = 0.7f),
//                        CircleShape
//                    )
//                    .clickable { onRemove() },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "×",
//                    color = Color.White,
//                    fontSize = 10.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun BottomActionsSection(
//    topicText: String,
//    descriptionText: String,
//    linkText: String,
//    additionalFields: List<InputFieldData>,
//    selectedMediaFiles: List<File>,
//    postCreationState: PostCreationState,
//    onClick: () -> Unit,
//    onImageClick: () -> Unit,
//    onVideoClick: () -> Unit,
//    onPdfClick: () -> Unit,
//    onShareClick: (String, String, String, List<InputFieldData>) -> Unit
//) {
//    Column {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//        ) {
//            // Left side - Media buttons
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                MediaButton(
//                    icon = R.drawable.ic_plus,
//                    text = "Add media",
//                    iconTint = Color(0xFFB787F5),
//                    onClick = onClick
//                )
//
//                MediaButton(
//                    icon = R.drawable.ic_image,
//                    text = "",
//                    iconTint = Color(0xFFB787F5),
//                    onClick = onImageClick,
//                    showText = false
//                )
//
//                MediaButton(
//                    icon = R.drawable.ic_video,
//                    text = "",
//                    iconTint = Color(0xFFB787F5),
//                    onClick = onVideoClick,
//                    showText = false
//                )
//
//                MediaButton(
//                    icon = R.drawable.ic_pdf,
//                    text = "",
//                    iconTint = Color(0xFFB787F5),
//                    onClick = onPdfClick,
//                    showText = false
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(20.dp))
//
//        // Share button
//        val isEnabled = postCreationState !is PostCreationState.Loading &&
//                (topicText.isNotBlank() || descriptionText.isNotBlank() ||
//                        linkText.isNotBlank() || selectedMediaFiles.isNotEmpty() ||
//                        additionalFields.any { it.text.isNotBlank() })
//
//        GradientButton(
//            text = if (postCreationState is PostCreationState.Loading) "Sharing..." else "Share",
//            modifier = Modifier.fillMaxWidth(),
//            enabled = isEnabled
//        ) {
//            if (isEnabled) {
//                onShareClick(topicText, descriptionText, linkText, additionalFields)
//            }
//        }
//    }
//}
//
//@Composable
//private fun MediaButton(
//    icon: Int,
//    text: String,
//    iconTint: Color,
//    onClick: () -> Unit,
//    showText: Boolean = true
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .clickable { onClick() }
//            .padding(4.dp)
//    ) {
//        Icon(
//            painter = painterResource(id = icon),
//            contentDescription = text,
//            tint = iconTint,
//            modifier = Modifier.size(20.dp)
//        )
//
//        if (showText && text.isNotEmpty()) {
//            Spacer(modifier = Modifier.width(6.dp))
//            Text(
//                text = text,
//                fontSize = 12.sp,
//                color = Color(0xFF666666)
//            )
//        }
//    }
//}
//
//@Composable
//private fun ErrorMessage(
//    postCreationState: PostCreationState,
//    onClearError: () -> Unit
//) {
//    if (postCreationState is PostCreationState.Error) {
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = postCreationState.message,
//            color = Color.Red,
//            fontSize = 12.sp,
//            modifier = Modifier.clickable { onClearError() }
//        )
//    }
//}
//
//
//
















//package com.cc.creatorcircle.ui.screens.resourcehub
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ModalBottomSheet
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
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
//import androidx.compose.ui.draw.drawBehind
//import androidx.compose.ui.focus.FocusRequester
//import androidx.compose.ui.focus.focusRequester
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.PathEffect
//import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.data.models.Post
//import com.cc.creatorcircle.data.models.PostCreationState
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.ui.components.ShareBottomSheetContent
//import com.cc.creatorcircle.ui.components.TopBarHome
//import com.cc.creatorcircle.ui.screens.home.CommentBottomSheetContent
//import com.cc.creatorcircle.ui.screens.home.PostCardSection
//import com.cc.creatorcircle.ui.screens.home.PostInputPopup
//import com.cc.creatorcircle.ui.screens.home.PostInputPopupDialog
//import com.cc.creatorcircle.utils.TokenManager
//import com.cc.creatorcircle.utils.UserData
//import com.cc.creatorcircle.utils.UserDataManager
//import com.cc.creatorcircle.utils.createFileFromUri
//import com.cc.creatorcircle.viewModel.ConnectionRequestState
//import com.cc.creatorcircle.viewModel.ConnectionViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//import java.io.File
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ResourceHub(
//    navController: NavController,
//    onCommentClick: (Post) -> Unit = {},
//    onShareClick: (Post) -> Unit = {}
//) {
//    val context = LocalContext.current
//    val viewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    var showPopup by remember { mutableStateOf(false) }
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
//
//    // Get auth token using TokenManager
//    val tokenManager = remember { TokenManager(context) }
//    val token = remember { tokenManager.getToken() }
//
//    // Fetch resource posts and user profile when the composable is first launched
//    LaunchedEffect(Unit) {
//        // Fetch resource posts specifically
//        viewModel.fetchPosts(postType = "resource")
//
//        // Fetch user profile if userData is empty
//        if (userData.username.isEmpty()) {
//            viewModel.fetchUserProfile()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopBarHome(
//                tabs = listOf("Feed", "Resources"),
//                selectedTab = "Resources", // This should be "Resources" since you're in ResourceHub
//                navController = navController,
//                onTabSelected = { tab ->
//                    // Make sure this doesn't try to navigate to "feed"
//                    when (tab) {
//                        "Feed" -> navController.navigate("home") // NOT "feed"
//                        "Resources" -> { /* Already here */
//                        }
//                    }
//                }
//            )
//        },
//        bottomBar = {
//            BottomNavBar(navController = navController)
//        }
//    ) { paddingValues -> // Fixed: Added paddingValues parameter
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues) // Fixed: Apply scaffold padding
//                .background(Color(0xFFF5F5F5))
//        ) {
//
//            // Profile Card Component
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(12.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color.White
//                ),
//                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Profile Image
//                    Card(
//                        modifier = Modifier.size(48.dp),
//                        shape = CircleShape,
//                        colors = CardDefaults.cardColors(
//                            containerColor = Color.Gray
//                        )
//                    ) {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            if (userData.profilePic?.isNotEmpty() == true) {
//                                AsyncImage(
//                                    model = userData.profilePic,
//                                    contentDescription = "Profile Picture",
//                                    modifier = Modifier
//                                        .fillMaxSize()
//                                        .clip(CircleShape),
//                                    contentScale = ContentScale.Crop,
//                                    fallback = painterResource(id = R.drawable.ic_profile),
//                                    error = painterResource(id = R.drawable.ic_profile)
//                                )
//                            } else {
//                                // Placeholder for profile image
//                                Icon(
//                                    imageVector = Icons.Default.Person,
//                                    contentDescription = "Profile",
//                                    tint = Color.White,
//                                    modifier = Modifier.size(24.dp)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.width(12.dp))
//
//                    // User Info
//                    Column(
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Text(
//                            text = userData.username.ifEmpty { "Loading..." },
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color.Black
//                        )
//                    }
//
//                    GradientButton(
//                        text = "Share your post",
//                        onClick = {
//                            showPopup = true
//                        }
//                    )
//                }
//            }
//
//            // Posts Content
//            when {
//                isLoading -> {
//                    // Loading state
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator(
//                            color = Color(0xFF8B5CF6)
//                        )
//                    }
//                }
//
//                error != null -> {
//                    // Error state
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = error!!,
//                            color = Color.Red,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                }
//
//                posts.isEmpty() -> {
//                    // Empty state
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No resource posts available",
//                            color = Color.Gray,
//                            fontSize = 16.sp
//                        )
//                    }
//                }
//
//                else -> {
//                    // Posts list
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        state = lazyListState // Fixed: Use the declared lazyListState
//                    ) {
//                        items(posts) { post ->
//                            PostCardSection(
//                                post = post,
//                                userProfile = userProfile,
//                                connectionViewModel = connectionViewModel,
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
//                                }
//                            )
//                        }
//                    }
//                }
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
//        // Post Creation Popup
//        if (showPopup) {
//            PostInputPopupDialogResource(
//                viewModel = viewModel,
//                username = userData.username.ifEmpty { "Loading..." },
//                profilePic = userData.profilePic,
//                onDismiss = { showPopup = false }
//            )
//        }
//    }
//}
//
//
//
//
//@Composable
//fun PostInputPopupDialogResource(
//    viewModel: PostsViewModel,
//    username: String,
//    profilePic: String?,
//    onDismiss: () -> Unit
//) {
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(screenHeight * 4f / 5f) // 2/3 of screen height
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(20.dp)
//            ) {
//                // Header with user info and close button
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // User Avatar
//                        Box(
//                            modifier = Modifier
//                                .size(40.dp)
//                                .background(
//                                    Color(0xFFE0E0E0),
//                                    CircleShape
//                                ),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            // You can replace this with actual user image
//                            AsyncImage(
//                                model = profilePic,
//                                contentDescription = "User Avatar",
//                                modifier = Modifier
//                                    .size(40.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(12.dp))
//
//                        Column {
//                            Text(
//                                text = username, // Replace with actual user name
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.Black
//                            )
//                            Text(
//                                text = "Member",
//                                fontSize = 12.sp,
//                                color = Color(0xFF666666)
//                            )
//                        }
//                    }
//
//                    // Close button
//                    Box(
//                        modifier = Modifier
//                            .size(32.dp)
//                            .background(
//                                Color(0xFFF5F5F5),
//                                CircleShape
//                            )
//                            .clickable { onDismiss() },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "×",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF666666)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//
//                PostInputPopupResource(
//                    viewModel = viewModel,
//                    modifier = Modifier.weight(1f)
//                )
//
//
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun PostInputPopupResource(
//    viewModel: PostsViewModel,
//    modifier: Modifier = Modifier,
//    focusRequester: FocusRequester = remember { FocusRequester() }
//) {
//    val context = LocalContext.current
//
//    // State for dynamic input fields
//    var inputFields by remember {
//        mutableStateOf(
//            listOf(
//                InputFieldData(
//                    id = "topic",
//                    label = "Topic",
//                    text = "",
//                    placeholder = "Write your topic"
//                ),
//                InputFieldData(
//                    id = "description",
//                    label = "Description",
//                    text = "",
//                    placeholder = "Write your description"
//                ),
//                InputFieldData(
//                    id = "link",
//                    label = "Link",
//                    text = "",
//                    placeholder = "Add link here"
//                )
//            )
//        )
//    }
//
//    val postCreationState by viewModel.postCreationState.collectAsState()
//    val selectedMediaFiles by viewModel.selectedMediaFiles.collectAsState()
//
//    // Auto-focus when popup opens
//    LaunchedEffect(Unit) {
//        focusRequester.requestFocus()
//    }
//
//    // Image picker launcher
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//    // Video picker launcher
//    val videoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//    val PdfPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//
//    // Camera launcher
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success) {
//            // Handle camera result
//            // You'll need to implement camera file handling
//        }
//    }
//
//    // Document picker launcher
//    val documentPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { selectedUri ->
//            val file = createFileFromUri(context, selectedUri)
//            file?.let { viewModel.addMediaFile(it) }
//        }
//    }
//
//    // Handle post creation state
//    LaunchedEffect(postCreationState) {
//        when (postCreationState) {
//            is PostCreationState.Success -> {
//                // Reset all fields
//                inputFields = inputFields.map { it.copy(text = "") }
//            }
//
//            is PostCreationState.Error -> {
//                // Handle error - could show a snackbar or toast
//            }
//
//            else -> {}
//        }
//    }
//
//    // Function to update input field text
//    fun updateInputField(id: String, newText: String) {
//        inputFields = inputFields.map { field ->
//            if (field.id == id) {
//                field.copy(text = newText)
//            } else field
//        }
//
//        // Add new dotted input field if user typed in the last field and it's not empty
//        val lastField = inputFields.lastOrNull()
//        if (lastField != null && lastField.text.isNotEmpty() && lastField.id == id) {
//            val newFieldId = "field_${System.currentTimeMillis()}"
//            inputFields = inputFields + InputFieldData(
//                id = newFieldId,
//                label = "Additional Info",
//                text = "",
//                placeholder = "Add more information"
//            )
//        }
//    }
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .verticalScroll(rememberScrollState())
//    ) {
//        // Dynamic input fields
//        inputFields.forEachIndexed { index, field ->
//            DynamicInputField(
//                label = field.label,
//                text = field.text,
//                onTextChange = { newText -> updateInputField(field.id, newText) },
//                placeholder = field.placeholder,
//                focusRequester = if (index == 0) focusRequester else null,
//                singleLine = field.id != "description",
//                minHeight = if (field.id == "description") 120.dp else null,
//                shouldUseDottedBorder = field.id == "link" || field.id.startsWith("field_") // Only link and additional fields get dotted border
//            )
//
////            Spacer(modifier = Modifier.height(16.dp))
//        }
//
//
////         Show selected media files
//        if (selectedMediaFiles.isNotEmpty()) {
//            MediaFilesSection(
//                selectedMediaFiles = selectedMediaFiles,
//                onRemoveFile = { file -> viewModel.removeMediaFile(file) }
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//        }
//
//        // Bottom section with attachments and share button
//        BottomActionsSection(
//            topicText = inputFields.find { it.id == "topic" }?.text ?: "",
//            descriptionText = inputFields.find { it.id == "description" }?.text ?: "",
//            linkText = inputFields.find { it.id == "link" }?.text ?: "",
//            selectedMediaFiles = selectedMediaFiles,
//            postCreationState = postCreationState,
//            onClick = {},
//            onImageClick = { imagePickerLauncher.launch("image/*") },
//            onVideoClick = { videoPickerLauncher.launch("video/*") },
//            onPdfClick = { PdfPickerLauncher.launch("pdf/*") },
//            onShareClick = {
//                // Extract data from input fields
//                val topic = inputFields.find { it.id == "topic" }?.text?.trim() ?: ""
//                val description = inputFields.find { it.id == "description" }?.text?.trim() ?: ""
//                val link = inputFields.find { it.id == "link" }?.text?.trim() ?: ""
//
//
//            }
//        )
//
//        GradientButton("Share", modifier = Modifier.fillMaxWidth()) { }
//
//        // Show error message if any
//        ErrorMessage(
//            postCreationState = postCreationState,
//            onClearError = { viewModel.clearError() }
//        )
//    }
//}
//
//
//// Data class for input field
//data class InputFieldData(
//    val id: String,
//    val label: String,
//    val text: String,
//    val placeholder: String
//)
//
//
//@Composable
//fun DynamicInputField(
//    label: String,
//    text: String,
//    onTextChange: (String) -> Unit,
//    placeholder: String,
//    modifier: Modifier = Modifier,
//    focusRequester: FocusRequester? = null,
//    singleLine: Boolean = true,
//    minHeight: Dp? = null,
//    shouldUseDottedBorder: Boolean = false
//) {
//    val focusManager = LocalFocusManager.current
//    var isFocused by remember { mutableStateOf(false) }
//
//    // Determine if field should have dotted border
//    val useDottedBorder = shouldUseDottedBorder && text.isEmpty() && !isFocused
//
//    Column(modifier = modifier) {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurface,
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        OutlinedTextField(
//            value = text,
//            onValueChange = onTextChange,
//            placeholder = {
//                Text(
//                    text = placeholder,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                )
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .then(
//                    if (minHeight != null) {
//                        Modifier.heightIn(min = minHeight)
//                    } else Modifier
//                )
//                .onFocusChanged { focusState ->
//                    isFocused = focusState.isFocused
//                }
//                .then(
//                    focusRequester?.let {
//                        Modifier.focusRequester(it)
//                    } ?: Modifier
//                )
//                .drawBehind {
//                    // Draw dotted border directly on the TextField
//                    if (useDottedBorder) {
//                        val strokeWidth = 2.dp.toPx()
//                        val dashLength = 8.dp.toPx()
//                        val gapLength = 4.dp.toPx()
//
//                        val pathEffect = PathEffect.dashPathEffect(
//                            floatArrayOf(dashLength, gapLength),
//                            0f
//                        )
//
//                        drawRoundRect(
//                            color = Color.Gray.copy(alpha = 0.5f),
//                            style = Stroke(
//                                width = strokeWidth,
//                                pathEffect = pathEffect
//                            ),
//                            cornerRadius = CornerRadius(8.dp.toPx())
//                        )
//                    }
//                },
//            singleLine = singleLine,
//            maxLines = if (singleLine) 1 else Int.MAX_VALUE,
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = MaterialTheme.colorScheme.primary,
//                unfocusedBorderColor = if (useDottedBorder)
//                    Color.Transparent else MaterialTheme.colorScheme.outline,
//            ),
//            shape = RoundedCornerShape(8.dp),
//            textStyle = MaterialTheme.typography.bodyMedium
//        )
//
//        // Remove the entire if (useDottedBorder) block - no longer needed!
//    }
//}
//
//
//@Composable
//private fun MediaFilesSection(
//    selectedMediaFiles: List<File>,
//    onRemoveFile: (File) -> Unit
//) {
//    Column {
//        Text(
//            text = "${selectedMediaFiles.size} images uploaded...",
//            fontSize = 12.sp,
//            color = Color(0xFF666666),
//            modifier = Modifier.padding(bottom = 8.dp)
//        )
//
//        LazyRow(
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            items(selectedMediaFiles.size) { index ->
//                val file = selectedMediaFiles[index]
//                MediaFileItem(
//                    file = file,
//                    onRemove = { onRemoveFile(file) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun MediaFileItem(
//    file: File,
//    onRemove: () -> Unit
//) {
//    Card(
//        modifier = Modifier.size(64.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Box(modifier = Modifier.fillMaxSize()) {
//            when {
//                file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "webp") -> {
//                    AsyncImage(
//                        model = file,
//                        contentDescription = "Selected image",
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop
//                    )
//                }
//
//                file.extension.lowercase() in listOf("mp4", "avi", "mov", "mkv", "3gp", "webm") -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0xFF000000)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_play),
//                            contentDescription = "Video",
//                            tint = Color.White,
//                            modifier = Modifier.size(20.dp)
//                        )
//                    }
//                }
//
//                else -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0xFFF5F5F5)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = file.extension.uppercase(),
//                            fontSize = 10.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//
//            // Remove button
//            Box(
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .size(18.dp)
//                    .background(
//                        Color.Black.copy(alpha = 0.7f),
//                        CircleShape
//                    )
//                    .clickable { onRemove() },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "×",
//                    color = Color.White,
//                    fontSize = 10.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun BottomActionsSection(
//    topicText: String,
//    descriptionText: String,
//    linkText: String,
//    selectedMediaFiles: List<File>,
//    postCreationState: PostCreationState,
//    onClick: () -> Unit,
//    onImageClick: () -> Unit,
//    onVideoClick: () -> Unit,
//    onPdfClick: () -> Unit,
//    onShareClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
////        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Left side - Media buttons
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            MediaButton(
//                icon = R.drawable.ic_plus,
//                text = "Add media",
//                iconTint = Color(0xFFB787F5),
//                onClick = onClick
//            )
//
//            MediaButton(
//                icon = R.drawable.ic_image,
//                text = "",
//                iconTint = Color(0xFFB787F5),
//                onClick = onImageClick,
//                showText = false
//            )
//
//            MediaButton(
//                icon = R.drawable.ic_video,
//                text = "",
//                iconTint = Color(0xFFB787F5),
//                onClick = onVideoClick,
//                showText = false
//            )
//
//
//
//            MediaButton(
//                icon = R.drawable.ic_pdf,
//                text = "",
//                iconTint = Color(0xFFB787F5),
//                onClick = onPdfClick,
//                showText = false
//            )
//
//
//        }
//
//        // Right side - Share button
//        val isEnabled = postCreationState !is PostCreationState.Loading &&
//                (topicText.isNotBlank() || descriptionText.isNotBlank() ||
//                        linkText.isNotBlank() || selectedMediaFiles.isNotEmpty())
//
//    }
//}
//
//@Composable
//private fun MediaButton(
//    icon: Int,
//    text: String,
//    iconTint: Color,
//    onClick: () -> Unit?,
//    showText: Boolean = true
//) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .clickable { onClick() }
//            .padding(4.dp)
//    ) {
//        Icon(
//            painter = painterResource(id = icon),
//            contentDescription = text,
//            tint = iconTint,
//            modifier = Modifier.size(20.dp)
//        )
//
//        if (showText && text.isNotEmpty()) {
//            Spacer(modifier = Modifier.width(6.dp))
//            Text(
//                text = text,
//                fontSize = 12.sp,
//                color = Color(0xFF666666)
//            )
//        }
//    }
//}
//
//@Composable
//private fun ErrorMessage(
//    postCreationState: PostCreationState,
//    onClearError: () -> Unit
//) {
//    if (postCreationState is PostCreationState.Error) {
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = postCreationState.message,
//            color = Color.Red,
//            fontSize = 12.sp,
//            modifier = Modifier.clickable { onClearError() }
//        )
//    }
//}

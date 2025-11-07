package com.cc.creatorcircle.ui.screens.sabo

import android.R.attr.onClick
import android.content.Context
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.R
import com.cc.creatorcircle.viewModel.websocket.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import com.cc.creatorcircle.data.socket.ChatMessage
import com.cc.creatorcircle.ui.components.TopBarSabo
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.data.socket.ConnectionState

data class FeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val prompt: String = ""
)




//@Composable
//fun SaboAIScreen(
//    navController: NavController,
//    viewModel: ChatViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val postViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
//        com.cc.creatorcircle.viewModel.ChatViewModel(context)
//    }
//
//    // User profile states
//    val userProfile by postViewModel.userProfile.collectAsState()
//    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
//    val profileLoading by chatViewModel.profileLoading.collectAsState()
//    val profileError by chatViewModel.profileError.collectAsState()
//
//    // Chat history states
//    val chatHistory by chatViewModel.chatHistory.collectAsState()
//    val historyLoading by chatViewModel.historyLoading.collectAsState()
//    val historyError by chatViewModel.historyError.collectAsState()
//
//    // Chat messages states
//    val chatMessages by chatViewModel.messages.collectAsState()
//    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
//    val messagesError by chatViewModel.messagesError.collectAsState()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var showChat by remember { mutableStateOf(false) }
//    var isMenuOpen by remember { mutableStateOf(false) }
//    var viewingHistoricalChat by remember { mutableStateOf(false) }
//    var isNewChat by remember { mutableStateOf(false) }
//
//    // Detect keyboard visibility
//    val isKeyboardOpen by keyboardAsState()
//
//    val activeProfile = chatUserProfiles.find { it.isActive }
//
//    // Get SharedPreferences
//    val sharedPreferences = remember {
//        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//    }
//
//    // Load sessionId from SharedPreferences
//    var currentSessionId by remember {
//        mutableStateOf<Int?>(
//            sharedPreferences.getInt("chatSessionId", -1).takeIf { it != -1 }
//        )
//    }
//
//    val currentProfileId by remember {
//        derivedStateOf {
//            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
//        }
//    }
//
//    // Track the last session ID to detect changes
//    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }
//
//    val userId = userProfile?.id ?: -1
//
//    // Socket.IO states
//    val socketIOMessages by viewModel.messages.collectAsState()
//    val connectionState by viewModel.connectionState.collectAsState()
//    val newSessionId by viewModel.newSessionId.collectAsState() // OBSERVE THIS
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//
//    // Fetch user profile and initialize chat session
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//        if (sessionId == -1) {
//            isNewChat = true
//        }
//    }
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let {
//            chatViewModel.fetchChatUserProfiles(it.id)
//        }
//    }
//
//    // Detect session changes and clear socket messages
//    LaunchedEffect(currentSessionId) {
//        if (currentSessionId != lastSessionId) {
//            Log.d(
//                "SaboAIScreen",
//                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
//            )
//            viewModel.clearMessages()
//            lastSessionId = currentSessionId
//        }
//    }
//
//    // NEW: Listen for new session ID from socket
//    LaunchedEffect(newSessionId) {
//        newSessionId?.let { sessionId ->
//            Log.d("SaboAIScreen", "New session ID received from socket: $sessionId")
//
//            // CRITICAL: Update currentSessionId and save to SharedPreferences
//            currentSessionId = sessionId
//            sharedPreferences.edit()
//                .putInt("chatSessionId", sessionId)
//                .apply()
//
//            // CRITICAL: Set isNewChat to false so subsequent messages use sessionId
//            isNewChat = false
//
//            Log.d("SaboAIScreen", "Session ID saved. isNewChat set to false. Future messages will use sessionId: $sessionId")
//
//            // Reconnect with new session ID
//            viewModel.connect(userId, sessionId)
//
//            // Clear the session ID from the flow
//            viewModel.clearNewSessionId()
//        }
//    }
//
//    // Connect to Socket.IO
//    LaunchedEffect(userId, currentSessionId) {
//        if (userId != -1) {
//            val sessionId = currentSessionId ?: -1
//            Log.d(
//                "SaboAIScreen",
//                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
//            )
//            viewModel.connect(userId, sessionId)
//        }
//    }
//
//    LaunchedEffect(chatUserProfiles) {
//        if (chatUserProfiles.isNotEmpty()) {
//            val activeProfile = chatUserProfiles.find { it.isActive }
//            Log.d(
//                "SaboAIScreen",
//                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
//            )
//
//            if (activeProfile != null) {
//                sharedPreferences.edit()
//                    .putInt("chatProfileId", activeProfile.id)
//                    .apply()
//
//                chatViewModel.fetchChatHistory(
//                    userId = userId,
//                    platform = activeProfile.platform,
//                    profileId = activeProfile.id
//                )
//
//                val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//                if (sessionId == -1) {
//                    isNewChat = true
//                    Log.d("SaboAIScreen", "New chat mode activated for profile: ${activeProfile.username}")
//                }
//            } else {
//                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
//                sharedPreferences.edit()
//                    .remove("chatProfileId")
//                    .remove("chatSessionId")
//                    .apply()
//                isNewChat = false
//            }
//        }
//    }
//
//    // Auto-scroll to bottom when new messages arrive
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
//            coroutineScope.launch {
//                val totalMessages = if (viewingHistoricalChat) {
//                    chatMessages.size + socketIOMessages.size
//                } else {
//                    socketIOMessages.size
//                }
//                if (totalMessages > 0) {
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    // Show chat when there are messages
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
//            showChat = true
//        }
//    }
//
//    val featureCards = listOf(
//        FeatureCard(
//            title = "Profile Enhance",
//            description = "Optimize your social media profile for...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Help me optimize my social media profile for maximum impact and engagement"
//        ),
//        FeatureCard(
//            title = "Bio Enhance",
//            description = "Create compelling bio that converts viewers...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create a compelling bio that converts viewers into followers"
//        ),
//        FeatureCard(
//            title = "Content idea creation",
//            description = "Generate fresh content ideas for your niche",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Generate fresh and creative content ideas for my social media niche"
//        ),
//        FeatureCard(
//            title = "Engaging content idea",
//            description = "Create content that drives engagement",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create content ideas that drive high engagement and interactions"
//        ),
//        FeatureCard(
//            title = "Suggest catchy blog post",
//            description = "Get ideas for compelling blog posts",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Suggest catchy and viral blog post ideas for my audience"
//        ),
//        FeatureCard(
//            title = "Unique reel creation",
//            description = "Create unique and viral reel concepts",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Help me create unique and viral reel concepts that stand out"
//        ),
//        FeatureCard(
//            title = "Analyze the current trends",
//            description = "Stay ahead with current social media trends",
//            icon = Icons.Default.TrendingUp,
//            backgroundColor = Color(0xFFFF9800),
//            prompt = "Analyze current social media trends and help me stay ahead of the curve"
//        ),
//        FeatureCard(
//            title = "Hashtag Strategy",
//            description = "Develop a winning hashtag strategy for growth",
//            icon = Icons.Default.Tag,
//            backgroundColor = Color(0xFFE91E63),
//            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
//        )
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            topBar = {
//                TopBarSabo(
//                    activeProfile = activeProfile,
//                    onClick = { isMenuOpen = true }
//                )
//            },
//            bottomBar = {
//                if (!isKeyboardOpen) {
//                    BottomNavBar(navController = navController)
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        ) { paddingValues ->
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                val shouldShowChatInterface = showChat &&
//                        (socketIOMessages.isNotEmpty() ||
//                                (viewingHistoricalChat && chatMessages.isNotEmpty()))
//
//                if (shouldShowChatInterface) {
//                    ChatInterface(
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            Log.d(
//                                "SaboAIScreen",
//                                "current session ID$currentSessionId, isNewChat: $isNewChat"
//                            )
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//                                // Check if we have a valid session ID first
//
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "current session ID$currentSessionId, isNewChat: $isNewChat"
//                                )
//
//                                val sessionId = currentSessionId
//
//                                if (sessionId != null && sessionId != -1 && !isNewChat) {
//                                    // We have a session ID, use it
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with existing sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                } else {
//                                    // No session ID, create new message
//                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                    if (profileId != -1) {
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating NEW chat with userId: $userId, profileId: $profileId (isNewChat: $isNewChat)"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                        // isNewChat will be set to false when newSessionId is received
//                                    } else {
//                                        Log.e(
//                                            "SaboAIScreen",
//                                            "No profileId found in SharedPreferences"
//                                        )
//                                    }
//                                }
//                                searchQuery = ""
//                            }
//                        },
//                        connectionState = connectionState,
//                        socketIOMessages = socketIOMessages,
//                        historicalMessages = chatMessages,
//                        isViewingHistory = viewingHistoricalChat,
//                        messagesLoading = messagesLoading,
//                        messagesError = messagesError,
//                        listState = listState,
//                        onBackClick = {
//                            showChat = false
//                            viewingHistoricalChat = false
//                            isNewChat = false
//                            currentSessionId = null
//                            sharedPreferences.edit().remove("chatSessionId").apply()
//                            chatViewModel.clearCurrentSession()
//                            viewModel.clearMessages()
//                        }
//                    )
//                } else {
//                    FeatureCardsInterface(
//                        activeProfile = activeProfile,
//                        featureCards = featureCards,
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty()) {
//                                // Check if we have a valid session ID first
//                                val sessionId = currentSessionId
//
//                                if (sessionId != null && sessionId != -1 && !isNewChat) {
//                                    // We have a session ID, use it
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with existing sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                } else {
//                                    // No session ID, create new message
//                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                    if (profileId != -1) {
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating NEW chat with userId: $userId, profileId: $profileId (isNewChat: $isNewChat)"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                    } else {
//                                        Log.e(
//                                            "SaboAIScreen",
//                                            "No profileId found - cannot create new message"
//                                        )
//                                    }
//                                }
//                                searchQuery = ""
//                                showChat = true
//                            }
//                        },
//                        onFeatureCardClick = { card ->
//                            // Check if we have a valid session ID first
//                            val sessionId = currentSessionId
//
//                            if (sessionId != null && sessionId != -1 && !isNewChat) {
//                                // We have a session ID, use it
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Sending feature card message with existing sessionId: $sessionId"
//                                )
//                                viewModel.sendMessage(
//                                    userId = userId,
//                                    sessionId = sessionId,
//                                    message = card.prompt
//                                )
//                            } else {
//                                // No session ID, create new message
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                if (profileId != -1) {
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Creating NEW chat from feature card with userId: $userId, profileId: $profileId"
//                                    )
//                                    viewModel.createNewMessage(
//                                        userId = userId,
//                                        profileId = profileId,
//                                        message = card.prompt
//                                    )
//                                } else {
//                                    Log.e(
//                                        "SaboAIScreen",
//                                        "No profileId found - cannot create new message"
//                                    )
//                                }
//                            }
//                            showChat = true
//                            viewingHistoricalChat = false
//                        }
//                    )
//                }
//            }
//        }
//
//        // Side Menu Overlay
//        if (isMenuOpen) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { isMenuOpen = false }
//            )
//
//            SideMenu(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(280.dp)
//                    .background(Color.White),
//                onClose = { isMenuOpen = false },
//                onAnalyzeProfileClick = { message ->
//                    // Check if we have a valid session ID first
//                    val sessionId = currentSessionId
//
//                    if (sessionId != null && sessionId != -1 && !isNewChat) {
//                        // We have a session ID, use it
//                        Log.d(
//                            "SaboAIScreen",
//                            "Sending analyze profile message with existing sessionId: $sessionId"
//                        )
//                        viewModel.sendMessage(
//                            userId = userId,
//                            sessionId = sessionId,
//                            message = message
//                        )
//                    } else {
//                        // No session ID, create new message
//                        val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                        if (profileId != -1) {
//                            Log.d(
//                                "SaboAIScreen",
//                                "Creating NEW chat from analyze profile with userId: $userId, profileId: $profileId"
//                            )
//                            viewModel.createNewMessage(
//                                userId = userId,
//                                profileId = profileId,
//                                message = message
//                            )
//                        } else {
//                            Log.e(
//                                "SaboAIScreen",
//                                "No profileId found - cannot create new message"
//                            )
//                        }
//                    }
//
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isMenuOpen = false
//                },
//                onStartChatting = {
//                    isMenuOpen = false
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isNewChat = true
//                    currentSessionId = null
//                    sharedPreferences.edit().remove("chatSessionId").apply()
//                    chatViewModel.clearCurrentSession()
//                    viewModel.clearMessages()
//                },
//                chatUserProfiles = chatUserProfiles,
//                isLoadingProfiles = profileLoading,
//                profileError = profileError,
//                onRefreshProfiles = {
//                    userProfile?.let {
//                        chatViewModel.fetchChatUserProfiles(it.id)
//                    }
//                },
//                chatViewModel = chatViewModel,
//                userId = userId,
//                chatHistory = chatHistory,
//                historyLoading = historyLoading,
//                historyError = historyError,
//                onChatHistoryClick = { session ->
//                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
//
//                    sharedPreferences.edit()
//                        .putInt("chatSessionId", session.sessionId)
//                        .apply()
//
//                    currentSessionId = session.sessionId
//                    chatViewModel.fetchChatMessages(session.sessionId)
//
//                    viewingHistoricalChat = true
//                    isMenuOpen = false
//                    showChat = true
//                },
//                onRefreshHistory = {
//                    val activeProfile = chatUserProfiles.find { it.isActive }
//                    Log.d(
//                        "SideMenu",
//                        "Refreshing history for active profile: ${activeProfile?.username}"
//                    )
//
//                    if (activeProfile != null) {
//                        val profileId = activeProfile.id
//                        chatViewModel.fetchChatHistory(
//                            userId = userId,
//                            platform = activeProfile.platform,
//                            profileId = profileId
//                        )
//                        sharedPreferences.edit()
//                            .putInt("chatProfileId", profileId)
//                            .apply()
//                    } else {
//                        chatViewModel.fetchChatHistory(userId = userId)
//                    }
//                },
//                onSessionChanged = { newSessionId ->
//                    currentSessionId = newSessionId
//                    if (newSessionId != null) {
//                        sharedPreferences.edit()
//                            .putInt("chatSessionId", newSessionId)
//                            .apply()
//                    } else {
//                        sharedPreferences.edit().remove("chatSessionId").apply()
//                    }
//                }
//            )
//        }
//    }
//}



@Composable
fun SaboAIScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val postViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
        com.cc.creatorcircle.viewModel.ChatViewModel(context)
    }

    // User profile states
    val userProfile by postViewModel.userProfile.collectAsState()
    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
    val profileLoading by chatViewModel.profileLoading.collectAsState()
    val profileError by chatViewModel.profileError.collectAsState()

    // Chat history states
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    val historyLoading by chatViewModel.historyLoading.collectAsState()
    val historyError by chatViewModel.historyError.collectAsState()

    // Chat messages states
    val chatMessages by chatViewModel.messages.collectAsState()
    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
    val messagesError by chatViewModel.messagesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var viewingHistoricalChat by remember { mutableStateOf(false) }
    var isNewChat by remember { mutableStateOf(false) }

    // Detect keyboard visibility
    val isKeyboardOpen by keyboardAsState()

    val activeProfile = chatUserProfiles.find { it.isActive }

    // Get SharedPreferences
    val sharedPreferences = remember {
        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
    }

    // Load sessionId from SharedPreferences
    var currentSessionId by remember {
        mutableStateOf<Int?>(
            sharedPreferences.getInt("chatSessionId", -1).takeIf { it != -1 }
        )
    }

    val currentProfileId by remember {
        derivedStateOf {
            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
        }
    }

    // Track the last session ID to detect changes
    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }

    val userId = userProfile?.id ?: -1

    // Socket.IO states
    val socketIOMessages by viewModel.messages.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val newSessionId by viewModel.newSessionId.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Fetch user profile and initialize chat session
    LaunchedEffect(Unit) {
        postViewModel.fetchUserProfile()

        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
        if (sessionId == -1) {
            isNewChat = true
        }



        // Clear sessionId from SharedPreferences
        val sharedPreferences =
            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove("chatSessionId").apply()

        // Clear messages
        chatViewModel.clearCurrentSession()

        // NOTE: We're NOT calling createNewChatSession here anymore
        // Just trigger the callback to open chat screen in "new chat" mode

    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            chatViewModel.fetchChatUserProfiles(it.id)
        }
    }

    // Detect session changes and clear socket messages ONLY if switching to a different session
    LaunchedEffect(currentSessionId) {
        if (currentSessionId != lastSessionId) {
            Log.d(
                "SaboAIScreen",
                "Session changed from $lastSessionId to $currentSessionId"
            )

            // Only clear messages if we're switching to a DIFFERENT existing session
            // Don't clear when creating a new session (going from null to a new ID)
            if (lastSessionId != null && currentSessionId != null && lastSessionId != currentSessionId) {
                Log.d("SaboAIScreen", "Clearing messages due to session switch")
                viewModel.clearMessages()
            }

            lastSessionId = currentSessionId
        }
    }

    // NEW: Listen for new session ID from socket
    LaunchedEffect(newSessionId) {
        newSessionId?.let { sessionId ->
            Log.d("SaboAIScreen", "New session ID received from socket: $sessionId")

            // CRITICAL: Update currentSessionId and save to SharedPreferences
            currentSessionId = sessionId
            sharedPreferences.edit()
                .putInt("chatSessionId", sessionId)
                .apply()

            // CRITICAL: Set isNewChat to false so subsequent messages use sessionId
            isNewChat = false

            Log.d("SaboAIScreen", "Session ID saved. isNewChat set to false. Future messages will use sessionId: $sessionId")

            // Reconnect with new session ID
            viewModel.connect(userId, sessionId)

            // Clear the session ID from the flow
            viewModel.clearNewSessionId()
        }
    }

    // Connect to Socket.IO
    LaunchedEffect(userId, currentSessionId) {
        if (userId != -1) {
            val sessionId = currentSessionId ?: -1
            Log.d(
                "SaboAIScreen",
                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
            )
            viewModel.connect(userId, sessionId)
        }
    }

    LaunchedEffect(chatUserProfiles) {
        if (chatUserProfiles.isNotEmpty()) {
            val activeProfile = chatUserProfiles.find { it.isActive }
            Log.d(
                "SaboAIScreen",
                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
            )

            if (activeProfile != null) {
                sharedPreferences.edit()
                    .putInt("chatProfileId", activeProfile.id)
                    .apply()

                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )

                val sessionId = sharedPreferences.getInt("chatSessionId", -1)
                if (sessionId == -1) {
                    isNewChat = true
                    Log.d("SaboAIScreen", "New chat mode activated for profile: ${activeProfile.username}")
                }
            } else {
                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
                sharedPreferences.edit()
                    .remove("chatProfileId")
                    .remove("chatSessionId")
                    .apply()
                isNewChat = false
            }
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
            coroutineScope.launch {
                val totalMessages = if (viewingHistoricalChat) {
                    chatMessages.size + socketIOMessages.size
                } else {
                    socketIOMessages.size
                }
                if (totalMessages > 0) {
                    listState.animateScrollToItem(totalMessages - 1)
                }
            }
        }
    }

    // Show chat when there are messages OR when showChat is explicitly true
    LaunchedEffect(socketIOMessages.size, chatMessages.size, showChat) {
        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
            showChat = true
        }
    }

    val featureCards = listOf(
        FeatureCard(
            title = "Profile Enhance",
            description = "Optimize your social media profile for...",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF9C27B0),
            prompt = "Help me optimize my social media profile for maximum impact and engagement"
        ),
        FeatureCard(
            title = "Bio Enhance",
            description = "Create compelling bio that converts viewers...",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF00BCD4),
            prompt = "Help me create a compelling bio that converts viewers into followers"
        ),
        FeatureCard(
            title = "Content idea creation",
            description = "Generate fresh content ideas for your niche",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF4CAF50),
            prompt = "Generate fresh and creative content ideas for my social media niche"
        ),
        FeatureCard(
            title = "Engaging content idea",
            description = "Create content that drives engagement",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF00BCD4),
            prompt = "Help me create content ideas that drive high engagement and interactions"
        ),
        FeatureCard(
            title = "Suggest catchy blog post",
            description = "Get ideas for compelling blog posts",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF9C27B0),
            prompt = "Suggest catchy and viral blog post ideas for my audience"
        ),
        FeatureCard(
            title = "Unique reel creation",
            description = "Create unique and viral reel concepts",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF4CAF50),
            prompt = "Help me create unique and viral reel concepts that stand out"
        ),
        FeatureCard(
            title = "Analyze the current trends",
            description = "Stay ahead with current social media trends",
            icon = Icons.Default.TrendingUp,
            backgroundColor = Color(0xFFFF9800),
            prompt = "Analyze current social media trends and help me stay ahead of the curve"
        ),
        FeatureCard(
            title = "Hashtag Strategy",
            description = "Develop a winning hashtag strategy for growth",
            icon = Icons.Default.Tag,
            backgroundColor = Color(0xFFE91E63),
            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBarSabo(
                    activeProfile = activeProfile,
                    onClick = { isMenuOpen = true }
                )
            },
            bottomBar = {
                if (!isKeyboardOpen) {
                    BottomNavBar(navController = navController)
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val shouldShowChatInterface = showChat &&
                        (socketIOMessages.isNotEmpty() ||
                                (viewingHistoricalChat && chatMessages.isNotEmpty()) ||
                                isNewChat) // Keep chat interface open even in new chat mode

                if (shouldShowChatInterface) {
                    ChatInterface(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSendClick = {
                            Log.d(
                                "SaboAIScreen",
                                "current session ID$currentSessionId, isNewChat: $isNewChat"
                            )
                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
                                Log.d(
                                    "SaboAIScreen",
                                    "current session ID$currentSessionId, isNewChat: $isNewChat"
                                )

                                val sessionId = currentSessionId

                                if (sessionId != null && sessionId != -1 && !isNewChat) {
                                    // We have a session ID, use it
                                    Log.d(
                                        "SaboAIScreen",
                                        "Sending message with existing sessionId: $sessionId"
                                    )
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = sessionId,
                                        message = searchQuery
                                    )
                                } else {
                                    // No session ID, create new message
                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
                                    if (profileId != -1) {
                                        Log.d(
                                            "SaboAIScreen",
                                            "Creating NEW chat with userId: $userId, profileId: $profileId (isNewChat: $isNewChat)"
                                        )
                                        viewModel.createNewMessage(
                                            userId = userId,
                                            profileId = profileId,
                                            message = searchQuery
                                        )
                                    } else {
                                        Log.e(
                                            "SaboAIScreen",
                                            "No profileId found in SharedPreferences"
                                        )
                                    }
                                }
                                searchQuery = ""
                            }
                        },
                        connectionState = connectionState,
                        socketIOMessages = socketIOMessages,
                        historicalMessages = chatMessages,
                        isViewingHistory = viewingHistoricalChat,
                        messagesLoading = messagesLoading,
                        messagesError = messagesError,
                        listState = listState,
                        onBackClick = {
                            showChat = false
                            viewingHistoricalChat = false
                            isNewChat = false
                            currentSessionId = null
                            sharedPreferences.edit().remove("chatSessionId").apply()
                            chatViewModel.clearCurrentSession()
                            viewModel.clearMessages()
                        }
                    )
                } else {
                    FeatureCardsInterface(
                        activeProfile = activeProfile,
                        featureCards = featureCards,
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSendClick = {
                            if (searchQuery.isNotEmpty()) {
                                val sessionId = currentSessionId

                                if (sessionId != null && sessionId != -1 && !isNewChat) {
                                    Log.d(
                                        "SaboAIScreen",
                                        "Sending message with existing sessionId: $sessionId"
                                    )
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = sessionId,
                                        message = searchQuery
                                    )
                                } else {
                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
                                    if (profileId != -1) {
                                        Log.d(
                                            "SaboAIScreen",
                                            "Creating NEW chat with userId: $userId, profileId: $profileId (isNewChat: $isNewChat)"
                                        )
                                        viewModel.createNewMessage(
                                            userId = userId,
                                            profileId = profileId,
                                            message = searchQuery
                                        )
                                    } else {
                                        Log.e(
                                            "SaboAIScreen",
                                            "No profileId found - cannot create new message"
                                        )
                                    }
                                }
                                searchQuery = ""
                                showChat = true
                            }
                        },
                        onFeatureCardClick = { card ->
                            val sessionId = currentSessionId

                            if (sessionId != null && sessionId != -1 && !isNewChat) {
                                Log.d(
                                    "SaboAIScreen",
                                    "Sending feature card message with existing sessionId: $sessionId"
                                )
                                viewModel.sendMessage(
                                    userId = userId,
                                    sessionId = sessionId,
                                    message = card.prompt
                                )
                            } else {
                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
                                if (profileId != -1) {
                                    Log.d(
                                        "SaboAIScreen",
                                        "Creating NEW chat from feature card with userId: $userId, profileId: $profileId"
                                    )
                                    viewModel.createNewMessage(
                                        userId = userId,
                                        profileId = profileId,
                                        message = card.prompt
                                    )
                                } else {
                                    Log.e(
                                        "SaboAIScreen",
                                        "No profileId found - cannot create new message"
                                    )
                                }
                            }
                            showChat = true
                            viewingHistoricalChat = false
                        }
                    )
                }
            }
        }

        // Side Menu Overlay
        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isMenuOpen = false }
            )

            SideMenu(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White),
                onClose = { isMenuOpen = false },
                onAnalyzeProfileClick = { message ->
                    val sessionId = currentSessionId

                    if (sessionId != null && sessionId != -1 && !isNewChat) {
                        Log.d(
                            "SaboAIScreen",
                            "Sending analyze profile message with existing sessionId: $sessionId"
                        )
                        viewModel.sendMessage(
                            userId = userId,
                            sessionId = sessionId,
                            message = message
                        )
                    } else {
                        val profileId = sharedPreferences.getInt("chatProfileId", -1)
                        if (profileId != -1) {
                            Log.d(
                                "SaboAIScreen",
                                "Creating NEW chat from analyze profile with userId: $userId, profileId: $profileId"
                            )
                            viewModel.createNewMessage(
                                userId = userId,
                                profileId = profileId,
                                message = message
                            )
                        } else {
                            Log.e(
                                "SaboAIScreen",
                                "No profileId found - cannot create new message"
                            )
                        }
                    }

                    showChat = true
                    viewingHistoricalChat = false
                    isMenuOpen = false
                },
                onStartChatting = {
                    isMenuOpen = false
                    showChat = true
                    viewingHistoricalChat = false
                    isNewChat = true
                    currentSessionId = null
                    sharedPreferences.edit().remove("chatSessionId").apply()
                    chatViewModel.clearCurrentSession()
                    viewModel.clearMessages()
                },
                chatUserProfiles = chatUserProfiles,
                isLoadingProfiles = profileLoading,
                profileError = profileError,
                onRefreshProfiles = {
                    userProfile?.let {
                        chatViewModel.fetchChatUserProfiles(it.id)
                    }
                },
                chatViewModel = chatViewModel,
                userId = userId,
                chatHistory = chatHistory,
                historyLoading = historyLoading,
                historyError = historyError,
                onChatHistoryClick = { session ->
                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")

                    sharedPreferences.edit()
                        .putInt("chatSessionId", session.sessionId)
                        .apply()

                    currentSessionId = session.sessionId
                    chatViewModel.fetchChatMessages(session.sessionId)

                    viewingHistoricalChat = true
                    isMenuOpen = false
                    showChat = true
                },
                onRefreshHistory = {
                    val activeProfile = chatUserProfiles.find { it.isActive }
                    Log.d(
                        "SideMenu",
                        "Refreshing history for active profile: ${activeProfile?.username}"
                    )

                    if (activeProfile != null) {
                        val profileId = activeProfile.id
                        chatViewModel.fetchChatHistory(
                            userId = userId,
                            platform = activeProfile.platform,
                            profileId = profileId
                        )
                        sharedPreferences.edit()
                            .putInt("chatProfileId", profileId)
                            .apply()
                    } else {
                        chatViewModel.fetchChatHistory(userId = userId)
                    }
                },
                onSessionChanged = { newSessionId ->
                    currentSessionId = newSessionId
                    if (newSessionId != null) {
                        sharedPreferences.edit()
                            .putInt("chatSessionId", newSessionId)
                            .apply()
                    } else {
                        sharedPreferences.edit().remove("chatSessionId").apply()
                    }
                }
            )
        }
    }
}




// Helper function to detect keyboard state
@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    val isKeyboardOpen = remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardOpen.value = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return isKeyboardOpen
}


@Composable
fun SideMenu(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onStartChatting: () -> Unit,
    onAnalyzeProfileClick: (String) -> Unit,
    chatUserProfiles: List<ChatUserProfile>,
    isLoadingProfiles: Boolean = false,
    profileError: String? = null,
    onRefreshProfiles: () -> Unit = {},
    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
    userId: Int,
    chatHistory: List<com.cc.creatorcircle.data.models.ChatSession> = emptyList(),
    historyLoading: Boolean = false,
    historyError: String? = null,
    onChatHistoryClick: (com.cc.creatorcircle.data.models.ChatSession) -> Unit = {},
    onRefreshHistory: () -> Unit = {},
    onSessionChanged: (Int?) -> Unit = {}
) {

    val context = LocalContext.current
    var showAccountsPopup by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("General") }
    var showProfiles by remember { mutableStateOf(true) }

    // Observe set profile active states
    val setProfileActiveLoading by chatViewModel.setProfileActiveLoading.collectAsState()
    val setProfileActiveError by chatViewModel.setProfileActiveError.collectAsState()
    val activeProfileUpdated by chatViewModel.activeProfileUpdated.collectAsState()

    // Observe add profile states
    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
    val addProfileError by chatViewModel.addProfileError.collectAsState()
    val profileAdded by chatViewModel.profileAdded.collectAsState()


    // Observe delete profile states
    val deleteProfileLoading by chatViewModel.deleteProfileLoading.collectAsState()
    val deleteProfileError by chatViewModel.deleteProfileError.collectAsState()
    val profileDeleted by chatViewModel.profileDeleted.collectAsState()

    // Handle successful profile deletion
    LaunchedEffect(profileDeleted) {
        profileDeleted?.let {
            Log.d("SideMenu", "Profile deleted successfully: ${it.message}")
            onRefreshProfiles()
            chatViewModel.clearDeleteProfileState()
        }
    }

    LaunchedEffect(activeProfileUpdated) {
        activeProfileUpdated?.let { updatedProfile ->
            Log.d("SideMenu", "Profile ${updatedProfile.username} set as active successfully")

            // SAVE TO SHAREDPREFERENCES
            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("chatProfileId", updatedProfile.id)
                .apply()

            chatViewModel.fetchChatHistory(
                userId = userId,
                platform = updatedProfile.platform,
                profileId = updatedProfile.id
            )
        }
    }

    // Handle successful profile addition
    LaunchedEffect(profileAdded) {
        profileAdded?.let {
            Log.d("SideMenu", "Profile ${it.username} added successfully")
            onRefreshProfiles()
            showAccountsPopup = false
        }
    }

    // Convert List<ChatUserProfile> to List<InstagramProfile> for compatibility
    val instagramProfiles = remember(chatUserProfiles) {
        chatUserProfiles
            .filter { it.platform.lowercase() == "instagram" }
            .map { profile ->
                InstagramProfile(
                    id = profile.id.toString(),
                    username = profile.username,
                    profileUrl = profile.platformLink ?: "",
                    isActive = profile.isActive
                )
            }
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        // Social Accounts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showProfiles = !showProfiles },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showProfiles) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (showProfiles) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Social Accounts",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Social Accounts",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = instagramProfiles.size.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (isLoadingProfiles || setProfileActiveLoading || addProfileLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF9C27B0),
                modifier = Modifier.clickable { showAccountsPopup = true }
            ) {
                Text(
                    text = "Add",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show profiles section only when expanded
        if (showProfiles) {
            // Show errors
            setProfileActiveError?.let { error ->
                Text(
                    text = "Failed to set profile active: $error",
                    fontSize = 10.sp,
                    color = Color.Red,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            addProfileError?.let { error ->
                Text(
                    text = "Failed to add profile: $error",
                    fontSize = 10.sp,
                    color = Color.Red,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Profiles List
            when {
                isLoadingProfiles -> {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading accounts...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                profileError != null -> {
                    Column {
                        Text(
                            text = "Failed to load accounts",
                            fontSize = 12.sp,
                            color = Color.Red,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onRefreshProfiles,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                instagramProfiles.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {


                        // Inside the LazyColumn items block in SideMenu
                        items(instagramProfiles) { profile ->
                            InstagramProfileItem(
                                profile = profile,
                                onProfileClick = { clickedProfile ->
                                    if (!clickedProfile.isActive) {
                                        Log.d(
                                            "SideMenu",
                                            "Setting profile ${clickedProfile.username} as active"
                                        )
                                        chatViewModel.setProfileActive(
                                            profileId = clickedProfile.id.toInt(),
                                            userId = userId
                                        )
                                    }
                                },
                                onSelectClick = { clickedProfile ->
                                    Log.d(
                                        "SideMenu",
                                        "Select clicked for profile ${clickedProfile.username}"
                                    )
                                    chatViewModel.setProfileActive(
                                        profileId = clickedProfile.id.toInt(),
                                        userId = userId
                                    )
                                },
                                onDeleteClick = { clickedProfile ->
                                    Log.d(
                                        "SideMenu",
                                        "Delete clicked for profile ${clickedProfile.username}"
                                    )
                                    chatViewModel.deleteChatProfile(
                                        profileId = clickedProfile.id.toInt(),
                                        userId = userId
                                    )
                                },
                                isLoading = setProfileActiveLoading && !profile.isActive
                            )
                        }


                    }
                }

                else -> {
                    Text(
                        text = "No accounts linked! Click \"Add\" to add your social accounts",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSessionChanged(null)
                    // Clear sessionId from SharedPreferences
                    val sharedPreferences =
                        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("chatSessionId").apply()

                    // Clear messages
                    chatViewModel.clearCurrentSession()

                    // NOTE: We're NOT calling createNewChatSession here anymore
                    // Just trigger the callback to open chat screen in "new chat" mode
                    onStartChatting()
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Chat",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "New Chat",
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(2.dp)
        ) {
            // Chat History Header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Chat History",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chat History",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                if (historyLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // This Week indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This Week",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chatHistory.size.toString(),
                    fontSize = 12.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat History List
            when {
                historyLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF9C27B0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Loading chat history...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                historyError != null -> {
                    Column {
                        Text(
                            text = "Failed to load chat history",
                            fontSize = 12.sp,
                            color = Color.Red,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onRefreshHistory,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                chatHistory.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
//                            .fillMaxWidth()
//                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
                            ChatHistoryItem(
                                session = session,
                                onClick = {
                                    Log.d(
                                        "SideMenu",
                                        "Chat history clicked - sessionId: ${session.sessionId}"
                                    )

                                    // Clear all messages first
                                    chatViewModel.clearMessages()

                                    // Set the session ID to trigger message loading
                                    onSessionChanged(session.sessionId)

                                    // Trigger the callback which saves to SharedPreferences
                                    onChatHistoryClick(session)
                                }
                            )
                        }
                    }
                }

                else -> {
                    Text(
                        text = "No chat history found. Start a conversation to see your history here.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }


        }
    }

    // Manage Social Accounts Popup
    if (showAccountsPopup) {
        ManageSocialAccountsPopup(
            onDismiss = {
                showAccountsPopup = false
                chatViewModel.clearAddProfileState()
            },
            onLinkAccount = { url -> },
            chatViewModel = chatViewModel,
            userId = userId
        )
    }
}



@Composable
fun ChatInterface(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    connectionState: ConnectionState,
    socketIOMessages: List<ChatMessage>,
    historicalMessages: List<com.cc.creatorcircle.data.models.ChatMessage>,
    isViewingHistory: Boolean,
    messagesLoading: Boolean,
    messagesError: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f)
                .fillMaxWidth(),
            state = listState
        ) {
            when {
                messagesLoading && historicalMessages.isEmpty() -> {
                    item(key = "loading_indicator") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF9C27B0)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading chat messages...",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                messagesError != null && historicalMessages.isEmpty() && socketIOMessages.isEmpty() -> {
                    item(key = "error_message") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Failed to load messages",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = messagesError,
                                    style = MaterialTheme.typography.body2,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                isViewingHistory -> {
                    // Show historical messages first
                    if (historicalMessages.isNotEmpty()) {
                        items(
                            items = historicalMessages,
                            key = { message -> "historical_${message.id}" }  // Add prefix to make unique
                        ) { message ->
                            HistoricalMessageItem(message = message)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Then append any new socket messages (only for current session)
                    if (socketIOMessages.isNotEmpty()) {
                        items(
                            items = socketIOMessages,
                            key = { message -> "socket_${message.id}" }  // Add prefix to make unique
                        ) { message ->
                            SocketIOMessageItem(message = message)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Show message if no messages at all
                    if (historicalMessages.isEmpty() && socketIOMessages.isEmpty()) {
                        item(key = "empty_history") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No messages in this chat",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                !isViewingHistory && socketIOMessages.isNotEmpty() -> {
                    items(
                        items = socketIOMessages,
                        key = { message -> "socket_${message.id}" }  // Add prefix to make unique
                    ) { message ->
                        SocketIOMessageItem(message = message)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                else -> {
                    item(key = "start_conversation") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start a conversation with Sabo AI...",
                                style = MaterialTheme.typography.body1,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        ChatInputField(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            onSendClick = onSendClick,
            isEnabled = connectionState == ConnectionState.CONNECTED,
            placeholder = if (connectionState == ConnectionState.CONNECTED) {
                "Type your message to get started..."
            } else {
                "Connecting to chat..."
            }
        )
    }
}

@Composable
fun ChatInputField(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean = true,
    placeholder: String = "Type your message to get started...",
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding(), // ADD THIS LINE
        color = Color.White
    ) {
        Column(
            modifier = Modifier.wrapContentHeight()
        ) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
//                verticalAlignment = Alignment.CenterVertically
            ) {


                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isFocused) 1.dp else 2.dp,
                            brush = if (isFocused) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Blue,
                                        Color.Blue
                                    ) // Blue to Green
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF893BCF),
                                        Color(0xFFEA3BA1)
                                    ) // Red to Yellow
                                )
                            },
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xFFF8F9FA),
                    elevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .wrapContentHeight()
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }


                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                            textStyle = TextStyle(
                                color = Color(0xFF1A1A1A),
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            enabled = isEnabled,
                            cursorBrush = SolidColor(Color(0xFF7C4DFF))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendClick,
                    enabled = searchQuery.isNotEmpty() && isEnabled,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = if (searchQuery.isNotEmpty() && isEnabled) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF893BCF), // Top purple-magenta
                                        Color(0xFFEA3BA1)  // Bottom deeper purple
                                    )
                                )
                            } else {
                                SolidColor(Color(0xFFE0E0E0))
                            },
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

            }
        }
    }
}


@Composable
fun HistoricalMessageItem(message: com.cc.creatorcircle.data.models.ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.role == "user") {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            Column(
                horizontalAlignment = if (message.role == "user") {
                    Alignment.End
                } else {
                    Alignment.Start
                }
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .widthIn(max = 320.dp),
                    backgroundColor = if (message.role != "user") {
                        Color(0xFF7C4DFF)
                    } else {
                        Color(0xFFE9ECEF)
                    },
                    elevation = 0.dp,
                    shape = RoundedCornerShape(
                        topStart = if (message.role != "user") 4.dp else 18.dp,
                        topEnd = if (message.role != "user") 18.dp else 4.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                ) {
                    FormattedMessageContent(
                        content = message.content,
                        isAssistant = message.role != "user"
                    )
                }

//                Card(
//                    modifier = Modifier
//                        .wrapContentWidth()
//                        .widthIn(max = 280.dp),
//                    backgroundColor = if (message.role == "user") {
//                        Color(0xFFE9ECEF)
//                    } else {
//                        Color(0xFF7C4DFF)
//                    },
//                    elevation = 0.dp,
//                    shape = RoundedCornerShape(
//                        topStart = if (message.role == "user") 18.dp else 4.dp,
//                        topEnd = if (message.role == "user") 4.dp else 18.dp,
//                        bottomStart = 18.dp,
//                        bottomEnd = 18.dp
//                    )
//                ) {
//                    Text(
//                        text = message.content.replaceFirst("Generating response...", "")
//                            .replace(". ", ".\n"),
//                        modifier = Modifier.padding(
//                            horizontal = 16.dp,
//                            vertical = 12.dp
//                        ),
//                        style = MaterialTheme.typography.body1.copy(
//                            fontSize = 14.sp,
//                            lineHeight = 20.sp
//                        ),
//                        color = if (message.role == "user") {
//                            Color(0xFF212529)
//                        } else {
//                            Color.White
//                        }
//                    )
//                }

                Spacer(modifier = Modifier.height(4.dp))

                // Format timestamp
                Text(
                    text = formatMessageTimestamp(message.timestamp),
                    style = MaterialTheme.typography.caption.copy(
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF6C757D),
                    modifier = Modifier.padding(
                        start = if (message.role == "user") 0.dp else 4.dp,
                        end = if (message.role == "user") 4.dp else 0.dp
                    )
                )
            }
        }
    }
}


// Updated ChatInputField with placeholder parameter

// NEW: Function to format message timestamps
fun formatMessageTimestamp(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        // Fallback formatting
        timestamp.substringBefore("T").let { date ->
            val time = timestamp.substringAfter("T").substringBefore("+").substringBefore(".")
            "$date ${time.substring(0, 5)}"
        }
    }
}


@Composable
fun ChatHistoryItem(
    session: com.cc.creatorcircle.data.models.ChatSession,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Platform icon

        Image(
            painter = painterResource(id = R.drawable.ic_robot_filled),
            contentDescription = session.platform,
            modifier = Modifier.size(24.dp)
        )



        Spacer(modifier = Modifier.width(12.dp))

        // Chat details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = session.title,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formatChatDate(session.updatedAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Platform badge
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            color = when (session.platform.lowercase()) {
//                "instagram" -> Color(0xFFE91E63).copy(alpha = 0.1f)
//                else -> Color(0xFF9C27B0).copy(alpha = 0.1f)
//            }
//        ) {
//            Text(
//                text = when (session.platform.lowercase()) {
//                    "instagram" -> "Instagram"
//                    else -> session.platform.capitalize()
//                },
//                fontSize = 10.sp,
//                color = when (session.platform.lowercase()) {
//                    "instagram" -> Color(0xFFE91E63)
//                    else -> Color(0xFF9C27B0)
//                },
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
//            )
//        }
    }
}

fun formatChatDate(dateString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString.substringBefore("T")
    }
}


@Composable
fun InstagramProfileItem(
    profile: InstagramProfile,
    onProfileClick: (InstagramProfile) -> Unit,
    onSelectClick: (InstagramProfile) -> Unit = {},
    onDeleteClick: (InstagramProfile) -> Unit = {},
    isLoading: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!isLoading) {
                    onProfileClick(profile)
                }
            }
            .background(
                if (profile.isActive) Color(0xFFF3E5F5) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_instagram),
            contentDescription = "Instagram",
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Instagram",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "@${profile.username}",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = if (profile.isActive) FontWeight.Medium else FontWeight.Normal
            )
        }

        if (profile.isActive) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF4CAF50)
            ) {
                Text(
                    text = "Active",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else if (isLoading) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFF9800)
            ) {
                Text(
                    text = "Setting...",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectClick(profile)
                    }
                ) {
                    Text("Select")
                }
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        showDeleteConfirmation = true
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Profile",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete @${profile.username}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick(profile)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Data class for Instagram profile (unchanged)
data class InstagramProfile(
    val id: String,
    val username: String,
    val profileUrl: String,
    val isActive: Boolean
)


@Composable
fun ManageSocialAccountsPopup(
    onDismiss: () -> Unit,
    onLinkAccount: (String) -> Unit,
    // Add these new parameters
    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
    userId: Int
) {
    var instagramUsername by remember { mutableStateOf("") }

    // Observe add profile states
    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
    val addProfileError by chatViewModel.addProfileError.collectAsState()
    val profileAdded by chatViewModel.profileAdded.collectAsState()

    // Handle successful profile addition
    LaunchedEffect(profileAdded) {
        profileAdded?.let {
            Log.d("ManageAccounts", "Profile ${it.username} added successfully")
            instagramUsername = "" // Clear the input field
            // Optionally close the popup or show success message
        }
    }

    // Clear add profile state when dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.clearAddProfileState()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage social Accounts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No accounts linked ! Click \"Link\" to add your social accounts",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Instagram Profile Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Instagram Icon (using a colored surface as placeholder)
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE1306C) // Instagram gradient color approximation
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Instagram Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "0 Linked",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8E8E8),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { /* Add new profile */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instagram Username Input (changed from URL to username)
                OutlinedTextField(
                    value = instagramUsername,
                    onValueChange = { instagramUsername = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter Instagram username (e.g., shadow_senpai_76)",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.Transparent,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !addProfileLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show add profile error if exists
                addProfileError?.let { error ->
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = Color.Red,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Show success message
                profileAdded?.let { profile ->
                    Text(
                        text = "Profile @${profile.username} added successfully!",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link Button
                Button(
                    onClick = {
                        if (instagramUsername.isNotBlank()) {
                            // Remove @ symbol if user entered it
                            val cleanUsername = instagramUsername.removePrefix("@").trim()
                            chatViewModel.addChatProfile(userId, "instagram", cleanUsername)
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = instagramUsername.isNotBlank() && !addProfileLoading
                ) {
                    if (addProfileLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (addProfileLoading) "Adding..." else "Link",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun SocketIOMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isAssistant) {
                Arrangement.Start
            } else {
                Arrangement.End
            }
        ) {
            Card(
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(max = 320.dp),
                backgroundColor = if (message.isAssistant) {
                    Color(0xFF7C4DFF)
                } else {
                    Color(0xFFE9ECEF)
                },
                elevation = 0.dp,
                shape = RoundedCornerShape(
                    topStart = if (message.isAssistant) 4.dp else 18.dp,
                    topEnd = if (message.isAssistant) 18.dp else 4.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )
            ) {
                FormattedMessageContent(
                    content = message.content,
                    isAssistant = message.isAssistant
                )
            }
        }
    }
}


@Composable
fun FormattedMessageContent(content: String, isAssistant: Boolean) {
    val textColor = if (isAssistant) Color.White else Color(0xFF212529)
    val lines = content.split("\n")

    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        var inList = false

        lines.forEach { line ->
            when {
                // Numbered list items (1. 2. 3. etc.)
                line.trim().matches(Regex("^\\d+\\.\\s+.*")) -> {
                    if (!inList) {
                        Spacer(modifier = Modifier.height(8.dp))
                        inList = true
                    }

                    val parts = line.trim().split(Regex("(?<=\\d\\.\\s)"), limit = 2)
                    if (parts.size == 2) {
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = parts[0],
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                ),
                                color = textColor
                            )
                            FormattedText(
                                text = parts[1],
                                textColor = textColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(start = 4.dp),
                                defaultFontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bullet points (- starting lines)
                line.trim().startsWith("-") -> {
                    val cleanLine = line.trim().removePrefix("-").trim()
                    Row(
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.body1.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            color = textColor
                        )
                        FormattedText(
                            text = cleanLine,
                            textColor = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Sub-content (indented content after list items)
                line.trim().isNotEmpty() && !line.trim().matches(Regex("^\\d+\\..*")) && inList -> {
                    FormattedText(
                        text = line.trim(),
                        textColor = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                // Empty line - reset list state
                line.trim().isEmpty() -> {
                    if (inList) {
                        Spacer(modifier = Modifier.height(4.dp))
                        inList = false
                    }
                }

                // Regular paragraph text
                else -> {
                    if (line.trim().isNotEmpty()) {
                        inList = false
                        FormattedText(
                            text = line.trim(),
                            textColor = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedText(
    text: String,
    textColor: Color,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
    defaultFontWeight: FontWeight = FontWeight.Normal
) {
    val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
    val parts = mutableListOf<Pair<String, Boolean>>()
    var lastIndex = 0

    boldPattern.findAll(text).forEach { matchResult ->
        // Add text before the bold section
        if (matchResult.range.first > lastIndex) {
            parts.add(text.substring(lastIndex, matchResult.range.first) to false)
        }
        // Add bold text (without the asterisks)
        parts.add(matchResult.groupValues[1] to true)
        lastIndex = matchResult.range.last + 1
    }

    // Add remaining text after last bold section
    if (lastIndex < text.length) {
        parts.add(text.substring(lastIndex) to false)
    }

    // If no bold formatting found, add entire text as non-bold
    if (parts.isEmpty()) {
        parts.add(text to false)
    }

    Text(
        text = buildAnnotatedString {
            parts.forEach { (part, isBold) ->
                withStyle(
                    style = SpanStyle(
                        fontWeight = if (isBold) FontWeight.Bold else defaultFontWeight
                    )
                ) {
                    append(part)
                }
            }
        },
        style = MaterialTheme.typography.body1.copy(
            fontSize = fontSize,
            lineHeight = lineHeight
        ),
        color = textColor,
        modifier = modifier
    )
}



@Composable
fun FeatureCardsInterface(
    activeProfile: ChatUserProfile?,
    featureCards: List<FeatureCard>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onFeatureCardClick: (FeatureCard) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Hello! I'm ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("SABO")
                        }
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Your AI expert for viral social media content. I can help you with creative strategies and content ideas.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))


            }


            if (activeProfile?.username == null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.ic_instagram),
                            contentDescription = "Instagram",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Provide your IG",
                            fontSize = 15.sp,
                            color = Color.Blue
                        )
                    }
                }
            }


//
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 12.dp),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_instagram),
//                        contentDescription = "Instagram",
//                        modifier = Modifier.size(24.dp)
//                    )
//
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Text(
//                        text = activeProfile?.username?.let { "@$it" } ?: "Provide your IG",
//                        fontSize = 15.sp,
//                        color = Color.Blue
//                    )
//                }
//
//
//            }


            item {
                ChatInputField(
                    searchQuery = searchQuery,
                    onQueryChange = onQueryChange,
                    onSendClick = onSendClick,
                    isEnabled = true,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "What would you like to work on today?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

//                Spacer(modifier = Modifier.height(24.dp))
            }


            item {
                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(600.dp),
//                    modifier = Modifier.height(850.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(featureCards) { card ->
                        FeatureCardItem(
                            card = card,
                            onClick = { onFeatureCardClick(card) }
                        )
                    }
                }
            }
        }

    }
}


@Composable
fun FeatureCardItem(
    card: FeatureCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {


            Text(
                text = card.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}







//@Composable
//fun ChatInterface(
//    searchQuery: String,
//    onQueryChange: (String) -> Unit,
//    onSendClick: () -> Unit,
//    connectionState: ConnectionState,
//    socketIOMessages: List<ChatMessage>,
//    historicalMessages: List<com.cc.creatorcircle.data.models.ChatMessage>,
//    isViewingHistory: Boolean,
//    messagesLoading: Boolean,
//    messagesError: String?,
//    listState: androidx.compose.foundation.lazy.LazyListState,
//    onBackClick: () -> Unit = {}
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .imePadding() // Add this
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .padding(horizontal = 4.dp)
//                .weight(1f) // Takes remaining space
//                .fillMaxWidth(),
//            state = listState
//        ) {
//            when {
//                messagesLoading && historicalMessages.isEmpty() -> {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                CircularProgressIndicator(
//                                    color = Color(0xFF9C27B0)
//                                )
//                                Spacer(modifier = Modifier.height(16.dp))
//                                Text(
//                                    text = "Loading chat messages...",
//                                    style = MaterialTheme.typography.body1,
//                                    color = Color.Gray
//                                )
//                            }
//                        }
//                    }
//                }
//
//                messagesError != null && historicalMessages.isEmpty() && socketIOMessages.isEmpty() -> {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                Text(
//                                    text = "Failed to load messages",
//                                    style = MaterialTheme.typography.body1,
//                                    color = Color.Red
//                                )
//                                Spacer(modifier = Modifier.height(8.dp))
//                                Text(
//                                    text = messagesError,
//                                    style = MaterialTheme.typography.body2,
//                                    color = Color.Gray,
//                                    textAlign = TextAlign.Center
//                                )
//                            }
//                        }
//                    }
//                }
//
//                isViewingHistory -> {
//                    // Show historical messages first
//                    if (historicalMessages.isNotEmpty()) {
//                        items(
//                            items = historicalMessages,
//                            key = { it.id }
//                        ) { message ->
//                            HistoricalMessageItem(message = message)
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
//                    }
//
//                    // Then append any new socket messages (only for current session)
//                    if (socketIOMessages.isNotEmpty()) {
//                        items(
//                            items = socketIOMessages,
//                            key = { it.id }
//                        ) { message ->
//                            SocketIOMessageItem(message = message)
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
//                    }
//
//                    // Show message if no messages at all
//                    if (historicalMessages.isEmpty() && socketIOMessages.isEmpty()) {
//                        item {
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(200.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    text = "No messages in this chat",
//                                    style = MaterialTheme.typography.body1,
//                                    color = Color.Gray
//                                )
//                            }
//                        }
//                    }
//                }
//
//                !isViewingHistory && socketIOMessages.isNotEmpty() -> {
//                    items(
//                        items = socketIOMessages,
//                        key = { it.id }
//                    ) { message ->
//                        SocketIOMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                else -> {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "Start a conversation with Sabo AI...",
//                                style = MaterialTheme.typography.body1,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        ChatInputField(
//            searchQuery = searchQuery,
//            onQueryChange = onQueryChange,
//            onSendClick = onSendClick,
//            isEnabled = connectionState == ConnectionState.CONNECTED,
//            placeholder = if (connectionState == ConnectionState.CONNECTED) {
//                "Type your message to get started..."
//            } else {
//                "Connecting to chat..."
//            }
//        )
//    }
//}













/*
@Composable
fun SaboAIScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val postViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
        com.cc.creatorcircle.viewModel.ChatViewModel(context)
    }

    // User profile states
    val userProfile by postViewModel.userProfile.collectAsState()
    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
    val profileLoading by chatViewModel.profileLoading.collectAsState()
    val profileError by chatViewModel.profileError.collectAsState()

    // Chat history states
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    val historyLoading by chatViewModel.historyLoading.collectAsState()
    val historyError by chatViewModel.historyError.collectAsState()

    // Chat messages states (for historical messages)
    val chatMessages by chatViewModel.messages.collectAsState()
    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
    val messagesError by chatViewModel.messagesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var viewingHistoricalChat by remember { mutableStateOf(false) }
    var isNewChat by remember { mutableStateOf(false) }

    // Detect keyboard visibility
    val isKeyboardOpen by keyboardAsState()

    val activeProfile = chatUserProfiles.find { it.isActive }

    // Get SharedPreferences
    val sharedPreferences = remember {
        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
    }

    // Load sessionId from SharedPreferences - start with null for new chat
    var currentSessionId by remember {
        mutableStateOf<Int?>(null)
    }

    val currentProfileId by remember {
        derivedStateOf {
            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
        }
    }

    // Track the last session ID to detect changes
    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }

    val userId = userProfile?.id ?: -1

    // Socket.IO states (for real-time messages)
    val socketIOMessages by viewModel.messages.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()


    // Listen for new session creation from socket messages and refresh history
    // Listen for new session creation and refresh history
    LaunchedEffect(socketIOMessages.size) {
        // Only proceed if we're in new chat mode and have messages
        if (isNewChat && socketIOMessages.isNotEmpty()) {
            // Wait a bit for the session to be created on the backend
            kotlinx.coroutines.delay(1000)

            // Refresh chat history to show the newly created session
            val activeProfile = chatUserProfiles.find { it.isActive }
            if (activeProfile != null) {
                Log.d("SaboAIScreen", "New messages received - refreshing chat history")
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )

                // Mark that we're no longer in new chat mode
                isNewChat = false
            }
        }
    }

    // Fetch user profile and initialize chat session when screen loads
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        // ALWAYS clear session when navigating to this screen to force new chat
//        sharedPreferences.edit().remove("chatSessionId").apply()
//        currentSessionId = null
//        isNewChat = true
//        viewModel.clearMessages()
//        chatViewModel.clearCurrentSession()
//
//        Log.d("SaboAIScreen", "Screen loaded - Session cleared, New chat mode activated")
//    }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            chatViewModel.fetchChatUserProfiles(it.id)
        }
    }

    // Detect session changes and clear socket messages
    LaunchedEffect(currentSessionId) {
        if (currentSessionId != lastSessionId) {
            Log.d(
                "SaboAIScreen",
                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
            )
            viewModel.clearMessages()
            lastSessionId = currentSessionId
        }
    }

    // Connect to Socket.IO with sessionId from SharedPreferences
    LaunchedEffect(userId, currentSessionId) {
        if (userId != -1) {
            // Only connect if we have a valid session OR if we're starting fresh (sessionId = -1)
            val sessionId = currentSessionId ?: -1

            // For new chat mode, connect with -1 (no session yet)
            Log.d(
                "SaboAIScreen",
                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
            )
            viewModel.connect(userId, sessionId)
        }
    }


    // Auto-reconnect on disconnection
//    LaunchedEffect(connectionState) {
//        if (connectionState == ConnectionState.DISCONNECTED ||
//            connectionState == ConnectionState.ERROR
//        ) {
//            kotlinx.coroutines.delay(2000)
//            val sessionId = currentSessionId ?: -1
//            viewModel.connect(userId, sessionId)
//        }
//    }


    LaunchedEffect(chatUserProfiles) {
        if (chatUserProfiles.isNotEmpty()) {
            val activeProfile = chatUserProfiles.find { it.isActive }
            Log.d(
                "SaboAIScreen",
                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
            )

            if (activeProfile != null) {
                // Save active profile ID to SharedPreferences
                sharedPreferences.edit()
                    .putInt("chatProfileId", activeProfile.id)
                    .apply()

                // Fetch chat history for active profile
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )

                // Don't check sessionId from SharedPreferences, use current state
                // Since we cleared it on screen load, isNewChat should already be true
                Log.d(
                    "SaboAIScreen",
                    "Profile ready for new chat: ${activeProfile.username}, isNewChat: $isNewChat"
                )
            } else {
                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
                // Clear profile ID
                sharedPreferences.edit()
                    .remove("chatProfileId")
                    .apply()
                isNewChat = false
            }
        }
    }


// Refresh history after a delay to allow backend to process
    coroutineScope.launch {
        kotlinx.coroutines.delay(2000)
        val activeProfile = chatUserProfiles.find { it.isActive }
        if (activeProfile != null) {
            Log.d("SaboAIScreen", "Refreshing chat history after new session")
            chatViewModel.fetchChatHistory(
                userId = userId,
                platform = activeProfile.platform,
                profileId = activeProfile.id
            )
        }
    }


//    // Auto-scroll to bottom when new messages arrive
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
//            coroutineScope.launch {
//                val totalMessages = if (viewingHistoricalChat) {
//                    chatMessages.size + socketIOMessages.size
//                } else {
//                    socketIOMessages.size
//                }
//                if (totalMessages > 0) {
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }

    // Show chat when there are messages
    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
            showChat = true
        }
    }

    val featureCards = listOf(
        FeatureCard(
            title = "Profile Enhance",
            description = "Optimize your social media profile for...",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF9C27B0),
            prompt = "Help me optimize my social media profile for maximum impact and engagement"
        ),
        FeatureCard(
            title = "Bio Enhance",
            description = "Create compelling bio that converts viewers...",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF00BCD4),
            prompt = "Help me create a compelling bio that converts viewers into followers"
        ),
        FeatureCard(
            title = "Content idea creation",
            description = "Generate fresh content ideas for your niche",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF4CAF50),
            prompt = "Generate fresh and creative content ideas for my social media niche"
        ),
        FeatureCard(
            title = "Engaging content idea",
            description = "Create content that drives engagement",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF00BCD4),
            prompt = "Help me create content ideas that drive high engagement and interactions"
        ),
        FeatureCard(
            title = "Suggest catchy blog post",
            description = "Get ideas for compelling blog posts",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF9C27B0),
            prompt = "Suggest catchy and viral blog post ideas for my audience"
        ),
        FeatureCard(
            title = "Unique reel creation",
            description = "Create unique and viral reel concepts",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF4CAF50),
            prompt = "Help me create unique and viral reel concepts that stand out"
        ),
        FeatureCard(
            title = "Analyze the current trends",
            description = "Stay ahead with current social media trends",
            icon = Icons.Default.TrendingUp,
            backgroundColor = Color(0xFFFF9800),
            prompt = "Analyze current social media trends and help me stay ahead of the curve"
        ),
        FeatureCard(
            title = "Hashtag Strategy",
            description = "Develop a winning hashtag strategy for growth",
            icon = Icons.Default.Tag,
            backgroundColor = Color(0xFFE91E63),
            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBarSabo(
                    activeProfile = activeProfile,
                    onClick = { isMenuOpen = true }
                )
            },
            bottomBar = {
                // Only show bottom bar when keyboard is closed
                if (!isKeyboardOpen) {
                    BottomNavBar(navController = navController)
                }
            },
            modifier = Modifier
                .fillMaxSize()
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val shouldShowChatInterface = showChat &&
                        (socketIOMessages.isNotEmpty() ||
                                (viewingHistoricalChat && chatMessages.isNotEmpty()))

                if (shouldShowChatInterface) {
                    ChatInterface(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },

                        onSendClick = {
                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
                                val profileId = sharedPreferences.getInt("chatProfileId", -1)

                                if (profileId != -1) {
                                    // Active profile exists
                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
                                        // Create new chat session
                                        Log.d(
                                            "SaboAIScreen",
                                            "Creating new message with userId: $userId, profileId: $profileId"
                                        )
                                        viewModel.createNewMessage(
                                            userId = userId,
                                            profileId = profileId,
                                            message = searchQuery
                                        )
                                        isNewChat = false
                                    } else {
                                        // Continue existing chat
                                        val sessionId = currentSessionId ?: -1
                                        Log.d(
                                            "SaboAIScreen",
                                            "Sending message with userId: $userId, sessionId: $sessionId"
                                        )
                                        viewModel.sendMessage(
                                            userId = userId,
                                            sessionId = sessionId,
                                            message = searchQuery
                                        )
                                    }
                                } else {
                                    // No active profile - send message without saving
                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = -1,
                                        message = searchQuery
                                    )
                                }
                                searchQuery = ""
                            }
                        },
//

//                        onSendClick = {
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                                if (profileId != -1) {
//                                    // Active profile exists
//                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
//                                        // Create new chat session
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating new message with userId: $userId, profileId: $profileId"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//
//                                        // Refresh history after a delay to allow backend to process
//                                        coroutineScope.launch {
//                                            kotlinx.coroutines.delay(2000)
//                                            val activeProfile =
//                                                chatUserProfiles.find { it.isActive }
//                                            if (activeProfile != null) {
//                                                Log.d(
//                                                    "SaboAIScreen",
//                                                    "Refreshing chat history after new session"
//                                                )
//                                                chatViewModel.fetchChatHistory(
//                                                    userId = userId,
//                                                    platform = activeProfile.platform,
//                                                    profileId = activeProfile.id
//                                                )
//                                            }
//                                        }
//
//                                        isNewChat = false
//                                    } else {
//                                        // Continue existing chat
//                                        val sessionId = currentSessionId ?: -1
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Sending message with userId: $userId, sessionId: $sessionId"
//                                        )
//                                        viewModel.sendMessage(
//                                            userId = userId,
//                                            sessionId = sessionId,
//                                            message = searchQuery
//                                        )
//                                    }
//                                } else {
//                                    // No active profile - send message without saving
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "No active profile - sending message without saving to history"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = -1,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                            }
//                        },

                        connectionState = connectionState,
                        socketIOMessages = socketIOMessages,
                        historicalMessages = chatMessages,
                        isViewingHistory = viewingHistoricalChat,
                        messagesLoading = messagesLoading,
                        messagesError = messagesError,
                        listState = listState,
                        onBackClick = {
                            showChat = false
                            viewingHistoricalChat = false
                            isNewChat = false
                            currentSessionId = null
                            sharedPreferences.edit().remove("chatSessionId").apply()
                            chatViewModel.clearCurrentSession()
                            viewModel.clearMessages()
                        }
                    )
                } else {
                    FeatureCardsInterface(
                        activeProfile = activeProfile,
                        featureCards = featureCards,
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty()) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                                if (profileId != -1) {
//                                    // Active profile exists
//                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
//                                        // Create new chat session
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating new message with userId: $userId, profileId: $profileId"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                        isNewChat = false
//                                    } else {
//                                        // Continue existing chat
//                                        val sessionId = currentSessionId ?: -1
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Sending message with userId: $userId, sessionId: $sessionId"
//                                        )
//                                        viewModel.sendMessage(
//                                            userId = userId,
//                                            sessionId = sessionId,
//                                            message = searchQuery
//                                        )
//                                    }
//                                } else {
//                                    // No active profile - send message without saving
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "No active profile - sending message without saving to history"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = -1,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                                showChat = true
//                            }
//                        },
//
//

                        onSendClick = {
                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
                                val profileId = sharedPreferences.getInt("chatProfileId", -1)

                                if (profileId != -1) {
                                    // Active profile exists
                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
                                        // Create new chat session
                                        Log.d(
                                            "SaboAIScreen",
                                            "Creating new message with userId: $userId, profileId: $profileId"
                                        )
                                        viewModel.createNewMessage(
                                            userId = userId,
                                            profileId = profileId,
                                            message = searchQuery
                                        )

                                        // Refresh history after a delay to allow backend to process
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(2000)
                                            val activeProfile = chatUserProfiles.find { it.isActive }
                                            if (activeProfile != null) {
                                                Log.d("SaboAIScreen", "Refreshing chat history after new session")
                                                chatViewModel.fetchChatHistory(
                                                    userId = userId,
                                                    platform = activeProfile.platform,
                                                    profileId = activeProfile.id
                                                )
                                            }
                                        }

                                        isNewChat = false
                                    } else {
                                        // Continue existing chat
                                        val sessionId = currentSessionId ?: -1
                                        Log.d(
                                            "SaboAIScreen",
                                            "Sending message with userId: $userId, sessionId: $sessionId"
                                        )
                                        viewModel.sendMessage(
                                            userId = userId,
                                            sessionId = sessionId,
                                            message = searchQuery
                                        )
                                    }
                                } else {
                                    // No active profile - send message without saving
                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = -1,
                                        message = searchQuery
                                    )
                                }
                                searchQuery = ""
                            }
                        },

                        onFeatureCardClick = { card ->
                            val profileId = sharedPreferences.getInt("chatProfileId", -1)

                            if (profileId != -1) {
                                // Active profile exists
                                if (isNewChat || currentSessionId == null || currentSessionId == -1) {
                                    // Create new chat session
                                    Log.d(
                                        "SaboAIScreen",
                                        "Creating new message with userId: $userId, profileId: $profileId"
                                    )
                                    viewModel.createNewMessage(
                                        userId = userId,
                                        profileId = profileId,
                                        message = card.prompt
                                    )
                                    isNewChat = false
                                } else {
                                    // Continue existing chat
                                    val sessionId = currentSessionId ?: -1
                                    Log.d(
                                        "SaboAIScreen",
                                        "Sending message with userId: $userId, sessionId: $sessionId"
                                    )
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = sessionId,
                                        message = card.prompt
                                    )
                                }
                            } else {
                                // No active profile - send message without saving
                                Log.d(
                                    "SaboAIScreen",
                                    "No active profile - sending feature card message without saving"
                                )
                                viewModel.sendMessage(
                                    userId = userId,
                                    sessionId = -1,
                                    message = card.prompt
                                )
                            }
                            showChat = true
                            viewingHistoricalChat = false
                        }
                    )
                }
            }
        }

        // Side Menu Overlay
        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isMenuOpen = false }
            )

            SideMenu(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White),
                onClose = { isMenuOpen = false },
                onAnalyzeProfileClick = { message ->
                    val profileId = sharedPreferences.getInt("chatProfileId", -1)

                    if (profileId != -1) {
                        // Active profile exists
                        if (isNewChat || currentSessionId == null || currentSessionId == -1) {
                            // Create new chat session
                            Log.d(
                                "SaboAIScreen",
                                "Creating new message with userId: $userId, profileId: $profileId"
                            )
                            viewModel.createNewMessage(
                                userId = userId,
                                profileId = profileId,
                                message = message
                            )
                            isNewChat = false
                        } else {
                            // Continue existing chat
                            val sessionId = currentSessionId ?: -1
                            Log.d(
                                "SaboAIScreen",
                                "Sending message with userId: $userId, sessionId: $sessionId"
                            )
                            viewModel.sendMessage(
                                userId = userId,
                                sessionId = sessionId,
                                message = message
                            )
                        }
                    } else {
                        // No active profile
                        Log.d("SaboAIScreen", "No active profile - cannot analyze profile")
                    }

                    showChat = true
                    viewingHistoricalChat = false
                    isMenuOpen = false
                },
                onStartChatting = {
                    isMenuOpen = false
                    showChat = true
                    viewingHistoricalChat = false
                    isNewChat = true
                    currentSessionId = null
                    sharedPreferences.edit().remove("chatSessionId").apply()
                    chatViewModel.clearCurrentSession()
                    viewModel.clearMessages()
                },
                chatUserProfiles = chatUserProfiles,
                isLoadingProfiles = profileLoading,
                profileError = profileError,
                onRefreshProfiles = {
                    userProfile?.let {
                        chatViewModel.fetchChatUserProfiles(it.id)
                    }
                },
                chatViewModel = chatViewModel,
                userId = userId,
                chatHistory = chatHistory,
                historyLoading = historyLoading,
                historyError = historyError,
                onChatHistoryClick = { session ->
                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")

                    sharedPreferences.edit()
                        .putInt("chatSessionId", session.sessionId)
                        .apply()

                    currentSessionId = session.sessionId
                    chatViewModel.fetchChatMessages(session.sessionId)

                    viewingHistoricalChat = true
                    isNewChat = false  // Important: We're viewing history, not creating new
                    isMenuOpen = false
                    showChat = true
                },
                onRefreshHistory = {
                    val activeProfile = chatUserProfiles.find { it.isActive }
                    Log.d(
                        "SideMenu",
                        "Refreshing history for active profile: ${activeProfile?.username}"
                    )

                    if (activeProfile != null) {
                        val profileId = activeProfile.id
                        chatViewModel.fetchChatHistory(
                            userId = userId,
                            platform = activeProfile.platform,
                            profileId = profileId
                        )
                        sharedPreferences.edit()
                            .putInt("chatProfileId", profileId)
                            .apply()
                    } else {
                        chatViewModel.fetchChatHistory(userId = userId)
                    }
                },
                onSessionChanged = { newSessionId ->
                    currentSessionId = newSessionId
                    if (newSessionId != null) {
                        sharedPreferences.edit()
                            .putInt("chatSessionId", newSessionId)
                            .apply()
                        isNewChat = false  // We have a session now
                    } else {
                        sharedPreferences.edit().remove("chatSessionId").apply()
                        isNewChat = true  // No session means new chat
                    }
                }
            )
        }
    }
}
*/




/*

//@Composable
//fun SaboAIScreen(
//    navController: NavController,
//    viewModel: ChatViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val postViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
//        com.cc.creatorcircle.viewModel.ChatViewModel(context)
//    }
//
//    // User profile states
//    val userProfile by postViewModel.userProfile.collectAsState()
//    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
//    val profileLoading by chatViewModel.profileLoading.collectAsState()
//    val profileError by chatViewModel.profileError.collectAsState()
//
//    // Chat history states
//    val chatHistory by chatViewModel.chatHistory.collectAsState()
//    val historyLoading by chatViewModel.historyLoading.collectAsState()
//    val historyError by chatViewModel.historyError.collectAsState()
//
//    // Chat messages states
//    val chatMessages by chatViewModel.messages.collectAsState()
//    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
//    val messagesError by chatViewModel.messagesError.collectAsState()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var showChat by remember { mutableStateOf(false) }
//    var isMenuOpen by remember { mutableStateOf(false) }
//    var viewingHistoricalChat by remember { mutableStateOf(false) }
//    var isNewChat by remember { mutableStateOf(false) }
//
//    // Detect keyboard visibility
//    val isKeyboardOpen by keyboardAsState()
//
//    val activeProfile = chatUserProfiles.find { it.isActive }
//
//    // Get SharedPreferences
//    val sharedPreferences = remember {
//        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//    }
//
//    // Load sessionId from SharedPreferences
//    var currentSessionId by remember {
//        mutableStateOf<Int?>(
//            sharedPreferences.getInt("chatSessionId", -1).takeIf { it != -1 }
//        )
//    }
//
//    val currentProfileId by remember {
//        derivedStateOf {
//            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
//        }
//    }
//
//    // Track the last session ID to detect changes
//    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }
//
//    val userId = userProfile?.id ?: -1
//
//    // Socket.IO states
//    val socketIOMessages by viewModel.messages.collectAsState()
//    val connectionState by viewModel.connectionState.collectAsState()
//    val newSessionId by viewModel.newSessionId.collectAsState() // OBSERVE THIS
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//
//    // Fetch user profile and initialize chat session
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//        if (sessionId == -1) {
//            isNewChat = true
//        }
//    }
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let {
//            chatViewModel.fetchChatUserProfiles(it.id)
//        }
//    }
//
//    // Detect session changes and clear socket messages
//    LaunchedEffect(currentSessionId) {
//        if (currentSessionId != lastSessionId) {
//            Log.d(
//                "SaboAIScreen",
//                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
//            )
//            viewModel.clearMessages()
//            lastSessionId = currentSessionId
//        }
//    }
//
//    // NEW: Listen for new session ID from socket
//    LaunchedEffect(newSessionId) {
//        newSessionId?.let { sessionId ->
//            Log.d("SaboAIScreen", "New session ID received from socket: $sessionId")
//
//            currentSessionId = sessionId
//            sharedPreferences.edit()
//                .putInt("chatSessionId", sessionId)
//                .apply()
//
//            isNewChat = false
//
//            // Reconnect with new session ID
//            viewModel.connect(userId, sessionId)
//
//            // Clear the session ID from the flow
//            viewModel.clearNewSessionId()
//        }
//    }
//
//    // Connect to Socket.IO
//    LaunchedEffect(userId, currentSessionId) {
//        if (userId != -1) {
//            val sessionId = currentSessionId ?: -1
//            Log.d(
//                "SaboAIScreen",
//                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
//            )
//            viewModel.connect(userId, sessionId)
//        }
//    }
//
//    LaunchedEffect(chatUserProfiles) {
//        if (chatUserProfiles.isNotEmpty()) {
//            val activeProfile = chatUserProfiles.find { it.isActive }
//            Log.d(
//                "SaboAIScreen",
//                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
//            )
//
//            if (activeProfile != null) {
//                sharedPreferences.edit()
//                    .putInt("chatProfileId", activeProfile.id)
//                    .apply()
//
//                chatViewModel.fetchChatHistory(
//                    userId = userId,
//                    platform = activeProfile.platform,
//                    profileId = activeProfile.id
//                )
//
//                val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//                if (sessionId == -1) {
//                    isNewChat = true
//                    Log.d("SaboAIScreen", "New chat mode activated for profile: ${activeProfile.username}")
//                }
//            } else {
//                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
//                sharedPreferences.edit()
//                    .remove("chatProfileId")
//                    .remove("chatSessionId")
//                    .apply()
//                isNewChat = false
//            }
//        }
//    }
//
//    // Auto-scroll to bottom when new messages arrive
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
//            coroutineScope.launch {
//                val totalMessages = if (viewingHistoricalChat) {
//                    chatMessages.size + socketIOMessages.size
//                } else {
//                    socketIOMessages.size
//                }
//                if (totalMessages > 0) {
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    // Show chat when there are messages
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
//            showChat = true
//        }
//    }
//
//    val featureCards = listOf(
//        FeatureCard(
//            title = "Profile Enhance",
//            description = "Optimize your social media profile for...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Help me optimize my social media profile for maximum impact and engagement"
//        ),
//        FeatureCard(
//            title = "Bio Enhance",
//            description = "Create compelling bio that converts viewers...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create a compelling bio that converts viewers into followers"
//        ),
//        FeatureCard(
//            title = "Content idea creation",
//            description = "Generate fresh content ideas for your niche",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Generate fresh and creative content ideas for my social media niche"
//        ),
//        FeatureCard(
//            title = "Engaging content idea",
//            description = "Create content that drives engagement",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create content ideas that drive high engagement and interactions"
//        ),
//        FeatureCard(
//            title = "Suggest catchy blog post",
//            description = "Get ideas for compelling blog posts",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Suggest catchy and viral blog post ideas for my audience"
//        ),
//        FeatureCard(
//            title = "Unique reel creation",
//            description = "Create unique and viral reel concepts",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Help me create unique and viral reel concepts that stand out"
//        ),
//        FeatureCard(
//            title = "Analyze the current trends",
//            description = "Stay ahead with current social media trends",
//            icon = Icons.Default.TrendingUp,
//            backgroundColor = Color(0xFFFF9800),
//            prompt = "Analyze current social media trends and help me stay ahead of the curve"
//        ),
//        FeatureCard(
//            title = "Hashtag Strategy",
//            description = "Develop a winning hashtag strategy for growth",
//            icon = Icons.Default.Tag,
//            backgroundColor = Color(0xFFE91E63),
//            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
//        )
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            topBar = {
//                TopBarSabo(
//                    activeProfile = activeProfile,
//                    onClick = { isMenuOpen = true }
//                )
//            },
//            bottomBar = {
//                if (!isKeyboardOpen) {
//                    BottomNavBar(navController = navController)
//                }
//            },
//            modifier = Modifier.fillMaxSize()
//        ) { paddingValues ->
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                val shouldShowChatInterface = showChat &&
//                        (socketIOMessages.isNotEmpty() ||
//                                (viewingHistoricalChat && chatMessages.isNotEmpty()))
//
//                if (shouldShowChatInterface) {
//                    ChatInterface(
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//                                if (isNewChat) {
//                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                    if (profileId != -1) {
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating new message with userId: $userId, profileId: $profileId"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                        // Don't set isNewChat to false here
//                                        // It will be set to false when newSessionId is received
//                                    } else {
//                                        Log.e(
//                                            "SaboAIScreen",
//                                            "No profileId found in SharedPreferences"
//                                        )
//                                    }
//                                } else {
//                                    val sessionId = currentSessionId ?: -1
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with userId: $userId, sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                            }
//                        },
//                        connectionState = connectionState,
//                        socketIOMessages = socketIOMessages,
//                        historicalMessages = chatMessages,
//                        isViewingHistory = viewingHistoricalChat,
//                        messagesLoading = messagesLoading,
//                        messagesError = messagesError,
//                        listState = listState,
//                        onBackClick = {
//                            showChat = false
//                            viewingHistoricalChat = false
//                            isNewChat = false
//                            currentSessionId = null
//                            sharedPreferences.edit().remove("chatSessionId").apply()
//                            chatViewModel.clearCurrentSession()
//                            viewModel.clearMessages()
//                        }
//                    )
//                } else {
//                    FeatureCardsInterface(
//                        activeProfile = activeProfile,
//                        featureCards = featureCards,
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty()) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                if (isNewChat && profileId != -1) {
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Creating new message with userId: $userId, profileId: $profileId"
//                                    )
//                                    viewModel.createNewMessage(
//                                        userId = userId,
//                                        profileId = profileId,
//                                        message = searchQuery
//                                    )
//                                } else {
//                                    val sessionId = currentSessionId ?: -1
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with userId: $userId, sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                                showChat = true
//                            }
//                        },
//                        onFeatureCardClick = { card ->
//                            val sessionId = currentSessionId ?: -1
//                            val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                            if (sessionId == -1 && profileId != -1) {
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Creating new message with userId: $userId, profileId: $profileId"
//                                )
//                                viewModel.createNewMessage(
//                                    userId = userId,
//                                    profileId = profileId,
//                                    message = card.prompt
//                                )
//                            } else {
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Sending message with userId: $userId, sessionId: $sessionId"
//                                )
//                                viewModel.sendMessage(
//                                    userId = userId,
//                                    sessionId = sessionId,
//                                    message = card.prompt
//                                )
//                            }
//                            showChat = true
//                            viewingHistoricalChat = false
//                        }
//                    )
//                }
//            }
//        }
//
//        // Side Menu Overlay
//        if (isMenuOpen) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { isMenuOpen = false }
//            )
//
//            SideMenu(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(280.dp)
//                    .background(Color.White),
//                onClose = { isMenuOpen = false },
//                onAnalyzeProfileClick = { message ->
//                    val sessionId = currentSessionId ?: -1
//                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                    if (sessionId == -1 && profileId != -1) {
//                        Log.d(
//                            "SaboAIScreen",
//                            "Creating new message with userId: $userId, profileId: $profileId"
//                        )
//                        viewModel.createNewMessage(
//                            userId = userId,
//                            profileId = profileId,
//                            message = message
//                        )
//                    } else {
//                        Log.d(
//                            "SaboAIScreen",
//                            "Sending message with userId: $userId, sessionId: $sessionId"
//                        )
//                        viewModel.sendMessage(
//                            userId = userId,
//                            sessionId = sessionId,
//                            message = message
//                        )
//                    }
//
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isMenuOpen = false
//                },
//                onStartChatting = {
//                    isMenuOpen = false
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isNewChat = true
//                    currentSessionId = null
//                    sharedPreferences.edit().remove("chatSessionId").apply()
//                    chatViewModel.clearCurrentSession()
//                    viewModel.clearMessages()
//                },
//                chatUserProfiles = chatUserProfiles,
//                isLoadingProfiles = profileLoading,
//                profileError = profileError,
//                onRefreshProfiles = {
//                    userProfile?.let {
//                        chatViewModel.fetchChatUserProfiles(it.id)
//                    }
//                },
//                chatViewModel = chatViewModel,
//                userId = userId,
//                chatHistory = chatHistory,
//                historyLoading = historyLoading,
//                historyError = historyError,
//                onChatHistoryClick = { session ->
//                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
//
//                    sharedPreferences.edit()
//                        .putInt("chatSessionId", session.sessionId)
//                        .apply()
//
//                    currentSessionId = session.sessionId
//                    chatViewModel.fetchChatMessages(session.sessionId)
//
//                    viewingHistoricalChat = true
//                    isMenuOpen = false
//                    showChat = true
//                },
//                onRefreshHistory = {
//                    val activeProfile = chatUserProfiles.find { it.isActive }
//                    Log.d(
//                        "SideMenu",
//                        "Refreshing history for active profile: ${activeProfile?.username}"
//                    )
//
//                    if (activeProfile != null) {
//                        val profileId = activeProfile.id
//                        chatViewModel.fetchChatHistory(
//                            userId = userId,
//                            platform = activeProfile.platform,
//                            profileId = profileId
//                        )
//                        sharedPreferences.edit()
//                            .putInt("chatProfileId", profileId)
//                            .apply()
//                    } else {
//                        chatViewModel.fetchChatHistory(userId = userId)
//                    }
//                },
//                onSessionChanged = { newSessionId ->
//                    currentSessionId = newSessionId
//                    if (newSessionId != null) {
//                        sharedPreferences.edit()
//                            .putInt("chatSessionId", newSessionId)
//                            .apply()
//                    } else {
//                        sharedPreferences.edit().remove("chatSessionId").apply()
//                    }
//                }
//            )
//        }
//    }
//}





//@Composable
//fun SaboAIScreen(
//    navController: NavController,
//    viewModel: ChatViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val postViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
//        com.cc.creatorcircle.viewModel.ChatViewModel(context)
//    }
//
//    // User profile states
//    val userProfile by postViewModel.userProfile.collectAsState()
//    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
//    val profileLoading by chatViewModel.profileLoading.collectAsState()
//    val profileError by chatViewModel.profileError.collectAsState()
//
//    // Chat history states
//    val chatHistory by chatViewModel.chatHistory.collectAsState()
//    val historyLoading by chatViewModel.historyLoading.collectAsState()
//    val historyError by chatViewModel.historyError.collectAsState()
//
//    // Chat messages states (for historical messages)
//    val chatMessages by chatViewModel.messages.collectAsState()
//    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
//    val messagesError by chatViewModel.messagesError.collectAsState()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var showChat by remember { mutableStateOf(false) }
//    var isMenuOpen by remember { mutableStateOf(false) }
//    var viewingHistoricalChat by remember { mutableStateOf(false) }
//    var isNewChat by remember { mutableStateOf(false) }
//
//    // Detect keyboard visibility
//    val isKeyboardOpen by keyboardAsState()
//
//    val activeProfile = chatUserProfiles.find { it.isActive }
//
//    // Get SharedPreferences
//    val sharedPreferences = remember {
//        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//    }
//
//    // Load sessionId from SharedPreferences
//    var currentSessionId by remember {
//        mutableStateOf<Int?>(
//            sharedPreferences.getInt("chatSessionId", -1).takeIf { it != -1 }
//        )
//    }
//
//    val currentProfileId by remember {
//        derivedStateOf {
//            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
//        }
//    }
//
//    // Track the last session ID to detect changes
//    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }
//
//    val userId = userProfile?.id ?: -1
//
//    // Socket.IO states (for real-time messages)
//    val socketIOMessages by viewModel.messages.collectAsState()
//    val connectionState by viewModel.connectionState.collectAsState()
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//
//    // Fetch user profile and initialize chat session when screen loads
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        // Check if we need to create a new session for an active profile
//        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//        if (sessionId == -1) {
//            // No active session, mark as new chat mode
//            isNewChat = true
//        }
//    }
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let {
//            chatViewModel.fetchChatUserProfiles(it.id)
//        }
//    }
//
//    // Detect session changes and clear socket messages
//    LaunchedEffect(currentSessionId) {
//        if (currentSessionId != lastSessionId) {
//            Log.d(
//                "SaboAIScreen",
//                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
//            )
//            viewModel.clearMessages()
//            lastSessionId = currentSessionId
//        }
//    }
//
//    // Connect to Socket.IO with sessionId from SharedPreferences
//    LaunchedEffect(userId, currentSessionId) {
//        if (userId != -1) {
//            val sessionId = currentSessionId ?: -1
//            Log.d(
//                "SaboAIScreen",
//                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
//            )
//            viewModel.connect(userId, sessionId)
//        }
//    }
//
//    LaunchedEffect(chatUserProfiles) {
//        if (chatUserProfiles.isNotEmpty()) {
//            val activeProfile = chatUserProfiles.find { it.isActive }
//            Log.d(
//                "SaboAIScreen",
//                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
//            )
//
//            if (activeProfile != null) {
//                // Save active profile ID to SharedPreferences
//                sharedPreferences.edit()
//                    .putInt("chatProfileId", activeProfile.id)
//                    .apply()
//
//                // Fetch chat history for active profile
//                chatViewModel.fetchChatHistory(
//                    userId = userId,
//                    platform = activeProfile.platform,
//                    profileId = activeProfile.id
//                )
//
//                // Check if we need to start a new chat
//                val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//                if (sessionId == -1) {
//                    // Mark as new chat mode - messages will be saved when user sends first message
//                    isNewChat = true
//                    Log.d("SaboAIScreen", "New chat mode activated for profile: ${activeProfile.username}")
//                }
//            } else {
//                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
//                // Clear profile ID and session ID
//                sharedPreferences.edit()
//                    .remove("chatProfileId")
//                    .remove("chatSessionId")
//                    .apply()
//                isNewChat = false
//            }
//        }
//    }
//
//    // Auto-scroll to bottom when new messages arrive
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
//            coroutineScope.launch {
//                val totalMessages = if (viewingHistoricalChat) {
//                    chatMessages.size + socketIOMessages.size
//                } else {
//                    socketIOMessages.size
//                }
//                if (totalMessages > 0) {
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    // Show chat when there are messages
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
//            showChat = true
//        }
//    }
//
//    val featureCards = listOf(
//        FeatureCard(
//            title = "Profile Enhance",
//            description = "Optimize your social media profile for...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Help me optimize my social media profile for maximum impact and engagement"
//        ),
//        FeatureCard(
//            title = "Bio Enhance",
//            description = "Create compelling bio that converts viewers...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create a compelling bio that converts viewers into followers"
//        ),
//        FeatureCard(
//            title = "Content idea creation",
//            description = "Generate fresh content ideas for your niche",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Generate fresh and creative content ideas for my social media niche"
//        ),
//        FeatureCard(
//            title = "Engaging content idea",
//            description = "Create content that drives engagement",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create content ideas that drive high engagement and interactions"
//        ),
//        FeatureCard(
//            title = "Suggest catchy blog post",
//            description = "Get ideas for compelling blog posts",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Suggest catchy and viral blog post ideas for my audience"
//        ),
//        FeatureCard(
//            title = "Unique reel creation",
//            description = "Create unique and viral reel concepts",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Help me create unique and viral reel concepts that stand out"
//        ),
//        FeatureCard(
//            title = "Analyze the current trends",
//            description = "Stay ahead with current social media trends",
//            icon = Icons.Default.TrendingUp,
//            backgroundColor = Color(0xFFFF9800),
//            prompt = "Analyze current social media trends and help me stay ahead of the curve"
//        ),
//        FeatureCard(
//            title = "Hashtag Strategy",
//            description = "Develop a winning hashtag strategy for growth",
//            icon = Icons.Default.Tag,
//            backgroundColor = Color(0xFFE91E63),
//            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
//        )
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            topBar = {
//                TopBarSabo(
//                    activeProfile = activeProfile,
//                    onClick = { isMenuOpen = true }
//                )
//            },
//            bottomBar = {
//                // Only show bottom bar when keyboard is closed
//                if (!isKeyboardOpen) {
//                    BottomNavBar(navController = navController)
//                }
//            },
//            modifier = Modifier
//                .fillMaxSize()
//        ) { paddingValues ->
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                val shouldShowChatInterface = showChat &&
//                        (socketIOMessages.isNotEmpty() ||
//                                (viewingHistoricalChat && chatMessages.isNotEmpty()))
//
//                if (shouldShowChatInterface) {
//                    ChatInterface(
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//                                if (isNewChat) {
//                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                    if (profileId != -1) {
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating new message with userId: $userId, profileId: $profileId"
//                                        )
//
//                                        // Launch coroutine to handle session ID response
//                                        coroutineScope.launch {
//                                            val newSessionId = viewModel.createNewMessageAndGetSessionId(
//                                                userId = userId,
//                                                profileId = profileId,
//                                                message = searchQuery
//                                            )
//
//                                            if (newSessionId != null) {
//                                                Log.d("SaboAIScreen", "New session created: $newSessionId")
//                                                currentSessionId = newSessionId
//                                                sharedPreferences.edit()
//                                                    .putInt("chatSessionId", newSessionId)
//                                                    .apply()
//                                                isNewChat = false
//
//                                                // Reconnect socket with new session ID
//                                                viewModel.connect(userId, newSessionId)
//                                            } else {
//                                                Log.e("SaboAIScreen", "Failed to create new session")
//                                            }
//                                        }
//                                    } else {
//                                        Log.e(
//                                            "SaboAIScreen",
//                                            "No profileId found in SharedPreferences"
//                                        )
//                                    }
//                                } else {
//                                    val sessionId = currentSessionId ?: -1
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with userId: $userId, sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                            }
//                        },
//                        connectionState = connectionState,
//                        socketIOMessages = socketIOMessages,
//                        historicalMessages = chatMessages,
//                        isViewingHistory = viewingHistoricalChat,
//                        messagesLoading = messagesLoading,
//                        messagesError = messagesError,
//                        listState = listState,
//                        onBackClick = {
//                            showChat = false
//                            viewingHistoricalChat = false
//                            isNewChat = false
//                            currentSessionId = null
//                            sharedPreferences.edit().remove("chatSessionId").apply()
//                            chatViewModel.clearCurrentSession()
//                            viewModel.clearMessages()
//                        }
//                    )
//                } else {
//                    FeatureCardsInterface(
//                        activeProfile = activeProfile,
//                        featureCards = featureCards,
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty()) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                if (isNewChat && profileId != -1) {
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Creating new message with userId: $userId, profileId: $profileId"
//                                    )
//
//                                    coroutineScope.launch {
//                                        val newSessionId = viewModel.createNewMessageAndGetSessionId(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//
//                                        if (newSessionId != null) {
//                                            Log.d("SaboAIScreen", "New session created: $newSessionId")
//                                            currentSessionId = newSessionId
//                                            sharedPreferences.edit()
//                                                .putInt("chatSessionId", newSessionId)
//                                                .apply()
//                                            isNewChat = false
//
//                                            // Reconnect socket with new session ID
//                                            viewModel.connect(userId, newSessionId)
//                                        }
//                                    }
//                                } else {
//                                    val sessionId = currentSessionId ?: -1
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with userId: $userId, sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                                showChat = true
//                            }
//                        },
//                        onFeatureCardClick = { card ->
//                            val sessionId = currentSessionId ?: -1
//                            val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                            if (sessionId == -1 && profileId != -1) {
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Creating new message with userId: $userId, profileId: $profileId"
//                                )
//
//                                coroutineScope.launch {
//                                    val newSessionId = viewModel.createNewMessageAndGetSessionId(
//                                        userId = userId,
//                                        profileId = profileId,
//                                        message = card.prompt
//                                    )
//
//                                    if (newSessionId != null) {
//                                        Log.d("SaboAIScreen", "New session created: $newSessionId")
//                                        currentSessionId = newSessionId
//                                        sharedPreferences.edit()
//                                            .putInt("chatSessionId", newSessionId)
//                                            .apply()
//                                        isNewChat = false
//
//                                        // Reconnect socket with new session ID
//                                        viewModel.connect(userId, newSessionId)
//                                    }
//                                }
//                            } else {
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Sending message with userId: $userId, sessionId: $sessionId"
//                                )
//                                viewModel.sendMessage(
//                                    userId = userId,
//                                    sessionId = sessionId,
//                                    message = card.prompt
//                                )
//                            }
//                            showChat = true
//                            viewingHistoricalChat = false
//                        }
//                    )
//                }
//            }
//        }
//
//        // Side Menu Overlay
//        if (isMenuOpen) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { isMenuOpen = false }
//            )
//
//            SideMenu(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(280.dp)
//                    .background(Color.White),
//                onClose = { isMenuOpen = false },
//                onAnalyzeProfileClick = { message ->
//                    val sessionId = currentSessionId ?: -1
//                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                    if (sessionId == -1 && profileId != -1) {
//                        Log.d(
//                            "SaboAIScreen",
//                            "Creating new message with userId: $userId, profileId: $profileId"
//                        )
//
//                        coroutineScope.launch {
//                            val newSessionId = viewModel.createNewMessageAndGetSessionId(
//                                userId = userId,
//                                profileId = profileId,
//                                message = message
//                            )
//
//                            if (newSessionId != null) {
//                                Log.d("SaboAIScreen", "New session created: $newSessionId")
//                                currentSessionId = newSessionId
//                                sharedPreferences.edit()
//                                    .putInt("chatSessionId", newSessionId)
//                                    .apply()
//                                isNewChat = false
//
//                                // Reconnect socket with new session ID
//                                viewModel.connect(userId, newSessionId)
//                            }
//                        }
//                    } else {
//                        Log.d(
//                            "SaboAIScreen",
//                            "Sending message with userId: $userId, sessionId: $sessionId"
//                        )
//                        viewModel.sendMessage(
//                            userId = userId,
//                            sessionId = sessionId,
//                            message = message
//                        )
//                    }
//
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isMenuOpen = false
//                },
//                onStartChatting = {
//                    isMenuOpen = false
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isNewChat = true
//                    currentSessionId = null
//                    sharedPreferences.edit().remove("chatSessionId").apply()
//                    chatViewModel.clearCurrentSession()
//                    viewModel.clearMessages()
//                },
//                chatUserProfiles = chatUserProfiles,
//                isLoadingProfiles = profileLoading,
//                profileError = profileError,
//                onRefreshProfiles = {
//                    userProfile?.let {
//                        chatViewModel.fetchChatUserProfiles(it.id)
//                    }
//                },
//                chatViewModel = chatViewModel,
//                userId = userId,
//                chatHistory = chatHistory,
//                historyLoading = historyLoading,
//                historyError = historyError,
//                onChatHistoryClick = { session ->
//                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
//
//                    sharedPreferences.edit()
//                        .putInt("chatSessionId", session.sessionId)
//                        .apply()
//
//                    currentSessionId = session.sessionId
//                    chatViewModel.fetchChatMessages(session.sessionId)
//
//                    viewingHistoricalChat = true
//                    isMenuOpen = false
//                    showChat = true
//                },
//                onRefreshHistory = {
//                    val activeProfile = chatUserProfiles.find { it.isActive }
//                    Log.d(
//                        "SideMenu",
//                        "Refreshing history for active profile: ${activeProfile?.username}"
//                    )
//
//                    if (activeProfile != null) {
//                        val profileId = activeProfile.id
//                        chatViewModel.fetchChatHistory(
//                            userId = userId,
//                            platform = activeProfile.platform,
//                            profileId = profileId
//                        )
//                        sharedPreferences.edit()
//                            .putInt("chatProfileId", profileId)
//                            .apply()
//                    } else {
//                        chatViewModel.fetchChatHistory(userId = userId)
//                    }
//                },
//                onSessionChanged = { newSessionId ->
//                    currentSessionId = newSessionId
//                    if (newSessionId != null) {
//                        sharedPreferences.edit()
//                            .putInt("chatSessionId", newSessionId)
//                            .apply()
//                    } else {
//                        sharedPreferences.edit().remove("chatSessionId").apply()
//                    }
//                }
//            )
//        }
//    }
//}





//@Composable
//fun SaboAIScreen(
//    navController: NavController,
//    viewModel: ChatViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val postViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
//        com.cc.creatorcircle.viewModel.ChatViewModel(context)
//    }
//
//    // User profile states
//    val userProfile by postViewModel.userProfile.collectAsState()
//    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
//    val profileLoading by chatViewModel.profileLoading.collectAsState()
//    val profileError by chatViewModel.profileError.collectAsState()
//
//    // Chat history states
//    val chatHistory by chatViewModel.chatHistory.collectAsState()
//    val historyLoading by chatViewModel.historyLoading.collectAsState()
//    val historyError by chatViewModel.historyError.collectAsState()
//
//    // Chat messages states (for historical messages)
//    val chatMessages by chatViewModel.messages.collectAsState()
//    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
//    val messagesError by chatViewModel.messagesError.collectAsState()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var showChat by remember { mutableStateOf(false) }
//    var isMenuOpen by remember { mutableStateOf(false) }
//    var viewingHistoricalChat by remember { mutableStateOf(false) }
//    var isNewChat by remember { mutableStateOf(false) }
//
//    // Detect keyboard visibility
//    val isKeyboardOpen by keyboardAsState()
//
//    val activeProfile = chatUserProfiles.find { it.isActive }
//
//    // Get SharedPreferences
//    val sharedPreferences = remember {
//        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//    }
//
//    // Load sessionId from SharedPreferences
//    var currentSessionId by remember {
//        mutableStateOf<Int?>(
//            sharedPreferences.getInt("chatSessionId", -1).takeIf { it != -1 }
//        )
//    }
//
//    val currentProfileId by remember {
//        derivedStateOf {
//            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
//        }
//    }
//
//    // Track the last session ID to detect changes
//    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }
//
//    val userId = userProfile?.id ?: -1
//
//    // Socket.IO states (for real-time messages)
//    val socketIOMessages by viewModel.messages.collectAsState()
//    val connectionState by viewModel.connectionState.collectAsState()
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//
//    // Fetch user profile when screen loads
////    LaunchedEffect(Unit) {
////        postViewModel.fetchUserProfile()
////    }
//
//
//// Fetch user profile and initialize chat session when screen loads
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        // Check if we need to create a new session for an active profile
//        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//        if (sessionId == -1) {
//            // No active session, mark as new chat mode
//            isNewChat = true
//        }
//    }
//
//
//
//
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let {
//            chatViewModel.fetchChatUserProfiles(it.id)
//        }
//    }
//
//    // Detect session changes and clear socket messages
//    LaunchedEffect(currentSessionId) {
//        if (currentSessionId != lastSessionId) {
//            Log.d(
//                "SaboAIScreen",
//                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
//            )
//            viewModel.clearMessages()
//            lastSessionId = currentSessionId
//        }
//    }
//
//    // Connect to Socket.IO with sessionId from SharedPreferences
//    LaunchedEffect(userId, currentSessionId) {
//        if (userId != -1) {
//            val sessionId = currentSessionId ?: -1
//            Log.d(
//                "SaboAIScreen",
//                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
//            )
//            viewModel.connect(userId, sessionId)
//        }
//    }
//
//    // Auto-reconnect on disconnection
////    LaunchedEffect(connectionState) {
////        if (connectionState == ConnectionState.DISCONNECTED ||
////            connectionState == ConnectionState.ERROR
////        ) {
////            kotlinx.coroutines.delay(2000)
////            val sessionId = currentSessionId ?: -1
////            viewModel.connect(userId, sessionId)
////        }
////    }
//
//    LaunchedEffect(chatUserProfiles) {
//        if (chatUserProfiles.isNotEmpty()) {
//            val activeProfile = chatUserProfiles.find { it.isActive }
//            Log.d(
//                "SaboAIScreen",
//                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
//            )
//
//            if (activeProfile != null) {
//                chatViewModel.fetchChatHistory(
//                    userId = userId,
//                    platform = activeProfile.platform,
//                    profileId = activeProfile.id
//                )
//            } else {
//                Log.w("SaboAIScreen", "No active profile found, fetching general history")
//                chatViewModel.fetchChatHistory(userId = userId)
//            }
//        }
//    }
//
//
//    LaunchedEffect(chatUserProfiles) {
//        if (chatUserProfiles.isNotEmpty()) {
//            val activeProfile = chatUserProfiles.find { it.isActive }
//            Log.d(
//                "SaboAIScreen",
//                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
//            )
//
//            if (activeProfile != null) {
//                // Save active profile ID to SharedPreferences
//                sharedPreferences.edit()
//                    .putInt("chatProfileId", activeProfile.id)
//                    .apply()
//
//                // Fetch chat history for active profile
//                chatViewModel.fetchChatHistory(
//                    userId = userId,
//                    platform = activeProfile.platform,
//                    profileId = activeProfile.id
//                )
//
//                // Check if we need to start a new chat
//                val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//                if (sessionId == -1) {
//                    // Mark as new chat mode - messages will be saved when user sends first message
//                    isNewChat = true
//                    Log.d("SaboAIScreen", "New chat mode activated for profile: ${activeProfile.username}")
//                }
//            } else {
//                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
//                // Clear profile ID and session ID
//                sharedPreferences.edit()
//                    .remove("chatProfileId")
//                    .remove("chatSessionId")
//                    .apply()
//                isNewChat = false
//            }
//        }
//    }
//
//    // Auto-scroll to bottom when new messages arrive
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
//            coroutineScope.launch {
//                val totalMessages = if (viewingHistoricalChat) {
//                    chatMessages.size + socketIOMessages.size
//                } else {
//                    socketIOMessages.size
//                }
//                if (totalMessages > 0) {
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    // Show chat when there are messages
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
//            showChat = true
//        }
//    }
//
//    val featureCards = listOf(
//        FeatureCard(
//            title = "Profile Enhance",
//            description = "Optimize your social media profile for...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Help me optimize my social media profile for maximum impact and engagement"
//        ),
//        FeatureCard(
//            title = "Bio Enhance",
//            description = "Create compelling bio that converts viewers...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create a compelling bio that converts viewers into followers"
//        ),
//        FeatureCard(
//            title = "Content idea creation",
//            description = "Generate fresh content ideas for your niche",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Generate fresh and creative content ideas for my social media niche"
//        ),
//        FeatureCard(
//            title = "Engaging content idea",
//            description = "Create content that drives engagement",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create content ideas that drive high engagement and interactions"
//        ),
//        FeatureCard(
//            title = "Suggest catchy blog post",
//            description = "Get ideas for compelling blog posts",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Suggest catchy and viral blog post ideas for my audience"
//        ),
//        FeatureCard(
//            title = "Unique reel creation",
//            description = "Create unique and viral reel concepts",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Help me create unique and viral reel concepts that stand out"
//        ),
//        FeatureCard(
//            title = "Analyze the current trends",
//            description = "Stay ahead with current social media trends",
//            icon = Icons.Default.TrendingUp,
//            backgroundColor = Color(0xFFFF9800),
//            prompt = "Analyze current social media trends and help me stay ahead of the curve"
//        ),
//        FeatureCard(
//            title = "Hashtag Strategy",
//            description = "Develop a winning hashtag strategy for growth",
//            icon = Icons.Default.Tag,
//            backgroundColor = Color(0xFFE91E63),
//            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
//        )
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            topBar = {
//                TopBarSabo(
//                    activeProfile = activeProfile,
//                    onClick = { isMenuOpen = true }
//                )
//            },
//            bottomBar = {
//                // Only show bottom bar when keyboard is closed
//                if (!isKeyboardOpen) {
//                    BottomNavBar(navController = navController)
//                }
//            },
//            modifier = Modifier
//                .fillMaxSize()
//        ) { paddingValues ->
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                val shouldShowChatInterface = showChat &&
//                        (socketIOMessages.isNotEmpty() ||
//                                (viewingHistoricalChat && chatMessages.isNotEmpty()))
//
//                if (shouldShowChatInterface) {
//                    ChatInterface(
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//
//
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//
//                                if (isNewChat) {
//                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                    if (profileId != -1) {
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating new message with userId: $userId, profileId: $profileId"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                        isNewChat = false
//                                    } else {
//                                        Log.e(
//                                            "SaboAIScreen",
//                                            "No profileId found in SharedPreferences"
//                                        )
//                                    }
//                                } else {
//                                    val sessionId = currentSessionId ?: -1
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with userId: $userId, sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                            }
//                        },
//
//
//                        connectionState = connectionState,
//                        socketIOMessages = socketIOMessages,
//                        historicalMessages = chatMessages,
//                        isViewingHistory = viewingHistoricalChat,
//                        messagesLoading = messagesLoading,
//                        messagesError = messagesError,
//                        listState = listState,
//                        onBackClick = {
//                            showChat = false
//                            viewingHistoricalChat = false
//                            isNewChat = false
//                            currentSessionId = null
//                            sharedPreferences.edit().remove("chatSessionId").apply()
//                            chatViewModel.clearCurrentSession()
//                            viewModel.clearMessages()
//                        }
//                    )
//                } else {
//                    FeatureCardsInterface(
//                        activeProfile = activeProfile,
//                        featureCards = featureCards,
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
//
//
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty()) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                if (isNewChat || profileId != -1) {
////                                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//                                    if (profileId != -1) {
//                                        Log.d(
//                                            "SaboAIScreen",
//                                            "Creating new message with userId: $userId, profileId: $profileId"
//                                        )
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                        isNewChat = false
//                                    }
//                                } else {
//                                    val sessionId = currentSessionId ?: -1
//                                    Log.d(
//                                        "SaboAIScreen",
//                                        "Sending message with userId: $userId, sessionId: $sessionId"
//                                    )
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = sessionId,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                                showChat = true
//                            }
//                        },
//
//
//
//                        onFeatureCardClick = { card ->
//                            val sessionId = currentSessionId ?: -1
//                            val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                            if (sessionId == -1 && profileId != -1) {
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Creating new message with userId: $userId, profileId: $profileId"
//                                )
//                                viewModel.createNewMessage(
//                                    userId = userId,
//                                    profileId = profileId,
//                                    message = card.prompt
//                                )
//                                isNewChat = false
//                            } else {
//                                Log.d(
//                                    "SaboAIScreen",
//                                    "Sending message with userId: $userId, sessionId: $sessionId"
//                                )
//                                viewModel.sendMessage(
//                                    userId = userId,
//                                    sessionId = sessionId,
//                                    message = card.prompt
//                                )
//                            }
//                            showChat = true
//                            viewingHistoricalChat = false
//                        }
//                    )
//                }
//            }
//        }
//
//        // Side Menu Overlay
//        if (isMenuOpen) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { isMenuOpen = false }
//            )
//
//            SideMenu(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(280.dp)
//                    .background(Color.White),
//                onClose = { isMenuOpen = false },
//                onAnalyzeProfileClick = { message ->
//                    val sessionId = currentSessionId ?: -1
//                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                    if (sessionId == -1 && profileId != -1) {
//                        Log.d(
//                            "SaboAIScreen",
//                            "Creating new message with userId: $userId, profileId: $profileId"
//                        )
//                        viewModel.createNewMessage(
//                            userId = userId,
//                            profileId = profileId,
//                            message = message
//                        )
//                        isNewChat = false
//                    } else {
//                        Log.d(
//                            "SaboAIScreen",
//                            "Sending message with userId: $userId, sessionId: $sessionId"
//                        )
//                        viewModel.sendMessage(
//                            userId = userId,
//                            sessionId = sessionId,
//                            message = message
//                        )
//                    }
//
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isMenuOpen = false
//                },
//                onStartChatting = {
//                    isMenuOpen = false
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isNewChat = true
//                    currentSessionId = null
//                    sharedPreferences.edit().remove("chatSessionId").apply()
//                    chatViewModel.clearCurrentSession()
//                    viewModel.clearMessages()
//                },
//                chatUserProfiles = chatUserProfiles,
//                isLoadingProfiles = profileLoading,
//                profileError = profileError,
//                onRefreshProfiles = {
//                    userProfile?.let {
//                        chatViewModel.fetchChatUserProfiles(it.id)
//                    }
//                },
//                chatViewModel = chatViewModel,
//                userId = userId,
//                chatHistory = chatHistory,
//                historyLoading = historyLoading,
//                historyError = historyError,
//                onChatHistoryClick = { session ->
//                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
//
//                    sharedPreferences.edit()
//                        .putInt("chatSessionId", session.sessionId)
//                        .apply()
//
//                    currentSessionId = session.sessionId
//                    chatViewModel.fetchChatMessages(session.sessionId)
//
//                    viewingHistoricalChat = true
//                    isMenuOpen = false
//                    showChat = true
//                },
//                onRefreshHistory = {
//                    val activeProfile = chatUserProfiles.find { it.isActive }
//                    Log.d(
//                        "SideMenu",
//                        "Refreshing history for active profile: ${activeProfile?.username}"
//                    )
//
//                    if (activeProfile != null) {
//                        val profileId = activeProfile.id
//                        chatViewModel.fetchChatHistory(
//                            userId = userId,
//                            platform = activeProfile.platform,
//                            profileId = profileId
//                        )
//                        sharedPreferences.edit()
//                            .putInt("chatProfileId", profileId)
//                            .apply()
//                    } else {
//                        chatViewModel.fetchChatHistory(userId = userId)
//                    }
//                },
//                onSessionChanged = { newSessionId ->
//                    currentSessionId = newSessionId
//                    if (newSessionId != null) {
//                        sharedPreferences.edit()
//                            .putInt("chatSessionId", newSessionId)
//                            .apply()
//                    } else {
//                        sharedPreferences.edit().remove("chatSessionId").apply()
//                    }
//                }
//            )
//        }
//    }
//}

*/








/*
package com.cc.creatorcircle.ui.screens.sabo

import android.R.attr.onClick
import android.content.Context
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.R
import com.cc.creatorcircle.viewModel.websocket.ChatViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import com.cc.creatorcircle.data.models.ChatSession
import com.cc.creatorcircle.data.socket.ChatMessage
import com.cc.creatorcircle.ui.components.TopBarSabo
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.data.socket.ConnectionState
import kotlinx.coroutines.delay

data class FeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val prompt: String = ""
)





@Composable
fun SaboAIScreen(
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    val postViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
        com.cc.creatorcircle.viewModel.ChatViewModel(context)
    }

    // User profile states
    val userProfile by postViewModel.userProfile.collectAsState()
    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
    val profileLoading by chatViewModel.profileLoading.collectAsState()
    val profileError by chatViewModel.profileError.collectAsState()

    // Chat history states
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    val historyLoading by chatViewModel.historyLoading.collectAsState()
    val historyError by chatViewModel.historyError.collectAsState()

    // Chat messages states (for historical messages)
    val chatMessages by chatViewModel.messages.collectAsState()
    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
    val messagesError by chatViewModel.messagesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var viewingHistoricalChat by remember { mutableStateOf(false) }
    var isNewChat by remember { mutableStateOf(false) }

    // Detect keyboard visibility
    val isKeyboardOpen by keyboardAsState()

    val activeProfile = chatUserProfiles.find { it.isActive }

    // Get SharedPreferences
    val sharedPreferences = remember {
        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
    }

    // Load sessionId from SharedPreferences - start with null for new chat
    var currentSessionId by remember {
        mutableStateOf<Int?>(null)
    }

    val currentProfileId by remember {
        derivedStateOf {
            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
        }
    }

    // Track the last session ID to detect changes
    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }

    val userId = userProfile?.id ?: -1

    // Socket.IO states (for real-time messages)
    val socketIOMessages by viewModel.messages.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()



// Detect session changes and reconnect socket
    LaunchedEffect(currentSessionId) {
        val newSessionId = currentSessionId // Create local copy
        val oldSessionId = lastSessionId

        if (newSessionId != oldSessionId) {
            Log.d(
                "SaboAIScreen",
                "Session changed from $oldSessionId to $newSessionId"
            )

            // Only reconnect if we have a valid new sessionId
            if (newSessionId != null && newSessionId != -1 && userId != -1) {
                Log.d("SaboAIScreen", "Reconnecting socket with new sessionId: $newSessionId")
                viewModel.connect(userId, newSessionId)
            }

            lastSessionId = newSessionId
        }
    }



    // 2. Add LaunchedEffect to listen for new session creation from socket
    LaunchedEffect(socketIOMessages.size, isNewChat) {
        if (socketIOMessages.isNotEmpty() && isNewChat) {
            delay(1500) // Wait for session to be created on server

            val activeProfile = chatUserProfiles.find { it.isActive }
            if (activeProfile != null) {
                // Fetch updated chat history to get the new session
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )

                delay(500) // Wait for history to load

                // Get the latest session (should be the newly created one)
                val latestSession = chatHistory.firstOrNull()
                if (latestSession != null) {
                    Log.d("SaboAIScreen", "New session captured: ${latestSession.sessionId}")

                    // Save the new session ID
                    sharedPreferences.edit()
                        .putInt("chatSessionId", latestSession.sessionId)
                        .apply()

                    currentSessionId = latestSession.sessionId
                    isNewChat = false

                    Log.d("SaboAIScreen", "Session saved. Future messages will use sessionId: ${latestSession.sessionId}")
                }
            }
        }
    }




// Connect to Socket.IO with sessionId from SharedPreferences
    LaunchedEffect(userId, currentSessionId) {
        if (userId != -1) {
            // Only connect if we have a valid session OR if we're starting fresh (sessionId = -1)
            val sessionId = currentSessionId ?: -1

            // For new chat mode, connect with -1 (no session yet)
            Log.d(
                "SaboAIScreen",
                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
            )
            viewModel.connect(userId, sessionId)
        }
    }

    // 1. Update LaunchedEffect(Unit) to CLEAR session on screen load
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        // Clear session when navigating to this screen
//        sharedPreferences.edit().remove("chatSessionId").apply()
//        currentSessionId = null
//        viewModel.clearMessages()
//        chatViewModel.clearCurrentSession()
//
//        Log.d("SaboAIScreen", "Screen loaded - Session cleared for new chat")
//    }

    LaunchedEffect(Unit) {
        isMenuOpen = false
        showChat = true
        viewingHistoricalChat = false

        // Clear session for new chat
        isNewChat = true
        currentSessionId = null
        sharedPreferences.edit().remove("chatSessionId").apply()

        chatViewModel.clearCurrentSession()
        viewModel.clearMessages()

        Log.d("SaboAIScreen", "Start Chatting - New chat mode activated")
    }


    LaunchedEffect(userProfile) {
        userProfile?.let {
            chatViewModel.fetchChatUserProfiles(it.id)
        }
    }

    // Detect session changes and clear socket messages
    LaunchedEffect(currentSessionId) {
        if (currentSessionId != lastSessionId) {
            Log.d(
                "SaboAIScreen",
                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
            )
            viewModel.clearMessages()
            lastSessionId = currentSessionId
        }
    }




    // Auto-reconnect on disconnection
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED ||
            connectionState == ConnectionState.ERROR
        ) {
            kotlinx.coroutines.delay(2000)
            val sessionId = currentSessionId ?: -1
            viewModel.connect(userId, sessionId)
        }
    }


    LaunchedEffect(chatUserProfiles) {
        if (chatUserProfiles.isNotEmpty()) {
            val activeProfile = chatUserProfiles.find { it.isActive }
            Log.d(
                "SaboAIScreen",
                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
            )

            if (activeProfile != null) {
                // Save active profile ID to SharedPreferences
                sharedPreferences.edit()
                    .putInt("chatProfileId", activeProfile.id)
                    .apply()

                // Fetch chat history for active profile
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )

                // Don't check sessionId from SharedPreferences, use current state
                // Since we cleared it on screen load, isNewChat should already be true
                Log.d(
                    "SaboAIScreen",
                    "Profile ready for new chat: ${activeProfile.username}, isNewChat: $isNewChat"
                )
            } else {
                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
                // Clear profile ID
                sharedPreferences.edit()
                    .remove("chatProfileId")
                    .apply()
                isNewChat = false
            }
        }
    }




    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
            coroutineScope.launch {
                val totalMessages = if (viewingHistoricalChat) {
                    chatMessages.size + socketIOMessages.size
                } else {
                    socketIOMessages.size
                }
                if (totalMessages > 0) {
                    listState.animateScrollToItem(totalMessages - 1)
                }
            }
        }
    }

    // Show chat when there are messages
    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
            showChat = true
        }
    }

    val featureCards = listOf(
        FeatureCard(
            title = "Profile Enhance",
            description = "Optimize your social media profile for...",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF9C27B0),
            prompt = "Help me optimize my social media profile for maximum impact and engagement"
        ),
        FeatureCard(
            title = "Bio Enhance",
            description = "Create compelling bio that converts viewers...",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF00BCD4),
            prompt = "Help me create a compelling bio that converts viewers into followers"
        ),
        FeatureCard(
            title = "Content idea creation",
            description = "Generate fresh content ideas for your niche",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF4CAF50),
            prompt = "Generate fresh and creative content ideas for my social media niche"
        ),
        FeatureCard(
            title = "Engaging content idea",
            description = "Create content that drives engagement",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF00BCD4),
            prompt = "Help me create content ideas that drive high engagement and interactions"
        ),
        FeatureCard(
            title = "Suggest catchy blog post",
            description = "Get ideas for compelling blog posts",
            icon = Icons.Default.Star,
            backgroundColor = Color(0xFF9C27B0),
            prompt = "Suggest catchy and viral blog post ideas for my audience"
        ),
        FeatureCard(
            title = "Unique reel creation",
            description = "Create unique and viral reel concepts",
            icon = Icons.Default.Create,
            backgroundColor = Color(0xFF4CAF50),
            prompt = "Help me create unique and viral reel concepts that stand out"
        ),
        FeatureCard(
            title = "Analyze the current trends",
            description = "Stay ahead with current social media trends",
            icon = Icons.Default.TrendingUp,
            backgroundColor = Color(0xFFFF9800),
            prompt = "Analyze current social media trends and help me stay ahead of the curve"
        ),
        FeatureCard(
            title = "Hashtag Strategy",
            description = "Develop a winning hashtag strategy for growth",
            icon = Icons.Default.Tag,
            backgroundColor = Color(0xFFE91E63),
            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBarSabo(
                    activeProfile = activeProfile,
                    onClick = { isMenuOpen = true }
                )
            },
            bottomBar = {
                // Only show bottom bar when keyboard is closed
                if (!isKeyboardOpen) {
                    BottomNavBar(navController = navController)
                }
            },
            modifier = Modifier
                .fillMaxSize()
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val shouldShowChatInterface = showChat &&
                        (socketIOMessages.isNotEmpty() ||
                                (viewingHistoricalChat && chatMessages.isNotEmpty()))

                if (shouldShowChatInterface) {
                    ChatInterface(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },


                        onSendClick = {
                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
                                val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)

                                if (profileId != -1) {
                                    if (savedSessionId == -1) {
                                        // No session exists - create new message (server will create session)
                                        Log.d("SaboAIScreen", "Creating new session with userId: $userId, profileId: $profileId")
                                        viewModel.createNewMessage(
                                            userId = userId,
                                            profileId = profileId,
                                            message = searchQuery
                                        )
                                        isNewChat = true
                                    } else {
                                        // Session exists - continue chat
                                        Log.d("SaboAIScreen", "Continuing chat with sessionId: $savedSessionId")
                                        viewModel.sendMessage(
                                            userId = userId,
                                            sessionId = savedSessionId,
                                            message = searchQuery
                                        )
                                    }
                                } else {
                                    // No active profile - send without saving
                                    Log.w("SaboAIScreen", "No active profile - sending message without saving")
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = -1,
                                        message = searchQuery
                                    )
                                }
                                searchQuery = ""
                            }
                        },


                        connectionState = connectionState,
                        socketIOMessages = socketIOMessages,
                        historicalMessages = chatMessages,
                        isViewingHistory = viewingHistoricalChat,
                        messagesLoading = messagesLoading,
                        messagesError = messagesError,
                        listState = listState,
                        onBackClick = {
                            showChat = false
                            viewingHistoricalChat = false
                            isNewChat = false

                            // Clear the session to start fresh next time
                            currentSessionId = null
                            sharedPreferences.edit().remove("chatSessionId").apply()

                            chatViewModel.clearCurrentSession()
                            viewModel.clearMessages()

                            Log.d("SaboAIScreen", "Back clicked - Session cleared")
                        }
                    )
                } else {
                    FeatureCardsInterface(
                        activeProfile = activeProfile,
                        featureCards = featureCards,
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },

                        // 3. Update onSendClick in FeatureCardsInterface similarly
                        onSendClick = {
                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
                                val profileId = sharedPreferences.getInt("chatProfileId", -1)

                                if (profileId != -1) {
                                    val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)

                                    if (savedSessionId == -1) {
                                        Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
                                        viewModel.createNewMessage(
                                            userId = userId,
                                            profileId = profileId,
                                            message = searchQuery
                                        )
                                        isNewChat = true
                                    } else {
                                        Log.d("SaboAIScreen", "Continuing chat with sessionId: $savedSessionId")
                                        viewModel.sendMessage(
                                            userId = userId,
                                            sessionId = savedSessionId,
                                            message = searchQuery
                                        )
                                    }
                                } else {
                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = -1,
                                        message = searchQuery
                                    )
                                }
                                searchQuery = ""
                            }
                        },



                        onFeatureCardClick = { card ->
                            val profileId = sharedPreferences.getInt("chatProfileId", -1)

                            if (profileId != -1) {
                                val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)

                                if (savedSessionId == -1) {
                                    Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
                                    viewModel.createNewMessage(
                                        userId = userId,
                                        profileId = profileId,
                                        message = card.prompt
                                    )
                                    // Keep isNewChat = true, let the LaunchedEffect handle it
                                } else {
                                    Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
                                    viewModel.sendMessage(
                                        userId = userId,
                                        sessionId = savedSessionId,
                                        message = card.prompt
                                    )
                                }
                            } else {
                                Log.d("SaboAIScreen", "No active profile - sending feature card message without saving")
                                viewModel.sendMessage(
                                    userId = userId,
                                    sessionId = -1,
                                    message = card.prompt
                                )
                            }
                            showChat = true
                            viewingHistoricalChat = false
                        }


                    )
                }
            }
        }

        // Side Menu Overlay
        if (isMenuOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isMenuOpen = false }
            )

            SideMenu(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White),
                onClose = { isMenuOpen = false },

                onAnalyzeProfileClick = { message ->
                    val profileId = sharedPreferences.getInt("chatProfileId", -1)

                    if (profileId != -1) {
                        val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)

                        if (savedSessionId == -1) {
                            Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
                            viewModel.createNewMessage(
                                userId = userId,
                                profileId = profileId,
                                message = message
                            )
                            // Keep isNewChat = true, let the LaunchedEffect handle it
                        } else {
                            Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
                            viewModel.sendMessage(
                                userId = userId,
                                sessionId = savedSessionId,
                                message = message
                            )
                        }
                    } else {
                        Log.d("SaboAIScreen", "No active profile - cannot analyze profile")
                    }

                    showChat = true
                    viewingHistoricalChat = false
                    isMenuOpen = false
                },


                onStartChatting = {
                    isMenuOpen = false
                    showChat = true
                    viewingHistoricalChat = false

                    // Clear session for new chat
                    isNewChat = true
                    currentSessionId = null
                    sharedPreferences.edit().remove("chatSessionId").apply()

                    chatViewModel.clearCurrentSession()
                    viewModel.clearMessages()

                    Log.d("SaboAIScreen", "Start Chatting - New chat mode activated")
                },

                chatUserProfiles = chatUserProfiles,
                isLoadingProfiles = profileLoading,
                profileError = profileError,
                onRefreshProfiles = {
                    userProfile?.let {
                        chatViewModel.fetchChatUserProfiles(it.id)
                    }
                },
                chatViewModel = chatViewModel,
                userId = userId,
                chatHistory = chatHistory,
                historyLoading = historyLoading,
                historyError = historyError,
                onChatHistoryClick = { session ->
                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")

                    sharedPreferences.edit()
                        .putInt("chatSessionId", session.sessionId)
                        .apply()

                    currentSessionId = session.sessionId
                    chatViewModel.fetchChatMessages(session.sessionId)

                    viewingHistoricalChat = true
                    isNewChat = false  // Important: We're viewing history, not creating new
                    isMenuOpen = false
                    showChat = true
                },
                onRefreshHistory = {
                    val activeProfile = chatUserProfiles.find { it.isActive }
                    Log.d(
                        "SideMenu",
                        "Refreshing history for active profile: ${activeProfile?.username}"
                    )

                    if (activeProfile != null) {
                        val profileId = activeProfile.id
                        chatViewModel.fetchChatHistory(
                            userId = userId,
                            platform = activeProfile.platform,
                            profileId = profileId
                        )
                        sharedPreferences.edit()
                            .putInt("chatProfileId", profileId)
                            .apply()
                    } else {
                        chatViewModel.fetchChatHistory(userId = userId)
                    }
                },
                onSessionChanged = { newSessionId ->
                    currentSessionId = newSessionId
                    if (newSessionId != null) {
                        sharedPreferences.edit()
                            .putInt("chatSessionId", newSessionId)
                            .apply()
                        isNewChat = false  // We have a session now
                    } else {
                        sharedPreferences.edit().remove("chatSessionId").apply()
                        isNewChat = true  // No session means new chat
                    }
                },
                onPinChat = {},
                onDeleteChat = {}
            )
        }
    }
}





// Helper function to detect keyboard state
@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    val isKeyboardOpen = remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardOpen.value = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return isKeyboardOpen
}


//@Composable
//fun SideMenu(
//    modifier: Modifier = Modifier,
//    onClose: () -> Unit,
//    onStartChatting: () -> Unit,
//    onAnalyzeProfileClick: (String) -> Unit,
//    chatUserProfiles: List<ChatUserProfile>,
//    isLoadingProfiles: Boolean = false,
//    profileError: String? = null,
//    onRefreshProfiles: () -> Unit = {},
//    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
//    userId: Int,
//    chatHistory: List<com.cc.creatorcircle.data.models.ChatSession> = emptyList(),
//    historyLoading: Boolean = false,
//    historyError: String? = null,
//    onChatHistoryClick: (com.cc.creatorcircle.data.models.ChatSession) -> Unit = {},
//    onRefreshHistory: () -> Unit = {},
//    onSessionChanged: (Int?) -> Unit = {},
//    onPinChat: (ChatSession) -> Unit,
//    onDeleteChat: (ChatSession) -> Unit,
//) {
//
//    // ... existing state variables
//    var chatToDelete by remember { mutableStateOf<ChatSession?>(null) }
//
//    // Observe delete chat session states
//    val deleteChatSessionLoading by chatViewModel.deleteChatSessionLoading.collectAsState()
//    val deleteChatSessionError by chatViewModel.deleteChatSessionError.collectAsState()
//    val chatSessionDeleted by chatViewModel.chatSessionDeleted.collectAsState()
//
//    val context = LocalContext.current
//    var showAccountsPopup by remember { mutableStateOf(false) }
//    var selectedTab by remember { mutableStateOf("General") }
//    var showProfiles by remember { mutableStateOf(true) }
//    var expanded by remember { mutableStateOf(false) } // ADD THIS LINE
//
//
//    // Observe set profile active states
//    val setProfileActiveLoading by chatViewModel.setProfileActiveLoading.collectAsState()
//    val setProfileActiveError by chatViewModel.setProfileActiveError.collectAsState()
//    val activeProfileUpdated by chatViewModel.activeProfileUpdated.collectAsState()
//
//    // Observe add profile states
//    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
//    val addProfileError by chatViewModel.addProfileError.collectAsState()
//    val profileAdded by chatViewModel.profileAdded.collectAsState()
//
//
//    // Observe delete profile states
//    val deleteProfileLoading by chatViewModel.deleteProfileLoading.collectAsState()
//    val deleteProfileError by chatViewModel.deleteProfileError.collectAsState()
//    val profileDeleted by chatViewModel.profileDeleted.collectAsState()
//
//    // Handle successful chat session deletion
//    LaunchedEffect(chatSessionDeleted) {
//        chatSessionDeleted?.let {
//            Log.d("SideMenu", "Chat session deleted successfully: ${it.message}")
//            onRefreshHistory()
//            chatViewModel.clearDeleteChatSessionState()
//        }
//    }
//
//
//    // Handle successful profile deletion
//    LaunchedEffect(profileDeleted) {
//        profileDeleted?.let {
//            Log.d("SideMenu", "Profile deleted successfully: ${it.message}")
//            onRefreshProfiles()
//            chatViewModel.clearDeleteProfileState()
//        }
//    }
//
//    LaunchedEffect(activeProfileUpdated) {
//        activeProfileUpdated?.let { updatedProfile ->
//            Log.d("SideMenu", "Profile ${updatedProfile.username} set as active successfully")
//
//            // SAVE TO SHAREDPREFERENCES
//            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//                .edit()
//                .putInt("chatProfileId", updatedProfile.id)
//                .apply()
//
//            chatViewModel.fetchChatHistory(
//                userId = userId,
//                platform = updatedProfile.platform,
//                profileId = updatedProfile.id
//            )
//        }
//    }
//
//    // Handle successful profile addition
//    LaunchedEffect(profileAdded) {
//        profileAdded?.let {
//            Log.d("SideMenu", "Profile ${it.username} added successfully")
//            onRefreshProfiles()
//            showAccountsPopup = false
//        }
//    }
//
//    // Convert List<ChatUserProfile> to List<InstagramProfile> for compatibility
//    val instagramProfiles = remember(chatUserProfiles) {
//        chatUserProfiles
//            .filter { it.platform.lowercase() == "instagram" }
//            .map { profile ->
//                InstagramProfile(
//                    id = profile.id.toString(),
//                    username = profile.username,
//                    profileUrl = profile.platformLink ?: "",
//                    isActive = profile.isActive
//                )
//            }
//    }
//
//    Column(
//        modifier = modifier.padding(16.dp)
//    ) {
//        // Header
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Menu",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//
//            IconButton(
//                onClick = onClose,
//                modifier = Modifier.size(32.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Close,
//                    contentDescription = "Close",
//                    tint = Color.Gray
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//
//        // Social Accounts Section
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                modifier = Modifier
//                    .weight(1f)
//                    .clickable { showProfiles = !showProfiles },
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = if (showProfiles) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
//                    contentDescription = if (showProfiles) "Collapse" else "Expand",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Icon(
//                    imageVector = Icons.Default.AccountCircle,
//                    contentDescription = "Social Accounts",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text(
//                    text = "Social Accounts",
//                    fontSize = 16.sp,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = instagramProfiles.size.toString(),
//                    fontSize = 14.sp,
//                    color = Color.Gray
//                )
//
//                if (isLoadingProfiles || setProfileActiveLoading || addProfileLoading) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(12.dp),
//                        strokeWidth = 1.dp,
//                        color = Color(0xFF9C27B0)
//                    )
//                }
//            }
//
//            Surface(
//                shape = RoundedCornerShape(4.dp),
//                color = Color(0xFF9C27B0),
//                modifier = Modifier.clickable { showAccountsPopup = true }
//            ) {
//                Text(
//                    text = "Add",
//                    color = Color.White,
//                    fontSize = 12.sp,
//                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(12.dp))
//
//        // Show profiles section only when expanded
//        if (showProfiles) {
//            // Show errors
//            setProfileActiveError?.let { error ->
//                Text(
//                    text = "Failed to set profile active: $error",
//                    fontSize = 10.sp,
//                    color = Color.Red,
//                    lineHeight = 12.sp,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//
//            addProfileError?.let { error ->
//                Text(
//                    text = "Failed to add profile: $error",
//                    fontSize = 10.sp,
//                    color = Color.Red,
//                    lineHeight = 12.sp,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//
//            // Profiles List
//            when {
//                isLoadingProfiles -> {
//                    Row(
//                        modifier = Modifier.padding(vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(16.dp),
//                            strokeWidth = 2.dp,
//                            color = Color(0xFF9C27B0)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Loading accounts...",
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
//                    }
//                }
//
//                profileError != null -> {
//                    Column {
//                        Text(
//                            text = "Failed to load accounts",
//                            fontSize = 12.sp,
//                            color = Color.Red,
//                            lineHeight = 16.sp
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        TextButton(
//                            onClick = onRefreshProfiles,
//                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
//                        ) {
//                            Text(
//                                text = "Retry",
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//                }
//
//                instagramProfiles.isNotEmpty() -> {
//                    LazyColumn(
//                        modifier = Modifier.heightIn(max = 200.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//
//
//                        // Inside the LazyColumn items block in SideMenu
//                        items(instagramProfiles) { profile ->
//                            InstagramProfileItem(
//                                profile = profile,
//                                onProfileClick = { clickedProfile ->
//                                    if (!clickedProfile.isActive) {
//                                        Log.d(
//                                            "SideMenu",
//                                            "Setting profile ${clickedProfile.username} as active"
//                                        )
//                                        chatViewModel.setProfileActive(
//                                            profileId = clickedProfile.id.toInt(),
//                                            userId = userId
//                                        )
//                                    }
//                                },
//                                onSelectClick = { clickedProfile ->
//                                    Log.d(
//                                        "SideMenu",
//                                        "Select clicked for profile ${clickedProfile.username}"
//                                    )
//                                    chatViewModel.setProfileActive(
//                                        profileId = clickedProfile.id.toInt(),
//                                        userId = userId
//                                    )
//                                },
//                                onDeleteClick = { clickedProfile ->
//                                    Log.d(
//                                        "SideMenu",
//                                        "Delete clicked for profile ${clickedProfile.username}"
//                                    )
//                                    chatViewModel.deleteChatProfile(
//                                        profileId = clickedProfile.id.toInt(),
//                                        userId = userId
//                                    )
//                                },
//                                isLoading = setProfileActiveLoading && !profile.isActive
//                            )
//                        }
//
//
//                    }
//                }
//
//                else -> {
//                    Text(
//                        text = "No accounts linked! Click \"Add\" to add your social accounts",
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        lineHeight = 16.sp
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable {
//                    onSessionChanged(null)
//                    // Clear sessionId from SharedPreferences
//                    val sharedPreferences =
//                        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//                    sharedPreferences.edit().remove("chatSessionId").apply()
//
//                    // Clear messages
//                    chatViewModel.clearCurrentSession()
//
//                    // NOTE: We're NOT calling createNewChatSession here anymore
//                    // Just trigger the callback to open chat screen in "new chat" mode
//                    onStartChatting()
//                }
//                .padding(vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                imageVector = Icons.Default.Add,
//                contentDescription = "New Chat",
//                tint = Color(0xFF9C27B0),
//                modifier = Modifier.size(20.dp)
//            )
//            Spacer(modifier = Modifier.width(12.dp))
//            Text(
//                text = "New Chat",
//                fontSize = 16.sp,
//                color = Color.Black
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(2.dp)
//        ) {
//            // Chat History Header
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    imageVector = Icons.Default.History,
//                    contentDescription = "Chat History",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text(
//                    text = "Chat History",
//                    fontSize = 16.sp,
//                    color = Color.Black
//                )
//
//                if (historyLoading) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(12.dp),
//                        strokeWidth = 1.dp,
//                        color = Color(0xFF9C27B0)
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // This Week indicator
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "This Week",
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = chatHistory.size.toString(),
//                    fontSize = 12.sp,
//                    color = Color(0xFF9C27B0),
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Chat History List
//            when {
//                historyLoading -> {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 16.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(16.dp),
//                                strokeWidth = 2.dp,
//                                color = Color(0xFF9C27B0)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = "Loading chat history...",
//                                fontSize = 12.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                }
//
//                historyError != null -> {
//                    Column {
//                        Text(
//                            text = "Failed to load chat history",
//                            fontSize = 12.sp,
//                            color = Color.Red,
//                            lineHeight = 16.sp
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        TextButton(
//                            onClick = onRefreshHistory,
//                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
//                        ) {
//                            Text(
//                                text = "Retry",
//                                fontSize = 12.sp
//                            )
//                        }
//                    }
//                }
//
//                chatHistory.isNotEmpty() -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(max = 300.dp),
//                        verticalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
////                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
////                            ChatHistoryItemWithMenu(
////                                session = session,
////                                onClick = {
////                                    Log.d(
////                                        "SideMenu",
////                                        "Chat history clicked - sessionId: ${session.sessionId}"
////                                    )
////
////                                    // Clear all messages first
////                                    chatViewModel.clearMessages()
////
////                                    // Set the session ID to trigger message loading
////                                    onSessionChanged(session.sessionId)
////
////                                    // Trigger the callback which saves to SharedPreferences
////                                    onChatHistoryClick(session)
////                                },
////                                onPinChat = { onPinChat(session) },
////                                onDeleteChat = { onDeleteChat(session) }
////                            )
////                        }
////
//
//                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
//                            ChatHistoryItemWithMenu(
//                                session = session,
//                                onClick = {
//                                    Log.d(
//                                        "SideMenu",
//                                        "Chat history clicked - sessionId: ${session.sessionId}"
//                                    )
//                                    chatViewModel.clearMessages()
//                                    onSessionChanged(session.sessionId)
//                                    onChatHistoryClick(session)
//                                },
//                                onPinChat = { onPinChat(session) },
//                                onDeleteChat = {
//                                    chatToDelete = session // Show confirmation dialog
//                                }
//                            )
//                        }
//
//
//
//                    }
//                }
//
//                else -> {
//                    Text(
//                        text = "No chat history found. Start a conversation to see your history here.",
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        lineHeight = 16.sp
//                    )
//                }
//            }
//
//        }
//    }
//
//    // Manage Social Accounts Popup
//    if (showAccountsPopup) {
//        ManageSocialAccountsPopup(
//            onDismiss = {
//                showAccountsPopup = false
//                chatViewModel.clearAddProfileState()
//            },
//            onLinkAccount = { url -> },
//            chatViewModel = chatViewModel,
//            userId = userId
//        )
//    }
//
//    // Add the confirmation dialog at the end of SideMenu
//    chatToDelete?.let { session ->
//        DeleteChatConfirmationDialog(
//            chatTitle = session.title,
//            onDismiss = { chatToDelete = null },
//            onConfirm = {
//                chatViewModel.deleteChatSession(
//                    sessionId = session.sessionId,
//                    userId = userId
//                )
//                chatToDelete = null
//            }
//        )
//    }
//
//    // Show delete error if any
//    deleteChatSessionError?.let { error ->
//        LaunchedEffect(error) {
//            Log.e("SideMenu", "Delete chat error: $error")
//            // You can show a Toast or Snackbar here
//        }
//    }
//
//}




@Composable
fun SideMenu(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onStartChatting: () -> Unit,
    onAnalyzeProfileClick: (String) -> Unit,
    chatUserProfiles: List<ChatUserProfile>,
    isLoadingProfiles: Boolean = false,
    profileError: String? = null,
    onRefreshProfiles: () -> Unit = {},
    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
    userId: Int,
    chatHistory: List<com.cc.creatorcircle.data.models.ChatSession> = emptyList(),
    historyLoading: Boolean = false,
    historyError: String? = null,
    onChatHistoryClick: (com.cc.creatorcircle.data.models.ChatSession) -> Unit = {},
    onRefreshHistory: () -> Unit = {},
    onSessionChanged: (Int?) -> Unit = {},
    onPinChat: (ChatSession) -> Unit,
    onDeleteChat: (ChatSession) -> Unit,
) {

    // ... existing state variables
    var chatToDelete by remember { mutableStateOf<ChatSession?>(null) }

    // Observe delete chat session states
    val deleteChatSessionLoading by chatViewModel.deleteChatSessionLoading.collectAsState()
    val deleteChatSessionError by chatViewModel.deleteChatSessionError.collectAsState()
    val chatSessionDeleted by chatViewModel.chatSessionDeleted.collectAsState()

    val context = LocalContext.current
    var showAccountsPopup by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("General") }
    var showProfiles by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) } // ADD THIS LINE


    // Observe set profile active states
    val setProfileActiveLoading by chatViewModel.setProfileActiveLoading.collectAsState()
    val setProfileActiveError by chatViewModel.setProfileActiveError.collectAsState()
    val activeProfileUpdated by chatViewModel.activeProfileUpdated.collectAsState()

    // Observe add profile states
    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
    val addProfileError by chatViewModel.addProfileError.collectAsState()
    val profileAdded by chatViewModel.profileAdded.collectAsState()


    // Observe delete profile states
    val deleteProfileLoading by chatViewModel.deleteProfileLoading.collectAsState()
    val deleteProfileError by chatViewModel.deleteProfileError.collectAsState()
    val profileDeleted by chatViewModel.profileDeleted.collectAsState()

    // Handle successful chat session deletion
    LaunchedEffect(chatSessionDeleted) {
        chatSessionDeleted?.let {
            Log.d("SideMenu", "Chat session deleted successfully: ${it.message}")
            onRefreshHistory()
            chatViewModel.clearDeleteChatSessionState()
        }
    }


    // Handle successful profile deletion
    LaunchedEffect(profileDeleted) {
        profileDeleted?.let {
            Log.d("SideMenu", "Profile deleted successfully: ${it.message}")
            onRefreshProfiles()
            chatViewModel.clearDeleteProfileState()
        }
    }

    LaunchedEffect(activeProfileUpdated) {
        activeProfileUpdated?.let { updatedProfile ->
            Log.d("SideMenu", "Profile ${updatedProfile.username} set as active successfully")

            // SAVE TO SHAREDPREFERENCES
            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("chatProfileId", updatedProfile.id)
                .apply()

            chatViewModel.fetchChatHistory(
                userId = userId,
                platform = updatedProfile.platform,
                profileId = updatedProfile.id
            )
        }
    }

    // Handle successful profile addition
    LaunchedEffect(profileAdded) {
        profileAdded?.let {
            Log.d("SideMenu", "Profile ${it.username} added successfully")
            onRefreshProfiles()
            showAccountsPopup = false
        }
    }

    // Convert List<ChatUserProfile> to List<InstagramProfile> for compatibility
    val instagramProfiles = remember(chatUserProfiles) {
        chatUserProfiles
            .filter { it.platform.lowercase() == "instagram" }
            .map { profile ->
                InstagramProfile(
                    id = profile.id.toString(),
                    username = profile.username,
                    profileUrl = profile.platformLink ?: "",
                    isActive = profile.isActive
                )
            }
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        // Social Accounts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showProfiles = !showProfiles },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (showProfiles) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                    contentDescription = if (showProfiles) "Collapse" else "Expand",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Social Accounts",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Social Accounts",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = instagramProfiles.size.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (isLoadingProfiles || setProfileActiveLoading || addProfileLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF9C27B0),
                modifier = Modifier.clickable { showAccountsPopup = true }
            ) {
                Text(
                    text = "Add",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show profiles section only when expanded
        if (showProfiles) {
            // Show errors
            setProfileActiveError?.let { error ->
                Text(
                    text = "Failed to set profile active: $error",
                    fontSize = 10.sp,
                    color = Color.Red,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            addProfileError?.let { error ->
                Text(
                    text = "Failed to add profile: $error",
                    fontSize = 10.sp,
                    color = Color.Red,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Profiles List
            when {
                isLoadingProfiles -> {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF9C27B0)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Loading accounts...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                profileError != null -> {
                    Column {
                        Text(
                            text = "Failed to load accounts",
                            fontSize = 12.sp,
                            color = Color.Red,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onRefreshProfiles,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                instagramProfiles.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {


                        // Inside the LazyColumn items block in SideMenu
                        items(instagramProfiles) { profile ->
                            InstagramProfileItem(
                                profile = profile,
                                onProfileClick = { clickedProfile ->
                                    if (!clickedProfile.isActive) {
                                        Log.d(
                                            "SideMenu",
                                            "Setting profile ${clickedProfile.username} as active"
                                        )
                                        chatViewModel.setProfileActive(
                                            profileId = clickedProfile.id.toInt(),
                                            userId = userId
                                        )
                                    }
                                },
                                onSelectClick = { clickedProfile ->
                                    Log.d(
                                        "SideMenu",
                                        "Select clicked for profile ${clickedProfile.username}"
                                    )
                                    chatViewModel.setProfileActive(
                                        profileId = clickedProfile.id.toInt(),
                                        userId = userId
                                    )
                                },
                                onDeleteClick = { clickedProfile ->
                                    Log.d(
                                        "SideMenu",
                                        "Delete clicked for profile ${clickedProfile.username}"
                                    )
                                    chatViewModel.deleteChatProfile(
                                        profileId = clickedProfile.id.toInt(),
                                        userId = userId
                                    )
                                },
                                isLoading = setProfileActiveLoading && !profile.isActive
                            )
                        }


                    }
                }

                else -> {
                    Text(
                        text = "No accounts linked! Click \"Add\" to add your social accounts",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onSessionChanged(null)
                    // Clear sessionId from SharedPreferences
                    val sharedPreferences =
                        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().remove("chatSessionId").apply()

                    // Clear messages
                    chatViewModel.clearCurrentSession()

                    // NOTE: We're NOT calling createNewChatSession here anymore
                    // Just trigger the callback to open chat screen in "new chat" mode
                    onStartChatting()
                }
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Chat",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "New Chat",
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(2.dp)
        ) {
            // Chat History Header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Chat History",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chat History",
                    fontSize = 16.sp,
                    color = Color.Black
                )

                if (historyLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // This Week indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This Week",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = chatHistory.size.toString(),
                    fontSize = 12.sp,
                    color = Color(0xFF9C27B0),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chat History List
            when {
                historyLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF9C27B0)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Loading chat history...",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                historyError != null -> {
                    Column {
                        Text(
                            text = "Failed to load chat history",
                            fontSize = 12.sp,
                            color = Color.Red,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onRefreshHistory,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
                        ) {
                            Text(
                                text = "Retry",
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                chatHistory.isNotEmpty() -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
//                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
//                            ChatHistoryItemWithMenu(
//                                session = session,
//                                onClick = {
//                                    Log.d(
//                                        "SideMenu",
//                                        "Chat history clicked - sessionId: ${session.sessionId}"
//                                    )
//
//                                    // Clear all messages first
//                                    chatViewModel.clearMessages()
//
//                                    // Set the session ID to trigger message loading
//                                    onSessionChanged(session.sessionId)
//
//                                    // Trigger the callback which saves to SharedPreferences
//                                    onChatHistoryClick(session)
//                                },
//                                onPinChat = { onPinChat(session) },
//                                onDeleteChat = { onDeleteChat(session) }
//                            )
//                        }
//

                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
                            ChatHistoryItemWithMenu(
                                session = session,
                                onClick = {
                                    Log.d(
                                        "SideMenu",
                                        "Chat history clicked - sessionId: ${session.sessionId}"
                                    )
                                    chatViewModel.clearMessages()
                                    onSessionChanged(session.sessionId)
                                    onChatHistoryClick(session)
                                },
                                onPinChat = { onPinChat(session) },
                                onDeleteChat = {
                                    chatToDelete = session // Show confirmation dialog
                                }
                            )
                        }



                    }
                }

                else -> {
                    Text(
                        text = "No chat history found. Start a conversation to see your history here.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }

        }
    }

    // Manage Social Accounts Popup
    if (showAccountsPopup) {
        ManageSocialAccountsPopup(
            onDismiss = {
                showAccountsPopup = false
                chatViewModel.clearAddProfileState()
            },
            onLinkAccount = { url -> },
            chatViewModel = chatViewModel,
            userId = userId
        )
    }

    // Add the confirmation dialog at the end of SideMenu
    chatToDelete?.let { session ->
        DeleteChatConfirmationDialog(
            chatTitle = session.title,
            onDismiss = { chatToDelete = null },
            onConfirm = {
                chatViewModel.deleteChatSession(
                    sessionId = session.sessionId,
                    userId = userId
                )
                chatToDelete = null
            }
        )
    }

    // Show delete error if any
    deleteChatSessionError?.let { error ->
        LaunchedEffect(error) {
            Log.e("SideMenu", "Delete chat error: $error")
            // You can show a Toast or Snackbar here
        }
    }

}






@Composable
fun ChatHistoryItemWithMenu(
    session: ChatSession,
    onClick: () -> Unit,
    onPinChat: () -> Unit,
    onDeleteChat: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Original ChatHistoryItem (without the three dots)
        Box(modifier = Modifier.weight(1f)) {
            ChatHistoryItem(
                session = session,
                onClick = onClick
            )
        }

        // Three dots menu
        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        onPinChat()
                        expanded = false
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            modifier = Modifier.size(18.dp)
                        )
                        Text("Pin chat", fontSize = 14.sp)
                    }
                }
                DropdownMenuItem(
                    onClick = {
                        onDeleteChat()
                        expanded = false
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = Color.Red
                        )
                        Text("Delete", fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
    }
}



@Composable
fun DeleteChatConfirmationDialog(
    chatTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Chat",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$chatTitle\"? This action cannot be undone.",
                fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun ChatInterface(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    connectionState: ConnectionState,
    socketIOMessages: List<ChatMessage>,
    historicalMessages: List<com.cc.creatorcircle.data.models.ChatMessage>,
    isViewingHistory: Boolean,
    messagesLoading: Boolean,
    messagesError: String?,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBackClick: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Track the last message to detect content changes (for streaming responses)
    val lastMessage = remember(socketIOMessages, historicalMessages, isViewingHistory) {
        if (isViewingHistory) {
            socketIOMessages.lastOrNull() ?: historicalMessages.lastOrNull()
        } else {
            socketIOMessages.lastOrNull()
        }
    }

    // Auto-scroll to bottom when messages change OR when last message content updates
    LaunchedEffect(socketIOMessages.size, historicalMessages.size, lastMessage) {
        if (socketIOMessages.isNotEmpty() || historicalMessages.isNotEmpty()) {
            delay(100) // Small delay to ensure content is rendered
            coroutineScope.launch {
                // Calculate total items count
                val totalItems = if (isViewingHistory) {
                    historicalMessages.size + socketIOMessages.size
                } else {
                    socketIOMessages.size
                }

                if (totalItems > 0) {
                    listState.animateScrollToItem(totalItems - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f)
                .fillMaxWidth(),
            state = listState
        ) {
            when {
                messagesLoading && historicalMessages.isEmpty() -> {
                    item(key = "loading_indicator") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF9C27B0)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Loading chat messages...",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                messagesError != null && historicalMessages.isEmpty() && socketIOMessages.isEmpty() -> {
                    item(key = "error_message") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Failed to load messages",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = messagesError,
                                    style = MaterialTheme.typography.body2,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                isViewingHistory -> {
                    // Show historical messages first
                    if (historicalMessages.isNotEmpty()) {
                        items(
                            items = historicalMessages,
                            key = { message -> "historical_${message.id}" }
                        ) { message ->
                            HistoricalMessageItem(message = message)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Then append any new socket messages (only for current session)
                    if (socketIOMessages.isNotEmpty()) {
                        items(
                            items = socketIOMessages,
                            key = { message -> "socket_${message.id}" }
                        ) { message ->
                            SocketIOMessageItem(message = message)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Show message if no messages at all
                    if (historicalMessages.isEmpty() && socketIOMessages.isEmpty()) {
                        item(key = "empty_history") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No messages in this chat",
                                    style = MaterialTheme.typography.body1,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                !isViewingHistory && socketIOMessages.isNotEmpty() -> {
                    items(
                        items = socketIOMessages,
                        key = { message -> "socket_${message.id}" }
                    ) { message ->
                        SocketIOMessageItem(message = message)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                else -> {
                    item(key = "start_conversation") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Start a conversation with Sabo AI...",
                                style = MaterialTheme.typography.body1,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        ChatInputField(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            onSendClick = onSendClick,
            isEnabled = connectionState == ConnectionState.CONNECTED,
            placeholder = if (connectionState == ConnectionState.CONNECTED) {
                "Type your message to get started..."
            } else {
                "Connecting to chat..."
            }
        )
    }
}


@Composable
fun ChatInputField(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean = true,
    placeholder: String = "Type your message to get started...",
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding(), // ADD THIS LINE
        color = Color.White
    ) {
        Column(
            modifier = Modifier.wrapContentHeight()
        ) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
//                verticalAlignment = Alignment.CenterVertically
            ) {


                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isFocused) 1.dp else 2.dp,
                            brush = if (isFocused) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Blue,
                                        Color.Blue
                                    ) // Blue to Green
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF893BCF),
                                        Color(0xFFEA3BA1)
                                    ) // Red to Yellow
                                )
                            },
                            shape = RoundedCornerShape(24.dp)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xFFF8F9FA),
                    elevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                            .wrapContentHeight()
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }


                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    isFocused = focusState.isFocused
                                },
                            textStyle = TextStyle(
                                color = Color(0xFF1A1A1A),
                                fontSize = 14.sp
                            ),
                            maxLines = 1,
                            enabled = isEnabled,
                            cursorBrush = SolidColor(Color(0xFF7C4DFF))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendClick,
                    enabled = searchQuery.isNotEmpty() && isEnabled,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = if (searchQuery.isNotEmpty() && isEnabled) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF893BCF), // Top purple-magenta
                                        Color(0xFFEA3BA1)  // Bottom deeper purple
                                    )
                                )
                            } else {
                                SolidColor(Color(0xFFE0E0E0))
                            },
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

            }
        }
    }
}


@Composable
fun HistoricalMessageItem(message: com.cc.creatorcircle.data.models.ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.role == "user") {
                Arrangement.End
            } else {
                Arrangement.Start
            }
        ) {
            Column(
                horizontalAlignment = if (message.role == "user") {
                    Alignment.End
                } else {
                    Alignment.Start
                }
            ) {
                Card(
                    modifier = Modifier
                        .wrapContentWidth()
                        .widthIn(max = 320.dp),
                    backgroundColor = if (message.role != "user") {
                        Color(0xFF7C4DFF)
                    } else {
                        Color(0xFFE9ECEF)
                    },
                    elevation = 0.dp,
                    shape = RoundedCornerShape(
                        topStart = if (message.role != "user") 4.dp else 18.dp,
                        topEnd = if (message.role != "user") 18.dp else 4.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                ) {
                    FormattedMessageContent(
                        content = message.content,
                        isAssistant = message.role != "user"
                    )
                }


                Spacer(modifier = Modifier.height(4.dp))

                // Format timestamp
                Text(
                    text = formatMessageTimestamp(message.timestamp),
                    style = MaterialTheme.typography.caption.copy(
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF6C757D),
                    modifier = Modifier.padding(
                        start = if (message.role == "user") 0.dp else 4.dp,
                        end = if (message.role == "user") 4.dp else 0.dp
                    )
                )
            }
        }
    }
}


// Updated ChatInputField with placeholder parameter

// NEW: Function to format message timestamps
fun formatMessageTimestamp(timestamp: String): String {
    return try {
        val instant = java.time.Instant.parse(timestamp)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        // Fallback formatting
        timestamp.substringBefore("T").let { date ->
            val time = timestamp.substringAfter("T").substringBefore("+").substringBefore(".")
            "$date ${time.substring(0, 5)}"
        }
    }
}


@Composable
fun ChatHistoryItem(
    session: com.cc.creatorcircle.data.models.ChatSession,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Platform icon

        Image(
            painter = painterResource(id = R.drawable.ic_robot_filled),
            contentDescription = session.platform,
            modifier = Modifier.size(24.dp)
        )



        Spacer(modifier = Modifier.width(12.dp))

        // Chat details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = session.title,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formatChatDate(session.updatedAt),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Platform badge
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            color = when (session.platform.lowercase()) {
//                "instagram" -> Color(0xFFE91E63).copy(alpha = 0.1f)
//                else -> Color(0xFF9C27B0).copy(alpha = 0.1f)
//            }
//        ) {
//            Text(
//                text = when (session.platform.lowercase()) {
//                    "instagram" -> "Instagram"
//                    else -> session.platform.capitalize()
//                },
//                fontSize = 10.sp,
//                color = when (session.platform.lowercase()) {
//                    "instagram" -> Color(0xFFE91E63)
//                    else -> Color(0xFF9C27B0)
//                },
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
//            )
//        }
    }
}

fun formatChatDate(dateString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd")
            .withZone(java.time.ZoneId.systemDefault())
        formatter.format(instant)
    } catch (e: Exception) {
        dateString.substringBefore("T")
    }
}


@Composable
fun InstagramProfileItem(
    profile: InstagramProfile,
    onProfileClick: (InstagramProfile) -> Unit,
    onSelectClick: (InstagramProfile) -> Unit = {},
    onDeleteClick: (InstagramProfile) -> Unit = {},
    isLoading: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!isLoading) {
                    onProfileClick(profile)
                }
            }
            .background(
                if (profile.isActive) Color(0xFFF3E5F5) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            painter = painterResource(id = R.drawable.ic_instagram),
            contentDescription = "Instagram",
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Instagram",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = "@${profile.username}",
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = if (profile.isActive) FontWeight.Medium else FontWeight.Normal
            )
        }

        if (profile.isActive) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF4CAF50)
            ) {
                Text(
                    text = "Active",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else if (isLoading) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFF9800)
            ) {
                Text(
                    text = "Setting...",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            IconButton(
                onClick = { expanded = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onSelectClick(profile)
                    }
                ) {
                    Text("Select")
                }
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        showDeleteConfirmation = true
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Profile",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete @${profile.username}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick(profile)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


// Data class for Instagram profile (unchanged)
data class InstagramProfile(
    val id: String,
    val username: String,
    val profileUrl: String,
    val isActive: Boolean
)


@Composable
fun ManageSocialAccountsPopup(
    onDismiss: () -> Unit,
    onLinkAccount: (String) -> Unit,
    // Add these new parameters
    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
    userId: Int
) {
    var instagramUsername by remember { mutableStateOf("") }

    // Observe add profile states
    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
    val addProfileError by chatViewModel.addProfileError.collectAsState()
    val profileAdded by chatViewModel.profileAdded.collectAsState()

    // Handle successful profile addition
    LaunchedEffect(profileAdded) {
        profileAdded?.let {
            Log.d("ManageAccounts", "Profile ${it.username} added successfully")
            instagramUsername = "" // Clear the input field
            // Optionally close the popup or show success message
        }
    }

    // Clear add profile state when dialog is dismissed
    DisposableEffect(Unit) {
        onDispose {
            chatViewModel.clearAddProfileState()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage social Accounts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No accounts linked ! Click \"Link\" to add your social accounts",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Instagram Profile Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Instagram Icon (using a colored surface as placeholder)
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE1306C) // Instagram gradient color approximation
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Instagram Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                        Text(
                            text = "0 Linked",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE8E8E8),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { /* Add new profile */ }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instagram Username Input (changed from URL to username)
                OutlinedTextField(
                    value = instagramUsername,
                    onValueChange = { instagramUsername = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter Instagram username (e.g., shadow_senpai_76)",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        backgroundColor = Color.Transparent,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !addProfileLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Show add profile error if exists
                addProfileError?.let { error ->
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = Color.Red,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Show success message
                profileAdded?.let { profile ->
                    Text(
                        text = "Profile @${profile.username} added successfully!",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Link Button
                Button(
                    onClick = {
                        if (instagramUsername.isNotBlank()) {
                            // Remove @ symbol if user entered it
                            val cleanUsername = instagramUsername.removePrefix("@").trim()
                            chatViewModel.addChatProfile(userId, "instagram", cleanUsername)
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = instagramUsername.isNotBlank() && !addProfileLoading
                ) {
                    if (addProfileLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = if (addProfileLoading) "Adding..." else "Link",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun SocketIOMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (message.isAssistant) {
                Arrangement.Start
            } else {
                Arrangement.End
            }
        ) {
            Card(
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(max = 320.dp),
                backgroundColor = if (message.isAssistant) {
                    Color(0xFF7C4DFF)
                } else {
                    Color(0xFFE9ECEF)
                },
                elevation = 0.dp,
                shape = RoundedCornerShape(
                    topStart = if (message.isAssistant) 4.dp else 18.dp,
                    topEnd = if (message.isAssistant) 18.dp else 4.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 18.dp
                )
            ) {
                FormattedMessageContent(
                    content = message.content,
                    isAssistant = message.isAssistant
                )
            }
        }
    }
}


@Composable
fun FormattedMessageContent(content: String, isAssistant: Boolean) {
    val textColor = if (isAssistant) Color.White else Color(0xFF212529)
    val lines = content.split("\n")

    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        var inList = false

        lines.forEach { line ->
            when {
                // Numbered list items (1. 2. 3. etc.)
                line.trim().matches(Regex("^\\d+\\.\\s+.*")) -> {
                    if (!inList) {
                        Spacer(modifier = Modifier.height(8.dp))
                        inList = true
                    }

                    val parts = line.trim().split(Regex("(?<=\\d\\.\\s)"), limit = 2)
                    if (parts.size == 2) {
                        Row(
                            modifier = Modifier.padding(bottom = 8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = parts[0],
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 20.sp
                                ),
                                color = textColor
                            )
                            FormattedText(
                                text = parts[1],
                                textColor = textColor,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(start = 4.dp),
                                defaultFontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bullet points (- starting lines)
                line.trim().startsWith("-") -> {
                    val cleanLine = line.trim().removePrefix("-").trim()
                    Row(
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.body1.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            ),
                            color = textColor
                        )
                        FormattedText(
                            text = cleanLine,
                            textColor = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Sub-content (indented content after list items)
                line.trim().isNotEmpty() && !line.trim().matches(Regex("^\\d+\\..*")) && inList -> {
                    FormattedText(
                        text = line.trim(),
                        textColor = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                // Empty line - reset list state
                line.trim().isEmpty() -> {
                    if (inList) {
                        Spacer(modifier = Modifier.height(4.dp))
                        inList = false
                    }
                }

                // Regular paragraph text
                else -> {
                    if (line.trim().isNotEmpty()) {
                        inList = false
                        FormattedText(
                            text = line.trim(),
                            textColor = textColor,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedText(
    text: String,
    textColor: Color,
    fontSize: TextUnit,
    lineHeight: TextUnit,
    modifier: Modifier = Modifier,
    defaultFontWeight: FontWeight = FontWeight.Normal
) {
    val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
    val parts = mutableListOf<Pair<String, Boolean>>()
    var lastIndex = 0

    boldPattern.findAll(text).forEach { matchResult ->
        // Add text before the bold section
        if (matchResult.range.first > lastIndex) {
            parts.add(text.substring(lastIndex, matchResult.range.first) to false)
        }
        // Add bold text (without the asterisks)
        parts.add(matchResult.groupValues[1] to true)
        lastIndex = matchResult.range.last + 1
    }

    // Add remaining text after last bold section
    if (lastIndex < text.length) {
        parts.add(text.substring(lastIndex) to false)
    }

    // If no bold formatting found, add entire text as non-bold
    if (parts.isEmpty()) {
        parts.add(text to false)
    }

    Text(
        text = buildAnnotatedString {
            parts.forEach { (part, isBold) ->
                withStyle(
                    style = SpanStyle(
                        fontWeight = if (isBold) FontWeight.Bold else defaultFontWeight
                    )
                ) {
                    append(part)
                }
            }
        },
        style = MaterialTheme.typography.body1.copy(
            fontSize = fontSize,
            lineHeight = lineHeight
        ),
        color = textColor,
        modifier = modifier
    )
}


// Alternative simpler version if you prefer markdown-like rendering
@Composable
fun SimpleFormattedMessageContent(content: String, isAssistant: Boolean) {
    val textColor = if (isAssistant) Color.White else Color(0xFF212529)

    Column(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        val annotatedString = buildAnnotatedString {
            val lines = content.split("\n")

            lines.forEachIndexed { index, line ->
                when {
                    // Numbered items - make bold
                    line.trim().matches(Regex("^\\d+\\.\\s+.*")) -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(line.trim())
                        }
                    }
                    // Bullet points
                    line.trim().startsWith("-") -> {
                        append("  • ${line.trim().removePrefix("-").trim()}")
                    }
                    // Regular text
                    else -> {
                        append(line.trim())
                    }
                }

                // Add line break if not the last line
                if (index < lines.size - 1 && line.trim().isNotEmpty()) {
                    append("\n")
                }
            }
        }

        Text(
            text = annotatedString,
            style = MaterialTheme.typography.body1.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = textColor
        )
    }
}


@Composable
fun FeatureCardsInterface(
    activeProfile: ChatUserProfile?,
    featureCards: List<FeatureCard>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onFeatureCardClick: (FeatureCard) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("Hello! I'm ")
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFF9C27B0),
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("SABO")
                        }
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Your AI expert for viral social media content. I can help you with creative strategies and content ideas.",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(20.dp))


            }


            if (activeProfile?.username == null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Image(
                            painter = painterResource(id = R.drawable.ic_instagram),
                            contentDescription = "Instagram",
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Provide your IG",
                            fontSize = 15.sp,
                            color = Color.Blue
                        )
                    }
                }
            }


//
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 12.dp),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_instagram),
//                        contentDescription = "Instagram",
//                        modifier = Modifier.size(24.dp)
//                    )
//
//
//                    Spacer(modifier = Modifier.width(8.dp))
//
//                    Text(
//                        text = activeProfile?.username?.let { "@$it" } ?: "Provide your IG",
//                        fontSize = 15.sp,
//                        color = Color.Blue
//                    )
//                }
//
//
//            }


            item {
                ChatInputField(
                    searchQuery = searchQuery,
                    onQueryChange = onQueryChange,
                    onSendClick = onSendClick,
                    isEnabled = true,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "What would you like to work on today?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

//                Spacer(modifier = Modifier.height(24.dp))
            }


            item {
                Spacer(modifier = Modifier.height(24.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(600.dp),
//                    modifier = Modifier.height(850.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(featureCards) { card ->
                        FeatureCardItem(
                            card = card,
                            onClick = { onFeatureCardClick(card) }
                        )
                    }
                }
            }
        }

    }
}


@Composable
fun FeatureCardItem(
    card: FeatureCard,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {


            Text(
                text = card.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}


//@Composable
//fun SaboAIScreen(
//    navController: NavController,
//    viewModel: ChatViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val postViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
//        com.cc.creatorcircle.viewModel.ChatViewModel(context)
//    }
//
//    // User profile states
//    val userProfile by postViewModel.userProfile.collectAsState()
//    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
//    val profileLoading by chatViewModel.profileLoading.collectAsState()
//    val profileError by chatViewModel.profileError.collectAsState()
//
//    // Chat history states
//    val chatHistory by chatViewModel.chatHistory.collectAsState()
//    val historyLoading by chatViewModel.historyLoading.collectAsState()
//    val historyError by chatViewModel.historyError.collectAsState()
//
//    // Chat messages states (for historical messages)
//    val chatMessages by chatViewModel.messages.collectAsState()
//    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
//    val messagesError by chatViewModel.messagesError.collectAsState()
//
//    var searchQuery by remember { mutableStateOf("") }
//    var showChat by remember { mutableStateOf(false) }
//    var isMenuOpen by remember { mutableStateOf(false) }
//    var viewingHistoricalChat by remember { mutableStateOf(false) }
//    var isNewChat by remember { mutableStateOf(false) }
//
//    // Detect keyboard visibility
//    val isKeyboardOpen by keyboardAsState()
//
//    val activeProfile = chatUserProfiles.find { it.isActive }
//
//    // Get SharedPreferences
//    val sharedPreferences = remember {
//        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//    }
//
//    // Load sessionId from SharedPreferences - start with null for new chat
//    var currentSessionId by remember {
//        mutableStateOf<Int?>(null)
//    }
//
//    val currentProfileId by remember {
//        derivedStateOf {
//            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
//        }
//    }
//
//    // Track the last session ID to detect changes
//    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }
//
//    val userId = userProfile?.id ?: -1
//
//    // Socket.IO states (for real-time messages)
//    val socketIOMessages by viewModel.messages.collectAsState()
//    val connectionState by viewModel.connectionState.collectAsState()
//
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//
//
//    // Listen for new session creation from socket messages and refresh history
//    // Listen for new session creation and refresh history
////    LaunchedEffect(socketIOMessages.size) {
////        // Only proceed if we're in new chat mode and have messages
////        if (isNewChat && socketIOMessages.isNotEmpty()) {
////            // Wait a bit for the session to be created on the backend
////            kotlinx.coroutines.delay(1000)
////
////            // Refresh chat history to show the newly created session
////            val activeProfile = chatUserProfiles.find { it.isActive }
////            if (activeProfile != null) {
////                Log.d("SaboAIScreen", "New messages received - refreshing chat history")
////                chatViewModel.fetchChatHistory(
////                    userId = userId,
////                    platform = activeProfile.platform,
////                    profileId = activeProfile.id
////                )
////
////                // Mark that we're no longer in new chat mode
////                isNewChat = false
////            }
////        }
////    }
//
//    // 2. Add LaunchedEffect to listen for new session creation from socket
////    LaunchedEffect(socketIOMessages.size) {
////        if (socketIOMessages.isNotEmpty()) {
////            // Check if we received a session_id from the socket response
////            val lastMessage = socketIOMessages.lastOrNull()
////            // Assuming your socket message contains sessionId when new session is created
////            // Adjust based on your actual socket response structure
////
////            // After creating new message, backend should return sessionId
////            // Wait a bit and fetch the latest session
////            if (isNewChat) {
////                kotlinx.coroutines.delay(1500)
////                val activeProfile = chatUserProfiles.find { it.isActive }
////                if (activeProfile != null) {
////                    // Fetch chat history to get the new session
////                    chatViewModel.fetchChatHistory(
////                        userId = userId,
////                        platform = activeProfile.platform,
////                        profileId = activeProfile.id
////                    )
////
////                    // Get the latest session from history
////                    kotlinx.coroutines.delay(500)
////                    val latestSession = chatHistory.firstOrNull()
////                    if (latestSession != null) {
////                        Log.d("SaboAIScreen", "New session created: ${latestSession.sessionId}")
////                        sharedPreferences.edit()
////                            .putInt("chatSessionId", latestSession.sessionId)
////                            .apply()
////                        currentSessionId = latestSession.sessionId
////                        isNewChat = true
////                    }
////                }
////            }
////        }
////    }
//
//
//    // 2. Add LaunchedEffect to listen for new session creation from socket
//    LaunchedEffect(socketIOMessages.size) {
//        if (socketIOMessages.isNotEmpty()) {
//            // After creating new message, backend should return sessionId
//            // Wait a bit and fetch the latest session
//            if (isNewChat) {
//                kotlinx.coroutines.delay(1500)
//                val activeProfile = chatUserProfiles.find { it.isActive }
//                if (activeProfile != null) {
//                    // Fetch chat history to get the new session
//                    chatViewModel.fetchChatHistory(
//                        userId = userId,
//                        platform = activeProfile.platform,
//                        profileId = activeProfile.id
//                    )
//
//                    // Get the latest session from history
//                    kotlinx.coroutines.delay(500)
//                    val latestSession = chatHistory.firstOrNull()
//                    if (latestSession != null) {
//                        Log.d("SaboAIScreen", "New session created: ${latestSession.sessionId}")
//                        sharedPreferences.edit()
//                            .putInt("chatSessionId", latestSession.sessionId)
//                            .apply()
//                        currentSessionId = latestSession.sessionId
//                        isNewChat = false
//                    }
//                }
//            }
//        }
//    }
//
//
////    // Fetch user profile and initialize chat session when screen loads
////    LaunchedEffect(Unit) {
////        postViewModel.fetchUserProfile()
////
////        // ALWAYS clear session when navigating to this screen to force new chat
////        sharedPreferences.edit().remove("chatSessionId").apply()
////        currentSessionId = null
////        isNewChat = true
////        viewModel.clearMessages()
////        chatViewModel.clearCurrentSession()
////
////        Log.d("SaboAIScreen", "Screen loaded - Session cleared, New chat mode activated")
////    }
//
////    LaunchedEffect(Unit) {
////        postViewModel.fetchUserProfile()
////
////        // Load existing session from SharedPreferences
////        val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
////        if (savedSessionId != -1) {
////            currentSessionId = savedSessionId
////            isNewChat = false
////            Log.d("SaboAIScreen", "Restored session: $savedSessionId")
////        } else {
////            currentSessionId = null
////            isNewChat = true
////            viewModel.clearMessages()
////            chatViewModel.clearCurrentSession()
////            Log.d("SaboAIScreen", "No saved session - New chat mode activated")
////        }
////    }
//
//    // 1. Update LaunchedEffect(Unit) to CLEAR session on screen load
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        // ALWAYS clear session when navigating to this screen to force new chat
//        sharedPreferences.edit().remove("chatSessionId").apply()
//        currentSessionId = null
//        isNewChat = true
//        viewModel.clearMessages()
//        chatViewModel.clearCurrentSession()
//
//        Log.d("SaboAIScreen", "Screen loaded - Session cleared, New chat mode activated")
//    }
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let {
//            chatViewModel.fetchChatUserProfiles(it.id)
//        }
//    }
//
//    // Detect session changes and clear socket messages
//    LaunchedEffect(currentSessionId) {
//        if (currentSessionId != lastSessionId) {
//            Log.d(
//                "SaboAIScreen",
//                "Session changed from $lastSessionId to $currentSessionId - clearing socket messages"
//            )
//            viewModel.clearMessages()
//            lastSessionId = currentSessionId
//        }
//    }
//
//    // Connect to Socket.IO with sessionId from SharedPreferences
//    LaunchedEffect(userId, currentSessionId) {
//        if (userId != -1) {
//            // Only connect if we have a valid session OR if we're starting fresh (sessionId = -1)
//            val sessionId = currentSessionId ?: -1
//
//            // For new chat mode, connect with -1 (no session yet)
//            Log.d(
//                "SaboAIScreen",
//                "Connecting to Socket.IO with userId: $userId, sessionId: $sessionId"
//            )
//            viewModel.connect(userId, sessionId)
//        }
//    }
//
//
//    // Auto-reconnect on disconnection
//    LaunchedEffect(connectionState) {
//        if (connectionState == ConnectionState.DISCONNECTED ||
//            connectionState == ConnectionState.ERROR
//        ) {
//            kotlinx.coroutines.delay(2000)
//            val sessionId = currentSessionId ?: -1
//            viewModel.connect(userId, sessionId)
//        }
//    }
//
//
//    LaunchedEffect(chatUserProfiles) {
//        if (chatUserProfiles.isNotEmpty()) {
//            val activeProfile = chatUserProfiles.find { it.isActive }
//            Log.d(
//                "SaboAIScreen",
//                "Active profile found: ${activeProfile?.username} (${activeProfile?.platform}) - ID: ${activeProfile?.id}"
//            )
//
//            if (activeProfile != null) {
//                // Save active profile ID to SharedPreferences
//                sharedPreferences.edit()
//                    .putInt("chatProfileId", activeProfile.id)
//                    .apply()
//
//                // Fetch chat history for active profile
//                chatViewModel.fetchChatHistory(
//                    userId = userId,
//                    platform = activeProfile.platform,
//                    profileId = activeProfile.id
//                )
//
//                // Don't check sessionId from SharedPreferences, use current state
//                // Since we cleared it on screen load, isNewChat should already be true
//                Log.d(
//                    "SaboAIScreen",
//                    "Profile ready for new chat: ${activeProfile.username}, isNewChat: $isNewChat"
//                )
//            } else {
//                Log.w("SaboAIScreen", "No active profile found, chat will not be saved")
//                // Clear profile ID
//                sharedPreferences.edit()
//                    .remove("chatProfileId")
//                    .apply()
//                isNewChat = false
//            }
//        }
//    }
//
//
//
//
//    // Auto-scroll to bottom when new messages arrive
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if ((socketIOMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
//            coroutineScope.launch {
//                val totalMessages = if (viewingHistoricalChat) {
//                    chatMessages.size + socketIOMessages.size
//                } else {
//                    socketIOMessages.size
//                }
//                if (totalMessages > 0) {
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    // Show chat when there are messages
//    LaunchedEffect(socketIOMessages.size, chatMessages.size) {
//        if (socketIOMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
//            showChat = true
//        }
//    }
//
//    val featureCards = listOf(
//        FeatureCard(
//            title = "Profile Enhance",
//            description = "Optimize your social media profile for...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Help me optimize my social media profile for maximum impact and engagement"
//        ),
//        FeatureCard(
//            title = "Bio Enhance",
//            description = "Create compelling bio that converts viewers...",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create a compelling bio that converts viewers into followers"
//        ),
//        FeatureCard(
//            title = "Content idea creation",
//            description = "Generate fresh content ideas for your niche",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Generate fresh and creative content ideas for my social media niche"
//        ),
//        FeatureCard(
//            title = "Engaging content idea",
//            description = "Create content that drives engagement",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF00BCD4),
//            prompt = "Help me create content ideas that drive high engagement and interactions"
//        ),
//        FeatureCard(
//            title = "Suggest catchy blog post",
//            description = "Get ideas for compelling blog posts",
//            icon = Icons.Default.Star,
//            backgroundColor = Color(0xFF9C27B0),
//            prompt = "Suggest catchy and viral blog post ideas for my audience"
//        ),
//        FeatureCard(
//            title = "Unique reel creation",
//            description = "Create unique and viral reel concepts",
//            icon = Icons.Default.Create,
//            backgroundColor = Color(0xFF4CAF50),
//            prompt = "Help me create unique and viral reel concepts that stand out"
//        ),
//        FeatureCard(
//            title = "Analyze the current trends",
//            description = "Stay ahead with current social media trends",
//            icon = Icons.Default.TrendingUp,
//            backgroundColor = Color(0xFFFF9800),
//            prompt = "Analyze current social media trends and help me stay ahead of the curve"
//        ),
//        FeatureCard(
//            title = "Hashtag Strategy",
//            description = "Develop a winning hashtag strategy for growth",
//            icon = Icons.Default.Tag,
//            backgroundColor = Color(0xFFE91E63),
//            prompt = "Help me develop a winning hashtag strategy for maximum reach and growth"
//        )
//    )
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        Scaffold(
//            topBar = {
//                TopBarSabo(
//                    activeProfile = activeProfile,
//                    onClick = { isMenuOpen = true }
//                )
//            },
//            bottomBar = {
//                // Only show bottom bar when keyboard is closed
//                if (!isKeyboardOpen) {
//                    BottomNavBar(navController = navController)
//                }
//            },
//            modifier = Modifier
//                .fillMaxSize()
//        ) { paddingValues ->
//
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(paddingValues)
//            ) {
//                val shouldShowChatInterface = showChat &&
//                        (socketIOMessages.isNotEmpty() ||
//                                (viewingHistoricalChat && chatMessages.isNotEmpty()))
//
//                if (shouldShowChatInterface) {
//                    ChatInterface(
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
////
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    // Active profile exists
////                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
////                                        // Create new chat session
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Creating new message with userId: $userId, profileId: $profileId"
////                                        )
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////                                        isNewChat = false
////                                    } else {
////                                        // Continue existing chat
////                                        val sessionId = currentSessionId ?: -1
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Sending message with userId: $userId, sessionId: $sessionId"
////                                        )
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = sessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    // No active profile - send message without saving
////                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                            }
////                        },
////
//
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    // Active profile exists
////                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
////                                        // Create new chat session
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Creating new message with userId: $userId, profileId: $profileId"
////                                        )
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////
////                                        // Refresh history after a delay to allow backend to process
////                                        coroutineScope.launch {
////                                            kotlinx.coroutines.delay(2000)
////                                            val activeProfile =
////                                                chatUserProfiles.find { it.isActive }
////                                            if (activeProfile != null) {
////                                                Log.d(
////                                                    "SaboAIScreen",
////                                                    "Refreshing chat history after new session"
////                                                )
////                                                chatViewModel.fetchChatHistory(
////                                                    userId = userId,
////                                                    platform = activeProfile.platform,
////                                                    profileId = activeProfile.id
////                                                )
////                                            }
////                                        }
////
////                                        isNewChat = false
////                                    } else {
////                                        // Continue existing chat
////                                        val sessionId = currentSessionId ?: -1
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Sending message with userId: $userId, sessionId: $sessionId"
////                                        )
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = sessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    // No active profile - send message without saving
////                                    Log.d(
////                                        "SaboAIScreen",
////                                        "No active profile - sending message without saving to history"
////                                    )
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                            }
////                        },
//
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
////
////                                    // Only create new chat if no session exists in SharedPreferences
////                                    if (savedSessionId == -1) {
////                                        // Create new chat session
////                                        Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////
////                                        // Refresh history after a delay
////                                        coroutineScope.launch {
////                                            kotlinx.coroutines.delay(2000)
////                                            val activeProfile = chatUserProfiles.find { it.isActive }
////                                            if (activeProfile != null) {
////                                                chatViewModel.fetchChatHistory(
////                                                    userId = userId,
////                                                    platform = activeProfile.platform,
////                                                    profileId = activeProfile.id
////                                                )
////                                            }
////                                        }
////                                        isNewChat = false
////                                    } else {
////                                        // Continue existing chat
////                                        Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = savedSessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    // No active profile - send message without saving
////                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                            }
////                        },
//
//                        // 2. Update onSendClick in ChatInterface to save sessionId
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
////
////                                    if (savedSessionId == -1) {
////                                        // Create new chat session
////                                        Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////
////                                        // Mark as new chat to trigger session capture
////                                        isNewChat = true
////                                    } else {
////                                        // Continue existing chat with saved sessionId
////                                        Log.d("SaboAIScreen", "Continuing chat with sessionId: $savedSessionId")
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = savedSessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    // No active profile - send message without saving
////                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                            }
////                        },
//
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                                if (profileId != -1) {
//                                    val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
//
//                                    if (savedSessionId == -1) {
//                                        // Create new chat session
//                                        Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//
//                                        // Mark as new chat to trigger session capture
//                                        isNewChat = true
//                                    } else {
//                                        // Continue existing chat with saved sessionId
//                                        Log.d("SaboAIScreen", "Continuing chat with sessionId: $savedSessionId")
//                                        viewModel.sendMessage(
//                                            userId = userId,
//                                            sessionId = savedSessionId,
//                                            message = searchQuery
//                                        )
//                                    }
//                                } else {
//                                    // No active profile - send message without saving
//                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = -1,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                            }
//                        },
//
//
//                        connectionState = connectionState,
//                        socketIOMessages = socketIOMessages,
//                        historicalMessages = chatMessages,
//                        isViewingHistory = viewingHistoricalChat,
//                        messagesLoading = messagesLoading,
//                        messagesError = messagesError,
//                        listState = listState,
//                        onBackClick = {
//                            showChat = false
//                            viewingHistoricalChat = false
//                            isNewChat = false
//                            currentSessionId = null
//                            sharedPreferences.edit().remove("chatSessionId").apply()
//                            chatViewModel.clearCurrentSession()
//                            viewModel.clearMessages()
//                        }
//                    )
//                } else {
//                    FeatureCardsInterface(
//                        activeProfile = activeProfile,
//                        featureCards = featureCards,
//                        searchQuery = searchQuery,
//                        onQueryChange = { searchQuery = it },
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty()) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    // Active profile exists
////                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
////                                        // Create new chat session
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Creating new message with userId: $userId, profileId: $profileId"
////                                        )
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////                                        isNewChat = false
////                                    } else {
////                                        // Continue existing chat
////                                        val sessionId = currentSessionId ?: -1
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Sending message with userId: $userId, sessionId: $sessionId"
////                                        )
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = sessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    // No active profile - send message without saving
////                                    Log.d(
////                                        "SaboAIScreen",
////                                        "No active profile - sending message without saving to history"
////                                    )
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                                showChat = true
////                            }
////                        },
////
////
//
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    // Active profile exists
////                                    if (isNewChat || currentSessionId == null || currentSessionId == -1) {
////                                        // Create new chat session
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Creating new message with userId: $userId, profileId: $profileId"
////                                        )
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////
////                                        // Refresh history after a delay to allow backend to process
////                                        coroutineScope.launch {
////                                            kotlinx.coroutines.delay(2000)
////                                            val activeProfile = chatUserProfiles.find { it.isActive }
////                                            if (activeProfile != null) {
////                                                Log.d("SaboAIScreen", "Refreshing chat history after new session")
////                                                chatViewModel.fetchChatHistory(
////                                                    userId = userId,
////                                                    platform = activeProfile.platform,
////                                                    profileId = activeProfile.id
////                                                )
////                                            }
////                                        }
////
////                                        isNewChat = false
////                                    } else {
////                                        // Continue existing chat
////                                        val sessionId = currentSessionId ?: -1
////                                        Log.d(
////                                            "SaboAIScreen",
////                                            "Sending message with userId: $userId, sessionId: $sessionId"
////                                        )
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = sessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    // No active profile - send message without saving
////                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                            }
////                        },
////
////                        onSendClick = {
////                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
////                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                                if (profileId != -1) {
////                                    val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
////
////                                    if (savedSessionId == -1) {
////                                        Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
////                                        viewModel.createNewMessage(
////                                            userId = userId,
////                                            profileId = profileId,
////                                            message = searchQuery
////                                        )
////
////                                        coroutineScope.launch {
////                                            kotlinx.coroutines.delay(2000)
////                                            val activeProfile = chatUserProfiles.find { it.isActive }
////                                            if (activeProfile != null) {
////                                                chatViewModel.fetchChatHistory(
////                                                    userId = userId,
////                                                    platform = activeProfile.platform,
////                                                    profileId = activeProfile.id
////                                                )
////                                            }
////                                        }
////                                        isNewChat = false
////                                    } else {
////                                        Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
////                                        viewModel.sendMessage(
////                                            userId = userId,
////                                            sessionId = savedSessionId,
////                                            message = searchQuery
////                                        )
////                                    }
////                                } else {
////                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = -1,
////                                        message = searchQuery
////                                    )
////                                }
////                                searchQuery = ""
////                            }
////                        },
//
//                        // 3. Update onSendClick in FeatureCardsInterface similarly
//                        onSendClick = {
//                            if (searchQuery.isNotEmpty() && connectionState == ConnectionState.CONNECTED) {
//                                val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                                if (profileId != -1) {
//                                    val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
//
//                                    if (savedSessionId == -1) {
//                                        Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
//                                        viewModel.createNewMessage(
//                                            userId = userId,
//                                            profileId = profileId,
//                                            message = searchQuery
//                                        )
//                                        isNewChat = true
//                                    } else {
//                                        Log.d("SaboAIScreen", "Continuing chat with sessionId: $savedSessionId")
//                                        viewModel.sendMessage(
//                                            userId = userId,
//                                            sessionId = savedSessionId,
//                                            message = searchQuery
//                                        )
//                                    }
//                                } else {
//                                    Log.d("SaboAIScreen", "No active profile - sending message without saving to history")
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = -1,
//                                        message = searchQuery
//                                    )
//                                }
//                                searchQuery = ""
//                            }
//                        },
//
////                        onFeatureCardClick = { card ->
////                            val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                            if (profileId != -1) {
////                                // Active profile exists
////                                if (isNewChat || currentSessionId == null || currentSessionId == -1) {
////                                    // Create new chat session
////                                    Log.d(
////                                        "SaboAIScreen",
////                                        "Creating new message with userId: $userId, profileId: $profileId"
////                                    )
////                                    viewModel.createNewMessage(
////                                        userId = userId,
////                                        profileId = profileId,
////                                        message = card.prompt
////                                    )
////                                    isNewChat = false
////                                } else {
////                                    // Continue existing chat
////                                    val sessionId = currentSessionId ?: -1
////                                    Log.d(
////                                        "SaboAIScreen",
////                                        "Sending message with userId: $userId, sessionId: $sessionId"
////                                    )
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = sessionId,
////                                        message = card.prompt
////                                    )
////                                }
////                            } else {
////                                // No active profile - send message without saving
////                                Log.d(
////                                    "SaboAIScreen",
////                                    "No active profile - sending feature card message without saving"
////                                )
////                                viewModel.sendMessage(
////                                    userId = userId,
////                                    sessionId = -1,
////                                    message = card.prompt
////                                )
////                            }
////                            showChat = true
////                            viewingHistoricalChat = false
////                        }
////
////
////                        onFeatureCardClick = { card ->
////                            val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                            if (profileId != -1) {
////                                val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
////
////                                if (savedSessionId == -1) {
////                                    Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
////                                    viewModel.createNewMessage(
////                                        userId = userId,
////                                        profileId = profileId,
////                                        message = card.prompt
////                                    )
////                                    isNewChat = false
////                                } else {
////                                    Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
////                                    viewModel.sendMessage(
////                                        userId = userId,
////                                        sessionId = savedSessionId,
////                                        message = card.prompt
////                                    )
////                                }
////                            } else {
////                                Log.d("SaboAIScreen", "No active profile - sending feature card message without saving")
////                                viewModel.sendMessage(
////                                    userId = userId,
////                                    sessionId = -1,
////                                    message = card.prompt
////                                )
////                            }
////                            showChat = true
////                            viewingHistoricalChat = false
////                        }
//
//
//                        // 4. Update onFeatureCardClick similarly
//                        onFeatureCardClick = { card ->
//                            val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                            if (profileId != -1) {
//                                val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
//
//                                if (savedSessionId == -1) {
//                                    Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
//                                    viewModel.createNewMessage(
//                                        userId = userId,
//                                        profileId = profileId,
//                                        message = card.prompt
//                                    )
//                                    isNewChat = false
//                                } else {
//                                    Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
//                                    viewModel.sendMessage(
//                                        userId = userId,
//                                        sessionId = savedSessionId,
//                                        message = card.prompt
//                                    )
//                                }
//                            } else {
//                                Log.d("SaboAIScreen", "No active profile - sending feature card message without saving")
//                                viewModel.sendMessage(
//                                    userId = userId,
//                                    sessionId = -1,
//                                    message = card.prompt
//                                )
//                            }
//                            showChat = true
//                            viewingHistoricalChat = false
//                        }
//
//                    )
//                }
//            }
//        }
//
//        // Side Menu Overlay
//        if (isMenuOpen) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable { isMenuOpen = false }
//            )
//
//            SideMenu(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(280.dp)
//                    .background(Color.White),
//                onClose = { isMenuOpen = false },
////                onAnalyzeProfileClick = { message ->
////                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
////
////                    if (profileId != -1) {
////                        // Active profile exists
////                        if (isNewChat || currentSessionId == null || currentSessionId == -1) {
////                            // Create new chat session
////                            Log.d(
////                                "SaboAIScreen",
////                                "Creating new message with userId: $userId, profileId: $profileId"
////                            )
////                            viewModel.createNewMessage(
////                                userId = userId,
////                                profileId = profileId,
////                                message = message
////                            )
////                            isNewChat = false
////                        } else {
////                            // Continue existing chat
////                            val sessionId = currentSessionId ?: -1
////                            Log.d(
////                                "SaboAIScreen",
////                                "Sending message with userId: $userId, sessionId: $sessionId"
////                            )
////                            viewModel.sendMessage(
////                                userId = userId,
////                                sessionId = sessionId,
////                                message = message
////                            )
////                        }
////                    } else {
////                        // No active profile
////                        Log.d("SaboAIScreen", "No active profile - cannot analyze profile")
////                    }
////
////                    showChat = true
////                    viewingHistoricalChat = false
////                    isMenuOpen = false
////                },
////
//
//                onAnalyzeProfileClick = { message ->
//                    val profileId = sharedPreferences.getInt("chatProfileId", -1)
//
//                    if (profileId != -1) {
//                        val savedSessionId = sharedPreferences.getInt("chatSessionId", -1)
//
//                        if (savedSessionId == -1) {
//                            Log.d("SaboAIScreen", "Creating new message with userId: $userId, profileId: $profileId")
//                            viewModel.createNewMessage(
//                                userId = userId,
//                                profileId = profileId,
//                                message = message
//                            )
//                            isNewChat = false
//                        } else {
//                            Log.d("SaboAIScreen", "Sending message with userId: $userId, sessionId: $savedSessionId")
//                            viewModel.sendMessage(
//                                userId = userId,
//                                sessionId = savedSessionId,
//                                message = message
//                            )
//                        }
//                    } else {
//                        Log.d("SaboAIScreen", "No active profile - cannot analyze profile")
//                    }
//
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isMenuOpen = false
//                },
//
//
////                onStartChatting = {
////                    isMenuOpen = false
////                    showChat = true
////                    viewingHistoricalChat = false
////                    isNewChat = true
////                    currentSessionId = null
////                    sharedPreferences.edit().remove("chatSessionId").apply()
////                    chatViewModel.clearCurrentSession()
////                    viewModel.clearMessages()
////                },
//
//                onStartChatting = {
//                    isMenuOpen = false
//                    showChat = true
//                    viewingHistoricalChat = false
//                    isNewChat = true
//                    currentSessionId = null
//                    sharedPreferences.edit().remove("chatSessionId").apply()
//                    chatViewModel.clearCurrentSession()
//                    viewModel.clearMessages()
//                },
//
//                chatUserProfiles = chatUserProfiles,
//                isLoadingProfiles = profileLoading,
//                profileError = profileError,
//                onRefreshProfiles = {
//                    userProfile?.let {
//                        chatViewModel.fetchChatUserProfiles(it.id)
//                    }
//                },
//                chatViewModel = chatViewModel,
//                userId = userId,
//                chatHistory = chatHistory,
//                historyLoading = historyLoading,
//                historyError = historyError,
//                onChatHistoryClick = { session ->
//                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
//
//                    sharedPreferences.edit()
//                        .putInt("chatSessionId", session.sessionId)
//                        .apply()
//
//                    currentSessionId = session.sessionId
//                    chatViewModel.fetchChatMessages(session.sessionId)
//
//                    viewingHistoricalChat = true
//                    isNewChat = false  // Important: We're viewing history, not creating new
//                    isMenuOpen = false
//                    showChat = true
//                },
//                onRefreshHistory = {
//                    val activeProfile = chatUserProfiles.find { it.isActive }
//                    Log.d(
//                        "SideMenu",
//                        "Refreshing history for active profile: ${activeProfile?.username}"
//                    )
//
//                    if (activeProfile != null) {
//                        val profileId = activeProfile.id
//                        chatViewModel.fetchChatHistory(
//                            userId = userId,
//                            platform = activeProfile.platform,
//                            profileId = profileId
//                        )
//                        sharedPreferences.edit()
//                            .putInt("chatProfileId", profileId)
//                            .apply()
//                    } else {
//                        chatViewModel.fetchChatHistory(userId = userId)
//                    }
//                },
//                onSessionChanged = { newSessionId ->
//                    currentSessionId = newSessionId
//                    if (newSessionId != null) {
//                        sharedPreferences.edit()
//                            .putInt("chatSessionId", newSessionId)
//                            .apply()
//                        isNewChat = false  // We have a session now
//                    } else {
//                        sharedPreferences.edit().remove("chatSessionId").apply()
//                        isNewChat = true  // No session means new chat
//                    }
//                },
//                onPinChat = {},
//                onDeleteChat = {}
//            )
//        }
//    }
//}
//
*/
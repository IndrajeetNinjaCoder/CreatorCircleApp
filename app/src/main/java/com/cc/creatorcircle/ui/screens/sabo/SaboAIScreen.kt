package com.cc.creatorcircle.ui.screens.sabo

import android.R.attr.onClick
import android.content.Context
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.window.Dialog
import com.cc.creatorcircle.data.socket.ChatMessage
import com.cc.creatorcircle.ui.components.TopBarSabo
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.data.socket.ConnectionState
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material3.AlertDialog

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

    // Chat messages states
    val chatMessages by chatViewModel.messages.collectAsState()
    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
    val messagesError by chatViewModel.messagesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var viewingHistoricalChat by remember { mutableStateOf(false) }
    var isNewChat by remember { mutableStateOf(false) }
    var shouldRefreshHistory by remember { mutableStateOf(false) }

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
//    LaunchedEffect(Unit) {
//        postViewModel.fetchUserProfile()
//
//        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
//        if (sessionId == -1) {
//            isNewChat = true
//        }
//
//
//
//        // Clear sessionId from SharedPreferences
//        val sharedPreferences =
//            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//        sharedPreferences.edit().remove("chatSessionId").apply()
//
//        // Clear messages
//        chatViewModel.clearCurrentSession()
//
//        // NOTE: We're NOT calling createNewChatSession here anymore
//        // Just trigger the callback to open chat screen in "new chat" mode
//
//    }

    LaunchedEffect(Unit) {
        postViewModel.fetchUserProfile()

        val sessionId = sharedPreferences.getInt("chatSessionId", -1)
        if (sessionId == -1) {
            isNewChat = true
        } else {
            // Load the existing chat session
            currentSessionId = sessionId
            chatViewModel.fetchChatMessages(sessionId)
            viewingHistoricalChat = true
            showChat = true
        }
    }



    LaunchedEffect(userProfile) {
        userProfile?.let {
            chatViewModel.fetchChatUserProfiles(it.id)
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

            Log.d(
                "SaboAIScreen",
                "Session ID saved. isNewChat set to false. Future messages will use sessionId: $sessionId"
            )

            // Reconnect with new session ID
            viewModel.connect(userId, sessionId)

            // Clear the session ID from the flow
            viewModel.clearNewSessionId()

            // Mark that we need to refresh history after first response
            shouldRefreshHistory = true
        }
    }


    // NEW: Monitor socket messages to detect when AI completes first response
    LaunchedEffect(socketIOMessages.size, shouldRefreshHistory) {
        if (shouldRefreshHistory && socketIOMessages.size >= 2) {
            // Wait for both user message and AI response
            val hasAIResponse = socketIOMessages.any { it.isAssistant }

            if (hasAIResponse) {
                Log.d("SaboAIScreen", "First AI response received, refreshing chat history")

                val activeProfile = chatUserProfiles.find { it.isActive }
                if (activeProfile != null) {
                    chatViewModel.fetchChatHistory(
                        userId = userId,
                        platform = activeProfile.platform,
                        profileId = activeProfile.id
                    )
                }

                // Reset the flag
                shouldRefreshHistory = false
            }
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
//                    Log.d(
//                        "SaboAIScreen",
//                        "New chat mode activated for profile: ${activeProfile.username}"
//                    )
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

                // This fetches chat history for ONLY the active profile
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )

                val sessionId = sharedPreferences.getInt("chatSessionId", -1)
                if (sessionId == -1) {
                    isNewChat = true
                    Log.d(
                        "SaboAIScreen",
                        "New chat mode activated for profile: ${activeProfile.username}"
                    )
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
//                val shouldShowChatInterface = showChat &&
//                        (socketIOMessages.isNotEmpty() ||
//                                (viewingHistoricalChat && chatMessages.isNotEmpty()) ||
//                                isNewChat) // Keep chat interface open even in new chat mode

                val shouldShowChatInterface = showChat

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
                            shouldRefreshHistory = false
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

//            SideMenu(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .width(280.dp)
//                    .background(Color.White),
//                onClose = { isMenuOpen = false },
//                onAnalyzeProfileClick = { message ->
//                    val sessionId = currentSessionId
//
//                    if (sessionId != null && sessionId != -1 && !isNewChat) {
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
//                    shouldRefreshHistory = false
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
//
//
//                onChatHistoryClick = { session ->
//                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
//
//                    sharedPreferences.edit()
//                        .putInt("chatSessionId", session.sessionId)
//                        .apply()
//
//                    currentSessionId = session.sessionId
//
//                    // Clear socket messages when viewing a different historical chat
//                    viewModel.clearMessages()
//
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
//

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
                    shouldRefreshHistory = false
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

                    // Clear socket messages when viewing a different historical chat
                    viewModel.clearMessages()

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
                },
                // NEW: Add delete chat session handler
//                onDeleteChatSession = { sessionId ->
//                    Log.d("SaboAIScreen", "Deleting chat session: $sessionId")
//
//                    // Delete the session
//                    chatViewModel.deleteChatSession(sessionId, userId)
//
//                    // If we're viewing this session, clear it
//                    if (currentSessionId == sessionId) {
//                        showChat = false
//                        viewingHistoricalChat = false
//                        isNewChat = false
//                        currentSessionId = null
//                        sharedPreferences.edit().remove("chatSessionId").apply()
//                        chatViewModel.clearCurrentSession()
//                        viewModel.clearMessages()
//                    }
//                }


                // NEW: Add delete chat session handler
                onDeleteChatSession = { sessionId ->
                    Log.d("SaboAIScreen", "Deleting chat session: $sessionId")

                    // Get the active profile ID from SharedPreferences
                    val activeProfileId = sharedPreferences.getInt("chatProfileId", -1)

                    // Delete the session with the active profile ID
                    if (activeProfileId != -1) {
                        chatViewModel.deleteChatSession(sessionId, userId)

                        // If we're viewing this session, clear it
                        if (currentSessionId == sessionId) {
                            showChat = false
                            viewingHistoricalChat = false
                            isNewChat = false
                            currentSessionId = null
                            sharedPreferences.edit().remove("chatSessionId").apply()
                            chatViewModel.clearCurrentSession()
                            viewModel.clearMessages()
                        }
                    } else {
                        Log.e("SaboAIScreen", "No active profile found, cannot delete chat")
                    }
                }

            )




        }
    }
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
    onSessionChanged: (Int?) -> Unit = {},
    onDeleteChatSession: (Int) -> Unit = {}  // NEW: Add this parameter
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

    // Observe delete chat session states
    val deleteChatSessionLoading by chatViewModel.deleteChatSessionLoading.collectAsState()
    val deleteChatSessionError by chatViewModel.deleteChatSessionError.collectAsState()
    val chatSessionDeleted by chatViewModel.chatSessionDeleted.collectAsState()

// Handle successful chat session deletion
//    LaunchedEffect(chatSessionDeleted) {
//        chatSessionDeleted?.let {
//            Log.d("SideMenu", "Chat session deleted successfully: ${it.message}")
//
//            // Refresh chat history to reflect the deletion
//            onRefreshHistory()
//
//            // Clear the delete state
//            chatViewModel.clearDeleteChatSessionState()
//        }
//    }

    // Handle successful chat session deletion
    LaunchedEffect(chatSessionDeleted) {
        chatSessionDeleted?.let {
            Log.d("SideMenu", "Chat session deleted successfully: ${it.message}")

            // Refresh chat history for the active profile only
            val activeProfile = chatUserProfiles.find { profile -> profile.isActive }
            if (activeProfile != null) {
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )
            } else {
                // Fallback to general refresh if no active profile
                onRefreshHistory()
            }

            // Clear the delete state
            chatViewModel.clearDeleteChatSessionState()
        }
    }

// Show delete error if any
    deleteChatSessionError?.let { error ->
        // You can show a Snackbar or Toast here
        Log.e("SideMenu", "Failed to delete chat session: $error")
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
                            .fillMaxSize()
                            .weight(1f),
//                            .fillMaxWidth()
//                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
//                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
//                            ChatHistoryItem(
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
//                                }
//                            )
//                        }
//

//                        // Inside the LazyColumn for chat history in SideMenu
//                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
//                            ChatHistoryItem(
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
//                                onDeleteClick = { sessionId ->
//                                    Log.d(
//                                        "SideMenu",
//                                        "Delete chat session clicked - sessionId: $sessionId"
//                                    )
//                                    chatViewModel.deleteChatSession(sessionId, userId)
//                                },
//                                onPinClick = { sessionId ->
//                                    Log.d(
//                                        "SideMenu",
//                                        "Pin chat session clicked - sessionId: $sessionId"
//                                    )
//                                    // TODO: Implement pin functionality
//                                    // For now, just log the action
//                                }
//                            )
//                        }


                        // Inside the LazyColumn for chat history in SideMenu
//                        items(chatHistory.sortedByDescending { it.sessionId }) { session ->
//                            ChatHistoryItem(
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
//                                onDeleteClick = { sessionId ->
//                                    Log.d(
//                                        "SideMenu",
//                                        "Delete chat session clicked - sessionId: $sessionId"
//                                    )
//                                    // Use the callback from SaboAIScreen to handle deletion
//                                    onDeleteChatSession(sessionId)
//                                },
//                                onPinClick = { sessionId ->
//                                    Log.d(
//                                        "SideMenu",
//                                        "Pin chat session clicked - sessionId: $sessionId"
//                                    )
//                                    // TODO: Implement pin functionality
//                                    // For now, just log the action
//                                }
//                            )
//                        }


                        // Inside the LazyColumn for chat history in SideMenu
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
                                },
                                onDeleteClick = {
                                    Log.d(
                                        "SideMenu",
                                        "Delete chat session clicked - sessionId: ${session.sessionId}"
                                    )
                                    // Use the callback from SaboAIScreen to handle deletion
                                    onDeleteChatSession(session.sessionId)
                                },
                                onPinClick = {
                                    Log.d(
                                        "SideMenu",
                                        "Pin chat session clicked - sessionId: ${session.sessionId}"
                                    )
                                    // TODO: Implement pin functionality
                                    // For now, just log the action
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


// Updated ChatInterface.kt
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

    // Track if AI is generating response
    val isGeneratingResponse = remember(socketIOMessages) {
        // Check if last message is from user and no assistant response yet
        // OR if the last assistant message is still being generated (incomplete)
        val lastMessage = socketIOMessages.lastOrNull()
        val lastUserMessage = socketIOMessages.lastOrNull { !it.isAssistant }
        val lastAssistantMessage = socketIOMessages.lastOrNull { it.isAssistant }

        // AI is generating if:
        // 1. Last message is from user and there's no assistant response after it
        // 2. Or if we're receiving chunks (you can track this via a flag in your ViewModel)
        lastMessage?.isAssistant == false ||
                (lastUserMessage != null && lastAssistantMessage != null &&
                        lastUserMessage.timestamp > lastAssistantMessage.timestamp)
    }

    // Track the last assistant message to detect when response is complete
    val lastAssistantMessage = remember(socketIOMessages) {
        socketIOMessages.lastOrNull { it.isAssistant }
    }

    // Auto-scroll when new messages arrive or response completes
    LaunchedEffect(socketIOMessages.size, lastAssistantMessage?.content) {
        if (socketIOMessages.isNotEmpty()) {
            val totalMessages = if (isViewingHistory) {
                historicalMessages.size + socketIOMessages.size
            } else {
                socketIOMessages.size
            }

            if (totalMessages > 0) {
                coroutineScope.launch {
                    listState.animateScrollToItem(totalMessages - 1)
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

                isViewingHistory && historicalMessages.isNotEmpty() -> {
                    items(
                        items = historicalMessages,
                        key = { message -> "historical_${message.id}" }
                    ) { message ->
                        HistoricalMessageItem(message = message)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (socketIOMessages.isNotEmpty()) {
                        items(
                            items = socketIOMessages,
                            key = { message -> "socket_${message.id}" }
                        ) { message ->
                            SocketIOMessageItem(message = message)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                socketIOMessages.isNotEmpty() -> {
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
            isGenerating = isGeneratingResponse,
            placeholder = if (connectionState == ConnectionState.CONNECTED) {
                "Type your message to get started..."
            } else {
                "Connecting to chat..."
            }
        )
    }
}


// Updated ChatInputField.kt
@Composable
fun ChatInputField(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean = true,
    isGenerating: Boolean = false,
    placeholder: String = "Type your message to get started...",
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .imePadding(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (isFocused) 1.dp else 2.dp,
                            brush = if (isFocused) {
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Blue, Color.Blue)
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF893BCF),
                                        Color(0xFFEA3BA1)
                                    )
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
                        if (searchQuery.isEmpty() && !isGenerating) {
                            Text(
                                text = placeholder,
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }

                        if (isGenerating && searchQuery.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF7C4DFF)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI is thinking...",
                                    fontSize = 14.sp,
                                    color = Color(0xFF7C4DFF),
                                    fontStyle = FontStyle.Italic
                                )
                            }
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
                            enabled = isEnabled && !isGenerating,
                            cursorBrush = SolidColor(Color(0xFF7C4DFF))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onSendClick,
                    enabled = searchQuery.isNotEmpty() && isEnabled && !isGenerating,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = if (searchQuery.isNotEmpty() && isEnabled && !isGenerating) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF893BCF),
                                        Color(0xFFEA3BA1)
                                    )
                                )
                            } else {
                                SolidColor(Color(0xFFE0E0E0))
                            },
                            shape = CircleShape
                        )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
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
//    val coroutineScope = rememberCoroutineScope()
//
//    // Track the last assistant message to detect when response is complete
//    val lastAssistantMessage = remember(socketIOMessages) {
//        socketIOMessages.lastOrNull { it.isAssistant }
//    }
//
//    // Auto-scroll when new messages arrive or response completes
//    LaunchedEffect(socketIOMessages.size, lastAssistantMessage?.content) {
//        if (socketIOMessages.isNotEmpty()) {
//            val totalMessages = if (isViewingHistory) {
//                historicalMessages.size + socketIOMessages.size
//            } else {
//                socketIOMessages.size
//            }
//
//            if (totalMessages > 0) {
//                coroutineScope.launch {
//                    // Smooth scroll to the last item
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .imePadding()
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .padding(horizontal = 4.dp)
//                .weight(1f)
//                .fillMaxWidth(),
//            state = listState
//        ) {
//            when {
//                messagesLoading && historicalMessages.isEmpty() -> {
//                    item(key = "loading_indicator") {
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
//                    item(key = "error_message") {
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
//                isViewingHistory && historicalMessages.isNotEmpty() -> {
//                    // Show historical messages first
//                    items(
//                        items = historicalMessages,
//                        key = { message -> "historical_${message.id}" }
//                    ) { message ->
//                        HistoricalMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // Show socket messages when they exist, even in historical view
//                    if (socketIOMessages.isNotEmpty()) {
//                        items(
//                            items = socketIOMessages,
//                            key = { message -> "socket_${message.id}" }
//                        ) { message ->
//                            SocketIOMessageItem(message = message)
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
//                    }
//                }
//
//                socketIOMessages.isNotEmpty() -> {
//                    items(
//                        items = socketIOMessages,
//                        key = { message -> "socket_${message.id}" }
//                    ) { message ->
//                        SocketIOMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                else -> {
//                    item(key = "start_conversation") {
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


// UPDATE YOUR SocketIOMessageItem COMPOSABLE LIKE THIS:
//@Composable
//fun SocketIOMessageItem(message: ChatMessage) {
//    // Filter out "Generating response..." placeholder text
//    val shouldHideMessage = message.content.trim().equals("Generating response...", ignoreCase = true) ||
//            message.content.trim().equals("Generating response.", ignoreCase = true) ||
//            message.content.isBlank()
//
//    if (shouldHideMessage) {
//        // Don't render anything for placeholder messages
//        return
//    }
//
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = if (message.isAssistant) {
//                Arrangement.Start
//            } else {
//                Arrangement.End
//            }
//        ) {
//            Card(
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .widthIn(max = 320.dp),
//                backgroundColor = if (message.isAssistant) {
//                    Color(0xFF7C4DFF)
//                } else {
//                    Color(0xFFE9ECEF)
//                },
//                elevation = 0.dp,
//                shape = RoundedCornerShape(
//                    topStart = if (message.isAssistant) 4.dp else 18.dp,
//                    topEnd = if (message.isAssistant) 18.dp else 4.dp,
//                    bottomStart = 18.dp,
//                    bottomEnd = 18.dp
//                )
//            ) {
//                FormattedMessageContent(
//                    content = message.content,
//                    isAssistant = message.isAssistant
//                )
//            }
//        }
//    }
//}


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
//    val coroutineScope = rememberCoroutineScope()
//
//    // Track the last assistant message to detect when response is complete
//    val lastAssistantMessage = remember(socketIOMessages) {
//        socketIOMessages.lastOrNull { it.isAssistant }
//    }
//
//    // Auto-scroll when new messages arrive or response completes
//    LaunchedEffect(socketIOMessages.size, lastAssistantMessage?.content) {
//        if (socketIOMessages.isNotEmpty()) {
//            val totalMessages = if (isViewingHistory) {
//                historicalMessages.size + socketIOMessages.size
//            } else {
//                socketIOMessages.size
//            }
//
//            if (totalMessages > 0) {
//                coroutineScope.launch {
//                    // Smooth scroll to the last item
//                    listState.animateScrollToItem(totalMessages - 1)
//                }
//            }
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .imePadding()
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .padding(horizontal = 4.dp)
//                .weight(1f)
//                .fillMaxWidth(),
//            state = listState
//        ) {
//            when {
//                messagesLoading && historicalMessages.isEmpty() -> {
//                    item(key = "loading_indicator") {
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
//                    item(key = "error_message") {
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
//                isViewingHistory && historicalMessages.isNotEmpty() -> {
//                    // Show historical messages first
//                    items(
//                        items = historicalMessages,
//                        key = { message -> "historical_${message.id}" }
//                    ) { message ->
//                        HistoricalMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // FIXED: Always show socket messages when they exist, even in historical view
//                    if (socketIOMessages.isNotEmpty()) {
//                        items(
//                            items = socketIOMessages,
//                            key = { message -> "socket_${message.id}" }
//                        ) { message ->
//                            SocketIOMessageItem(message = message)
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
//                    }
//                }
//
//                socketIOMessages.isNotEmpty() -> {
//                    items(
//                        items = socketIOMessages,
//                        key = { message -> "socket_${message.id}" }
//                    ) { message ->
//                        SocketIOMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                else -> {
//                    item(key = "start_conversation") {
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
//            .imePadding()
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .padding(horizontal = 4.dp)
//                .weight(1f)
//                .fillMaxWidth(),
//            state = listState
//        ) {
//            when {
//                messagesLoading && historicalMessages.isEmpty() -> {
//                    item(key = "loading_indicator") {
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
//                    item(key = "error_message") {
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
//                isViewingHistory && historicalMessages.isNotEmpty() -> {
//                    // Show historical messages first
//                    items(
//                        items = historicalMessages,
//                        key = { message -> "historical_${message.id}" }
//                    ) { message ->
//                        HistoricalMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // FIXED: Always show socket messages when they exist, even in historical view
//                    if (socketIOMessages.isNotEmpty()) {
//                        items(
//                            items = socketIOMessages,
//                            key = { message -> "socket_${message.id}" }
//                        ) { message ->
//                            SocketIOMessageItem(message = message)
//                            Spacer(modifier = Modifier.height(16.dp))
//                        }
//                    }
//                }
//
//                socketIOMessages.isNotEmpty() -> {
//                    items(
//                        items = socketIOMessages,
//                        key = { message -> "socket_${message.id}" }
//                    ) { message ->
//                        SocketIOMessageItem(message = message)
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//                }
//
//                else -> {
//                    item(key = "start_conversation") {
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
    onClick: () -> Unit,
    onDeleteClick: () -> Unit = {},
    onPinClick: () -> Unit = {}
) {
    var showDropdown by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main clickable area for opening chat
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
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
        }

        // Three dots menu button
        Box {
            IconButton(
                onClick = { showDropdown = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Dropdown menu
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier.background(Color.White)
            ) {
                // Pin chat option
                DropdownMenuItem(
                    onClick = {
                        showDropdown = false
                        onPinClick()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Pin chat",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }

                // Delete option
                DropdownMenuItem(
                    onClick = {
                        showDropdown = false
                        showDeleteDialog = true
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Delete",
                            fontSize = 14.sp,
                            color = Color.Red
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Chat",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this chat? This action cannot be undone.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .width(120.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White
        )
    }
}


//@Composable
//fun ChatHistoryItem(
//    session: com.cc.creatorcircle.data.models.ChatSession,
//    onClick: () -> Unit,
//    onDeleteClick: () -> Unit = {},
//    onPinClick: () -> Unit = {}
//) {
//    var showDropdown by remember { mutableStateOf(false) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                Color.White,
//                RoundedCornerShape(8.dp)
//            )
//            .padding(12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Main clickable area for opening chat
//        Row(
//            modifier = Modifier
//                .weight(1f)
//                .clickable { onClick() },
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Platform icon
//            Image(
//                painter = painterResource(id = R.drawable.ic_robot_filled),
//                contentDescription = session.platform,
//                modifier = Modifier.size(24.dp)
//            )
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            // Chat details
//            Column(
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = session.title,
//                    fontSize = 14.sp,
//                    color = Color.Black,
//                    fontWeight = FontWeight.Medium,
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//
//                Spacer(modifier = Modifier.height(2.dp))
//
//                Text(
//                    text = formatChatDate(session.updatedAt),
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//            }
//        }
//
//        // Three dots menu button
//        Box {
//            IconButton(
//                onClick = { showDropdown = true },
//                modifier = Modifier.size(32.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.MoreVert,
//                    contentDescription = "More options",
//                    tint = Color.Gray,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            // Dropdown menu
//            DropdownMenu(
//                expanded = showDropdown,
//                onDismissRequest = { showDropdown = false },
//                modifier = Modifier.background(Color.White)
//            ) {
//                // Pin chat option
//                DropdownMenuItem(
//                    onClick = {
//                        showDropdown = false
//                        onPinClick()
//                    }
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.PushPin,
//                            contentDescription = "Pin",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(18.dp)
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            text = "Pin chat",
//                            fontSize = 14.sp,
//                            color = Color.Black
//                        )
//                    }
//                }
//
//                // Delete option
//                DropdownMenuItem(
//                    onClick = {
//                        showDropdown = false
//                        onDeleteClick()
//                    }
//                ) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Delete,
//                            contentDescription = "Delete",
//                            tint = Color.Red,
//                            modifier = Modifier.size(18.dp)
//                        )
//                        Spacer(modifier = Modifier.width(12.dp))
//                        Text(
//                            text = "Delete",
//                            fontSize = 14.sp,
//                            color = Color.Red
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

//@Composable
//fun ChatHistoryItem(
//    session: com.cc.creatorcircle.data.models.ChatSession,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .background(
//                Color.White,
//                RoundedCornerShape(8.dp)
//            )
//            .padding(12.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Platform icon
//
//        Image(
//            painter = painterResource(id = R.drawable.ic_robot_filled),
//            contentDescription = session.platform,
//            modifier = Modifier.size(24.dp)
//        )
//
//
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        // Chat details
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = session.title,
//                fontSize = 14.sp,
//                color = Color.Black,
//                fontWeight = FontWeight.Medium,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//
//            Spacer(modifier = Modifier.height(2.dp))
//
//            Text(
//                text = formatChatDate(session.updatedAt),
//                fontSize = 12.sp,
//                color = Color.Gray
//            )
//        }
//    }
//}
//


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


//@Composable
//fun SocketIOMessageItem(message: ChatMessage) {
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = if (message.isAssistant) {
//                Arrangement.Start
//            } else {
//                Arrangement.End
//            }
//        ) {
//            Card(
//                modifier = Modifier
//                    .wrapContentWidth()
//                    .widthIn(max = 320.dp),
//                backgroundColor = if (message.isAssistant) {
//                    Color(0xFF7C4DFF)
//                } else {
//                    Color(0xFFE9ECEF)
//                },
//                elevation = 0.dp,
//                shape = RoundedCornerShape(
//                    topStart = if (message.isAssistant) 4.dp else 18.dp,
//                    topEnd = if (message.isAssistant) 18.dp else 4.dp,
//                    bottomStart = 18.dp,
//                    bottomEnd = 18.dp
//                )
//            ) {
//                FormattedMessageContent(
//                    content = message.content,
//                    isAssistant = message.isAssistant
//                )
//            }
//        }
//    }
//}


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












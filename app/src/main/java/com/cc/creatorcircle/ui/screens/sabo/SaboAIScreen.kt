package com.cc.creatorcircle.ui.screens.sabo

import android.util.Log
import androidx.compose.foundation.background
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
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.websocket.StreamingMessage
import com.cc.creatorcircle.data.websocket.WebSocketManager
import com.cc.creatorcircle.viewModel.websocket.ChatViewModel
import kotlinx.coroutines.launch


import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.cc.creatorcircle.ui.components.TopBarSabo
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.data.models.ChatUserProfile

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

    // Get context and create viewModels
    val context = LocalContext.current
    val postViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Create ChatViewModel instance for profile fetching
    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
        com.cc.creatorcircle.viewModel.ChatViewModel(context)
    }

    // User profile states
    val userProfile by postViewModel.userProfile.collectAsState()

    // Updated to use multiple profiles
    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
    val profileLoading by chatViewModel.profileLoading.collectAsState()
    val profileError by chatViewModel.profileError.collectAsState()

    // Chat history states
    val chatHistory by chatViewModel.chatHistory.collectAsState()
    val historyLoading by chatViewModel.historyLoading.collectAsState()
    val historyError by chatViewModel.historyError.collectAsState()

    // NEW: Chat messages states
    val chatMessages by chatViewModel.messages.collectAsState()
    val messagesLoading by chatViewModel.messagesLoading.collectAsState()
    val messagesError by chatViewModel.messagesError.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }

    // NEW: State to track if we're viewing historical messages
    var viewingHistoricalChat by remember { mutableStateOf(false) }
    var currentSessionId by remember { mutableStateOf<Int?>(null) }

    val userId = userProfile?.id ?: 233
    val websocketUrl = "wss://creatorcircle.in/api/chat/ws/"

    val connectionState by viewModel.connectionState.collectAsState()
    val streamingMessages by viewModel.streamingMessages.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Fetch user profile and chat profiles when screen loads
    LaunchedEffect(Unit) {
        postViewModel.fetchUserProfile()
    }

    // Fetch chat user profiles once we have the main user profile
    LaunchedEffect(userProfile) {
        userProfile?.let {
            chatViewModel.fetchChatUserProfiles(it.id)
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
                chatViewModel.fetchChatHistory(
                    userId = userId,
                    platform = activeProfile.platform,
                    profileId = activeProfile.id
                )
            } else {
                Log.w("SaboAIScreen", "No active profile found, fetching general history")
                chatViewModel.fetchChatHistory(userId = userId)
            }
        }
    }

    // Auto-connect when screen loads
    LaunchedEffect(Unit) {
        if (connectionState == WebSocketManager.ConnectionState.DISCONNECTED) {
            viewModel.connect(websocketUrl)
        }
    }

    // Auto-reconnect on error - silently handle connection errors
    LaunchedEffect(connectionState) {
        when (connectionState) {
            WebSocketManager.ConnectionState.ERROR -> {
                kotlinx.coroutines.delay(2000)
                viewModel.connect(websocketUrl)
            }

            WebSocketManager.ConnectionState.DISCONNECTED -> {
                viewModel.connect(websocketUrl)
            }

            else -> {}
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(streamingMessages.size, chatMessages.size) {
        if ((streamingMessages.isNotEmpty() || chatMessages.isNotEmpty()) && showChat) {
            coroutineScope.launch {
                val totalMessages = if (viewingHistoricalChat) chatMessages.size else streamingMessages.size
                if (totalMessages > 0) {
                    listState.animateScrollToItem(totalMessages - 1)
                }
            }
        }
    }

    // Show chat when there are messages
    LaunchedEffect(streamingMessages.size, chatMessages.size) {
        if (streamingMessages.isNotEmpty() || (viewingHistoricalChat && chatMessages.isNotEmpty())) {
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
                    onClick = { isMenuOpen = true }
                )
            },
            bottomBar = { BottomNavBar(navController = navController) },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (showChat) {
                    // Chat Interface - UPDATED to handle both streaming and historical messages
                    ChatInterface(
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSendClick = {
                            if (searchQuery.isNotBlank()) {
                                // If viewing historical chat, switch to new chat
                                if (viewingHistoricalChat) {
                                    viewingHistoricalChat = false
                                    currentSessionId = null
                                    chatViewModel.clearCurrentSession()
                                }
                                viewModel.sendMessage(userId, searchQuery)
                                searchQuery = ""
                            }
                        },
                        connectionState = connectionState,
                        streamingMessages = streamingMessages,
                        historicalMessages = chatMessages,
                        isViewingHistory = viewingHistoricalChat,
                        messagesLoading = messagesLoading,
                        messagesError = messagesError,
                        listState = listState,
                        onBackClick = {
                            showChat = false
                            viewingHistoricalChat = false
                            currentSessionId = null
                            chatViewModel.clearCurrentSession()
                        }
                    )
                } else {
                    // Feature Cards Interface
                    FeatureCardsInterface(
                        featureCards = featureCards,
                        searchQuery = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSendClick = {
                            if (searchQuery.isNotEmpty()) {
                                viewModel.sendMessage(userId, searchQuery)
                                searchQuery = ""
                                showChat = true
                                viewingHistoricalChat = false
                            }
                        },
                        onFeatureCardClick = { card ->
                            viewModel.sendMessage(userId, card.prompt)
                            showChat = true
                            viewingHistoricalChat = false
                        },
                        connectionState = connectionState
                    )
                }
            }
        }

        // Side Menu Overlay
        if (isMenuOpen) {
            // Dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { isMenuOpen = false }
            )

            // Side Menu with real profile data and chat history
            SideMenu(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(Color.White),
                onClose = { isMenuOpen = false },
                onStartChatting = {
                    isMenuOpen = false
                    showChat = false
                    viewingHistoricalChat = false
                    currentSessionId = null
                    chatViewModel.clearCurrentSession()
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
                    // NEW: Load historical chat messages
                    Log.d("SaboAIScreen", "Loading chat history for session: ${session.sessionId}")
                    chatViewModel.fetchChatMessages(session.sessionId)
                    currentSessionId = session.sessionId
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
                        chatViewModel.fetchChatHistory(
                            userId = userId,
                            platform = activeProfile.platform,
                            profileId = activeProfile.id
                        )
                    } else {
                        chatViewModel.fetchChatHistory(userId = userId)
                    }
                }
            )
        }
    }
}







@Composable
fun ChatInterface(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    connectionState: WebSocketManager.ConnectionState,
    streamingMessages: Map<String, StreamingMessage>,
    historicalMessages: List<com.cc.creatorcircle.data.models.ChatMessage>, // NEW parameter
    isViewingHistory: Boolean, // NEW parameter
    messagesLoading: Boolean, // NEW parameter
    messagesError: String?, // NEW parameter
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Messages List - UPDATED to show historical or streaming messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            when {
                // Show loading state for historical messages
                isViewingHistory && messagesLoading -> {
                    item {
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

                // Show error state for historical messages
                isViewingHistory && messagesError != null -> {
                    item {
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

                // Show historical messages
                isViewingHistory && historicalMessages.isNotEmpty() -> {
                    items(
                        items = historicalMessages,
                        key = { it.id }
                    ) { message ->
                        HistoricalMessageItem(message = message) // NEW component
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Show streaming messages (existing functionality)
                !isViewingHistory && streamingMessages.isNotEmpty() -> {
                    val messagesList = mutableListOf<StreamingMessage>()

                    val userMessages = streamingMessages.values
                        .filter { it.role == "user" }
                        .sortedBy { it.timestamp }

                    val aiMessages = streamingMessages.values
                        .filter { it.role == "assistant" }
                        .sortedBy {
                            try {
                                if (it.timestamp.matches(Regex("\\d+"))) {
                                    it.timestamp.toLong()
                                } else {
                                    it.timestamp.hashCode().toLong()
                                }
                            } catch (e: Exception) {
                                System.currentTimeMillis()
                            }
                        }

                    val maxPairs = maxOf(userMessages.size, aiMessages.size)
                    for (i in 0 until maxPairs) {
                        if (i < userMessages.size) {
                            messagesList.add(userMessages[i])
                        }
                        if (i < aiMessages.size) {
                            messagesList.add(aiMessages[i])
                        }
                    }

                    items(
                        items = messagesList,
                        key = { it.id }
                    ) { message ->
                        StreamingMessageItem(message = message)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Show empty state
                else -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isViewingHistory) {
                                    "No messages in this chat"
                                } else {
                                    "Start a conversation with Sabo AI..."
                                },
                                style = MaterialTheme.typography.body1,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Message Input - Updated to show appropriate state
        ChatInputField(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            onSendClick = onSendClick,
            isEnabled = if (isViewingHistory) {
                // Allow sending new messages even when viewing history (will switch to new chat)
                true
            } else {
                connectionState == WebSocketManager.ConnectionState.CONNECTED
            },
            placeholder = if (isViewingHistory) {
                "Start a new conversation..."
            } else {
                "Type your message to get started..."
            }
        )
    }
}

// NEW: Component to display historical messages
@Composable
fun HistoricalMessageItem(message: com.cc.creatorcircle.data.models.ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.role == "user") {
                            Color(0xFF6C757D)
                        } else {
                            Color(0xFF7C4DFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (message.role == "user") {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier.wrapContentWidth(),
                    backgroundColor = if (message.role == "user") {
                        Color(0xFFE9ECEF)
                    } else {
                        Color(0xFF7C4DFF)
                    },
                    elevation = 0.dp,
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                ) {
                    Text(
                        text = message.content.replace(". ", ".\n"),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = if (message.role == "user") {
                            Color(0xFF212529)
                        } else {
                            Color.White
                        }
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
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}


// Updated ChatInputField with placeholder parameter
@Composable
fun ChatInputField(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isEnabled: Boolean = true,
    placeholder: String = "Type your message to get started...", // NEW parameter
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        elevation = 8.dp,
        color = Color.White
    ) {
        Column {
            Divider(
                color = Color(0xFFE0E0E0),
                thickness = 0.5.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color(0xFFF8F9FA),
                    elevation = 0.dp
                ) {
                    Box(
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = placeholder, // Use dynamic placeholder
                                fontSize = 14.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                color = Color(0xFF1A1A1A),
                                fontSize = 14.sp
                            ),
                            maxLines = 3,
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
                        .size(48.dp)
                        .background(
                            color = if (searchQuery.isNotEmpty() && isEnabled) {
                                Color(0xFF7C4DFF)
                            } else {
                                Color(0xFFE0E0E0)
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
fun SideMenu(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onStartChatting: () -> Unit,
    chatUserProfiles: List<ChatUserProfile>,
    isLoadingProfiles: Boolean = false,
    profileError: String? = null,
    onRefreshProfiles: () -> Unit = {},
    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
    userId: Int,
    // Chat history parameters
    chatHistory: List<com.cc.creatorcircle.data.models.ChatSession> = emptyList(),
    historyLoading: Boolean = false,
    historyError: String? = null,
    onChatHistoryClick: (com.cc.creatorcircle.data.models.ChatSession) -> Unit = {},
    onRefreshHistory: () -> Unit = {}
) {
    var showAccountsPopup by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("General") }

    // Observe set profile active states
    val setProfileActiveLoading by chatViewModel.setProfileActiveLoading.collectAsState()
    val setProfileActiveError by chatViewModel.setProfileActiveError.collectAsState()
    val activeProfileUpdated by chatViewModel.activeProfileUpdated.collectAsState()

    // Observe add profile states
    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
    val addProfileError by chatViewModel.addProfileError.collectAsState()
    val profileAdded by chatViewModel.profileAdded.collectAsState()

    // Handle successful profile activation
    LaunchedEffect(activeProfileUpdated) {
        activeProfileUpdated?.let { updatedProfile ->
            Log.d("SideMenu", "Profile ${updatedProfile.username} set as active successfully")
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

        // AI Chat Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStartChatting() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sabo),
                contentDescription = "AI Chat",
                tint = Color(0xFF9C27B0),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "AI Chat",
                fontSize = 16.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider(color = Color(0xFFE0E0E0))
        Spacer(modifier = Modifier.height(16.dp))

        // Social Accounts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    items(instagramProfiles) { profile ->
                        InstagramProfileItem(
                            profile = profile,
                            onProfileClick = { clickedProfile ->
                                if (!clickedProfile.isActive) {
                                    Log.d("SideMenu", "Setting profile ${clickedProfile.username} as active")
                                    chatViewModel.setProfileActive(
                                        profileId = clickedProfile.id.toInt(),
                                        userId = userId
                                    )
                                }
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

        Spacer(modifier = Modifier.height(16.dp))

        // New Chat Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    chatViewModel.createNewChatSession(userId)
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

        // Chat History Header
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        // Tab Row (General, Analysis, Pinned)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("General", "Analysis", "Pinned").forEach { tab ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (selectedTab == tab) Color.White else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = tab }
                ) {
                    Text(
                        text = tab,
                        fontSize = 12.sp,
                        color = if (selectedTab == tab) Color.Black else Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                    )
                }
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

        // Chat History List - SINGLE IMPLEMENTATION
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
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(chatHistory) { session ->
                        ChatHistoryItem(
                            session = session,
                            onClick = { onChatHistoryClick(session) }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Analyze Section (if profiles exist)
        if (instagramProfiles.isNotEmpty()) {
            Text(
                text = "Analyze",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val profileToAnalyze = instagramProfiles.find { it.isActive } ?: instagramProfiles.first()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .clickable { /* Handle analyze action */ }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Analyze",
                    tint = Color(0xFF9C27B0),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Analyze @${profileToAnalyze.username}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Start Chatting Button
        Button(
            onClick = onStartChatting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9C27B0)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Start Chatting",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
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
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (session.platform.lowercase()) {
                "instagram" -> Color(0xFFE91E63)
                "facebook" -> Color(0xFF1877F2)
                "twitter" -> Color(0xFF1DA1F2)
                else -> Color(0xFF9C27B0)
            },
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (session.platform.lowercase()) {
                        "instagram" -> Icons.Default.Camera
                        "facebook" -> Icons.Default.Facebook
                        else -> Icons.Default.Chat
                    },
                    contentDescription = session.platform,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = when (session.platform.lowercase()) {
                "instagram" -> Color(0xFFE91E63).copy(alpha = 0.1f)
                else -> Color(0xFF9C27B0).copy(alpha = 0.1f)
            }
        ) {
            Text(
                text = when (session.platform.lowercase()) {
                    "instagram" -> "Instagram"
                    else -> session.platform.capitalize()
                },
                fontSize = 10.sp,
                color = when (session.platform.lowercase()) {
                    "instagram" -> Color(0xFFE91E63)
                    else -> Color(0xFF9C27B0)
                },
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
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



// Updated InstagramProfileItem component
@Composable
fun InstagramProfileItem(
    profile: InstagramProfile,
    onProfileClick: (InstagramProfile) -> Unit,
    isLoading: Boolean = false
) {
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
        // Instagram icon placeholder
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFE1306C)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isLoading && !profile.isActive) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "📷",
                        fontSize = 12.sp
                    )
                }
            }
        }

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

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "More options",
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
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
fun ChatInterface(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    connectionState: WebSocketManager.ConnectionState,
    streamingMessages: Map<String, StreamingMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (streamingMessages.isEmpty()) {
                item {
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
            } else {
                val messagesList = mutableListOf<StreamingMessage>()

                val userMessages = streamingMessages.values
                    .filter { it.role == "user" }
                    .sortedBy { it.timestamp }

                val aiMessages = streamingMessages.values
                    .filter { it.role == "assistant" }
                    .sortedBy {
                        try {
                            if (it.timestamp.matches(Regex("\\d+"))) {
                                it.timestamp.toLong()
                            } else {
                                it.timestamp.hashCode().toLong()
                            }
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }
                    }

                val maxPairs = maxOf(userMessages.size, aiMessages.size)
                for (i in 0 until maxPairs) {
                    if (i < userMessages.size) {
                        messagesList.add(userMessages[i])
                    }
                    if (i < aiMessages.size) {
                        messagesList.add(aiMessages[i])
                    }
                }

                val sortedMessages = messagesList

                items(
                    items = sortedMessages,
                    key = { it.id }
                ) { message ->
                    StreamingMessageItem(message = message)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Message Input
        ChatInputField(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            onSendClick = onSendClick,
            isEnabled = connectionState == WebSocketManager.ConnectionState.CONNECTED
        )
    }
}

@Composable
fun FeatureCardsInterface(
    featureCards: List<FeatureCard>,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onFeatureCardClick: (FeatureCard) -> Unit,
    connectionState: WebSocketManager.ConnectionState
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

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "What would you like to work on today?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(600.dp),
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

        ChatInputField(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            onSendClick = onSendClick,
            isEnabled = connectionState == WebSocketManager.ConnectionState.CONNECTED,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun StreamingMessageItem(message: StreamingMessage) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.role == "user") {
                            Color(0xFF6C757D)
                        } else {
                            Color(0xFF7C4DFF)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (message.role == "user") {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Card(
                    modifier = Modifier.wrapContentWidth(),
                    backgroundColor = if (message.role == "user") {
                        Color(0xFFE9ECEF)
                    } else {
                        Color(0xFF7C4DFF)
                    },
                    elevation = 0.dp,
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                ) {
                    Text(
                        text = message.content.replace(". ", ".\n"),
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        style = MaterialTheme.typography.body1.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = if (message.role == "user") {
                            Color(0xFF212529)
                        } else {
                            Color.White
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = message.timestamp.split(" ").getOrNull(1)?.substring(0, 5) ?: "",
                    style = MaterialTheme.typography.caption.copy(
                        fontSize = 11.sp
                    ),
                    color = Color(0xFF6C757D),
                    modifier = Modifier.padding(start = 4.dp)
                )
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(card.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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



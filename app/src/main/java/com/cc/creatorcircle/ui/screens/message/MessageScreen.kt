package com.cc.creatorcircle.ui.screens.message

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.ConversationMessage
import com.cc.creatorcircle.utils.TokenManager
import com.cc.creatorcircle.viewModel.MessageViewModel
import kotlinx.coroutines.launch







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    navController: NavController,
    otherUserId: Int,
    userName: String,
    profilePic: String?
) {
    val context = LocalContext.current
    val viewModel = remember { MessageViewModel(context) }
    val tokenManager = remember { TokenManager(context) }

    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<ConversationMessage?>(null) }

    // Observe state flows
    val conversationMessages by viewModel.conversationMessages.collectAsState()
    val conversationLoading by viewModel.conversationLoading.collectAsState()
    val conversationError by viewModel.conversationError.collectAsState()
    val messageSending by viewModel.messageSending.collectAsState()
    val messageSentSuccess by viewModel.messageSentSuccess.collectAsState()
    val messageError by viewModel.messageError.collectAsState()

    // Delete message states
    val messageDeleting by viewModel.messageDeleting.collectAsState()
    val messageDeleteSuccess by viewModel.messageDeleteSuccess.collectAsState()
    val messageDeleteError by viewModel.messageDeleteError.collectAsState()

    // Get current user ID from token
    val currentotherUserId = remember { tokenManager.getUserId()?.toIntOrNull() }

    // Fetch conversation on initial load
    LaunchedEffect(otherUserId) {
        viewModel.fetchConversation(otherUserId)
    }

    // Scroll to bottom when new message is sent successfully
    LaunchedEffect(messageSentSuccess) {
        if (messageSentSuccess) {
            viewModel.fetchConversation(otherUserId)
            viewModel.resetSuccessState()
            coroutineScope.launch {
                if (conversationMessages.isNotEmpty()) {
                    listState.animateScrollToItem(conversationMessages.size - 1)
                }
            }
        }
    }

    // Handle delete success
    LaunchedEffect(messageDeleteSuccess) {
        if (messageDeleteSuccess) {
            showDeleteDialog = false
            messageToDelete = null
            viewModel.resetDeleteSuccessState()
        }
    }

    // Auto scroll when messages are loaded
    LaunchedEffect(conversationMessages.size) {
        if (conversationMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(conversationMessages.size - 1)
            }
        }
    }

    Log.d("MessageScreen", "currentotherUserId: $currentotherUserId")

    // Delete confirmation dialog
    if (showDeleteDialog && messageToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                if (!messageDeleting) {
                    showDeleteDialog = false
                    messageToDelete = null
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFD32F2F)
                )
            },
            title = {
                Text(
                    text = "Delete Message",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Are you sure you want to delete this message?")

                    if (messageDeleteError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = messageDeleteError ?: "Failed to delete message",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        messageToDelete?.let { message ->
                            viewModel.deleteMessage(message.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    enabled = !messageDeleting
                ) {
                    if (messageDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (messageDeleting) "Deleting..." else "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        messageToDelete = null
                        viewModel.clearDeleteError()
                    },
                    enabled = !messageDeleting
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Use Column instead of Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Custom header without gap
        MessageHeader(
            userName = userName,
            profilePic = profilePic,
            onBackClick = { navController.navigateUp() }
        )

        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                conversationLoading && conversationMessages.isEmpty() -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD946A6))
                    }
                }
                conversationError != null && conversationMessages.isEmpty() -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = conversationError ?: "Failed to load messages",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.fetchConversation(otherUserId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD946A6)
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                conversationMessages.isEmpty() -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No messages yet. Start the conversation!",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    // Messages list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        state = listState
                    ) {
                        items(conversationMessages) { message ->
                            Log.d("MessageScreen", "currentotherUserId: $currentotherUserId, message.sender_id: ${message.sender_id} ")
                            MessageItem(
                                message = message,
                                isFromCurrentUser = message.receiver_id == otherUserId,
                                onLongPress = {
                                    // Only allow deleting own messages
                                    if (message.receiver_id == otherUserId) {
                                        messageToDelete = message
                                        showDeleteDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom bar
        Column {
            // Error message display
            messageError?.let { error ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearMessageError() }) {
                            Text("Dismiss", color = Color(0xFFD32F2F))
                        }
                    }
                }
            }

            // Input box at bottom
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        placeholder = {
                            Text(
                                text = "Type your message to get started...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedContainerColor = Color(0xFFF5F5F5)
                        ),
                        singleLine = true,
                        enabled = !messageSending
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Send button
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank() && !messageSending) {
                                viewModel.sendMessage(
                                    receiverId = otherUserId,
                                    content = messageText.trim(),
                                    messageType = "text"
                                )
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (messageText.isNotBlank() && !messageSending) {
                                    Color(0xFFD946A6)
                                } else {
                                    Color(0xFFE0E0E0)
                                },
                                shape = CircleShape
                            ),
                        enabled = messageText.isNotBlank() && !messageSending
                    ) {
                        if (messageSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (messageText.isNotBlank()) Color.White else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}












@Composable
fun MessageHeader(
    userName: String,
    profilePic: String?,
    onBackClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            // Profile image
            if (profilePic != null) {
                AsyncImage(
                    model = profilePic,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile1),
                    error = painterResource(id = R.drawable.ic_profile1)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User info
            Column {
                Text(
                    text = userName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "Creator",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageItem(
    message: ConversationMessage,
    isFromCurrentUser: Boolean,
    onLongPress: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isFromCurrentUser) {
                        Color(0xFFD946A6)
                    } else {
                        Color(0xFFE0E0E0)
                    },
                    shape = RoundedCornerShape(24.dp)
                )
                .combinedClickable(
                    onClick = { },
                    onLongClick = {
                        if (isFromCurrentUser) {
                            onLongPress()
                        }
                    }
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = message.content,
                color = if (isFromCurrentUser) Color.White else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}




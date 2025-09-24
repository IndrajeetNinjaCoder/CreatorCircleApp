package com.cc.creatorcircle.ui.screens.sabo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cc.creatorcircle.data.models.websocket.StreamingMessage
import com.cc.creatorcircle.data.websocket.WebSocketManager
import com.cc.creatorcircle.viewModel.websocket.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val userId = "233" // Fixed user ID
    val websocketUrl = "wss://creatorcircle.in/api/chat/ws/" // Fixed WebSocket URL

    val connectionState by viewModel.connectionState.collectAsState()
    val streamingMessages by viewModel.streamingMessages.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-connect when screen loads
    LaunchedEffect(Unit) {
        if (connectionState == WebSocketManager.ConnectionState.DISCONNECTED) {
            viewModel.connect(websocketUrl)
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(streamingMessages.size) {
        if (streamingMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(streamingMessages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Connection Status (Optional - can be removed if you don't want to show status)
        if (connectionState != WebSocketManager.ConnectionState.CONNECTED) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (connectionState) {
                        WebSocketManager.ConnectionState.CONNECTING -> MaterialTheme.colorScheme.secondaryContainer
                        WebSocketManager.ConnectionState.ERROR -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (connectionState == WebSocketManager.ConnectionState.CONNECTING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = when (connectionState) {
                            WebSocketManager.ConnectionState.CONNECTING -> "Connecting..."
                            WebSocketManager.ConnectionState.ERROR -> "Connection Error"
                            else -> "Disconnected"
                        },
                        color = when (connectionState) {
                            WebSocketManager.ConnectionState.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Messages List
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (streamingMessages.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start a conversation...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(
                        items = streamingMessages.values.sortedBy { it.timestamp },
                        key = { it.id }
                    ) { message ->
                        StreamingMessageItem(message = message)
                        if (message != streamingMessages.values.sortedBy { it.timestamp }.last()) {
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                label = { Text("Type your message...") },
                modifier = Modifier.weight(1f),
                enabled = connectionState == WebSocketManager.ConnectionState.CONNECTED,
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(userId.toIntOrNull() ?: 233, messageText)
                        messageText = ""
                    }
                },
                enabled = connectionState == WebSocketManager.ConnectionState.CONNECTED &&
                        messageText.isNotBlank()
            ) {
                Text("Send")
            }
        }
    }
}

//@Composable
//fun StreamingMessageItem(message: StreamingMessage) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (message.role == "user") {
//                MaterialTheme.colorScheme.primaryContainer
//            } else {
//                MaterialTheme.colorScheme.secondaryContainer
//            }
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp)
//        ) {
//            // Message header
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = message.role.uppercase(),
//                    style = MaterialTheme.typography.labelMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    // Streaming indicator
//                    if (!message.isComplete) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(12.dp),
//                            strokeWidth = 1.dp,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "Typing...",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = MaterialTheme.colorScheme.primary
//                        )
//                    } else {
//                        Text(
//                            text = "Complete",
//                            style = MaterialTheme.typography.labelSmall,
//                            color = Color.Green
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Message content with streaming effect
//            Text(
//                text = message.content,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            // Timestamp
//            Text(
//                text = message.timestamp,
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    }
//}

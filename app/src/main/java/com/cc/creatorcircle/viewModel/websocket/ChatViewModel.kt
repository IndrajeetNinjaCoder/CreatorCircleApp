package com.cc.creatorcircle.viewModel.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.websocket.WebSocketManager
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val webSocketManager = WebSocketManager()

    val connectionState = webSocketManager.connectionState
    val messages = webSocketManager.messages

    // Expose streaming messages
    val streamingMessages = webSocketManager.streamingMessages

    fun connect(url: String) {
        viewModelScope.launch {
            webSocketManager.connect(url)
        }
    }

    fun sendMessage(userId: Int, message: String) {
        viewModelScope.launch {
            webSocketManager.sendMessage(userId, message)
        }
    }

    fun disconnect() {
        webSocketManager.disconnect()
    }

    fun clearMessages() {
        webSocketManager.clearMessages()
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }
}
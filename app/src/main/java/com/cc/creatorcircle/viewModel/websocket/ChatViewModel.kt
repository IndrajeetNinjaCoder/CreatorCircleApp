package com.cc.creatorcircle.viewModel.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.socket.ChatMessage
import com.cc.creatorcircle.data.socket.ConnectionState
import com.cc.creatorcircle.data.socket.SocketIOManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val socketIOManager = SocketIOManager()

    val messages: StateFlow<List<ChatMessage>> = socketIOManager.messages
    val connectionState: StateFlow<ConnectionState> = socketIOManager.connectionState
    val newSessionId: StateFlow<Int?> = socketIOManager.newSessionId // EXPOSE THIS

    fun connect(userId: Int, sessionId: Int) {
        viewModelScope.launch {
            socketIOManager.connect(userId, sessionId)
        }
    }

    fun sendMessage(userId: Int, sessionId: Int, message: String) {
        viewModelScope.launch {
            socketIOManager.sendMessage(userId, sessionId, message)
        }
    }

    fun createNewMessage(userId: Int, profileId: Int, message: String) {
        viewModelScope.launch {
            socketIOManager.createNewMessage(userId, profileId, message)
        }
    }

    fun clearMessages() {
        socketIOManager.clearMessages()
    }

    fun clearNewSessionId() {
        socketIOManager.clearNewSessionId()
    }

    override fun onCleared() {
        super.onCleared()
        socketIOManager.disconnect()
    }

    fun reconnect(userId: Int, sessionId: Int) {
        viewModelScope.launch {
            socketIOManager.reconnect(userId, sessionId)
        }
    }

    fun disconnect() {
        socketIOManager.disconnect()
    }
}








//package com.cc.creatorcircle.viewModel.websocket
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cc.creatorcircle.data.socket.ChatMessage
//import com.cc.creatorcircle.data.socket.ConnectionState
//import com.cc.creatorcircle.data.socket.SocketIOManager
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class ChatViewModel : ViewModel() {
//    private val socketIOManager = SocketIOManager()
//
//    val messages: StateFlow<List<ChatMessage>> = socketIOManager.messages
//    val connectionState: StateFlow<ConnectionState> = socketIOManager.connectionState
//
//    fun connect(userId: Int, sessionId: Int) {
//        viewModelScope.launch {
//            socketIOManager.connect(userId, sessionId)
//        }
//    }
//
//    // Existing function: Send message to existing session
//    fun sendMessage(userId: Int, sessionId: Int, message: String) {
//        viewModelScope.launch {
//            socketIOManager.sendMessage(userId, sessionId, message)
//        }
//    }
//
//    // New function: Create new message with profile_id
//    fun createNewMessage(userId: Int, profileId: Int, message: String) {
//        viewModelScope.launch {
//            socketIOManager.createNewMessage(userId, profileId, message)
//        }
//    }
//
//    fun clearMessages() {
//        socketIOManager.clearMessages()
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        socketIOManager.disconnect()
//    }
//
//    fun reconnect(userId: Int, sessionId: Int) {
//        viewModelScope.launch {
//            socketIOManager.reconnect(userId, sessionId)
//        }
//    }
//
//    // ADD THIS METHOD
//    fun disconnect() {
//        socketIOManager.disconnect()
//    }
//}

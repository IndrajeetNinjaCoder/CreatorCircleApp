package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.SendMessageResponse
import com.cc.creatorcircle.data.models.ConversationMessage
import com.cc.creatorcircle.data.repository.MessageRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MessageViewModel(private val context: Context) : ViewModel() {

    private val repository = MessageRepository(context)
    private val tokenManager = TokenManager(context)

    // Sent Message State
    private val _sentMessage = MutableStateFlow<SendMessageResponse?>(null)
    val sentMessage: StateFlow<SendMessageResponse?> = _sentMessage.asStateFlow()

    private val _messageSending = MutableStateFlow(false)
    val messageSending: StateFlow<Boolean> = _messageSending.asStateFlow()

    private val _messageError = MutableStateFlow<String?>(null)
    val messageError: StateFlow<String?> = _messageError.asStateFlow()

    private val _messageSentSuccess = MutableStateFlow(false)
    val messageSentSuccess: StateFlow<Boolean> = _messageSentSuccess.asStateFlow()

    // Conversation State
    private val _conversationMessages = MutableStateFlow<List<ConversationMessage>>(emptyList())
    val conversationMessages: StateFlow<List<ConversationMessage>> = _conversationMessages.asStateFlow()

    private val _conversationLoading = MutableStateFlow(false)
    val conversationLoading: StateFlow<Boolean> = _conversationLoading.asStateFlow()

    private val _conversationError = MutableStateFlow<String?>(null)
    val conversationError: StateFlow<String?> = _conversationError.asStateFlow()

    // Delete Message State
    private val _messageDeleting = MutableStateFlow(false)
    val messageDeleting: StateFlow<Boolean> = _messageDeleting.asStateFlow()

    private val _messageDeleteSuccess = MutableStateFlow(false)
    val messageDeleteSuccess: StateFlow<Boolean> = _messageDeleteSuccess.asStateFlow()

    private val _messageDeleteError = MutableStateFlow<String?>(null)
    val messageDeleteError: StateFlow<String?> = _messageDeleteError.asStateFlow()

    private val _deletedMessageId = MutableStateFlow<Int?>(null)
    val deletedMessageId: StateFlow<Int?> = _deletedMessageId.asStateFlow()

    /**
     * Send a message to a user
     */
    fun sendMessage(
        receiverId: Int,
        content: String,
        messageType: String = "text"
    ) {
        viewModelScope.launch {
            try {
                Log.d("MessageViewModel", "Sending message to receiverId: $receiverId")
                _messageSending.value = true
                _messageError.value = null
                _messageSentSuccess.value = false

                repository.sendMessage(receiverId, content, messageType)
                    .onSuccess { messageResponse ->
                        Log.d("MessageViewModel", "Message sent successfully: ${messageResponse.id}")
                        _sentMessage.value = messageResponse
                        _messageSentSuccess.value = true
                    }
                    .onFailure { exception ->
                        Log.e("MessageViewModel", "Failed to send message", exception)
                        _messageError.value = exception.message ?: "Failed to send message"
                        _messageSentSuccess.value = false
                    }
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Exception in sendMessage", e)
                _messageError.value = "Network error: ${e.message}"
                _messageSentSuccess.value = false
            } finally {
                _messageSending.value = false
            }
        }
    }

    /**
     * Clear sent message data
     */
    fun clearSentMessage() {
        _sentMessage.value = null
        _messageError.value = null
        _messageSentSuccess.value = false
    }

    /**
     * Clear message error
     */
    fun clearMessageError() {
        _messageError.value = null
    }

    /**
     * Reset success state
     */
    fun resetSuccessState() {
        _messageSentSuccess.value = false
    }

    /**
     * Check if user has valid token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Get current sent message
     */
    fun getCurrentSentMessage(): SendMessageResponse? {
        return _sentMessage.value
    }

    /**
     * Fetch conversation messages with a user
     */
    fun fetchConversation(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("MessageViewModel", "Fetching conversation with userId: $userId")
                _conversationLoading.value = true
                _conversationError.value = null

                repository.getConversation(userId)
                    .onSuccess { messages ->
                        Log.d("MessageViewModel", "Conversation fetched successfully: ${messages.size} messages")
                        _conversationMessages.value = messages
                    }
                    .onFailure { exception ->
                        Log.e("MessageViewModel", "Failed to fetch conversation", exception)
                        _conversationError.value = exception.message ?: "Failed to fetch conversation"
                    }
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Exception in fetchConversation", e)
                _conversationError.value = "Network error: ${e.message}"
            } finally {
                _conversationLoading.value = false
            }
        }
    }

    /**
     * Refresh conversation messages
     */
    fun refreshConversation(userId: Int) {
        fetchConversation(userId)
    }

    /**
     * Clear conversation data
     */
    fun clearConversation() {
        _conversationMessages.value = emptyList()
        _conversationError.value = null
    }

    /**
     * Clear conversation error
     */
    fun clearConversationError() {
        _conversationError.value = null
    }

    /**
     * Get current conversation messages
     */
    fun getCurrentConversation(): List<ConversationMessage> {
        return _conversationMessages.value
    }

    /**
     * Delete a message
     */
    fun deleteMessage(messageId: Int) {
        viewModelScope.launch {
            try {
                Log.d("MessageViewModel", "Deleting message with id: $messageId")
                _messageDeleting.value = true
                _messageDeleteError.value = null
                _messageDeleteSuccess.value = false

                repository.deleteMessage(messageId)
                    .onSuccess { deleteResponse ->
                        if (deleteResponse.success) {
                            Log.d("MessageViewModel", "Message deleted successfully: $messageId")
                            _messageDeleteSuccess.value = true
                            _deletedMessageId.value = messageId

                            // Remove the deleted message from the conversation list
                            _conversationMessages.value = _conversationMessages.value.filter { it.id != messageId }
                        } else {
                            Log.e("MessageViewModel", "Delete response returned false")
                            _messageDeleteError.value = "Failed to delete message"
                            _messageDeleteSuccess.value = false
                        }
                    }
                    .onFailure { exception ->
                        Log.e("MessageViewModel", "Failed to delete message", exception)
                        _messageDeleteError.value = exception.message ?: "Failed to delete message"
                        _messageDeleteSuccess.value = false
                    }
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Exception in deleteMessage", e)
                _messageDeleteError.value = "Network error: ${e.message}"
                _messageDeleteSuccess.value = false
            } finally {
                _messageDeleting.value = false
            }
        }
    }

    /**
     * Clear delete message state
     */
    fun clearDeleteMessageState() {
        _messageDeleteError.value = null
        _messageDeleteSuccess.value = false
        _deletedMessageId.value = null
    }

    /**
     * Clear delete error
     */
    fun clearDeleteError() {
        _messageDeleteError.value = null
    }

    /**
     * Reset delete success state
     */
    fun resetDeleteSuccessState() {
        _messageDeleteSuccess.value = false
        _deletedMessageId.value = null
    }
}















//package com.cc.creatorcircle.viewModel
//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cc.creatorcircle.data.models.SendMessageResponse
//import com.cc.creatorcircle.data.models.ConversationMessage
//import com.cc.creatorcircle.data.repository.MessageRepository
//import com.cc.creatorcircle.utils.TokenManager
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class MessageViewModel(private val context: Context) : ViewModel() {
//
//    private val repository = MessageRepository(context)
//    private val tokenManager = TokenManager(context)
//
//    // Sent Message State
//    private val _sentMessage = MutableStateFlow<SendMessageResponse?>(null)
//    val sentMessage: StateFlow<SendMessageResponse?> = _sentMessage.asStateFlow()
//
//    private val _messageSending = MutableStateFlow(false)
//    val messageSending: StateFlow<Boolean> = _messageSending.asStateFlow()
//
//    private val _messageError = MutableStateFlow<String?>(null)
//    val messageError: StateFlow<String?> = _messageError.asStateFlow()
//
//    private val _messageSentSuccess = MutableStateFlow(false)
//    val messageSentSuccess: StateFlow<Boolean> = _messageSentSuccess.asStateFlow()
//
//    // Conversation State
//    private val _conversationMessages = MutableStateFlow<List<ConversationMessage>>(emptyList())
//    val conversationMessages: StateFlow<List<ConversationMessage>> = _conversationMessages.asStateFlow()
//
//    private val _conversationLoading = MutableStateFlow(false)
//    val conversationLoading: StateFlow<Boolean> = _conversationLoading.asStateFlow()
//
//    private val _conversationError = MutableStateFlow<String?>(null)
//    val conversationError: StateFlow<String?> = _conversationError.asStateFlow()
//
//    /**
//     * Send a message to a user
//     */
//    fun sendMessage(
//        receiverId: Int,
//        content: String,
//        messageType: String = "text"
//    ) {
//        viewModelScope.launch {
//            try {
//                Log.d("MessageViewModel", "Sending message to receiverId: $receiverId")
//                _messageSending.value = true
//                _messageError.value = null
//                _messageSentSuccess.value = false
//
//                repository.sendMessage(receiverId, content, messageType)
//                    .onSuccess { messageResponse ->
//                        Log.d("MessageViewModel", "Message sent successfully: ${messageResponse.id}")
//                        _sentMessage.value = messageResponse
//                        _messageSentSuccess.value = true
//                    }
//                    .onFailure { exception ->
//                        Log.e("MessageViewModel", "Failed to send message", exception)
//                        _messageError.value = exception.message ?: "Failed to send message"
//                        _messageSentSuccess.value = false
//                    }
//            } catch (e: Exception) {
//                Log.e("MessageViewModel", "Exception in sendMessage", e)
//                _messageError.value = "Network error: ${e.message}"
//                _messageSentSuccess.value = false
//            } finally {
//                _messageSending.value = false
//            }
//        }
//    }
//
//    /**
//     * Clear sent message data
//     */
//    fun clearSentMessage() {
//        _sentMessage.value = null
//        _messageError.value = null
//        _messageSentSuccess.value = false
//    }
//
//    /**
//     * Clear message error
//     */
//    fun clearMessageError() {
//        _messageError.value = null
//    }
//
//    /**
//     * Reset success state
//     */
//    fun resetSuccessState() {
//        _messageSentSuccess.value = false
//    }
//
//    /**
//     * Check if user has valid token
//     */
//    fun hasValidToken(): Boolean {
//        return tokenManager.getToken().isNotEmpty()
//    }
//
//    /**
//     * Get current sent message
//     */
//    fun getCurrentSentMessage(): SendMessageResponse? {
//        return _sentMessage.value
//    }
//
//    /**
//     * Fetch conversation messages with a user
//     */
//    fun fetchConversation(userId: Int) {
//        viewModelScope.launch {
//            try {
//                Log.d("MessageViewModel", "Fetching conversation with userId: $userId")
//                _conversationLoading.value = true
//                _conversationError.value = null
//
//                repository.getConversation(userId)
//                    .onSuccess { messages ->
//                        Log.d("MessageViewModel", "Conversation fetched successfully: ${messages.size} messages")
//                        _conversationMessages.value = messages
//                    }
//                    .onFailure { exception ->
//                        Log.e("MessageViewModel", "Failed to fetch conversation", exception)
//                        _conversationError.value = exception.message ?: "Failed to fetch conversation"
//                    }
//            } catch (e: Exception) {
//                Log.e("MessageViewModel", "Exception in fetchConversation", e)
//                _conversationError.value = "Network error: ${e.message}"
//            } finally {
//                _conversationLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Refresh conversation messages
//     */
//    fun refreshConversation(userId: Int) {
//        fetchConversation(userId)
//    }
//
//    /**
//     * Clear conversation data
//     */
//    fun clearConversation() {
//        _conversationMessages.value = emptyList()
//        _conversationError.value = null
//    }
//
//    /**
//     * Clear conversation error
//     */
//    fun clearConversationError() {
//        _conversationError.value = null
//    }
//
//    /**
//     * Get current conversation messages
//     */
//    fun getCurrentConversation(): List<ConversationMessage> {
//        return _conversationMessages.value
//    }
//}
//
//

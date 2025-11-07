package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.SendMessageResponse
import com.cc.creatorcircle.data.models.SendMessageRequest
import com.cc.creatorcircle.data.models.ConversationMessage
import com.cc.creatorcircle.data.models.DeleteMessageResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class MessageRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Send message to a user
     */
    suspend fun sendMessage(
        receiverId: Int,
        content: String,
        messageType: String = "text"
    ): Result<SendMessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure<SendMessageResponse>(
                        Exception("Access token not found. Please log in again.")
                    )
                }

                val request = SendMessageRequest(
                    receiver_id = receiverId,
                    content = content,
                    message_type = messageType
                )

                val response = apiService.sendMessage(request, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { message ->
                        Log.d("MessageRepository", "Message sent successfully: ${message.id}")
                        Result.success(message)
                    } ?: Result.failure<SendMessageResponse>(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("MessageRepository", "API Error in sendMessage: ${response.code()} - $errorMessage")
                    Result.failure<SendMessageResponse>(
                        Exception("Failed to send message: ${response.code()} - $errorMessage")
                    )
                }
            } catch (e: Exception) {
                Log.e("MessageRepository", "Exception in sendMessage", e)
                Result.failure<SendMessageResponse>(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Send message (Response version)
     */
    suspend fun sendMessageResponse(
        receiverId: Int,
        content: String,
        messageType: String = "text"
    ): Response<SendMessageResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            val request = SendMessageRequest(
                receiver_id = receiverId,
                content = content,
                message_type = messageType
            )
            apiService.sendMessage(request, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Get conversation messages with a user
     */
    suspend fun getConversation(userId: Int): Result<List<ConversationMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure<List<ConversationMessage>>(
                        Exception("Access token not found. Please log in again.")
                    )
                }

                val response = apiService.getConversation(userId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { messages ->
                        Log.d("MessageRepository", "Successfully fetched ${messages.size} messages for conversation with user $userId")
                        Result.success(messages)
                    } ?: Result.failure<List<ConversationMessage>>(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("MessageRepository", "API Error in getConversation: ${response.code()} - $errorMessage")
                    Result.failure<List<ConversationMessage>>(
                        Exception("Failed to get conversation: ${response.code()} - $errorMessage")
                    )
                }
            } catch (e: Exception) {
                Log.e("MessageRepository", "Exception in getConversation", e)
                Result.failure<List<ConversationMessage>>(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Get conversation (Response version)
     */
    suspend fun getConversationResponse(userId: Int): Response<List<ConversationMessage>> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getConversation(userId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Delete a message
     */
    suspend fun deleteMessage(messageId: Int): Result<DeleteMessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure<DeleteMessageResponse>(
                        Exception("Access token not found. Please log in again.")
                    )
                }

                val response = apiService.deleteMessage(messageId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { deleteResponse ->
                        Log.d("MessageRepository", "Message deleted successfully: $messageId")
                        Result.success(deleteResponse)
                    } ?: Result.failure<DeleteMessageResponse>(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("MessageRepository", "API Error in deleteMessage: ${response.code()} - $errorMessage")
                    Result.failure<DeleteMessageResponse>(
                        Exception("Failed to delete message: ${response.code()} - $errorMessage")
                    )
                }
            } catch (e: Exception) {
                Log.e("MessageRepository", "Exception in deleteMessage", e)
                Result.failure<DeleteMessageResponse>(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Delete message (Response version)
     */
    suspend fun deleteMessageResponse(messageId: Int): Response<DeleteMessageResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.deleteMessage(messageId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }
}















//package com.cc.creatorcircle.data.repository
//
//import android.content.Context
//import android.util.Log
//import com.cc.creatorcircle.data.api.RetrofitInstance
//import com.cc.creatorcircle.data.models.SendMessageResponse
//import com.cc.creatorcircle.data.models.SendMessageRequest
//import com.cc.creatorcircle.data.models.ConversationMessage
//import com.cc.creatorcircle.utils.TokenManager
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import retrofit2.Response
//
//class MessageRepository(private val context: Context) {
//
//    private val apiService = RetrofitInstance.api
//    private val tokenManager = TokenManager(context)
//
//    /**
//     * Send message to a user
//     */
//    suspend fun sendMessage(
//        receiverId: Int,
//        content: String,
//        messageType: String = "text"
//    ): Result<SendMessageResponse> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val token = tokenManager.getToken()
//                if (token.isEmpty()) {
//                    return@withContext Result.failure<SendMessageResponse>(
//                        Exception("Access token not found. Please log in again.")
//                    )
//                }
//
//                val request = SendMessageRequest(
//                    receiver_id = receiverId,
//                    content = content,
//                    message_type = messageType
//                )
//
//                val response = apiService.sendMessage(request, "Bearer $token")
//
//                if (response.isSuccessful) {
//                    response.body()?.let { message ->
//                        Log.d("MessageRepository", "Message sent successfully: ${message.id}")
//                        Result.success(message)
//                    } ?: Result.failure<SendMessageResponse>(Exception("Empty response body"))
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
//                    Log.e("MessageRepository", "API Error in sendMessage: ${response.code()} - $errorMessage")
//                    Result.failure<SendMessageResponse>(
//                        Exception("Failed to send message: ${response.code()} - $errorMessage")
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e("MessageRepository", "Exception in sendMessage", e)
//                Result.failure<SendMessageResponse>(e)
//            }
//        }
//    }
//
//    /**
//     * Alternative method using Response<T> pattern
//     * Send message (Response version)
//     */
//    suspend fun sendMessageResponse(
//        receiverId: Int,
//        content: String,
//        messageType: String = "text"
//    ): Response<SendMessageResponse> {
//        val token = tokenManager.getToken()
//        return if (token.isNotEmpty()) {
//            val request = SendMessageRequest(
//                receiver_id = receiverId,
//                content = content,
//                message_type = messageType
//            )
//            apiService.sendMessage(request, "Bearer $token")
//        } else {
//            throw Exception("Access token not found. Please log in again.")
//        }
//    }
//
//    /**
//     * Get conversation messages with a user
//     */
//    suspend fun getConversation(userId: Int): Result<List<ConversationMessage>> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val token = tokenManager.getToken()
//                if (token.isEmpty()) {
//                    return@withContext Result.failure<List<ConversationMessage>>(
//                        Exception("Access token not found. Please log in again.")
//                    )
//                }
//
//                val response = apiService.getConversation(userId, "Bearer $token")
//
//                if (response.isSuccessful) {
//                    response.body()?.let { messages ->
//                        Log.d("MessageRepository", "Successfully fetched ${messages.size} messages for conversation with user $userId")
//                        Result.success(messages)
//                    } ?: Result.failure<List<ConversationMessage>>(Exception("Empty response body"))
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
//                    Log.e("MessageRepository", "API Error in getConversation: ${response.code()} - $errorMessage")
//                    Result.failure<List<ConversationMessage>>(
//                        Exception("Failed to get conversation: ${response.code()} - $errorMessage")
//                    )
//                }
//            } catch (e: Exception) {
//                Log.e("MessageRepository", "Exception in getConversation", e)
//                Result.failure<List<ConversationMessage>>(e)
//            }
//        }
//    }
//
//    /**
//     * Alternative method using Response<T> pattern
//     * Get conversation (Response version)
//     */
//    suspend fun getConversationResponse(userId: Int): Response<List<ConversationMessage>> {
//        val token = tokenManager.getToken()
//        return if (token.isNotEmpty()) {
//            apiService.getConversation(userId, "Bearer $token")
//        } else {
//            throw Exception("Access token not found. Please log in again.")
//        }
//    }
//}
//
//

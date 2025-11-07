package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.AddChatProfileRequest
import com.cc.creatorcircle.data.models.AddChatProfileResponse
import com.cc.creatorcircle.data.models.ChatHistoryResponse
import com.cc.creatorcircle.data.models.ChatMessagesResponse
import com.cc.creatorcircle.data.models.ChatSessionResponse
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.data.models.ChatUserProfilesResponse
import com.cc.creatorcircle.data.models.CreateChatSessionRequest
import com.cc.creatorcircle.data.models.CreateChatSessionResponse
import com.cc.creatorcircle.data.models.DeleteChatProfileResponse
import com.cc.creatorcircle.data.models.DeleteChatSessionResponse
import com.cc.creatorcircle.data.models.SetProfileActiveRequest
import com.cc.creatorcircle.data.models.SetProfileActiveResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class ChatRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get chat user profile by user ID
     */
    suspend fun getChatUserProfile(userId: Int): Result<ChatUserProfilesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getChatUserProfile(userId, "Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { userProfile ->
                        Result.success(userProfile)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in getChatUserProfile: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get user profile: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in getChatUserProfile", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Create a new chat session
     */
    suspend fun createNewChatSession(userId: Int): Result<CreateChatSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val request = CreateChatSessionRequest(userId)
                val response = apiService.createNewChatSession(request, "Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { chatSession ->
                        Result.success(chatSession)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in createNewChatSession: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to create chat session: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in createNewChatSession", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get chat history for a user
     */
    suspend fun getChatHistory(
        userId: Int, 
        platform: String? = null, 
        profileId: Int? = null
    ): Result<ChatHistoryResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getChatHistory(userId, "Bearer $token", platform, profileId)
                
                if (response.isSuccessful) {
                    response.body()?.let { chatHistory ->
                        Result.success(chatHistory)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in getChatHistory: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get chat history: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in getChatHistory", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get specific chat session details
     */
    suspend fun getChatSession(sessionId: Int): Result<ChatSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getChatSession(sessionId, "Bearer $token")
                
                if (response.isSuccessful) {
                    response.body()?.let { chatSession ->
                        Result.success(chatSession)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in getChatSession: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get chat session: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in getChatSession", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get messages for a specific chat session
     */

    suspend fun getChatMessages(sessionId: Int): Result<ChatMessagesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getChatMessages(sessionId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { chatMessages ->
                        Log.d("ChatRepository", "Successfully fetched ${chatMessages.size} messages for session $sessionId")
                        Result.success(chatMessages)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in getChatMessages: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get chat messages: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in getChatMessages", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Alternative method using Response<T> pattern like PostsRepository
     * Get chat user profile (Response version)
     */
    suspend fun getChatUserProfileResponse(userId: Int): Response<ChatUserProfilesResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getChatUserProfile(userId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern like PostsRepository
     * Create new chat session (Response version)
     */
    suspend fun createNewChatSessionResponse(userId: Int): Response<CreateChatSessionResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            val request = CreateChatSessionRequest(userId)
            apiService.createNewChatSession(request, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern like PostsRepository
     * Get chat history (Response version)
     */
    suspend fun getChatHistoryResponse(
        userId: Int, 
        platform: String? = null, 
        profileId: Int? = null
    ): Response<ChatHistoryResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getChatHistory(userId, "Bearer $token", platform, profileId)
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern like PostsRepository
     * Get chat session (Response version)
     */
    suspend fun getChatSessionResponse(sessionId: Int): Response<ChatSessionResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getChatSession(sessionId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern like PostsRepository
     * Get chat messages (Response version)
     */
    suspend fun getChatMessagesResponse(sessionId: Int): Response<ChatMessagesResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getChatMessages(sessionId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }


    /**
     * Set profile as active
     */
    suspend fun setProfileActive(profileId: Int, userId: Int): Result<SetProfileActiveResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val request = SetProfileActiveRequest(userId)
                val response = apiService.setProfileActive(profileId, request, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { activeProfileResponse ->
                        Log.d("ChatRepository", "Profile set as active successfully: ${activeProfileResponse.profile.username}")
                        Result.success(activeProfileResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in setProfileActive: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to set profile active: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in setProfileActive", e)
                Result.failure(e)
            }
        }
    }



    /**
     * Add/Create a new chat profile
     */
    suspend fun addChatProfile(userId: Int, platform: String, username: String): Result<AddChatProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val request = AddChatProfileRequest(userId, platform, username)
                val response = apiService.addChatProfile(request, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { addProfileResponse ->
                        Log.d("ChatRepository", "Profile added successfully: ${addProfileResponse.username}")
                        Result.success(addProfileResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in addChatProfile: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to add chat profile: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in addChatProfile", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Add chat profile (Response version)
     */
    suspend fun addChatProfileResponse(userId: Int, platform: String, username: String): Response<AddChatProfileResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            val request = AddChatProfileRequest(userId, platform, username)
            apiService.addChatProfile(request, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }



    suspend fun deleteChatProfile(profileId: Int, userId: Int): Result<DeleteChatProfileResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.deleteChatProfile(profileId, userId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { deleteProfileResponse ->
                        Log.d("ChatRepository", "Profile deleted successfully: ${deleteProfileResponse.message}")
                        Result.success(deleteProfileResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in deleteChatProfile: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to delete chat profile: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in deleteChatProfile", e)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteChatSession(sessionId: Int): Result<DeleteChatSessionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.deleteChatSession(sessionId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { deleteSessionResponse ->
                        Log.d("ChatRepository", "Chat session deleted successfully: ${deleteSessionResponse.message}")
                        Result.success(deleteSessionResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("ChatRepository", "API Error in deleteChatSession: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to delete chat session: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("ChatRepository", "Exception in deleteChatSession", e)
                Result.failure(e)
            }
        }
    }


}




package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.AddChatProfileResponse
import com.cc.creatorcircle.data.models.ChatMessage
import com.cc.creatorcircle.data.models.ChatSession
import com.cc.creatorcircle.data.models.ChatSessionDetail
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.data.models.CreateChatSessionResponse
import com.cc.creatorcircle.data.models.ProfileData
import com.cc.creatorcircle.data.repository.ChatRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val context: Context) : ViewModel() {

    // Multiple User Profiles State (add this after _profileError)
    private val _userProfiles = MutableStateFlow<List<ChatUserProfile>>(emptyList())
    val userProfiles: StateFlow<List<ChatUserProfile>> = _userProfiles.asStateFlow()


    private val repository = ChatRepository(context)
    private val tokenManager = TokenManager(context)

    // User Profile State
    private val _userProfile = MutableStateFlow<ChatUserProfile?>(null)
    val userProfile: StateFlow<ChatUserProfile?> = _userProfile.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError.asStateFlow()

    // Chat History State
    private val _chatHistory = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatHistory: StateFlow<List<ChatSession>> = _chatHistory.asStateFlow()

    private val _historyLoading = MutableStateFlow(false)
    val historyLoading: StateFlow<Boolean> = _historyLoading.asStateFlow()

    private val _historyError = MutableStateFlow<String?>(null)
    val historyError: StateFlow<String?> = _historyError.asStateFlow()

    // Current Chat Session State
    private val _currentSession = MutableStateFlow<ChatSessionDetail?>(null)
    val currentSession: StateFlow<ChatSessionDetail?> = _currentSession.asStateFlow()

    private val _sessionLoading = MutableStateFlow(false)
    val sessionLoading: StateFlow<Boolean> = _sessionLoading.asStateFlow()

    private val _sessionError = MutableStateFlow<String?>(null)
    val sessionError: StateFlow<String?> = _sessionError.asStateFlow()

    // Chat Messages State
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _messagesLoading = MutableStateFlow(false)
    val messagesLoading: StateFlow<Boolean> = _messagesLoading.asStateFlow()

    private val _messagesError = MutableStateFlow<String?>(null)
    val messagesError: StateFlow<String?> = _messagesError.asStateFlow()

    // Chat Session Creation State
    private val _sessionCreationLoading = MutableStateFlow(false)
    val sessionCreationLoading: StateFlow<Boolean> = _sessionCreationLoading.asStateFlow()

    private val _sessionCreationError = MutableStateFlow<String?>(null)
    val sessionCreationError: StateFlow<String?> = _sessionCreationError.asStateFlow()

    private val _newSessionCreated = MutableStateFlow<CreateChatSessionResponse?>(null)
    val newSessionCreated: StateFlow<CreateChatSessionResponse?> = _newSessionCreated.asStateFlow()



    // Set Profile Active State
    private val _setProfileActiveLoading = MutableStateFlow(false)
    val setProfileActiveLoading: StateFlow<Boolean> = _setProfileActiveLoading.asStateFlow()

    private val _setProfileActiveError = MutableStateFlow<String?>(null)
    val setProfileActiveError: StateFlow<String?> = _setProfileActiveError.asStateFlow()

    private val _activeProfileUpdated = MutableStateFlow<ProfileData?>(null)
    val activeProfileUpdated: StateFlow<ProfileData?> = _activeProfileUpdated.asStateFlow()



    // Add Profile State (add this after _activeProfileUpdated)
    private val _addProfileLoading = MutableStateFlow(false)
    val addProfileLoading: StateFlow<Boolean> = _addProfileLoading.asStateFlow()

    private val _addProfileError = MutableStateFlow<String?>(null)
    val addProfileError: StateFlow<String?> = _addProfileError.asStateFlow()

    private val _profileAdded = MutableStateFlow<AddChatProfileResponse?>(null)
    val profileAdded: StateFlow<AddChatProfileResponse?> = _profileAdded.asStateFlow()







    /**
     * Fetch chat user profile by user ID
     */





    fun fetchChatUserProfiles(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Starting fetchChatUserProfiles for userId: $userId")
                _profileLoading.value = true
                _profileError.value = null

                repository.getChatUserProfile(userId)
                    .onSuccess { userProfilesResponse ->
                        Log.d("ChatViewModel", "User profiles response: $userProfilesResponse")

                        // Store all profiles
                        _userProfiles.value = userProfilesResponse

                        // Also set the active profile in the single profile field for backward compatibility
                        val activeProfile = userProfilesResponse.find { it.isActive }
                        if (activeProfile != null) {
                            _userProfile.value = activeProfile
                        } else if (userProfilesResponse.isNotEmpty()) {
                            _userProfile.value = userProfilesResponse.first()
                        }

                        Log.d("ChatViewModel", "All user profiles set: ${userProfilesResponse.size} profiles")
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to fetch user profiles", exception)
                        _profileError.value = exception.message ?: "Failed to fetch user profiles"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in fetchChatUserProfiles", e)
                _profileError.value = "Network error: ${e.message}"
            } finally {
                _profileLoading.value = false
            }
        }
    }









    fun fetchChatUserProfile(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Starting fetchChatUserProfile for userId: $userId")
                _profileLoading.value = true
                _profileError.value = null

                repository.getChatUserProfile(userId)
                    .onSuccess { userProfilesResponse ->
                        Log.d("ChatViewModel", "User profiles response: $userProfilesResponse")

                        // Handle the direct list of profiles from the response
                        if (userProfilesResponse.isNotEmpty()) {
                            // If you expect a single profile, take the first one
                            val userProfile = userProfilesResponse.first()
                            _userProfile.value = userProfile
                            Log.d("ChatViewModel", "User profile set: $userProfile")
                        } else {
                            Log.w("ChatViewModel", "No user profiles found in response")
                            _profileError.value = "No user profile found"
                        }
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to fetch user profile", exception)
                        _profileError.value = exception.message ?: "Failed to fetch user profile"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in fetchChatUserProfile", e)
                _profileError.value = "Network error: ${e.message}"
            } finally {
                _profileLoading.value = false
            }
        }
    }






    /**
     * Create a new chat session
     */
    fun createNewChatSession(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Creating new chat session for userId: $userId")
                _sessionCreationLoading.value = true
                _sessionCreationError.value = null
                _newSessionCreated.value = null

                repository.createNewChatSession(userId)
                    .onSuccess { sessionResponse ->
                        Log.d("ChatViewModel", "Chat session created: $sessionResponse")
                        _newSessionCreated.value = sessionResponse
                        // Refresh chat history to include new session
                        fetchChatHistory(userId)
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to create chat session", exception)
                        _sessionCreationError.value =
                            exception.message ?: "Failed to create chat session"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in createNewChatSession", e)
                _sessionCreationError.value = "Network error: ${e.message}"
            } finally {
                _sessionCreationLoading.value = false
            }
        }
    }




    /**
     * Set a profile as active
     */
    fun setProfileActive(profileId: Int, userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Setting profile $profileId as active for userId: $userId")
                _setProfileActiveLoading.value = true
                _setProfileActiveError.value = null
                _activeProfileUpdated.value = null

                repository.setProfileActive(profileId, userId)
                    .onSuccess { response ->
                        Log.d("ChatViewModel", "Profile set as active successfully: ${response.message}")
                        _activeProfileUpdated.value = response.profile

                        // Update the local profiles list to reflect the change
                        updateLocalProfileActiveState(response.profile)

                        // Refresh user profiles to get the updated state from server
                        fetchChatUserProfiles(userId)
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to set profile active", exception)
                        _setProfileActiveError.value = exception.message ?: "Failed to set profile active"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in setProfileActive", e)
                _setProfileActiveError.value = "Network error: ${e.message}"
            } finally {
                _setProfileActiveLoading.value = false
            }
        }
    }

    /**
     * Update local profile active state (optimistic update)
     */
    private fun updateLocalProfileActiveState(updatedProfile: ProfileData) {
        val currentProfiles = _userProfiles.value.toMutableList()

        // Set all profiles as inactive first
        val updatedProfiles = currentProfiles.map { profile ->
            profile.copy(isActive = false)
        }.toMutableList()

        // Find and update the active profile
        val profileIndex = updatedProfiles.indexOfFirst { it.id == updatedProfile.id }
        if (profileIndex != -1) {
            // Update the existing profile with new active state
            val existingProfile = updatedProfiles[profileIndex]
            val activeChatProfile = existingProfile.copy(
                username = updatedProfile.username,
                platform = updatedProfile.platform,
                platformLink = updatedProfile.platform_link,
                isActive = updatedProfile.is_active
            )
            updatedProfiles[profileIndex] = activeChatProfile

            // Update the single user profile state for backward compatibility
            _userProfile.value = activeChatProfile
        }

        _userProfiles.value = updatedProfiles
    }



    /**
     * Add a new chat profile
     */
    fun addChatProfile(userId: Int, platform: String, username: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Adding new chat profile - Platform: $platform, Username: $username")
                _addProfileLoading.value = true
                _addProfileError.value = null
                _profileAdded.value = null

                repository.addChatProfile(userId, platform, username)
                    .onSuccess { addProfileResponse ->
                        Log.d("ChatViewModel", "Profile added successfully: ${addProfileResponse.username}")
                        _profileAdded.value = addProfileResponse

                        // Refresh user profiles to include the new profile
                        fetchChatUserProfiles(userId)
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to add chat profile", exception)
                        _addProfileError.value = exception.message ?: "Failed to add chat profile"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in addChatProfile", e)
                _addProfileError.value = "Network error: ${e.message}"
            } finally {
                _addProfileLoading.value = false
            }
        }
    }



    fun fetchChatHistory(userId: Int, platform: String? = null, profileId: Int? = null) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "=== FETCHING CHAT HISTORY ===")
                Log.d("ChatViewModel", "userId: $userId")
                Log.d("ChatViewModel", "platform: $platform")
                Log.d("ChatViewModel", "profileId: $profileId")

                _historyLoading.value = true
                _historyError.value = null

                repository.getChatHistory(userId, platform, profileId)
                    .onSuccess { historyResponse ->
                        Log.d("ChatViewModel", "✅ SUCCESS: Chat history fetched")
                        Log.d("ChatViewModel", "Number of sessions: ${historyResponse.size}")

                        // Log each session for debugging
                        historyResponse.forEachIndexed { index, session ->
                            Log.d("ChatViewModel", "Session $index: ${session.title} (${session.platform}) - SessionID: ${session.sessionId}")
                        }

                        _chatHistory.value = historyResponse
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "❌ FAILED: to fetch chat history", exception)
                        Log.e("ChatViewModel", "Parameters were - userId: $userId, platform: $platform, profileId: $profileId")
                        _historyError.value = exception.message ?: "Failed to fetch chat history"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "💥 EXCEPTION in fetchChatHistory", e)
                _historyError.value = "Network error: ${e.message}"
            } finally {
                _historyLoading.value = false
                Log.d("ChatViewModel", "=== CHAT HISTORY FETCH COMPLETE ===")
            }
        }
    }

    /**
     * Fetch specific chat session details
     */
    fun fetchChatSession(sessionId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching chat session: $sessionId")
                _sessionLoading.value = true
                _sessionError.value = null

                repository.getChatSession(sessionId)
                    .onSuccess { sessionResponse ->
                        Log.d("ChatViewModel", "Chat session fetched: $sessionResponse")
                        _currentSession.value = sessionResponse.data
                        // Also update messages from the session
                        _messages.value = sessionResponse.data.messages
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to fetch chat session", exception)
                        _sessionError.value = exception.message ?: "Failed to fetch chat session"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in fetchChatSession", e)
                _sessionError.value = "Network error: ${e.message}"
            } finally {
                _sessionLoading.value = false
            }
        }
    }

    /**
     * Fetch messages for a specific chat session
     */
    fun fetchChatMessages(sessionId: Int) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Fetching messages for session: $sessionId")
                _messagesLoading.value = true
                _messagesError.value = null

                repository.getChatMessages(sessionId)
                    .onSuccess { messagesResponse ->
                        Log.d("ChatViewModel", "Messages fetched: ${messagesResponse.size}")
                        // messagesResponse is now directly List<ChatMessage>, not wrapped in a data object
                        _messages.value = messagesResponse
                    }
                    .onFailure { exception ->
                        Log.e("ChatViewModel", "Failed to fetch messages", exception)
                        _messagesError.value = exception.message ?: "Failed to fetch messages"
                    }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception in fetchChatMessages", e)
                _messagesError.value = "Network error: ${e.message}"
            } finally {
                _messagesLoading.value = false
            }
        }
    }


    /**
     * Refresh chat history
     */
    fun refreshChatHistory(userId: Int, platform: String? = null, profileId: Int? = null) {
        fetchChatHistory(userId, platform, profileId)
    }

    /**
     * Refresh current session
     */
    fun refreshCurrentSession(sessionId: Int) {
        fetchChatSession(sessionId)
    }

    /**
     * Refresh messages
     */
    fun refreshMessages(sessionId: Int) {
        fetchChatMessages(sessionId)
    }

    /**
     * Clear current session data
     */
    fun clearCurrentSession() {
        _currentSession.value = null
        _messages.value = emptyList()
        _sessionError.value = null
        _messagesError.value = null
    }

    /**
     * Clear chat history
     */
    fun clearChatHistory() {
        _chatHistory.value = emptyList()
        _historyError.value = null
    }

    /**
     * Clear user profile data
     */
    fun clearUserProfile() {
        _userProfile.value = null
        _profileError.value = null
    }

    /**
     * Clear all chat data
     */
    fun clearAllChatData() {
        clearUserProfile()
        clearChatHistory()
        clearCurrentSession()
        _sessionCreationError.value = null
        _newSessionCreated.value = null
    }

    /**
     * Clear errors
     */
    fun clearErrors() {
        _profileError.value = null
        _historyError.value = null
        _sessionError.value = null
        _messagesError.value = null
        _sessionCreationError.value = null
    }

    /**
     * Clear session creation state
     */
    fun clearSessionCreationState() {
        _newSessionCreated.value = null
        _sessionCreationError.value = null
    }

    /**
     * Check if user has active token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Get current user profile
     */
    fun getCurrentUserProfile(): ChatUserProfile? {
        return _userProfile.value
    }

    /**
     * Get current chat session
     */
    fun getCurrentChatSession(): ChatSessionDetail? {
        return _currentSession.value
    }

    /**
     * Get current messages
     */
    fun getCurrentMessages(): List<ChatMessage> {
        return _messages.value
    }

    /**
     * Add a message locally (for optimistic UI updates)
     * This can be used when sending a message to immediately show it in UI
     */
    fun addMessageLocally(message: ChatMessage) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages
    }

    /**
     * Update a message locally (for when message status changes)
     */
    fun updateMessageLocally(messageId: String, updatedMessage: ChatMessage) {
        _messages.value = _messages.value.map { message ->
            if (message.id == messageId) updatedMessage else message
        }
    }

    /**
     * Clear add profile state
     */
    fun clearAddProfileState() {
        _profileAdded.value = null
        _addProfileError.value = null
    }
}
















//    fun fetchChatMessages(sessionId: Int) {
//        viewModelScope.launch {
//            try {
//                Log.d("ChatViewModel", "Fetching messages for session: $sessionId")
//                _messagesLoading.value = true
//                _messagesError.value = null
//
//                repository.getChatMessages(sessionId)
//                    .onSuccess { messagesResponse ->
//                        Log.d("ChatViewModel", "Messages fetched: ${messagesResponse.data.size}")
//                        _messages.value = messagesResponse.data
//                    }
//                    .onFailure { exception ->
//                        Log.e("ChatViewModel", "Failed to fetch messages", exception)
//                        _messagesError.value = exception.message ?: "Failed to fetch messages"
//                    }
//            } catch (e: Exception) {
//                Log.e("ChatViewModel", "Exception in fetchChatMessages", e)
//                _messagesError.value = "Network error: ${e.message}"
//            } finally {
//                _messagesLoading.value = false
//            }
//        }
//    }

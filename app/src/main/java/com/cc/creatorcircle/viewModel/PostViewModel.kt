package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.models.Comment
import com.cc.creatorcircle.data.models.UserList
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.data.repository.PostsRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.PostCreationState
import com.cc.creatorcircle.data.models.PostRequest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File


class PostsViewModel(private val context: Context) : ViewModel() {

    private val repository = PostsRepository(context)
    private val tokenManager = TokenManager(context)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    private val _reply = MutableStateFlow<List<Comment>>(emptyList())
    val reply: StateFlow<List<Comment>> = _reply


    private val _commentsLoading = MutableStateFlow(false)
    val commentsLoading: StateFlow<Boolean> = _commentsLoading

    private val _commentsError = MutableStateFlow<String?>(null)
    val commentsError: StateFlow<String?> = _commentsError


    private val _postCreationState = MutableStateFlow<PostCreationState>(PostCreationState.Idle)
    val postCreationState: StateFlow<PostCreationState> = _postCreationState.asStateFlow()

    private val _selectedMediaFiles = MutableStateFlow<List<File>>(emptyList())
    val selectedMediaFiles: StateFlow<List<File>> = _selectedMediaFiles.asStateFlow()


    // New StateFlows for user profile and accepted connections
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _acceptedConnections = MutableStateFlow<UserList?>(null)
    val acceptedConnections: StateFlow<UserList?> = _acceptedConnections

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading

    private val _profileError = MutableStateFlow<String?>(null)
    val profileError: StateFlow<String?> = _profileError




    // StateFlows for other user's profile

    // Replace the single otherUserProfile with a map
    private val _otherUserProfiles = MutableStateFlow<Map<Int, UserProfile>>(emptyMap())
    val otherUserProfiles: StateFlow<Map<Int, UserProfile>> = _otherUserProfiles

    private val _otherUserLoading = MutableStateFlow<Set<Int>>(emptySet())
    val otherUserLoading: StateFlow<Set<Int>> = _otherUserLoading

    private val _otherUserError = MutableStateFlow<Map<Int, String>>(emptyMap())
    val otherUserError: StateFlow<Map<Int, String>> = _otherUserError


//    init {
//        fetchPosts()
//    }

    fun fetchPosts(postType: String = "feed") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response =
                    if (postType == "feed") repository.getAllPosts()
                    else repository.getPosts(post_type = postType)

                if (response.isSuccessful) {
                    response.body()?.let { postsResponse ->
                        _posts.value = postsResponse.data
                    }
                } else {
                    _error.value = "Failed to fetch posts: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshPosts() {
        fetchPosts()
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    _error.value = "Please login to like posts"
                    return@launch
                }

                val response = repository.toggleLike(token, postId)
                if (response.isSuccessful) {
                    response.body()?.let { likeResponse ->
                        // Update the specific post in the list
                        _posts.value = _posts.value.map { post ->
                            if (post.id == postId) {
                                post.copy(
                                    isLiked = likeResponse.isLiked,
                                    likes = likeResponse.like_count
                                )
                            } else {
                                post
                            }
                        }
                    }
                } else {
                    _error.value = "Failed to toggle like: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            }
        }
    }


    fun toggleCommentLike(commentId: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    _commentsError.value = "Please login to like comments"
                    return@launch
                }

                val response = repository.toggleCommentLike(token, commentId)
                if (response.isSuccessful) {
                    response.body()?.let { commentLikeResponse ->
                        // Helper function to update comment likes recursively
                        fun updateCommentLikes(comments: List<Comment>): List<Comment> {
                            return comments.map { comment ->
                                if (comment.id == commentId) {
                                    comment.copy(
                                        likes = commentLikeResponse.like_count,
                                        userHasLiked = commentLikeResponse.isLiked
                                    )
                                } else {
                                    // Check in replies as well
                                    val updatedReplies = comment.replies?.let { replies ->
                                        updateCommentLikes(replies)
                                    }
                                    comment.copy(replies = updatedReplies)
                                }
                            }
                        }

                        // Update the comments list
                        _comments.value = updateCommentLikes(_comments.value)
                    }
                } else {
                    _commentsError.value = "Failed to toggle comment like: ${response.message()}"
                }
            } catch (e: Exception) {
                _commentsError.value = "Network error: ${e.message}"
            }
        }
    }


    fun fetchComments(postId: String) {
        viewModelScope.launch {
            try {
                _commentsLoading.value = true
                _commentsError.value = null
                _comments.value = emptyList() // Clear previous comments

                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    _commentsError.value = "Please login to view comments"
                    return@launch
                }

                val response = repository.getComments(token, postId)
                if (response.isSuccessful) {
                    response.body()?.let { commentResponse ->
                        _comments.value = commentResponse.data
                    }
                } else {
                    _commentsError.value = "Failed to fetch comments: ${response.message()}"
                }
            } catch (e: Exception) {
                _commentsError.value = "Network error: ${e.message}"
            } finally {
                _commentsLoading.value = false
            }
        }
    }

    fun clearComments() {
        _comments.value = emptyList()
        _commentsError.value = null
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    _commentsError.value = "Please login to add comments"
                    return@launch
                }

                if (content.isBlank()) {
                    _commentsError.value = "Comment cannot be empty"
                    return@launch
                }

                val response = repository.addComment(token, postId, content)
                if (response.isSuccessful) {
                    response.body()?.let { addCommentResponse ->
                        // Add the new comment directly to the existing comments list
                        val currentComments = _comments.value.toMutableList()
                        currentComments.add(0, addCommentResponse.data) // Add at the beginning
                        _comments.value = currentComments

                        // Update the post's comment count in the posts list
                        _posts.value = _posts.value.map { post ->
                            if (post.id == postId) {
                                post.copy(comments = post.comments + 1)
                            } else {
                                post
                            }
                        }
                    }
                } else {
                    _commentsError.value = "Failed to add comment: ${response.message()}"
                }
            } catch (e: Exception) {
                _commentsError.value = "Network error: ${e.message}"
            }
        }
    }


    fun addReply(postId: String, content: String, parentId: Int) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    _commentsError.value = "Please login to add Reply"
                    return@launch
                }

                if (content.isBlank()) {
                    _commentsError.value = "Reply cannot be empty"
                    return@launch
                }

                val response = repository.addReply(token, postId, content, parentId)
                if (response.isSuccessful) {
                    response.body()?.let { addReplyResponse ->
                        // Helper function to add reply to the correct parent comment
                        fun addReplyToComment(comments: List<Comment>): List<Comment> {
                            return comments.map { comment ->
                                if (comment.id == parentId) {
                                    // This is the parent comment, add the reply to its replies list
                                    val currentReplies =
                                        comment.replies?.toMutableList() ?: mutableListOf()
                                    currentReplies.add(
                                        0,
                                        addReplyResponse.data
                                    ) // Add at the beginning
                                    comment.copy(replies = currentReplies)
                                } else {
                                    // Check if this comment has replies and recursively search
                                    val updatedReplies = comment.replies?.let { replies ->
                                        addReplyToComment(replies)
                                    }
                                    comment.copy(replies = updatedReplies)
                                }
                            }
                        }

                        // Update the comments list with the new reply
                        _comments.value = addReplyToComment(_comments.value)

                        // Update the post's comment count in the posts list
                        _posts.value = _posts.value.map { post ->
                            if (post.id == postId) {
                                post.copy(comments = post.comments + 1)
                            } else {
                                post
                            }
                        }
                    }
                } else {
                    _commentsError.value = "Failed to add reply: ${response.message()}"
                }
            } catch (e: Exception) {
                _commentsError.value = "Network error: ${e.message}"
            }
        }
    }


    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("PostsViewModel", "Starting fetchUserProfile")
                _profileLoading.value = true
                _profileError.value = null

                val response = repository.getUserProfile()
                Log.d("PostsViewModel", "Repository response received")

                if (response.isSuccessful) {
                    response.body()?.let { userProfile ->
                        Log.d("PostsViewModel", "User profile data: $userProfile")
                        _userProfile.value = userProfile
                        _acceptedConnections.value = userProfile.accepted_connections
                        Log.d("PostsViewModel", "Profile data set successfully")
                    } ?: run {
                        Log.e("PostsViewModel", "Response body is null")
                        _profileError.value = "Empty response body"
                    }
                } else {
                    Log.e(
                        "PostsViewModel",
                        "API call failed: ${response.code()} - ${response.message()}"
                    )
                    _profileError.value = "Failed to fetch user profile: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("PostsViewModel", "Exception in fetchUserProfile", e)
                _profileError.value = "Network error: ${e.message}"
            } finally {
                _profileLoading.value = false
            }
        }
    }


    // Function to get accepted connections specifically
    fun getAcceptedConnections(): UserList? {
        return _acceptedConnections.value
    }

    // Function to refresh accepted connections
    fun refreshAcceptedConnections() {
        fetchUserProfile()
    }

    // Function to clear profile data
    fun clearProfileData() {
        _userProfile.value = null
        _acceptedConnections.value = null
        _profileError.value = null
    }


    fun createPost(content: String, postType: String) {
        if (content.isBlank()) {
            _postCreationState.value = PostCreationState.Error("Post content cannot be empty")
            return
        }

        viewModelScope.launch {
            _postCreationState.value = PostCreationState.Loading

            val postRequest = PostRequest(
                content = content.trim(),
                mediaFiles = _selectedMediaFiles.value
            )

            repository.createPost(postRequest, postType)
                .onSuccess { response ->
                    _postCreationState.value = PostCreationState.Success(response)
                    // Clear selected files after successful post
                    _selectedMediaFiles.value = emptyList()
                }
                .onFailure { exception ->
                    _postCreationState.value = PostCreationState.Error(
                        exception.message ?: "Failed to create post"
                    )
                }
        }
    }

    fun addMediaFile(file: File) {
        val currentFiles = _selectedMediaFiles.value.toMutableList()
        if (!currentFiles.contains(file) && currentFiles.size < 5) { // Limit to 5 files
            currentFiles.add(file)
            _selectedMediaFiles.value = currentFiles
        }
    }

    fun removeMediaFile(file: File) {
        val currentFiles = _selectedMediaFiles.value.toMutableList()
        currentFiles.remove(file)
        _selectedMediaFiles.value = currentFiles
    }

    fun clearError() {
        if (_postCreationState.value is PostCreationState.Error) {
            _postCreationState.value = PostCreationState.Idle
        }
    }










    // Update the fetch function
    fun fetchUserProfileById(userId: Int) {
        // Don't fetch if already loading or already loaded
        if (_otherUserLoading.value.contains(userId) || _otherUserProfiles.value.containsKey(userId)) {
            return
        }

        viewModelScope.launch {
            try {
                Log.d("PostsViewModel", "Fetching profile for userId: $userId")
                _otherUserLoading.value = _otherUserLoading.value + userId

                val currentErrors = _otherUserError.value.toMutableMap()
                currentErrors.remove(userId)
                _otherUserError.value = currentErrors

                val response = repository.getUserProfileById(userId)
                Log.d("PostsViewModel", "Repository response received for user $userId")

                if (response.isSuccessful) {
                    response.body()?.let { userProfile ->
                        Log.d("PostsViewModel", "User profile data: $userProfile")
                        _otherUserProfiles.value = _otherUserProfiles.value + (userId to userProfile)
                        Log.d("PostsViewModel", "Profile data set successfully for user $userId")
                    } ?: run {
                        Log.e("PostsViewModel", "Response body is null for user $userId")
                        val errors = _otherUserError.value.toMutableMap()
                        errors[userId] = "Empty response body"
                        _otherUserError.value = errors
                    }
                } else {
                    Log.e(
                        "PostsViewModel",
                        "API call failed for user $userId: ${response.code()} - ${response.message()}"
                    )
                    val errors = _otherUserError.value.toMutableMap()
                    errors[userId] = "Failed to fetch user profile: ${response.message()}"
                    _otherUserError.value = errors
                }
            } catch (e: Exception) {
                Log.e("PostsViewModel", "Exception in fetchUserProfileById for user $userId", e)
                val errors = _otherUserError.value.toMutableMap()
                errors[userId] = "Network error: ${e.message}"
                _otherUserError.value = errors
            } finally {
                _otherUserLoading.value = _otherUserLoading.value - userId
            }
        }
    }

    // Update clear function
    fun clearOtherUserProfiles() {
        _otherUserProfiles.value = emptyMap()
        _otherUserError.value = emptyMap()
    }

    // Helper function to get a specific user's profile
    fun getUserProfile(userId: Int): UserProfile? {
        return _otherUserProfiles.value[userId]
    }

    // Update other helper functions
    fun getOtherUserAcceptedConnections(userId: Int): UserList? {
        return _otherUserProfiles.value[userId]?.accepted_connections
    }

    fun getOtherUserFollowers(userId: Int): UserList? {
        return _otherUserProfiles.value[userId]?.followers
    }

    fun getOtherUserFollowing(userId: Int): UserList? {
        return _otherUserProfiles.value[userId]?.following
    }


}





package com.cc.creatorcircle.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.Post
import com.cc.creatorcircle.data.repository.PostsRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostsViewModel(private val context: Context) : ViewModel() {

    private val repository = PostsRepository(context)
    private val tokenManager = TokenManager(context)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchPosts()
    }

    fun fetchPosts(limit: Int = 50, offset: Int = 0) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = repository.getPosts(limit, offset)

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
}
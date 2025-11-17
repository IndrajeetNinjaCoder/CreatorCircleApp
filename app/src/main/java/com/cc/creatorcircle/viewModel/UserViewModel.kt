package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.SocialMediaResponse
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.data.repository.PlatformFollower
import com.cc.creatorcircle.data.repository.PlatformFollowers
import com.cc.creatorcircle.data.repository.SocialMediaLink
import com.cc.creatorcircle.data.repository.SocialMediaLinks
import com.cc.creatorcircle.data.repository.UserRepository
import com.cc.creatorcircle.data.repository.UserUpdateRequest
import com.cc.creatorcircle.utils.TokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File


sealed class UserUpdateState {
    object Idle : UserUpdateState()
    object Loading : UserUpdateState()
    data class Success(val response: SocialMediaResponse) : UserUpdateState()
    data class Error(val message: String) : UserUpdateState()
}

class UserViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoutUiState())
    val uiState: StateFlow<LogoutUiState> = _uiState.asStateFlow()

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    private val repository = UserRepository(context)
    private val gson = Gson()

    // Google Sign-In client - lazy initialization
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // User profile state
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // User update state
    private val _updateState = MutableStateFlow<UserUpdateState>(UserUpdateState.Idle)
    val updateState: StateFlow<UserUpdateState> = _updateState.asStateFlow()

    // Profile picture file
    private val _selectedProfilePic = MutableStateFlow<File?>(null)
    val selectedProfilePic: StateFlow<File?> = _selectedProfilePic.asStateFlow()

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Starting fetchUserProfile")
                _isLoading.value = true
                _error.value = null

                val response = repository.getUserProfile()
                Log.d("UserViewModel", "Repository response received")

                if (response.isSuccessful) {
                    response.body()?.let { userProfile ->
                        Log.d("UserViewModel", "User profile data: $userProfile")
                        _userProfile.value = userProfile
                        Log.d("UserViewModel", "Profile data set successfully")
                    } ?: run {
                        Log.e("UserViewModel", "Response body is null")
                        _error.value = "Empty response body"
                    }
                } else {
                    Log.e("UserViewModel", "API call failed: ${response.code()} - ${response.message()}")
                    _error.value = "Failed to fetch user profile: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Exception in fetchUserProfile", e)
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Updated method with structured data support
    fun updateUser(
        fullName: String? = null,
        password: String? = null,
        mobileNumber: String? = null,
        platformFollowers: PlatformFollowers? = null,
        username: String? = null,
        socialMediaLinks: SocialMediaLinks? = null,
        onboardingStatus: Boolean? = null,
        categories: List<String>? = null,
        bio: String? = null,
        age: Int? = null
    ) {
        viewModelScope.launch {
            _updateState.value = UserUpdateState.Loading

            // Convert categories list to JSON string
            val categoriesJson = categories?.takeIf { it.isNotEmpty() }?.let {
                gson.toJson(it)
            }

            val updateRequest = UserUpdateRequest(
                fullName = fullName?.takeIf { it.isNotBlank() },
                password = password?.takeIf { it.isNotBlank() },
                mobileNumber = mobileNumber?.takeIf { it.isNotBlank() },
                platformFollowers = platformFollowers,
                username = username?.takeIf { it.isNotBlank() },
                socialMediaLinks = socialMediaLinks,
                onboardingStatus = onboardingStatus,
                categories = categoriesJson,
                bio = bio?.takeIf { it.isNotBlank() },
                age = age?.takeIf { it > 0 },
                profilePicFile = _selectedProfilePic.value
            )

            repository.updateUser(updateRequest)
                .onSuccess { response ->
                    _updateState.value = UserUpdateState.Success(response)
                    // Refresh user profile after successful update
                    fetchUserProfile()
                    // Clear selected profile pic after successful update
                    _selectedProfilePic.value = null
                }
                .onFailure { exception ->
                    _updateState.value = UserUpdateState.Error(
                        exception.message ?: "Failed to update user"
                    )
                }
        }
    }


    // Convenience method for updating social media followers
    fun updatePlatformFollowers(
        instagramFollowers: String? = null,
        youtubeFollowers: String? = null,
        tiktokFollowers: String? = null,
        twitterFollowers: String? = null,
        facebookFollowers: String? = null
    ) {
        val platformFollowers = PlatformFollowers(
            instagram = instagramFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            youtube = youtubeFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            tiktok = tiktokFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            twitter = twitterFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            facebook = facebookFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            }
        )

        updateUser(platformFollowers = platformFollowers)
    }

    // Convenience method for updating social media links
    fun updateSocialMediaLinks(
        instagramLink: String? = null,
        youtubeLink: String? = null,
        tiktokLink: String? = null,
        twitterLink: String? = null,
        facebookLink: String? = null
    ) {
        val socialMediaLinks = SocialMediaLinks(
            instagram = instagramLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            youtube = youtubeLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            tiktok = tiktokLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            twitter = twitterLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            facebook = facebookLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            }
        )

        updateUser(socialMediaLinks = socialMediaLinks, onboardingStatus = false)
    }

    // Combined convenience method for updating both followers and links
    fun updateSocialMediaData(
        instagramFollowers: String? = null,
        instagramLink: String? = null,
        youtubeFollowers: String? = null,
        youtubeLink: String? = null,
        tiktokFollowers: String? = null,
        tiktokLink: String? = null,
        twitterFollowers: String? = null,
        twitterLink: String? = null,
        facebookFollowers: String? = null,
        facebookLink: String? = null
    ) {
        val platformFollowers = PlatformFollowers(
            instagram = instagramFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            youtube = youtubeFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            tiktok = tiktokFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            twitter = twitterFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            },
            facebook = facebookFollowers?.let {
                listOf(PlatformFollower(followers = it, is_primary = true))
            }
        )

        val socialMediaLinks = SocialMediaLinks(
            instagram = instagramLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            youtube = youtubeLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            tiktok = tiktokLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            twitter = twitterLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            },
            facebook = facebookLink?.let {
                listOf(SocialMediaLink(link = it, is_primary = true))
            }
        )

        updateUser(
            platformFollowers = platformFollowers,
            socialMediaLinks = socialMediaLinks
        )
    }

    // Backward compatibility method for string-based platform followers and social media links
    @Deprecated("Use updateUser with structured data or convenience methods instead")
    fun updateUserLegacy(
        fullName: String? = null,
        password: String? = null,
        mobileNumber: String? = null,
        platformFollowers: String? = null,
        username: String? = null,
        socialMediaLinks: String? = null,
        onboardingStatus: String? = null,
        categories: List<String>? = null,
        bio: String? = null,
        age: Int? = null
    ) {
        // This method is kept for backward compatibility
        // You might want to parse the JSON strings and convert them to structured data
        // For now, it will log a warning
        Log.w("UserViewModel", "Using deprecated updateUserLegacy method. Consider using structured data methods.")
    }

    fun setProfilePic(file: File) {
        _selectedProfilePic.value = file
    }

    fun removeProfilePic() {
        _selectedProfilePic.value = null
    }

//    fun clearError() {
//        _error.value = null
//    }

    fun clearUpdateError() {
        if (_updateState.value is UserUpdateState.Error) {
            _updateState.value = UserUpdateState.Idle
        }
    }

    fun resetUpdateState() {
        _updateState.value = UserUpdateState.Idle
    }

    fun clearUserData() {
        _userProfile.value = null
        _error.value = null
        _updateState.value = UserUpdateState.Idle
        _selectedProfilePic.value = null
    }



    /**
     * Performs complete logout including:
     * 1. API logout call
     * 2. Google Sign-Out
     * 3. Clear local tokens and preferences
     * 4. Clear user data from ViewModel
     */
    fun logout(performGoogleSignOut: Boolean = true) {
        val token = tokenManager.getToken()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Step 1: Call API logout
                val response = apiService.logout("Bearer $token")

                if (response.isSuccessful) {
                    Log.d("Logout", "API logout successful: ${response.body()}")

                    // Step 2: Google Sign-Out (if requested)
                    if (performGoogleSignOut) {
                        try {
                            googleSignInClient.signOut().await()
                            Log.d("Logout", "Google sign-out successful")
                        } catch (e: Exception) {
                            Log.e("Logout", "Google sign-out failed: ${e.message}", e)
                            // Don't fail the entire logout process if Google sign-out fails
                        }
                    }

                    // Step 3: Clear local data
                    clearAllLocalData()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedOut = true,
                        message = response.body()?.message ?: "Logout successful"
                    )
                } else {
                    Log.e("Logout", "API logout failed: ${response.code()}")

                    // Even if API logout fails, still clear local data for security
                    if (performGoogleSignOut) {
                        try {
                            googleSignInClient.signOut().await()
                            Log.d("Logout", "Google sign-out successful (fallback)")
                        } catch (e: Exception) {
                            Log.e("Logout", "Google sign-out failed (fallback): ${e.message}", e)
                        }
                    }

                    clearAllLocalData()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedOut = true, // Still mark as logged out locally
                        error = "Logout failed: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("Logout", "Logout error: ${e.message}", e)

                // Fallback: still clear local data even if network call fails
                if (performGoogleSignOut) {
                    try {
                        googleSignInClient.signOut().await()
                        Log.d("Logout", "Google sign-out successful (exception fallback)")
                    } catch (googleError: Exception) {
                        Log.e("Logout", "Google sign-out failed (exception fallback): ${googleError.message}", googleError)
                    }
                }

                clearAllLocalData()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedOut = true, // Still mark as logged out locally
                    error = "Network error: ${e.message}"
                )
            }
        }
    }

    /**
     * Performs Google Sign-Out only (without API logout)
     * Useful for specific scenarios where you only want to clear Google session
     */
    suspend fun performGoogleSignOut(): Result<Unit> {
        return try {
            googleSignInClient.signOut().await()
            Log.d("GoogleSignOut", "Google sign-out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GoogleSignOut", "Google sign-out failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Revokes Google access completely (more aggressive than sign-out)
     * This will require user to grant permissions again on next login
     */
    suspend fun revokeGoogleAccess(): Result<Unit> {
        return try {
            googleSignInClient.revokeAccess().await()
            Log.d("GoogleRevoke", "Google access revoked successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("GoogleRevoke", "Google access revocation failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Clears all local data including tokens, preferences, and ViewModel state
     */
    private fun clearAllLocalData() {
        // Clear token from TokenManager
        tokenManager.clearTokens()

        // Clear SharedPreferences
        val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", false)
            remove("access_token")
            apply()
        }

        // Clear ViewModel state
        clearUserData()

        Log.d("Logout", "All local data cleared")
    }




    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = LogoutUiState()
    }

    data class LogoutUiState(
        val isLoading: Boolean = false,
        val isLoggedOut: Boolean = false,
        val message: String? = null,
        val error: String? = null
    )
}

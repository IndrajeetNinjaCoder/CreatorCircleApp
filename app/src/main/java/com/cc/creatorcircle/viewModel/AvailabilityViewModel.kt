package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.AvailabilityResponse
import com.cc.creatorcircle.data.repository.AvailabilityRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvailabilityViewModel(private val context: Context) : ViewModel() {

    private val repository = AvailabilityRepository(context)
    private val tokenManager = TokenManager(context)

    // Mentor Availability State
    private val _mentorAvailability = MutableStateFlow<AvailabilityResponse?>(null)
    val mentorAvailability: StateFlow<AvailabilityResponse?> = _mentorAvailability.asStateFlow()

    private val _availabilityLoading = MutableStateFlow(false)
    val availabilityLoading: StateFlow<Boolean> = _availabilityLoading.asStateFlow()

    private val _availabilityError = MutableStateFlow<String?>(null)
    val availabilityError: StateFlow<String?> = _availabilityError.asStateFlow()

    /**
     * Fetch mentor availability by user ID
     */
    fun fetchMentorAvailability(userId: Int) {
        viewModelScope.launch {
            try {
                Log.d("AvailabilityViewModel", "Fetching mentor availability for userId: $userId")
                _availabilityLoading.value = true
                _availabilityError.value = null

                repository.getMentorAvailability(userId)
                    .onSuccess { availabilityResponse ->
                        Log.d("AvailabilityViewModel", "Mentor availability fetched successfully: $availabilityResponse")
                        _mentorAvailability.value = availabilityResponse
                    }
                    .onFailure { exception ->
                        Log.e("AvailabilityViewModel", "Failed to fetch mentor availability", exception)
                        _availabilityError.value = exception.message ?: "Failed to fetch mentor availability"
                    }
            } catch (e: Exception) {
                Log.e("AvailabilityViewModel", "Exception in fetchMentorAvailability", e)
                _availabilityError.value = "Network error: ${e.message}"
            } finally {
                _availabilityLoading.value = false
            }
        }
    }

    /**
     * Refresh mentor availability
     */
    fun refreshMentorAvailability(userId: Int) {
        fetchMentorAvailability(userId)
    }

    /**
     * Clear mentor availability data
     */
    fun clearMentorAvailability() {
        _mentorAvailability.value = null
        _availabilityError.value = null
    }

    /**
     * Clear availability error
     */
    fun clearAvailabilityError() {
        _availabilityError.value = null
    }

    /**
     * Check if user has valid token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Get current mentor availability
     */
    fun getCurrentMentorAvailability(): AvailabilityResponse? {
        return _mentorAvailability.value
    }
}
package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.LiveSessionAvailabilityResponse
import com.cc.creatorcircle.data.repository.LiveSessionAvailabilityRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveSessionAvailabilityViewModel(private val context: Context) : ViewModel() {

    private val repository = LiveSessionAvailabilityRepository(context)
    private val tokenManager = TokenManager(context)

    // Live Session Availability State
    private val _liveSessionAvailability = MutableStateFlow<LiveSessionAvailabilityResponse?>(null)
    val liveSessionAvailability: StateFlow<LiveSessionAvailabilityResponse?> = _liveSessionAvailability.asStateFlow()

    private val _availabilityLoading = MutableStateFlow(false)
    val availabilityLoading: StateFlow<Boolean> = _availabilityLoading.asStateFlow()

    private val _availabilityError = MutableStateFlow<String?>(null)
    val availabilityError: StateFlow<String?> = _availabilityError.asStateFlow()

    /**
     * Fetch live session availability by session ID
     */
    fun fetchLiveSessionAvailability(sessionId: Int) {
        viewModelScope.launch {
            try {
                Log.d("LiveSessionAvailabilityViewModel", "Fetching live session availability for sessionId: $sessionId")
                _availabilityLoading.value = true
                _availabilityError.value = null

                repository.getLiveSessionAvailability(sessionId)
                    .onSuccess { availabilityResponse ->
                        Log.d("LiveSessionAvailabilityViewModel", "Live session availability fetched successfully: $availabilityResponse")
                        _liveSessionAvailability.value = availabilityResponse
                    }
                    .onFailure { exception ->
                        Log.e("LiveSessionAvailabilityViewModel", "Failed to fetch live session availability", exception)
                        _availabilityError.value = exception.message ?: "Failed to fetch live session availability"
                    }
            } catch (e: Exception) {
                Log.e("LiveSessionAvailabilityViewModel", "Exception in fetchLiveSessionAvailability", e)
                _availabilityError.value = "Network error: ${e.message}"
            } finally {
                _availabilityLoading.value = false
            }
        }
    }

    /**
     * Refresh live session availability
     */
    fun refreshLiveSessionAvailability(sessionId: Int) {
        fetchLiveSessionAvailability(sessionId)
    }

    /**
     * Clear live session availability data
     */
    fun clearLiveSessionAvailability() {
        _liveSessionAvailability.value = null
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
     * Get current live session availability
     */
    fun getCurrentLiveSessionAvailability(): LiveSessionAvailabilityResponse? {
        return _liveSessionAvailability.value
    }

    /**
     * Get service slots from current availability
     */
    fun getServiceSlots() = _liveSessionAvailability.value?.serviceSlots

    /**
     * Get available time slots for a specific date and duration
     */
    fun getTimeSlotsForDateAndDuration(date: String, duration: String) =
        _liveSessionAvailability.value?.availableTimeSlots?.get(date)?.get(duration)

    /**
     * Get all available dates
     */
    fun getAvailableDates(): List<String> {
        return _liveSessionAvailability.value?.availableTimeSlots?.keys?.toList() ?: emptyList()
    }

    /**
     * Get available durations for a specific date
     */
    fun getAvailableDurationsForDate(date: String): List<String> {
        return _liveSessionAvailability.value?.availableTimeSlots?.get(date)?.keys?.toList() ?: emptyList()
    }

    /**
     * Get configuration details
     */
    fun getConfiguration() = _liveSessionAvailability.value?.configuration
}
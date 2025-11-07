package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.BookSlotResponse
import com.cc.creatorcircle.data.models.CancelBookingResponse
import com.cc.creatorcircle.data.models.Influencers
import com.cc.creatorcircle.data.repository.BookingRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val context: Context) : ViewModel() {

    private val repository = BookingRepository(context)
    private val tokenManager = TokenManager(context)

    // Suggested Influencers State
    private val _suggestedInfluencers = MutableStateFlow<List<Influencers>>(emptyList())
    val suggestedInfluencers: StateFlow<List<Influencers>> = _suggestedInfluencers.asStateFlow()

    private val _influencersLoading = MutableStateFlow(false)
    val influencersLoading: StateFlow<Boolean> = _influencersLoading.asStateFlow()

    private val _influencersError = MutableStateFlow<String?>(null)
    val influencersError: StateFlow<String?> = _influencersError.asStateFlow()

    // Booking State
    private val _bookingLoading = MutableStateFlow(false)
    val bookingLoading: StateFlow<Boolean> = _bookingLoading.asStateFlow()

    private val _bookingError = MutableStateFlow<String?>(null)
    val bookingError: StateFlow<String?> = _bookingError.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<BookSlotResponse?>(null)
    val bookingSuccess: StateFlow<BookSlotResponse?> = _bookingSuccess.asStateFlow()

    // Cancel Booking State
    private val _cancelLoading = MutableStateFlow(false)
    val cancelLoading: StateFlow<Boolean> = _cancelLoading.asStateFlow()

    private val _cancelError = MutableStateFlow<String?>(null)
    val cancelError: StateFlow<String?> = _cancelError.asStateFlow()

    private val _cancelSuccess = MutableStateFlow<CancelBookingResponse?>(null)
    val cancelSuccess: StateFlow<CancelBookingResponse?> = _cancelSuccess.asStateFlow()

    /**
     * Fetch suggested influencers for live sessions
     */
    fun fetchSuggestedInfluencers() {
        viewModelScope.launch {
            try {
                Log.d("BookingViewModel", "Starting fetchSuggestedInfluencers")
                _influencersLoading.value = true
                _influencersError.value = null

                repository.getSuggestedInfluencers()
                    .onSuccess { influencers ->
                        Log.d("BookingViewModel", "Suggested influencers fetched: ${influencers.size}")
                        _suggestedInfluencers.value = influencers
                    }
                    .onFailure { exception ->
                        Log.e("BookingViewModel", "Failed to fetch suggested influencers", exception)
                        _influencersError.value = exception.message ?: "Failed to fetch suggested influencers"
                    }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Exception in fetchSuggestedInfluencers", e)
                _influencersError.value = "Network error: ${e.message}"
            } finally {
                _influencersLoading.value = false
            }
        }
    }

    /**
     * Book a live session slot
     */
    fun bookLiveSessionSlot(
        mentorUserId: Int,
        timeSlotId: Int,
        topic: String,
        startTime: String,
        endTime: String,
        description: String,
        serviceSlotId: Int,
        seekerEmail: String,
        name: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("BookingViewModel", "Booking slot - Mentor: $mentorUserId, TimeSlot: $timeSlotId")
                _bookingLoading.value = true
                _bookingError.value = null
                _bookingSuccess.value = null

                repository.bookLiveSessionSlot(
                    mentorUserId = mentorUserId,
                    timeSlotId = timeSlotId,
                    topic = topic,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    serviceSlotId = serviceSlotId,
                    seekerEmail = seekerEmail,
                    name = name
                )
                    .onSuccess { bookingResponse ->
                        Log.d("BookingViewModel", "Slot booked successfully - ID: ${bookingResponse.id}")
                        Log.d("BookingViewModel", "Meeting link: ${bookingResponse.googleMeetLink}")
                        _bookingSuccess.value = bookingResponse
                    }
                    .onFailure { exception ->
                        Log.e("BookingViewModel", "Failed to book slot", exception)
                        _bookingError.value = exception.message ?: "Failed to book slot"
                    }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Exception in bookLiveSessionSlot", e)
                _bookingError.value = "Network error: ${e.message}"
            } finally {
                _bookingLoading.value = false
            }
        }
    }

    /**
     * Cancel a booking
     */
    fun cancelBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BookingViewModel", "Cancelling booking - ID: $bookingId")
                _cancelLoading.value = true
                _cancelError.value = null
                _cancelSuccess.value = null

                repository.cancelBooking(bookingId)
                    .onSuccess { cancelResponse ->
                        Log.d("BookingViewModel", "Booking cancelled successfully - ID: $bookingId")
                        Log.d("BookingViewModel", "Status: ${cancelResponse.bookingStatus}, Refunded: ${cancelResponse.refundedPayments}")
                        Log.d("BookingViewModel", "Cancelled by: ${cancelResponse.cancelledBy} (${cancelResponse.cancelledByRole})")
                        _cancelSuccess.value = cancelResponse
                    }
                    .onFailure { exception ->
                        Log.e("BookingViewModel", "Failed to cancel booking", exception)
                        _cancelError.value = exception.message ?: "Failed to cancel booking"
                    }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Exception in cancelBooking", e)
                _cancelError.value = "Network error: ${e.message}"
            } finally {
                _cancelLoading.value = false
            }
        }
    }

    /**
     * Refresh suggested influencers
     */
    fun refreshSuggestedInfluencers() {
        fetchSuggestedInfluencers()
    }

    /**
     * Clear booking state after successful booking
     */
    fun clearBookingState() {
        _bookingSuccess.value = null
        _bookingError.value = null
    }

    /**
     * Clear cancel booking state
     */
    fun clearCancelState() {
        _cancelSuccess.value = null
        _cancelError.value = null
    }

    /**
     * Clear influencers error
     */
    fun clearInfluencersError() {
        _influencersError.value = null
    }

    /**
     * Clear all errors
     */
    fun clearErrors() {
        _influencersError.value = null
        _bookingError.value = null
        _cancelError.value = null
    }

    /**
     * Clear all booking data
     */
    fun clearAllBookingData() {
        _suggestedInfluencers.value = emptyList()
        _bookingSuccess.value = null
        _cancelSuccess.value = null
        clearErrors()
    }

    /**
     * Check if user has active token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Get current booking response
     */
    fun getCurrentBooking(): BookSlotResponse? {
        return _bookingSuccess.value
    }

    /**
     * Get current cancel response
     */
    fun getCurrentCancelResponse(): CancelBookingResponse? {
        return _cancelSuccess.value
    }

    /**
     * Get suggested influencers list
     */
    fun getSuggestedInfluencersList(): List<Influencers> {
        return _suggestedInfluencers.value
    }

    /**
     * Check if booking is being cancelled
     */
    fun isCancelling(): Boolean {
        return _cancelLoading.value
    }

    /**
     * Check if there's a cancel error
     */
    fun hasCancelError(): Boolean {
        return _cancelError.value != null
    }

    /**
     * Check if cancellation was successful
     */
    fun isCancellationSuccessful(): Boolean {
        return _cancelSuccess.value != null
    }
}


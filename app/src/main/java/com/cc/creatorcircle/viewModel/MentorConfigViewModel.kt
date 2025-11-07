package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.GuidanceConfigurationState
import com.cc.creatorcircle.data.models.MentorConfigurationResponse
import com.cc.creatorcircle.data.models.UpdateConfigurationState
import com.cc.creatorcircle.data.models.UpdateMentorConfigurationRequest
import com.cc.creatorcircle.data.repository.MentorConfigRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MentorConfigViewModel(private val context: Context) : ViewModel() {

    private val repository = MentorConfigRepository(context)
    private val tokenManager = TokenManager(context)

    // Configuration State
    private val _configuration = MutableStateFlow<MentorConfigurationResponse?>(null)
    val configuration: StateFlow<MentorConfigurationResponse?> = _configuration.asStateFlow()

    private val _configurationLoading = MutableStateFlow(false)
    val configurationLoading: StateFlow<Boolean> = _configurationLoading.asStateFlow()

    private val _configurationError = MutableStateFlow<String?>(null)
    val configurationError: StateFlow<String?> = _configurationError.asStateFlow()

    // Alternative: Using GuidanceConfigurationState sealed class
    private val _configurationState = MutableStateFlow<GuidanceConfigurationState>(GuidanceConfigurationState.Idle)
    val configurationState: StateFlow<GuidanceConfigurationState> = _configurationState.asStateFlow()

    // Update Configuration State
    private val _updateLoading = MutableStateFlow(false)
    val updateLoading: StateFlow<Boolean> = _updateLoading.asStateFlow()

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError.asStateFlow()

    private val _updateSuccess = MutableStateFlow<String?>(null)
    val updateSuccess: StateFlow<String?> = _updateSuccess.asStateFlow()

    // Alternative: Using UpdateConfigurationState sealed class
    private val _updateState = MutableStateFlow<UpdateConfigurationState>(UpdateConfigurationState.Idle)
    val updateState: StateFlow<UpdateConfigurationState> = _updateState.asStateFlow()

    /**
     * Fetch mentor guidance configuration
     */
    fun fetchConfiguration() {
        viewModelScope.launch {
            try {
                Log.d("MentorConfigViewModel", "Fetching guidance configuration")

                _configurationLoading.value = true
                _configurationError.value = null
                _configurationState.value = GuidanceConfigurationState.Loading

                repository.getGuidanceConfiguration()
                    .onSuccess { configResponse ->
                        Log.d("MentorConfigViewModel", "Configuration fetched successfully: ID=${configResponse.configurationId}")
                        _configuration.value = configResponse
                        _configurationState.value = GuidanceConfigurationState.Success(configResponse)
                    }
                    .onFailure { exception ->
                        Log.e("MentorConfigViewModel", "Failed to fetch configuration", exception)
                        val errorMessage = exception.message ?: "Failed to fetch configuration"
                        _configurationError.value = errorMessage
                        _configurationState.value = GuidanceConfigurationState.Error(errorMessage)
                    }
            } catch (e: Exception) {
                Log.e("MentorConfigViewModel", "Exception in fetchConfiguration", e)
                val errorMessage = "Network error: ${e.message}"
                _configurationError.value = errorMessage
                _configurationState.value = GuidanceConfigurationState.Error(errorMessage)
            } finally {
                _configurationLoading.value = false
            }
        }
    }

    /**
     * Update mentor guidance configuration
     */
    fun updateConfiguration(configId: Int, request: UpdateMentorConfigurationRequest) {
        viewModelScope.launch {
            try {
                Log.d("MentorConfigViewModel", "Updating guidance configuration ID: $configId")

                _updateLoading.value = true
                _updateError.value = null
                _updateSuccess.value = null
                _updateState.value = UpdateConfigurationState.Loading

                repository.updateGuidanceConfiguration(configId, request)
                    .onSuccess { updateResponse ->
                        Log.d("MentorConfigViewModel", "Configuration updated successfully: ${updateResponse.message}")
                        _updateSuccess.value = updateResponse.message
                        _updateState.value = UpdateConfigurationState.Success(updateResponse.message)

                        // Refresh configuration after successful update
                        fetchConfiguration()
                    }
                    .onFailure { exception ->
                        Log.e("MentorConfigViewModel", "Failed to update configuration", exception)
                        val errorMessage = exception.message ?: "Failed to update configuration"
                        _updateError.value = errorMessage
                        _updateState.value = UpdateConfigurationState.Error(errorMessage)
                    }
            } catch (e: Exception) {
                Log.e("MentorConfigViewModel", "Exception in updateConfiguration", e)
                val errorMessage = "Network error: ${e.message}"
                _updateError.value = errorMessage
                _updateState.value = UpdateConfigurationState.Error(errorMessage)
            } finally {
                _updateLoading.value = false
            }
        }
    }

    /**
     * Refresh configuration
     */
    fun refreshConfiguration() {
        fetchConfiguration()
    }

    /**
     * Get current configuration
     */
    fun getCurrentConfiguration(): MentorConfigurationResponse? {
        return _configuration.value
    }

    /**
     * Get price per hour from configuration
     */
    fun getPricePerHour(): Double? {
        return _configuration.value?.serviceSlotConfig?.pricePerHour
    }

    /**
     * Get allowed durations from configuration
     */
    fun getAllowedDurations(): List<Int>? {
        return _configuration.value?.serviceSlotConfig?.allowedDurations
    }

    /**
     * Get max advance days for scheduling
     */
    fun getMaxAdvanceDays(): Int? {
        return _configuration.value?.schedulingWindow?.maxAdvanceDays
    }

    /**
     * Get min advance hours for scheduling
     */
    fun getMinAdvanceHours(): Int? {
        return _configuration.value?.schedulingWindow?.minAdvanceHours
    }

    /**
     * Get max bookings per day
     */
    fun getMaxBookingsPerDay(): Int? {
        return _configuration.value?.schedulingWindow?.maxBookingsPerDay
    }

    /**
     * Check if payment is required upfront
     */
    fun isPaymentUpfrontRequired(): Boolean {
        return _configuration.value?.bookingForm?.collectPaymentUpfront ?: false
    }

    /**
     * Check if auto-confirm is enabled
     */
    fun isAutoConfirmEnabled(): Boolean {
        return _configuration.value?.bookingConfirmation?.autoConfirm ?: false
    }

    /**
     * Check if approval is required
     */
    fun isApprovalRequired(): Boolean {
        return _configuration.value?.bookingConfirmation?.requireApproval ?: false
    }

    /**
     * Check if guests can reschedule
     */
    fun canGuestsReschedule(): Boolean {
        return _configuration.value?.guestPermissions?.canReschedule ?: false
    }

    /**
     * Check if guests can cancel
     */
    fun canGuestsCancel(): Boolean {
        return _configuration.value?.guestPermissions?.canCancel ?: false
    }

    /**
     * Get reschedule limit hours
     */
    fun getRescheduleLimitHours(): Int? {
        return _configuration.value?.guestPermissions?.rescheduleLimitHours
    }

    /**
     * Get cancel limit hours
     */
    fun getCancelLimitHours(): Int? {
        return _configuration.value?.guestPermissions?.cancelLimitHours
    }

    /**
     * Get custom message for booking form
     */
    fun getCustomMessage(): String? {
        return _configuration.value?.bookingForm?.customMessage
    }

    /**
     * Get reminder timing in hours
     */
    fun getReminderTiming(): List<Int>? {
        return _configuration.value?.bookingConfirmation?.reminderTiming
    }

    /**
     * Clear configuration data
     */
    fun clearConfiguration() {
        _configuration.value = null
        _configurationError.value = null
        _configurationState.value = GuidanceConfigurationState.Idle
    }

    /**
     * Clear update state
     */
    fun clearUpdateState() {
        _updateError.value = null
        _updateSuccess.value = null
        _updateState.value = UpdateConfigurationState.Idle
    }

    /**
     * Clear errors
     */
    fun clearErrors() {
        _configurationError.value = null
        _updateError.value = null
    }

    /**
     * Check if user has active token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Check if configuration is loaded
     */
    fun hasConfiguration(): Boolean {
        return _configuration.value != null
    }

    /**
     * Check if configuration is currently loading
     */
    fun isLoading(): Boolean {
        return _configurationLoading.value
    }

    /**
     * Check if update is in progress
     */
    fun isUpdateInProgress(): Boolean {
        return _updateLoading.value
    }

    /**
     * Check if there's an error
     */
    fun hasError(): Boolean {
        return _configurationError.value != null || _updateError.value != null
    }

    /**
     * Get current error message
     */
    fun getErrorMessage(): String? {
        return _configurationError.value ?: _updateError.value
    }

    /**
     * Check if update was successful
     */
    fun isUpdateSuccessful(): Boolean {
        return _updateSuccess.value != null
    }

    /**
     * Get update success message
     */
    fun getUpdateSuccessMessage(): String? {
        return _updateSuccess.value
    }
}











//package com.cc.creatorcircle.viewModel
//
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cc.creatorcircle.data.models.GuidanceConfigurationState
//import com.cc.creatorcircle.data.models.MentorConfigurationResponse
//import com.cc.creatorcircle.data.repository.MentorConfigRepository
//import com.cc.creatorcircle.utils.TokenManager
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//class MentorConfigViewModel(private val context: Context) : ViewModel() {
//
//    private val repository = MentorConfigRepository(context)
//    private val tokenManager = TokenManager(context)
//
//    // Configuration State
//    private val _configuration = MutableStateFlow<MentorConfigurationResponse?>(null)
//    val configuration: StateFlow<MentorConfigurationResponse?> = _configuration.asStateFlow()
//
//    private val _configurationLoading = MutableStateFlow(false)
//    val configurationLoading: StateFlow<Boolean> = _configurationLoading.asStateFlow()
//
//    private val _configurationError = MutableStateFlow<String?>(null)
//    val configurationError: StateFlow<String?> = _configurationError.asStateFlow()
//
//    // Alternative: Using GuidanceConfigurationState sealed class
//    private val _configurationState = MutableStateFlow<GuidanceConfigurationState>(GuidanceConfigurationState.Idle)
//    val configurationState: StateFlow<GuidanceConfigurationState> = _configurationState.asStateFlow()
//
//    /**
//     * Fetch mentor guidance configuration
//     */
//    fun fetchConfiguration() {
//        viewModelScope.launch {
//            try {
//                Log.d("MentorConfigViewModel", "Fetching guidance configuration")
//
//                _configurationLoading.value = true
//                _configurationError.value = null
//                _configurationState.value = GuidanceConfigurationState.Loading
//
//                repository.getGuidanceConfiguration()
//                    .onSuccess { configResponse ->
//                        Log.d("MentorConfigViewModel", "Configuration fetched successfully: ID=${configResponse.configurationId}")
//                        _configuration.value = configResponse
//                        _configurationState.value = GuidanceConfigurationState.Success(configResponse)
//                    }
//                    .onFailure { exception ->
//                        Log.e("MentorConfigViewModel", "Failed to fetch configuration", exception)
//                        val errorMessage = exception.message ?: "Failed to fetch configuration"
//                        _configurationError.value = errorMessage
//                        _configurationState.value = GuidanceConfigurationState.Error(errorMessage)
//                    }
//            } catch (e: Exception) {
//                Log.e("MentorConfigViewModel", "Exception in fetchConfiguration", e)
//                val errorMessage = "Network error: ${e.message}"
//                _configurationError.value = errorMessage
//                _configurationState.value = GuidanceConfigurationState.Error(errorMessage)
//            } finally {
//                _configurationLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Refresh configuration
//     */
//    fun refreshConfiguration() {
//        fetchConfiguration()
//    }
//
//    /**
//     * Get current configuration
//     */
//    fun getCurrentConfiguration(): MentorConfigurationResponse? {
//        return _configuration.value
//    }
//
//    /**
//     * Get price per hour from configuration
//     */
//    fun getPricePerHour(): Double? {
//        return _configuration.value?.serviceSlotConfig?.pricePerHour
//    }
//
//    /**
//     * Get allowed durations from configuration
//     */
//    fun getAllowedDurations(): List<Int>? {
//        return _configuration.value?.serviceSlotConfig?.allowedDurations
//    }
//
//    /**
//     * Get max advance days for scheduling
//     */
//    fun getMaxAdvanceDays(): Int? {
//        return _configuration.value?.schedulingWindow?.maxAdvanceDays
//    }
//
//    /**
//     * Get min advance hours for scheduling
//     */
//    fun getMinAdvanceHours(): Int? {
//        return _configuration.value?.schedulingWindow?.minAdvanceHours
//    }
//
//    /**
//     * Get max bookings per day
//     */
//    fun getMaxBookingsPerDay(): Int? {
//        return _configuration.value?.schedulingWindow?.maxBookingsPerDay
//    }
//
//    /**
//     * Check if payment is required upfront
//     */
//    fun isPaymentUpfrontRequired(): Boolean {
//        return _configuration.value?.bookingForm?.collectPaymentUpfront ?: false
//    }
//
//    /**
//     * Check if auto-confirm is enabled
//     */
//    fun isAutoConfirmEnabled(): Boolean {
//        return _configuration.value?.bookingConfirmation?.autoConfirm ?: false
//    }
//
//    /**
//     * Check if approval is required
//     */
//    fun isApprovalRequired(): Boolean {
//        return _configuration.value?.bookingConfirmation?.requireApproval ?: false
//    }
//
//    /**
//     * Check if guests can reschedule
//     */
//    fun canGuestsReschedule(): Boolean {
//        return _configuration.value?.guestPermissions?.canReschedule ?: false
//    }
//
//    /**
//     * Check if guests can cancel
//     */
//    fun canGuestsCancel(): Boolean {
//        return _configuration.value?.guestPermissions?.canCancel ?: false
//    }
//
//    /**
//     * Get reschedule limit hours
//     */
//    fun getRescheduleLimitHours(): Int? {
//        return _configuration.value?.guestPermissions?.rescheduleLimitHours
//    }
//
//    /**
//     * Get cancel limit hours
//     */
//    fun getCancelLimitHours(): Int? {
//        return _configuration.value?.guestPermissions?.cancelLimitHours
//    }
//
//    /**
//     * Get custom message for booking form
//     */
//    fun getCustomMessage(): String? {
//        return _configuration.value?.bookingForm?.customMessage
//    }
//
//    /**
//     * Get reminder timing in hours
//     */
//    fun getReminderTiming(): List<Int>? {
//        return _configuration.value?.bookingConfirmation?.reminderTiming
//    }
//
//    /**
//     * Clear configuration data
//     */
//    fun clearConfiguration() {
//        _configuration.value = null
//        _configurationError.value = null
//        _configurationState.value = GuidanceConfigurationState.Idle
//    }
//
//    /**
//     * Clear errors
//     */
//    fun clearErrors() {
//        _configurationError.value = null
//    }
//
//    /**
//     * Check if user has active token
//     */
//    fun hasValidToken(): Boolean {
//        return tokenManager.getToken().isNotEmpty()
//    }
//
//    /**
//     * Check if configuration is loaded
//     */
//    fun hasConfiguration(): Boolean {
//        return _configuration.value != null
//    }
//
//    /**
//     * Check if configuration is currently loading
//     */
//    fun isLoading(): Boolean {
//        return _configurationLoading.value
//    }
//
//    /**
//     * Check if there's an error
//     */
//    fun hasError(): Boolean {
//        return _configurationError.value != null
//    }
//
//    /**
//     * Get current error message
//     */
//    fun getErrorMessage(): String? {
//        return _configurationError.value
//    }
//}
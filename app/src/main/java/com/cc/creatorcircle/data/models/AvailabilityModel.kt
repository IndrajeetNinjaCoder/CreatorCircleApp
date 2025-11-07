package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

// Response Models
data class AvailabilityResponse(
    @SerializedName("service_slots") val serviceSlots: List<ServiceSlot>,
    @SerializedName("available_time_slots") val availableTimeSlots: Map<String, DurationSlots>,
    @SerializedName("configuration") val configuration: Configuration
)

data class ServiceSlot(
    @SerializedName("id") val id: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("is_active") val isActive: Boolean
)

data class DurationSlots(
    @SerializedName("15") val fifteenMin: List<AvailableTimeSlot>? = null,
    @SerializedName("30") val thirtyMin: List<AvailableTimeSlot>? = null,
    @SerializedName("45") val fortyFiveMin: List<AvailableTimeSlot>? = null
)

data class AvailableTimeSlot(
    @SerializedName("time_slot_id") val timeSlotId: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String
)

data class Configuration(
    @SerializedName("configuration_id") val configurationId: Int,
    @SerializedName("scheduling_window") val schedulingWindow: AvailableSchedulingWindow,
    @SerializedName("booking_form") val bookingForm: BookingForm,
    @SerializedName("guest_permissions") val guestPermissions: GuestPermissions,
    @SerializedName("booking_confirmation") val bookingConfirmation: BookingConfirmation
)

data class AvailableSchedulingWindow(
    @SerializedName("max_advance_days") val maxAdvanceDays: Int,
    @SerializedName("min_advance_hours") val minAdvanceHours: Int,
    @SerializedName("buffer_time") val bufferTime: Int,
    @SerializedName("max_bookings_per_day") val maxBookingsPerDay: Int
)

// State Management
sealed class AvailabilityState {
    object Idle : AvailabilityState()
    object Loading : AvailabilityState()
    data class Success(val response: AvailabilityResponse) : AvailabilityState()
    data class Error(val message: String) : AvailabilityState()
}
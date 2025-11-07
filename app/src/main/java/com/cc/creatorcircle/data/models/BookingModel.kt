package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

// Request Model
data class BookSlotRequest(
    @SerializedName("mentorUserId") val mentorUserId: Int,
    @SerializedName("timeSlotId") val timeSlotId: Int,
    @SerializedName("topic") val topic: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("description") val description: String,
    @SerializedName("serviceSlotId") val serviceSlotId: Int,
    @SerializedName("seeker_email") val seekerEmail: String,
    @SerializedName("name") val name: String
)

// Response Model
data class BookSlotResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("mentor_id") val mentorId: Int,
    @SerializedName("seeker_id") val seekerId: Int,
    @SerializedName("time_slot_id") val timeSlotId: Int,
    @SerializedName("service_id") val serviceId: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("is_paid") val isPaid: Boolean,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("description") val description: String,
    @SerializedName("google_meet_link") val googleMeetLink: String,
    @SerializedName("form_data") val formData: Map<String, Any>
)

// State Management (similar to PostCreationState pattern)
sealed class BookSlotState {
    object Idle : BookSlotState()
    object Loading : BookSlotState()
    data class Success(val response: BookSlotResponse) : BookSlotState()
    data class Error(val message: String) : BookSlotState()
}



// Response Model
data class CancelBookingResponse(
    @SerializedName("message") val message: String,
    @SerializedName("cancelled_by") val cancelledBy: String,
    @SerializedName("cancelled_by_role") val cancelledByRole: String,
    @SerializedName("refunded_payments") val refundedPayments: Int,
    @SerializedName("booking_status") val bookingStatus: String
)

// State Management
sealed class CancelBookingState {
    object Idle : CancelBookingState()
    object Loading : CancelBookingState()
    data class Success(val response: CancelBookingResponse) : CancelBookingState()
    data class Error(val message: String) : CancelBookingState()
}
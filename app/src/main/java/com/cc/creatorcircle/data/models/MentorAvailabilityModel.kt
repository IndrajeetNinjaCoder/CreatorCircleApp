package com.cc.creatorcircle.data.models


import com.google.gson.annotations.SerializedName

// Response Models
data class LiveSessionAvailabilityResponse(
    @SerializedName("service_slots") val serviceSlots: List<ServiceSlot>,
    @SerializedName("available_time_slots") val availableTimeSlots: Map<String, Map<String, List<TimeSlot>>>,
    @SerializedName("configuration") val configuration: Configuration
)

//data class ServiceSlot(
//    @SerializedName("id") val id: Int,
//    @SerializedName("duration") val duration: Int,
//    @SerializedName("price") val price: Double,
//    @SerializedName("is_active") val isActive: Boolean
//)
//
//data class TimeSlot(
//    @SerializedName("time_slot_id") val timeSlotId: Int,
//    @SerializedName("start_time") val startTime: String,
//    @SerializedName("end_time") val endTime: String
//)
//
//data class Configuration(
//    @SerializedName("configuration_id") val configurationId: Int,
//    @SerializedName("scheduling_window") val schedulingWindow: SchedulingWindow,
//    @SerializedName("booking_form") val bookingForm: BookingForm,
//    @SerializedName("guest_permissions") val guestPermissions: GuestPermissions,
//    @SerializedName("booking_confirmation") val bookingConfirmation: BookingConfirmation
//)
//
//data class SchedulingWindow(
//    @SerializedName("max_advance_days") val maxAdvanceDays: Int,
//    @SerializedName("min_advance_hours") val minAdvanceHours: Int,
//    @SerializedName("buffer_time") val bufferTime: Int,
//    @SerializedName("max_bookings_per_day") val maxBookingsPerDay: Int
//)

//data class BookingForm(
//    @SerializedName("custom_message") val customMessage: String,
//    @SerializedName("collect_payment_upfront") val collectPaymentUpfront: Boolean,
//    @SerializedName("fields") val fields: List<FormField>
//)
//
//data class FormField(
//    @SerializedName("field_name") val fieldName: String,
//    @SerializedName("field_type") val fieldType: String,
//    @SerializedName("is_required") val isRequired: Boolean,
//    @SerializedName("is_enabled") val isEnabled: Boolean,
//    @SerializedName("field_order") val fieldOrder: Int
//)

//data class GuestPermissions(
//    @SerializedName("can_invite_others") val canInviteOthers: Boolean,
//    @SerializedName("can_reschedule") val canReschedule: Boolean,
//    @SerializedName("can_cancel") val canCancel: Boolean,
//    @SerializedName("reschedule_limit_hours") val rescheduleLimitHours: Int,
//    @SerializedName("cancel_limit_hours") val cancelLimitHours: Int
//)
//
//data class BookingConfirmation(
//    @SerializedName("send_calendar_invitation") val sendCalendarInvitation: Boolean,
//    @SerializedName("send_email_confirmation") val sendEmailConfirmation: Boolean,
//    @SerializedName("send_sms_reminders") val sendSmsReminders: Boolean,
//    @SerializedName("reminder_timing") val reminderTiming: List<Int>,
//    @SerializedName("auto_confirm") val autoConfirm: Boolean,
//    @SerializedName("require_approval") val requireApproval: Boolean
//)

// State Management
sealed class LiveSessionAvailabilityState {
    object Idle : LiveSessionAvailabilityState()
    object Loading : LiveSessionAvailabilityState()
    data class Success(val response: LiveSessionAvailabilityResponse) : LiveSessionAvailabilityState()
    data class Error(val message: String) : LiveSessionAvailabilityState()
}
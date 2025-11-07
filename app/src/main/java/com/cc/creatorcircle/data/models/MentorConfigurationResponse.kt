package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

// Response Model
data class MentorConfigurationResponse(
    @SerializedName("configuration_id") val configurationId: Int,
    @SerializedName("service_slot_config") val serviceSlotConfig: ServiceSlotConfig,
    @SerializedName("scheduling_window") val schedulingWindow: SchedulingWindow,
    @SerializedName("booking_form") val bookingForm: BookingForm,
    @SerializedName("guest_permissions") val guestPermissions: GuestPermissions,
    @SerializedName("booking_confirmation") val bookingConfirmation: BookingConfirmation,
    @SerializedName("selected_time_slots") val selectedTimeSlots: List<Any>
)

// Update Request Model
data class UpdateMentorConfigurationRequest(
    @SerializedName("service_slot_config") val serviceSlotConfig: ServiceSlotConfig,
    @SerializedName("selected_time_slots") val selectedTimeSlots: List<SelectedTimeSlot>,
    @SerializedName("scheduling_window") val schedulingWindow: SchedulingWindow,
    @SerializedName("booking_form") val bookingForm: BookingForm,
    @SerializedName("guest_permissions") val guestPermissions: GuestPermissions,
    @SerializedName("booking_confirmation") val bookingConfirmation: BookingConfirmation
)

// Update Response Model
data class UpdateMentorConfigurationResponse(
    @SerializedName("message") val message: String
)

data class ServiceSlotConfig(
    @SerializedName("price_per_hour") val pricePerHour: Double,
    @SerializedName("allowed_durations") val allowedDurations: List<Int>
)

data class SelectedTimeSlot(
    @SerializedName("date") val date: String,
    @SerializedName("slots") val slots: List<TimeSlot>
)

data class TimeSlot(
    @SerializedName("id") val id: Int,
    @SerializedName("start_time") val startTime: String,
    @SerializedName("end_time") val endTime: String,
    @SerializedName("is_recurring") val isRecurring: Boolean,
    @SerializedName("recurring_pattern") val recurringPattern: Map<String, Any>? = null,
    @SerializedName("recurring_end_date") val recurringEndDate: String? = null
)

data class SchedulingWindow(
    @SerializedName("max_advance_days") val maxAdvanceDays: Int,
    @SerializedName("min_advance_hours") val minAdvanceHours: Int,
    @SerializedName("buffer_time") val bufferTime: Int,
    @SerializedName("max_bookings_per_day") val maxBookingsPerDay: Int
)

data class BookingForm(
    @SerializedName("fields") val fields: List<FormField>,
    @SerializedName("custom_message") val customMessage: String,
    @SerializedName("collect_payment_upfront") val collectPaymentUpfront: Boolean
)

data class FormField(
    @SerializedName("field_name") val fieldName: String,
    @SerializedName("field_type") val fieldType: String,
    @SerializedName("is_required") val isRequired: Boolean,
    @SerializedName("is_enabled") val isEnabled: Boolean,
    @SerializedName("field_order") val fieldOrder: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("required") val required: Boolean,
    @SerializedName("enabled") val enabled: Boolean
)

data class GuestPermissions(
    @SerializedName("can_invite_others") val canInviteOthers: Boolean,
    @SerializedName("can_reschedule") val canReschedule: Boolean,
    @SerializedName("can_cancel") val canCancel: Boolean,
    @SerializedName("reschedule_limit_hours") val rescheduleLimitHours: Int,
    @SerializedName("cancel_limit_hours") val cancelLimitHours: Int
)

data class BookingConfirmation(
    @SerializedName("send_calendar_invitation") val sendCalendarInvitation: Boolean,
    @SerializedName("send_email_confirmation") val sendEmailConfirmation: Boolean,
    @SerializedName("send_sms_reminders") val sendSmsReminders: Boolean,
    @SerializedName("reminder_timing") val reminderTiming: List<Int>,
    @SerializedName("auto_confirm") val autoConfirm: Boolean,
    @SerializedName("require_approval") val requireApproval: Boolean
)

// State Management
sealed class GuidanceConfigurationState {
    object Idle : GuidanceConfigurationState()
    object Loading : GuidanceConfigurationState()
    data class Success(val response: MentorConfigurationResponse) : GuidanceConfigurationState()
    data class Error(val message: String) : GuidanceConfigurationState()
}

// Update State Management
sealed class UpdateConfigurationState {
    object Idle : UpdateConfigurationState()
    object Loading : UpdateConfigurationState()
    data class Success(val message: String) : UpdateConfigurationState()
    data class Error(val message: String) : UpdateConfigurationState()
}

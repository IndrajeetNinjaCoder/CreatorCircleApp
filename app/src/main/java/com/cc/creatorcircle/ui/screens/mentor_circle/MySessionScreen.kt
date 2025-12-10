package com.cc.creatorcircle.ui.screens.mentor_circle

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.*
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.viewModel.MentorConfigViewModel
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySessionScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { MentorConfigViewModel(context) }
    val configState by viewModel.configurationState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val firebaseAnalytics = remember { Firebase.analytics }

    val userDataManager = remember { UserDataManager(context) }
    var userData by remember { mutableStateOf(UserData()) }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("MySessionScreen", "MySessionScreen")
        userData = userDataManager.getUserData()
    }

    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("session_config_load_started")
        viewModel.fetchConfiguration()
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateConfigurationState.Success -> {
                val message = (updateState as UpdateConfigurationState.Success).message
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                FirebaseAnalyticsHelper.logEvent(
                    "session_config_update_success",
                    mapOf("message" to message)
                )

                viewModel.clearUpdateState()
            }

            is UpdateConfigurationState.Error -> {
                val errorMessage = (updateState as UpdateConfigurationState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()

                FirebaseAnalyticsHelper.logEvent(
                    "session_config_update_error",
                    mapOf("error" to errorMessage)
                )
            }

            else -> {}
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
                .background(Color.White)
        ) {
            when (configState) {
                is GuidanceConfigurationState.Loading -> {
                    FirebaseAnalyticsHelper.logEvent("session_config_loading")
                    LoadingView()
                }
                is GuidanceConfigurationState.Success -> {
                    val config = (configState as GuidanceConfigurationState.Success).response
                    FirebaseAnalyticsHelper.logEvent(
                        "session_config_loaded",
                        mapOf("config_id" to config.configurationId.toString())
                    )
                    ServiceConfigurationContent(
                        userData = userData,
                        configuration = config,
                        viewModel = viewModel,
                        onRefresh = {
                            FirebaseAnalyticsHelper.logFeatureUsed("session_config_refresh")
                            viewModel.refreshConfiguration()
                        }
                    )
                }

                is GuidanceConfigurationState.Error -> {
                    val errorMessage = (configState as GuidanceConfigurationState.Error).message
                    FirebaseAnalyticsHelper.logEvent(
                        "session_config_error",
                        mapOf("error" to errorMessage)
                    )
                    ErrorView(
                        message = errorMessage,
                        onRetry = {
                            FirebaseAnalyticsHelper.logFeatureUsed("session_config_retry")
                            viewModel.refreshConfiguration()
                        }
                    )
                }

                is GuidanceConfigurationState.Idle -> EmptyConfigurationView()
            }
        }
    }
}




@Composable
fun ServiceConfigurationContent(
    userData: UserData,
    configuration: MentorConfigurationResponse,
    viewModel: MentorConfigViewModel,
    onRefresh: () -> Unit
) {

    var aboutText by remember { mutableStateOf(configuration.bookingForm.customMessage) }
    var isEditingAbout by remember { mutableStateOf(false) }

    // Service Slot Config
    var pricePerHour by remember { mutableStateOf(configuration.serviceSlotConfig.pricePerHour) }
    var allowedDurations by remember { mutableStateOf(configuration.serviceSlotConfig.allowedDurations.toMutableList()) }
    var isEditingService by remember { mutableStateOf(false) }

    // Date and Time Slots
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedTimeSlots by remember { mutableStateOf<MutableList<SelectedTimeSlot>>(mutableListOf()) }
    var isEditingDateSlots by remember { mutableStateOf(false) }

    // Time slot dialog
    var showTimeSlotDialog by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }

    // Scheduling Window
    var maxAdvanceDays by remember { mutableStateOf(configuration.schedulingWindow.maxAdvanceDays) }
    var minAdvanceHours by remember { mutableStateOf(configuration.schedulingWindow.minAdvanceHours) }
    var bufferTime by remember { mutableStateOf(configuration.schedulingWindow.bufferTime) }
    var maxBookingsPerDay by remember { mutableStateOf(configuration.schedulingWindow.maxBookingsPerDay) }
    var isEditingScheduling by remember { mutableStateOf(false) }

    // Booking Form
    var customMessage by remember { mutableStateOf(configuration.bookingForm.customMessage) }
    var collectPaymentUpfront by remember { mutableStateOf(configuration.bookingForm.collectPaymentUpfront) }
    var isEditingBookingForm by remember { mutableStateOf(false) }

    // Guest Permissions
    var canInviteOthers by remember { mutableStateOf(configuration.guestPermissions.canInviteOthers) }
    var canReschedule by remember { mutableStateOf(configuration.guestPermissions.canReschedule) }
    var canCancel by remember { mutableStateOf(configuration.guestPermissions.canCancel) }
    var rescheduleLimitHours by remember { mutableStateOf(configuration.guestPermissions.rescheduleLimitHours) }
    var cancelLimitHours by remember { mutableStateOf(configuration.guestPermissions.cancelLimitHours) }
    var isEditingGuestPermissions by remember { mutableStateOf(false) }

    // Booking Confirmation
    var sendCalendarInvitation by remember { mutableStateOf(configuration.bookingConfirmation.sendCalendarInvitation) }
    var sendEmailConfirmation by remember { mutableStateOf(configuration.bookingConfirmation.sendEmailConfirmation) }
    var sendSmsReminders by remember { mutableStateOf(configuration.bookingConfirmation.sendSmsReminders) }
    var autoConfirm by remember { mutableStateOf(configuration.bookingConfirmation.autoConfirm) }
    var requireApproval by remember { mutableStateOf(configuration.bookingConfirmation.requireApproval) }
    var isEditingConfirmation by remember { mutableStateOf(false) }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (userData.profilePic != null) {
                AsyncImage(
                    model = userData.profilePic,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile),
                    error = painterResource(id = R.drawable.ic_profile)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = userData.username, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "creator • Mentor", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // About Section - Editable
        EditableAboutSection(
            aboutText = aboutText,
            isEditing = isEditingAbout,
            onAboutTextChange = { aboutText = it },
            onEditClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("edit_about_section_clicked")
                FirebaseAnalyticsHelper.logEvent("about_section_edit_started")
                isEditingAbout = true
            },
            onSaveClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("save_about_section")
                FirebaseAnalyticsHelper.logEvent(
                    "about_section_saved",
                    mapOf("text_length" to aboutText.length.toString())
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = configuration.serviceSlotConfig,
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = configuration.schedulingWindow,
                    bookingForm = BookingForm(
                        fields = configuration.bookingForm.fields,
                        customMessage = aboutText,
                        collectPaymentUpfront = configuration.bookingForm.collectPaymentUpfront
                    ),
                    guestPermissions = configuration.guestPermissions,
                    bookingConfirmation = configuration.bookingConfirmation
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingAbout = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_about_section_edit")
                aboutText = ""
                isEditingAbout = false
            }
        )


        Spacer(modifier = Modifier.height(24.dp))




        EditableServiceSlotSection(
            pricePerHour = pricePerHour,
            allowedDurations = allowedDurations,
            onPriceChange = { pricePerHour = it },
            onDurationsChange = { allowedDurations = it },
            onSaveClick = { newPrice, newDurations ->
                FirebaseAnalyticsHelper.logFeatureUsed("save_service_slot_config")
                FirebaseAnalyticsHelper.logEvent(
                    "service_slot_config_saved",
                    mapOf(
                        "price_per_hour" to newPrice.toString(),
                        "duration_count" to newDurations.size.toString()
                    )
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(
                        pricePerHour = newPrice,
                        allowedDurations = newDurations
                    ),
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = SchedulingWindow(
                        maxAdvanceDays, minAdvanceHours, bufferTime, maxBookingsPerDay
                    ),
                    bookingForm = configuration.bookingForm,
                    guestPermissions = configuration.guestPermissions,
                    bookingConfirmation = configuration.bookingConfirmation
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingService = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_service_slot_edit")
                pricePerHour = configuration.serviceSlotConfig.pricePerHour
                allowedDurations = configuration.serviceSlotConfig.allowedDurations.toMutableList()
                isEditingService = false
            }
        )




        Spacer(modifier = Modifier.height(24.dp))

        // Date Selection & Time Slots - Fully Editable
        EditableDateTimeSlotSection(
            isEditing = isEditingDateSlots,
            selectedMonth = selectedMonth,
            selectedDate = selectedDate,
            selectedTimeSlots = selectedTimeSlots,
            onMonthChange = { selectedMonth = it },
            onDateSelect = {
                FirebaseAnalyticsHelper.logEvent(
                    "date_selected",
                    mapOf("date" to it.toString())
                )
                selectedDate = it
                if (isEditingDateSlots) {
                    showTimeSlotDialog = true
                }
            },
            onEditClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("edit_date_time_slots_clicked")
                FirebaseAnalyticsHelper.logEvent("date_time_slots_edit_started")
                isEditingDateSlots = true
            },
            onSaveClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("save_date_time_slots")
                FirebaseAnalyticsHelper.logEvent(
                    "date_time_slots_saved",
                    mapOf("slot_count" to selectedTimeSlots.size.toString())
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(pricePerHour, allowedDurations),
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = SchedulingWindow(
                        maxAdvanceDays, minAdvanceHours, bufferTime, maxBookingsPerDay
                    ),
                    bookingForm = configuration.bookingForm,
                    guestPermissions = configuration.guestPermissions,
                    bookingConfirmation = configuration.bookingConfirmation
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingDateSlots = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_date_time_slots_edit")
                selectedTimeSlots.clear()
                selectedDate = null
                isEditingDateSlots = false
            },
            onRemoveTimeSlot = { date ->
                FirebaseAnalyticsHelper.logEvent(
                    "time_slot_removed",
                    mapOf("date" to date)
                )
                selectedTimeSlots.removeAll { it.date == date }
            }
        )


        if (showTimeSlotDialog && selectedDate != null) {
            TimeSlotDialog(
                date = selectedDate!!,
                startTime = startTime,
                endTime = endTime,
                onStartTimeChange = { startTime = it },
                onEndTimeChange = { endTime = it },
                onConfirm = { isRecurring, pattern, interval, endDate ->
                    FirebaseAnalyticsHelper.logEvent(
                        "time_slot_added",
                        mapOf(
                            "date" to selectedDate.toString(),
                            "start_time" to startTime,
                            "end_time" to endTime,
                            "is_recurring" to isRecurring.toString(),
                            "pattern" to (pattern ?: "none")
                        )
                    )

                    val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")
                    val dateStr = selectedDate!!.format(formatter)

                    val recurringPatternMap =
                        if (isRecurring && pattern != null && pattern != "No Repeat") {
                            mapOf(
                                "type" to pattern,
                                "interval" to (interval ?: 1)
                            )
                        } else null

                    val newSlot = TimeSlot(
                        id = (selectedTimeSlots.flatMap { it.slots }.maxOfOrNull { it.id }
                            ?: 0) + 1,
                        startTime = startTime,
                        endTime = endTime,
                        isRecurring = isRecurring,
                        recurringPattern = recurringPatternMap,
                        recurringEndDate = endDate
                    )

                    val existingSlot = selectedTimeSlots.find { it.date == dateStr }
                    if (existingSlot != null) {
                        val updatedSlots = existingSlot.slots.toMutableList()
                        updatedSlots.add(newSlot)
                        selectedTimeSlots.remove(existingSlot)
                        selectedTimeSlots.add(existingSlot.copy(slots = updatedSlots))
                    } else {
                        selectedTimeSlots.add(
                            SelectedTimeSlot(date = dateStr, slots = listOf(newSlot))
                        )
                    }

                    showTimeSlotDialog = false
                    startTime = "18:15"
                    endTime = "19:15"
                },
                onDismiss = {
                    FirebaseAnalyticsHelper.logDialogClosed("time_slot_dialog", "dismissed")
                    showTimeSlotDialog = false
                }
            )
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Scheduling Window
        EditableSchedulingWindow(
            isEditing = isEditingScheduling,
            maxAdvanceDays = maxAdvanceDays,
            minAdvanceHours = minAdvanceHours,
            bufferTime = bufferTime,
            maxBookingsPerDay = maxBookingsPerDay,
            onMaxAdvanceDaysChange = { maxAdvanceDays = it },
            onMinAdvanceHoursChange = { minAdvanceHours = it },
            onBufferTimeChange = { bufferTime = it },
            onMaxBookingsPerDayChange = { maxBookingsPerDay = it },
            onEditClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("edit_scheduling_window_clicked")
                FirebaseAnalyticsHelper.logEvent("scheduling_window_edit_started")
                isEditingScheduling = true
            },
            onSaveClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("save_scheduling_window")
                FirebaseAnalyticsHelper.logEvent(
                    "scheduling_window_saved",
                    mapOf(
                        "max_advance_days" to maxAdvanceDays.toString(),
                        "min_advance_hours" to minAdvanceHours.toString(),
                        "buffer_time" to bufferTime.toString(),
                        "max_bookings_per_day" to maxBookingsPerDay.toString()
                    )
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(pricePerHour, allowedDurations),
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = SchedulingWindow(
                        maxAdvanceDays, minAdvanceHours, bufferTime, maxBookingsPerDay
                    ),
                    bookingForm = configuration.bookingForm,
                    guestPermissions = configuration.guestPermissions,
                    bookingConfirmation = configuration.bookingConfirmation
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingScheduling = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_scheduling_window_edit")
                maxAdvanceDays = configuration.schedulingWindow.maxAdvanceDays
                minAdvanceHours = configuration.schedulingWindow.minAdvanceHours
                bufferTime = configuration.schedulingWindow.bufferTime
                maxBookingsPerDay = configuration.schedulingWindow.maxBookingsPerDay
                isEditingScheduling = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Booking Form
        EditableBookingFormSection(
            isEditing = isEditingBookingForm,
            configuration = configuration,
            customMessage = customMessage,
            collectPaymentUpfront = collectPaymentUpfront,
            onCustomMessageChange = { customMessage = it },
            onCollectPaymentChange = { collectPaymentUpfront = it },
            onEditClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("edit_booking_form_clicked")
                FirebaseAnalyticsHelper.logEvent("booking_form_edit_started")
                isEditingBookingForm = true
            },
            onSaveClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("save_booking_form")
                FirebaseAnalyticsHelper.logEvent(
                    "booking_form_saved",
                    mapOf(
                        "collect_payment_upfront" to collectPaymentUpfront.toString(),
                        "message_length" to customMessage.length.toString()
                    )
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(pricePerHour, allowedDurations),
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = SchedulingWindow(
                        maxAdvanceDays, minAdvanceHours, bufferTime, maxBookingsPerDay
                    ),
                    bookingForm = BookingForm(
                        fields = configuration.bookingForm.fields,
                        customMessage = customMessage,
                        collectPaymentUpfront = collectPaymentUpfront
                    ),
                    guestPermissions = configuration.guestPermissions,
                    bookingConfirmation = configuration.bookingConfirmation
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingBookingForm = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_booking_form_edit")
                customMessage = configuration.bookingForm.customMessage
                collectPaymentUpfront = configuration.bookingForm.collectPaymentUpfront
                isEditingBookingForm = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Guest Permissions
        EditableGuestPermissionsSection(
            isEditing = isEditingGuestPermissions,
            canInviteOthers = canInviteOthers,
            canReschedule = canReschedule,
            canCancel = canCancel,
            rescheduleLimitHours = rescheduleLimitHours,
            cancelLimitHours = cancelLimitHours,
            onCanInviteOthersChange = { canInviteOthers = it },
            onCanRescheduleChange = { canReschedule = it },
            onCanCancelChange = { canCancel = it },
            onRescheduleLimitHoursChange = { rescheduleLimitHours = it },
            onCancelLimitHoursChange = { cancelLimitHours = it },
            onEditClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("edit_guest_permissions_clicked")
                FirebaseAnalyticsHelper.logEvent("guest_permissions_edit_started")
                isEditingGuestPermissions = true
            },
            onSaveClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("save_guest_permissions")
                FirebaseAnalyticsHelper.logEvent(
                    "guest_permissions_saved",
                    mapOf(
                        "can_invite_others" to canInviteOthers.toString(),
                        "can_reschedule" to canReschedule.toString(),
                        "can_cancel" to canCancel.toString(),
                        "reschedule_limit_hours" to rescheduleLimitHours.toString(),
                        "cancel_limit_hours" to cancelLimitHours.toString()
                    )
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(pricePerHour, allowedDurations),
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = SchedulingWindow(
                        maxAdvanceDays, minAdvanceHours, bufferTime, maxBookingsPerDay
                    ),
                    bookingForm = configuration.bookingForm,
                    guestPermissions = GuestPermissions(
                        canInviteOthers, canReschedule, canCancel,
                        rescheduleLimitHours, cancelLimitHours
                    ),
                    bookingConfirmation = configuration.bookingConfirmation
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingGuestPermissions = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_guest_permissions_edit")
                canInviteOthers = configuration.guestPermissions.canInviteOthers
                canReschedule = configuration.guestPermissions.canReschedule
                canCancel = configuration.guestPermissions.canCancel
                rescheduleLimitHours = configuration.guestPermissions.rescheduleLimitHours
                cancelLimitHours = configuration.guestPermissions.cancelLimitHours
                isEditingGuestPermissions = false
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Booking Confirmation
        EditableBookingConfirmationSection(
            isEditing = isEditingConfirmation,
            configuration = configuration,
            sendCalendarInvitation = sendCalendarInvitation,
            sendEmailConfirmation = sendEmailConfirmation,
            sendSmsReminders = sendSmsReminders,
            autoConfirm = autoConfirm,
            requireApproval = requireApproval,
            onSendCalendarInvitationChange = { sendCalendarInvitation = it },
            onSendEmailConfirmationChange = { sendEmailConfirmation = it },
            onSendSmsRemindersChange = { sendSmsReminders = it },
            onAutoConfirmChange = { autoConfirm = it },
            onRequireApprovalChange = { requireApproval = it },
            onEditClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("edit_booking_confirmation_clicked")
                FirebaseAnalyticsHelper.logEvent("booking_confirmation_edit_started")
                isEditingConfirmation = true
            },
            onSaveClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("save_booking_confirmation")
                FirebaseAnalyticsHelper.logEvent(
                    "booking_confirmation_saved",
                    mapOf(
                        "send_calendar_invitation" to sendCalendarInvitation.toString(),
                        "send_email_confirmation" to sendEmailConfirmation.toString(),
                        "send_sms_reminders" to sendSmsReminders.toString(),
                        "auto_confirm" to autoConfirm.toString(),
                        "require_approval" to requireApproval.toString()
                    )
                )

                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(pricePerHour, allowedDurations),
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = SchedulingWindow(
                        maxAdvanceDays, minAdvanceHours, bufferTime, maxBookingsPerDay
                    ),
                    bookingForm = configuration.bookingForm,
                    guestPermissions = configuration.guestPermissions,
                    bookingConfirmation = BookingConfirmation(
                        sendCalendarInvitation, sendEmailConfirmation, sendSmsReminders,
                        configuration.bookingConfirmation.reminderTiming,
                        autoConfirm, requireApproval
                    )
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingConfirmation = false
            },
            onCancelClick = {
                FirebaseAnalyticsHelper.logFeatureUsed("cancel_booking_confirmation_edit")
                sendCalendarInvitation = configuration.bookingConfirmation.sendCalendarInvitation
                sendEmailConfirmation = configuration.bookingConfirmation.sendEmailConfirmation
                sendSmsReminders = configuration.bookingConfirmation.sendSmsReminders
                autoConfirm = configuration.bookingConfirmation.autoConfirm
                requireApproval = configuration.bookingConfirmation.requireApproval
                isEditingConfirmation = false
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}





@Composable
fun EditableAboutSection(
    aboutText: String,
    isEditing: Boolean,
    onAboutTextChange: (String) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "About", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (!isEditing) {
                TextButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("about_edit_button_clicked")
                    onEditClick()
                }) {
                    Text("Edit", color = Color(0xFF6B4EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = aboutText,
            onValueChange = {
                if (!isEditing) {
                    FirebaseAnalyticsHelper.logEvent("about_text_field_activated")
                    onEditClick()
                }
                if (it.length <= 150) {
                    FirebaseAnalyticsHelper.logEvent(
                        "about_text_changed",
                        mapOf("text_length" to it.length.toString())
                    )
                    onAboutTextChange(it)
                }
            },
            placeholder = { Text("Add Your about", color = Color.LightGray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .focusRequester(focusRequester)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (!isEditing) {
                            FirebaseAnalyticsHelper.logEvent("about_text_field_tapped")
                            onEditClick()
                        }
                        focusRequester.requestFocus()
                    }
                },
            enabled = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = Color(0xFF6B4EFF),
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Text(
            text = "${aboutText.length}/150",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End
        )

        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("about_cancel_clicked")
                    FirebaseAnalyticsHelper.logEvent(
                        "about_edit_cancelled",
                        mapOf("text_length_at_cancel" to aboutText.length.toString())
                    )
                    onCancelClick()
                }) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("about_save_clicked")
                        FirebaseAnalyticsHelper.logEvent(
                            "about_save_attempted",
                            mapOf("final_text_length" to aboutText.length.toString())
                        )
                        onSaveClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                ) {
                    Text("Save")
                }
            }
        }
    }
}


@Composable
fun EditableServiceSlotSection(
    pricePerHour: Double,
    allowedDurations: MutableList<Int>,
    onPriceChange: (Double) -> Unit,
    onDurationsChange: (MutableList<Int>) -> Unit,
    onSaveClick: (Double, MutableList<Int>) -> Unit,
    onCancelClick: () -> Unit
) {
    val availableDurations = listOf(15, 30, 45, 60)

    // Local editing state
    var isEditing by remember { mutableStateOf(false) }
    var tempPrice by remember { mutableStateOf(pricePerHour.toString()) }
    var tempDurations by remember { mutableStateOf(allowedDurations.toMutableList()) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // Header
        Text(
            text = "Configure Service Slot",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Set your price per hour and allowed slot durations",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // PRICE SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Price for 60 minutes ₹", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = tempPrice,
                onValueChange = {
                    if (!isEditing) {
                        FirebaseAnalyticsHelper.logEvent("service_slot_price_edit_started")
                    }
                    isEditing = true
                    tempPrice = it
                    FirebaseAnalyticsHelper.logEvent(
                        "service_slot_price_changed",
                        mapOf("new_price" to it)
                    )
                },
                modifier = Modifier
                    .width(80.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text("/ hr", fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SLOT SELECTION
        Text(
            text = "Allowed Slot Durations",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableDurations.size) { index ->
                val duration = availableDurations[index]
                SlotChip(
                    duration = duration,
                    isSelected = tempDurations.contains(duration),
                    onToggle = {
                        if (!isEditing) {
                            FirebaseAnalyticsHelper.logEvent("service_slot_duration_edit_started")
                        }
                        isEditing = true
                        val newDurations = tempDurations.toMutableList()
                        if (newDurations.contains(duration)) {
                            newDurations.remove(duration)
                            FirebaseAnalyticsHelper.logEvent(
                                "service_slot_duration_removed",
                                mapOf("duration" to duration.toString())
                            )
                        } else {
                            newDurations.add(duration)
                            newDurations.sort()
                            FirebaseAnalyticsHelper.logEvent(
                                "service_slot_duration_added",
                                mapOf("duration" to duration.toString())
                            )
                        }
                        tempDurations = newDurations
                        FirebaseAnalyticsHelper.logEvent(
                            "service_slot_durations_updated",
                            mapOf("total_durations" to newDurations.size.toString())
                        )
                    },
                    enabled = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NOTE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8E0FF), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "Note: The price you set here is for a 60-minute session. Shorter slots will be priced proportionally.",
                fontSize = 12.sp,
                color = Color(0xFF6B4EFF)
            )
        }

        // SHOW SAVE / CANCEL WHEN USER STARTS EDITING
        if (isEditing) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("service_slot_cancel_clicked")
                    FirebaseAnalyticsHelper.logEvent(
                        "service_slot_edit_cancelled",
                        mapOf(
                            "price_at_cancel" to tempPrice,
                            "durations_count_at_cancel" to tempDurations.size.toString()
                        )
                    )
                    // Reset local changes
                    tempPrice = pricePerHour.toString()
                    tempDurations = allowedDurations.toMutableList()
                    isEditing = false
                    onCancelClick()
                }) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("service_slot_save_clicked")
                        tempPrice.toDoubleOrNull()?.let { newPrice ->
                            FirebaseAnalyticsHelper.logEvent(
                                "service_slot_save_attempted",
                                mapOf(
                                    "final_price" to newPrice.toString(),
                                    "final_durations" to tempDurations.joinToString(","),
                                    "durations_count" to tempDurations.size.toString()
                                )
                            )
                            onPriceChange(newPrice)
                            onDurationsChange(tempDurations)
                            isEditing = false
                            onSaveClick(newPrice, tempDurations)
                        } ?: run {
                            FirebaseAnalyticsHelper.logEvent(
                                "service_slot_save_failed",
                                mapOf("reason" to "invalid_price", "entered_value" to tempPrice)
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                ) {
                    Text("Save")
                }
            }
        }
    }
}


@Composable
fun EditableDateTimeSlotSection(
    isEditing: Boolean,
    selectedMonth: YearMonth,
    selectedDate: LocalDate?,
    selectedTimeSlots: List<SelectedTimeSlot>,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onRemoveTimeSlot: (String) -> Unit
) {
    // Track when section becomes visible
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent(
            "date_time_slot_section_viewed",
            mapOf("existing_slots_count" to selectedTimeSlots.size.toString())
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select the available date",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!isEditing) {
                TextButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("date_time_slot_edit_button_clicked")
                    FirebaseAnalyticsHelper.logEvent("date_time_slot_edit_mode_activated")
                    onEditClick()
                }) {
                    Text("Edit", color = Color(0xFF6B4EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CalendarView(
            selectedMonth = selectedMonth,
            selectedDate = selectedDate,
            onMonthChange = {
                if (!isEditing) {
                    FirebaseAnalyticsHelper.logEvent("calendar_month_changed_triggered_edit")
                    onEditClick()
                }
                FirebaseAnalyticsHelper.logEvent(
                    "calendar_month_changed",
                    mapOf(
                        "new_month" to it.toString(),
                        "is_editing" to isEditing.toString()
                    )
                )
                onMonthChange(it)
            },
            onDateSelect = {
                if (!isEditing) {
                    FirebaseAnalyticsHelper.logEvent("date_selection_triggered_edit")
                    onEditClick()
                }
                FirebaseAnalyticsHelper.logEvent(
                    "calendar_date_selected",
                    mapOf(
                        "date" to it.toString(),
                        "day_of_week" to it.dayOfWeek.toString(),
                        "is_editing" to isEditing.toString()
                    )
                )
                onDateSelect(it)
            },
            enabled = true,
            highlightedDates = selectedTimeSlots.map { it.date }
        )

        if (selectedTimeSlots.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected Time Slots", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(selectedTimeSlots.size) { index ->
                    TimeSlotCard(
                        timeSlot = selectedTimeSlots[index],
                        onRemove = if (isEditing) {
                            {
                                FirebaseAnalyticsHelper.logFeatureUsed("time_slot_remove_clicked")
                                FirebaseAnalyticsHelper.logEvent(
                                    "time_slot_removed_from_card",
                                    mapOf(
                                        "date" to selectedTimeSlots[index].date,
                                        "slots_count" to selectedTimeSlots[index].slots.size.toString()
                                    )
                                )
                                onRemoveTimeSlot(selectedTimeSlots[index].date)
                            }
                        } else null
                    )
                }
            }
        } else {
            // Track when no slots are present
            LaunchedEffect(Unit) {
                FirebaseAnalyticsHelper.logEvent("date_time_slots_empty_state_shown")
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("date_time_slot_cancel_clicked")
                    FirebaseAnalyticsHelper.logEvent(
                        "date_time_slot_edit_cancelled",
                        mapOf(
                            "slots_count_at_cancel" to selectedTimeSlots.size.toString(),
                            "had_unsaved_changes" to "true"
                        )
                    )
                    onCancelClick()
                }) {
                    Text("Cancel", color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("date_time_slot_save_clicked")
                        FirebaseAnalyticsHelper.logEvent(
                            "date_time_slot_save_attempted",
                            mapOf(
                                "total_dates" to selectedTimeSlots.size.toString(),
                                "total_individual_slots" to selectedTimeSlots.sumOf { it.slots.size }.toString(),
                                "dates_list" to selectedTimeSlots.map { it.date }.joinToString(",")
                            )
                        )
                        onSaveClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                ) {
                    Text("Save")
                }
            }
        }
    }
}


@Composable
fun TimeSlotDialog(
    date: LocalDate,
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onConfirm: (isRecurring: Boolean, pattern: String?, interval: Int?, endDate: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var isRecurring by remember { mutableStateOf(false) }
    var repeatPattern by remember { mutableStateOf("No Repeat") }
    var interval by remember { mutableStateOf("1") }
    var recurringEndDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Track dialog opened
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent(
            "time_slot_dialog_opened",
            mapOf(
                "date" to date.toString(),
                "initial_start_time" to startTime,
                "initial_end_time" to endTime
            )
        )
    }

    // Time options (15-minute intervals)
    val timeOptions = remember {
        (0..23).flatMap { hour ->
            listOf(0, 15, 30, 45).map { minute ->
                String.format("%02d:%02d", hour, minute)
            }
        }
    }

    val repeatPatterns = listOf("No Repeat", "Daily", "Weekly", "Monthly", "yearly")

    // Calculate duration
    val duration = remember(startTime, endTime) {
        try {
            val start = LocalTime.parse(startTime)
            val end = LocalTime.parse(endTime)
            val hours = java.time.Duration.between(start, end).toHours()
            val minutes = java.time.Duration.between(start, end).toMinutes() % 60

            val durationStr = if (hours > 0 && minutes > 0) {
                "$hours hour ${minutes} min"
            } else if (hours > 0) {
                "$hours hour"
            } else {
                "$minutes min"
            }

            // Track duration calculation
            FirebaseAnalyticsHelper.logEvent(
                "time_slot_duration_calculated",
                mapOf(
                    "duration" to durationStr,
                    "hours" to hours.toString(),
                    "minutes" to minutes.toString()
                )
            )

            durationStr
        } catch (e: Exception) {
            FirebaseAnalyticsHelper.logEvent(
                "time_slot_duration_calculation_error",
                mapOf("error" to e.message.orEmpty())
            )
            "1 hour"
        }
    }

    Dialog(
        onDismissRequest = {
            FirebaseAnalyticsHelper.logDialogClosed("time_slot_dialog", "outside_click")
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Time Slot - ${date.format(DateTimeFormatter.ofPattern("M/d/yyyy"))}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("time_slot_dialog_close_button")
                            FirebaseAnalyticsHelper.logDialogClosed("time_slot_dialog", "close_button")
                            onDismiss()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Divider(color = Color.LightGray, thickness = 1.dp)

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Available Time Range
                    Text(
                        text = "Available Time Range",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Time Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Start Time Dropdown
                        var startExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = {
                                    FirebaseAnalyticsHelper.logFeatureUsed("time_slot_start_time_dropdown_opened")
                                    startExpanded = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text(startTime, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                            DropdownMenu(
                                expanded = startExpanded,
                                onDismissRequest = { startExpanded = false },
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                timeOptions.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = {
                                            FirebaseAnalyticsHelper.logEvent(
                                                "time_slot_start_time_selected",
                                                mapOf(
                                                    "previous_time" to startTime,
                                                    "new_time" to time
                                                )
                                            )
                                            onStartTimeChange(time)
                                            startExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text("to", color = Color.Gray)

                        // End Time Dropdown
                        var endExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = {
                                    FirebaseAnalyticsHelper.logFeatureUsed("time_slot_end_time_dropdown_opened")
                                    endExpanded = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text(endTime, color = Color.Black)
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                            DropdownMenu(
                                expanded = endExpanded,
                                onDismissRequest = { endExpanded = false },
                                modifier = Modifier.heightIn(max = 300.dp)
                            ) {
                                timeOptions.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = {
                                            FirebaseAnalyticsHelper.logEvent(
                                                "time_slot_end_time_selected",
                                                mapOf(
                                                    "previous_time" to endTime,
                                                    "new_time" to time
                                                )
                                            )
                                            onEndTimeChange(time)
                                            endExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Text(duration, fontSize = 12.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Make this recurring checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                FirebaseAnalyticsHelper.logEvent(
                                    "time_slot_recurring_toggled",
                                    mapOf(
                                        "previous_state" to isRecurring.toString(),
                                        "new_state" to (!isRecurring).toString()
                                    )
                                )
                                isRecurring = !isRecurring
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isRecurring,
                            onCheckedChange = {
                                FirebaseAnalyticsHelper.logEvent(
                                    "time_slot_recurring_checkbox_changed",
                                    mapOf("is_recurring" to it.toString())
                                )
                                isRecurring = it
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF6B4EFF)
                            )
                        )
                        Text(
                            text = "Make this recurring",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }

                    // Expandable recurring options
                    AnimatedVisibility(visible = isRecurring) {
                        LaunchedEffect(isRecurring) {
                            if (isRecurring) {
                                FirebaseAnalyticsHelper.logEvent("time_slot_recurring_options_expanded")
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            // Repeat Pattern
                            Text(
                                text = "Repeat Pattern",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            var patternExpanded by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        FirebaseAnalyticsHelper.logFeatureUsed("time_slot_repeat_pattern_dropdown_opened")
                                        patternExpanded = true
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Text(repeatPattern, color = Color.Black)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                                DropdownMenu(
                                    expanded = patternExpanded,
                                    onDismissRequest = { patternExpanded = false }
                                ) {
                                    repeatPatterns.forEach { pattern ->
                                        DropdownMenuItem(
                                            text = { Text(pattern) },
                                            onClick = {
                                                FirebaseAnalyticsHelper.logEvent(
                                                    "time_slot_repeat_pattern_selected",
                                                    mapOf(
                                                        "previous_pattern" to repeatPattern,
                                                        "new_pattern" to pattern
                                                    )
                                                )
                                                repeatPattern = pattern
                                                patternExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interval
                            Text(
                                text = "Interval",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = interval,
                                onValueChange = {
                                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                        FirebaseAnalyticsHelper.logEvent(
                                            "time_slot_interval_changed",
                                            mapOf(
                                                "previous_interval" to interval,
                                                "new_interval" to it
                                            )
                                        )
                                        interval = it
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B4EFF),
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // End Date
                            Text(
                                text = "End Date",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = recurringEndDate,
                                onValueChange = { recurringEndDate = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        FirebaseAnalyticsHelper.logFeatureUsed("time_slot_end_date_field_clicked")
                                        showDatePicker = true
                                    },
                                placeholder = { Text("dd-mm-yyyy", color = Color.Gray) },
                                readOnly = true,
                                enabled = false,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        FirebaseAnalyticsHelper.logFeatureUsed("time_slot_end_date_picker_opened")
                                        showDatePicker = true
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Select Date",
                                            tint = Color.Gray
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6B4EFF),
                                    unfocusedBorderColor = Color.LightGray,
                                    disabledBorderColor = Color.LightGray,
                                    disabledTextColor = Color.Black
                                )
                            )
                        }
                    }
                }

                Divider(color = Color.LightGray, thickness = 1.dp)

                // Buttons at bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("time_slot_dialog_cancel_clicked")
                            FirebaseAnalyticsHelper.logEvent(
                                "time_slot_creation_cancelled",
                                mapOf(
                                    "date" to date.toString(),
                                    "was_recurring" to isRecurring.toString(),
                                    "start_time" to startTime,
                                    "end_time" to endTime
                                )
                            )
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Cancel", color = Color.DarkGray)
                    }

                    Button(
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("time_slot_dialog_add_slot_clicked")
                            FirebaseAnalyticsHelper.logEvent(
                                "time_slot_creation_confirmed",
                                mapOf(
                                    "date" to date.toString(),
                                    "start_time" to startTime,
                                    "end_time" to endTime,
                                    "duration" to duration,
                                    "is_recurring" to isRecurring.toString(),
                                    "repeat_pattern" to (if (isRecurring) repeatPattern else "none"),
                                    "interval" to (if (isRecurring) interval else "none"),
                                    "has_end_date" to (isRecurring && recurringEndDate.isNotEmpty()).toString()
                                )
                            )
                            onConfirm(
                                isRecurring,
                                if (isRecurring && repeatPattern != "No Repeat") repeatPattern else null,
                                if (isRecurring) interval.toIntOrNull() else null,
                                if (isRecurring && recurringEndDate.isNotEmpty()) recurringEndDate else null
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B4EFF)
                        )
                    ) {
                        Text("Add Slot", color = Color.White)
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = if (recurringEndDate.isNotEmpty()) {
                try {
                    LocalDate.parse(recurringEndDate, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                } catch (e: Exception) {
                    FirebaseAnalyticsHelper.logEvent(
                        "date_picker_parse_error",
                        mapOf("invalid_date" to recurringEndDate)
                    )
                    LocalDate.now()
                }
            } else {
                LocalDate.now()
            },
            onDateSelected = { selectedDate ->
                FirebaseAnalyticsHelper.logEvent(
                    "time_slot_end_date_selected",
                    mapOf("selected_date" to selectedDate.toString())
                )
                recurringEndDate = selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                showDatePicker = false
            },
            onDismiss = {
                FirebaseAnalyticsHelper.logDialogClosed("date_picker_dialog", "dismissed")
                showDatePicker = false
            }
        )
    }
}

@Composable
fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var tempSelectedDate by remember { mutableStateOf(selectedDate) }

    // Track dialog opened
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent(
            "date_picker_dialog_opened",
            mapOf(
                "initial_date" to selectedDate.toString(),
                "initial_month" to currentMonth.toString()
            )
        )
    }

    Dialog(onDismissRequest = {
        FirebaseAnalyticsHelper.logDialogClosed("date_picker_dialog", "outside_click")
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with month navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("date_picker_previous_month")
                        FirebaseAnalyticsHelper.logEvent(
                            "date_picker_month_changed",
                            mapOf(
                                "previous_month" to currentMonth.toString(),
                                "new_month" to currentMonth.minusMonths(1).toString(),
                                "direction" to "previous"
                            )
                        )
                        currentMonth = currentMonth.minusMonths(1)
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Month"
                        )
                    }
                    Text(
                        text = "${currentMonth.month.name.take(3)} ${currentMonth.year}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("date_picker_next_month")
                        FirebaseAnalyticsHelper.logEvent(
                            "date_picker_month_changed",
                            mapOf(
                                "previous_month" to currentMonth.toString(),
                                "new_month" to currentMonth.plusMonths(1).toString(),
                                "direction" to "next"
                            )
                        )
                        currentMonth = currentMonth.plusMonths(1)
                    }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar grid
                val firstDayOfMonth = currentMonth.atDay(1)
                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(250.dp),
                    userScrollEnabled = false
                ) {
                    // Empty cells before first day
                    items(firstDayOfWeek) {
                        Box(modifier = Modifier.size(40.dp))
                    }

                    // Days of month
                    items(daysInMonth) { day ->
                        val currentDate = currentMonth.atDay(day + 1)
                        val isSelected = currentDate == tempSelectedDate
                        val isPast = currentDate.isBefore(LocalDate.now())

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(2.dp)
                                .background(
                                    if (isSelected) Color(0xFF6B4EFF) else Color.Transparent,
                                    CircleShape
                                )
                                .clickable(enabled = !isPast) {
                                    FirebaseAnalyticsHelper.logEvent(
                                        "date_picker_date_clicked",
                                        mapOf(
                                            "previous_date" to tempSelectedDate.toString(),
                                            "new_date" to currentDate.toString(),
                                            "day_of_week" to currentDate.dayOfWeek.toString(),
                                            "is_past" to isPast.toString()
                                        )
                                    )
                                    tempSelectedDate = currentDate
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day + 1}",
                                color = when {
                                    isSelected -> Color.White
                                    isPast -> Color.LightGray
                                    else -> Color.Black
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("date_picker_cancel_clicked")
                        FirebaseAnalyticsHelper.logEvent(
                            "date_picker_cancelled",
                            mapOf("selected_date_at_cancel" to tempSelectedDate.toString())
                        )
                        onDismiss()
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("date_picker_ok_clicked")
                            FirebaseAnalyticsHelper.logEvent(
                                "date_picker_date_confirmed",
                                mapOf(
                                    "final_date" to tempSelectedDate.toString(),
                                    "initial_date" to selectedDate.toString(),
                                    "date_changed" to (tempSelectedDate != selectedDate).toString()
                                )
                            )
                            onDateSelected(tempSelectedDate)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6B4EFF)
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}



@Composable
fun TimeSlotCard(
    timeSlot: SelectedTimeSlot,
    onRemove: (() -> Unit)?
) {
    // Track when card is viewed
    LaunchedEffect(timeSlot) {
        FirebaseAnalyticsHelper.logEvent(
            "time_slot_card_viewed",
            mapOf(
                "date" to timeSlot.date,
                "slot_count" to timeSlot.slots.size.toString(),
                "has_remove_option" to (onRemove != null).toString()
            )
        )
    }

    Card(
        modifier = Modifier
            .width(120.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeSlot.date.take(10),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${timeSlot.slots.size} Slot${if (timeSlot.slots.size > 1) "s" else ""}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                timeSlot.slots.forEach { slot ->
                    Text(
                        text = "${slot.startTime}-${slot.endTime}",
                        fontSize = 9.sp,
                        color = Color(0xFF6B4EFF)
                    )
                }
            }

            if (onRemove != null) {
                IconButton(
                    onClick = {
                        FirebaseAnalyticsHelper.logFeatureUsed("time_slot_card_remove_clicked")
                        FirebaseAnalyticsHelper.logEvent(
                            "time_slot_card_remove_button_clicked",
                            mapOf(
                                "date" to timeSlot.date,
                                "slot_count" to timeSlot.slots.size.toString(),
                                "time_slots" to timeSlot.slots.joinToString(",") {
                                    "${it.startTime}-${it.endTime}"
                                }
                            )
                        )
                        onRemove()
                    },
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}








@Composable
fun CalendarView(
    selectedMonth: YearMonth,
    selectedDate: LocalDate?,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    enabled: Boolean = true,
    highlightedDates: List<String> = emptyList()
) {
    val today = LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("calendar_previous_month_clicked")
                    FirebaseAnalyticsHelper.logEvent(
                        "calendar_month_changed",
                        mapOf(
                            "direction" to "previous",
                            "from_month" to selectedMonth.toString(),
                            "to_month" to selectedMonth.minusMonths(1).toString()
                        )
                    )
                    onMonthChange(selectedMonth.minusMonths(1))
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                }
                Text(
                    "${selectedMonth.month.name.take(3)} ${selectedMonth.year}",
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("calendar_next_month_clicked")
                    FirebaseAnalyticsHelper.logEvent(
                        "calendar_month_changed",
                        mapOf(
                            "direction" to "next",
                            "from_month" to selectedMonth.toString(),
                            "to_month" to selectedMonth.plusMonths(1).toString()
                        )
                    )
                    onMonthChange(selectedMonth.plusMonths(1))
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val firstDayOfMonth = selectedMonth.atDay(1)
            val daysInMonth = selectedMonth.lengthOfMonth()
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(250.dp),
                userScrollEnabled = false
            ) {
                items(firstDayOfWeek) {
                    Box(modifier = Modifier.size(40.dp))
                }
                items(daysInMonth) { day ->
                    val date = selectedMonth.atDay(day + 1)
                    val isSelected = date == selectedDate
                    val isPastDate = date.isBefore(today)
                    val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")
                    val isHighlighted =
                        highlightedDates.any { it.contains(date.format(formatter).take(10)) }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(4.dp)
                            .background(
                                when {
                                    isSelected -> Color(0xFF6B4EFF)
                                    isHighlighted -> Color(0xFFE8E0FF)
                                    else -> Color.Transparent
                                },
                                CircleShape
                            )
                            .clickable(enabled = !isPastDate) {
                                if (!isPastDate) {
                                    FirebaseAnalyticsHelper.logFeatureUsed("calendar_date_selected")
                                    FirebaseAnalyticsHelper.logEvent(
                                        "calendar_date_clicked",
                                        mapOf(
                                            "date" to date.toString(),
                                            "is_highlighted" to isHighlighted.toString(),
                                            "day_of_week" to date.dayOfWeek.toString()
                                        )
                                    )
                                    onDateSelect(date)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${day + 1}",
                            color = when {
                                isSelected -> Color.White
                                isPastDate -> Color.LightGray
                                else -> Color.Black
                            },
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun EditableSchedulingWindow(
    isEditing: Boolean,
    maxAdvanceDays: Int,
    minAdvanceHours: Int,
    bufferTime: Int,
    maxBookingsPerDay: Int,
    onMaxAdvanceDaysChange: (Int) -> Unit,
    onMinAdvanceHoursChange: (Int) -> Unit,
    onBufferTimeChange: (Int) -> Unit,
    onMaxBookingsPerDayChange: (Int) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scheduling Window",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!isEditing) {
                TextButton(onClick = onEditClick) {
                    Text("Edit", color = Color(0xFF6B4EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Scheduling Window Details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B4EFF)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    EditableField("Max Advance Days", maxAdvanceDays) {
                        FirebaseAnalyticsHelper.logEvent(
                            "scheduling_field_changed",
                            mapOf(
                                "field" to "max_advance_days",
                                "old_value" to maxAdvanceDays.toString(),
                                "new_value" to it.toString()
                            )
                        )
                        onMaxAdvanceDaysChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Min Advance Hours",
                        minAdvanceHours
                    ) {
                        FirebaseAnalyticsHelper.logEvent(
                            "scheduling_field_changed",
                            mapOf(
                                "field" to "min_advance_hours",
                                "old_value" to minAdvanceHours.toString(),
                                "new_value" to it.toString()
                            )
                        )
                        onMinAdvanceHoursChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField("Buffer Time (minutes)", bufferTime) {
                        FirebaseAnalyticsHelper.logEvent(
                            "scheduling_field_changed",
                            mapOf(
                                "field" to "buffer_time",
                                "old_value" to bufferTime.toString(),
                                "new_value" to it.toString()
                            )
                        )
                        onBufferTimeChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Max Bookings/Day",
                        maxBookingsPerDay
                    ) {
                        FirebaseAnalyticsHelper.logEvent(
                            "scheduling_field_changed",
                            mapOf(
                                "field" to "max_bookings_per_day",
                                "old_value" to maxBookingsPerDay.toString(),
                                "new_value" to it.toString()
                            )
                        )
                        onMaxBookingsPerDayChange(it)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancelClick) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSaveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    DetailItem(
                        "Max Advance Days",
                        "$maxAdvanceDays (How far in advance bookings can be made)"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailItem("Min Advance Hours", "$minAdvanceHours (Minimum notice required)")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailItem("Buffer Time", "$bufferTime min (Break between sessions)")
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailItem("Max Bookings/Day", "$maxBookingsPerDay (Limit per day)")
                }
            }
        }
    }
}

@Composable
fun EditableBookingFormSection(
    isEditing: Boolean,
    configuration: MentorConfigurationResponse,
    customMessage: String,
    collectPaymentUpfront: Boolean,
    onCustomMessageChange: (String) -> Unit,
    onCollectPaymentChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Booking form configuration",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!isEditing) {
                TextButton(onClick = onEditClick) {
                    Text("Edit", color = Color(0xFF9B7EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Booking form Details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE91E8C)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    Text("Custom Message:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = {
                            FirebaseAnalyticsHelper.logEvent(
                                "booking_form_message_changed",
                                mapOf("message_length" to it.length.toString())
                            )
                            onCustomMessageChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter custom message") },
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Collect Payment Upfront:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = collectPaymentUpfront,
                            onCheckedChange = {
                                FirebaseAnalyticsHelper.logFeatureUsed("booking_form_payment_toggle")
                                FirebaseAnalyticsHelper.logEvent(
                                    "booking_form_payment_changed",
                                    mapOf(
                                        "collect_payment_upfront" to it.toString(),
                                        "previous_value" to collectPaymentUpfront.toString()
                                    )
                                )
                                onCollectPaymentChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF6B4EFF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancelClick) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSaveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    val fields = configuration.bookingForm.fields
                    ConfigItem("Default Fields", fields.filter { it.isEnabled }
                        .sortedBy { it.fieldOrder }
                        .joinToString(", ") { "${it.fieldName}${if (it.isRequired) " (required)" else ""}" })
                    ConfigItem(
                        "Form Display Text",
                        if (customMessage.isNotEmpty()) customMessage else "None"
                    )
                    ConfigItem(
                        "Collect Payment Upfront",
                        if (collectPaymentUpfront) "Yes" else "No"
                    )
                }
            }
        }
    }
}

@Composable
fun EditableGuestPermissionsSection(
    isEditing: Boolean,
    canInviteOthers: Boolean,
    canReschedule: Boolean,
    canCancel: Boolean,
    rescheduleLimitHours: Int,
    cancelLimitHours: Int,
    onCanInviteOthersChange: (Boolean) -> Unit,
    onCanRescheduleChange: (Boolean) -> Unit,
    onCanCancelChange: (Boolean) -> Unit,
    onRescheduleLimitHoursChange: (Int) -> Unit,
    onCancelLimitHoursChange: (Int) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Guest Permissions and policies",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!isEditing) {
                TextButton(onClick = onEditClick) {
                    Text("Edit", color = Color(0xFF9B7EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Guest Permission",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00C853)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    SwitchRow("Can Invite Others", canInviteOthers) {
                        FirebaseAnalyticsHelper.logFeatureUsed("guest_permission_invite_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "guest_permission_changed",
                            mapOf(
                                "permission" to "can_invite_others",
                                "value" to it.toString()
                            )
                        )
                        onCanInviteOthersChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Can Reschedule", canReschedule) {
                        FirebaseAnalyticsHelper.logFeatureUsed("guest_permission_reschedule_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "guest_permission_changed",
                            mapOf(
                                "permission" to "can_reschedule",
                                "value" to it.toString()
                            )
                        )
                        onCanRescheduleChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Reschedule Limit (hours)",
                        rescheduleLimitHours
                    ) {
                        FirebaseAnalyticsHelper.logEvent(
                            "guest_permission_limit_changed",
                            mapOf(
                                "limit_type" to "reschedule",
                                "hours" to it.toString()
                            )
                        )
                        onRescheduleLimitHoursChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Can Cancel", canCancel) {
                        FirebaseAnalyticsHelper.logFeatureUsed("guest_permission_cancel_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "guest_permission_changed",
                            mapOf(
                                "permission" to "can_cancel",
                                "value" to it.toString()
                            )
                        )
                        onCanCancelChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Cancel Limit (hours)",
                        cancelLimitHours
                    ) {
                        FirebaseAnalyticsHelper.logEvent(
                            "guest_permission_limit_changed",
                            mapOf(
                                "limit_type" to "cancel",
                                "hours" to it.toString()
                            )
                        )
                        onCancelLimitHoursChange(it)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancelClick) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSaveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    ConfigItem("Can Invite Others", if (canInviteOthers) "Yes" else "No")
                    ConfigItem("Can Reschedule", if (canReschedule) "Yes" else "No")
                    ConfigItem("Reschedule Limit", "$rescheduleLimitHours hours")
                    ConfigItem("Can Cancel", if (canCancel) "Yes" else "No")
                    ConfigItem("Cancel Limit", "$cancelLimitHours hours")
                }
            }
        }
    }
}

@Composable
fun EditableBookingConfirmationSection(
    isEditing: Boolean,
    configuration: MentorConfigurationResponse,
    sendCalendarInvitation: Boolean,
    sendEmailConfirmation: Boolean,
    sendSmsReminders: Boolean,
    autoConfirm: Boolean,
    requireApproval: Boolean,
    onSendCalendarInvitationChange: (Boolean) -> Unit,
    onSendEmailConfirmationChange: (Boolean) -> Unit,
    onSendSmsRemindersChange: (Boolean) -> Unit,
    onAutoConfirmChange: (Boolean) -> Unit,
    onRequireApprovalChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Booking confirmation & \nNotification",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!isEditing) {
                TextButton(onClick = onEditClick) {
                    Text("Edit", color = Color(0xFF9B7EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(1.dp),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Booking Confirmation",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFFC107)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (isEditing) {
                    SwitchRow(
                        "Send Calendar Invitation",
                        sendCalendarInvitation
                    ) {
                        FirebaseAnalyticsHelper.logFeatureUsed("booking_confirmation_calendar_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "booking_confirmation_changed",
                            mapOf(
                                "setting" to "send_calendar_invitation",
                                "value" to it.toString()
                            )
                        )
                        onSendCalendarInvitationChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow(
                        "Send Email Confirmation",
                        sendEmailConfirmation
                    ) {
                        FirebaseAnalyticsHelper.logFeatureUsed("booking_confirmation_email_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "booking_confirmation_changed",
                            mapOf(
                                "setting" to "send_email_confirmation",
                                "value" to it.toString()
                            )
                        )
                        onSendEmailConfirmationChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Send SMS Reminders", sendSmsReminders) {
                        FirebaseAnalyticsHelper.logFeatureUsed("booking_confirmation_sms_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "booking_confirmation_changed",
                            mapOf(
                                "setting" to "send_sms_reminders",
                                "value" to it.toString()
                            )
                        )
                        onSendSmsRemindersChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Auto Confirm", autoConfirm) {
                        FirebaseAnalyticsHelper.logFeatureUsed("booking_confirmation_auto_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "booking_confirmation_changed",
                            mapOf(
                                "setting" to "auto_confirm",
                                "value" to it.toString()
                            )
                        )
                        onAutoConfirmChange(it)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Require Approval", requireApproval) {
                        FirebaseAnalyticsHelper.logFeatureUsed("booking_confirmation_approval_toggle")
                        FirebaseAnalyticsHelper.logEvent(
                            "booking_confirmation_changed",
                            mapOf(
                                "setting" to "require_approval",
                                "value" to it.toString()
                            )
                        )
                        onRequireApprovalChange(it)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onCancelClick) {
                            Text("Cancel", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onSaveClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    ConfigItem(
                        "Send Calendar Invitation",
                        if (sendCalendarInvitation) "Yes" else "No"
                    )
                    ConfigItem(
                        "Send Email Confirmation",
                        if (sendEmailConfirmation) "Yes" else "No"
                    )
                    ConfigItem("Send SMS Reminders", if (sendSmsReminders) "Yes" else "No")
                    ConfigItem(
                        "Reminder Timing",
                        configuration.bookingConfirmation.reminderTiming.joinToString(", ")
                    )
                    ConfigItem("Auto Confirm", if (autoConfirm) "Yes" else "No")
                    ConfigItem("Require Approval", if (requireApproval) "Yes" else "No")
                }
            }
        }
    }
}




@Composable
fun EditableField(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = value.toString(),
            onValueChange = {
                it.toIntOrNull()?.let { newValue ->
                    FirebaseAnalyticsHelper.logEvent(
                        "editable_field_changed",
                        mapOf(
                            "field_label" to label,
                            "old_value" to value.toString(),
                            "new_value" to newValue.toString()
                        )
                    )
                    onValueChange(newValue)
                }
            },
            modifier = Modifier.width(100.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("$label:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Switch(
            checked = checked,
            onCheckedChange = {
                FirebaseAnalyticsHelper.logFeatureUsed("switch_toggled")
                FirebaseAnalyticsHelper.logEvent(
                    "switch_row_changed",
                    mapOf(
                        "switch_label" to label,
                        "old_state" to checked.toString(),
                        "new_state" to it.toString()
                    )
                )
                onCheckedChange(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF6B4EFF)
            )
        )
    }
}

@Composable
fun DetailItem(title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("• ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Column {
            Text("$title : ", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun ConfigItem(label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text("• ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Column(modifier = Modifier.weight(1f)) {
            Text("$label : ", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Text(value, fontSize = 13.sp, color = Color.Gray)
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}


@Composable
fun SlotChip(duration: Int, isSelected: Boolean, onToggle: () -> Unit, enabled: Boolean = true) {
    Row(
        modifier = Modifier.clickable {
            FirebaseAnalyticsHelper.logFeatureUsed("slot_chip_clicked")
            FirebaseAnalyticsHelper.logEvent(
                "slot_chip_toggled",
                mapOf(
                    "duration" to duration.toString(),
                    "was_selected" to isSelected.toString(),
                    "new_state" to (!isSelected).toString(),
                    "enabled" to enabled.toString()
                )
            )
            onToggle()
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .border(
                    width = 2.dp,
                    color = if (isSelected) Color(0xFF9B7EFF) else Color(0xFFD0D0D0),
                    shape = RoundedCornerShape(6.dp)
                )
                .background(
                    color = if (isSelected) Color(0xFFE8DCFF) else Color.White,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF7C5DFF),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Text(
            "$duration Mins",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}


// Utility Composables
@Composable
fun MySessionHeader(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            modifier = Modifier
                .size(24.dp)
                .clickable {
                    FirebaseAnalyticsHelper.logFeatureUsed("my_session_menu_opened")
                    FirebaseAnalyticsHelper.logEvent("my_session_header_menu_clicked")
                    onMenuClick()
                },
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("My Session", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
fun MySessionSidebarContent(onCloseClick: () -> Unit, navController: NavController) {
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("my_session_sidebar_opened")
    }

    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("my_session_sidebar_closed")
                        FirebaseAnalyticsHelper.logEvent("my_session_sidebar_close_clicked")
                        onCloseClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("X", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Mentor Circle",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("sidebar_navigation_mentor_circle")
                        FirebaseAnalyticsHelper.logEvent(
                            "sidebar_menu_item_clicked",
                            mapOf("menu_item" to "mentor_circle")
                        )
                        navController.navigate("livesession")
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp))
            Text(
                "Seek Guidance",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("sidebar_navigation_seek_guidance")
                        FirebaseAnalyticsHelper.logEvent(
                            "sidebar_menu_item_clicked",
                            mapOf("menu_item" to "seek_guidance")
                        )
                        navController.navigate(Screen.LiveSession.route)
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp))
            Text(
                "Bookings",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("sidebar_navigation_bookings")
                        FirebaseAnalyticsHelper.logEvent(
                            "sidebar_menu_item_clicked",
                            mapOf("menu_item" to "bookings")
                        )
                        navController.navigate(Screen.BookingScreen.route)
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE91E63), RoundedCornerShape(20.dp))
                    .clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("sidebar_navigation_my_session")
                        FirebaseAnalyticsHelper.logEvent(
                            "sidebar_menu_item_clicked",
                            mapOf("menu_item" to "my_session")
                        )
                        navController.navigate(Screen.MySessionScreen.route)
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "My Session",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LoadingView() {
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("loading_view_displayed")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF6B4EFF))
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    LaunchedEffect(message) {
        FirebaseAnalyticsHelper.logEvent(
            "error_view_displayed",
            mapOf("error_message" to message)
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("error_view_retry_clicked")
                    FirebaseAnalyticsHelper.logEvent(
                        "error_view_retry_attempted",
                        mapOf("error_message" to message)
                    )
                    onRetry()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyConfigurationView() {
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("empty_configuration_view_displayed")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configure",
                tint = Color(0xFF6B4EFF),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Please Configure",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Set up your session configuration to get started",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}



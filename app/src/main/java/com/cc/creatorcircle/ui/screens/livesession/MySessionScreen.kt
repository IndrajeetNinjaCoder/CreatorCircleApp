package com.cc.creatorcircle.ui.screens.livesession

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
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.viewModel.MentorConfigViewModel
import kotlinx.coroutines.launch
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

    val userDataManager = remember { UserDataManager(context) }
    var userData by remember { mutableStateOf(UserData()) }

    LaunchedEffect(Unit) {
        userData = userDataManager.getUserData()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchConfiguration()
    }

    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateConfigurationState.Success -> {
                Toast.makeText(
                    context,
                    (updateState as UpdateConfigurationState.Success).message,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.clearUpdateState()
            }

            is UpdateConfigurationState.Error -> {
                Toast.makeText(
                    context,
                    (updateState as UpdateConfigurationState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
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
                is GuidanceConfigurationState.Loading -> LoadingView()
                is GuidanceConfigurationState.Success -> {
                    val config = (configState as GuidanceConfigurationState.Success).response
                    ServiceConfigurationContent(
                        userData = userData,
                        configuration = config,
                        viewModel = viewModel,
                        onRefresh = { viewModel.refreshConfiguration() }
                    )
                }

                is GuidanceConfigurationState.Error -> {
                    ErrorView(
                        message = (configState as GuidanceConfigurationState.Error).message,
                        onRetry = { viewModel.refreshConfiguration() }
                    )
                }

                is GuidanceConfigurationState.Idle -> EmptyConfigurationView()
            }
        }
    }
}




//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MySessionScreen(navController: NavController) {
//    val context = LocalContext.current
//    val viewModel = remember { MentorConfigViewModel(context) }
//    val configState by viewModel.configurationState.collectAsState()
//    val updateState by viewModel.updateState.collectAsState()
//    val drawerState = rememberDrawerState(DrawerValue.Closed)
//    val scope = rememberCoroutineScope()
//
//
//    val userDataManager = remember { UserDataManager(context) }
//    var userData by remember { mutableStateOf(UserData()) }
//
//    LaunchedEffect(Unit) {
//        userData = userDataManager.getUserData()
//    }
//
//    LaunchedEffect(Unit) {
//        viewModel.fetchConfiguration()
//    }
//
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UpdateConfigurationState.Success -> {
//                Toast.makeText(
//                    context,
//                    (updateState as UpdateConfigurationState.Success).message,
//                    Toast.LENGTH_SHORT
//                ).show()
//                viewModel.clearUpdateState()
//            }
//
//            is UpdateConfigurationState.Error -> {
//                Toast.makeText(
//                    context,
//                    (updateState as UpdateConfigurationState.Error).message,
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//
//            else -> {}
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            MySessionSidebarContent(
//                onCloseClick = { scope.launch { drawerState.close() } },
//                navController = navController
//            )
//        }
//    ) {
//        Scaffold(
////            topBar = { TopBar(title = "Bookings", navController) },
//            bottomBar = { BottomNavBar(navController = navController) },
//            modifier = Modifier.fillMaxSize()
//        ) { paddingValues ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White)
//            ) {
//                MySessionHeader(
//                    onMenuClick = { scope.launch { drawerState.open() } }
//                )
//
//                when (configState) {
//                    is GuidanceConfigurationState.Loading -> LoadingView()
//                    is GuidanceConfigurationState.Success -> {
//                        val config = (configState as GuidanceConfigurationState.Success).response
//                        ServiceConfigurationContent(
//                            userData = userData,
//                            configuration = config,
//                            viewModel = viewModel,
//                            onRefresh = { viewModel.refreshConfiguration() }
//                        )
//                    }
//
//                    is GuidanceConfigurationState.Error -> {
//                        ErrorView(
//                            message = (configState as GuidanceConfigurationState.Error).message,
//                            onRetry = { viewModel.refreshConfiguration() }
//                        )
//                    }
//
//                    is GuidanceConfigurationState.Idle -> EmptyConfigurationView()
//                }
//            }
//        }
//    }
//}

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
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile1),
                    error = painterResource(id = R.drawable.ic_profile1)
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
            onEditClick = { isEditingAbout = true },
//            onSaveClick = {
//                // Save about text if needed
//                isEditingAbout = false
//            },

            onSaveClick = {
                // Save ONLY the about text, keep everything else from configuration
                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = configuration.serviceSlotConfig, // Keep existing
                    selectedTimeSlots = selectedTimeSlots,
                    schedulingWindow = configuration.schedulingWindow, // Keep existing
                    bookingForm = BookingForm(
                        fields = configuration.bookingForm.fields,
                        customMessage = aboutText, // Only update this
                        collectPaymentUpfront = configuration.bookingForm.collectPaymentUpfront // Keep existing
                    ),
                    guestPermissions = configuration.guestPermissions, // Keep existing
                    bookingConfirmation = configuration.bookingConfirmation // Keep existing
                )
                viewModel.updateConfiguration(configuration.configurationId, request)
                isEditingAbout = false
            },
            onCancelClick = {
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
            onSaveClick = { newPrice, newDurations ->  // Accept parameters
                val request = UpdateMentorConfigurationRequest(
                    serviceSlotConfig = ServiceSlotConfig(
                        pricePerHour = newPrice,  // Use the new values directly
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
                selectedDate = it
                if (isEditingDateSlots) {
                    showTimeSlotDialog = true
                }
            },
            onEditClick = { isEditingDateSlots = true },
            onSaveClick = {
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
                selectedTimeSlots.clear()
                selectedDate = null
                isEditingDateSlots = false
            },
            onRemoveTimeSlot = { date ->
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
                    val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")
                    val dateStr = selectedDate!!.format(formatter)

                    // Convert pattern to Map format
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
                onDismiss = { showTimeSlotDialog = false }
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
            onEditClick = { isEditingScheduling = true },
            onSaveClick = {
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
            onEditClick = { isEditingBookingForm = true },
            onSaveClick = {
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
            onEditClick = { isEditingGuestPermissions = true },
            onSaveClick = {
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
            onEditClick = { isEditingConfirmation = true },
            onSaveClick = {
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
                TextButton(onClick = onEditClick) {
                    Text("Edit", color = Color(0xFF6B4EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = aboutText,
            onValueChange = {
                if (!isEditing) onEditClick()
                if (it.length <= 150) onAboutTextChange(it)
            },
            placeholder = { Text("Add Your about", color = Color.LightGray) },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .focusRequester(focusRequester)
                .pointerInput(Unit) {
                    detectTapGestures {
                        if (!isEditing) {
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
        }
    }
}


@Composable
fun EditableServiceSlotSection(
    pricePerHour: Double,
    allowedDurations: MutableList<Int>,
    onPriceChange: (Double) -> Unit,
    onDurationsChange: (MutableList<Int>) -> Unit,
    onSaveClick: (Double, MutableList<Int>) -> Unit,  // Updated signature
    onCancelClick: () -> Unit
) {
    val availableDurations = listOf(15, 30, 45, 60)

    // Local editing state (automatically turns true when user types or toggles a chip)
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
                    isEditing = true
                    tempPrice = it
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
                        isEditing = true
                        val newDurations = tempDurations.toMutableList()
                        if (newDurations.contains(duration)) {
                            newDurations.remove(duration)
                        } else {
                            newDurations.add(duration)
                            newDurations.sort()
                        }
                        tempDurations = newDurations
                    },
                    enabled = true // Always editable now
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
                        tempPrice.toDoubleOrNull()?.let { newPrice ->
                            onPriceChange(newPrice)
                            onDurationsChange(tempDurations)
                            isEditing = false
                            onSaveClick(newPrice, tempDurations)  // Pass the values
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
                TextButton(onClick = onEditClick) {
                    Text("Edit", color = Color(0xFF6B4EFF))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        CalendarView(
            selectedMonth = selectedMonth,
            selectedDate = selectedDate,
            onMonthChange = {
                if (!isEditing) onEditClick()
                onMonthChange(it)
            },
            onDateSelect = {
                if (!isEditing) onEditClick()
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
                            { onRemoveTimeSlot(selectedTimeSlots[index].date) }
                        } else null
                    )
                }
            }
        }

        if (isEditing) {
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
            if (hours > 0 && minutes > 0) {
                "$hours hour ${minutes} min"
            } else if (hours > 0) {
                "$hours hour"
            } else {
                "$minutes min"
            }
        } catch (e: Exception) {
            "1 hour"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                        onClick = onDismiss,
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
                                onClick = { startExpanded = true },
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
                                onClick = { endExpanded = true },
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
                            .clickable { isRecurring = !isRecurring },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
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
                                    onClick = { patternExpanded = true },
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
                                    .clickable { showDatePicker = true },
                                placeholder = { Text("dd-mm-yyyy", color = Color.Gray) },
                                readOnly = true,
                                enabled = false,
                                trailingIcon = {
                                    IconButton(onClick = { showDatePicker = true }) {
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
                        onClick = onDismiss,
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
                    LocalDate.now()
                }
            } else {
                LocalDate.now()
            },
            onDateSelected = { selectedDate ->
                recurringEndDate = selectedDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
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

    Dialog(onDismissRequest = onDismiss) {
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
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
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
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
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
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onDateSelected(tempSelectedDate) },
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
                    onClick = onRemove,
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
                IconButton(onClick = { onMonthChange(selectedMonth.minusMonths(1)) }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
                }
                Text(
                    "${selectedMonth.month.name.take(3)} ${selectedMonth.year}",
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { onMonthChange(selectedMonth.plusMonths(1)) }) {
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
                    EditableField("Max Advance Days", maxAdvanceDays) { onMaxAdvanceDaysChange(it) }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Min Advance Hours",
                        minAdvanceHours
                    ) { onMinAdvanceHoursChange(it) }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField("Buffer Time (minutes)", bufferTime) { onBufferTimeChange(it) }
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Max Bookings/Day",
                        maxBookingsPerDay
                    ) { onMaxBookingsPerDayChange(it) }

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
                        onValueChange = onCustomMessageChange,
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
                            onCheckedChange = onCollectPaymentChange,
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
                    SwitchRow("Can Invite Others", canInviteOthers, onCanInviteOthersChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Can Reschedule", canReschedule, onCanRescheduleChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Reschedule Limit (hours)",
                        rescheduleLimitHours,
                        onRescheduleLimitHoursChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Can Cancel", canCancel, onCanCancelChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    EditableField(
                        "Cancel Limit (hours)",
                        cancelLimitHours,
                        onCancelLimitHoursChange
                    )

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
                        sendCalendarInvitation,
                        onSendCalendarInvitationChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow(
                        "Send Email Confirmation",
                        sendEmailConfirmation,
                        onSendEmailConfirmationChange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Send SMS Reminders", sendSmsReminders, onSendSmsRemindersChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Auto Confirm", autoConfirm, onAutoConfirmChange)
                    Spacer(modifier = Modifier.height(8.dp))
                    SwitchRow("Require Approval", requireApproval, onRequireApprovalChange)

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

// Helper Composables
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
            onValueChange = { it.toIntOrNull()?.let(onValueChange) },
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
            onCheckedChange = onCheckedChange,
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
        modifier = Modifier.clickable { onToggle() },
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
                .clickable { onMenuClick() },
            tint = Color.Black
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("My Session", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
fun MySessionSidebarContent(onCloseClick: () -> Unit, navController: NavController) {
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
                    .clickable { onCloseClick() },
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
                    .clickable { navController.navigate("livesession"); onCloseClick() }
                    .padding(vertical = 12.dp))
            Text(
                "Seek Guidance", fontSize = 16.sp, color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.LiveSession.route); onCloseClick() }
                    .padding(vertical = 12.dp))
            Text(
                "Bookings", fontSize = 16.sp, color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Screen.BookingScreen.route); onCloseClick() }
                    .padding(vertical = 12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE91E63), RoundedCornerShape(20.dp))
                    .clickable { navController.navigate(Screen.MySessionScreen.route); onCloseClick() }
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF6B4EFF))
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
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
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B4EFF))
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
fun EmptyConfigurationView() {
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


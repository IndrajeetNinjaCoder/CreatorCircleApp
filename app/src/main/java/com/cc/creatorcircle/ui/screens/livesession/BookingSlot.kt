package com.cc.creatorcircle.ui.screens.livesession

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.viewModel.AvailabilityViewModel
import com.cc.creatorcircle.viewModel.BookingViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import com.cc.creatorcircle.R
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper


@Composable
fun BookingSlot(
    navController: NavController,
    userId: Int,
    influencerName: String
) {
    val context = LocalContext.current

    val availabilityViewModel: AvailabilityViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AvailabilityViewModel(context) as T
            }
        }
    )

    val bookingViewModel: BookingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return BookingViewModel(context) as T
            }
        }
    )

    val postsViewModel: PostsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PostsViewModel(context) as T
            }
        }
    )

    val mentorAvailability by availabilityViewModel.mentorAvailability.collectAsState()
    val isLoading by availabilityViewModel.availabilityLoading.collectAsState()
    val error by availabilityViewModel.availabilityError.collectAsState()

    val bookingLoading by bookingViewModel.bookingLoading.collectAsState()
    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
    val bookingError by bookingViewModel.bookingError.collectAsState()

    val userProfile by postsViewModel.userProfile.collectAsState()
    val profileLoading by postsViewModel.profileLoading.collectAsState()

    val otherUserProfiles by postsViewModel.otherUserProfiles.collectAsState()
    val mentorProfile = otherUserProfiles[userId]

    var selectedDuration by remember { mutableStateOf<Int?>(null) }
    var selectedServiceSlotId by remember { mutableStateOf<Int?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedTimeSlot by remember { mutableStateOf<Triple<Int, String, String>?>(null) }

    var emailAddress by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showProcessingDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var isSlotSectionExpanded by remember { mutableStateOf(true) }
    var isDateSectionExpanded by remember { mutableStateOf(true) }
    var isTimeSectionExpanded by remember { mutableStateOf(true) }

    // Firebase: Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView(
            screenName = "BookingSlot",
            screenClass = "BookingSlot"
        )

        FirebaseAnalyticsHelper.logEvent(
            "booking_screen_viewed",
            mapOf(
                "mentor_id" to userId.toString(),
                "mentor_name" to influencerName,
                "source" to "booking_flow"
            )
        )

        postsViewModel.fetchUserProfile()
    }

    LaunchedEffect(userId) {
        postsViewModel.fetchUserProfileById(userId)
        availabilityViewModel.fetchMentorAvailability(userId)
    }

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            emailAddress = profile.email
            name = profile.full_name ?: ""
        }
    }

    LaunchedEffect(bookingLoading) {
        if (bookingLoading) {
            showConfirmDialog = false
            showProcessingDialog = true
        }
    }

    LaunchedEffect(bookingSuccess) {
        if (bookingSuccess != null) {
            showProcessingDialog = false
            showSuccessDialog = true

            // Firebase: Booking successful
            val selectedSlot = mentorAvailability?.serviceSlots?.find { it.id == selectedServiceSlotId }
            FirebaseAnalyticsHelper.logEvent(
                "booking_completed_success",
                mapOf(
                    "mentor_id" to userId.toString(),
                    "mentor_name" to influencerName,
                    "duration" to (selectedDuration?.toString() ?: "unknown"),
                    "price" to (selectedSlot?.price?.toString() ?: "unknown"),
                    "date" to (selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "unknown"),
                    "time_slot" to "${selectedTimeSlot?.second ?: ""}-${selectedTimeSlot?.third ?: ""}",
                    "topic" to topic
                )
            )
        }
    }

    LaunchedEffect(bookingError) {
        if (bookingError != null) {
            showProcessingDialog = false

            // Firebase: Booking failed
            FirebaseAnalyticsHelper.logEvent(
                "booking_failed",
                mapOf(
                    "mentor_id" to userId.toString(),
                    "mentor_name" to influencerName,
                    "error" to (bookingError ?: "unknown_error")
                )
            )
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (isLoading || profileLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: $error", color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .padding(horizontal = 16.dp)
            ) {
                // Profile Header Section
                item {
                    mentorProfile?.let { profile ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                AsyncImage(
                                    model = profile.profile_pic ?: "",
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE5E7EB)),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.ic_profile1)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = profile.full_name ?: profile.username,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "Content creator",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )

                                    profile.bio?.let { bio ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = bio,
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            CustomOutlinedButton("About") {
                                // Firebase: About button clicked
                                FirebaseAnalyticsHelper.logEvent(
                                    "booking_about_clicked",
                                    mapOf(
                                        "mentor_id" to userId.toString(),
                                        "mentor_name" to influencerName,
                                        "source" to "booking_screen"
                                    )
                                )

                                navController.navigate("about_section/$userId/$influencerName")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE5E7EB))
                        )
                    }
                }

                // Slot Selection Section
                item {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSlotSectionExpanded = !isSlotSectionExpanded

                                    // Firebase: Slot section toggled
                                    FirebaseAnalyticsHelper.logEvent(
                                        "slot_section_toggled",
                                        mapOf(
                                            "expanded" to isSlotSectionExpanded.toString(),
                                            "mentor_id" to userId.toString()
                                        )
                                    )
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Choose your slot",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                if (!isSlotSectionExpanded && selectedDuration != null) {
                                    val selectedSlot = mentorAvailability?.serviceSlots?.find { it.id == selectedServiceSlotId }
                                    selectedSlot?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "$selectedDuration Min - ₹ ${it.price.toInt()}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF893BCF)
                                        )
                                    }
                                }
                            }

                            Icon(
                                imageVector = if (isSlotSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isSlotSectionExpanded) "Collapse" else "Expand",
                                tint = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isSlotSectionExpanded) 16.dp else 4.dp))

                        if (isSlotSectionExpanded) {
                            mentorAvailability?.serviceSlots?.let { slots ->
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.heightIn(max = 200.dp)
                                ) {
                                    items(slots.filter { it.isActive }) { slot ->
                                        DurationCard(
                                            duration = slot.duration,
                                            price = slot.price,
                                            isSelected = selectedDuration == slot.duration,
                                            onClick = {
                                                selectedDuration = slot.duration
                                                selectedServiceSlotId = slot.id
                                                selectedDate = null
                                                selectedTimeSlot = null
                                                isSlotSectionExpanded = false

                                                // Firebase: Slot selected
                                                FirebaseAnalyticsHelper.logEvent(
                                                    "slot_duration_selected",
                                                    mapOf(
                                                        "mentor_id" to userId.toString(),
                                                        "mentor_name" to influencerName,
                                                        "duration" to slot.duration.toString(),
                                                        "price" to slot.price.toString(),
                                                        "slot_id" to slot.id.toString()
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Date Selection Section
                item {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isDateSectionExpanded = !isDateSectionExpanded

                                    // Firebase: Date section toggled
                                    FirebaseAnalyticsHelper.logEvent(
                                        "date_section_toggled",
                                        mapOf(
                                            "expanded" to isDateSectionExpanded.toString(),
                                            "mentor_id" to userId.toString()
                                        )
                                    )
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Select Date",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                if (!isDateSectionExpanded && selectedDate != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        selectedDate!!.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF893BCF)
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isDateSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isDateSectionExpanded) "Collapse" else "Expand",
                                tint = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(if (isDateSectionExpanded) 16.dp else 4.dp))

                        if (isDateSectionExpanded) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    currentMonth = currentMonth.minusMonths(1)

                                    // Firebase: Month navigation
                                    FirebaseAnalyticsHelper.logEvent(
                                        "calendar_month_changed",
                                        mapOf(
                                            "direction" to "previous",
                                            "month" to currentMonth.toString(),
                                            "mentor_id" to userId.toString()
                                        )
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.KeyboardArrowLeft,
                                        contentDescription = "Previous Month"
                                    )
                                }
                                Text(
                                    "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                IconButton(onClick = {
                                    currentMonth = currentMonth.plusMonths(1)

                                    // Firebase: Month navigation
                                    FirebaseAnalyticsHelper.logEvent(
                                        "calendar_month_changed",
                                        mapOf(
                                            "direction" to "next",
                                            "month" to currentMonth.toString(),
                                            "mentor_id" to userId.toString()
                                        )
                                    )
                                }) {
                                    Icon(
                                        Icons.Default.KeyboardArrowRight,
                                        contentDescription = "Next Month"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            CalendarGrid(
                                currentMonth = currentMonth,
                                selectedDate = selectedDate,
                                selectedDuration = selectedDuration,
                                availableTimeSlots = mentorAvailability?.availableTimeSlots,
                                onDateSelected = {
                                    selectedDate = it
                                    selectedTimeSlot = null
                                    isDateSectionExpanded = false

                                    // Firebase: Date selected
                                    FirebaseAnalyticsHelper.logEvent(
                                        "booking_date_selected",
                                        mapOf(
                                            "mentor_id" to userId.toString(),
                                            "mentor_name" to influencerName,
                                            "date" to it.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                            "day_of_week" to it.dayOfWeek.toString()
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                // Time Slot Section
                item {
                    if (selectedDate != null && selectedDuration != null) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        isTimeSectionExpanded = !isTimeSectionExpanded

                                        // Firebase: Time section toggled
                                        FirebaseAnalyticsHelper.logEvent(
                                            "time_section_toggled",
                                            mapOf(
                                                "expanded" to isTimeSectionExpanded.toString(),
                                                "mentor_id" to userId.toString()
                                            )
                                        )
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Available Time",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )

                                    if (!isTimeSectionExpanded && selectedTimeSlot != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "${selectedTimeSlot!!.second} - ${selectedTimeSlot!!.third}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF893BCF)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isTimeSectionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isTimeSectionExpanded) "Collapse" else "Expand",
                                    tint = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (isTimeSectionExpanded) {
                                val dateKey = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val timeSlots = mentorAvailability?.availableTimeSlots?.get(dateKey)
                                val slotsForDuration = when (selectedDuration) {
                                    15 -> timeSlots?.fifteenMin
                                    30 -> timeSlots?.thirtyMin
                                    45 -> timeSlots?.fortyFiveMin
                                    else -> null
                                }

                                if (slotsForDuration != null && slotsForDuration.isNotEmpty()) {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.heightIn(max = 300.dp)
                                    ) {
                                        items(slotsForDuration) { slot ->
                                            TimeSlotCard(
                                                startTime = slot.startTime,
                                                endTime = slot.endTime,
                                                isSelected = selectedTimeSlot?.second == slot.startTime && selectedTimeSlot?.third == slot.endTime,
                                                onClick = {
                                                    selectedTimeSlot = Triple(slot.timeSlotId, slot.startTime, slot.endTime)
                                                    isTimeSectionExpanded = false

                                                    // Firebase: Time slot selected
                                                    FirebaseAnalyticsHelper.logEvent(
                                                        "time_slot_selected",
                                                        mapOf(
                                                            "mentor_id" to userId.toString(),
                                                            "mentor_name" to influencerName,
                                                            "start_time" to slot.startTime,
                                                            "end_time" to slot.endTime,
                                                            "time_slot_id" to slot.timeSlotId.toString(),
                                                            "date" to dateKey
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        "No available slots for this date",
                                        fontSize = 14.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Booking Details Section
                item {
                    if (selectedTimeSlot != null) {
                        Column {
                            Text(
                                "Booking Details",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Enter Email Address",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = emailAddress,
                                onValueChange = {
                                    emailAddress = it

                                    // Firebase: Email field interaction (only log once when they start typing)
                                    if (it.length == 1) {
                                        FirebaseAnalyticsHelper.logEvent(
                                            "booking_email_started",
                                            mapOf(
                                                "mentor_id" to userId.toString()
                                            )
                                        )
                                    }
                                },
                                placeholder = { Text("Enter your email", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !bookingLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it

                                    // Firebase: Name field interaction
                                    if (it.length == 1) {
                                        FirebaseAnalyticsHelper.logEvent(
                                            "booking_name_started",
                                            mapOf(
                                                "mentor_id" to userId.toString()
                                            )
                                        )
                                    }
                                },
                                placeholder = { Text("Enter your name", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !bookingLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Topic",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = topic,
                                onValueChange = {
                                    if (it.length <= 50) {
                                        topic = it

                                        // Firebase: Topic field interaction
                                        if (it.length == 1) {
                                            FirebaseAnalyticsHelper.logEvent(
                                                "booking_topic_started",
                                                mapOf(
                                                    "mentor_id" to userId.toString()
                                                )
                                            )
                                        }
                                    }
                                },
                                placeholder = { Text("Add your topic", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !bookingLoading,
                                supportingText = {
                                    Text(
                                        "${topic.length}/50",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Description (Optional)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    if (it.length <= 150) {
                                        description = it

                                        // Firebase: Description field interaction
                                        if (it.length == 1) {
                                            FirebaseAnalyticsHelper.logEvent(
                                                "booking_description_started",
                                                mapOf(
                                                    "mentor_id" to userId.toString()
                                                )
                                            )
                                        }
                                    }
                                },
                                placeholder = { Text("Add your description", color = Color.Gray) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE5E7EB),
                                    unfocusedBorderColor = Color(0xFFE5E7EB),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 4,
                                enabled = !bookingLoading,
                                supportingText = {
                                    Text(
                                        "${description.length}/150",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.End
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (bookingError != null && !showProcessingDialog) {
                                Text(
                                    bookingError!!,
                                    fontSize = 14.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val isButtonEnabled = !bookingLoading && emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .background(
                                        brush = if (isButtonEnabled) {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF893BCF),
                                                    Color(0xFFEA3BA1)
                                                )
                                            )
                                        } else {
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFE5E7EB),
                                                    Color(0xFFE5E7EB)
                                                )
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable(enabled = isButtonEnabled) {
                                        showConfirmDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Book Session",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isButtonEnabled) Color.White else Color(0xFF9CA3AF)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }

        if (showConfirmDialog) {
            val selectedSlot = mentorAvailability?.serviceSlots?.find { it.id == selectedServiceSlotId }
            val formattedDate = selectedDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: ""

            ConfirmBookingDialog(
                mentorName = influencerName,
                topic = topic,
                date = formattedDate,
                duration = selectedDuration ?: 0,
                startTime = selectedTimeSlot?.second ?: "",
                endTime = selectedTimeSlot?.third ?: "",
                bookedBy = name,
                email = emailAddress,
                price = selectedSlot?.price ?: 0.0,
                mentorUserId = userId,
                timeSlotId = selectedTimeSlot?.first ?: 0,
                serviceSlotId = selectedServiceSlotId ?: 0,
                description = description,
                bookingViewModel = bookingViewModel,
                onDismiss = { showConfirmDialog = false },
                onConfirm = { }
            )
        }

        if (showProcessingDialog) {
            ProcessingBookingDialog()
        }

        if (showSuccessDialog) {
            SuccessBookingDialog(
                navController = navController,
                onDismiss = {
                    showSuccessDialog = false
                    bookingViewModel.clearBookingState()
                    navController.popBackStack()
                }
            )
        }
    }
}




@Composable
fun ConfirmBookingDialog(
    mentorName: String,
    topic: String,
    date: String,
    duration: Int,
    startTime: String,
    endTime: String,
    bookedBy: String,
    email: String,
    price: Double,
    mentorUserId: Int,
    timeSlotId: Int,
    serviceSlotId: Int,
    description: String = "",
    bookingViewModel: BookingViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Firebase: Track dialog view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent(
            "confirm_booking_dialog_opened",
            mapOf(
                "mentor_id" to mentorUserId.toString(),
                "mentor_name" to mentorName,
                "duration" to duration.toString(),
                "price" to price.toString(),
                "topic" to topic,
                "date" to date,
                "time" to "$startTime-$endTime"
            )
        )
    }

    Dialog(onDismissRequest = {
        // Firebase: Dialog dismissed
        FirebaseAnalyticsHelper.logEvent(
            "confirm_booking_dialog_dismissed",
            mapOf(
                "mentor_id" to mentorUserId.toString(),
                "mentor_name" to mentorName,
                "dismiss_method" to "outside_click"
            )
        )
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Confirm Your Booking",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Please review your booking details below",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = {
                                // Firebase: Close button clicked
                                FirebaseAnalyticsHelper.logEvent(
                                    "confirm_booking_dialog_dismissed",
                                    mapOf(
                                        "mentor_id" to mentorUserId.toString(),
                                        "mentor_name" to mentorName,
                                        "dismiss_method" to "close_button"
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Booking Details Section
                    Text(
                        "Booking Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Amount Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFAF5FF),
                                        Color(0xFFFCE7F3)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Total Amount",
                                fontSize = 14.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "₹${price.toInt()}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7E22CE)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFBFDBFE),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(8.dp)
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF1D4ED8),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "This amount will be deducted from your account once booking is confirmed",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1D4ED8),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Details Grid
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailCard(
                                label = "Topic",
                                value = topic,
                                modifier = Modifier.weight(1f)
                            )
                            DetailCard(
                                label = "Date",
                                value = date,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailCard(
                                label = "Duration",
                                value = "$duration minutes",
                                modifier = Modifier.weight(1f)
                            )
                            DetailCard(
                                label = "Time",
                                value = "$startTime - $endTime",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailCard(
                                label = "Mentor",
                                value = mentorName,
                                modifier = Modifier.weight(1f)
                            )
                            DetailCard(
                                label = "Booked by",
                                value = "$bookedBy\n$email",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                // Firebase: Cancel button clicked
                                FirebaseAnalyticsHelper.logEvent(
                                    "booking_cancelled",
                                    mapOf(
                                        "mentor_id" to mentorUserId.toString(),
                                        "mentor_name" to mentorName,
                                        "duration" to duration.toString(),
                                        "price" to price.toString(),
                                        "cancellation_stage" to "confirmation_dialog"
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3F4F6)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancel",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF374151)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF893BCF),
                                            Color(0xFFEA3BA1)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Firebase: Confirm button clicked
                                    FirebaseAnalyticsHelper.logEvent(
                                        "booking_confirmed",
                                        mapOf(
                                            "mentor_id" to mentorUserId.toString(),
                                            "mentor_name" to mentorName,
                                            "topic" to topic,
                                            "duration" to duration.toString(),
                                            "price" to price.toString(),
                                            "date" to date,
                                            "time_slot" to "$startTime-$endTime",
                                            "service_slot_id" to serviceSlotId.toString(),
                                            "time_slot_id" to timeSlotId.toString(),
                                            "has_description" to description.isNotBlank().toString()
                                        )
                                    )

                                    bookingViewModel.bookLiveSessionSlot(
                                        mentorUserId = mentorUserId,
                                        timeSlotId = timeSlotId,
                                        topic = topic,
                                        startTime = startTime,
                                        endTime = endTime,
                                        description = description,
                                        serviceSlotId = serviceSlotId,
                                        seekerEmail = email,
                                        name = bookedBy
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Confirm",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun DetailCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 14.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp
            )
        }
    }
}





@Composable
fun ProcessingBookingDialog() {
    // Firebase: Track processing dialog view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent(
            "booking_processing_dialog_shown",
            mapOf(
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }

    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Processing payment and booking your session...",
                    fontSize = 18.sp,
                    color = Color(0xFF2563EB),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(3) { index ->
                        val infiniteTransition = rememberInfiniteTransition(label = "dot$index")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.5f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse,
                                initialStartOffset = StartOffset(index * 200)
                            ),
                            label = "scale$index"
                        )

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .scale(scale)
                                .background(
                                    Color(0xFFFBBF24),
                                    CircleShape
                                )
                        )

                        if (index < 2) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }
            }
        }
    }
}






@Composable
fun SuccessBookingDialog(
    navController: NavController,
    onDismiss: () -> Unit
) {
    // Firebase: Track success dialog view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent(
            "booking_success_dialog_shown",
            mapOf(
                "timestamp" to System.currentTimeMillis().toString()
            )
        )
    }

    Dialog(onDismissRequest = {
        // Firebase: Dialog dismissed
        FirebaseAnalyticsHelper.logEvent(
            "booking_success_dialog_dismissed",
            mapOf(
                "dismiss_method" to "outside_click"
            )
        )
        onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                        .padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Session Booked Successfully!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Meeting links have been shared to your email addresses",
                        fontSize = 15.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            // Firebase: Done button clicked
                            FirebaseAnalyticsHelper.logEvent(
                                "booking_success_done_clicked",
                                mapOf(
                                    "navigation_destination" to "live_session"
                                )
                            )

                            onDismiss()
                            navController.navigate(Screen.LiveSession.route)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Done",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Close Button at top-right
                IconButton(
                    onClick = {
                        // Firebase: Close button clicked
                        FirebaseAnalyticsHelper.logEvent(
                            "booking_success_dialog_dismissed",
                            mapOf(
                                "dismiss_method" to "close_button"
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF3F4F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}





@Composable
fun DurationCard(
    duration: Int,
    price: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF893BCF),
                            Color(0xFFEA3BA1)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.White)
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable {
                // Firebase: Duration card clicked
                FirebaseAnalyticsHelper.logEvent(
                    "duration_card_clicked",
                    mapOf(
                        "duration" to duration.toString(),
                        "price" to price.toString(),
                        "was_selected" to isSelected.toString()
                    )
                )
                onClick()
            }
            .padding(16.dp)
    ) {
        Column {
            Text(
                "$duration Min - ₹ ${price.toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else Color.Black
            )
        }
    }
}


@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    selectedDuration: Int?,
    availableTimeSlots: Map<String, com.cc.creatorcircle.data.models.DurationSlots>?,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()

    Column {
        // Week day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days
        var dayCounter = 1
        for (week in 0..5) {
            if (dayCounter > daysInMonth) break

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    if ((week == 0 && dayOfWeek < firstDayOfWeek) || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val date = currentMonth.atDay(dayCounter)
                        val dateKey = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

                        // Check if date exists in response
                        val dateExistsInResponse = availableTimeSlots?.containsKey(dateKey) == true

                        val hasSlots = selectedDuration != null &&
                                availableTimeSlots?.get(dateKey)?.let { slots ->
                                    when (selectedDuration) {
                                        15 -> slots.fifteenMin?.isNotEmpty() == true
                                        30 -> slots.thirtyMin?.isNotEmpty() == true
                                        45 -> slots.fortyFiveMin?.isNotEmpty() == true
                                        else -> false
                                    }
                                } == true

                        CalendarDay(
                            day = dayCounter,
                            date = date,
                            isSelected = selectedDate == date,
                            hasSlots = hasSlots,
                            dateExistsInResponse = dateExistsInResponse,
                            isCurrentMonth = true,
                            selectedDuration = selectedDuration,
                            onDateSelected = {
                                if ((hasSlots || selectedDuration == null) && dateExistsInResponse) {
                                    // Firebase: Date selected
                                    FirebaseAnalyticsHelper.logEvent(
                                        "booking_date_selected",
                                        mapOf(
                                            "date" to dateKey,
                                            "day" to dayCounter.toString(),
                                            "month" to currentMonth.month.toString(),
                                            "year" to currentMonth.year.toString(),
                                            "has_slots" to hasSlots.toString(),
                                            "selected_duration" to (selectedDuration?.toString() ?: "none")
                                        )
                                    )
                                    onDateSelected(date)
                                }
                            }
                        )
                        dayCounter++
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun RowScope.CalendarDay(
    day: Int,
    date: LocalDate,
    isSelected: Boolean,
    hasSlots: Boolean,
    dateExistsInResponse: Boolean,
    isCurrentMonth: Boolean,
    selectedDuration: Int?,
    onDateSelected: () -> Unit
) {
    val today = LocalDate.now()
    val isPast = date.isBefore(today)
    val isDisabled = isPast || !dateExistsInResponse

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                brush = when {
                    isSelected -> Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF893BCF),
                            Color(0xFFEA3BA1)
                        )
                    )

                    date == today && dateExistsInResponse -> Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFDDD6FE),
                            Color(0xFFDDD6FE)
                        )
                    )

                    else -> Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent
                        )
                    )
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = !isDisabled) {
                onDateSelected()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 14.sp,
            color = when {
                isDisabled -> Color(0xFFD1D5DB)
                isSelected -> Color.White
                else -> Color.Black
            },
            fontWeight = if (hasSlots && !isDisabled) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TimeSlotCard(
    startTime: String,
    endTime: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = if (isSelected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF893BCF),
                            Color(0xFFEA3BA1)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.White)
                    )
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF7C3AED) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                // Firebase: Time slot selected
                FirebaseAnalyticsHelper.logEvent(
                    "time_slot_selected",
                    mapOf(
                        "start_time" to startTime,
                        "end_time" to endTime,
                        "time_slot" to "$startTime-$endTime",
                        "was_selected" to isSelected.toString()
                    )
                )
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "$startTime - $endTime",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Black
        )
    }
}

fun getDaySuffix(day: Int): String {
    return when {
        day in 11..13 -> "th"
        day % 10 == 1 -> "st"
        day % 10 == 2 -> "nd"
        day % 10 == 3 -> "rd"
        else -> "th"
    }
}



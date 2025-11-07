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

    // Add state for mentor profile
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

    LaunchedEffect(Unit) {
        postsViewModel.fetchUserProfile()
    }

    // Fetch mentor profile by ID
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
        }
    }

    LaunchedEffect(bookingError) {
        if (bookingError != null) {
            showProcessingDialog = false
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
                                // Profile Picture
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

                                // Name and Bio
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


//                            Box(
//                                modifier = Modifier
//                                    .border(
//                                        width = 1.dp,
//                                        color = Color(0xFFE5E7EB),
//                                        shape = RoundedCornerShape(20.dp)
//                                    )
//                                    .clickable {
//                                        // Navigate to About/Reviews page
//                                        navController.navigate("about_section/$userId/$influencerName")
//                                    }
//                                    .padding(horizontal = 16.dp, vertical = 8.dp)
//                            ) {
//                                Text(
//                                    text = "About",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black
//                                )
//                            }

                            CustomOutlinedButton("About") {
                                navController.navigate("about_section/$userId/$influencerName")
                            }

                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Divider
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
                                .clickable { isSlotSectionExpanded = !isSlotSectionExpanded }
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
                                .clickable { isDateSectionExpanded = !isDateSectionExpanded }
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
                                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
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
                                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
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
                                    .clickable { isTimeSectionExpanded = !isTimeSectionExpanded }
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
                                onValueChange = { emailAddress = it },
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
                                onValueChange = { name = it },
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
                                    if (it.length <= 50) topic = it
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
                                    if (it.length <= 150) description = it
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
    Dialog(onDismissRequest = onDismiss) {
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
                            onClick = onDismiss,
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
                            onClick = onDismiss,
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








//
//
//@Composable
//fun BookingSlot(
//    navController: NavController,
//    userId: Int
//) {
//    val context = LocalContext.current
//
//    val availabilityViewModel: AvailabilityViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return AvailabilityViewModel(context) as T
//            }
//        }
//    )
//
//    val bookingViewModel: BookingViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return BookingViewModel(context) as T
//            }
//        }
//    )
//
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return PostsViewModel(context) as T
//            }
//        }
//    )
//
//    val mentorAvailability by availabilityViewModel.mentorAvailability.collectAsState()
//    val isLoading by availabilityViewModel.availabilityLoading.collectAsState()
//    val error by availabilityViewModel.availabilityError.collectAsState()
//
//    val bookingLoading by bookingViewModel.bookingLoading.collectAsState()
//    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
//    val bookingError by bookingViewModel.bookingError.collectAsState()
//
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val profileLoading by postsViewModel.profileLoading.collectAsState()
//
//    var selectedDuration by remember { mutableStateOf<Int?>(null) }
//    var selectedServiceSlotId by remember { mutableStateOf<Int?>(null) }
//    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
//    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
//    var selectedTimeSlot by remember { mutableStateOf<Triple<Int, String, String>?>(null) }
//
//    var emailAddress by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var topic by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//
//    var showConfirmDialog by remember { mutableStateOf(false) }
//    var showProcessingDialog by remember { mutableStateOf(false) }
//    var showSuccessDialog by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        postsViewModel.fetchUserProfile()
//    }
//
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            emailAddress = profile.email
//            name = profile.full_name ?: ""
//        }
//    }
//
//    LaunchedEffect(userId) {
//        availabilityViewModel.fetchMentorAvailability(userId)
//    }
//
//    LaunchedEffect(bookingLoading) {
//        if (bookingLoading) {
//            showConfirmDialog = false
//            showProcessingDialog = true
//        }
//    }
//
//    LaunchedEffect(bookingSuccess) {
//        if (bookingSuccess != null) {
//            showProcessingDialog = false
//            showSuccessDialog = true
//        }
//    }
//
//    LaunchedEffect(bookingError) {
//        if (bookingError != null) {
//            showProcessingDialog = false
//        }
//    }
//
//    Scaffold(
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        if (isLoading || profileLoading) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (error != null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Error: $error", color = Color.Red)
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .padding(horizontal = 16.dp)
//            ) {
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        "Choose your slot",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//
//                item {
//                    mentorAvailability?.serviceSlots?.let { slots ->
//                        LazyVerticalGrid(
//                            columns = GridCells.Fixed(2),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp),
//                            modifier = Modifier.heightIn(max = 200.dp)
//                        ) {
//                            items(slots.filter { it.isActive }) { slot ->
//                                DurationCard(
//                                    duration = slot.duration,
//                                    price = slot.price,
//                                    isSelected = selectedDuration == slot.duration,
//                                    onClick = {
//                                        selectedDuration = slot.duration
//                                        selectedServiceSlotId = slot.id
//                                        selectedDate = null
//                                        selectedTimeSlot = null
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                item {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowLeft,
//                                contentDescription = "Previous Month"
//                            )
//                        }
//                        Text(
//                            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowRight,
//                                contentDescription = "Next Month"
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                item {
//                    CalendarGrid(
//                        currentMonth = currentMonth,
//                        selectedDate = selectedDate,
//                        selectedDuration = selectedDuration,
//                        availableTimeSlots = mentorAvailability?.availableTimeSlots,
//                        onDateSelected = {
//                            selectedDate = it
//                            selectedTimeSlot = null
//                        }
//                    )
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                item {
//                    if (selectedDate != null && selectedDuration != null) {
//                        Text(
//                            "${selectedDate!!.dayOfMonth}${getDaySuffix(selectedDate!!.dayOfMonth)} ${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            "Available Time",
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        val dateKey = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
//                        val timeSlots = mentorAvailability?.availableTimeSlots?.get(dateKey)
//                        val slotsForDuration = when (selectedDuration) {
//                            15 -> timeSlots?.fifteenMin
//                            30 -> timeSlots?.thirtyMin
//                            45 -> timeSlots?.fortyFiveMin
//                            else -> null
//                        }
//
//                        if (slotsForDuration != null && slotsForDuration.isNotEmpty()) {
//                            LazyVerticalGrid(
//                                columns = GridCells.Fixed(2),
//                                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                                verticalArrangement = Arrangement.spacedBy(12.dp),
//                                modifier = Modifier.heightIn(max = 500.dp)
//                            ) {
//                                items(slotsForDuration) { slot ->
//                                    TimeSlotCard(
//                                        startTime = slot.startTime,
//                                        endTime = slot.endTime,
//                                        isSelected = selectedTimeSlot?.second == slot.startTime && selectedTimeSlot?.third == slot.endTime,
//                                        onClick = {
//                                            selectedTimeSlot = Triple(slot.timeSlotId, slot.startTime, slot.endTime)
//                                        }
//                                    )
//                                }
//                            }
//                        } else {
//                            Text(
//                                "No available slots for this date",
//                                fontSize = 14.sp,
//                                color = Color.Gray,
//                                modifier = Modifier.padding(vertical = 16.dp)
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                item {
//                    if (selectedTimeSlot != null) {
//                        Column {
//                            Text(
//                                "Booking Details",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            Text(
//                                "Enter Email Address",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = emailAddress,
//                                onValueChange = { emailAddress = it },
//                                placeholder = { Text("Enter your email", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            Text(
//                                "Name",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = name,
//                                onValueChange = { name = it },
//                                placeholder = { Text("Enter your name", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            Text(
//                                "Topic",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = topic,
//                                onValueChange = {
//                                    if (it.length <= 50) topic = it
//                                },
//                                placeholder = { Text("Add your topic", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading,
//                                supportingText = {
//                                    Text(
//                                        "${topic.length}/50",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            Text(
//                                "Description (Optional)",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = description,
//                                onValueChange = {
//                                    if (it.length <= 150) description = it
//                                },
//                                placeholder = { Text("Add your description", color = Color.Gray) },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 100.dp),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                maxLines = 4,
//                                enabled = !bookingLoading,
//                                supportingText = {
//                                    Text(
//                                        "${description.length}/150",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            if (bookingError != null && !showProcessingDialog) {
//                                Text(
//                                    bookingError!!,
//                                    fontSize = 14.sp,
//                                    color = Color.Red,
//                                    modifier = Modifier.padding(vertical = 8.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            val isButtonEnabled = !bookingLoading && emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()
//
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(50.dp)
//                                    .background(
//                                        brush = if (isButtonEnabled) {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFF893BCF),
//                                                    Color(0xFFEA3BA1)
//                                                )
//                                            )
//                                        } else {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFFE5E7EB),
//                                                    Color(0xFFE5E7EB)
//                                                )
//                                            )
//                                        },
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .clickable(enabled = isButtonEnabled) {
//                                        showConfirmDialog = true
//                                    },
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    "Book Session",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = if (isButtonEnabled) Color.White else Color(0xFF9CA3AF)
//                                )
//                            }
//                            Spacer(modifier = Modifier.height(24.dp))
//                        }
//                    }
//                }
//            }
//        }
//
//        if (showConfirmDialog) {
//            val selectedSlot = mentorAvailability?.serviceSlots?.find { it.id == selectedServiceSlotId }
//            val formattedDate = selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: ""
//
//            ConfirmBookingDialog(
//                mentorName ="Mentor",
//                topic = topic,
//                date = formattedDate,
//                duration = selectedDuration ?: 0,
//                startTime = selectedTimeSlot?.second ?: "",
//                endTime = selectedTimeSlot?.third ?: "",
//                bookedBy = name,
//                email = emailAddress,
//                price = selectedSlot?.price ?: 0.0,
//                onDismiss = { showConfirmDialog = false },
//                onConfirm = {
//                    selectedTimeSlot?.let { (timeSlotId, startTime, endTime) ->
//                        selectedServiceSlotId?.let { serviceSlotId ->
//                            bookingViewModel.bookLiveSessionSlot(
//                                mentorUserId = userId,
//                                timeSlotId = timeSlotId,
//                                topic = topic,
//                                startTime = startTime,
//                                endTime = endTime,
//                                description = description,
//                                serviceSlotId = serviceSlotId,
//                                seekerEmail = emailAddress,
//                                name = name
//                            )
//                        }
//                    }
//                }
//            )
//        }
//
//        if (showProcessingDialog) {
//            ProcessingBookingDialog()
//        }
//
//        if (showSuccessDialog) {
//            SuccessBookingDialog(
//                onDismiss = {
//                    showSuccessDialog = false
//                    bookingViewModel.clearBookingState()
//                    navController.popBackStack()
//                }
//            )
//        }
//    }
//}
//
//
//
//
//
//
//
//@Composable
//fun ConfirmBookingDialog(
//    mentorName: String,
//    topic: String,
//    date: String,
//    duration: Int,
//    startTime: String,
//    endTime: String,
//    bookedBy: String,
//    email: String,
//    price: Double,
//    mentorUserId: String,
//    timeSlotId: String,
//    serviceSlotId: String,
//    description: String = "",
//    bookingViewModel: BookingViewModel,
//    onDismiss: () -> Unit,
//    onConfirm: () -> Unit
//) {
//    var showProcessing by remember { mutableStateOf(false) }
//    var showSuccess by remember { mutableStateOf(false) }
//
//    val bookingState by bookingViewModel.bookingState.collectAsState()
//
//    LaunchedEffect(bookingState) {
//        when (bookingState) {
//            is BookingState.Loading -> {
//                showProcessing = true
//            }
//            is BookingState.Success -> {
//                showProcessing = false
//                showSuccess = true
//            }
//            is BookingState.Error -> {
//                showProcessing = false
//                // Handle error if needed
//            }
//            else -> {}
//        }
//    }
//
//    when {
//        showSuccess -> {
//            SuccessBookingDialog(
//                onDismiss = {
//                    showSuccess = false
//                    onConfirm()
//                    onDismiss()
//                }
//            )
//        }
//        showProcessing -> {
//            ProcessingBookingDialog()
//        }
//        else -> {
//            Dialog(onDismissRequest = onDismiss) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .wrapContentHeight(),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White)
//                ) {
//                    Column(
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(1f, fill = false)
//                                .verticalScroll(rememberScrollState())
//                                .padding(24.dp)
//                        ) {
//                            // Header
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Column {
//                                    Text(
//                                        "Confirm Your Booking",
//                                        fontSize = 20.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color.Black
//                                    )
//                                    Spacer(modifier = Modifier.height(4.dp))
//                                    Text(
//                                        "Please review your booking details below",
//                                        fontSize = 14.sp,
//                                        color = Color.Gray
//                                    )
//                                }
//                                IconButton(
//                                    onClick = onDismiss,
//                                    modifier = Modifier.size(32.dp)
//                                ) {
//                                    Icon(
//                                        Icons.Default.Close,
//                                        contentDescription = "Close",
//                                        tint = Color.Gray
//                                    )
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(24.dp))
//
//                            // Booking Details Section
//                            Text(
//                                "Booking Details",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.Black
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Total Amount Card
//                            Card(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(8.dp)
//                                    .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(12.dp))
//                                    .background(
//                                        brush = Brush.horizontalGradient(
//                                            colors = listOf(
//                                                Color(0xFFFAF5FF),
//                                                Color(0xFFFCE7F3)
//                                            )
//                                        ),
//                                        shape = RoundedCornerShape(12.dp)
//                                    ),
//                                shape = RoundedCornerShape(12.dp),
//                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//                            ) {
//                                Column(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(16.dp),
//                                    horizontalAlignment = Alignment.CenterHorizontally
//                                ) {
//                                    Text(
//                                        "Total Amount",
//                                        fontSize = 14.sp,
//                                        color = Color.Black,
//                                        fontWeight = FontWeight.Medium
//                                    )
//                                    Spacer(modifier = Modifier.height(4.dp))
//                                    Text(
//                                        "₹${price.toInt()}",
//                                        fontSize = 32.sp,
//                                        fontWeight = FontWeight.Bold,
//                                        color = Color(0xFF7E22CE)
//                                    )
//                                    Spacer(modifier = Modifier.height(12.dp))
//
//                                    Row(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .background(
//                                                color = Color(0xFFEFF6FF),
//                                                shape = RoundedCornerShape(6.dp)
//                                            )
//                                            .border(
//                                                width = 1.dp,
//                                                color = Color(0xFFBFDBFE),
//                                                shape = RoundedCornerShape(6.dp)
//                                            )
//                                            .padding(8.dp)
//                                            .padding(top = 8.dp),
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.Center
//                                    ) {
//                                        Icon(
//                                            Icons.Default.Info,
//                                            contentDescription = null,
//                                            tint = Color(0xFF1D4ED8),
//                                            modifier = Modifier.size(20.dp)
//                                        )
//                                        Spacer(modifier = Modifier.width(8.dp))
//                                        Text(
//                                            "This amount will be deducted from your account once booking is confirmed",
//                                            fontSize = 12.sp,
//                                            color = Color(0xFF1D4ED8),
//                                            lineHeight = 16.sp
//                                        )
//                                    }
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Details Grid
//                            Column(
//                                modifier = Modifier.fillMaxWidth(),
//                                verticalArrangement = Arrangement.spacedBy(12.dp)
//                            ) {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                                ) {
//                                    DetailCard(
//                                        label = "Topic",
//                                        value = topic,
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                    DetailCard(
//                                        label = "Date",
//                                        value = date,
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                }
//
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                                ) {
//                                    DetailCard(
//                                        label = "Duration",
//                                        value = "$duration minutes",
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                    DetailCard(
//                                        label = "Time",
//                                        value = "$startTime - $endTime",
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                }
//
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                                ) {
//                                    DetailCard(
//                                        label = "Mentor",
//                                        value = mentorName,
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                    DetailCard(
//                                        label = "Booked by",
//                                        value = "$bookedBy\n$email",
//                                        modifier = Modifier.weight(1f)
//                                    )
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(24.dp))
//
//                            // Action Buttons
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(12.dp)
//                            ) {
//                                Button(
//                                    onClick = onDismiss,
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(50.dp),
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color(0xFFF3F4F6)
//                                    ),
//                                    shape = RoundedCornerShape(8.dp)
//                                ) {
//                                    Text(
//                                        "Cancel",
//                                        fontSize = 14.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color(0xFF374151)
//                                    )
//                                }
//
//                                Box(
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .height(50.dp)
//                                        .background(
//                                            brush = Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFF893BCF),
//                                                    Color(0xFFEA3BA1)
//                                                )
//                                            ),
//                                            shape = RoundedCornerShape(8.dp)
//                                        )
//                                        .clickable {
//                                            bookingViewModel.bookLiveSessionSlot(
//                                                mentorUserId = mentorUserId,
//                                                timeSlotId = timeSlotId,
//                                                topic = topic,
//                                                startTime = startTime,
//                                                endTime = endTime,
//                                                description = description,
//                                                serviceSlotId = serviceSlotId,
//                                                seekerEmail = email,
//                                                name = bookedBy
//                                            )
//                                        },
//                                    contentAlignment = Alignment.Center
//                                ) {
//                                    Text(
//                                        "Confirm Booking",
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color.White
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//









//@Composable
//fun ConfirmBookingDialog(
//    mentorName: String,
//    topic: String,
//    date: String,
//    duration: Int,
//    startTime: String,
//    endTime: String,
//    bookedBy: String,
//    email: String,
//    price: Double,
//    onDismiss: () -> Unit,
//    onConfirm: () -> Unit
//) {
//    Dialog(onDismissRequest = onDismiss) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentHeight(),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f, fill = false)
//                        .verticalScroll(rememberScrollState())
//                        .padding(24.dp)
//                ) {
//                    // Header
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Column {
//                            Text(
//                                "Confirm Your Booking",
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                "Please review your booking details below",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                        IconButton(
//                            onClick = onDismiss,
//                            modifier = Modifier.size(32.dp)
//                        ) {
//                            Icon(
//                                Icons.Default.Close,
//                                contentDescription = "Close",
//                                tint = Color.Gray
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Booking Details Section
//                    Text(
//                        "Booking Details",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.Black
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Total Amount Card
//                    Card(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp)
//                            .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(12.dp)) // border-purple-200
//                            .background(
//                                brush = Brush.horizontalGradient(
//                                    colors = listOf(
//                                        Color(0xFFFAF5FF), // from-purple-100
//                                        Color(0xFFFCE7F3)  // to-pink-100
//                                    )
//                                ),
//                                shape = RoundedCornerShape(12.dp)
//                            ),
//                        shape = RoundedCornerShape(12.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
//                    )  {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(
//                                "Total Amount",
//                                fontSize = 14.sp,
//                                color = Color.Black,
//                                fontWeight = FontWeight.Medium
//                            )
//                            Spacer(modifier = Modifier.height(4.dp))
//                            Text(
//                                "₹${price.toInt()}",
//                                fontSize = 32.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color(0xFF7E22CE)
//                            )
//                            Spacer(modifier = Modifier.height(12.dp))
//
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .background(
//                                        color = Color(0xFFEFF6FF), // bg-blue-50
//                                        shape = RoundedCornerShape(6.dp) // rounded-md ≈ 6.dp
//                                    )
//                                    .border(
//                                        width = 1.dp,
//                                        color = Color(0xFFBFDBFE), // border-blue-200 (#BFDBFE)
//                                        shape = RoundedCornerShape(6.dp)
//                                    )
//                                    .padding(8.dp) // p-2 = 8dp
//                                    .padding(top = 8.dp), // mt-2 = 8dp top margin (simulate)
//                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.Center
//                            ) {
//                                Icon(
//                                    Icons.Default.Info,
//                                    contentDescription = null,
//                                    tint = Color(0xFF1D4ED8),
//                                    modifier = Modifier.size(20.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    "This amount will be deducted from your account once booking is confirmed",
//                                    fontSize = 12.sp,
//                                    color = Color(0xFF1D4ED8),
//                                    lineHeight = 16.sp
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Details Grid
//                    Column(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            DetailCard(
//                                label = "Topic",
//                                value = topic,
//                                modifier = Modifier.weight(1f)
//                            )
//                            DetailCard(
//                                label = "Date",
//                                value = date,
//                                modifier = Modifier.weight(1f)
//                            )
//                        }
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            DetailCard(
//                                label = "Duration",
//                                value = "$duration minutes",
//                                modifier = Modifier.weight(1f)
//                            )
//                            DetailCard(
//                                label = "Time",
//                                value = "$startTime - $endTime",
//                                modifier = Modifier.weight(1f)
//                            )
//                        }
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            DetailCard(
//                                label = "Mentor",
//                                value = mentorName,
//                                modifier = Modifier.weight(1f)
//                            )
//                            DetailCard(
//                                label = "Booked by",
//                                value = "$bookedBy\n$email",
//                                modifier = Modifier.weight(1f)
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//
//                    // Action Buttons
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Button(
//                            onClick = onDismiss,
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(40.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xFFF3F4F6)
//                            ),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Text(
//                                "Cancel",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF374151)
//                            )
//                        }
//
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(50.dp)
//                                .background(
//                                    brush = Brush.horizontalGradient(
//                                        colors = listOf(
//                                            Color(0xFF893BCF),
//                                            Color(0xFFEA3BA1)
//                                        )
//                                    ),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .clickable {
//                                    bookingViewModel.bookLiveSessionSlot(
//                                        mentorUserId = mentorUserId,
//                                        timeSlotId = timeSlotId,
//                                        topic = topic,
//                                        startTime = startTime,
//                                        endTime = endTime,
//                                        description = description,
//                                        serviceSlotId = serviceSlotId,
//                                        seekerEmail = email,
//                                        name = bookedBy
//                                    )
//                                },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                "Confirm Booking",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.White
//                            )
//                        }
//
//
////                        GradientButton("Confirm Booking") {
////                            onConfirm
////                        }
//
////                        Button(
////                            onClick = onConfirm,
////                            modifier = Modifier
////                                .weight(1f)
////                                .height(50.dp),
////                            colors = ButtonDefaults.buttonColors(
////                                containerColor = Color(0xFF7C3AED)
////                            ),
////                            shape = RoundedCornerShape(8.dp)
////                        ) {
////                            Text(
////                                "Confirm Booking",
////                                fontSize = 16.sp,
////                                fontWeight = FontWeight.SemiBold,
////                                color = Color.White
////                            )
////                        }
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun DetailCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
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
    Dialog(onDismissRequest = onDismiss) {
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
                    onClick = onDismiss,
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






//
//@Composable
//fun BookingSlot(
//    navController: NavController,
//    userId: Int
//) {
//    val context = LocalContext.current
//
//    val availabilityViewModel: AvailabilityViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return AvailabilityViewModel(context) as T
//            }
//        }
//    )
//
//    val bookingViewModel: BookingViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return BookingViewModel(context) as T
//            }
//        }
//    )
//
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return PostsViewModel(context) as T
//            }
//        }
//    )
//
//    val mentorAvailability by availabilityViewModel.mentorAvailability.collectAsState()
//    val isLoading by availabilityViewModel.availabilityLoading.collectAsState()
//    val error by availabilityViewModel.availabilityError.collectAsState()
//
//    val bookingLoading by bookingViewModel.bookingLoading.collectAsState()
//    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
//    val bookingError by bookingViewModel.bookingError.collectAsState()
//
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val profileLoading by postsViewModel.profileLoading.collectAsState()
//
//    var selectedDuration by remember { mutableStateOf<Int?>(null) }
//    var selectedServiceSlotId by remember { mutableStateOf<Int?>(null) }
//    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
//    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
//    var selectedTimeSlot by remember { mutableStateOf<Triple<Int, String, String>?>(null) }
//    var selectedPrice by remember { mutableStateOf<Double?>(null) }
//
//    // Form fields
//    var emailAddress by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var topic by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//
//    var showConfirmDialog by remember { mutableStateOf(false) }
//    var showSuccessDialog by remember { mutableStateOf(false) }
//
//    // Fetch user profile on composition
//    LaunchedEffect(Unit) {
//        postsViewModel.fetchUserProfile()
//    }
//
//    // Update email and name when user profile is loaded
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            emailAddress = profile.email
//            name = profile.full_name ?: ""
//        }
//    }
//
//    LaunchedEffect(userId) {
//        availabilityViewModel.fetchMentorAvailability(userId)
//    }
//
//    LaunchedEffect(bookingSuccess) {
//        if (bookingSuccess != null) {
//            showSuccessDialog = true
//        }
//    }
//
//    Scaffold(
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        if (isLoading || profileLoading) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (error != null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Error: $error", color = Color.Red)
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .padding(horizontal = 16.dp)
//            ) {
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        "Choose your slot",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//
//                // Duration Selection
//                item {
//                    mentorAvailability?.serviceSlots?.let { slots ->
//                        LazyVerticalGrid(
//                            columns = GridCells.Fixed(2),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp),
//                            modifier = Modifier.heightIn(max = 200.dp)
//                        ) {
//                            items(slots.filter { it.isActive }) { slot ->
//                                DurationCard(
//                                    duration = slot.duration,
//                                    price = slot.price,
//                                    isSelected = selectedDuration == slot.duration,
//                                    onClick = {
//                                        selectedDuration = slot.duration
//                                        selectedServiceSlotId = slot.id
//                                        selectedPrice = slot.price
//                                        selectedDate = null
//                                        selectedTimeSlot = null
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Calendar Header
//                item {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowLeft,
//                                contentDescription = "Previous Month"
//                            )
//                        }
//                        Text(
//                            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowRight,
//                                contentDescription = "Next Month"
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                // Calendar Grid
//                item {
//                    CalendarGrid(
//                        currentMonth = currentMonth,
//                        selectedDate = selectedDate,
//                        selectedDuration = selectedDuration,
//                        availableTimeSlots = mentorAvailability?.availableTimeSlots,
//                        onDateSelected = {
//                            selectedDate = it
//                            selectedTimeSlot = null
//                        }
//                    )
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Time Slots
//                item {
//                    if (selectedDate != null && selectedDuration != null) {
//                        Text(
//                            "${selectedDate!!.dayOfMonth}${getDaySuffix(selectedDate!!.dayOfMonth)} ${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            "Available Time",
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        val dateKey = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
//                        val timeSlots = mentorAvailability?.availableTimeSlots?.get(dateKey)
//                        val slotsForDuration = when (selectedDuration) {
//                            15 -> timeSlots?.fifteenMin
//                            30 -> timeSlots?.thirtyMin
//                            45 -> timeSlots?.fortyFiveMin
//                            else -> null
//                        }
//
//                        if (slotsForDuration != null && slotsForDuration.isNotEmpty()) {
//                            LazyVerticalGrid(
//                                columns = GridCells.Fixed(2),
//                                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                                verticalArrangement = Arrangement.spacedBy(12.dp),
//                                modifier = Modifier.heightIn(max = 500.dp)
//                            ) {
//                                items(slotsForDuration) { slot ->
//                                    TimeSlotCard(
//                                        startTime = slot.startTime,
//                                        endTime = slot.endTime,
//                                        isSelected = selectedTimeSlot?.second == slot.startTime && selectedTimeSlot?.third == slot.endTime,
//                                        onClick = {
//                                            selectedTimeSlot = Triple(slot.timeSlotId, slot.startTime, slot.endTime)
//                                        }
//                                    )
//                                }
//                            }
//                        } else {
//                            Text(
//                                "No available slots for this date",
//                                fontSize = 14.sp,
//                                color = Color.Gray,
//                                modifier = Modifier.padding(vertical = 16.dp)
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Booking Form
//                item {
//                    if (selectedTimeSlot != null) {
//                        Column {
//                            Text(
//                                "Booking Details",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Email Address Field
//                            Text(
//                                "Enter Email Address",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = emailAddress,
//                                onValueChange = { emailAddress = it },
//                                placeholder = { Text("Enter your email", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Name Field
//                            Text(
//                                "Name",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = name,
//                                onValueChange = { name = it },
//                                placeholder = { Text("Enter your name", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Topic Field
//                            Text(
//                                "Topic",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = topic,
//                                onValueChange = {
//                                    if (it.length <= 50) topic = it
//                                },
//                                placeholder = { Text("Add your topic", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                supportingText = {
//                                    Text(
//                                        "${topic.length}/50",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Description Field
//                            Text(
//                                "Description (Optional)",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = description,
//                                onValueChange = {
//                                    if (it.length <= 150) description = it
//                                },
//                                placeholder = { Text("Add your description", color = Color.Gray) },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 100.dp),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                maxLines = 4,
//                                supportingText = {
//                                    Text(
//                                        "${description.length}/150",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Show booking error if any
//                            if (bookingError != null) {
//                                Text(
//                                    bookingError!!,
//                                    fontSize = 14.sp,
//                                    color = Color.Red,
//                                    modifier = Modifier.padding(vertical = 8.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Submit Button
//                            val isButtonEnabled = emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()
//
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(50.dp)
//                                    .background(
//                                        brush = if (isButtonEnabled) {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFF893BCF),
//                                                    Color(0xFFEA3BA1)
//                                                )
//                                            )
//                                        } else {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFFE5E7EB),
//                                                    Color(0xFFE5E7EB)
//                                                )
//                                            )
//                                        },
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .clickable(enabled = isButtonEnabled) {
//                                        showConfirmDialog = true
//                                    },
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    "Book Session",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = if (isButtonEnabled) Color.White else Color(0xFF9CA3AF)
//                                )
//                            }
//                            Spacer(modifier = Modifier.height(24.dp))
//                        }
//                    }
//                }
//            }
//        }
//
//        // Confirmation Dialog
//        if (showConfirmDialog) {
//            Dialog(
//                onDismissRequest = { showConfirmDialog = false }
//            ) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White)
//                ) {
//                    Column(
//                        modifier = Modifier.padding(24.dp)
//                    ) {
//                        // Header
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                "Confirm Your Booking",
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            IconButton(
//                                onClick = { showConfirmDialog = false },
//                                modifier = Modifier.size(24.dp)
//                            ) {
//                                Icon(
//                                    Icons.Default.Close,
//                                    contentDescription = "Close",
//                                    tint = Color.Gray
//                                )
//                            }
//                        }
//
//                        Text(
//                            "Please review your booking details below",
//                            fontSize = 14.sp,
//                            color = Color.Gray,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        Text(
//                            "Booking Details",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Total Amount Box
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(
//                                    Color(0xFFF3E8FF),
//                                    shape = RoundedCornerShape(12.dp)
//                                )
//                                .padding(16.dp)
//                        ) {
//                            Column {
//                                Text(
//                                    "Total Amount",
//                                    fontSize = 14.sp,
//                                    color = Color.Gray
//                                )
//                                Text(
//                                    "₹${selectedPrice?.toInt() ?: 0}",
//                                    fontSize = 28.sp,
//                                    fontWeight = FontWeight.Bold,
//                                    color = Color(0xFF7C3AED)
//                                )
//
//                                Spacer(modifier = Modifier.height(12.dp))
//
//                                Row(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .background(
//                                            Color.White,
//                                            shape = RoundedCornerShape(8.dp)
//                                        )
//                                        .padding(12.dp)
//                                ) {
//                                    Icon(
//                                        Icons.Default.Info,
//                                        contentDescription = null,
//                                        tint = Color(0xFF3B82F6),
//                                        modifier = Modifier.size(20.dp)
//                                    )
//                                    Spacer(modifier = Modifier.width(8.dp))
//                                    Text(
//                                        "This amount will be deducted from your account once booking is confirmed",
//                                        fontSize = 12.sp,
//                                        color = Color(0xFF3B82F6),
//                                        lineHeight = 16.sp
//                                    )
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        // Booking Details Grid
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .background(
//                                        Color(0xFFF9FAFB),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(12.dp)
//                            ) {
//                                Text(
//                                    "Topic",
//                                    fontSize = 12.sp,
//                                    color = Color.Gray
//                                )
//                                Text(
//                                    topic,
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black,
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .background(
//                                        Color(0xFFF9FAFB),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(12.dp)
//                            ) {
//                                Text(
//                                    "Date",
//                                    fontSize = 12.sp,
//                                    color = Color.Gray
//                                )
//                                Text(
//                                    selectedDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) ?: "",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black,
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .background(
//                                        Color(0xFFF9FAFB),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(12.dp)
//                            ) {
//                                Text(
//                                    "Duration",
//                                    fontSize = 12.sp,
//                                    color = Color.Gray
//                                )
//                                Text(
//                                    "$selectedDuration minutes",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black,
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .background(
//                                        Color(0xFFF9FAFB),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(12.dp)
//                            ) {
//                                Text(
//                                    "Time",
//                                    fontSize = 12.sp,
//                                    color = Color.Gray
//                                )
//                                Text(
//                                    "${selectedTimeSlot?.second} - ${selectedTimeSlot?.third}",
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black,
//                                    modifier = Modifier.padding(top = 4.dp)
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .background(
//                                        Color(0xFFF9FAFB),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(12.dp)
//                            ) {
//                                Text(
//                                    "Mentor",
//                                    fontSize = 12.sp,
//                                    color = Color.Gray
//                                )
////                                Text(
////                                    mentorAvailability?. ?: "Mentor",
////                                    fontSize = 14.sp,
////                                    fontWeight = FontWeight.Medium,
////                                    color = Color.Black,
////                                    modifier = Modifier.padding(top = 4.dp)
////                                )
//                            }
//
//                            Column(
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .background(
//                                        Color(0xFFF9FAFB),
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .padding(12.dp)
//                            ) {
//                                Text(
//                                    "Booked by",
//                                    fontSize = 12.sp,
//                                    color = Color.Gray
//                                )
//                                Text(
//                                    name,
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black,
//                                    modifier = Modifier.padding(top = 4.dp),
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                                Text(
//                                    emailAddress,
//                                    fontSize = 11.sp,
//                                    color = Color.Gray,
//                                    modifier = Modifier.padding(top = 2.dp),
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        // Action Buttons
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            Button(
//                                onClick = { showConfirmDialog = false },
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(48.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFFF3F4F6)
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Text(
//                                    "Cancel",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color(0xFF6B7280)
//                                )
//                            }
//
//                            Button(
//                                onClick = {
//                                    showConfirmDialog = false
//                                    selectedTimeSlot?.let { (timeSlotId, startTime, endTime) ->
//                                        selectedServiceSlotId?.let { serviceSlotId ->
//                                            bookingViewModel.bookLiveSessionSlot(
//                                                mentorUserId = userId,
//                                                timeSlotId = timeSlotId,
//                                                topic = topic,
//                                                startTime = startTime,
//                                                endTime = endTime,
//                                                description = description,
//                                                serviceSlotId = serviceSlotId,
//                                                seekerEmail = emailAddress,
//                                                name = name
//                                            )
//                                        }
//                                    }
//                                },
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(48.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color(0xFF7C3AED)
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Text(
//                                    "Confirm Booking",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.White
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Processing Dialog
//        if (bookingLoading) {
//            Dialog(onDismissRequest = {}) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(32.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(32.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            "Processing payment and booking your session...",
//                            fontSize = 16.sp,
//                            color = Color(0xFF3B82F6),
//                            textAlign = TextAlign.Center,
//                            fontWeight = FontWeight.Medium
//                        )
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        Row(
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            repeat(3) {
//                                Box(
//                                    modifier = Modifier
//                                        .size(12.dp)
//                                        .background(
//                                            Color(0xFFFBBF24),
//                                            shape = CircleShape
//                                        )
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Success Dialog
//        if (showSuccessDialog) {
//            Dialog(onDismissRequest = {}) {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(32.dp),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(32.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Spacer(modifier = Modifier.width(24.dp))
//                            Text(
//                                "Session Booked Successfully!",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black,
//                                textAlign = TextAlign.Center,
//                                modifier = Modifier.weight(1f)
//                            )
//                            IconButton(
//                                onClick = {
//                                    showSuccessDialog = false
//                                    bookingViewModel.clearBookingState()
//                                    navController.popBackStack()
//                                },
//                                modifier = Modifier.size(24.dp)
//                            ) {
//                                Icon(
//                                    Icons.Default.Close,
//                                    contentDescription = "Close",
//                                    tint = Color.Gray
//                                )
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Text(
//                            "Meeting links have been shared to your email addresses",
//                            fontSize = 14.sp,
//                            color = Color.Gray,
//                            textAlign = TextAlign.Center
//                        )
//
//                        Spacer(modifier = Modifier.height(24.dp))
//
//                        Button(
//                            onClick = {
//                                showSuccessDialog = false
//                                bookingViewModel.clearBookingState()
//                                navController.popBackStack()
//                            },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(48.dp),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xFF10B981)
//                            ),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Text(
//                                "Done",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.White
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//






















//
//
//
//@Composable
//fun BookingSlot(
//    navController: NavController,
//    userId: Int
//) {
//    val context = LocalContext.current
//
//    val availabilityViewModel: AvailabilityViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return AvailabilityViewModel(context) as T
//            }
//        }
//    )
//
//    val bookingViewModel: BookingViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return BookingViewModel(context) as T
//            }
//        }
//    )
//
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return PostsViewModel(context) as T
//            }
//        }
//    )
//
//    val mentorAvailability by availabilityViewModel.mentorAvailability.collectAsState()
//    val isLoading by availabilityViewModel.availabilityLoading.collectAsState()
//    val error by availabilityViewModel.availabilityError.collectAsState()
//
//    val bookingLoading by bookingViewModel.bookingLoading.collectAsState()
//    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
//    val bookingError by bookingViewModel.bookingError.collectAsState()
//
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val profileLoading by postsViewModel.profileLoading.collectAsState()
//
//    var selectedDuration by remember { mutableStateOf<Int?>(null) }
//    var selectedServiceSlotId by remember { mutableStateOf<Int?>(null) }
//    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
//    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
//    var selectedTimeSlot by remember { mutableStateOf<Triple<Int, String, String>?>(null) }
//
//    // Form fields - initialize with empty strings
//    var emailAddress by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var topic by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//
//    var showSuccessDialog by remember { mutableStateOf(false) }
//
//    // Fetch user profile on composition
//    LaunchedEffect(Unit) {
//        postsViewModel.fetchUserProfile()
//    }
//
//    // Update email and name when user profile is loaded
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            emailAddress = profile.email
//            name = profile.full_name ?: ""
//        }
//    }
//
//    LaunchedEffect(userId) {
//        availabilityViewModel.fetchMentorAvailability(userId)
//    }
//
//    LaunchedEffect(bookingSuccess) {
//        if (bookingSuccess != null) {
//            showSuccessDialog = true
//        }
//    }
//
//    Scaffold(
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        if (isLoading || profileLoading) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (error != null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Error: $error", color = Color.Red)
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .padding(horizontal = 16.dp)
//            ) {
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        "Choose your slot",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//
//                // Duration Selection
//                item {
//                    mentorAvailability?.serviceSlots?.let { slots ->
//                        LazyVerticalGrid(
//                            columns = GridCells.Fixed(2),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp),
//                            modifier = Modifier.heightIn(max = 200.dp)
//                        ) {
//                            items(slots.filter { it.isActive }) { slot ->
//                                DurationCard(
//                                    duration = slot.duration,
//                                    price = slot.price,
//                                    isSelected = selectedDuration == slot.duration,
//                                    onClick = {
//                                        selectedDuration = slot.duration
//                                        selectedServiceSlotId = slot.id
//                                        selectedDate = null
//                                        selectedTimeSlot = null
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Calendar Header
//                item {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowLeft,
//                                contentDescription = "Previous Month"
//                            )
//                        }
//                        Text(
//                            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowRight,
//                                contentDescription = "Next Month"
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                // Calendar Grid
//                item {
//                    CalendarGrid(
//                        currentMonth = currentMonth,
//                        selectedDate = selectedDate,
//                        selectedDuration = selectedDuration,
//                        availableTimeSlots = mentorAvailability?.availableTimeSlots,
//                        onDateSelected = {
//                            selectedDate = it
//                            selectedTimeSlot = null
//                        }
//                    )
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Time Slots
//                item {
//                    if (selectedDate != null && selectedDuration != null) {
//                        Text(
//                            "${selectedDate!!.dayOfMonth}${getDaySuffix(selectedDate!!.dayOfMonth)} ${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            "Available Time",
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        val dateKey = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
//                        val timeSlots = mentorAvailability?.availableTimeSlots?.get(dateKey)
//                        val slotsForDuration = when (selectedDuration) {
//                            15 -> timeSlots?.fifteenMin
//                            30 -> timeSlots?.thirtyMin
//                            45 -> timeSlots?.fortyFiveMin
//                            else -> null
//                        }
//
//                        if (slotsForDuration != null && slotsForDuration.isNotEmpty()) {
//                            LazyVerticalGrid(
//                                columns = GridCells.Fixed(2),
//                                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                                verticalArrangement = Arrangement.spacedBy(12.dp),
//                                modifier = Modifier.heightIn(max = 500.dp)
//                            ) {
//                                items(slotsForDuration) { slot ->
//                                    TimeSlotCard(
//                                        startTime = slot.startTime,
//                                        endTime = slot.endTime,
//                                        isSelected = selectedTimeSlot?.second == slot.startTime && selectedTimeSlot?.third == slot.endTime,
//                                        onClick = {
//                                            selectedTimeSlot = Triple(slot.timeSlotId, slot.startTime, slot.endTime)
//                                        }
//                                    )
//                                }
//                            }
//                        } else {
//                            Text(
//                                "No available slots for this date",
//                                fontSize = 14.sp,
//                                color = Color.Gray,
//                                modifier = Modifier.padding(vertical = 16.dp)
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Booking Form
//                item {
//                    if (selectedTimeSlot != null) {
//                        Column {
//                            Text(
//                                "Booking Details",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Email Address Field
//                            Text(
//                                "Enter Email Address",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = emailAddress,
//                                onValueChange = { emailAddress = it },
//                                placeholder = { Text("Enter your email", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Name Field
//                            Text(
//                                "Name",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = name,
//                                onValueChange = { name = it },
//                                placeholder = { Text("Enter your name", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Topic Field
//                            Text(
//                                "Topic",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = topic,
//                                onValueChange = {
//                                    if (it.length <= 50) topic = it
//                                },
//                                placeholder = { Text("Add your topic", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading,
//                                supportingText = {
//                                    Text(
//                                        "${topic.length}/50",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Description Field
//                            Text(
//                                "Description (Optional)",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = description,
//                                onValueChange = {
//                                    if (it.length <= 150) description = it
//                                },
//                                placeholder = { Text("Add your description", color = Color.Gray) },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 100.dp),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                maxLines = 4,
//                                enabled = !bookingLoading,
//                                supportingText = {
//                                    Text(
//                                        "${description.length}/150",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Show booking error if any
//                            if (bookingError != null) {
//                                Text(
//                                    bookingError!!,
//                                    fontSize = 14.sp,
//                                    color = Color.Red,
//                                    modifier = Modifier.padding(vertical = 8.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Submit Button
//                            val isButtonEnabled = !bookingLoading && emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()
//
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(50.dp)
//                                    .background(
//                                        brush = if (isButtonEnabled) {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFF893BCF),
//                                                    Color(0xFFEA3BA1)
//                                                )
//                                            )
//                                        } else {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFFE5E7EB),
//                                                    Color(0xFFE5E7EB)
//                                                )
//                                            )
//                                        },
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .clickable(enabled = isButtonEnabled) {
//                                        if (emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()) {
//                                            selectedTimeSlot?.let { (timeSlotId, startTime, endTime) ->
//                                                selectedServiceSlotId?.let { serviceSlotId ->
//                                                    bookingViewModel.bookLiveSessionSlot(
//                                                        mentorUserId = userId,
//                                                        timeSlotId = timeSlotId,
//                                                        topic = topic,
//                                                        startTime = startTime,
//                                                        endTime = endTime,
//                                                        description = description,
//                                                        serviceSlotId = serviceSlotId,
//                                                        seekerEmail = emailAddress,
//                                                        name = name
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    },
//                                contentAlignment = Alignment.Center
//                            ) {
//                                if (bookingLoading) {
//                                    CircularProgressIndicator(
//                                        color = Color.White,
//                                        modifier = Modifier.size(24.dp)
//                                    )
//                                } else {
//                                    Text(
//                                        "Book Session",
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = if (isButtonEnabled) Color.White else Color(0xFF9CA3AF)
//                                    )
//                                }
//                            }
//                            Spacer(modifier = Modifier.height(24.dp))
//                        }
//                    }
//                }
//            }
//        }
//
//        // Success Dialog
//        if (showSuccessDialog) {
//            AlertDialog(
//                onDismissRequest = {
//                    showSuccessDialog = false
//                    bookingViewModel.clearBookingState()
//                    navController.popBackStack()
//                },
//                title = {
//                    Text(
//                        "Success",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 20.sp
//                    )
//                },
//                text = {
//                    Text(
//                        "Booking is Done",
//                        fontSize = 16.sp
//                    )
//                },
//                confirmButton = {
//                    Button(
//                        onClick = {
//                            showSuccessDialog = false
//                            bookingViewModel.clearBookingState()
//                            navController.popBackStack()
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF7C3AED)
//                        )
//                    ) {
//                        Text("OK")
//                    }
//                },
//                containerColor = Color.White,
//                shape = RoundedCornerShape(16.dp)
//            )
//        }
//    }
//}
//















//@Composable
//fun BookingSlot(
//    navController: NavController,
//    userId: Int
//) {
//    val context = LocalContext.current
//
//    val availabilityViewModel: AvailabilityViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return AvailabilityViewModel(context) as T
//            }
//        }
//    )
//
//    val bookingViewModel: BookingViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                return BookingViewModel(context) as T
//            }
//        }
//    )
//
//    val mentorAvailability by availabilityViewModel.mentorAvailability.collectAsState()
//    val isLoading by availabilityViewModel.availabilityLoading.collectAsState()
//    val error by availabilityViewModel.availabilityError.collectAsState()
//
//    val bookingLoading by bookingViewModel.bookingLoading.collectAsState()
//    val bookingSuccess by bookingViewModel.bookingSuccess.collectAsState()
//    val bookingError by bookingViewModel.bookingError.collectAsState()
//
//    var selectedDuration by remember { mutableStateOf<Int?>(null) }
//    var selectedServiceSlotId by remember { mutableStateOf<Int?>(null) }
//    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
//    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
//    var selectedTimeSlot by remember { mutableStateOf<Triple<Int, String, String>?>(null) } // timeSlotId, startTime, endTime
//
//    // Form fields
//    var emailAddress by remember { mutableStateOf("") }
//    var name by remember { mutableStateOf("") }
//    var topic by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//
//    // Show success alert
//    var showSuccessDialog by remember { mutableStateOf(false) }
//
//    LaunchedEffect(userId) {
//        availabilityViewModel.fetchMentorAvailability(userId)
//    }
//
//    // Handle booking success
//    LaunchedEffect(bookingSuccess) {
//        if (bookingSuccess != null) {
//            showSuccessDialog = true
//        }
//    }
//
//    Scaffold(
////        topBar = { TopBar(title = "Bookings", navController) },
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        if (isLoading) {
//            Box(
//                modifier = Modifier
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (error != null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .background(Color.White),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Error: $error", color = Color.Red)
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(bottom = paddingValues.calculateBottomPadding())
//                    .padding(horizontal = 16.dp)
//            ) {
//                item {
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Text(
//                        "Choose your slot",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//
//                // Duration Selection
//                item {
//                    mentorAvailability?.serviceSlots?.let { slots ->
//                        LazyVerticalGrid(
//                            columns = GridCells.Fixed(2),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp),
//                            modifier = Modifier.heightIn(max = 200.dp)
//                        ) {
//                            items(slots.filter { it.isActive }) { slot ->
//                                DurationCard(
//                                    duration = slot.duration,
//                                    price = slot.price,
//                                    isSelected = selectedDuration == slot.duration,
//                                    onClick = {
//                                        selectedDuration = slot.duration
//                                        selectedServiceSlotId = slot.id
//                                        selectedDate = null
//                                        selectedTimeSlot = null
//                                    }
//                                )
//                            }
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Calendar Header
//                item {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowLeft,
//                                contentDescription = "Previous Month"
//                            )
//                        }
//                        Text(
//                            "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
//                            Icon(
//                                Icons.Default.KeyboardArrowRight,
//                                contentDescription = "Next Month"
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(8.dp))
//                }
//
//                // Calendar Grid
//                item {
//                    CalendarGrid(
//                        currentMonth = currentMonth,
//                        selectedDate = selectedDate,
//                        selectedDuration = selectedDuration,
//                        availableTimeSlots = mentorAvailability?.availableTimeSlots,
//                        onDateSelected = {
//                            selectedDate = it
//                            selectedTimeSlot = null
//                        }
//                    )
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Time Slots
//                item {
//                    if (selectedDate != null && selectedDuration != null) {
//                        Text(
//                            "${selectedDate!!.dayOfMonth}${getDaySuffix(selectedDate!!.dayOfMonth)} ${selectedDate!!.month.getDisplayName(TextStyle.FULL, Locale.getDefault())}",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            "Available Time",
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        val dateKey = selectedDate!!.format(DateTimeFormatter.ISO_LOCAL_DATE)
//                        val timeSlots = mentorAvailability?.availableTimeSlots?.get(dateKey)
//                        val slotsForDuration = when (selectedDuration) {
//                            15 -> timeSlots?.fifteenMin
//                            30 -> timeSlots?.thirtyMin
//                            45 -> timeSlots?.fortyFiveMin
//                            else -> null
//                        }
//
//                        if (slotsForDuration != null && slotsForDuration.isNotEmpty()) {
//                            LazyVerticalGrid(
//                                columns = GridCells.Fixed(2),
//                                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                                verticalArrangement = Arrangement.spacedBy(12.dp),
//                                modifier = Modifier.heightIn(max = 500.dp)
//                            ) {
//                                items(slotsForDuration) { slot ->
//                                    TimeSlotCard(
//                                        startTime = slot.startTime,
//                                        endTime = slot.endTime,
//                                        isSelected = selectedTimeSlot?.second == slot.startTime && selectedTimeSlot?.third == slot.endTime,
//                                        onClick = {
//                                            selectedTimeSlot = Triple(slot.timeSlotId, slot.startTime, slot.endTime)
//                                        }
//                                    )
//                                }
//                            }
//                        } else {
//                            Text(
//                                "No available slots for this date",
//                                fontSize = 14.sp,
//                                color = Color.Gray,
//                                modifier = Modifier.padding(vertical = 16.dp)
//                            )
//                        }
//                    }
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//
//                // Booking Form
//                item {
//                    if (selectedTimeSlot != null) {
//                        Column {
//                            Text(
//                                "Booking Details",
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Email Address Field
//                            Text(
//                                "Enter Email Address",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = emailAddress,
//                                onValueChange = { emailAddress = it },
//                                placeholder = { Text("Enter your email", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Name Field
//                            Text(
//                                "Name",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = name,
//                                onValueChange = { name = it },
//                                placeholder = { Text("Enter your name", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Topic Field
//                            Text(
//                                "Topic",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = topic,
//                                onValueChange = {
//                                    if (it.length <= 50) topic = it
//                                },
//                                placeholder = { Text("Add your topic", color = Color.Gray) },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                enabled = !bookingLoading,
//                                supportingText = {
//                                    Text(
//                                        "${topic.length}/50",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Description Field
//                            Text(
//                                "Description (Optional)",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Normal,
//                                color = Color.Black
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = description,
//                                onValueChange = {
//                                    if (it.length <= 150) description = it
//                                },
//                                placeholder = { Text("Add your description", color = Color.Gray) },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(min = 100.dp),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    focusedBorderColor = Color(0xFFE5E7EB),
//                                    unfocusedBorderColor = Color(0xFFE5E7EB),
//                                    focusedContainerColor = Color.White,
//                                    unfocusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(8.dp),
//                                maxLines = 4,
//                                enabled = !bookingLoading,
//                                supportingText = {
//                                    Text(
//                                        "${description.length}/150",
//                                        fontSize = 12.sp,
//                                        color = Color.Gray,
//                                        modifier = Modifier.fillMaxWidth(),
//                                        textAlign = TextAlign.End
//                                    )
//                                }
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Show booking error if any
//                            if (bookingError != null) {
//                                Text(
//                                    bookingError!!,
//                                    fontSize = 14.sp,
//                                    color = Color.Red,
//                                    modifier = Modifier.padding(vertical = 8.dp)
//                                )
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//
//
//
//                            // Submit Button
//                            val isButtonEnabled = !bookingLoading && emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()
//
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(50.dp)
//                                    .background(
//                                        brush = if (isButtonEnabled) {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFF893BCF),
//                                                    Color(0xFFEA3BA1)
//                                                )
//                                            )
//                                        } else {
//                                            Brush.horizontalGradient(
//                                                colors = listOf(
//                                                    Color(0xFFE5E7EB),
//                                                    Color(0xFFE5E7EB)
//                                                )
//                                            )
//                                        },
//                                        shape = RoundedCornerShape(8.dp)
//                                    )
//                                    .clickable(enabled = isButtonEnabled) {
//                                        if (emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()) {
//                                            selectedTimeSlot?.let { (timeSlotId, startTime, endTime) ->
//                                                selectedServiceSlotId?.let { serviceSlotId ->
//                                                    bookingViewModel.bookLiveSessionSlot(
//                                                        mentorUserId = userId,
//                                                        timeSlotId = timeSlotId,
//                                                        topic = topic,
//                                                        startTime = startTime,
//                                                        endTime = endTime,
//                                                        description = description,
//                                                        serviceSlotId = serviceSlotId,
//                                                        seekerEmail = emailAddress,
//                                                        name = name
//                                                    )
//                                                }
//                                            }
//                                        }
//                                    },
//                                contentAlignment = Alignment.Center
//                            ) {
//                                if (bookingLoading) {
//                                    CircularProgressIndicator(
//                                        color = Color.White,
//                                        modifier = Modifier.size(24.dp)
//                                    )
//                                } else {
//                                    Text(
//                                        "Book Session",
//                                        fontSize = 16.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = if (isButtonEnabled) Color.White else Color(0xFF9CA3AF)
//                                    )
//                                }
//                            }
//
//
//
//
//
//
//
//
//
//
////                            // Submit Button
////                            Button(
////                                onClick = {
////                                    if (emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()) {
////                                        selectedTimeSlot?.let { (timeSlotId, startTime, endTime) ->
////                                            selectedServiceSlotId?.let { serviceSlotId ->
////                                                bookingViewModel.bookLiveSessionSlot(
////                                                    mentorUserId = userId,
////                                                    timeSlotId = timeSlotId,
////                                                    topic = topic,
////                                                    startTime = startTime,
////                                                    endTime = endTime,
////                                                    description = description,
////                                                    serviceSlotId = serviceSlotId,
////                                                    seekerEmail = emailAddress,
////                                                    name = name
////                                                )
////                                            }
////                                        }
////                                    }
////                                },
////                                modifier = Modifier
////                                    .fillMaxWidth()
////                                    .height(50.dp),
////                                colors = ButtonDefaults.buttonColors(
////                                    containerColor = Color(0xFF7C3AED)
////                                ),
////                                shape = RoundedCornerShape(8.dp),
////                                enabled = !bookingLoading && emailAddress.isNotBlank() && name.isNotBlank() && topic.isNotBlank()
////                            ) {
////                                if (bookingLoading) {
////                                    CircularProgressIndicator(
////                                        color = Color.White,
////                                        modifier = Modifier.size(24.dp)
////                                    )
////                                } else {
////                                    Text(
////                                        "Book Session",
////                                        fontSize = 16.sp,
////                                        fontWeight = FontWeight.SemiBold,
////                                        color = Color.White
////                                    )
////                                }
////                            }
//                            Spacer(modifier = Modifier.height(24.dp))
//                        }
//                    }
//                }
//            }
//        }
//
//
//
//        // Success Dialog
//        if (showSuccessDialog) {
//            AlertDialog(
//                onDismissRequest = {
//                    showSuccessDialog = false
//                    bookingViewModel.clearBookingState()
//                    navController.popBackStack()
//                },
//                title = {
//                    Text(
//                        "Success",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 20.sp
//                    )
//                },
//                text = {
//                    Text(
//                        "Booking is Done",
//                        fontSize = 16.sp
//                    )
//                },
//                confirmButton = {
//                    Button(
//                        onClick = {
//                            showSuccessDialog = false
//                            bookingViewModel.clearBookingState()
//                            navController.popBackStack()
//                        },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFF7C3AED)
//                        )
//                    ) {
//                        Text("OK")
//                    }
//                },
//                containerColor = Color.White,
//                shape = RoundedCornerShape(16.dp)
//            )
//        }
//    }
//}



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
//            .background(
//                color = if (isSelected) Color(0xFF7C3AED) else Color.White,
//                shape = RoundedCornerShape(12.dp)
//            )
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
            .clickable(onClick = onClick)
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
                            onDateSelected = {
                                if ((hasSlots || selectedDuration == null) && dateExistsInResponse) {
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
//            .background(
//                color = when {
//                    isSelected -> Color(0xFF7C3AED)
//                    date == today && dateExistsInResponse -> Color(0xFFDDD6FE)
//                    else -> Color.Transparent
//                },
//                shape = RoundedCornerShape(8.dp)
//            )
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
            .clickable(enabled = !isDisabled) { onDateSelected() },
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
            .clickable(onClick = onClick)
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




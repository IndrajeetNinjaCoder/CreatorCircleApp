package com.example.mentorcircle

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.data.models.Session
import com.cc.creatorcircle.data.models.SessionsState
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.User
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.ui.screens.livesession.MentorTabBar
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.viewModel.BookingViewModel
import com.cc.creatorcircle.viewModel.SessionViewModel
import com.cc.creatorcircle.viewModel.toDisplayDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun BookingsHeader(onMenuClick: () -> Unit) {
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
        Text(
            text = "Bookings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun BookingsSidebarContent(
    onCloseClick: () -> Unit,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onCloseClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "X",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Menu Items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Mentor Circle",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("livesession")
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp)
            )

            Text(
                text = "Seek Guidance",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate(Screen.LiveSession.route)
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFE91E63),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable {
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bookings",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "My Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController.navigate("mysession")
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp)
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val sessionViewModel: SessionViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return SessionViewModel(context) as T
            }
        }
    )

    val bookingViewModel: BookingViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BookingViewModel(context) as T
            }
        }
    )

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Upcoming", "Attended", "Canceled")

    val sessionsState by sessionViewModel.sessionsState.collectAsState()
    val sessions by sessionViewModel.sessions.collectAsState()

    val cancelSuccess by bookingViewModel.cancelSuccess.collectAsState()
    val cancelError by bookingViewModel.cancelError.collectAsState()
    val cancelLoading by bookingViewModel.cancelLoading.collectAsState()

    // Filter states
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedSessionType by remember { mutableStateOf("All Sessions") }
    var selectedStatuses by remember { mutableStateOf(setOf("Upcoming")) }
    var fromDate by remember { mutableStateOf("") }
    var toDate by remember { mutableStateOf("") }
    var isFilterApplied by remember { mutableStateOf(false) }

    // Fetch sessions based on tab when filter is not applied
    LaunchedEffect(selectedTab) {
        if (!isFilterApplied) {
            when (selectedTab) {
                0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
                1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
                2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
            }
        }
    }

    LaunchedEffect(cancelSuccess) {
        cancelSuccess?.let {
            android.widget.Toast.makeText(
                context,
                "Booking cancelled successfully. Refund: ${it.refundedPayments} payment(s)",
                android.widget.Toast.LENGTH_LONG
            ).show()

            // Refresh based on filter status
            if (isFilterApplied) {
                applyFilters(
                    sessionViewModel = sessionViewModel,
                    selectedStatuses = selectedStatuses,
                    fromDate = fromDate,
                    toDate = toDate,
                    selectedSessionType = selectedSessionType
                )
            } else {
                when (selectedTab) {
                    0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
                    1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
                    2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
                }
            }
            bookingViewModel.clearCancelState()
        }
    }

    LaunchedEffect(cancelError) {
        cancelError?.let { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
            bookingViewModel.clearCancelState()
        }
    }

    // Filter sessions based on applied filter or current tab
    val filteredSessions = remember(sessions, selectedTab, isFilterApplied, selectedStatuses) {
        if (isFilterApplied) {
            // When filter is applied, show sessions matching selected statuses
            sessions.filter { session ->
                selectedStatuses.any { status ->
                    when (status) {
                        "Upcoming" -> session.status.equals("upcoming", ignoreCase = true)
                        "Attended" -> session.status.equals("attended", ignoreCase = true)
                        "Cancelled" -> session.status.equals("cancelled", ignoreCase = true) ||
                                session.status.equals("canceled", ignoreCase = true)
                        else -> false
                    }
                }
            }
        } else {
            // When no filter, show based on selected tab
            when (selectedTab) {
                0 -> sessions.filter { it.status.equals("upcoming", ignoreCase = true) }
                1 -> sessions.filter { it.status.equals("attended", ignoreCase = true) }
                2 -> sessions.filter {
                    it.status.equals("cancelled", ignoreCase = true) ||
                            it.status.equals("canceled", ignoreCase = true)
                }
                else -> emptyList()
            }
        }
    }

    // Calculate active tab indicators based on filter
    val activeTabIndices = remember(selectedStatuses, isFilterApplied) {
        if (isFilterApplied) {
            buildSet {
                if (selectedStatuses.contains("Upcoming")) add(0)
                if (selectedStatuses.contains("Attended")) add(1)
                if (selectedStatuses.contains("Cancelled")) add(2)
            }
        } else {
            setOf(selectedTab)
        }
    }

    Scaffold(
        topBar = {
            MentorTabBar(
                selectedTab = 1, // Bookings is active
                navController = navController
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Tabs with multi-select indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    TabButton(
                        text = tab,
                        selected = activeTabIndices.contains(index),
                        onClick = {
                            selectedTab = index
                            // Clear filter when clicking on a tab
                            isFilterApplied = false
                            selectedStatuses = when(index) {
                                0 -> setOf("Upcoming")
                                1 -> setOf("Attended")
                                2 -> setOf("Cancelled")
                                else -> setOf("Upcoming")
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calendar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = LocalDate.now().month.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        ),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show clear filter button if filter is applied
                    if (isFilterApplied) {
                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            color = Color(0xFF7B3FF2),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                isFilterApplied = false
                                selectedStatuses = when(selectedTab) {
                                    0 -> setOf("Upcoming")
                                    1 -> setOf("Attended")
                                    2 -> setOf("Cancelled")
                                    else -> setOf("Upcoming")
                                }
                                selectedSessionType = "All Sessions"
                                fromDate = ""
                                toDate = ""
                                // Refresh based on current tab
                                when (selectedTab) {
                                    0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
                                    1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
                                    2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
                                }
                            }
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = "Filter",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { showFilterDialog = true }
                    )
                }
            }

            when (val state = sessionsState) {
                is SessionsState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF7B3FF2))
                    }
                }

                is SessionsState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Error: ${state.message}",
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                            Button(onClick = {
                                if (isFilterApplied) {
                                    applyFilters(
                                        sessionViewModel = sessionViewModel,
                                        selectedStatuses = selectedStatuses,
                                        fromDate = fromDate,
                                        toDate = toDate,
                                        selectedSessionType = selectedSessionType
                                    )
                                } else {
                                    when (selectedTab) {
                                        0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
                                        1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
                                        2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
                                    }
                                }
                            }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is SessionsState.Success, SessionsState.Idle -> {
                    if (filteredSessions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            val statusText = if (isFilterApplied && selectedStatuses.size > 1) {
                                "selected statuses"
                            } else {
                                tabs[selectedTab].lowercase()
                            }
                            Text(
                                text = "No $statusText sessions",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredSessions) { session ->
                                MeetingCard(
                                    session = session,
                                    bookingViewModel = bookingViewModel,
                                    onViewDetails = {
                                        navController.navigate("session_details/${session.slotId}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            sessionType = selectedSessionType,
            selectedStatuses = selectedStatuses,
            fromDate = fromDate,
            toDate = toDate,
            onSessionTypeChange = { selectedSessionType = it },
            onStatusesChange = { selectedStatuses = it },
            onFromDateChange = { fromDate = it },
            onToDateChange = { toDate = it },
            onDismiss = { showFilterDialog = false },
            onApplyFilters = {
                isFilterApplied = true
                applyFilters(
                    sessionViewModel = sessionViewModel,
                    selectedStatuses = selectedStatuses,
                    fromDate = fromDate,
                    toDate = toDate,
                    selectedSessionType = selectedSessionType
                )
                showFilterDialog = false
            }
        )
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun BookingScreen(
//    navController: NavController,
//) {
//    val context = LocalContext.current
//    val sessionViewModel: SessionViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return SessionViewModel(context) as T
//            }
//        }
//    )
//
//    val bookingViewModel: BookingViewModel = viewModel(
//        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
//            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//                @Suppress("UNCHECKED_CAST")
//                return BookingViewModel(context) as T
//            }
//        }
//    )
//
//    val drawerState = rememberDrawerState(DrawerValue.Closed)
//    val scope = rememberCoroutineScope()
//    var selectedTab by remember { mutableStateOf(0) }
//    val tabs = listOf("Upcoming", "Attended", "Canceled")
//
//    val sessionsState by sessionViewModel.sessionsState.collectAsState()
//    val sessions by sessionViewModel.sessions.collectAsState()
//
//    val cancelSuccess by bookingViewModel.cancelSuccess.collectAsState()
//    val cancelError by bookingViewModel.cancelError.collectAsState()
//    val cancelLoading by bookingViewModel.cancelLoading.collectAsState()
//
//    // Filter states
//    var showFilterDialog by remember { mutableStateOf(false) }
//    var selectedSessionType by remember { mutableStateOf("All Sessions") }
//    var selectedStatuses by remember { mutableStateOf(setOf("Upcoming")) }
//    var fromDate by remember { mutableStateOf("") }
//    var toDate by remember { mutableStateOf("") }
//    var isFilterApplied by remember { mutableStateOf(false) }
//
//    // Fetch sessions based on tab when filter is not applied
//    LaunchedEffect(selectedTab) {
//        if (!isFilterApplied) {
//            when (selectedTab) {
//                0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
//                1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
//                2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
//            }
//        }
//    }
//
//    LaunchedEffect(cancelSuccess) {
//        cancelSuccess?.let {
//            android.widget.Toast.makeText(
//                context,
//                "Booking cancelled successfully. Refund: ${it.refundedPayments} payment(s)",
//                android.widget.Toast.LENGTH_LONG
//            ).show()
//
//            // Refresh based on filter status
//            if (isFilterApplied) {
//                applyFilters(
//                    sessionViewModel = sessionViewModel,
//                    selectedStatuses = selectedStatuses,
//                    fromDate = fromDate,
//                    toDate = toDate,
//                    selectedSessionType = selectedSessionType
//                )
//            } else {
//                when (selectedTab) {
//                    0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
//                    1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
//                    2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
//                }
//            }
//            bookingViewModel.clearCancelState()
//        }
//    }
//
//    LaunchedEffect(cancelError) {
//        cancelError?.let { error ->
//            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
//            bookingViewModel.clearCancelState()
//        }
//    }
//
//    // Filter sessions based on applied filter or current tab
//    val filteredSessions = remember(sessions, selectedTab, isFilterApplied, selectedStatuses) {
//        if (isFilterApplied) {
//            // When filter is applied, show sessions matching selected statuses
//            sessions.filter { session ->
//                selectedStatuses.any { status ->
//                    when (status) {
//                        "Upcoming" -> session.status.equals("upcoming", ignoreCase = true)
//                        "Attended" -> session.status.equals("attended", ignoreCase = true)
//                        "Cancelled" -> session.status.equals("cancelled", ignoreCase = true) ||
//                                session.status.equals("canceled", ignoreCase = true)
//                        else -> false
//                    }
//                }
//            }
//        } else {
//            // When no filter, show based on selected tab
//            when (selectedTab) {
//                0 -> sessions.filter { it.status.equals("upcoming", ignoreCase = true) }
//                1 -> sessions.filter { it.status.equals("attended", ignoreCase = true) }
//                2 -> sessions.filter {
//                    it.status.equals("cancelled", ignoreCase = true) ||
//                            it.status.equals("canceled", ignoreCase = true)
//                }
//                else -> emptyList()
//            }
//        }
//    }
//
//    // Calculate active tab indicators based on filter
//    val activeTabIndices = remember(selectedStatuses, isFilterApplied) {
//        if (isFilterApplied) {
//            buildSet {
//                if (selectedStatuses.contains("Upcoming")) add(0)
//                if (selectedStatuses.contains("Attended")) add(1)
//                if (selectedStatuses.contains("Cancelled")) add(2)
//            }
//        } else {
//            setOf(selectedTab)
//        }
//    }
//
//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            BookingsSidebarContent(
//                onCloseClick = {
//                    scope.launch {
//                        drawerState.close()
//                    }
//                },
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
//                BookingsHeader(
//                    onMenuClick = {
//                        scope.launch {
//                            drawerState.open()
//                        }
//                    }
//                )
//
//                // Tabs with multi-select indicator
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                        .padding(top = 16.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    tabs.forEachIndexed { index, tab ->
//                        TabButton(
//                            text = tab,
//                            selected = activeTabIndices.contains(index),
//                            onClick = {
//                                selectedTab = index
//                                // Clear filter when clicking on a tab
//                                isFilterApplied = false
//                                selectedStatuses = when(index) {
//                                    0 -> setOf("Upcoming")
//                                    1 -> setOf("Attended")
//                                    2 -> setOf("Cancelled")
//                                    else -> setOf("Upcoming")
//                                }
//                            }
//                        )
//                    }
//                }
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            text = "Calendar",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color.Black
//                        )
//                        Text(
//                            text = LocalDate.now().month.getDisplayName(
//                                TextStyle.FULL,
//                                Locale.getDefault()
//                            ),
//                            fontSize = 12.sp,
//                            color = Color.Gray
//                        )
//                    }
//
//                    Row(
//                        horizontalArrangement = Arrangement.spacedBy(8.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        // Show clear filter button if filter is applied
//                        if (isFilterApplied) {
//                            Text(
//                                text = "Clear",
//                                fontSize = 12.sp,
//                                color = Color(0xFF7B3FF2),
//                                fontWeight = FontWeight.Medium,
//                                modifier = Modifier.clickable {
//                                    isFilterApplied = false
//                                    selectedStatuses = when(selectedTab) {
//                                        0 -> setOf("Upcoming")
//                                        1 -> setOf("Attended")
//                                        2 -> setOf("Cancelled")
//                                        else -> setOf("Upcoming")
//                                    }
//                                    selectedSessionType = "All Sessions"
//                                    fromDate = ""
//                                    toDate = ""
//                                    // Refresh based on current tab
//                                    when (selectedTab) {
//                                        0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
//                                        1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
//                                        2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
//                                    }
//                                }
//                            )
//                        }
//
//                        Image(
//                            painter = painterResource(id = R.drawable.ic_filter),
//                            contentDescription = "Filter",
//                            modifier = Modifier
//                                .size(40.dp)
//                                .clickable { showFilterDialog = true }
//                        )
//                    }
//                }
//
//                when (val state = sessionsState) {
//                    is SessionsState.Loading -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            CircularProgressIndicator(color = Color(0xFF7B3FF2))
//                        }
//                    }
//
//                    is SessionsState.Error -> {
//                        Box(
//                            modifier = Modifier.fillMaxSize(),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally,
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                Text(
//                                    text = "Error: ${state.message}",
//                                    color = Color.Red,
//                                    textAlign = TextAlign.Center
//                                )
//                                Button(onClick = {
//                                    if (isFilterApplied) {
//                                        applyFilters(
//                                            sessionViewModel = sessionViewModel,
//                                            selectedStatuses = selectedStatuses,
//                                            fromDate = fromDate,
//                                            toDate = toDate,
//                                            selectedSessionType = selectedSessionType
//                                        )
//                                    } else {
//                                        when (selectedTab) {
//                                            0 -> sessionViewModel.fetchSessions(statuses = listOf("upcoming"))
//                                            1 -> sessionViewModel.fetchSessions(statuses = listOf("attended"))
//                                            2 -> sessionViewModel.fetchSessions(statuses = listOf("cancelled"))
//                                        }
//                                    }
//                                }) {
//                                    Text("Retry")
//                                }
//                            }
//                        }
//                    }
//
//                    is SessionsState.Success, SessionsState.Idle -> {
//                        if (filteredSessions.isEmpty()) {
//                            Box(
//                                modifier = Modifier.fillMaxSize(),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                val statusText = if (isFilterApplied && selectedStatuses.size > 1) {
//                                    "selected statuses"
//                                } else {
//                                    tabs[selectedTab].lowercase()
//                                }
//                                Text(
//                                    text = "No $statusText sessions",
//                                    color = Color.Gray,
//                                    fontSize = 16.sp
//                                )
//                            }
//                        } else {
//                            LazyColumn(
//                                modifier = Modifier.fillMaxSize(),
//                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                                verticalArrangement = Arrangement.spacedBy(12.dp)
//                            ) {
//                                items(filteredSessions) { session ->
//                                    MeetingCard(
//                                        session = session,
//                                        bookingViewModel = bookingViewModel,
//                                        onViewDetails = {
//                                            navController.navigate("session_details/${session.slotId}")
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    // Filter Dialog
//    if (showFilterDialog) {
//        FilterDialog(
//            sessionType = selectedSessionType,
//            selectedStatuses = selectedStatuses,
//            fromDate = fromDate,
//            toDate = toDate,
//            onSessionTypeChange = { selectedSessionType = it },
//            onStatusesChange = { selectedStatuses = it },
//            onFromDateChange = { fromDate = it },
//            onToDateChange = { toDate = it },
//            onDismiss = { showFilterDialog = false },
//            onApplyFilters = {
//                isFilterApplied = true
//                applyFilters(
//                    sessionViewModel = sessionViewModel,
//                    selectedStatuses = selectedStatuses,
//                    fromDate = fromDate,
//                    toDate = toDate,
//                    selectedSessionType = selectedSessionType
//                )
//                showFilterDialog = false
//            }
//        )
//    }
//}
//


// Helper function to apply filters
private fun applyFilters(
    sessionViewModel: SessionViewModel,
    selectedStatuses: Set<String>,
    fromDate: String,
    toDate: String,
    selectedSessionType: String
) {
    val statusList = selectedStatuses.map { status ->
        when (status) {
            "Upcoming" -> "upcoming"
            "Attended" -> "attended"
            "Cancelled" -> "cancelled"
            else -> status.lowercase()
        }
    }

    sessionViewModel.fetchSessions(
        statuses = statusList,
        fromDate = fromDate,
        toDate = toDate,
        sessionType = when (selectedSessionType) {
            "Seeking Guidance" -> "seeking"
            "Providing Guidance" -> "providing"
            else -> "both"
        }
    )
}






@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    sessionType: String,
    selectedStatuses: Set<String>,
    fromDate: String,
    toDate: String,
    onSessionTypeChange: (String) -> Unit,
    onStatusesChange: (Set<String>) -> Unit,
    onFromDateChange: (String) -> Unit,
    onToDateChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onApplyFilters: () -> Unit
) {
    val context = LocalContext.current
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_filter),
                            contentDescription = "Filter",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filter sessions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Session Type Section
                    item {
                        FilterSection(title = "Session type") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("All Sessions", "Providing Guidance", "Seeking guidance").forEach { type ->
                                    RadioOption(
                                        text = type,
                                        selected = sessionType == type,
                                        onClick = { onSessionTypeChange(type) }
                                    )
                                }
                            }
                        }
                    }

                    // Date Range Section
                    item {
                        FilterSection(title = "Date Range") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = fromDate,
                                        onValueChange = { },
                                        label = { Text("From", fontSize = 12.sp) },
                                        placeholder = { Text("dd/mm/yyyy", fontSize = 14.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showFromDatePicker = true },
                                        singleLine = true,
                                        readOnly = true,
                                        enabled = false,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF7B3FF2),
                                            unfocusedBorderColor = Color.LightGray,
                                            disabledBorderColor = Color.LightGray,
                                            disabledTextColor = Color.Black,
                                            disabledPlaceholderColor = Color.Gray,
                                            disabledLabelColor = Color.DarkGray
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_calendar),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.Gray
                                            )
                                        }
                                    )
                                    OutlinedTextField(
                                        value = toDate,
                                        onValueChange = { },
                                        label = { Text("To", fontSize = 12.sp) },
                                        placeholder = { Text("dd/mm/yyyy", fontSize = 14.sp) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { showToDatePicker = true },
                                        singleLine = true,
                                        readOnly = true,
                                        enabled = false,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF7B3FF2),
                                            unfocusedBorderColor = Color.LightGray,
                                            disabledBorderColor = Color.LightGray,
                                            disabledTextColor = Color.Black,
                                            disabledPlaceholderColor = Color.Gray,
                                            disabledLabelColor = Color.DarkGray
                                        ),
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_calendar),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = Color.Gray
                                            )
                                        }
                                    )
                                }
                                Text(
                                    text = "Select a date range (both from and to required)",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }

                    // Status Section
                    item {
                        FilterSection(title = "Select by Status") {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("Upcoming", "Attended", "Cancelled").forEach { status ->
                                    CheckboxOption(
                                        text = status,
                                        checked = selectedStatuses.contains(status),
                                        onCheckedChange = { checked ->
                                            val newSet = selectedStatuses.toMutableSet()
                                            if (checked) {
                                                newSet.add(status)
                                            } else {
                                                newSet.remove(status)
                                            }
                                            onStatusesChange(newSet)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Apply Button
                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B3FF2)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Apply filters",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Date Pickers
    if (showFromDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showFromDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.util.Date(millis)
                        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        onFromDateChange(formatter.format(date))
                    }
                    showFromDatePicker = false
                }) {
                    Text("OK", color = Color(0xFF7B3FF2))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF7B3FF2),
                    todayContentColor = Color(0xFF7B3FF2),
                    todayDateBorderColor = Color(0xFF7B3FF2)
                )
            )
        }
    }

    if (showToDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showToDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.util.Date(millis)
                        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        onToDateChange(formatter.format(date))
                    }
                    showToDatePicker = false
                }) {
                    Text("OK", color = Color(0xFF7B3FF2))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToDatePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF7B3FF2),
                    todayContentColor = Color(0xFF7B3FF2),
                    todayDateBorderColor = Color(0xFF7B3FF2)
                )
            )
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun RadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = if (selected) Color.Black else Color.DarkGray
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF7B3FF2),
                unselectedColor = Color.Gray
            )
        )
    }
}

@Composable
fun CheckboxOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            fontSize = 15.sp,
            color = if (checked) Color.Black else Color.DarkGray
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF7B3FF2),
                uncheckedColor = Color.Gray
            )
        )
    }
}











@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val gradientColors = listOf(Color(0xFF893BCF), Color(0xFFEA3BA1))

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = if (selected) Color.White else Color(0xFFB8A3D6)
        ),
        modifier = Modifier
            .then(
                if (selected) {
                    Modifier.background(
                        brush = Brush.horizontalGradient(gradientColors),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


//@Composable
//fun TabButton(
//    text: String,
//    selected: Boolean,
//    onClick: () -> Unit
//) {
//    Button(
//        onClick = onClick,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = if (selected) Color(0xFF7B3FF2) else Color.Transparent,
//            contentColor = if (selected) Color.White else Color(0xFFB8A3D6)
//        ),
//        shape = RoundedCornerShape(12.dp),
//        elevation = ButtonDefaults.buttonElevation(0.dp),
//        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
//    ) {
//        Text(
//            text = text,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}








@Composable
fun MeetingCard(
    session: Session,
    bookingViewModel: BookingViewModel,
    onViewDetails: () -> Unit
) {
    val context = LocalContext.current
    val userDataManager = remember { UserDataManager(context) }
    val currentUserData = remember { userDataManager.getUserData() }

    val isProvidingHelp = session.currentUserRole == "provider"
    val otherUser = if (isProvidingHelp) session.seeker else session.provider

    // State for showing dialog
    var showDetailsDialog by remember { mutableStateOf(false) }

    // Format display date as "29 Oct' 25" from session.date
    val displayDate = session.date.toDisplayDate()

    // Parse time from start_time field
    val timeText = try {
        // start_time is "2025-10-29T00:15:00" format
        val dateTime = if (session.startTime.contains("T")) {
            val timePart = session.startTime.split("T")[1].substring(0, 5) // Get "00:15"
            val (hour, minute) = timePart.split(":").map { it.toInt() }
            val startTime = LocalTime.of(hour, minute)
            val formatter = DateTimeFormatter.ofPattern("h:mm a")

            // Parse duration
            val durationMinutes = when {
                session.duration.contains("minutes") ->
                    session.duration.replace(" minutes", "").trim().toIntOrNull() ?: 30

                session.duration.contains("-") -> {
                    // Duration is in format "12:15 AM - 12:30 AM"
                    30 // Default duration
                }

                else -> 30
            }

            val endTime = startTime.plusMinutes(durationMinutes.toLong())
            "${startTime.format(formatter)} - ${endTime.format(formatter)}"
        } else {
            session.duration // Fallback to duration field
        }
        dateTime
    } catch (e: Exception) {
        session.duration
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // LEFT COLUMN - Profile and Date
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Profile Image
                AsyncImage(
                    model = otherUser.profilePic.ifEmpty {
                        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop"
                    },
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Date display
                Text(
                    text = displayDate,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // RIGHT COLUMN - Meeting Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meeting with ${otherUser.fullName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                }

                // Badge
                Surface(
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isProvidingHelp) "Providing help" else "Request assistance",
                            fontSize = 11.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = timeText,
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }

                // Status
                Text(
                    text = "Status: ${session.status.replaceFirstChar { it.uppercase() }}",
                    fontSize = 11.sp,
                    color = Color(0xFF757575)
                )

                // Topic
                if (session.topic.isNotEmpty()) {
                    Text(
                        text = "Topic: ${session.topic}",
                        fontSize = 12.sp,
                        color = Color(0xFF212121),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                // View Details Button
                    GradientButton("Details") {
                        showDetailsDialog = true
                    }

                    if(session.status == "upcoming") {

                        Button(
                            onClick = {
                                session.googleMeetLink?.let { meetLink ->
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(meetLink))
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFB787F5))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_meet),
                                contentDescription = "Google Meet",
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Join",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFB787F5)
                            )
                        }

                    }

                }
            }
        }
    }

    // Meeting Details Dialog
    if (showDetailsDialog) {
        MeetingDetailsDialog(
            session = session,
            isProvidingHelp = isProvidingHelp,
            currentUserData = currentUserData,
            timeText = timeText,
            bookingViewModel = bookingViewModel,
            onDismiss = { showDetailsDialog = false },
            onCancelBooking = {
                showDetailsDialog = false
            }
        )
    }
}

@Composable
fun MeetingDetailsDialog(
    session: Session,
    isProvidingHelp: Boolean,
    currentUserData: UserData,
    timeText: String,
    bookingViewModel: BookingViewModel,
    onDismiss: () -> Unit,
    onCancelBooking: () -> Unit
) {
    val cancelLoading by bookingViewModel.cancelLoading.collectAsState()
    var showCancelConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Close button
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF757575)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Provider Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = (if (isProvidingHelp) currentUserData.profilePic else session.provider.profilePic)?.ifEmpty {
                            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop"
                        } ?: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
                        contentDescription = "Provider profile",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = if (isProvidingHelp) currentUserData.username else session.provider.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF1976D2),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Providing Help",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1976D2),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "0",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Seeker Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = (if (!isProvidingHelp) currentUserData.profilePic else session.seeker.profilePic)?.ifEmpty {
                            "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop"
                        } ?: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop",
                        contentDescription = "Seeker profile",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = if (!isProvidingHelp) currentUserData.username else session.seeker.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Help,
                                    contentDescription = null,
                                    tint = Color(0xFFF57C00),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Seeking Help",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF57C00),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "0",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Meeting Details
                Text(
                    text = "Requested on Topic: ${session.topic}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Description:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )
                if (session.description.isNotEmpty()) {
                    Text(
                        text = session.description,
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Date: ${session.date}",
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Duration: $timeText",
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Meeting status: ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = session.status,
                    fontSize = 14.sp,
                    color = Color(0xFFAB47BC),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!session.googleMeetLink.isNullOrEmpty()) {
                    Text(
                        text = "Google Meet Link: ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = session.googleMeetLink,
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))


                Button(
                    onClick = { showCancelConfirmation = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
                    enabled = !cancelLoading && session.status != "cancelled",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        disabledContainerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (cancelLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (session.status == "cancelled") "Already Cancelled" else "Cancel Booking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBDBDBD)
                        )
                    }
                }

                // Cancel Booking Button
//                Button(
//                    onClick = { showCancelConfirmation = true },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(48.dp),
//                    enabled = !cancelLoading && session.status != "cancelled",
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFE53935),
//                        disabledContainerColor = Color(0xFFBDBDBD)
//                    ),
//                    shape = RoundedCornerShape(8.dp)
//                ) {
//                    if (cancelLoading) {
//                        CircularProgressIndicator(
//                            modifier = Modifier.size(24.dp),
//                            color = Color.White,
//                            strokeWidth = 2.dp
//                        )
//                    } else {
//                        Text(
//                            text = if (session.status == "cancelled") "Already Cancelled" else "Cancel Booking",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White
//                        )
//                    }
//                }
            }
        }
    }

    // Confirmation Dialog
    if (showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmation = false },
            title = {
                Text(
                    text = "Cancel Booking",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to cancel this booking? This action cannot be undone and any payment will be refunded.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        Log.d("CancelBooking", "Attempting to cancel booking with ID: ${session.slotId}")
                        Log.d("CancelBooking", "Session details - topic: ${session.topic}, status: ${session.status}")

                        bookingViewModel.cancelBooking(session.slotId)
                        showCancelConfirmation = false
                        onCancelBooking()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmation = false }) {
                    Text("No, Keep It")
                }
            }
        )
    }
}

package com.cc.creatorcircle.ui.screens.notification

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Notification
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.NotificationViewModel
import com.cc.creatorcircle.viewModel.NotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current

    // Updated ViewModel initialization with custom factory
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(context)
    )

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("NotificationScreen", "NotificationScreen")
    }

    // Collect states from ViewModel
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadNotifications by notificationViewModel.unreadNotifications.collectAsState()
    val isLoading by notificationViewModel.notificationsLoading.collectAsState()
    val unreadLoading by notificationViewModel.unreadNotificationsLoading.collectAsState()
    val error by notificationViewModel.notificationsError.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val markAllReadLoading by notificationViewModel.markAllReadLoading.collectAsState()
    val markAllReadSuccess by notificationViewModel.markAllReadSuccess.collectAsState()
    val markAllReadError by notificationViewModel.markAllReadError.collectAsState()

    // State for selected tab (All or Unread)
    var selectedTab by remember { mutableStateOf("Unread") } // Changed default to "Unread"

    // Fetch notifications when screen loads
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("notifications_screen_opened")
        notificationViewModel.fetchAllNotifications()
        notificationViewModel.fetchUnreadNotifications()
        notificationViewModel.fetchUnreadCount()
    }

    // Handle mark all read success
    LaunchedEffect(markAllReadSuccess) {
        if (markAllReadSuccess) {
            // Refresh notifications after marking all as read
            notificationViewModel.fetchAllNotifications()
            notificationViewModel.fetchUnreadNotifications()
            notificationViewModel.fetchUnreadCount()
            notificationViewModel.resetMarkAllReadState()
        }
    }

    // Track tab changes
    LaunchedEffect(selectedTab) {
        FirebaseAnalyticsHelper.logEvent(
            "notification_tab_selected",
            mapOf("tab_name" to selectedTab)
        )
    }

    Scaffold(
        topBar = { TopBar(title = "Notifications", navController) },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAF8F8))
        ) {
            // Determine which loading state to show
            val showLoading = when (selectedTab) {
                "Unread" -> unreadLoading
                else -> isLoading
            }

            when {
                showLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFB388FF)
                        )
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                FirebaseAnalyticsHelper.logFeatureUsed("retry_notifications_load")
                                notificationViewModel.fetchAllNotifications()
                                notificationViewModel.fetchUnreadNotifications()
                                notificationViewModel.fetchUnreadCount()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB388FF)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    // Unread count and Mark all as read header - ALWAYS SHOW THIS SECTION
                    UnreadCountHeader(
                        unreadCount = unreadCount,
                        markAllReadLoading = markAllReadLoading,
                        onMarkAllAsRead = {
                            FirebaseAnalyticsHelper.logFeatureUsed("mark_all_as_read")
                            notificationViewModel.markAllNotificationsAsRead()
                        }
                    )

                    // Show error if mark all read failed
                    markAllReadError?.let {
                        MarkAllReadErrorBanner(
                            errorMessage = it,
                            onDismiss = { notificationViewModel.resetMarkAllReadState() }
                        )
                    }

                    // Tab selector
                    NotificationTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        allCount = notifications.size,
                        unreadCount = unreadNotifications.size
                    )

                    // Notifications list - Show based on selected tab
                    val displayNotifications = when (selectedTab) {
                        "Unread" -> unreadNotifications
                        else -> notifications
                    }

                    NotificationsList(
                        notifications = displayNotifications,
                        navController = navController,
                        notificationViewModel = notificationViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    allCount: Int,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unread Tab
        TabItem(
            text = "Unread",
            count = unreadCount,
            isSelected = selectedTab == "Unread",
            onClick = { onTabSelected("Unread") }
        )

        // All Tab
        TabItem(
            text = "All",
            count = allCount,
            isSelected = selectedTab == "All",
            onClick = { onTabSelected("All") }
        )
    }
}

@Composable
fun TabItem(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFFB388FF) else Color.Gray
            )
            Text(
                text = "($count)",
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFFB388FF) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(40.dp)
                    .background(
                        color = Color(0xFFB388FF),
                        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                    )
            )
        }
    }
}

@Composable
fun UnreadCountHeader(
    unreadCount: Int,
    markAllReadLoading: Boolean,
    onMarkAllAsRead: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (unreadCount > 0) "$unreadCount unread" else "All caught up!",
                fontSize = 14.sp,
                color = if (unreadCount > 0) Color(0xFFB388FF) else Color.Gray,
                fontWeight = FontWeight.Medium
            )

            if (unreadCount > 0) {
                if (markAllReadLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFB388FF),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Mark all as read",
                        fontSize = 14.sp,
                        color = Color(0xFFB388FF),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onMarkAllAsRead() }
                    )
                }
            }
        }

        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}

@Composable
fun MarkAllReadErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            color = Color(0xFFD32F2F),
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onDismiss
        ) {
            Text("Dismiss", fontSize = 12.sp, color = Color(0xFFD32F2F))
        }
    }
}

//@Composable
//fun NotificationsList(
//    notifications: List<Notification>,
//    navController: NavController,
//    notificationViewModel: NotificationViewModel
//) {
//    if (notifications.isEmpty()) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(32.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Notifications,
//                    contentDescription = "No notifications",
//                    modifier = Modifier.size(64.dp),
//                    tint = Color.Gray.copy(alpha = 0.5f)
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//                Text(
//                    text = "No notifications",
//                    color = Color.Gray,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//    } else {
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(vertical = 8.dp)
//        ) {
//            items(
//                count = notifications.size,
//                key = { index -> notifications[index].id }
//            ) { index ->
//                val notification = notifications[index]
//                NotificationItem(
//                    notification = notification,
//                    onClick = {
//                        FirebaseAnalyticsHelper.logEvent(
//                            "notification_clicked",
//                            mapOf(
//                                "notification_id" to notification.id.toString(),
//                                "notification_type" to notification.type,
//                                "notification_subtype" to (notification.subtype ?: "none")
//                            )
//                        )
//                        // Mark as read locally for immediate UI update
//                        notificationViewModel.markNotificationAsReadLocally(notification.id)
//
//                        // Handle navigation based on notification type
//                        handleNotificationClick(navController, notification)
//                    }
//                )
//            }
//        }
//    }
//}


// Replace the NotificationsList composable with this updated version

@Composable
fun NotificationsList(
    notifications: List<Notification>,
    navController: NavController,
    notificationViewModel: NotificationViewModel
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "No notifications",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No notifications",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                count = notifications.size,
                key = { index -> notifications[index].id }
            ) { index ->
                val notification = notifications[index]
                NotificationItem(
                    notification = notification,
                    onClick = {
                        FirebaseAnalyticsHelper.logEvent(
                            "notification_clicked",
                            mapOf(
                                "notification_id" to notification.id.toString(),
                                "notification_type" to notification.type,
                                "notification_subtype" to (notification.subtype ?: "none")
                            )
                        )

                        // Mark notification as read in API if it's unread
                        if (!notification.isRead) {
                            notificationViewModel.markNotificationAsRead(notification.id)
                        } else {
                            // If already read, just mark locally for immediate UI update
                            notificationViewModel.markNotificationAsReadLocally(notification.id)
                        }

                        // Handle navigation based on notification type
                        handleNotificationClick(navController, notification)
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val backgroundColor = if (notification.isUnread) Color(0xFFF5F0FF) else Color.White

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon based on notification type
            NotificationIcon(notification.type, notification.subtype)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatNotificationTime(notification.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (notification.isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB388FF))
                        )
                    }
                }
            }
        }

        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 56.dp)
        )
    }
}

@Composable
fun NotificationIcon(type: String, subtype: String?) {

    val (icon, backgroundColor) = when (type) {
        "booking" -> when (subtype) {
            "confirmed" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
            "cancelled" -> Pair(Icons.Default.Cancel, Color(0xFFF44336))
            else -> Pair(Icons.Default.Event, Color(0xFFB388FF))
        }
        "message" -> Pair(Icons.Default.Message, Color(0xFF2196F3))
        "friend_request" -> when (subtype) {
            "sent" -> Pair(Icons.Default.PersonAdd, Color(0xFFFF9800))
            "accepted" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
            else -> Pair(Icons.Default.Person, Color(0xFFB388FF))
        }
        "post_interaction" -> when (subtype) {
            "like" -> Pair(Icons.Default.Favorite, Color(0xFFE91E63))
            "comment" -> Pair(Icons.Default.ChatBubble, Color(0xFF9C27B0))
            "new_post" -> Pair(Icons.Default.Add, Color(0xFFB388FF))
            "unlike" -> Pair(Icons.Outlined.FavoriteBorder, Color(0xFF9E9E9E))
            else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
        }
        "mutual_friend" -> Pair(Icons.Default.Group, Color(0xFFFFB300))
        else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type,
            modifier = Modifier.size(20.dp),
            tint = backgroundColor
        )
    }
}

fun handleNotificationClick(navController: NavController, notification: Notification) {
    when (notification.type) {
        "booking" -> {
            navController.navigate("bookingScreen")
//            when (notification.subtype) {
//                "confirmed" -> {
//                    // Navigate to booking screen (all bookings)
//                    navController.navigate("bookingScreen")
//                }
//                "cancelled" -> {
//                    // Navigate to booking screen with cancelled tab
//                    // If your booking screen supports deep linking to tabs, use:
//                    navController.navigate("bookingScreen")
//                    // Otherwise just navigate to main booking screen:
//                    // navController.navigate("bookingScreen")
//                }
//                else -> {
//                    // Default booking navigation
//                    navController.navigate("bookingScreen")
//                }
//            }
        }

        "message" -> {
            navController.navigate(
                Screen.MessageScreen.createRouteWithUserId(userId = notification.senderId)
            )
//            when (notification.subtype) {
//                "direct_message" -> {
//                    // Navigate to message screen with sender ID
////                    navController.navigate("messageScreen/${notification.senderId}")
//                    navController.navigate(
//                        Screen.MessageScreen.createRouteWithUserId(userId = notification.senderId)
//                    )
//                }
//                else -> {
//                    // Navigate to general messages screen
////                    navController.navigate("messageScreen/${notification.senderId}")
//                    navController.navigate(
//                        Screen.MessageScreen.createRouteWithUserId(userId = notification.senderId)
//                    )
//                }
//            }
        }

        "friend_request" -> {
            navController.navigate("userProfile/${notification.senderId}")

//            when (notification.subtype) {
//                "sent" -> {
//                    // Navigate to sender's profile to accept/reject request
//                    navController.navigate("userProfile/${notification.senderId}")
//                }
//                "accepted" -> {
//                    // Navigate to the friend's profile who accepted the request
//                    navController.navigate("userProfile/${notification.senderId}")
//                }
//                else -> {
//                    // Default navigation to sender's profile
//                    navController.navigate("userProfile/${notification.senderId}")
//                }
//            }
        }

        "post_interaction" -> {
            notification.postId?.let { postId ->
                navController.navigate("postscreen/$postId")
            }
        }

        "mutual_friend" -> {
            // Navigate to the mutual friend's profile
            navController.navigate("userProfile/${notification.senderId}")
        }

        else -> {
            // For any unknown notification types, log and optionally navigate to a default screen
            FirebaseAnalyticsHelper.logEvent(
                "unknown_notification_type",
                mapOf(
                    "type" to notification.type,
                    "subtype" to (notification.subtype ?: "none")
                )
            )
            // Optionally navigate to home or notifications screen
            // navController.navigate("home")
        }
    }
}

fun formatNotificationTime(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(createdAt)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
            days < 30 -> "${days / 7} ${if (days / 7 == 1L) "week" else "weeks"} ago"
            else -> date?.let { outputFormat.format(it) } ?: createdAt
        }
    } catch (e: Exception) {
        createdAt
    }
}











/*
package com.cc.creatorcircle.ui.screens.notification

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Notification
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.NotificationViewModel
import com.cc.creatorcircle.viewModel.NotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current

    // Updated ViewModel initialization with custom factory
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(context)
    )

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("NotificationScreen", "NotificationScreen")
    }

    // Collect states from ViewModel
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadNotifications by notificationViewModel.unreadNotifications.collectAsState()
    val isLoading by notificationViewModel.notificationsLoading.collectAsState()
    val unreadLoading by notificationViewModel.unreadNotificationsLoading.collectAsState()
    val error by notificationViewModel.notificationsError.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val markAllReadLoading by notificationViewModel.markAllReadLoading.collectAsState()
    val markAllReadSuccess by notificationViewModel.markAllReadSuccess.collectAsState()
    val markAllReadError by notificationViewModel.markAllReadError.collectAsState()

    // State for selected tab (All or Unread)
    var selectedTab by remember { mutableStateOf("Unread") } // Changed default to "Unread"

    // Fetch notifications when screen loads
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("notifications_screen_opened")
        notificationViewModel.fetchAllNotifications()
        notificationViewModel.fetchUnreadNotifications()
        notificationViewModel.fetchUnreadCount()
    }

    // Handle mark all read success
    LaunchedEffect(markAllReadSuccess) {
        if (markAllReadSuccess) {
            // Refresh notifications after marking all as read
            notificationViewModel.fetchAllNotifications()
            notificationViewModel.fetchUnreadNotifications()
            notificationViewModel.fetchUnreadCount()
            notificationViewModel.resetMarkAllReadState()
        }
    }

    // Track tab changes
    LaunchedEffect(selectedTab) {
        FirebaseAnalyticsHelper.logEvent(
            "notification_tab_selected",
            mapOf("tab_name" to selectedTab)
        )
    }

    Scaffold(
        topBar = { TopBar(title = "Notifications", navController) },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAF8F8))
        ) {
            // Determine which loading state to show
            val showLoading = when (selectedTab) {
                "Unread" -> unreadLoading
                else -> isLoading
            }

            when {
                showLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFB388FF)
                        )
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                FirebaseAnalyticsHelper.logFeatureUsed("retry_notifications_load")
                                notificationViewModel.fetchAllNotifications()
                                notificationViewModel.fetchUnreadNotifications()
                                notificationViewModel.fetchUnreadCount()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB388FF)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    // Unread count and Mark all as read header - ALWAYS SHOW THIS SECTION
                    UnreadCountHeader(
                        unreadCount = unreadCount,
                        markAllReadLoading = markAllReadLoading,
                        onMarkAllAsRead = {
                            FirebaseAnalyticsHelper.logFeatureUsed("mark_all_as_read")
                            notificationViewModel.markAllNotificationsAsRead()
                        }
                    )

                    // Show error if mark all read failed
                    markAllReadError?.let {
                        MarkAllReadErrorBanner(
                            errorMessage = it,
                            onDismiss = { notificationViewModel.resetMarkAllReadState() }
                        )
                    }

                    // Tab selector
                    NotificationTabs(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        allCount = notifications.size,
                        unreadCount = unreadNotifications.size
                    )

                    // Notifications list - Show based on selected tab
                    val displayNotifications = when (selectedTab) {
                        "Unread" -> unreadNotifications
                        else -> notifications
                    }

                    NotificationsList(
                        notifications = displayNotifications,
                        navController = navController,
                        notificationViewModel = notificationViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    allCount: Int,
    unreadCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Unread Tab
        TabItem(
            text = "Unread",
            count = unreadCount,
            isSelected = selectedTab == "Unread",
            onClick = { onTabSelected("Unread") }
        )

        // All Tab
        TabItem(
            text = "All",
            count = allCount,
            isSelected = selectedTab == "All",
            onClick = { onTabSelected("All") }
        )
    }
}

@Composable
fun TabItem(
    text: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color(0xFFB388FF) else Color.Gray
            )
            Text(
                text = "($count)",
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFFB388FF) else Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isSelected) {
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(40.dp)
                    .background(
                        color = Color(0xFFB388FF),
                        shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                    )
            )
        }
    }
}

@Composable
fun UnreadCountHeader(
    unreadCount: Int,
    markAllReadLoading: Boolean,
    onMarkAllAsRead: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (unreadCount > 0) "$unreadCount unread" else "All caught up!",
                fontSize = 14.sp,
                color = if (unreadCount > 0) Color(0xFFB388FF) else Color.Gray,
                fontWeight = FontWeight.Medium
            )

            if (unreadCount > 0) {
                if (markAllReadLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFB388FF),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Mark all as read",
                        fontSize = 14.sp,
                        color = Color(0xFFB388FF),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onMarkAllAsRead() }
                    )
                }
            }
        }

        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp
        )
    }
}

@Composable
fun MarkAllReadErrorBanner(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFEBEE))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            color = Color(0xFFD32F2F),
            modifier = Modifier.weight(1f)
        )
        TextButton(
            onClick = onDismiss
        ) {
            Text("Dismiss", fontSize = 12.sp, color = Color(0xFFD32F2F))
        }
    }
}

@Composable
fun NotificationsList(
    notifications: List<Notification>,
    navController: NavController,
    notificationViewModel: NotificationViewModel
) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "No notifications",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No notifications",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                count = notifications.size,
                key = { index -> notifications[index].id }
            ) { index ->
                val notification = notifications[index]
                NotificationItem(
                    notification = notification,
                    onClick = {
                        FirebaseAnalyticsHelper.logEvent(
                            "notification_clicked",
                            mapOf(
                                "notification_id" to notification.id.toString(),
                                "notification_type" to notification.type
                            )
                        )
                        // Mark as read locally for immediate UI update
                        notificationViewModel.markNotificationAsReadLocally(notification.id)

                        // Handle navigation based on notification type
                        handleNotificationClick(navController, notification)
                    }
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val backgroundColor = if (notification.isUnread) Color(0xFFF5F0FF) else Color.White

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon based on notification type
            NotificationIcon(notification.type, notification.subtype)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatNotificationTime(notification.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    if (notification.isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB388FF))
                        )
                    }
                }
            }
        }

        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 0.5.dp,
            modifier = Modifier.padding(start = 56.dp)
        )
    }
}

@Composable
fun NotificationIcon(type: String, subtype: String?) {

    val (icon, backgroundColor) = when (type) {
        "booking" -> when (subtype) {
            "confirmed" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
            "cancelled" -> Pair(Icons.Default.Cancel, Color(0xFFF44336))
            else -> Pair(Icons.Default.Event, Color(0xFFB388FF))
        }
        "message" -> Pair(Icons.Default.Message, Color(0xFF2196F3))
        "friend_request" -> when (subtype) {
            "sent" -> Pair(Icons.Default.PersonAdd, Color(0xFFFF9800))
            "accepted" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
            else -> Pair(Icons.Default.Person, Color(0xFFB388FF))
        }
        "post_interaction" -> when (subtype) {
            "like" -> Pair(Icons.Default.Favorite, Color(0xFFE91E63))
            "comment" -> Pair(Icons.Default.ChatBubble, Color(0xFF9C27B0))
            "new_post" -> Pair(Icons.Default.Add, Color(0xFFB388FF))
            "unlike" -> Pair(Icons.Outlined.FavoriteBorder, Color(0xFF9E9E9E))
            else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
        }
        "mutual_friend" -> Pair(Icons.Default.Group, Color(0xFFFFB300))
        else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type,
            modifier = Modifier.size(20.dp),
            tint = backgroundColor
        )
    }
}

fun handleNotificationClick(navController: NavController, notification: Notification) {
    when (notification.type) {
        "booking" -> {
            // Navigate to bookings screen or booking detail
            // navController.navigate("booking_detail/${notification.id}")
        }
        "message" -> {
            // Navigate to chat or messages
            // navController.navigate("chat/${notification.senderId}")
        }
        "friend_request" -> {
            // Navigate to connections or profile
            // navController.navigate("profile/${notification.senderId}")
        }
        "post_interaction" -> {
            notification.postId?.let { postId ->
                // Navigate to post detail
                // navController.navigate("post_detail/$postId")
            }
        }
        "mutual_friend" -> {
            // Navigate to connections or profile
            // navController.navigate("profile/${notification.senderId}")
        }
    }
}

fun formatNotificationTime(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(createdAt)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
            days < 30 -> "${days / 7} ${if (days / 7 == 1L) "week" else "weeks"} ago"
            else -> date?.let { outputFormat.format(it) } ?: createdAt
        }
    } catch (e: Exception) {
        createdAt
    }
}

*/









//package com.cc.creatorcircle.ui.screens.notification
//
//import android.annotation.SuppressLint
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.Message
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Cancel
//import androidx.compose.material.icons.filled.ChatBubble
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Event
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.Group
//import androidx.compose.material.icons.filled.Message
//import androidx.compose.material.icons.filled.Notifications
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.PersonAdd
//import androidx.compose.material.icons.outlined.FavoriteBorder
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.data.models.Notification
//import com.cc.creatorcircle.ui.components.BottomNavBar
//import com.cc.creatorcircle.ui.components.TopBar
//import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
//import com.cc.creatorcircle.viewModel.NotificationViewModel
//import com.cc.creatorcircle.viewModel.NotificationViewModelFactory
//import java.text.SimpleDateFormat
//import java.util.*
//
//@Composable
//fun NotificationScreen(navController: NavController) {
//    val context = LocalContext.current
//
//    // Updated ViewModel initialization with custom factory
//    val notificationViewModel: NotificationViewModel = viewModel(
//        factory = NotificationViewModelFactory(context)
//    )
//
//    // Track screen view
//    LaunchedEffect(Unit) {
//        FirebaseAnalyticsHelper.logScreenView("NotificationScreen", "NotificationScreen")
//    }
//
//    // Collect states from ViewModel
//    val notifications by notificationViewModel.notifications.collectAsState()
//    val unreadNotifications by notificationViewModel.unreadNotifications.collectAsState()
//    val isLoading by notificationViewModel.notificationsLoading.collectAsState()
//    val error by notificationViewModel.notificationsError.collectAsState()
//    val unreadCount by notificationViewModel.unreadCount.collectAsState()
//    val markAllReadLoading by notificationViewModel.markAllReadLoading.collectAsState()
//    val markAllReadSuccess by notificationViewModel.markAllReadSuccess.collectAsState()
//    val markAllReadError by notificationViewModel.markAllReadError.collectAsState()
//
//    // State for selected tab (All or Unread)
//    var selectedTab by remember { mutableStateOf("All") }
//
//    // Fetch notifications when screen loads
//    LaunchedEffect(Unit) {
//        FirebaseAnalyticsHelper.logEvent("notifications_screen_opened")
//        notificationViewModel.fetchAllNotifications()
//        notificationViewModel.fetchUnreadNotifications()
//        notificationViewModel.fetchUnreadCount()
//    }
//
//    // Handle mark all read success
//    LaunchedEffect(markAllReadSuccess) {
//        if (markAllReadSuccess) {
//            // Refresh notifications after marking all as read
//            notificationViewModel.fetchAllNotifications()
//            notificationViewModel.fetchUnreadNotifications()
//            notificationViewModel.fetchUnreadCount()
//            notificationViewModel.resetMarkAllReadState()
//        }
//    }
//
//    // Track tab changes
//    LaunchedEffect(selectedTab) {
//        FirebaseAnalyticsHelper.logEvent(
//            "notification_tab_selected",
//            mapOf("tab_name" to selectedTab)
//        )
//    }
//
//    Scaffold(
//        topBar = { TopBar(title = "Notifications", navController) },
//        bottomBar = { BottomNavBar(navController = navController) },
//        modifier = Modifier.fillMaxSize()
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .background(Color(0xFFFAF8F8))
//        ) {
//            when {
//                isLoading -> {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.Center),
//                        color = Color(0xFFB388FF)
//                    )
//                }
//
//                error != null -> {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(16.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = error ?: "An error occurred",
//                            color = Color.Red,
//                            fontSize = 16.sp
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        Button(
//                            onClick = {
//                                FirebaseAnalyticsHelper.logFeatureUsed("retry_notifications_load")
//                                notificationViewModel.fetchAllNotifications()
//                                notificationViewModel.fetchUnreadNotifications()
//                                notificationViewModel.fetchUnreadCount()
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xFFB388FF)
//                            )
//                        ) {
//                            Text("Retry")
//                        }
//                    }
//                }
//
//                else -> {
//                    NotificationContent(
//                        navController = navController,
//                        allNotifications = notifications,
//                        unreadNotifications = unreadNotifications,
//                        selectedTab = selectedTab,
//                        onTabSelected = { tab -> selectedTab = tab },
//                        notificationViewModel = notificationViewModel,
//                        markAllReadLoading = markAllReadLoading,
//                        markAllReadError = markAllReadError
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun NotificationContent(
//    navController: NavController,
//    allNotifications: List<Notification>,
//    unreadNotifications: List<Notification>,
//    selectedTab: String,
//    onTabSelected: (String) -> Unit,
//    notificationViewModel: NotificationViewModel,
//    markAllReadLoading: Boolean,
//    markAllReadError: String?
//) {
//    val displayNotifications = if (selectedTab == "All") allNotifications else unreadNotifications
//    val unreadCount = allNotifications.count { it.isUnread }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFAF8F8))
//    ) {
//        // Unread count and Mark all as read header
//        if (unreadCount > 0) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color.White)
//                    .padding(horizontal = 16.dp, vertical = 12.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "$unreadCount unread",
//                    fontSize = 14.sp,
//                    color = Color(0xFFB388FF),
//                    fontWeight = FontWeight.Medium
//                )
//
//                if (markAllReadLoading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(16.dp),
//                        color = Color(0xFFB388FF),
//                        strokeWidth = 2.dp
//                    )
//                } else {
//                    Text(
//                        text = "Mark all as read",
//                        fontSize = 14.sp,
//                        color = Color(0xFFB388FF),
//                        fontWeight = FontWeight.Medium,
//                        modifier = Modifier.clickable {
//                            FirebaseAnalyticsHelper.logFeatureUsed("mark_all_as_read")
//                            notificationViewModel.markAllNotificationsAsRead()
//                        }
//                    )
//                }
//            }
//
//            Divider(
//                color = Color(0xFFEEEEEE),
//                thickness = 1.dp
//            )
//        }
//
//        // Show error if mark all read failed
//        if (markAllReadError != null) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFFFFEBEE))
//                    .padding(horizontal = 16.dp, vertical = 8.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = markAllReadError,
//                    fontSize = 12.sp,
//                    color = Color(0xFFD32F2F),
//                    modifier = Modifier.weight(1f)
//                )
//                TextButton(
//                    onClick = { notificationViewModel.resetMarkAllReadState() }
//                ) {
//                    Text("Dismiss", fontSize = 12.sp)
//                }
//            }
//        }
//
//        // Notifications list
//        if (displayNotifications.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(32.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "No notifications",
//                    color = Color.Gray,
//                    fontSize = 16.sp
//                )
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(vertical = 8.dp)
//            ) {
//                items(
//                    count = displayNotifications.size,
//                    key = { index -> displayNotifications[index].id }
//                ) { index ->
//                    val notification = displayNotifications[index]
//                    NotificationItem(
//                        notification = notification,
//                        onClick = {
//                            FirebaseAnalyticsHelper.logEvent(
//                                "notification_clicked",
//                                mapOf(
//                                    "notification_id" to notification.id.toString(),
//                                    "notification_type" to notification.type
//                                )
//                            )
//                            // Mark as read locally for immediate UI update
//                            notificationViewModel.markNotificationAsReadLocally(notification.id)
//
//                            // Handle navigation based on notification type
//                            handleNotificationClick(navController, notification)
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun NotificationItem(
//    notification: Notification,
//    onClick: () -> Unit
//) {
//    val backgroundColor = if (notification.isUnread) Color(0xFFF5F0FF) else Color.White
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(backgroundColor)
//            .clickable { onClick() }
//            .padding(horizontal = 16.dp, vertical = 12.dp),
//        verticalAlignment = Alignment.Top
//    ) {
//        // Icon based on notification type
//        NotificationIcon(notification.type, notification.subtype)
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = notification.message,
//                fontSize = 14.sp,
//                color = Color.Black,
//                lineHeight = 20.sp
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = formatNotificationTime(notification.createdAt),
//                    fontSize = 12.sp,
//                    color = Color.Gray
//                )
//
//                if (notification.isUnread) {
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFFB388FF))
//                    )
//                }
//            }
//        }
//    }
//
//    Divider(
//        color = Color(0xFFEEEEEE),
//        thickness = 0.5.dp,
//        modifier = Modifier.padding(start = 56.dp)
//    )
//}
//
//@Composable
//fun NotificationIcon(type: String, subtype: String?) {
//
//    val (icon, backgroundColor) = when (type) {
//        "booking" -> when (subtype) {
//            "confirmed" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
//            "cancelled" -> Pair(Icons.Default.Cancel, Color(0xFFF44336))
//            else -> Pair(Icons.Default.Event, Color(0xFFB388FF))
//        }
//        "message" -> Pair(Icons.Default.Message, Color(0xFF2196F3))
//        "friend_request" -> when (subtype) {
//            "sent" -> Pair(Icons.Default.PersonAdd, Color(0xFFFF9800))
//            "accepted" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
//            else -> Pair(Icons.Default.Person, Color(0xFFB388FF))
//        }
//        "post_interaction" -> when (subtype) {
//            "like" -> Pair(Icons.Default.Favorite, Color(0xFFE91E63))
//            "comment" -> Pair(Icons.Default.ChatBubble, Color(0xFF9C27B0))
//            "new_post" -> Pair(Icons.Default.Add, Color(0xFFB388FF))
//            "unlike" -> Pair(Icons.Outlined.FavoriteBorder, Color(0xFF9E9E9E))
//            else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
//        }
//        "mutual_friend" -> Pair(Icons.Default.Group, Color(0xFFFFB300))
//        else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
//    }
//
//    Box(
//        modifier = Modifier
//            .size(40.dp)
//            .clip(CircleShape)
//            .background(backgroundColor.copy(alpha = 0.15f)),
//        contentAlignment = Alignment.Center
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = type,
//            modifier = Modifier.size(20.dp),
//            tint = backgroundColor
//        )
//    }
//}
//
//fun handleNotificationClick(navController: NavController, notification: Notification) {
//    when (notification.type) {
//        "booking" -> {
//            // Navigate to bookings screen or booking detail
//            // navController.navigate("booking_detail/${notification.id}")
//        }
//        "message" -> {
//            // Navigate to chat or messages
//            // navController.navigate("chat/${notification.senderId}")
//        }
//        "friend_request" -> {
//            // Navigate to connections or profile
//            // navController.navigate("profile/${notification.senderId}")
//        }
//        "post_interaction" -> {
//            notification.postId?.let { postId ->
//                // Navigate to post detail
//                // navController.navigate("post_detail/$postId")
//            }
//        }
//        "mutual_friend" -> {
//            // Navigate to connections or profile
//            // navController.navigate("profile/${notification.senderId}")
//        }
//    }
//}
//
//fun formatNotificationTime(createdAt: String): String {
//    return try {
//        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
//        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
//        val date = inputFormat.parse(createdAt)
//
//        val now = Date()
//        val diff = now.time - (date?.time ?: 0)
//
//        val seconds = diff / 1000
//        val minutes = seconds / 60
//        val hours = minutes / 60
//        val days = hours / 24
//
//        when {
//            seconds < 60 -> "Just now"
//            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
//            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
//            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
//            days < 30 -> "${days / 7} ${if (days / 7 == 1L) "week" else "weeks"} ago"
//            else -> date?.let { outputFormat.format(it) } ?: createdAt
//        }
//    } catch (e: Exception) {
//        createdAt
//    }
//}








//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun NotificationTopBar(unreadCount: Int, onMarkAllAsRead: () -> Unit) {
//    TopAppBar(
//        title = {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "Notifications",
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//
//                if (unreadCount > 0) {
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "$unreadCount unread",
//                            fontSize = 14.sp,
//                            color = Color(0xFFB388FF),
//                            fontWeight = FontWeight.Medium
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = "Mark all as read",
//                            fontSize = 14.sp,
//                            color = Color(0xFFB388FF),
//                            fontWeight = FontWeight.Medium,
//                            modifier = Modifier.clickable {
//                                FirebaseAnalyticsHelper.logFeatureUsed("mark_all_as_read")
//                                onMarkAllAsRead()
//                            }
//                        )
//                    }
//                }
//            }
//        },
//        colors = TopAppBarDefaults.topAppBarColors(
//            containerColor = Color.White
//        ),
//        modifier = Modifier.height(64.dp)
//    )
//}





/*
package com.cc.creatorcircle.ui.screens.notification

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.Notification
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.NotificationViewModel
import com.cc.creatorcircle.viewModel.NotificationViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

//@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NotificationScreen(navController: NavController) {
    val context = LocalContext.current

    // Updated ViewModel initialization with custom factory
    val notificationViewModel: NotificationViewModel = viewModel(
        factory = NotificationViewModelFactory(context)
    )

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("NotificationScreen", "NotificationScreen")
    }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("NotificationScreen", "NotificationScreen")
    }

    // Collect states from ViewModel
    val notifications by notificationViewModel.notifications.collectAsState()
    val unreadNotifications by notificationViewModel.unreadNotifications.collectAsState()
    val isLoading by notificationViewModel.notificationsLoading.collectAsState()
    val error by notificationViewModel.notificationsError.collectAsState()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // State for selected tab (All or Unread)
    var selectedTab by remember { mutableStateOf("All") }

    // Fetch notifications when screen loads
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("notifications_screen_opened")
        notificationViewModel.fetchAllNotifications()
        notificationViewModel.fetchUnreadNotifications()
    }

    // Track tab changes
    LaunchedEffect(selectedTab) {
        FirebaseAnalyticsHelper.logEvent(
            "notification_tab_selected",
            mapOf("tab_name" to selectedTab)
        )
    }

    Scaffold(
        topBar = { TopBar(title = "Notifications", navController) },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAF8F8))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFFB388FF)
                    )
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                FirebaseAnalyticsHelper.logFeatureUsed("retry_notifications_load")
                                notificationViewModel.fetchAllNotifications()
                                notificationViewModel.fetchUnreadNotifications()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB388FF)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    NotificationContent(
                        navController = navController,
                        allNotifications = notifications,
                        unreadNotifications = unreadNotifications,
                        selectedTab = selectedTab,
                        onTabSelected = { tab -> selectedTab = tab },
                        notificationViewModel = notificationViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTopBar(unreadCount: Int) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (unreadCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$unreadCount unread",
                            fontSize = 14.sp,
                            color = Color(0xFFB388FF),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mark all as read",
                            fontSize = 14.sp,
                            color = Color(0xFFB388FF),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                FirebaseAnalyticsHelper.logFeatureUsed("mark_all_as_read")
                                // TODO: Implement mark all as read functionality
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ),
        modifier = Modifier.height(64.dp)
    )
}


@Composable
fun NotificationContent(
    navController: NavController,
    allNotifications: List<Notification>,
    unreadNotifications: List<Notification>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    notificationViewModel: NotificationViewModel
) {
    val displayNotifications = if (selectedTab == "All") allNotifications else unreadNotifications
    val unreadCount = allNotifications.count { it.isUnread }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
        // Unread count and Mark all as read header
        if (unreadCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$unreadCount unread",
                    fontSize = 14.sp,
                    color = Color(0xFFB388FF),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Mark all as read",
                    fontSize = 14.sp,
                    color = Color(0xFFB388FF),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("mark_all_as_read")
                        // Mark all notifications as read
                        allNotifications.forEach { notification ->
                            if (notification.isUnread) {
                                notificationViewModel.markNotificationAsReadLocally(notification.id)
                            }
                        }
                    }
                )
            }

            Divider(
                color = Color(0xFFEEEEEE),
                thickness = 1.dp
            )
        }

        // Notifications list
        if (displayNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notifications",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    count = displayNotifications.size,
                    key = { index -> displayNotifications[index].id }
                ) { index ->
                    val notification = displayNotifications[index]
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            FirebaseAnalyticsHelper.logEvent(
                                "notification_clicked",
                                mapOf(
                                    "notification_id" to notification.id.toString(),
                                    "notification_type" to notification.type
                                )
                            )
                            // Mark as read
                            notificationViewModel.markNotificationAsReadLocally(notification.id)

                            // Handle navigation based on notification type
                            handleNotificationClick(navController, notification)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    val backgroundColor = if (notification.isUnread) Color(0xFFF5F0FF) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon based on notification type
        NotificationIcon(notification.type, notification.subtype)

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatNotificationTime(notification.createdAt),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                if (notification.isUnread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFB388FF))
                    )
                }
            }
        }
    }

    Divider(
        color = Color(0xFFEEEEEE),
        thickness = 0.5.dp,
        modifier = Modifier.padding(start = 56.dp)
    )
}

@Composable
fun NotificationIcon(type: String, subtype: String?) {

    val (icon, backgroundColor) = when (type) {
        "booking" -> when (subtype) {
            "confirmed" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
            "cancelled" -> Pair(Icons.Default.Cancel, Color(0xFFF44336))
            else -> Pair(Icons.Default.Event, Color(0xFFB388FF))
        }
        "message" -> Pair(Icons.Default.Message, Color(0xFF2196F3))
        "friend_request" -> when (subtype) {
            "sent" -> Pair(Icons.Default.PersonAdd, Color(0xFFFF9800))
            "accepted" -> Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
            else -> Pair(Icons.Default.Person, Color(0xFFB388FF))
        }
        "post_interaction" -> when (subtype) {
            "like" -> Pair(Icons.Default.Favorite, Color(0xFFE91E63))
            "comment" -> Pair(Icons.Default.ChatBubble, Color(0xFF9C27B0))
            "new_post" -> Pair(Icons.Default.Add, Color(0xFFB388FF))
            "unlike" -> Pair(Icons.Outlined.FavoriteBorder, Color(0xFF9E9E9E))
            else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
        }
        "mutual_friend" -> Pair(Icons.Default.Group, Color(0xFFFFB300))
        else -> Pair(Icons.Default.Notifications, Color(0xFFB388FF))
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type,
            modifier = Modifier.size(20.dp),
            tint = backgroundColor
        )
    }
}

fun handleNotificationClick(navController: NavController, notification: Notification) {
    when (notification.type) {
        "booking" -> {
            // Navigate to bookings screen or booking detail
            // navController.navigate("booking_detail/${notification.id}")
        }
        "message" -> {
            // Navigate to chat or messages
            // navController.navigate("chat/${notification.senderId}")
        }
        "friend_request" -> {
            // Navigate to connections or profile
            // navController.navigate("profile/${notification.senderId}")
        }
        "post_interaction" -> {
            notification.postId?.let { postId ->
                // Navigate to post detail
                // navController.navigate("post_detail/$postId")
            }
        }
        "mutual_friend" -> {
            // Navigate to connections or profile
            // navController.navigate("profile/${notification.senderId}")
        }
    }
}

fun formatNotificationTime(createdAt: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        val date = inputFormat.parse(createdAt)

        val now = Date()
        val diff = now.time - (date?.time ?: 0)

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
            days < 30 -> "${days / 7} ${if (days / 7 == 1L) "week" else "weeks"} ago"
            else -> date?.let { outputFormat.format(it) } ?: createdAt
        }
    } catch (e: Exception) {
        createdAt
    }
}

*/

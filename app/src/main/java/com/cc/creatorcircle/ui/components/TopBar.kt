@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.viewModel.UserConnectionsViewModel
import com.cc.creatorcircle.viewModel.UserViewModel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource

// Data class for notification state
data class NotificationState(
    val hasUnreadNotifications: Boolean = false,
    val unreadCount: Int = 0,
    val isLoading: Boolean = false
)


@Composable
fun TopBar(
    title: String,
    navController: NavController,
    selectedTab: String = title,
    onTabSelected: (String) -> Unit = {},
    notificationState: NotificationState = NotificationState(),
    onNotificationClick: () -> Unit = {}
) {
    val tabs = listOf(title)
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 2.dp), // Reduced from 4dp to 8dp total
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo with accessibility - reduced size slightly
        Image(
            painter = painterResource(id = R.drawable.ic_cc_logo),
            contentDescription = "Creator Circle Logo",
            modifier = Modifier
                .size(36.dp) // Reduced from 40dp to 36dp
                .semantics {
                    contentDescription = "Creator Circle application logo"
                }
        )

        Spacer(modifier = Modifier.width(12.dp)) // Reduced from 14dp to 12dp

        // Tab section
        tabs.forEachIndexed { index, tab ->
            val isSelected = tab == selectedTab

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = "Select $tab tab"
                        ) {
                            onTabSelected(tab)
                        }
                        .semantics {
                            role = Role.Tab
                            contentDescription = "$tab tab, currently selected"
                        }
                ) {
                    Text(
                        text = tab,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 2.dp
                        ) // Reduced padding
                    )
                }
            } else {
                Text(
                    text = tab,
                    color = Color(0xFFB388FF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = "Select $tab tab"
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp) // Adjusted padding
                        .semantics {
                            role = Role.Tab
                            contentDescription = "$tab tab"
                        }
                )
            }

            if (index != tabs.lastIndex) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Professional notification button with badge - optimized size
        Box(
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    try {
                        onNotificationClick()
                        navController.navigate("notifications")
                    } catch (e: Exception) {
                        // Handle navigation error gracefully
                        // You might want to log this or show a snackbar
                    }
                },
                modifier = Modifier
                    .size(44.dp) // Reduced from 48dp to 44dp
                    .semantics {
                        contentDescription = if (notificationState.hasUnreadNotifications) {
                            "Notifications, ${notificationState.unreadCount} unread"
                        } else {
                            "Notifications"
                        }
                    }
            ) {
                if (notificationState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFB0A9A9)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null, // Handled by IconButton
                        tint = if (notificationState.hasUnreadNotifications) {
                            Color(0xFFB726FF)
                        } else {
                            Color(0xFFB0A9A9)
                        },
                        modifier = Modifier.size(22.dp) // Reduced from 24dp to 22dp
                    )
                }
            }

            // Notification badge
            if (notificationState.hasUnreadNotifications && !notificationState.isLoading) {
                Box(
                    modifier = Modifier
                        .offset(x = 7.dp, y = (-7).dp) // Slightly adjusted offset
                        .size(
                            if (notificationState.unreadCount > 99) 22.dp
                            else if (notificationState.unreadCount > 9) 18.dp
                            else 15.dp
                        )
                        .background(
                            Color(0xFFFF4444),
                            CircleShape
                        )
                        .semantics {
                            contentDescription =
                                "${notificationState.unreadCount} unread notifications"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            notificationState.unreadCount > 99 -> "99+"
                            else -> notificationState.unreadCount.toString()
                        },
                        color = Color.White,
                        fontSize = when {
                            notificationState.unreadCount > 99 -> 7.sp
                            notificationState.unreadCount > 9 -> 8.sp
                            else -> 9.sp
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


@Composable
fun TopBarSabo(
    onClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 2.dp), // Reduced from 4dp to 8dp total
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Menu icon on the left
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_menu),
                contentDescription = "Menu",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        // SABO logo on the right
        Icon(
            painter = painterResource(id = R.drawable.ic_sabo),
            contentDescription = "SABO",
            tint = Color.Unspecified, // Keep original colors
            modifier = Modifier
                .width(100.dp) // Adjust height as needed
                .height(30.dp) // Adjust height as needed
//                .fillMaxWidth()
        )
    }
}


@Composable
fun TopBarProfile(
    title: String,
    navController: NavController,
    userViewModel: UserViewModel,
    selectedTab: String = title,
    onTabSelected: (String) -> Unit = {},
    notificationState: NotificationState = NotificationState(),
    onNotificationClick: () -> Unit = {}
) {
    val tabs = listOf(title)
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }


    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(
                    text = "Log out?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to log out?",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Cancel button on the left
                    CustomOutlinedButton(
                        "Cancel",
                        modifier = Modifier
                            .semantics {
                                contentDescription = "Cancel logout"
                            }
                            .padding(end = 8.dp)
                    ) {
                        showLogoutDialog = false
                    }


                    GradientButton("Log out") {
                        Log.d("LOGOUT", "Button clicked")
                        showLogoutDialog = false
                        Log.d("LOGOUT", "Dialog dismissed")

                        // Simply call the ViewModel's logout method
                        // The ViewModel will handle everything: API logout, Google sign-out, and local data clearing
                        userViewModel.logout(performGoogleSignOut = true)

                        // Navigate to login screen
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }


                }
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .semantics {
                    contentDescription = "Logout confirmation dialog"
                }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo with accessibility
        Image(
            painter = painterResource(id = R.drawable.ic_cc_logo),
            contentDescription = "Creator Circle Logo",
            modifier = Modifier
                .size(36.dp)
                .semantics {
                    contentDescription = "Creator Circle application logo"
                }
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Tab section
        tabs.forEachIndexed { index, tab ->
            val isSelected = tab == selectedTab

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = "Select $tab tab"
                        ) {
                            onTabSelected(tab)
                        }
                        .semantics {
                            role = Role.Tab
                            contentDescription = "$tab tab, currently selected"
                        }
                ) {
                    Text(
                        text = tab,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    text = tab,
                    color = Color(0xFFB388FF),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(
                            role = Role.Tab,
                            onClickLabel = "Select $tab tab"
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .semantics {
                            role = Role.Tab
                            contentDescription = "$tab tab"
                        }
                )
            }

            if (index != tabs.lastIndex) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Professional notification button with badge
        Box(
            contentAlignment = Alignment.Center
        )
        {
            IconButton(
                onClick = {
                    try {
                        onNotificationClick()
                        navController.navigate("notifications")
                    } catch (e: Exception) {
                        // Handle navigation error gracefully
                        // You might want to log this or show a snackbar
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = if (notificationState.hasUnreadNotifications) {
                            "Notifications, ${notificationState.unreadCount} unread"
                        } else {
                            "Notifications"
                        }
                    }
            ) {
                if (notificationState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFB0A9A9)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null, // Handled by IconButton
                        tint = if (notificationState.hasUnreadNotifications) {
                            Color(0xFFB726FF)
                        } else {
                            Color(0xFFB0A9A9)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Notification badge
            if (notificationState.hasUnreadNotifications && !notificationState.isLoading) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(
                            if (notificationState.unreadCount > 99) 24.dp
                            else if (notificationState.unreadCount > 9) 20.dp
                            else 16.dp
                        )
                        .background(
                            Color(0xFFFF4444),
                            CircleShape
                        )
                        .semantics {
                            contentDescription =
                                "${notificationState.unreadCount} unread notifications"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            notificationState.unreadCount > 99 -> "99+"
                            else -> notificationState.unreadCount.toString()
                        },
                        color = Color.White,
                        fontSize = when {
                            notificationState.unreadCount > 99 -> 8.sp
                            notificationState.unreadCount > 9 -> 9.sp
                            else -> 10.sp
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

        }


        // Simplified working notification button
        IconButton(
            onClick = {
                navController.navigate("creatorcoin")
            },
            modifier = Modifier.size(48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_coin),
                contentDescription = "Coin Icon",
                modifier = Modifier.size(25.dp), // Slightly smaller than button for proper padding
                colorFilter = null
            )
        }





        Spacer(modifier = Modifier.width(8.dp))

        // Logout button - now shows confirmation dialog
        IconButton(
            onClick = {
                showLogoutDialog = true
            },
            modifier = Modifier
                .size(48.dp)
                .semantics {
                    contentDescription = "Logout from account"
                }
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null, // Handled by IconButton
                tint = Color(0xFFB0A9A9),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun TopBarHome(
    tabs: List<String>,
    selectedTab: String,
    navController: NavController,
    onTabSelected: (String) -> Unit = {},
    notificationState: NotificationState = NotificationState(),
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo with accessibility
        Image(
            painter = painterResource(id = R.drawable.ic_cc_logo),
            contentDescription = "Creator Circle Logo",
            modifier = Modifier
                .size(36.dp)
                .semantics {
                    contentDescription = "Creator Circle application logo"
                }
        )

        // Centered tabs section
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == selectedTab

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFFB726FF), Color(0xFFFB3D91))
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    role = Role.Tab,
                                    onClickLabel = "Select $tab tab"
                                ) {
                                    // Handle navigation for specific tabs
                                    handleTabNavigation(tab, navController)
                                    onTabSelected(tab)
                                }
                                .semantics {
                                    role = Role.Tab
                                    contentDescription = "$tab tab, currently selected"
                                }
                        ) {
                            Text(
                                text = tab,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    role = Role.Tab,
                                    onClickLabel = "Select $tab tab"
                                ) {
                                    // Handle navigation for specific tabs
                                    handleTabNavigation(tab, navController)
                                    onTabSelected(tab)
                                }
                                .semantics {
                                    role = Role.Tab
                                    contentDescription = "$tab tab"
                                }
                        ) {
                            Text(
                                text = tab,
                                color = Color(0xFFB388FF),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Professional notification button with badge
        Box(
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    try {
                        onNotificationClick()
                        navController.navigate("notifications")
                    } catch (e: Exception) {
                        // Handle navigation error gracefully
                        // You might want to log this or show a snackbar
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = if (notificationState.hasUnreadNotifications) {
                            "Notifications, ${notificationState.unreadCount} unread"
                        } else {
                            "Notifications"
                        }
                    }
            ) {
                if (notificationState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFB0A9A9)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bell),
                        contentDescription = null, // Handled by IconButton
                        tint = if (notificationState.hasUnreadNotifications) {
                            Color(0xFFB726FF)
                        } else {
                            Color(0xFFB0A9A9)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Notification badge
            if (notificationState.hasUnreadNotifications && !notificationState.isLoading) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(
                            if (notificationState.unreadCount > 99) 24.dp
                            else if (notificationState.unreadCount > 9) 20.dp
                            else 16.dp
                        )
                        .background(
                            Color(0xFFFF4444),
                            CircleShape
                        )
                        .semantics {
                            contentDescription =
                                "${notificationState.unreadCount} unread notifications"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            notificationState.unreadCount > 99 -> "99+"
                            else -> notificationState.unreadCount.toString()
                        },
                        color = Color.White,
                        fontSize = when {
                            notificationState.unreadCount > 99 -> 8.sp
                            notificationState.unreadCount > 9 -> 9.sp
                            else -> 12.sp
                        },
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


// Helper function to handle tab navigation
private fun handleTabNavigation(tab: String, navController: NavController) {
    try {
        when (tab.lowercase()) {
            "resources" -> {
                navController.navigate("resourcehub") {
                    launchSingleTop = true
                    restoreState = true
                }
            }

            "feed" -> {
                navController.navigate("home") {
                    launchSingleTop = true
                    restoreState = true
                    // Pop back to home and clear any intermediate destinations
                    popUpTo("home") {
                        inclusive = false
                    }
                }
            }
            // Add other tab navigation cases as needed
            else -> {
                // Handle unknown tabs - don't navigate anywhere
                Log.d("TopBarHome", "Unknown tab clicked: $tab")
            }
        }
    } catch (e: Exception) {
        // Log the exception for debugging
        Log.e("TopBarHome", "Navigation error for tab: $tab", e)
    }
}

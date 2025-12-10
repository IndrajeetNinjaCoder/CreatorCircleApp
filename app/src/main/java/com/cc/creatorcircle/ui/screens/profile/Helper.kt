package com.cc.creatorcircle.ui.screens.profile

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.UserViewModel

@Composable
fun Account(
    navController: NavController,
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("AccountScreen", "AccountScreen")
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                FirebaseAnalyticsHelper.logDialogClosed("logout_dialog", "dismissed")
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
                        FirebaseAnalyticsHelper.logDialogClosed("logout_dialog", "cancelled")
                        showLogoutDialog = false
                    }

                    GradientButton("Log out") {
                        Log.d("LOGOUT", "Button clicked")

                        // Track logout confirmation
                        FirebaseAnalyticsHelper.logFeatureUsed("logout_confirmed")
                        FirebaseAnalyticsHelper.logEvent("user_logout_initiated")

                        showLogoutDialog = false
                        Log.d("LOGOUT", "Dialog dismissed")

                        // Simply call the ViewModel's logout method
                        // The ViewModel will handle everything: API logout, Google sign-out, and local data clearing
                        userViewModel.logout(performGoogleSignOut = true)

                        // Track logout completion
                        FirebaseAnalyticsHelper.logEvent("user_logout_completed")

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar with Back Arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_left_arrow),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(26.dp)
                        .clickable {
                            FirebaseAnalyticsHelper.logFeatureUsed("account_back_button")
                            onDismiss()
                        },
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Account",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            AccountMenuItem(
                icon = R.drawable.ic_profile_icon,
                text = "Your profile",
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("your_profile_menu_clicked")
                    FirebaseAnalyticsHelper.logEvent("account_menu_navigation", mapOf("destination" to "your_profile"))
                    navController.navigate(Screen.YourProfileScreen.route)
                }
            )

            AccountMenuItem(
                icon = R.drawable.ic_edit,
                text = "Personal Information",
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("personal_info_menu_clicked")
                    FirebaseAnalyticsHelper.logEvent("account_menu_navigation", mapOf("destination" to "personal_info"))
                    navController.navigate(Screen.PersonalInfoScreen.route)
                }
            )

            AccountMenuItem(
                icon = R.drawable.ic_profile_icon,
                text = "Accounts",
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("accounts_menu_clicked")
                    FirebaseAnalyticsHelper.logEvent("account_menu_navigation", mapOf("destination" to "accounts"))
                    navController.navigate(Screen.AccountsScreen.route)
                }
            )

            AccountMenuItem(
                icon = R.drawable.ic_security,
                text = "Password & security",
                onClick = {
                    FirebaseAnalyticsHelper.logFeatureUsed("password_security_menu_clicked")
                    FirebaseAnalyticsHelper.logEvent("account_menu_navigation", mapOf("destination" to "password_security"))
                    navController.navigate(Screen.PasswordSecurityScreen.route)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Item (Different styling)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        FirebaseAnalyticsHelper.logFeatureUsed("logout_button_clicked")
                        FirebaseAnalyticsHelper.logEvent("logout_dialog_opened")
                        showLogoutDialog = true
                    }
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_logout),
                    contentDescription = "Logout",
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFFE53935)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AccountMenuItem(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(28.dp),
            tint = Color(0xFF9E9E9E)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_right_arrow),
            contentDescription = "Navigate",
            modifier = Modifier.size(20.dp),
            tint = Color(0xFF9E9E9E)
        )
    }
}


@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        Text(label, fontSize = 13.sp, color = Color.Black)
    }
}

@Composable
fun SocialIcon(iconRes: Int, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(count, fontSize = 14.sp, color = Color.Black)
    }
}

// Helper function to format follower counts
fun formatFollowers(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fk", count / 1_000.0)
        else -> count.toString()
    }
}


// Add this to Helper.kt

data class SocialMediaCounts(
    val instagram: Int = 0,
    val youtube: Int = 0,
    val facebook: Int = 0
) {
    fun hasAnyFollowers(): Boolean = instagram > 0 || youtube > 0 || facebook > 0
}

fun parseSocialMediaFollowers(profile: com.cc.creatorcircle.data.models.UserProfile): SocialMediaCounts {
    val instagram = try {
        val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
        val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
        (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
    } catch (e: Exception) {
        Log.e("ProfileUtils", "Error parsing Instagram followers", e)
        0
    }

    val youtube = try {
        val platformFollowers = profile.platform_followers?.get("youtube") as? List<*>
        val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
        (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
    } catch (e: Exception) {
        Log.e("ProfileUtils", "Error parsing YouTube followers", e)
        0
    }

    val facebook = try {
        val platformFollowers = profile.platform_followers?.get("facebook") as? List<*>
        val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
        (firstAccount?.get("followers") as? Number)?.toInt() ?: 0
    } catch (e: Exception) {
        Log.e("ProfileUtils", "Error parsing Facebook followers", e)
        0
    }

    return SocialMediaCounts(instagram, youtube, facebook)
}

@Composable
fun ProfileBioSection(
    categories: List<String>?,
    bio: String?
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (!categories.isNullOrEmpty()) {
            Text(
                text = categories.firstOrNull() ?: "",
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
        }

        Text(
            text = bio ?: "No bio available",
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun SocialMediaRow(
    instagramFollowers: Int,
    youtubeFollowers: Int,
    facebookFollowers: Int
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (youtubeFollowers > 0) {
            SocialIcon(R.drawable.ic_youtube, formatFollowers(youtubeFollowers))
            Spacer(modifier = Modifier.width(16.dp))
        }
        if (instagramFollowers > 0) {
            SocialIcon(R.drawable.ic_instagram, formatFollowers(instagramFollowers))
            Spacer(modifier = Modifier.width(16.dp))
        }
        if (facebookFollowers > 0) {
            SocialIcon(R.drawable.ic_facebook, formatFollowers(facebookFollowers))
        }
    }
}

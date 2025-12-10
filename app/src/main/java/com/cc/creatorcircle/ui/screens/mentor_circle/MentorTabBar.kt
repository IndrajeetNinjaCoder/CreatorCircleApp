package com.cc.creatorcircle.ui.screens.mentor_circle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper


@Composable
fun MentorTabBar(
    selectedTab: Int,
    navController: NavController
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabItem(
                    title = "Guidance",
                    isSelected = selectedTab == 0,
                    onClick = {
                        if (selectedTab != 0) {
                            // Firebase: Guidance tab clicked
                            FirebaseAnalyticsHelper.logTabSelected("mentor_guidance")

                            FirebaseAnalyticsHelper.logEvent(
                                "mentor_tab_clicked",
                                mapOf(
                                    "tab_name" to "Guidance",
                                    "previous_tab" to if (selectedTab == 1) "Bookings" else "Unknown",
                                    "tab_index" to "0"
                                )
                            )

                            navController.navigate(Screen.LiveSession.route) {
                                popUpTo("mentor_circle") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                TabItem(
                    title = "Bookings",
                    isSelected = selectedTab == 1,
                    onClick = {
                        if (selectedTab != 1) {
                            // Firebase: Bookings tab clicked
                            FirebaseAnalyticsHelper.logTabSelected("mentor_bookings")

                            FirebaseAnalyticsHelper.logEvent(
                                "mentor_tab_clicked",
                                mapOf(
                                    "tab_name" to "Bookings",
                                    "previous_tab" to if (selectedTab == 0) "Guidance" else "Unknown",
                                    "tab_index" to "1"
                                )
                            )

                            navController.navigate(Screen.BookingScreen.route) {
                                popUpTo("bookings") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF9C27B0) else Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(
                    if (isSelected) Color(0xFF9C27B0) else Color.Transparent
                )
        )
    }
}
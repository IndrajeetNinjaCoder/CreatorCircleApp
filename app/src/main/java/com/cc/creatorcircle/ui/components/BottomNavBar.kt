package com.cc.creatorcircle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.screens.home.BottomNavItem
import com.cc.creatorcircle.ui.screens.home.BottomNavProfileItem

@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(R.drawable.ic_home, "Home", Color(0xFFB726FF))
        BottomNavItem(R.drawable.ic_connections, "Connections", Color.Gray)
        BottomNavItem(R.drawable.ic_sabo_ai, "Sabo AI", Color(0xFFFF6600))
        BottomNavItem(R.drawable.ic_video, "Live session", Color.Gray)

        // For profile icon in circle
        BottomNavProfileItem(R.drawable.ic_profile, "You", Color.Gray)
    }
}
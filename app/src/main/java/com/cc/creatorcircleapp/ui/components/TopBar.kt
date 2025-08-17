@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircleapp.ui.components

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
import com.cc.creatorcircleapp.R



@Composable
fun TopBar(
    selectedTab: String = "Feed",
    onTabSelected: (String) -> Unit = {}
) {
    val tabs = listOf("Feed", "Resources", "Explore")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.ic_cc_logo),
            contentDescription = "Logo",
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.width(14.dp))

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
                        .clickable { onTabSelected(tab) }
                ) {
                    Text(
                        text = tab,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
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
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            if (index != tabs.lastIndex) {
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Chat icon
        Icon(
            painter = painterResource(id = R.drawable.ic_chat),
            contentDescription = "Messages",
            tint = Color(0xFFB0A9A9),
            modifier = Modifier
                .size(24.dp)
                .clickable { }
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Notifications icon
        Icon(
            painter = painterResource(id = R.drawable.ic_bell),
            contentDescription = "Notifications",
            tint = Color(0xFFB0A9A9),
            modifier = Modifier
                .size(24.dp)
                .clickable { }
        )
    }
}

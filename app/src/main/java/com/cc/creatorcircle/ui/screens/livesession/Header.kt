//package com.cc.creatorcircle.ui.screens.livesession
//
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Menu
//import androidx.compose.material3.DrawerValue
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.Text
//import androidx.compose.material3.rememberDrawerState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import kotlinx.coroutines.launch
//
//@Composable
//fun SeekGuidanceHeader(onMenuClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            imageVector = Icons.Default.Menu,
//            contentDescription = "Menu",
//            modifier = Modifier
//                .size(24.dp)
//                .clickable { onMenuClick() },
//            tint = Color.Black
//        )
//        Spacer(modifier = Modifier.width(12.dp))
//        Text(
//            text = "Seek Guidance",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color.Black
//        )
//    }
//}
//
//@Composable
//fun SidebarContent(
//    onCloseClick: () -> Unit,
//    navController: NavController
//) {
//    Column(
//        modifier = Modifier
//            .width(280.dp)
//            .fillMaxHeight()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        // Close button
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(32.dp)
//                    .clickable { onCloseClick() },
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "X",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Menu Items
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Text(
//                text = "Mentor Circle",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        onCloseClick()
//                    }
//                    .padding(vertical = 12.dp)
//            )
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(
//                        color = Color(0xFFE91E63),
//                        shape = RoundedCornerShape(20.dp)
//                    )
//                    .clickable {
//                        onCloseClick()
//                    }
//                    .padding(vertical = 12.dp, horizontal = 16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "Seek Guidance",
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            Text(
//                text = "Bookings",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Normal,
//                color = Color.Black,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        onCloseClick()
//                    }
//                    .padding(vertical = 12.dp)
//            )
//
//            Text(
//                text = "My Session",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Normal,
//                color = Color.Black,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable {
//                        onCloseClick()
//                    }
//                    .padding(vertical = 12.dp)
//            )
//        }
//    }
//}

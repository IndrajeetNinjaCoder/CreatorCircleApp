@file:OptIn(ExperimentalMaterial3Api::class)
package com.cc.creatorcircle.ui.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.cc.creatorcircle.R
@Composable
fun ProfileScreen() {
    val profileImage =
        "https://images.unsplash.com/photo-1544005313-94ddf0286df2"
    val postImage =
        "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8)),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            // --- Top Bar ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
            }

            // --- Profile Info Row ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(profileImage),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Nisha", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProfileStat("1000", "Posts")
                        Spacer(modifier = Modifier.width(20.dp))
                        ProfileStat("235k", "Connection")
                        Spacer(modifier = Modifier.width(20.dp))
                        ProfileStat("456", "Following")
                    }
                }
            }

            // --- Bio ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Vlogger", fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Professional | Educated 10K+ users | Video Editor",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }

            // --- Social Icons ---
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialIcon(R.drawable.ic_youtube, "675k")
                Spacer(modifier = Modifier.width(16.dp))
                SocialIcon(R.drawable.ic_instagram, "877k")
                Spacer(modifier = Modifier.width(16.dp))
                SocialIcon(R.drawable.ic_facebook, "43k")
            }

            // --- Buttons ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val buttonColor = Color(0xFFB388FF) // Light purple like in image

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25), // pill-shaped corners
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = buttonColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(buttonColor)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = buttonColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Edit profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(25),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = buttonColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(buttonColor)
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = buttonColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Share profile",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }



            // --- Tabs ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Posts", color = Color(0xFF9C27B0), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Reels", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Divider(color = Color(0xFFECECEC), thickness = 1.dp)


            // --- Post Card ---
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    // Header Row
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImage),
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Hi Connections",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• 1 day ago",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Row {
                                Text(
                                    text = "You ",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "@sonu",
                                    color = Color(0xFF9C27B0), // Purple clickable style
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = " has done a great performance",
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Post Image
                    Image(
                        painter = rememberAsyncImagePainter(postImage),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Likes & Comments Count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("David warner & 76 others", fontSize = 13.sp, color = Color.Gray)
                        Text("55 Comments", fontSize = 13.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        IconWithLabel(R.drawable.ic_like, "Like", Color.Gray)
                        IconWithLabel(R.drawable.ic_comment, "Comment", Color.Gray)
                        IconWithLabel(R.drawable.ic_share, "Share", Color.Gray)
                    }
                }
            }

        }
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

@Composable
fun IconWithLabel(iconRes: Int, label: String, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 13.sp, color = tint)
    }
}

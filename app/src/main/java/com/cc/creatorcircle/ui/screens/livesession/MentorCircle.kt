package com.example.mentorcircle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.launch


data class TrendingMentor(
    val name: String,
    val followers: String, // e.g., "561"
    val price: String, // e.g., "300"
    val duration: String, // e.g., "15 Min"
    val image: String
)

data class Mentor(
    val id: Int,
    val name: String,
    val expertise: String,
    val description: String,
    val price: String,
    val availability: String,
    val rating: Float,
    val reviews: Int,
    val image: String,
    val isVerified: Boolean = false
)
@Composable
fun SeekGuidanceHeader(onMenuClick: () -> Unit) {
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
            text = "Seek Guidance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
fun SidebarContent(
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
//                    .background(
//                        color = Color(0xFFFF6B6B),
//                        shape = RoundedCornerShape(4.dp)
//                    )
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
                        // Handle navigation
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
                        // Handle navigation
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Seek Guidance",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Bookings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Handle navigation
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp)
            )

            Text(
                text = "My Session",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Handle navigation
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorCircle(navController: NavController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val trendingMentors = listOf(
        TrendingMentor(
            name = "Bharath Surya",
            followers = "561",
            price = "300",
            duration = "15 Min",
            image = ""
        ),
        TrendingMentor(
            name = "Razeen",
            followers = "1.2K",
            price = "250",
            duration = "20 Min",
            image = ""
        )
    )

    val suggestedMentors = listOf(
        Mentor(
            id = 1,
            name = "Bharath Surya",
            expertise = "@bharath",
            description = "Aspire to free like Luffy from one piece",
            price = "₹300/15 Min",
            availability = "Sep 25th - Oct 30th",
            rating = 4.8f,
            reviews = 124,
            image = "",
            isVerified = false
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                onCloseClick = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                navController = navController
            )
        }
    ) {
        Scaffold(
            topBar = { TopBar(title = "Connections", navController) },
            bottomBar = { BottomNavBar(navController = navController) },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)
            ) {
                // Content
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        // Seek Guidance Header with menu click handler
                        SeekGuidanceHeader(
                            onMenuClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        )
                    }

                    item {
                        // Search Bar
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SearchBar()
                        }
                    }

                    item {
                        // Trending Section
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                            TrendingSection(trendingMentors)
                        }
                    }

                    item {
                        // Suggestions Section
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            SuggestionsSection(suggestedMentors)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Color(0xFFF5F5F5),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Search for the influencer",
            color = Color.Gray,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filter",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Filter",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
@Composable
fun TrendingSection(mentors: List<TrendingMentor>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trending now",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = "View all",
                fontSize = 14.sp,
                color = Color(0xFF9C27B0),
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mentors) { mentor ->
                TrendingMentorCard(mentor)
            }
        }
    }
}

@Composable
fun TrendingMentorCard(mentor: TrendingMentor) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(80.dp),
        shape = RoundedCornerShape(45.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Image
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                // Profile image placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Gray.copy(alpha = 0.3f))
                ) {
                    // You can replace this with AsyncImage for actual images
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = mentor.name,
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Name
                Text(
                    text = mentor.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )

                // Instagram followers row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Instagram icon
                    Icon(
                        imageVector = Icons.Default.Person, // Replace with Instagram icon
                        contentDescription = "Instagram",
                        tint = Color(0xFFE1306C), // Instagram pink color
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${mentor.followers}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Price
                Text(
                    text = "₹ ${mentor.price}/${mentor.duration}",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SuggestionsSection(mentors: List<Mentor>) {
    Column {
        Text(
            text = "Suggestions for you",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        mentors.forEach { mentor ->
            MentorCard(mentor)
        }
    }
}

@Composable
fun MentorCard(mentor: Mentor) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image - centered
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = mentor.name,
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name and handle - centered
            Text(
                text = mentor.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Text(
                text = mentor.expertise,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Text(
                text = mentor.description,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Rating - centered
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${mentor.rating}",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${mentor.reviews}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price and Availability - centered layout
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Price:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = mentor.price,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Availability",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = mentor.availability,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons - full width
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C27B0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Book Now",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF9C27B0)
                    )
                ) {
                    Text(
                        text = "View Profile",
                        fontSize = 14.sp,
                        color = Color(0xFF9C27B0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
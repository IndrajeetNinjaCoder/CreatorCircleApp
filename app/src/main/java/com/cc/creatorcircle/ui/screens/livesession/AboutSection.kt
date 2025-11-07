package com.cc.creatorcircle.ui.screens.livesession


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.viewModel.PostsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSection(
    navController: NavController,
    userId: Int,
    influencerName: String
) {
    val context = LocalContext.current

    val postsViewModel: PostsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PostsViewModel(context) as T
            }
        }
    )

    val otherUserProfiles by postsViewModel.otherUserProfiles.collectAsState()
    val mentorProfile = otherUserProfiles[userId]
    val profileLoading by postsViewModel.profileLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var userRating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    // Fetch mentor profile by ID
    LaunchedEffect(userId) {
        postsViewModel.fetchUserProfileById(userId)
    }

    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets(0, 0, 0, 0)),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Live session",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        if (profileLoading && mentorProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(paddingValues)
            ) {
                // Tab Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {


                        GradientButton("About", modifier = Modifier.weight(1f)) {
                            selectedTab = 0
                        }

                        CustomOutlinedButton("Continue Booking", modifier = Modifier.weight(1f)) {
                            selectedTab = 1
                            navController.popBackStack()
                        }


                        // About Tab
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(44.dp)
//                                .background(
//                                    color = if (selectedTab == 0) Color(0xFF893BCF) else Color.Transparent,
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .border(
//                                    width = if (selectedTab == 0) 0.dp else 1.dp,
//                                    color = if (selectedTab == 0) Color.Transparent else Color(
//                                        0xFFE5E7EB
//                                    ),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .clickable { selectedTab = 0 },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                "About",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = if (selectedTab == 0) Color.White else Color.Black
//                            )
//                        }
//
//                        // Continue Booking Tab
//                        Box(
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(44.dp)
//                                .background(
//                                    color = if (selectedTab == 1) Color(0xFF893BCF) else Color.Transparent,
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .border(
//                                    width = if (selectedTab == 1) 0.dp else 1.dp,
//                                    color = if (selectedTab == 1) Color.Transparent else Color(
//                                        0xFFE5E7EB
//                                    ),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .clickable {
//                                    selectedTab = 1
//                                    navController.popBackStack()
//                                },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                "Continue Booking",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = if (selectedTab == 1) Color.White else Color.Black
//                            )
//                        }
                    }
                }

                // Profile Section
                item {
                    mentorProfile?.let { profile ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Profile Picture
                            AsyncImage(
                                model = profile.profile_pic ?: "",
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE5E7EB)),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.ic_profile)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = profile.full_name ?: profile.username,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Content creator",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )

                                profile.bio?.let { bio ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = bio,
                                        fontSize = 14.sp,
                                        color = Color(0xFF6B7280),
                                        lineHeight = 20.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Highlights",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // Reviews & Ratings Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Reviews & Ratings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rating Summary
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Good",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "965 ratings & 221 Reviews",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Rating Bars
                        RatingBar(5, 0.95f)
                        Spacer(modifier = Modifier.height(8.dp))
                        RatingBar(4, 0.25f)
                        Spacer(modifier = Modifier.height(8.dp))
                        RatingBar(3, 0.15f)
                        Spacer(modifier = Modifier.height(8.dp))
                        RatingBar(2, 0.08f)
                        Spacer(modifier = Modifier.height(8.dp))
                        RatingBar(1, 0.08f)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Review Items
                items(3) { index ->
                    ReviewItem(
                        name = "Ben",
                        rating = 4.5f,
                        review = "The video tutorial is clear and concise",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Add a Review Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Add a Review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Review Text Field
                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = {
                                Text(
                                    "Write your review...",
                                    color = Color(0xFF9CA3AF)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE5E7EB),
                                unfocusedBorderColor = Color(0xFFE5E7EB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Star Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Your Rating:",
                                fontSize = 14.sp,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Row {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star ${index + 1}",
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { userRating = index + 1 },
                                        tint = Color(0xFFFBBF24)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Submit Button
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF893BCF),
                                            Color(0xFFEA3BA1)
                                        )
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Handle submit review
                                }
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Submit Review",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(stars: Int, progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$stars",
            fontSize = 14.sp,
            color = Color.Black,
            modifier = Modifier.width(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Star",
            modifier = Modifier.size(16.dp),
            tint = Color(0xFFFBBF24)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE5E7EB))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(Color(0xFF893BCF))
            )
        }
    }
}

@Composable
fun ReviewItem(
    name: String,
    rating: Float,
    review: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB))
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review,
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = rating.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFBBF24)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFBBF24)
                )
            }
        }
    }
}















//package com.cc.creatorcircle.ui.screens.livesession
//
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material.icons.filled.Star
//import androidx.compose.material.icons.filled.StarBorder
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.OutlinedTextFieldDefaults
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.TopAppBarDefaults
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AboutSection(
//    navController: NavController,
//    userId: Int,
//    influencerName: String
//) {
//    var selectedTab by remember { mutableStateOf(0) }
//    var userRating by remember { mutableStateOf(0) }
//    var reviewText by remember { mutableStateOf("") }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text(
//                        "Live session",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = Color.Black
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "Back",
//                            tint = Color.Black
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Color.White
//                )
//            )
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White)
//                .padding(paddingValues)
//        ) {
//            // Tab Row
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 8.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    // About Tab
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(44.dp)
//                            .background(
//                                color = if (selectedTab == 0) Color(0xFF893BCF) else Color.Transparent,
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .border(
//                                width = if (selectedTab == 0) 0.dp else 1.dp,
//                                color = if (selectedTab == 0) Color.Transparent else Color(0xFFE5E7EB),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .clickable { selectedTab = 0 },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "About",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = if (selectedTab == 0) Color.White else Color.Black
//                        )
//                    }
//
//                    // Continue Booking Tab
//                    Box(
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(44.dp)
//                            .background(
//                                color = if (selectedTab == 1) Color(0xFF893BCF) else Color.Transparent,
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .border(
//                                width = if (selectedTab == 1) 0.dp else 1.dp,
//                                color = if (selectedTab == 1) Color.Transparent else Color(0xFFE5E7EB),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .clickable {
//                                selectedTab = 1
//                                navController.popBackStack()
//                            },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "Continue Booking",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = if (selectedTab == 1) Color.White else Color.Black
//                        )
//                    }
//                }
//            }
//
//            // Profile Section
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp, vertical = 24.dp),
//                    verticalAlignment = Alignment.Top
//                ) {
//                    // Profile Picture
//                    Box(
//                        modifier = Modifier
//                            .size(80.dp)
//                            .clip(CircleShape)
//                            .background(Color(0xFFE5E7EB))
//                    ) {
//                        // Add AsyncImage here with actual profile pic
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Profile",
//                            modifier = Modifier
//                                .size(40.dp)
//                                .align(Alignment.Center),
//                            tint = Color.Gray
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.width(16.dp))
//
//                    Column {
//                        Text(
//                            text = influencerName,
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//
//                        Spacer(modifier = Modifier.height(4.dp))
//
//                        Text(
//                            text = "Content creator",
//                            fontSize = 14.sp,
//                            color = Color.Gray
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Text(
//                            text = "Aspire to free like Luffy from one piece",
//                            fontSize = 14.sp,
//                            color = Color(0xFF6B7280),
//                            lineHeight = 20.sp
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Text(
//                            text = "Highlights",
//                            fontSize = 14.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                    }
//                }
//            }
//
//            // Reviews & Ratings Section
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                ) {
//                    Text(
//                        text = "Reviews & Ratings",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    // Rating Summary
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Good",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//
//                        Spacer(modifier = Modifier.width(8.dp))
//
//                        Row {
//                            repeat(5) { index ->
//                                Icon(
//                                    imageVector = Icons.Default.Star,
//                                    contentDescription = "Star",
//                                    modifier = Modifier.size(16.dp),
//                                    tint = Color(0xFFFBBF24)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(4.dp))
//
//                    Text(
//                        text = "965 ratings & 221 Reviews",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Rating Bars
//                    RatingBar(5, 0.95f)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    RatingBar(4, 0.25f)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    RatingBar(3, 0.15f)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    RatingBar(2, 0.08f)
//                    Spacer(modifier = Modifier.height(8.dp))
//                    RatingBar(1, 0.08f)
//
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//            }
//
//            // Review Items
//            items(3) { index ->
//                ReviewItem(
//                    name = "Ben",
//                    rating = 4.5f,
//                    review = "The video tutorial is clear and concise",
//                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
//                )
//            }
//
//            // Add a Review Section
//            item {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                ) {
//                    Text(
//                        text = "Add a Review",
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Review Text Field
//                    OutlinedTextField(
//                        value = reviewText,
//                        onValueChange = { reviewText = it },
//                        placeholder = {
//                            Text(
//                                "Write your review...",
//                                color = Color(0xFF9CA3AF)
//                            )
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(min = 120.dp),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = Color(0xFFE5E7EB),
//                            unfocusedBorderColor = Color(0xFFE5E7EB),
//                            focusedContainerColor = Color.White,
//                            unfocusedContainerColor = Color.White
//                        ),
//                        shape = RoundedCornerShape(8.dp),
//                        maxLines = 5
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Star Rating
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = "Your Rating:",
//                            fontSize = 14.sp,
//                            color = Color.Black
//                        )
//
//                        Spacer(modifier = Modifier.width(12.dp))
//
//                        Row {
//                            repeat(5) { index ->
//                                Icon(
//                                    imageVector = if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder,
//                                    contentDescription = "Star ${index + 1}",
//                                    modifier = Modifier
//                                        .size(28.dp)
//                                        .clickable { userRating = index + 1 },
//                                    tint = Color(0xFFFBBF24)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(20.dp))
//
//                    // Submit Button
//                    Box(
//                        modifier = Modifier
//                            .height(48.dp)
//                            .background(
//                                brush = Brush.horizontalGradient(
//                                    colors = listOf(
//                                        Color(0xFF893BCF),
//                                        Color(0xFFEA3BA1)
//                                    )
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                            .clickable {
//                                // Handle submit review
//                            }
//                            .padding(horizontal = 24.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "Submit Review",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White
//                        )
//                    }
//
//                    Spacer(modifier = Modifier.height(24.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun RatingBar(stars: Int, progress: Float) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "$stars",
//            fontSize = 14.sp,
//            color = Color.Black,
//            modifier = Modifier.width(20.dp)
//        )
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Icon(
//            imageVector = Icons.Default.Star,
//            contentDescription = "Star",
//            modifier = Modifier.size(16.dp),
//            tint = Color(0xFFFBBF24)
//        )
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Box(
//            modifier = Modifier
//                .weight(1f)
//                .height(8.dp)
//                .clip(RoundedCornerShape(4.dp))
//                .background(Color(0xFFE5E7EB))
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxHeight()
//                    .fillMaxWidth(progress)
//                    .background(Color(0xFF893BCF))
//            )
//        }
//    }
//}
//
//@Composable
//fun ReviewItem(
//    name: String,
//    rating: Float,
//    review: String,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .background(Color.White)
//            .padding(vertical = 8.dp)
//    ) {
//        // Avatar
//        Box(
//            modifier = Modifier
//                .size(40.dp)
//                .clip(CircleShape)
//                .background(Color(0xFFE5E7EB))
//        ) {
//            Icon(
//                imageVector = Icons.Default.Person,
//                contentDescription = "User",
//                modifier = Modifier
//                    .size(24.dp)
//                    .align(Alignment.Center),
//                tint = Color.Gray
//            )
//        }
//
//        Spacer(modifier = Modifier.width(12.dp))
//
//        Column(
//            modifier = Modifier.weight(1f)
//        ) {
//            Text(
//                text = name,
//                fontSize = 14.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Text(
//                text = review,
//                fontSize = 14.sp,
//                color = Color(0xFF6B7280),
//                lineHeight = 20.sp
//            )
//
//            Spacer(modifier = Modifier.height(4.dp))
//
//            Row(
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = rating.toString(),
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color(0xFFFBBF24)
//                )
//
//                Spacer(modifier = Modifier.width(4.dp))
//
//                Icon(
//                    imageVector = Icons.Default.Star,
//                    contentDescription = "Rating",
//                    modifier = Modifier.size(14.dp),
//                    tint = Color(0xFFFBBF24)
//                )
//            }
//        }
//    }
//}
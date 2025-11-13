package com.example.mentorcircle


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.data.models.GuidanceConfigurationState
import com.cc.creatorcircle.data.models.Influencers
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.CustomOutlinedButton
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.ui.components.GradientIconButton
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.ui.screens.livesession.MentorTabBar
import com.cc.creatorcircle.utils.UserData
import com.cc.creatorcircle.utils.UserDataManager
import com.cc.creatorcircle.viewModel.InfluencerViewModel
import com.cc.creatorcircle.viewModel.LiveSessionAvailabilityViewModel
import com.cc.creatorcircle.viewModel.MentorConfigViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                Color.White,
                RoundedCornerShape(28.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "Search",
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        BasicTextField(
            value = searchText,
            onValueChange = onSearchTextChange,
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = Color(0xFF424242),
                fontSize = 16.sp
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box {
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Search for the Influencers",
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (searchText.isNotEmpty()) {
            Icon(
                painter = painterResource(id = R.drawable.ic_cross),
                contentDescription = "Clear",
                tint = Color(0xFF9E9E9E),
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onSearchTextChange("") }
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(Color(0xFFE0E0E0))
        )

        Spacer(modifier = Modifier.width(6.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = "Filter Icon",
            modifier = Modifier
                .size(30.dp)
                .clickable { onFilterClick() }
        )
    }
}







@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorCircle(
    navController: NavController,
    viewModel: InfluencerViewModel = viewModel()
) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    var showFilterDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val influencers by viewModel.influencers.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val isEmpty by viewModel.isEmpty.observeAsState(false)

    // User profile states
    val userProfile by postsViewModel.userProfile.collectAsState()

    // Filter influencers based on search text
    val filteredInfluencers = remember(influencers, searchText) {
        if (searchText.isEmpty()) {
            influencers
        } else {
            influencers.filter { influencer ->
                influencer.fullName.contains(searchText, ignoreCase = true) ||
                        influencer.username.contains(searchText, ignoreCase = true) ||
                        (influencer.bio?.contains(searchText, ignoreCase = true) == true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSuggestedInfluencers()
    }

    // Fetch user profile when component is first created
    LaunchedEffect(Unit) {
        postsViewModel.fetchUserProfile()
    }

    Scaffold(
        topBar = {
            MentorTabBar(
                selectedTab = 0, // Guidance is active
                navController = navController
            )
        },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF9C27B0)
                    )
                }

                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "An error occurred",
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }

                isEmpty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No influencers available",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Add the MentorSection here
                        item {
                            MentorSection(
                                navController = navController,
                                userProfile = userProfile
                            )
                        }

                        item {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                SearchBar(
                                    searchText = searchText,
                                    onSearchTextChange = { searchText = it },
                                    onFilterClick = { showFilterDialog = true }
                                )
                            }
                        }

                        item {
                            val currentFilter by viewModel.currentFilter.observeAsState()

                            if (currentFilter != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .background(
                                                color = Color(0xFFFCE4EC),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable { viewModel.clearFilters() }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Filters",
                                            tint = Color(0xFFE91E63),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Clear All Filters",
                                            color = Color(0xFFE91E63),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredInfluencers.isEmpty() && searchText.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No influencers found for \"$searchText\"",
                                        color = Color.Gray,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 16.dp
                                    )
                                ) {
                                    TrendingSection(
                                        navController,
                                        postsViewModel,
                                        filteredInfluencers.take(5)
                                    )
                                }
                            }

                            item {
                                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    SuggestionsSection(
                                        navController,
                                        postsViewModel,
                                        filteredInfluencers
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                showDialog = showFilterDialog,
                onDismiss = { showFilterDialog = false },
                onApplyFilters = { filterState ->
                    val categories =
                        filterState.selectedCategories.toList().takeIf { it.isNotEmpty() }

                    val priceRanges =
                        if (filterState.priceMin > 0 || filterState.priceMax < 100000) {
                            listOf(
                                listOf(
                                    filterState.priceMin.toDouble(),
                                    filterState.priceMax.toDouble()
                                )
                            )
                        } else null

                    val followerRanges =
                        convertFollowerRangesToNumbers(filterState.selectedFollowerRanges)

                    val dateFrom = filterState.startDate
                    val dateTo = filterState.endDate

                    viewModel.applyFilters(
                        categories = categories,
                        priceRanges = priceRanges,
                        followerRanges = followerRanges,
                        dateFrom = dateFrom,
                        dateTo = dateTo
                    )
                }
            )
        }

//        if (showFilterDialog) {
//            FilterDialog(
//                showDialog = showFilterDialog,
//                onDismiss = { showFilterDialog = false },
//                onApplyFilters = { filterState ->
//                    val categories =
//                        filterState.selectedCategories.toList().takeIf { it.isNotEmpty() }
//
//                    val priceRanges =
//                        if (filterState.priceMin > 0 || filterState.priceMax < 100000) {
//                            listOf(
//                                listOf(
//                                    filterState.priceMin.toDouble(),
//                                    filterState.priceMax.toDouble()
//                                )
//                            )
//                        } else null
//
//                    val followerRanges =
//                        convertFollowerRangesToNumbers(filterState.selectedFollowerRanges)
//
//                    val (dateFrom, dateTo) = parseDateRange(filterState.selectedDate)
//
//                    viewModel.applyFilters(
//                        categories = categories,
//                        priceRanges = priceRanges,
//                        followerRanges = followerRanges,
//                        dateFrom = dateFrom,
//                        dateTo = dateTo
//                    )
//                }
//            )
//        }
    }
}






@Composable
fun MentorSection(
    navController: NavController,
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    val viewModel = remember { MentorConfigViewModel(context) }
    val configState by viewModel.configurationState.collectAsState()

    // Track expanded/collapsed state - default is collapsed
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchConfiguration()
    }

    userProfile?.let { profile ->

        val instagramFollowers = try {
            val platformFollowers = profile.platform_followers?.get("instagram") as? List<*>
            val firstAccount = platformFollowers?.firstOrNull() as? Map<*, *>
            val followers = firstAccount?.get("followers") as? Number
            Log.d("MentorSection", "Platform followers: $platformFollowers")
            Log.d("MentorSection", "First account: $firstAccount")
            Log.d("MentorSection", "Instagram followers: $followers")
            followers?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e("MentorSection", "Error parsing Instagram followers", e)
            0
        }

        // Track if mentor configuration is valid
        var showCopyLink by remember { mutableStateOf(false) }

        // Calculate price and date for display
        var priceFor15Min by remember { mutableStateOf(0.0) }
        var nearestDate by remember { mutableStateOf<String?>(null) }

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { isExpanded = !isExpanded },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Collapsed View: Profile Image + Price + Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Image
                    if (profile.profile_pic != null) {
                        AsyncImage(
                            model = profile.profile_pic,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.ic_profile),
                            error = painterResource(id = R.drawable.ic_profile)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E0E0))
                        )
                    }

                    // Price and Date info (always visible)
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        when (configState) {
                            is GuidanceConfigurationState.Success -> {
                                val config = (configState as GuidanceConfigurationState.Success).response

                                // Calculate price for 15 minutes
                                priceFor15Min = (config.serviceSlotConfig.pricePerHour / 60) * 15

                                // Get nearest availability date
                                nearestDate = if (config.selectedTimeSlots.isNotEmpty()) {
                                    try {
                                        val firstSlot = config.selectedTimeSlots.first() as? Map<*, *>
                                        val dateStr = firstSlot?.get("date") as? String

                                        // Parse and format date
                                        if (dateStr != null) {
                                            val formatter = DateTimeFormatter.ofPattern("EEE MMM dd yyyy")
                                            val date = LocalDate.parse(dateStr, formatter)
                                            val displayFormatter = DateTimeFormatter.ofPattern("MMM dd")
                                            date.format(displayFormatter)
                                        } else {
                                            null
                                        }
                                    } catch (e: Exception) {
                                        null
                                    }
                                } else {
                                    null
                                }

                                // Update showCopyLink based on whether both values are valid
                                showCopyLink = priceFor15Min > 0 && nearestDate != null

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "₹ ${String.format("%.0f", priceFor15Min)} / 15 Min",
                                        fontSize = 13.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Date",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.Gray
                                        )
                                        Text(
                                            text = nearestDate ?: "Not set",
                                            fontSize = 13.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                            is GuidanceConfigurationState.Loading -> {
                                showCopyLink = false

                                Text(
                                    text = "Loading...",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                            else -> {
                                showCopyLink = false

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "₹ -- / 15 Min",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = "Date",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.Gray
                                        )
                                        Text(
                                            text = "Not set",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Expand/Collapse indicator
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Expanded Content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Name and Stats Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = profile.full_name ?: profile.username,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )

                            // Instagram Icon with count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_instagram),
                                    contentDescription = "Instagram",
                                    modifier = Modifier.size(16.dp)
                                )

                                Text(
                                    text = "${formatFollowers(instagramFollowers)}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            // Followers Icon with count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Followers",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color(0xFFE91E63)
                                )
                                Text(
                                    text = "${profile.accepted_connections.count}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Bio
                        when (configState) {
                            is GuidanceConfigurationState.Success -> {
                                val config = (configState as GuidanceConfigurationState.Success).response

                                Text(
                                    text = config.bookingForm.customMessage.ifEmpty {
                                        profile.bio ?: "No bio available"
                                    },
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            is GuidanceConfigurationState.Loading -> {
                                Text(
                                    text = profile.bio ?: "Loading bio...",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            else -> {
                                Text(
                                    text = profile.bio ?: "No bio available",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Only show Copy Link button when configuration is valid
                            if (showCopyLink) {
                                CustomOutlinedButton("Copy Link", modifier = Modifier.weight(1f)) {
                                    val link = "http://localhost:5173/book/${profile.username}"
                                    val clip = ClipData.newPlainText("Mentor Link", link)
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT)
                                        .show()
                                }

                                Spacer(modifier = Modifier.width(4.dp))
                            }

//                            GradientButton("Provide Guidance") {
//                                navController.navigate(Screen.MySessionScreen.route)
//                            }
                            GradientIconButton("Guidance", R.drawable.ic_edit, modifier = Modifier.weight(1f)) {
                                navController.navigate(Screen.MySessionScreen.route)
                            }
                        }
                    }
                }
            }
        }
    }
}












@Composable
fun TrendingSection(
    navController: NavController,
    postsViewModel: PostsViewModel,
    influencers: List<Influencers>
) {
    var showAll by remember { mutableStateOf(false) }

    Column {
        if (!showAll) {
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
                    modifier = Modifier.clickable { showAll = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(influencers) { influencer ->
                    TrendingInfluencerCard(navController, postsViewModel, influencer)
                }
            }
        } else {
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
                    text = "Show less",
                    fontSize = 14.sp,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.clickable { showAll = false }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            influencers.forEach { influencer ->
                InfluencerCard(navController, postsViewModel, influencer)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SuggestionsSection(
    navController: NavController,
    postsViewModel: PostsViewModel,
    influencers: List<Influencers>
) {
    Column {
        Text(
            text = "Suggestions for you",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        influencers.forEach { influencer ->
            InfluencerCard(navController, postsViewModel, influencer)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Top Influencers",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
        )

        Text(
            text = "Connect with top influencers to seek advice",
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        influencers.forEach { influencer ->
            InfluencerCard(navController, postsViewModel, influencer)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@Composable
fun InfluencerCard(
    navController: NavController,
    postsViewModel: PostsViewModel,
    influencer: Influencers
) {
    val context = LocalContext.current

    // Create separate ViewModel instance for each card
    val liveSessionViewModel = remember(influencer.userId) {
        LiveSessionAvailabilityViewModel(context)
    }

    val lowestPrice = influencer.pricing.minByOrNull { it.price }
    val otherUserProfiles by postsViewModel.otherUserProfiles.collectAsState()
    val liveSessionAvailability by liveSessionViewModel.liveSessionAvailability.collectAsState()

    val connectionsCount = remember(otherUserProfiles, influencer.userId) {
        otherUserProfiles[influencer.userId]?.accepted_connections?.count ?: 0
    }

    // Fetch availability when card is displayed
    LaunchedEffect(influencer.userId) {
        postsViewModel.fetchUserProfileById(influencer.userId)
        liveSessionViewModel.fetchLiveSessionAvailability(influencer.userId)
    }

    // Get earliest and latest available dates
    val (earliestDate, latestDate) = remember(liveSessionAvailability) {
        val dates = liveSessionAvailability?.availableTimeSlots?.keys?.sorted()
        if (dates != null && dates.isNotEmpty()) {
            Pair(dates.first(), dates.last())
        } else {
            Pair(null, null)
        }
    }

    // Format date range to "Nov 20th - Nov 21st" format
    val formattedDateRange = remember(earliestDate, latestDate) {
        if (earliestDate != null && latestDate != null) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                val startDate = sdf.parse(earliestDate)
                val endDate = sdf.parse(latestDate)

                if (startDate != null && endDate != null) {
                    val startMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(startDate)
                    val endMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(endDate)

                    val startCalendar = Calendar.getInstance().apply { time = startDate }
                    val endCalendar = Calendar.getInstance().apply { time = endDate }

                    val startDay = startCalendar.get(Calendar.DAY_OF_MONTH)
                    val endDay = endCalendar.get(Calendar.DAY_OF_MONTH)

                    val startSuffix = when (startDay % 10) {
                        1 -> if (startDay == 11) "th" else "st"
                        2 -> if (startDay == 12) "th" else "nd"
                        3 -> if (startDay == 13) "th" else "rd"
                        else -> "th"
                    }

                    val endSuffix = when (endDay % 10) {
                        1 -> if (endDay == 11) "th" else "st"
                        2 -> if (endDay == 12) "th" else "nd"
                        3 -> if (endDay == 13) "th" else "rd"
                        else -> "th"
                    }

                    if (earliestDate == latestDate) {
                        "$startMonth $startDay$startSuffix"
                    } else {
                        "$startMonth $startDay$startSuffix - $endMonth $endDay$endSuffix"
                    }
                } else null
            } catch (e: Exception) {
                null
            }
        } else null
    }

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
            AsyncImage(
                model = influencer.profilePic,
                contentDescription = influencer.fullName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = android.R.drawable.ic_menu_report_image),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = influencer.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )

            Text(
                text = "@${influencer.username}",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            if (influencer.hasBio) {
                Text(
                    text = influencer.bio ?: "",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (influencer.totalSocialMediaFollowers > 0) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_instagram),
                        contentDescription = "Instagram Icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatFollowers(influencer.totalSocialMediaFollowers),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                if (connectionsCount > 0) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Connections",
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFFDB2777)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$connectionsCount",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price and Availability Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Price Section
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Price:",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                    lowestPrice?.let { pricing ->
                        Text(
                            text = "₹${pricing.price.toInt()}/${pricing.durationMinutes} Min",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                // Availability Section
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Availability",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = formattedDateRange ?: "Not Available",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GradientButton("Book Now", Modifier.weight(1f)) {
                    navController.navigate(
                        Screen.BookingSlot.createRoute(
                            influencer.userId,
                            influencerName = influencer.fullName
                        )
                    )
                }

                CustomOutlinedButton("View Profile", Modifier.weight(1f)) {
                    navController.navigate("userprofile/${influencer.userId}")
                }
            }
        }
    }
}

@Composable
fun TrendingInfluencerCard(
    navController: NavController,
    postsViewModel: PostsViewModel,
    influencer: Influencers
) {
    val lowestPrice = influencer.pricing.minByOrNull { it.price }
    val otherUserProfiles by postsViewModel.otherUserProfiles.collectAsState()

    val connectionsCount = remember(otherUserProfiles, influencer.userId) {
        otherUserProfiles[influencer.userId]?.accepted_connections?.count ?: 0
    }

    LaunchedEffect(influencer.userId) {
        postsViewModel.fetchUserProfileById(influencer.userId)
    }

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(80.dp),
        shape = RoundedCornerShape(45.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        onClick = {
            navController.navigate(
                Screen.BookingSlot.createRoute(
                    influencer.userId,
                    influencerName = influencer.fullName
                )
            )
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                AsyncImage(
                    model = influencer.profilePic,
                    contentDescription = influencer.fullName,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_menu_report_image),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = influencer.fullName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    maxLines = 1,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (influencer.totalSocialMediaFollowers > 0) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_instagram),
                            contentDescription = "Instagram Icon",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatFollowers(influencer.totalSocialMediaFollowers),
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (connectionsCount > 0) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Connections",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFDB2777)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$connectionsCount",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                lowestPrice?.let { pricing ->
                    Text(
                        text = "₹${pricing.price.toInt()}/${pricing.durationMinutes} Min",
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


private fun formatFollowers(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}





data class FilterState(
    val startDate: String? = null,
    val endDate: String? = null,
    val priceMin: Int = 0,
    val priceMax: Int = 100000,
    val selectedCategories: Set<String> = emptySet(),
    val selectedFollowerRanges: Set<String> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (FilterState) -> Unit
) {
    var filterState by remember { mutableStateOf(FilterState()) }
    var expandedSection by remember { mutableStateOf(true) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showPriceSelector by remember { mutableStateOf(false) }
    var showCategorySelector by remember { mutableStateOf(false) }
    var showFollowersSelector by remember { mutableStateOf(false) }

    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.7f)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filters",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onDismiss() },
                        tint = Color(0xFF424242)
                    )
                }

                Divider(
                    color = Color(0xFFE0E0E0),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedSection = !expandedSection }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Choose by filters",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = if (expandedSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = Color(0xFF424242)
                            )
                        }
                    }

                    // Filter Badges
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (filterState.startDate != null && filterState.endDate != null) {
                                FilterBadge(
                                    text = "${filterState.startDate} - ${filterState.endDate}",
                                    onRemove = {
                                        filterState = filterState.copy(startDate = null, endDate = null)
                                    }
                                )
                            }

                            if (filterState.priceMin > 0 || filterState.priceMax < 100000) {
                                FilterBadge(
                                    text = "₹${filterState.priceMin} - ₹${filterState.priceMax}",
                                    onRemove = {
                                        filterState = filterState.copy(priceMin = 0, priceMax = 100000)
                                    }
                                )
                            }

                            filterState.selectedCategories.forEach { category ->
                                FilterBadge(
                                    text = category,
                                    onRemove = {
                                        filterState = filterState.copy(
                                            selectedCategories = filterState.selectedCategories - category
                                        )
                                    }
                                )
                            }

                            filterState.selectedFollowerRanges.forEach { range ->
                                FilterBadge(
                                    text = range,
                                    onRemove = {
                                        filterState = filterState.copy(
                                            selectedFollowerRanges = filterState.selectedFollowerRanges - range
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (expandedSection) {
                        item { Spacer(modifier = Modifier.height(12.dp)) }

                        item {
                            FilterOptionItem(
                                text = "Select by Date",
                                isSelected = filterState.startDate != null && filterState.endDate != null,
                                onClick = { showStartDatePicker = true }
                            )
                        }

                        item {
                            FilterOptionItem(
                                text = "Select by Price",
                                isSelected = filterState.priceMin > 0 || filterState.priceMax < 100000,
                                onClick = { showPriceSelector = true }
                            )
                        }

                        item {
                            FilterOptionItem(
                                text = "Select by Category of Influencer",
                                isSelected = filterState.selectedCategories.isNotEmpty(),
                                onClick = { showCategorySelector = true }
                            )
                        }

                        item {
                            FilterOptionItem(
                                text = "Select by Followers Count",
                                isSelected = filterState.selectedFollowerRanges.isNotEmpty(),
                                onClick = { showFollowersSelector = true }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onApplyFilters(filterState)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(
                        text = "Save & Close",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (showStartDatePicker) {
                DatePickerDialog(
                    title = "Select Start Date",
                    onDismiss = { showStartDatePicker = false },
                    onDateSelected = { date ->
                        filterState = filterState.copy(startDate = date)
                        showStartDatePicker = false
                        showEndDatePicker = true
                    }
                )
            }

            if (showEndDatePicker) {
                DatePickerDialog(
                    title = "Select End Date",
                    onDismiss = { showEndDatePicker = false },
                    onDateSelected = { date ->
                        filterState = filterState.copy(endDate = date)
                        showEndDatePicker = false
                    }
                )
            }

            if (showPriceSelector) {
                PriceSelectorDialog(
                    currentMin = filterState.priceMin,
                    currentMax = filterState.priceMax,
                    onDismiss = { showPriceSelector = false },
                    onPriceSelected = { min, max ->
                        filterState = filterState.copy(priceMin = min, priceMax = max)
                        showPriceSelector = false
                    }
                )
            }

            if (showCategorySelector) {
                CategorySelectorDialog(
                    selectedCategories = filterState.selectedCategories,
                    onDismiss = { showCategorySelector = false },
                    onCategoriesSelected = { categories ->
                        filterState = filterState.copy(selectedCategories = categories)
                        showCategorySelector = false
                    }
                )
            }

            if (showFollowersSelector) {
                FollowersSelectorDialog(
                    selectedRanges = filterState.selectedFollowerRanges,
                    onDismiss = { showFollowersSelector = false },
                    onRangesSelected = { ranges ->
                        filterState = filterState.copy(selectedFollowerRanges = ranges)
                        showFollowersSelector = false
                    }
                )
            }
        }
    }
}

@Composable
fun FilterBadge(
    text: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = Color(0xFFFCE4EC),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE91E63),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFFE91E63),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() },
            tint = Color(0xFFE91E63)
        )
    }
}

@Composable
fun DatePickerDialog(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(10) }
    var selectedYear by remember { mutableStateOf(2025) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color.White, RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
                .padding(20.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE91E63), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (selectedMonth == 1) {
                                selectedMonth = 12
                                selectedYear--
                            } else {
                                selectedMonth--
                            }
                        }
                )

                Text(
                    text = "${getMonthName(selectedMonth)} $selectedYear",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (selectedMonth == 12) {
                                selectedMonth = 1
                                selectedYear++
                            } else {
                                selectedMonth++
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val daysInMonth = getDaysInMonth(selectedMonth, selectedYear)
            val firstDayOfWeek = getFirstDayOfWeek(selectedMonth, selectedYear)

            Column {
                var dayCounter = 1
                for (week in 0..5) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (day in 0..6) {
                            val currentDay = dayCounter - firstDayOfWeek
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable(
                                        enabled = currentDay > 0 && currentDay <= daysInMonth
                                    ) {
                                        if (currentDay > 0 && currentDay <= daysInMonth) {
                                            selectedDay = currentDay
                                            onDateSelected("$currentDay/${selectedMonth}/$selectedYear")
                                        }
                                    }
                                    .then(
                                        if (selectedDay == currentDay && currentDay > 0) {
                                            Modifier.background(
                                                Color(0xFFE91E63),
                                                CircleShape
                                            )
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentDay > 0 && currentDay <= daysInMonth) {
                                    Text(
                                        text = currentDay.toString(),
                                        fontSize = 14.sp,
                                        color = if (selectedDay == currentDay) Color.White else Color.Black,
                                        fontWeight = if (selectedDay == currentDay) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}






//data class FilterState(
//    val selectedDate: String? = null,
//    val priceMin: Int = 0,
//    val priceMax: Int = 100000,
//    val selectedCategories: Set<String> = emptySet(),
//    val selectedFollowerRanges: Set<String> = emptySet()
//)

@Composable
fun FilterOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                color = if (isSelected) Color(0xFFFCE4EC) else Color.White,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFE91E63) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            color = if (isSelected) Color(0xFFE91E63) else Color(0xFF424242),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
    Spacer(modifier = Modifier.height(10.dp))
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FilterDialog(
//    showDialog: Boolean,
//    onDismiss: () -> Unit,
//    onApplyFilters: (FilterState) -> Unit
//) {
//    var filterState by remember { mutableStateOf(FilterState()) }
//    var expandedSection by remember { mutableStateOf(true) }
//    var showDatePicker by remember { mutableStateOf(false) }
//    var showPriceSelector by remember { mutableStateOf(false) }
//    var showCategorySelector by remember { mutableStateOf(false) }
//    var showFollowersSelector by remember { mutableStateOf(false) }
//
//    if (showDialog) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.Black.copy(alpha = 0.5f))
//                .clickable(
//                    onClick = onDismiss,
//                    indication = null,
//                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth(0.9f)
//                    .fillMaxHeight(0.7f)
//                    .background(Color.White, RoundedCornerShape(20.dp))
//                    .clickable(
//                        onClick = {},
//                        indication = null,
//                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
//                    )
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(20.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "Filters",
//                        fontSize = 22.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "Close",
//                        modifier = Modifier
//                            .size(28.dp)
//                            .clickable { onDismiss() },
//                        tint = Color(0xFF424242)
//                    )
//                }
//
//                Divider(
//                    color = Color(0xFFE0E0E0),
//                    thickness = 1.dp,
//                    modifier = Modifier.padding(horizontal = 20.dp)
//                )
//
//                LazyColumn(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth()
//                        .padding(horizontal = 20.dp, vertical = 16.dp)
//                ) {
//                    item {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable { expandedSection = !expandedSection }
//                                .padding(vertical = 12.dp),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = "Choose by filters",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.Black
//                            )
//                            Icon(
//                                imageVector = if (expandedSection) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
//                                contentDescription = "Expand",
//                                tint = Color(0xFF424242)
//                            )
//                        }
//                    }
//
//                    if (expandedSection) {
//                        item { Spacer(modifier = Modifier.height(12.dp)) }
//
//                        item {
//                            FilterOptionItem(
//                                text = "Select by Date",
//                                isSelected = filterState.selectedDate != null,
//                                onClick = { showDatePicker = true }
//                            )
//                        }
//
//                        item {
//                            FilterOptionItem(
//                                text = "Select by Price",
//                                isSelected = filterState.priceMin > 0 || filterState.priceMax < 100000,
//                                onClick = { showPriceSelector = true }
//                            )
//                        }
//
//                        item {
//                            FilterOptionItem(
//                                text = "Select by Category of Influencer",
//                                isSelected = filterState.selectedCategories.isNotEmpty(),
//                                onClick = { showCategorySelector = true }
//                            )
//                        }
//
//                        item {
//                            FilterOptionItem(
//                                text = "Select by Followers Count",
//                                isSelected = filterState.selectedFollowerRanges.isNotEmpty(),
//                                onClick = { showFollowersSelector = true }
//                            )
//                        }
//                    }
//                }
//
//                Button(
//                    onClick = {
//                        onApplyFilters(filterState)
//                        onDismiss()
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(20.dp)
//                        .height(52.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFFE91E63)
//                    ),
//                    shape = RoundedCornerShape(26.dp)
//                ) {
//                    Text(
//                        text = "Save & Close",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//            }
//
//            if (showDatePicker) {
//                DatePickerDialog(
//                    onDismiss = { showDatePicker = false },
//                    onDateSelected = { date ->
//                        filterState = filterState.copy(selectedDate = date)
//                        showDatePicker = false
//                    }
//                )
//            }
//
//            if (showPriceSelector) {
//                PriceSelectorDialog(
//                    currentMin = filterState.priceMin,
//                    currentMax = filterState.priceMax,
//                    onDismiss = { showPriceSelector = false },
//                    onPriceSelected = { min, max ->
//                        filterState = filterState.copy(priceMin = min, priceMax = max)
//                        showPriceSelector = false
//                    }
//                )
//            }
//
//            if (showCategorySelector) {
//                CategorySelectorDialog(
//                    selectedCategories = filterState.selectedCategories,
//                    onDismiss = { showCategorySelector = false },
//                    onCategoriesSelected = { categories ->
//                        filterState = filterState.copy(selectedCategories = categories)
//                        showCategorySelector = false
//                    }
//                )
//            }
//
//            if (showFollowersSelector) {
//                FollowersSelectorDialog(
//                    selectedRanges = filterState.selectedFollowerRanges,
//                    onDismiss = { showFollowersSelector = false },
//                    onRangesSelected = { ranges ->
//                        filterState = filterState.copy(selectedFollowerRanges = ranges)
//                        showFollowersSelector = false
//                    }
//                )
//            }
//        }
//    }
//}

@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(10) }
    var selectedYear by remember { mutableStateOf(2025) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .background(Color.White, RoundedCornerShape(20.dp))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE91E63), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (selectedMonth == 1) {
                                selectedMonth = 12
                                selectedYear--
                            } else {
                                selectedMonth--
                            }
                        }
                )

                Text(
                    text = "${getMonthName(selectedMonth)} $selectedYear",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            if (selectedMonth == 12) {
                                selectedMonth = 1
                                selectedYear++
                            } else {
                                selectedMonth++
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val daysInMonth = getDaysInMonth(selectedMonth, selectedYear)
            val firstDayOfWeek = getFirstDayOfWeek(selectedMonth, selectedYear)

            Column {
                var dayCounter = 1
                for (week in 0..5) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (day in 0..6) {
                            val currentDay = dayCounter - firstDayOfWeek
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clickable(
                                        enabled = currentDay > 0 && currentDay <= daysInMonth
                                    ) {
                                        if (currentDay > 0 && currentDay <= daysInMonth) {
                                            selectedDay = currentDay
                                            onDateSelected("$currentDay/${selectedMonth}/$selectedYear")
                                        }
                                    }
                                    .then(
                                        if (selectedDay == currentDay && currentDay > 0) {
                                            Modifier.background(
                                                Color(0xFFE91E63),
                                                CircleShape
                                            )
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (currentDay > 0 && currentDay <= daysInMonth) {
                                    Text(
                                        text = currentDay.toString(),
                                        fontSize = 14.sp,
                                        color = if (selectedDay == currentDay) Color.White else Color.Black,
                                        fontWeight = if (selectedDay == currentDay) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            dayCounter++
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PriceSelectorDialog(
    currentMin: Int,
    currentMax: Int,
    onDismiss: () -> Unit,
    onPriceSelected: (Int, Int) -> Unit
) {
    var minPrice by remember { mutableStateOf(currentMin.toFloat()) }
    var maxPrice by remember { mutableStateOf(currentMax.toFloat()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select by Price",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Clear",
                    fontSize = 14.sp,
                    color = Color(0xFFE91E63),
                    modifier = Modifier.clickable {
                        minPrice = 0f
                        maxPrice = 100000f
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            androidx.compose.material3.RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    minPrice = range.start
                    maxPrice = range.endInclusive
                },
                valueRange = 0f..100000f,
                steps = 100,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFE91E63),
                    activeTrackColor = Color(0xFFE91E63),
                    inactiveTrackColor = Color(0xFFE0E0E0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹${minPrice.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Min",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "To",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Column {
                    Text(
                        text = "₹${maxPrice.toInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Max",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onPriceSelected(minPrice.toInt(), maxPrice.toInt())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Apply", fontSize = 16.sp)
            }
        }
    }
}



@Composable
fun CategorySelectorDialog(
    selectedCategories: Set<String>,
    onDismiss: () -> Unit,
    onCategoriesSelected: (Set<String>) -> Unit
) {
    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }
    var searchText by remember { mutableStateOf("") }
    var customCategories by remember { mutableStateOf<List<String>>(emptyList()) }

    val predefinedCategories = listOf(
        "Beauty",
        "Life style",
        "Technology",
        "Entertainment",
        "Fashion",
        "Food",
        "Travel",
        "Fitness",
        "Gaming",
        "Music"
    )

    val allCategories = predefinedCategories + customCategories

    val filteredCategories = if (searchText.isEmpty()) {
        allCategories
    } else {
        allCategories.filter { it.contains(searchText, ignoreCase = true) }
    }

    // Sort categories: custom selected first, then other custom, then predefined
    val sortedCategories = filteredCategories.sortedWith(compareBy(
        { !customCategories.contains(it) || !tempSelectedCategories.contains(it) },
        { !customCategories.contains(it) },
        { it }
    ))

    val showCreateOption = searchText.isNotEmpty() &&
            !allCategories.any { it.equals(searchText, ignoreCase = true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .heightIn(max = 500.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
                .padding(24.dp)
        ) {
            Text(
                text = "Select by Category of Influencer",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            if (searchText.isEmpty()) {
                                Text(
                                    text = "Search",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showCreateOption) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFFCE4EC),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    customCategories = customCategories + searchText
                                    tempSelectedCategories = tempSelectedCategories + searchText
                                    searchText = ""
                                }
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create and select \"$searchText\"",
                                fontSize = 15.sp,
                                color = Color(0xFFE91E63),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                items(sortedCategories) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempSelectedCategories = if (tempSelectedCategories.contains(
                                        category
                                    )
                                ) {
                                    tempSelectedCategories - category
                                } else {
                                    tempSelectedCategories + category
                                }
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = category,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            if (customCategories.contains(category)) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Custom",
                                    fontSize = 11.sp,
                                    color = Color(0xFFE91E63),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .background(
                                            color = Color(0xFFFCE4EC),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Checkbox(
                            checked = tempSelectedCategories.contains(category),
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFE91E63),
                                uncheckedColor = Color.Gray
                            )
                        )
                    }
                }

                if (filteredCategories.isEmpty() && !showCreateOption && searchText.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No categories found",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onCategoriesSelected(tempSelectedCategories)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Apply", fontSize = 16.sp)
            }
        }
    }
}





//@Composable
//fun CategorySelectorDialog(
//    selectedCategories: Set<String>,
//    onDismiss: () -> Unit,
//    onCategoriesSelected: (Set<String>) -> Unit
//) {
//    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }
//    var searchText by remember { mutableStateOf("") }
//
//    val categories = listOf(
//        "Beauty",
//        "Life style",
//        "Technology",
//        "Entertainment",
//        "Fashion",
//        "Food",
//        "Travel",
//        "Fitness",
//        "Gaming",
//        "Music"
//    )
//
//    val filteredCategories = if (searchText.isEmpty()) {
//        categories
//    } else {
//        categories.filter { it.contains(searchText, ignoreCase = true) }
//    }
//
//    val showCreateOption = searchText.isNotEmpty() &&
//            filteredCategories.isEmpty() &&
//            !categories.any { it.equals(searchText, ignoreCase = true) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .clickable(
//                onClick = onDismiss,
//                indication = null,
//                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            modifier = Modifier
//                .width(360.dp)
//                .heightIn(max = 500.dp)
//                .background(Color.White, RoundedCornerShape(16.dp))
//                .clickable(
//                    onClick = {},
//                    indication = null,
//                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
//                )
//                .padding(24.dp)
//        ) {
//            Text(
//                text = "Select by Category of Influencer",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            BasicTextField(
//                value = searchText,
//                onValueChange = { searchText = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
//                    .padding(12.dp),
//                textStyle = androidx.compose.ui.text.TextStyle(
//                    fontSize = 14.sp,
//                    color = Color.Black
//                ),
//                decorationBox = { innerTextField ->
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.Search,
//                            contentDescription = "Search",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Box {
//                            if (searchText.isEmpty()) {
//                                Text(
//                                    text = "Search",
//                                    color = Color.Gray,
//                                    fontSize = 14.sp
//                                )
//                            }
//                            innerTextField()
//                        }
//                    }
//                }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//            ) {
//                if (showCreateOption) {
//                    item {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(
//                                    color = Color(0xFFFCE4EC),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .clickable {
//                                    tempSelectedCategories = tempSelectedCategories + searchText
//                                    searchText = ""
//                                }
//                                .padding(vertical = 12.dp, horizontal = 12.dp),
//                            horizontalArrangement = Arrangement.Start,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Add,
//                                contentDescription = "Create",
//                                tint = Color(0xFFE91E63),
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = "Create and select \"$searchText\"",
//                                fontSize = 15.sp,
//                                color = Color(0xFFE91E63),
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(12.dp))
//                    }
//                }
//
//                items(filteredCategories) { category ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                tempSelectedCategories = if (tempSelectedCategories.contains(
//                                        category
//                                    )
//                                ) {
//                                    tempSelectedCategories - category
//                                } else {
//                                    tempSelectedCategories + category
//                                }
//                            }
//                            .padding(vertical = 12.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = category,
//                            fontSize = 16.sp,
//                            color = Color.Black
//                        )
//                        Checkbox(
//                            checked = tempSelectedCategories.contains(category),
//                            onCheckedChange = null,
//                            colors = CheckboxDefaults.colors(
//                                checkedColor = Color(0xFFE91E63),
//                                uncheckedColor = Color.Gray
//                            )
//                        )
//                    }
//                }
//
//                if (filteredCategories.isEmpty() && !showCreateOption && searchText.isNotEmpty()) {
//                    item {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 32.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text(
//                                text = "No categories found",
//                                color = Color.Gray,
//                                fontSize = 14.sp
//                            )
//                        }
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    onCategoriesSelected(tempSelectedCategories)
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE91E63)
//                ),
//                shape = RoundedCornerShape(24.dp)
//            ) {
//                Text("Apply", fontSize = 16.sp)
//            }
//        }
//    }
//}




//@Composable
//fun CategorySelectorDialog(
//    selectedCategories: Set<String>,
//    onDismiss: () -> Unit,
//    onCategoriesSelected: (Set<String>) -> Unit
//) {
//    var tempSelectedCategories by remember { mutableStateOf(selectedCategories) }
//    var searchText by remember { mutableStateOf("") }
//
//    val categories = listOf(
//        "Beauty",
//        "Life style",
//        "Technology",
//        "Entertainment",
//        "Fashion",
//        "Food",
//        "Travel",
//        "Fitness",
//        "Gaming",
//        "Music"
//    )
//
//    val filteredCategories = if (searchText.isEmpty()) {
//        categories
//    } else {
//        categories.filter { it.contains(searchText, ignoreCase = true) }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .clickable(
//                onClick = onDismiss,
//                indication = null,
//                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
//            ),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            modifier = Modifier
//                .width(360.dp)
//                .heightIn(max = 500.dp)
//                .background(Color.White, RoundedCornerShape(16.dp))
//                .clickable(
//                    onClick = {},
//                    indication = null,
//                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
//                )
//                .padding(24.dp)
//        ) {
//            Text(
//                text = "Select by Category of Influencer",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            BasicTextField(
//                value = searchText,
//                onValueChange = { searchText = it },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
//                    .padding(12.dp),
//                textStyle = androidx.compose.ui.text.TextStyle(
//                    fontSize = 14.sp,
//                    color = Color.Black
//                ),
//                decorationBox = { innerTextField ->
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            imageVector = Icons.Default.Search,
//                            contentDescription = "Search",
//                            tint = Color.Gray,
//                            modifier = Modifier.size(20.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Box {
//                            if (searchText.isEmpty()) {
//                                Text(
//                                    text = "Search",
//                                    color = Color.Gray,
//                                    fontSize = 14.sp
//                                )
//                            }
//                            innerTextField()
//                        }
//                    }
//                }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//            ) {
//                items(filteredCategories) { category ->
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .clickable {
//                                tempSelectedCategories = if (tempSelectedCategories.contains(
//                                        category
//                                    )
//                                ) {
//                                    tempSelectedCategories - category
//                                } else {
//                                    tempSelectedCategories + category
//                                }
//                            }
//                            .padding(vertical = 12.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = category,
//                            fontSize = 16.sp,
//                            color = Color.Black
//                        )
//                        Checkbox(
//                            checked = tempSelectedCategories.contains(category),
//                            onCheckedChange = null,
//                            colors = CheckboxDefaults.colors(
//                                checkedColor = Color(0xFFE91E63),
//                                uncheckedColor = Color.Gray
//                            )
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Button(
//                onClick = {
//                    onCategoriesSelected(tempSelectedCategories)
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE91E63)
//                ),
//                shape = RoundedCornerShape(24.dp)
//            ) {
//                Text("Apply", fontSize = 16.sp)
//            }
//        }
//    }
//}

@Composable
fun FollowersSelectorDialog(
    selectedRanges: Set<String>,
    onDismiss: () -> Unit,
    onRangesSelected: (Set<String>) -> Unit
) {
    var tempSelectedRanges by remember { mutableStateOf(selectedRanges) }

    val followerRanges = listOf(
        "<1k followers",
        "Nano Influencer - 1k to 10k followers",
        "Micro Influencer - 10k to 100k followers",
        "Mid-tier influencers - 100k to 500k followers",
        "Macro-influencers - 1M to 5M followers"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
                .padding(24.dp)
        ) {
            Text(
                text = "Select by Followers Count",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            followerRanges.forEach { range ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tempSelectedRanges = if (tempSelectedRanges.contains(range)) {
                                tempSelectedRanges - range
                            } else {
                                tempSelectedRanges + range
                            }
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = range,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Checkbox(
                        checked = tempSelectedRanges.contains(range),
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFE91E63),
                            uncheckedColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onRangesSelected(tempSelectedRanges)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Apply", fontSize = 16.sp)
            }
        }
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> ""
    }
}

private fun getDaysInMonth(month: Int, year: Int): Int {
    return when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

private fun getFirstDayOfWeek(month: Int, year: Int): Int {
    val t = intArrayOf(0, 3, 2, 5, 0, 3, 5, 1, 4, 6, 2, 4)
    var y = year
    if (month < 3) y--
    return (y + y / 4 - y / 100 + y / 400 + t[month - 1] + 1) % 7
}

private fun convertFollowerRangesToNumbers(ranges: Set<String>): List<List<Int>>? {
    if (ranges.isEmpty()) return null

    return ranges.mapNotNull { range ->
        when (range) {
            "<1k followers" -> listOf(0, 1000)
            "Nano Influencer - 1k to 10k followers" -> listOf(1000, 10000)
            "Micro Influencer - 10k to 100k followers" -> listOf(10000, 100000)
            "Mid-tier influencers - 100k to 500k followers" -> listOf(100000, 500000)
            "Macro-influencers - 1M to 5M followers" -> listOf(1000000, 5000000)
            else -> null
        }
    }
}

private fun parseDateRange(selectedDate: String?): Pair<String?, String?> {
    if (selectedDate == null) return Pair(null, null)

    try {
        val parts = selectedDate.split("/")
        if (parts.size == 3) {
            val day = parts[0].padStart(2, '0')
            val month = parts[1].padStart(2, '0')
            val year = parts[2]
            val formattedDate = "$year-$month-$day"

            return Pair(formattedDate, formattedDate)
        }
    } catch (e: Exception) {
        return Pair(null, null)
    }

    return Pair(null, null)
}




// Your composable functions go here...


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
                        navController.navigate("bookingscreen")
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
                        navController.navigate("mysession")
                        onCloseClick()
                    }
                    .padding(vertical = 12.dp)
            )
        }
    }
}

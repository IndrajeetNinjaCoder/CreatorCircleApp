package com.cc.creatorcircle.ui.screens.brandcollab

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.ui.components.TopBarSabo
import com.cc.creatorcircle.ui.screens.sabo.ChatHistoryItem
import com.cc.creatorcircle.ui.screens.sabo.InstagramProfile
import com.cc.creatorcircle.ui.screens.sabo.InstagramProfileItem
import com.cc.creatorcircle.ui.screens.sabo.ManageSocialAccountsPopup
import com.cc.creatorcircle.ui.screens.sabo.SideMenu
import com.cc.creatorcircle.viewModel.DealsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.example.creatorcircle.models.Brand
import com.example.creatorcircle.models.Deal
import com.example.creatorcircle.models.GenerateEmailResponse
import com.example.mentorcircle.FilterDialog

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.ui.platform.LocalContext



@Composable
fun BrandCollabScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel = remember { DealsViewModel(context) }

    // Initialize ChatViewModel to fetch profiles
    val chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel = viewModel {
        com.cc.creatorcircle.viewModel.ChatViewModel(context)
    }

    // Get SharedPreferences
    val sharedPreferences = remember {
        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
    }

    // Load sessionId from SharedPreferences
    var currentSessionId by remember {
        mutableStateOf<Int?>(
            sharedPreferences.getInt("chatSessionId", -1).takeIf { it != -1 }
        )
    }

    val currentProfileId by remember {
        derivedStateOf {
            sharedPreferences.getInt("chatProfileId", -1).takeIf { it != -1 }
        }
    }

    // Track the last session ID to detect changes
    var lastSessionId by remember { mutableStateOf<Int?>(currentSessionId) }
    var lastProfileId by remember { mutableStateOf<Int?>(currentProfileId) }

    var isMenuOpen by remember { mutableStateOf(false) }

    // UPDATED: Always show preferences popup on screen open
    var showPreferencesPopup by remember { mutableStateOf(true) }

    // Log for debugging
    LaunchedEffect(Unit) {
        Log.d("BrandCollabScreen", "Screen opened - showing preferences popup")
    }

    // Collect state from ViewModel
    val deals by viewModel.deals.collectAsState()
    val isLoadingDeals by viewModel.dealsLoading.collectAsState()
    val dealsError by viewModel.dealsError.collectAsState()

    // Brands state
    val brands by viewModel.brands.collectAsState()
    val isLoadingBrands by viewModel.brandsLoading.collectAsState()
    val brandsError by viewModel.brandsError.collectAsState()
    val hasMoreBrands by viewModel.hasMoreBrands.collectAsState()

    // Chat profiles state
    val chatUserProfiles by chatViewModel.userProfiles.collectAsState()
    val profileLoading by chatViewModel.profileLoading.collectAsState()
    val profileError by chatViewModel.profileError.collectAsState()

    // Observe set profile active states
    val setProfileActiveLoading by chatViewModel.setProfileActiveLoading.collectAsState()
    val activeProfileUpdated by chatViewModel.activeProfileUpdated.collectAsState()

    // Default filters state
    val defaultFiltersLoading by viewModel.defaultFiltersLoading.collectAsState()
    val defaultFiltersError by viewModel.defaultFiltersError.collectAsState()
    val defaultFiltersSaveSuccess by viewModel.defaultFiltersSaveSuccess.collectAsState()

    // UPDATED: Enhanced filter states
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSimpleFilterDialog by remember { mutableStateOf(false) }

    // Active filter states
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var selectedCountries by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedStates by remember { mutableStateOf<List<String>>(emptyList()) }

    // Temporary filter states (for Apply/Cancel functionality)
    var tempSelectedCategory by remember { mutableStateOf<String?>(null) }
    var tempSelectedStatus by remember { mutableStateOf<String?>(null) }
    var tempSelectedCountries by remember { mutableStateOf<List<String>>(emptyList()) }
    var tempSelectedStates by remember { mutableStateOf<List<String>>(emptyList()) }

    // Simple filter dialog states
    var simpleSelectedCategory by remember { mutableStateOf<String?>(null) }
    var simpleSelectedCountry by remember { mutableStateOf<String?>(null) }

    // Get available filter options from ViewModel
    val categories = viewModel.getAvailableCategories()
    val statuses = listOf("Active", "Inactive", "Pending", "Verified", "New")
    val countries = viewModel.getAvailableCountries()
    val states = mapOf(
        "United States" to listOf(
            "California",
            "Texas",
            "New York",
            "Florida",
            "Illinois",
            "Pennsylvania"
        ),
        "United Kingdom" to listOf("England", "Scotland", "Wales", "Northern Ireland"),
        "Canada" to listOf("Ontario", "Quebec", "British Columbia", "Alberta", "Manitoba"),
        "Australia" to listOf(
            "New South Wales",
            "Victoria",
            "Queensland",
            "Western Australia",
            "South Australia"
        ),
        "India" to listOf(
            "Maharashtra",
            "Karnataka",
            "Tamil Nadu",
            "Delhi",
            "Gujarat",
            "Uttar Pradesh"
        )
    )

    // Calculate selected filters count
    val selectedFiltersCount = listOfNotNull(
        selectedCategory,
        selectedStatus
    ).size + selectedCountries.size + selectedStates.size

    // Get user ID from PostsViewModel
    val postViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )
    val userProfile by postViewModel.userProfile.collectAsState()
    val userId = userProfile?.id ?: -1

    // Fetch user profile and chat profiles on first composition
    LaunchedEffect(Unit) {
        viewModel.fetchDeals()
        viewModel.fetchBrands()
        postViewModel.fetchUserProfile()
    }

    // Fetch chat profiles when user ID is available
    LaunchedEffect(userProfile) {
        userProfile?.let {
            chatViewModel.fetchChatUserProfiles(it.id)
        }
    }

    // UPDATED: Handle profile switch - Show simple filter dialog when profile ID changes
    LaunchedEffect(activeProfileUpdated) {
        activeProfileUpdated?.let { updatedProfile ->
            Log.d(
                "BrandCollabScreen",
                "Profile switched to ${updatedProfile.username} (ID: ${updatedProfile.id})"
            )

            // Reset simple filter states
            simpleSelectedCategory = null
            simpleSelectedCountry = null

            // Show simple filter dialog
            showSimpleFilterDialog = true
        }
    }

    // Handle successful profile activation
    LaunchedEffect(activeProfileUpdated) {
        activeProfileUpdated?.let { updatedProfile ->
            Log.d(
                "BrandCollabScreen",
                "Profile ${updatedProfile.username} set as active successfully"
            )

            // Save to SharedPreferences
            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("chatProfileId", updatedProfile.id)
                .apply()

            Toast.makeText(
                context,
                "Profile @${updatedProfile.username} selected",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Handle successful default filters save
    LaunchedEffect(defaultFiltersSaveSuccess) {
        if (defaultFiltersSaveSuccess) {
            Log.d("BrandCollabScreen", "Default filters saved successfully")

            // Reset the success flag
            viewModel.resetDefaultFiltersSaveSuccess()

            // Show success message is now handled in onComplete callback
        }
    }

    // Handle default filters save error
    LaunchedEffect(defaultFiltersError) {
        defaultFiltersError?.let { error ->
            Log.e("BrandCollabScreen", "Failed to save default filters: $error")
            Toast.makeText(
                context,
                "Failed to save preferences: $error",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Simple Filter Dialog - shown when profile is switched
//    if (showSimpleFilterDialog) {
    if (showPreferencesPopup) {


        Log.d("BrandCollabScreen", "Rendering PreferencesPopup")
        PreferencesPopup(
            onDismiss = {
                Log.d("BrandCollabScreen", "PreferencesPopup dismissed")
                showPreferencesPopup = false
                // REMOVED: No longer saving preferencesSet flag to allow it to show again
            },
            onComplete = { categories, locations ->
                Log.d("BrandCollabScreen", "PreferencesPopup completed")
                Log.d("BrandCollabScreen", "Categories: $categories")
                Log.d("BrandCollabScreen", "Locations: $locations")

                val profileId = currentProfileId

                if (profileId != null && profileId != -1) {
                    // Save preferences via API
                    Log.d("BrandCollabScreen", "Saving preferences for profile $profileId")

                    viewModel.saveDefaultFilters(
                        profileId = profileId,
                        preferredCategories = categories,
                        preferredLocations = locations
                    )

                    // Save to SharedPreferences as backup
                    sharedPreferences.edit()
                        .putStringSet("preferredCategories", categories.toSet())
                        .putStringSet("preferredLocations", locations.toSet())
                        .apply()

                    showPreferencesPopup = false

                    Toast.makeText(
                        context,
                        "Preferences saved successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Optionally fetch brands based on preferences
                    if (categories.isNotEmpty()) {
                        viewModel.fetchBrands(category = categories.first())
                    } else if (locations.isNotEmpty()) {
                        viewModel.fetchBrands(states = locations)
                    }
                } else {
                    // No profile ID available, just save locally
                    Log.w(
                        "BrandCollabScreen",
                        "No profile ID available, saving preferences locally only"
                    )

                    sharedPreferences.edit()
                        .putStringSet("preferredCategories", categories.toSet())
                        .putStringSet("preferredLocations", locations.toSet())
                        .apply()

                    showPreferencesPopup = false

                    Toast.makeText(
                        context,
                        "Preferences saved locally. Please select a profile to sync with server.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            isLoading = defaultFiltersLoading
        )


    }






    // Inside BrandCollabScreen @Composable function
// Replace the existing MobileEnhancedFilterDialog call (around line 394) with:

    if (showFilterDialog) {
        MobileEnhancedFilterDialog(
            categories = categories,
            statuses = statuses,
            countries = countries,
            states = states,
            selectedCategory = tempSelectedCategory,
            selectedStatus = tempSelectedStatus,
            selectedCountries = tempSelectedCountries,
            selectedStates = tempSelectedStates,
            onCategorySelected = { tempSelectedCategory = it },
            onStatusSelected = { tempSelectedStatus = it },
            onCountryToggle = { country ->
                tempSelectedCountries = if (tempSelectedCountries.contains(country)) {
                    tempSelectedCountries - country
                } else {
                    tempSelectedCountries + country
                }
            },
            onStateToggle = { state ->
                tempSelectedStates = if (tempSelectedStates.contains(state)) {
                    tempSelectedStates - state
                } else {
                    tempSelectedStates + state
                }
            },
            onDismiss = {
                showFilterDialog = false
            },
            onApply = {
                // Apply the temporary filters
                selectedCategory = tempSelectedCategory
                selectedStatus = tempSelectedStatus
                selectedCountries = tempSelectedCountries
                selectedStates = tempSelectedStates

                // Fetch brands with applied filters
                viewModel.fetchBrands(
                    category = selectedCategory,
                    country = selectedCountries.firstOrNull(),
                    states = selectedStates
                )

                showFilterDialog = false

                Toast.makeText(
                    context,
                    "Filters applied and saved",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onClear = {
                // Clear all temporary filters
                tempSelectedCategory = null
                tempSelectedStatus = null
                tempSelectedCountries = emptyList()
                tempSelectedStates = emptyList()
            },
            // NEW PARAMETERS
            viewModel = viewModel,
            profileId = currentProfileId,
            onSaveFilters = { categories, locations ->
                // Save filters via same API as PreferencesPopup
                currentProfileId?.let { profileId ->
                    if (profileId != -1) {
                        viewModel.saveDefaultFilters(
                            profileId = profileId,
                            preferredCategories = categories,
                            preferredLocations = locations
                        )

                        // Also save to SharedPreferences
                        sharedPreferences.edit()
                            .putStringSet("preferredCategories", categories.toSet())
                            .putStringSet("preferredLocations", locations.toSet())
                            .apply()
                    }
                }
            }
        )
    }










    // Main Content with Box to layer popup on top
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopBarSabo(
                    activeProfile = null,
                    onClick = { isMenuOpen = true }
                )
            },
            bottomBar = { BottomNavBar(navController = navController) },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8F8F8))
            ) {
                if (isLoadingDeals && deals.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF6C63FF)
                    )
                } else if (dealsError != null && deals.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = dealsError ?: "Failed to load deals",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchDeals() },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF6C63FF)
                            )
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header Section
                        item {
                            HeaderSection(
                                activeDealsCount = deals.count { it.status == "active" }
                            )
                        }

                        // Deals Section
                        if (deals.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No deals available",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(deals) { deal ->
                                DealCard(deal = deal)
                            }
                        }

                        // Brand List Section Header - UPDATED
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            BrandListHeader(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                onFilterClick = {
                                    // Copy current filters to temp states before opening dialog
                                    tempSelectedCategory = selectedCategory
                                    tempSelectedStatus = selectedStatus
                                    tempSelectedCountries = selectedCountries
                                    tempSelectedStates = selectedStates
                                    showFilterDialog = true
                                },
                                selectedFiltersCount = selectedFiltersCount
                            )
                        }

                        // Brands List
                        val filteredBrands = if (searchQuery.isNotEmpty()) {
                            brands.filter {
                                it.brandName.contains(searchQuery, ignoreCase = true) ||
                                        it.brandCategory.contains(searchQuery, ignoreCase = true)
                            }
                        } else {
                            brands
                        }

                        if (isLoadingBrands && brands.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color(0xFF6C63FF)
                                    )
                                }
                            }
                        } else if (brandsError != null && brands.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = brandsError ?: "Failed to load brands",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { viewModel.fetchBrands() },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFF6C63FF)
                                        )
                                    ) {
                                        Text("Retry", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        } else if (filteredBrands.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (searchQuery.isNotEmpty()) "No brands found" else "No brands available",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(filteredBrands) { brand ->
                                BrandCard(
                                    brand = brand,
                                    viewModel = viewModel,
                                    profileId = currentProfileId ?: -1
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Load more indicator
                            if (hasMoreBrands && isLoadingBrands) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color(0xFF6C63FF)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }

            // Side Menu with Instagram Profiles
            if (isMenuOpen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { isMenuOpen = false }
                )

                InstaSideMenu(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .background(Color.White),
                    onClose = { isMenuOpen = false },
                    chatUserProfiles = chatUserProfiles,
                    isLoadingProfiles = profileLoading,
                    profileError = profileError,
                    onRefreshProfiles = {
                        userProfile?.let {
                            chatViewModel.fetchChatUserProfiles(it.id)
                        }
                    },
                    chatViewModel = chatViewModel,
                    userId = userId
                )
            }
        }

        // UPDATED: Preferences Popup - ALWAYS SHOW ON SCREEN OPEN
        if (showPreferencesPopup) {
            Log.d("BrandCollabScreen", "Rendering PreferencesPopup")
            PreferencesPopup(
                onDismiss = {
                    Log.d("BrandCollabScreen", "PreferencesPopup dismissed")
                    showPreferencesPopup = false
                    // REMOVED: No longer saving preferencesSet flag to allow it to show again
                },
                onComplete = { categories, locations ->
                    Log.d("BrandCollabScreen", "PreferencesPopup completed")
                    Log.d("BrandCollabScreen", "Categories: $categories")
                    Log.d("BrandCollabScreen", "Locations: $locations")

                    val profileId = currentProfileId

                    if (profileId != null && profileId != -1) {
                        // Save preferences via API
                        Log.d("BrandCollabScreen", "Saving preferences for profile $profileId")

                        viewModel.saveDefaultFilters(
                            profileId = profileId,
                            preferredCategories = categories,
                            preferredLocations = locations
                        )

                        // Save to SharedPreferences as backup
                        sharedPreferences.edit()
                            .putStringSet("preferredCategories", categories.toSet())
                            .putStringSet("preferredLocations", locations.toSet())
                            .apply()

                        showPreferencesPopup = false

                        Toast.makeText(
                            context,
                            "Preferences saved successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Optionally fetch brands based on preferences
                        if (categories.isNotEmpty()) {
                            viewModel.fetchBrands(category = categories.first())
                        } else if (locations.isNotEmpty()) {
                            viewModel.fetchBrands(states = locations)
                        }
                    } else {
                        // No profile ID available, just save locally
                        Log.w(
                            "BrandCollabScreen",
                            "No profile ID available, saving preferences locally only"
                        )

                        sharedPreferences.edit()
                            .putStringSet("preferredCategories", categories.toSet())
                            .putStringSet("preferredLocations", locations.toSet())
                            .apply()

                        showPreferencesPopup = false

                        Toast.makeText(
                            context,
                            "Preferences saved locally. Please select a profile to sync with server.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                isLoading = defaultFiltersLoading
            )
        }
    }
}


// Updated PreferencesPopup with loading state
@Composable
fun PreferencesPopup(
    onDismiss: () -> Unit,
    onComplete: (List<String>, List<String>) -> Unit,
    isLoading: Boolean = false
) {
    var currentStep by remember { mutableStateOf(0) } // 0 = Categories, 1 = Location
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCountries by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedStates by remember { mutableStateOf<List<String>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var stateSearchQuery by remember { mutableStateOf("") }
    var selectedCountryForStates by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Books", "Business & Professional",
        "Cafe / Bakery", "Education & Learning",
        "Entertainment & Music", "Finance & Investment",
        "Food", "Food & Beverage",
        "Gym", "Health & Fitness",
        "Home & Lifestyle", "Parenting & Family",
        "Pet Care & Animals", "Sports & Athletics",
        "Sustainable & Eco-Friendly", "Technology & Gaming",
        "Travel & Hospitality"
    )

    val countries = listOf(
        "Australia", "Canada", "Chile", "China", "France", "Germany", "India",
        "Italy", "Japan", "Mexico", "Spain", "United Kingdom", "United States"
    )

    // States/Cities data for each country
    val countryStates = mapOf(
        "India" to listOf(
            "Ahmedabad", "Bangalore", "Chennai", "Delhi", "Gujarat",
            "Hyderabad", "Kolkata", "Maharashtra", "Mumbai", "Punjab"
        ),
        "United States" to listOf(
            "California", "Florida", "Illinois", "New York", "Texas",
            "Washington", "Arizona", "Nevada", "Oregon", "Colorado"
        ),
        "Canada" to listOf(
            "Alberta", "British Columbia", "Manitoba", "New Brunswick",
            "Ontario", "Quebec", "Saskatchewan"
        ),
        "Australia" to listOf(
            "New South Wales", "Queensland", "South Australia",
            "Tasmania", "Victoria", "Western Australia"
        ),
        "United Kingdom" to listOf(
            "England", "Scotland", "Wales", "Northern Ireland",
            "London", "Manchester", "Birmingham"
        ),
        "China" to listOf(
            "Beijing", "Shanghai", "Guangdong", "Zhejiang",
            "Jiangsu", "Shandong", "Sichuan"
        ),
        "Germany" to listOf(
            "Bavaria", "Berlin", "Hamburg", "Hesse",
            "North Rhine-Westphalia", "Saxony"
        ),
        "France" to listOf(
            "Île-de-France", "Provence-Alpes-Côte d'Azur", "Auvergne-Rhône-Alpes",
            "Nouvelle-Aquitaine", "Occitanie", "Hauts-de-France"
        ),
        "Italy" to listOf(
            "Lazio", "Lombardy", "Campania", "Sicily",
            "Veneto", "Piedmont", "Emilia-Romagna"
        ),
        "Spain" to listOf(
            "Madrid", "Catalonia", "Andalusia", "Valencia",
            "Galicia", "Castile and León"
        ),
        "Japan" to listOf(
            "Tokyo", "Osaka", "Kyoto", "Hokkaido",
            "Fukuoka", "Nagoya", "Yokohama"
        ),
        "Mexico" to listOf(
            "Mexico City", "Jalisco", "Nuevo León", "Puebla",
            "Guanajuato", "Chihuahua", "Veracruz"
        ),
        "Chile" to listOf(
            "Santiago Metropolitan", "Valparaíso", "Biobío",
            "Araucanía", "Los Lagos", "Maule"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = false) { }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set Your Preferences",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Text(
                    text = if (currentStep == 0)
                        "Choose your preferred categories and location to get personalized brand recommendations."
                    else
                        "Select countries and states/cities where you'd like to find collaboration opportunities.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    TabItem(
                        text = "Categories",
                        isSelected = currentStep == 0,
                        onClick = { if (!isLoading) currentStep = 0 }
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    TabItem(
                        text = "Location",
                        isSelected = currentStep == 1,
                        onClick = { if (!isLoading) currentStep = 1 }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content based on step
                if (currentStep == 0) {
                    // Categories Step
                    Text(
                        text = "Choose Your Preferred Categories",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Categories Grid
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories.chunked(2)) { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryChip(
                                        text = category,
                                        isSelected = selectedCategories.contains(category),
                                        onClick = {
                                            if (!isLoading) {
                                                selectedCategories =
                                                    if (selectedCategories.contains(category)) {
                                                        selectedCategories - category
                                                    } else {
                                                        selectedCategories + category
                                                    }
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Fill empty space if odd number
                                if (rowCategories.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // Location Step
                    Text(
                        text = "Choose Your Preferred Locations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )

                    Text(
                        text = "Select countries and states/cities where you'd like to find collaboration opportunities.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Selected Locations Display
                    if (selectedStates.isNotEmpty()) {
                        Text(
                            text = "Selected Locations:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedStates.forEach { state ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFF3E5F5),
                                    modifier = Modifier.clickable(enabled = !isLoading) {
                                        selectedStates = selectedStates - state
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 6.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = state,
                                            fontSize = 12.sp,
                                            color = Color(0xFF9C27B0),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color(0xFF9C27B0),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Countries List
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Search Box for Countries
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search country...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color.White,
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                enabled = !isLoading
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                val filteredCountries = if (searchQuery.isEmpty()) {
                                    countries
                                } else {
                                    countries.filter { it.contains(searchQuery, ignoreCase = true) }
                                }

                                items(filteredCountries) { country ->
                                    CountryItem(
                                        country = country,
                                        isSelected = selectedCountryForStates == country,
                                        onClick = {
                                            if (!isLoading) {
                                                selectedCountryForStates =
                                                    if (selectedCountryForStates == country) {
                                                        null
                                                    } else {
                                                        country
                                                    }
                                                stateSearchQuery = ""
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // States/Cities List
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Search Box for States
                            OutlinedTextField(
                                value = stateSearchQuery,
                                onValueChange = { stateSearchQuery = it },
                                placeholder = { Text("Search state/city...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    backgroundColor = Color.White,
                                    focusedBorderColor = Color(0xFF9C27B0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true,
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                enabled = selectedCountryForStates != null && !isLoading
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (selectedCountryForStates != null) {
                                val states = countryStates[selectedCountryForStates] ?: emptyList()
                                val filteredStates = if (stateSearchQuery.isEmpty()) {
                                    states
                                } else {
                                    states.filter {
                                        it.contains(
                                            stateSearchQuery,
                                            ignoreCase = true
                                        )
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    items(filteredStates) { state ->
                                        StateItem(
                                            state = state,
                                            isSelected = selectedStates.contains(state),
                                            onClick = {
                                                if (!isLoading) {
                                                    selectedStates =
                                                        if (selectedStates.contains(state)) {
                                                            selectedStates - state
                                                        } else {
                                                            selectedStates + state
                                                        }
                                                }
                                            }
                                        )
                                    }
                                }

                                // Change Country button at bottom
                                if (states.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    TextButton(
                                        onClick = {
                                            selectedCountryForStates = null
                                            stateSearchQuery = ""
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFF9C27B0)
                                        ),
                                        enabled = !isLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Change Country",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Select a country to see\nstates/cities",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray),
                        enabled = !isLoading
                    ) {
                        Text("Skip for now", fontSize = 14.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStep == 1) {
                            OutlinedButton(
                                onClick = { currentStep = 0 },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                shape = RoundedCornerShape(8.dp),
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Back", fontSize = 14.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (currentStep == 0) {
                                    currentStep = 1
                                } else {
                                    onComplete(selectedCategories, selectedStates)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF9C27B0)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading && currentStep == 1) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (currentStep == 0) "Next: Location" else if (isLoading) "Saving..." else "Complete Setup",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (currentStep == 0 && !isLoading) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StateItem(
    state: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF9C27B0) else Color.Black,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF9C27B0) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF9C27B0) else Color.Gray
            )
        }
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(2.dp)
                    .background(Color(0xFF9C27B0))
            )
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFFF3E5F5) else Color(0xFFF5F5F5),
        border = if (isSelected) BorderStroke(1.dp, Color(0xFF9C27B0)) else null
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isSelected) Color(0xFF9C27B0) else Color.Black,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CountryItem(
    country: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = country,
                fontSize = 13.sp,
                color = if (isSelected) Color(0xFF9C27B0) else Color.Black,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}


@Composable
fun InstaSideMenu(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    chatUserProfiles: List<ChatUserProfile>,
    isLoadingProfiles: Boolean = false,
    profileError: String? = null,
    onRefreshProfiles: () -> Unit = {},
    chatViewModel: com.cc.creatorcircle.viewModel.ChatViewModel,
    userId: Int
) {

    val context = LocalContext.current
    var showAccountsPopup by remember { mutableStateOf(false) }

    // Observe set profile active states
    val setProfileActiveLoading by chatViewModel.setProfileActiveLoading.collectAsState()
    val setProfileActiveError by chatViewModel.setProfileActiveError.collectAsState()
    val activeProfileUpdated by chatViewModel.activeProfileUpdated.collectAsState()

    // Observe add profile states
    val addProfileLoading by chatViewModel.addProfileLoading.collectAsState()
    val addProfileError by chatViewModel.addProfileError.collectAsState()
    val profileAdded by chatViewModel.profileAdded.collectAsState()

    LaunchedEffect(activeProfileUpdated) {
        activeProfileUpdated?.let { updatedProfile ->
            Log.d("SideMenu", "Profile ${updatedProfile.username} set as active successfully")

            // SAVE TO SHAREDPREFERENCES
            context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                .edit()
                .putInt("chatProfileId", updatedProfile.id)
                .apply()

            chatViewModel.fetchChatHistory(
                userId = userId,
                platform = updatedProfile.platform,
                profileId = updatedProfile.id
            )
        }
    }

    // Handle successful profile addition
    LaunchedEffect(profileAdded) {
        profileAdded?.let {
            Log.d("SideMenu", "Profile ${it.username} added successfully")
            onRefreshProfiles()
            showAccountsPopup = false
        }
    }

    // Convert List<ChatUserProfile> to List<InstagramProfile> for compatibility
    val instagramProfiles = remember(chatUserProfiles) {
        chatUserProfiles
            .filter { it.platform.lowercase() == "instagram" }
            .map { profile ->
                InstagramProfile(
                    id = profile.id.toString(),
                    username = profile.username,
                    profileUrl = profile.platformLink ?: "",
                    isActive = profile.isActive
                )
            }
    }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instagram Accounts Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Instagram Accounts",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Instagram Accounts",
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = instagramProfiles.size.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (isLoadingProfiles || setProfileActiveLoading || addProfileLoading) {
                    Spacer(modifier = Modifier.width(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.dp,
                        color = Color(0xFF9C27B0)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFF9C27B0),
                modifier = Modifier.clickable { showAccountsPopup = true }
            ) {
                Text(
                    text = "Add",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show errors
        setProfileActiveError?.let { error ->
            Text(
                text = "Failed to set profile active: $error",
                fontSize = 10.sp,
                color = Color.Red,
                lineHeight = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        addProfileError?.let { error ->
            Text(
                text = "Failed to add profile: $error",
                fontSize = 10.sp,
                color = Color.Red,
                lineHeight = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Instagram Profiles List
        when {
            isLoadingProfiles -> {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF9C27B0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loading Instagram accounts...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            profileError != null -> {
                Column {
                    Text(
                        text = "Failed to load Instagram accounts",
                        fontSize = 12.sp,
                        color = Color.Red,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = onRefreshProfiles,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9C27B0))
                    ) {
                        Text(
                            text = "Retry",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            instagramProfiles.isNotEmpty() -> {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(instagramProfiles) { profile ->
                        InstagramProfileItem(
                            profile = profile,
                            onProfileClick = { clickedProfile ->
                                if (!clickedProfile.isActive) {
                                    Log.d(
                                        "SideMenu",
                                        "Setting profile ${clickedProfile.username} as active"
                                    )
                                    chatViewModel.setProfileActive(
                                        profileId = clickedProfile.id.toInt(),
                                        userId = userId
                                    )
                                }
                            },
                            isLoading = setProfileActiveLoading && !profile.isActive
                        )
                    }
                }
            }

            else -> {
                Text(
                    text = "No Instagram accounts linked! Click \"Add\" to connect your Instagram account",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 16.sp
                )
            }
        }
    }

    // Manage Social Accounts Popup
    if (showAccountsPopup) {
        ManageSocialAccountsPopup(
            onDismiss = {
                showAccountsPopup = false
                chatViewModel.clearAddProfileState()
            },
            onLinkAccount = { url -> },
            chatViewModel = chatViewModel,
            userId = userId
        )
    }
}


@Composable
fun HeaderSection(activeDealsCount: Int = 0) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = Color(0xFF6C63FF),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ongoing Deals",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8E5FF)
            ) {
                Text(
                    text = "$activeDealsCount available",
                    fontSize = 12.sp,
                    color = Color(0xFF6C63FF),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = "Discover and manage your brand collaboration opportunities",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun FilterDialog(
    categories: List<String>,
    countries: List<String>,
    selectedCategory: String?,
    selectedCountry: String?,
    onCategorySelected: (String?) -> Unit,
    onCountrySelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Brands",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCategorySelected(
                                        if (selectedCategory == category) null else category
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = {
                                    onCategorySelected(
                                        if (selectedCategory == category) null else category
                                    )
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6C63FF)
                                )
                            )
                            Text(
                                text = category,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Country",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                ) {
                    items(countries) { country ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCountrySelected(
                                        if (selectedCountry == country) null else country
                                    )
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCountry == country,
                                onClick = {
                                    onCountrySelected(
                                        if (selectedCountry == country) null else country
                                    )
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF6C63FF)
                                )
                            )
                            Text(
                                text = country,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6C63FF)
                    )
                ) {
                    Text("Apply", color = Color.White)
                }
            }
        }
    )
}








@Composable
fun BrandCard(
    brand: Brand,
    viewModel: DealsViewModel,
    profileId: Int
) {
    val context = LocalContext.current
    var showEmail by remember { mutableStateOf(false) }
    var showCollaborateDialog by remember { mutableStateOf(false) }

    // Observe email generation states
    val emailGenerationLoading by viewModel.emailGenerationLoading.collectAsState()
    val emailGenerationSuccess by viewModel.emailGenerationSuccess.collectAsState()
    val emailGenerationError by viewModel.emailGenerationError.collectAsState()
    val emailGenerationResponse by viewModel.emailGenerationResponse.collectAsState()

    // Handle email generation success
    LaunchedEffect(emailGenerationSuccess) {
        if (emailGenerationSuccess && emailGenerationResponse != null) {
            // Dialog will show the generated email automatically
        }
    }

    // Handle email generation error
    LaunchedEffect(emailGenerationError) {
        emailGenerationError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 1.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFFF5F5F5)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = brand.brandName.firstOrNull()?.toString() ?: "?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6C63FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = brand.brandName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = brand.brandCategory,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${brand.brandState}, ${brand.brandCountry}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        brand.brandWebsite?.let { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Website",
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { showEmail = !showEmail },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.White,
                        contentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Contact",
                        fontSize = 12.sp
                    )
                }
            }

            // Email display section
            AnimatedVisibility(
                visible = showEmail,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable {
                            brand.brandEmail?.let { email ->
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$email")
                                }
                                context.startActivity(intent)
                            }
                        },
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mail,
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = brand.brandEmail ?: "No email available",
                            fontSize = 12.sp,
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Button(
                onClick = {
                    showCollaborateDialog = true
                    viewModel.generateEmail(
                        brandId = brand.brandId,
                        profileId = profileId
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF9C27B0)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mail,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Collaborate Now",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }

    // Collaborate Dialog
    if (showCollaborateDialog) {
        CollaborateDialog(
            brand = brand,
            isLoading = emailGenerationLoading,
            emailResponse = emailGenerationResponse,
            onDismiss = {
                showCollaborateDialog = false
                viewModel.clearEmailGeneration()
            },
            onRegenerate = {
                viewModel.generateEmail(
                    brandId = brand.brandId,
                    profileId = profileId
                )
            },
            onSend = { subject, body ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${brand.brandEmail}")
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(intent)
                showCollaborateDialog = false
                viewModel.clearEmailGeneration()
            }
        )
    }
}



@Composable
fun CollaborateDialog(
    brand: Brand,
    isLoading: Boolean,
    emailResponse: GenerateEmailResponse?,
    onDismiss: () -> Unit,
    onRegenerate: () -> Unit,
    onSend: (String, String) -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Collaborate with ${brand.brandName}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Edit your message and choose an action",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    // Loading State
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF9C27B0),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Generating email...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else if (emailResponse != null) {
                    // Email Content with scrolling
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // To Field
                        EmailField(
                            label = "To:",
                            value = brand.brandEmail ?: "",
                            readOnly = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subject Field - NOW COPYABLE
                        EmailField(
                            label = "Subject:",
                            value = emailResponse.subject,
                            readOnly = false
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Body Field
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Message:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Gray
                                    )
                                    TextButton(onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("email_body", emailResponse.body)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Text(
                                            text = "Copy",
                                            fontSize = 12.sp,
                                            color = Color(0xFF9C27B0)
                                        )
                                    }
                                }
                                Text(
                                    text = emailResponse.body,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Action Buttons (shown for both loading and success states)
                if (emailResponse != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onRegenerate,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    backgroundColor = Color.White,
                                    contentColor = Color(0xFF9C27B0)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF9C27B0)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Regenerate",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    sendEmail(
                                        context = context,
                                        recipient = brand.brandEmail ?: "",
                                        subject = emailResponse.subject,
                                        body = emailResponse.body
                                    )
                                    onSend(emailResponse.subject, emailResponse.body)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF9C27B0)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Send",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmailField(
    label: String,
    value: String,
    readOnly: Boolean = false
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
            if (!readOnly) {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("email_field", value)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }) {
                    Text(
                        text = "Copy",
                        fontSize = 12.sp,
                        color = Color(0xFF9C27B0)
                    )
                }
            }
        }
    }
}

// Email sending function
fun sendEmail(
    context: Context,
    recipient: String,
    subject: String,
    body: String
) {
    val uriText = "mailto:$recipient" +
            "?subject=${Uri.encode(subject)}" +
            "&body=${Uri.encode(body)}"

    val uri = Uri.parse(uriText)
    val intent = Intent(Intent.ACTION_SENDTO, uri)

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}







//
//
//@Composable
//fun CollaborateDialog(
//    brand: Brand,
//    isLoading: Boolean,
//    emailResponse: GenerateEmailResponse?,
//    onDismiss: () -> Unit,
//    onRegenerate: () -> Unit,
//    onSend: (String, String) -> Unit
//) {
//    val context = LocalContext.current
//
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth(0.98f)
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp),
//            color = Color.White
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)
//            ) {
//                // Header
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            text = "Collaborate with ${brand.brandName}",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                        Text(
//                            text = "Edit your message and choose an action",
//                            fontSize = 12.sp,
//                            color = Color.Gray,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
//                    IconButton(onClick = onDismiss) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = Color.Gray
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                if (isLoading) {
//                    // Loading State
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(300.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            CircularProgressIndicator(
//                                color = Color(0xFF9C27B0),
//                                modifier = Modifier.size(48.dp)
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text(
//                                text = "Generating email...",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                } else if (emailResponse != null) {
//                    // Email Content with scrolling
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(max = 400.dp)
//                            .verticalScroll(rememberScrollState())
//                    ) {
//                        // To Field
//                        EmailField(
//                            label = "To:",
//                            value = brand.brandEmail ?: "",
//                            readOnly = true
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Subject Field
//                        EmailField(
//                            label = "Subject:",
//                            value = emailResponse.subject,
//                            readOnly = true
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Body Field
//                        Surface(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(8.dp),
//                            color = Color(0xFFF5F5F5)
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(12.dp)
//                            ) {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Message:",
//                                        fontSize = 12.sp,
//                                        fontWeight = FontWeight.Medium,
//                                        color = Color.Gray
//                                    )
//                                    TextButton(onClick = {
//                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                                        val clip = ClipData.newPlainText("email_body", emailResponse.body)
//                                        clipboard.setPrimaryClip(clip)
//                                        Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
//                                    }) {
//                                        Text(
//                                            text = "Copy",
//                                            fontSize = 12.sp,
//                                            color = Color(0xFF9C27B0)
//                                        )
//                                    }
//                                }
//                                Text(
//                                    text = emailResponse.body,
//                                    fontSize = 13.sp,
//                                    color = Color.DarkGray,
//                                    lineHeight = 20.sp,
//                                    modifier = Modifier.padding(top = 8.dp)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(20.dp))
//                }
//
//                // Action Buttons (shown for both loading and success states)
//                if (emailResponse != null) {
//                    Surface(
//                        modifier = Modifier.fillMaxWidth(),
//                        shape = RoundedCornerShape(12.dp),
//                        color = Color.Transparent
//                    ) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            OutlinedButton(
//                                onClick = onRegenerate,
//                                modifier = Modifier.weight(1f),
//                                colors = ButtonDefaults.outlinedButtonColors(
//                                    backgroundColor = Color.White,
//                                    contentColor = Color(0xFF9C27B0)
//                                ),
//                                border = BorderStroke(1.dp, Color(0xFF9C27B0)),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Refresh,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Regenerate",
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//
//                            Button(
//                                onClick = {
//                                    sendEmail(
//                                        context = context,
//                                        recipient = brand.brandEmail ?: "",
//                                        subject = emailResponse.subject,
//                                        body = emailResponse.body
//                                    )
//                                    onSend(emailResponse.subject, emailResponse.body)
//                                },
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(42.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    backgroundColor = Color(0xFF9C27B0)
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Send,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp),
//                                    tint = Color.White
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Send",
//                                    color = Color.White,
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EmailField(
//    label: String,
//    value: String,
//    readOnly: Boolean = false
//) {
//    val context = LocalContext.current
//
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(8.dp),
//        color = Color(0xFFF5F5F5)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                modifier = Modifier.weight(1f),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = label,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = value,
//                    fontSize = 13.sp,
//                    color = Color.DarkGray
//                )
//            }
//            if (!readOnly) {
//                TextButton(onClick = {
//                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                    val clip = ClipData.newPlainText("email_field", value)
//                    clipboard.setPrimaryClip(clip)
//                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
//                }) {
//                    Text(
//                        text = "Copy",
//                        fontSize = 12.sp,
//                        color = Color(0xFF9C27B0)
//                    )
//                }
//            }
//        }
//    }
//}
//
//// Email sending function
//fun sendEmail(
//    context: Context,
//    recipient: String,
//    subject: String,
//    body: String
//) {
//    val intent = Intent(Intent.ACTION_SENDTO).apply {
//        data = Uri.parse("mailto:")
//        putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
//        putExtra(Intent.EXTRA_SUBJECT, subject)
//        putExtra(Intent.EXTRA_TEXT, body)
//    }
//
//    try {
//        context.startActivity(Intent.createChooser(intent, "Send email via..."))
//    } catch (e: Exception) {
//        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
//    }
//}





//@Composable
//fun CollaborateDialog(
//    brand: Brand,
//    isLoading: Boolean,
//    emailResponse: GenerateEmailResponse?,
//    onDismiss: () -> Unit,
//    onRegenerate: () -> Unit,
//    onSend: (String, String) -> Unit
//) {
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth(0.98f)
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp),
//            color = Color.White
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)
//            ) {
//                // Header
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            text = "Collaborate with ${brand.brandName}",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                        Text(
//                            text = "Edit your message and choose an action",
//                            fontSize = 12.sp,
//                            color = Color.Gray,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
//                    IconButton(onClick = onDismiss) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = Color.Gray
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                if (isLoading) {
//                    // Loading State
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(300.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            CircularProgressIndicator(
//                                color = Color(0xFF9C27B0),
//                                modifier = Modifier.size(48.dp)
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text(
//                                text = "Generating email...",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                } else if (emailResponse != null) {
//                    // Email Content with scrolling
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .heightIn(max = 400.dp)
//                            .verticalScroll(rememberScrollState())
//                    ) {
//                        // To Field
//                        EmailField(
//                            label = "To:",
//                            value = brand.brandEmail ?: "",
//                            readOnly = true
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Subject Field
//                        EmailField(
//                            label = "Subject:",
//                            value = emailResponse.subject,
//                            readOnly = true
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Body Field
//                        Surface(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(8.dp),
//                            color = Color(0xFFF5F5F5)
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(12.dp)
//                            ) {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Message:",
//                                        fontSize = 12.sp,
//                                        fontWeight = FontWeight.Medium,
//                                        color = Color.Gray
//                                    )
//                                    TextButton(onClick = { /* Copy functionality */ }) {
//                                        Text(
//                                            text = "Copy",
//                                            fontSize = 12.sp,
//                                            color = Color(0xFF9C27B0)
//                                        )
//                                    }
//                                }
//                                Text(
//                                    text = emailResponse.body,
//                                    fontSize = 13.sp,
//                                    color = Color.DarkGray,
//                                    lineHeight = 20.sp,
//                                    modifier = Modifier.padding(top = 8.dp)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(20.dp))
//                }
//
//                // Action Buttons (shown for both loading and success states)
//                if (emailResponse != null) {
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxWidth(),
////                            .border(
////                                width = 2.dp,
////                                color = Color(0xFFE74C3C),
////                                shape = RoundedCornerShape(12.dp)
////                            ),
//                        shape = RoundedCornerShape(12.dp),
//                        color = Color.Transparent
//                    ) {
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth(),
//                            horizontalArrangement = Arrangement.spacedBy(12.dp)
//                        ) {
//                            OutlinedButton(
//                                onClick = onRegenerate,
//                                modifier = Modifier.weight(1f),
//                                colors = ButtonDefaults.outlinedButtonColors(
//                                    backgroundColor = Color.White,
//                                    contentColor = Color(0xFF9C27B0)
//                                ),
//                                border = BorderStroke(1.dp, Color(0xFF9C27B0)),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Refresh,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp)
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Regenerate",
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//
//                            Button(
//                                onClick = {
//                                    onSend(emailResponse.subject, emailResponse.body)
//                                },
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .height(42.dp),
//                                colors = ButtonDefaults.buttonColors(
//                                    backgroundColor = Color(0xFF9C27B0)
//                                ),
//                                shape = RoundedCornerShape(8.dp)
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Send,
//                                    contentDescription = null,
//                                    modifier = Modifier.size(18.dp),
//                                    tint = Color.White
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Send",
//                                    color = Color.White,
//                                    fontSize = 12.sp,
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EmailField(
//    label: String,
//    value: String,
//    readOnly: Boolean = false
//) {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(8.dp),
//        color = Color(0xFFF5F5F5)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                modifier = Modifier.weight(1f),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = label,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = value,
//                    fontSize = 13.sp,
//                    color = Color.DarkGray
//                )
//            }
//            if (!readOnly) {
//                TextButton(onClick = { /* Copy functionality */ }) {
//                    Text(
//                        text = "Copy",
//                        fontSize = 12.sp,
//                        color = Color(0xFF9C27B0)
//                    )
//                }
//            }
//        }
//    }
//}
//
//






//
//
//@Composable
//fun BrandCard(
//    brand: Brand,
//    viewModel: DealsViewModel,
//    profileId: Int
//) {
//    val context = LocalContext.current
//    var showEmail by remember { mutableStateOf(false) }
//    var showCollaborateDialog by remember { mutableStateOf(false) }
//
//    // Observe email generation states
//    val emailGenerationLoading by viewModel.emailGenerationLoading.collectAsState()
//    val emailGenerationSuccess by viewModel.emailGenerationSuccess.collectAsState()
//    val emailGenerationError by viewModel.emailGenerationError.collectAsState()
//    val emailGenerationResponse by viewModel.emailGenerationResponse.collectAsState()
//
//    // Handle email generation success
//    LaunchedEffect(emailGenerationSuccess) {
//        if (emailGenerationSuccess && emailGenerationResponse != null) {
//            // Dialog will show the generated email automatically
//        }
//    }
//
//    // Handle email generation error
//    LaunchedEffect(emailGenerationError) {
//        emailGenerationError?.let { error ->
//            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        shape = RoundedCornerShape(12.dp),
//        elevation = 1.dp,
//        backgroundColor = Color.White
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.Top
//            ) {
//                Surface(
//                    modifier = Modifier.size(48.dp),
//                    shape = CircleShape,
//                    color = Color(0xFFF5F5F5)
//                ) {
//                    Box(
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = brand.brandName.firstOrNull()?.toString() ?: "?",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF6C63FF)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(
//                        text = brand.brandName,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Text(
//                        text = brand.brandCategory,
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(top = 4.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.LocationOn,
//                            contentDescription = null,
//                            tint = Color.Gray,
//                            modifier = Modifier.size(12.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "${brand.brandState}, ${brand.brandCountry}",
//                            fontSize = 11.sp,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 12.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                OutlinedButton(
//                    onClick = {
//                        brand.brandWebsite?.let { url ->
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                            context.startActivity(intent)
//                        }
//                    },
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        backgroundColor = Color.White,
//                        contentColor = Color.Gray
//                    ),
//                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
//                    shape = RoundedCornerShape(6.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Language,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp),
//                        tint = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Website",
//                        fontSize = 12.sp
//                    )
//                }
//
//                OutlinedButton(
//                    onClick = { showEmail = !showEmail },
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        backgroundColor = Color.White,
//                        contentColor = Color.Gray
//                    ),
//                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
//                    shape = RoundedCornerShape(6.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Mail,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp),
//                        tint = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Contact",
//                        fontSize = 12.sp
//                    )
//                }
//            }
//
//            // Email display section
//            AnimatedVisibility(
//                visible = showEmail,
//                enter = expandVertically() + fadeIn(),
//                exit = shrinkVertically() + fadeOut()
//            ) {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp)
//                        .clickable {
//                            brand.brandEmail?.let { email ->
//                                val intent = Intent(Intent.ACTION_SENDTO).apply {
//                                    data = Uri.parse("mailto:$email")
//                                }
//                                context.startActivity(intent)
//                            }
//                        },
//                    shape = RoundedCornerShape(6.dp),
//                    color = Color.White
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Mail,
//                            contentDescription = null,
//                            tint = Color(0xFF2196F3),
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = brand.brandEmail ?: "No email available",
//                            fontSize = 12.sp,
//                            color = Color(0xFF2196F3),
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                }
//            }
//
//            Button(
//                onClick = {
//                    showCollaborateDialog = true
//                    viewModel.generateEmail(
//                        brandId = brand.brandId,
//                        profileId = profileId
//                    )
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 12.dp),
//                colors = ButtonDefaults.buttonColors(
//                    backgroundColor = Color(0xFF9C27B0)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Mail,
//                    contentDescription = null,
//                    modifier = Modifier.size(18.dp),
//                    tint = Color.White
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Collaborate Now",
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//        }
//    }
//
//    // Collaborate Dialog
//    if (showCollaborateDialog) {
//        CollaborateDialog(
//            brand = brand,
//            isLoading = emailGenerationLoading,
//            emailResponse = emailGenerationResponse,
//            onDismiss = {
//                showCollaborateDialog = false
//                viewModel.clearEmailGeneration()
//            },
//            onRegenerate = {
//                viewModel.generateEmail(
//                    brandId = brand.brandId,
//                    profileId = profileId
//                )
//            },
//            onSend = { subject, body ->
//                val intent = Intent(Intent.ACTION_SENDTO).apply {
//                    data = Uri.parse("mailto:${brand.brandEmail}")
//                    putExtra(Intent.EXTRA_SUBJECT, subject)
//                    putExtra(Intent.EXTRA_TEXT, body)
//                }
//                context.startActivity(intent)
//                showCollaborateDialog = false
//                viewModel.clearEmailGeneration()
//            }
//        )
//    }
//}
//
//@Composable
//fun CollaborateDialog(
//    brand: Brand,
//    isLoading: Boolean,
//    emailResponse: GenerateEmailResponse?,
//    onDismiss: () -> Unit,
//    onRegenerate: () -> Unit,
//    onSend: (String, String) -> Unit
//) {
//    Dialog(
//        onDismissRequest = onDismiss,
//        properties = DialogProperties(usePlatformDefaultWidth = false)
//    ) {
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            shape = RoundedCornerShape(16.dp),
//            color = Color.White
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)
//            ) {
//                // Header
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(
//                            text = "Collaborate with ${brand.brandName}",
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                        Text(
//                            text = "Edit your message and choose an action",
//                            fontSize = 12.sp,
//                            color = Color.Gray,
//                            modifier = Modifier.padding(top = 4.dp)
//                        )
//                    }
//                    IconButton(onClick = onDismiss) {
//                        Icon(
//                            imageVector = Icons.Default.Close,
//                            contentDescription = "Close",
//                            tint = Color.Gray
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                if (isLoading) {
//                    // Loading State
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(300.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            CircularProgressIndicator(
//                                color = Color(0xFF9C27B0),
//                                modifier = Modifier.size(48.dp)
//                            )
//                            Spacer(modifier = Modifier.height(16.dp))
//                            Text(
//                                text = "Generating email...",
//                                fontSize = 14.sp,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                } else if (emailResponse != null) {
//                    // Email Content
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .verticalScroll(rememberScrollState())
//                    ) {
//                        // To Field
//                        EmailField(
//                            label = "To:",
//                            value = brand.brandEmail ?: "",
//                            readOnly = true
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Subject Field
//                        EmailField(
//                            label = "Subject:",
//                            value = emailResponse.subject,
//                            readOnly = true
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        // Body Field
//                        Surface(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(8.dp),
//                            color = Color(0xFFF5F5F5)
//                        ) {
//                            Column(
//                                modifier = Modifier.padding(12.dp)
//                            ) {
//                                Row(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    horizontalArrangement = Arrangement.SpaceBetween,
//                                    verticalAlignment = Alignment.CenterVertically
//                                ) {
//                                    Text(
//                                        text = "Message:",
//                                        fontSize = 12.sp,
//                                        fontWeight = FontWeight.Medium,
//                                        color = Color.Gray
//                                    )
//                                    TextButton(onClick = { /* Copy functionality */ }) {
//                                        Text(
//                                            text = "Copy",
//                                            fontSize = 12.sp,
//                                            color = Color(0xFF9C27B0)
//                                        )
//                                    }
//                                }
//                                Text(
//                                    text = emailResponse.body,
//                                    fontSize = 13.sp,
//                                    color = Color.DarkGray,
//                                    lineHeight = 20.sp,
//                                    modifier = Modifier.padding(top = 8.dp)
//                                )
//                            }
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(20.dp))
//
//                    // Action Buttons
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        OutlinedButton(
//                            onClick = onRegenerate,
//                            modifier = Modifier.weight(1f),
//                            colors = ButtonDefaults.outlinedButtonColors(
//                                backgroundColor = Color.White,
//                                contentColor = Color(0xFF9C27B0)
//                            ),
//                            border = BorderStroke(1.dp, Color(0xFF9C27B0)),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Refresh,
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = "Regenerate",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//
//                        Button(
//                            onClick = {
//                                onSend(emailResponse.subject, emailResponse.body)
//                            },
//                            modifier = Modifier.weight(1f),
//                            colors = ButtonDefaults.buttonColors(
//                                backgroundColor = Color(0xFF9C27B0)
//                            ),
//                            shape = RoundedCornerShape(8.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.Send,
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp),
//                                tint = Color.White
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = "Send",
//                                color = Color.White,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun EmailField(
//    label: String,
//    value: String,
//    readOnly: Boolean = false
//) {
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(8.dp),
//        color = Color(0xFFF5F5F5)
//    ) {
//        Row(
//            modifier = Modifier.padding(12.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                modifier = Modifier.weight(1f),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = label,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Gray
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = value,
//                    fontSize = 13.sp,
//                    color = Color.DarkGray
//                )
//            }
//            if (!readOnly) {
//                TextButton(onClick = { /* Copy functionality */ }) {
//                    Text(
//                        text = "Copy",
//                        fontSize = 12.sp,
//                        color = Color(0xFF9C27B0)
//                    )
//                }
//            }
//        }
//    }
//}
//










//
//@Composable
//fun BrandCard(brand: Brand) {
//    val context = LocalContext.current
//    var showEmail by remember { mutableStateOf(false) }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp),
//        shape = RoundedCornerShape(12.dp),
//        elevation = 1.dp,
//        backgroundColor = Color.White
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.Top
//            ) {
//                Surface(
//                    modifier = Modifier.size(48.dp),
//                    shape = CircleShape,
//                    color = Color(0xFFF5F5F5)
//                ) {
//                    Box(
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = brand.brandName.firstOrNull()?.toString() ?: "?",
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color(0xFF6C63FF)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.width(12.dp))
//
//                Column(
//                    modifier = Modifier.weight(1f)
//                ) {
//                    Text(
//                        text = brand.brandName,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Text(
//                        text = brand.brandCategory,
//                        fontSize = 12.sp,
//                        color = Color.Gray,
//                        modifier = Modifier.padding(top = 4.dp)
//                    )
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        modifier = Modifier.padding(top = 4.dp)
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.LocationOn,
//                            contentDescription = null,
//                            tint = Color.Gray,
//                            modifier = Modifier.size(12.dp)
//                        )
//                        Spacer(modifier = Modifier.width(4.dp))
//                        Text(
//                            text = "${brand.brandState}, ${brand.brandCountry}",
//                            fontSize = 11.sp,
//                            color = Color.Gray
//                        )
//                    }
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 12.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                OutlinedButton(
//                    onClick = {
//                        brand.brandWebsite?.let { url ->
//                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//                            context.startActivity(intent)
//                        }
//                    },
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        backgroundColor = Color.White,
//                        contentColor = Color.Gray
//                    ),
//                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
//                    shape = RoundedCornerShape(6.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Language,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp),
//                        tint = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Website",
//                        fontSize = 12.sp
//                    )
//                }
//
//                OutlinedButton(
//                    onClick = { showEmail = !showEmail },
//                    modifier = Modifier.weight(1f),
//                    colors = ButtonDefaults.outlinedButtonColors(
//                        backgroundColor = Color.White,
//                        contentColor = Color.Gray
//                    ),
//                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
//                    shape = RoundedCornerShape(6.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Mail,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp),
//                        tint = Color.Gray
//                    )
//                    Spacer(modifier = Modifier.width(4.dp))
//                    Text(
//                        text = "Contact",
//                        fontSize = 12.sp
//                    )
//                }
//            }
//
//
//            // Email display section
//            AnimatedVisibility(
//                visible = showEmail,
//                enter = expandVertically() + fadeIn(),
//                exit = shrinkVertically() + fadeOut()
//            ) {
//                Surface(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp)
//                        .clickable {
//                            brand.brandEmail?.let { email ->
//                                val intent = Intent(Intent.ACTION_SENDTO).apply {
//                                    data = Uri.parse("mailto:$email")
//                                }
//                                context.startActivity(intent)
//                            }
//                        },
//                    shape = RoundedCornerShape(6.dp),
////                    border = BorderStroke(1.dp, Color(0xFFE91E63)),
//                    color = Color.White
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Mail,
//                            contentDescription = null,
//                            tint = Color(0xFF2196F3),
//                            modifier = Modifier.size(16.dp)
//                        )
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(
//                            text = brand.brandEmail ?: "No email available",
//                            fontSize = 12.sp,
//                            color = Color(0xFF2196F3),
//                            modifier = Modifier.weight(1f)
//                        )
//                    }
//                }
//            }
//
//
//
//
//
//
//            Button(
//                onClick = { /* Collaborate with brand */ },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 12.dp),
//                colors = ButtonDefaults.buttonColors(
//                    backgroundColor = Color(0xFF9C27B0)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Mail,
//                    contentDescription = null,
//                    modifier = Modifier.size(18.dp),
//                    tint = Color.White
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    text = "Collaborate Now",
//                    color = Color.White,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//        }
//    }
//}
//

@Composable
fun BrandListHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    selectedFiltersCount: Int = 0
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Explore Brands",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search brands...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    backgroundColor = Color.White,
                    focusedBorderColor = Color(0xFF6C63FF),
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onFilterClick() },
                shape = RoundedCornerShape(8.dp),
                color = if (selectedFiltersCount > 0) Color(0xFFE8E5FF) else Color.White,
                border = BorderStroke(
                    1.dp,
                    if (selectedFiltersCount > 0) Color(0xFF6C63FF) else Color(0xFFE0E0E0)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (selectedFiltersCount > 0) Color(0xFF6C63FF) else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    if (selectedFiltersCount > 0) {
                        Surface(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp),
                            shape = CircleShape,
                            color = Color(0xFFFF5252)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = selectedFiltersCount.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class FilterSection {
    MAIN, CATEGORIES, STATUS, LOCATION
}


//
@Composable
fun FilterSidebarItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFFE8E5FF) else Color.Transparent
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF9C27B0) else Color(0xFF666666),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun StatusFilterContent(
    statuses: List<String>,
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    Column {
        Text(
            text = "Brand Status",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                StatusOption(
                    text = "All Status",
                    isSelected = selectedStatus == null,
                    onClick = { onStatusSelected(null) }
                )
            }
            items(statuses) { status ->
                StatusOption(
                    text = status,
                    isSelected = selectedStatus == status,
                    onClick = { onStatusSelected(if (selectedStatus == status) null else status) }
                )
            }
        }
    }
}

@Composable
fun StatusOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF9C27B0) else Color(0xFFE0E0E0)
        ),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF9C27B0)
                )
            )
            Text(
                text = text,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun LocationFilterContent(
    countries: List<String>,
    states: Map<String, List<String>>,
    selectedCountries: List<String>,
    selectedStates: List<String>,
    searchCountryQuery: String,
    searchStateQuery: String,
    onSearchCountryChange: (String) -> Unit,
    onSearchStateChange: (String) -> Unit,
    onCountryToggle: (String) -> Unit,
    onStateToggle: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Selected locations chips
        if (selectedCountries.isNotEmpty() || selectedStates.isNotEmpty()) {
            Text(
                text = "Selected Locations (${selectedCountries.size + selectedStates.size})",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedCountries.forEach { country ->
                    LocationChip(text = country, onRemove = { onCountryToggle(country) })
                }
                selectedStates.forEach { state ->
                    LocationChip(text = state, onRemove = { onStateToggle(state) })
                }
            }
        }

        // Step indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Step 1: Select Country",
                fontSize = 13.sp,
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Step 2: Select State/City",
                fontSize = 13.sp,
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Country selection
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = searchCountryQuery,
                    onValueChange = onSearchCountryChange,
                    placeholder = { Text("Search countries...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filteredCountries = countries.filter {
                        it.contains(searchCountryQuery, ignoreCase = true)
                    }

                    items(filteredCountries) { country ->
                        val isSelected = selectedCountries.contains(country)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountryToggle(country) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFF3E5F5) else Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF9C27B0) else Color(0xFFE0E0E0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = country,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF9C27B0),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 4.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Added to filters",
                                                fontSize = 10.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { onCountryToggle(country) },
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = "Remove",
                                            fontSize = 11.sp,
                                            color = Color(0xFF9C27B0)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (selectedCountries.isNotEmpty()) {
                    TextButton(
                        onClick = { /* Change country logic */ },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "← Change Country",
                            fontSize = 12.sp,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
            }

            // State selection
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = searchStateQuery,
                    onValueChange = onSearchStateChange,
                    placeholder = {
                        Text(
                            "Search states in ${selectedCountries.firstOrNull() ?: "country"}...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    enabled = selectedCountries.isNotEmpty()
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val availableStates = selectedCountries.flatMap { country ->
                        states[country] ?: emptyList()
                    }.filter {
                        it.contains(searchStateQuery, ignoreCase = true)
                    }

                    items(availableStates) { state ->
                        val isSelected = selectedStates.contains(state)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStateToggle(state) },
                            color = if (isSelected) Color(0xFFF3E5F5) else Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onStateToggle(state) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF9C27B0)
                                    )
                                )
                                Text(
                                    text = state,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


//enum class FilterSection {
//    MAIN, CATEGORIES, STATUS, LOCATION
//}



@Composable
fun MobileEnhancedFilterDialog(
    categories: List<String>,
    statuses: List<String>,
    countries: List<String>,
    states: Map<String, List<String>>,
    selectedCategory: String?,
    selectedStatus: String?,
    selectedCountries: List<String>,
    selectedStates: List<String>,
    onCategorySelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onCountryToggle: (String) -> Unit,
    onStateToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    // NEW PARAMETERS
    viewModel: DealsViewModel,
    profileId: Int?,
    onSaveFilters: (List<String>, List<String>) -> Unit // Callback for saving filters via API
) {
    var currentSection by remember { mutableStateOf(FilterSection.MAIN) }
    var searchCategoryQuery by remember { mutableStateOf("") }
    var searchCountryQuery by remember { mutableStateOf("") }
    var searchStateQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with back button and close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentSection != FilterSection.MAIN) {
                        IconButton(onClick = { currentSection = FilterSection.MAIN }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Text(
                        text = when (currentSection) {
                            FilterSection.MAIN -> "Filters"
                            FilterSection.CATEGORIES -> "Categories"
                            FilterSection.STATUS -> "Brand Status"
                            FilterSection.LOCATION -> "Location"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0))

                // Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (currentSection) {
                        FilterSection.MAIN -> {
                            MobileMainContent(
                                onCategoryClick = { currentSection = FilterSection.CATEGORIES },
                                onStatusClick = { currentSection = FilterSection.STATUS },
                                onLocationClick = { currentSection = FilterSection.LOCATION },
                                selectedCategory = selectedCategory,
                                selectedStatus = selectedStatus,
                                selectedCountriesCount = selectedCountries.size + selectedStates.size
                            )
                        }

                        FilterSection.CATEGORIES -> {
                            MobileCategoriesSelectionContent(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                searchQuery = searchCategoryQuery,
                                onSearchChange = { searchCategoryQuery = it },
                                onCategorySelected = onCategorySelected
                            )
                        }

                        FilterSection.STATUS -> {
                            MobileStatusContent(
                                statuses = statuses,
                                selectedStatus = selectedStatus,
                                onStatusSelected = onStatusSelected
                            )
                        }

                        FilterSection.LOCATION -> {
                            MobileLocationContent(
                                countries = countries,
                                states = states,
                                selectedCountries = selectedCountries,
                                selectedStates = selectedStates,
                                searchCountryQuery = searchCountryQuery,
                                searchStateQuery = searchStateQuery,
                                onSearchCountryChange = { searchCountryQuery = it },
                                onSearchStateChange = { searchStateQuery = it },
                                onCountryToggle = onCountryToggle,
                                onStateToggle = onStateToggle
                            )
                        }
                    }
                }

                // Footer Buttons
                Divider(color = Color(0xFFE0E0E0))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            // NEW: Save filters via API before applying
                            val categoriesToSave = listOfNotNull(selectedCategory)
                            val locationsToSave = selectedCountries + selectedStates

                            if (profileId != null && profileId != -1) {
                                onSaveFilters(categoriesToSave, locationsToSave)
                            }

                            onApply()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply Filters", color = Color.White, fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancel", fontSize = 14.sp)
                        }
                        OutlinedButton(
                            onClick = onClear,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF9C27B0)
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFF9C27B0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Reset", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}







@Composable
fun MobileMainContent(
    onCategoryClick: () -> Unit,
    onStatusClick: () -> Unit,
    onLocationClick: () -> Unit,
    selectedCategory: String?,
    selectedStatus: String?,
    selectedCountriesCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Filter Options",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )

        // Category Filter Card
        FilterOptionCard(
            title = "Categories",
            subtitle = selectedCategory ?: "All Categories",
            badge = if (selectedCategory != null) "1" else null,
            onClick = onCategoryClick
        )

        // Status Filter Card
        FilterOptionCard(
            title = "Brand Status",
            subtitle = selectedStatus ?: "All Status",
            badge = if (selectedStatus != null) "1" else null,
            onClick = onStatusClick
        )

        // Location Filter Card
        FilterOptionCard(
            title = "Location",
            subtitle = if (selectedCountriesCount > 0)
                "$selectedCountriesCount selected"
            else
                "Select countries & states",
            badge = if (selectedCountriesCount > 0) selectedCountriesCount.toString() else null,
            onClick = onLocationClick
        )
    }
}

@Composable
fun MobileCategoriesSelectionContent(
    categories: List<String>,
    selectedCategory: String?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search categories...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(fontSize = 14.sp)
        )

        // All Categories Option
        MobileCategoryOption(
            text = "All Categories",
            isSelected = selectedCategory == null,
            onClick = { onCategorySelected(null) }
        )

        // Category List
        val filteredCategories = categories.filter {
            it.contains(searchQuery, ignoreCase = true)
        }

        filteredCategories.forEach { category ->
            MobileCategoryOption(
                text = category,
                isSelected = selectedCategory == category,
                onClick = {
                    onCategorySelected(if (selectedCategory == category) null else category)
                }
            )
        }
    }
}

@Composable
fun MobileCategoryOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            2.dp,
            if (isSelected) Color(0xFF9C27B0) else Color(0xFFE0E0E0)
        ),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF9C27B0)
                )
            )
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun FilterOptionCard(
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (badge != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF9C27B0)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MobileStatusContent(
    statuses: List<String>,
    selectedStatus: String?,
    onStatusSelected: (String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // All Status Option
        MobileStatusOption(
            text = "All Status",
            isSelected = selectedStatus == null,
            onClick = { onStatusSelected(null) }
        )

        statuses.forEach { status ->
            MobileStatusOption(
                text = status,
                isSelected = selectedStatus == status,
                onClick = { onStatusSelected(if (selectedStatus == status) null else status) }
            )
        }
    }
}

@Composable
fun MobileStatusOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            2.dp,
            if (isSelected) Color(0xFF9C27B0) else Color(0xFFE0E0E0)
        ),
        color = if (isSelected) Color(0xFFF3E5F5) else Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF9C27B0)
                )
            )
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MobileLocationContent(
    countries: List<String>,
    states: Map<String, List<String>>,
    selectedCountries: List<String>,
    selectedStates: List<String>,
    searchCountryQuery: String,
    searchStateQuery: String,
    onSearchCountryChange: (String) -> Unit,
    onSearchStateChange: (String) -> Unit,
    onCountryToggle: (String) -> Unit,
    onStateToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Selected chips
        if (selectedCountries.isNotEmpty() || selectedStates.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Selected (${selectedCountries.size + selectedStates.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedCountries.forEach { country ->
                        LocationChip(text = country, onRemove = { onCountryToggle(country) })
                    }
                    selectedStates.forEach { state ->
                        LocationChip(text = state, onRemove = { onStateToggle(state) })
                    }
                }
            }
        }

        // Country Selection
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Select Country",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            OutlinedTextField(
                value = searchCountryQuery,
                onValueChange = onSearchCountryChange,
                placeholder = { Text("Search countries...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(fontSize = 14.sp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val filteredCountries = countries.filter {
                    it.contains(searchCountryQuery, ignoreCase = true)
                }

                items(filteredCountries) { country ->
                    val isSelected = selectedCountries.contains(country)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCountryToggle(country) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFFF3E5F5) else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF9C27B0) else Color(0xFFE0E0E0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onCountryToggle(country) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF9C27B0)
                                )
                            )
                            Text(
                                text = country,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // State Selection
        if (selectedCountries.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Select State/City",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                OutlinedTextField(
                    value = searchStateQuery,
                    onValueChange = onSearchStateChange,
                    placeholder = {
                        Text(
                            "Search states...",
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = TextStyle(fontSize = 14.sp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val availableStates = selectedCountries.flatMap { country ->
                        states[country] ?: emptyList()
                    }.filter {
                        it.contains(searchStateQuery, ignoreCase = true)
                    }

                    items(availableStates) { state ->
                        val isSelected = selectedStates.contains(state)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStateToggle(state) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFFF3E5F5) else Color.White,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF9C27B0) else Color(0xFFE0E0E0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onStateToggle(state) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF9C27B0)
                                    )
                                )
                                Text(
                                    text = state,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF9C27B0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE8E5FF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color(0xFF9C27B0),
                fontWeight = FontWeight.Medium
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color(0xFF9C27B0),
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}


@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        var yPosition = 0
        var xPosition = 0
        var maxHeight = 0

        val rows = mutableListOf<List<Pair<Placeable, Int>>>()
        var currentRow = mutableListOf<Pair<Placeable, Int>>()

        placeables.forEach { placeable ->
            if (xPosition + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                yPosition += maxHeight + 8
                xPosition = 0
                maxHeight = 0
            }

            currentRow.add(placeable to xPosition)
            xPosition += placeable.width + 8
            maxHeight = maxOf(maxHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        val height = yPosition + maxHeight

        layout(constraints.maxWidth, height) {
            rows.forEachIndexed { index, row ->
                var rowY = 0
                row.forEachIndexed { _, (placeable, x) ->
                    placeable.place(x, yPosition - maxHeight + rowY)
                }
                yPosition += maxHeight + 8
                maxHeight = 0
            }
        }
    }
}

//enum class FilterSection {
//    CATEGORIES,
//    STATUS,
//    LOCATION
//}


@Composable
fun DealCard(deal: Deal) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Brand Name
            Text(
                text = deal.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Posted by
            Text(
                text = "by ${deal.brandName}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Category
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = deal.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = deal.location,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Budget
            val budgetText = if (deal.minAmount != null && deal.maxAmount != null) {
                "₹ ${deal.minAmount} - ${deal.maxAmount} ${deal.currency}"
            } else {
                "Barter Deal"
            }

            Text(
                text = budgetText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (deal.collaborationType == "barter") Color(0xFFFF9800) else Color(
                    0xFF00C853
                ),
                modifier = Modifier.padding(top = 8.dp)
            )

            // Collaboration Type Badge
            Surface(
                modifier = Modifier.padding(top = 4.dp),
                shape = RoundedCornerShape(6.dp),
                color = when (deal.collaborationType) {
                    "paid" -> Color(0xFFE8F5E9)
                    "barter" -> Color(0xFFFFF3E0)
                    "hybrid" -> Color(0xFFE3F2FD)
                    else -> Color(0xFFF5F5F5)
                }
            ) {
                Text(
                    text = deal.collaborationType.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = when (deal.collaborationType) {
                        "paid" -> Color(0xFF2E7D32)
                        "barter" -> Color(0xFFE65100)
                        "hybrid" -> Color(0xFF1565C0)
                        else -> Color.Gray
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            // Barter Details
            if (deal.collaborationType == "barter" && deal.barterDetails.offer != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Barter: ${deal.barterDetails.offer}",
                        fontSize = 12.sp,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Content",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = deal.description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Targeted Audience
                Text(
                    text = "Targeted Audience",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 12.dp)
                )

                Text(
                    text = "Age: ${deal.targetedUsers.age}",
                    fontSize = 11.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Text(
                    text = "Interest: ${deal.targetedUsers.interest}",
                    fontSize = 11.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Followers
                Text(
                    text = "Followers: ${deal.targetedUsers.followers.joinToString(", ")}",
                    fontSize = 11.sp,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Deliverables
                Text(
                    text = "Deliverables",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    deal.deliverables.forEach { deliverable ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = deliverable,
                                fontSize = 11.sp,
                                color = Color(0xFFFF6F00),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Application Count
            if (deal.applicationCount > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null,
                        tint = Color(0xFF6C63FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${deal.applicationCount} applications",
                        fontSize = 12.sp,
                        color = Color(0xFF6C63FF)
                    )
                }
            }

            // Deal Status or Button
            if (deal.status == "closed") {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Deal Closed",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (deal.isApplied) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = Color(0xFFE3F2FD),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Application Submitted",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            } else {
                Button(
                    onClick = { /* Handle deal application */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Apply for Deal",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}


//
@Composable
fun EnhancedFilterDialog(
    categories: List<String>,
    statuses: List<String>,
    countries: List<String>,
    states: Map<String, List<String>>,
    selectedCategory: String?,
    selectedStatus: String?,
    selectedCountries: List<String>,
    selectedStates: List<String>,
    onCategorySelected: (String?) -> Unit,
    onStatusSelected: (String?) -> Unit,
    onCountryToggle: (String) -> Unit,
    onStateToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit
) {
    var currentSection by remember { mutableStateOf(FilterSection.CATEGORIES) }
    var searchCountryQuery by remember { mutableStateOf("") }
    var searchStateQuery by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                Column(
                    modifier = Modifier
                        .width(180.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFF8F9FA))
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Categories",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    FilterSidebarItem(
                        text = "Status",
                        isSelected = currentSection == FilterSection.STATUS,
                        onClick = { currentSection = FilterSection.STATUS }
                    )

                    FilterSidebarItem(
                        text = "Location",
                        isSelected = currentSection == FilterSection.LOCATION,
                        onClick = { currentSection = FilterSection.LOCATION }
                    )
                }

                // Content Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentSection) {
                                FilterSection.CATEGORIES -> "Categories"
                                FilterSection.STATUS -> "Status"
                                FilterSection.LOCATION -> "Location"
                                FilterSection.MAIN -> TODO()
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    }

                    Divider(color = Color(0xFFE0E0E0))

                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        when (currentSection) {
                            FilterSection.STATUS -> {
                                StatusFilterContent(
                                    statuses = statuses,
                                    selectedStatus = selectedStatus,
                                    onStatusSelected = onStatusSelected
                                )
                            }

                            FilterSection.LOCATION -> {
                                LocationFilterContent(
                                    countries = countries,
                                    states = states,
                                    selectedCountries = selectedCountries,
                                    selectedStates = selectedStates,
                                    searchCountryQuery = searchCountryQuery,
                                    searchStateQuery = searchStateQuery,
                                    onSearchCountryChange = { searchCountryQuery = it },
                                    onSearchStateChange = { searchStateQuery = it },
                                    onCountryToggle = onCountryToggle,
                                    onStateToggle = onStateToggle
                                )
                            }

                            else -> {}
                        }
                    }

                    // Footer Buttons
                    Divider(color = Color(0xFFE0E0E0))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            )
                        ) {
                            Text("Cancel")
                        }
                        OutlinedButton(
                            onClick = onClear,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF9C27B0)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF9C27B0))
                        ) {
                            Text("Reset Filters")
                        }
                        Button(
                            onClick = onApply,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Text("Apply Filters", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

package com.cc.creatorcircle.ui.screens.profile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserUpdateState
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import androidx.compose.foundation.layout.ExperimentalLayoutApi


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonalInfoScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val postsViewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // Initialize UserViewModel for updates
    val userViewModel = remember { UserViewModel(context) }

    // Initialize Firebase Analytics
    val firebaseAnalytics = remember { Firebase.analytics }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("PersonalInfoScreen", "PersonalInfoScreen")
        FirebaseAnalyticsHelper.logEvent("personal_info_screen_opened")
    }

    // Collect user profile from PostsViewModel
    val userProfile by postsViewModel.userProfile.collectAsState()
    val isLoading by postsViewModel.profileLoading.collectAsState()
    val error by postsViewModel.profileError.collectAsState()

    // Collect update state from UserViewModel
    val updateState by userViewModel.updateState.collectAsState()

    // State variables for form fields
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    // Track field changes
    var hasEditedFullName by remember { mutableStateOf(false) }
    var hasEditedBio by remember { mutableStateOf(false) }
    var hasEditedMobile by remember { mutableStateOf(false) }
    var hasEditedAge by remember { mutableStateOf(false) }

    // Available categories
    val availableCategories = listOf(
        "Education", "Technology", "Fashion", "Food", "Travel",
        "Fitness", "Gaming", "Music", "Art", "Photography",
        "Business", "Lifestyle", "Entertainment"
    )

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logEvent("personal_info_load_started")
        postsViewModel.fetchUserProfile()
    }

    // Update fields when profile is loaded
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            email = profile.email
            username = profile.username
            fullName = profile.full_name ?: ""
            bio = profile.bio ?: ""
            mobileNumber = profile.mobile_number ?: ""
            age = profile.age.toString()
            selectedCategories = profile.categories ?: emptyList()

            // Track profile loaded
            FirebaseAnalyticsHelper.logEvent(
                "personal_info_loaded",
                mapOf(
                    "has_full_name" to (profile.full_name?.isNotBlank() == true).toString(),
                    "has_bio" to (profile.bio?.isNotBlank() == true).toString(),
                    "has_mobile" to (profile.mobile_number?.isNotBlank() == true).toString(),
                    "has_age" to (profile.age != null).toString(),
                    "categories_count" to (profile.categories?.size ?: 0).toString()
                )
            )
        }
    }

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                // Track successful update
                FirebaseAnalyticsHelper.logFeatureUsed("personal_info_updated_successfully")
                FirebaseAnalyticsHelper.logEvent(
                    "personal_info_update_success",
                    mapOf(
                        "edited_full_name" to hasEditedFullName.toString(),
                        "edited_bio" to hasEditedBio.toString(),
                        "edited_mobile" to hasEditedMobile.toString(),
                        "edited_age" to hasEditedAge.toString(),
                        "categories_count" to selectedCategories.size.toString()
                    )
                )

                userViewModel.resetUpdateState()
                // Refresh the profile data
                postsViewModel.fetchUserProfile()
                // Navigate back or dismiss
                onDismiss()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()

                // Track update error
                FirebaseAnalyticsHelper.logEvent(
                    "personal_info_update_error",
                    mapOf("error" to errorMessage)
                )

                userViewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFB388FF)
                )
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error ?: "An error occurred",
                        color = Color.Red,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            FirebaseAnalyticsHelper.logFeatureUsed("retry_personal_info_load")
                            postsViewModel.fetchUserProfile()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB388FF)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            else -> {
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
                                    FirebaseAnalyticsHelper.logFeatureUsed("personal_info_back_button")
                                    FirebaseAnalyticsHelper.logEvent(
                                        "personal_info_cancelled",
                                        mapOf("reason" to "back_button")
                                    )
                                    onDismiss()
                                },
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Personal Information",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Email Section (Read-only)
                        Column {
                            Text(
                                text = "Email",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = email,
                                fontSize = 16.sp,
                                color = Color(0xFF757575),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Username Section (Read-only)
                        Column {
                            Text(
                                text = "Username",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = username,
                                fontSize = 16.sp,
                                color = Color(0xFF757575),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        // Full Name Section
                        Column {
                            Text(
                                text = "Full Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = {
                                    fullName = it
                                    if (!hasEditedFullName) {
                                        hasEditedFullName = true
                                        FirebaseAnalyticsHelper.logEvent("full_name_field_edited")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 16.sp,
                                    color = Color(0xFF424242)
                                )
                            )
                        }

                        // Bio Section
                        Column {
                            Text(
                                text = "Bio",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bio,
                                onValueChange = {
                                    bio = it
                                    if (!hasEditedBio) {
                                        hasEditedBio = true
                                        FirebaseAnalyticsHelper.logEvent("bio_field_edited")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 16.sp,
                                    color = Color(0xFF424242)
                                )
                            )
                        }

                        // Mobile Number Section
                        Column {
                            Text(
                                text = "Mobile Number",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = mobileNumber,
                                onValueChange = {
                                    mobileNumber = it
                                    if (!hasEditedMobile) {
                                        hasEditedMobile = true
                                        FirebaseAnalyticsHelper.logEvent("mobile_number_field_edited")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 16.sp,
                                    color = Color(0xFF424242)
                                )
                            )
                        }

                        // Age Section
                        Column {
                            Text(
                                text = "Age",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = age,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                        age = newValue
                                        if (!hasEditedAge) {
                                            hasEditedAge = true
                                            FirebaseAnalyticsHelper.logEvent("age_field_edited")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedBorderColor = Color(0xFF8B5CF6),
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 16.sp,
                                    color = Color(0xFF424242)
                                )
                            )
                        }



                        // Categories Section
                        Column {
                            Text(
                                text = "Categories",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Display selected categories in wrapping rows
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedCategories.forEach { category ->
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFFE3F2FD),
                                        modifier = Modifier
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(
                                                horizontal = 16.dp,
                                                vertical = 8.dp
                                            ),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = category,
                                                color = Color(0xFF1976D2),
                                                fontSize = 14.sp
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_cross),
                                                contentDescription = "Remove category",
                                                tint = Color(0xFFE53935),
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        FirebaseAnalyticsHelper.logFeatureUsed("category_removed")
                                                        FirebaseAnalyticsHelper.logEvent(
                                                            "category_removed",
                                                            mapOf("category" to category)
                                                        )
                                                        selectedCategories = selectedCategories.filter { it != category }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Select Categories Button
                            TextButton(
                                onClick = {
                                    FirebaseAnalyticsHelper.logFeatureUsed("select_categories_clicked")
                                    FirebaseAnalyticsHelper.logEvent(
                                        "category_dialog_opened",
                                        mapOf("current_count" to selectedCategories.size.toString())
                                    )
                                    showCategoryDialog = true
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF8B5CF6)
                                )
                            ) {
                                Text(
                                    text = "Select Categories",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }



                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Save Button (Fixed at bottom)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (updateState is UserUpdateState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp),
                                color = Color(0xFFB388FF)
                            )
                        } else {
                            GradientButton(
                                text = "Save",
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Track save button click
                                FirebaseAnalyticsHelper.logFeatureUsed("personal_info_save_clicked")

                                // Log Firebase Analytics event
                                val bundle = Bundle().apply {
                                    putString("screen_name", "personal_info")
                                    putString("username", username)
                                    putInt("categories_count", selectedCategories.size)
                                    putBoolean("has_full_name", fullName.isNotBlank())
                                    putBoolean("has_bio", bio.isNotBlank())
                                    putBoolean("has_mobile", mobileNumber.isNotBlank())
                                    putBoolean("has_age", age.isNotBlank())
                                }
                                firebaseAnalytics.logEvent("profile_save_clicked", bundle)

                                // Validate age
                                val ageInt = age.toIntOrNull()

                                if (ageInt == null && age.isNotBlank()) {
                                    Toast.makeText(context, "Please enter a valid age", Toast.LENGTH_SHORT).show()

                                    // Track validation error
                                    FirebaseAnalyticsHelper.logEvent(
                                        "personal_info_validation_error",
                                        mapOf("error_type" to "invalid_age")
                                    )
                                    return@GradientButton
                                }

                                // Track save initiation
                                FirebaseAnalyticsHelper.logEvent(
                                    "personal_info_save_initiated",
                                    mapOf(
                                        "full_name_length" to fullName.length.toString(),
                                        "bio_length" to bio.length.toString(),
                                        "has_mobile" to mobileNumber.isNotBlank().toString(),
                                        "has_age" to (ageInt != null).toString(),
                                        "categories_count" to selectedCategories.size.toString(),
                                        "categories" to selectedCategories.joinToString(",")
                                    )
                                )

                                // Update user data
                                userViewModel.updateUser(
                                    fullName = fullName.takeIf { it.isNotBlank() },
                                    bio = bio.takeIf { it.isNotBlank() },
                                    mobileNumber = mobileNumber.takeIf { it.isNotBlank() },
                                    age = ageInt,
                                    categories = selectedCategories.takeIf { it.isNotEmpty() }
                                )

                                Log.d("ProfileInfoScreen", "Saving profile data:")
                                Log.d("ProfileInfoScreen", "Full Name: $fullName")
                                Log.d("ProfileInfoScreen", "Bio: $bio")
                                Log.d("ProfileInfoScreen", "Mobile Number: $mobileNumber")
                                Log.d("ProfileInfoScreen", "Age: $age")
                                Log.d("ProfileInfoScreen", "Categories: $selectedCategories")
                            }
                        }
                    }
                }
            }
        }

        // Category Selection Dialog
        if (showCategoryDialog) {
            AlertDialog(
                onDismissRequest = {
                    FirebaseAnalyticsHelper.logDialogClosed("category_dialog", "dismissed")
                    showCategoryDialog = false
                },
                title = {
                    Text(
                        text = "Select Categories",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        availableCategories.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val wasSelected = selectedCategories.contains(category)
                                        selectedCategories = if (wasSelected) {
                                            selectedCategories.filter { it != category }
                                        } else {
                                            selectedCategories + category
                                        }

                                        // Track category selection
                                        FirebaseAnalyticsHelper.logFeatureUsed(
                                            if (wasSelected) "category_deselected" else "category_selected"
                                        )
                                        FirebaseAnalyticsHelper.logEvent(
                                            if (wasSelected) "category_deselected" else "category_selected",
                                            mapOf(
                                                "category" to category,
                                                "total_selected" to selectedCategories.size.toString()
                                            )
                                        )
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedCategories.contains(category),
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF8B5CF6)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            FirebaseAnalyticsHelper.logDialogClosed("category_dialog", "confirmed")
                            FirebaseAnalyticsHelper.logEvent(
                                "category_selection_completed",
                                mapOf(
                                    "total_selected" to selectedCategories.size.toString(),
                                    "categories" to selectedCategories.joinToString(",")
                                )
                            )
                            showCategoryDialog = false
                        }
                    ) {
                        Text(
                            text = "Done",
                            color = Color(0xFF8B5CF6)
                        )
                    }
                },
                containerColor = Color.White
            )
        }

    }
}










//package com.cc.creatorcircle.ui.screens.profile
//
//import android.os.Bundle
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//import com.cc.creatorcircle.viewModel.UserViewModel
//import com.cc.creatorcircle.viewModel.UserUpdateState
//import com.google.firebase.Firebase
//import com.google.firebase.analytics.analytics
//
//@Composable
//fun PersonalInfoScreen(
//    navController: NavController,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    // Initialize UserViewModel for updates
//    val userViewModel = remember { UserViewModel(context) }
//
//    // Initialize Firebase Analytics
//    val firebaseAnalytics = remember { Firebase.analytics }
//
//    // Collect user profile from PostsViewModel
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val isLoading by postsViewModel.profileLoading.collectAsState()
//    val error by postsViewModel.profileError.collectAsState()
//
//    // Collect update state from UserViewModel
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // State variables for form fields
//    var email by remember { mutableStateOf("") }
//    var username by remember { mutableStateOf("") }
//    var fullName by remember { mutableStateOf("") }
//    var bio by remember { mutableStateOf("") }
//    var mobileNumber by remember { mutableStateOf("") }
//    var age by remember { mutableStateOf("") }
//    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
//    var showCategoryDialog by remember { mutableStateOf(false) }
//
//    // Available categories
//    val availableCategories = listOf(
//        "Education", "Technology", "Fashion", "Food", "Travel",
//        "Fitness", "Gaming", "Music", "Art", "Photography",
//        "Business", "Lifestyle", "Entertainment"
//    )
//
//    // Fetch user profile when screen loads
//    LaunchedEffect(Unit) {
//        postsViewModel.fetchUserProfile()
//    }
//
//    // Update fields when profile is loaded
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            email = profile.email
//            username = profile.username
//            fullName = profile.full_name ?: ""
//            bio = profile.bio ?: ""
//            mobileNumber = profile.mobile_number ?: ""
//            age = profile.age.toString()
//            selectedCategories = profile.categories ?: emptyList()
//        }
//    }
//
//    // Handle update state changes
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UserUpdateState.Success -> {
//                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
//                userViewModel.resetUpdateState()
//                // Refresh the profile data
//                postsViewModel.fetchUserProfile()
//                // Navigate back or dismiss
//                onDismiss()
//            }
//            is UserUpdateState.Error -> {
//                val errorMessage = (updateState as UserUpdateState.Error).message
//                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
//                userViewModel.resetUpdateState()
//            }
//            else -> {}
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFAF8F8))
//    ) {
//        when {
//            isLoading -> {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                    color = Color(0xFFB388FF)
//                )
//            }
//
//            error != null -> {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = error ?: "An error occurred",
//                        color = Color.Red,
//                        fontSize = 16.sp
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = { postsViewModel.fetchUserProfile() },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFB388FF)
//                        )
//                    ) {
//                        Text("Retry")
//                    }
//                }
//            }
//
//            else -> {
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    // Top Bar with Back Arrow
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_left_arrow),
//                            contentDescription = "Back",
//                            modifier = Modifier
//                                .size(26.dp)
//                                .clickable { onDismiss() },
//                            tint = Color.Black
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text(
//                            text = "Personal Information",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                    }
//
//                    // Scrollable Content
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .verticalScroll(rememberScrollState())
//                            .padding(horizontal = 24.dp),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Email Section (Read-only)
//                        Column {
//                            Text(
//                                text = "Email",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = email,
//                                fontSize = 16.sp,
//                                color = Color(0xFF757575),
//                                modifier = Modifier.padding(vertical = 4.dp)
//                            )
//                        }
//
//                        // Username Section (Read-only)
//                        Column {
//                            Text(
//                                text = "Username",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = username,
//                                fontSize = 16.sp,
//                                color = Color(0xFF757575),
//                                modifier = Modifier.padding(vertical = 4.dp)
//                            )
//                        }
//
//                        // Full Name Section
//                        Column {
//                            Text(
//                                text = "Full Name",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = fullName,
//                                onValueChange = { fullName = it },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//                        // Bio Section
//                        Column {
//                            Text(
//                                text = "Bio",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = bio,
//                                onValueChange = { bio = it },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//                        // Mobile Number Section
//                        Column {
//                            Text(
//                                text = "Mobile Number",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = mobileNumber,
//                                onValueChange = { mobileNumber = it },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//                        // Age Section
//                        Column {
//                            Text(
//                                text = "Age",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = age,
//                                onValueChange = { newValue ->
//                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
//                                        age = newValue
//                                    }
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//
//
//                        // Categories Section
//                        Column {
//                            Text(
//                                text = "Categories",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Display selected categories in wrapping rows
//                            FlowRow(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                selectedCategories.forEach { category ->
//                                    Surface(
//                                        shape = RoundedCornerShape(20.dp),
//                                        color = Color(0xFFE3F2FD),
//                                        modifier = Modifier
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.padding(
//                                                horizontal = 16.dp,
//                                                vertical = 8.dp
//                                            ),
//                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                            verticalAlignment = Alignment.CenterVertically
//                                        ) {
//                                            Text(
//                                                text = category,
//                                                color = Color(0xFF1976D2),
//                                                fontSize = 14.sp
//                                            )
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.ic_cross),
//                                                contentDescription = "Remove category",
//                                                tint = Color(0xFFE53935),
//                                                modifier = Modifier
//                                                    .size(18.dp)
//                                                    .clickable {
//                                                        selectedCategories = selectedCategories.filter { it != category }
//                                                    }
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Select Categories Button
//                            TextButton(
//                                onClick = { showCategoryDialog = true },
//                                colors = ButtonDefaults.textButtonColors(
//                                    contentColor = Color(0xFF8B5CF6)
//                                )
//                            ) {
//                                Text(
//                                    text = "Select Categories",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//                        }
//
//
//
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // Save Button (Fixed at bottom)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 12.dp, vertical = 8.dp)
//                    ) {
//                        if (updateState is UserUpdateState.Loading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier
//                                    .align(Alignment.Center)
//                                    .size(40.dp),
//                                color = Color(0xFFB388FF)
//                            )
//                        } else {
//                            GradientButton(
//                                text = "Save",
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                // Log Firebase Analytics event
//                                val bundle = Bundle().apply {
//                                    putString("screen_name", "personal_info")
//                                    putString("username", username)
//                                    putInt("categories_count", selectedCategories.size)
//                                    putBoolean("has_full_name", fullName.isNotBlank())
//                                    putBoolean("has_bio", bio.isNotBlank())
//                                    putBoolean("has_mobile", mobileNumber.isNotBlank())
//                                    putBoolean("has_age", age.isNotBlank())
//                                }
//                                firebaseAnalytics.logEvent("profile_save_clicked", bundle)
//
//                                // Validate age
//                                val ageInt = age.toIntOrNull()
//
//                                if (ageInt == null && age.isNotBlank()) {
//                                    Toast.makeText(context, "Please enter a valid age", Toast.LENGTH_SHORT).show()
//                                    return@GradientButton
//                                }
//
//                                // Update user data
//                                userViewModel.updateUser(
//                                    fullName = fullName.takeIf { it.isNotBlank() },
//                                    bio = bio.takeIf { it.isNotBlank() },
//                                    mobileNumber = mobileNumber.takeIf { it.isNotBlank() },
//                                    age = ageInt,
//                                    categories = selectedCategories.takeIf { it.isNotEmpty() }
//                                )
//
//                                Log.d("ProfileInfoScreen", "Saving profile data:")
//                                Log.d("ProfileInfoScreen", "Full Name: $fullName")
//                                Log.d("ProfileInfoScreen", "Bio: $bio")
//                                Log.d("ProfileInfoScreen", "Mobile Number: $mobileNumber")
//                                Log.d("ProfileInfoScreen", "Age: $age")
//                                Log.d("ProfileInfoScreen", "Categories: $selectedCategories")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Category Selection Dialog
//        if (showCategoryDialog) {
//            AlertDialog(
//                onDismissRequest = { showCategoryDialog = false },
//                title = {
//                    Text(
//                        text = "Select Categories",
//                        fontWeight = FontWeight.SemiBold
//                    )
//                },
//                text = {
//                    Column(
//                        modifier = Modifier.verticalScroll(rememberScrollState())
//                    ) {
//                        availableCategories.forEach { category ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        selectedCategories = if (selectedCategories.contains(category)) {
//                                            selectedCategories.filter { it != category }
//                                        } else {
//                                            selectedCategories + category
//                                        }
//                                    }
//                                    .padding(vertical = 12.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Checkbox(
//                                    checked = selectedCategories.contains(category),
//                                    onCheckedChange = null,
//                                    colors = CheckboxDefaults.colors(
//                                        checkedColor = Color(0xFF8B5CF6)
//                                    )
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = category,
//                                    fontSize = 16.sp
//                                )
//                            }
//                        }
//                    }
//                },
//                confirmButton = {
//                    TextButton(
//                        onClick = { showCategoryDialog = false }
//                    ) {
//                        Text(
//                            text = "Done",
//                            color = Color(0xFF8B5CF6)
//                        )
//                    }
//                },
//                containerColor = Color.White
//            )
//        }
//
//    }
//}
//













//package com.cc.creatorcircle.ui.screens.profile
//
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//import com.cc.creatorcircle.viewModel.UserViewModel
//import com.cc.creatorcircle.viewModel.UserUpdateState
//
//@Composable
//fun PersonalInfoScreen(
//    navController: NavController,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    val postsViewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    // Initialize UserViewModel for updates
//    val userViewModel = remember { UserViewModel(context) }
//
//    // Collect user profile from PostsViewModel
//    val userProfile by postsViewModel.userProfile.collectAsState()
//    val isLoading by postsViewModel.profileLoading.collectAsState()
//    val error by postsViewModel.profileError.collectAsState()
//
//    // Collect update state from UserViewModel
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // State variables for form fields
//    var email by remember { mutableStateOf("") }
//    var username by remember { mutableStateOf("") }
//    var fullName by remember { mutableStateOf("") }
//    var bio by remember { mutableStateOf("") }
//    var mobileNumber by remember { mutableStateOf("") }
//    var age by remember { mutableStateOf("") }
//    var selectedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
//    var showCategoryDialog by remember { mutableStateOf(false) }
//
//    // Available categories
//    val availableCategories = listOf(
//        "Education", "Technology", "Fashion", "Food", "Travel",
//        "Fitness", "Gaming", "Music", "Art", "Photography",
//        "Business", "Lifestyle", "Entertainment"
//    )
//
//    // Fetch user profile when screen loads
//    LaunchedEffect(Unit) {
//        postsViewModel.fetchUserProfile()
//    }
//
//    // Update fields when profile is loaded
//    LaunchedEffect(userProfile) {
//        userProfile?.let { profile ->
//            email = profile.email
//            username = profile.username
//            fullName = profile.full_name ?: ""
//            bio = profile.bio ?: ""
//            mobileNumber = profile.mobile_number ?: ""
//            age = profile.age.toString()
//            selectedCategories = profile.categories ?: emptyList()
//        }
//    }
//
//    // Handle update state changes
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UserUpdateState.Success -> {
//                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
//                userViewModel.resetUpdateState()
//                // Refresh the profile data
//                postsViewModel.fetchUserProfile()
//                // Navigate back or dismiss
//                onDismiss()
//            }
//            is UserUpdateState.Error -> {
//                val errorMessage = (updateState as UserUpdateState.Error).message
//                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
//                userViewModel.resetUpdateState()
//            }
//            else -> {}
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFAF8F8))
//    ) {
//        when {
//            isLoading -> {
//                CircularProgressIndicator(
//                    modifier = Modifier.align(Alignment.Center),
//                    color = Color(0xFFB388FF)
//                )
//            }
//
//            error != null -> {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = error ?: "An error occurred",
//                        color = Color.Red,
//                        fontSize = 16.sp
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Button(
//                        onClick = { postsViewModel.fetchUserProfile() },
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = Color(0xFFB388FF)
//                        )
//                    ) {
//                        Text("Retry")
//                    }
//                }
//            }
//
//            else -> {
//                Column(
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    // Top Bar with Back Arrow
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp, vertical = 16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_left_arrow),
//                            contentDescription = "Back",
//                            modifier = Modifier
//                                .size(26.dp)
//                                .clickable { onDismiss() },
//                            tint = Color.Black
//                        )
//                        Spacer(modifier = Modifier.width(16.dp))
//                        Text(
//                            text = "Personal Information",
//                            fontSize = 22.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.Black
//                        )
//                    }
//
//                    // Scrollable Content
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .weight(1f)
//                            .verticalScroll(rememberScrollState())
//                            .padding(horizontal = 24.dp),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        // Email Section (Read-only)
//                        Column {
//                            Text(
//                                text = "Email",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = email,
//                                fontSize = 16.sp,
//                                color = Color(0xFF757575),
//                                modifier = Modifier.padding(vertical = 4.dp)
//                            )
//                        }
//
//                        // Username Section (Read-only)
//                        Column {
//                            Text(
//                                text = "Username",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = username,
//                                fontSize = 16.sp,
//                                color = Color(0xFF757575),
//                                modifier = Modifier.padding(vertical = 4.dp)
//                            )
//                        }
//
//                        // Full Name Section
//                        Column {
//                            Text(
//                                text = "Full Name",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = fullName,
//                                onValueChange = { fullName = it },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//                        // Bio Section
//                        Column {
//                            Text(
//                                text = "Bio",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = bio,
//                                onValueChange = { bio = it },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//                        // Mobile Number Section
//                        Column {
//                            Text(
//                                text = "Mobile Number",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = mobileNumber,
//                                onValueChange = { mobileNumber = it },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//                        // Age Section
//                        Column {
//                            Text(
//                                text = "Age",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            OutlinedTextField(
//                                value = age,
//                                onValueChange = { newValue ->
//                                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
//                                        age = newValue
//                                    }
//                                },
//                                modifier = Modifier.fillMaxWidth(),
//                                colors = OutlinedTextFieldDefaults.colors(
//                                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                                    focusedBorderColor = Color(0xFF8B5CF6),
//                                    unfocusedContainerColor = Color.White,
//                                    focusedContainerColor = Color.White
//                                ),
//                                shape = RoundedCornerShape(12.dp),
//                                textStyle = LocalTextStyle.current.copy(
//                                    fontSize = 16.sp,
//                                    color = Color(0xFF424242)
//                                )
//                            )
//                        }
//
//
//
//                        // Categories Section
//                        Column {
//                            Text(
//                                text = "Categories",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Display selected categories in wrapping rows
//                            FlowRow(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                verticalArrangement = Arrangement.spacedBy(8.dp)
//                            ) {
//                                selectedCategories.forEach { category ->
//                                    Surface(
//                                        shape = RoundedCornerShape(20.dp),
//                                        color = Color(0xFFE3F2FD),
//                                        modifier = Modifier
//                                    ) {
//                                        Row(
//                                            modifier = Modifier.padding(
//                                                horizontal = 16.dp,
//                                                vertical = 8.dp
//                                            ),
//                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                            verticalAlignment = Alignment.CenterVertically
//                                        ) {
//                                            Text(
//                                                text = category,
//                                                color = Color(0xFF1976D2),
//                                                fontSize = 14.sp
//                                            )
//                                            Icon(
//                                                painter = painterResource(id = R.drawable.ic_cross),
//                                                contentDescription = "Remove category",
//                                                tint = Color(0xFFE53935),
//                                                modifier = Modifier
//                                                    .size(18.dp)
//                                                    .clickable {
//                                                        selectedCategories = selectedCategories.filter { it != category }
//                                                    }
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Select Categories Button
//                            TextButton(
//                                onClick = { showCategoryDialog = true },
//                                colors = ButtonDefaults.textButtonColors(
//                                    contentColor = Color(0xFF8B5CF6)
//                                )
//                            ) {
//                                Text(
//                                    text = "Select Categories",
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium
//                                )
//                            }
//                        }
//
//
//
//                        Spacer(modifier = Modifier.height(16.dp))
//                    }
//
//                    // Save Button (Fixed at bottom)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 12.dp, vertical = 8.dp)
//                    ) {
//                        if (updateState is UserUpdateState.Loading) {
//                            CircularProgressIndicator(
//                                modifier = Modifier
//                                    .align(Alignment.Center)
//                                    .size(40.dp),
//                                color = Color(0xFFB388FF)
//                            )
//                        } else {
//                            GradientButton(
//                                text = "Save",
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                // Validate age
//                                val ageInt = age.toIntOrNull()
//
//                                if (ageInt == null && age.isNotBlank()) {
//                                    Toast.makeText(context, "Please enter a valid age", Toast.LENGTH_SHORT).show()
//                                    return@GradientButton
//                                }
//
//                                // Update user data
//                                userViewModel.updateUser(
//                                    fullName = fullName.takeIf { it.isNotBlank() },
//                                    bio = bio.takeIf { it.isNotBlank() },
//                                    mobileNumber = mobileNumber.takeIf { it.isNotBlank() },
//                                    age = ageInt,
//                                    categories = selectedCategories.takeIf { it.isNotEmpty() }
//                                )
//
//                                Log.d("ProfileInfoScreen", "Saving profile data:")
//                                Log.d("ProfileInfoScreen", "Full Name: $fullName")
//                                Log.d("ProfileInfoScreen", "Bio: $bio")
//                                Log.d("ProfileInfoScreen", "Mobile Number: $mobileNumber")
//                                Log.d("ProfileInfoScreen", "Age: $age")
//                                Log.d("ProfileInfoScreen", "Categories: $selectedCategories")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // Category Selection Dialog
//        if (showCategoryDialog) {
//            AlertDialog(
//                onDismissRequest = { showCategoryDialog = false },
//                title = {
//                    Text(
//                        text = "Select Categories",
//                        fontWeight = FontWeight.SemiBold
//                    )
//                },
//                text = {
//                    Column(
//                        modifier = Modifier.verticalScroll(rememberScrollState())
//                    ) {
//                        availableCategories.forEach { category ->
//                            Row(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .clickable {
//                                        selectedCategories = if (selectedCategories.contains(category)) {
//                                            selectedCategories.filter { it != category }
//                                        } else {
//                                            selectedCategories + category
//                                        }
//                                    }
//                                    .padding(vertical = 12.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Checkbox(
//                                    checked = selectedCategories.contains(category),
//                                    onCheckedChange = null,
//                                    colors = CheckboxDefaults.colors(
//                                        checkedColor = Color(0xFF8B5CF6)
//                                    )
//                                )
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = category,
//                                    fontSize = 16.sp
//                                )
//                            }
//                        }
//                    }
//                },
//                confirmButton = {
//                    TextButton(
//                        onClick = { showCategoryDialog = false }
//                    ) {
//                        Text(
//                            text = "Done",
//                            color = Color(0xFF8B5CF6)
//                        )
//                    }
//                },
//                containerColor = Color.White
//            )
//        }
//
//    }
//}
//

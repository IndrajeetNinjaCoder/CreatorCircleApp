package com.cc.creatorcircle.ui.screens.profile

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
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserUpdateState

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

    // Available categories
    val availableCategories = listOf(
        "Education", "Technology", "Fashion", "Food", "Travel",
        "Fitness", "Gaming", "Music", "Art", "Photography",
        "Business", "Lifestyle", "Entertainment"
    )

    // Fetch user profile when screen loads
    LaunchedEffect(Unit) {
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
        }
    }

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                userViewModel.resetUpdateState()
                // Refresh the profile data
                postsViewModel.fetchUserProfile()
                // Navigate back or dismiss
                onDismiss()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
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
                        onClick = { postsViewModel.fetchUserProfile() },
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
                                .clickable { onDismiss() },
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
                                onValueChange = { fullName = it },
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
                                onValueChange = { bio = it },
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
                                onValueChange = { mobileNumber = it },
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
//                        Column {
//                            Text(
//                                text = "Categories",
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color(0xFF424242)
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//
//                            // Display selected categories
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                                verticalAlignment = Alignment.CenterVertically
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
                                onClick = { showCategoryDialog = true },
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
                                // Validate age
                                val ageInt = age.toIntOrNull()

                                if (ageInt == null && age.isNotBlank()) {
                                    Toast.makeText(context, "Please enter a valid age", Toast.LENGTH_SHORT).show()
                                    return@GradientButton
                                }

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
                onDismissRequest = { showCategoryDialog = false },
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
                                        selectedCategories = if (selectedCategories.contains(category)) {
                                            selectedCategories.filter { it != category }
                                        } else {
                                            selectedCategories + category
                                        }
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
                        onClick = { showCategoryDialog = false }
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


@file:OptIn(ExperimentalLayoutApi::class)

package com.cc.creatorcircle.ui.screens.onboarding


import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.UserUpdateState
import com.cc.creatorcircle.viewModel.UserViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.alpha

enum class OnboardingStep {
    CATEGORY_SELECTION,
    PERSONAL_INFORMATION,
    INSTAGRAM_CONNECTION
}

@Composable
fun SignupOnboarding(navController: NavController) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(context) }
    var showDialog by remember { mutableStateOf(true) }

    // Track onboarding screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("SignupOnboarding", "SignupOnboarding")
        FirebaseAnalyticsHelper.logEvent("onboarding_started")
    }

    if (showDialog) {
        OnboardingFlowDialog(
            userViewModel = userViewModel,
            onDismiss = {
                FirebaseAnalyticsHelper.logEvent("onboarding_dismissed", mapOf(
                    "completion_status" to "incomplete"
                ))
                showDialog = false
                navController.navigate("saboai")
            },
            onComplete = { categories, name, age, instagramHandle ->
                println("Selected categories: $categories")
                println("Personal info - Name: $name, Age: $age")
                println("Instagram handle: $instagramHandle")

                FirebaseAnalyticsHelper.logEvent("onboarding_completed", mapOf(
                    "categories_count" to categories.size.toString(),
                    "has_name" to if (name.isNotBlank()) "yes" else "no",
                    "has_age" to if (age > 0) "yes" else "no",
                    "has_instagram" to if (instagramHandle.isNotBlank()) "yes" else "no"
                ))

                showDialog = false
            }
        )
    }
}

@Composable
fun OnboardingFlowDialog(
    userViewModel: UserViewModel,
    onDismiss: () -> Unit,
    onComplete: (List<String>, String, Int, String) -> Unit
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.CATEGORY_SELECTION) }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(0) }
    var instagramHandle by remember { mutableStateOf("") }

    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()

    // Track step changes
    LaunchedEffect(currentStep) {
        val stepName = when (currentStep) {
            OnboardingStep.CATEGORY_SELECTION -> "category_selection"
            OnboardingStep.PERSONAL_INFORMATION -> "personal_information"
            OnboardingStep.INSTAGRAM_CONNECTION -> "instagram_connection"
        }
        FirebaseAnalyticsHelper.logEvent("onboarding_step_viewed", mapOf(
            "step" to stepName,
            "step_number" to when (currentStep) {
                OnboardingStep.CATEGORY_SELECTION -> "1"
                OnboardingStep.PERSONAL_INFORMATION -> "2"
                OnboardingStep.INSTAGRAM_CONNECTION -> "3"
            }
        ))
    }

    // Track errors
    LaunchedEffect(error) {
        error?.let { errorMsg ->
            FirebaseAnalyticsHelper.logError(
                errorType = "onboarding_error",
                errorMessage = errorMsg,
                context = "OnboardingFlowDialog"
            )
            println("Error: $errorMsg")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            when (currentStep) {
                OnboardingStep.CATEGORY_SELECTION -> {
                    CategorySelectionStep(
                        userViewModel = userViewModel,
                        onDismiss = onDismiss,
                        onNext = { categories ->
                            selectedCategories = categories

                            // Track category selection
                            FirebaseAnalyticsHelper.logEvent("onboarding_categories_selected", mapOf(
                                "categories_count" to categories.size.toString(),
                                "categories" to categories.joinToString(",")
                            ))

                            userViewModel.updateUser(categories = categories)
                            currentStep = OnboardingStep.PERSONAL_INFORMATION
                        }
                    )
                }

                OnboardingStep.PERSONAL_INFORMATION -> {
                    PersonalInformationStep(
                        userViewModel = userViewModel,
                        isLoading = isLoading,
                        onDismiss = onDismiss,
                        onBack = {
                            FirebaseAnalyticsHelper.logEvent("onboarding_step_back", mapOf(
                                "from_step" to "personal_information",
                                "to_step" to "category_selection"
                            ))
                            currentStep = OnboardingStep.CATEGORY_SELECTION
                        },
                        onNext = { name, ageValue ->
                            fullName = name
                            age = ageValue

                            // Track personal information submission
                            FirebaseAnalyticsHelper.logEvent("onboarding_personal_info_submitted", mapOf(
                                "has_name" to if (name.isNotBlank()) "yes" else "no",
                                "has_age" to if (ageValue > 0) "yes" else "no",
                                "age_range" to when {
                                    ageValue == 0 -> "not_provided"
                                    ageValue < 18 -> "under_18"
                                    ageValue in 18..24 -> "18_24"
                                    ageValue in 25..34 -> "25_34"
                                    ageValue in 35..44 -> "35_44"
                                    ageValue >= 45 -> "45_plus"
                                    else -> "unknown"
                                }
                            ))

                            userViewModel.updateUser(
                                fullName = if (name.isNotBlank()) name else null,
                                age = if (ageValue > 0) ageValue else null
                            )

                            currentStep = OnboardingStep.INSTAGRAM_CONNECTION
                        }
                    )
                }

                OnboardingStep.INSTAGRAM_CONNECTION -> {
                    InstagramConnectionStep(
                        userViewModel = userViewModel,
                        onDismiss = onDismiss,
                        onBack = {
                            FirebaseAnalyticsHelper.logEvent("onboarding_step_back", mapOf(
                                "from_step" to "instagram_connection",
                                "to_step" to "personal_information"
                            ))
                            currentStep = OnboardingStep.PERSONAL_INFORMATION
                        },
                        onComplete = {
                            FirebaseAnalyticsHelper.logEvent("onboarding_instagram_completed")
                        }
                    )
                }
            }
        }
    }

    error?.let { errorMsg ->
        LaunchedEffect(errorMsg) {
            delay(3000)
            userViewModel.clearError()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelectionStep(
    userViewModel: UserViewModel,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onNext: (List<String>) -> Unit
) {
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }

    // Track category interactions
    LaunchedEffect(selectedCategories) {
        if (selectedCategories.isNotEmpty()) {
            FirebaseAnalyticsHelper.logEvent("onboarding_category_interaction", mapOf(
                "selected_count" to selectedCategories.size.toString()
            ))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar with skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = Color(0xFFE91E63),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    FirebaseAnalyticsHelper.logEvent("onboarding_step_skipped", mapOf(
                        "step" to "category_selection",
                        "categories_selected" to selectedCategories.size.toString()
                    ))
                    userViewModel.updateUser(onboardingStatus = false)
                    if (!isLoading) onDismiss()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Step 1: Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select one or more categories that best reflect your content or areas of interest",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        CategorySelection(
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            onCategoriesSelected = { categories ->
                selectedCategories = categories
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            GradientButton(
                text = if (isLoading) "Saving..." else "Next",
                enabled = !isLoading,
            ) {
                Log.d("Selected-categories", "CategorySelectionStep: " + selectedCategories)

                FirebaseAnalyticsHelper.logEvent("onboarding_step_next_clicked", mapOf(
                    "step" to "category_selection",
                    "categories_count" to selectedCategories.size.toString()
                ))

                val categoriesList = selectedCategories.toList()
                if (!isLoading) {
                    onNext(categoriesList)
                }
            }
        }
    }
}

@Composable
fun PersonalInformationStep(
    userViewModel: UserViewModel,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onNext: (String, Int) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(0) }
    var ageText by remember { mutableStateOf("") }

    val isFormValid = fullName.isNotBlank() || age != 0

    // Track field interactions
    LaunchedEffect(fullName) {
        if (fullName.length >= 3) {
            FirebaseAnalyticsHelper.logEvent("onboarding_name_entered")
        }
    }

    LaunchedEffect(age) {
        if (age > 0) {
            FirebaseAnalyticsHelper.logEvent("onboarding_age_entered", mapOf(
                "age_range" to when {
                    age < 18 -> "under_18"
                    age in 18..24 -> "18_24"
                    age in 25..34 -> "25_34"
                    age in 35..44 -> "35_44"
                    age >= 45 -> "45_plus"
                    else -> "unknown"
                }
            ))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar with skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = Color(0xFFE91E63),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    FirebaseAnalyticsHelper.logEvent("onboarding_step_skipped", mapOf(
                        "step" to "personal_information",
                        "has_name" to if (fullName.isNotBlank()) "yes" else "no",
                        "has_age" to if (age > 0) "yes" else "no"
                    ))

                    if (!isLoading) {
                        userViewModel.updateUser(onboardingStatus = false)
                        onDismiss()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Step 2: Personal Information",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tell us a bit about yourself to Personalize your experience",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Full Name field
            Column {
                Text(
                    text = "Full Name",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "Enter your Name",
                    enabled = !isLoading
                )
            }

            // Age field
            Column {
                Text(
                    text = "Age",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = ageText,
                    onValueChange = { newAgeText ->
                        if (newAgeText.all { it.isDigit() } && newAgeText.length <= 3) {
                            ageText = newAgeText
                            age = newAgeText.toIntOrNull() ?: 0
                        }
                    },
                    placeholder = "Enter your Age",
                    keyboardType = KeyboardType.Number,
                    enabled = !isLoading
                )
            }
        }

        if (!isFormValid) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Note: Please fill in either your full name or age to continue to the next step",
                    fontSize = 12.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
        ) {
            TextButton(
                onClick = {
                    FirebaseAnalyticsHelper.logEvent("onboarding_back_clicked", mapOf(
                        "from_step" to "personal_information"
                    ))
                    onBack()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text("Back")
            }

            GradientButton(
                text = if (isLoading) "Loading..." else "Next",
                enabled = isFormValid && !isLoading
            ) {
                if (isFormValid && !isLoading) {
                    FirebaseAnalyticsHelper.logEvent("onboarding_step_next_clicked", mapOf(
                        "step" to "personal_information",
                        "has_name" to if (fullName.isNotBlank()) "yes" else "no",
                        "has_age" to if (age > 0) "yes" else "no",
                        "name_length" to fullName.length.toString()
                    ))

                    onNext(fullName, age)
                }
            }
        }
    }
}



















// Without Firebase Events
//@file:OptIn(ExperimentalLayoutApi::class)
//
//package com.cc.creatorcircle.ui.screens.onboarding
//
//
//import android.util.Log
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.ExperimentalLayoutApi
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.BasicTextField
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.CheckCircle
//import androidx.compose.material.icons.filled.Error
//import androidx.compose.material.icons.filled.Shield
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.OutlinedButton
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.window.Dialog
//import androidx.navigation.NavController
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.viewModel.UserUpdateState
//import com.cc.creatorcircle.viewModel.UserViewModel
//import kotlinx.coroutines.delay
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.SolidColor
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.draw.alpha
//
//enum class OnboardingStep {
//    CATEGORY_SELECTION,
//    PERSONAL_INFORMATION,
//    INSTAGRAM_CONNECTION
//}
//
//@Composable
//fun SignupOnboarding(navController: NavController) {
//
//
//    val context = LocalContext.current
//
//    val userViewModel = remember { UserViewModel(context) }
//
//    var showDialog by remember { mutableStateOf(true) }
//
//    if (showDialog) {
//        OnboardingFlowDialog(
//            userViewModel = userViewModel,
//            onDismiss = {
//                showDialog = false
//                navController.navigate("saboai")
//            },
//            onComplete = { categories, name, age, instagramHandle ->
//                println("Selected categories: $categories")
//                println("Personal info - Name: $name, Age: $age")
//                println("Instagram handle: $instagramHandle")
//                showDialog = false
//            }
//        )
//    }
//}
//
//@Composable
//fun OnboardingFlowDialog(
//    userViewModel: UserViewModel,
//    onDismiss: () -> Unit,
//    onComplete: (List<String>, String, Int, String) -> Unit
//) {
//    var currentStep by remember { mutableStateOf(OnboardingStep.CATEGORY_SELECTION) }
//    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
//    var fullName by remember { mutableStateOf("") }
//    var age by remember { mutableStateOf(0) }
//    var instagramHandle by remember { mutableStateOf("") }
//
//    // Observe ViewModel states using collectAsState for StateFlow
//    val isLoading by userViewModel.isLoading.collectAsState()
////    val profileUpdateLoading by userViewModel.profileUpdateLoading.collectAsState()
//    val error by userViewModel.error.collectAsState()
////    val successMessage by userViewModel.successMessage.collectAsState()
////    val updateSuccess by userViewModel.updateSuccess.collectAsState()
//
//    // Show error message if any
//    LaunchedEffect(error) {
//        if (!error.isNullOrEmpty()) {
//            // You can show a toast or snackbar here
//            println("Error: $error")
//        }
//    }
//
//
//
//    Dialog(onDismissRequest = onDismiss) {
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.9f)
//                .padding(6.dp),
//            shape = RoundedCornerShape(16.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White),
//            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//        ) {
//            when (currentStep) {
//
//                OnboardingStep.CATEGORY_SELECTION -> {
//                    CategorySelectionStep(
//                        userViewModel = userViewModel,
////      isLoading = profileUpdateLoading,
//                        onDismiss = onDismiss,
//                        onNext = { categories ->
//                            selectedCategories = categories
//                            // Update user categories with list instead of comma-separated string
//                            userViewModel.updateUser(
//                                categories = categories
//                            )
//                            currentStep = OnboardingStep.PERSONAL_INFORMATION
//                        }
//                    )
//                }
//
//                OnboardingStep.PERSONAL_INFORMATION -> {
//                    PersonalInformationStep(
//                        userViewModel = userViewModel,
//                        isLoading = isLoading,
//                        onDismiss = onDismiss,
//                        onBack = {
//                            currentStep = OnboardingStep.CATEGORY_SELECTION
//                        },
//                        onNext = { name, ageValue ->
//                            // Store the values
//                            fullName = name
//                            age = ageValue
//
//                            // Update user with the provided name and age
//                            userViewModel.updateUser(
//                                fullName = if (name.isNotBlank()) name else null,
//                                age = if (ageValue > 0) ageValue else null
//                            )
//
//                            // Navigate to next step
//                            currentStep = OnboardingStep.INSTAGRAM_CONNECTION
//                        }
//                    )
//                }
//
//                OnboardingStep.INSTAGRAM_CONNECTION -> {
//                    InstagramConnectionStep(
//                        userViewModel = userViewModel,
////                        isLoading = updateState is UserUpdateState.Loading,
//                        onDismiss = onDismiss,
//                        onBack = {
//                            currentStep = OnboardingStep.PERSONAL_INFORMATION
//                        },
//                        onComplete = {
//                        }
//                    )
//                }
//            }
//        }
//    }
//
//    // Show error snackbar if needed
//    error?.let { errorMsg ->
//        LaunchedEffect(errorMsg) {
//            // You can implement a snackbar here
//            delay(3000)
//            userViewModel.clearError()
//        }
//    }
//}
//
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun CategorySelectionStep(
//    userViewModel: UserViewModel,
//    isLoading: Boolean = false,
//    onDismiss: () -> Unit,
//    onNext: (List<String>) -> Unit
//) {
//    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Top bar with skip button
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Text(
//                text = "Skip",
//                color = Color(0xFFE91E63),
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.clickable {
//                    userViewModel.updateUser(onboardingStatus = false)
//                    if (!isLoading) onDismiss()
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Title and description
//        Text(
//            text = "Step 1: Category",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = Color.Black
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = "Select one or more categories that best reflect your content or areas of interest",
//            fontSize = 14.sp,
//            color = Color(0xFF666666),
//            lineHeight = 20.sp
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Category selection takes up available space
//        CategorySelection(
//            modifier = Modifier.weight(1f),
//            enabled = !isLoading,
//            onCategoriesSelected = { categories ->
//                selectedCategories = categories
//            }
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            GradientButton(
//                text = if (isLoading) "Saving..." else "Next",
//                enabled = !isLoading,
//            ) {
//                Log.d("Selected-categories", "CategorySelectionStep: " + selectedCategories)
//                // Convert categories to JSON array format
//                val categoriesList = selectedCategories.toList()
//                if (!isLoading) {
//                    onNext(categoriesList)
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun PersonalInformationStep(
//    userViewModel: UserViewModel,
//    isLoading: Boolean = false,
//    onDismiss: () -> Unit,
//    onBack: () -> Unit,
//    onNext: (String, Int) -> Unit
//) {
//    var fullName by remember { mutableStateOf("") }
//    var age by remember { mutableStateOf(0) }
//    var ageText by remember { mutableStateOf("") }
//
//    val isFormValid = fullName.isNotBlank() || age != 0
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//
//    ) {
//        // Top bar with skip button
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Text(
//                text = "Skip",
//                color = Color(0xFFE91E63),
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.clickable {
//                    if (!isLoading) {
//                        userViewModel.updateUser(onboardingStatus = false)
//                        onDismiss()
//                    }
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Title and description
//        Text(
//            text = "Step 2: Personal Information",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = Color.Black
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = "Tell us a bit about yourself to Personalize your experience",
//            fontSize = 14.sp,
//            color = Color(0xFF666666),
//            lineHeight = 20.sp
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Form fields
//        Column(
//            modifier = Modifier
//                .weight(1f),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Full Name field
//            Column {
//                Text(
//                    text = "Full Name",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                CustomTextField(
//                    value = fullName,
//                    onValueChange = { fullName = it },
//                    placeholder = "Enter your Name",
//                    enabled = !isLoading
//                )
//            }
//
//            // Age field
//            Column {
//                Text(
//                    text = "Age",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                CustomTextField(
//                    value = ageText,
//                    onValueChange = { newAgeText ->
//                        // Only allow numeric input and limit to 3 digits
//                        if (newAgeText.all { it.isDigit() } && newAgeText.length <= 3) {
//                            ageText = newAgeText
//                            age = newAgeText.toIntOrNull() ?: 0
//                        }
//                    },
//                    placeholder = "Enter your Age",
//                    keyboardType = KeyboardType.Number,
//                    enabled = !isLoading
//                )
//            }
//        }
//
//        // Note section
//        if (!isFormValid) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color(0xFFF5F5F5)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text(
//                    text = "Note: Please fill in either your full name or age to continue to the next step",
//                    fontSize = 12.sp,
//                    color = Color(0xFF666666),
//                    modifier = Modifier.padding(12.dp)
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Bottom buttons
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
//        ) {
//            // Back button
//            TextButton(
//                onClick = onBack,
//                enabled = !isLoading,
//                colors = ButtonDefaults.textButtonColors(
//                    contentColor = Color(0xFF666666)
//                )
//            ) {
//                Text("Back")
//            }
//
//            // Next button - Only calls onNext
//            GradientButton(
//                text = if (isLoading) "Loading..." else "Next",
//                enabled = isFormValid && !isLoading
//            ) {
//                if (isFormValid && !isLoading) {
//                    onNext(fullName, age)
//                }
//            }
//        }
//    }
//}































//@Composable
//fun InstagramConnectionStep(
//    userViewModel: UserViewModel,
//    isLoading: Boolean = false,
//    onDismiss: () -> Unit,
//    onBack: () -> Unit,
//    onComplete: () -> Unit // Changed from onComplete to onNext for consistency
//) {
//    var instagramHandle by remember { mutableStateOf("") }
//    var validationError by remember { mutableStateOf<String?>(null) }
//    var hasUserInteracted by remember { mutableStateOf(false) }
//
//    // Observe the update state from UserViewModel
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // Validate Instagram handle
//    val isValidHandle = instagramHandle.isNotBlank() &&
//            instagramHandle.matches(Regex("^[a-zA-Z0-9._]{1,30}$")) &&
//            !instagramHandle.startsWith(".") &&
//            !instagramHandle.endsWith(".")
//
//    // Update validation error based on input
//    LaunchedEffect(instagramHandle, hasUserInteracted) {
//        if (hasUserInteracted) {
//            validationError = when {
//                instagramHandle.isBlank() -> null // Allow empty for optional field
//                !instagramHandle.matches(Regex("^[a-zA-Z0-9._]{1,30}$")) ->
//                    "Handle can only contain letters, numbers, periods, and underscores"
//
//                instagramHandle.startsWith(".") || instagramHandle.endsWith(".") ->
//                    "Handle cannot start or end with a period"
//
//                instagramHandle.length > 30 -> "Handle must be 30 characters or less"
//                else -> null
//            }
//        }
//    }
//
//    // Handle update state changes
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UserUpdateState.Success -> {
//                // Update successful, proceed to next step
////                onNext(instagramHandle)
//                userViewModel.resetUpdateState() // Reset state for next use
//            }
//
//            is UserUpdateState.Error -> {
//                // Handle error - you might want to show an error message
////                Log.e("InstagramStep", "Failed to update Instagram: ${updateState.message}")
//            }
//
//            else -> { /* Do nothing for Idle and Loading states */
//            }
//        }
//    }
//
//    // Determine if we're currently loading (either from prop or update state)
//    val isCurrentlyLoading = isLoading || updateState is UserUpdateState.Loading
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp)
//    ) {
//        // Top bar with skip button
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.End
//        ) {
//            Text(
//                text = "Skip",
//                color = Color(0xFFE91E63),
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.clickable {
//                    if (!isCurrentlyLoading) {
//                        // Skip Instagram connection but still update onboarding status
//                        userViewModel.updateUser(onboardingStatus = true)
////                        onNext("") // Pass empty string to indicate skipped
//                        onDismiss()
//                    }
//                }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Title and description
//        Text(
//            text = "Step 3: Connect your Instagram",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = Color.Black
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        Text(
//            text = "Link Your Instagram account to enhance your profile and connect with others",
//            fontSize = 14.sp,
//            color = Color(0xFF666666),
//            lineHeight = 20.sp
//        )
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // Instagram handle input
//        Column {
//            Text(
//                text = "Instagram Handle",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Input field with enhanced styling
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color.White
//                ),
//                elevation = CardDefaults.cardElevation(
//                    defaultElevation = if (validationError != null) 0.dp else 1.dp
//                ),
//                border = BorderStroke(
//                    width = 1.5.dp,
//                    color = when {
//                        validationError != null -> Color(0xFFE53E3E)
//                        instagramHandle.isNotEmpty() && (isValidHandle || instagramHandle.isEmpty()) -> Color(
//                            0xFF38A169
//                        )
//
//                        instagramHandle.isNotEmpty() -> Color(0xFFE91E63)
//                        else -> Color(0xFFE2E8F0)
//                    }
//                ),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 16.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    // Instagram icon with better styling
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_instagram),
//                        contentDescription = "Instagram",
//                        modifier = Modifier.size(20.dp)
//                    )
//
//                    // @ symbol
//                    Text(
//                        text = "@",
//                        fontSize = 16.sp,
//                        color = Color(0xFF666666),
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    // Text field
//                    BasicTextField(
//                        value = instagramHandle,
//                        onValueChange = { newValue ->
//                            if (newValue.length <= 30) {
//                                instagramHandle = newValue.lowercase()
//                                hasUserInteracted = true
//                            }
//                        },
//                        modifier = Modifier.weight(1f),
//                        enabled = !isCurrentlyLoading,
//                        textStyle = TextStyle(
//                            fontSize = 16.sp,
//                            color = Color.Black,
//                            fontWeight = FontWeight.Medium
//                        ),
//                        singleLine = true,
//                        keyboardOptions = KeyboardOptions(
//                            keyboardType = KeyboardType.Text,
//                            imeAction = ImeAction.Done
//                        ),
//                        decorationBox = { innerTextField ->
//                            Box(
//                                modifier = Modifier.fillMaxWidth(),
//                                contentAlignment = Alignment.CenterStart
//                            ) {
//                                if (instagramHandle.isEmpty()) {
//                                    Text(
//                                        text = "your_username",
//                                        color = Color(0xFFA0AEC0),
//                                        fontSize = 16.sp
//                                    )
//                                }
//                                innerTextField()
//                            }
//                        }
//                    )
//
//                    // Validation indicator
//                    if (instagramHandle.isNotEmpty()) {
//                        Icon(
//                            imageVector = if (isValidHandle || instagramHandle.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Error,
//                            contentDescription = if (isValidHandle || instagramHandle.isEmpty()) "Valid" else "Invalid",
//                            modifier = Modifier.size(20.dp),
//                            tint = if (isValidHandle || instagramHandle.isEmpty()) Color(0xFF38A169) else Color(
//                                0xFFE53E3E
//                            )
//                        )
//                    }
//                }
//            }
//
//            // Error message
//            AnimatedVisibility(
//                visible = validationError != null,
//                enter = slideInVertically() + fadeIn(),
//                exit = slideOutVertically() + fadeOut()
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(top = 8.dp, start = 4.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(6.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Error,
//                        contentDescription = "Error",
//                        modifier = Modifier.size(14.dp),
//                        tint = Color(0xFFE53E3E)
//                    )
//                    Text(
//                        text = validationError ?: "",
//                        fontSize = 12.sp,
//                        color = Color(0xFFE53E3E),
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Privacy information with better styling
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = Color(0xFFF8FAFC)
//            ),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Row(
//                modifier = Modifier.padding(12.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Shield,
//                    contentDescription = "Privacy",
//                    modifier = Modifier.size(16.dp),
//                    tint = Color(0xFF4299E1)
//                )
//                Text(
//                    text = "Your Instagram data is secure and will only be used to enhance your experience on the platform",
//                    fontSize = 12.sp,
//                    color = Color(0xFF4A5568),
//                    lineHeight = 16.sp
//                )
//            }
//        }
//
//        // Show error message if update failed
//        AnimatedVisibility(
//            visible = updateState is UserUpdateState.Error,
//            enter = slideInVertically() + fadeIn(),
//            exit = slideOutVertically() + fadeOut()
//        ) {
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 16.dp),
//                colors = CardDefaults.cardColors(
//                    containerColor = Color(0xFFFED7D7)
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Row(
//                    modifier = Modifier.padding(12.dp),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Error,
//                        contentDescription = "Error",
//                        modifier = Modifier.size(16.dp),
//                        tint = Color(0xFFE53E3E)
//                    )
//                    Text(
//                        text = (updateState as? UserUpdateState.Error)?.message
//                            ?: "Failed to update Instagram information",
//                        fontSize = 12.sp,
//                        color = Color(0xFFE53E3E),
//                        lineHeight = 16.sp
//                    )
//                }
//            }
//        }
//
//        // This Spacer will push the bottom buttons to the bottom
//        Spacer(modifier = Modifier.weight(1f))
//
//        // Bottom buttons - now positioned at the bottom
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Back button (outlined style)
//            OutlinedButton(
//                onClick = onBack,
//                enabled = !isCurrentlyLoading,
//                colors = ButtonDefaults.outlinedButtonColors(
//                    contentColor = Color(0xFF666666)
//                ),
//                border = ButtonDefaults.outlinedButtonBorder.copy(
//                    width = 1.dp
//                ),
//                shape = RoundedCornerShape(8.dp)
//            ) {
//                Text("Back")
//            }
//
//            // Next button (filled style) - Changed from "Finish" to "Next"
//            GradientButton(
//                text = when {
//                    isCurrentlyLoading -> "Saving..."
//                    else -> "Finish"
//                },
//                enabled = !isCurrentlyLoading && (instagramHandle.isEmpty() || isValidHandle)
//            ) {
//                if (!isCurrentlyLoading && (instagramHandle.isEmpty() || isValidHandle)) {
//                    if (instagramHandle.isNotEmpty()) {
//                        // Update Instagram link using the convenience method
//                        val instagramUrl = "https://www.instagram.com/$instagramHandle/"
//                        userViewModel.updateSocialMediaLinks(instagramLink = instagramUrl)
//                    } else {
//                        // Skip Instagram but mark onboarding as complete
//                        userViewModel.updateUser(onboardingStatus = true)
////                        onNext("") // Pass empty string for skipped
//                    }
//                }
//
//
//
//                onDismiss()
//
//
//            }
//        }
//    }
//}







@Composable
fun InstagramConnectionStep(
    userViewModel: UserViewModel,
    isLoading: Boolean = false,
    onDismiss: () -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit // Changed from onComplete to onNext for consistency
) {
    var instagramInput by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var hasUserInteracted by remember { mutableStateOf(false) }

    // Observe the update state from UserViewModel
    val updateState by userViewModel.updateState.collectAsState()

    // Helper function to extract handle from URL or return the input as handle
    fun extractInstagramHandle(input: String): String {
        val trimmedInput = input.trim()

        // Check if it's a URL
        val urlPattern = Regex("(?:https?://)(?:www\\.)?instagram\\.com/([a-zA-Z0-9._]{1,30})/?(?:\\?.*)?")
        val urlMatch = urlPattern.find(trimmedInput)

        if (urlMatch != null) {
            return urlMatch.groupValues[1] // Extract handle from URL
        }

        // If not a URL, treat as handle (remove @ if present)
        return trimmedInput.removePrefix("@")
    }

    // Validate Instagram input (handle or URL)
    val isValidInput = if (instagramInput.isNotBlank()) {
        val extractedHandle = extractInstagramHandle(instagramInput)
        extractedHandle.matches(Regex("^[a-zA-Z0-9._]{1,30}$")) &&
                !extractedHandle.startsWith(".") &&
                !extractedHandle.endsWith(".")
    } else {
        true // Empty is valid (optional field)
    }

    // Update validation error based on input
    LaunchedEffect(instagramInput, hasUserInteracted) {
        if (hasUserInteracted && instagramInput.isNotBlank()) {
            val trimmedInput = instagramInput.trim()

            // Check if it looks like a URL
            if (trimmedInput.contains("instagram.com") || trimmedInput.startsWith("http")) {
                val urlPattern = Regex("^https?://(?:www\\.)?instagram\\.com/([a-zA-Z0-9._]{1,30})/?(?:\\?.*)?$")
                if (!urlPattern.matches(trimmedInput)) {
                    validationError = "Invalid Instagram URL format. Use: https://www.instagram.com/username/"
                } else {
                    val handle = extractInstagramHandle(trimmedInput)
                    validationError = when {
                        !handle.matches(Regex("^[a-zA-Z0-9._]{1,30}$")) ->
                            "Username can only contain letters, numbers, periods, and underscores"
                        handle.startsWith(".") || handle.endsWith(".") ->
                            "Username cannot start or end with a period"
                        handle.length > 30 -> "Username must be 30 characters or less"
                        else -> null
                    }
                }
            } else {
                // Treat as handle
                val handle = trimmedInput.removePrefix("@")
                validationError = when {
                    !handle.matches(Regex("^[a-zA-Z0-9._]{1,30}$")) ->
                        "Handle can only contain letters, numbers, periods, and underscores"
                    handle.startsWith(".") || handle.endsWith(".") ->
                        "Handle cannot start or end with a period"
                    handle.length > 30 -> "Handle must be 30 characters or less"
                    else -> null
                }
            }
        } else if (hasUserInteracted && instagramInput.isBlank()) {
            validationError = null
        }
    }

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                // Update successful, proceed to next step
                userViewModel.resetUpdateState() // Reset state for next use
            }

            is UserUpdateState.Error -> {
                // Handle error - you might want to show an error message
            }

            else -> { /* Do nothing for Idle and Loading states */
            }
        }
    }

    // Determine if we're currently loading (either from prop or update state)
    val isCurrentlyLoading = isLoading || updateState is UserUpdateState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar with skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                color = Color(0xFFE91E63),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    if (!isCurrentlyLoading) {
                        // Skip Instagram connection but still update onboarding status
                        userViewModel.updateUser(onboardingStatus = false)
                        onDismiss()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title and description
        Text(
            text = "Step 3: Connect your Instagram",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Link Your Instagram account to enhance your profile and connect with others",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Instagram input field
        Column {
            Text(
                text = "Instagram Handle or URL",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Helper text
            Text(
                text = "Enter your username (e.g., john_doe) or complete Instagram URL",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Input field with enhanced styling
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (validationError != null) 0.dp else 1.dp
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = when {
                        validationError != null -> Color(0xFFE53E3E)
                        instagramInput.isNotEmpty() && isValidInput -> Color(0xFF38A169)
                        instagramInput.isNotEmpty() -> Color(0xFFE91E63)
                        else -> Color(0xFFE2E8F0)
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Instagram icon with better styling
                    Image(
                        painter = painterResource(id = R.drawable.ic_instagram),
                        contentDescription = "Instagram",
                        modifier = Modifier.size(20.dp)
                    )

                    // Text field
                    BasicTextField(
                        value = instagramInput,
                        onValueChange = { newValue ->
                            // Allow longer input for URLs but limit handles
                            if (newValue.contains("instagram.com") || newValue.length <= 100) {
                                instagramInput = newValue.trim()
                                hasUserInteracted = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isCurrentlyLoading,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (instagramInput.isEmpty()) {
                                    Text(
                                        text = "username or URL",
                                        color = Color(0xFFA0AEC0),
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Validation indicator
                    if (instagramInput.isNotEmpty()) {
                        Icon(
                            imageVector = if (isValidInput) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = if (isValidInput) "Valid" else "Invalid",
                            modifier = Modifier.size(20.dp),
                            tint = if (isValidInput) Color(0xFF38A169) else Color(0xFFE53E3E)
                        )
                    }
                }
            }

            // Error message
            AnimatedVisibility(
                visible = validationError != null,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFE53E3E)
                    )
                    Text(
                        text = validationError ?: "",
                        fontSize = 12.sp,
                        color = Color(0xFFE53E3E),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Show extracted handle if URL was entered
            if (instagramInput.contains("instagram.com") && isValidInput) {
                val extractedHandle = extractInstagramHandle(instagramInput)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0F8F0)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF38A169)
                        )
                        Text(
                            text = "Username detected: @$extractedHandle",
                            fontSize = 12.sp,
                            color = Color(0xFF38A169),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy information with better styling
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8FAFC)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Privacy",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF4299E1)
                )
                Text(
                    text = "Your Instagram data is secure and will only be used to enhance your experience on the platform",
                    fontSize = 12.sp,
                    color = Color(0xFF4A5568),
                    lineHeight = 16.sp
                )
            }
        }

        // Show error message if update failed
        AnimatedVisibility(
            visible = updateState is UserUpdateState.Error,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFED7D7)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFE53E3E)
                    )
                    Text(
                        text = (updateState as? UserUpdateState.Error)?.message
                            ?: "Failed to update Instagram information",
                        fontSize = 12.sp,
                        color = Color(0xFFE53E3E),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // This Spacer will push the bottom buttons to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Bottom buttons - now positioned at the bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button (outlined style)
            OutlinedButton(
                onClick = onBack,
                enabled = !isCurrentlyLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF666666)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Back")
            }

            // Next button (filled style) - Changed from "Finish" to "Next"
            GradientButton(
                text = when {
                    isCurrentlyLoading -> "Saving..."
                    else -> "Finish"
                },
                enabled = !isCurrentlyLoading && (instagramInput.isEmpty() || isValidInput)
            ) {
                if (!isCurrentlyLoading && (instagramInput.isEmpty() || isValidInput)) {
                    if (instagramInput.isNotEmpty()) {
                        val extractedHandle = extractInstagramHandle(instagramInput)
                        // Always create a consistent URL format
                        val instagramUrl = "https://www.instagram.com/$extractedHandle/"
                        userViewModel.updateSocialMediaLinks(instagramLink = instagramUrl)
//                        userViewModel.updateUser(onboardingStatus = false)
                    } else {
                        // Skip Instagram but mark onboarding as complete
                        userViewModel.updateUser(onboardingStatus = false)
                    }
                }
                onDismiss()
            }
        }
    }
}











@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = if (enabled) Color.Transparent else Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF893BCF), Color(0xFFEA3BA1))
                ),
                shape = RoundedCornerShape(12.dp)
            )
//            .border(
//                width = 1.dp,
//                color = if (enabled) {
//                    if (value.isNotEmpty()) Color(0xFFE91E63) else Color(0xFFE0E0E0)
//                } else {
//                    Color(0xFFE0E0E0)
//                },
//                shape = RoundedCornerShape(8.dp)
//            )
            .padding(horizontal = 12.dp),
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = if (enabled) Color.Black else Color(0xFF999999)
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun CategorySelection(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCategoriesSelected: (Set<String>) -> Unit
) {
    var selectedCategories by remember {
        mutableStateOf(setOf<String>())
    }

    var customCategory by remember { mutableStateOf("") }

    // Notify parent when categories change
    LaunchedEffect(selectedCategories) {
        onCategoriesSelected(selectedCategories)
    }

    val allCategories = listOf(
        "Fun", "Travel", "Motivation", "Food",
        "Technology", "Fashion", "Fitness", "Education",
        "Entertainment", "Pets", "DIY", "Health",
        "Beauty", "Art", "Music", "Gaming",
        "Sports", "Comedy", "News", "Science",
        "Photography", "Parenting", "Cooking", "Finance",
        "Lifestyle", "Relationships", "Automotive", "Books",
        "History", "Nature", "Productivity", "Marketing",
        "Business", "Career", "Adventure", "Spirituality",
        "Home Decor", "Sustainability", "Real Estate", "Culture",
        "Personal development", "Languages", "Hobbies"
    )

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Available category label
        Text(
            text = "Available category",
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable content with grid layout
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Create rows of 3 items each for better fit
            allCategories.chunked(3).forEach { rowCategories ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowCategories.forEach { category ->
                        CategoryChip(
                            text = category,
                            isSelected = category in selectedCategories,
                            enabled = enabled,
                            onToggle = {
                                if (enabled) {
                                    selectedCategories = if (category in selectedCategories) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining spaces in the row if needed
                    repeat(3 - rowCategories.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Custom category section
            CustomCategorySection(
                customCategory = customCategory,
                enabled = enabled,
                onCustomCategoryChange = { customCategory = it },
                onAddCategory = {
                    if (enabled && customCategory.isNotBlank() && customCategory !in selectedCategories) {
                        selectedCategories = selectedCategories + customCategory.trim()
                        customCategory = ""
                    }
                }
            )
        }
    }
}

@Composable
fun CustomCategorySection(
    customCategory: String,
    enabled: Boolean = true,
    onCustomCategoryChange: (String) -> Unit,
    onAddCategory: () -> Unit
) {
    Column {
        Text(
            text = "or create your own",
            fontSize = 16.sp,
            color = Color.Black,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = customCategory,
                onValueChange = onCustomCategoryChange,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(
                        color = if (enabled) Color.Transparent else Color(0xFFF5F5F5),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (enabled) {
                            if (customCategory.isNotEmpty()) Color(0xFFE91E63) else Color(0xFFE0E0E0)
                        } else {
                            Color(0xFFE0E0E0)
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = if (enabled) Color.Black else Color(0xFF999999)
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (customCategory.isEmpty()) {
                            Text(
                                text = "Enter category",
                                color = Color(0xFF999999),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            GradientButton(
                text = "Add",
                enabled = enabled && customCategory.isNotBlank(),
                modifier = Modifier.padding(0.dp)
            ) {
                if (enabled && customCategory.isNotBlank()) {
                    onAddCategory()
                }
            }
        }
    }
}





@Composable
fun CategoryChip(
    text: String,
    isSelected: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {

    val gradientColors: List<Color> = listOf(Color(0xFF893BCF), Color(0xFFEA3BA1))

    val backgroundBrush = if (isSelected) {
        Brush.horizontalGradient(gradientColors)
    } else {
        Brush.horizontalGradient(listOf(Color.White, Color.White))
    }

    val textColor = if (isSelected) Color.White else Color(0xFF333333)
    val borderColor = if (isSelected) {
        Brush.horizontalGradient(listOf(Color(0xFF893BCF), Color(0xFFEA3BA1)))
    } else {
        SolidColor(Color(0xFFE0E0E0))
    }
    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .background(
                brush = backgroundBrush,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                brush = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .alpha(alpha)
            .clickable(enabled = enabled) { onToggle() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}






//
//@Composable
//fun CategoryChip(
//    text: String,
//    isSelected: Boolean,
//    enabled: Boolean = true,
//    onToggle: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//
//    val gradientColors: List<Color> = listOf(Color(0xFF893BCF), Color(0xFFEA3BA1))
//
//    val backgroundBrush = if (isSelected) {
//        Brush.horizontalGradient(gradientColors)
//    } else {
//        Brush.horizontalGradient(listOf(Color.White, Color.White))
//    }
//
//    val textColor = if (isSelected) Color.White else Color(0xFF333333)
//    val borderColor = if (isSelected) listOf(Color(0xFF893BCF), Color(0xFFEA3BA1)) else Color(0xFFE0E0E0)
//    val alpha = if (enabled) 1f else 0.5f
//
//    Box(
//        modifier = modifier
//            .background(
//                brush = backgroundBrush,
//                shape = RoundedCornerShape(8.dp)
//            )
//            .border(1.dp, borderColor.copy(alpha = alpha), RoundedCornerShape(8.dp))
//            .clickable(enabled = enabled) { onToggle() }
//            .padding(horizontal = 12.dp, vertical = 8.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = textColor.copy(alpha = alpha),
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Normal,
//            textAlign = TextAlign.Center,
//            maxLines = 1
//        )
//    }
//}











//
//@Composable
//fun CategoryChip(
//    text: String,
//    isSelected: Boolean,
//    enabled: Boolean = true,
//    onToggle: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//
//    val gradientColors: List<Color> = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
//    val normalColor: List<Color> = listOf(Color.White, Color.White)
//
//
//    val backgroundColor = if (isSelected) Brush.horizontalGradient(gradientColors) else Color.White
//    val textColor = if (isSelected) Color.White else Color(0xFF333333)
//    val borderColor = if (isSelected) Color(0xFFE91E63) else Color(0xFFE0E0E0)
//    val alpha = if (enabled) 1f else 0.5f
//
//    Box(
//        modifier = modifier
////            .background(
////                color = backgroundColor.copy(alpha = alpha),
////                shape = RoundedCornerShape(8.dp)
////            )
//            .background(
//                brush = Brush.horizontalGradient(backgroundColor),
//                shape = RoundedCornerShape(8.dp)
//            )
//            .border(1.dp, borderColor.copy(alpha = alpha), RoundedCornerShape(8.dp))
//            .clickable(enabled = enabled) { onToggle() }
//            .padding(horizontal = 12.dp, vertical = 8.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = textColor.copy(alpha = alpha),
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Normal,
//            textAlign = TextAlign.Center,
//            maxLines = 1
//        )
//    }
//}


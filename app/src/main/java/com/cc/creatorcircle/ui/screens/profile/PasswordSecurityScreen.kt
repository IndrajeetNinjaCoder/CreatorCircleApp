package com.cc.creatorcircle.ui.screens.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.components.GradientButton
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserUpdateState

@Composable
fun PasswordSecurityScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(context) }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("PasswordSecurityScreen", "PasswordSecurityScreen")
        FirebaseAnalyticsHelper.logEvent("password_change_screen_opened")
    }

    // State for password fields
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // State for validation errors
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    // Collect update state
    val updateState by userViewModel.updateState.collectAsState()

    // Handle update state changes
    LaunchedEffect(updateState) {
        when (updateState) {
            is UserUpdateState.Success -> {
                Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()

                // Track successful password change
                FirebaseAnalyticsHelper.logFeatureUsed("password_changed_successfully")
                FirebaseAnalyticsHelper.logEvent("password_change_success")

                userViewModel.resetUpdateState()
                onDismiss()
            }
            is UserUpdateState.Error -> {
                val errorMessage = (updateState as UserUpdateState.Error).message
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()

                // Track password change error
                FirebaseAnalyticsHelper.logEvent(
                    "password_change_error",
                    mapOf("error" to errorMessage)
                )

                userViewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    // Password validation function
    fun validatePassword(password: String): String? {
        return when {
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { it.isLetter() } -> "Password must contain at least one letter"
            !password.any { "!@#$%^&*()".contains(it) } -> "Password must contain at least one special character (!@#$%^&*())"
            else -> null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF8F8))
    ) {
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
                            FirebaseAnalyticsHelper.logFeatureUsed("password_security_back_button")
                            FirebaseAnalyticsHelper.logEvent(
                                "password_change_cancelled",
                                mapOf("reason" to "back_button")
                            )
                            onDismiss()
                        },
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Change Password",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Password Requirements Text
                Text(
                    text = "Your password must be at least 6 characters and should include a combination of numbers, letters and special characters (!$@%).",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // New Password Section
                Text(
                    text = "New Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        passwordError = null

                        // Track password input started
                        if (it.isNotEmpty() && newPassword.isEmpty()) {
                            FirebaseAnalyticsHelper.logEvent("new_password_input_started")
                        }
                    },
                    placeholder = {
                        Text(
                            "Enter new password",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        errorBorderColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (newPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            newPasswordVisible = !newPasswordVisible

                            // Track password visibility toggle
                            FirebaseAnalyticsHelper.logFeatureUsed(
                                if (newPasswordVisible) "new_password_shown" else "new_password_hidden"
                            )
                            FirebaseAnalyticsHelper.logEvent(
                                "password_visibility_toggled",
                                mapOf(
                                    "field" to "new_password",
                                    "visible" to newPasswordVisible.toString()
                                )
                            )
                        }) {
                            Icon(
                                imageVector = if (newPasswordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = if (newPasswordVisible)
                                    "Hide password"
                                else
                                    "Show password",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    },
                    isError = passwordError != null,
                    singleLine = true
                )

                // Password error message
                if (passwordError != null) {
                    Text(
                        text = passwordError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )

                    // Track validation error
                    LaunchedEffect(passwordError) {
                        FirebaseAnalyticsHelper.logEvent(
                            "password_validation_error",
                            mapOf("error_type" to passwordError!!)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Confirm Password Section
                Text(
                    text = "Confirm Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmError = null

                        // Track confirm password input started
                        if (it.isNotEmpty() && confirmPassword.isEmpty()) {
                            FirebaseAnalyticsHelper.logEvent("confirm_password_input_started")
                        }
                    },
                    placeholder = {
                        Text(
                            "Confirm new password",
                            color = Color(0xFFBDBDBD),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF8B5CF6),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        errorBorderColor = Color.Red
                    ),
                    shape = RoundedCornerShape(8.dp),
                    visualTransformation = if (confirmPasswordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = {
                            confirmPasswordVisible = !confirmPasswordVisible

                            // Track password visibility toggle
                            FirebaseAnalyticsHelper.logFeatureUsed(
                                if (confirmPasswordVisible) "confirm_password_shown" else "confirm_password_hidden"
                            )
                            FirebaseAnalyticsHelper.logEvent(
                                "password_visibility_toggled",
                                mapOf(
                                    "field" to "confirm_password",
                                    "visible" to confirmPasswordVisible.toString()
                                )
                            )
                        }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible)
                                    Icons.Filled.Visibility
                                else
                                    Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible)
                                    "Hide password"
                                else
                                    "Show password",
                                tint = Color(0xFF9E9E9E)
                            )
                        }
                    },
                    isError = confirmError != null,
                    singleLine = true
                )

                // Confirm password error message
                if (confirmError != null) {
                    Text(
                        text = confirmError!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )

                    // Track confirm password error
                    LaunchedEffect(confirmError) {
                        FirebaseAnalyticsHelper.logEvent(
                            "confirm_password_error",
                            mapOf("error_type" to confirmError!!)
                        )
                    }
                }
            }

            // Change Password Button (Fixed at bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
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
                        text = "Change password",
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Track change password button click
                        FirebaseAnalyticsHelper.logFeatureUsed("change_password_button_clicked")

                        // Validate inputs
                        var isValid = true
                        val validationErrors = mutableListOf<String>()

                        // Validate new password
                        val passwordValidationError = validatePassword(newPassword)
                        if (passwordValidationError != null) {
                            passwordError = passwordValidationError
                            validationErrors.add(passwordValidationError)
                            isValid = false
                        } else if (newPassword.isBlank()) {
                            passwordError = "Please enter a new password"
                            validationErrors.add("empty_new_password")
                            isValid = false
                        }

                        // Validate confirm password
                        if (confirmPassword.isBlank()) {
                            confirmError = "Please confirm your password"
                            validationErrors.add("empty_confirm_password")
                            isValid = false
                        } else if (newPassword != confirmPassword) {
                            confirmError = "Passwords do not match"
                            validationErrors.add("passwords_mismatch")
                            isValid = false
                        }

                        // Track validation result
                        if (!isValid) {
                            FirebaseAnalyticsHelper.logEvent(
                                "password_change_validation_failed",
                                mapOf("errors" to validationErrors.joinToString(", "))
                            )
                        }

                        // If all validations pass, update password
                        if (isValid) {
                            Log.d("PasswordSecurityScreen", "Changing password")

                            // Track password change initiation
                            FirebaseAnalyticsHelper.logEvent("password_change_initiated")
                            FirebaseAnalyticsHelper.logEvent(
                                "password_change_submitted",
                                mapOf(
                                    "password_length" to newPassword.length.toString(),
                                    "has_numbers" to newPassword.any { it.isDigit() }.toString(),
                                    "has_special_chars" to newPassword.any { "!@#$%^&*()".contains(it) }.toString()
                                )
                            )

                            userViewModel.updateUser(
                                password = newPassword
                            )
                        }
                    }
                }
            }
        }
    }
}



















//
//
//package com.cc.creatorcircle.ui.screens.profile
//
//import android.util.Log
//import android.widget.Toast
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.ui.components.GradientButton
//import com.cc.creatorcircle.viewModel.UserViewModel
//import com.cc.creatorcircle.viewModel.UserUpdateState
//
//@Composable
//fun PasswordSecurityScreen(
//    navController: NavController,
//    onDismiss: () -> Unit
//) {
//    val context = LocalContext.current
//    val userViewModel = remember { UserViewModel(context) }
//
//    // State for password fields
//    var newPassword by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var newPasswordVisible by remember { mutableStateOf(false) }
//    var confirmPasswordVisible by remember { mutableStateOf(false) }
//
//    // State for validation errors
//    var passwordError by remember { mutableStateOf<String?>(null) }
//    var confirmError by remember { mutableStateOf<String?>(null) }
//
//    // Collect update state
//    val updateState by userViewModel.updateState.collectAsState()
//
//    // Handle update state changes
//    LaunchedEffect(updateState) {
//        when (updateState) {
//            is UserUpdateState.Success -> {
//                Toast.makeText(context, "Password changed successfully!", Toast.LENGTH_SHORT).show()
//                userViewModel.resetUpdateState()
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
//    // Password validation function
//    fun validatePassword(password: String): String? {
//        return when {
//            password.length < 6 -> "Password must be at least 6 characters"
//            !password.any { it.isDigit() } -> "Password must contain at least one number"
//            !password.any { it.isLetter() } -> "Password must contain at least one letter"
//            !password.any { "!@#$%^&*()".contains(it) } -> "Password must contain at least one special character (!@#$%^&*())"
//            else -> null
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFAF8F8))
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Top Bar with Back Arrow
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp, vertical = 16.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_left_arrow),
//                    contentDescription = "Back",
//                    modifier = Modifier
//                        .size(26.dp)
//                        .clickable { onDismiss() },
//                    tint = Color.Black
//                )
//                Spacer(modifier = Modifier.width(16.dp))
//                Text(
//                    text = "Change Password",
//                    fontSize = 22.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color.Black
//                )
//            }
//
//            // Content
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .padding(horizontal = 24.dp)
//            ) {
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Password Requirements Text
//                Text(
//                    text = "Your password must be at least 6 characters and should include a combination of numbers, letters and special characters (!$@%).",
//                    fontSize = 14.sp,
//                    color = Color(0xFF666666),
//                    lineHeight = 20.sp,
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(32.dp))
//
//                // New Password Section
//                Text(
//                    text = "New Password",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFF333333),
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                OutlinedTextField(
//                    value = newPassword,
//                    onValueChange = {
//                        newPassword = it
//                        passwordError = null
//                    },
//                    placeholder = {
//                        Text(
//                            "Enter new password",
//                            color = Color(0xFFBDBDBD),
//                            fontSize = 14.sp
//                        )
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color(0xFFE0E0E0),
//                        focusedBorderColor = Color(0xFF8B5CF6),
//                        unfocusedContainerColor = Color.White,
//                        focusedContainerColor = Color.White,
//                        errorBorderColor = Color.Red
//                    ),
//                    shape = RoundedCornerShape(8.dp),
//                    visualTransformation = if (newPasswordVisible)
//                        VisualTransformation.None
//                    else
//                        PasswordVisualTransformation(),
//                    trailingIcon = {
//                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
//                            Icon(
//                                imageVector = if (newPasswordVisible)
//                                    Icons.Filled.Visibility
//                                else
//                                    Icons.Filled.VisibilityOff,
//                                contentDescription = if (newPasswordVisible)
//                                    "Hide password"
//                                else
//                                    "Show password",
//                                tint = Color(0xFF9E9E9E)
//                            )
//                        }
//                    },
//                    isError = passwordError != null,
//                    singleLine = true
//                )
//
//                // Password error message
//                if (passwordError != null) {
//                    Text(
//                        text = passwordError!!,
//                        color = Color.Red,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Confirm Password Section
//                Text(
//                    text = "Confirm Password",
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFF333333),
//                    modifier = Modifier.fillMaxWidth()
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                OutlinedTextField(
//                    value = confirmPassword,
//                    onValueChange = {
//                        confirmPassword = it
//                        confirmError = null
//                    },
//                    placeholder = {
//                        Text(
//                            "Confirm new password",
//                            color = Color(0xFFBDBDBD),
//                            fontSize = 14.sp
//                        )
//                    },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color(0xFFE0E0E0),
//                        focusedBorderColor = Color(0xFF8B5CF6),
//                        unfocusedContainerColor = Color.White,
//                        focusedContainerColor = Color.White,
//                        errorBorderColor = Color.Red
//                    ),
//                    shape = RoundedCornerShape(8.dp),
//                    visualTransformation = if (confirmPasswordVisible)
//                        VisualTransformation.None
//                    else
//                        PasswordVisualTransformation(),
//                    trailingIcon = {
////                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
////                            Icon(
////                                painter = painterResource(
////                                    id = if (confirmPasswordVisible)
////                                        Icons.Filled.VisibilityOff
////                                    else
////                                        Icons.Filled.visibility
////                                ),
////                                contentDescription = if (confirmPasswordVisible)
////                                    "Hide password"
////                                else
////                                    "Show password",
////                                tint = Color(0xFF9E9E9E)
////                            )
////                        }
//
//                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
//                            Icon(
//                                imageVector = if (confirmPasswordVisible)
//                                    Icons.Filled.Visibility
//                                else
//                                    Icons.Filled.VisibilityOff,
//                                contentDescription = if (confirmPasswordVisible)
//                                    "Hide password"
//                                else
//                                    "Show password",
//                                tint = Color(0xFF9E9E9E)
//                            )
//                        }
//                    },
//                    isError = confirmError != null,
//                    singleLine = true
//                )
//
//                // Confirm password error message
//                if (confirmError != null) {
//                    Text(
//                        text = confirmError!!,
//                        color = Color.Red,
//                        fontSize = 12.sp,
//                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
//                    )
//                }
//            }
//
//            // Change Password Button (Fixed at bottom)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 24.dp, vertical = 16.dp)
//            ) {
//                if (updateState is UserUpdateState.Loading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .size(40.dp),
//                        color = Color(0xFFB388FF)
//                    )
//                } else {
//                    GradientButton(
//                        text = "Change password",
//                        modifier = Modifier.fillMaxWidth()
//                    ) {
//                        // Validate inputs
//                        var isValid = true
//
//                        // Validate new password
//                        val passwordValidationError = validatePassword(newPassword)
//                        if (passwordValidationError != null) {
//                            passwordError = passwordValidationError
//                            isValid = false
//                        } else if (newPassword.isBlank()) {
//                            passwordError = "Please enter a new password"
//                            isValid = false
//                        }
//
//                        // Validate confirm password
//                        if (confirmPassword.isBlank()) {
//                            confirmError = "Please confirm your password"
//                            isValid = false
//                        } else if (newPassword != confirmPassword) {
//                            confirmError = "Passwords do not match"
//                            isValid = false
//                        }
//
//                        // If all validations pass, update password
//                        if (isValid) {
//                            Log.d("PasswordSecurityScreen", "Changing password")
//                            userViewModel.updateUser(
//                                password = newPassword
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
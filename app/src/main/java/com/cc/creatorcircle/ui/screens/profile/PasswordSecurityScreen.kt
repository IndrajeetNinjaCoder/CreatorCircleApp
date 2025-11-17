

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
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserUpdateState

@Composable
fun PasswordSecurityScreen(
    navController: NavController,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userViewModel = remember { UserViewModel(context) }

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
                userViewModel.resetUpdateState()
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
                        .clickable { onDismiss() },
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
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
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
//                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
//                            Icon(
//                                painter = painterResource(
//                                    id = if (confirmPasswordVisible)
//                                        Icons.Filled.VisibilityOff
//                                    else
//                                        Icons.Filled.visibility
//                                ),
//                                contentDescription = if (confirmPasswordVisible)
//                                    "Hide password"
//                                else
//                                    "Show password",
//                                tint = Color(0xFF9E9E9E)
//                            )
//                        }

                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
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
                        // Validate inputs
                        var isValid = true

                        // Validate new password
                        val passwordValidationError = validatePassword(newPassword)
                        if (passwordValidationError != null) {
                            passwordError = passwordValidationError
                            isValid = false
                        } else if (newPassword.isBlank()) {
                            passwordError = "Please enter a new password"
                            isValid = false
                        }

                        // Validate confirm password
                        if (confirmPassword.isBlank()) {
                            confirmError = "Please confirm your password"
                            isValid = false
                        } else if (newPassword != confirmPassword) {
                            confirmError = "Passwords do not match"
                            isValid = false
                        }

                        // If all validations pass, update password
                        if (isValid) {
                            Log.d("PasswordSecurityScreen", "Changing password")
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
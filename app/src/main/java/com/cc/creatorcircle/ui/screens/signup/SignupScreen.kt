

@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircleapp.ui.screens.signup

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.screens.login.handleGoogleSignInResult
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.viewModel.SignUpViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.Unit












@Composable
fun SignupScreen(
    onLoginClick: () -> Unit = {},
    navController: NavController,
    viewModel: SignUpViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val username by viewModel.username.collectAsState() // Add username field
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) } // Changed to false initially

    val context = LocalContext.current

    // Google Sign-In client
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken("384802735119-2d1juctutnr1skrncm1ipdk41n7jaedh.apps.googleusercontent.com")
        .requestIdToken("965728963593-73o2kl524t3jhrsvgahfkrqhhmpoqdcf.apps.googleusercontent.com")
//        .requestIdToken("384802735119-bvs8oib450vhh8slh9qpssblcbh4ogi5.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Google launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(
            task,
            onSuccess = { googleIdToken ->
                val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putBoolean("isLoggedIn", true).apply()

                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()

                Log.d("AUTH-TOKEN-GOOGLE", "LoginScreen: $googleIdToken")

                try {
                    // --- POST request to your backend using OkHttp --- //
                    val client = OkHttpClient()
                    val mediaType = "application/json; charset=utf-8".toMediaType()

                    val jsonBody = JSONObject().apply {
                        put("token", googleIdToken)   // <- id_token from google login
                        put("config", "firebase")
                    }

                    val requestBody = jsonBody.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("https://crazycontent.in/api/auth/google")
                        .post(requestBody)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("AUTH", "POST failed: ${e.message}")
                        }

                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            val responseBody = response.body?.string()
                            Log.d("AUTH_RESPONSE", responseBody ?: "")

                            try {
                                val jsonObj = JSONObject(responseBody ?: "")

                                // Safely check if key exists
                                val accessToken = if (jsonObj.has("access_token")) {
                                    jsonObj.getString("access_token")
                                } else {
                                    // → if server sends inside a nested "data" object or something else, handle it here.
                                    val userObj = jsonObj.getJSONObject("user")
                                    userObj.getString("access_token")    // modify according to actual response
                                }

                                sharedPref.edit().putString("access_token", accessToken).apply()

                                Log.d("ACCESS-TOKEN", accessToken)

                                // ✅ Navigate on Main Thread
                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(Screen.Webhome.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }

                                // ✅ Use navigation instead of Intent
//                                navController.navigate(Screen.Webhome.route) {
//                                    popUpTo(Screen.Login.route) { inclusive = true }
//                                }

//                                val activity = context as Activity
//                                val intent = Intent(activity, MainActivity2::class.java)
////                                intent.putExtra("token", accessToken)
//                                activity.startActivity(intent)
//                                activity.finish()

                            } catch (e: Exception) {
                                Log.e("AUTH_PARSE_ERROR", e.toString())
                            }
                        }

                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error while sending token", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }



    // Handle success state - Show dialog when signup is successful and navigate after success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showDialog = true // Show success dialog

            // Store login state and access token
            val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                putBoolean("isLoggedIn", true)
                // Store access token from the signup response
                uiState.signUpResponse?.access_token?.let { token ->
                    putString("access_token", token)
                }
                apply()
            }

            // ✅ Navigate on Main Thread
            CoroutineScope(Dispatchers.Main).launch {
                navController.navigate(Screen.Webhome.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }

            // ✅ Use navigation instead of Intent
//            navController.navigate(Screen.Webhome.route) {
//                popUpTo(Screen.Signup.route) { inclusive = true }
//            }

            // Navigate to main activity after successful signup
//            try {
//                val activity = context as Activity
//                val intent = Intent(activity, MainActivity2::class.java)
//                uiState.signUpResponse?.access_token?.let { token ->
//                    intent.putExtra("token", token)
//                }
//                activity.startActivity(intent)
//                activity.finish() // Close current activity
//            } catch (e: Exception) {
//                Log.e("NAVIGATION_ERROR", "Error navigating to MainActivity2", e)
//            }
        }
    }

    // Handle error messages - removed auto-clear and toast to show persistent error display
    // Error will be displayed in the UI and user can dismiss it manually

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {

        if (showDialog) {
            SignupSuccess(
                onContinue = {
                    showDialog = false
                    viewModel.resetSignUpState() // Reset state when continuing
                    // Don't call onLoginClick() here since we're navigating to MainActivity2
                },
                onDismiss = {
                    showDialog = false
                    viewModel.resetSignUpState() // Reset state when dismissing
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Title
        Text(
            text = "Sign up",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB85CD9),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(18.dp))

        // General Error Message Display
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE) // Light red background
                ),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFEF5350)) // Red border
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close Error",
                        tint = Color(0xFFD32F2F), // Dark red
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { viewModel.clearError() }
                    )
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F), // Dark red
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss Error",
                        tint = Color(0xFFD32F2F), // Dark red
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { viewModel.clearError() }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Email Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Email",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.updateEmail(it) },
                placeholder = {
                    Text(
                        "Enter your email",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFB85CD9),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = uiState.emailError != null
            )
            // Error message for email
            uiState.emailError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Username Field (Added since ViewModel expects it)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Username",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.updateUsername(it) },
                placeholder = {
                    Text(
                        "Enter your username",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFB85CD9),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                isError = uiState.usernameError != null
            )
            // Error message for username
            uiState.usernameError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Password Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Password",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.updatePassword(it) },
                placeholder = {
                    Text(
                        "Enter Your Password",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle Password",
                            tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFB85CD9),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                isError = uiState.passwordError != null
            )
            // Error message for password
            uiState.passwordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Password Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Confirm Password",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.updateConfirmPassword(it) },
                placeholder = {
                    Text(
                        "Confirm Your Password",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Toggle Password",
                            tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFFB85CD9),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                isError = uiState.confirmPasswordError != null
            )
            // Error message for confirm password
            uiState.confirmPasswordError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Signup Button
        Button(
            onClick = {
                // Clear any existing errors before attempting signup
//                viewModel.clearError()
                viewModel.signUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFB85CD9),
                    shape = RoundedCornerShape(8.dp)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF7EBFD)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFB787F5),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Sign up",
                    color = Color(0xFFB85CD9),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Login Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Login here",
                fontSize = 14.sp,
                color = Color(0xFFB85CD9),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray,
                thickness = 1.dp
            )
            Text(
                text = " or ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Divider(
                modifier = Modifier.weight(1f),
                color = Color.LightGray,
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val signInIntent = googleSignInClient.signInIntent
                googleLauncher.launch(signInIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(121, 33, 164, (0.8f * 255).toInt()),
                                Color(214, 85, 157, 255)
                            )
                        ),
//                        shape = RoundedCornerShape(4)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_colored),
                        contentDescription = "Google Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue to google",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}











fun handleGoogleSignInResult(
    task: Task<GoogleSignInAccount>,
    onSuccess: (String?) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val account = task.getResult(ApiException::class.java)
        val idToken = account?.idToken
        onSuccess(idToken)
    } catch (e: ApiException) {
        onError("Google sign-in failed: ${e.statusCode}")
    }
}

@Composable
fun SignupSuccess(
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Success!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB85CD9) // pinkish-purple
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Congratulations! you have been\nsuccessfully signed up",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Gradient Continue button
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(121, 33, 164, (0.8f * 255).toInt()),
                                        Color(214, 85, 157, 255)
                                    )
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Continue",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}




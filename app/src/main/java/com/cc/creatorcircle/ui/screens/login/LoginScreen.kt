@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircleapp.ui.screens.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.MainActivity2
import com.cc.creatorcircle.ui.screens.login.handleGoogleSignInResult
import com.cc.creatorcircle.viewModel.LoginViewModel
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val loginResult = viewModel.loginState
    val isLoading = viewModel.isLoading
    var showError by remember { mutableStateOf(false) }

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("LoginScreen", "LoginScreen")
    }

    // Google Sign-In client
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("965728963593-73o2kl524t3jhrsvgahfkrqhhmpoqdcf.apps.googleusercontent.com")
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
                // Track Google sign-in initiation
                FirebaseAnalyticsHelper.logEvent("google_signin_token_received")

                val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putBoolean("isLoggedIn", true).apply()

                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()

                Log.d("AUTH-TOKEN-GOOGLE", "LoginScreen: $googleIdToken")

                try {
                    // Track backend authentication attempt
                    FirebaseAnalyticsHelper.logEvent("google_backend_auth_started")

                    val client = OkHttpClient()
                    val mediaType = "application/json; charset=utf-8".toMediaType()

                    val jsonBody = JSONObject().apply {
                        put("token", googleIdToken)
                        put("config", "firebase")
                    }

                    val requestBody = jsonBody.toString().toRequestBody(mediaType)

                    val request = Request.Builder()
                        .url("https://creatorcircle.in/api/auth/google")
                        .post(requestBody)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("AUTH", "POST failed: ${e.message}")

                            // Track authentication failure
                            FirebaseAnalyticsHelper.logError(
                                errorType = "google_auth_network_error",
                                errorMessage = e.message ?: "Unknown network error",
                                context = "LoginScreen"
                            )
                        }

                        override fun onResponse(call: Call, response: okhttp3.Response) {
                            val responseBody = response.body?.string()
                            Log.d("AUTH_RESPONSE", responseBody ?: "")

                            try {
                                val jsonObj = JSONObject(responseBody ?: "")

                                val accessToken = if (jsonObj.has("access_token")) {
                                    jsonObj.getString("access_token")
                                } else {
                                    val userObj = jsonObj.getJSONObject("user")
                                    userObj.getString("access_token")
                                }

                                sharedPref.edit().putString("access_token", accessToken).apply()

                                Log.d("ACCESS-TOKEN", accessToken)

                                // Track successful Google login
                                FirebaseAnalyticsHelper.logEvent("login_success", mapOf(
                                    "method" to "google",
                                    "timestamp" to System.currentTimeMillis().toString()
                                ))

                                CoroutineScope(Dispatchers.Main).launch {
                                    navController.navigate(Screen.SaboAI.route) {
                                        popUpTo(Screen.Login.route) { inclusive = true }
                                    }
                                }

                            } catch (e: Exception) {
                                Log.e("AUTH_PARSE_ERROR", e.toString())

                                // Track parsing error
                                FirebaseAnalyticsHelper.logError(
                                    errorType = "google_auth_parse_error",
                                    errorMessage = e.message ?: "Response parsing failed",
                                    context = "LoginScreen"
                                )
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error while sending token", Toast.LENGTH_SHORT).show()

                    // Track token sending error
                    FirebaseAnalyticsHelper.logError(
                        errorType = "google_auth_token_send_error",
                        errorMessage = e.message ?: "Token send failed",
                        context = "LoginScreen"
                    )
                }
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()

                // Track Google sign-in error
                FirebaseAnalyticsHelper.logError(
                    errorType = "google_signin_error",
                    errorMessage = error,
                    context = "LoginScreen"
                )
            }
        )
    }

    // Handle email/password login result
    LaunchedEffect(loginResult) {
        loginResult?.onSuccess { token ->
            val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isLoggedIn", true).apply()

            try {
                val activity = context as Activity

                val accessToken = token.access_token
                sharedPref.edit().putString("access_token", accessToken).apply()

                Log.d("AUTH-TOKEN", "LoginScreen: $accessToken")

                // Track successful email login
                FirebaseAnalyticsHelper.logEvent("login_success", mapOf(
                    "method" to "email",
                    "timestamp" to System.currentTimeMillis().toString()
                ))

                navController.navigate(Screen.Webhome.route)

            } catch (e: Exception) {
                Toast.makeText(context, "Context casting failed", Toast.LENGTH_SHORT).show()

                // Track navigation error
                FirebaseAnalyticsHelper.logError(
                    errorType = "login_navigation_error",
                    errorMessage = e.message ?: "Context casting failed",
                    context = "LoginScreen"
                )
            }
        }?.onFailure {
            showError = true
            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()

            // Track email login failure
            FirebaseAnalyticsHelper.logError(
                errorType = "email_login_error",
                errorMessage = "Invalid credentials",
                context = "LoginScreen"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(60.dp))

        // Title
        Text(
            text = "Login",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB85CD9),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        // error text
        if (showError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Invalid email or password",
                color = Color(0xFFE53935),
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

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
                onValueChange = {
                    email = it
                    showError = false
                },
                placeholder = {
                    Text(
                        "Enter your email",
                        color = Color.Gray,
                        fontSize = 16.sp
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
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                onValueChange = {
                    password = it
                    showError = false
                },
                placeholder = {
                    Text(
                        "Enter Your Password",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        passwordVisible = !passwordVisible

                        // Track password visibility toggle
                        FirebaseAnalyticsHelper.logFeatureUsed(
                            "password_visibility_toggle",
                            if (passwordVisible) "show" else "hide"
                        )
                    }) {
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
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Forgot Password
        Text(
            text = "Forget password?",
            color = Color(0xFFB85CD9),
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Track forgot password click
                    FirebaseAnalyticsHelper.logFeatureUsed("forgot_password_clicked", "login_screen")
                    onForgotPasswordClick()
                },
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Login Button
        Button(
            onClick = {
                // Track login attempt
                FirebaseAnalyticsHelper.logEvent("login_attempt", mapOf(
                    "method" to "email",
                    "has_email" to if (email.isNotEmpty()) "yes" else "no",
                    "has_password" to if (password.isNotEmpty()) "yes" else "no"
                ))

                viewModel.login(email, password, context)
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
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFB787F5),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Login",
                    color = Color(0xFFB85CD9),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Don't have an account? ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Signup",
                fontSize = 14.sp,
                color = Color(0xFFB85CD9),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    // Track signup navigation
                    FirebaseAnalyticsHelper.logFeatureUsed("signup_clicked", "login_screen")
                    onSignUpClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Track Google sign-in button click
                FirebaseAnalyticsHelper.logEvent("login_attempt", mapOf(
                    "method" to "google",
                    "source" to "login_screen"
                ))

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
                        )
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












//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.cc.creatorcircleapp.ui.screens.login
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
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
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.cc.creatorcircle.MainActivity2
//import com.cc.creatorcircle.ui.screens.login.handleGoogleSignInResult
//import com.cc.creatorcircle.viewModel.LoginViewModel
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.ui.navigation.Screen
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody.Companion.toRequestBody
//import org.json.JSONObject
//import java.io.IOException
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import org.json.JSONException
//
//@Composable
//fun LoginScreen(
//    onSignUpClick: () -> Unit = {},
//    onForgotPasswordClick: () -> Unit = {},
//    navController: NavController,
//    viewModel: LoginViewModel = viewModel()
//) {
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//
//    val loginResult = viewModel.loginState
//    val isLoading = viewModel.isLoading
//    var showError by remember { mutableStateOf(false) }
//
//    // Google Sign-In client
//    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken("965728963593-73o2kl524t3jhrsvgahfkrqhhmpoqdcf.apps.googleusercontent.com")
//        .requestEmail()
//        .build()
//    val googleSignInClient = GoogleSignIn.getClient(context, gso)
//
////    // Google launcher
//    val googleLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        handleGoogleSignInResult(
//            task,
//            onSuccess = { googleIdToken ->
//                val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//                sharedPref.edit().putBoolean("isLoggedIn", true).apply()
//
//                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()
//
//                Log.d("AUTH-TOKEN-GOOGLE", "LoginScreen: $googleIdToken")
//
//                try {
//                    // --- POST request to your backend using OkHttp --- //
//                    val client = OkHttpClient()
//                    val mediaType = "application/json; charset=utf-8".toMediaType()
//
//                    val jsonBody = JSONObject().apply {
//                        put("token", googleIdToken)   // <- id_token from google login
//                        put("config", "firebase")
//                    }
//
//                    val requestBody = jsonBody.toString().toRequestBody(mediaType)
//
//                    val request = Request.Builder()
//                        .url("https://creatorcircle.in/api/auth/google")
//                        .post(requestBody)
//                        .build()
//
//                    client.newCall(request).enqueue(object : Callback {
//                        override fun onFailure(call: Call, e: IOException) {
//                            Log.e("AUTH", "POST failed: ${e.message}")
//                        }
//
//                        override fun onResponse(call: Call, response: okhttp3.Response) {
//                            val responseBody = response.body?.string()
//                            Log.d("AUTH_RESPONSE", responseBody ?: "")
//
//                            try {
//                                val jsonObj = JSONObject(responseBody ?: "")
//
//                                // Safely check if key exists
//                                val accessToken = if (jsonObj.has("access_token")) {
//                                    jsonObj.getString("access_token")
//                                } else {
//                                    // → if server sends inside a nested "data" object or something else, handle it here.
//                                    val userObj = jsonObj.getJSONObject("user")
//                                    userObj.getString("access_token")    // modify according to actual response
//                                }
//
//                                sharedPref.edit().putString("access_token", accessToken).apply()
//
//                                Log.d("ACCESS-TOKEN", accessToken)
//
//                                CoroutineScope(Dispatchers.Main).launch {
//                                    navController.navigate(Screen.SaboAI.route) {
//                                        popUpTo(Screen.Login.route) { inclusive = true }
//                                    }
//                                }
//
//
//                            } catch (e: Exception) {
//                                Log.e("AUTH_PARSE_ERROR", e.toString())
//                            }
//                        }
//
//                    })
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    Toast.makeText(context, "Error while sending token", Toast.LENGTH_SHORT).show()
//                }
//            },
//            onError = { error ->
//                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
//            }
//        )
//    }
//
//
//
//
//
//    LaunchedEffect(loginResult) {
//        loginResult?.onSuccess { token ->
//
//            val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
//            sharedPref.edit().putBoolean("isLoggedIn", true).apply()
//
//            try {
//                val activity = context as Activity
//
//                val accessToken = token.access_token  // ✅ extract only access_token
//                sharedPref.edit().putString("access_token", accessToken).apply()
//
//                Log.d("AUTH-TOKEN", "LoginScreen: $accessToken")
//
//                // Navigate to MainActivity2
////                val intent = Intent(activity, MainActivity2::class.java)
////                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
////                activity.startActivity(intent)
////                activity.finish()
//
//                navController.navigate(Screen.Webhome.route)
//
//            } catch (e: Exception) {
//                Toast.makeText(context, "Context casting failed", Toast.LENGTH_SHORT).show()
//            }
//        }?.onFailure {
//            showError = true
//            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(horizontal = 24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//
//        Spacer(modifier = Modifier.height(60.dp))
//
//        // Title
//        Text(
//            text = "Login",
//            fontSize = 22.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color(0xFFB85CD9),
//            modifier = Modifier.fillMaxWidth(),
//            textAlign = TextAlign.Start
//        )
//
//
//        // error text
//        if (showError) {
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = "Invalid email or password",
//                color = Color(0xFFE53935),
//                fontSize = 16.sp, // or 14.sp
//                modifier = Modifier.fillMaxWidth(),
//                textAlign = TextAlign.Start
//            )
//        }
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//
//        // Email Field
//        Column(modifier = Modifier.fillMaxWidth()) {
//            Text(
//                text = "Email",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            OutlinedTextField(
//                value = email,
//                onValueChange = { email = it },
//                placeholder = {
//                    Text(
//                        "Enter your email",
//                        color = Color.Gray,
//                        fontSize = 16.sp
//                    )
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
////                    .height(50.dp)
//                    .background(Color.Transparent),
//                shape = RoundedCornerShape(8.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedTextColor = Color.Black,
//                    unfocusedTextColor = Color.Black,
//                    focusedBorderColor = Color(0xFFB85CD9),
//                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White
//                ),
//                singleLine = true
//            )
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Password Field
//        Column(modifier = Modifier.fillMaxWidth()) {
//            Text(
//                text = "Password",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                placeholder = {
//                    Text(
//                        "Enter Your Password",
//                        color = Color.Gray,
//                        fontSize = 16.sp
//                    )
//                },
//                trailingIcon = {
//                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                        Icon(
//                            if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
//                            contentDescription = "Toggle Password",
//                            tint = Color.Gray
//                        )
//                    }
//                },
//                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                modifier = Modifier
//                    .fillMaxWidth(),
////                    .height(50.dp),
//                shape = RoundedCornerShape(8.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedTextColor = Color.Black,       // typed text when focused
//                    unfocusedTextColor = Color.Black,
//                    focusedBorderColor = Color(0xFFB85CD9),
//                    unfocusedBorderColor = Color(0xFFE0E0E0),
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White
//                ),
//                singleLine = true
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Forgot Password
//        Text(
//            text = "Forget password?",
//            color = Color(0xFFB85CD9),
//            fontSize = 14.sp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { onForgotPasswordClick() },
//            textAlign = TextAlign.Start
//        )
//
//        Spacer(modifier = Modifier.height(40.dp))
//
//        // Login Button
//        Button(
//            onClick = { viewModel.login(email, password, context) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(44.dp)
//                .border(
//                    width = 1.dp,
//                    color = Color(0xFFB85CD9),
//                    shape = RoundedCornerShape(8.dp)
//                ),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFFF7EBFD)
//            ),
//            shape = RoundedCornerShape(8.dp),
//            enabled = !isLoading
//        ) {
//            if (isLoading) {
//                CircularProgressIndicator(
//                    color = Color(0xFFB787F5),
//                    strokeWidth = 2.dp,
//                    modifier = Modifier.size(20.dp)
//                )
//            } else {
//                Text(
//                    text = "Login",
//                    color = Color(0xFFB85CD9),
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Sign Up Link
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "Don't have an account? ",
//                fontSize = 14.sp,
//                color = Color.Gray
//            )
//            Text(
//                text = "Signup",
//                fontSize = 14.sp,
//                color = Color(0xFFB85CD9),
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.clickable { onSignUpClick() }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(22.dp))
//
//        // OR Divider
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Divider(
//                modifier = Modifier.weight(1f),
//                color = Color.LightGray,
//                thickness = 1.dp
//            )
//            Text(
//                text = " or ",
//                fontSize = 14.sp,
//                color = Color.Gray
//            )
//            Divider(
//                modifier = Modifier.weight(1f),
//                color = Color.LightGray,
//                thickness = 1.dp
//            )
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        Button(
//            onClick = {
//                val signInIntent = googleSignInClient.signInIntent
//                googleLauncher.launch(signInIntent)
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(46.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color.Transparent
//            ),
//            shape = RoundedCornerShape(8.dp),
//            contentPadding = PaddingValues()
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(
//                        brush = Brush.horizontalGradient(
//                            colors = listOf(
//                                Color(121, 33, 164, (0.8f * 255).toInt()),
//                                Color(214, 85, 157, 255)
//                            )
//                        ),
////                        shape = RoundedCornerShape(4)
//                    )
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_google_colored),
//                        contentDescription = "Google Icon",
//                        tint = Color.Unspecified,
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    Text(
//                        text = "Continue to google",
//                        color = Color.White,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//fun handleGoogleSignInResult(
//    task: Task<GoogleSignInAccount>,
//    onSuccess: (String?) -> Unit,
//    onError: (String) -> Unit
//) {
//    try {
//        val account = task.getResult(ApiException::class.java)
//        val idToken = account?.idToken
//        onSuccess(idToken)
//    } catch (e: ApiException) {
//        onError("Google sign-in failed: ${e.statusCode}")
//    }
//}

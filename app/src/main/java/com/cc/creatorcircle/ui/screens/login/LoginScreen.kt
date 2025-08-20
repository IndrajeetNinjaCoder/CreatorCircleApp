
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
import androidx.compose.ui.text.*
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
import com.cc.creatorcircle.ui.viewModel.LoginViewModel
import com.cc.creatorcircle.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

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
            onSuccess = { token ->

                val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
                sharedPref.edit().putBoolean("isLoggedIn", true).apply()

                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()
                // TODO: Send token to backend and navigate
                try {
                    val activity = context as Activity
                    val intent = Intent(activity, MainActivity2::class.java)

//                    Log.d("AUTH-TOKEN-GOOGLE", "LoginScreen: " + token)
//                    val accessToken = token.access_token  // ✅ extract only access_token
//                    val accessToken = token  // ✅ extract only access_token


                    Log.d("AUTH-TOKEN-GOOGLE-NEW", "LoginScreen: $token")
                    intent.putExtra("token", token)
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Context casting failed", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }



    LaunchedEffect(loginResult) {
        loginResult?.onSuccess { token ->

            val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("isLoggedIn", true).apply()

            try {
                val activity = context as Activity
                val intent = Intent(activity, MainActivity2::class.java)

                val accessToken = token.access_token  // ✅ extract only access_token
                sharedPref.edit().putString("access_token", accessToken).apply()

                Log.d("AUTH-TOKEN", "LoginScreen: $accessToken")
//                intent.putExtra("token", accessToken)
                activity.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Context casting failed", Toast.LENGTH_SHORT).show()
            }
        }?.onFailure {
            showError = true
            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
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
                fontSize = 16.sp, // or 14.sp
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
                onValueChange = { email = it },
                placeholder = {
                    Text(
                        "Enter your email",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
//                    .height(50.dp)
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
                onValueChange = { password = it },
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
                modifier = Modifier
                    .fillMaxWidth(),
//                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,       // typed text when focused
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
                .clickable { onForgotPasswordClick() },
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Login Button
        Button(
            onClick = { viewModel.login(email, password, context) },
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
                modifier = Modifier.clickable { onSignUpClick() }
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





















/*
@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.viewModel.LoginViewModel
import com.cc.creatorcircle.R

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

    // Observe email login result
    LaunchedEffect(loginResult) {
        println("LoginScreen: LaunchedEffect triggered with loginResult: $loginResult")

        loginResult?.onSuccess { loginResponse ->
            println("=== LoginScreen: Email Login Success ===")
            println("LoginScreen: Access Token: ${loginResponse.access_token}")
            println("LoginScreen: User Email: ${loginResponse.user.email}")
            println("LoginScreen: User ID: ${loginResponse.user.id}")
            println("LoginScreen: Username: ${loginResponse.user.username}")

            // Debug the stored data
            viewModel.debugStoredData(context)

            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

            // Navigate to webview - MainActivity2 will handle the webview authentication
            navController.navigate("webhome") {
                popUpTo("webhome") { inclusive = true }
            }

            println("LoginScreen: Navigation to webhome completed")
            println("=== End LoginScreen: Email Login Success ===")
        }?.onFailure { exception ->
            println("LoginScreen: Login failed with exception: ${exception.message}")
            exception.printStackTrace()
            Toast.makeText(context, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Logo
                Icon(
                    painter = painterResource(id = R.drawable.ic_cc_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Creator Circle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email label
                Text(
                    buildAnnotatedString {
                        append("Email")
                        withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email address") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password label
                Text(
                    buildAnnotatedString {
                        append("Password")
                        withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(Icons.Filled.Visibility, contentDescription = "Toggle Password")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sign In Button
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            println("LoginScreen: Starting email login with email: $email")
                            // Clear any previous login state
                            viewModel.logout(context)
                            // Start login process
                            viewModel.login(email, password, context)
                        } else {
                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(listOf(Color(0xFFD6559D), Color(0xFF7921A4))),
                            shape = RoundedCornerShape(28.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Sign in", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // OR Divider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text("  or  ", fontSize = 13.sp, color = Color.Gray)
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Note: Google Sign-In is disabled as requested
                Text(
                    text = "Google Sign-In temporarily disabled",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Forgot password?",
                    color = Color(0xFFD6559D),
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.End).clickable { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(
                            style = SpanStyle(color = Color(0xFF7B1FA2), fontWeight = FontWeight.Medium)
                        ) { append("Sign up here") }
                    },
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { onSignUpClick() }
                )
            }
        }
    }
}

*/


//@file:OptIn(ExperimentalMaterial3Api::class)
//
//package com.cc.creatorcircle.ui.screens.login
//
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.*
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.cc.creatorcircle.ui.viewModel.LoginViewModel
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.tasks.Task
//import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//import com.cc.creatorcircle.R
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
//
//    // Google Sign-In client
//    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken("384802735119-2d1juctutnr1skrncm1ipdk41n7jaedh.apps.googleusercontent.com")
//        .requestEmail()
//        .build()
//    val googleSignInClient = GoogleSignIn.getClient(context, gso)
//
//    // Google launcher
//    val googleLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//        handleGoogleSignInResult(task,
//            onSuccess = { token ->
//                // Save Google auth data
//                val sharedPrefs = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
//                sharedPrefs.edit().apply {
//                    putString("auth_token", token ?: "")
//                    putString("auth_type", "google")
//                    putBoolean("is_logged_in", true)
//                    apply()
//                }
//
//                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()
//                navController.navigate("webhome") {
//                    popUpTo("webhome") { inclusive = true }
//                }
//            },
//            onError = { error ->
//                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
//            }
//        )
//    }
//
//    // Observe normal login result
//    LaunchedEffect(loginResult) {
//        loginResult?.onSuccess {
//            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
//            navController.navigate("webhome") {
//                popUpTo("webhome") { inclusive = true }


//            }
//        }?.onFailure { exception ->
//            Toast.makeText(context, "Login failed: ${exception.message}", Toast.LENGTH_LONG).show()
//            println("LoginScreen: Login failed with exception: ${exception.message}")
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFFCFCFC)),
//        contentAlignment = Alignment.Center
//    ) {
//        Card(
//            shape = RoundedCornerShape(16.dp),
//            modifier = Modifier
//                .padding(horizontal = 20.dp)
//                .fillMaxWidth()
//                .wrapContentHeight()
//                .shadow(6.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.White)
//        ) {
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 24.dp, vertical = 32.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                // Logo
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_cc_logo),
//                    contentDescription = "Logo",
//                    tint = Color.Unspecified,
//                    modifier = Modifier.size(40.dp)
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Creator Circle",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Text(
//                    text = "Login",
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = Color.Black
//                )
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                // Email label
//                Text(
//                    buildAnnotatedString {
//                        append("Email")
//                        withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
//                    },
//                    fontSize = 13.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier
//                        .align(Alignment.Start)
//                        .padding(bottom = 4.dp)
//                )
//
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = { email = it },
//                    placeholder = { Text("Enter your email address") },
//                    modifier = Modifier.fillMaxWidth().height(56.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    singleLine = true
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Password label
//                Text(
//                    buildAnnotatedString {
//                        append("Password")
//                        withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
//                    },
//                    fontSize = 13.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier
//                        .align(Alignment.Start)
//                        .padding(bottom = 4.dp)
//                )
//
//                OutlinedTextField(
//                    value = password,
//                    onValueChange = { password = it },
//                    placeholder = { Text("Enter your password") },
//                    trailingIcon = {
//                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
//                            Icon(Icons.Filled.Visibility, contentDescription = "Toggle Password")
//                        }
//                    },
//                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                    modifier = Modifier.fillMaxWidth().height(56.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    singleLine = true
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Sign In Button
//                Button(
//                    onClick = {
//                        if (email.isNotBlank() && password.isNotBlank()) {
//                            viewModel.login(email, password, context)
//                        } else {
//                            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
//                        }
//                    },
//                    modifier = Modifier.fillMaxWidth().height(48.dp),
//                    shape = RoundedCornerShape(28.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//                    contentPadding = PaddingValues(),
//                    enabled = !isLoading
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize().background(
//                            Brush.horizontalGradient(listOf(Color(0xFFD6559D), Color(0xFF7921A4))),
//                            shape = RoundedCornerShape(28.dp)
//                        ),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        if (isLoading) {
//                            CircularProgressIndicator(
//                                color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
//                            )
//                        } else {
//                            Text("Sign in", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // OR Divider
//                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
//                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
//                    Text("  or  ", fontSize = 13.sp, color = Color.Gray)
//                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
//                }
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                // Google Sign In (Working)
//                OutlinedButton(
//                    onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
//                    modifier = Modifier.fillMaxWidth().height(48.dp),
//                    shape = RoundedCornerShape(8.dp),
//                    border = ButtonDefaults.outlinedButtonBorder
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_google),
//                        contentDescription = "Google",
//                        modifier = Modifier.size(20.dp),
//                        tint = Color.Unspecified
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Sign in with Google", color = Color.Black, fontSize = 14.sp)
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Text(
//                    text = "Forgot password?",
//                    color = Color(0xFFD6559D),
//                    fontSize = 13.sp,
//                    modifier = Modifier.align(Alignment.End).clickable { onForgotPasswordClick() }
//                )
//
//                Spacer(modifier = Modifier.height(20.dp))
//
//                Text(
//                    buildAnnotatedString {
//                        append("Don't have an account? ")
//                        withStyle(
//                            style = SpanStyle(color = Color(0xFF7B1FA2), fontWeight = FontWeight.Medium)
//                        ) { append("Sign up here") }
//                    },
//                    fontSize = 13.sp,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth().clickable { onSignUpClick() }
//                )
//            }
//        }
//    }
//
//    fun handleGoogleSignInResult(
//        task: Task<GoogleSignInAccount>,
//        onSuccess: (String?) -> Unit,
//        onError: (String) -> Unit
//    ) {
//        try {
//            val account = task.getResult(ApiException::class.java)
//            val idToken = account?.idToken
//            onSuccess(idToken)
//        } catch (e: ApiException) {
//            onError("Google sign-in failed: ${e.statusCode}")
//        }
//    }
//}


/*
@file:OptIn(ExperimentalMaterial3Api::class)

package com.cc.creatorcircle.ui.screens.login

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.viewModel.LoginViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.cc.creatorcircle.R

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

    // Google Sign-In client
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("384802735119-2d1juctutnr1skrncm1ipdk41n7jaedh.apps.googleusercontent.com")
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    // Google launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task,
            onSuccess = { token ->
                Toast.makeText(context, "Google Login Success", Toast.LENGTH_SHORT).show()
                // TODO: Send token to backend and navigate
            },
            onError = { error ->
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Observe normal login result
    LaunchedEffect(loginResult) {
        loginResult?.onSuccess {
            navController.navigate("webhome") {
                popUpTo("webhome") { inclusive = true }
            }
        }?.onFailure {
            Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCFCFC)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Logo
                Icon(
                    painter = painterResource(id = R.drawable.ic_cc_logo),
                    contentDescription = "Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Creator Circle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Email label
                Text(
                    buildAnnotatedString {
                        append("Email")
                        withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Enter your email address") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password label
                Text(
                    buildAnnotatedString {
                        append("Password")
                        withStyle(style = SpanStyle(color = Color.Red)) { append(" *") }
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Enter your password") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(Icons.Filled.Visibility, contentDescription = "Toggle Password")
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Sign In Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = !isLoading
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.horizontalGradient(listOf(Color(0xFFD6559D), Color(0xFF7921A4))),
                            shape = RoundedCornerShape(28.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Sign in", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // OR Divider
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                    Text("  or  ", fontSize = 13.sp, color = Color.Gray)
                    Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Google Sign In (Working)
                OutlinedButton(
                    onClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign in with Google", color = Color.Black, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Forgot password?",
                    color = Color(0xFFD6559D),
                    fontSize = 13.sp,
                    modifier = Modifier.align(Alignment.End).clickable { onForgotPasswordClick() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    buildAnnotatedString {
                        append("Don't have an account? ")
                        withStyle(
                            style = SpanStyle(color = Color(0xFF7B1FA2), fontWeight = FontWeight.Medium)
                        ) { append("Sign up here") }
                    },
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().clickable { onSignUpClick() }
                )
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
}

*/




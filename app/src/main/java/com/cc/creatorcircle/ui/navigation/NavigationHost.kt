package com.cc.creatorcircle.ui.navigation


import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cc.creatorcircle.MainActivity2
import com.cc.creatorcircle.ui.screens.home.HomeScreen
import com.cc.creatorcircle.ui.screens.profile.ProfileScreen
import com.cc.creatorcircleapp.ui.screens.login.LoginScreen
import com.cc.creatorcircleapp.ui.screens.signup.SignupScreen

@Composable
fun NavigationHost(navController: NavHostController) {

    val context = LocalContext.current

    // Read login status
    val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

    // Decide start destination
//    val startDestination = if (isLoggedIn) Screen.Webhome.route else Screen.Login.route
    val startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route


    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onSignUpClick = { navController.navigate(Screen.Signup.route) },
                onForgotPasswordClick = { /* TODO */ },
                navController
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) }, // or navigate(Screen.Login.route)
                navController
            )
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }



        // 🔹 New WebView Route
        composable("webhome") {
            val context = LocalContext.current
            context.startActivity(Intent(context, MainActivity2::class.java))
        }

    }
}

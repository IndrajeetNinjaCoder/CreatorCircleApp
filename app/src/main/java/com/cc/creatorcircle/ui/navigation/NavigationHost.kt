package com.cc.creatorcircle.ui.navigation


import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cc.creatorcircle.MainActivity2
import com.cc.creatorcircle.data.api.ApiService
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.ui.screens.creatorcoin.CreatorCoin
import com.cc.creatorcircle.ui.screens.home.HomeScreen
import com.cc.creatorcircle.ui.screens.livesession.LiveSession
import com.cc.creatorcircle.ui.screens.notification.NotificationsWeb
import com.cc.creatorcircle.ui.screens.onboarding.SignupOnboarding
import com.cc.creatorcircle.ui.screens.profile.ProfileWeb
import com.cc.creatorcircle.ui.screens.profile.UserProfile
import com.cc.creatorcircle.ui.screens.resourcehub.ResourceHub
import com.cc.creatorcircle.ui.screens.sabo.ChatScreen
import com.cc.creatorcircle.ui.screens.sabo.SaboAIScreen
import com.cc.creatorcircle.ui.screens.sabo.SaboWeb
import com.cc.creatorcircleapp.ui.screens.login.LoginScreen
import com.cc.creatorcircleapp.ui.screens.signup.SignupScreen
import com.example.app.ConnectionsScreen

@Composable
fun NavigationHost(navController: NavHostController) {

    val context = LocalContext.current

    val apiService: ApiService = RetrofitInstance.api;

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
            HomeScreen(navController)
        }
//        composable(Screen.Profile.route) {
//            ProfileScreen()
//        }





        // 🔹 New WebView Route
        composable("webhome") {
            val context = LocalContext.current
            context.startActivity(Intent(context, MainActivity2::class.java))
        }

        composable(Screen.Connections.route) {
//            ConnectionsWeb(navController)
            ConnectionsScreen(navController)
//            SignupOnboarding(navController)
        }

        composable(Screen.SaboAI.route) {
            SaboWeb(navController)
//            SaboAIScreen(navController)
//            ChatScreen()
        }

        composable(Screen.LiveSession.route) {
            LiveSession(navController)
        }


        composable(Screen.ProfileWeb.route) {
            ProfileWeb(navController)
        }

        composable(Screen.NotificationsWeb.route) {
//            NotificationsWeb(navController)
            CreatorCoin(navController)
        }

        composable(Screen.CreatorCoin.route) {
            CreatorCoin(navController)
        }



        composable(Screen.ResourceHub.route) {
            ResourceHub(navController)

        }

        composable(Screen.SignupOnboarding.route) {
            SignupOnboarding(navController)
        }



        composable(
            route = "userprofile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            UserProfile(navController, userId = userId)
        }


    }
}

package com.cc.creatorcircleapp.ui.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cc.creatorcircleapp.ui.screens.home.FeedScreen
import com.cc.creatorcircleapp.ui.screens.home.HomeScreen
import com.cc.creatorcircleapp.ui.screens.login.LoginScreen
import com.cc.creatorcircleapp.ui.screens.profile.ProfileScreen
import com.cc.creatorcircleapp.ui.screens.signup.SignupScreen

@Composable
fun NavigationHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onSignUpClick = { navController.navigate(Screen.Signup.route) },
                onForgotPasswordClick = { /* TODO */ },
                navController
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onLoginClick = { navController.popBackStack() } // or navigate(Screen.Login.route)
            )
        }
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }

        composable(Screen.Feed.route) {
            FeedScreen()
        }


    }
}

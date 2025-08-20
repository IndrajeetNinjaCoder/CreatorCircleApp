package com.cc.creatorcircle.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Feed : Screen("feed")


    object Webhome : Screen("webhome")
}
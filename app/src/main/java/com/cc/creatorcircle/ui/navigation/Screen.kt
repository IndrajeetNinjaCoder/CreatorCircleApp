package com.cc.creatorcircle.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
//    object Profile : Screen("profile")


    object Webhome : Screen("webhome")

    object Connections : Screen("connections")

    object SaboAI : Screen("saboai")

    object LiveSession : Screen("livesession")

    object ProfileWeb : Screen("profile")

    object NotificationsWeb : Screen("notifications")

    object CreatorCoin : Screen("creatorcoin")



    object ResourceHub : Screen("resourcehub")




    object SignupOnboarding : Screen("signup-onboarding")

//    object UserProfile : Screen("userprofile")

    object UserProfile : Screen("userprofile/{userId}") {
        fun createRoute(userId: Int) = "userprofile/$userId"
    }







}
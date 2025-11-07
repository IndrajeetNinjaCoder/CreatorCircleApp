package com.cc.creatorcircle.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
//    object Profile : Screen("profile")


    object Webhome : Screen("webhome")

    object Connections : Screen("connections")

    object BrandCollab : Screen("brandcollab")


    object SaboAI : Screen("saboai")

    object LiveSession : Screen("livesession")

    object MentorCircle : Screen("mentorcircle")
    object BookingScreen : Screen("bookingscreen")

    object MySessionScreen : Screen("mysession")



    object ProfileWeb : Screen("profile")

    object NotificationsWeb : Screen("notifications")

    object CreatorCoin : Screen("creatorcoin")


    object ResourceHub : Screen("resourcehub")


    object SignupOnboarding : Screen("signup-onboarding")

//    object UserProfile : Screen("userprofile")

    object UserProfile : Screen("userprofile/{userId}") {
        fun createRoute(userId: Int) = "userprofile/$userId"
    }

//    object BookingSlot : Screen("booking_slot/{userId}") {
//        fun createRoute(userId: Int) = "booking_slot/$userId"
//    }

    object BookingSlot : Screen("booking_slot/{userId}/{influencerName}") {
        fun createRoute(userId: Int, influencerName: String) =
            "booking_slot/$userId/${Uri.encode(influencerName)}"
    }

    // Screen object
    object AboutSection : Screen("about_section/{userId}/{influencerName}") {  // ✅ Changed from "about" to "about_section"
        fun createRoute(userId: Int, influencerName: String) =
            "about_section/$userId/${Uri.encode(influencerName)}"  // ✅ Changed from "about" to "about_section"
    }


    object MessageConnections : Screen("message_connections")

//    object MessageScreen : Screen("message_screen")

    object MessageScreen : Screen("message_screen/{userId}/{userName}/{profilePic}") {
        fun createRoute(userId: Int, userName: String, profilePic: String?) =
            "message_screen/$userId/${Uri.encode(userName)}/${Uri.encode(profilePic ?: "")}"
    }




}
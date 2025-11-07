package com.cc.creatorcircle.utils

import android.content.Context

data class UserData(
    val userId: Int = -1,
    val username: String = "",
    val profilePic: String? = null
)

class UserDataManager(private val context: Context) {
    private val sharedPref = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)

    fun saveUserData(userId: Int, username: String, profilePic: String?) {
        sharedPref.edit().apply {
            putInt("userId", userId)
            putString("username", username)
            if (profilePic != null) {
                putString("profilePic", profilePic)
            } else {
                remove("profilePic")
            }
            apply()
        }
    }

    fun getUserData(): UserData {
        return UserData(
            userId = sharedPref.getInt("userId", -1),
            username = sharedPref.getString("username", "") ?: "",
            profilePic = sharedPref.getString("profilePic", null)
        )
    }

    fun clearUserData() {
        sharedPref.edit().apply {
            remove("userId")
            remove("username")
            remove("profilePic")
            apply()
        }
    }
}

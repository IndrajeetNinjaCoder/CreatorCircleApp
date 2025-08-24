package com.cc.creatorcircle.utils

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE) // Use same prefs file

    companion object {
        private const val AUTH_TOKEN = "access_token" // Use same key
        private const val USER_ID = "user_id"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit()
            .putString(AUTH_TOKEN, token)
            .apply()
    }

    fun getToken(): String {
        return sharedPreferences.getString(AUTH_TOKEN, "") ?: ""
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit()
            .putString(USER_ID, userId)
            .apply()
    }

    fun getUserId(): String {
        return sharedPreferences.getString(USER_ID, "") ?: ""
    }

    fun clearTokens() {
        sharedPreferences.edit()
            .remove(AUTH_TOKEN)
            .remove(USER_ID)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken().isNotEmpty()
    }
}
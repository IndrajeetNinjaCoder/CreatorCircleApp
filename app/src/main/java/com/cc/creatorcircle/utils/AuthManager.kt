package com.cc.creatorcircle.utils

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val AUTH_PREFS = "auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PASSWORD = "user_password"
    private const val KEY_AUTH_TYPE = "auth_type"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(AUTH_PREFS, Context.MODE_PRIVATE)
    }

    fun saveEmailLogin(context: Context, email: String, password: String, token: String? = null) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_PASSWORD, password)
            putString(KEY_AUTH_TYPE, "email")
            token?.let { putString(KEY_AUTH_TOKEN, it) }
            apply()
        }
    }

    fun saveGoogleLogin(context: Context, token: String) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_AUTH_TOKEN, token)
            putString(KEY_AUTH_TYPE, "google")
            apply()
        }
    }

    fun logout(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getAuthToken(context: Context): String? {
        return getPrefs(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_EMAIL, null)
    }

    fun getAuthType(context: Context): String {
        return getPrefs(context).getString(KEY_AUTH_TYPE, "email") ?: "email"
    }
}
package com.cc.creatorcircle.ui.viewModel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.LoginResponse
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.Result

class LoginViewModel : ViewModel() {
    var loginState by mutableStateOf<Result<LoginResponse>?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                println("LoginViewModel: Making API call to login with username: $username")
                val response = RetrofitInstance.api.login(username, password)
                println("LoginViewModel: API response received - Success: ${response.isSuccessful}, Code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    println("LoginViewModel: Raw API Response Body: $loginResponse")

                    // Convert the entire LoginResponse to JSON string using Gson
                    val gson = Gson()
                    val loginResponseJson = gson.toJson(loginResponse)
                    println("LoginViewModel: LoginResponse JSON: $loginResponseJson")

                    // Save authentication data to SharedPreferences
                    val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    sharedPrefs.edit().apply {
                        // Save the complete login response as JSON (MainActivity2 expects this)
                        putString("login_response", loginResponseJson)

                        // Save primary authentication data
                        putString("access_token", loginResponse.access_token)
                        putString("refresh_token", loginResponse.refresh_token ?: "")
                        putString("token_type", loginResponse.token_type ?: "Bearer")
                        putString("auth_token", loginResponse.access_token) // MainActivity2 fallback

                        // Save user credentials for form filling fallback
                        putString("user_email", username)
                        putString("user_password", password)
                        putString("auth_type", "email")

                        // Save user data for easy access
                        putInt("user_id", loginResponse.user.id)
                        putString("username", loginResponse.user.username ?: "")
                        putString("profile_pic", loginResponse.user.profile_pic ?: "")
                        putString("full_name", loginResponse.user.full_name ?: "")
                        putString("bio", loginResponse.user.bio ?: "")
                        putString("mobile_number", loginResponse.user.mobile_number ?: "")
                        putBoolean("is_active", loginResponse.user.is_active)
                        putInt("age", loginResponse.user.age ?: 0)
                        putBoolean("onboarding_status", loginResponse.user.onboarding_status)
                        putBoolean("is_connected", loginResponse.user.is_connected)
                        putBoolean("is_follower", loginResponse.user.is_follower)
                        putBoolean("is_following", loginResponse.user.is_following)

                        // Mark as logged in (this triggers webview authentication)
                        putBoolean("is_logged_in", true)

                        apply()
                    }

                    println("LoginViewModel: All login data saved to SharedPreferences successfully!")
                    println("LoginViewModel: Access Token: ${loginResponse.access_token}")
                    println("LoginViewModel: User Email: ${loginResponse.user.email}")
                    println("LoginViewModel: User ID: ${loginResponse.user.id}")

                    loginState = Result.success(loginResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("LoginViewModel: Login failed - Code: ${response.code()}, Error: $errorBody")
                    loginState = Result.failure(Exception("Login failed: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                println("LoginViewModel: Login error: ${e.message}")
                e.printStackTrace()
                loginState = Result.failure(e)
            } finally {
                isLoading = false
            }
        }
    }

    // Helper function to get saved access token
    fun getAccessToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("access_token", null)
    }

    // Helper function to check if user is logged in
    fun isUserLoggedIn(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false)
        val hasAccessToken = !sharedPrefs.getString("access_token", null).isNullOrEmpty()
        println("LoginViewModel: User logged in status - isLoggedIn: $isLoggedIn, hasAccessToken: $hasAccessToken")
        return isLoggedIn && hasAccessToken
    }

    // Helper function to get login response JSON
    fun getLoginResponseJson(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("login_response", null)
    }

    // Helper function to logout and clear all data
    fun logout(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        loginState = null
        println("LoginViewModel: User logged out, all data cleared")
    }

    // Helper function to debug stored data
    fun debugStoredData(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        println("LoginViewModel: === DEBUGGING STORED DATA ===")
        println("LoginViewModel: is_logged_in: ${sharedPrefs.getBoolean("is_logged_in", false)}")
        println("LoginViewModel: access_token exists: ${!sharedPrefs.getString("access_token", "").isNullOrEmpty()}")
        println("LoginViewModel: user_email: ${sharedPrefs.getString("user_email", "NOT_SET")}")
        println("LoginViewModel: login_response exists: ${!sharedPrefs.getString("login_response", "").isNullOrEmpty()}")
        println("LoginViewModel: ================================")
    }
}
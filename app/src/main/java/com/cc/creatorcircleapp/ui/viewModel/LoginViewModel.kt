package com.cc.creatorcircleapp.ui.viewModel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircleapp.data.api.RetrofitInstance
import com.cc.creatorcircleapp.data.models.LoginResponse
import kotlinx.coroutines.launch
import kotlin.Result


class LoginViewModel : ViewModel() {
    var loginState by mutableStateOf<Result<LoginResponse>?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set
    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitInstance.api.login(username, password)
                if (response.isSuccessful && response.body() != null) {
                    loginState = Result.success(response.body()!!)
                } else {
                    loginState = Result.failure(Exception("Login failed: ${response.code()}"))
                }
            } catch (e: Exception) {
                println("Login error: ${e.message}")
                e.printStackTrace()
                loginState = Result.failure(e)
            }

            isLoading = false
        }
    }
}

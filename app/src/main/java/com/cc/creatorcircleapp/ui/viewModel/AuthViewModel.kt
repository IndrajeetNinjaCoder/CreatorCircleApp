package com.cc.creatorcircleapp.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircleapp.data.models.SignUpRequest
import com.cc.creatorcircleapp.data.models.SignUpResponse
import com.cc.creatorcircleapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _signUpState = MutableStateFlow<Response<SignUpResponse>?>(null)
    val signUpState: StateFlow<Response<SignUpResponse>?> = _signUpState

    fun signUp(email: String, username: String, password: String) {
        viewModelScope.launch {
            val request = SignUpRequest(email, username, password)
            val response = repository.registerUser(request)
            _signUpState.value = response
        }
    }
}

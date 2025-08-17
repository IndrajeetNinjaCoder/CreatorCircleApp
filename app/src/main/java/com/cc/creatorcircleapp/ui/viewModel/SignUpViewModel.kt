package com.cc.creatorcircleapp.ui.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircleapp.data.models.SignUpRequest
import com.cc.creatorcircleapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class SignUpViewModel : ViewModel() {

    private val repository = AuthRepository()

    val email = MutableStateFlow("")
    val username = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun updateEmail(value: String) { email.value = value }
    fun updateUsername(value: String) { username.value = value }
    fun updatePassword(value: String) { password.value = value }
    fun updateConfirmPassword(value: String) { confirmPassword.value = value }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSignUpState() {
        _uiState.value = SignUpUiState()
    }

    fun signUp() {
        if (!validateInputs()) return

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val response = repository.registerUser(
                    SignUpRequest(email.value, username.value, password.value)
                )
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Signup failed: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Unknown error"
                )
            }
        }
    }

    private fun validateInputs(): Boolean {
        var valid = true
        var emailErr: String? = null
        var usernameErr: String? = null
        var passwordErr: String? = null
        var confirmErr: String? = null

        if (email.value.isBlank()) {
            emailErr = "Email is required"
            valid = false
        }
        if (username.value.isBlank()) {
            usernameErr = "Username is required"
            valid = false
        }
        if (password.value.length < 6) {
            passwordErr = "Password must be at least 6 characters"
            valid = false
        }
        if (confirmPassword.value != password.value) {
            confirmErr = "Passwords do not match"
            valid = false
        }

        _uiState.value = _uiState.value.copy(
            emailError = emailErr,
            usernameError = usernameErr,
            passwordError = passwordErr,
            confirmPasswordError = confirmErr
        )

        return valid
    }
}

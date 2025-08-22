package com.cc.creatorcircle.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import org.json.JSONObject
import com.cc.creatorcircle.data.models.SignUpResponse

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val signUpResponse: SignUpResponse? = null,
    val errorMessage: String? = null,
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    val email = MutableStateFlow("")
    val username = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun updateEmail(value: String) {
        email.value = value
        // Clear email error when user starts typing
        if (_uiState.value.emailError != null) {
            _uiState.value = _uiState.value.copy(emailError = null)
        }
    }

    fun updateUsername(value: String) {
        username.value = value
        // Clear username error when user starts typing
        if (_uiState.value.usernameError != null) {
            _uiState.value = _uiState.value.copy(usernameError = null)
        }
    }

    fun updatePassword(value: String) {
        password.value = value
        // Clear password error when user starts typing
        if (_uiState.value.passwordError != null) {
            _uiState.value = _uiState.value.copy(passwordError = null)
        }
    }

    fun updateConfirmPassword(value: String) {
        confirmPassword.value = value
        // Clear confirm password error when user starts typing
        if (_uiState.value.confirmPasswordError != null) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = null)
        }
    }

    fun signUp() {
        // Validate inputs first
        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val signUpRequest = SignUpRequest(
                    email = email.value.trim(),
                    username = username.value.trim(),
                    password = password.value
                )

                Log.d("SIGNUP-DATA", "signUp: ${email.value}, ${username.value}, ${password.value}")

                val response = authRepository.registerUser(signUpRequest)

                if (response.isSuccessful) {
                    val signUpResponse = response.body()
                    if (signUpResponse != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true,
                            signUpResponse = signUpResponse,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Signup successful but no response data"
                        )
                    }
                } else {
                    // Handle error response
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody, response.code())

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = when (e) {
                        is java.net.UnknownHostException -> "No internet connection. Please check your network."
                        is java.net.SocketTimeoutException -> "Request timeout. Please try again."
                        else -> e.localizedMessage ?: "Network error. Please check your connection."
                    }
                )
            }
        }
    }

    fun resetSignUpState() {
        _uiState.value = SignUpUiState()
        email.value = ""
        username.value = ""
        password.value = ""
        confirmPassword.value = ""
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun parseErrorMessage(errorBody: String?, statusCode: Int): String {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                val jsonObject = JSONObject(errorBody)
                // Try to get the 'detail' field first (as shown in your API response)
                if (jsonObject.has("detail")) {
                    jsonObject.getString("detail")
                }
                // Try to get 'message' field as fallback
                else if (jsonObject.has("message")) {
                    jsonObject.getString("message")
                }
                // Try to get 'error' field as another fallback
                else if (jsonObject.has("error")) {
                    jsonObject.getString("error")
                }
                else {
                    getDefaultErrorMessage(statusCode)
                }
            } else {
                getDefaultErrorMessage(statusCode)
            }
        } catch (e: Exception) {
            // If JSON parsing fails, fallback to default messages
            getDefaultErrorMessage(statusCode)
        }
    }

    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Invalid input. Please check your details."
            401 -> "Unauthorized. Please try again."
            409 -> "Email or username already exists."
            422 -> "Invalid email format or weak password."
            500 -> "Server error. Please try again later."
            else -> "Signup failed. Please try again."
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        val currentState = _uiState.value

        // Reset field errors
        _uiState.value = currentState.copy(
            emailError = null,
            usernameError = null,
            passwordError = null,
            confirmPasswordError = null
        )

        // Email validation
        if (email.value.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "Email is required")
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.value.trim()).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Please enter a valid email")
            isValid = false
        }

        // Username validation
        if (username.value.isBlank()) {
            _uiState.value = _uiState.value.copy(usernameError = "Username is required")
            isValid = false
        } else if (username.value.trim().length < 3) {
            _uiState.value = _uiState.value.copy(usernameError = "Username must be at least 3 characters")
            isValid = false
        } else if (!username.value.trim().matches(Regex("^[a-zA-Z0-9_]+$"))) {
            _uiState.value = _uiState.value.copy(usernameError = "Username can only contain letters, numbers, and underscores")
            isValid = false
        }

        // Password validation
        if (password.value.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "Password is required")
            isValid = false
        } else if (password.value.length < 6) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must be at least 6 characters")
            isValid = false
        } else if (!password.value.matches(Regex(".*[A-Za-z].*"))) {
            _uiState.value = _uiState.value.copy(passwordError = "Password must contain at least one letter")
            isValid = false
        }

        // Confirm password validation
        if (confirmPassword.value.isBlank()) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Please confirm your password")
            isValid = false
        } else if (confirmPassword.value != password.value) {
            _uiState.value = _uiState.value.copy(confirmPasswordError = "Passwords do not match")
            isValid = false
        }

        return isValid
    }
}




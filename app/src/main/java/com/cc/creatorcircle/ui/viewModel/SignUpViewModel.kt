package com.cc.creatorcircle.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Patterns
import org.json.JSONObject

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

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetSignUpState() {
        _uiState.value = SignUpUiState()
        email.value = ""
        username.value = ""
        password.value = ""
        confirmPassword.value = ""
    }

    fun signUp() {
        if (!validateInputs()) return

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val response = repository.registerUser(
                    SignUpRequest(
                        email = email.value.trim(),
                        username = username.value.trim(),
                        password = password.value
                    )
                )

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                } else {
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
                        else -> e.localizedMessage ?: "Unknown error occurred. Please try again."
                    }
                )
            }
        }
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
        var valid = true
        var emailErr: String? = null
        var usernameErr: String? = null
        var passwordErr: String? = null
        var confirmErr: String? = null

        // Email validation
        if (email.value.isBlank()) {
            emailErr = "Email is required"
            valid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.value.trim()).matches()) {
            emailErr = "Please enter a valid email address"
            valid = false
        }

        // Username validation
        if (username.value.isBlank()) {
            usernameErr = "Username is required"
            valid = false
        } else if (username.value.trim().length < 3) {
            usernameErr = "Username must be at least 3 characters"
            valid = false
        } else if (!username.value.trim().matches(Regex("^[a-zA-Z0-9_]+$"))) {
            usernameErr = "Username can only contain letters, numbers, and underscores"
            valid = false
        }

        // Password validation
        if (password.value.isBlank()) {
            passwordErr = "Password is required"
            valid = false
        } else if (password.value.length < 6) {
            passwordErr = "Password must be at least 6 characters"
            valid = false
        } else if (!password.value.matches(Regex(".*[A-Za-z].*"))) {
            passwordErr = "Password must contain at least one letter"
            valid = false
        }

        // Confirm password validation
        if (confirmPassword.value.isBlank()) {
            confirmErr = "Please confirm your password"
            valid = false
        } else if (confirmPassword.value != password.value) {
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










//package com.cc.creatorcircle.ui.viewModel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.cc.creatorcircle.data.models.SignUpRequest
//import com.cc.creatorcircle.data.repository.AuthRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import android.util.Patterns
//import org.json.JSONObject
//
//data class SignUpUiState(
//    val isLoading: Boolean = false,
//    val isSuccess: Boolean = false,
//    val errorMessage: String? = null,
//    val emailError: String? = null,
//    val usernameError: String? = null,
//    val passwordError: String? = null,
//    val confirmPasswordError: String? = null
//)
//
//class SignUpViewModel : ViewModel() {
//
//    private val repository = AuthRepository()
//
//    val email = MutableStateFlow("")
//    val username = MutableStateFlow("")
//    val password = MutableStateFlow("")
//    val confirmPassword = MutableStateFlow("")
//
//    private val _uiState = MutableStateFlow(SignUpUiState())
//    val uiState: StateFlow<SignUpUiState> = _uiState
//
//    fun updateEmail(value: String) {
//        email.value = value
//        // Clear email error when user starts typing
//        if (_uiState.value.emailError != null) {
//            _uiState.value = _uiState.value.copy(emailError = null)
//        }
//    }
//
//    fun updateUsername(value: String) {
//        username.value = value
//        // Clear username error when user starts typing
//        if (_uiState.value.usernameError != null) {
//            _uiState.value = _uiState.value.copy(usernameError = null)
//        }
//    }
//
//    fun updatePassword(value: String) {
//        password.value = value
//        // Clear password error when user starts typing
//        if (_uiState.value.passwordError != null) {
//            _uiState.value = _uiState.value.copy(passwordError = null)
//        }
//    }
//
//    fun updateConfirmPassword(value: String) {
//        confirmPassword.value = value
//        // Clear confirm password error when user starts typing
//        if (_uiState.value.confirmPasswordError != null) {
//            _uiState.value = _uiState.value.copy(confirmPasswordError = null)
//        }
//    }
//
//    fun clearError() {
//        _uiState.value = _uiState.value.copy(errorMessage = null)
//    }
//
//    fun resetSignUpState() {
//        _uiState.value = SignUpUiState()
//        email.value = ""
//        username.value = ""
//        password.value = ""
//        confirmPassword.value = ""
//    }
//
//    fun signUpWithGoogle(idToken: String) {
//        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.registerWithGoogle(idToken)
//
//                if (response.isSuccessful) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        isSuccess = true,
//                        errorMessage = null
//                    )
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    val errorMessage = parseErrorMessage(errorBody, response.code())
//
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = errorMessage
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    errorMessage = when (e) {
//                        is java.net.UnknownHostException -> "No internet connection. Please check your network."
//                        is java.net.SocketTimeoutException -> "Request timeout. Please try again."
//                        else -> e.localizedMessage ?: "Google signup failed. Please try again."
//                    }
//                )
//            }
//        }
//    }
//
//    fun signUp() {
//        if (!validateInputs()) return
//
//        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
//
//        viewModelScope.launch {
//            try {
//                val response = repository.registerUser(
//                    SignUpRequest(
//                        email = email.value.trim(),
//                        username = username.value.trim(),
//                        password = password.value
//                    )
//                )
//
//                if (response.isSuccessful) {
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        isSuccess = true,
//                        errorMessage = null
//                    )
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    val errorMessage = parseErrorMessage(errorBody, response.code())
//
//                    _uiState.value = _uiState.value.copy(
//                        isLoading = false,
//                        errorMessage = errorMessage
//                    )
//                }
//            } catch (e: Exception) {
//                _uiState.value = _uiState.value.copy(
//                    isLoading = false,
//                    errorMessage = when (e) {
//                        is java.net.UnknownHostException -> "No internet connection. Please check your network."
//                        is java.net.SocketTimeoutException -> "Request timeout. Please try again."
//                        else -> e.localizedMessage ?: "Unknown error occurred. Please try again."
//                    }
//                )
//            }
//        }
//    }
//
//    private fun parseErrorMessage(errorBody: String?, statusCode: Int): String {
//        return try {
//            if (!errorBody.isNullOrEmpty()) {
//                val jsonObject = JSONObject(errorBody)
//                // Try to get the 'detail' field first (as shown in your API response)
//                if (jsonObject.has("detail")) {
//                    jsonObject.getString("detail")
//                }
//                // Try to get 'message' field as fallback
//                else if (jsonObject.has("message")) {
//                    jsonObject.getString("message")
//                }
//                // Try to get 'error' field as another fallback
//                else if (jsonObject.has("error")) {
//                    jsonObject.getString("error")
//                }
//                else {
//                    getDefaultErrorMessage(statusCode)
//                }
//            } else {
//                getDefaultErrorMessage(statusCode)
//            }
//        } catch (e: Exception) {
//            // If JSON parsing fails, fallback to default messages
//            getDefaultErrorMessage(statusCode)
//        }
//    }
//
//    private fun getDefaultErrorMessage(statusCode: Int): String {
//        return when (statusCode) {
//            400 -> "Invalid input. Please check your details."
//            401 -> "Unauthorized. Please try again."
//            409 -> "Email or username already exists."
//            422 -> "Invalid email format or weak password."
//            500 -> "Server error. Please try again later."
//            else -> "Signup failed. Please try again."
//        }
//    }
//
//    private fun validateInputs(): Boolean {
//        var valid = true
//        var emailErr: String? = null
//        var usernameErr: String? = null
//        var passwordErr: String? = null
//        var confirmErr: String? = null
//
//        // Email validation
//        if (email.value.isBlank()) {
//            emailErr = "Email is required"
//            valid = false
//        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.value.trim()).matches()) {
//            emailErr = "Please enter a valid email address"
//            valid = false
//        }
//
//        // Username validation
//        if (username.value.isBlank()) {
//            usernameErr = "Username is required"
//            valid = false
//        } else if (username.value.trim().length < 3) {
//            usernameErr = "Username must be at least 3 characters"
//            valid = false
//        } else if (!username.value.trim().matches(Regex("^[a-zA-Z0-9_]+$"))) {
//            usernameErr = "Username can only contain letters, numbers, and underscores"
//            valid = false
//        }
//
//        // Password validation
//        if (password.value.isBlank()) {
//            passwordErr = "Password is required"
//            valid = false
//        } else if (password.value.length < 6) {
//            passwordErr = "Password must be at least 6 characters"
//            valid = false
//        } else if (!password.value.matches(Regex(".*[A-Za-z].*"))) {
//            passwordErr = "Password must contain at least one letter"
//            valid = false
//        }
//
//        // Confirm password validation
//        if (confirmPassword.value.isBlank()) {
//            confirmErr = "Please confirm your password"
//            valid = false
//        } else if (confirmPassword.value != password.value) {
//            confirmErr = "Passwords do not match"
//            valid = false
//        }
//
//        _uiState.value = _uiState.value.copy(
//            emailError = emailErr,
//            usernameError = usernameErr,
//            passwordError = passwordErr,
//            confirmPasswordError = confirmErr
//        )
//
//        return valid
//    }
//}
package com.cc.creatorcircle.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.models.SignUpResponse
import com.cc.creatorcircle.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val signUpResponse: SignUpResponse? = null
)

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Replace the direct Response with a more user-friendly UI state
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // Keep the old state for backward compatibility if needed
    private val _signUpState = MutableStateFlow<Response<SignUpResponse>?>(null)
    val signUpState: StateFlow<Response<SignUpResponse>?> = _signUpState

    fun signUp(email: String, username: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = SignUpRequest(email, username, password)
                // Use the instance method instead of companion object
                val response = repository.registerUser(request)

                // Update the old state for backward compatibility
                _signUpState.value = response

                // Update the new UI state
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        signUpResponse = response.body(),
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = getErrorMessage(response.code(), response.errorBody()?.string())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = when (e) {
                        is UnknownHostException -> "No internet connection. Please check your network."
                        is SocketTimeoutException -> "Request timeout. Please try again."
                        else -> e.localizedMessage ?: "Sign up failed. Please try again."
                    }
                )
            }
        }
    }

    fun signUpWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val response = repository.registerWithGoogle(idToken)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = false,
                        errorMessage = getErrorMessage(response.code(), response.errorBody()?.string())
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = false,
                    errorMessage = when (e) {
                        is UnknownHostException -> "No internet connection. Please check your network."
                        is SocketTimeoutException -> "Request timeout. Please try again."
                        else -> e.localizedMessage ?: "Google sign up failed. Please try again."
                    }
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetState() {
        _uiState.value = AuthUiState()
        _signUpState.value = null
    }

    private fun getErrorMessage(statusCode: Int, errorBody: String?): String {
        return try {
            if (!errorBody.isNullOrEmpty()) {
                val jsonObject = JSONObject(errorBody)
                when {
                    jsonObject.has("detail") -> jsonObject.getString("detail")
                    jsonObject.has("message") -> jsonObject.getString("message")
                    jsonObject.has("error") -> jsonObject.getString("error")
                    else -> getDefaultErrorMessage(statusCode)
                }
            } else {
                getDefaultErrorMessage(statusCode)
            }
        } catch (e: Exception) {
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
            else -> "Sign up failed. Please try again."
        }
    }
}
package com.cc.creatorcircle.viewModel


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.CancelConnectionResponse
import com.cc.creatorcircle.data.models.Connection
import com.cc.creatorcircle.data.models.ConnectionActionResponse
import com.cc.creatorcircle.data.models.ConnectionResponse
import com.cc.creatorcircle.data.models.RemoveConnectionResponse
import com.cc.creatorcircle.data.repository.ConnectionRepository
import kotlinx.coroutines.launch

class ConnectionViewModel(application: Application) : AndroidViewModel(application) {

    private val connectionRepository = ConnectionRepository(application)

    // Connection request state
    private val _connectionRequestState = MutableLiveData<ConnectionRequestState>()
    val connectionRequestState: LiveData<ConnectionRequestState> = _connectionRequestState

    // Connection response data
    private val _connectionResponse = MutableLiveData<ConnectionResponse?>()
    val connectionResponse: LiveData<ConnectionResponse?> = _connectionResponse

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // List of sent connection requests (for tracking)
    private val _sentConnections = MutableLiveData<List<Connection>>(emptyList())
    val sentConnections: LiveData<List<Connection>> = _sentConnections

    fun sendConnectionRequest(userId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _connectionRequestState.value = ConnectionRequestState.Loading
                _errorMessage.value = null

                Log.d("ConnectionViewModel", "Sending connection request to user: $userId")

                val result = connectionRepository.sendConnectionRequest(userId)

                result.fold(
                    onSuccess = { connectionResponse ->
                        _connectionResponse.value = connectionResponse
                        _connectionRequestState.value =
                            ConnectionRequestState.Success(connectionResponse)

                        // Add to sent connections list
                        val currentList = _sentConnections.value?.toMutableList() ?: mutableListOf()
                        currentList.add(connectionResponse.connection)
                        _sentConnections.value = currentList

                        Log.d(
                            "ConnectionViewModel",
                            "Connection request sent successfully: ${connectionResponse.message}"
                        )
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to send connection request"
                        _errorMessage.value = errorMsg
                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                        Log.e("ConnectionViewModel", "Failed to send connection request: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = "Unexpected error: ${e.message}"
                _errorMessage.value = errorMsg
                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                Log.e("ConnectionViewModel", "Exception in sendConnectionRequest", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun removeConnection(userId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _connectionRequestState.value = ConnectionRequestState.Loading
                _errorMessage.value = null

                Log.d("ConnectionViewModel", "Removing connection for userId: $userId")

                val result = connectionRepository.removeConnection(userId)

                result.fold(
                    onSuccess = { removeConnectionResponse ->
                        Log.d(
                            "ConnectionViewModel",
                            "Connection removed successfully: ${removeConnectionResponse.message}"
                        )
                        _connectionRequestState.value =
                            RemoveConnectionRequestState.Success(removeConnectionResponse)
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to remove connection"
                        _errorMessage.value = errorMsg
                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                        Log.e("ConnectionViewModel", "Failed to remove connection: $errorMsg")
                    }
                )
            } catch (e: Exception) {
                val errorMsg = "Unexpected error while removing connection: ${e.message}"
                _errorMessage.value = errorMsg
                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                Log.e("ConnectionViewModel", "Exception in removeConnection", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelConnection(userId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _connectionRequestState.value = ConnectionRequestState.Loading
                _errorMessage.value = null

                Log.d("ConnectionViewModel", "Canceling connection request for userId: $userId")

                val result = connectionRepository.cancelConnection(userId)

                result.fold(
                    onSuccess = { cancelConnectionResponse ->
                        Log.d(
                            "ConnectionViewModel",
                            "Connection request canceled successfully: ${cancelConnectionResponse.message}"
                        )
                        _connectionRequestState.value =
                            CancelConnectionRequestState.Success(cancelConnectionResponse)
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Failed to cancel connection request"
                        _errorMessage.value = errorMsg
                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                        Log.e(
                            "ConnectionViewModel",
                            "Failed to cancel connection request: $errorMsg"
                        )
                    }
                )
            } catch (e: Exception) {
                val errorMsg = "Unexpected error while canceling connection request: ${e.message}"
                _errorMessage.value = errorMsg
                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                Log.e("ConnectionViewModel", "Exception in cancelConnection", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun RespondConnectionRequest(connectionId: Int, action: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _connectionRequestState.value = ConnectionRequestState.Loading
                _errorMessage.value = null

                Log.d(
                    "ConnectionViewModel",
                    "Responding to connection request - ID: $connectionId, Action: $action"
                )

                val result = connectionRepository.RespondConnectionRequest(
                    connectionId = connectionId,
                    action = action
                )

                result.fold(
                    onSuccess = { connectionActionResponse ->
                        Log.d(
                            "ConnectionViewModel",
                            "Connection request $action completed successfully for ID: $connectionId. Response: ${connectionActionResponse.message}"
                        )
                        _connectionRequestState.value =
                            RespondConnectionRequestState.Success(connectionActionResponse)
                    },
                    onFailure = { exception ->
                        val errorMsg =
                            exception.message ?: "Failed to respond to connection request"
                        _errorMessage.value = errorMsg
                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                        Log.e(
                            "ConnectionViewModel",
                            "Failed to respond to connection request - ID: $connectionId, Action: $action, Error: $errorMsg"
                        )
                    }
                )
            } catch (e: Exception) {
                val errorMsg =
                    "Unexpected error while responding to connection request: ${e.message}"
                _errorMessage.value = errorMsg
                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)

                Log.e(
                    "ConnectionViewModel",
                    "Exception in RespondConnectionRequest - ID: $connectionId, Action: $action",
                    e
                )
            } finally {
                _isLoading.value = false
                Log.d(
                    "ConnectionViewModel",
                    "RespondConnectionRequest operation completed for ID: $connectionId"
                )
            }
        }
    }

//    fun RespondConnectionRequest(connectionId: Int, action: String) {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                _connectionRequestState.value = ConnectionRequestState.Loading
//                _errorMessage.value = null
//
//                Log.d("ConnectionViewModel", "Canceling connection request for userId: $connectionId")
//
//                val result = connectionRepository.RespondConnectionRequest(connectionId = connectionId, action = action)
//
//                result.fold(
//                    onSuccess = { connectionActionResponse ->
//                        Log.d(
//                            "ConnectionViewModel",
//                            "Connection request canceled successfully: ${connectionActionResponse.message}"
//                        )
//                        _connectionRequestState.value =
//                            RespondConnectionRequestState.Success(connectionActionResponse)
//                    },
//                    onFailure = { exception ->
//                        val errorMsg = exception.message ?: "Failed to cancel connection request"
//                        _errorMessage.value = errorMsg
//                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)
//
//                        Log.e(
//                            "ConnectionViewModel",
//                            "Failed to cancel connection request: $errorMsg"
//                        )
//                    }
//                )
//            } catch (e: Exception) {
//                val errorMsg = "Unexpected error while canceling connection request: ${e.message}"
//                _errorMessage.value = errorMsg
//                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)
//
//                Log.e("ConnectionViewModel", "Exception in cancelConnection", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }


    fun clearError() {
        _errorMessage.value = null
    }

    fun clearConnectionResponse() {
        _connectionResponse.value = null
        _connectionRequestState.value = ConnectionRequestState.Idle
    }

    // Check if connection already sent to avoid duplicate requests
    fun isConnectionAlreadySent(userId: Int): Boolean {
        return _sentConnections.value?.any { it.userId == userId } ?: false
    }

    // Get connection by user ID
    fun getConnectionByUserId(userId: Int): Connection? {
        return _sentConnections.value?.find { it.userId == userId }
    }

    // Remove connection from local list (if needed for UI updates)
    fun removeConnectionFromList(userId: Int) {
        val currentList = _sentConnections.value?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.userId == userId }
        _sentConnections.value = currentList
    }
}

sealed class ConnectionRequestState {
    object Idle : ConnectionRequestState()
    object Loading : ConnectionRequestState()
    data class Success(val connectionResponse: ConnectionResponse) : ConnectionRequestState()
    data class Error(val message: String) : ConnectionRequestState()
}

sealed class RemoveConnectionRequestState {
    object Idle : ConnectionRequestState()
    object Loading : ConnectionRequestState()
    data class Success(val removeConnectionResponse: RemoveConnectionResponse) :
        ConnectionRequestState()

    data class Error(val message: String) : ConnectionRequestState()
}


sealed class CancelConnectionRequestState {
    object Idle : ConnectionRequestState()
    object Loading : ConnectionRequestState()
    data class Success(val cancelConnectionResponse: CancelConnectionResponse) :
        ConnectionRequestState()

    data class Error(val message: String) : ConnectionRequestState()
}

sealed class RespondConnectionRequestState {
    object Idle : ConnectionRequestState()
    object Loading : ConnectionRequestState()
    data class Success(val cancelConnectionResponse: ConnectionActionResponse) :
        ConnectionRequestState()

    data class Error(val message: String) : ConnectionRequestState()
}


//
//    fun removeConnection(userId: Int) {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                _connectionRequestState.value = ConnectionRequestState.Loading
//                _errorMessage.value = null
//
//                Log.d("ConnectionViewModel", "Removing connection: $userId")
//
//                val result = connectionRepository.removeConnection(userId)
//
//                result.fold(
//                    onSuccess = { removeConnectionResponse ->
//
//
//                        Log.d(
//                            "ConnectionViewModel",
//                            "Connection Removed successfully: ${removeConnectionResponse.message}"
//                        )
//                    },
//                    onFailure = { exception ->
//                        val errorMsg = exception.message ?: "Failed to send connection request"
//                        _errorMessage.value = errorMsg
//                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)
//
//                        Log.e("ConnectionViewModel", "Failed to send connection request: $errorMsg")
//                    }
//                )
//            } catch (e: Exception) {
//                val errorMsg = "Unexpected error: ${e.message}"
//                _errorMessage.value = errorMsg
//                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)
//
//                Log.e("ConnectionViewModel", "Exception in sendConnectionRequest", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//
//    fun cancelConnection(userId: Int) {
//        viewModelScope.launch {
//            try {
//                _isLoading.value = true
//                _connectionRequestState.value = ConnectionRequestState.Loading
//                _errorMessage.value = null
//
//                Log.d("ConnectionViewModel", "Removing connection: $userId")
//
//                val result = connectionRepository.cancelConnection(userId)
//
//                result.fold(
//                    onSuccess = { cancelConnectionResponse ->
//
//
//                        Log.d(
//                            "ConnectionViewModel",
//                            "Connection Removed successfully: ${cancelConnectionResponse.message}"
//                        )
//                    },
//                    onFailure = { exception ->
//                        val errorMsg = exception.message ?: "Failed to send connection request"
//                        _errorMessage.value = errorMsg
//                        _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)
//
//                        Log.e("ConnectionViewModel", "Failed to send connection request: $errorMsg")
//                    }
//                )
//            } catch (e: Exception) {
//                val errorMsg = "Unexpected error: ${e.message}"
//                _errorMessage.value = errorMsg
//                _connectionRequestState.value = ConnectionRequestState.Error(errorMsg)
//
//                Log.e("ConnectionViewModel", "Exception in sendConnectionRequest", e)
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//

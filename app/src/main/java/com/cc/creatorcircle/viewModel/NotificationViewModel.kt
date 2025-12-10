package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.Notification
import com.cc.creatorcircle.data.repository.NotificationRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val context: Context) : ViewModel() {

    private val repository = NotificationRepository(context)
    private val tokenManager = TokenManager(context)

    // All Notifications State
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _notificationsLoading = MutableStateFlow(false)
    val notificationsLoading: StateFlow<Boolean> = _notificationsLoading.asStateFlow()

    private val _notificationsError = MutableStateFlow<String?>(null)
    val notificationsError: StateFlow<String?> = _notificationsError.asStateFlow()

    // Unread Notifications State
    private val _unreadNotifications = MutableStateFlow<List<Notification>>(emptyList())
    val unreadNotifications: StateFlow<List<Notification>> = _unreadNotifications.asStateFlow()

    private val _unreadNotificationsLoading = MutableStateFlow(false)
    val unreadNotificationsLoading: StateFlow<Boolean> = _unreadNotificationsLoading.asStateFlow()

    private val _unreadNotificationsError = MutableStateFlow<String?>(null)
    val unreadNotificationsError: StateFlow<String?> = _unreadNotificationsError.asStateFlow()

    // Notification Count State
    private val _notificationCount = MutableStateFlow(0)
    val notificationCount: StateFlow<Int> = _notificationCount.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()


    private val _markAllReadLoading = MutableStateFlow(false)
    val markAllReadLoading: StateFlow<Boolean> = _markAllReadLoading.asStateFlow()

    private val _markAllReadSuccess = MutableStateFlow(false)
    val markAllReadSuccess: StateFlow<Boolean> = _markAllReadSuccess.asStateFlow()

    private val _markAllReadError = MutableStateFlow<String?>(null)
    val markAllReadError: StateFlow<String?> = _markAllReadError.asStateFlow()





    private val _markReadLoading = MutableStateFlow(false)
    val markReadLoading: StateFlow<Boolean> = _markReadLoading.asStateFlow()

    private val _markReadSuccess = MutableStateFlow<Int?>(null)
    val markReadSuccess: StateFlow<Int?> = _markReadSuccess.asStateFlow()

    private val _markReadError = MutableStateFlow<String?>(null)
    val markReadError: StateFlow<String?> = _markReadError.asStateFlow()





    /**
     * Fetch all notifications
     */
    fun fetchAllNotifications() {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "=== FETCHING ALL NOTIFICATIONS ===")
                _notificationsLoading.value = true
                _notificationsError.value = null

                repository.getAllNotifications()
                    .onSuccess { notificationResponse ->
                        Log.d("NotificationViewModel", "✅ SUCCESS: All notifications fetched")
                        Log.d("NotificationViewModel", "Total notifications: ${notificationResponse.size}")

                        _notifications.value = notificationResponse
                        _notificationCount.value = notificationResponse.size

                        // Also update unread count
                        val unreadCount = notificationResponse.count { !it.isRead }
                        _unreadCount.value = unreadCount

                        Log.d("NotificationViewModel", "Unread notifications: $unreadCount")
                    }
                    .onFailure { exception ->
                        Log.e("NotificationViewModel", "❌ FAILED: to fetch all notifications", exception)
                        _notificationsError.value = exception.message ?: "Failed to fetch notifications"
                    }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "💥 EXCEPTION in fetchAllNotifications", e)
                _notificationsError.value = "Network error: ${e.message}"
            } finally {
                _notificationsLoading.value = false
                Log.d("NotificationViewModel", "=== ALL NOTIFICATIONS FETCH COMPLETE ===")
            }
        }
    }

    /**
     * Fetch only unread notifications
     */
    fun fetchUnreadNotifications() {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "=== FETCHING UNREAD NOTIFICATIONS ===")
                _unreadNotificationsLoading.value = true
                _unreadNotificationsError.value = null

                repository.getUnreadNotifications()
                    .onSuccess { notificationResponse ->
                        Log.d("NotificationViewModel", "✅ SUCCESS: Unread notifications fetched")
                        Log.d("NotificationViewModel", "Unread count: ${notificationResponse.size}")

                        _unreadNotifications.value = notificationResponse
                        _unreadCount.value = notificationResponse.size

                        // Log notification types for debugging
                        notificationResponse.groupBy { it.type }.forEach { (type, notifications) ->
                            Log.d("NotificationViewModel", "Type '$type': ${notifications.size} notifications")
                        }
                    }
                    .onFailure { exception ->
                        Log.e("NotificationViewModel", "❌ FAILED: to fetch unread notifications", exception)
                        _unreadNotificationsError.value = exception.message ?: "Failed to fetch unread notifications"
                    }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "💥 EXCEPTION in fetchUnreadNotifications", e)
                _unreadNotificationsError.value = "Network error: ${e.message}"
            } finally {
                _unreadNotificationsLoading.value = false
                Log.d("NotificationViewModel", "=== UNREAD NOTIFICATIONS FETCH COMPLETE ===")
            }
        }
    }

    /**
     * Refresh all notifications
     */
    fun refreshAllNotifications() {
        fetchAllNotifications()
    }

    /**
     * Refresh unread notifications
     */
    fun refreshUnreadNotifications() {
        fetchUnreadNotifications()
    }

    /**
     * Get notifications by type
     */
    fun getNotificationsByType(type: String): List<Notification> {
        return _notifications.value.filter { it.type == type }
    }

    /**
     * Get booking notifications
     */
    fun getBookingNotifications(): List<Notification> {
        return getNotificationsByType("booking")
    }

    /**
     * Get message notifications
     */
    fun getMessageNotifications(): List<Notification> {
        return getNotificationsByType("message")
    }

    /**
     * Get friend request notifications
     */
    fun getFriendRequestNotifications(): List<Notification> {
        return getNotificationsByType("friend_request")
    }

    /**
     * Get post interaction notifications
     */
    fun getPostInteractionNotifications(): List<Notification> {
        return getNotificationsByType("post_interaction")
    }

    /**
     * Get mutual friend notifications
     */
    fun getMutualFriendNotifications(): List<Notification> {
        return getNotificationsByType("mutual_friend")
    }

    /**
     * Mark notification as read locally (optimistic update)
     */
    fun markNotificationAsReadLocally(notificationId: Int) {
        _notifications.value = _notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }

        _unreadNotifications.value = _unreadNotifications.value.filter {
            it.id != notificationId
        }

        // Update unread count
        _unreadCount.value = _notifications.value.count { !it.isRead }
    }

    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        _notifications.value = emptyList()
        _notificationCount.value = 0
        _notificationsError.value = null
    }

    /**
     * Clear unread notifications
     */
    fun clearUnreadNotifications() {
        _unreadNotifications.value = emptyList()
        _unreadCount.value = 0
        _unreadNotificationsError.value = null
    }

    /**
     * Clear all errors
     */
    fun clearErrors() {
        _notificationsError.value = null
        _unreadNotificationsError.value = null
    }

    /**
     * Clear all notification data
     */
    fun clearAllNotificationData() {
        clearAllNotifications()
        clearUnreadNotifications()
        clearErrors()
    }

    /**
     * Check if user has valid token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Get current notifications
     */
    fun getCurrentNotifications(): List<Notification> {
        return _notifications.value
    }

    /**
     * Get current unread notifications
     */
    fun getCurrentUnreadNotifications(): List<Notification> {
        return _unreadNotifications.value
    }

    /**
     * Get notification by ID
     */
    fun getNotificationById(notificationId: Int): Notification? {
        return _notifications.value.find { it.id == notificationId }
    }

    /**
     * Check if there are unread notifications
     */
    fun hasUnreadNotifications(): Boolean {
        return _unreadCount.value > 0
    }





    /**
     * Mark all notifications as read
     */
    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "=== MARKING ALL NOTIFICATIONS AS READ ===")
                _markAllReadLoading.value = true
                _markAllReadError.value = null
                _markAllReadSuccess.value = false

                repository.markAllNotificationsAsRead()
                    .onSuccess { response ->
                        Log.d("NotificationViewModel", "✅ SUCCESS: ${response.message}")
                        _markAllReadSuccess.value = true

                        // Update local state - mark all as read
                        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
                        _unreadNotifications.value = emptyList()
                        _unreadCount.value = 0

                        Log.d("NotificationViewModel", "Local state updated: All notifications marked as read")
                    }
                    .onFailure { exception ->
                        Log.e("NotificationViewModel", "❌ FAILED: to mark all as read", exception)
                        _markAllReadError.value = exception.message ?: "Failed to mark all as read"
                    }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "💥 EXCEPTION in markAllNotificationsAsRead", e)
                _markAllReadError.value = "Network error: ${e.message}"
            } finally {
                _markAllReadLoading.value = false
                Log.d("NotificationViewModel", "=== MARK ALL AS READ COMPLETE ===")
            }
        }
    }

    /**
     * Fetch unread notification count
     */
    fun fetchUnreadCount() {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "=== FETCHING UNREAD COUNT ===")

                repository.getUnreadNotificationCount()
                    .onSuccess { response ->
                        Log.d("NotificationViewModel", "✅ SUCCESS: Unread count = ${response.unreadCount}")
                        _unreadCount.value = response.unreadCount
                    }
                    .onFailure { exception ->
                        Log.e("NotificationViewModel", "❌ FAILED: to fetch unread count", exception)
                    }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "💥 EXCEPTION in fetchUnreadCount", e)
            } finally {
                Log.d("NotificationViewModel", "=== UNREAD COUNT FETCH COMPLETE ===")
            }
        }
    }

    /**
     * Reset mark all read state
     */
    fun resetMarkAllReadState() {
        _markAllReadSuccess.value = false
        _markAllReadError.value = null
    }




    /**
     * Mark a specific notification as read
     */
    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch {
            try {
                Log.d("NotificationViewModel", "=== MARKING NOTIFICATION $notificationId AS READ ===")
                _markReadLoading.value = true
                _markReadError.value = null
                _markReadSuccess.value = null

                // Optimistic update
                markNotificationAsReadLocally(notificationId)

                repository.markNotificationAsRead(notificationId)
                    .onSuccess { response ->
                        Log.d("NotificationViewModel", "✅ SUCCESS: ${response.message}")
                        _markReadSuccess.value = notificationId
                    }
                    .onFailure { exception ->
                        Log.e("NotificationViewModel", "❌ FAILED: to mark notification as read", exception)
                        _markReadError.value = exception.message ?: "Failed to mark notification as read"

                        // Revert optimistic update on failure
                        fetchAllNotifications()
                    }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "💥 EXCEPTION in markNotificationAsRead", e)
                _markReadError.value = "Network error: ${e.message}"

                // Revert optimistic update on failure
                fetchAllNotifications()
            } finally {
                _markReadLoading.value = false
                Log.d("NotificationViewModel", "=== MARK NOTIFICATION AS READ COMPLETE ===")
            }
        }
    }

    /**
     * Reset mark read state
     */
    fun resetMarkReadState() {
        _markReadSuccess.value = null
        _markReadError.value = null
    }
}

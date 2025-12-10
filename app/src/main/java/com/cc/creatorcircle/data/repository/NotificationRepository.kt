package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.MarkAllReadResponse
import com.cc.creatorcircle.data.models.MarkNotificationReadResponse
import com.cc.creatorcircle.data.models.Notification
import com.cc.creatorcircle.data.models.NotificationResponse
import com.cc.creatorcircle.data.models.UnreadCountResponse
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class NotificationRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get notifications with optional unread filter
     */
//    suspend fun getNotifications(unreadOnly: Boolean = false): Result<NotificationResponse> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val token = tokenManager.getToken()
//                if (token.isEmpty()) {
//                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
//                }
//
//                val response = apiService.getNotifications(unreadOnly, "Bearer $token")
//
//                if (response.isSuccessful) {
//                    response.body()?.let { notificationResponse ->
//                        Log.d("NotificationRepository", "Successfully fetched ${notificationResponse.count} notifications")
//                        Result.success(notificationResponse)
//                    } ?: Result.failure(Exception("Empty response body"))
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
//                    Log.e("NotificationRepository", "API Error in getNotifications: ${response.code()} - $errorMessage")
//                    Result.failure(Exception("Failed to get notifications: ${response.code()} - $errorMessage"))
//                }
//            } catch (e: Exception) {
//                Log.e("NotificationRepository", "Exception in getNotifications", e)
//                Result.failure(e)
//            }
//        }
//    }

    /**
     * Get notifications with optional unread filter
     */
    suspend fun getNotifications(unreadOnly: Boolean = false): Result<NotificationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getNotifications(unreadOnly, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { notificationResponse ->
                        Log.d("NotificationRepository", "Successfully fetched ${notificationResponse.size} notifications")
                        Result.success(notificationResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("NotificationRepository", "API Error in getNotifications: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get notifications: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("NotificationRepository", "Exception in getNotifications", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get all notifications
     */
    suspend fun getAllNotifications(): Result<NotificationResponse> {
        return getNotifications(unreadOnly = false)
    }

    /**
     * Get only unread notifications
     */
    suspend fun getUnreadNotifications(): Result<NotificationResponse> {
        return getNotifications(unreadOnly = true)
    }

    /**
     * Alternative method using Response<T> pattern like PostsRepository
     * Get notifications (Response version)
     */
    suspend fun getNotificationsResponse(unreadOnly: Boolean = false): Response<NotificationResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getNotifications(unreadOnly, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Get all notifications (Response version)
     */
    suspend fun getAllNotificationsResponse(): Response<NotificationResponse> {
        return getNotificationsResponse(unreadOnly = false)
    }

    /**
     * Get only unread notifications (Response version)
     */
    suspend fun getUnreadNotificationsResponse(): Response<NotificationResponse> {
        return getNotificationsResponse(unreadOnly = true)
    }






    /**
     * Mark all notifications as read
     */
    suspend fun markAllNotificationsAsRead(): Result<MarkAllReadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.markAllNotificationsAsRead("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { markAllReadResponse ->
                        Log.d("NotificationRepository", "Successfully marked all notifications as read")
                        Result.success(markAllReadResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("NotificationRepository", "API Error in markAllNotificationsAsRead: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to mark all as read: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("NotificationRepository", "Exception in markAllNotificationsAsRead", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Get unread notification count
     */
    suspend fun getUnreadNotificationCount(): Result<UnreadCountResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getUnreadNotificationCount("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { unreadCountResponse ->
                        Log.d("NotificationRepository", "Successfully fetched unread count: ${unreadCountResponse.unreadCount}")
                        Result.success(unreadCountResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("NotificationRepository", "API Error in getUnreadNotificationCount: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get unread count: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("NotificationRepository", "Exception in getUnreadNotificationCount", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Mark all notifications as read (Response version)
     */
    suspend fun markAllNotificationsAsReadResponse(): Response<MarkAllReadResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.markAllNotificationsAsRead("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Get unread notification count (Response version)
     */
    suspend fun getUnreadNotificationCountResponse(): Response<UnreadCountResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getUnreadNotificationCount("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Mark a specific notification as read
     */
    suspend fun markNotificationAsRead(notificationId: Int): Result<MarkNotificationReadResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.markNotificationAsRead(notificationId, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { markReadResponse ->
                        Log.d("NotificationRepository", "Successfully marked notification $notificationId as read")
                        Result.success(markReadResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("NotificationRepository", "API Error in markNotificationAsRead: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to mark notification as read: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("NotificationRepository", "Exception in markNotificationAsRead", e)
                Result.failure(e)
            }
        }
    }



}
package com.cc.creatorcircle.data.repository

import android.content.Context
import android.util.Log
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.data.models.BookSlotRequest
import com.cc.creatorcircle.data.models.BookSlotResponse
import com.cc.creatorcircle.data.models.CancelBookingResponse
import com.cc.creatorcircle.data.models.Influencers
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class BookingRepository(private val context: Context) {

    private val apiService = RetrofitInstance.api
    private val tokenManager = TokenManager(context)

    /**
     * Get suggested influencers for live sessions
     */
    suspend fun getSuggestedInfluencers(): Result<List<Influencers>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.getSuggestedInfluencers("Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { influencers ->
                        Log.d("BookingRepository", "Successfully fetched ${influencers.size} suggested influencers")
                        Result.success(influencers)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("BookingRepository", "API Error in getSuggestedInfluencers: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to get suggested influencers: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("BookingRepository", "Exception in getSuggestedInfluencers", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Book a live session slot
     */
    suspend fun bookLiveSessionSlot(
        mentorUserId: Int,
        timeSlotId: Int,
        topic: String,
        startTime: String,
        endTime: String,
        description: String,
        serviceSlotId: Int,
        seekerEmail: String,
        name: String
    ): Result<BookSlotResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val request = BookSlotRequest(
                    mentorUserId = mentorUserId,
                    timeSlotId = timeSlotId,
                    topic = topic,
                    startTime = startTime,
                    endTime = endTime,
                    description = description,
                    serviceSlotId = serviceSlotId,
                    seekerEmail = seekerEmail,
                    name = name
                )

                val response = apiService.bookLiveSessionSlot(request, "Bearer $token")

                if (response.isSuccessful) {
                    response.body()?.let { bookingResponse ->
                        Log.d("BookingRepository", "Successfully booked slot - ID: ${bookingResponse.id}, Status: ${bookingResponse.status}")
                        Result.success(bookingResponse)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                    Log.e("BookingRepository", "API Error in bookLiveSessionSlot: ${response.code()} - $errorMessage")
                    Result.failure(Exception("Failed to book slot: ${response.code()} - $errorMessage"))
                }
            } catch (e: Exception) {
                Log.e("BookingRepository", "Exception in bookLiveSessionSlot", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Cancel a booking
     */
//    suspend fun cancelBooking(bookingId: Int): Result<CancelBookingResponse> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val token = tokenManager.getToken()
//                if (token.isEmpty()) {
//                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
//                }
//
//                val response = apiService.cancelBooking(bookingId, "Bearer $token")
//
//                if (response.isSuccessful) {
//                    response.body()?.let { cancelResponse ->
//                        Log.d("BookingRepository", "Successfully cancelled booking - ID: $bookingId, Status: ${cancelResponse.bookingStatus}")
//                        Log.d("BookingRepository", "Cancelled by: ${cancelResponse.cancelledBy}, Refunded: ${cancelResponse.refundedPayments}")
//                        Result.success(cancelResponse)
//                    } ?: Result.failure(Exception("Empty response body"))
//                } else {
//                    val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
//                    Log.e("BookingRepository", "API Error in cancelBooking: ${response.code()} - $errorMessage")
//                    Result.failure(Exception("Failed to cancel booking: ${response.code()} - $errorMessage"))
//                }
//            } catch (e: Exception) {
//                Log.e("BookingRepository", "Exception in cancelBooking", e)
//                Result.failure(e)
//            }
//        }
//    }





    suspend fun cancelBooking(bookingId: Int): Result<CancelBookingResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.getToken()
                if (token.isEmpty()) {
                    return@withContext Result.failure(Exception("Access token not found. Please log in again."))
                }

                val response = apiService.cancelBooking(bookingId, "Bearer $token")

                when {
                    response.isSuccessful -> {
                        response.body()?.let { cancelResponse ->
                            Log.d("BookingRepository", "Successfully cancelled booking - ID: $bookingId")
                            Result.success(cancelResponse)
                        } ?: Result.failure(Exception("Empty response body"))
                    }
                    response.code() == 404 -> {
                        Result.failure(Exception("Booking not found. It may have already been cancelled."))
                    }
                    response.code() == 403 -> {
                        Result.failure(Exception("You don't have permission to cancel this booking."))
                    }
                    response.code() == 400 -> {
                        val errorMessage = response.errorBody()?.string() ?: "Invalid request"
                        Result.failure(Exception("Cannot cancel: $errorMessage"))
                    }
                    response.code() == 500 -> {
                        Result.failure(Exception("Server error while cancelling. Please try again later or contact support."))
                    }
                    else -> {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("BookingRepository", "API Error: ${response.code()} - $errorMessage")
                        Result.failure(Exception("Failed to cancel: ${response.code()}"))
                    }
                }
            } catch (e: Exception) {
                Log.e("BookingRepository", "Exception in cancelBooking", e)
                Result.failure(e)
            }
        }
    }










    /**
     * Alternative method using Response<T> pattern
     * Get suggested influencers (Response version)
     */
    suspend fun getSuggestedInfluencersResponse(): Response<List<Influencers>> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.getSuggestedInfluencers("Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Book live session slot (Response version)
     */
    suspend fun bookLiveSessionSlotResponse(
        mentorUserId: Int,
        timeSlotId: Int,
        topic: String,
        startTime: String,
        endTime: String,
        description: String,
        serviceSlotId: Int,
        seekerEmail: String,
        name: String
    ): Response<BookSlotResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            val request = BookSlotRequest(
                mentorUserId = mentorUserId,
                timeSlotId = timeSlotId,
                topic = topic,
                startTime = startTime,
                endTime = endTime,
                description = description,
                serviceSlotId = serviceSlotId,
                seekerEmail = seekerEmail,
                name = name
            )
            apiService.bookLiveSessionSlot(request, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }

    /**
     * Alternative method using Response<T> pattern
     * Cancel booking (Response version)
     */
    suspend fun cancelBookingResponse(bookingId: Int): Response<CancelBookingResponse> {
        val token = tokenManager.getToken()
        return if (token.isNotEmpty()) {
            apiService.cancelBooking(bookingId, "Bearer $token")
        } else {
            throw Exception("Access token not found. Please log in again.")
        }
    }
}


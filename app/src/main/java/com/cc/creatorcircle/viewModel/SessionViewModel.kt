package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.DateRange
import com.cc.creatorcircle.data.models.Session
import com.cc.creatorcircle.data.models.SessionsRequest
import com.cc.creatorcircle.data.models.SessionsState
import com.cc.creatorcircle.data.repository.SessionRepository
import com.cc.creatorcircle.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class SessionViewModel(private val context: Context) : ViewModel() {

    private val repository = SessionRepository(context)
    private val tokenManager = TokenManager(context)

    // Sessions State
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()

    private val _sessionsLoading = MutableStateFlow(false)
    val sessionsLoading: StateFlow<Boolean> = _sessionsLoading.asStateFlow()

    private val _sessionsError = MutableStateFlow<String?>(null)
    val sessionsError: StateFlow<String?> = _sessionsError.asStateFlow()

    // Alternative: Using SessionsState sealed class
    private val _sessionsState = MutableStateFlow<SessionsState>(SessionsState.Idle)
    val sessionsState: StateFlow<SessionsState> = _sessionsState.asStateFlow()

    /**
     * Fetch sessions based on filters
     */
    fun fetchSessions(
        statuses: List<String>,
        fromDate: String? = null,  // Now nullable with default null
        toDate: String? = null,     // Now nullable with default null
        sessionType: String? = null // Now nullable with default null
    ) {
        viewModelScope.launch {
            try {
                Log.d("SessionViewModel", "Fetching sessions")
                Log.d("SessionViewModel", "Statuses: $statuses")
                Log.d("SessionViewModel", "Date Range: $fromDate to $toDate")
                Log.d("SessionViewModel", "Session Type: $sessionType")

                _sessionsLoading.value = true
                _sessionsError.value = null
                _sessionsState.value = SessionsState.Loading

                // Only include date_range if both dates are provided
                val dateRange = if (!fromDate.isNullOrEmpty() && !toDate.isNullOrEmpty()) {
                    DateRange(fromDate = fromDate, toDate = toDate)
                } else {
                    null
                }

                val request = SessionsRequest(
                    statuses = statuses,
                    dateRange = dateRange,      // Will be null if dates not provided
                    sessionType = sessionType   // Will be null if not provided
                )

                repository.getSessions(request)
                    .onSuccess { sessionsResponse ->
                        Log.d("SessionViewModel", "Sessions fetched successfully: ${sessionsResponse.sessions.size} sessions")
                        _sessions.value = sessionsResponse.sessions
                        _sessionsState.value = SessionsState.Success(sessionsResponse)
                    }
                    .onFailure { exception ->
                        Log.e("SessionViewModel", "Failed to fetch sessions", exception)
                        val errorMessage = exception.message ?: "Failed to fetch sessions"
                        _sessionsError.value = errorMessage
                        _sessionsState.value = SessionsState.Error(errorMessage)
                    }
            } catch (e: Exception) {
                Log.e("SessionViewModel", "Exception in fetchSessions", e)
                val errorMessage = "Network error: ${e.message}"
                _sessionsError.value = errorMessage
                _sessionsState.value = SessionsState.Error(errorMessage)
            } finally {
                _sessionsLoading.value = false
            }
        }
    }

    /**
     * Fetch sessions with default parameters
     */
    fun fetchAllSessions() {
        fetchSessions(
            statuses = listOf("upcoming", "completed", "cancelled", "pending"),
            fromDate = "",
            toDate = "",
            sessionType = "all"
        )
    }

    /**
     * Fetch only upcoming sessions
     */
    fun fetchUpcomingSessions(fromDate: String = "", toDate: String = "") {
        fetchSessions(
            statuses = listOf("upcoming"),
            fromDate = fromDate,
            toDate = toDate,
            sessionType = "all"
        )
    }

    /**
     * Fetch only completed sessions
     */
    fun fetchCompletedSessions(fromDate: String = "", toDate: String = "") {
        fetchSessions(
            statuses = listOf("completed"),
            fromDate = fromDate,
            toDate = toDate,
            sessionType = "all"
        )
    }

    /**
     * Fetch cancelled sessions
     */
    fun fetchCancelledSessions(fromDate: String = "", toDate: String = "") {
        fetchSessions(
            statuses = listOf("cancelled"),
            fromDate = fromDate,
            toDate = toDate,
            sessionType = "all"
        )
    }

    /**
     * Fetch sessions by type (provider or seeker)
     */
    fun fetchSessionsByType(
        sessionType: String,
        statuses: List<String> = listOf("upcoming", "completed", "cancelled", "pending"),
        fromDate: String = "",
        toDate: String = ""
    ) {
        fetchSessions(
            statuses = statuses,
            fromDate = fromDate,
            toDate = toDate,
            sessionType = sessionType
        )
    }

    /**
     * Refresh sessions with current filters
     */
    fun refreshSessions(
        statuses: List<String>,
        fromDate: String,
        toDate: String,
        sessionType: String
    ) {
        fetchSessions(statuses, fromDate, toDate, sessionType)
    }

    /**
     * Get sessions filtered by status locally
     */
    fun getSessionsByStatus(status: String): List<Session> {
        return _sessions.value.filter { it.status == status }
    }

    /**
     * Get upcoming sessions
     */
    fun getUpcomingSessions(): List<Session> {
        return _sessions.value.filter { it.status == "upcoming" }
    }

    /**
     * Get completed sessions
     */
    fun getCompletedSessions(): List<Session> {
        return _sessions.value.filter { it.status == "completed" }
    }

    /**
     * Get cancelled sessions
     */
    fun getCancelledSessions(): List<Session> {
        return _sessions.value.filter { it.status == "cancelled" }
    }

    /**
     * Get pending sessions
     */
    fun getPendingSessions(): List<Session> {
        return _sessions.value.filter { it.status == "pending" }
    }

    /**
     * Get session by slot ID
     */
    fun getSessionBySlotId(slotId: Int): Session? {
        return _sessions.value.find { it.slotId == slotId }
    }

    /**
     * Get sessions where current user is provider
     */
    fun getSessionsAsProvider(): List<Session> {
        return _sessions.value.filter { it.currentUserRole == "provider" }
    }

    /**
     * Get sessions where current user is seeker
     */
    fun getSessionsAsSeeker(): List<Session> {
        return _sessions.value.filter { it.currentUserRole == "seeker" }
    }

    /**
     * Clear sessions data
     */
    fun clearSessions() {
        _sessions.value = emptyList()
        _sessionsError.value = null
        _sessionsState.value = SessionsState.Idle
    }

    /**
     * Clear errors
     */
    fun clearErrors() {
        _sessionsError.value = null
    }

    /**
     * Check if user has active token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Get current sessions list
     */
    fun getCurrentSessions(): List<Session> {
        return _sessions.value
    }

    /**
     * Get sessions count
     */
    fun getSessionsCount(): Int {
        return _sessions.value.size
    }

    /**
     * Check if there are any sessions
     */
    fun hasSessions(): Boolean {
        return _sessions.value.isNotEmpty()
    }
}


object DateFormatter {

    /**
     * Formats date from "2025-10-29" to "29 Oct' 25"
     */
    fun formatToDisplayDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val day = date.dayOfMonth
            val month = date.month.getDisplayName(
                java.time.format.TextStyle.SHORT,
                Locale.getDefault()
            )
            val year = date.year.toString().takeLast(2)
            "$day $month' $year"
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }

    /**
     * Formats date from "2025-10-29" to "29 October 2025"
     */
    fun formatToFullDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
            date.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Formats date from "2025-10-29" to "Wed, 29 Oct"
     */
    fun formatToDayMonthDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val dayOfWeek = date.dayOfWeek.getDisplayName(
                java.time.format.TextStyle.SHORT,
                Locale.getDefault()
            )
            val day = date.dayOfMonth
            val month = date.month.getDisplayName(
                java.time.format.TextStyle.SHORT,
                Locale.getDefault()
            )
            "$dayOfWeek, $day $month"
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Parse date string to LocalDate
     */
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

// Extension functions for String
fun String.toDisplayDate(): String = DateFormatter.formatToDisplayDate(this)
fun String.toFullDate(): String = DateFormatter.formatToFullDate(this)
fun String.toDayMonthDate(): String = DateFormatter.formatToDayMonthDate(this)
fun String.toLocalDate(): LocalDate? = DateFormatter.parseDate(this)
package com.cc.creatorcircle.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Helper class for Firebase Analytics events
 * Provides type-safe methods for logging analytics events
 * Automatically includes logged-in user ID in all events
 */
object FirebaseAnalyticsHelper {

    private val analytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    private var loggedInUserId: String? = null
    private lateinit var userDataManager: UserDataManager

    /**
     * Initialize the helper with context to access UserDataManager
     * Call this in your Application class or main activity
     */
    fun initialize(context: Context) {
        userDataManager = UserDataManager(context)
        val userData = userDataManager.getUserData()
        if (userData.userId != -1) {
            loggedInUserId = userData.userId.toString()
        }
    }

    /**
     * Update logged-in user ID (call after login/logout)
     */
    fun updateLoggedInUserId(userId: Int?) {
        loggedInUserId = userId?.toString()
        userId?.let { setUserId(it.toString()) }
    }

    /**
     * Helper function to create Bundle with logged-in user ID
     */
    private fun createBundle(block: Bundle.() -> Unit = {}): Bundle {
        return Bundle().apply {
            loggedInUserId?.let { putString("logged_in_user_id", it) }
            block()
        }
    }

    // Generic Event Logging
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        val bundle = createBundle {
            params.forEach { (key, value) ->
                putString(key, value)
            }
        }
        analytics.logEvent(eventName, bundle)
    }

    // Screen View Events
    fun logScreenView(screenName: String, screenClass: String) {
        val bundle = createBundle {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Post Events
    fun logPostViewed(postId: String, authorId: Int, postType: String = "feed") {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("author_id", authorId.toString())
            putString("post_type", postType)
        }
        analytics.logEvent("post_viewed", bundle)
    }

    fun logPostCreated(
        postId: String,
        hasMedia: Boolean,
        mediaCount: Int,
        contentLength: Int,
        hasLinks: Boolean
    ) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("has_media", if (hasMedia) "yes" else "no")
            putLong("media_count", mediaCount.toLong())
            putLong("content_length", contentLength.toLong())
            putString("has_links", if (hasLinks) "yes" else "no")
        }
        analytics.logEvent("post_created", bundle)
    }

    fun logPostUpdated(postId: String, fieldsUpdated: String) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("fields_updated", fieldsUpdated)
        }
        analytics.logEvent("post_updated", bundle)
    }

    fun logPostDeleted(postId: String, authorId: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("author_id", authorId.toString())
        }
        analytics.logEvent("post_deleted", bundle)
    }

    fun logPostExpanded(postId: String) {
        val bundle = createBundle {
            putString("post_id", postId)
        }
        analytics.logEvent("post_expanded", bundle)
    }

    fun logPostCollapsed(postId: String) {
        val bundle = createBundle {
            putString("post_id", postId)
        }
        analytics.logEvent("post_collapsed", bundle)
    }

    // Engagement Events
    fun logLikePost(postId: String, authorId: Int, isLiked: Boolean) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("author_id", authorId.toString())
            putString("action", if (isLiked) "like" else "unlike")
        }
        analytics.logEvent("post_liked", bundle)
    }

    fun logCommentClick(postId: String, authorId: Int, commentCount: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("author_id", authorId.toString())
            putLong("existing_comments", commentCount.toLong())
        }
        analytics.logEvent("comment_section_opened", bundle)
    }

    fun logCommentSubmitted(postId: String, commentLength: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putLong("comment_length", commentLength.toLong())
        }
        analytics.logEvent("comment_submitted", bundle)
    }

    fun logShareClick(postId: String, authorId: Int) {
        val bundle = createBundle {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "post")
            putString(FirebaseAnalytics.Param.ITEM_ID, postId)
            putString("author_id", authorId.toString())
        }
        analytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
    }

    fun logShareMethodSelected(postId: String, shareMethod: String) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("share_method", shareMethod)
        }
        analytics.logEvent("share_method_selected", bundle)
    }

    // Connection Events
    fun logConnectionRequestSent(targetUserId: Int, source: String = "post") {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
            putString("source", source)
        }
        analytics.logEvent("connection_request_sent", bundle)
    }

    fun logConnectionRequestInitiated(targetUserId: Int, source: String = "unknown") {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
            putString("source", source)
        }
        analytics.logEvent("connection_request_initiated", bundle)
    }

    fun logConnectionRequestSuccess(targetUserId: Int) {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
        }
        analytics.logEvent("connection_request_success", bundle)
    }

    fun logConnectionRequestError(targetUserId: Int, errorMessage: String) {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
            putString("error_message", errorMessage)
        }
        analytics.logEvent("connection_request_error", bundle)
    }

    fun logConnectionRequestCancelled(targetUserId: Int, source: String = "unknown") {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
            putString("source", source)
        }
        analytics.logEvent("connection_request_cancelled", bundle)
    }

    fun logConnectionRequestAccepted(connectionId: String, source: String = "unknown") {
        val bundle = createBundle {
            putString("connection_id", connectionId)
            putString("source", source)
        }
        analytics.logEvent("connection_request_accepted", bundle)
    }

    fun logConnectionRequestRejected(connectionId: String, source: String = "unknown") {
        val bundle = createBundle {
            putString("connection_id", connectionId)
            putString("source", source)
        }
        analytics.logEvent("connection_request_rejected", bundle)
    }

    fun logConnectionRemoved(targetUserId: Int, source: String = "unknown") {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
            putString("source", source)
        }
        analytics.logEvent("connection_removed", bundle)
    }

    fun logRecommendationViewed(userId: Int, score: Float = 0f) {
        val bundle = createBundle {
            putString("user_id", userId.toString())
            if (score > 0f) {
                putString("compatibility_score", String.format("%.2f", score))
            }
        }
        analytics.logEvent("recommendation_viewed", bundle)
    }

    fun logSentConnectionViewed(targetUserId: Int, username: String = "") {
        val bundle = createBundle {
            putString("target_user_id", targetUserId.toString())
            if (username.isNotEmpty()) {
                putString("username", username)
            }
        }
        analytics.logEvent("sent_connection_viewed", bundle)
    }

    // Media Events
    fun logMediaClicked(postId: String, mediaType: String, mediaIndex: Int) {
        val bundle = createBundle {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, mediaType)
            putString(FirebaseAnalytics.Param.ITEM_ID, postId)
            putLong("media_index", mediaIndex.toLong())
        }
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun logVideoStarted(postId: String, videoUrl: String) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("video_url", videoUrl)
        }
        analytics.logEvent("video_started", bundle)
    }

    fun logVideoPaused(postId: String, progress: Float) {
        val bundle = createBundle {
            putString("post_id", postId)
            putLong("progress_percent", (progress * 100).toLong())
        }
        analytics.logEvent("video_paused", bundle)
    }

    fun logVideoCompleted(postId: String) {
        val bundle = createBundle {
            putString("post_id", postId)
        }
        analytics.logEvent("video_completed", bundle)
    }

    fun logLinkClicked(postId: String, linkUrl: String, linkIndex: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("link_url", linkUrl)
            putLong("link_index", linkIndex.toLong())
        }
        analytics.logEvent("link_clicked", bundle)
    }

    // Navigation Events
    fun logTabSelected(tabName: String) {
        val bundle = createBundle {
            putString("tab_name", tabName)
        }
        analytics.logEvent("tab_selected", bundle)
    }

    fun logProfileClicked(userId: Int, source: String) {
        val bundle = createBundle {
            putString("user_id", userId.toString())
            putString("source", source)
        }
        analytics.logEvent("profile_clicked", bundle)
    }

    fun logMessageIconClicked() {
        val bundle = createBundle {
            putString("source", "home_fab")
        }
        analytics.logEvent("message_icon_clicked", bundle)
    }

    // Feed Events
    fun logFeedRefreshed(manual: Boolean) {
        val bundle = createBundle {
            putString("refresh_type", if (manual) "manual" else "automatic")
        }
        analytics.logEvent("feed_refreshed", bundle)
    }

    fun logFeedLoaded(postCount: Int, loadTimeMs: Long) {
        val bundle = createBundle {
            putLong("post_count", postCount.toLong())
            putLong("load_time_ms", loadTimeMs)
        }
        analytics.logEvent("feed_loaded", bundle)
    }

    fun logFeedError(errorMessage: String) {
        val bundle = createBundle {
            putString("error_message", errorMessage)
        }
        analytics.logEvent("feed_error", bundle)
    }

    fun logFeedScrolled(scrollDepth: Int) {
        val bundle = createBundle {
            putLong("scroll_depth", scrollDepth.toLong())
        }
        analytics.logEvent("feed_scrolled", bundle)
    }

    // Dialog Events
    fun logDialogOpened(dialogType: String, postId: String? = null) {
        val bundle = createBundle {
            putString("dialog_type", dialogType)
            postId?.let { putString("post_id", it) }
        }
        analytics.logEvent("dialog_opened", bundle)
    }

    fun logDialogClosed(dialogType: String, action: String) {
        val bundle = createBundle {
            putString("dialog_type", dialogType)
            putString("action", action)
        }
        analytics.logEvent("dialog_closed", bundle)
    }

    // Menu Events
    fun logMenuOpened(postId: String) {
        val bundle = createBundle {
            putString("post_id", postId)
        }
        analytics.logEvent("post_menu_opened", bundle)
    }

    fun logMenuItemSelected(postId: String, menuItem: String) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("menu_item", menuItem)
        }
        analytics.logEvent("post_menu_item_selected", bundle)
    }

    // Error Events
    fun logError(
        errorType: String,
        errorMessage: String,
        context: String,
        fatal: Boolean = false
    ) {
        val bundle = createBundle {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
            putString("context", context)
            putString("fatal", if (fatal) "yes" else "no")
        }
        analytics.logEvent("app_error", bundle)
    }

    // User Properties
    fun setUserProperty(propertyName: String, propertyValue: String) {
        analytics.setUserProperty(propertyName, propertyValue)
    }

    fun setUserId(userId: String) {
        analytics.setUserId(userId)
    }

    // Session Events
    fun logSessionStart() {
        val bundle = createBundle {
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("session_start", bundle)
    }

    fun logSessionEnd(durationSeconds: Long) {
        val bundle = createBundle {
            putLong("session_duration_seconds", durationSeconds)
        }
        analytics.logEvent("session_end", bundle)
    }

    // Feature Usage
    fun logFeatureUsed(featureName: String, source: String = "") {
        val bundle = createBundle {
            putString("feature_name", featureName)
            if (source.isNotEmpty()) {
                putString("source", source)
            }
        }
        analytics.logEvent("feature_used", bundle)
    }

    // Performance Metrics
    fun logPerformanceMetric(metricName: String, durationMs: Long) {
        val bundle = createBundle {
            putString("metric_name", metricName)
            putLong("duration_ms", durationMs)
        }
        analytics.logEvent("performance_metric", bundle)
    }

    // Comment Events
    fun logCommentAdded(postId: String, commentLength: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putLong("comment_length", commentLength.toLong())
        }
        analytics.logEvent("comment_added", bundle)
    }

    fun logReplyAdded(postId: String, parentCommentId: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("parent_comment_id", parentCommentId.toString())
        }
        analytics.logEvent("reply_added", bundle)
    }

    fun logCommentLiked(postId: String, commentId: Int) {
        val bundle = createBundle {
            putString("post_id", postId)
            putString("comment_id", commentId.toString())
        }
        analytics.logEvent("comment_liked", bundle)
    }

    // Profile Events
    fun logProfileLoadStarted() {
        val bundle = createBundle {
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent("profile_load_started", bundle)
    }

    fun logProfileLoaded(userId: Int, postCount: Int, connectionCount: Int) {
        val bundle = createBundle {
            putString("user_id", userId.toString())
            putLong("post_count", postCount.toLong())
            putLong("connection_count", connectionCount.toLong())
        }
        analytics.logEvent("profile_loaded", bundle)
    }

    fun logProfileError(errorMessage: String) {
        val bundle = createBundle {
            putString("error_message", errorMessage)
        }
        analytics.logEvent("profile_load_error", bundle)
    }

    fun logProfileMenuOpened(userId: Int) {
        val bundle = createBundle {
            putString("user_id", userId.toString())
        }
        analytics.logEvent("profile_menu_opened", bundle)
    }

    fun logUserPostsLoadStarted(userId: Int) {
        val bundle = createBundle {
            putString("user_id", userId.toString())
        }
        analytics.logEvent("user_posts_load_started", bundle)
    }

    fun logUserPostsError(errorMessage: String) {
        val bundle = createBundle {
            putString("error_message", errorMessage)
        }
        analytics.logEvent("user_posts_load_error", bundle)
    }

    // Influencer Events
    fun logInfluencerBookingInitiated(
        influencerId: String,
        influencerName: String,
        username: String,
        price: String,
        durationMin: String,
        hasAvailability: Boolean
    ) {
        val bundle = createBundle {
            putString("influencer_id", influencerId)
            putString("influencer_name", influencerName)
            putString("username", username)
            putString("price", price)
            putString("duration_min", durationMin)
            putString("has_availability", if (hasAvailability) "yes" else "no")
        }
        analytics.logEvent("influencer_booking_initiated", bundle)
    }

    fun logInfluencerProfileViewed(
        influencerId: String,
        influencerName: String,
        username: String,
        from: String
    ) {
        val bundle = createBundle {
            putString("influencer_id", influencerId)
            putString("influencer_name", influencerName)
            putString("username", username)
            putString("from", from)
        }
        analytics.logEvent("influencer_profile_viewed", bundle)
    }

    fun logTrendingInfluencerBookingInitiated(
        influencerId: String,
        influencerName: String,
        username: String,
        price: String,
        durationMin: String
    ) {
        val bundle = createBundle {
            putString("influencer_id", influencerId)
            putString("influencer_name", influencerName)
            putString("username", username)
            putString("price", price)
            putString("duration_min", durationMin)
        }
        analytics.logEvent("trending_influencer_booking_initiated", bundle)
    }
}





















//package com.cc.creatorcircle.utils
//
//
//import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.analytics.ktx.analytics
//import com.google.firebase.analytics.ktx.logEvent
//import com.google.firebase.ktx.Firebase
//
///**
// * Helper class for Firebase Analytics events
// * Provides type-safe methods for logging analytics events
// */
//object FirebaseAnalyticsHelper {
//
//    private val analytics: FirebaseAnalytics by lazy {
//        Firebase.analytics
//    }
//
//    // Generic Event Logging
//    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
//        analytics.logEvent(eventName) {
//            params.forEach { (key, value) ->
//                param(key, value)
//            }
//        }
//    }
//
//    // Screen View Events
//    fun logScreenView(screenName: String, screenClass: String) {
//        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
//            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
//            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
//        }
//    }
//
//    // Post Events
//    fun logPostViewed(postId: String, authorId: Int, postType: String = "feed") {
//        analytics.logEvent("post_viewed") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//            param("post_type", postType)
//        }
//    }
//
//    fun logPostCreated(
//        postId: String,
//        hasMedia: Boolean,
//        mediaCount: Int,
//        contentLength: Int,
//        hasLinks: Boolean
//    ) {
//        analytics.logEvent("post_created") {
//            param("post_id", postId)
//            param("has_media", if (hasMedia) "yes" else "no")
//            param("media_count", mediaCount.toLong())
//            param("content_length", contentLength.toLong())
//            param("has_links", if (hasLinks) "yes" else "no")
//        }
//    }
//
//    fun logPostUpdated(postId: String, fieldsUpdated: String) {
//        analytics.logEvent("post_updated") {
//            param("post_id", postId)
//            param("fields_updated", fieldsUpdated)
//        }
//    }
//
//    fun logPostDeleted(postId: String, authorId: Int) {
//        analytics.logEvent("post_deleted") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//        }
//    }
//
//    fun logPostExpanded(postId: String) {
//        analytics.logEvent("post_expanded") {
//            param("post_id", postId)
//        }
//    }
//
//    fun logPostCollapsed(postId: String) {
//        analytics.logEvent("post_collapsed") {
//            param("post_id", postId)
//        }
//    }
//
//    // Engagement Events
//    fun logLikePost(postId: String, authorId: Int, isLiked: Boolean) {
//        analytics.logEvent("post_liked") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//            param("action", if (isLiked) "like" else "unlike")
//        }
//    }
//
//    fun logCommentClick(postId: String, authorId: Int, commentCount: Int) {
//        analytics.logEvent("comment_section_opened") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//            param("existing_comments", commentCount.toLong())
//        }
//    }
//
//    fun logCommentSubmitted(postId: String, commentLength: Int) {
//        analytics.logEvent("comment_submitted") {
//            param("post_id", postId)
//            param("comment_length", commentLength.toLong())
//        }
//    }
//
//    fun logShareClick(postId: String, authorId: Int) {
//        analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
//            param(FirebaseAnalytics.Param.CONTENT_TYPE, "post")
//            param(FirebaseAnalytics.Param.ITEM_ID, postId)
//            param("author_id", authorId.toString())
//        }
//    }
//
//    fun logShareMethodSelected(postId: String, shareMethod: String) {
//        analytics.logEvent("share_method_selected") {
//            param("post_id", postId)
//            param("share_method", shareMethod)
//        }
//    }
//
//    // Connection Events
//    fun logConnectionRequestSent(targetUserId: Int, source: String = "post") {
//        analytics.logEvent("connection_request_sent") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestInitiated(targetUserId: Int, source: String = "unknown") {
//        analytics.logEvent("connection_request_initiated") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestSuccess(targetUserId: Int) {
//        analytics.logEvent("connection_request_success") {
//            param("target_user_id", targetUserId.toString())
//        }
//    }
//
//    fun logConnectionRequestError(targetUserId: Int, errorMessage: String) {
//        analytics.logEvent("connection_request_error") {
//            param("target_user_id", targetUserId.toString())
//            param("error_message", errorMessage)
//        }
//    }
//
//    fun logConnectionRequestCancelled(targetUserId: Int, source: String = "unknown") {
//        analytics.logEvent("connection_request_cancelled") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestAccepted(connectionId: String, source: String = "unknown") {
//        analytics.logEvent("connection_request_accepted") {
//            param("connection_id", connectionId)
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestRejected(connectionId: String, source: String = "unknown") {
//        analytics.logEvent("connection_request_rejected") {
//            param("connection_id", connectionId)
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRemoved(targetUserId: Int, source: String = "unknown") {
//        analytics.logEvent("connection_removed") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logRecommendationViewed(userId: Int, score: Float = 0f) {
//        analytics.logEvent("recommendation_viewed") {
//            param("user_id", userId.toString())
//            if (score > 0f) {
//                param("compatibility_score", String.format("%.2f", score))
//            }
//        }
//    }
//
//    fun logSentConnectionViewed(targetUserId: Int, username: String = "") {
//        analytics.logEvent("sent_connection_viewed") {
//            param("target_user_id", targetUserId.toString())
//            if (username.isNotEmpty()) {
//                param("username", username)
//            }
//        }
//    }
//
//    // Media Events
//    fun logMediaClicked(postId: String, mediaType: String, mediaIndex: Int) {
//        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
//            param(FirebaseAnalytics.Param.CONTENT_TYPE, mediaType)
//            param(FirebaseAnalytics.Param.ITEM_ID, postId)
//            param("media_index", mediaIndex.toLong())
//        }
//    }
//
//    fun logVideoStarted(postId: String, videoUrl: String) {
//        analytics.logEvent("video_started") {
//            param("post_id", postId)
//            param("video_url", videoUrl)
//        }
//    }
//
//    fun logVideoPaused(postId: String, progress: Float) {
//        analytics.logEvent("video_paused") {
//            param("post_id", postId)
//            param("progress_percent", (progress * 100).toLong())
//        }
//    }
//
//    fun logVideoCompleted(postId: String) {
//        analytics.logEvent("video_completed") {
//            param("post_id", postId)
//        }
//    }
//
//    fun logLinkClicked(postId: String, linkUrl: String, linkIndex: Int) {
//        analytics.logEvent("link_clicked") {
//            param("post_id", postId)
//            param("link_url", linkUrl)
//            param("link_index", linkIndex.toLong())
//        }
//    }
//
//    // Navigation Events
//    fun logTabSelected(tabName: String) {
//        analytics.logEvent("tab_selected") {
//            param("tab_name", tabName)
//        }
//    }
//
//    fun logProfileClicked(userId: Int, source: String) {
//        analytics.logEvent("profile_clicked") {
//            param("user_id", userId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logMessageIconClicked() {
//        analytics.logEvent("message_icon_clicked") {
//            param("source", "home_fab")
//        }
//    }
//
//    // Feed Events
//    fun logFeedRefreshed(manual: Boolean) {
//        analytics.logEvent("feed_refreshed") {
//            param("refresh_type", if (manual) "manual" else "automatic")
//        }
//    }
//
//    fun logFeedLoaded(postCount: Int, loadTimeMs: Long) {
//        analytics.logEvent("feed_loaded") {
//            param("post_count", postCount.toLong())
//            param("load_time_ms", loadTimeMs)
//        }
//    }
//
//    fun logFeedError(errorMessage: String) {
//        analytics.logEvent("feed_error") {
//            param("error_message", errorMessage)
//        }
//    }
//
//    fun logFeedScrolled(scrollDepth: Int) {
//        analytics.logEvent("feed_scrolled") {
//            param("scroll_depth", scrollDepth.toLong())
//        }
//    }
//
//    // Dialog Events
//    fun logDialogOpened(dialogType: String, postId: String? = null) {
//        analytics.logEvent("dialog_opened") {
//            param("dialog_type", dialogType)
//            postId?.let { param("post_id", it) }
//        }
//    }
//
//    fun logDialogClosed(dialogType: String, action: String) {
//        analytics.logEvent("dialog_closed") {
//            param("dialog_type", dialogType)
//            param("action", action) // dismissed, confirmed, cancelled
//        }
//    }
//
//    // Menu Events
//    fun logMenuOpened(postId: String) {
//        analytics.logEvent("post_menu_opened") {
//            param("post_id", postId)
//        }
//    }
//
//    fun logMenuItemSelected(postId: String, menuItem: String) {
//        analytics.logEvent("post_menu_item_selected") {
//            param("post_id", postId)
//            param("menu_item", menuItem)
//        }
//    }
//
//    // Error Events
//    fun logError(
//        errorType: String,
//        errorMessage: String,
//        context: String,
//        fatal: Boolean = false
//    ) {
//        analytics.logEvent("app_error") {
//            param("error_type", errorType)
//            param("error_message", errorMessage)
//            param("context", context)
//            param("fatal", if (fatal) "yes" else "no")
//        }
//    }
//
//    // User Properties
//    fun setUserProperty(propertyName: String, propertyValue: String) {
//        analytics.setUserProperty(propertyName, propertyValue)
//    }
//
//    fun setUserId(userId: String) {
//        analytics.setUserId(userId)
//    }
//
//    // Session Events
//    fun logSessionStart() {
//        analytics.logEvent("session_start") {
//            param("timestamp", System.currentTimeMillis())
//        }
//    }
//
//    fun logSessionEnd(durationSeconds: Long) {
//        analytics.logEvent("session_end") {
//            param("session_duration_seconds", durationSeconds)
//        }
//    }
//
//    // Feature Usage
//    fun logFeatureUsed(featureName: String, source: String = "") {
//        analytics.logEvent("feature_used") {
//            param("feature_name", featureName)
//            if (source.isNotEmpty()) {
//                param("source", source)
//            }
//        }
//    }
//
//    // Performance Metrics
//    fun logPerformanceMetric(metricName: String, durationMs: Long) {
//        analytics.logEvent("performance_metric") {
//            param("metric_name", metricName)
//            param("duration_ms", durationMs)
//        }
//    }
//
//    // Comment Events
//    fun logCommentAdded(postId: String, commentLength: Int) {
//        analytics.logEvent("comment_added") {
//            param("post_id", postId)
//            param("comment_length", commentLength.toLong())
//        }
//    }
//
//    fun logReplyAdded(postId: String, parentCommentId: Int) {
//        analytics.logEvent("reply_added") {
//            param("post_id", postId)
//            param("parent_comment_id", parentCommentId.toString())
//        }
//    }
//
//    fun logCommentLiked(postId: String, commentId: Int) {
//        analytics.logEvent("comment_liked") {
//            param("post_id", postId)
//            param("comment_id", commentId.toString())
//        }
//    }
//
//    // Profile Events - NEW FUNCTIONS TO FIX ERRORS
//    fun logProfileLoadStarted() {
//        analytics.logEvent("profile_load_started") {
//            param("timestamp", System.currentTimeMillis())
//        }
//    }
//
//    fun logProfileLoaded(userId: Int, postCount: Int, connectionCount: Int) {
//        analytics.logEvent("profile_loaded") {
//            param("user_id", userId.toString())
//            param("post_count", postCount.toLong())
//            param("connection_count", connectionCount.toLong())
//        }
//    }
//
//    fun logProfileError(errorMessage: String) {
//        analytics.logEvent("profile_load_error") {
//            param("error_message", errorMessage)
//        }
//    }
//
//    fun logProfileMenuOpened(userId: Int) {
//        analytics.logEvent("profile_menu_opened") {
//            param("user_id", userId.toString())
//        }
//    }
//
//    fun logUserPostsLoadStarted(userId: Int) {
//        analytics.logEvent("user_posts_load_started") {
//            param("user_id", userId.toString())
//        }
//    }
//
//    fun logUserPostsError(errorMessage: String) {
//        analytics.logEvent("user_posts_load_error") {
//            param("error_message", errorMessage)
//        }
//    }
//
//
//    // Influencer Events - Type-safe methods
//    fun logInfluencerBookingInitiated(
//        influencerId: String,
//        influencerName: String,
//        username: String,
//        price: String,
//        durationMin: String,
//        hasAvailability: Boolean
//    ) {
//        analytics.logEvent("influencer_booking_initiated") {
//            param("influencer_id", influencerId)
//            param("influencer_name", influencerName)
//            param("username", username)
//            param("price", price)
//            param("duration_min", durationMin)
//            param("has_availability", if (hasAvailability) "yes" else "no")
//        }
//    }
//
//    fun logInfluencerProfileViewed(
//        influencerId: String,
//        influencerName: String,
//        username: String,
//        from: String
//    ) {
//        analytics.logEvent("influencer_profile_viewed") {
//            param("influencer_id", influencerId)
//            param("influencer_name", influencerName)
//            param("username", username)
//            param("from", from)
//        }
//    }
//
//    fun logTrendingInfluencerBookingInitiated(
//        influencerId: String,
//        influencerName: String,
//        username: String,
//        price: String,
//        durationMin: String
//    ) {
//        analytics.logEvent("trending_influencer_booking_initiated") {
//            param("influencer_id", influencerId)
//            param("influencer_name", influencerName)
//            param("username", username)
//            param("price", price)
//            param("duration_min", durationMin)
//        }
//    }
//}


























//package com.cc.creatorcircle.utils
//
//import android.os.Bundle
//import com.google.firebase.analytics.FirebaseAnalytics
//import com.google.firebase.analytics.ktx.analytics
//import com.google.firebase.analytics.ktx.logEvent
//import com.google.firebase.ktx.Firebase
//
///**
// * Helper class for Firebase Analytics events
// * Provides type-safe methods for logging analytics events
// */
//object FirebaseAnalyticsHelper {
//
//    private val analytics: FirebaseAnalytics by lazy {
//        Firebase.analytics
//    }
//
//    // Generic Event Logging
//    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
//        analytics.logEvent(eventName) {
//            params.forEach { (key, value) ->
//                param(key, value)
//            }
//        }
//    }
//
//    // Screen View Events
//    fun logScreenView(screenName: String, screenClass: String) {
//        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
//            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
//            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
//        }
//    }
//
//    // Post Events
//    fun logPostViewed(postId: String, authorId: Int, postType: String = "feed") {
//        analytics.logEvent("post_viewed") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//            param("post_type", postType)
//        }
//    }
//
//    fun logPostCreated(
//        postId: String,
//        hasMedia: Boolean,
//        mediaCount: Int,
//        contentLength: Int,
//        hasLinks: Boolean
//    ) {
//        analytics.logEvent("post_created") {
//            param("post_id", postId)
//            param("has_media", if (hasMedia) "yes" else "no")
//            param("media_count", mediaCount.toLong())
//            param("content_length", contentLength.toLong())
//            param("has_links", if (hasLinks) "yes" else "no")
//        }
//    }
//
//    fun logPostUpdated(postId: String, fieldsUpdated: String) {
//        analytics.logEvent("post_updated") {
//            param("post_id", postId)
//            param("fields_updated", fieldsUpdated)
//        }
//    }
//
//    fun logPostDeleted(postId: String, authorId: Int) {
//        analytics.logEvent("post_deleted") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//        }
//    }
//
//    fun logPostExpanded(postId: String) {
//        analytics.logEvent("post_expanded") {
//            param("post_id", postId)
//        }
//    }
//
//    fun logPostCollapsed(postId: String) {
//        analytics.logEvent("post_collapsed") {
//            param("post_id", postId)
//        }
//    }
//
//    // Engagement Events
//    fun logLikePost(postId: String, authorId: Int, isLiked: Boolean) {
//        analytics.logEvent("post_liked") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//            param("action", if (isLiked) "like" else "unlike")
//        }
//    }
//
//    fun logCommentClick(postId: String, authorId: Int, commentCount: Int) {
//        analytics.logEvent("comment_section_opened") {
//            param("post_id", postId)
//            param("author_id", authorId.toString())
//            param("existing_comments", commentCount.toLong())
//        }
//    }
//
//    fun logCommentSubmitted(postId: String, commentLength: Int) {
//        analytics.logEvent("comment_submitted") {
//            param("post_id", postId)
//            param("comment_length", commentLength.toLong())
//        }
//    }
//
//    fun logShareClick(postId: String, authorId: Int) {
//        analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
//            param(FirebaseAnalytics.Param.CONTENT_TYPE, "post")
//            param(FirebaseAnalytics.Param.ITEM_ID, postId)
//            param("author_id", authorId.toString())
//        }
//    }
//
//    fun logShareMethodSelected(postId: String, shareMethod: String) {
//        analytics.logEvent("share_method_selected") {
//            param("post_id", postId)
//            param("share_method", shareMethod)
//        }
//    }
//
//    // Connection Events
//    fun logConnectionRequestSent(targetUserId: Int, source: String = "post") {
//        analytics.logEvent("connection_request_sent") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestInitiated(targetUserId: Int, source: String = "unknown") {
//        analytics.logEvent("connection_request_initiated") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestSuccess(targetUserId: Int) {
//        analytics.logEvent("connection_request_success") {
//            param("target_user_id", targetUserId.toString())
//        }
//    }
//
//    fun logConnectionRequestError(targetUserId: Int, errorMessage: String) {
//        analytics.logEvent("connection_request_error") {
//            param("target_user_id", targetUserId.toString())
//            param("error_message", errorMessage)
//        }
//    }
//
//    fun logConnectionRequestCancelled(targetUserId: Int, source: String = "unknown") {
//        analytics.logEvent("connection_request_cancelled") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestAccepted(connectionId: String, source: String = "unknown") {
//        analytics.logEvent("connection_request_accepted") {
//            param("connection_id", connectionId)
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRequestRejected(connectionId: String, source: String = "unknown") {
//        analytics.logEvent("connection_request_rejected") {
//            param("connection_id", connectionId)
//            param("source", source)
//        }
//    }
//
//    fun logConnectionRemoved(targetUserId: Int, source: String = "unknown") {
//        analytics.logEvent("connection_removed") {
//            param("target_user_id", targetUserId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logRecommendationViewed(userId: Int, score: Float = 0f) {
//        analytics.logEvent("recommendation_viewed") {
//            param("user_id", userId.toString())
//            if (score > 0f) {
//                param("compatibility_score", String.format("%.2f", score))
//            }
//        }
//    }
//
//    fun logSentConnectionViewed(targetUserId: Int, username: String = "") {
//        analytics.logEvent("sent_connection_viewed") {
//            param("target_user_id", targetUserId.toString())
//            if (username.isNotEmpty()) {
//                param("username", username)
//            }
//        }
//    }
//
//    // Media Events
//    fun logMediaClicked(postId: String, mediaType: String, mediaIndex: Int) {
//        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
//            param(FirebaseAnalytics.Param.CONTENT_TYPE, mediaType)
//            param(FirebaseAnalytics.Param.ITEM_ID, postId)
//            param("media_index", mediaIndex.toLong())
//        }
//    }
//
//    fun logVideoStarted(postId: String, videoUrl: String) {
//        analytics.logEvent("video_started") {
//            param("post_id", postId)
//            param("video_url", videoUrl)
//        }
//    }
//
//    fun logVideoPaused(postId: String, progress: Float) {
//        analytics.logEvent("video_paused") {
//            param("post_id", postId)
//            param("progress_percent", (progress * 100).toLong())
//        }
//    }
//
//    fun logVideoCompleted(postId: String) {
//        analytics.logEvent("video_completed") {
//            param("post_id", postId)
//        }
//    }
//
//    fun logLinkClicked(postId: String, linkUrl: String, linkIndex: Int) {
//        analytics.logEvent("link_clicked") {
//            param("post_id", postId)
//            param("link_url", linkUrl)
//            param("link_index", linkIndex.toLong())
//        }
//    }
//
//    // Navigation Events
//    fun logTabSelected(tabName: String) {
//        analytics.logEvent("tab_selected") {
//            param("tab_name", tabName)
//        }
//    }
//
//    fun logProfileClicked(userId: Int, source: String) {
//        analytics.logEvent("profile_clicked") {
//            param("user_id", userId.toString())
//            param("source", source)
//        }
//    }
//
//    fun logMessageIconClicked() {
//        analytics.logEvent("message_icon_clicked") {
//            param("source", "home_fab")
//        }
//    }
//
//    // Feed Events
//    fun logFeedRefreshed(manual: Boolean) {
//        analytics.logEvent("feed_refreshed") {
//            param("refresh_type", if (manual) "manual" else "automatic")
//        }
//    }
//
//    fun logFeedLoaded(postCount: Int, loadTimeMs: Long) {
//        analytics.logEvent("feed_loaded") {
//            param("post_count", postCount.toLong())
//            param("load_time_ms", loadTimeMs)
//        }
//    }
//
//    fun logFeedError(errorMessage: String) {
//        analytics.logEvent("feed_error") {
//            param("error_message", errorMessage)
//        }
//    }
//
//    fun logFeedScrolled(scrollDepth: Int) {
//        analytics.logEvent("feed_scrolled") {
//            param("scroll_depth", scrollDepth.toLong())
//        }
//    }
//
//    // Dialog Events
//    fun logDialogOpened(dialogType: String, postId: String? = null) {
//        analytics.logEvent("dialog_opened") {
//            param("dialog_type", dialogType)
//            postId?.let { param("post_id", it) }
//        }
//    }
//
//    fun logDialogClosed(dialogType: String, action: String) {
//        analytics.logEvent("dialog_closed") {
//            param("dialog_type", dialogType)
//            param("action", action) // dismissed, confirmed, cancelled
//        }
//    }
//
//    // Menu Events
//    fun logMenuOpened(postId: String) {
//        analytics.logEvent("post_menu_opened") {
//            param("post_id", postId)
//        }
//    }
//
//    fun logMenuItemSelected(postId: String, menuItem: String) {
//        analytics.logEvent("post_menu_item_selected") {
//            param("post_id", postId)
//            param("menu_item", menuItem)
//        }
//    }
//
//    // Error Events
//    fun logError(
//        errorType: String,
//        errorMessage: String,
//        context: String,
//        fatal: Boolean = false
//    ) {
//        analytics.logEvent("app_error") {
//            param("error_type", errorType)
//            param("error_message", errorMessage)
//            param("context", context)
//            param("fatal", if (fatal) "yes" else "no")
//        }
//    }
//
//    // User Properties
//    fun setUserProperty(propertyName: String, propertyValue: String) {
//        analytics.setUserProperty(propertyName, propertyValue)
//    }
//
//    fun setUserId(userId: String) {
//        analytics.setUserId(userId)
//    }
//
//    // Session Events
//    fun logSessionStart() {
//        analytics.logEvent("session_start") {
//            param("timestamp", System.currentTimeMillis())
//        }
//    }
//
//    fun logSessionEnd(durationSeconds: Long) {
//        analytics.logEvent("session_end") {
//            param("session_duration_seconds", durationSeconds)
//        }
//    }
//
//    // Feature Usage
//    fun logFeatureUsed(featureName: String, source: String = "") {
//        analytics.logEvent("feature_used") {
//            param("feature_name", featureName)
//            if (source.isNotEmpty()) {
//                param("source", source)
//            }
//        }
//    }
//
//    // Performance Metrics
//    fun logPerformanceMetric(metricName: String, durationMs: Long) {
//        analytics.logEvent("performance_metric") {
//            param("metric_name", metricName)
//            param("duration_ms", durationMs)
//        }
//    }
//
//
//    // Add these functions to the FirebaseAnalyticsHelper object
//
//    fun logCommentAdded(postId: String, commentLength: Int) {
//        analytics.logEvent("comment_added") {
//            param("post_id", postId)
//            param("comment_length", commentLength.toLong())
//        }
//    }
//
//    fun logReplyAdded(postId: String, parentCommentId: Int) {
//        analytics.logEvent("reply_added") {
//            param("post_id", postId)
//            param("parent_comment_id", parentCommentId.toString())
//        }
//    }
//
//    fun logCommentLiked(postId: String, commentId: Int) {
//        analytics.logEvent("comment_liked") {
//            param("post_id", postId)
//            param("comment_id", commentId.toString())
//        }
//    }
//}

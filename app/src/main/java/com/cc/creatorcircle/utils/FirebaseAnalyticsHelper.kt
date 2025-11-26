package com.cc.creatorcircle.utils

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

/**
 * Helper class for Firebase Analytics events
 * Provides type-safe methods for logging analytics events
 */
object FirebaseAnalyticsHelper {

    private val analytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    // Generic Event Logging
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        analytics.logEvent(eventName) {
            params.forEach { (key, value) ->
                param(key, value)
            }
        }
    }

    // Screen View Events
    fun logScreenView(screenName: String, screenClass: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
    }

    // Post Events
    fun logPostViewed(postId: String, authorId: Int, postType: String = "feed") {
        analytics.logEvent("post_viewed") {
            param("post_id", postId)
            param("author_id", authorId.toString())
            param("post_type", postType)
        }
    }

    fun logPostCreated(
        postId: String,
        hasMedia: Boolean,
        mediaCount: Int,
        contentLength: Int,
        hasLinks: Boolean
    ) {
        analytics.logEvent("post_created") {
            param("post_id", postId)
            param("has_media", if (hasMedia) "yes" else "no")
            param("media_count", mediaCount.toLong())
            param("content_length", contentLength.toLong())
            param("has_links", if (hasLinks) "yes" else "no")
        }
    }

    fun logPostUpdated(postId: String, fieldsUpdated: String) {
        analytics.logEvent("post_updated") {
            param("post_id", postId)
            param("fields_updated", fieldsUpdated)
        }
    }

    fun logPostDeleted(postId: String, authorId: Int) {
        analytics.logEvent("post_deleted") {
            param("post_id", postId)
            param("author_id", authorId.toString())
        }
    }

    fun logPostExpanded(postId: String) {
        analytics.logEvent("post_expanded") {
            param("post_id", postId)
        }
    }

    fun logPostCollapsed(postId: String) {
        analytics.logEvent("post_collapsed") {
            param("post_id", postId)
        }
    }

    // Engagement Events
    fun logLikePost(postId: String, authorId: Int, isLiked: Boolean) {
        analytics.logEvent("post_liked") {
            param("post_id", postId)
            param("author_id", authorId.toString())
            param("action", if (isLiked) "like" else "unlike")
        }
    }

    fun logCommentClick(postId: String, authorId: Int, commentCount: Int) {
        analytics.logEvent("comment_section_opened") {
            param("post_id", postId)
            param("author_id", authorId.toString())
            param("existing_comments", commentCount.toLong())
        }
    }

    fun logCommentSubmitted(postId: String, commentLength: Int) {
        analytics.logEvent("comment_submitted") {
            param("post_id", postId)
            param("comment_length", commentLength.toLong())
        }
    }

    fun logShareClick(postId: String, authorId: Int) {
        analytics.logEvent(FirebaseAnalytics.Event.SHARE) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, "post")
            param(FirebaseAnalytics.Param.ITEM_ID, postId)
            param("author_id", authorId.toString())
        }
    }

    fun logShareMethodSelected(postId: String, shareMethod: String) {
        analytics.logEvent("share_method_selected") {
            param("post_id", postId)
            param("share_method", shareMethod)
        }
    }

    // Connection Events
    fun logConnectionRequestSent(targetUserId: Int, source: String = "post") {
        analytics.logEvent("connection_request_sent") {
            param("target_user_id", targetUserId.toString())
            param("source", source)
        }
    }

    fun logConnectionRequestSuccess(targetUserId: Int) {
        analytics.logEvent("connection_request_success") {
            param("target_user_id", targetUserId.toString())
        }
    }

    fun logConnectionRequestError(targetUserId: Int, errorMessage: String) {
        analytics.logEvent("connection_request_error") {
            param("target_user_id", targetUserId.toString())
            param("error_message", errorMessage)
        }
    }

    // Media Events
    fun logMediaClicked(postId: String, mediaType: String, mediaIndex: Int) {
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT) {
            param(FirebaseAnalytics.Param.CONTENT_TYPE, mediaType)
            param(FirebaseAnalytics.Param.ITEM_ID, postId)
            param("media_index", mediaIndex.toLong())
        }
    }

    fun logVideoStarted(postId: String, videoUrl: String) {
        analytics.logEvent("video_started") {
            param("post_id", postId)
            param("video_url", videoUrl)
        }
    }

    fun logVideoPaused(postId: String, progress: Float) {
        analytics.logEvent("video_paused") {
            param("post_id", postId)
            param("progress_percent", (progress * 100).toLong())
        }
    }

    fun logVideoCompleted(postId: String) {
        analytics.logEvent("video_completed") {
            param("post_id", postId)
        }
    }

    fun logLinkClicked(postId: String, linkUrl: String, linkIndex: Int) {
        analytics.logEvent("link_clicked") {
            param("post_id", postId)
            param("link_url", linkUrl)
            param("link_index", linkIndex.toLong())
        }
    }

    // Navigation Events
    fun logTabSelected(tabName: String) {
        analytics.logEvent("tab_selected") {
            param("tab_name", tabName)
        }
    }

    fun logProfileClicked(userId: Int, source: String) {
        analytics.logEvent("profile_clicked") {
            param("user_id", userId.toString())
            param("source", source)
        }
    }

    fun logMessageIconClicked() {
        analytics.logEvent("message_icon_clicked") {
            param("source", "home_fab")
        }
    }

    // Feed Events
    fun logFeedRefreshed(manual: Boolean) {
        analytics.logEvent("feed_refreshed") {
            param("refresh_type", if (manual) "manual" else "automatic")
        }
    }

    fun logFeedLoaded(postCount: Int, loadTimeMs: Long) {
        analytics.logEvent("feed_loaded") {
            param("post_count", postCount.toLong())
            param("load_time_ms", loadTimeMs)
        }
    }

    fun logFeedError(errorMessage: String) {
        analytics.logEvent("feed_error") {
            param("error_message", errorMessage)
        }
    }

    fun logFeedScrolled(scrollDepth: Int) {
        analytics.logEvent("feed_scrolled") {
            param("scroll_depth", scrollDepth.toLong())
        }
    }

    // Dialog Events
    fun logDialogOpened(dialogType: String, postId: String? = null) {
        analytics.logEvent("dialog_opened") {
            param("dialog_type", dialogType)
            postId?.let { param("post_id", it) }
        }
    }

    fun logDialogClosed(dialogType: String, action: String) {
        analytics.logEvent("dialog_closed") {
            param("dialog_type", dialogType)
            param("action", action) // dismissed, confirmed, cancelled
        }
    }

    // Menu Events
    fun logMenuOpened(postId: String) {
        analytics.logEvent("post_menu_opened") {
            param("post_id", postId)
        }
    }

    fun logMenuItemSelected(postId: String, menuItem: String) {
        analytics.logEvent("post_menu_item_selected") {
            param("post_id", postId)
            param("menu_item", menuItem)
        }
    }

    // Error Events
    fun logError(
        errorType: String,
        errorMessage: String,
        context: String,
        fatal: Boolean = false
    ) {
        analytics.logEvent("app_error") {
            param("error_type", errorType)
            param("error_message", errorMessage)
            param("context", context)
            param("fatal", if (fatal) "yes" else "no")
        }
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
        analytics.logEvent("session_start") {
            param("timestamp", System.currentTimeMillis())
        }
    }

    fun logSessionEnd(durationSeconds: Long) {
        analytics.logEvent("session_end") {
            param("session_duration_seconds", durationSeconds)
        }
    }

    // Feature Usage
    fun logFeatureUsed(featureName: String, source: String = "") {
        analytics.logEvent("feature_used") {
            param("feature_name", featureName)
            if (source.isNotEmpty()) {
                param("source", source)
            }
        }
    }

    // Performance Metrics
    fun logPerformanceMetric(metricName: String, durationMs: Long) {
        analytics.logEvent("performance_metric") {
            param("metric_name", metricName)
            param("duration_ms", durationMs)
        }
    }
}


























//
// package com.cc.creatorcircle.utils
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
//}
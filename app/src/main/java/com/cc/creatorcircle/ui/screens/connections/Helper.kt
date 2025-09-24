package com.cc.creatorcircle.ui.screens.connections

import com.cc.creatorcircle.data.models.PlatformFollower
import android.util.Log

// Helper function to extract Instagram followers from platform_followers map (raw data)
fun getInstagramFollowers(platformFollowers: Map<String, Any>): Int? {
    return try {
        val instagramData = platformFollowers["instagram"] as? List<*>
        val firstEntry = instagramData?.firstOrNull() as? Map<*, *>
        val followersValue = firstEntry?.get("followers")

        when (followersValue) {
            is Int -> followersValue
            is String -> {
                if (followersValue.isBlank()) {
                    null
                } else {
                    followersValue.toIntOrNull()
                }
            }
            is Double -> followersValue.toInt()
            is Float -> followersValue.toInt()
            else -> null
        }
    } catch (e: Exception) {
        Log.w("ConnectionsHelper", "Error parsing Instagram followers from raw data", e)
        null
    }
}

// Fixed helper function for ConnectionUser's platformFollowers
//fun getInstagramFollowersFromConnectionUser(platformFollowers: Map<String, List<PlatformFollower>>): Int? {
//    return try {
//        val instagramFollowers = platformFollowers["instagram"]?.firstOrNull()
//        instagramFollowers?.let { follower ->
//            // Use the safe followers property that handles string conversion
//            val count = follower.followers
//            if (count > 0) count else null
//        }
//    } catch (e: Exception) {
//        Log.w("ConnectionsHelper", "Error parsing Instagram followers from ConnectionUser", e)
//        null
//    }
//}


fun getInstagramFollowersFromConnectionUser(platformFollowers: Map<String, List<PlatformFollower>>): Int? {
    return try {
        platformFollowers["instagram"]
            ?.firstOrNull()
            ?.let { follower ->
                val count = follower.followers
                if (count > 0) count else null
            }
    } catch (e: Exception) {
        Log.w("ConnectionsHelper", "Error parsing Instagram followers", e)
        null
    }
}

// Helper function to format follower count
fun formatFollowerCount(count: Int): String {
    return when {
        count >= 1_000_000 -> {
            val millions = count / 1_000_000.0
            if (millions % 1 == 0.0) {
                "${millions.toInt()}M"
            } else {
                String.format("%.1fM", millions)
            }
        }
        count >= 1_000 -> {
            val thousands = count / 1_000.0
            if (thousands % 1 == 0.0) {
                "${thousands.toInt()}K"
            } else {
                String.format("%.1fK", thousands)
            }
        }
        else -> count.toString()
    }
}

// Additional helper to safely extract followers with multiple fallback attempts
fun safeGetInstagramFollowers(platformFollowers: Any?): Int? {
    return try {
        when (platformFollowers) {
            is Map<*, *> -> {
                val instagram = (platformFollowers as? Map<String, Any>)?.get("instagram")
                when (instagram) {
                    is List<*> -> {
                        val firstEntry = instagram.firstOrNull() as? Map<*, *>
                        val followersValue = firstEntry?.get("followers")
                        parseFollowersValue(followersValue)
                    }
                    is Map<*, *> -> {
                        val followersValue = instagram["followers"]
                        parseFollowersValue(followersValue)
                    }
                    else -> null
                }
            }
            else -> null
        }
    } catch (e: Exception) {
        Log.w("ConnectionsHelper", "Error in safeGetInstagramFollowers", e)
        null
    }
}

// Helper to safely parse various follower value types
private fun parseFollowersValue(value: Any?): Int? {
    return when (value) {
        is Int -> if (value > 0) value else null
        is String -> {
            if (value.isBlank()) {
                null
            } else {
                try {
                    val parsed = value.toInt()
                    if (parsed > 0) parsed else null
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
        is Double -> {
            val intValue = value.toInt()
            if (intValue > 0) intValue else null
        }
        is Float -> {
            val intValue = value.toInt()
            if (intValue > 0) intValue else null
        }
        else -> null
    }
}


















//package com.cc.creatorcircle.ui.screens.connections
//
//import com.cc.creatorcircle.data.models.PlatformFollower
//
//
//// Helper function to extract Instagram followers from platform_followers map
//fun getInstagramFollowers(platformFollowers: Map<String, Any>): Int? {
//    return try {
//        val instagramData = platformFollowers["instagram"] as? List<*>
//        val firstEntry = instagramData?.firstOrNull() as? Map<*, *>
//        firstEntry?.get("followers") as? Int
//    } catch (e: Exception) {
//        null
//    }
//}
//
//// Helper function to extract Instagram followers from ConnectionUser's platformFollowers
//fun getInstagramFollowersFromConnectionUser(platformFollowers: Map<String, List<PlatformFollower>>): Int? {
//    return try {
//        platformFollowers["instagram"]?.firstOrNull()?.followers
//    } catch (e: Exception) {
//        null
//    }
//}
//
//// Helper function to format follower count
//fun formatFollowerCount(count: Int): String {
//    return when {
//        count >= 1_000_000 -> "${count / 1_000_000}M"
//        count >= 1_000 -> "${count / 1_000}K"
//        else -> count.toString()
//    }
//}
//

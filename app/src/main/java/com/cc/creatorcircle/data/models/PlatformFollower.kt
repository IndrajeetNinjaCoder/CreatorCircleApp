package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

// Safe Platform follower model that handles various data types
data class PlatformFollower(
    @SerializedName("username")
    val username: String? = null, // Added username field

    @SerializedName("followers")
    private val followersRaw: Any? = null,

    @SerializedName("is_primary")
    val isPrimary: Boolean = false
) {
    // Safe conversion property that handles multiple data types
    val followers: Int
        get() = when (followersRaw) {
            is Int -> followersRaw
            is String -> {
                if (followersRaw.isBlank()) {
                    0
                } else {
                    try {
                        followersRaw.toInt()
                    } catch (e: NumberFormatException) {
                        0
                    }
                }
            }
            is Double -> followersRaw.toInt()
            is Float -> followersRaw.toInt()
            else -> 0
        }
}

// Alternative: Custom deserializer approach (if you prefer more control)
class SafePlatformFollowerDeserializer : JsonDeserializer<PlatformFollower> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PlatformFollower {
        val jsonObject = json?.asJsonObject

        // Parse username safely
        val usernameElement = jsonObject?.get("username")
        val usernameValue = usernameElement?.asString

        // Parse followers safely
        val followersElement = jsonObject?.get("followers")
        val followersValue = when {
            followersElement?.isJsonPrimitive == true -> {
                when {
                    followersElement.asJsonPrimitive.isNumber ->
                        followersElement.asJsonPrimitive.asInt
                    followersElement.asJsonPrimitive.isString -> {
                        val str = followersElement.asJsonPrimitive.asString
                        if (str.isBlank()) 0 else str.toIntOrNull() ?: 0
                    }
                    else -> 0
                }
            }
            else -> 0
        }

        // Parse is_primary safely
        val isPrimaryElement = jsonObject?.get("is_primary")
        val isPrimaryValue = isPrimaryElement?.asBoolean ?: false

        return PlatformFollower(usernameValue, followersValue, isPrimaryValue)
    }
}








// package com.cc.creatorcircle.data.models
//
//import com.google.gson.annotations.SerializedName
//import com.google.gson.JsonDeserializationContext
//import com.google.gson.JsonDeserializer
//import com.google.gson.JsonElement
//import java.lang.reflect.Type
//
//// Safe Platform follower model that handles various data types
//data class PlatformFollower(
//    @SerializedName("followers")
//    private val followersRaw: Any? = null,
//
//    @SerializedName("is_primary")
//    val isPrimary: Boolean = false
//) {
//    // Safe conversion property that handles multiple data types
//    val followers: Int
//        get() = when (followersRaw) {
//            is Int -> followersRaw
//            is String -> {
//                if (followersRaw.isBlank()) {
//                    0
//                } else {
//                    try {
//                        followersRaw.toInt()
//                    } catch (e: NumberFormatException) {
//                        0
//                    }
//                }
//            }
//            is Double -> followersRaw.toInt()
//            is Float -> followersRaw.toInt()
//            else -> 0
//        }
//}
//
//// Alternative: Custom deserializer approach (if you prefer more control)
//class SafePlatformFollowerDeserializer : JsonDeserializer<PlatformFollower> {
//    override fun deserialize(
//        json: JsonElement?,
//        typeOfT: Type?,
//        context: JsonDeserializationContext?
//    ): PlatformFollower {
//        val jsonObject = json?.asJsonObject
//
//        // Parse followers safely
//        val followersElement = jsonObject?.get("followers")
//        val followersValue = when {
//            followersElement?.isJsonPrimitive == true -> {
//                when {
//                    followersElement.asJsonPrimitive.isNumber ->
//                        followersElement.asJsonPrimitive.asInt
//                    followersElement.asJsonPrimitive.isString -> {
//                        val str = followersElement.asJsonPrimitive.asString
//                        if (str.isBlank()) 0 else str.toIntOrNull() ?: 0
//                    }
//                    else -> 0
//                }
//            }
//            else -> 0
//        }
//
//        // Parse is_primary safely
//        val isPrimaryElement = jsonObject?.get("is_primary")
//        val isPrimaryValue = isPrimaryElement?.asBoolean ?: false
//
//        return PlatformFollower(followersValue, isPrimaryValue)
//    }
//}
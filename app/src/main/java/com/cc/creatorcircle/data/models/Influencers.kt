package com.cc.creatorcircle.data.models

import com.google.gson.annotations.SerializedName

data class Influencers(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("mentor_id") val mentorId: Int,
    @SerializedName("username") val username: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("profile_pic") val profilePic: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("total_social_media_followers") val totalSocialMediaFollowers: Int,
    @SerializedName("pricing") val pricing: List<Pricing>
) {
    // Helper properties for UI
    val displayName: String get() = fullName
    val followerCount: Int get() = totalSocialMediaFollowers
    val hasBio: Boolean get() = !bio.isNullOrBlank()
}

data class Pricing(
    @SerializedName("duration_minutes") val durationMinutes: Int,
    @SerializedName("price") val price: Double
) {
    // Helper properties for UI
    val formattedDuration: String get() = "$durationMinutes min"
    val formattedPrice: String get() = "₹$price"
}

// Type alias for the API response (which is just a list)
typealias InfluencersResponse = List<Influencers>













data class InfluencerFilterRequest(
    @SerializedName("categories") val categories: List<String>? = null,
    @SerializedName("price") val price: List<List<Double>>? = null,
    @SerializedName("follower_ranges") val followerRanges: List<List<Int>>? = null,
    @SerializedName("date_range") val dateRange: DateRange? = null
)

//data class DateRange(
//    @SerializedName("from") val from: String,
//    @SerializedName("to") val to: String
//)

class InfluencerFilterBuilder {
    private var categories: MutableList<String>? = null
    private var priceRanges: MutableList<List<Double>>? = null
    private var followerRanges: MutableList<List<Int>>? = null
    private var dateRange: DateRange? = null

    fun addCategory(category: String) = apply {
        if (categories == null) categories = mutableListOf()
        categories?.add(category)
    }

    fun addCategories(vararg category: String) = apply {
        if (categories == null) categories = mutableListOf()
        categories?.addAll(category)
    }

    fun addPriceRange(min: Double, max: Double) = apply {
        if (priceRanges == null) priceRanges = mutableListOf()
        priceRanges?.add(listOf(min, max))
    }

    fun addFollowerRange(min: Int, max: Int) = apply {
        if (followerRanges == null) followerRanges = mutableListOf()
        followerRanges?.add(listOf(min, max))
    }

    fun setDateRange(from: String, to: String) = apply {
        dateRange = DateRange(from, to)
    }

    fun build(): InfluencerFilterRequest {
        return InfluencerFilterRequest(
            categories = categories,
            price = priceRanges,
            followerRanges = followerRanges,
            dateRange = dateRange
        )
    }
}
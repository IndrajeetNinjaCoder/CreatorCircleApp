package com.example.creatorcircle.models

import com.google.gson.annotations.SerializedName

// ==================== DEALS MODELS ====================

data class DealsResponse(
    @SerializedName("deals")
    val deals: List<Deal>,
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("has_more")
    val hasMore: Boolean,
    @SerializedName("filters_applied")
    val filtersApplied: FiltersApplied
)

data class Deal(
    @SerializedName("id")
    val id: Int,
    @SerializedName("brand_id")
    val brandId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("collaboration_type")
    val collaborationType: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("min_amount")
    val minAmount: String?,
    @SerializedName("max_amount")
    val maxAmount: String?,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("targeted_users")
    val targetedUsers: TargetedUsers,
    @SerializedName("description")
    val description: String,
    @SerializedName("barter_details")
    val barterDetails: BarterDetails,
    @SerializedName("requirements")
    val requirements: Requirements,
    @SerializedName("deliverables")
    val deliverables: List<String>,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("brand_name")
    val brandName: String,
    @SerializedName("application_deadline")
    val applicationDeadline: String?,
    @SerializedName("start_date")
    val startDate: String?,
    @SerializedName("end_date")
    val endDate: String?,
    @SerializedName("view_count")
    val viewCount: Int,
    @SerializedName("application_count")
    val applicationCount: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("is_applied")
    val isApplied: Boolean,
    @SerializedName("application_status")
    val applicationStatus: String?
)

data class TargetedUsers(
    @SerializedName("age")
    val age: String,
    @SerializedName("interest")
    val interest: String,
    @SerializedName("followers")
    val followers: List<String>
)

data class BarterDetails(
    @SerializedName("offer")
    val offer: String? = null
)

// Using a typealias for an empty object that might have dynamic fields later
typealias Requirements = Map<String, Any>

data class FiltersApplied(
    @SerializedName("category")
    val category: String?,
    @SerializedName("collaboration_type")
    val collaborationType: String?,
    @SerializedName("location")
    val location: String?,
    @SerializedName("min_amount")
    val minAmount: String?,
    @SerializedName("max_amount")
    val maxAmount: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("brand_id")
    val brandId: Int?
)

// ==================== BRANDS MODELS ====================

data class BrandsResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("total_count")
    val totalCount: Int,
    @SerializedName("limit")
    val limit: Int,
    @SerializedName("offset")
    val offset: Int,
    @SerializedName("has_more")
    val hasMore: Boolean,
    @SerializedName("returned_count")
    val returnedCount: Int,
    @SerializedName("has_active_profile")
    val hasActiveProfile: Boolean,
    @SerializedName("has_default_filters")
    val hasDefaultFilters: Boolean,
    @SerializedName("show_onboarding")
    val showOnboarding: Boolean,
    @SerializedName("default_filters")
    val defaultFilters: List<DefaultFilter>,
    @SerializedName("filters")
    val filters: BrandFilters,
    @SerializedName("categories")
    val categories: List<String>,
    @SerializedName("countries")
    val countries: List<String>,
    @SerializedName("states")
    val states: Map<String, List<String>>,
    @SerializedName("data")
    val data: List<Brand>
)

data class Brand(
    @SerializedName("brand_id")
    val brandId: Int,
    @SerializedName("brand_name")
    val brandName: String,
    @SerializedName("brand_category")
    val brandCategory: String,
    @SerializedName("brand_email")
    val brandEmail: String,
    @SerializedName("brand_website")
    val brandWebsite: String,
    @SerializedName("brand_state")
    val brandState: String,
    @SerializedName("brand_country")
    val brandCountry: String,
    @SerializedName("is_authenticated")
    val isAuthenticated: Boolean,
    @SerializedName("is_sent")
    val isSent: Boolean
)

data class DefaultFilter(
    @SerializedName("profile_id")
    val profileId: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("platform")
    val platform: String,
    @SerializedName("preferred_categories")
    val preferredCategories: List<String>,
    @SerializedName("preferred_locations")
    val preferredLocations: List<String>,
    @SerializedName("has_filters")
    val hasFilters: Boolean,
    @SerializedName("is_brand_onboarding")
    val isBrandOnboarding: Boolean
)

data class BrandFilters(
    @SerializedName("category")
    val category: String?,
    @SerializedName("states")
    val states: List<String>,
    @SerializedName("country")
    val country: String,
    @SerializedName("profile_id")
    val profileId: Int?
)



// ==================== DEFAULT FILTERS API MODELS ====================

data class SaveDefaultFiltersRequest(
    @SerializedName("profile_id")
    val profileId: Int,
    @SerializedName("preferred_categories")
    val preferredCategories: List<String>,
    @SerializedName("preferred_locations")
    val preferredLocations: List<String>
)

data class SaveDefaultFiltersResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: Int,
    @SerializedName("filters")
    val filters: SavedFilters,
    @SerializedName("onboarding_complete")
    val onboardingComplete: Boolean
)

data class SavedFilters(
    @SerializedName("profile_id")
    val profileId: Int,
    @SerializedName("preferred_categories")
    val preferredCategories: List<String>,
    @SerializedName("preferred_locations")
    val preferredLocations: List<String>
)

// ==================== EMAIL GENERATION MODELS ====================




data class GenerateEmailRequest(
    @SerializedName("brand_id")
    val brandId: Int,
    @SerializedName("profile_id")
    val profileId: Int
)

data class GenerateEmailResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("body")
    val body: String
)




data class ApplyToDealRequest(
    @SerializedName("phone_number")
    val phoneNumber: String,
    @SerializedName("instagram_username")
    val instagramUsername: String,
    @SerializedName("location")
    val location: String
)

data class ApplyToDealResponse(
    @SerializedName("detail")
    val detail: String
)
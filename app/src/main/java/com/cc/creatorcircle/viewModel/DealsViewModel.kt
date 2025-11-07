package com.cc.creatorcircle.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.repository.DealsRepository
import com.cc.creatorcircle.utils.TokenManager
import com.example.creatorcircle.models.ApplyToDealResponse
import com.example.creatorcircle.models.Brand
import com.example.creatorcircle.models.BrandsResponse
import com.example.creatorcircle.models.Deal
import com.example.creatorcircle.models.DealsResponse
import com.example.creatorcircle.models.GenerateEmailResponse
import com.example.creatorcircle.models.SaveDefaultFiltersResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DealsViewModel(private val context: Context) : ViewModel() {

    private val repository = DealsRepository(context)
    private val tokenManager = TokenManager(context)

    // Deals State
    private val _dealsResponse = MutableStateFlow<DealsResponse?>(null)
    val dealsResponse: StateFlow<DealsResponse?> = _dealsResponse.asStateFlow()

    private val _deals = MutableStateFlow<List<Deal>>(emptyList())
    val deals: StateFlow<List<Deal>> = _deals.asStateFlow()

    private val _dealsLoading = MutableStateFlow(false)
    val dealsLoading: StateFlow<Boolean> = _dealsLoading.asStateFlow()

    private val _dealsError = MutableStateFlow<String?>(null)
    val dealsError: StateFlow<String?> = _dealsError.asStateFlow()

    // Deals Pagination State
    private val _hasMoreDeals = MutableStateFlow(false)
    val hasMoreDeals: StateFlow<Boolean> = _hasMoreDeals.asStateFlow()

    private var currentDealsOffset = 0
    private val dealsPageLimit = 20

    // Brands State
    private val _brandsResponse = MutableStateFlow<BrandsResponse?>(null)
    val brandsResponse: StateFlow<BrandsResponse?> = _brandsResponse.asStateFlow()

    private val _brands = MutableStateFlow<List<Brand>>(emptyList())
    val brands: StateFlow<List<Brand>> = _brands.asStateFlow()

    private val _brandsLoading = MutableStateFlow(false)
    val brandsLoading: StateFlow<Boolean> = _brandsLoading.asStateFlow()

    private val _brandsError = MutableStateFlow<String?>(null)
    val brandsError: StateFlow<String?> = _brandsError.asStateFlow()

    // Brands Pagination State
    private val _hasMoreBrands = MutableStateFlow(false)
    val hasMoreBrands: StateFlow<Boolean> = _hasMoreBrands.asStateFlow()

    private var currentBrandsOffset = 0
    private val brandsPageLimit = 20

    // Default Filters State
    private val _defaultFiltersResponse = MutableStateFlow<SaveDefaultFiltersResponse?>(null)
    val defaultFiltersResponse: StateFlow<SaveDefaultFiltersResponse?> = _defaultFiltersResponse.asStateFlow()

    private val _defaultFiltersLoading = MutableStateFlow(false)
    val defaultFiltersLoading: StateFlow<Boolean> = _defaultFiltersLoading.asStateFlow()

    private val _defaultFiltersError = MutableStateFlow<String?>(null)
    val defaultFiltersError: StateFlow<String?> = _defaultFiltersError.asStateFlow()

    private val _defaultFiltersSaveSuccess = MutableStateFlow(false)
    val defaultFiltersSaveSuccess: StateFlow<Boolean> = _defaultFiltersSaveSuccess.asStateFlow()






    // Email Generation State
    private val _emailGenerationResponse = MutableStateFlow<GenerateEmailResponse?>(null)
    val emailGenerationResponse: StateFlow<GenerateEmailResponse?> = _emailGenerationResponse.asStateFlow()

    private val _emailGenerationLoading = MutableStateFlow(false)
    val emailGenerationLoading: StateFlow<Boolean> = _emailGenerationLoading.asStateFlow()

    private val _emailGenerationError = MutableStateFlow<String?>(null)
    val emailGenerationError: StateFlow<String?> = _emailGenerationError.asStateFlow()

    private val _emailGenerationSuccess = MutableStateFlow(false)
    val emailGenerationSuccess: StateFlow<Boolean> = _emailGenerationSuccess.asStateFlow()




    // Apply to Deal State
    private val _applyToDealResponse = MutableStateFlow<ApplyToDealResponse?>(null)
    val applyToDealResponse: StateFlow<ApplyToDealResponse?> = _applyToDealResponse.asStateFlow()

    private val _applyToDealLoading = MutableStateFlow(false)
    val applyToDealLoading: StateFlow<Boolean> = _applyToDealLoading.asStateFlow()

    private val _applyToDealError = MutableStateFlow<String?>(null)
    val applyToDealError: StateFlow<String?> = _applyToDealError.asStateFlow()

    private val _applyToDealSuccess = MutableStateFlow(false)
    val applyToDealSuccess: StateFlow<Boolean> = _applyToDealSuccess.asStateFlow()



    // ==================== DEALS FUNCTIONS ====================

    /**
     * Fetch deals with optional filters
     */
    fun fetchDeals(
        category: String? = null,
        collaborationType: String? = null,
        location: String? = null,
        minAmount: String? = null,
        maxAmount: String? = null,
        status: String? = null,
        brandId: Int? = null,
        resetPagination: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                Log.d("DealsViewModel", "Fetching deals with filters")
                _dealsLoading.value = true
                _dealsError.value = null

                if (resetPagination) {
                    currentDealsOffset = 0
                }

                repository.getDeals(
                    category = category,
                    collaborationType = collaborationType,
                    location = location,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                    status = status,
                    brandId = brandId,
                    limit = dealsPageLimit,
                    offset = currentDealsOffset
                )
                    .onSuccess { dealsResponse ->
                        Log.d("DealsViewModel", "Deals fetched successfully: ${dealsResponse.deals.size} deals")
                        _dealsResponse.value = dealsResponse

                        if (resetPagination) {
                            _deals.value = dealsResponse.deals
                        } else {
                            _deals.value = _deals.value + dealsResponse.deals
                        }

                        _hasMoreDeals.value = dealsResponse.hasMore
                        currentDealsOffset += dealsResponse.deals.size
                    }
                    .onFailure { exception ->
                        Log.e("DealsViewModel", "Failed to fetch deals", exception)
                        _dealsError.value = exception.message ?: "Failed to fetch deals"
                    }
            } catch (e: Exception) {
                Log.e("DealsViewModel", "Exception in fetchDeals", e)
                _dealsError.value = "Network error: ${e.message}"
            } finally {
                _dealsLoading.value = false
            }
        }
    }

    /**
     * Load more deals (pagination)
     */
    fun loadMoreDeals(
        category: String? = null,
        collaborationType: String? = null,
        location: String? = null,
        minAmount: String? = null,
        maxAmount: String? = null,
        status: String? = null,
        brandId: Int? = null
    ) {
        if (!_dealsLoading.value && _hasMoreDeals.value) {
            fetchDeals(
                category = category,
                collaborationType = collaborationType,
                location = location,
                minAmount = minAmount,
                maxAmount = maxAmount,
                status = status,
                brandId = brandId,
                resetPagination = false
            )
        }
    }

    /**
     * Fetch active deals only
     */
    fun fetchActiveDeals() {
        viewModelScope.launch {
            try {
                Log.d("DealsViewModel", "Fetching active deals")
                _dealsLoading.value = true
                _dealsError.value = null

                repository.getActiveDeals(limit = dealsPageLimit, offset = 0)
                    .onSuccess { dealsResponse ->
                        Log.d("DealsViewModel", "Active deals fetched: ${dealsResponse.deals.size} deals")
                        _dealsResponse.value = dealsResponse
                        _deals.value = dealsResponse.deals
                        _hasMoreDeals.value = dealsResponse.hasMore
                        currentDealsOffset = dealsResponse.deals.size
                    }
                    .onFailure { exception ->
                        Log.e("DealsViewModel", "Failed to fetch active deals", exception)
                        _dealsError.value = exception.message ?: "Failed to fetch active deals"
                    }
            } catch (e: Exception) {
                Log.e("DealsViewModel", "Exception in fetchActiveDeals", e)
                _dealsError.value = "Network error: ${e.message}"
            } finally {
                _dealsLoading.value = false
            }
        }
    }

    /**
     * Fetch deals by category
     */
    fun fetchDealsByCategory(category: String) {
        fetchDeals(category = category, resetPagination = true)
    }

    /**
     * Fetch deals by collaboration type
     */
    fun fetchDealsByCollaborationType(collaborationType: String) {
        fetchDeals(collaborationType = collaborationType, resetPagination = true)
    }

    /**
     * Refresh deals
     */
    fun refreshDeals(
        category: String? = null,
        collaborationType: String? = null,
        location: String? = null,
        minAmount: String? = null,
        maxAmount: String? = null,
        status: String? = null,
        brandId: Int? = null
    ) {
        fetchDeals(
            category = category,
            collaborationType = collaborationType,
            location = location,
            minAmount = minAmount,
            maxAmount = maxAmount,
            status = status,
            brandId = brandId,
            resetPagination = true
        )
    }

    /**
     * Clear deals data
     */
    fun clearDeals() {
        _dealsResponse.value = null
        _deals.value = emptyList()
        _dealsError.value = null
        _hasMoreDeals.value = false
        currentDealsOffset = 0
    }

    /**
     * Clear deals error
     */
    fun clearDealsError() {
        _dealsError.value = null
    }

    /**
     * Get deal by ID
     */
    fun getDealById(dealId: Int): Deal? {
        return _deals.value.find { it.id == dealId }
    }

    /**
     * Get deals count
     */
    fun getDealsCount(): Int {
        return _deals.value.size
    }

    /**
     * Check if deals are empty
     */
    fun isDealsEmpty(): Boolean {
        return _deals.value.isEmpty()
    }

    // ==================== BRANDS FUNCTIONS ====================

    /**
     * Fetch brands with optional filters
     */
    fun fetchBrands(
        category: String? = null,
        states: List<String>? = null,
        country: String? = null,
        profileId: Int? = null,
        resetPagination: Boolean = true
    ) {
        viewModelScope.launch {
            try {
                Log.d("DealsViewModel", "Fetching brands with filters")
                _brandsLoading.value = true
                _brandsError.value = null

                if (resetPagination) {
                    currentBrandsOffset = 0
                }

                repository.getBrands(
                    category = category,
                    states = states,
                    country = country,
                    profileId = profileId,
                    limit = brandsPageLimit,
                    offset = currentBrandsOffset
                )
                    .onSuccess { brandsResponse ->
                        Log.d("DealsViewModel", "Brands fetched successfully: ${brandsResponse.data.size} brands")
                        _brandsResponse.value = brandsResponse

                        if (resetPagination) {
                            _brands.value = brandsResponse.data
                        } else {
                            _brands.value = _brands.value + brandsResponse.data
                        }

                        _hasMoreBrands.value = brandsResponse.hasMore
                        currentBrandsOffset += brandsResponse.data.size
                    }
                    .onFailure { exception ->
                        Log.e("DealsViewModel", "Failed to fetch brands", exception)
                        _brandsError.value = exception.message ?: "Failed to fetch brands"
                    }
            } catch (e: Exception) {
                Log.e("DealsViewModel", "Exception in fetchBrands", e)
                _brandsError.value = "Network error: ${e.message}"
            } finally {
                _brandsLoading.value = false
            }
        }
    }

    /**
     * Load more brands (pagination)
     */
    fun loadMoreBrands(
        category: String? = null,
        states: List<String>? = null,
        country: String? = null,
        profileId: Int? = null
    ) {
        if (!_brandsLoading.value && _hasMoreBrands.value) {
            fetchBrands(
                category = category,
                states = states,
                country = country,
                profileId = profileId,
                resetPagination = false
            )
        }
    }

    /**
     * Fetch brands by category
     */
    fun fetchBrandsByCategory(category: String) {
        fetchBrands(category = category, resetPagination = true)
    }

    /**
     * Fetch brands by country
     */
    fun fetchBrandsByCountry(country: String) {
        fetchBrands(country = country, resetPagination = true)
    }

    /**
     * Fetch brands by states
     */
    fun fetchBrandsByStates(states: List<String>, country: String? = null) {
        fetchBrands(states = states, country = country, resetPagination = true)
    }

    /**
     * Refresh brands
     */
    fun refreshBrands(
        category: String? = null,
        states: List<String>? = null,
        country: String? = null,
        profileId: Int? = null
    ) {
        fetchBrands(
            category = category,
            states = states,
            country = country,
            profileId = profileId,
            resetPagination = true
        )
    }

    /**
     * Clear brands data
     */
    fun clearBrands() {
        _brandsResponse.value = null
        _brands.value = emptyList()
        _brandsError.value = null
        _hasMoreBrands.value = false
        currentBrandsOffset = 0
    }

    /**
     * Clear brands error
     */
    fun clearBrandsError() {
        _brandsError.value = null
    }

    /**
     * Get brand by ID
     */
    fun getBrandById(brandId: Int): Brand? {
        return _brands.value.find { it.brandId == brandId }
    }

    /**
     * Get brands count
     */
    fun getBrandsCount(): Int {
        return _brands.value.size
    }

    /**
     * Check if brands are empty
     */
    fun isBrandsEmpty(): Boolean {
        return _brands.value.isEmpty()
    }

    /**
     * Get available categories from brands response
     */
    fun getAvailableCategories(): List<String> {
        return _brandsResponse.value?.categories ?: emptyList()
    }

    /**
     * Get available countries from brands response
     */
    fun getAvailableCountries(): List<String> {
        return _brandsResponse.value?.countries ?: emptyList()
    }

    /**
     * Get available states for a country from brands response
     */
    fun getAvailableStates(country: String): List<String> {
        return _brandsResponse.value?.states?.get(country) ?: emptyList()
    }

    // ==================== DEFAULT FILTERS FUNCTIONS ====================

    /**
     * Save default filters for a profile
     */
    fun saveDefaultFilters(
        profileId: Int,
        preferredCategories: List<String>,
        preferredLocations: List<String>
    ) {
        viewModelScope.launch {
            try {
                Log.d("DealsViewModel", "Saving default filters for profile $profileId")
                _defaultFiltersLoading.value = true
                _defaultFiltersError.value = null
                _defaultFiltersSaveSuccess.value = false

                repository.saveDefaultFilters(
                    profileId = profileId,
                    preferredCategories = preferredCategories,
                    preferredLocations = preferredLocations
                )
                    .onSuccess { response ->
                        Log.d("DealsViewModel", "Default filters saved successfully")
                        _defaultFiltersResponse.value = response
                        _defaultFiltersSaveSuccess.value = true
                    }
                    .onFailure { exception ->
                        Log.e("DealsViewModel", "Failed to save default filters", exception)
                        _defaultFiltersError.value = exception.message ?: "Failed to save default filters"
                        _defaultFiltersSaveSuccess.value = false
                    }
            } catch (e: Exception) {
                Log.e("DealsViewModel", "Exception in saveDefaultFilters", e)
                _defaultFiltersError.value = "Network error: ${e.message}"
                _defaultFiltersSaveSuccess.value = false
            } finally {
                _defaultFiltersLoading.value = false
            }
        }
    }






    // ==================== EMAIL GENERATION FUNCTIONS ====================

    /**
     * Generate email for brand partnership
     */
    fun generateEmail(
        brandId: Int,
        profileId: Int
    ) {
        viewModelScope.launch {
            try {
                Log.d("DealsViewModel", "Generating email for brand $brandId and profile $profileId")
                _emailGenerationLoading.value = true
                _emailGenerationError.value = null
                _emailGenerationSuccess.value = false

                repository.generateEmail(
                    brandId = brandId,
                    profileId = profileId
                )
                    .onSuccess { response ->
                        Log.d("DealsViewModel", "Email generated successfully")
                        _emailGenerationResponse.value = response
                        _emailGenerationSuccess.value = true
                    }
                    .onFailure { exception ->
                        Log.e("DealsViewModel", "Failed to generate email", exception)
                        _emailGenerationError.value = exception.message ?: "Failed to generate email"
                        _emailGenerationSuccess.value = false
                    }
            } catch (e: Exception) {
                Log.e("DealsViewModel", "Exception in generateEmail", e)
                _emailGenerationError.value = "Network error: ${e.message}"
                _emailGenerationSuccess.value = false
            } finally {
                _emailGenerationLoading.value = false
            }
        }
    }

    /**
     * Get generated email subject
     */
    fun getGeneratedEmailSubject(): String? {
        return _emailGenerationResponse.value?.subject
    }

    /**
     * Get generated email body
     */
    fun getGeneratedEmailBody(): String? {
        return _emailGenerationResponse.value?.body
    }

    /**
     * Clear email generation data
     */
    fun clearEmailGeneration() {
        _emailGenerationResponse.value = null
        _emailGenerationError.value = null
        _emailGenerationSuccess.value = false
    }

    /**
     * Clear email generation error
     */
    fun clearEmailGenerationError() {
        _emailGenerationError.value = null
    }

    /**
     * Reset email generation success flag
     */
    fun resetEmailGenerationSuccess() {
        _emailGenerationSuccess.value = false
    }


    /**
     * Clear default filters data
     */
    fun clearDefaultFilters() {
        _defaultFiltersResponse.value = null
        _defaultFiltersError.value = null
        _defaultFiltersSaveSuccess.value = false
    }

    /**
     * Clear default filters error
     */
    fun clearDefaultFiltersError() {
        _defaultFiltersError.value = null
    }

    /**
     * Reset default filters save success flag
     */
    fun resetDefaultFiltersSaveSuccess() {
        _defaultFiltersSaveSuccess.value = false
    }

    /**
     * Check if onboarding is complete
     */
    fun isOnboardingComplete(): Boolean {
        return _defaultFiltersResponse.value?.onboardingComplete ?: false
    }

    /**
     * Get saved preferred categories
     */
    fun getSavedPreferredCategories(): List<String> {
        return _defaultFiltersResponse.value?.filters?.preferredCategories ?: emptyList()
    }

    /**
     * Get saved preferred locations
     */
    fun getSavedPreferredLocations(): List<String> {
        return _defaultFiltersResponse.value?.filters?.preferredLocations ?: emptyList()
    }

    // ==================== COMMON FUNCTIONS ====================

    /**
     * Check if user has valid token
     */
    fun hasValidToken(): Boolean {
        return tokenManager.getToken().isNotEmpty()
    }

    /**
     * Clear all data
     */
//    fun clearAllData() {
//        clearDeals()
//        clearBrands()
//        clearDefaultFilters()
//    }





    // ==================== APPLY TO DEAL FUNCTIONS ====================

    /**
     * Apply to a deal
     */
    fun applyToDeal(
        dealId: Int,
        phoneNumber: String,
        instagramUsername: String,
        location: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("DealsViewModel", "Applying to deal $dealId")
                _applyToDealLoading.value = true
                _applyToDealError.value = null
                _applyToDealSuccess.value = false

                repository.applyToDeal(
                    dealId = dealId,
                    phoneNumber = phoneNumber,
                    instagramUsername = instagramUsername,
                    location = location
                )
                    .onSuccess { response ->
                        Log.d("DealsViewModel", "Successfully applied to deal: ${response.detail}")
                        _applyToDealResponse.value = response
                        _applyToDealSuccess.value = true
                    }
                    .onFailure { exception ->
                        Log.e("DealsViewModel", "Failed to apply to deal", exception)
                        _applyToDealError.value = exception.message ?: "Failed to apply to deal"
                        _applyToDealSuccess.value = false
                    }
            } catch (e: Exception) {
                Log.e("DealsViewModel", "Exception in applyToDeal", e)
                _applyToDealError.value = "Network error: ${e.message}"
                _applyToDealSuccess.value = false
            } finally {
                _applyToDealLoading.value = false
            }
        }
    }

    /**
     * Get apply to deal message
     */
    fun getApplyToDealMessage(): String? {
        return _applyToDealResponse.value?.detail
    }

    /**
     * Clear apply to deal data
     */
    fun clearApplyToDeal() {
        _applyToDealResponse.value = null
        _applyToDealError.value = null
        _applyToDealSuccess.value = false
    }

    /**
     * Clear apply to deal error
     */
    fun clearApplyToDealError() {
        _applyToDealError.value = null
    }

    /**
     * Reset apply to deal success flag
     */
    fun resetApplyToDealSuccess() {
        _applyToDealSuccess.value = false
    }

    /**
     * Clear all data
     */
    fun clearAllData() {
        clearDeals()
        clearBrands()
        clearDefaultFilters()
        clearEmailGeneration()
        clearApplyToDeal()
    }

}

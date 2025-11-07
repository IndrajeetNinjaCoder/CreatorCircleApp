package com.cc.creatorcircle.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.cc.creatorcircle.data.models.DateRange
import com.cc.creatorcircle.data.models.InfluencerFilterRequest
import com.cc.creatorcircle.data.models.Influencers
import com.cc.creatorcircle.data.repository.InfluencerRepository
import kotlinx.coroutines.launch

class InfluencerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InfluencerRepository(application)

    // LiveData for influencers list
    private val _influencers = MutableLiveData<List<Influencers>>()
    val influencers: LiveData<List<Influencers>> = _influencers

    // LiveData for loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData for error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // LiveData for empty state
    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val _currentFilter = MutableLiveData<InfluencerFilterRequest?>()
    val currentFilter: LiveData<InfluencerFilterRequest?> = _currentFilter


    /**
     * Fetch suggested influencers
     */
    fun fetchSuggestedInfluencers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = repository.getSuggestedInfluencers()

                result.onSuccess { influencersList ->
                    _influencers.value = influencersList
                    _isEmpty.value = influencersList.isEmpty()
                    Log.d("InfluencerViewModel", "Successfully loaded ${influencersList.size} influencers")
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load influencers"
                    _isEmpty.value = true
                    Log.e("InfluencerViewModel", "Error loading influencers: ${exception.message}", exception)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isEmpty.value = true
                Log.e("InfluencerViewModel", "Exception in fetchSuggestedInfluencers", e)
            } finally {
                _isLoading.value = false
            }
        }
    }





    fun fetchFilteredInfluencers(filterRequest: InfluencerFilterRequest) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _currentFilter.value = filterRequest

                Log.d("InfluencerViewModel", "Fetching influencers with filters: $filterRequest")
                val result = repository.getFilteredInfluencers(filterRequest)

                result.onSuccess { influencersList ->
                    _influencers.value = influencersList
                    _isEmpty.value = influencersList.isEmpty()
                    Log.d("InfluencerViewModel", "Successfully loaded ${influencersList.size} filtered influencers")
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Failed to load filtered influencers"
                    _isEmpty.value = true
                    Log.e("InfluencerViewModel", "Error loading filtered influencers: ${exception.message}", exception)
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "An unexpected error occurred"
                _isEmpty.value = true
                Log.e("InfluencerViewModel", "Exception in fetchFilteredInfluencers", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyFilters(
        categories: List<String>? = null,
        priceRanges: List<List<Double>>? = null,
        followerRanges: List<List<Int>>? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        val filterRequest = InfluencerFilterRequest(
            categories = categories,
            price = priceRanges,
            followerRanges = followerRanges,
            dateRange = if (dateFrom != null && dateTo != null) {
                DateRange(dateFrom, dateTo)
            } else null
        )
        fetchFilteredInfluencers(filterRequest)
    }

    fun clearFilters() {
        _currentFilter.value = null
        fetchSuggestedInfluencers()
    }
















    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Retry loading influencers
     */
    fun retry() {
        fetchSuggestedInfluencers()
    }

    /**
     * Get influencer by user ID
     */
    fun getInfluencerById(userId: Int): Influencers? {
        return _influencers.value?.find { it.userId == userId }
    }

    /**
     * Filter influencers by minimum followers
     */
    fun filterByFollowers(minFollowers: Int): List<Influencers> {
        return _influencers.value?.filter {
            it.totalSocialMediaFollowers >= minFollowers
        } ?: emptyList()
    }

    /**
     * Filter influencers by maximum price
     */
    fun filterByMaxPrice(maxPrice: Double): List<Influencers> {
        return _influencers.value?.filter { influencer ->
            influencer.pricing.any { it.price <= maxPrice }
        } ?: emptyList()
    }

    /**
     * Sort influencers by followers (descending)
     */
    fun sortByFollowers(): List<Influencers> {
        return _influencers.value?.sortedByDescending {
            it.totalSocialMediaFollowers
        } ?: emptyList()
    }

    /**
     * Sort influencers by lowest price
     */
    fun sortByLowestPrice(): List<Influencers> {
        return _influencers.value?.sortedBy { influencer ->
            influencer.pricing.minOfOrNull { it.price } ?: Double.MAX_VALUE
        } ?: emptyList()
    }
}
package com.example.nutritionalappplanner.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.nutritionalappplanner.data.remote.FatSecretRepository

data class FoodDetail(
    val name: String,
    val serving: String?,
    val calories: Int,
    val fat: Double?,
    val carbs: Double?,
    val protein: Double?
)

sealed class FoodDetailState {
    object Loading : FoodDetailState()
    data class Success(val detail: FoodDetail) : FoodDetailState()
    data class Error(val message: String) : FoodDetailState()
}

class FoodDetailViewModel(
    private val repository: FatSecretRepository
) : ViewModel() {

    private val _state = MutableStateFlow<FoodDetailState>(FoodDetailState.Loading)
    val state: StateFlow<FoodDetailState> = _state

    fun load(foodId: String) {
        _state.value = FoodDetailState.Loading
        viewModelScope.launch {
            try {
                // You will implement this in FatSecretRepository
                val detail = repository.getFoodDetails(foodId)
                _state.value = FoodDetailState.Success(detail)
            } catch (e: Exception) {
                _state.value = FoodDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }
}


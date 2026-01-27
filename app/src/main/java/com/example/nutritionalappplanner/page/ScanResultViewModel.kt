package com.example.nutritionalappplanner.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritionalappplanner.data.remote.FatSecretRepository
import com.example.fitbite.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.nutritionalappplanner.data.remote.ClarifaiRepository

class ScanResultViewModel : ViewModel() {

    private val fatSecretRepository = FatSecretRepository(
        consumerKey = BuildConfig.FATSECRET_CONSUMER_KEY,
        consumerSecret = BuildConfig.FATSECRET_CONSUMER_SECRET
    )

    private val clarifaiRepository = ClarifaiRepository(
        apiKey = BuildConfig.CLARIFAI_API_KEY,
        workflowId = BuildConfig.CLARIFAI_WORKFLOW_ID,
        workflowVersion = BuildConfig.CLARIFAI_WORKFLOW_VERSION
    )

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state

    fun scanImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.value = ScanState.Loading
            try {
                // 1️⃣ Clarifai workflow detection
                val detectedFood: String = clarifaiRepository.recognizeFood(imageBytes)

                // Show the detected food immediately
                _state.value = ScanState.ClarifaiResult(detectedFood)

                // 2️⃣ FatSecret text search
                val nutrition = fatSecretRepository.searchFoodByName(detectedFood)

                _state.value = ScanState.Success(nutrition)

            } catch (e: Exception) {
                _state.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

package com.example.nutritionalappplanner.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutritionalappplanner.page.FatSecretRepository
import com.example.fitbite.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.nutritionalappplanner.data.remote.ClarifaiRepository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.example.fitbite.FoodItem

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

    // Firestore + Auth
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state

    fun scanImage(imageBytes: ByteArray) {
        viewModelScope.launch {
            _state.value = ScanState.Loading
            try {
                // Clarifai workflow detection
                val detectedFood: String = clarifaiRepository.recognizeFood(imageBytes)

                // Show the detected food immediately
                _state.value = ScanState.ClarifaiResult(detectedFood)

                // FatSecret text search (returns LIST)
                val results: List<FoodItem> = fatSecretRepository.searchFoodByName(detectedFood)

                // Pick top result only
                val top: FoodItem = results.firstOrNull()
                    ?: run {
                        _state.value = ScanState.Error("No nutrition results found for: $detectedFood")
                        return@launch
                    }

                // Save to Firestore

                // Update UI state (single item)
                _state.value = ScanState.Success(top)

            } catch (e: Exception) {
                _state.value = ScanState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun saveScannedFoodToFirestore(food: FoodItem) {
        val uid = auth.currentUser?.uid ?: return

        // If foodId is missing, we can't normalize properly
        val foodId = food.foodId ?: return

        //Save normalized nutrition info to: food_items/{foodId}
        val foodData = hashMapOf(
            "foodId" to foodId,
            "name" to food.name,
            "calories" to food.calories,
            "fat" to food.fat,
            "carbs" to food.carbs,
            "protein" to food.protein,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("food_items")
            .document(foodId)
            .set(foodData)

        // Save pantry instance to: users/{uid}/pantry_items/{autoId}
        val pantryData = hashMapOf(
            "foodId" to foodId,
            "nameSnapshot" to food.name,
            "storage" to "FRIDGE",            // default location
            "quantity" to 1,                  // default quantity
            "unit" to "count",                // default unit
            "createdAt" to System.currentTimeMillis(),
            "source" to "IMAGE"               // since this came from scanning
        )

        db.collection("users")
            .document(uid)
            .collection("pantry_items")
            .add(pantryData)
    }
}

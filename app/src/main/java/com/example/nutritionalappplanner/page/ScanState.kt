package com.example.nutritionalappplanner.page

import com.example.fitbite.FoodItem

sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()

    // State for when Clarifai returns a detected food name
    data class ClarifaiResult(val foodName: String) : ScanState()

    // FatSecret result
    data class Success(val food: FoodItem) : ScanState()
    data class Error(val message: String) : ScanState()
}

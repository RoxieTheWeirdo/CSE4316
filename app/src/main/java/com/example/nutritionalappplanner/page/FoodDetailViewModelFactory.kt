package com.example.nutritionalappplanner.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nutritionalappplanner.data.remote.FatSecretRepository
import com.example.fitbite.BuildConfig

class FoodDetailViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodDetailViewModel::class.java)) {
            // Reuse your existing repository and keys from BuildConfig
            val repo = FatSecretRepository(
                consumerKey = BuildConfig.FATSECRET_CONSUMER_KEY,
                consumerSecret = BuildConfig.FATSECRET_CONSUMER_SECRET
            )
            return FoodDetailViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

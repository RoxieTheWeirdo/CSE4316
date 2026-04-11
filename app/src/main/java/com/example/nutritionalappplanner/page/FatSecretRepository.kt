package com.example.nutritionalappplanner.page

import android.util.Base64
import android.util.Log
import com.example.fitbite.FoodItem
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class FatSecretRepository(
    consumerKey: String,
    consumerSecret: String
) {
    private val consumerKey = consumerKey.trim()
    private val consumerSecret = consumerSecret.trim()
    private val tokenMutex = Mutex()
    private var cachedToken: String? = null

    private val authApi: FatSecretAuthApi
    private val foodApi: FatSecretFoodApi

    init {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofitAuth = Retrofit.Builder()
            .baseUrl("https://oauth.fatsecret.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val retrofitFood = Retrofit.Builder()
            .baseUrl("https://platform.fatsecret.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        authApi = retrofitAuth.create(FatSecretAuthApi::class.java)
        foodApi = retrofitFood.create(FatSecretFoodApi::class.java)
    }

    //Search foods by name using FatSecret text API
    suspend fun searchFoodByName(foodName: String): List<FoodItem> {
        val token = getAccessToken()
        val response = foodApi.searchFoodByName("Bearer $token", foodName)
        val foods = response.foods?.food ?: emptyList()
        return foods.map { food ->
            val desc = food.foodDescription
            FoodItem(
                // ensure FoodItem has fields matching this order
                food.foodId,
                food.foodName,
                extractCalories(food.foodDescription),
                extractDouble(desc, "Fat"),
                extractDouble(desc, "Carbs"),
                extractDouble(desc, "Protein")
            )
        }
    }

    //Get detailed nutrition for a specific food by id (food.get)
    suspend fun getFoodDetails(foodId: String): FoodDetail {
        val token = getAccessToken()
        val response = foodApi.getFoodById("Bearer $token", foodId)
        val food = response.food ?: throw IllegalStateException("No food in response")
        val serving = food.servings?.serving

        return FoodDetail(
            name = food.foodName,
            serving = serving?.servingDescription,
            calories = serving?.calories?.toDoubleOrNull()?.toInt() ?: 0,
            fat = serving?.fat?.toDoubleOrNull(),
            carbs = serving?.carbs?.toDoubleOrNull(),
            protein = serving?.protein?.toDoubleOrNull()
        )
    }

    //Get OAuth access token from FatSecret
    private suspend fun getAccessToken(): String = tokenMutex.withLock {
        cachedToken?.let { return it }

        val credentials = "$consumerKey:$consumerSecret"
        val encoded = Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

        val tokenResponse = authApi.getAccessToken(
            "client_credentials",
            "basic",
            "Basic $encoded"
        )

        Log.d("FatSecret", "Access token: ${tokenResponse.accessToken}")
        cachedToken = tokenResponse.accessToken
        return cachedToken!!
    }

    // Helper to pull kcal out of FatSecret's food_description text
    private fun extractCalories(desc: String?): Int {
        if (desc == null) return 0
        val match = Regex("Calories:\\s*(\\d+)kcal").find(desc)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun extractDouble(desc: String?, label: String): Double {
        if (desc == null) return 0.0
        val regex = Regex("$label:\\s*([0-9.]+)g")
        val match = regex.find(desc)
        return match?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    }

}


import retrofit2.http.*
import com.google.gson.annotations.SerializedName

interface FatSecretFoodApi {

    // Text-based foods.search (used with Clarifai label)
    @FormUrlEncoded
    @POST("rest/server.api")
    suspend fun searchFoodByName(
        @Header("Authorization") authorization: String,
        @Field("search_expression") query: String,
        @Field("method") method: String = "foods.search",
        @Field("format") format: String = "json"
    ): FoodSearchResponse

    // food.get for full nutrition by food_id
    @FormUrlEncoded
    @POST("rest/server.api")
    suspend fun getFoodById(
        @Header("Authorization") authorization: String,
        @Field("food_id") foodId: String,
        @Field("method") method: String = "food.get",
        @Field("format") format: String = "json"
    ): FoodDetailResponse
}

// foods.search models
data class FoodSearchResponse(
    @SerializedName("foods") val foods: FoodList?
)

data class FoodList(
    @SerializedName("food") val food: List<SearchFood>
)

data class SearchFood(
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_description") val foodDescription: String?
)

// food.get models (simplified to first serving)
data class FoodDetailResponse(
    @SerializedName("food") val food: DetailFood?
)

data class DetailFood(
    @SerializedName("food_name") val foodName: String,
    @SerializedName("servings") val servings: Servings?
)

data class Servings(
    @SerializedName("serving") val serving: Serving
)

data class Serving(
    @SerializedName("serving_description") val servingDescription: String?,
    @SerializedName("calories") val calories: String?,
    @SerializedName("fat") val fat: String?,
    @SerializedName("carbohydrate") val carbs: String?,
    @SerializedName("protein") val protein: String?
)

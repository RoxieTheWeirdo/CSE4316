import retrofit2.http.*
import com.google.gson.annotations.SerializedName

interface FatSecretAuthApi {
    @FormUrlEncoded
    @POST("connect/token")
    suspend fun getAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "basic",
        @Header("Authorization") authHeader: String
    ): TokenResponse
}

data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("expires_in") val expiresIn: Int
)

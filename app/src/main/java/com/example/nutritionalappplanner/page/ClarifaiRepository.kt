package com.example.nutritionalappplanner.data.remote

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ClarifaiRepository(
    private val apiKey: String,
    private val workflowId: String,
    private val workflowVersion: String
) {

    private val api: ClarifaiApi

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clarifai.com/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ClarifaiApi::class.java)
    }

    suspend fun recognizeFood(imageBytes: ByteArray): String =
        withContext(Dispatchers.IO) {

            // Convert image to Base64
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            // Build the correct workflow request
            val request = ClarifaiRequest(
                inputs = listOf(
                    ClarifaiInput(
                        data = ClarifaiData(
                            image = ClarifaiImage(
                                base64 = base64Image
                            )
                        )
                    )
                )
            )

            // Call workflow endpoint
            val response = api.runWorkflow(
                authHeader = "Key $apiKey",
                workflowId = workflowId,
                versionId = workflowVersion,
                request = request
            )

            // Parse top result
            val topConcept = response.results
                ?.firstOrNull()
                ?.outputs
                ?.firstOrNull()
                ?.data
                ?.concepts
                ?.maxByOrNull { it.value }

            return@withContext topConcept?.name ?: "Unknown"
        }
}



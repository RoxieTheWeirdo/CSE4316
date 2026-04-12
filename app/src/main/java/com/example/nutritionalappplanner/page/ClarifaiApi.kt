package com.example.nutritionalappplanner.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ClarifaiApi {

    @POST("users/{userId}/apps/{appId}/workflows/{workflowId}/versions/{versionId}/results")
    suspend fun runWorkflow(
        @Header("Authorization") authHeader: String,
        @Path("workflowId") workflowId: String,
        @Path("versionId") versionId: String,
        @Body request: ClarifaiRequest,
        @Path("userId") userId: String = "xb8v9ik6mz12",
        @Path("appId") appId: String = "FitBite"
    ): ClarifaiWorkflowResponse
}

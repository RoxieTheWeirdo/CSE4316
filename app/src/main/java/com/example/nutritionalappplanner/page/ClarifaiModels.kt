package com.example.nutritionalappplanner.data.remote

import com.google.gson.annotations.SerializedName

// --------------------------
// Request Models
// --------------------------
data class ClarifaiRequest(
    val inputs: List<ClarifaiInput>
) {
    constructor(base64Image: String) : this(
        inputs = listOf(
            ClarifaiInput(
                data = ClarifaiData(
                    image = ClarifaiImage(base64Image)
                )
            )
        )
    )
}

data class ClarifaiInput(
    val data: ClarifaiData
)

data class ClarifaiData(
    val image: ClarifaiImage
)

data class ClarifaiImage(
    val base64: String
)


// --------------------------
// Response Models (Updated for Workflow Results)
// --------------------------
data class ClarifaiWorkflowResponse(
    val results: List<ClarifaiWorkflowResult>?
)

data class ClarifaiWorkflowResult(
    val outputs: List<ClarifaiOutput>?
)

data class ClarifaiOutput(
    val data: ClarifaiConceptData?
)

data class ClarifaiConceptData(
    val concepts: List<ClarifaiConcept>?
)

data class ClarifaiConcept(
    val id: String?,
    val name: String?,
    val value: Float
)

package com.example.nutritionalappplanner.page

data class RecipeMatchUi(
    val recipeId: String,
    val title: String,
    val matched: Int,
    val total: Int,
    val percentage: Int,
    val missing: List<String>
)

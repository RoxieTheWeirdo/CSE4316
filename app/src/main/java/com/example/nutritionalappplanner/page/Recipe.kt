package com.example.nutritionalappplanner.page

data class Recipe(
    val title: String = "",
    val ingredients: List<String> = emptyList(),
    val ingredientTokens: List<String> = emptyList(),
    val imageUrl: String = "",
    val sourceUrl: String = ""
)

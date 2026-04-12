package com.example.nutritionalappplanner.page


data class PantryItem(
    val pantryItemId: String = "",   // Firestore doc id (fill when reading)
    val foodId: String = "",
    val nameSnapshot: String = "",
    val storage: String = "PANTRY",
    val quantity: Long = 1,
    val unit: String = "count",
    val createdAt: Long = 0,
    val source: String = "UNKNOWN"
)
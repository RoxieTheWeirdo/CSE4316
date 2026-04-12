package com.example.nutritionalappplanner.page

object RecipeMatcher {

    // Words we ignore so matching isn't noisy
    private val stopWords = setOf(
        "fresh", "chopped", "ground", "large", "small", "to", "taste",
        "and", "or", "of", "a", "an", "the",
        "c", "cup", "cups", "tsp", "tbsp"
    )
    private val excludedWords = setOf(
        "broth", "stock", "sauce", "oil", "juice"
    )

    fun tokenize(text: String): Set<String> {
        return text
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")   // remove punctuation
            .split(Regex("\\s+"))                 // split by spaces
            .map { it.trim() }
            .filter { it.isNotBlank() && it !in stopWords }
            .map { singularize(it) }
            .toSet()
    }

    //singular converter (so eggs -> egg, tomatoes -> tomato)
    private fun singularize(w: String): String {
        return when {
            w.endsWith("ies") && w.length > 3 -> w.dropLast(3) + "y"
            w.endsWith("es") && w.length > 3 -> w.dropLast(2)
            w.endsWith("s") && w.length > 2 -> w.dropLast(1)
            else -> w
        }
    }

    //pantryTokens: tokens from all pantry items recipes: list of (docId, Recipe)
    fun matchRecipes(
        pantryTokens: Set<String>,
        recipes: List<Pair<String, Recipe>>,
        requiredTokens: Set<String> = emptySet(),
        limit: Int = 10
    ): List<RecipeMatchUi> {

        val matches = recipes.mapNotNull { (id, r) ->

            if (r.ingredients.isEmpty()) return@mapNotNull null

            // Required filter still uses tokens
            if (requiredTokens.isNotEmpty()) {
                val allRecipeTokens = if (r.ingredientTokens.isNotEmpty()) {
                    r.ingredientTokens.map { singularize(it.lowercase().trim()) }.toSet()
                } else {
                    r.ingredients.flatMap { tokenize(it) }.toSet()
                }

                if (allRecipeTokens.intersect(requiredTokens).isEmpty()) {
                    return@mapNotNull null
                }
            }

            var matchedCount = 0
            val missingIngredients = mutableListOf<String>()

            r.ingredients.forEach { ingredient ->

                val ingredientTokens = tokenize(ingredient)
                if (ingredientTokens.any { it in excludedWords }) {
                    missingIngredients.add(ingredient)
                    return@forEach
                }
                val matchedTokens = ingredientTokens.count { token ->
                    pantryTokens.contains(token)
                }

                val isMatched = ingredientTokens.any { token ->
                    pantryTokens.contains(token)
                }

                if (isMatched) {
                    matchedCount++
                } else {
                    missingIngredients.add(ingredient)
                }
            }
            val totalIngredients = r.ingredients.size
            val missingCount = totalIngredients - matchedCount

            val score = (matchedCount * 2) - (missingCount * 0.0)
            val maxScore = totalIngredients * 2

            val percentage = if (maxScore == 0) {
                0
            } else {
                ((score.toDouble() / maxScore) * 100)
                    .coerceIn(0.0, 100.0)
                    .toInt()
            }
            RecipeMatchUi(
                recipeId = id,
                title = r.title,
                matched = matchedCount,
                total = totalIngredients,
                percentage = percentage,
                missing = missingIngredients
            )
        }

        return matches
            .sortedByDescending { it.percentage }
            .take(limit)
    }
}

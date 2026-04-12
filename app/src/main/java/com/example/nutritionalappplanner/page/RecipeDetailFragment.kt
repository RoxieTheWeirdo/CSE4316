package com.example.nutritionalappplanner.page

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.example.fitbite.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecipeDetailFragment : Fragment(R.layout.fragment_recipe_detail) {

    private val db = FirebaseFirestore.getInstance()

    private var recipeId: String? = null
    private var title: String? = null
    private var matched: Int = 0
    private var total: Int = 0
    private var missing: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            recipeId = it.getString("recipeId")
            title = it.getString("title")
            matched = it.getInt("matched")
            total = it.getInt("total")
            missing = it.getStringArrayList("missing") ?: emptyList()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTitle = view.findViewById<TextView>(R.id.tvRecipeTitle)
        val tvMatch = view.findViewById<TextView>(R.id.tvRecipeMatch)
        val layoutIngredients = view.findViewById<LinearLayout>(R.id.layoutIngredients)
        val layoutInstructions = view.findViewById<LinearLayout>(R.id.layoutInstructions)
        val tvInstructions = view.findViewById<TextView>(R.id.tvInstructions)

        val tabIngredients = view.findViewById<View>(R.id.tabIngredients)
        val tabInstructions = view.findViewById<View>(R.id.tabInstructions)

        val tvIngredientsTab = view.findViewById<TextView>(R.id.tvIngredientsTab)
        val tvInstructionsTab = view.findViewById<TextView>(R.id.tvInstructionsTab)

        val underlineIngredients = view.findViewById<View>(R.id.underlineIngredients)
        val underlineInstructions = view.findViewById<View>(R.id.underlineInstructions)

        // -------- BASIC INFO --------
        tvTitle.text = title ?: "Recipe"

        val percent = if (total > 0) {
            ((matched.toFloat() / total) * 100).toInt()
        } else 0

        tvMatch.text = "$matched/$total matched ($percent%)"

        // -------- TAB LOGIC --------
        layoutIngredients.visibility = View.VISIBLE
        layoutInstructions.visibility = View.GONE

        tabIngredients.setOnClickListener {
            layoutIngredients.visibility = View.VISIBLE
            layoutInstructions.visibility = View.GONE

            tvIngredientsTab.setTextColor(Color.WHITE)
            tvIngredientsTab.setTypeface(null, Typeface.BOLD)
            underlineIngredients.visibility = View.VISIBLE

            tvInstructionsTab.setTextColor(Color.parseColor("#9AA0A6"))
            tvInstructionsTab.setTypeface(null, Typeface.NORMAL)
            underlineInstructions.visibility = View.INVISIBLE
        }

        tabInstructions.setOnClickListener {
            layoutIngredients.visibility = View.GONE
            layoutInstructions.visibility = View.VISIBLE

            tvInstructionsTab.setTextColor(Color.WHITE)
            tvInstructionsTab.setTypeface(null, Typeface.BOLD)
            underlineInstructions.visibility = View.VISIBLE

            tvIngredientsTab.setTextColor(Color.parseColor("#9AA0A6"))
            tvIngredientsTab.setTypeface(null, Typeface.NORMAL)
            underlineIngredients.visibility = View.INVISIBLE
        }

        // -------- TOOLBAR (CHECKMARK) --------
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.food_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        showCookDialog()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)

        // -------- FETCH DATA --------
        recipeId?.let { id ->
            db.collection("recipes")
                .document(id)
                .get()
                .addOnSuccessListener { doc ->

                    val ingredients = doc.get("ingredients") as? List<String> ?: emptyList()
                    val instructionsList = doc.get("instructions") as? List<String> ?: emptyList()

                    layoutIngredients.removeAllViews()

                    ingredients.forEach { ingredient ->
                        val tv = TextView(requireContext())
                        tv.textSize = 16f
                        tv.setPadding(0, 12, 0, 12)

                        if (missing.contains(ingredient.lowercase())) {
                            tv.setTextColor(Color.RED)
                            tv.text = "✖ $ingredient"
                        } else {
                            tv.setTextColor(Color.parseColor("#2E7D32"))
                            tv.text = "✔ $ingredient"
                        }

                        layoutIngredients.addView(tv)
                    }

                    val formattedInstructions = instructionsList.mapIndexed { index, step ->
                        "${index + 1}. $step"
                    }.joinToString("\n\n")

                    tvInstructions.text = formattedInstructions
                }
        }
    }

    // -------- HELPERS --------
    private fun extractQuantity(text: String): Double {
        val fractionRegex = Regex("(\\d+)/(\\d+)")
        val fractionMatch = fractionRegex.find(text)

        if (fractionMatch != null) {
            val (num, denom) = fractionMatch.destructured
            return num.toDouble() / denom.toDouble()
        }

        val decimalRegex = Regex("(\\d+\\.?\\d*)")
        return decimalRegex.find(text)?.value?.toDoubleOrNull() ?: 1.0
    }

    // -------- COOK LOGIC --------
    private fun cookRecipe() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("recipes")
            .document(recipeId ?: return)
            .get()
            .addOnSuccessListener { doc ->

                val ingredients = doc.get("ingredients") as? List<String> ?: emptyList()

                db.collection("users").document(uid)
                    .collection("pantry_items")
                    .get()
                    .addOnSuccessListener { snapshot ->

                        val pantryItems = snapshot.documents

                        ingredients.forEach { ingredient ->

                            val ingredientTokens = RecipeMatcher.tokenize(ingredient)
                            val neededQty = extractQuantity(ingredient)

                            val match = pantryItems.find { p ->
                                val name = p.getString("nameSnapshot") ?: ""
                                val pantryTokens = RecipeMatcher.tokenize(name)
                                ingredientTokens.any { it in pantryTokens }
                            }

                            if (match != null) {
                                val currentQty = when (val q = match.get("quantity")) {
                                    is Number -> q.toDouble()
                                    is String -> q.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }

                                val newQty = (currentQty - neededQty).coerceAtLeast(0.0)

                                db.collection("users").document(uid)
                                    .collection("pantry_items")
                                    .document(match.id)
                                    .update("quantity", newQty)
                            }
                        }

                        Toast.makeText(requireContext(), "Cooked! Pantry updated.", Toast.LENGTH_SHORT).show()
                    }
            }
    }
    private fun showCookDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Cook Recipe?")
            .setMessage("This will update your pantry quantities.")
            .setPositiveButton("Cook") { _, _ ->
                cookRecipe()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

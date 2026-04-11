package com.example.nutritionalappplanner.page

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class PantryItemDetailFragment : Fragment(R.layout.fragment_pantry_item_detail) {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var tvName: TextView

    // Form UI
    private lateinit var actQuantity: AutoCompleteTextView
    private lateinit var actUnit: AutoCompleteTextView
    private lateinit var actStorage: AutoCompleteTextView

    // Recipes UI
    private lateinit var rvRecipes: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter

    private var pantryId: String? = null
    private var nameSnapshot: String? = null

    private lateinit var tvServing: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvProtein: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pantryId = it.getString("pantryId")
            nameSnapshot = it.getString("nameSnapshot")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tvPantryName)

        tvServing = view.findViewById(R.id.tvServing)
        tvCalories = view.findViewById(R.id.tvCalories)
        tvFat = view.findViewById(R.id.tvFat)
        tvCarbs = view.findViewById(R.id.tvCarbs)
        tvProtein = view.findViewById(R.id.tvProtein)

        actQuantity = view.findViewById(R.id.actQuantity)
        actUnit = view.findViewById(R.id.actUnit)
        actStorage = view.findViewById(R.id.actStorage)

        tvName.text = nameSnapshot ?: "(Unnamed item)"

        setupDropdowns()
        loadPantryDetails()

        // TOOLBAR
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(requireContext().getColor(android.R.color.white))

        // CHECKMARK MENU
        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.food_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        saveChanges()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)

        setupRecipes(view)
        loadRecipeSuggestions()
    }

    private fun setupDropdowns() {

        val numbers = (1..100).map { it.toString() }
        val quantityAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_dark,
            numbers
        )
        quantityAdapter.setDropDownViewResource(R.layout.spinner_item_dark)
        actQuantity.setAdapter(quantityAdapter)

        val units = listOf("count", "grams", "lbs", "oz", "ml", "liters")
        val unitAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_dark,
            units
        )
        unitAdapter.setDropDownViewResource(R.layout.spinner_item_dark)
        actUnit.setAdapter(unitAdapter)

        val storageOptions = listOf("FRIDGE", "FREEZER", "PANTRY")
        val storageAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_dark,
            storageOptions
        )
        storageAdapter.setDropDownViewResource(R.layout.spinner_item_dark)
        actStorage.setAdapter(storageAdapter)
    }

    private fun loadPantryDetails() {
        val uid = auth.currentUser?.uid ?: return
        val id = pantryId ?: return

        db.collection("users").document(uid)
            .collection("pantry_items").document(id)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) return@addOnSuccessListener

                val quantity = doc.getDouble("quantity") ?: 1.0
                val unit = doc.getString("unit") ?: "count"
                val storage = doc.getString("storage") ?: "PANTRY"
                val foodId = doc.getString("foodId")

                actQuantity.setText(quantity.toInt().toString(), false)
                actUnit.setText(unit, false)
                actStorage.setText(storage, false)

                if (!foodId.isNullOrBlank()) {

                    db.collection("food_items").document(foodId)
                        .get()
                        .addOnSuccessListener { foodDoc ->

                            if (!foodDoc.exists()) return@addOnSuccessListener

                            val name = foodDoc.getString("name") ?: nameSnapshot ?: "-"

                            val calories = (foodDoc.get("calories") as? Number)?.toDouble() ?: 0.0
                            val fat = (foodDoc.get("fat") as? Number)?.toDouble() ?: 0.0
                            val carbs = (foodDoc.get("carbs") as? Number)?.toDouble() ?: 0.0
                            val protein = (foodDoc.get("protein") as? Number)?.toDouble() ?: 0.0

                            tvName.text = name
                            tvServing.text = "Serving: 1"
                            tvCalories.text = "Calories: ${calories.toInt()} kcal"
                            tvFat.text = "Fat: $fat g"
                            tvCarbs.text = "Carbs: $carbs g"
                            tvProtein.text = "Protein: $protein g"
                        }
                }
            }
    }

    private fun saveChanges() {
        val uid = auth.currentUser?.uid ?: return
        val id = pantryId ?: return

        val quantity = actQuantity.text.toString().toDoubleOrNull() ?: 0.0
        val unit = actUnit.text.toString().trim()
        val storage = actStorage.text.toString()

        if (quantity <= 0) {
            Toast.makeText(requireContext(), "Invalid quantity.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(uid)
            .collection("pantry_items").document(id)
            .update(
                mapOf(
                    "quantity" to quantity,
                    "unit" to unit,
                    "storage" to storage
                )
            )
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Item updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Update failed.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete() {
        val uid = auth.currentUser?.uid ?: return
        val id = pantryId ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("users").document(uid)
                    .collection("pantry_items").document(id)
                    .delete()
                    .addOnSuccessListener {
                        parentFragmentManager.popBackStack()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecipes(view: View) {
        rvRecipes = view.findViewById(R.id.rvRecipes)
        recipeAdapter = RecipeAdapter { recipe ->
            val fragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("recipeId", recipe.recipeId)
                    putString("title", recipe.title)
                    putInt("matched", recipe.matched)
                    putInt("total", recipe.total)
                    putStringArrayList("missing", ArrayList(recipe.missing))
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        rvRecipes.adapter = recipeAdapter
    }

    private fun loadRecipeSuggestions() {
        val uid = auth.currentUser?.uid ?: return
        val requiredTokens = RecipeMatcher.tokenize(nameSnapshot ?: "")

        db.collection("users").document(uid)
            .collection("pantry_items")
            .get()
            .addOnSuccessListener { pantrySnap ->

                val pantryTokens = pantrySnap.documents
                    .mapNotNull { it.getString("nameSnapshot") }
                    .flatMap { RecipeMatcher.tokenize(it) }
                    .toSet()

                val recipeQuery = db.collection("recipes")

                recipeQuery.get().addOnSuccessListener { recipeSnap ->

                    val recipes = recipeSnap.documents.mapNotNull { doc ->
                        val r = doc.toObject(Recipe::class.java) ?: return@mapNotNull null
                        doc.id to r
                    }

                    val matches = RecipeMatcher.matchRecipes(
                        pantryTokens = pantryTokens,
                        recipes = recipes,
                        requiredTokens = requiredTokens,
                        limit = 10
                    )

                    recipeAdapter.submitList(matches)
                }
            }
    }
}
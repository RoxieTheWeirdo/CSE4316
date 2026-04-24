package com.example.nutritionalappplanner.page

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.example.fitbite.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import androidx.fragment.app.FragmentManager
import com.example.nutritionalappplanner.page.PantryFragment
import androidx.core.content.ContextCompat

class FoodDetailFragment : Fragment() {

    private lateinit var tvFoodName: TextView
    private lateinit var tvServing: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvStatus: TextView

    // NEW INPUTS
    private lateinit var actQuantity: AutoCompleteTextView
    private lateinit var actUnit: AutoCompleteTextView
    private lateinit var actStorage: AutoCompleteTextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // STORE ARGS FOR SAVE (unchanged logic)
    private var foodId: String? = null
    private var name: String = "-"
    private var calories: Int = 0
    private var fat: Double = 0.0
    private var carbs: Double = 0.0
    private var protein: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_food_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvFoodName = view.findViewById(R.id.tvFoodName)
        tvServing = view.findViewById(R.id.tvServing)
        tvCalories = view.findViewById(R.id.tvCalories)
        tvFat = view.findViewById(R.id.tvFat)
        tvCarbs = view.findViewById(R.id.tvCarbs)
        tvProtein = view.findViewById(R.id.tvProtein)
        tvStatus = view.findViewById(R.id.tvStatus)

        // NEW DROPDOWNS
        actQuantity = view.findViewById(R.id.actQuantity)
        actUnit = view.findViewById(R.id.actUnit)
        actStorage = view.findViewById(R.id.actStorage)

        val args = requireArguments()

        // SAME DATA FLOW (UNCHANGED)
        foodId = args.getString("foodId")
        name = args.getString("foodName") ?: "-"
        calories = args.getInt("calories", 0)
        fat = args.getDouble("fat", 0.0)
        carbs = args.getDouble("carbs", 0.0)
        protein = args.getDouble("protein", 0.0)

        val defaultStorage = args.getString("defaultStorage") ?: "FRIDGE"

        tvStatus.text = ""
        tvFoodName.text = name
        tvServing.text = "Serving: 1"
        tvCalories.text = "Calories: $calories kcal"
        tvFat.text = "Fat: $fat g"
        tvCarbs.text = "Carbs: $carbs g"
        tvProtein.text = "Protein: $protein g"

        setupDropdowns(defaultStorage)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        val textColor = ContextCompat.getColor(requireContext(), R.color.textPrimary)

        toolbar.setTitleTextColor(textColor)
        toolbar.overflowIcon?.setTint(textColor)
        // CHECKMARK MENU
        requireActivity().addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.food_detail_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        saveToPantry()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)
    }

    // DROPDOWNS SETUP
    private fun setupDropdowns(defaultStorage: String) {

        val numbers = (1..100).map { it.toString() }
        actQuantity.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_item_dark, numbers))
        actQuantity.setText("1", false)

        val units = listOf("count", "grams", "lbs", "oz", "ml", "liters")
        actUnit.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_item_dark, units))
        actUnit.setText("count", false)

        val storageOptions = listOf("FRIDGE", "FREEZER", "PANTRY")
        actStorage.setAdapter(ArrayAdapter(requireContext(), R.layout.spinner_item_dark, storageOptions))
        actStorage.setText(defaultStorage, false)
    }

    // SAVE LOGIC
    private fun saveToPantry() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Not signed in.", Toast.LENGTH_SHORT).show()
            return
        }

        if (foodId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Missing foodId (cannot save).", Toast.LENGTH_SHORT).show()
            return
        }

        val quantity = actQuantity.text.toString().toIntOrNull() ?: 1
        val unit = actUnit.text.toString()
        val storage = actStorage.text.toString()

        tvStatus.text = "Saving to pantry..."

        val foodData = hashMapOf(
            "foodId" to foodId,
            "name" to name,
            "calories" to calories,
            "fat" to fat,
            "carbs" to carbs,
            "protein" to protein,
            "updatedAt" to System.currentTimeMillis()
        )

        db.collection("food_items")
            .document(foodId!!)
            .set(foodData, SetOptions.merge())
            .addOnSuccessListener {

                val pantryData = hashMapOf(
                    "foodId" to foodId,
                    "nameSnapshot" to name,
                    "storage" to storage,
                    "quantity" to quantity,
                    "unit" to unit,
                    "createdAt" to System.currentTimeMillis(),
                    "source" to "SCAN"
                )

                db.collection("users")
                    .document(uid)
                    .collection("pantry_items")
                    .add(pantryData)
                    .addOnSuccessListener {
                        tvStatus.text = "Added to pantry"
                        Toast.makeText(requireContext(), "Added to pantry!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, PantryFragment())
                            .commit()
                    }
                    .addOnFailureListener { e ->
                        tvStatus.text = "Failed to add pantry item."
                        Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                tvStatus.text = "Failed to save food info."
                Toast.makeText(requireContext(), e.message ?: "Error", Toast.LENGTH_SHORT).show()
            }
    }
}
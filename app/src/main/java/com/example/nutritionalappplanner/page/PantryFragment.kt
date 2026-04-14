package com.example.nutritionalappplanner.page

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.ContextCompat

class PantryFragment : Fragment() {

    private val viewModel: PantryViewModel by viewModels()

    private lateinit var rv: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var empty: TextView
    private lateinit var adapter: PantryAdapter

    private lateinit var searchInput: EditText
    private lateinit var scanButton: ImageButton

    private var isSearching = false // prevents duplicate calls

    private val fatSecretRepository = FatSecretRepository(
        consumerKey = BuildConfig.FATSECRET_CONSUMER_KEY,
        consumerSecret = BuildConfig.FATSECRET_CONSUMER_SECRET
    )

    // Tabs
    private lateinit var tabAll: View
    private lateinit var tabFridge: View
    private lateinit var tabFreezer: View
    private lateinit var tabPantry: View

    private lateinit var tvAll: TextView
    private lateinit var tvFridge: TextView
    private lateinit var tvFreezer: TextView
    private lateinit var tvPantry: TextView

    private lateinit var underlineAll: View
    private lateinit var underlineFridge: View
    private lateinit var underlineFreezer: View
    private lateinit var underlinePantry: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pantry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Search + Scan
        searchInput = view.findViewById(R.id.searchInput)
        scanButton = view.findViewById(R.id.scanButton)

        scanButton.setOnClickListener {
            requireActivity().findViewById<View>(R.id.bottom_nav).visibility = View.GONE
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ScanCameraFragment())
                .addToBackStack(null)
                .commit()
        }

        // ONLY ONE listener (fixes double popup)
        searchInput.setOnEditorActionListener { _, actionId, event ->
            val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (isSearchAction) {
                val query = searchInput.text.toString().trim()

                if (query.isNotEmpty()) {
                    hideKeyboard()
                    searchFoods(query)
                } else {
                    Toast.makeText(requireContext(), "Type something to search.", Toast.LENGTH_SHORT).show()
                }
                true
            } else false
        }

        // Recycler
        rv = view.findViewById(R.id.rvPantry)
        progress = view.findViewById(R.id.progressBar)
        empty = view.findViewById(R.id.tvEmpty)

        adapter = PantryAdapter(
            onClick = { row ->
                val frag = PantryItemDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("pantryId", row.docId)
                        putString("nameSnapshot", row.item.nameSnapshot)
                        putString("storage", row.item.storage)
                    }
                }

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, frag)
                    .addToBackStack(null)
                    .commit()
            },
            onLongClick = { row ->
                confirmDelete(row)
            }
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        // Tabs
        tabAll = view.findViewById(R.id.tabAll)
        tabFridge = view.findViewById(R.id.tabFridge)
        tabFreezer = view.findViewById(R.id.tabFreezer)
        tabPantry = view.findViewById(R.id.tabPantry)

        tvAll = view.findViewById(R.id.tvAll)
        tvFridge = view.findViewById(R.id.tvFridge)
        tvFreezer = view.findViewById(R.id.tvFreezer)
        tvPantry = view.findViewById(R.id.tvPantry)

        underlineAll = view.findViewById(R.id.underlineAll)
        underlineFridge = view.findViewById(R.id.underlineFridge)
        underlineFreezer = view.findViewById(R.id.underlineFreezer)
        underlinePantry = view.findViewById(R.id.underlinePantry)

        tabAll.setOnClickListener {
            selectTab(null)
            viewModel.startListening(null)
        }

        tabFridge.setOnClickListener {
            selectTab("FRIDGE")
            viewModel.startListening("FRIDGE")
        }

        tabFreezer.setOnClickListener {
            selectTab("FREEZER")
            viewModel.startListening("FREEZER")
        }

        tabPantry.setOnClickListener {
            selectTab("PANTRY")
            viewModel.startListening("PANTRY")
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is PantryUiState.Loading -> {
                        progress.visibility = View.VISIBLE
                        empty.visibility = View.GONE
                    }

                    is PantryUiState.Loaded -> {
                        progress.visibility = View.GONE
                        adapter.submitList(state.items)
                        empty.visibility =
                            if (state.items.isEmpty()) View.VISIBLE else View.GONE
                    }

                    is PantryUiState.Error -> {
                        progress.visibility = View.GONE
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        selectTab(null)
        viewModel.startListening(null)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                systemBars.bottom
            )

            insets
        }
    }

    // API SEARCH
    private fun searchFoods(query: String) {
        if (isSearching) return
        isSearching = true

        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            try {
                val results = fatSecretRepository.searchFoodByName(query)

                if (results.isEmpty()) {
                    Toast.makeText(requireContext(), "No results found.", Toast.LENGTH_SHORT).show()
                } else {
                    showSearchResults(results)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Search failed.", Toast.LENGTH_SHORT).show()
            } finally {
                progress.visibility = View.GONE
                isSearching = false
            }
        }
    }

    private fun showSearchResults(results: List<FoodItem>) {
        val sheet = SearchResultsBottomSheet(results) { item ->
            openFoodDetail(item)
        }

        sheet.show(parentFragmentManager, "SearchResultsBottomSheet")
    }

    private fun openFoodDetail(item: FoodItem) {
        val fragment = FoodDetailFragment().apply {
            arguments = Bundle().apply {
                putString("foodId", item.foodId)
                putString("foodName", item.name)
                putInt("calories", item.calories)
                putDouble("fat", item.fat)
                putDouble("carbs", item.carbs)
                putDouble("protein", item.protein)
                putString("defaultStorage", "PANTRY")
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
    }

    private fun selectTab(selected: String?) {

        underlineAll.visibility = View.INVISIBLE
        underlineFridge.visibility = View.INVISIBLE
        underlineFreezer.visibility = View.INVISIBLE
        underlinePantry.visibility = View.INVISIBLE

        val selectedColor = ContextCompat.getColor(requireContext(), R.color.textPrimary)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.textSecondary)

        // Reset all to unselected
        tvAll.setTextColor(unselectedColor)
        tvFridge.setTextColor(unselectedColor)
        tvFreezer.setTextColor(unselectedColor)
        tvPantry.setTextColor(unselectedColor)

        // Apply selected state
        when (selected) {
            null -> {
                underlineAll.visibility = View.VISIBLE
                tvAll.setTextColor(selectedColor)
            }
            "FRIDGE" -> {
                underlineFridge.visibility = View.VISIBLE
                tvFridge.setTextColor(selectedColor)
            }
            "FREEZER" -> {
                underlineFreezer.visibility = View.VISIBLE
                tvFreezer.setTextColor(selectedColor)
            }
            "PANTRY" -> {
                underlinePantry.visibility = View.VISIBLE
                tvPantry.setTextColor(selectedColor)
            }
        }
    }

    private fun confirmDelete(row: PantryItemUi) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete item?")
            .setMessage("Delete ${row.item.nameSnapshot}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("pantry_items").document(row.docId)
                    .delete()
                    .addOnSuccessListener {
                        viewModel.startListening(null) // force refresh
                    }
            }
            .show()
    }
}
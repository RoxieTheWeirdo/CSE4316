package com.example.nutritionalappplanner.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.BuildConfig
import com.example.fitbite.FoodItem
import com.example.fitbite.R
import kotlinx.coroutines.launch

class PantryTextSearchFragment : Fragment() {

    private lateinit var etQuery: EditText
    private lateinit var btnSearch: Button
    private lateinit var progress: ProgressBar
    private lateinit var rvResults: RecyclerView

    private lateinit var adapter: FoodListAdapter

    private val fatSecretRepository = com.example.nutritionalappplanner.page.FatSecretRepository(
        consumerKey = BuildConfig.FATSECRET_CONSUMER_KEY,
        consumerSecret = BuildConfig.FATSECRET_CONSUMER_SECRET
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_pantry_text_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etQuery = view.findViewById(R.id.etQuery)
        btnSearch = view.findViewById(R.id.btnSearch)
        progress = view.findViewById(R.id.progress)
        rvResults = view.findViewById(R.id.rvResults)

        // clicking a result opens FoodDetail instead of saving immediately
        adapter = FoodListAdapter { item ->
            openFoodDetail(item)
        }

        rvResults.layoutManager = LinearLayoutManager(requireContext())
        rvResults.adapter = adapter

        btnSearch.setOnClickListener {
            val q = etQuery.text.toString().trim()
            if (q.isBlank()) {
                Toast.makeText(requireContext(), "Type something to search.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            searchFoods(q)
        }
    }

    private fun searchFoods(query: String) {
        lifecycleScope.launch {
            progress.visibility = View.VISIBLE
            btnSearch.isEnabled = false
            try {
                val results = fatSecretRepository.searchFoodByName(query)
                adapter.submitList(results)
                if (results.isEmpty()) {
                    Toast.makeText(requireContext(), "No results found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), e.message ?: "Search failed.", Toast.LENGTH_SHORT).show()
            } finally {
                progress.visibility = View.GONE
                btnSearch.isEnabled = true
            }
        }
    }

    private fun openFoodDetail(item: FoodItem) {
        val fragment = FoodDetailFragment().apply {
            arguments = Bundle().apply {
                putString("foodId", item.foodId)      // IMPORTANT for saving normalized food_items
                putString("foodName", item.name)
                putInt("calories", item.calories)
                putDouble("fat", item.fat)
                putDouble("carbs", item.carbs)
                putDouble("protein", item.protein)
                putString("defaultStorage", "PANTRY")

                // Optional: lets FoodDetail know where it came from (for debugging)
                putString("source", "TEXT_SEARCH")

            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}

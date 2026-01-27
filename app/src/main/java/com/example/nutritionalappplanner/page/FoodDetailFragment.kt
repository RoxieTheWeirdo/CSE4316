package com.example.nutritionalappplanner.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fitbite.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FoodDetailFragment : Fragment() {

    private val viewModel: FoodDetailViewModel by viewModels {
        FoodDetailViewModelFactory()
    }

    private lateinit var tvFoodName: TextView
    private lateinit var tvServing: TextView
    private lateinit var tvCalories: TextView
    private lateinit var tvFat: TextView
    private lateinit var tvCarbs: TextView
    private lateinit var tvProtein: TextView
    private lateinit var tvStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Make sure this layout file is named fragment_food_detail.xml
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

        val args = requireArguments()
        val name = args.getString("foodName") ?: "-"
        val calories = args.getInt("calories", 0)
        val fat = args.getDouble("fat", 0.0)
        val carbs = args.getDouble("carbs", 0.0)
        val protein = args.getDouble("protein", 0.0)

        tvStatus.text = ""
        tvFoodName.text = name
        tvServing.text = "Serving: 1"  // still placeholder
        tvCalories.text = "Calories: $calories kcal"
        tvFat.text = "Fat: $fat g"
        tvCarbs.text = "Carbs: $carbs g"
        tvProtein.text = "Protein: $protein g"
    }

}

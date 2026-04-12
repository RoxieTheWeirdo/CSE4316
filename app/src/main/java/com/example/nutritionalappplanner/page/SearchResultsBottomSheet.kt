package com.example.nutritionalappplanner.page

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.FoodItem
import com.example.fitbite.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SearchResultsBottomSheet(
    private val results: List<FoodItem>,
    private val onItemClick: (FoodItem) -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_search_results, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvSearchResults)

        val adapter = FoodListAdapter { item ->
            onItemClick(item)
            dismiss() // close after selecting
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        adapter.submitList(results)
    }
    override fun onStart() {
        super.onStart()

        val dialog = dialog as? com.google.android.material.bottomsheet.BottomSheetDialog
        val bottomSheet = dialog?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = (resources.displayMetrics.heightPixels * 0.75).toInt() // 75% screen
            it.layoutParams = layoutParams
        }
    }
}
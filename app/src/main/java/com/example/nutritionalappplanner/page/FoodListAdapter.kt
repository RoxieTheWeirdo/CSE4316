package com.example.nutritionalappplanner.page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.FoodItem
import com.example.fitbite.databinding.ItemFoodBinding

class FoodListAdapter(
    private val onItemClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodListAdapter.ViewHolder>() {

    private var items: List<FoodItem> = emptyList()

    fun submitList(newList: List<FoodItem>) {
        items = newList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemFoodBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.foodName.text = item.name
        holder.binding.foodCalories.text = "${item.calories} kcal"

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size
}


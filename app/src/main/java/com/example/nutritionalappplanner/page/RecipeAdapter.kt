package com.example.nutritionalappplanner.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R
import android.graphics.Color

class RecipeAdapter(
    private val onRecipeClick: (RecipeMatchUi) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.VH>() {

    private val items = mutableListOf<RecipeMatchUi>()

    fun submitList(list: List<RecipeMatchUi>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_recipe, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            onRecipeClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvRecipeTitle)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvRecipeMeta)

        fun bind(item: RecipeMatchUi) {
            tvTitle.text = item.title

            val percent = item.percentage

            val missingPreview = item.missing.take(3).joinToString(", ")
            val missingText = if (missingPreview.isBlank()) "None 🎉" else missingPreview

            // Set colored percentage
            val color = when {
                percent >= 70 -> Color.parseColor("#4CAF50") // Green
                percent >= 50 -> Color.parseColor("#FFC107") // Yellow
                else -> Color.parseColor("#F44336")          // Red
            }

            tvMeta.setTextColor(color)

            tvMeta.text = "$percent% Match • Missing: $missingText"
        }
    }
}

package com.example.nutritionalappplanner.page

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fitbite.R

data class PantryItemUi(
    val docId: String,
    val item: PantryItem
)

class PantryAdapter(
    private val onClick: (PantryItemUi) -> Unit,
    private val onLongClick: (PantryItemUi) -> Unit
) : RecyclerView.Adapter<PantryAdapter.VH>() {

    private val items = mutableListOf<PantryItemUi>()

    fun submitList(newItems: List<PantryItemUi>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_pantry, parent, false)
        return VH(v, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(
        itemView: View,
        private val onClick: (PantryItemUi) -> Unit,
        private val onLongClick: (PantryItemUi) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvItemAmount)
        private val tvStorage: TextView = itemView.findViewById(R.id.tvItemStorage)

        fun bind(row: PantryItemUi) {
            val item = row.item
            tvName.text = item.nameSnapshot.ifBlank { "(Unnamed item)" }
            tvAmount.text = "${item.quantity.toInt()} ${item.unit}"
            tvStorage.text = item.storage

            itemView.setOnClickListener { onClick(row) }

            // long press delete trigger
            itemView.setOnLongClickListener {
                onLongClick(row)
                true
            }
        }
    }
}

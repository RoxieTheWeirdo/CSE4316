package com.example.fitbite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private final List<Food> foods;
    private final OnFoodActionListener listener;

    public interface OnFoodActionListener {
        void onDelete(Food food);
        void onEdit(Food food);
    }

    public FoodAdapter(List<Food> foods, OnFoodActionListener listener) {
        this.foods = foods;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foods.get(position);

        holder.foodName.setText(food.getName());
        holder.foodCalories.setText(food.getCalories() + " cal");
        holder.foodMacros.setText(String.format("P: %sg  C: %sg  F: %sg",
                formatMacro(food.getProtein()),
                formatMacro(food.getCarbs()),
                formatMacro(food.getFat())));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(food);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onDelete(food);
            return true;
        });
    }

    private static String formatMacro(double value) {
        return value == (long) value
                ? String.valueOf((long) value)
                : String.format("%.1f", value);
    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView foodName;
        TextView foodCalories;
        TextView foodMacros;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.foodName);
            foodCalories = itemView.findViewById(R.id.foodCalories);
            foodMacros = itemView.findViewById(R.id.foodMacros);
        }
    }
}
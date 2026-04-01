package com.example.fitbite;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.ViewHolder> {

    private final List<FoodItem> foodList;
    private final Context context;

    public FoodItemAdapter(Context context, List<FoodItem> foodList) {
        this.context = context;
        this.foodList = foodList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_meal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem food = foodList.get(position);

        holder.foodName.setText(food.name);
        holder.foodCalories.setText(food.calories + " cal");
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, foodCalories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.meal_name);
            foodCalories = itemView.findViewById(R.id.meal_calories);
        }
    }
}
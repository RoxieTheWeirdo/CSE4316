package com.example.fitbite;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private Context context;
    private List<Meal> mealList;

    public MealAdapter(Context context, List<Meal> mealList) {
        this.context = context;
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_day, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);

        //  Day label
        holder.mealDay.setText("Day " + (position + 1));

        //  Show ALL meals
        holder.mealTime.setText("Breakfast / Lunch / Dinner");

        holder.mealName.setText(
                "B: " + meal.getBreakfast() +
                        "\nL: " + meal.getLunch() +
                        "\nD: " + meal.getDinner()
        );

        holder.mealCalories.setText("Calories: " + meal.getCalories());

        // Optional click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MealDetailActivity.class);
            intent.putExtra("mealName", meal.getBreakfast());
            intent.putExtra("mealCalories", meal.getCalories());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {

        TextView mealDay, mealTime, mealName, mealCalories;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);

            mealDay = itemView.findViewById(R.id.mealDay);
            mealTime = itemView.findViewById(R.id.mealTime);
            mealName = itemView.findViewById(R.id.mealName);
            mealCalories = itemView.findViewById(R.id.mealCalories);
        }
    }
}
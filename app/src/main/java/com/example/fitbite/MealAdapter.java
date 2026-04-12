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
    private List<Meal> mealList;
    private Context context;

    public MealAdapter(Context context, List<Meal> mealList) {
        this.context = context;
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.mealDay.setText(meal.getDay());
        holder.mealTime.setText(meal.getTime());
        holder.mealName.setText(meal.getName());
        holder.mealCalories.setText("Calories: " + meal.getCalories());

        //  open MealDetailActivity and pass data
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MealDetailActivity.class);
                intent.putExtra("mealDay", meal.getDay());
                intent.putExtra("mealTime", meal.getTime());
                intent.putExtra("mealName", meal.getName());
                intent.putExtra("mealCalories", meal.getCalories());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        TextView mealName, mealDay, mealTime, mealCalories;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            mealDay = itemView.findViewById(R.id.mealDay);
            mealTime = itemView.findViewById(R.id.mealTime);
            mealName = itemView.findViewById(R.id.mealName);
            mealCalories = itemView.findViewById(R.id.mealCalories);
        }
    }
    public void updateMeals(List<Meal> newMeals) {
        mealList.clear();
        mealList.addAll(newMeals);
        notifyDataSetChanged();
    }
}
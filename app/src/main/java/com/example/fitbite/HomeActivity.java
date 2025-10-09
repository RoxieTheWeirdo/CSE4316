package com.example.fitbite;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Calories Section
        TextView tvCaloriesRemaining = findViewById(R.id.tv_calories_remaining);
        TextView tvBaseGoal = findViewById(R.id.tv_base_goal);
        TextView tvFoodTotal = findViewById(R.id.tv_food_total);
        TextView tvExerciseTotal = findViewById(R.id.tv_exercise_total);

        // Food Log
        TextView tvMealName = findViewById(R.id.tv_meal_name);
        TextView tvMealCal = findViewById(R.id.tv_meal_cal);
        TextView tvMealTime = findViewById(R.id.tv_food_time);
        ImageView ivMealThumb = findViewById(R.id.iv_meal_thumb);

        // Steps and Exercise
        TextView tvStepsCount = findViewById(R.id.tv_steps_count);
        TextView tvStepsGoal = findViewById(R.id.tv_steps_goal);
        TextView tvExerciseCal = findViewById(R.id.tv_ex_cal);
        TextView tvExerciseTime = findViewById(R.id.tv_ex_time);

        // --- Example data ---
        int calorieGoal = 1900;
        int foodConsumed = 1225;
        int exerciseBurned = 200;
        int remaining = calorieGoal - foodConsumed + exerciseBurned;

        tvCaloriesRemaining.setText(String.valueOf(remaining));
        tvBaseGoal.setText("Goal: " + calorieGoal);
        tvFoodTotal.setText("Food: " + foodConsumed);
        tvExerciseTotal.setText("Exercise: " + exerciseBurned);

        tvMealName.setText("Grilled Chicken Bowl");
        tvMealCal.setText("540 cal");
        tvMealTime.setText("Today • 1:05 PM");
        ivMealThumb.setImageResource(R.drawable.meal_placeholder);

        tvStepsCount.setText("3,871");
        tvStepsGoal.setText("Goal: 10,000 steps");
        tvExerciseCal.setText("56 cal");
        tvExerciseTime.setText("00:00 hr");

        ivMealThumb.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Opening food log...", Toast.LENGTH_SHORT).show()
        );
    }
}

package com.example.fitbite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Notifications.createChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{ android.Manifest.permission.POST_NOTIFICATIONS },
                        100
                );
            }
        }
        Notifications.showNotification(this, 1, "A Notification!", "Test Notification", Notifications.MinimalNotifs);
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

        // --- Center + Button Popup Functionality ---
        MaterialCardView centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(this::showPopupMenu);

        //ADDED MEAL PLAN section 
        LinearLayout planSection = findViewById(R.id.plan_section);
        planSection.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MealPlanActivity.class);
            startActivity(intent);
        });

        LinearLayout diarySection = findViewById(R.id.diary_section);
        diarySection.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FoodDiaryActivity.class);
            startActivity(intent);
        });

        LinearLayout moreSection = findViewById(R.id.more_section);
        moreSection.setOnClickListener(v -> {
            Toast.makeText(this, "Diary clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, SettingsOverview.class);
            startActivity(intent);
        });
    }

    // Method to show popup menu
    private void showPopupMenu(View anchorView) {
        // Inflate popup layout
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_options, null);

        // Create popup window
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setElevation(10);

        // Set button actions
        popupView.findViewById(R.id.btnSearchFood).setOnClickListener(v -> {
            Toast.makeText(this, "Search Food clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, SearchFoodActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnBarcodeScan).setOnClickListener(v -> {
            Toast.makeText(this, "Barcode Scan clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, BarcodeScanner.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnMealScan).setOnClickListener(v -> {
            Toast.makeText(this, "Meal Scan clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, PantryScanner.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnWeight).setOnClickListener(v -> {
            Toast.makeText(this, "Weight clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, WeightActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        // Show popup centered on screen
        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 550);
    }
}

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
import com.google.android.material.progressindicator.CircularProgressIndicator;

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

        // ── Calories Section ──
        TextView tvCaloriesRemaining = findViewById(R.id.tv_calories_remaining);
        TextView tvBaseGoal          = findViewById(R.id.tv_base_goal);
        TextView tvFoodTotal         = findViewById(R.id.tv_food_total);
        TextView tvExerciseTotal     = findViewById(R.id.tv_exercise_total);

        // ── Food Log ──
        TextView tvMealName  = findViewById(R.id.tv_meal_name);
        TextView tvMealCal   = findViewById(R.id.tv_meal_cal);
        TextView tvMealTime  = findViewById(R.id.tv_food_time);
        ImageView ivMealThumb = findViewById(R.id.iv_meal_thumb);

        // ── Steps and Exercise ──
        TextView tvStepsCount  = findViewById(R.id.tv_steps_count);
        TextView tvStepsGoal   = findViewById(R.id.tv_steps_goal);
        TextView tvExerciseCal = findViewById(R.id.tv_ex_cal);
        TextView tvExerciseTime = findViewById(R.id.tv_ex_time);

        // ── Macro Widget ──
        CircularProgressIndicator progressProtein = findViewById(R.id.progress_protein);
        CircularProgressIndicator progressCarbs   = findViewById(R.id.progress_carbs);
        CircularProgressIndicator progressFat     = findViewById(R.id.progress_fat);
        TextView tvProteinRemaining = findViewById(R.id.tv_protein_remaining);
        TextView tvCarbsRemaining   = findViewById(R.id.tv_carbs_remaining);
        TextView tvFatRemaining     = findViewById(R.id.tv_fat_remaining);
        TextView tvProteinGoal      = findViewById(R.id.tv_protein_goal);
        TextView tvCarbsGoal        = findViewById(R.id.tv_carbs_goal);
        TextView tvFatGoal          = findViewById(R.id.tv_fat_goal);

        // ── Get all goals ──
        int calorieGoal = getIntent().getIntExtra("CALORIE_TARGET", 0);
        int proteinGoal = getIntent().getIntExtra("PROTEIN_GOAL",   0);
        int carbGoal    = getIntent().getIntExtra("CARB_GOAL",      0);
        int fatGoal     = getIntent().getIntExtra("FAT_GOAL",       0);

        int foodConsumed    = 1225;
        int exerciseBurned  = 200;
        int proteinConsumed = 0;
        int carbConsumed    = 0;
        int fatConsumed     = 0;

        // ── Calories ──
        int remaining = calorieGoal - foodConsumed + exerciseBurned;
        tvCaloriesRemaining.setText(String.valueOf(remaining));
        tvBaseGoal.setText("Goal: " + calorieGoal);
        tvFoodTotal.setText("Food: " + foodConsumed);
        tvExerciseTotal.setText("Exercise: " + exerciseBurned);

        // ── Food Log ──
        tvMealName.setText("Grilled Chicken Bowl");
        tvMealCal.setText("540 cal");
        tvMealTime.setText("Today • 1:05 PM");
        ivMealThumb.setImageResource(R.drawable.meal_placeholder);

        // ── Steps / Exercise ──
        tvStepsCount.setText("3,871");
        tvStepsGoal.setText("Goal: 10,000 steps");
        tvExerciseCal.setText("56 cal");
        tvExerciseTime.setText("00:00 hr");

        ivMealThumb.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Opening food log...", Toast.LENGTH_SHORT).show()
        );

        // ── Macro Widget ──
        int proteinLeft = Math.max(proteinGoal - proteinConsumed, 0);
        int carbsLeft   = Math.max(carbGoal    - carbConsumed,    0);
        int fatLeft     = Math.max(fatGoal     - fatConsumed,     0);

        tvProteinRemaining.setText(proteinLeft + "g");
        tvCarbsRemaining.setText(carbsLeft + "g");
        tvFatRemaining.setText(fatLeft + "g");

        tvProteinGoal.setText("Goal: " + proteinGoal + "g");
        tvCarbsGoal.setText("Goal: " + carbGoal + "g");
        tvFatGoal.setText("Goal: " + fatGoal + "g");

        progressProtein.setProgress(proteinGoal > 0 ? Math.min((proteinConsumed * 100) / proteinGoal, 100) : 0);
        progressCarbs.setProgress(carbGoal     > 0 ? Math.min((carbConsumed    * 100) / carbGoal,    100) : 0);
        progressFat.setProgress(fatGoal       > 0 ? Math.min((fatConsumed     * 100) / fatGoal,     100) : 0);

        // ── Center Button ──
        MaterialCardView centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(this::showPopupMenu);

        // ✅ FIXED PLAN BUTTON (ONLY CHANGE)
        LinearLayout planSection = findViewById(R.id.plan_section);
        planSection.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MealGeneratorActivity.class);
            startActivity(intent);
        });

        // ── Diary ──
        LinearLayout diarySection = findViewById(R.id.diary_section);
        diarySection.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FoodDiaryActivity.class);
            startActivity(intent);
        });

        // ── More ──
        LinearLayout moreSection = findViewById(R.id.more_section);
        moreSection.setOnClickListener(v -> {
            Toast.makeText(this, "Diary clicked", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, SettingsOverview.class);
            startActivity(intent);
        });
    }

    private void showPopupMenu(View anchorView) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_options, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setElevation(10);

        popupView.findViewById(R.id.btnSearchFood).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SearchFoodActivity.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnBarcodeScan).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, BarcodeScanner.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnMealScan).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, PantryScanner.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnWeight).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, WeightActivity.class));
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 550);
    }
}
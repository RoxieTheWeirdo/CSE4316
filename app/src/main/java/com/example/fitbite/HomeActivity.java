package com.example.fitbite;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.nutritionalappplanner.page.FoodDetailFragment;
import com.example.nutritionalappplanner.page.ScanResultFragment;
import com.google.android.material.card.MaterialCardView;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Notifications.createChannel(this);

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }

        // Optional: keep their test notification (remove later if you want)
        Notifications.showNotification(this, 1, "A Notification!", "Test Notification", Notifications.MinimalNotifs);


        setupHomeViews();
        setupCenterButton();
        setupSections();
        setupMoreSection();
        setupFragmentBackStackListener();
    }

    //Home screen views and sample data
    private void setupHomeViews() {
        TextView tvCaloriesRemaining = findViewById(R.id.tv_calories_remaining);
        TextView tvBaseGoal = findViewById(R.id.tv_base_goal);
        TextView tvFoodTotal = findViewById(R.id.tv_food_total);
        TextView tvExerciseTotal = findViewById(R.id.tv_exercise_total);

        TextView tvMealName = findViewById(R.id.tv_meal_name);
        TextView tvMealCal = findViewById(R.id.tv_meal_cal);
        TextView tvMealTime = findViewById(R.id.tv_food_time);
        ImageView ivMealThumb = findViewById(R.id.iv_meal_thumb);

        TextView tvStepsCount = findViewById(R.id.tv_steps_count);
        TextView tvStepsGoal = findViewById(R.id.tv_steps_goal);
        TextView tvExerciseCal = findViewById(R.id.tv_ex_cal);
        TextView tvExerciseTime = findViewById(R.id.tv_ex_time);

        // Example data
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

    //center + button popup
    private void setupCenterButton() {
        MaterialCardView centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(this::showPopupMenu);
    }

    //Setup plan & diary sections
    private void setupSections() {
        LinearLayout planSection = findViewById(R.id.plan_section);
        planSection.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MealPlanActivity.class))
        );

        LinearLayout diarySection = findViewById(R.id.diary_section);
        diarySection.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, FoodDiaryActivity.class))
        );
    }

    //Setup "More" section (Settings)
    private void setupMoreSection() {
        LinearLayout moreSection = findViewById(R.id.more_section);
        if (moreSection != null) {
            moreSection.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SettingsOverview.class);
                startActivity(intent);
            });
        }
    }

    //Show popup menu
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
            Toast.makeText(this, "Meal Scan clicked", Toast.LENGTH_SHORT).show();

            //fragment-based meal scan flow
            hideHomeViews();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ScanCameraFragment())
                    .addToBackStack(null)
                    .commit();

            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnWeight).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, WeightActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 550);
    }

    //Hide/show home views helpers
    private void hideHomeViews() {
        View contentRoot = findViewById(R.id.content_root);
        View scroll = findViewById(R.id.scroll);
        if (contentRoot != null) contentRoot.setVisibility(View.GONE);
        if (scroll != null) scroll.setVisibility(View.GONE);
    }

    private void showHomeViews() {
        View contentRoot = findViewById(R.id.content_root);
        View scroll = findViewById(R.id.scroll);
        if (contentRoot != null) contentRoot.setVisibility(View.VISIBLE);
        if (scroll != null) scroll.setVisibility(View.VISIBLE);
    }

    //Handle fragment back stack changes to restore/hide home views
    private void setupFragmentBackStackListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment topFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (topFragment instanceof ScanCameraFragment
                    || topFragment instanceof ScanResultFragment
                    || topFragment instanceof FoodDetailFragment) {
                hideHomeViews();
            } else {
                showHomeViews();
            }
        });
    }
}

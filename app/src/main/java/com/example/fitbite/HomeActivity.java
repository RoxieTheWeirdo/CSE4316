package com.example.fitbite;

import android.Manifest;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends AppCompatActivity {

    // Request codes
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 101;
    private static final int ACTIVITY_RECOGNITION_REQUEST_CODE = 102;
    private static final int POST_NOTIFICATIONS_REQUEST_CODE = 100;

    // Google Fit options (steps)
    private final FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    // Views
    private TextView tvCaloriesRemaining, tvBaseGoal, tvFoodTotal, tvExerciseTotal;
    private TextView tvMealName, tvMealCal, tvMealTime;
    private ImageView ivMealThumb;

    private TextView tvStepsCount, tvStepsGoal, tvExerciseCal, tvExerciseTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // -------------------- Notifications --------------------
        Notifications.createChannel(this);
        requestPostNotificationPermissionIfNeeded();

        Notifications.showNotification(
                this,
                1,
                "A Notification!",
                "Test Notification",
                Notifications.MinimalNotifs
        );

        // -------------------- UI Setup --------------------
        bindViews();
        populateDashboard();
        setupClickListeners();

        // -------------------- Steps via Google Fit --------------------
        // For Android 10+ you need ACTIVITY_RECOGNITION runtime permission for step sensors.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestActivityRecognitionPermissionIfNeeded();
        } else {
            // Pre-Android 10: no ACTIVITY_RECOGNITION runtime permission needed
            ensureGoogleFitPermissionsAndReadSteps();
        }
    }

    // -------------------- Bind Views --------------------

    private void bindViews() {
        // Calories Section
        tvCaloriesRemaining = findViewById(R.id.tv_calories_remaining);
        tvBaseGoal = findViewById(R.id.tv_base_goal);
        tvFoodTotal = findViewById(R.id.tv_food_total);
        tvExerciseTotal = findViewById(R.id.tv_exercise_total);

        // Food Log
        tvMealName = findViewById(R.id.tv_meal_name);
        tvMealCal = findViewById(R.id.tv_meal_cal);
        tvMealTime = findViewById(R.id.tv_food_time);
        ivMealThumb = findViewById(R.id.iv_meal_thumb);

        // Steps and Exercise
        tvStepsCount = findViewById(R.id.tv_steps_count);
        tvStepsGoal = findViewById(R.id.tv_steps_goal);
        tvExerciseCal = findViewById(R.id.tv_ex_cal);
        tvExerciseTime = findViewById(R.id.tv_ex_time);
    }

    // -------------------- Populate UI --------------------

    private void populateDashboard() {
        // Get calorie goal from account creation
        int calorieGoal = getIntent().getIntExtra("CALORIE_TARGET", 0);

        // Temporary example values
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

        // Steps placeholder until Fit returns
        tvStepsCount.setText("--");
        tvStepsGoal.setText("Goal: 10,000 steps");

        tvExerciseCal.setText("56 cal");
        tvExerciseTime.setText("00:00 hr");
    }

    // -------------------- Click Listeners --------------------

    private void setupClickListeners() {
        ivMealThumb.setOnClickListener(v ->
                Toast.makeText(HomeActivity.this, "Opening food log...", Toast.LENGTH_SHORT).show()
        );

        // Center + Button Popup
        MaterialCardView centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(this::showPopupMenu);

        // Meal Plan Section
        LinearLayout planSection = findViewById(R.id.plan_section);
        planSection.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, MealPlanActivity.class))
        );

        // Diary Section
        LinearLayout diarySection = findViewById(R.id.diary_section);
        diarySection.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, FoodDiaryActivity.class))
        );

        // Settings / More Section
        LinearLayout moreSection = findViewById(R.id.more_section);
        moreSection.setOnClickListener(v -> {
            Toast.makeText(this, "More clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, SettingsOverview.class));
        });
    }

    // -------------------- Permissions --------------------

    private void requestPostNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        POST_NOTIFICATIONS_REQUEST_CODE
                );
            }
        }
    }

    private void requestActivityRecognitionPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        ACTIVITY_RECOGNITION_REQUEST_CODE
                );
            } else {
                // Already granted
                ensureGoogleFitPermissionsAndReadSteps();
            }
        }
    }

    private void ensureGoogleFitPermissionsAndReadSteps() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    account,
                    fitnessOptions
            );
        } else {
            readTodaySteps();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

            if (GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                Toast.makeText(this, "Fit permission granted", Toast.LENGTH_SHORT).show();
                readTodaySteps();
            } else {
                Toast.makeText(this, "Fit permission NOT granted", Toast.LENGTH_SHORT).show();
                tvStepsCount.setText("--");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now we can proceed to Fit consent + read steps
                ensureGoogleFitPermissionsAndReadSteps();
            } else {
                Toast.makeText(this, "Activity Recognition permission denied", Toast.LENGTH_SHORT).show();
                tvStepsCount.setText("--");
            }
        }
    }

    // -------------------- Google Fit Steps --------------------

    private void readTodaySteps() {
        GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        if (account == null) {
            tvStepsCount.setText("--");
            Toast.makeText(this, "No Google account available for Fit", Toast.LENGTH_SHORT).show();
            return;
        }

        long startTime = getStartOfTodayMillis();
        long endTime = System.currentTimeMillis();

        DataReadRequest request = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, account)
                .readData(request)
                .addOnSuccessListener(response -> {
                    long totalSteps = 0;

                    if (!response.getBuckets().isEmpty()) {
                        for (int b = 0; b < response.getBuckets().size(); b++) {
                            for (int ds = 0; ds < response.getBuckets().get(b).getDataSets().size(); ds++) {
                                for (int dp = 0; dp < response.getBuckets().get(b).getDataSets().get(ds).getDataPoints().size(); dp++) {
                                    totalSteps += response.getBuckets().get(b)
                                            .getDataSets().get(ds)
                                            .getDataPoints().get(dp)
                                            .getValue(Field.FIELD_STEPS).asInt();
                                }
                            }
                        }
                    } else {
                        for (int ds = 0; ds < response.getDataSets().size(); ds++) {
                            for (int dp = 0; dp < response.getDataSets().get(ds).getDataPoints().size(); dp++) {
                                totalSteps += response.getDataSets().get(ds)
                                        .getDataPoints().get(dp)
                                        .getValue(Field.FIELD_STEPS).asInt();
                            }
                        }
                    }

                    tvStepsCount.setText(String.valueOf(totalSteps));
                })
                .addOnFailureListener(e -> {
                    tvStepsCount.setText("--");
                    Toast.makeText(this, "Failed to read steps: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private long getStartOfTodayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    // -------------------- Popup Menu --------------------

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
            Toast.makeText(this, "Search Food clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, SearchFoodActivity.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnBarcodeScan).setOnClickListener(v -> {
            Toast.makeText(this, "Barcode Scan clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, BarcodeScanner.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnMealScan).setOnClickListener(v -> {
            Toast.makeText(this, "Meal Scan clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, PantryScanner.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.btnWeight).setOnClickListener(v -> {
            Toast.makeText(this, "Weight clicked", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, WeightActivity.class));
            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(anchorView, Gravity.CENTER, 0, 550);
    }
}
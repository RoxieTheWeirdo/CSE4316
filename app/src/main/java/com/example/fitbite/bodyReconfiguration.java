package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class bodyReconfiguration extends AppCompatActivity {

    private CardView cardLoseWeight, cardGainMuscle, cardMaintainWeight;
    private RadioButton radioLoseWeight, radioGainMuscle, radioMaintainWeight;
    private Button btnNext;

    private String selectedGoal = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bodyreconfiguration);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Views
        cardLoseWeight = findViewById(R.id.cardLoseWeight);
        cardGainMuscle = findViewById(R.id.cardGainMuscle);
        cardMaintainWeight = findViewById(R.id.cardMaintainWeight);

        radioLoseWeight = findViewById(R.id.radioLoseWeight);
        radioGainMuscle = findViewById(R.id.radioGainMuscle);
        radioMaintainWeight = findViewById(R.id.radioMaintainWeight);

        btnNext = findViewById(R.id.btnNext);
        btnNext.setEnabled(false);
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Card click listeners
        cardLoseWeight.setOnClickListener(v -> selectGoal("LOSE"));
        cardGainMuscle.setOnClickListener(v -> selectGoal("GAIN"));
        cardMaintainWeight.setOnClickListener(v -> selectGoal("MAINTAIN"));

        // Finish button click
        btnNext.setOnClickListener(v -> saveGoalAndProceed());
    }

    private void selectGoal(String goal) {
        // Uncheck all first
        radioLoseWeight.setChecked(false);
        radioGainMuscle.setChecked(false);
        radioMaintainWeight.setChecked(false);

        // Check selected
        switch (goal) {
            case "LOSE":
                radioLoseWeight.setChecked(true);
                break;
            case "GAIN":
                radioGainMuscle.setChecked(true);
                break;
            case "MAINTAIN":
                radioMaintainWeight.setChecked(true);
                break;
        }

        selectedGoal = goal;
        enableNextButton();
    }

    private void enableNextButton() {
        btnNext.setEnabled(true);
        btnNext.setBackgroundResource(R.drawable.button_enabled_background);
        btnNext.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void saveGoalAndProceed() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGoal.isEmpty()) {
            Toast.makeText(this, "Please select a goal", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        Map<String, Object> userData = new HashMap<>();
        userData.put("goalType", selectedGoal);
        userData.put("initialized", true);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Calculate calories and go to HomeActivity
                    db.collection("users").document(userId).get()
                            .addOnSuccessListener(document -> {
                                Double weightInPounds = document.getDouble("weightInPounds");
                                Double heightCm = document.getDouble("heightInCm");
                                Long age = document.getLong("age");
                                String sex = document.getString("sex");
                                String exercise = document.getString("exerciseLevel");

                                if (weightInPounds == null || heightCm == null || age == null
                                        || sex == null || exercise == null) {
                                    Toast.makeText(this, "Please complete your profile first", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                double weightKg = weightInPounds / 2.205;
                                double bmr = calculateBMR(weightKg, heightCm, age, sex);
                                double tdee = bmr * getActivityFactor(exercise);
                                int finalCalories = adjustForGoal((int) Math.round(tdee), selectedGoal);

                                Intent intent = new Intent(bodyReconfiguration.this, HomeActivity.class);
                                intent.putExtra("CALORIE_TARGET", finalCalories);
                                intent.putExtra("GOAL_TYPE", selectedGoal);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show());
    }

    // BMR calculation
    private double calculateBMR(double weightKg, double heightCm, long age, String sex) {
        if ("male".equalsIgnoreCase(sex)) {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }
    }

    private double getActivityFactor(String exercise) {
        switch (exercise) {
            case "0 sessions/week": return 1.2;
            case "1-3 sessions/week": return 1.375;
            case "4-6 sessions/week": return 1.55;
            case "7+ sessions/week": return 1.725;
            default: return 1.2;
        }
    }

    private int adjustForGoal(int calories, String goal) {
        switch (goal) {
            case "LOSE": return calories - 500;
            case "GAIN": return calories + 500;
            default: return calories;
        }
    }
}
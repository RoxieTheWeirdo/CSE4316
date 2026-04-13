package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class bodyReconfiguration extends AppCompatActivity {

    private CardView loseWeightCard, gainMuscleCard, maintainWeightCard;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bodyreconfiguration);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loseWeightCard = findViewById(R.id.loseWeightCard);
        gainMuscleCard = findViewById(R.id.gainMuscleCard);
        maintainWeightCard = findViewById(R.id.maintainWeightCard);

        loseWeightCard.setOnClickListener(v -> calculateAndGoHome("LOSE"));
        gainMuscleCard.setOnClickListener(v -> calculateAndGoHome("GAIN"));
        maintainWeightCard.setOnClickListener(v -> calculateAndGoHome("MAINTAIN"));
    }

    private void calculateAndGoHome(String goal) {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(this, "User profile not found", Toast.LENGTH_SHORT).show();
                        return;
                    }


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
                    int finalCalories = adjustForGoal((int) Math.round(tdee), goal);

                    Intent intent = new Intent(bodyReconfiguration.this, HomeActivity.class);
                    intent.putExtra("CALORIE_TARGET", finalCalories);
                    intent.putExtra("GOAL_TYPE", goal);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                );
    }

    // 🔹 STEP 1 — BMR calculation
    private double calculateBMR(double weightKg, double heightCm, long age, String sex) {
        if ("male".equalsIgnoreCase(sex)) {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }
    }

    // 🔹 STEP 2 — Activity multiplier
    private double getActivityFactor(String exercise) {
        switch (exercise) {
            case "0 sessions/week":
                return 1.2;
            case "1-3 sessions/week":
                return 1.375;
            case "4-6 sessions/week":
                return 1.55;
            case "7+ sessions/week":
                return 1.725;
            default:
                return 1.2;
        }
    }

    // 🔹 STEP 3 — Goal adjustment
    private int adjustForGoal(int calories, String goal) {
        switch (goal) {
            case "LOSE":
                return calories - 500;
            case "GAIN":
                return calories +500;
            default:
                return calories;
        }
    }
}

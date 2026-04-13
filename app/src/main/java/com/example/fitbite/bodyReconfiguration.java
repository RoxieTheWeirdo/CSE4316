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

                    // STEP 1 — Calories
                    double bmr = calculateBMR(weightKg, heightCm, age, sex);
                    double tdee = bmr * getActivityFactor(exercise);
                    int finalCalories = adjustForGoal((int) Math.round(tdee), goal, weightInPounds);

                    // STEP 2 — Protein (0.8g per lb of bodyweight)
                    int proteinGoal = (int) Math.round(weightInPounds * 0.8);

                    // STEP 3 — Carbs (% of calories by goal, 4 cal per gram)
                    double carbPercent;
                    switch (goal) {
                        case "LOSE":    carbPercent = 0.40; break;
                        case "GAIN":    carbPercent = 0.50; break;
                        default:        carbPercent = 0.45; break; // MAINTAIN
                    }
                    int carbGoal = (int) Math.round((finalCalories * carbPercent) / 4);

                    // STEP 4 — Fat (remaining calories after protein + carbs, 9 cal per gram)
                    int fatGoal = (int) Math.round(
                            (finalCalories - (proteinGoal * 4) - (carbGoal * 4)) / 9.0
                    );

                    // STEP 5 — Fiber (14g per 1,000 calories)
                    int fiberGoal = (int) Math.round((finalCalories / 1000.0) * 14);

                    // STEP 6 — Sodium (2,300mg flat daily limit)
                    int sodiumGoal = 2300;

                    // Pass everything to HomeActivity
                    Intent intent = new Intent(bodyReconfiguration.this, HomeActivity.class);
                    intent.putExtra("CALORIE_TARGET", finalCalories);
                    intent.putExtra("GOAL_TYPE", goal);
                    intent.putExtra("PROTEIN_GOAL", proteinGoal);
                    intent.putExtra("CARB_GOAL", carbGoal);
                    intent.putExtra("FAT_GOAL", fatGoal);
                    intent.putExtra("FIBER_GOAL", fiberGoal);
                    intent.putExtra("SODIUM_GOAL", sodiumGoal);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load profile data", Toast.LENGTH_SHORT).show()
                );
    }

    // STEP 1 — BMR calculation
    private double calculateBMR(double weightKg, double heightCm, long age, String sex) {
        if ("male".equalsIgnoreCase(sex)) {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }
    }

    // STEP 2 — Activity multiplier
    private double getActivityFactor(String exercise) {
        switch (exercise) {
            case "0 sessions/week":   return 1.2;
            case "1-3 sessions/week": return 1.375;
            case "4-6 sessions/week": return 1.55;
            case "7+ sessions/week":  return 1.725;
            default:                  return 1.2;
        }
    }

    // STEP 3 — Goal adjustment
    private int adjustForGoal(int calories, String goal, Double weightInPounds) {
        switch (goal) {
            case "LOSE": return calories - 500;
            case "GAIN": return calories + 500;
            default:     return calories;
        }
    }
}
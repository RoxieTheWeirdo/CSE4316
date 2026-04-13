package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class activity_basics_exercise extends AppCompatActivity {

    private static final String TAG = "BasicsExerciseActivity";
    private CardView card0Sessions, card1to3Sessions, card4to6Sessions, card7PlusSessions;
    private RadioButton radio0Sessions, radio1to3Sessions, radio4to6Sessions, radio7PlusSessions;
    private Button btnNext;
    private String selectedExercise = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_exercise);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        card0Sessions = findViewById(R.id.card0Sessions);
        card1to3Sessions = findViewById(R.id.card1to3Sessions);
        card4to6Sessions = findViewById(R.id.card4to6Sessions);
        card7PlusSessions = findViewById(R.id.card7PlusSessions);

        radio0Sessions = findViewById(R.id.radio0Sessions);
        radio1to3Sessions = findViewById(R.id.radio1to3Sessions);
        radio4to6Sessions = findViewById(R.id.radio4to6Sessions);
        radio7PlusSessions = findViewById(R.id.radio7PlusSessions);

        btnNext = findViewById(R.id.btnNext);

        // 0 sessions card
        card0Sessions.setOnClickListener(v -> selectExercise(0));

        // 1-3 sessions card
        card1to3Sessions.setOnClickListener(v -> selectExercise(1));

        // 4-6 sessions card
        card4to6Sessions.setOnClickListener(v -> selectExercise(2));

        // 7+ sessions card
        card7PlusSessions.setOnClickListener(v -> selectExercise(3));

        // Next button
        btnNext.setOnClickListener(v -> saveExerciseAndProceed());
    }

    private void saveExerciseAndProceed() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedExercise.isEmpty()) {
            Toast.makeText(this, "Please select an exercise level.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("exerciseLevel", selectedExercise);
        userData.put("initialized", true);
        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Exercise level successfully written!");
                    // Proceed to the correct HomeActivity
                    Intent intent = new Intent(activity_basics_exercise.this, bodyReconfiguration.class);
                    startActivity(intent);
                    finish(); // Close the onboarding flow

                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing exercise level", e);
                    Toast.makeText(activity_basics_exercise.this, "Failed to save exercise level.", Toast.LENGTH_SHORT).show();
                });
    }

    private void selectExercise(int option) {
        // Uncheck all
        radio0Sessions.setChecked(false);
        radio1to3Sessions.setChecked(false);
        radio4to6Sessions.setChecked(false);
        radio7PlusSessions.setChecked(false);

        // Check selected
        switch(option) {
            case 0:
                radio0Sessions.setChecked(true);
                selectedExercise = "0 sessions/week";
                break;
            case 1:
                radio1to3Sessions.setChecked(true);
                selectedExercise = "1-3 sessions/week";
                break;
            case 2:
                radio4to6Sessions.setChecked(true);
                selectedExercise = "4-6 sessions/week";
                break;
            case 3:
                radio7PlusSessions.setChecked(true);
                selectedExercise = "7+ sessions/week";
                break;
        }

        enableNextButton();
    }

    private void enableNextButton() {
        btnNext.setEnabled(true);
        btnNext.setBackgroundResource(R.drawable.button_enabled_background);
        btnNext.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }
}

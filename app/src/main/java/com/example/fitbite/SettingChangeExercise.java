package com.example.fitbite;

import android.os.Bundle;
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

public class SettingChangeExercise extends AppCompatActivity {

    private CardView card0Sessions, card1to3Sessions, card4to6Sessions, card7PlusSessions;
    private RadioButton radio0Sessions, radio1to3Sessions, radio4to6Sessions, radio7PlusSessions;
    private Button btnSave;
    private String selectedExercise = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_changeexercise);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Views
        card0Sessions = findViewById(R.id.card0Sessions);
        card1to3Sessions = findViewById(R.id.card1to3Sessions);
        card4to6Sessions = findViewById(R.id.card4to6Sessions);
        card7PlusSessions = findViewById(R.id.card7PlusSessions);

        radio0Sessions = findViewById(R.id.radio0Sessions);
        radio1to3Sessions = findViewById(R.id.radio1to3Sessions);
        radio4to6Sessions = findViewById(R.id.radio4to6Sessions);
        radio7PlusSessions = findViewById(R.id.radio7PlusSessions);

        btnSave = findViewById(R.id.btnNext);

        // Card click listeners
        card0Sessions.setOnClickListener(v -> selectExercise(0));
        card1to3Sessions.setOnClickListener(v -> selectExercise(1));
        card4to6Sessions.setOnClickListener(v -> selectExercise(2));
        card7PlusSessions.setOnClickListener(v -> selectExercise(3));

        btnSave.setOnClickListener(v -> saveExercise());
    }

    private void selectExercise(int option) {
        // Uncheck all
        radio0Sessions.setChecked(false);
        radio1to3Sessions.setChecked(false);
        radio4to6Sessions.setChecked(false);
        radio7PlusSessions.setChecked(false);

        switch (option) {
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

        enableSaveButton();
    }

    private void enableSaveButton() {
        btnSave.setEnabled(true);
        btnSave.setBackgroundResource(R.drawable.button_enabled_background);
        btnSave.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void saveExercise() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedExercise.isEmpty()) {
            Toast.makeText(this, "Please select an exercise level.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("exerciseLevel", selectedExercise);

        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Exercise level updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to SettingEditPersonal
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save exercise level.", Toast.LENGTH_SHORT).show();
                });
    }
}

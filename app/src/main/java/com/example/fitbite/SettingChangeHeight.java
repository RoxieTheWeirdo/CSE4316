package com.example.fitbite;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SettingChangeHeight extends AppCompatActivity {

    private static final String TAG = "SettingChangeHeight";

    private Button btnFeetInches, btnCentimeters, btnSave;
    private NumberPicker heightPicker;
    private boolean isFeetInches = true;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_height);

        btnFeetInches = findViewById(R.id.btnFeetInches);
        btnCentimeters = findViewById(R.id.btnCentimeters);
        heightPicker = findViewById(R.id.heightPicker);
        btnSave = findViewById(R.id.btnNext); // Reuse Next button as Save

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupFeetInches(); // default view

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnFeetInches.setOnClickListener(v -> {
            if (!isFeetInches) {
                isFeetInches = true;
                setupFeetInches();
            }
        });

        btnCentimeters.setOnClickListener(v -> {
            if (isFeetInches) {
                isFeetInches = false;
                setupCentimeters();
            }
        });

        btnSave.setOnClickListener(v -> saveHeight());
    }

    private void saveHeight() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        double heightInCm;
        if (isFeetInches) {
            String selectedHeight = heightPicker.getDisplayedValues()[heightPicker.getValue()];
            String[] parts = selectedHeight.replace(" ft", "").replace(" in", "").split(" ");
            int feet = Integer.parseInt(parts[0]);
            int inches = Integer.parseInt(parts[1]);
            heightInCm = (feet * 12 + inches) * 2.54;
        } else {
            heightInCm = 100 + heightPicker.getValue();
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("heightInCm", heightInCm); // optional numeric value

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Height updated!", Toast.LENGTH_SHORT).show();
                    finish(); // close and return to settings
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error saving height", e);
                    Toast.makeText(this, "Failed to save height.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupFeetInches() {
        int count = 0;
        for (int feet = 4; feet <= 7; feet++) {
            for (int inches = 0; inches < 12; inches++) {
                if (feet == 7 && inches > 11) break;
                count++;
            }
        }

        String[] heights = new String[count];
        int index = 0;
        for (int feet = 4; feet <= 7; feet++) {
            for (int inches = 0; inches < 12; inches++) {
                if (feet == 7 && inches > 11) break;
                heights[index++] = feet + " ft " + inches + " in";
            }
        }

        heightPicker.setMinValue(0);
        heightPicker.setMaxValue(count - 1);
        heightPicker.setDisplayedValues(heights);
        heightPicker.setValue(19); // default ~5 ft 7 in
    }

    private void setupCentimeters() {
        String[] heights = new String[151]; // 100 cm to 250 cm
        for (int i = 0; i < 151; i++) heights[i] = (100 + i) + " cm";

        heightPicker.setMinValue(0);
        heightPicker.setMaxValue(150);
        heightPicker.setDisplayedValues(heights);
        heightPicker.setValue(70); // default ~170 cm
    }
}

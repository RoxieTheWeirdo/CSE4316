package com.example.fitbite;

import android.content.Intent;
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

public class activity_basics_height extends AppCompatActivity {

    private static final String TAG = "BasicsHeightActivity";
    private Button btnFeetInches, btnCentimeters, btnNext;
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
        btnNext = findViewById(R.id.btnNext);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupFeetInches(); // default view

        // ✅ Back
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

        btnNext.setOnClickListener(v -> saveHeightAndProceed());
    }

    private void saveHeightAndProceed() {
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
            double totalInches = (feet * 12) + inches;
            heightInCm = totalInches * 2.54;
        } else {
            heightInCm = 100 + heightPicker.getValue();
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("heightInCm", heightInCm);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Height successful");
                    Intent intent = new Intent(activity_basics_height.this, activity_basics_weight.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing height", e);
                    Toast.makeText(activity_basics_height.this, "Failed to save height.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupFeetInches() {
        // Count total entries first
        int count = 0;
        for (int feet = 4; feet <= 7; feet++) {
            for (int inches = 0; inches < 12; inches++) {
                if (feet == 7 && inches > 11) break;
                count++;
            }
        }

        // Create array with exact size
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
        heightPicker.setValue(19);
    }

    private void setupCentimeters() {
        String[] heights = new String[151];
        for (int i = 0; i < 151; i++) heights[i] = (100 + i) + " cm";
        heightPicker.setMinValue(0);
        heightPicker.setMaxValue(150);
        heightPicker.setDisplayedValues(heights);
        heightPicker.setValue(70);
    }
}

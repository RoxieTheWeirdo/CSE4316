package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class activity_basics_weight extends AppCompatActivity {

    private static final String TAG = "BasicsWeightActivity";
    private Button btnPounds, btnKilograms, btnNext;
    private SeekBar weightSeekBar;
    private TextView tvWeightValue;
    private boolean isPounds = true;
    private int currentWeight = 183;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_weight);

        btnPounds = findViewById(R.id.btnPounds);
        btnKilograms = findViewById(R.id.btnKilograms);
        weightSeekBar = findViewById(R.id.weightSeekBar);
        tvWeightValue = findViewById(R.id.tvWeightValue);
        btnNext = findViewById(R.id.btnNext);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupPounds();

        // ✅ Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnPounds.setOnClickListener(v -> {
            if (!isPounds) {
                isPounds = true;
                currentWeight = (int) (currentWeight * 2.205);
                setupPounds();
            }
        });

        btnKilograms.setOnClickListener(v -> {
            if (isPounds) {
                isPounds = false;
                currentWeight = (int) (currentWeight / 2.205);
                setupKilograms();
            }
        });

        weightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentWeight = progress;
                updateWeightDisplay();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        btnNext.setOnClickListener(v -> saveWeightAndProceed());
    }

    private void saveWeightAndProceed() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        double weightInPounds = isPounds ? currentWeight : currentWeight * 2.205;

        Map<String, Object> userData = new HashMap<>();
        userData.put("weightInPounds", weightInPounds);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Weight successfully written!");
                    // Proceed to the next activity
                    Intent intent = new Intent(activity_basics_weight.this, activity_basics_exercise.class);
                    intent.putExtra("weight", currentWeight);
                    intent.putExtra("isPounds", isPounds);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing weight", e);
                    Toast.makeText(activity_basics_weight.this, "Failed to save weight.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupPounds() {
        weightSeekBar.setMax(400);
        weightSeekBar.setProgress(currentWeight);
        updateWeightDisplay();
    }

    private void setupKilograms() {
        weightSeekBar.setMax(200);
        weightSeekBar.setProgress(currentWeight);
        updateWeightDisplay();
    }

    private void updateWeightDisplay() {
        tvWeightValue.setText(currentWeight + (isPounds ? " lbs" : " kg"));
    }
}

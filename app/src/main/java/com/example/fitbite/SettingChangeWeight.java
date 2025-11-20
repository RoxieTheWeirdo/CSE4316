package com.example.fitbite;

import android.os.Bundle;
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

public class SettingChangeWeight extends AppCompatActivity {

    private Button btnPounds, btnKilograms, btnSave;
    private SeekBar weightSeekBar;
    private TextView tvWeightValue;
    private boolean isPounds = true;
    private int currentWeight = 183;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_changeweight);

        btnPounds = findViewById(R.id.btnPounds);
        btnKilograms = findViewById(R.id.btnKilograms);
        weightSeekBar = findViewById(R.id.weightSeekBar);
        tvWeightValue = findViewById(R.id.tvWeightValue);
        btnSave = findViewById(R.id.btnNext);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupPounds();

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
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        btnSave.setOnClickListener(v -> saveWeight());
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

    private void saveWeight() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }

        double weightInPounds = isPounds ? currentWeight : currentWeight * 2.205;

        Map<String, Object> data = new HashMap<>();
        data.put("weightInPounds", weightInPounds);

        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Weight updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to SettingEditPersonal
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save weight.", Toast.LENGTH_SHORT).show();
                });
    }
}

package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class activity_basics_weight extends AppCompatActivity {

    private Button btnPounds, btnKilograms, btnNext;
    private SeekBar weightSeekBar;
    private TextView tvWeightValue;
    private boolean isPounds = true;
    private int currentWeight = 183;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_weight);

        btnPounds = findViewById(R.id.btnPounds);
        btnKilograms = findViewById(R.id.btnKilograms);
        weightSeekBar = findViewById(R.id.weightSeekBar);
        tvWeightValue = findViewById(R.id.tvWeightValue);
        btnNext = findViewById(R.id.btnNext);

        setupPounds();

        // ✅ Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnPounds.setOnClickListener(v -> {
            isPounds = true;
            currentWeight = (int)(currentWeight * 2.205);
            setupPounds();
        });

        btnKilograms.setOnClickListener(v -> {
            isPounds = false;
            currentWeight = (int)(currentWeight / 2.205);
            setupKilograms();
        });

        weightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentWeight = progress;
                updateWeightDisplay();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(activity_basics_weight.this, activity_basics_exercise.class);
            intent.putExtra("weight", currentWeight);
            intent.putExtra("isPounds", isPounds);
            startActivity(intent);
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

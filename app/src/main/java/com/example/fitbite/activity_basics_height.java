package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;
import androidx.appcompat.app.AppCompatActivity;

public class activity_basics_height extends AppCompatActivity {

    private Button btnFeetInches, btnCentimeters, btnNext;
    private NumberPicker heightPicker;
    private boolean isFeetInches = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_height);

        btnFeetInches = findViewById(R.id.btnFeetInches);
        btnCentimeters = findViewById(R.id.btnCentimeters);
        heightPicker = findViewById(R.id.heightPicker);
        btnNext = findViewById(R.id.btnNext);

        setupFeetInches(); // default view

        // ✅ Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnFeetInches.setOnClickListener(v -> {
            isFeetInches = true;
            setupFeetInches();
        });

        btnCentimeters.setOnClickListener(v -> {
            isFeetInches = false;
            setupCentimeters();
        });

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(activity_basics_height.this, activity_basics_weight.class);
            intent.putExtra("height", heightPicker.getValue());
            intent.putExtra("isFeetInches", isFeetInches);
            startActivity(intent);
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
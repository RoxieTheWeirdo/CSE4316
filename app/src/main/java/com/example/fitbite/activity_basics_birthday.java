package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

public class activity_basics_birthday extends AppCompatActivity {

    private NumberPicker monthPicker, dayPicker, yearPicker;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_birthday);

        monthPicker = findViewById(R.id.monthPicker);
        dayPicker = findViewById(R.id.dayPicker);
        yearPicker = findViewById(R.id.yearPicker);
        btnNext = findViewById(R.id.btnNext);

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(5);

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setValue(15);

        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(2010);
        yearPicker.setValue(1995);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(activity_basics_birthday.this, activity_basics_height.class);
            startActivity(intent);
        });
    }
}
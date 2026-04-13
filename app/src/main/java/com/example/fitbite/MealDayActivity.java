package com.example.fitbite;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.List;

public class MealDayActivity extends AppCompatActivity {

    private TextView tvMonBreakfast, tvMonLunch, tvMonDinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.item_meal_day);


        tvMonBreakfast = findViewById(R.id.tvMonBreakfast);
        tvMonLunch = findViewById(R.id.tvMonLunch);
        tvMonDinner = findViewById(R.id.tvMonDinner);

        // GET DATA
        Serializable data = getIntent().getSerializableExtra("mealList");

        if (data instanceof List<?>) {
            List<Meal> mealList = (List<Meal>) data;

            if (!mealList.isEmpty()) {
                Meal monday = mealList.get(0);

                tvMonBreakfast.setText(monday.getBreakfast());
                tvMonLunch.setText(monday.getLunch());
                tvMonDinner.setText(monday.getDinner());
            }
        }
    }
}
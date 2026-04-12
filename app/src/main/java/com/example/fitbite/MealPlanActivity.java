package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity {

    private List<Meal> mealList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_meal_day);

        // 🔥 GET DATA FROM PREVIOUS SCREEN
        Serializable data = getIntent().getSerializableExtra("mealList");

        if (data != null && data instanceof List<?>) {
            mealList = (List<Meal>) data;
        } else {
            mealList = new ArrayList<>();
        }

        // ===== MONDAY =====
        TextView tvMonBreakfast = findViewById(R.id.tvMonBreakfast);
        TextView tvMonLunch = findViewById(R.id.tvMonLunch);
        TextView tvMonDinner = findViewById(R.id.tvMonDinner);

        // ===== TUESDAY =====
        TextView tvTueBreakfast = findViewById(R.id.tvTueBreakfast);
        TextView tvTueLunch = findViewById(R.id.tvTueLunch);
        TextView tvTueDinner = findViewById(R.id.tvTueDinner);

        // ===== WEDNESDAY =====
        TextView tvWedBreakfast = findViewById(R.id.tvWedBreakfast);
        TextView tvWedLunch = findViewById(R.id.tvWedLunch);
        TextView tvWedDinner = findViewById(R.id.tvWedDinner);

        // ===== THURSDAY =====
        TextView tvThuBreakfast = findViewById(R.id.tvThuBreakfast);
        TextView tvThuLunch = findViewById(R.id.tvThuLunch);
        TextView tvThuDinner = findViewById(R.id.tvThuDinner);

        // ===== FRIDAY =====
        TextView tvFriBreakfast = findViewById(R.id.tvFriBreakfast);
        TextView tvFriLunch = findViewById(R.id.tvFriLunch);
        TextView tvFriDinner = findViewById(R.id.tvFriDinner);

        // ===== SATURDAY =====
        TextView tvSatBreakfast = findViewById(R.id.tvSatBreakfast);
        TextView tvSatLunch = findViewById(R.id.tvSatLunch);
        TextView tvSatDinner = findViewById(R.id.tvSatDinner);

        // ===== SUNDAY =====
        TextView tvSunBreakfast = findViewById(R.id.tvSunBreakfast);
        TextView tvSunLunch = findViewById(R.id.tvSunLunch);
        TextView tvSunDinner = findViewById(R.id.tvSunDinner);

        // 🔥 SET DATA INTO UI
        if (mealList.size() >= 7) {

            // Monday
            tvMonBreakfast.setText(mealList.get(0).getBreakfast());
            tvMonLunch.setText(mealList.get(0).getLunch());
            tvMonDinner.setText(mealList.get(0).getDinner());

            // Tuesday
            tvTueBreakfast.setText(mealList.get(1).getBreakfast());
            tvTueLunch.setText(mealList.get(1).getLunch());
            tvTueDinner.setText(mealList.get(1).getDinner());

            // Wednesday
            tvWedBreakfast.setText(mealList.get(2).getBreakfast());
            tvWedLunch.setText(mealList.get(2).getLunch());
            tvWedDinner.setText(mealList.get(2).getDinner());

            // Thursday
            tvThuBreakfast.setText(mealList.get(3).getBreakfast());
            tvThuLunch.setText(mealList.get(3).getLunch());
            tvThuDinner.setText(mealList.get(3).getDinner());

            // Friday
            tvFriBreakfast.setText(mealList.get(4).getBreakfast());
            tvFriLunch.setText(mealList.get(4).getLunch());
            tvFriDinner.setText(mealList.get(4).getDinner());

            // Saturday
            tvSatBreakfast.setText(mealList.get(5).getBreakfast());
            tvSatLunch.setText(mealList.get(5).getLunch());
            tvSatDinner.setText(mealList.get(5).getDinner());

            // Sunday
            tvSunBreakfast.setText(mealList.get(6).getBreakfast());
            tvSunLunch.setText(mealList.get(6).getLunch());
            tvSunDinner.setText(mealList.get(6).getDinner());
        }

        // 🔁 REGENERATE BUTTON
        Button btnRegenerate = findViewById(R.id.btnRegenerate);
        btnRegenerate.setOnClickListener(v -> {
            Intent intent = new Intent(MealPlanActivity.this, AutoMealPlan.class);
            startActivity(intent);
        });
    }
}
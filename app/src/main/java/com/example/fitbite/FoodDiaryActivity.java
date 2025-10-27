package com.example.fitbite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class FoodDiaryActivity extends AppCompatActivity {

    private RecyclerView diaryRecyclerView;
    private FloatingActionButton addMealFab;
    private MealAdapter mealAdapter;
    private List<Meal> diaryMeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_diary);

        diaryRecyclerView = findViewById(R.id.diaryRecyclerView);
        addMealFab = findViewById(R.id.addMealFab);

        diaryMeals = new ArrayList<>();
        mealAdapter = new MealAdapter(this,diaryMeals);
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diaryRecyclerView.setAdapter(mealAdapter);

        // Button to add new meal
        addMealFab.setOnClickListener(v -> showAddMealDialog());
    }

    private void showAddMealDialog() {
        // input dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_meal, null);
        EditText etDay = dialogView.findViewById(R.id.etDay);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etTime = dialogView.findViewById(R.id.etTime);
        EditText etCalories = dialogView.findViewById(R.id.etCalories);

        new AlertDialog.Builder(this)
                .setTitle("Log a Meal")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String day = etDay.getText().toString();
                    String name = etName.getText().toString();
                    String time = etTime.getText().toString();
                    String caloriesStr = etCalories.getText().toString();

                    if (day.isEmpty() || name.isEmpty() || time.isEmpty() || caloriesStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int calories = Integer.parseInt(caloriesStr);
                    diaryMeals.add(new Meal(day, name, time, calories));
                    mealAdapter.notifyItemInserted(diaryMeals.size() - 1);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

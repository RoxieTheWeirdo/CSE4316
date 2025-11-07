package com.example.fitbite;





import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MealPlanActivity extends AppCompatActivity {

    // UI elements
    private TextView nextMealLabel, nextMealTime, nextMealName, mealCalories;
    private RecyclerView weekMealsRecyclerView;


    // Adapter + data
    private MealAdapter mealAdapter;
    private List<Meal> mealList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mealplan);

        // Initialize UI
        nextMealLabel = findViewById(R.id.nextMealLabel);
        nextMealTime = findViewById(R.id.nextMealTime);
        nextMealName = findViewById(R.id.nextMealName);
        mealCalories = findViewById(R.id.MealCalories);
        weekMealsRecyclerView = findViewById(R.id.weekMealsRecyclerView);


        // sample data
        nextMealTime.setText("Today, 12:30 PM");
        nextMealName.setText("Chicken");
        mealCalories.setText("Calories: 600");

        // Initialize RecyclerView
        mealList = getSampleMeals();
        mealAdapter = new MealAdapter(this, mealList);
        weekMealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weekMealsRecyclerView.setAdapter(mealAdapter);
    }

    // Sample data
    private List<Meal> getSampleMeals() {
        List<Meal> meals = new ArrayList<>();
        meals.add(new Meal("Monday", "Pasta", "12:00 AM", 550));
        meals.add(new Meal("Tuesday", "Turkey", "12:00 PM", 480));
        meals.add(new Meal("Wednesday", "Rice", "11:30 PM", 600));
        meals.add(new Meal("Thursday", "Chicken", "1:00 PM", 520));
        meals.add(new Meal("Friday", "Cheeseburger", "7:00 PM", 430));
        return meals;
    }
}
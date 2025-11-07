
package com.example.fitbite;



import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

//LATER we can add ingredients of a meal to this section
public class MealDetailActivity extends AppCompatActivity {

    private TextView mealName, mealDay, mealTime, mealCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_detail);

        mealName = findViewById(R.id.detailMealName);
        mealDay = findViewById(R.id.detailMealDay);
        mealTime = findViewById(R.id.detailMealTime);
        mealCalories = findViewById(R.id.detailMealCalories);

        // Get data from Intent
        String name = getIntent().getStringExtra("mealName");
        String day = getIntent().getStringExtra("mealDay");
        String time = getIntent().getStringExtra("mealTime");
        int calories = getIntent().getIntExtra("mealCalories", 0);

        // Display data
        mealName.setText(name);
        mealDay.setText("Day: " + day);
        mealTime.setText("Time: " + time);
        mealCalories.setText("Calories: " + calories);
    }
}

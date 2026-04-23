package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MealGeneratorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meal_generator);

        Button generateBtn = findViewById(R.id.btnGenerateMeal);

        generateBtn.setOnClickListener(v -> {

            List<Food> foods = loadFoodsFromCSV();

            int targetCalories = getIntent().getIntExtra("CALORIE_TARGET", 2000);

            List<Meal> mealList = generateMeals(foods, targetCalories);

            Intent intent = new Intent(MealGeneratorActivity.this, MealPlanActivity.class);
            intent.putExtra("mealList", (Serializable) mealList);
            startActivity(intent);
        });
    }

    // =========================
    // LOAD CSV
    // =========================
    private List<Food> loadFoodsFromCSV() {

        List<Food> foodList = new ArrayList<>();

        try {
            InputStream is = getAssets().open("newfood.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {

                String[] tokens = line.split(",");

                String name = tokens[0];

                int calories = (int) Double.parseDouble(tokens[2]);
                int fat = (int) Double.parseDouble(tokens[3]);
                int protein = (int) Double.parseDouble(tokens[4]);
                int carbs = (int) Double.parseDouble(tokens[5]);

                foodList.add(new Food(name, calories, protein, carbs, fat));
            }

            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return foodList;
    }

    // =========================
    // GENERATE MEALS
    // =========================
    private List<Meal> generateMeals(List<Food> foods, int targetCalories) {

        List<Meal> mealList = new ArrayList<>();

        List<Food> proteins = new ArrayList<>();
        List<Food> carbsList = new ArrayList<>();
        List<Food> fats = new ArrayList<>();

        // CLASSIFY FOODS
        for (Food food : foods) {

            if (food.getProtein() > food.getCarbs() && food.getProtein() > food.getFat()) {
                proteins.add(food);
            }
            else if (food.getCarbs() > food.getProtein() && food.getCarbs() > food.getFat()) {
                carbsList.add(food);
            }
            else {
                fats.add(food);
            }
        }

        // SAFETY CHECK
        if (proteins.isEmpty() || carbsList.isEmpty() || fats.isEmpty()) {
            return mealList;
        }

        Random random = new Random();

        // GENERATE 7 DAYS
        for (int i = 0; i < 7; i++) {

            int breakfastTarget = (int)(targetCalories * 0.3);
            int lunchTarget = (int)(targetCalories * 0.35);
            int dinnerTarget = (int)(targetCalories * 0.35);

            // BUILD EACH MEAL
            MealData breakfast = buildMeal(proteins, carbsList, fats, breakfastTarget, random);
            MealData lunch = buildMeal(proteins, carbsList, fats, lunchTarget, random);
            MealData dinner = buildMeal(proteins, carbsList, fats, dinnerTarget, random);

            String breakfastStr = breakfast.description;
            String lunchStr = lunch.description;
            String dinnerStr = dinner.description;

            int totalCalories = breakfast.calories + lunch.calories + dinner.calories;

            mealList.add(new Meal(breakfastStr, lunchStr, "", dinnerStr, totalCalories));
        }

        return mealList;
    }

    // =========================
    // BUILD SINGLE MEAL
    // =========================
    private MealData buildMeal(List<Food> proteins, List<Food> carbs, List<Food> fats,
                               int targetCalories, Random random) {

        Food protein = proteins.get(random.nextInt(proteins.size()));
        Food carb = carbs.get(random.nextInt(carbs.size()));
        Food fat = fats.get(random.nextInt(fats.size()));

        int totalCalories = protein.getCalories() + carb.getCalories() + fat.getCalories();

        String description = protein.getName() + " + " + carb.getName() + " + " + fat.getName();

        return new MealData(description, totalCalories);
    }

    // =========================
    // HELPER CLASS

    static class MealData {
        String description;
        int calories;

        MealData(String description, int calories) {
            this.description = description;
            this.calories = calories;
        }
    }
}
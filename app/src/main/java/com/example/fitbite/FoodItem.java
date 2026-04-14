package com.example.fitbite;

public class FoodItem {
    public String foodId;
    public String name;
    public int calories;
    public double fat;
    public double carbs;
    public double protein;

    // REQUIRED for Firebase
    public FoodItem() {}

    // Full constructor (for full nutrition data)
    public FoodItem(String foodId, String name, int calories, double fat, double carbs, double protein) {
        this.foodId = foodId;
        this.name = name;
        this.calories = calories;
        this.fat = fat;
        this.carbs = carbs;
        this.protein = protein;
    }

    // Lightweight constructor (for simple lists / search)
    public FoodItem(String name, int calories) {
        this.foodId = null;
        this.name = name;
        this.calories = calories;
        this.fat = 0;
        this.carbs = 0;
        this.protein = 0;
    }
}
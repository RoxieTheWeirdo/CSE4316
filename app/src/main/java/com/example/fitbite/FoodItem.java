package com.example.fitbite;

public class FoodItem {
    public String foodId;
    public String name;
    public int calories;
    public double fat;
    public double carbs;
    public double protein;

    public FoodItem(String foodId, String name, int calories, double fat, double carbs, double protein) {
        this.name = name;
        this.calories = calories;
        this.fat = fat;
        this.carbs = carbs;
        this.protein = protein;
    }
}

package com.example.fitbite;

public class FoodItem {
    public String name;
    public int calories;

    // 🔥 REQUIRED for Firebase
    public FoodItem() {}

    public FoodItem(String name, int calories) {
        this.name = name;
        this.calories = calories;
    }
}
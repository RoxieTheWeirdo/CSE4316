package com.example.fitbite;

public class FoodForML {
    public String name;
    public double calories;
    public double protein;
    public double fat;
    public double carbs;
    public double fiber;
    public double sugar;
    public double rating; // initial user rating

    public FoodForML(String name, double calories, double protein, double fat, double carbs, double fiber, double sugar, double rating) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugar = sugar;
        this.rating = rating; // default
    }
}
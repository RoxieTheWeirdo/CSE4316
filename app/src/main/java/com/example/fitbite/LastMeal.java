package com.example.fitbite;

public class LastMeal {
    private String foodName;
    private String mealType;
    private int quantity;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private String dateKey;
    private long eatenAt;

    public LastMeal() {
    }

    public LastMeal(String foodName, String mealType, int quantity, int calories,
                    double protein, double carbs, double fat,
                    String dateKey, long eatenAt) {
        this.foodName = foodName;
        this.mealType = mealType;
        this.quantity = quantity;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.dateKey = dateKey;
        this.eatenAt = eatenAt;
    }

    public String getFoodName() {
        return foodName;
    }

    public String getMealType() {
        return mealType;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCalories() {
        return calories;
    }

    public double getProtein() {
        return protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public double getFat() {
        return fat;
    }

    public String getDateKey() {
        return dateKey;
    }

    public long getEatenAt() {
        return eatenAt;
    }
}
package com.example.fitbite;

public class Food {

    private String id;
    private String name;
    private int calories;   // calories per 1 quantity
    private int quantity;

    private double protein;
    private double carbs;
    private double fat;

    private String mealType;   // breakfast, lunch, dinner
    private long eatenAt;      // timestamp in millis

    public Food() {
        // Required empty constructor for Firestore
    }

    public Food(String name, int calories, double protein, double carbs, double fat) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.quantity = 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getBaseCalories() {
        return calories;
    }

    public void setBaseCalories(int calories) {
        this.calories = calories;
    }

    public int getQuantity() {
        return quantity <= 0 ? 1 : quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCalories() {
        return getBaseCalories() * getQuantity();
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getProtein() {
        return protein * getQuantity();
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getBaseProtein() {
        return protein;
    }

    public double getCarbs() {
        return carbs * getQuantity();
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getBaseCarbs() {
        return carbs;
    }

    public double getFat() {
        return fat * getQuantity();
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getBaseFat() {
        return fat;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public long getEatenAt() {
        return eatenAt;
    }

    public void setEatenAt(long eatenAt) {
        this.eatenAt = eatenAt;
    }
}
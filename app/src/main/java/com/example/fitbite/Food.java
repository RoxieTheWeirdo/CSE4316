package com.example.fitbite;

public class Food {

    private String id;
    private String name;

    private int calories;   // base calories (per 1 quantity)
    private int quantity;

    private double protein;
    private double carbs;
    private double fat;

    private String mealType;   // breakfast, lunch, dinner
    private long eatenAt;      // timestamp

    //Required for Firebase to work
    public Food() {}

    // Main constructor (used across app)
    public Food(String name, int calories, double protein, double carbs, double fat) {
        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.quantity = 1;
    }

    // -------- BASIC GETTERS --------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // -------- CALORIES --------

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

    // -------- PROTEIN --------

    public double getProtein() {
        return protein * getQuantity();
    }

    public double getBaseProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    // -------- CARBS --------

    public double getCarbs() {
        return carbs * getQuantity();
    }

    public double getBaseCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    // -------- FAT --------

    public double getFat() {
        return fat * getQuantity();
    }

    public double getBaseFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    // -------- MEAL TRACKING --------

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
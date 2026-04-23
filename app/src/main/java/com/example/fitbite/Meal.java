package com.example.fitbite;

import java.io.Serializable;

public class Meal implements Serializable {

    private String breakfast;
    private String lunch;
    private String snack;
    private String dinner;
    private int calories;
    private double protein;
    private double carbs;
    private double fat;
    private int day;
    private String slot;

    // Per-slot macros for display on each meal card
    private double breakfastProtein, breakfastCarbs, breakfastFat;
    private double lunchProtein,     lunchCarbs,     lunchFat;
    private double dinnerProtein,    dinnerCarbs,    dinnerFat;

    public Meal(String breakfast, String lunch, String snack, String dinner, int calories) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.snack = snack;
        this.dinner = dinner;
        this.calories = calories;
    }

    public String getBreakfast() { return breakfast; }
    public String getLunch()     { return lunch; }
    public String getSnack()     { return snack; }
    public String getDinner()    { return dinner; }
    public int    getCalories()  { return calories; }
    public double getProtein()   { return protein; }
    public double getCarbs()     { return carbs; }
    public double getFat()       { return fat; }
    public int    getDay()       { return day; }
    public String getSlot()      { return slot; }

    public double getBreakfastProtein() { return breakfastProtein; }
    public double getBreakfastCarbs()   { return breakfastCarbs; }
    public double getBreakfastFat()     { return breakfastFat; }
    public double getLunchProtein()     { return lunchProtein; }
    public double getLunchCarbs()       { return lunchCarbs; }
    public double getLunchFat()         { return lunchFat; }
    public double getDinnerProtein()    { return dinnerProtein; }
    public double getDinnerCarbs()      { return dinnerCarbs; }
    public double getDinnerFat()        { return dinnerFat; }

    public void setProtein(double v) { this.protein = v; }
    public void setCarbs(double v)   { this.carbs = v; }
    public void setFat(double v)     { this.fat = v; }
    public void setDay(int v)        { this.day = v; }
    public void setSlot(String v)    { this.slot = v; }

    public void setBreakfastMacros(double p, double c, double f) { breakfastProtein = p; breakfastCarbs = c; breakfastFat = f; }
    public void setLunchMacros(double p, double c, double f)     { lunchProtein = p;     lunchCarbs = c;     lunchFat = f; }
    public void setDinnerMacros(double p, double c, double f)    { dinnerProtein = p;    dinnerCarbs = c;    dinnerFat = f; }
}
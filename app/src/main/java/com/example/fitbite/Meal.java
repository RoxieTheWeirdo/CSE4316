package com.example.fitbite;

import java.io.Serializable;

public class Meal implements Serializable {

    private String breakfast;
    private String lunch;
    private String dinner;
    private int calories;

    public Meal(String breakfast, String lunch, String dinner, int calories) {
        this.breakfast = breakfast;
        this.lunch = lunch;
        this.dinner = dinner;
        this.calories = calories;
    }

    public String getBreakfast() {
        return breakfast;
    }

    public String getLunch() {
        return lunch;
    }

    public String getDinner() {
        return dinner;
    }

    public int getCalories() {
        return calories;
    }
}
package com.example.fitbite;

public class Meal {
    private String day;
    private String name;
    private String time;
    private int calories;

    public Meal(String day, String name, String time, int calories) {
        this.day = day;
        this.name = name;
        this.time = time;
        this.calories = calories;
    }

    public String getDay() { return day; }
    public String getName() { return name; }
    public String getTime() { return time; }
    public int getCalories() { return calories; }
}


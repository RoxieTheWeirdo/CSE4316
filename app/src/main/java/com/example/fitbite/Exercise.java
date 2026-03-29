package com.example.fitbite;

public class Exercise {
    public String name;
    public int calories;
    public int duration;
    public String type;
    public String date;
    public String id;

    public Exercise() {}

    public Exercise(String name, int calories, int duration, String type, String date) {
        this.name = name;
        this.calories = calories;
        this.duration = duration;
        this.type = type;
        this.date = date;
    }
}

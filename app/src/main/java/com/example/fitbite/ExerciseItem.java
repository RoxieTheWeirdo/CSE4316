package com.example.fitbite;

public class ExerciseItem {
    public String name;
    public String intensity;
    public String type;
    public double met;

    public ExerciseItem(String name, String intensity, String type, double met) {
        this.name = name;
        this.intensity = intensity;
        this.type = type;
        this.met = met;
    }
}

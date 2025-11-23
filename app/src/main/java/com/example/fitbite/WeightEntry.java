package com.example.fitbite;

public class WeightEntry {
    public float weight;
    public String date;
    public String time;

    public WeightEntry(float w, String d, String t) {
        weight = w;
        date = d;
        time = t;
    }

    public float getWeight() {
        return weight;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }
}

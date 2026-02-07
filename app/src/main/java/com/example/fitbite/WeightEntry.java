package com.example.fitbite;

public class WeightEntry {

    private final float weight;
    private final String date;
    private final String time;

    private final long timestamp;

    public WeightEntry(float weight, String date, String time, long timestamp) {
        this.weight = weight;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
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

    public long getTimestamp() { return timestamp; }
}

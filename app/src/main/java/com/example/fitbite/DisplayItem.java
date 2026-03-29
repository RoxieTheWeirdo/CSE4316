package com.example.fitbite;

public class DisplayItem {
    public boolean isHeader;
    public String headerTitle;
    public ExerciseItem exercise;
    public boolean expanded = false;
    public Exercise exerciseRef;

    // Header constructor
    public DisplayItem(String headerTitle) {
        this.isHeader = true;
        this.headerTitle = headerTitle;
    }

    // Exercise constructor
    public DisplayItem(ExerciseItem exercise) {
        this.isHeader = false;
        this.exercise = exercise;
    }
}

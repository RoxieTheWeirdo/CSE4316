
package com.example.fitbite;

public class FoodForML {



    public String name;

    public double calories;
    public double protein;
    public double fat;
    public double carbs;
    public double fiber;
    public double sugar;



    // Constructor
    public FoodForML(String name,
                     double calories,
                     double protein,
                     double fat,
                     double carbs,
                     double fiber,
                     double sugar,
                     double rating) {

        this.name = name;
        this.calories = calories;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
        this.fiber = fiber;
        this.sugar = sugar;

    }




    public String getName() { return name; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getFat() { return fat; }
    public double getCarbs() { return carbs; }
    public double getFiber() { return fiber; }
    public double getSugar() { return sugar; }




    public boolean isProtein() {
        return protein >= 10;
    }

    public boolean isCarb() {
        return carbs >= 15;
    }

    public boolean isFat() {
        return fat >= 8;
    }


    @Override
    public String toString() {
        return name +
                " | Cal:" + calories +
                " P:" + protein +
                " C:" + carbs +
                " F:" + fat;
    }
}
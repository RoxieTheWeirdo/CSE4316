package com.example.fitbite;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class FoodDiaryActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private final List<Food> breakfastFoods = new ArrayList<>();
    private final List<Food> lunchFoods = new ArrayList<>();
    private final List<Food> dinnerFoods = new ArrayList<>();

    private ListenerRegistration breakfastListener;
    private ListenerRegistration lunchListener;
    private ListenerRegistration dinnerListener;

    private TextView selectedDateText;
    private Calendar selectedDate = Calendar.getInstance();
    private String selectedDateKey;

    private int goalCalories = 0;
    private int exerciseCalories = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_diary);

        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("AUTH_CHECK", "No user logged in, finishing activity.");
            finish();
            return;
        }

        loadGoalCalories();
        selectedDateText = findViewById(R.id.selected_date_text);
        updateDisplayedDate();

        selectedDateText.setOnClickListener(v -> showDatePicker());

        setupMealSection(
                R.id.breakfast_section,
                "Breakfast",
                "breakfast",
                breakfastFoods
        );

        setupMealSection(
                R.id.lunch_section,
                "Lunch",
                "lunch",
                lunchFoods
        );

        setupMealSection(
                R.id.dinner_section,
                "Dinner",
                "dinner",
                dinnerFoods
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeMealListeners();
    }

    private void removeMealListeners() {
        if (breakfastListener != null) {
            breakfastListener.remove();
            breakfastListener = null;
        }
        if (lunchListener != null) {
            lunchListener.remove();
            lunchListener = null;
        }
        if (dinnerListener != null) {
            dinnerListener.remove();
            dinnerListener = null;
        }
    }

    private void showDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    updateDisplayedDate();
                    reloadAllMealsForSelectedDate();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    private void updateDisplayedDate() {
        selectedDateKey = formatDateKey(selectedDate);
        selectedDateText.setText(formatDisplayDate(selectedDate));
    }

    private String formatDateKey(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());
    }

    private String formatDisplayDate(Calendar calendar) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(calendar.getTime());
    }

    private String getUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.e("AUTH_DEBUG", "User is NULL, cannot get user ID.");
            return null;
        }
        return user.getUid();
    }

    private void reloadAllMealsForSelectedDate() {
        removeMealListeners();

        breakfastFoods.clear();
        lunchFoods.clear();
        dinnerFoods.clear();

        updateCalorieSummary();

        setupMealSection(
                R.id.breakfast_section,
                "Breakfast",
                "breakfast",
                breakfastFoods
        );

        setupMealSection(
                R.id.lunch_section,
                "Lunch",
                "lunch",
                lunchFoods
        );

        setupMealSection(
                R.id.dinner_section,
                "Dinner",
                "dinner",
                dinnerFoods
        );
    }

    private void setupMealSection(
            int sectionId,
            String mealName,
            String mealKey,
            List<Food> foodList
    ) {
        View section = findViewById(sectionId);
        if (section == null) {
            Log.e("UI_ERROR", "Section not found for id: " + sectionId);
            return;
        }

        TextView title = section.findViewById(R.id.meal_name);
        TextView mealCaloriesText = section.findViewById(R.id.meal_calories);
        RecyclerView recycler = section.findViewById(R.id.recyclerFoods);
        TextView addButton = section.findViewById(R.id.add_food_button);

        title.setText(mealName);
        mealCaloriesText.setText("0 cal");

        FoodAdapter adapter = new FoodAdapter(foodList, new FoodAdapter.OnFoodActionListener() {
            @Override
            public void onDelete(Food food) {
                showDeleteConfirmation(mealKey, food);
            }

            @Override
            public void onEdit(Food food) {
                showEditQuantityDialog(mealKey, food);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        recycler.setNestedScrollingEnabled(false);

        ListenerRegistration registration = listenForFoodUpdates(
                mealKey,
                foodList,
                adapter,
                mealCaloriesText
        );

        if ("breakfast".equals(mealKey)) {
            breakfastListener = registration;
        } else if ("lunch".equals(mealKey)) {
            lunchListener = registration;
        } else if ("dinner".equals(mealKey)) {
            dinnerListener = registration;
        }

        addButton.setOnClickListener(v -> showAddFoodDialog(mealKey));
    }

    private ListenerRegistration listenForFoodUpdates(
            String mealCollection,
            List<Food> foodList,
            FoodAdapter adapter,
            TextView mealCaloriesText
    ) {
        String uid = getUserId();
        if (uid == null) return null;

        return db.collection("users").document(uid)
                .collection("foodDiary").document(selectedDateKey)
                .collection("meals").document(mealCollection)
                .collection("items")
                .addSnapshotListener((snapshots, e) -> {

                    if (e != null) {
                        Log.e("FIRESTORE_ERROR", "Listen failed for " + mealCollection, e);
                        return;
                    }

                    if (snapshots == null) return;

                    foodList.clear();

                    snapshots.forEach(doc -> {
                        Food food = doc.toObject(Food.class);
                        if (food != null) {
                            food.setId(doc.getId());
                            foodList.add(food);
                        }
                    });

                    adapter.notifyDataSetChanged();

                    int mealTotalCalories = sumCalories(foodList);
                    mealCaloriesText.setText(mealTotalCalories + " cal");

                    updateCalorieSummary();
                });
    }

    private void showAddFoodDialog(String mealCollection) {
        View view = getLayoutInflater().inflate(R.layout.popup_add_food, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        EditText nameInput = view.findViewById(R.id.food_name_input);
        EditText caloriesInput = view.findViewById(R.id.food_calories_input);
        EditText proteinInput = view.findViewById(R.id.food_protein_input);
        EditText carbsInput = view.findViewById(R.id.food_carbs_input);
        EditText fatInput = view.findViewById(R.id.food_fat_input);

        view.findViewById(R.id.add_food_confirm_button).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String calStr = caloriesInput.getText().toString().trim();
            String proteinStr = proteinInput.getText().toString().trim();
            String carbsStr = carbsInput.getText().toString().trim();
            String fatStr = fatInput.getText().toString().trim();

            if (name.isEmpty()) {
                nameInput.setError("Enter food name");
                return;
            }

            if (calStr.isEmpty()) {
                caloriesInput.setError("Enter calories");
                return;
            }

            if (proteinStr.isEmpty()) proteinStr = "0";
            if (carbsStr.isEmpty()) carbsStr = "0";
            if (fatStr.isEmpty()) fatStr = "0";

            try {
                int calories = Integer.parseInt(calStr);
                double protein = Double.parseDouble(proteinStr);
                double carbs = Double.parseDouble(carbsStr);
                double fat = Double.parseDouble(fatStr);

                if (calories <= 0) {
                    caloriesInput.setError("Calories must be greater than 0");
                    return;
                }

                String uid = getUserId();
                if (uid == null) {
                    Log.e("FIRESTORE", "Cannot add food, user is not logged in.");
                    return;
                }

                Food newFood = new Food(name, calories, protein, carbs, fat);
                newFood.setQuantity(1);
                newFood.setMealType(mealCollection);
                newFood.setEatenAt(System.currentTimeMillis());

                db.collection("users").document(uid)
                        .collection("foodDiary").document(selectedDateKey)
                        .collection("meals").document(mealCollection)
                        .collection("items")
                        .add(newFood)
                        .addOnSuccessListener(doc -> {
                            saveLastMeal(uid, newFood, selectedDateKey);
                            Log.d("FIRESTORE", "Food added to " + mealCollection + " for " + selectedDateKey);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Log.e("FIRESTORE", "Error adding food to " + mealCollection, e));

            } catch (NumberFormatException e) {
                Log.e("INPUT_ERROR", "Invalid macro/calorie input", e);
            }
        });

        dialog.show();
    }

    private void saveLastMeal(String uid, Food food, String dateKey) {
        LastMeal lastMeal = new LastMeal(
                food.getName(),
                food.getMealType(),
                food.getQuantity(),
                food.getCalories(),
                food.getProtein(),
                food.getCarbs(),
                food.getFat(),
                dateKey,
                food.getEatenAt()
        );

        db.collection("users")
                .document(uid)
                .collection("meta")
                .document("lastMeal")
                .set(lastMeal)
                .addOnSuccessListener(unused -> Log.d("LAST_MEAL", "Last meal saved"))
                .addOnFailureListener(e -> Log.e("LAST_MEAL", "Failed to save last meal", e));
    }

    private void showDeleteConfirmation(String mealCollection, Food food) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Food")
                .setMessage("Delete " + food.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFood(mealCollection, food))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFood(String mealCollection, Food food) {
        String uid = getUserId();
        if (uid == null) return;

        db.collection("users").document(uid)
                .collection("foodDiary").document(selectedDateKey)
                .collection("meals").document(mealCollection)
                .collection("items").document(food.getId())
                .delete()
                .addOnSuccessListener(unused ->
                        Log.d("DELETE", "Food deleted"))
                .addOnFailureListener(e ->
                        Log.e("DELETE", "Error deleting", e));
    }

    private void showEditQuantityDialog(String mealCollection, Food food) {
        EditText input = new EditText(this);
        input.setText(String.valueOf(food.getQuantity()));
        input.setSelection(input.getText().length());
        input.setHint("Quantity");

        new AlertDialog.Builder(this)
                .setTitle("Edit Quantity")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    try {
                        int newQty = Integer.parseInt(input.getText().toString().trim());

                        if (newQty <= 0) {
                            Log.e("EDIT", "Quantity must be greater than 0");
                            return;
                        }

                        String uid = getUserId();
                        if (uid == null) return;

                        db.collection("users").document(uid)
                                .collection("foodDiary").document(selectedDateKey)
                                .collection("meals").document(mealCollection)
                                .collection("items").document(food.getId())
                                .update("quantity", newQty)
                                .addOnSuccessListener(unused ->
                                        Log.d("EDIT", "Quantity updated"))
                                .addOnFailureListener(e ->
                                        Log.e("EDIT", "Failed to update quantity", e));

                    } catch (NumberFormatException e) {
                        Log.e("EDIT", "Invalid quantity", e);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void updateCalorieSummary() {
        int foodCalories =
                sumCalories(breakfastFoods) +
                        sumCalories(lunchFoods) +
                        sumCalories(dinnerFoods);

        double totalProtein =
                sumProtein(breakfastFoods) +
                        sumProtein(lunchFoods) +
                        sumProtein(dinnerFoods);

        double totalCarbs =
                sumCarbs(breakfastFoods) +
                        sumCarbs(lunchFoods) +
                        sumCarbs(dinnerFoods);

        double totalFat =
                sumFat(breakfastFoods) +
                        sumFat(lunchFoods) +
                        sumFat(dinnerFoods);

        int remaining = goalCalories - foodCalories + exerciseCalories;

        ((TextView) findViewById(R.id.goal_calories_text_view))
                .setText(String.valueOf(goalCalories));

        ((TextView) findViewById(R.id.food_calories_text_view))
                .setText(String.valueOf(foodCalories));

        ((TextView) findViewById(R.id.exercise_calories_text_view))
                .setText(String.valueOf(exerciseCalories));

        ((TextView) findViewById(R.id.remaining_calories_text_view))
                .setText(String.valueOf(remaining));

        /*((TextView) findViewById(R.id.protein_text_view))
                .setText(String.format(Locale.getDefault(), "%.1fg", totalProtein));

        ((TextView) findViewById(R.id.carbs_text_view))
                .setText(String.format(Locale.getDefault(), "%.1fg", totalCarbs));

        ((TextView) findViewById(R.id.fat_text_view))
                .setText(String.format(Locale.getDefault(), "%.1fg", totalFat));*/
    }


    private int sumCalories(List<Food> foods) {
        int total = 0;
            for (Food f : foods) {
                total += f.getCalories();
        }
        return total;
    }

    private double sumProtein(List<Food> foods) {
        double total = 0;
            for (Food f : foods) {
             total += f.getProtein();
        }
     return total;
    }

    private double sumCarbs(List<Food> foods) {
        double total = 0;
        for (Food f : foods) {
             total += f.getCarbs();
            }
        return total;
    }

    private double sumFat(List<Food> foods) {
        double total = 0;
            for (Food f : foods) {
                total += f.getFat();
            }
            return total;
    }
    private void loadGoalCalories() {
        String uid = getUserId();
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long goalCaloriesLong = document.getLong("goalCalories");

                        if (goalCaloriesLong != null) {
                            goalCalories = goalCaloriesLong.intValue();
                        } else {
                            Double goalCaloriesDouble = document.getDouble("goalCalories");
                            if (goalCaloriesDouble != null) {
                                goalCalories = (int) Math.round(goalCaloriesDouble);
                            } else {
                                goalCalories = 1600; // fallback
                            }
                        }
                    } else {
                        goalCalories = 1600; // fallback
                    }

                    updateCalorieSummary();
                })
                .addOnFailureListener(e -> {
                    Log.e("GOAL_LOAD", "Failed to load goal calories", e);
                    goalCalories = 1600; // fallback
                    updateCalorieSummary();
                });
    }
}

package com.example.fitbite;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodDiaryActivity extends AppCompatActivity {

    private static final String TAG = "FoodDiaryActivity";
    private MealAdapter mealAdapter;
    private List<Meal> diaryMeals;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_diary);

        RecyclerView diaryRecyclerView = findViewById(R.id.diaryRecyclerView);
        FloatingActionButton addMealFab = findViewById(R.id.addMealFab);

        diaryMeals = new ArrayList<>();
        mealAdapter = new MealAdapter(this, diaryMeals);
        diaryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        diaryRecyclerView.setAdapter(mealAdapter);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is signed in and set up listener
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            setupFirestoreListener(currentUser.getUid());
        } else {
            Toast.makeText(this, "You must be logged in to view your diary.", Toast.LENGTH_SHORT).show();
            finish(); // or redirect to login
        }

        // Button to add new meal
        addMealFab.setOnClickListener(v -> showAddMealDialog());
    }

    private void setupFirestoreListener(String userId) {
        db.collection("users").document(userId).collection("meals")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        Toast.makeText(FoodDiaryActivity.this, "Error getting meals.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        diaryMeals.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            if (doc != null) {
                                String day = doc.getString("day");
                                String name = doc.getString("name");
                                String time = doc.getString("time");
                                Long caloriesLong = doc.getLong("calories");
                                int calories = (caloriesLong != null) ? caloriesLong.intValue() : 0;
                                diaryMeals.add(new Meal(day, name, time, calories));
                            }
                        }
                        mealAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void showAddMealDialog() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to add a meal.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        // input dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_meal, null);
        EditText etDay = dialogView.findViewById(R.id.etDay);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etTime = dialogView.findViewById(R.id.etTime);
        EditText etCalories = dialogView.findViewById(R.id.etCalories);

        new AlertDialog.Builder(this)
                .setTitle("Log a Meal")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String day = etDay.getText().toString();
                    String name = etName.getText().toString();
                    String time = etTime.getText().toString();
                    String caloriesStr = etCalories.getText().toString();

                    if (day.isEmpty() || name.isEmpty() || time.isEmpty() || caloriesStr.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int calories = Integer.parseInt(caloriesStr);

                    // Create a Map to store the meal data
                    Map<String, Object> mealData = new HashMap<>();
                    mealData.put("day", day);
                    mealData.put("name", name);
                    mealData.put("time", time);
                    mealData.put("calories", calories);

                    // Add a new document to the current user's "meals" subcollection
                    db.collection("users").document(userId).collection("meals")
                            .add(mealData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(FoodDiaryActivity.this, "Meal saved.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error adding document", e);
                                Toast.makeText(FoodDiaryActivity.this, "Error saving meal.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

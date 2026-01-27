package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Example: Initialize search box if present
        searchBox = findViewById(R.id.searchFoodText);

        // Example: Load default fragment if needed
        // loadFragment(new HomeFragment()); // Uncomment if you have a fragment for the main dashboard
    }

    /**
     * Opens the search screen inside the fragment container
     */
    private void openSearchScreen() {
        RecyclerView recyclerView = findViewById(R.id.foodRecyclerView);
        if (recyclerView != null) {
            List<FoodItem> foodList = new ArrayList<>();
            FoodAdapter adapter = new FoodAdapter(this, foodList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        }
    }

    /**
     * Opens the EditFoodActivity screen
     */
    private void openEditScreen() {
        Intent intent = new Intent(MainActivity.this, EditFoodActivity.class);
        startActivity(intent);
    }

    /**
     * Helper method to replace the fragment in the container
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

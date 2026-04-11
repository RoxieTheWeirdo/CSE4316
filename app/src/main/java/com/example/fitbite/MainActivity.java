package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.fitbite.network.ProxyClient;

public class MainActivity extends AppCompatActivity {

    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // If the search box exists on this layout, grab it
        searchBox = findViewById(R.id.searchFoodText);

        // Keep main's FatSecret proxy test (remove later if you don’t want it always running)
        testFatsecret();

        // Only build the search list UI if the recycler exists on this screen
        openSearchScreen();
    }

    private void openSearchScreen() {
        RecyclerView recyclerView = findViewById(R.id.foodRecyclerView);
        if (recyclerView == null) return;

        List<FoodItem> foodList = new ArrayList<>();

        FoodAdapter adapter = new FoodAdapter(this, foodList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void openEditScreen() {
        Intent intent = new Intent(MainActivity.this, EditFoodActivity.class);
        startActivity(intent);
    }

    private void testFatsecret() {
        new Thread(() -> {
            try {
                ProxyClient proxy = new ProxyClient();
                String json = proxy.searchFood("apple");
                runOnUiThread(() -> Log.d("FATSECRET_TEST", json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}


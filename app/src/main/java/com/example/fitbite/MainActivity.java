package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitbite.network.ProxyClient;

public class MainActivity extends AppCompatActivity {
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // testFatsecret();
        // If you want to show the food list on start:
        // openSearchScreen();
    }

    private void openSearchScreen() {
        RecyclerView recyclerView = findViewById(R.id.foodRecyclerView);
        if (recyclerView == null) return;

        List<Food> foodList = new ArrayList<>();

        foodList.add(new Food("French Toast", 350,10,10,10));
        foodList.add(new Food("Apple", 100,10,10,10));
        foodList.add(new Food("Orange", 60,10,10,10));
        foodList.add(new Food("Pizza Slice", 320,10,10,10));
        foodList.add(new Food("Veggie Pizza Slice", 350,10,10,10));
        foodList.add(new Food("Donut Holes", 35,10,10,10));
        foodList.add(new Food("Glazed Donut", 60,10,10,10));

        FoodAdapter adapter = new FoodAdapter(foodList,null);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchBox = findViewById(R.id.searchFoodText);
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

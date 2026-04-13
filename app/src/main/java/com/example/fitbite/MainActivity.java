package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;               // <-- ADDED
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.fitbite.network.ProxyClient;   // <-- ADDED

public class MainActivity extends AppCompatActivity {
    private EditText searchBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        testFatsecret();
    }

    private void openSearchScreen() {
        RecyclerView recyclerView = findViewById(R.id.foodRecyclerView);
        List<FoodItem> foodList = new ArrayList<>();

        //This is just an example array from the discord picture
        //I believe how this will work is that we will pull data dynamically from user profile.
        foodList.add(new FoodItem("French Toast", 350));
        foodList.add(new FoodItem("Apple", 100));
        foodList.add(new FoodItem("Orange", 60));
        foodList.add(new FoodItem("Pizza Slice", 320));
        foodList.add(new FoodItem("Veggie Pizza Slice", 350));
        foodList.add(new FoodItem("Donut Holes", 35));
        foodList.add(new FoodItem("Glazed Donut", 60));
        foodList.add(new FoodItem("Some really long food name like an entire cake", 11000));

        FoodAdapter adapter = new FoodAdapter(this, foodList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //Placeholder to start on searchfoodscreen, replace with the login screen once made.
       // openSearchScreen();
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

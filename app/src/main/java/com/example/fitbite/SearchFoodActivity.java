package com.example.fitbite;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SearchFoodActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchfoodscreen);
        RecyclerView recyclerView = findViewById(R.id.foodRecyclerView);

        List<FoodItem> foodList = new ArrayList<>();
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
    }
}

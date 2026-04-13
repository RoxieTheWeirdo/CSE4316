package com.example.fitbite;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitbite.network.ProxyClient;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SearchFoodActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private EditText searchInput;
    private ImageButton searchButton;
    private List<FoodItem> foodList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchfoodscreen);

        recyclerView = findViewById(R.id.foodRecyclerView);


        searchInput = findViewById(R.id.searchFoodText);

        searchButton = findViewById(R.id.searchButton);

        adapter = new FoodAdapter(this, foodList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 🔥 When search button is clicked → fetch from API
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchFood(query);
            }
        });
    }

    // gets food from your Proxy Server
    private void searchFood(String query) {
        new Thread(() -> {
            try {
                ProxyClient proxy = new ProxyClient();
                String json = proxy.searchFood(query);
                Log.d("FATSECRET_JSON", json);

                FoodResponse response = new Gson().fromJson(json, FoodResponse.class);

                foodList.clear();

                if (response != null &&
                        response.foods != null &&
                        response.foods.food != null) {

                    for (FoodResponse.Food f : response.foods.food) {
                        int calories = extractCalories(f.food_description);

                        foodList.add(new FoodItem(
                                f.food_name,
                                calories
                        ));
                    }
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                Log.e("FATSECRET_JSON", "Error", e);
            }
        }).start();
    }

    // Extract first number before “kcal”
    private int extractCalories(String description) {
        try {
            String part = description.split("Calories:")[1].split("kcal")[0].trim();
            return Integer.parseInt(part);
        } catch (Exception e) {
            return 0;
        }
    }

    //  Gson model for parsing JSON
    public static class FoodResponse {
        Foods foods;

        public static class Foods {
            List<Food> food;
        }

        public static class Food {
            String food_name;

            @SerializedName("food_description")
            String food_description;
        }
    }
}

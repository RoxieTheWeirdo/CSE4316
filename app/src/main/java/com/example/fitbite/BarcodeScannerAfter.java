package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.fitbite.network.ProxyClient;

import org.json.JSONArray;
import org.json.JSONObject;

public class BarcodeScannerAfter extends AppCompatActivity {

    private TextView nameTextView, caloriesView, sodiumView, fatsView, carbsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner_after);

        // Link UI elements
        nameTextView = findViewById(R.id.textView);
        caloriesView = findViewById(R.id.caloriesTextView);
        sodiumView = findViewById(R.id.sodiumTextView);
        fatsView = findViewById(R.id.fatsTextView);
        carbsView = findViewById(R.id.carbsTextView);

        // Get barcode from previous screen
        String barcode = getIntent().getStringExtra("data");
        nameTextView.setText("Loading…");

        // Fetch nutrition using FatSecret via proxy
        fetchFoodByBarcode(barcode);

        // Scan again
        Button barcodeScan = findViewById(R.id.scanAnotherButton);
        barcodeScan.setOnClickListener(v -> scanCode());

        // More info screen (you can expand later)
        Button moreInfo = findViewById(R.id.moreInfo);
        moreInfo.setOnClickListener(v -> {
            Intent i = new Intent(this, BarcodeMoreInfo.class);
            startActivity(i);
        });

        // Add to diary button – currently just stub
        Button addToDiaryButton = findViewById(R.id.addToDiaryButton);
        addToDiaryButton.setOnClickListener(v -> {
            // TODO: implement adding to food diary
        });
    }

    /**
     * Step 1: Use barcode -> get food_id
     * Step 2: Use food_id -> get full nutrition details
     */
    private void fetchFoodByBarcode(String barcode) {
        new Thread(() -> {
            try {
                ProxyClient client = new ProxyClient();

                // first  Get food_id from barcode
                String idJson = client.getFoodIdForBarcode(barcode);
                Log.d("FatSecret", "ID JSON: " + idJson);

                JSONObject idObj = new JSONObject(idJson);
                // Response format: { "food_id": { "value": "1836324" } }
                String foodId = idObj
                        .getJSONObject("food_id")
                        .getString("value");

                // second  Get full food details
                String detailsJson = client.getFoodDetails(foodId);
                Log.d("FatSecret", "Details JSON: " + detailsJson);

                JSONObject root = new JSONObject(detailsJson);
                JSONObject food = root.getJSONObject("food");

                String foodName = food.optString("food_name", "Unknown item");


                JSONObject servings = food.getJSONObject("servings");
                Object servingNode = servings.get("serving");

                JSONObject serving;
                if (servingNode instanceof JSONArray) {
                    serving = ((JSONArray) servingNode).getJSONObject(0);
                } else {
                    serving = (JSONObject) servingNode;
                }

                double calories = serving.optDouble("calories", 0);
                double fat = serving.optDouble("fat", 0);
                double carbs = serving.optDouble("carbohydrate", 0);
                double sodium = serving.optDouble("sodium", 0);

                // third  Update UI on main thread
                runOnUiThread(() -> {
                    nameTextView.setText(foodName);
                    caloriesView.setText("Calories: " + calories);
                    fatsView.setText("Fats: " + fat + " g");
                    carbsView.setText("Carbs: " + carbs + " g");
                    sodiumView.setText("Sodium: " + sodium + " mg");
                });

            } catch (Exception e) {
                Log.e("FatSecret", "Error loading food", e);
                runOnUiThread(() -> nameTextView.setText("Failed to load data"));
            }
        }).start();
    }

    private void scanCode() {
        Intent intent = new Intent(this, BarcodeScanner.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

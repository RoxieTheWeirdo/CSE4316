package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class BarcodeMoreInfo extends AppCompatActivity {

    private TextView proteinView, carbsView, fatsView, sodiumView, cholView, nutrientView, servingSizeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_more_info);

        servingSizeView = findViewById(R.id.servingSizeTextView);
        fatsView = findViewById(R.id.fatsTextView);
        cholView = findViewById(R.id.cholTextView);
        sodiumView = findViewById(R.id.sodiumTextView);
        carbsView = findViewById(R.id.carbsTextView);
        proteinView = findViewById(R.id.proteinTextView);
        nutrientView = findViewById(R.id.nutrientsTextView);

        String barcode = getIntent().getStringExtra("data");

        if (barcode != null && !barcode.trim().isEmpty()) {
            servingSizeView.setText("Looking up barcode...");
            fetchFoodData(barcode.trim());
        } else {
            showError("No barcode found");
        }
    }

    private void fetchFoodData(String barcode) {
        new Thread(() -> {
            try {
                String apiUrl = "https://platform.fatsecret.com/rest/server.api"
                        + "?method=food.find_id_for_barcode"
                        + "&barcode=" + barcode
                        + "&format=json";

                String encodedUrl = URLEncoder.encode(apiUrl, "UTF-8");
                String proxyUrl = "https://fatsecret-proxy-cd6t.onrender.com/proxy/fatsecret?url=" + encodedUrl;

                URL url = new URL(proxyUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");

                // 🔥 IMPORTANT FIX (timeout)
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                int responseCode = conn.getResponseCode();
                Log.d("HTTP", "Response Code: " + responseCode);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                (responseCode >= 200 && responseCode < 300)
                                        ? conn.getInputStream()
                                        : conn.getErrorStream()
                        )
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                String response = result.toString();
                Log.d("API_RESPONSE", response);

                if (responseCode < 200 || responseCode >= 300) {
                    showError("Barcode lookup failed");
                    return;
                }

                JSONObject json = new JSONObject(response);

                if (!json.has("food_id")) {
                    showError("Food not found for this barcode");
                    return;
                }

                String foodId;

                Object foodIdObj = json.get("food_id");

                if (foodIdObj instanceof JSONObject) {
                    foodId = ((JSONObject) foodIdObj).optString("value", "");
                } else {
                    foodId = String.valueOf(foodIdObj);
                }

                if (foodId.isEmpty()) {
                    showError("Invalid food ID");
                    return;
                }

                getFoodDetails(foodId);

            } catch (Exception e) {
                Log.e("API_ERROR", "fetchFoodData failed", e);
                showError("Error finding barcode");
            }
        }).start();
    }

    private void getFoodDetails(String foodId) {
        new Thread(() -> {
            try {
                String apiUrl = "https://platform.fatsecret.com/rest/server.api"
                        + "?method=food.get.v2"
                        + "&food_id=" + foodId
                        + "&format=json";

                String encodedUrl = URLEncoder.encode(apiUrl, "UTF-8");
                String proxyUrl = "https://fatsecret-proxy-cd6t.onrender.com/proxy/fatsecret?url=" + encodedUrl;

                URL url = new URL(proxyUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");

                // 🔥 IMPORTANT FIX (timeout)
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);

                int responseCode = conn.getResponseCode();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                (responseCode >= 200 && responseCode < 300)
                                        ? conn.getInputStream()
                                        : conn.getErrorStream()
                        )
                );

                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                reader.close();

                String response = result.toString();
                Log.d("DETAIL_RESPONSE", response);

                if (responseCode < 200 || responseCode >= 300) {
                    showError("Could not get food details");
                    return;
                }

                JSONObject json = new JSONObject(response);

                if (!json.has("food")) {
                    showError("Food details not found");
                    return;
                }

                JSONObject food = json.getJSONObject("food");

                String name = food.optString("food_name", "Unknown Food");

                JSONObject servings = food.optJSONObject("servings");

                if (servings == null || !servings.has("serving")) {
                    showError("Serving data not found");
                    return;
                }

                Object servingObj = servings.get("serving");
                JSONObject serving;

                if (servingObj instanceof JSONArray) {
                    JSONArray arr = (JSONArray) servingObj;
                    if (arr.length() == 0) {
                        showError("Serving data not found");
                        return;
                    }
                    serving = arr.getJSONObject(0);
                } else {
                    serving = (JSONObject) servingObj;
                }

                String servingSize = serving.optString("serving_description", "N/A");
                String calories = serving.optString("calories", "N/A");
                String protein = serving.optString("protein", "N/A");
                String fat = serving.optString("fat", "N/A");
                String carbs = serving.optString("carbohydrate", "N/A");
                String sodium = serving.optString("sodium", "N/A");
                String cholesterol = serving.optString("cholesterol", "N/A");
                String fiber = serving.optString("fiber", "N/A");
                String sugar = serving.optString("sugar", "N/A");

                runOnUiThread(() -> {
                    servingSizeView.setText("Food: " + name + "\nServing Size: " + servingSize);
                    fatsView.setText("Fat: " + fat + "g");
                    cholView.setText("Cholesterol: " + cholesterol + "mg");
                    sodiumView.setText("Calories: " + calories + "\nSodium: " + sodium + "mg");
                    carbsView.setText("Carbs: " + carbs + "g\nFiber: " + fiber + "g\nSugar: " + sugar + "g");
                    proteinView.setText("Protein: " + protein + "g");
                    nutrientView.setText("Nutrition loaded");
                });

            } catch (Exception e) {
                Log.e("DETAIL_ERROR", "getFoodDetails failed", e);
                showError("Error loading food details");
            }
        }).start();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            servingSizeView.setText(message);
            fatsView.setText("");
            cholView.setText("");
            sodiumView.setText("");
            carbsView.setText("");
            proteinView.setText("");
            nutrientView.setText("");
        });
    }
}
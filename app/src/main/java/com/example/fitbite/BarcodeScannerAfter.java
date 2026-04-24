package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.util.ArrayList;

public class BarcodeScannerAfter extends AppCompatActivity {

    private TextView nameTextView, caloriesView, sodiumView, fatsView, carbsView;
    private ArrayList<String> moreInfoPage = new ArrayList<>();

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
        if (barcode != null && !barcode.trim().isEmpty()) {
            fetchFoodData(barcode.trim());
        } else {
            showError("No barcode found");
        }

        // Scan again
        Button barcodeScan = findViewById(R.id.scanAnotherButton);
        barcodeScan.setOnClickListener(v -> scanCode());

        // More info screen (you can expand later)
        Button moreInfo = findViewById(R.id.moreInfo);
        moreInfo.setOnClickListener(v -> {
            Intent i = new Intent(this, BarcodeMoreInfo.class);
            i.putStringArrayListExtra("data", moreInfoPage);
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

                String calories = serving.optString("calories", "N/A");
                String fat = serving.optString("fat", "N/A");
                String carbs = serving.optString("carbohydrate", "N/A");
                String sodium = serving.optString("sodium", "N/A");

                moreInfoPage.add(serving.optString("serving_description", "N/A"));
                moreInfoPage.add(serving.optString("cholesterol", "N/A"));
                moreInfoPage.add(serving.optString("fiber", "N/A"));
                moreInfoPage.add(serving.optString("sugar", "N/A"));
                moreInfoPage.add(serving.optString("protein", "N/A"));


                runOnUiThread(() -> {
                    nameTextView.setText(name);
                    caloriesView.setText(calories);
                    carbsView.setText(carbs + "g");
                    fatsView.setText(fat + "g");
                    sodiumView.setText(sodium + "g");
                });

            } catch (Exception e) {
                Log.e("DETAIL_ERROR", "getFoodDetails failed", e);
                showError("Error loading food details");
            }
        }).start();
    }

    private void showError(String message) {
        runOnUiThread(() -> {
            nameTextView.setText("");
            fatsView.setText("");
            sodiumView.setText("");
            carbsView.setText("");
            caloriesView.setText("");
            /*
            servingSizeView.setText(message);
            cholView.setText("");
            proteinView.setText("");
            nutrientView.setText("");
            */
        });
    }

    private void scanCode() {
        Intent intent = new Intent(this, BarcodeScanner.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}

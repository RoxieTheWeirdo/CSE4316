package com.example.fitbite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class AutoMealPlan extends AppCompatActivity {

    // --- UI Components ---
    private RecyclerView weekMealsRecyclerView;
    private Button btnGenerate;

    // Rows: Min, Max, None
    private EditText etProtMin, etProtMax, etCarbMin, etCarbMax,
            etFatMin, etFatMax, etFibMin, etFibMax, etSugMin, etSugMax, etCalsMin, etCalsMax;
    private CheckBox cbProtNone, cbCarbNone, cbFatNone, cbFibNone, cbSugNone, cbCalsNone;

    // --- Data & ML Variables ---
    private List<FoodForML> foodList;
    private MealAdapter mealAdapter;
    private FirebaseFirestore db;
    private String userId;

    // --- User Biological Data (Fetched from Firebase) ---
    private double userWeightKg = 70.0;
    private double userHeightCm = 170.0;
    private int userAge = 25;
    private String userSex = "Male";

    // --- SCALER STATISTICS (For Normalization/ML) ---
    private double meanCals, devCals, meanProt, devProt, meanCarb, devCarb,
            meanFat, devFat, meanFib, devFib, meanSug, devSug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.automealplan);

        // 1. Initialize Firebase
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) userId = user.getUid();

        // 2. Initialize Views
        initViews();

        // 3. Load Dataset & "Train" Scaler
        try {
            foodList = loadFoodCSV();
            trainStandardScaler();
        } catch (IOException e) {
            Toast.makeText(this, "Error loading CSV", Toast.LENGTH_SHORT).show();
        }

        // 4. Get User Profile for Biological Target
        fetchUserProfile();

        // 5. Setup List
        mealAdapter = new MealAdapter(this, new ArrayList<>());
        weekMealsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        weekMealsRecyclerView.setAdapter(mealAdapter);

        // 6. Execution
        btnGenerate.setOnClickListener(v -> runConstrainedML());
    }

    private void initViews() {
        weekMealsRecyclerView = findViewById(R.id.weekMealsRecyclerView);
        btnGenerate = findViewById(R.id.btnGenerateMeal);

        etProtMin = findViewById(R.id.etProteinMin); etProtMax = findViewById(R.id.etProteinMax);
        etCarbMin = findViewById(R.id.etCarbsMin);   etCarbMax = findViewById(R.id.etCarbsMax);
        etFatMin  = findViewById(R.id.etFatMin);     etFatMax  = findViewById(R.id.etFatMax);
        etFibMin  = findViewById(R.id.etFiberMin);   etFibMax  = findViewById(R.id.etFiberMax);
        etSugMin  = findViewById(R.id.etSugarMin);   etSugMax  = findViewById(R.id.etSugarMax);
        etCalsMin = findViewById(R.id.etCalsMin);    etCalsMax = findViewById(R.id.etCalsMax);

        cbCalsNone = findViewById(R.id.cbCalsNone);
        cbProtNone = findViewById(R.id.cbProteinNone);
        cbCarbNone = findViewById(R.id.cbCarbsNone);
        cbFatNone  = findViewById(R.id.cbFatNone);
        cbFibNone  = findViewById(R.id.cbFiberNone);
        cbSugNone  = findViewById(R.id.cbSugarNone);
    }

    // --- THE CORE ML ENGINE ---
    private void runConstrainedML() {
        // A. Parse Hard Constraints (User enters Daily Totals)
        double dailyMinProt = parse(etProtMin), dailyMaxProt = parse(etProtMax);
        double dailyMinCarb = parse(etCarbMin), dailyMaxCarb = parse(etCarbMax);
        double dailyMinFat  = parse(etFatMin),  dailyMaxFat  = parse(etFatMax);
        double dailyMinFib  = parse(etFibMin),  dailyMaxFib  = parse(etFibMax);
        double dailyMinSug  = parse(etSugMin),  dailyMaxSug  = parse(etSugMax);
        double dailyMinCals = parse(etCalsMin), dailyMaxCals = parse(etCalsMax);

        // B. Apply Hard Filters (Divide targets by 3 to get Per-Meal limits)
        List<FoodForML> validFoods = new ArrayList<>();
        for (FoodForML f : foodList) {
            // We check if the individual food fits into 1/3rd of the daily budget
            if (!isAllowed(f.protein,  dailyMinProt/3.0, dailyMaxProt/3.0, cbProtNone)) continue;
            if (!isAllowed(f.carbs,    dailyMinCarb/3.0, dailyMaxCarb/3.0, cbCarbNone)) continue;
            if (!isAllowed(f.fat,      dailyMinFat/3.0,  dailyMaxFat/3.0,  cbFatNone))  continue;
            if (!isAllowed(f.fiber,    dailyMinFib/3.0,  dailyMaxFib/3.0,  cbFibNone))  continue;
            if (!isAllowed(f.sugar,    dailyMinSug/3.0,  dailyMaxSug/3.0,  cbSugNone))  continue;
            if (!isAllowed(f.calories, dailyMinCals/3.0, dailyMaxCals/3.0, cbCalsNone)) continue;

            validFoods.add(f);
        }

        if (validFoods.size() < 3) {
            Toast.makeText(this, "No foods are small enough to fit that limit!", Toast.LENGTH_LONG).show();
            return;
        }

        // C. Calculate Target Vector (Biological Ideal)
        double bmr = (10 * userWeightKg) + (6.25 * userHeightCm) - (5 * userAge);
        bmr += (userSex.equalsIgnoreCase("Male")) ? 5 : -161;
        double targetCalsPerMeal = (bmr * 1.2) / 3.0;

        // ML Target Point
        double zTargetCals = (targetCalsPerMeal - meanCals) / devCals;
        double zTargetProt = (((targetCalsPerMeal * 0.3) / 4) - meanProt) / devProt;
        double zTargetCarb = (((targetCalsPerMeal * 0.4) / 4) - meanCarb) / devCarb;
        double zTargetFat  = (((targetCalsPerMeal * 0.3) / 9) - meanFat)  / devFat;

        // D. KNN Ranking
        List<FoodScore> rankedFoods = new ArrayList<>();
        for (FoodForML f : validFoods) {
            double fCals = (f.calories - meanCals) / devCals;
            double fProt = (f.protein - meanProt) / devProt;
            double fCarb = (f.carbs - meanCarb) / devCarb;
            double fFat  = (f.fat - meanFat) / devFat;

            double distance = Math.sqrt(
                    Math.pow(fCals - zTargetCals, 2) +
                            Math.pow(fProt - zTargetProt, 2) +
                            Math.pow(fCarb - zTargetCarb, 2) +
                            Math.pow(fFat - zTargetFat, 2)
            );
            rankedFoods.add(new FoodScore(f, distance));
        }

        rankedFoods.sort(Comparator.comparingDouble(fs -> fs.distance));
        generateWeekPlan(rankedFoods);
    }

    // --- HELPER: Hard Filter Logic ---
    private boolean isAllowed(double val, double min, double max, CheckBox cbNone) {
        // 1. Hard Rule: If "None" is checked, value MUST be 0
        if (cbNone.isChecked()) {
            return val <= 0.0;
        }

        // 2. If user typed 0 in Max box, also treat as "None"
        if (max == 0) {
            return val <= 0.0;
        }

        // 3. Min Check: Only filter if min is NOT -1 and NOT 0
        // (We use > 0 because a min of 0 is the same as no restriction)
        if (min > 0 && val < min) {
            return false;
        }

        // 4. Max Check: Only filter if max is NOT -1
        // (Since we already handled max == 0 above, we only check for positive limits)
        if (max > 0 && val > max) {
            return false;
        }

        return true;
    }

    private double parse(EditText et) {
        String s = et.getText().toString();
        if (s.isEmpty() || s.equals("-")) return -1;
        try { return Double.parseDouble(s); } catch (Exception e) { return -1; }
    }

    // --- DATA LOADING & PRE-PROCESSING ---
    private void trainStandardScaler() {
        if (foodList.isEmpty()) return;
        meanCals = foodList.stream().mapToDouble(f -> f.calories).average().orElse(0);
        meanProt = foodList.stream().mapToDouble(f -> f.protein).average().orElse(0);
        meanCarb = foodList.stream().mapToDouble(f -> f.carbs).average().orElse(0);
        meanFat  = foodList.stream().mapToDouble(f -> f.fat).average().orElse(0);
        meanFib  = foodList.stream().mapToDouble(f -> f.fiber).average().orElse(0);
        meanSug  = foodList.stream().mapToDouble(f -> f.sugar).average().orElse(0);

        devCals = getStdDev(foodList.stream().mapToDouble(f -> f.calories).toArray(), meanCals);
        devProt = getStdDev(foodList.stream().mapToDouble(f -> f.protein).toArray(), meanProt);
        devCarb = getStdDev(foodList.stream().mapToDouble(f -> f.carbs).toArray(), meanCarb);
        devFat  = getStdDev(foodList.stream().mapToDouble(f -> f.fat).toArray(), meanFat);
        devFib  = getStdDev(foodList.stream().mapToDouble(f -> f.fiber).toArray(), meanFib);
        devSug  = getStdDev(foodList.stream().mapToDouble(f -> f.sugar).toArray(), meanSug);
    }

    private double getStdDev(double[] data, double mean) {
        double sum = 0;
        for (double d : data) sum += Math.pow(d - mean, 2);
        return Math.sqrt(sum / data.length);
    }

    private List<FoodForML> loadFoodCSV() throws IOException {
        List<FoodForML> foods = new ArrayList<>();
        InputStream is = getAssets().open("newfood.csv");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        reader.readLine(); // skip header
        String line;
        while ((line = reader.readLine()) != null) {
            String[] t = line.split(",");
            if (t.length < 8) continue;
            foods.add(new FoodForML(t[0], Double.parseDouble(t[2]), Double.parseDouble(t[3]),
                    Double.parseDouble(t[4]), Double.parseDouble(t[5]), Double.parseDouble(t[6]),
                    Double.parseDouble(t[7]), 3.0));
        }
        reader.close();
        return foods;
    }

    // --- FIREBASE INTEGRATION ---
    private void fetchUserProfile() {
        if (userId == null) return;
        db.collection("users").document(userId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long w = document.getLong("weightInPounds");
                        if (w != null) this.userWeightKg = w * 0.453592;
                        Long h = document.getLong("heightInCm");
                        if (h != null) this.userHeightCm = h;
                        Long by = document.getLong("birthYear");
                        if (by != null) this.userAge = Calendar.getInstance().get(Calendar.YEAR) - by.intValue();
                        String s = document.getString("sex");
                        if (s != null) this.userSex = s;
                    }
                });
    }

    // --- OUTPUT ---
    private void generateWeekPlan(List<FoodScore> rankedFoods) {
        List<Meal> meals = new ArrayList<>();
        String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        int poolSize = Math.min(25, rankedFoods.size());

        for (int i = 0; i < 7; i++) {
            List<FoodScore> pool = new ArrayList<>(rankedFoods.subList(0, poolSize));
            Collections.shuffle(pool);

            FoodForML b = pool.get(0).food;
            FoodForML l = pool.get(1).food;
            FoodForML d = pool.get(2).food;

            String desc = "Breakfast: " + b.name + "\nLunch: " + l.name + "\nDinner: " + d.name;
            int total = (int)(b.calories + l.calories + d.calories);
            meals.add(new Meal(days[i], desc, "ML Balanced Plan", total));
        }
        mealAdapter.updateMeals(meals);
    }

    // --- HELPER CLASSES ---
    private static class FoodScore {
        FoodForML food;
        double distance;
        FoodScore(FoodForML f, double d) { this.food = f; this.distance = d; }
    }
}
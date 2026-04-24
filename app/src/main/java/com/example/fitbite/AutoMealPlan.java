package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.*;
import java.util.*;

public class AutoMealPlan extends AppCompatActivity {

    private Button btnGenerate;

    private EditText etProtMin, etProtMax, etCarbMin, etCarbMax,
            etFatMin, etFatMax, etFibMin, etFibMax, etSugMin, etSugMax, etCalsMin, etCalsMax;

    private CheckBox cbProtNone, cbCarbNone, cbFatNone, cbFibNone, cbSugNone, cbCalsNone;

    private List<FoodForML> foodList;
    private FirebaseFirestore db;
    private String userId;

    private double meanCals, devCals, meanProt, devProt, meanCarb, devCarb,
            meanFat, devFat, meanFib, devFib, meanSug, devSug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.automealplan);

        db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) userId = user.getUid();

        initViews();

        try {
            foodList = loadFoodCSV();
            trainStandardScaler();
        } catch (IOException e) {
            Toast.makeText(this, "Error loading food data", Toast.LENGTH_SHORT).show();
            foodList = new ArrayList<>();
        }

        btnGenerate.setOnClickListener(v -> runConstrainedML());
    }

    private void initViews() {
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

    private void runConstrainedML() {

        if (foodList == null || foodList.isEmpty()) {
            Toast.makeText(this, "Food data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        double dailyMinProt = parse(etProtMin), dailyMaxProt = parse(etProtMax);
        double dailyMinCarb = parse(etCarbMin), dailyMaxCarb = parse(etCarbMax);
        double dailyMinFat  = parse(etFatMin),  dailyMaxFat  = parse(etFatMax);
        double dailyMinFib  = parse(etFibMin),  dailyMaxFib  = parse(etFibMax);
        double dailyMinSug  = parse(etSugMin),  dailyMaxSug  = parse(etSugMax);
        double dailyMinCals = parse(etCalsMin), dailyMaxCals = parse(etCalsMax);

        List<FoodForML> validFoods = new ArrayList<>();

        for (FoodForML f : foodList) {
            if (!isAllowed(f.protein,  dailyMinProt/3.0, dailyMaxProt/3.0, cbProtNone)) continue;
            if (!isAllowed(f.carbs,    dailyMinCarb/3.0, dailyMaxCarb/3.0, cbCarbNone)) continue;
            if (!isAllowed(f.fat,      dailyMinFat/3.0,  dailyMaxFat/3.0,  cbFatNone))  continue;
            if (!isAllowed(f.fiber,    dailyMinFib/3.0,  dailyMaxFib/3.0,  cbFibNone))  continue;
            if (!isAllowed(f.sugar,    dailyMinSug/3.0,  dailyMaxSug/3.0,  cbSugNone))  continue;
            if (!isAllowed(f.calories, dailyMinCals/3.0, dailyMaxCals/3.0, cbCalsNone)) continue;

            validFoods.add(f);
        }

        if (validFoods.size() < 4) {
            Toast.makeText(this, "Not enough foods match constraints", Toast.LENGTH_LONG).show();
            return;
        }

        //Use real calories from previous screen
        int calorieTarget = getIntent().getIntExtra("CALORIE_TARGET", 2000);
        double targetPerMeal = calorieTarget / 3.0;

        double zTargetCals = (targetPerMeal - meanCals) / devCals;
        double zTargetProt = (((targetPerMeal * 0.3)/4) - meanProt) / devProt;
        double zTargetCarb = (((targetPerMeal * 0.4)/4) - meanCarb) / devCarb;
        double zTargetFat  = (((targetPerMeal * 0.3)/9) - meanFat) / devFat;

        List<FoodScore> rankedFoods = new ArrayList<>();

        for (FoodForML f : validFoods) {
            double fCals = (f.calories - meanCals) / devCals;
            double fProt = (f.protein  - meanProt) / devProt;
            double fCarb = (f.carbs    - meanCarb) / devCarb;
            double fFat  = (f.fat      - meanFat)  / devFat;

            double distance = Math.sqrt(
                    Math.pow(fCals - zTargetCals, 2) +
                            Math.pow(fProt - zTargetProt, 2) +
                            Math.pow(fCarb - zTargetCarb, 2) +
                            Math.pow(fFat  - zTargetFat,  2)
            );

            rankedFoods.add(new FoodScore(f, distance));
        }

        rankedFoods.sort(Comparator.comparingDouble(fs -> fs.distance));

        generateWeekPlan(rankedFoods);
    }

    private void generateWeekPlan(List<FoodScore> rankedFoods) {

        List<Meal> meals = new ArrayList<>();

        int poolSize = Math.min(30, rankedFoods.size());

        for (int i = 0; i < 7; i++) {

            List<FoodScore> pool = new ArrayList<>(rankedFoods.subList(0, poolSize));
            Collections.shuffle(pool);

            Set<String> used = new HashSet<>();
            List<FoodForML> picks = new ArrayList<>();

            for (FoodScore fs : pool) {
                if (!used.contains(fs.food.name)) {
                    picks.add(fs.food);
                    used.add(fs.food.name);
                }
                if (picks.size() == 4) break;
            }

            FoodForML b = picks.get(0);
            FoodForML l = picks.get(1);
            FoodForML s = picks.get(2);
            FoodForML d = picks.get(3);

            meals.add(new Meal(
                    b.name,
                    l.name,
                    s.name,
                    d.name,
                    (int)(b.calories + l.calories + s.calories + d.calories)
            ));
        }

        //Send to Next screen
        Intent intent = new Intent(AutoMealPlan.this, MealPlanActivity.class);
        intent.putExtra("mealList", new ArrayList<>(meals));
        startActivity(intent);
    }

    private boolean isAllowed(double val, double min, double max, CheckBox cbNone) {
        if (cbNone == null) {
            // No checkbox in UI means treat as normal constraint
        } else if (cbNone.isChecked()) {
            return val <= 0.0;
        }

        if (min > 0 && val < min) return false;
        if (max > 0 && val > max) return false;
        return true;
    }

    private double parse(EditText et) {
        if (et == null) return -1;

        String s = et.getText().toString().trim();
        if (s.isEmpty()) return -1;

        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return -1;
        }
    }

    private void trainStandardScaler() {
        meanCals = foodList.stream().mapToDouble(f -> f.calories).average().orElse(0);
        meanProt = foodList.stream().mapToDouble(f -> f.protein).average().orElse(0);
        meanCarb = foodList.stream().mapToDouble(f -> f.carbs).average().orElse(0);
        meanFat  = foodList.stream().mapToDouble(f -> f.fat).average().orElse(0);

        devCals = getStd(foodList.stream().mapToDouble(f -> f.calories).toArray(), meanCals);
        devProt = getStd(foodList.stream().mapToDouble(f -> f.protein).toArray(), meanProt);
        devCarb = getStd(foodList.stream().mapToDouble(f -> f.carbs).toArray(), meanCarb);
        devFat  = getStd(foodList.stream().mapToDouble(f -> f.fat).toArray(), meanFat);
    }

    private double getStd(double[] data, double mean) {
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

            foods.add(new FoodForML(
                    t[0],
                    Double.parseDouble(t[2]),
                    Double.parseDouble(t[3]),
                    Double.parseDouble(t[4]),
                    Double.parseDouble(t[5]),
                    Double.parseDouble(t[6]),
                    Double.parseDouble(t[7]),
                    3.0
            ));
        }

        return foods;
    }

    private static class FoodScore {
        FoodForML food;
        double distance;

        FoodScore(FoodForML f, double d) {
            food = f;
            distance = d;
        }
    }
}
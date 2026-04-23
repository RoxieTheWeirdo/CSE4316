package com.example.fitbite;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MealPlanActivity extends AppCompatActivity {

    private List<Meal> mealList = new ArrayList<>();
    private View loadingOverlay;
    private Button btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_meal_day);

        loadingOverlay = findViewById(R.id.loadingOverlay);
        btnGenerate = findViewById(R.id.btnRegenerate);

        List<Meal> intentMeals = getMealListFromIntent();
        if (intentMeals != null && intentMeals.size() >= 7) {
            mealList = intentMeals;
            populateWeek();
        } else {
            loadSavedPlan();
        }

        btnGenerate.setOnClickListener(v -> fetchAndGenerate());
    }

    private void fetchAndGenerate() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showError("Not signed in", "Please sign in and try again.");
            return;
        }

        setLoading(true);

        FirebaseFirestore.getInstance()
                .collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc ->
                        user.getIdToken(false)
                                .addOnSuccessListener(result -> callApi(doc, result.getToken()))
                                .addOnFailureListener(e -> showError("Auth error", e.getMessage())))
                .addOnFailureListener(e -> showError("Could not load profile", e.getMessage()));
    }

    private void callApi(DocumentSnapshot doc, String idToken) {
        MealPlannerApi.PlanRequest request = buildRequest(doc);
        final double dailyTarget = request.daily_calories;

        MealPlannerApi.generatePlan(idToken, request, new MealPlannerApi.Callback() {
            @Override
            public void onSuccess(List<Meal> meals) {
                savePlanToFirestore(meals, dailyTarget);
                runOnUiThread(() -> {
                    mealList = meals;
                    if (mealList.size() >= 7) populateWeek();
                    setLoading(false);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showError("Could not generate plan", message));
            }
        });
    }

    @SuppressWarnings({"unchecked", "deprecation"})
    private List<Meal> getMealListFromIntent() {
        Object data = getIntent().getSerializableExtra("mealList");
        return (data instanceof List<?>) ? (List<Meal>) data : null;
    }

    private void loadSavedPlan() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        setLoading(true);

        FirebaseFirestore.getInstance()
                .collection("meal_plans").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Meal> loaded = parseMealsFromDoc(doc);
                        if (loaded.size() >= 7) {
                            mealList = loaded;
                            runOnUiThread(() -> { populateWeek(); setLoading(false); });
                            return;
                        }
                    }
                    runOnUiThread(() -> setLoading(false));
                })
                .addOnFailureListener(e -> {
                    Log.e("MealPlanActivity", "Failed to load saved plan", e);
                    runOnUiThread(() -> setLoading(false));
                });
    }

    @SuppressWarnings("unchecked")
    private List<Meal> parseMealsFromDoc(DocumentSnapshot doc) {
        List<Meal> meals = new ArrayList<>();
        Object raw = doc.get("meals");
        if (!(raw instanceof List)) return meals;

        for (Object item : (List<?>) raw) {
            if (!(item instanceof Map)) continue;
            Map<?, ?> m = (Map<?, ?>) item;

            Meal meal = new Meal(
                    strVal(m, "breakfast"), strVal(m, "lunch"),
                    strVal(m, "snack"),     strVal(m, "dinner"),
                    intVal(m, "calories")
            );
            meal.setDay(intVal(m, "day"));
            meal.setProtein(dblVal(m, "protein"));
            meal.setCarbs(dblVal(m, "carbs"));
            meal.setFat(dblVal(m, "fat"));
            meal.setBreakfastMacros(dblVal(m, "bProtein"), dblVal(m, "bCarbs"), dblVal(m, "bFat"));
            meal.setLunchMacros(    dblVal(m, "lProtein"), dblVal(m, "lCarbs"), dblVal(m, "lFat"));
            meal.setDinnerMacros(   dblVal(m, "dProtein"), dblVal(m, "dCarbs"), dblVal(m, "dFat"));
            meals.add(meal);
        }

        Collections.sort(meals, (a, b) -> Integer.compare(a.getDay(), b.getDay()));
        return meals;
    }

    private String strVal(Map<?, ?> m, String key) {
        Object v = m.get(key); return v != null ? v.toString() : "—";
    }
    private int intVal(Map<?, ?> m, String key) {
        Object v = m.get(key);
        if (v instanceof Long)   return ((Long) v).intValue();
        if (v instanceof Double) return ((Double) v).intValue();
        return 0;
    }
    private double dblVal(Map<?, ?> m, String key) {
        Object v = m.get(key);
        if (v instanceof Double) return (Double) v;
        if (v instanceof Long)   return ((Long) v).doubleValue();
        return 0.0;
    }

    private void savePlanToFirestore(List<Meal> meals, double dailyTarget) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        List<Map<String, Object>> mealsData = new ArrayList<>();
        for (Meal meal : meals) {
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day",       meal.getDay());
            dayMap.put("breakfast", meal.getBreakfast());
            dayMap.put("lunch",     meal.getLunch());
            dayMap.put("dinner",    meal.getDinner());
            dayMap.put("snack",     meal.getSnack());
            dayMap.put("calories",  meal.getCalories());
            dayMap.put("protein",   meal.getProtein());
            dayMap.put("carbs",     meal.getCarbs());
            dayMap.put("fat",       meal.getFat());
            // Per-slot macros so card lines survive a reload
            dayMap.put("bProtein",  meal.getBreakfastProtein());
            dayMap.put("bCarbs",    meal.getBreakfastCarbs());
            dayMap.put("bFat",      meal.getBreakfastFat());
            dayMap.put("lProtein",  meal.getLunchProtein());
            dayMap.put("lCarbs",    meal.getLunchCarbs());
            dayMap.put("lFat",      meal.getLunchFat());
            dayMap.put("dProtein",  meal.getDinnerProtein());
            dayMap.put("dCarbs",    meal.getDinnerCarbs());
            dayMap.put("dFat",      meal.getDinnerFat());
            mealsData.add(dayMap);
        }

        Map<String, Object> planDoc = new HashMap<>();
        planDoc.put("generated_at", FieldValue.serverTimestamp());
        planDoc.put("daily_target", dailyTarget);
        planDoc.put("meals",        mealsData);

        FirebaseFirestore.getInstance()
                .collection("meal_plans")
                .document(user.getUid())
                .set(planDoc)
                .addOnFailureListener(e -> Log.e("MealPlanActivity", "Firestore save failed", e));
    }

    private MealPlannerApi.PlanRequest buildRequest(DocumentSnapshot doc) {
        int age = doc.getLong("age") != null ? doc.getLong("age").intValue() : 25;

        String sex = doc.getString("sex");
        String gender = sex != null ? sex.toLowerCase() : "male";

        Double heightCm = doc.getDouble("heightInCm");
        double height = heightCm != null ? heightCm : 170.0;

        Double weightLbs = doc.getDouble("weightInPounds");
        double weightKg = weightLbs != null ? weightLbs * 0.453592 : 70.0;

        String exerciseLevel = doc.getString("exerciseLevel");
        String activityLevel = mapExerciseLevel(exerciseLevel);

        Double goalCal = doc.getDouble("goalCalories");
        if (goalCal == null) {
            Long goalCalLong = doc.getLong("goalCalories");
            goalCal = goalCalLong != null ? goalCalLong.doubleValue() : 2000.0;
        }

        return new MealPlannerApi.PlanRequest(
                age, gender, height, weightKg,
                activityLevel, "omnivore", goalCal, "none", 7
        );
    }

    private String mapExerciseLevel(String stored) {
        if (stored == null) return "sedentary";
        switch (stored) {
            case "1-3 sessions/week": return "lightly active";
            case "4-6 sessions/week": return "moderately active";
            case "7+ sessions/week":  return "very active";
            default:                  return "sedentary";
        }
    }

    private void setLoading(boolean loading) {
        if (loadingOverlay != null)
            loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (btnGenerate != null) {
            btnGenerate.setEnabled(!loading);
            btnGenerate.setText(loading ? "Generating..." : "Generate Meal Plan");
        }
    }

    private void showError(String title, String detail) {
        runOnUiThread(() -> {
            setLoading(false);
            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(detail != null ? detail : title)
                    .setPositiveButton("Try Again", (d, w) -> fetchAndGenerate())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void populateWeek() {
        bindDay(0, R.id.tvMonBreakfast, R.id.tvMonBreakfastMacros,
                   R.id.tvMonLunch,    R.id.tvMonLunchMacros,
                   R.id.tvMonSnack,
                   R.id.tvMonDinner,   R.id.tvMonDinnerMacros);
        bindDay(1, R.id.tvTueBreakfast, R.id.tvTueBreakfastMacros,
                   R.id.tvTueLunch,    R.id.tvTueLunchMacros,
                   R.id.tvTueSnack,
                   R.id.tvTueDinner,   R.id.tvTueDinnerMacros);
        bindDay(2, R.id.tvWedBreakfast, R.id.tvWedBreakfastMacros,
                   R.id.tvWedLunch,    R.id.tvWedLunchMacros,
                   R.id.tvWedSnack,
                   R.id.tvWedDinner,   R.id.tvWedDinnerMacros);
        bindDay(3, R.id.tvThuBreakfast, R.id.tvThuBreakfastMacros,
                   R.id.tvThuLunch,    R.id.tvThuLunchMacros,
                   R.id.tvThuSnack,
                   R.id.tvThuDinner,   R.id.tvThuDinnerMacros);
        bindDay(4, R.id.tvFriBreakfast, R.id.tvFriBreakfastMacros,
                   R.id.tvFriLunch,    R.id.tvFriLunchMacros,
                   R.id.tvFriSnack,
                   R.id.tvFriDinner,   R.id.tvFriDinnerMacros);
        bindDay(5, R.id.tvSatBreakfast, R.id.tvSatBreakfastMacros,
                   R.id.tvSatLunch,    R.id.tvSatLunchMacros,
                   R.id.tvSatSnack,
                   R.id.tvSatDinner,   R.id.tvSatDinnerMacros);
        bindDay(6, R.id.tvSunBreakfast, R.id.tvSunBreakfastMacros,
                   R.id.tvSunLunch,    R.id.tvSunLunchMacros,
                   R.id.tvSunSnack,
                   R.id.tvSunDinner,   R.id.tvSunDinnerMacros);
    }

    private void bindDay(int index,
                         int bId, int bMacId,
                         int lId, int lMacId,
                         int sId,
                         int dId, int dMacId) {
        Meal meal = mealList.get(index);
        setText(bId, meal.getBreakfast());
        setText(lId, meal.getLunch());
        setText(sId, meal.getSnack());
        setText(dId, meal.getDinner());
        setText(bMacId, formatMacros(meal.getBreakfastProtein(), meal.getBreakfastCarbs(), meal.getBreakfastFat()));
        setText(lMacId, formatMacros(meal.getLunchProtein(),     meal.getLunchCarbs(),     meal.getLunchFat()));
        setText(dMacId, formatMacros(meal.getDinnerProtein(),    meal.getDinnerCarbs(),    meal.getDinnerFat()));
    }

    private String formatMacros(double protein, double carbs, double fat) {
        return String.format("P: %dg  C: %dg  F: %dg",
                Math.round(protein), Math.round(carbs), Math.round(fat));
    }

    private void setText(int viewId, String value) {
        TextView tv = findViewById(viewId);
        if (tv != null) tv.setText(value != null ? value : "—");
    }
}

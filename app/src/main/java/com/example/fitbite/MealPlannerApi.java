package com.example.fitbite;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MealPlannerApi {
                                        //API url here
    private static final String BASE_URL = "xxxxxxxx";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public interface Callback {
        void onSuccess(List<Meal> meals);
        void onError(String message);
    }

    public static class PlanRequest {
        int age;
        String gender;
        double height_cm;
        double weight_kg;
        String activity_level;
        String dietary_preference;
        double daily_calories;
        String health_condition;
        int days;

        public PlanRequest(int age, String gender, double height_cm, double weight_kg,
                           String activity_level, String dietary_preference,
                           double daily_calories, String health_condition, int days) {
            this.age = age;
            this.gender = gender;
            this.height_cm = height_cm;
            this.weight_kg = weight_kg;
            this.activity_level = activity_level;
            this.dietary_preference = dietary_preference;
            this.daily_calories = daily_calories;
            this.health_condition = health_condition;
            this.days = days;
        }
    }

    private static class PlanResponse {
        String status;
        @SerializedName("daily_target") double dailyTarget;
        List<PlanItem> plan;
    }

    private static class PlanItem {
        int day;
        String slot;
        String meal;
        double calories;
        double protein;
        double carbs;
        double fat;
    }

    public static void generatePlan(String idToken, PlanRequest request, Callback callback) {
        RequestBody body = RequestBody.create(gson.toJson(request), JSON);

        Request httpRequest = new Request.Builder()
                .url(BASE_URL + "/generate-plan")
                .addHeader("Authorization", "Bearer " + idToken)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(httpRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError("Server returned " + response.code());
                    return;
                }
                try {
                    PlanResponse parsed = gson.fromJson(responseBody, PlanResponse.class);
                    if (parsed.plan == null || parsed.plan.isEmpty()) {
                        callback.onError("No meal plan in response");
                        return;
                    }
                    callback.onSuccess(buildMealList(parsed.plan));
                } catch (Exception e) {
                    callback.onError("Failed to parse response");
                }
            }
        });
    }

    private static List<Meal> buildMealList(List<PlanItem> items) {
        int maxDay = 0;
        for (PlanItem item : items) {
            if (item.day > maxDay) maxDay = item.day;
        }

        String[] breakfasts = new String[maxDay + 1];
        String[] lunches    = new String[maxDay + 1];
        String[] snacks     = new String[maxDay + 1];
        String[] dinners    = new String[maxDay + 1];
        double[] cals       = new double[maxDay + 1];
        double[] proteins   = new double[maxDay + 1];
        double[] carbs      = new double[maxDay + 1];
        double[] fats       = new double[maxDay + 1];

        // Per-slot macros for card display
        double[] bP = new double[maxDay + 1], bC = new double[maxDay + 1], bF = new double[maxDay + 1];
        double[] lP = new double[maxDay + 1], lC = new double[maxDay + 1], lF = new double[maxDay + 1];
        double[] dP = new double[maxDay + 1], dC = new double[maxDay + 1], dF = new double[maxDay + 1];

        for (PlanItem item : items) {
            int d = item.day;
            cals[d]     += item.calories;
            proteins[d] += item.protein;
            carbs[d]    += item.carbs;
            fats[d]     += item.fat;
            switch (item.slot.toLowerCase()) {
                case "breakfast":
                    breakfasts[d] = item.meal;
                    bP[d] = item.protein; bC[d] = item.carbs; bF[d] = item.fat;
                    break;
                case "lunch":
                    lunches[d] = item.meal;
                    lP[d] = item.protein; lC[d] = item.carbs; lF[d] = item.fat;
                    break;
                case "snack":
                    snacks[d] = item.meal;
                    break;
                case "dinner":
                    dinners[d] = item.meal;
                    dP[d] = item.protein; dC[d] = item.carbs; dF[d] = item.fat;
                    break;
            }
        }

        List<Meal> meals = new ArrayList<>();
        for (int d = 1; d <= maxDay; d++) {
            Meal meal = new Meal(
                    safe(breakfasts[d]),
                    safe(lunches[d]),
                    safe(snacks[d]),
                    safe(dinners[d]),
                    (int) Math.round(cals[d])
            );
            meal.setProtein(proteins[d]);
            meal.setCarbs(carbs[d]);
            meal.setFat(fats[d]);
            meal.setDay(d);
            meal.setBreakfastMacros(bP[d], bC[d], bF[d]);
            meal.setLunchMacros(lP[d], lC[d], lF[d]);
            meal.setDinnerMacros(dP[d], dC[d], dF[d]);
            meals.add(meal);
        }
        return meals;
    }

    private static String safe(String s) {
        return s != null ? s : "—";
    }
}

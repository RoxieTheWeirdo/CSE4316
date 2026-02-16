package com.example.fitbite.network;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ProxyClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/proxy/fatsecret?url=";
    private final OkHttpClient client = new OkHttpClient();

    // Generic helper: send any FatSecret URL through the proxy
    private String callFatsecret(String fatsecretUrl) throws Exception {
        String encodedUrl = URLEncoder.encode(fatsecretUrl, "UTF-8");

        Request request = new Request.Builder()
                .url(BASE_URL + encodedUrl)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            return body.string();
        }
    }

    // Text search (you already had this)
    public String searchFood(String query) throws Exception {
        String fatsecretUrl =
                "https://platform.fatsecret.com/rest/server.api" +
                        "?method=foods.search" +
                        "&search_expression=" + query +
                        "&format=json";

        return callFatsecret(fatsecretUrl);
    }

    // 🔹 NEW: get food_id from barcode
    public String getFoodIdForBarcode(String barcode) throws Exception {
        String fatsecretUrl =
                "https://platform.fatsecret.com/rest/server.api" +
                        "?method=food.find_id_for_barcode" +
                        "&barcode=" + barcode +
                        "&format=json";

        return callFatsecret(fatsecretUrl);
    }

    // 🔹 NEW: get full nutrition details for a food_id
    public String getFoodDetails(String foodId) throws Exception {
        String fatsecretUrl =
                "https://platform.fatsecret.com/rest/server.api" +
                        "?method=food.get.v2" +
                        "&food_id=" + foodId +
                        "&format=json";

        return callFatsecret(fatsecretUrl);
    }
}

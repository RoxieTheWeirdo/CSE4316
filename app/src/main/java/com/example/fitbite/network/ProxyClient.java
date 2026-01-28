package com.example.fitbite.network;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;

public class ProxyClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/proxy/fatsecret?url=";
    private final OkHttpClient client = new OkHttpClient();

    public String searchFood(String query) throws Exception {

        String fatsecretUrl =
                "https://platform.fatsecret.com/rest/server.api?method=foods.search&search_expression="
                        + query +
                        "&format=json";

        String encodedUrl = URLEncoder.encode(fatsecretUrl, "UTF-8");

        Request request = new Request.Builder()
                .url(BASE_URL + encodedUrl)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}

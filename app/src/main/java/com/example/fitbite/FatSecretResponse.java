package com.example.fitbite;

import java.util.List;

public class FatSecretResponse {
    public Foods foods;

    public static class Foods {
        public List<Food> food;
    }

    public static class Food {
        public String food_id;
        public String food_name;
        public String food_description;
        public String food_type;
        public String brand_name;  // sometimes null
    }
}

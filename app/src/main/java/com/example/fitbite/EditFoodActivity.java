package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class EditFoodActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editfoodscreen);

        Button backButton = findViewById(R.id.saveButton);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditFoodActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

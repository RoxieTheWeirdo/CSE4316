package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class CreateAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Automatically start the setup flow (first screen)
        Intent intent = new Intent(CreateAccount.this, activity_basics_sex.class);
        startActivity(intent);
        finish();
    }
}

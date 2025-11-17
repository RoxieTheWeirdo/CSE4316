package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class activity_basics_birthday extends AppCompatActivity {

    private static final String TAG = "BasicsBirthdayActivity";
    private NumberPicker monthPicker, dayPicker, yearPicker;
    private Button btnNext;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_birthday);

        monthPicker = findViewById(R.id.monthPicker);
        dayPicker = findViewById(R.id.dayPicker);
        yearPicker = findViewById(R.id.yearPicker);
        btnNext = findViewById(R.id.btnNext);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setDisplayedValues(months);
        monthPicker.setValue(5);

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);
        dayPicker.setValue(15);

        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(2010);
        yearPicker.setValue(1995);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnNext.setOnClickListener(v -> saveBirthdayAndProceed());
    }

    private void saveBirthdayAndProceed() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        String[] months = monthPicker.getDisplayedValues();
        String month = months[monthPicker.getValue()];
        int day = dayPicker.getValue();
        int year = yearPicker.getValue();

        // Create a map to hold the birthday data
        Map<String, Object> userBirthday = new HashMap<>();
        userBirthday.put("birthMonth", month);
        userBirthday.put("birthDay", day);
        userBirthday.put("birthYear", year);

        // Save the data to Firestore, merging with existing data
        db.collection("users").document(userId)
                .set(userBirthday, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Birthday successfully written to Firestore.");
                    Toast.makeText(activity_basics_birthday.this, "Birthday saved!", Toast.LENGTH_SHORT).show();

                    // Proceed to the next activity
                    Intent intent = new Intent(activity_basics_birthday.this, activity_basics_height.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing birthday document", e);
                    Toast.makeText(activity_basics_birthday.this, "Error saving birthday.", Toast.LENGTH_SHORT).show();
                });
    }
}

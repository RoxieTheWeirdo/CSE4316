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
import android.icu.util.Calendar;

public class SettingChangeBirthday extends AppCompatActivity {

    private static final String TAG = "SettingsChangeBirthday";
    private NumberPicker monthPicker, dayPicker, yearPicker;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_changebirthday);

        monthPicker = findViewById(R.id.monthPicker);
        dayPicker = findViewById(R.id.dayPicker);
        yearPicker = findViewById(R.id.yearPicker);
        btnSave = findViewById(R.id.btnNext); // Reuse Next button as Save

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        setupPickers();
        btnSave.setOnClickListener(v -> saveBirthday());
    }

    private void setupPickers() {
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(months.length - 1);
        monthPicker.setDisplayedValues(months);

        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(31);

        yearPicker.setMinValue(1950);
        yearPicker.setMaxValue(2010);

        // Optional: prefill with current user data
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(snap -> {
                if (snap.exists()) {
                    String month = snap.getString("birthMonth");
                    Long day = snap.getLong("birthDay");
                    Long year = snap.getLong("birthYear");
                    if (month != null && day != null && year != null) {
                        for (int i = 0; i < months.length; i++) {
                            if (months[i].equals(month)) {
                                monthPicker.setValue(i);
                                break;
                            }
                        }
                        dayPicker.setValue(day.intValue());
                        yearPicker.setValue(year.intValue());
                    }
                }
            });
        }
    }

    private void saveBirthday() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] months = monthPicker.getDisplayedValues();
        String month = months[monthPicker.getValue()];
        int day = dayPicker.getValue();
        int year = yearPicker.getValue();

        Map<String, Object> birthdayData = new HashMap<>();
        birthdayData.put("birthMonth", month);
        birthdayData.put("birthDay", day);
        birthdayData.put("birthYear", year);

        SettingEditPersonalCalculateAge(month, day, year, db, user.getUid());

        db.collection("users").document(user.getUid())
                .set(birthdayData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Birthday updated!", Toast.LENGTH_SHORT).show();

                    // Return result to previous activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("birthMonth", month);
                    resultIntent.putExtra("birthDay", day);
                    resultIntent.putExtra("birthYear", year);
                    setResult(RESULT_OK, resultIntent);

                    finish(); // Close this activity
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving birthday", e);
                    Toast.makeText(this, "Failed to update birthday.", Toast.LENGTH_SHORT).show();
                });
    }

    private void SettingEditPersonalCalculateAge(String month, int day, int year, FirebaseFirestore db, String userId) {
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - year;

        int monthInt = 0;
        switch (month) {
            case "Jan": monthInt = 1; break;
            case "Feb": monthInt = 2; break;
            case "Mar": monthInt = 3; break;
            case "Apr": monthInt = 4; break;
            case "May": monthInt = 5; break;
            case "Jun": monthInt = 6; break;
            case "Jul": monthInt = 7; break;
            case "Aug": monthInt = 8; break;
            case "Sep": monthInt = 9; break;
            case "Oct": monthInt = 10; break;
            case "Nov": monthInt = 11; break;
            case "Dec": monthInt = 12; break;
            default:
                Log.w(TAG, "Invalid month for age calculation!");
                return;
        }

        if (today.get(Calendar.MONTH) + 1 < monthInt ||
                (today.get(Calendar.MONTH) + 1 == monthInt && today.get(Calendar.DAY_OF_MONTH) < day)) {
            age--;
        }

        Map<String, Object> ageMap = new HashMap<>();
        ageMap.put("age", age);
        db.collection("users").document(userId)
                .set(ageMap, SetOptions.merge());
    }
}

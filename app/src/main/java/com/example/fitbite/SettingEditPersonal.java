package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingEditPersonal extends AppCompatActivity {

    TextView curBirthday, curHeight, curWeight, curSex, curExercise;
    Button btnChangeBirthday, btnChangeHeight, btnChangeWeight, btnChangeSex, btnChangeExercise;
    LocalSettings localSettings;
    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings2);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        localSettings = new LocalSettings(this);
        // ----------------------------
        // FIND VIEWS
        // ----------------------------
        curBirthday = findViewById(R.id.curBirthday);
        curHeight   = findViewById(R.id.curHeight);
        curWeight   = findViewById(R.id.curWeight);
        curSex      = findViewById(R.id.curSex);
        curExercise = findViewById(R.id.curExercise);

        btnChangeBirthday = findViewById(R.id.btnChangeBirthday);
        btnChangeHeight   = findViewById(R.id.btnChangeHeight);
        btnChangeWeight   = findViewById(R.id.btnChangeWeight);
        btnChangeSex      = findViewById(R.id.btnChangeSex);
        btnChangeExercise = findViewById(R.id.btnChangeExercise);
        if (user != null) {
            DocumentReference ref =
                    db.collection("users").document(user.getUid());

            ref.addSnapshotListener((snap, e) -> {
                if (e != null || snap == null || !snap.exists()) return;

                // Birthday
                Long day = snap.getLong("birthDay");
                String month = snap.getString("birthMonth");
                Long year = snap.getLong("birthYear");
                if (day != null && month != null && year != null) {
                    curBirthday.setText(String.format("%s %02d, %04d", month, day, year));
                } else {
                    curBirthday.setText("Not Set");
                }

                // Height & Weight
                Long heightCm = snap.getLong("heightInCm");
                Long weightLbs = snap.getLong("weightInPounds");

                String unitPref = localSettings.getUnitPreference(); // "Metric" or "Imperial"

                if (heightCm != null) {
                    if (unitPref.equals("Metric")) {
                        curHeight.setText(heightCm + " cm");
                    } else {
                        double totalInches = heightCm / 2.54;
                        int feet = (int) (totalInches / 12);
                        int inches = (int) Math.round(totalInches % 12);

                        // Prevent 12 inches from appearing
                        if (inches == 12) {
                            feet += 1;
                            inches = 0;
                        }

                        curHeight.setText(feet + " ft " + inches + " in");
                    }
                } else {
                    curHeight.setText("Not set");
                }

                if (weightLbs != null) {
                    if (unitPref.equals("Metric")) {
                        double kg = weightLbs * 0.453592;
                        curWeight.setText(String.format("%.1f kg", kg));
                    } else {
                        curWeight.setText(weightLbs + " lbs");
                    }
                } else {
                    curWeight.setText("Not Set");
                }

                curSex.setText(snap.getString("sex") != null ? snap.getString("sex") : "Not Set");
                curExercise.setText(snap.getString("exerciseLevel") != null ? snap.getString("exerciseLevel") : "Not Set");
            });
        }

        btnChangeBirthday.setOnClickListener(v -> {
            Intent intent = new Intent(SettingEditPersonal.this, SettingChangeBirthday.class);
            startActivity(intent);
        });
        btnChangeHeight.setOnClickListener(v -> {
            Intent intent = new Intent(SettingEditPersonal.this, SettingChangeHeight.class);
            startActivity(intent);
        });
        btnChangeWeight.setOnClickListener(v -> {
            Intent intent = new Intent(SettingEditPersonal.this, SettingChangeWeight.class);
            startActivity(intent);
        });
        btnChangeSex.setOnClickListener(v -> {
            Intent intent = new Intent(SettingEditPersonal.this, SettingChangeSex.class);
            startActivity(intent);
        });
        btnChangeExercise.setOnClickListener(v -> {
            Intent intent = new Intent(SettingEditPersonal.this, SettingChangeExercise.class);
            startActivity(intent);
        });
    }
}

package com.example.fitbite;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SettingChangeSex extends AppCompatActivity {

    private CardView cardFemale, cardMale;
    private RadioButton radioFemale, radioMale;
    private Button btnSave;
    private String selectedSex = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_changesex);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cardFemale = findViewById(R.id.cardFemale);
        cardMale = findViewById(R.id.cardMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioMale = findViewById(R.id.radioMale);
        btnSave = findViewById(R.id.btnNext);

        btnSave.setEnabled(false);

        cardFemale.setOnClickListener(v -> selectSex("Female"));
        cardMale.setOnClickListener(v -> selectSex("Male"));
        radioFemale.setOnClickListener(v -> selectSex("Female"));
        radioMale.setOnClickListener(v -> selectSex("Male"));

        btnSave.setOnClickListener(v -> saveSex());
    }

    private void selectSex(String sex) {
        selectedSex = sex;
        radioFemale.setChecked(sex.equals("Female"));
        radioMale.setChecked(sex.equals("Male"));

        btnSave.setEnabled(true);
        btnSave.setBackgroundResource(R.drawable.button_enabled_background);
        btnSave.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void saveSex() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSex.isEmpty()) {
            Toast.makeText(this, "Please select an option.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sex", selectedSex);

        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Sex updated!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to SettingEditPersonal
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save selection.", Toast.LENGTH_SHORT).show();
                });
    }
}

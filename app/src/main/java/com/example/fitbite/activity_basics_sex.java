package com.example.fitbite;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class activity_basics_sex extends AppCompatActivity {

    private static final String TAG = "BasicsSexActivity";
    private CardView cardFemale, cardMale;
    private RadioButton radioFemale, radioMale;
    private Button btnNext;
    private String selectedSex = "";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basics_sex);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cardFemale = findViewById(R.id.cardFemale);
        cardMale = findViewById(R.id.cardMale);
        radioFemale = findViewById(R.id.radioFemale);
        radioMale = findViewById(R.id.radioMale);
        btnNext = findViewById(R.id.btnNext);

        btnNext.setEnabled(false);

        cardFemale.setOnClickListener(v -> selectSex("Female"));
        cardMale.setOnClickListener(v -> selectSex("Male"));
        radioFemale.setOnClickListener(v -> selectSex("Female"));
        radioMale.setOnClickListener(v -> selectSex("Male"));

        btnNext.setOnClickListener(v -> saveSexAndProceed());
    }

    private void saveSexAndProceed() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in to save your data.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSex.isEmpty()) {
            Toast.makeText(this, "Please select an option.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("sex", selectedSex);

        db.collection("users").document(userId)
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Sex successfully written!");
                    Intent intent = new Intent(activity_basics_sex.this, activity_basics_birthday.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing sex", e);
                    Toast.makeText(activity_basics_sex.this, "Failed to save selection.", Toast.LENGTH_SHORT).show();
                });
    }

    private void selectSex(String sex) {
        selectedSex = sex;
        radioFemale.setChecked(sex.equals("Female"));
        radioMale.setChecked(sex.equals("Male"));
        btnNext.setEnabled(true);
        btnNext.setBackgroundResource(R.drawable.button_enabled_background);
        btnNext.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }
}

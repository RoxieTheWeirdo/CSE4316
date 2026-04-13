package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingChangeAllergies extends AppCompatActivity {

    MaterialButton btnDairy, btnGluten, btnPeanut;
    Button btnDone;

    FirebaseUser user;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_changeallergies);

        // Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Views
        btnDairy  = findViewById(R.id.btnDairy);
        btnGluten = findViewById(R.id.btnGluten);
        btnPeanut = findViewById(R.id.btnPeanut);
        btnDone   = findViewById(R.id.btnDone);

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ----------------------------
        // Load existing allergies
        // ----------------------------
        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String allergyString = doc.getString("allergies");
                    if (allergyString == null || allergyString.isEmpty()) return;

                    List<String> allergies = Arrays.asList(allergyString.split(",\\s*"));

                    btnDairy.setChecked(allergies.contains("Dairy"));
                    btnGluten.setChecked(allergies.contains("Gluten"));
                    btnPeanut.setChecked(allergies.contains("Peanut"));
                });

        // ----------------------------
        // Save on Done
        // ----------------------------
        btnDone.setOnClickListener(v -> {
            ArrayList<String> allergies = new ArrayList<>();

            if (btnDairy.isChecked())  allergies.add("Dairy");
            if (btnGluten.isChecked()) allergies.add("Gluten");
            if (btnPeanut.isChecked()) allergies.add("Peanut");

            String allergyString = String.join(", ", allergies);

            db.collection("users")
                    .document(user.getUid())
                    .update("allergies", allergyString)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Allergies updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save allergies", Toast.LENGTH_SHORT).show()
                    );
        });
    }
}

package com.example.fitbite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsOverview extends AppCompatActivity {

    LinearLayout tabAccount, tabPersonal, tabApp;
    TextView tabAccountText, tabPersonalText, tabAppText;
    FrameLayout contentContainer;
    ImageButton btnEditFields;
    String currentTab = "account"; // keep track of active tab
    LocalSettings localSettings;
    FirebaseUser user;
    FirebaseFirestore db;
    @Override
    protected void onResume() {
        super.onResume();
        if (currentTab.equals("app")) {
            showTab("app"); // re-render the app tab and refresh theme text
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_overview);
        localSettings = new LocalSettings(this);
        // Top tabs
        tabAccount = findViewById(R.id.tab_account);
        tabPersonal = findViewById(R.id.tab_personal);
        tabApp = findViewById(R.id.tab_app);

        tabAccountText = findViewById(R.id.tabAccountText);
        tabPersonalText = findViewById(R.id.tabPersonalText);
        tabAppText = findViewById(R.id.tabAppText);

        // Content container
        contentContainer = findViewById(R.id.contentContainer);

        // Pencil icon
        btnEditFields = findViewById(R.id.btnEditFields);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Default tab on load
        showTab("account");

        // Tab click listeners
        tabAccount.setOnClickListener(v -> showTab("account"));
        tabPersonal.setOnClickListener(v -> showTab("personal"));
        tabApp.setOnClickListener(v -> showTab("app"));

        // Pencil edit click listener
        btnEditFields.setOnClickListener(v -> handleEditClick());
    }

    private void showTab(String tab) {
        currentTab = tab; // update active tab
        contentContainer.removeAllViews();

        // Reset tab colors
        tabAccountText.setTextColor(Color.parseColor("#6B6F75"));
        tabPersonalText.setTextColor(Color.parseColor("#6B6F75"));
        tabAppText.setTextColor(Color.parseColor("#6B6F75"));

        View view = null;
        switch (tab) {
            case "account":
                view = getLayoutInflater().inflate(R.layout.settings_accountpage, contentContainer, false);
                tabAccountText.setTextColor(Color.parseColor("#4DA6FF"));
                // Optional: Load current username/email in this content
                TextView usernameView = view.findViewById(R.id.curUsername);
                TextView emailView = view.findViewById(R.id.curEmail);

                if (user != null) {
                    emailView.setText(user.getEmail());
                    DocumentReference userDoc = db.collection("users").document(user.getUid());
                    userDoc.addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        if (snapshot != null && snapshot.exists()) {
                            String username = snapshot.getString("username");
                            usernameView.setText(username != null ? username : "Unknown");
                        }
                    });
                }
                break;

            case "personal":
                view = getLayoutInflater().inflate(R.layout.settings_personalpage, contentContainer, false);
                tabPersonalText.setTextColor(Color.parseColor("#4DA6FF"));

                TextView ageView = view.findViewById(R.id.age);
                TextView birthdayView = view.findViewById(R.id.personalBirthday);

                if (user != null) {
                    DocumentReference userDoc = db.collection("users").document(user.getUid());
                    View finalView = view;
                    userDoc.addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        if (snapshot != null && snapshot.exists()) {
                            // Birthday & Age
                            Long day = snapshot.getLong("birthDay");
                            String month = snapshot.getString("birthMonth");
                            Long year = snapshot.getLong("birthYear");
                            if (day != null && month != null && year != null) {
                                birthdayView.setText(String.format("%s %02d, %04d", month, day, year));
                                // Optional: calculate age if stored
                                Long age = snapshot.getLong("age");
                                ageView.setText(age != null ? String.valueOf(age) : "-");
                            } else {
                                birthdayView.setText("Not set");
                                ageView.setText("-");
                            }

                            // Height & Weight
                            TextView heightView = finalView.findViewById(R.id.personalHeight);
                            TextView weightView = finalView.findViewById(R.id.personalWeight);

                            Long heightCm = snapshot.getLong("heightInCm");
                            Long weightLbs = snapshot.getLong("weightInPounds");
                            String unitPref = localSettings.getUnitPreference(); // "Metric" or "Imperial"

                            if (heightCm != null) {
                                if (unitPref.equals("Metric")) {
                                    heightView.setText(heightCm + " cm");
                                } else {
                                    double totalInches = heightCm / 2.54;
                                    int feet = (int) (totalInches / 12);
                                    int inches = (int) Math.round(totalInches % 12);

                                    // Prevent 12 inches from appearing
                                    if (inches == 12) {
                                        feet += 1;
                                        inches = 0;
                                    }

                                    heightView.setText(feet + " ft " + inches + " in");
                                }
                            } else {
                                heightView.setText("Not set");
                            }

                            if (weightLbs != null) {
                                if (unitPref.equals("Metric")) {
                                    double kg = weightLbs * 0.453592;
                                    weightView.setText(String.format("%.1f kg", kg));
                                } else {
                                    weightView.setText(weightLbs + " lbs");
                                }
                            } else {
                                weightView.setText("Not set");
                            }
                            TextView sexView = finalView.findViewById(R.id.sex);
                            String sex = snapshot.getString("sex");
                            if (sex != null && !sex.isEmpty()) {
                                sexView.setText(sex);
                            }
                            else {
                                sexView.setText("Not set");
                            }
                            TextView exerciseView = finalView.findViewById(R.id.exerciselevel);
                            String exercise = snapshot.getString("exerciseLevel");
                            if (exercise != null && !exercise.isEmpty()) {
                                exerciseView.setText(exercise);
                            }
                            else {
                                exerciseView.setText("Not set");
                            }
                        }
                    });
                }
                break;

            case "app":
                view = getLayoutInflater().inflate(R.layout.settings_apppage, contentContainer, false);
                tabAppText.setTextColor(Color.parseColor("#4DA6FF"));

                // NEW CODE — Update the displayed theme
                TextView appThemeStatus = view.findViewById(R.id.appThemeStatus);
                LocalSettings localSettings = new LocalSettings(this);

                String currentTheme = localSettings.getTheme();

                switch (currentTheme) {
                    case "Light":
                        appThemeStatus.setText("Light");
                        break;

                    case "Dark":
                        appThemeStatus.setText("Dark");
                        break;

                    default:
                        appThemeStatus.setText("System Default");
                        break;
                }
                TextView unitStatus = view.findViewById(R.id.appUnitStatus);
                String currentUnit = localSettings.getUnitPreference();
                if (currentUnit.equals("Metric")) {
                    unitStatus.setText("Metric");
                } else {
                    unitStatus.setText("Imperial");
                }
                break;
        }
        if (view != null) contentContainer.addView(view);
    }

    private void handleEditClick() {
        // Determine which tab is active and launch the appropriate edit dialog
        switch (currentTab) {
            case "account":
                startActivity(new Intent(this, SettingEditAccount.class));
                break;
            case "personal":
                startActivity(new Intent(this, SettingEditPersonal.class));
                break;
            case "app":
                startActivity(new Intent(this, SettingEditApp.class));
                break;
        }
    }
}
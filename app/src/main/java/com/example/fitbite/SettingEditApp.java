package com.example.fitbite;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
public class SettingEditApp extends AppCompatActivity {

    MaterialButtonToggleGroup toggleGroup;
    LocalSettings localSettings;
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings3);
        auth = FirebaseAuth.getInstance();
        localSettings = new LocalSettings(this);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // ===== App Theme =====
        MaterialButtonToggleGroup themeGroup = findViewById(R.id.themeToggleGroup);
        MaterialButton btnLight = findViewById(R.id.btnLight);
        MaterialButton btnDark = findViewById(R.id.btnDark);
        MaterialButton btnSystem = findViewById(R.id.btnSystem);

        // Preselect theme
        switch (localSettings.getTheme()) {
            case "Light": themeGroup.check(R.id.btnLight); break;
            case "Dark": themeGroup.check(R.id.btnDark); break;
            default: themeGroup.check(R.id.btnSystem); break;
        }

        themeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String selected = "System";
            if (checkedId == R.id.btnLight) selected = "Light";
            if (checkedId == R.id.btnDark) selected = "Dark";

            localSettings.setTheme(selected);

            switch (selected) {
                case "Light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
                case "Dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
            }
        });

        // ===== Unit Preference =====
        MaterialButtonToggleGroup unitGroup = findViewById(R.id.unitToggleGroup);
        MaterialButton btnMetric = findViewById(R.id.btnMetric);
        MaterialButton btnImperial = findViewById(R.id.btnImperial);

        if (localSettings.getUnitPreference().equals("Metric")) unitGroup.check(R.id.btnMetric);
        else unitGroup.check(R.id.btnImperial);

        unitGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String selected = (checkedId == R.id.btnMetric) ? "Metric" : "Imperial";
            localSettings.setUnitPreference(selected);
        });

        // ===== Notification Preference =====
        MaterialButtonToggleGroup notifGroup = findViewById(R.id.notificationToggleGroup);
        MaterialButton btnOff = findViewById(R.id.btnNotifyOff);
        MaterialButton btnMinimal = findViewById(R.id.btnNotifyMinimal);
        MaterialButton btnAll = findViewById(R.id.btnNotifyAll);

        switch (localSettings.getNotificationMode()) {
            case "Minimal": notifGroup.check(R.id.btnNotifyMinimal); break;
            case "All": notifGroup.check(R.id.btnNotifyAll); break;
            default: notifGroup.check(R.id.btnNotifyOff); break;
        }

        notifGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            String selected = "Off";
            if (checkedId == R.id.btnNotifyMinimal) selected = "Minimal";
            if (checkedId == R.id.btnNotifyAll) selected = "All";

            // Update Firestore
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .update("notificationPreference", selected);
            }

            // Notifications warning
            boolean systemEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
            boolean permissionGranted = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionGranted = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED;
            }
            if (!systemEnabled || !permissionGranted) {
                Toast.makeText(this,
                        "Enable notifications in FitBite's settings to show notifications!",
                        Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionGranted) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
                }
            }

            // Save locally
            localSettings.setNotificationMode(selected);
        });
    }
}

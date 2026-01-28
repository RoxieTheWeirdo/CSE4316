package com.example.fitbite;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;

public class SettingEditApp extends AppCompatActivity {

    MaterialButtonToggleGroup toggleGroup;
    LocalSettings localSettings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings3);

        localSettings = new LocalSettings(this);

        toggleGroup = findViewById(R.id.themeToggleGroup);
        MaterialButton btnLight = findViewById(R.id.btnLight);
        MaterialButton btnDark = findViewById(R.id.btnDark);
        MaterialButton btnSystem = findViewById(R.id.btnSystem);

        // Preselect current theme
        String currentTheme = localSettings.getTheme();
        switch (currentTheme) {
            case "Light":
                toggleGroup.check(R.id.btnLight);
                break;
            case "Dark":
                toggleGroup.check(R.id.btnDark);
                break;
            default:
                toggleGroup.check(R.id.btnSystem);
                break;
        }

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return; // only care about selection
            String selectedTheme = "System";
            if (checkedId == R.id.btnLight) selectedTheme = "Light";
            if (checkedId == R.id.btnDark) selectedTheme = "Dark";

            // Save locally
            localSettings.setTheme(selectedTheme);

            // Apply theme immediately
            switch (selectedTheme) {
                case "Light":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "Dark":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    break;
            }
        });
        MaterialButtonToggleGroup unitToggleGroup = findViewById(R.id.unitToggleGroup);
        MaterialButton btnMetric = findViewById(R.id.btnMetric);
        MaterialButton btnImperial = findViewById(R.id.btnImperial);

        String currentUnit = localSettings.getUnitPreference();
        if (currentUnit.equals("Metric")) {
            unitToggleGroup.check(R.id.btnMetric);
        } else {
            unitToggleGroup.check(R.id.btnImperial);
        }

        unitToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String selectedUnit = "Metric";
            if (checkedId == R.id.btnImperial) selectedUnit = "Imperial";

            localSettings.setUnitPreference(selectedUnit);
        });
        MaterialButtonToggleGroup notifGroup = findViewById(R.id.notificationToggleGroup);
        MaterialButton btnOff = findViewById(R.id.btnNotifyOff);
        MaterialButton btnMinimal = findViewById(R.id.btnNotifyMinimal);
        MaterialButton btnAll = findViewById(R.id.btnNotifyAll);

        String mode = localSettings.getNotificationMode();
        switch (mode) {
            case "Minimal": notifGroup.check(R.id.btnNotifyMinimal); break;
            case "All": notifGroup.check(R.id.btnNotifyAll); break;
            default: notifGroup.check(R.id.btnNotifyOff); break;
        }
        notifGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;

            String selected = "Off";
            if (checkedId == R.id.btnNotifyMinimal) selected = "Minimal";
            if (checkedId == R.id.btnNotifyAll) selected = "All";

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            boolean systemEnabled = manager.areNotificationsEnabled();
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

            // Save setting locally anyway
            localSettings.setNotificationMode(selected);
        });
    }
}

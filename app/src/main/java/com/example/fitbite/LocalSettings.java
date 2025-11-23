package com.example.fitbite;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalSettings {

    private static final String PREFS_NAME = "app_settings";
    private static final String KEY_THEME = "theme";
    private static final String UnitPreference = "UnitPreference";
    final private SharedPreferences prefs;

    public LocalSettings(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getTheme() {
        return prefs.getString(KEY_THEME, "System");
    }
    public String getUnitPreference() {
        return prefs.getString(UnitPreference, "System");
    }
    public void setTheme(String theme) {
        prefs.edit().putString(KEY_THEME, theme).apply();
    }
    public void setUnitPreference(String unit) { prefs.edit().putString(UnitPreference, unit).apply();
    }
}

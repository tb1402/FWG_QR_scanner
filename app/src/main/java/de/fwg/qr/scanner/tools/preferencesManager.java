package de.fwg.qr.scanner.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class to interact with the sharedPreferences
 */
public class preferencesManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public static String preferenceName = "FWG_QR";

    public preferencesManager(Context c) {
        preferences = c.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    public boolean isFirstRun() {
        if (preferences.contains("firstrun")) {
            return getBoolean("firstrun", false);
        }
        saveBoolean("firstrun", true);
        return true;
    }

    public int getDarkMode() {
        if (preferences.contains("darkmode")) {
            return Integer.parseInt(getString("darkmode", "0"));
        }
        saveString("darkmode", "0");
        return 0;
    }

    public void saveBoolean(String name, boolean data) {
        editor = preferences.edit();
        editor.putBoolean(name, data);
        editor.apply();
    }

    public boolean getBoolean(String name, boolean default_value) {
        return preferences.getBoolean(name, default_value);
    }

    public void saveInt(String name, int data) {
        editor = preferences.edit();
        editor.putInt(name, data);
        editor.apply();
    }

    public int getInt(String name, int default_value) {
        return preferences.getInt(name, default_value);
    }

    public String getString(String name, String defaultValue) {
        return preferences.getString(name, defaultValue);
    }

    public void saveString(String name, String data) {
        editor = preferences.edit();
        editor.putString(name, data);
        editor.apply();
    }

    public boolean contains(String name) {
        return preferences.contains(name);
    }

    public int getMode() {
        return Integer.parseInt(preferences.getString("mode", "1"));
    }

    public String getVideoResolution() {
        return preferences.getString("video_quality", "medium");
    }

    public String getImageResolution() {
        return preferences.getString("image_quality", "high");
    }
}

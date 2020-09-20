package de.fwg.qr.scanner.tools;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class to interact with the sharedPreferences
 */
public class preferencesManager {

    private static preferencesManager pm;//field for singleton
    public static String preferenceName = "FWG_QR";//name of the shared preferences file
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private preferencesManager(Context c) {
        preferences = c.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    /**
     * Method for getting instance of preferencesManager
     * @param c context
     * @return instance
     */
    public static preferencesManager getInstance(Context c){
        if(pm==null){
            pm=new preferencesManager(c);
        }
        return pm;
    }

    /**
     * Get reference to the SharedPreferences to retrieve values
     * @return instance
     */
    public SharedPreferences getPreferences(){
        return preferences;
    }

    /**
     * Method to check if application has its first run, needed for quick guide, agb etc.
     * @return isFirstRun?
     */
    public boolean isFirstRun() {
        if (preferences.contains("firstrun")) {
            return preferences.getBoolean("firstrun", false);
        }
        saveBoolean("firstrun", true);
        return true;
    }

    /**
     * Integer that corresponds to darkMode settings
     * 0=darkMode
     * 1=lightMode
     * 2=follow system settings
     * @return current setting
     */
    public int getDarkMode() {
        if (preferences.contains("darkmode")) {
            return Integer.parseInt(preferences.getString("darkmode", "0"));
        }
        saveString("darkmode", "0");
        return 0;
    }

    /**
     * Method to save a boolean value to the sharedPreferences
     * @param name name of the value
     * @param data value
     */
    public void saveBoolean(String name, boolean data) {
        editor = preferences.edit();
        editor.putBoolean(name, data);
        editor.apply();
    }

    /**
     * Save an integer to the preferences
     * @param name name of the value
     * @param data value
     */
    public void saveInt(String name, int data) {
        editor = preferences.edit();
        editor.putInt(name, data);
        editor.apply();
    }

    /**
     * Save a string to the preferences
     * @param name name of the value
     * @param data value
     */
    public void saveString(String name, String data) {
        editor = preferences.edit();
        editor.putString(name, data);
        editor.apply();
    }

    /**
     * Method to delete a key from the preferences
     * @param name name of the key
     */
    public void deleteValue(String name){
        editor=preferences.edit();
        editor.remove(name);
        editor.apply();
    }

    /**
     * get current mode setting
     * @return mode (true=rallye, false=info)
     */
    public boolean isRallyeMode() {
        return Integer.parseInt(preferences.getString("mode", "1")) == 0;
    }

    /**
     * get video resolution setting
     * @return resolution
     */
    public String getVideoResolution() {
        return preferences.getString("video_quality", "medium");
    }

    /**
     * get image resolution setting
     * @return resolution
     */
    public String getImageResolution() {
        return preferences.getString("image_quality", "high");
    }

    /**
     * get unlocked features boolean
     * @return features unlocked?
     */
    public boolean areFeaturesUnlocked(){
        return preferences.getBoolean("unlocked",false);
    }
}

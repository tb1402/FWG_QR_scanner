package de.fwg.qr.scanner.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class preferencesManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    public static String preferenceName="FWG_QR";

    public preferencesManager(Context c){
        preferences=c.getSharedPreferences(preferenceName,Context.MODE_PRIVATE);
    }
    public boolean isFirstRun(){
        if(preferences.contains("firstrun")){
            return false;
        }
        saveBoolean("firstrun",true);
        return true;
    }
    public void saveBoolean(String name, boolean data){
        editor=preferences.edit();
        editor.putBoolean(name,data);
        editor.apply();
    }
    public boolean getBoolean(String name, boolean default_value){
        return preferences.getBoolean(name,default_value);
    }
    public void saveInt(String name, int data){
        editor=preferences.edit();
        editor.putInt(name,data);
        editor.apply();
    }
    public int getInt(String name, int default_value){
        return preferences.getInt(name,default_value);
    }
    public int getMode(){
        return Integer.parseInt(preferences.getString("mode","1"));
    }
}

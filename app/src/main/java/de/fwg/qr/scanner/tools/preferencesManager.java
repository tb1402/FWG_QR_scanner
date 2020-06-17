package de.fwg.qr.scanner.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class preferencesManager {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public preferencesManager(Context c){
        preferences=c.getSharedPreferences("FWG_QR",Context.MODE_PRIVATE);
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
}

package de.fwg.qr.scanner;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import de.fwg.qr.scanner.tools.preferencesManager;

public class fragmentSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager pm=getPreferenceManager();
        pm.setSharedPreferencesName(preferencesManager.preferenceName);
        setPreferencesFromResource(R.xml.setttings,rootKey);
    }
}

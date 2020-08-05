package de.fwg.qr.scanner;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import de.fwg.qr.scanner.tools.preferencesManager;

public class fragmentSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesName(preferencesManager.preferenceName);
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set up the restart prompt
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                PendingIntent pi = PendingIntent.getActivity(requireContext(), 0, new Intent(requireContext(), activityMain.class), PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager am = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
                if (am != null) {
                    am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);
                } else {//this is not really a restart, just a kind of dirty solution, but good enough for acting as a fail-safe
                    //although the alarmManager should never be null on a normal android os
                    Intent i = new Intent(requireActivity(), activityMain.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                requireActivity().recreate();
            }
        });
        final AlertDialog dialog = builder.create();

        // set listener to darkmode setting
        ListPreference lp = findPreference("darkmode");
        Preference.OnPreferenceChangeListener pcl = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                dialog.show();
                return true;
            }
        };
        assert lp != null;
        lp.setOnPreferenceChangeListener(pcl);
    }
}

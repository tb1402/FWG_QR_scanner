package de.fwg.qr.scanner;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import de.fwg.qr.scanner.history.historyManager;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * fragment for displaying settings
 */
public class fragmentSettings extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager pm = getPreferenceManager();
        pm.setSharedPreferencesName(preferencesManager.preferenceName);
        setPreferencesFromResource(R.xml.settings, rootKey);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListPreference mp = findPreference("mode");

        // set listener to darkmode setting
        ListPreference lp = findPreference("darkmode");
        Preference.OnPreferenceChangeListener pcl = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                showRestartDialog();
                return true;
            }
        };
        assert lp != null;
        lp.setOnPreferenceChangeListener(pcl);

        //ser listener for mode setting
        Preference.OnPreferenceChangeListener modeChangeListener=new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int newVal=Integer.parseInt((String)newValue);
                preferencesManager pm=preferencesManager.getInstance(requireContext());
                if(pm.areFeaturesUnlocked()){
                    if(!pm.isRallyeMode()&&newVal==0){
                        showHistoryDeletionWarningDialog();
                        return false;
                    }
                }
                else{
                    if(!pm.isRallyeMode()&&newVal==0){
                        Toast.makeText(requireContext(), getString(R.string.scan_teacher_code), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
                return true;
            }
        };
        assert mp != null;
        mp.setOnPreferenceChangeListener(modeChangeListener);
    }

    /**
     * Warning, that history will be deleted, shown, when changing from info to rally mode, while having a valid license
     */
    private void showHistoryDeletionWarningDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.dialog_del_warning_title))
                .setMessage(getString(R.string.dialog_del_warning_content))
                .setPositiveButton(getString(R.string.dialog_del_warning_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new historyManager(requireContext()).clearHistory();
                        preferencesManager.getInstance(requireContext()).saveString("mode",String.valueOf(0));
                        requireActivity().recreate();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_del_warning_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /**
     * Restart dialog, to prevent issues with darkmode
     */
    private void showRestartDialog(){
        //set up the restart prompt
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                .setMessage(R.string.restart_dialog_message).setTitle(R.string.restart_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                PendingIntent pi = PendingIntent.getActivity(requireContext(), 0, new Intent(requireContext(), activityMain.class), PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager am = (AlarmManager) requireActivity().getSystemService(Context.ALARM_SERVICE);
                if (am != null) {
                    am.set(AlarmManager.RTC, System.currentTimeMillis() + 100, pi);
                } else {
                    //this is not really a restart, just a kind of dirty solution, but good enough for acting as a fail-safe
                    //although the alarmManager should never be null on a normal android os
                    Intent i = new Intent(requireActivity(), activityMain.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            }
        })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                requireActivity().recreate();
            }
        });
        builder.create().show();
    }
}

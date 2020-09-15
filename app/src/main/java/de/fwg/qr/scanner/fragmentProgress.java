package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import de.fwg.qr.scanner.history.taskResultCallback;
import de.fwg.qr.scanner.progress.progressListAdapter;
import de.fwg.qr.scanner.progress.progressManager;
import de.fwg.qr.scanner.progress.visitedStation;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * fragment showing the progress
 */
public class fragmentProgress extends fragmentWrapper {

    public ListView listProgress;
    public ProgressBar barStationProgress;
    public TextView txtStationProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle sis) {
        //Visuelle Elemente
        listProgress = v.findViewById(R.id.progress_list_view);
        barStationProgress = v.findViewById(R.id.bar_station_progress);
        txtStationProgress = v.findViewById(R.id.txt_station_progress);

        if (preferencesManager.getInstance(c).areFeaturesUnlocked()) {
            final progressManager manager = new progressManager(c);
            manager.getProgressAsync(new taskResultCallback<visitedStation[]>() {
                @Override
                public void onFinished(visitedStation[] result) {
                    progressListAdapter adapter = new progressListAdapter(c, result);
                    listProgress.setAdapter(adapter);

                    barStationProgress.setMax(result.length);
                    barStationProgress.setProgress((int) (result.length * manager.OverallProgress));
                    txtStationProgress.setText((getString(R.string.txt_progress_name) + " : " + (int) (result.length * manager.OverallProgress) + " / " + result.length));

                }
            });
        }
        else{
            Toast.makeText(c,getString(R.string.scan_teacher_code),Toast.LENGTH_LONG).show();
        }


    }
}
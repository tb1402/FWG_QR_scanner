package de.fwg.qr.scanner;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import de.fwg.qr.scanner.progress.*;
import de.fwg.qr.scanner.history.*;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentProgress extends fragmentWrapper {

    public ListView listProgress;

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
        listProgress = (ListView)v.findViewById(R.id.progress_list_view);

        final View cont = v;

        // historyManager instance
        final historyManager manager = new historyManager(c);
        //lockUI(true);
        /* DEBUG: Sample Entries:
        manager.clearHistory();
        manager.addEntry(new historyEntry("dYjeGwGiIm"));
        manager.addEntry(new historyEntry("EQUgDFPunm"));
        manager.addEntry(new historyEntry("EsluEnKeHJ"));
        */
        manager.getVisitedStationsAsync(new taskResultCallback(){
            @Override
            public void onFinished(Object result) {
                final String[] visitedIds = (String[]) result;
                final ArrayList<String> visited;
                if(visitedIds.length == 0)
                    visited = new ArrayList<>();
                else
                    visited = new ArrayList<String>(Arrays.asList(visitedIds));

                // get all Stations by using a network request
                networkCallbackInterface webCllb = new networkCallbackInterface() {
                    @Override
                    public void onPostCallback(String operation, String response){
                        JSONArray datalists;
                        try {
                            datalists = new JSONArray(response);
                            JSONArray Ids, Names;

                            Ids = datalists.getJSONArray(0);
                            Names = datalists.getJSONArray(1);

                            // Convert the data into an Dictionary
                            Hashtable<String, String> stations = new Hashtable<String, String>();
                            for(int i = 0; i < Ids.length(); i++){
                                stations.put(Ids.getJSONArray(i).getString(0), Names.getJSONArray(i).getString(0));
                            }

                            // create the resulting visitedStations by comparing the Ids of visited stations
                            visitedStation[] stationprogress = new visitedStation[stations.size()];
                            ArrayList<String> keys = Collections.list(stations.keys());
                            int i = 0;
                            for(String key : keys){
                                boolean wasVisited = visited.contains(key); // check if the arraylist of visited stations contains the current iterated one
                                stationprogress[i] = new visitedStation(key, stations.get(key), wasVisited);
                                i++;
                            }

                            // Finished successfully!
                            progressListAdapter adapter = new progressListAdapter(c, stationprogress);
                            listProgress.setAdapter(adapter);
                            return;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onImageCallback(String name, Bitmap image) {}
                };
                net.makePostRequest(new WeakReference<networkCallbackInterface>(webCllb), "fetchIdAndName" , "");


            }
        });

    }
}
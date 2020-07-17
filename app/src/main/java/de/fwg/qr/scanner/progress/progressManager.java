package de.fwg.qr.scanner.progress;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.history.historyEntry;
import de.fwg.qr.scanner.history.historyManager;
import de.fwg.qr.scanner.history.taskResultCallback;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class progressManager {

    private Context context;
    public float OverallProgress;

    public progressManager(Context ctx) {
        context = ctx;
        OverallProgress = 0.0f;
    }



    /*
     * yields the Progress as a float between 0 and 1 indicating the percentage of overall progress
     * @param callback callback method provides the float as result

    public void getProgressAmoutAsync(final taskResultCallback callback){

    }*/

    /**
     * yields asynchronously a list of all Stations flagged whether they were visited or not
     * Writes the progress percentage to the public Property {@code OverallProgress}
     *
     * @param callback callback method provides visitedStation[] as result
     */
    public void getProgressAsync(final taskResultCallback callback) {
        // historyManager instance

        getUniqueStationsAsync(new taskResultCallback() {
            @Override
            public void onFinished(Object result) {
                final historyEntry[] visitedStations = (historyEntry[]) result;

                getStationsAsync(new taskResultCallback() {
                    @Override
                    public void onFinished(Object result) {

                        Hashtable<String, String> stations = (Hashtable<String, String>) result;

                        // save the visited Stations as a hastable
                        Hashtable<String, historyEntry> visitedStationsById = new Hashtable<>();
                        for (historyEntry visitedStation : visitedStations) {
                            visitedStationsById.put(visitedStation.StationId, visitedStation);
                        }

                        // create the resulting visitedStations by comparing the Ids of visited stations
                        visitedStation[] stationprogress = new visitedStation[stations.size()];
                        Set<String> hashtablekeys = stations.keySet();
                        int i = 0;
                        for (String key : hashtablekeys) { // iterate over all stationIds in the stations hashtable
                            if (visitedStationsById.containsKey(key)) { // station was visited once
                                stationprogress[i] = new visitedStation(key, stations.get(key), true, visitedStationsById.get(key).TimeVisited);
                            } else { // station not visited
                                stationprogress[i] = new visitedStation(key, stations.get(key), false);
                            }

                            i++;
                        }
                        OverallProgress = (float) visitedStations.length / (float) stationprogress.length;
                        callback.onFinished(stationprogress);
                    }
                });
            }
        });

    }

    /**
     * Method to get a list of unique stations and the latest time they were visited (asynchronously)
     *
     * @param callback provides a historyEntry[] on finish
     */
    private void getUniqueStationsAsync(final taskResultCallback callback) {
        final historyManager manager = new historyManager(context);

        manager.getEntriesAsync(new taskResultCallback() {
            @Override
            public void onFinished(Object result) {

                historyEntry[] entries = (historyEntry[]) result;
                // Array of the history, each entry providing stationId and visitedTime
                // => Task: sorting out duplicates and always save the one with the newest date

                // May be worth a rework:
                ArrayList<historyEntry> uniqueEntries = new ArrayList<>();
                for (int i = 0; i < entries.length; i++) {

                    // check if the entry already exists
                    boolean exists = false;
                    for (int j = 0; j < uniqueEntries.size(); j++) {

                        if (entries[i].StationId==uniqueEntries.get(j).StationId) {//todo normally this should be replaceable with equals, but it isnt, find out why
                            exists = true;
                            // check if the one wich was found is worse than the current iterator
                            // so it may have to be replaced
                            if (entries[i].TimeVisited.getTime() > uniqueEntries.get(j).TimeVisited.getTime()) {
                                // entries[i] is the newer date and will be replaced
                                historyEntry replacement = uniqueEntries.get(i);
                                replacement.TimeVisited = entries[i].TimeVisited;
                                uniqueEntries.set(j, replacement);
                                break;
                            }
                        }
                    }
                    if (!exists)
                        uniqueEntries.add(entries[i]);

                }
                callback.onFinished(uniqueEntries.toArray(new historyEntry[0]));

            }
        });

    }

    /**
     * Method to get all stations from the server
     *
     * @param callback callback method providing a hastable<string,string> as result
     */
    private void getStationsAsync(final taskResultCallback callback) {
        // get all Stations by using a network request
        networkCallbackInterface webCllb = new networkCallbackInterface() {
            @Override
            public void onPostCallback(String operation, String response) {
                JSONArray datalists;
                try {
                    datalists = new JSONArray(response);
                    JSONArray Ids, Names;

                    Ids = datalists.getJSONArray(0);
                    Names = datalists.getJSONArray(1);

                    // Convert the data into an Dictionary
                    Hashtable<String, String> stations = new Hashtable<>();
                    for (int i = 0; i < Ids.length(); i++) {
                        stations.put(Ids.getJSONArray(i).getString(0), Names.getJSONArray(i).getString(0));
                    }
                    callback.onFinished(stations);

                } catch (JSONException e) {
                    Intent i = new Intent(context, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                    context.startActivity(i);
                }
            }

            @Override
            public void onImageCallback(String name, Bitmap image) {
            }
        };
        network net = new network(context);
        net.makePostRequest(new WeakReference<>(webCllb), "fetchIdAndName", "");
    }


}

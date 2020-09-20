package de.fwg.qr.scanner.history;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

/**
 * Manager Class to get & add Entries of the Users History in an asynchronous as well as synchronous manner
 */
public class historyManager {

    /**
     * Constant that defines the amount of Entries after which the oldest entry gets deleted on insertion of a new one
     */
    public static final int MaxEntries = 50;
    public static boolean FileLocked;
    public ArrayList<historyEntry> Entries;
    private Context AppContext;

    /**
     * Minimalistic HistoryManager Constructor
     *
     * @param c ApplicationContext needed for the Apps' File Directory
     */
    public historyManager(Context c) {
        AppContext = c;
        historyManager.FileLocked = false;
        //Entries = new ArrayList<historyEntry>(Arrays.asList(getEntries()));
    }

    /**
     * synchronously get all entries of the Users history
     *
     * @return All Entries saved in the Users History
     */
    public historyEntry[] getEntries() {
        historyFileReadTask readTask = new historyFileReadTask(AppContext, getHistoryFile(), null);
        try {
            return readTask.execute().get();
        } catch (Exception e) {
            Intent i = new Intent(AppContext, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            AppContext.startActivity(i);
        }
        return new historyEntry[0];
    }

    /**
     * Asynchronously reads all entries of the Users history
     *
     * @param callback method called on finish providing an array of historyEntrys
     */
    public void getEntriesAsync(taskResultCallback<historyEntry[]> callback) {
        historyFileReadTask readTask = new historyFileReadTask(AppContext, getHistoryFile(), callback);
        readTask.execute();
    }


    /**
     * Synchronously adds an Entry to the Users History
     *
     * @param newEntry The Entry which should be inserted
     */
    public void addEntry(historyEntry newEntry) {
        historyEntry[] entries = getEntries();
        Entries = (entries.length == 0) ? new ArrayList<historyEntry>() : new ArrayList<>(Arrays.asList(entries));

        /*
        // Delete the first if the size exceeds the maximum define in the constant
        if(Entries.size() + 1 >= historyManager.MaxEntries){
            Entries.remove(0);
        }*/

        Entries.add(newEntry);
        historyFileWriteTask writeTask = new historyFileWriteTask(AppContext, getHistoryFile(), null);
        try {
            writeTask.execute(Entries.toArray(new historyEntry[0])).get();
        } catch (Exception e) {
            Intent i = new Intent(AppContext, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            AppContext.startActivity(i);
        }

    }

    /**
     * Asynchronously adds an Entry to the Users History. Caution: Wait for the callback before trying to read/write the locked file
     *
     * @param newEntry The Entry which should be inserted
     * @param callback Callback when the asynchronus Insertion is finished, Optional, wont throw exception when null
     */
    public void addEntryAsync(final historyEntry newEntry, final taskCallback callback) {

        historyFileReadTask readTask = new historyFileReadTask(AppContext, getHistoryFile(), new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] result) {
                if (result.length == 0)
                    Entries = new ArrayList<>();
                else
                    Entries = new ArrayList<>(Arrays.asList(result));

                /*
                // Delete the first if the size exceeds the maximum define in the constant
                if(Entries.size() + 1 >= historyManager.MaxEntries){
                    Entries.remove(0);
                }
                */


                Entries.add(newEntry);
                historyFileWriteTask writeTask = new historyFileWriteTask(AppContext, getHistoryFile(), new taskCallback() {
                    @Override
                    public void onFinished() {
                        if (callback != null)
                            callback.onFinished();
                    }
                });
                writeTask.execute(Entries.toArray(new historyEntry[0]));
            }
        });
        readTask.execute();
    }

    /**
     * Asynchronously fetches all Stations from the server and associates its names with the Entries Id's
     *
     * @param callback Callback Interface called on completion of the Asynchronous task (result of type {@code historyEntry[]})
     */
    public void getAssociatedEntriesAsync(final taskResultCallback<historyEntry[]> callback) {

        historyFileReadTask readTask = new historyFileReadTask(AppContext, getHistoryFile(), new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] result) {
                if (result.length == 0)
                    Entries = new ArrayList<>();
                else
                    Entries = new ArrayList<>(Arrays.asList(result));

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
                            Dictionary<String, String> stations = new Hashtable<>();
                            for (int i = 0; i < Ids.length(); i++) {
                                stations.put(Ids.getJSONArray(i).getString(0), Names.getJSONArray(i).getString(0));
                            }

                            // Iterate over all Entries and set their proper Station Names
                            historyEntry modified;
                            for (int i = 0; i < Entries.size(); i++) {
                                modified = Entries.get(i);
                                modified.StationName = stations.get(modified.StationId);
                                Entries.set(i, modified);
                            }

                            // Calling the callback method
                            if (callback != null)
                                callback.onFinished(Entries.toArray(new historyEntry[0]));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onImageCallback(String name, Bitmap image) {
                    }
                };

                // Calling the HTTP-Request to get the Stations Names
                network.getInstance(AppContext).makePostRequest(webCllb, "fetchIdAndName", "",AppContext);

            }
        });
        readTask.execute();
    }


    public void getAssociatedEntriesAsync(final taskResultCallback<historyEntry[]> callback, final int limit) {

        getAssociatedEntriesAsync(new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] allEntries) {
                if (limit > 0 && limit <= allEntries.length) {
                    // only return the last #limit entries
                    historyEntry[] latest = Arrays.copyOfRange(allEntries, (allEntries.length - limit), allEntries.length);
                    callback.onFinished(latest);
                } else {
                    // just return everything
                    callback.onFinished(allEntries);
                }
            }
        });

    }

    /**
     * Synchronously gets a list of all visited StationIds by using the history
     *
     * @return An Array of all StationIds which had been in the history at one point
     */
    public String[] getVisitedStations() {
        historyEntry[] allEntries = getEntries();
        // filtering out the stations visited multiple times
        HashSet<String> visitedIds = new HashSet<>();
        for (historyEntry allEntry : allEntries) {
            visitedIds.add(allEntry.StationId); // The HashSet prevents multiple Entries of the same type so each station,
            // if visited twice only shows up once
        }
        return visitedIds.toArray(new String[0]);
    }

    /**
     * Asynchronously gets a list of all visited StationIds
     *
     * @param callback callback Interface gets called on exexutionFinish and provides a string[] result
     */
    public void getVisitedStationsAsync(final taskResultCallback<String[]> callback) {
        historyFileReadTask readTask = new historyFileReadTask(AppContext, getHistoryFile(), new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] result) {

                HashSet<String> visitedIds = new HashSet<>();
                for (historyEntry entry : result) {
                    visitedIds.add(entry.StationId); // The HashSet prevents multiple Entries of the same type so each station,
                    // if visited twice only shows up once
                }
                callback.onFinished(visitedIds.toArray(new String[0]));
            }
        });
        readTask.execute();

    }


    /**
     * Synchronous Function to wipe all recorded History
     */
    public void clearHistory() {
        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(getHistoryFile());

            DataOutputStream dataStream = new DataOutputStream(fileStream);
            dataStream.writeInt(0); // write the amount of Entries in the file which is 0
        } catch (Exception ex) {
            // Dont care because if this is the case, there is no history because the file does not exist
        }
    }

    // Helper Method to obtain the File for the History
    public File getHistoryFile() {
        File dir = AppContext.getFilesDir();
        return new File(dir, "app.history");
    }
}

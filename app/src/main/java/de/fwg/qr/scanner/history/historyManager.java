package de.fwg.qr.scanner.history;

import android.content.Context;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Manager Class to get & add Entries of the Users History in an asynchronous as well as synchronous manner
 */
public class historyManager {

    private Context AppContext;
    public static boolean FileLocked;
    public ArrayList<historyEntry> Entries;

    /**
     * Minimalistic HistoryManager Constructor
     * @param c ApplicationContext needed for the Apps' File Directory
     */
    public historyManager(Context c){
        AppContext = c;
        historyManager.FileLocked = false;
        //Entries = new ArrayList<historyEntry>(Arrays.asList(getEntries()));
    }

    /**
     * synchronously get all entries of the Users history
     * @return All Entries saved in the Users History
     */
    public historyEntry[] getEntries(){
        historyFileReadTask readTask = new historyFileReadTask(getHistoryFile(), null);
        try {
            return readTask.execute().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new historyEntry[0];
    }


    /**
     * Synchronously adds an Entry to the Users History
     * @param newEntry The Entry which should be inserted
     */
    public void addEntry(historyEntry newEntry){

        historyEntry[] entries = getEntries();
        if(entries.length == 0)
            Entries = new ArrayList<historyEntry>();
        else
            Entries = new ArrayList<historyEntry>(Arrays.asList(entries));

        Entries.add(newEntry);
        historyFileWriteTask writeTask = new historyFileWriteTask(getHistoryFile(), null);
        try {
            writeTask.execute(Entries.toArray(new historyEntry[0])).get();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Asynchronously adds an Entry to the Users History. Caution: Wait for the callback before trying to read/write the locked file
     * @param newEntry The Entry which should be inserted
     * @param callback Callback when the asynchronus Insertion is finished, Optional, wont throw exception when null
     */
    public void addEntryAsync(final historyEntry newEntry, final taskCallback callback){

        historyFileReadTask readTask = new historyFileReadTask(getHistoryFile(), new taskResultCallback() {
            @Override
            public void onFinished(Object result) {
                historyEntry[] entries = getEntries();
                if(entries.length == 0)
                    Entries = new ArrayList<historyEntry>();
                else
                    Entries = new ArrayList<historyEntry>(Arrays.asList(entries));
                Entries.add(newEntry);

                historyFileWriteTask writeTask = new historyFileWriteTask(getHistoryFile(), new taskCallback() {
                    @Override
                    public void onFinished() {
                        if(callback != null)
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
     * @param callback Callback Interface called on completion of the Asynchronous task (result of type {@code historyEntry[]})
     */
    public void getAssociatedEntriesAsync(final taskResultCallback callback){

        historyFileReadTask readTask = new historyFileReadTask(getHistoryFile(), new taskResultCallback() {
            @Override
            public void onFinished(Object result) {
                historyEntry[] entries = (historyEntry[]) result;
                if(entries.length == 0)
                    Entries = new ArrayList<historyEntry>();
                else
                    Entries = new ArrayList<historyEntry>(Arrays.asList(entries));
                /*
                // Calling the HTTP-Request to get the Stations Names
                network net = new network(AppContext);

                networkCallbackInterface webCllb = new networkCallbackInterface() {
                    @Override
                    public void onPostCallback(String operation, String response) {
                        JSONArray allStations;
                        try {
                            allStations = new JSONArray(response);
                            JSONObject item;

                            // Convert the data into an Dictionary
                            Dictionary<String, String> stations = new Hashtable<String, String>();
                            for(int i = 0; i < allStations.length(); i++){
                                item = allStations.getJSONObject(i);
                                stations.put(item.getString("__id__"), item.getString("__StationName__")); // TODO Replace Placeholders

                            }
                            // Iterate over all Entries and set their proper Station Names
                            historyEntry modified;
                            for(int i = 0; i < Entries.size(); i++){
                                modified = Entries.get(i);
                                modified.StationName = stations.get(modified.StationId);
                                Entries.set(i, modified);
                            }


                            // Calling the callback method
                            if callback != null
                                callback.onFinished(Entries.toArray(new historyEntry[0]));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onImageCallback(String name, Bitmap image) {}
                };
                net.makePostRequest(new WeakReference<networkCallbackInterface>(webCllb), "__stations__", ""); // TODO: Replace Placeholders
                */
                // Calling the callback method // TODO remove this later on:
                if (callback != null)
                    callback.onFinished(Entries.toArray(new historyEntry[0]));

            }
        });
        readTask.execute();
    }

    /**
     * Synchronous Function to wipe all recorded History
     */
    public void clearHistory(){
        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(getHistoryFile());

            DataOutputStream dataStream = new DataOutputStream(fileStream);
            dataStream.writeInt(0); // write the amount of Entries in the file which is 0
        }
        catch(Exception ex){
            // Dont care because if this is the case, there is no history because the file does not exist
        }
    }

    // Helper Method to obtain the File for the History
    public File getHistoryFile(){
        File dir = AppContext.getFilesDir();
        return new File(dir,"app.history");
    }
}

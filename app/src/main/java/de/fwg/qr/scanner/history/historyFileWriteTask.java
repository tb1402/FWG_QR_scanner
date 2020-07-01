package de.fwg.qr.scanner.history;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class historyFileWriteTask extends AsyncTask<historyEntry, Object, Object> {

    public File HistoryFile;
    public taskCallback Callback;

    public historyFileWriteTask(File file, taskCallback callback){
        HistoryFile = file;
        Callback = callback;
    }

    @Override
    protected Object doInBackground(historyEntry ... Entries) {

        // Simple Try to eliminate Errors in File Reading and Writing
        if(historyManager.FileLocked) return null;
        else historyManager.FileLocked = true;

        // No need to check if the file exists, we overwrite it completely so we don't care
        FileOutputStream fileStream;
        try{
            fileStream = new FileOutputStream(HistoryFile);

            DataOutputStream dataStream = new DataOutputStream(fileStream);
            dataStream.writeInt(Entries.length); // write the amount of Entries first

            for(int i = 0; i < Entries.length; i++){
                historyEntry entry = Entries[i];

                byte[] idBuff = entry.StationId.getBytes(StandardCharsets.UTF_8);
                dataStream.writeShort(idBuff.length);
                dataStream.write(idBuff);
                dataStream.writeLong(entry.TimeVisited.getTime() / 1000L); // getTime yields the milliseconds since
                // 1900 whereas the unix timestamp counts the seconds. Since we save the unix timestamp we
                // divide the milliseconds by 1000
            }

            fileStream.close();
            dataStream.close();
        } catch (Exception ex) {
            Log.d("History Manager", "Writing:" + ex.toString());
        } //TODO Exceptionhandling, File may be unable to create bcs. maybe someone with malificious intent created a folder with the same name in the directory or else...

        // Unlock File
        historyManager.FileLocked = false;
        if(Callback != null)
            Callback.onFinished();
        return null;
    }
}

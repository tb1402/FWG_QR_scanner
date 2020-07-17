package de.fwg.qr.scanner.history;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;

import de.fwg.qr.scanner.activityErrorHandling;

public class historyFileWriteTask extends AsyncTask<historyEntry, Object, Object> {

    public File HistoryFile;
    public taskCallback Callback;
    private WeakReference<Context> cref;

    public historyFileWriteTask(Context c, File file, taskCallback callback) {
        HistoryFile = file;
        Callback = callback;
        this.cref = new WeakReference<>(c);
    }

    @Override
    protected Object doInBackground(historyEntry... Entries) {

        // Simple Try to eliminate Errors in File Reading and Writing
        if (historyManager.FileLocked) return null;
        else historyManager.FileLocked = true;

        // No need to check if the file exists, we overwrite it completely so we don't care
        FileOutputStream fileStream;
        try {
            fileStream = new FileOutputStream(HistoryFile);

            DataOutputStream dataStream = new DataOutputStream(fileStream);
            dataStream.writeInt(Entries.length); // write the amount of Entries first

            for (historyEntry entry : Entries) {
                byte[] idBuff = entry.StationId.getBytes(StandardCharsets.UTF_8);
                dataStream.writeShort(idBuff.length);
                dataStream.write(idBuff);
                dataStream.writeLong(entry.TimeVisited.getTime() / 1000L); // getTime yields the milliseconds since
                // 1900 whereas the unix timestamp counts the seconds. Since we save the unix timestamp we
                // divide the milliseconds by 1000
            }

            fileStream.close();
            dataStream.close();
        } catch (Exception e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }

        // Unlock File
        historyManager.FileLocked = false;
        if (Callback != null)
            Callback.onFinished();
        return null;
    }
}

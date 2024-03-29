package de.fwg.qr.scanner.history;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.lang.Exception;

import de.fwg.qr.scanner.activityErrorHandling;

public class historyFileReadTask extends AsyncTask<Object, Object, historyEntry[]> {

    private File HistoryFile;
    private taskResultCallback<historyEntry[]> Callback;
    private WeakReference<Context> cref;

    public historyFileReadTask(Context c, File file, taskResultCallback<historyEntry[]> callback) {
        HistoryFile = file;
        Callback = callback;
        this.cref = new WeakReference<>(c);
    }

    @Override
    protected historyEntry[] doInBackground(Object... objects) {

        // Simple Try to eliminate Errors in File Reading and Writing
        if (historyManager.FileLocked)
        {
            Exception e = new Exception("HistoryFile access violation error");
            // Really should never happen, but if it happens there is no chance for the FileLock to be unlocked without a reset of the app or a successfull execution of the method, 
            // whilst the latter wont happen with a locked file
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }
        else historyManager.FileLocked = true;

        ArrayList<historyEntry> entries = new ArrayList<>();

        if (!HistoryFile.exists()) {
            // file does not exist:
            // the entries array list stays empty
            historyManager.FileLocked = false;
            return new historyEntry[0]; // simply return no elements
            // File gets created when the first entry gets inserted
        } else {
            try {
                FileInputStream fileStream = new FileInputStream(HistoryFile);
                DataInputStream dataStream = new DataInputStream(fileStream);

                int entriesCount = dataStream.readInt();

                // clear the caches Entries in beforehand
                entries.clear();

                historyEntry current;
                for (int i = 0; i < entriesCount; i++) {

                    short strLength = dataStream.readShort();
                    byte[] strBuff = new byte[strLength];
                    for (short s = 0; s < strLength; s++) {
                        strBuff[s] = dataStream.readByte();
                    }
                    current = new historyEntry(new String(strBuff, StandardCharsets.UTF_8), new Date(dataStream.readLong() * 1000L));
                    entries.add(current);
                }
                dataStream.close();
                fileStream.close();

            } catch (Exception e) {
                // would cause exception because the lock wouldnt be flipped back as on end of this method
                // but the error handling activity resets the app so the lock is reset aswell
                Intent i = new Intent(cref.get(), activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                cref.get().startActivity(i);
            } // catch is not important, just required, we already checked
            // if the file exists so this should NEVER lead to any kind of exception
        }

        // Unlock File
        historyManager.FileLocked = false;
        historyEntry[] res = entries.toArray(new historyEntry[0]);
        if (Callback != null)
            Callback.onFinished(res);
        return res;
    }
}

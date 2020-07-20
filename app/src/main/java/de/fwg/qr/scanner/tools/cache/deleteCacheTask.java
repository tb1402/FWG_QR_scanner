package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.File;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.activityErrorHandling;

/**
 * asynchronous task to delete cache files on storage
 */
class deleteCacheTask extends AsyncTask<File, Void, Void> {

    private WeakReference<Context> cref;

    public deleteCacheTask(Context c) {
        this.cref = new WeakReference<>(c);
    }

    @Override
    protected Void doInBackground(File... files) {
        try {
            if (files[0].isDirectory()) {
                File[] fa = files[0].listFiles();
                if (fa != null) {
                    for (File f : fa) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }
        return null;
    }
}

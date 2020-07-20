package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.activityErrorHandling;

/**
 * asynchronous task to read files from storage cache
 */
class readCacheFileTask extends AsyncTask<File, Void, Bitmap> {

    private WeakReference<readCacheCallback> ref;
    private WeakReference<addToMemCacheWhileReadInterface> cm;
    private final String key;
    private WeakReference<Context> cref;

    public readCacheFileTask(Context c, WeakReference<readCacheCallback> ref, WeakReference<addToMemCacheWhileReadInterface> cm, String key) {
        this.ref = ref;
        this.cm = cm;
        this.key = key;
        this.cref = new WeakReference<>(c);
    }

    @Override
    protected Bitmap doInBackground(File... files) {
        try {
            FileInputStream i = new FileInputStream(files[0]);
            return BitmapFactory.decodeStream(i);
        } catch (IOException e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (result == null) {
            ref.get().cacheCallback(true, null);
        } else {
            ref.get().cacheCallback(false, result);
            cm.get().addToCache(key, result);//add image to memory cache
        }
    }
}

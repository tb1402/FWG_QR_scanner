package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.async.asyncTask;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

class readCacheFileNA extends asyncTask<Void> {

    private final String key;
    private WeakReference<networkCallbackInterface> ref;
    private WeakReference<addToMemCacheWhileReadInterface> cm;
    private WeakReference<Context> cref;
    private String operation;
    private File f;

    public readCacheFileNA(Context c, WeakReference<networkCallbackInterface> ref, WeakReference<addToMemCacheWhileReadInterface> cm, String key,String operation, File f) {
        this.ref = ref;
        this.cm = cm;
        this.key = key;
        this.cref = new WeakReference<>(c);
        this.operation = operation;
        this.f=f;
    }

    @Override
    public Void call() {
        Log.i("FWGO","cache load");
        Bitmap b;
        try {
            FileInputStream i = new FileInputStream(f);
            b=BitmapFactory.decodeStream(i);
        } catch (IOException e) {
            Intent i = new Intent(cref.get(), activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            cref.get().startActivity(i);
            b=null;
        }
        Log.i("FWGO","loaded");
        if (b== null) {
            ref.get().onImageCallback("errorCache",null);
        } else {
            ref.get().onImageCallback(operation,b);
            cm.get().addToCache(key, b);//add image to memory cache
        }
        return null;
    }
}

package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

class readCacheFileTask extends AsyncTask<File,Void,Bitmap> {

    private WeakReference<readCacheCallback> ref;
    private WeakReference<addToMemCacheWhileReadInterface> cm;
    private final String key;

    public readCacheFileTask(WeakReference<readCacheCallback> ref,WeakReference<addToMemCacheWhileReadInterface> cm,String key){
        this.ref=ref;
        this.cm=cm;
        this.key=key;
    }
    @Override
    protected Bitmap doInBackground(File... files) {
        try {
            FileInputStream i = new FileInputStream(files[0]);
            return BitmapFactory.decodeStream(i);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onPostExecute(Bitmap result){
        if(result==null){
            ref.get().cacheCallback(true,null);
        }
        else {
            ref.get().cacheCallback(false, result);
            cm.get().addToCache(key,result);
        }
    }
}

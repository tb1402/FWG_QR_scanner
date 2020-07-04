package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.preferencesManager;

public class cacheManager implements addToMemCacheWhileReadInterface{

    private Context c;
    public int cacheSaveIndex=0;
    private memoryCacheSingleton memoryCache;

    public cacheManager(Context c){
        this.c=c;
        memoryCache=memoryCacheSingleton.getInstance(c);
    }

    // Checks if a volume containing external storage is available
    // for read and write.
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // Checks if a volume containing external storage is available to at least read.
    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    public void cacheImage(String id, int number, Bitmap data,boolean preview){
        String key;
        if(preview){
            key="low-"+id+"-"+number;
        }
        else{
            preferencesManager pm=new preferencesManager(c);
            key=pm.getImageResolution()+"-"+id+"-"+number;
        }
        if(memoryCache.get(key)==null){
            if(memoryCache.size()+data.getByteCount()<=memoryCache.memCacheSize){
                memoryCache.put(key,data);
            }
        }
        if(isExternalStorageWritable()) {
            File f;
            if(preview){
                f = new File(c.getExternalCacheDir(), key+ ".img");
            }
            else{
                f= new File(c.getExternalCacheDir(), key+ ".img");
            }
            if(!f.exists()){
                new writeCacheFileTask(data).execute(f);
            }
        }
    }
    public boolean loadCachedImage(WeakReference<readCacheCallback> ref, String id, int number,boolean preview){
        String key;
        if(preview){
            key="low-"+id+"-"+number;
        }
        else{
            preferencesManager pm=new preferencesManager(c);
            key=pm.getImageResolution()+"-"+id+"-"+number;
        }
        Bitmap bm=memoryCache.get(key);
        if(bm==null) {
            if (isExternalStorageReadable()) {
                File f;
                if (preview) {
                    f = new File(c.getExternalCacheDir(), key + ".img");
                } else {
                    f = new File(c.getExternalCacheDir(), key + ".img");
                }
                if (f.exists() && !f.isDirectory()) {
                    new readCacheFileTask(ref,new WeakReference<>((addToMemCacheWhileReadInterface) this),key).execute(f);
                    return true;
                }
            }
        }
        else{
            ref.get().cacheCallback(false,bm);
            return true;
        }
        return false;
    }

    @Override
    public void addToCache(String key,Bitmap data) {
        memoryCache.put(key,data);
    }
}

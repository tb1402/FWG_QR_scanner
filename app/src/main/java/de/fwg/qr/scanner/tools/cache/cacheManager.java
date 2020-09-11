package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.async.asyncWrapper;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * Class used to interact with the cached data (both memory and storage)
 * Also used to save data to the cache
 */
public class cacheManager implements addToMemCacheWhileReadInterface {

    public int cacheSaveIndex = 0;//index to count how many pictures have been cached
    private Context c;
    private memoryCacheSingleton memoryCache;

    /**
     * Why do we always need this f*****g context?
     *
     * @param c android context
     */
    public cacheManager(Context c) {
        this.c = c;
        memoryCache = memoryCacheSingleton.getInstance();
    }

    /**
     * Checks whether the external storage is available for reading and writing
     *
     * @return boolean available?
     */
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * Checks whether the external storage can be read
     *
     * @return boolean readable?
     */
    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * Adds an image to the cache
     *
     * @param id      id of the station
     * @param number  number of the image
     * @param data    the actual image data
     * @param preview is it a preview? if so the image will only be saved in low quality,
     *                used separate for simplicity and compatibility
     */
    public void cacheImage(String id, int number, Bitmap data, boolean preview) {
        String key = getCacheKey(id, number, preview);
        if (memoryCache.get(key) == null) {//memory cache exists?
            if (memoryCache.size() + data.getByteCount() <= memoryCache.memCacheSize) {//enough free space in memCache
                memoryCache.put(key, data);//save the image in memory cache
            }
        }
        if (isExternalStorageWritable()) {
            File f = new File(c.getExternalCacheDir(), key + ".img");
            if (!f.exists()) {
                new writeCacheFileTask(c, data,"mykey").execute(f);//task to write image asynchronous
            }
        }
    }

    /**
     * Load an image from the cache
     *
     * @param ref     reference to callback in target class
     * @param id      id of the image
     * @param number  number of the image
     * @param preview used for preview
     * @return true means image is in cache and will be loaded and given back via the {@link readCacheCallback} interface,
     * false means no image is in cache
     */
    public boolean loadCachedImage(WeakReference<networkCallbackInterface> ref, String id, String operation, int number, boolean preview) {
        String key = getCacheKey(id, number, preview);
        Bitmap bm = memoryCache.get(key);//get image from memory cache, no need for an asynchronous task, because RAM is very fast ;-)
        if (bm == null) {//if null, image isn't in memoryCache
            if (isExternalStorageReadable()) {
                File f = new File(c.getExternalCacheDir(), key + ".img");
                if (f.exists() && !f.isDirectory()) {
                    Log.i("FWGO","cached!");
                    //new readCacheFileTask(c, ref, (addToMemCacheWhileReadInterface) this, key,operation,"mykey").execute(f);//load from storage and add to memory cache see the readCacheFileTask
                    new readCacheFileNA(c, ref.get(), (addToMemCacheWhileReadInterface) this, key,operation,f,"mykey").execute();
                    return true;
                }
            }
        } else {//image found in memory cache, can be given back
            ref.get().onImageCallback(operation,bm);
            Log.i("FWGO","cached!");
            return true;
        }
        return false;
    }

    /**
     * Implementation of {@link addToMemCacheWhileReadInterface}
     *
     * @param key  key of the image
     * @param data image data
     */
    @Override
    public void addToCache(String key, Bitmap data) {
        memoryCache.put(key, data);
        Log.i("FWGO","added");
    }

    /**
     * Delete all cached images, this method is triggered remotely by changing a value on the server
     * Needed if we update pictures
     */
    public void invalidateCache() {
        memoryCacheSingleton.invalidate();
        memoryCache = memoryCacheSingleton.getInstance();
        File f = new File(c.getExternalCacheDir(), "/");
        new deleteCacheTask(c).execute(f);
        Log.i("FWGO","del");
    }

    /**
     * Get a unique key to use in cache (filename for storage cache) based on some image information
     *
     * @param id      id of the image
     * @param number  number of the image
     * @param preview preview image?
     * @return crafted key
     */
    private String getCacheKey(String id, int number, boolean preview) {
        return preview ? "low-" + id + "-" + number : new preferencesManager(c).getImageResolution() + "-" + id + "-" + number;
    }
}

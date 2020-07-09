package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;

/**
 * Interface used to write to the memory cache while reading from the storage cache
 * Needed because memory cache is deleted every time when the app is closed
 * when needed, called from {@link readCacheFileTask#onPostExecute(Bitmap)} and executed in {@link cacheManager#addToCache(String, Bitmap)}
 */
public interface addToMemCacheWhileReadInterface {
    void addToCache(String key, Bitmap data);//method containing the data which should be cached and a corresponding key
}

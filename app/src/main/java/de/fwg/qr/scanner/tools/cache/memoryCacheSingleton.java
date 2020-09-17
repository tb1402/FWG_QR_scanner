package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Singleton for memory cache
 */
class memoryCacheSingleton {
    private static memoryCacheSingleton memoryCacheSingleton;//instance
    private final int memMaxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);//get maximal available ram in kb
    final int memCacheSize = memMaxMemory / 4;//only use one quarter of available for cache
    private LruCache<String, Bitmap> memoryCache;//memory cache

    private memoryCacheSingleton() {
        memoryCache = new LruCache<String, Bitmap>(memCacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    static memoryCacheSingleton getInstance() {
        if (memoryCacheSingleton == null) {
            memoryCacheSingleton = new memoryCacheSingleton();
        }
        return memoryCacheSingleton;
    }

    /**
     * invalidate (remove) the memory cache
     */
    static void invalidate() {
        memoryCacheSingleton = null;
    }

    /**
     * get an image from the cache
     * @param key spacial string, to identify the image
     * @return the image matching the key
     */
    Bitmap get(String key) {
        return memoryCache.get(key);
    }

    /**
     * get size of memory cache
     * @return int size
     */
    int size() {
        return memoryCache.size();
    }

    /**
     * add an image to the cache
     * @param key the key to identify the image
     * @param data the image data
     */
    void put(String key, Bitmap data) {
        memoryCache.put(key, data);
    }
}

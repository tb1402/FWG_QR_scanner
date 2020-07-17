package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Singleton for memory cache
 */
class memoryCacheSingleton {
    private static memoryCacheSingleton memoryCacheSingleton;
    private LruCache<String, Bitmap> memoryCache;
    private final int memMaxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int memCacheSize = memMaxMemory / 4;

    static memoryCacheSingleton getInstance() {
        if (memoryCacheSingleton == null) {
            memoryCacheSingleton = new memoryCacheSingleton();
        }
        return memoryCacheSingleton;
    }

    static void invalidate() {
        memoryCacheSingleton = null;
    }

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

    Bitmap get(String key) {
        return memoryCache.get(key);
    }

    int size() {
        return memoryCache.size();
    }

    void put(String key, Bitmap data) {
        memoryCache.put(key, data);
    }
}

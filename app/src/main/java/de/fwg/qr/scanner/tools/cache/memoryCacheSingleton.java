package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

class memoryCacheSingleton {
    private static memoryCacheSingleton memoryCacheSingleton;
    private LruCache<String, Bitmap> memoryCache;
    private final int memMaxMemory=(int)(Runtime.getRuntime().maxMemory()/1024);
    final int memCacheSize=memMaxMemory/4;

    static memoryCacheSingleton getInstance(Context c) {
        if(memoryCacheSingleton==null){
            memoryCacheSingleton=new memoryCacheSingleton(c);
        }
        return memoryCacheSingleton;
    }
    static void invalidate(){
        memoryCacheSingleton=null;
    }

    private memoryCacheSingleton(Context c) {
        memoryCache=new LruCache<String, Bitmap>(memCacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }
    Bitmap get(String key){
        return memoryCache.get(key);
    }
    int size(){
        return memoryCache.size();
    }
    void put(String key,Bitmap data){
        memoryCache.put(key,data);
    }
}

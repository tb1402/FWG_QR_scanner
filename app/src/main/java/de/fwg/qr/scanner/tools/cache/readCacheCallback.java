package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;

/**
 * Used to return cached data to the calling activity etc.
 */
public interface readCacheCallback {
    void cacheCallback(boolean error, Bitmap image);
}

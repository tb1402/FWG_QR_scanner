package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;

public interface readCacheCallback {
    void cacheCallback(boolean error,Bitmap image);
}

package de.fwg.qr.scanner.tools;

import android.graphics.Bitmap;

/**
 * Callback interface for network requests
 */
public interface networkCallbackInterface {
    void onPostCallback(String operation, String response);
    void onImageCallback(String name, Bitmap image);
}

package de.fwg.qr.scanner.tools;

import android.graphics.Bitmap;

/**
 * Callback interface for imageID request
 */
public interface networkCallbackImageID extends networkCallbackInterface{

    /**
     * callback method
     * @param name name assigned to requested image
     * @param image image data
     * @param number number of the image being requested
     */
    void onImageCallback(String name, Bitmap image, int number);
}

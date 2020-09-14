package de.fwg.qr.scanner.tools;

import android.graphics.Bitmap;

/**
 * Callback interface for network requests
 */
public interface networkCallbackInterface {

    /**
     * Method for postRequest callback
     * @param operation name of requested php file
     * @param response response from the server
     */
    void onPostCallback(String operation, String response);

    /**
     * Method for imageCallback
     * @param name name assigned to the image on request
     * @param image image data
     */
    void onImageCallback(String name, Bitmap image);
}

package de.fwg.qr.scanner.tools;

import android.graphics.Bitmap;

public interface networkCallbackImageID extends networkCallbackInterface{

    void onImageCallback(String name, Bitmap image, int number);
}

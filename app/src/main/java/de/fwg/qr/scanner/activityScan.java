package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityScan extends toolbarWrapper implements networkCallbackInterface {

    private ImageView image;
    private VideoView video;
    private TextView text;
    private WeakReference<networkCallbackInterface> ref;

    private String barcodeValue = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.toolbar_scan,this,getString(R.string.title_scanned));
        super.onCreate(savedInstanceState);
        Intent recievedIntent = getIntent();//global attribute not needed (my hint to check for null was only meant for scanning the code
        barcodeValue = recievedIntent.getStringExtra(Intent.EXTRA_TEXT);

    }


    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {

    }
}

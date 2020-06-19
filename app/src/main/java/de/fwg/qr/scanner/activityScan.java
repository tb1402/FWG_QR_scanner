package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityScan extends AppCompatActivity implements networkCallbackInterface {

    private ImageView image;
    private VideoView video;
    private TextView text;
    private WeakReference<networkCallbackInterface> ref;

    private Intent recievedIntent = null;
    private String barcodeValue = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (recievedIntent == null) {
            recievedIntent = getIntent();
            barcodeValue = recievedIntent.getStringExtra(Intent.EXTRA_TEXT);
        }
        setContentView(R.layout.activity_scan);
    }


    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {

    }
}

package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityScan extends toolbarWrapper implements networkCallbackInterface {

    private ImageView image;
    private ImageSwitcher imageSwitcher;
    private Button buttonPre;
    private Button buttonNext;
    private TextView text;
    private WeakReference<networkCallbackInterface> ref;
    network net;

    private String barcodeValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.toolbar_scan, this, getString(R.string.title_scanned));
        super.onCreate(savedInstanceState);
        net = new network(this);
        Intent receivedIntent = getIntent();
        barcodeValue = receivedIntent.getStringExtra("barcode");
        image = findViewById(R.id.imageView);
        text = findViewById(R.id.textView);
        imageSwitcher = findViewById(R.id.imageSwitcher);
        buttonPre = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);

    }


    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        lockUI(false);
        if (name.contentEquals("test")) {
            this.image.setImageBitmap(image);

        }

    }

   /* private void PlayVideo(String URL) { //Unimportant: Video playback in activity_fullscreen_video_playback
        try {
            this.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(video);

            Uri uri = Uri.parse(network.baseURL + "/images/" );
            video.setMediaController(mediaController);
            video.setVideoURI(uri);
            video.requestFocus();
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    video.start();
                }
            });
            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    video.start();
                }
            });
        } catch (Exception e) {
            System.out.println("Video Play Error :" + e.toString());
        }
       } */
}
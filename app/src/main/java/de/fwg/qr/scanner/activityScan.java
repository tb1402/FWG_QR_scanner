package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
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
    private TextView textView;

    private WeakReference<networkCallbackInterface> ref;
    network net;

    private String ID = "";
    private String name = "";
    private String text = "";
    private String bild = "";
    private String video = "";

    private int imagePosition = 0;
    private String[] imageURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.toolbar_scan, this, getString(R.string.title_scanned));
        super.onCreate(savedInstanceState);
        net = new network(this);
        Intent receivedIntent = getIntent();
        ID = receivedIntent.getStringExtra("ID");
        name = receivedIntent.getStringExtra("Name");
        text = receivedIntent.getStringExtra("Text");
        bild = receivedIntent.getStringExtra("Bild");
        video = receivedIntent.getStringExtra("Video");
        setToolbarTitle(name);
        imageURL = new String[Integer.parseInt(bild)];
        image = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        imageSwitcher = findViewById(R.id.imageSwitcher);
        buttonPre = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        assignButtons();
        setImageURL();
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

    public void assignButtons() {
        buttonPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imagePosition <= 0) {
                    imagePosition = (imageURL.length - 1);
                } else {
                    imagePosition--;
                }
            }

        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imagePosition >= (imageURL.length - 1)) {
                    imagePosition = 0;
                } else {
                    imagePosition++;
                }
            }
        });
    }

    public void setImageSwitcher() {

    }

    public void setImageURL() {
        for (int i = 0; i < imageURL.length; i++) {
            imageURL[i] = network.baseURL + "/images/" /* + net.getImRes() + "/" */ + ID + "/" + i + ".png";
        }
    }

}
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
import android.widget.ViewSwitcher;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityScan extends toolbarWrapper implements networkCallbackInterface {

    private ImageView imageView;
    private ImageSwitcher imageSwitcher;
    private Button buttonPre;
    private Button buttonNext;
    private TextView textView;
    private FloatingActionButton videoButton;

    private WeakReference<networkCallbackInterface> ref;
    network net;

    private String ID = "";
    private String name = "";
    private String text = "";
    private String bild = "";
    private String video = "";

    private static final int VIDEO_CALL = 0;
    private static final int IMAGE_CALL = 1;

    private int imagePosition = 0;
    private int i = 0;
    private ArrayList<Bitmap> images;
    private Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.toolbar_scan, this, getString(R.string.title_scanned));
        super.onCreate(savedInstanceState);
        net = new network(this);
        ref = new WeakReference<>((networkCallbackInterface) this);
        Intent receivedIntent = getIntent();
        ID = receivedIntent.getStringExtra("ID");
        name = receivedIntent.getStringExtra("Name");
        text = receivedIntent.getStringExtra("Text");
        bild = receivedIntent.getStringExtra("Bild");
        video = receivedIntent.getStringExtra("Video");
        setToolbarTitle(name);
        setupAbHome();
        images = new ArrayList<Bitmap>();
        imageView = new ImageView(this);
        textView = findViewById(R.id.textView);
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        imageSwitcher = findViewById(R.id.imageSwitcher);
        buttonPre = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        videoButton = findViewById(R.id.videoButton);
        if (Integer.parseInt(video) > 0) {
            videoButton.setVisibility(View.VISIBLE);
        } else {
            videoButton.setVisibility(View.INVISIBLE);
        }
        assignButtons();
        clickableImageSwitcher();
        getImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        intent = null;
    }


    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        lockUI(false);
        if (name.contentEquals("ImagePreview")) {
            images.add(image);
            if (i >= Integer.parseInt(bild)) {
                setImageSwitcher();
            }
        }
    }

    public void assignButtons() {
        buttonPre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imagePosition <= 0) {
                    imagePosition = (Integer.parseInt(bild) - 1);
                } else {
                    imagePosition--;
                }
                imageView.setImageBitmap(images.get(imagePosition));
            }

        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imagePosition >= (Integer.parseInt(bild) - 1)) {
                    imagePosition = 0;
                } else {
                    imagePosition++;
                }
                imageView.setImageBitmap(images.get(imagePosition));
            }
        });
        if (Integer.parseInt(video) > 0) {
            videoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    newIntent(VIDEO_CALL);
                }
            });
        }
    }

    public void setImageSwitcher() {
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                if (imageView.getDrawable() == null) {
                    imageView.setImageBitmap(images.get(imagePosition));
                    return imageView;
                } else {
                    imageSwitcher.removeView(imageView);
                    imageView.setImageBitmap(images.get(imagePosition));
                    return imageView;
                }
            }
        });

    }

    public void getImages() {
        lockUI(true);
        for (i = 0; i < Integer.parseInt(bild); i++) {
            net.makeImageRequest(ref, "ImagePreview", ID, i, true);
        }

    }

    public void newIntent(int operation) {
        if (intent == null && Integer.parseInt(video) > 0 && operation == VIDEO_CALL) { //Check for video number unnecessary, getting checked before; just for safety purposes
            intent = new Intent(this, activityFullscreenVideoPlayback.class);
            intent.putExtra("ID", ID);
            startActivity(intent);
        }
        if (intent == null && operation == IMAGE_CALL) {
            intent = new Intent(this, activityPictureFullscreen.class);
            intent.putExtra("ID", ID);
            intent.putExtra("Position", imagePosition);
            startActivity(intent);
        }
    }

    public void clickableImageSwitcher() {
        imageSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newIntent(IMAGE_CALL);
            }
        });
    }


}
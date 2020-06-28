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

    private WeakReference<networkCallbackInterface> ref;
    network net;

    private String ID = "";
    private String name = "";
    private String text = "";
    private String bild = "";
    private String video = "";

    private int imagePosition = 0;
    private int i = 0;
    private ArrayList<Bitmap> images;

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
        //System.out.println(bild);
        images = new ArrayList<Bitmap>();
        imageView = new ImageView(this);
        textView = findViewById(R.id.textView);
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        imageSwitcher = findViewById(R.id.imageSwitcher);
        buttonPre = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        assignButtons();
        getImages();
    }


    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        lockUI(false);
        if (name.contentEquals("Images")) {
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
            System.out.print("i größer als 0");
            net.makeImageRequest(ref, "Images", ID, i, true);
        }

    }


}
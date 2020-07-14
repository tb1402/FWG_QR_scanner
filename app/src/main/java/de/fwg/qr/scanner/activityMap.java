package de.fwg.qr.scanner;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.tools.cache.cacheManager;
import de.fwg.qr.scanner.tools.cache.readCacheCallback;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;


public class activityMap extends toolbarWrapper implements networkCallbackInterface, readCacheCallback {

    private ImageView imageView;
    private ArrayList<Bitmap> images;
    private TextView textView;
    private ProgressBar progressBar;

    private network net;
    private WeakReference<networkCallbackInterface> ref;

    private WeakReference<readCacheCallback> cacheRef;
    private cacheManager cm;

    private int i = 0;


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);
        net = new network(this);
        ref = new WeakReference<>((networkCallbackInterface) this);
        cacheRef = new WeakReference<>((readCacheCallback) this);
        cm = new cacheManager(getApplicationContext());
        images = new ArrayList<>();
        setupAbHome();
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        getImages();


    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButton1:
                if (checked) {
                    if (images.size() >= 1) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(0));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.radioButton2:
                if (checked) {
                    if (images.size() >= 2) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(1));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.radioButton3:
                if (checked) {
                    if (images.size() >= 3) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(2));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        if (name.contentEquals("ImagePreview")) {
            images.add(image);
            if (images.size() >= 1) {
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                imageView.setImageBitmap(images.get(0));
            }
            i++;
            if (i < 3) {
                getImages();
            }

        }

    }

    public void getImages() {
        if (!cm.loadCachedImage(cacheRef, "map", i, true)) {
            net.makeImageRequest(ref, "ImagePreview", "map", i, true);
        }
    }

    @Override
    public void cacheCallback(boolean error, Bitmap image) {
        if (!error) {
            images.add(image);
            i++;
            if (i < 3) {
                getImages();
            }
        }
    }
}

package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.history.historyEntry;
import de.fwg.qr.scanner.history.historyManager;
import de.fwg.qr.scanner.tools.cache.cacheManager;
import de.fwg.qr.scanner.tools.cache.readCacheCallback;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityScan extends toolbarWrapper implements networkCallbackInterface, readCacheCallback {

    private ImageView imageView;
    private ImageSwitcher imageSwitcher;
    private Button buttonPre;
    private Button buttonNext;
    private FloatingActionButton videoButton;
    private ProgressBar progressBar;

    private WeakReference<networkCallbackInterface> ref;
    private WeakReference<readCacheCallback> cacheRef;
    private network net;

    private String ID = "";
    private String bild = "";
    private int video = -1;

    private int imagePosition = 0;
    private int i = 0;
    private int imageRequestCount = 0;
    private ArrayList<Bitmap> images;
    private cacheManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.toolbar_scan, this,"Placeholder");
        super.onCreate(savedInstanceState);
        net = new network(this);
        ref = new WeakReference<>((networkCallbackInterface) this);
        cacheRef = new WeakReference<>((readCacheCallback) this);
        cm = new cacheManager(getApplicationContext());
        Intent receivedIntent = getIntent();
        ID = receivedIntent.getStringExtra("ID");
        String name = receivedIntent.getStringExtra("Name");
        String text = receivedIntent.getStringExtra("Text");
        bild = receivedIntent.getStringExtra("Bild");
        String videoIntentExtra = receivedIntent.getStringExtra("Video");
        video = Integer.parseInt(videoIntentExtra == null ? "-1" : videoIntentExtra);
        setToolbarTitle(name);
        setupAbHome();
        images = new ArrayList<>();
        imageView = new ImageView(this);
        TextView textView = findViewById(R.id.textView);
        textView.setText(text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        imageSwitcher = findViewById(R.id.imageSwitcher);
        buttonPre = findViewById(R.id.buttonPrevious);
        buttonNext = findViewById(R.id.buttonNext);
        videoButton = findViewById(R.id.videoButton);
        videoButton.setVisibility(video > 0 ? View.VISIBLE : View.INVISIBLE);
        assignButtons();
        clickableImageSwitcher();
        getImages();

        //add to history
        new historyManager(getApplicationContext()).addEntry(new historyEntry(ID));
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        if (name.contentEquals("ImagePreview")) {
            images.add(image);
            cm.cacheImage(ID, cm.cacheSaveIndex, image, true);
            cm.cacheSaveIndex++;
            i++;
            if (i >= Integer.parseInt(bild)) {
                lockUI(false);
                setImageSwitcher();
            } else {
                getImages();
            }
        }
    }

    @Override
    public void cacheCallback(boolean error, Bitmap image) {
        if (!error) {
            images.add(image);
            i++;
            if (i >= Integer.parseInt(bild) || imageRequestCount == Integer.parseInt(bild) - 1) {
                lockUI(false);
                setImageSwitcher();
            }
            imageRequestCount++;
            if (i < Integer.parseInt(bild)) {
                getImages();
            }

        }
    }

    public void assignButtons() {
        if (Integer.parseInt(bild) > 1) {
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
        } else {
            buttonNext.setVisibility(View.GONE);
            buttonPre.setVisibility(View.GONE);
        }
        if (video > 0) {
            videoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), activityFullscreenVideoPlayback.class);
                    intent.putExtra("ID".toLowerCase(), ID);
                    startActivity(intent);
                }
            });
        }
    }

    public void setImageSwitcher() {
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
                if (imageView.getDrawable() != null) {
                    imageSwitcher.removeView(imageView);
                }
                imageView.setImageBitmap(images.get(imagePosition));
                return imageView;
            }
        });

    }

    public void getImages() {
        lockUI(true);
        if (!cm.loadCachedImage(cacheRef, ID, i, true)) {
            net.makeImageRequest(ref, "ImagePreview", ID, i, true);
        }
    }

    public void clickableImageSwitcher() {
        imageSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), activityPictureFullscreen.class);
                intent.putExtra("ID", ID);
                intent.putExtra("Position", imagePosition);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.tb_item_settings:
                i = new Intent(getApplicationContext(), activitySettings.class);
                startActivity(i);
                return true;
            case R.id.tb_item_map:
                i = new Intent(getApplicationContext(), activityMap.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
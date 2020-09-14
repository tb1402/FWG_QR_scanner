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
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

/**
*Activity which shows information about a station, after code as been scanned
*/
public class activityScan extends toolbarWrapper implements networkCallbackInterface {

    private ImageView imageView;
    private ImageSwitcher imageSwitcher;
    private Button buttonPre;
    private Button buttonNext;
    private FloatingActionButton videoButton;
    private ProgressBar progressBar;

    private WeakReference<networkCallbackInterface> ref;
    private network net;

    /**
     * The ID of the visited Station as a String
     */
    private String ID = "";
    /**
     * String with how many pictures this station hast
     */
    private String bild = "";
    /**
     * Value for indicating if this station has a video; (-1 || 0): no video, (>=1): video existing
     */
    private int video = -1;
    /**
     * Value for indicating which picture schould be displayed by ImageSwitcher
     */
    private int imagePosition = 0;
    /**
     * Value for knowing which picture should be loaded next
     */
    private int i = 0;
    /**
     * ArrayList for all bitmaps given back by ImageCallback
     */
    private ArrayList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.toolbar_scan, this, "Placeholder");
        super.onCreate(savedInstanceState);
        net = new network(this);
        ref = new WeakReference<>((networkCallbackInterface) this);
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
            i++;
            if (i >= Integer.parseInt(bild)) {
                lockUI(false);
                setImageSwitcher();
            } else {
                getImages();
            }
        }
    }

    /**
     * Method for handling Buttons used by ImageSwitcher as well as creating a Button if a video for this station exists
     */
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

    /**
     * Initialization of ImageSwitcher; Removal of progressBar when initialized successfully
     */
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

    /**
     * Method for making image requests
     */
    public void getImages() {
        lockUI(true);
        net.makeImageRequest(ref, "ImagePreview", ID, i, true);
    }

    /**
     * Method for setting onCLickListener on ImageSwitcher to display pictures in Fullscreen via the activity activityPictureFullscreen
     */
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
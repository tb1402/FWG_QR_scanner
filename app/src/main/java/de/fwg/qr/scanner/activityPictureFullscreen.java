package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityPictureFullscreen extends AppCompatActivity implements networkCallbackInterface {

    private ImageView imageView;
    private FloatingActionButton button;
    private ProgressBar progressBar;
    private TextView textView;

    /**
     * The ID of the visited Station as a String
     */
    private String ID = "";
    /**
     * Value indicating which picture should be displayed in fullscreen
     */
    private int imagePosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//set fullscreen mode
        setContentView(R.layout.activity_picture_fullscreen);
        network net = new network(this);
        WeakReference<networkCallbackInterface> ref = new WeakReference<>((networkCallbackInterface) this);
        Intent receivedIntent = getIntent();
        if (receivedIntent != null) {
            ID = receivedIntent.getStringExtra("ID");
            imagePosition = receivedIntent.getIntExtra("Position", 0);
        }
        imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.fab);
        button.setVisibility(View.INVISIBLE);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        assignButton();
        net.makeImageRequest(ref, "Image", ID, imagePosition, false);
    }


    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("FWGO",name);
        if (name.contentEquals("Image")) {
            imageView.setImageBitmap(image);
            textView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, getText(R.string.image_not_found_with_current_resolution), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Assignment of button for exiting the activity (Back press possible as well)
     */
    public void assignButton() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}

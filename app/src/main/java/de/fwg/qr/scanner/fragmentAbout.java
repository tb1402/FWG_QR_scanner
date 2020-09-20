package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

/**
 * fragment showing information about the developers
 */
public class fragmentAbout extends fragmentWrapper implements networkCallbackInterface {

    private ImageView imageView;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View v, @Nullable Bundle sis) {
        imageView = v.findViewById(R.id.imageView);
        progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        net.makeImageRequest(this, "ImageRequest", "groupPicture", 0, true, c);
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        if (name.contentEquals("ImageRequest")) {
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
            imageView.setImageBitmap(image);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(c, activityPictureFullscreen.class);
                    intent.putExtra("ID", "groupPicture");
                    intent.putExtra("Position", 0);
                    startActivity(intent);
                }
            });
        }
    }
}
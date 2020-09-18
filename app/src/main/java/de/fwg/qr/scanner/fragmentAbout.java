package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

/**
 * fragment showing information about the developers
 */
public class fragmentAbout extends fragmentWrapper implements networkCallbackInterface {

    private TextView textView; //TODO: Load text either through network call or just plainly use setText()
    private ImageView imageView;
    private ProgressBar progressBar;

    private network net;
    private WeakReference<networkCallbackInterface> ref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View v, @Nullable Bundle sis) {
        textView = v.findViewById(R.id.textView);
        imageView = v.findViewById(R.id.imageView);
        progressBar = v.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        net = new network(c);
        ref = new WeakReference<>((networkCallbackInterface) this);
        net.makeImageRequest(ref, "ImageRequest", "GroupPicture", 0, true);
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
                    intent.putExtra("ID", "GroupPicture");
                    intent.putExtra("Position", 0);
                    startActivity(intent);
                }
            });
        }
    }
}
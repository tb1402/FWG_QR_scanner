package de.fwg.qr.scanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentEscapeRoutes extends fragmentWrapper implements networkCallbackInterface {

    ImageView test;
    WeakReference<networkCallbackInterface> ref;
    ProgressDialog pd;
    Button testb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
        ref = new WeakReference<>((networkCallbackInterface) this);
        if (!net.isNetworkAvailable()) {
            Toast.makeText(c, "Keine Netzwerkverbindung!", Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_escape_routes, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle sis) {
        test = v.findViewById(R.id.imageView);
        //net.makeImageRequest(ref, "test", "/1.jpg");
        pd = new ProgressDialog(c);
        pd.setTitle(getString(R.string.network_buffering));
        pd.setCancelable(false);
        //a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        testb = v.findViewById(R.id.but_vs);
        testb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(c, activityFullscreenVideoPlayback.class);
                i.putExtra("isER", false);
                i.putExtra("id", "test");
                startActivity(i);
            }
        });
    }

    @Override
    public void onPostCallback(String op, String res) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("fwg", name);
        lockUI(false);
        if (name.contentEquals("test")) {
            test.setImageBitmap(image);
        }
    }
}
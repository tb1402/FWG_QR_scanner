package de.fwg.qr.scanner;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentScan extends fragment_wrapper implements networkCallbackInterface {

    ImageView test;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!net.isNetworkAvailable()){
            Toast.makeText(c, "Keine Netzwerkverbindung!", Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }
    @Override
    public void onViewCreated(View v, @Nullable Bundle sis){
        test=v.findViewById(R.id.imageView);
        net.makeImageRequest(ref,"test","/1.jpg");
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("fwg",name);
        if(name.contentEquals("test")){
            test.setImageBitmap(image);
        }
    }
}
package de.fwg.qr.scanner.map;

import android.content.Context;
import android.graphics.Bitmap;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class mapBuilding implements networkCallbackInterface {

    private Context context;

    private ArrayList<Bitmap> stockwerk;

    private Bitmap bitmap;

    private network net;
    private WeakReference<networkCallbackInterface> ref;

    private int i = 0;

    public mapBuilding(Context context, int level) {
        this.context = context;
        net = new network(context);
        ref = new WeakReference<>((networkCallbackInterface) this);
        String floor = "";
        switch (level) {
            case 0:
                floor = "Erdgeschoss";
                break;
            case 1:
                floor = "1. Stockwerk";
                break;
            case 2:
                floor = "2.Stockwerk";
                break;
        }
        net.makeImageRequest(ref, "ImageRequest", floor, i, true);
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {

    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}

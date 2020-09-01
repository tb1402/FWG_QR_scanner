package de.fwg.qr.scanner.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackImageID;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class mapBuilding implements networkCallbackImageID {

    private Bitmap result;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint paint;

    private final static int AMOUNT_OF_STATIONS = 25;
    private network net;
    private WeakReference<networkCallbackInterface> ref;
    private Context context;

    private int i = 0;
    private ArrayList<String> allObtainedStationNames;
    private boolean check = false;

    public mapBuilding(Context context, int level, ArrayList<String> stationNames) {
        this.context = context;
        net = new network(context);
        ref = new WeakReference<>((networkCallbackInterface) this);
        if (level >= 0 && level <= 2) {

        } else {
            Intent i = new Intent(context, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, "Usage of wrong level ID");
            context.startActivity(i);
        }

        if (stationNames != null) {
            allObtainedStationNames = stationNames;
        } else {
            allObtainedStationNames.add("NamesNotFound");
        }
        getImages(level);
    }

    @Override
    public void onPostCallback(String operation, String response) {
    }

    @Override
    public void onImageCallback(String name, Bitmap image, int number) {
        if (name.contentEquals("ImageRequest")) {
            bitmap = addBitmapToMap(image, number);
        }
    }


    public Bitmap getBitmap() {
        if (check) {
            return bitmap;
        } else {
            return null;
        }
    }


    public void getImages(int level) {
        if (level == 0) {
            for (int j = 0; j < AMOUNT_OF_STATIONS; j++) {
                if (j == 15 || j == 17) {
                    j++;
                }
                net.makeImageRequest(ref, "ImageRequest", "map", j, true);
            }
        } else if (level != -1) {
            i = 24;
            switch (level) {
                case 1:
                    net.makeImageRequest(ref, "ImageRequest", "map", 15, true);
                    break;
                case 2:
                    net.makeImageRequest(ref, "ImageRequest", "map", 17, true);
            }
        }
    }

    private Bitmap addBitmapToMap(Bitmap newImage, int number) {
        i++;
        if (result == null) {
            result = Bitmap.createBitmap(newImage.getWidth(), newImage.getHeight(), newImage.getConfig());
        }
        if (canvas == null) {
            canvas = new Canvas(result);
        }
        if (paint == null) {
            paint = new Paint();
        }
        if (allObtainedStationNames.lastIndexOf("map" + number) != -1) {
            paint.setAlpha(255);
        } else {
            paint.setAlpha(100);
        }
        canvas.drawBitmap(newImage, 0f, 0f, paint);
        if (i >= (AMOUNT_OF_STATIONS - 1)) {
            check = true;
        }
        return result;
    }
}
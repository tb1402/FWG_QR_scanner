package de.fwg.qr.scanner.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackImageID;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class mapBuilding implements networkCallbackImageID {

    private Context context;

    private ArrayList<Bitmap> stockwerk;

    private Bitmap result;
    private Bitmap bitmap;
    private Canvas canvas;

    private network net;
    private WeakReference<networkCallbackInterface> ref;

    private String floor;
    private String[] allObtainedID;

    private int i = 0;
    private int length = -1;

    //private Intent intent;

    public mapBuilding(Context context, int level, String JSONobject, String[] ids) {
        this.context = context;
        net = new network(context);
        ref = new WeakReference<>((networkCallbackInterface) this);
        try {
            JSONObject json = new JSONObject(JSONobject);
            switch (level) {
                case 0:
                    floor = json.getString("Erdgeschoss");
                    break;
                case 1:
                    floor = json.getString("ersterStock");
                    break;
                case 2:
                    floor = json.getString("zweiterStock");
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        allObtainedID = ids;
        getImages();
    }

    @Override
    public void onPostCallback(String operation, String response) {
        if (operation.contains("error") || response.contains("Error") || response.contains("error")) {
            Intent i = new Intent(context, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, response);
            context.startActivity(i);
        }
        if (operation.contentEquals("getInfo")) {
            try {
                JSONObject object = new JSONObject(response);
                length = Integer.parseInt(object.getString("Bild"));

            } catch (JSONException e) {
                //intent = new Intent();
                Intent i = new Intent(context, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                context.startActivity(i);
            }

        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image, int number) {
        if (name.contentEquals("ImageRequest")) {
            bitmap = addBitmapToMap(image);
        }
    }


    public Bitmap getBitmap() {
        return bitmap;
    }


    public void getImages() {
        //for (int x = 0; x < floor.length; x++) {
        for (int j = 0; j < length; j++) {
            net.makeImageRequest(ref, "ImageRequest", floor, j, true);
        }
        //}
    }

    private Bitmap addBitmapToMap(Bitmap newImage) {
        if (result == null) {
            result = Bitmap.createBitmap(newImage.getWidth(), newImage.getHeight(), newImage.getConfig());
        }
        if (canvas == null) {
            canvas = new Canvas(result);
        }
        canvas.drawBitmap(newImage, 0f, 0f, null);
        return result;
    }
}
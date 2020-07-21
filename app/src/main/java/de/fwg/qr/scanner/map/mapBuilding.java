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

    private Bitmap bitmap;
    private Canvas canvas;

    private network net;
    private WeakReference<networkCallbackInterface> ref;

    private String[] floor;

    private int i = 0;
    private int length = -1;

    private Intent intent;

    public mapBuilding(Context context, String object, String[] ids) {
        this.context = context;
        net = new network(context);
        ref = new WeakReference<>((networkCallbackInterface) this);
        try {
            JSONObject json = new JSONObject(object);
            /*switch (level) {
                case 0:
                    floor = json.getString("Erdgeschoss");
                    break;
                case 1:
                    floor = json.getString("ersterStock");
                    break;
                case 2:
                    floor = json.getString("zweiterStock");
                    break;

            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
        floor = new String[]{"Erdgeschoss", "stock1", "stock2"};
        net.makeImageRequest(ref, "ImageRequest", floor[0], i, true);
    }

    @Override
    public void onPostCallback(String operation, String response) {
        if (operation.contains("error") || response.contains("Error") || response.contains("error")) {
            intent = new Intent();
            Intent i = new Intent(context, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, response);
            context.startActivity(intent);
        }
        if (operation.contentEquals("getInfo")) {
            try {
                JSONObject object = new JSONObject(response);
                length = Integer.parseInt(object.getString("Bild"));

            } catch (JSONException e) {
                intent = new Intent();
                Intent i = new Intent(context, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                context.startActivity(intent);
            }

        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image, int number) {
        if (name.contentEquals("ImageRequest")) {


        }
    }


    public Bitmap getBitmap() {
        return bitmap;
    }


    public void getImages() {
        for (int x = 0; x < floor.length; x++) {
            for (int j = 0; j < length; j++) {
                net.makeImageRequest(ref, "ImageRequest", floor[x], j, true);
            }
        }
    }
}

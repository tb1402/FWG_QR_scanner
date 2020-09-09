package de.fwg.qr.scanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.tools.cache.cacheManager;
import de.fwg.qr.scanner.tools.cache.readCacheCallback;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.networkCallbackImageID;
import de.fwg.qr.scanner.tools.preferencesManager;
import de.fwg.qr.scanner.progress.progressManager;
import de.fwg.qr.scanner.activityErrorHandling;
import de.fwg.qr.scanner.history.historyEntry;
import de.fwg.qr.scanner.history.taskResultCallback;


public class activityMap extends toolbarWrapper implements networkCallbackInterface, readCacheCallback {

    private ImageView imageView;
    private ArrayList<Bitmap> images;
    private TextView textView;
    private ProgressBar progressBar;

    private RadioButton button1;
    private RadioButton button2;
    private RadioButton button3;

    private network net;
    private WeakReference<networkCallbackInterface> ref;
    private static int AMOUNT_OF_STATIONS = 0;

    private WeakReference<readCacheCallback> cacheRef;
    private cacheManager cm;

    private int i = 0;
    private static int[] AMOUNT_OF_STATIONS_PER_LEVEL;
    private preferencesManager manager;
    private int currentLevel = 0;
    private ArrayList<Integer> allObtainedStationNames;
    private JSONObject mapData;
    private JSONArray stationData;


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);
        final Context c = this;
        net = new network(this);
        ref = new WeakReference<>((networkCallbackInterface) this);
        manager = new preferencesManager(this);
        cacheRef = new WeakReference<>((readCacheCallback) this);
        cm = new cacheManager(getApplicationContext());
        images = new ArrayList<>();
        allObtainedStationNames = new ArrayList<>();
        AMOUNT_OF_STATIONS_PER_LEVEL = new int[4];
        setupAbHome();
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        button1 = findViewById(R.id.radioButton1);
        button2 = findViewById(R.id.radioButton2);
        button3 = findViewById(R.id.radioButton3);
        getMapParts(new taskResultCallback<String[]>() {
            @Override
            public void onFinished(String[] result) {
                for (int i = 0; i < result.length; i++) {
                    allObtainedStationNames.add(Integer.parseInt(result[i]));
                }
                net.makePostRequest(ref, "getMapData", "");
            }
        });
    }

    public void onRadioButtonClicked(View view) { //TODO: altering certain parts of Method; adding fourth radio button, change of currentLevel

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButton1:
                if (checked) {
                    if (images.size() >= 1) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        //imageView.setImageBitmap(images.get(0));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.radioButton2:
                if (checked) {
                    if (images.size() >= 2) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(1));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.radioButton3:
                if (checked) {
                    if (images.size() >= 3) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(2));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    @Override
    public void onPostCallback(String operation, String response) {
        if (operation.contentEquals("getMapData")) {
            try {
                mapData = new JSONObject(response);
                if (!mapData.getString("status").contentEquals("ok")) {
                    Intent i = new Intent(this, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, "mapData status not ok");
                    startActivity(i);
                }
                stationData = mapData.getJSONArray("stations");
                AMOUNT_OF_STATIONS = stationData.length();
                AMOUNT_OF_STATIONS_PER_LEVEL = getStationsPerLevel();
                //getImages(level); TODO: level value
            } catch (JSONException e) {
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image) { //TODO: start working on this method
        if (name.contentEquals("ImagePreview")) {
            images.add(image);
            if (images.size() >= 1) {
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                if (images.size() == 1) {
                    imageView.setImageBitmap(images.get(0));
                } else if (images.size() == 2 && button2.isChecked()) {
                    imageView.setImageBitmap(images.get(1));
                } else if (images.size() == 3 && button3.isChecked()) {
                    imageView.setImageBitmap(images.get(2));
                }
            }
            i++;
            if (i < 3) {
                //getImages();
            } else {
                if (button1.isChecked()) {
                    imageView.setImageBitmap(images.get(0));
                } else if (button2.isChecked()) {
                    imageView.setImageBitmap(images.get(1));
                } else if (button3.isChecked()) {
                    imageView.setImageBitmap(images.get(2));
                }
            }

        }

    }


    public int[] getStationsPerLevel() { //Methode um zu wissen, wieviele Stationen pro Stockwerk vorhanden sind
        int[] array = new int[4];
        for (int j = 0; j < stationData.length(); j++) {
            try {
                switch (Integer.parseInt(stationData.getJSONObject(j).getString("floor"))) {
                    case (-1):
                        array[0]++; //Untergeschoss
                        break;
                    case (0):
                        array[1]++; //Erdgeschoss
                        break;
                    case (1):
                        array[2]++; //1. Stock
                        break;
                    case (2):
                        array[3]++; //2. Stock
                        break;
                    default:
                        Intent i = new Intent(this, activityErrorHandling.class);
                        i.putExtra(activityErrorHandling.errorNameIntentExtra, "Error with methode getStationsPerLevel");
                        startActivity(i);
                        break;
                }
            } catch (JSONException e) {
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        }
        array[0]--; //Wegen Weg zur Sporthalle, ansonsten später Fehler bei getImages()
        return array;
    }

    //TODO: implementing cacheManager
    public void getImages(int level) { //TODO: CHange name of request for every request that wants the next station in rally mode
        net.makeImageRequest(ref, "FloorRequest", "mapFloors", level, true);
        switch (level) {
            case 0:
                for (int j = 0; j < AMOUNT_OF_STATIONS_PER_LEVEL[1]; j++) { //Alle Stationen vom ersten Stock werden durchlauft
                    if (allObtainedStationNames.lastIndexOf(j) != -1) { // Nur wenn station besucht wurde wird netrequest gemacht
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", j, true);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1)) { // Wenn die letzte eingescannte station nicht die letzte Station des Stockwerkes ist, wird die nächste Station geladen (AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1, weil das bei 1 beginnt, die erhaltenen Stationen bei 0
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true);
                    }
                }
                break;
            case -1:
                for (int j = AMOUNT_OF_STATIONS_PER_LEVEL[1]; j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0]); j++) {
                    if (allObtainedStationNames.lastIndexOf(j) != -1) {
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", j, true);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1 && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1)) {
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true);
                    }
                }
                break;
            case 1:
                for (int j = (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0]); j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2]); j++) {
                    if (allObtainedStationNames.lastIndexOf(j) != -1) {
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", j, true);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1)) {
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true);
                    }
                }
                break;
            case 2:
                for (int j = (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2]); j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 1); j++) { // -1 in for-Schleife wirchtig, letzte station gehört zum Erdgeschoss
                    if (allObtainedStationNames.lastIndexOf(j) != -1) {
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", j, true);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1) && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 2)) { //-2 wegen selben gründen wie bei der for-schleife
                        net.makeImageRequest(ref, "ImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true);
                    }
                }
                break;
            default:
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, "Error with methode getImages; Wrong level id");
                startActivity(i);
                return;

        }
        if (manager.isRallyeMode()) { //Letzte Station ist Speziallfall, ist nicht nach Stockwerk geordnet und ersetzt den Erdgeschoss
            if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) == (AMOUNT_OF_STATIONS - 2)) {
                net.makeImageRequest(ref, "ImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true);
            }
        }
    }

    public Bitmap mergeAllBitmaps(ArrayList<Bitmap> bitmaps) {
        Bitmap result = Bitmap.createBitmap(bitmaps.get(1).getWidth(), bitmaps.get(1).getHeight(), bitmaps.get(1).getConfig()); //TODO using floor bitmap as base
        Canvas canvas = new Canvas(result); //TODO making canvas global for dealing with paint problem
        Paint paint = new Paint();
        for (int i = 0; i < bitmaps.size(); i++) {
            if (allObtainedStationNames.lastIndexOf(i) != -1) {
                paint.setAlpha(255);
            } else {
                paint.setAlpha(100);
            }
            canvas.drawBitmap(bitmaps.get(i), 0f, 0f, null);
        }
        return result;
    }


    public void getMapParts(final taskResultCallback<String[]> callback) {

        progressManager prog = new progressManager(this);
        prog.getUniqueStationsAsync(new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] result) {

                JSONArray arr = new JSONArray();
                for (int i = 0; i < result.length; i++) {
                    arr.put(result[i].StationId);
                }
                String json = arr.toString();

                networkCallbackInterface nci = new networkCallbackInterface() {
                    @Override
                    public void onPostCallback(String operation, String response) {

                        try {
                            JSONArray ret = new JSONArray(response);

                            String[] mapfragments = new String[ret.length()];
                            for (int j = 0; j < ret.length(); j++) {
                                mapfragments[j] = ret.getString(j);
                            }
                            callback.onFinished(mapfragments);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onImageCallback(String name, Bitmap image) {

                    }
                };
                net.makePostRequest(new WeakReference<networkCallbackInterface>(nci), "PermittedMapFragments", json);

            }
        });
    }


    @Override
    public void cacheCallback(boolean error, Bitmap image) { //TODO: same changes as in onImageCallback() needed
        if (!error) {
            images.add(image);
            if (images.size() >= 1) {
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                if (images.size() == 1) {
                    imageView.setImageBitmap(images.get(0));
                } else if (images.size() == 2 && button2.isChecked()) {
                    imageView.setImageBitmap(images.get(1));
                } else if (images.size() == 3 && button3.isChecked()) {
                    imageView.setImageBitmap(images.get(2));
                }
                i++;
                if (i < 3) {
                    //getImages();
                } else {
                    if (button1.isChecked()) {
                        imageView.setImageBitmap(images.get(0));
                    } else if (button2.isChecked()) {
                        imageView.setImageBitmap(images.get(1));
                    } else if (button3.isChecked()) {
                        imageView.setImageBitmap(images.get(2));
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.tb_item_settings:
                Intent i = new Intent(getApplicationContext(), activitySettings.class);
                startActivity(i);
                return true;
            case R.id.tb_item_map:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

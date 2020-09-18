package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import de.fwg.qr.scanner.history.historyEntry;
import de.fwg.qr.scanner.history.taskResultCallback;
import de.fwg.qr.scanner.progress.progressManager;
import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackImageID;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.preferencesManager;


/**
 * Activity to display and manage the map
 */
public class activityMap extends toolbarWrapper implements networkCallbackImageID {

    private ImageView imageView;

    private network net;
    private static int AMOUNT_OF_STATIONS = 0;
    private preferencesManager manager;

    //Canvas where all bitmaps get drawn to
    private Canvas canvas;
    private Bitmap bitmapOfImageView;
    private static int[] AMOUNT_OF_STATIONS_PER_LEVEL;

    //Bitmap returned by image request methods
    private Bitmap result;

    private int currentLevel = 0;
    //Important for hindering user to load same floor over and over again
    private int check = -2;
    private ArrayList<Integer> allObtainedStationNames;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);//prevent screenshots and video capture (not supported by some devices)
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);

        manager = preferencesManager.getInstance(this);
        if (!manager.areFeaturesUnlocked()) {
            finish();
        }

        imageView=findViewById(R.id.imageView);
        net = network.getInstance(getApplicationContext());
        allObtainedStationNames = new ArrayList<>();
        AMOUNT_OF_STATIONS_PER_LEVEL = new int[4];
        setupAbHome();
        final networkCallbackInterface nci=this;
        getMapParts(new taskResultCallback<String[]>() {
            @Override
            public void onFinished(String[] result) {
                for (String s : result) {
                    allObtainedStationNames.add(Integer.parseInt(s));
                }
                net.makePostRequest(nci, "getMapData", "",getApplicationContext());
            }
        });
    }

    /**
     * Method for changing between radioButtons grouped under radioGroup
     * All RadioButtons: *1 Main floor
     * *2 Basement
     * *3 First floor
     * *4 Second floor
     *
     * @param view View provided by radioButtons
     */
    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();


        switch (view.getId()) {
            case R.id.radioButton1: //Erdgeschoss
                if (checked) {
                    currentLevel = 0;
                    if (check != currentLevel) {
                        getImages(currentLevel);
                    }
                }
                break;
            case R.id.radioButton2: //Untergeschoss
                if (checked) {
                    currentLevel = -1;
                    if (check != currentLevel) {
                        getImages(currentLevel);
                    }
                }
                break;
            case R.id.radioButton3: //1. Stock
                if (checked) {
                    currentLevel = 1;
                    if (check != currentLevel) {
                        getImages(currentLevel);
                    }
                    break;
                }
            case R.id.radioButton4: // 2. Stock
                if (checked) {
                    currentLevel = 2;
                    if (check != currentLevel) {
                        getImages(currentLevel);
                    }
                }
        }
    }

    @Override
    public void onPostCallback(String operation, String response) {
        if (operation.contentEquals("getMapData")) {
            try {
                JSONObject mapData = new JSONObject(response);
                if (!mapData.getString("status").contentEquals("ok")) {
                    Intent i = new Intent(this, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, "mapData status not ok");
                    startActivity(i);
                }
                JSONArray stationData = mapData.getJSONArray("stations");
                AMOUNT_OF_STATIONS = stationData.length();
                AMOUNT_OF_STATIONS_PER_LEVEL = getStationsPerLevel(stationData);
                if (manager.isRallyeMode()) {
                    currentLevel = getLevelPerId(allObtainedStationNames.get(allObtainedStationNames.size() - 1));
                    switch (currentLevel) {
                        case 0:
                            RadioButton button1 = findViewById(R.id.radioButton1);
                            button1.setChecked(true);
                            break;
                        case -1:
                            RadioButton button2 = findViewById(R.id.radioButton2);
                            button2.setChecked(true);
                            break;
                        case 1:
                            RadioButton button3 = findViewById(R.id.radioButton3);
                            button3.setChecked(true);
                            break;
                        case 2:
                            RadioButton button4 = findViewById(R.id.radioButton4);
                            button4.setChecked(true);
                            break;
                    }
                }
                getImages(currentLevel);
            } catch (JSONException e) {
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image) {


    }

    @Override
    public void onImageCallback(String name, Bitmap image, int number) {
        if (!name.contentEquals("FloorRequest") && !name.contentEquals("FinalStage")) {
            switch (currentLevel) {
                case 0:
                    if (AMOUNT_OF_STATIONS_PER_LEVEL[1] <= number) {
                        return;
                    }
                    break;
                case -1:
                    if ((AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) <= number || AMOUNT_OF_STATIONS_PER_LEVEL[1] > number) {
                        return;
                    }
                    break;
                case 1:
                    if ((AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1) <= number || (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) > number) {
                        return;
                    }
                    break;
                case 2:
                    if ((AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3]) <= number || (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[2]) > number) {
                        return;
                    }
                    break;
            }
        }
        if (name.contentEquals("FloorRequest")) {
            if (number != currentLevel) {
                return;
            }
            bitmapOfImageView = floorRequest(image);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmapOfImageView);
                }
            });

        } else if (name.contentEquals("ImageRequest")) {
            bitmapOfImageView = imageRequest(image);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmapOfImageView);
                }
            });

        } else if (name.contentEquals("NextImageRequest")) {
            bitmapOfImageView = nextImageRequest(image);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmapOfImageView);
                }
            });

        } else if (name.contentEquals("FinalStage")) {
            if (currentLevel == 0) {
                final Bitmap bit = nextImageRequest(image);
                canvas = null;
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bit);
                    }
                });
            }

        }
    }


    /**
     * Method for knowing how many Stations there are per level (Last station excluded)
     *
     * @param stationData JSONArray containing all Information about all Stations
     * @return int array index: *0 Basement
     * *1 Main floor
     * *2 First floor
     * *3 Second floor
     */
    public int[] getStationsPerLevel(JSONArray stationData) {
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
                        i.putExtra(activityErrorHandling.errorNameIntentExtra, "Error with method getStationsPerLevel");
                        startActivity(i);
                        break;
                }
            } catch (JSONException e) {
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        }
        array[1]--; //Wegen Weg zur Sporthalle, ansonsten später Fehler bei getImages()
        return array;
    }

    /**
     * Method for making all ImageRequests based on the current Level
     *
     * @param level current Level
     */

    public void getImages(int level) {
        canvas = null;
        check = level;
        if (allObtainedStationNames.size() == 0) {
            net.makeImageRequestWithIDCallback(this, "FloorRequest", "mapFloors", level, true,this);
            if (manager.isRallyeMode()) {
                net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.size(), true,this);
            }
            return;
        }
        if (manager.isRallyeMode() && currentLevel == 0) { //Letzte Station ist Speziallfall, ist nicht nach Stockwerk geordnet und ersetzt den Erdgeschoss
            if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) == (AMOUNT_OF_STATIONS - 2)) {
                net.makeImageRequestWithIDCallback(this, "FinalStage", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true,this);
                return;
            }
        }
        net.makeImageRequestWithIDCallback(this, "FloorRequest", "mapFloors", level, true,this);
        switch (level) {
            case 0:
                for (int j = 0; j < AMOUNT_OF_STATIONS_PER_LEVEL[1]; j++) { //Alle Stationen vom ersten Stock werden durchlauft
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) { // Nur wenn station besucht wurde wird netrequest gemacht
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true,this);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && currentLevel == level) { // Wenn die letzte eingescannte station nicht die letzte Station des Stockwerkes ist, wird die nächste Station geladen (AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1, weil das bei 1 beginnt, die erhaltenen Stationen bei 0
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true,this);
                    }
                }
                break;
            case -1:
                for (int j = AMOUNT_OF_STATIONS_PER_LEVEL[1]; j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0]); j++) {
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true,this);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1 && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true,this);
                    }
                }
                break;
            case 1:
                for (int j = (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0]); j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2]); j++) {
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true,this);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1) && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true,this);
                    }
                }
                break;
            case 2:
                for (int j = (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2]); j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 1); j++) { // -1 in for-Schleife wirchtig, letzte station gehört zum Erdgeschoss
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true,this);
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1) && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 2) && currentLevel == level) { //-2 wegen selben gründen wie bei der for-schleife
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true,this);
                    }
                }
                break;
            default:
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, "Error with methode getImages; Wrong level id");
                startActivity(i);

        }
    }

    /**
     * Method for adding image to canvas
     *
     * @param bitmap new bitmap drawn on canvas
     * @return bitmap of canvas
     */

    public Bitmap imageRequest(Bitmap bitmap) {
        if (result == null || canvas == null) {
            result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            canvas = new Canvas(result);
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null);
        return result;
    }

    /**
     * Method for adding image of floor to canvas
     *
     * @param bitmap floor gets drawn on canvas
     * @return bitmap of canvas
     */

    public Bitmap floorRequest(Bitmap bitmap) {
        if (result == null || canvas == null) {
            result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            canvas = new Canvas(result);
        }
        canvas.drawBitmap(bitmap, 0f, 0f, null);
        return result;
    }

    /**
     * Method for adding image which should be the next one to be scanned when using app in Rally-Mode
     *
     * @param bitmap new bitmap drawn opaque on canvas
     * @return bitmap of canvas
     */

    public Bitmap nextImageRequest(Bitmap bitmap) {
        if (result == null || canvas == null) {
            result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            canvas = new Canvas(result);
        }
        Paint paint = new Paint();
        paint.setAlpha(100);
        canvas.drawBitmap(bitmap, 0f, 0f, paint);
        return result;
    }

    /**
     * Method for knowing which id is on which level
     *
     * @param id ID you want to know the floor for
     * @return number of floor, if not found, return -2
     */
    private int getLevelPerId(int id) {
        int[] array = {0, AMOUNT_OF_STATIONS_PER_LEVEL[1], AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1], AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[2]};
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] <= id) {
                switch (i) {
                    case 3:
                        return 2;
                    case 2:
                        return 1;
                    case 1:
                        return -1;
                    case 0:
                        return 0;
                    default:
                        return 0;
                }

            }
        }
        return -2;
    }

    /**
     * Methode for getting String array with all previously visited stations
     *
     * @param callback in order to get string returned
     */

    public void getMapParts(final taskResultCallback<String[]> callback) {

        progressManager prog = new progressManager(this);
        prog.getUniqueStationsAsync(new taskResultCallback<historyEntry[]>() {
            @Override
            public void onFinished(historyEntry[] result) {

                JSONArray arr = new JSONArray();
                for (de.fwg.qr.scanner.history.historyEntry historyEntry : result) {
                    arr.put(historyEntry.StationId);
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
                net.makePostRequest(nci, "PermittedMapFragments", json,getApplicationContext());

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.tb_item_settings) {
            Intent i = new Intent(getApplicationContext(), activitySettings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

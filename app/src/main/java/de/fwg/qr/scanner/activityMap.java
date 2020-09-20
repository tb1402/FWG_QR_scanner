package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

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
    private ProgressBar progressBar;
    private TextView textView;

    private network net;
    private static int AMOUNT_OF_STATIONS = 0;
    private static int AMOUNT_OF_IMAGE_REQUESTS = -1;
    private preferencesManager manager;

    //Canvas where all bitmaps get drawn to
    private Canvas canvas;
    private Bitmap bitmapOfImageView;
    private static int[] AMOUNT_OF_STATIONS_PER_LEVEL;

    // Constants for drawing
    private final int BITMAP_SIDELENGTH = 1254;
    private final float NEXT_MARKER_TIP_X = 64f / 128f;
    private final float NEXT_MARKER_TIP_Y = 118f / 128f;
    private final float MARKER_SIZE_SCALING = 0.37f;
    private final float CURRENT_MARKER_MIDPOINT = 0.5f;
    private final float ARROW_SIZE_SCALING = 0.50f;
    private final float MARKINGS_OPACITY = 0.60f;
    private JSONArray arrowCoords;

    //Bitmap returned by image request methods
    private Bitmap result;

    private int currentLevel = 0;
    //Important for hindering user to load same floor over and over again
    private int check = -2;
    private ArrayList<Integer> allObtainedStationNames;
    private JSONArray stationData;


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);//prevent screenshots and video capture (not supported by some devices)
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);

        manager = preferencesManager.getInstance(this);
        if (!manager.areFeaturesUnlocked()) {
            finish();
        }

        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.textView);
        net = network.getInstance(getApplicationContext());
        allObtainedStationNames = new ArrayList<>();
        AMOUNT_OF_STATIONS_PER_LEVEL = new int[4];
        setupAbHome();
        final networkCallbackInterface nci = this;
        getMapParts(new taskResultCallback<String[]>() {
            @Override
            public void onFinished(String[] result) {
                for (String s : result) {
                    allObtainedStationNames.add(Integer.parseInt(s));
                }
                net.makePostRequest(nci, "getMapData", "", getApplicationContext());
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
                        if (progressBar.getVisibility() == View.INVISIBLE || textView.getVisibility() == View.INVISIBLE) {
                            progressBar.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                        }
                        getImages(currentLevel);
                    }
                }
                break;
            case R.id.radioButton2: //Untergeschoss
                if (checked) {
                    currentLevel = -1;
                    if (check != currentLevel) {
                        if (progressBar.getVisibility() == View.INVISIBLE || textView.getVisibility() == View.INVISIBLE) {
                            progressBar.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                        }
                        getImages(currentLevel);
                    }
                }
                break;
            case R.id.radioButton3: //1. Stock
                if (checked) {
                    currentLevel = 1;
                    if (check != currentLevel) {
                        if (progressBar.getVisibility() == View.INVISIBLE || textView.getVisibility() == View.INVISIBLE) {
                            progressBar.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                        }
                        getImages(currentLevel);
                    }
                    break;
                }
            case R.id.radioButton4: // 2. Stock
                if (checked) {
                    currentLevel = 2;
                    if (check != currentLevel) {
                        if (progressBar.getVisibility() == View.INVISIBLE || textView.getVisibility() == View.INVISIBLE) {
                            progressBar.setVisibility(View.VISIBLE);
                            textView.setVisibility(View.VISIBLE);
                        }
                        getImages(currentLevel);
                    }
                }
        }
    }

    /**
     * Implementation of the networkCallbackInterface listening for the callback of the {@code ref2} Attribute
     * @param operation name of requested php file
     * @param response response from the server
     */
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
                stationData = mapData.getJSONArray("stations");
                AMOUNT_OF_STATIONS = stationData.length();
                AMOUNT_OF_STATIONS_PER_LEVEL = getStationsPerLevel(stationData);
                if (manager.isRallyeMode() && allObtainedStationNames.size() != 0) {

                    currentLevel = getLevelPerId(allObtainedStationNames.get(allObtainedStationNames.size() - 1));
                    switch (currentLevel) {
                        case 0:
                            RadioButton button1 = findViewById(R.id.radioButton1);
                            if (!button1.isChecked()) {
                                button1.setChecked(true);
                            }
                            break;
                        case -1:
                            RadioButton button2 = findViewById(R.id.radioButton2);
                            if (!button2.isChecked()) {
                                button2.setChecked(true);
                            }
                            break;
                        case 1:
                            RadioButton button3 = findViewById(R.id.radioButton3);
                            if (!button3.isChecked()) {
                                button3.setChecked(true);
                            }
                            break;
                        case 2:
                            RadioButton button4 = findViewById(R.id.radioButton4);
                            if (!button4.isChecked()) {
                                button4.setChecked(true);
                            }
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
        if (progressBar.getVisibility() == View.VISIBLE || textView.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
        }
        if (!name.contentEquals("FloorRequest") && !name.contentEquals("FinalStage")) {
            if (getLevelPerId(number) != currentLevel) {
                return;
            }
        }
        AMOUNT_OF_IMAGE_REQUESTS--;
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

            // request the data where to draw the arrows exactly:
            /*net.makePostRequest(new networkCallbackInterface() {
                @Override
                public void onPostCallback(String operation, String response) {
                    try {
                        arrowCoords = new JSONArray(response);

                        // Next Image Request is only called in ralley mode, arrows and markers are drawn in ralley mode aswell,
                        // drawing them after the images to avoid wrong layering
                        getCurrentMarkings(stationData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onImageCallback(String name, Bitmap image) {

                }
            }, "ArrowCoords", "", getApplicationContext());*/


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
        if (AMOUNT_OF_IMAGE_REQUESTS == 0) {
            // request the data where to draw the arrows exactly:
            net.makePostRequest(new networkCallbackInterface() {
                @Override
                public void onPostCallback(String operation, String response) {
                    try {
                        arrowCoords = new JSONArray(response);

                        // Next Image Request is only called in ralley mode, arrows and markers are drawn in ralley mode aswell,
                        // drawing them after the images to avoid wrong layering
                        getCurrentMarkings(stationData);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onImageCallback(String name, Bitmap image) {

                }
            }, "ArrowCoords", "", getApplicationContext());
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
     * Method for drawing the Arrows dependant on the highest index of the station progress
     */
    public void getCurrentMarkings(JSONArray stationData){



        // next station after the highest index in tne visited stations
        int nextStation;
        if(allObtainedStationNames.size() == 0){ // No station visited yet
            nextStation = 0; // Lead to the first station
        }
        else
            nextStation = allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1; // highest indexed station plus 1


        // get the data about the next station
        try {

            // find the index of the next object bearing the nextStation Id
            int nextIndex = -1;
            for (int i = 0; i < stationData.length(); i++) { //Todo nullPointerException
                if (stationData.getJSONObject(i).getInt("mapId") == nextStation) {
                    nextIndex = i;
                }
            }

            JSONObject stationEntry = stationData.getJSONObject(nextIndex);
            String markerpos = stationEntry.getString("markerPos");
            int stationFloor = stationEntry.getInt("floor");
            JSONArray arrows = stationEntry.getJSONArray("arrows");



            // check if there are two markers:
            if(markerpos.contains("_")){
                // use the underscore as a delimiter to determine the multiple coordinates
                String[] markers =  markerpos.split("_");
                int[] xPos = new int[markers.length];
                int[] yPos = new int[markers.length];

                for(int i = 0; i < markers.length; i++){
                    xPos[i] = Integer.parseInt(markers[i].split(",")[0]);
                    yPos[i] = Integer.parseInt(markers[i].split(",")[1]);

                }

                for(int i = 0; i < markers.length; i++){

                    bitmapOfImageView = drawNextMarker(xPos[i], yPos[i], stationFloor); // call the draw method on all points
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmapOfImageView);
                        }
                    });
                }

            }
            else{
                int xPos = Integer.parseInt(markerpos.split(",")[0]);
                int yPos = Integer.parseInt(markerpos.split(",")[1]);
                bitmapOfImageView = drawNextMarker(xPos, yPos, stationFloor); // call the draw method on all points
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmapOfImageView);
                    }
                });

            }


            // draw the marker of the currently visited station
            if(allObtainedStationNames.size() != 0){ // Draw only if there was a station visited last
                int currentStation = nextStation - 1;

                int currentIndex = -1;
                // find the index of the current station
                for(int i = 0; i <  stationData.length(); i++){
                    if(stationData.getJSONObject(i).getInt("mapId") == currentStation){
                        currentIndex = i;
                    }
                }
                JSONObject currentStationEntry = stationData.getJSONObject(currentIndex);

                String currmarkerpos = currentStationEntry.getString("markerPos");
                int currStationFloor = currentStationEntry.getInt("floor");

                // check if there are two markers:
                if(currmarkerpos.contains("_")){
                    // use the underscore as a delimiter to determine the multiple coordinates
                    String[] currmarkers =  currmarkerpos.split("_");
                    int[] xPos = new int[currmarkers.length];
                    int[] yPos = new int[currmarkers.length];

                    for(int i = 0; i < currmarkers.length; i++){
                        xPos[i] = Integer.parseInt(currmarkers[i].split(",")[0]);
                        yPos[i] = Integer.parseInt(currmarkers[i].split(",")[1]);

                    }

                    for(int i = 0; i < currmarkers.length; i++){

                        bitmapOfImageView = drawCurrentMarker(xPos[i], yPos[i], currStationFloor); // call the draw method on all points
                        this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmapOfImageView);
                            }
                        });
                    }

                }
                else{
                    int xPos = Integer.parseInt(currmarkerpos.split(",")[0]);
                    int yPos = Integer.parseInt(currmarkerpos.split(",")[1]);
                    bitmapOfImageView = drawCurrentMarker(xPos, yPos, currStationFloor); // call the draw method on all points
                    this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(bitmapOfImageView);
                        }
                    });

                }


            }


            // parse out the Arrows
            for(int i = 0; i < arrows.length(); i++){

                JSONArray arrow = arrows.getJSONArray(i);

                String[] posdata = arrow.getString(1).split(",");
                int x =  Integer.parseInt(posdata[0]);
                int y = Integer.parseInt(posdata[1]);

                String arrowname = arrow.getString(0);
                int floor = arrow.getInt(2);


                bitmapOfImageView = drawArrow(arrowname, x, y, floor); // call the method to draw on each arrow
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmapOfImageView);
                    }
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private Paint getMarkerOpacity(){
        Paint p = new Paint();
        p.setAlpha((int)(255 * MARKINGS_OPACITY));
        return p;
    }

    /**
     * Method draws a marker on the canvas
     * @param x  x-position of the markers tip
     * @param y y-position of the markers tip
     * @param floor Floor of the marker, only drawn if the map is currently in the right floor
     */
    private Bitmap drawNextMarker(int x, int y, int floor){
        if (currentLevel != floor) return result;

        Bitmap markerBmp = BitmapFactory.decodeResource(getResources(), R.raw.marker_next);

        if (result == null || canvas == null) {
            result = Bitmap.createBitmap(BITMAP_SIDELENGTH, BITMAP_SIDELENGTH, markerBmp.getConfig());
            canvas = new Canvas(result);
        }


        //calculate fitting startpoint:
        x -= markerBmp.getWidth() * NEXT_MARKER_TIP_X * MARKER_SIZE_SCALING;
        y -= markerBmp.getHeight() * NEXT_MARKER_TIP_Y * MARKER_SIZE_SCALING;

        markerBmp = Bitmap.createScaledBitmap(markerBmp, (int)(markerBmp.getWidth() * MARKER_SIZE_SCALING), (int)(markerBmp.getHeight() * MARKER_SIZE_SCALING), true);
        canvas.drawBitmap(markerBmp, x, y, getMarkerOpacity());
        return result;
    }

    /**
     * Method that draws the marker on the current, last visited station
     * (different method because it draws a different marker with different calculation of positions)
     * @param x x-position of the markers midpoint
     * @param y y-position of the markers midpoint
     * @param floor only drawn if the maplayer is currently showing that floor
     * @return
     */
    public Bitmap drawCurrentMarker(int x, int y, int floor){

        if(currentLevel == floor) {
            Bitmap markerBmp = BitmapFactory.decodeResource(getResources(), R.raw.marker_current);
            if (result == null || canvas == null) {
                result = Bitmap.createBitmap(BITMAP_SIDELENGTH, BITMAP_SIDELENGTH, markerBmp.getConfig());
                canvas = new Canvas(result);
            }
            x -= markerBmp.getWidth() * CURRENT_MARKER_MIDPOINT * MARKER_SIZE_SCALING;
            y -= markerBmp.getWidth() * CURRENT_MARKER_MIDPOINT * MARKER_SIZE_SCALING;

            markerBmp = Bitmap.createScaledBitmap(markerBmp, (int) (markerBmp.getWidth() * MARKER_SIZE_SCALING), (int) (markerBmp.getHeight() * MARKER_SIZE_SCALING), true);

            canvas.drawBitmap(markerBmp, x, y, getMarkerOpacity());
            return result;
        }
        return result;
    }

    /**
     * Method draws an Arrow on the mapCanvas
     * @param name Resource name of the Arrow in question in a String
     * @param x x-Position of the Arrow
     * @param y y-Position of the Arrow
     * @param floor Floor the Arrow is drawn in, only if the floor in question is currently active
     */
    private Bitmap drawArrow(String name, int x, int y, int floor) throws JSONException{
        if (currentLevel != floor) return result;

        int resourceId = getResourceByName(name);
        Bitmap arrowBmp = BitmapFactory.decodeResource(getResources(), resourceId);

        if (result == null || canvas == null) {
            result = Bitmap.createBitmap(BITMAP_SIDELENGTH, BITMAP_SIDELENGTH, arrowBmp.getConfig());
            canvas = new Canvas(result);
        }

        // get the offset out of the data array
        // find the entry where the arrow name equals the searched one
        for(int i = 0; i < arrowCoords.length(); i++){
            JSONObject obj = arrowCoords.getJSONObject(i);
            if(obj.getString("name").contentEquals(name)){

                x -= obj.getInt("tipX") * ARROW_SIZE_SCALING;
                y -= obj.getInt("tipY") * ARROW_SIZE_SCALING;

                arrowBmp = Bitmap.createScaledBitmap(arrowBmp, (int)(arrowBmp.getWidth() * ARROW_SIZE_SCALING), (int)(arrowBmp.getHeight() * ARROW_SIZE_SCALING), true);
                canvas.drawBitmap(arrowBmp, x, y, getMarkerOpacity());
                return result;
            }

        }
        return result;


    }

    private int getResourceByName(String name){
        return getApplicationContext().getResources().getIdentifier(name, "raw", getApplicationContext().getPackageName());
    }

    /**
     * Method for making all ImageRequests based on the current Level
     *
     * @param level current Level
     */

    public void getImages(int level) {
        canvas = null;
        check = level;
        AMOUNT_OF_IMAGE_REQUESTS = 0;
        if (allObtainedStationNames.size() == 0) {
            net.makeImageRequestWithIDCallback(this, "FloorRequest", "mapFloors", level, true, this);
            AMOUNT_OF_IMAGE_REQUESTS++;
            if (manager.isRallyeMode()) {
                net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.size(), true, this);
                AMOUNT_OF_IMAGE_REQUESTS++;
            }
            return;
        }
        if (manager.isRallyeMode() && currentLevel == 0) { //Letzte Station ist Speziallfall, ist nicht nach Stockwerk geordnet und ersetzt den Erdgeschoss
            if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) == (AMOUNT_OF_STATIONS - 2)) {
                net.makeImageRequestWithIDCallback(this, "FinalStage", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true, this);
                AMOUNT_OF_IMAGE_REQUESTS++;
                return;
            }
        }
        net.makeImageRequestWithIDCallback(this, "FloorRequest", "mapFloors", level, true, this);
        AMOUNT_OF_IMAGE_REQUESTS++;
        switch (level) {
            case 0:
                for (int j = 0; j < AMOUNT_OF_STATIONS_PER_LEVEL[1]; j++) { //Alle Stationen vom ersten Stock werden durchlaufen
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) { // Nur wenn station besucht wurde wird netrequest gemacht
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && currentLevel == level) { // Wenn die letzte eingescannte station nicht die letzte Station des Stockwerkes ist, wird die nächste Station geladen (AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1, weil das bei 1 beginnt, die erhaltenen Stationen bei 0
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                break;
            case -1:
                for (int j = AMOUNT_OF_STATIONS_PER_LEVEL[1]; j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0]); j++) {
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1 && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                break;
            case 1:
                for (int j = (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0]); j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2]); j++) {
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] - 1) && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1) && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                break;
            case 2:
                for (int j = (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2]); j < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 1); j++) { // -1 in for-Schleife wichtig, letzte station gehört zum Erdgeschoss
                    if (allObtainedStationNames.lastIndexOf(j) != -1 && currentLevel == level) {
                        net.makeImageRequestWithIDCallback(this, "ImageRequest", "mapFragments", j, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                if (manager.isRallyeMode()) {
                    if (allObtainedStationNames.get(allObtainedStationNames.size() - 1) >= (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] - 1) && allObtainedStationNames.get(allObtainedStationNames.size() - 1) < (AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 2) && currentLevel == level) { //-2 wegen selben gründen wie bei der for-schleife
                        net.makeImageRequestWithIDCallback(this, "NextImageRequest", "mapFragments", allObtainedStationNames.get(allObtainedStationNames.size() - 1) + 1, true, this);
                        AMOUNT_OF_IMAGE_REQUESTS++;
                    }
                }
                break;
            default:
                Intent i = new Intent(this, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, "Error with method getImages; Wrong level id");
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
        if (id >= (AMOUNT_OF_STATIONS_PER_LEVEL[0] + AMOUNT_OF_STATIONS_PER_LEVEL[1] + AMOUNT_OF_STATIONS_PER_LEVEL[2] + AMOUNT_OF_STATIONS_PER_LEVEL[3] - 1)) {
            return 0;
        }
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
     * Method for getting String array with the Id's of all allowed Map parts, ordered ascending
     *
     * @param callback in order to get string returned
     */

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
                net.makePostRequest(nci, "PermittedMapFragments", json, getApplicationContext());

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

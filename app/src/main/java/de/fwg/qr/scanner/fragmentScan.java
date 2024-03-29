package de.fwg.qr.scanner;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.fwg.qr.scanner.history.historyManager;
import de.fwg.qr.scanner.tools.cache.cacheManager;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
 * fragment to scan a qr code
 */
public class fragmentScan extends fragmentWrapper implements networkCallbackInterface {

    private CameraSource source;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surface = null;
    private TextView textView2;
    private ProgressBar pb;

    private preferencesManager pm;

    //boolean used to differentiate between a teacherCode is scanned (true) or not,
    //needed because the operation getPermission gets called when a code is scanned and on startup to verify the permission,
    //but the fragment needs only be recreated (otherwise camera and scan issues) when a code was scanned
    private boolean isTeacherCodeScanned = false;

    private String[] stationIds;

    /**
     * Intent made global to prevent (possibly multiple) called receiveDetection(s) to start a new intent each time
     */
    private Intent i = null;

    /**
     * Value received by detected barcode
     */
    private String barcodeValue = "";
    /**
     * Boolean for checking if activity is resuming for the first time or not; if it isn't the first time the fragment will be recreated because of camera issues
     */
    private boolean check = true;

    /**
     * Integer to know which station was scanned last
     */
    private int current;

    //boolean necessary because of barcode and cameraSource initialisation needing to be called when creating view; Checks if
    //processing of scan should take place
    private boolean detectionCheck = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (net.noNetworkAvailable(c)) {
            Toast.makeText(c, getString(R.string.network_no_connection), Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View v, @Nullable Bundle sis) {
        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 201);
        }
        surface = v.findViewById(R.id.surfaceView);
        textView2 = v.findViewById(R.id.textView2);
        pb = v.findViewById(R.id.progressBar);
        lockUI(true);
        pb.setVisibility(View.VISIBLE);
        pm = preferencesManager.getInstance(c);
        detectionCheck = false;
        net.makePostRequest(this, "getVersion", "", c);
        detection();
    }

    /**
     * If user always denies camera access, they'll be sent to the settings of his device
     *
     * @param requestCode  Code for permissionRequest
     * @param permissions  All permissions that were asked of user
     * @param grantResults Shows if permissions with same index were granted or denied
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == 201) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission); // Variable for knowing if user has always denied camera access, if so user gets sent to settings of his device
                    if (!showRationale) {
                        Toast.makeText(c, getString(R.string.permission_needed), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", a.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        a.finishAffinity();
                    }
                } else {
                    ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
                }
            }
        }
    }

    /**
     * Method that checks if reset button for rally mode data should be shown, only visible in rally mode
     *
     * @param menu menu provided by app
     */
    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        menu.findItem(R.id.tb_item_reset).setVisible(preferencesManager.getInstance(c).isRallyeMode());
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onPostCallback(String operation, String response) {
        pb.setVisibility(View.GONE);
        lockUI(false);
        if (operation.contentEquals("getInfo")) {
            try {
                pm.saveInt("rallyStationNumber", current + 1);
                JSONObject object = new JSONObject(response); //All data provided by server is parsed to activityScan through intent
                i.putExtra("ID", barcodeValue);
                i.putExtra("Name", object.getString("Name"));
                i.putExtra("Text", object.getString("Text"));
                i.putExtra("Bild", object.getString("Bild"));
                i.putExtra("Video", object.getString("Video"));
                startActivity(i);
            } catch (JSONException e) {
                Toast.makeText(c, R.string.barcode_not_found, Toast.LENGTH_SHORT).show();
                ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
            }
        } else if (operation.contentEquals("getVersion")) {
            try {
                preferencesManager pref = preferencesManager.getInstance(c);

                JSONObject j = new JSONObject(response);
                String date = j.getString("date");

                //set the encryption key base, delivered from the server
                if (cacheManager.encryptionKeyBase == null) cacheManager.encryptionKeyBase = j.getString("ek");

                //get required data and check if cache needs to be deleted
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                if (!pref.getPreferences().contains("cache_date")) pref.saveString("cache_date", date);//set date if none present

                String savedDateString = pref.getPreferences().getString("cache_date", "2020-01-01");

                //compare the locally and the server date for cache update, if server date is larger, cached files will be deleted
                if (df.parse(date).getTime() > df.parse(savedDateString).getTime()) {
                    cacheManager.invalidateCache(c);
                    pref.saveString("cache_date", date);
                }

                //check for update
                if (checkUpdate(j.getString("version"))) {
                    updateAlert();
                    return;
                }
                pb.setVisibility(View.VISIBLE);
                lockUI(true);
                net.makePostRequest(this, "getPermission", preferencesManager.getInstance(c).getPreferences().getString("token", ""), c);
            } catch (Exception e) {
                Intent i = new Intent(c, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        } else if (operation.contentEquals("getPermission")) {
            try {
                JSONObject js = new JSONObject(response);
                if (js.getString("status").contentEquals("200")) {
                    if (!pm.getPreferences().contains("token")) {
                        pm.saveBoolean("unlocked", true);
                        pm.saveString("token", barcodeValue);
                        pm.saveString("mode", String.valueOf(0));
                        new historyManager(c).clearHistory();
                        Toast.makeText(c, getString(R.string.scan_teacher_success), Toast.LENGTH_SHORT).show();
                        pm.saveInt("rallyStationNumber", -1);
                    }
                    net.makePostRequest(this, "getMapData", "", c);
                } else {//error returned by server, lock all features
                    if (isTeacherCodeScanned) Toast.makeText(c, getString(R.string.scan_teacher_error), Toast.LENGTH_SHORT).show();
                    if (pm.getPreferences().contains("token")) pm.deleteValue("token");
                    if (pm.areFeaturesUnlocked()) pm.saveBoolean("unlocked", false);
                    if (pm.isRallyeMode()) pm.saveString("mode", "1");
                    detectionCheck = true;
                }
                if (isTeacherCodeScanned) ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
            } catch (JSONException e) {
                Intent i = new Intent(c, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        } else if (operation.contentEquals("getMapData")) {
            try {
                JSONObject js = new JSONObject(response);
                if (!js.getString("status").contentEquals("ok")) {
                    Intent i = new Intent(c, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, "map data error");
                    startActivity(i);
                }
                JSONArray jsa = js.getJSONArray("stations");
                stationIds = new String[jsa.length()];
                for (int i = 0; i < stationIds.length; i++) {
                    stationIds[i] = jsa.getJSONObject(i).getString("stationId");
                }
                detectionCheck = true;
            } catch (JSONException e) {
                Intent i = new Intent(c, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
    }


    @Override
    public void onPause() {
        super.onPause();
        check = false; //TODO: Fix not working probably; Fragment needs to be reworked in order for fix to work
        try {
            if (source == null) {
                return;
            }
            if (source.getPreviewSize() != null) {
                source.stop();
            }
        } catch (Exception e) {
            Intent i = new Intent(c, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            startActivity(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!check) {
            ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
            /*String[] num = Build.VERSION.RELEASE.split("\\.");
            int n;
            if (num.length == 0) {
                n = Integer.parseInt(Build.VERSION.RELEASE);
            } else {
                n = Integer.parseInt(num[0]);
            }
            if (n < 8 || (ActivityCompat.checkSelfPermission(c, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) { //Only for fixing bug with Android version 7.X.X or lower; Otherwise camera would't manually start when resuming the app
                ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
            }*/
        }

    }

    /**
     * Method for starting usage of camera
     */
    private void startCamera() {

        source = new CameraSource.Builder(c, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedFps(20)
                .setRequestedPreviewSize(1920, 1080)
                .setFacing(CameraSource.CAMERA_FACING_BACK).build();

        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NotNull SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    textView2.setVisibility(View.GONE);
                    source.start(surface.getHolder());

                } catch (IOException e) {
                    Intent i = new Intent(c, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                    startActivity(i);
                }
            }

            @Override
            public void surfaceChanged(@NotNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NotNull SurfaceHolder holder) {
                try {
                    if (source.getPreviewSize() != null) {
                        source.stop();
                    }
                } catch (Exception e) {
                    Intent i = new Intent(c, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                    startActivity(i);
                }
            }
        });

    }

    /**
     * Method for handling situation where barcode is detected, starts newIntent afterwards
     */
    private void detection() {
        i = null;
        //barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(Barcode.QR_CODE).build();

        barcodeDetector = new BarcodeDetector.Builder(c).setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) { //TODO: Bug where scanning wont work immediately if user goes back to scan from map or activityScan
                final SparseArray<Barcode> detectedFrames = detections.getDetectedItems();
                if (detectedFrames.size() != 0 && detectionCheck) {
                    barcodeValue = detectedFrames.valueAt(0).displayValue;
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            newIntent();
                        }
                    });
                }

            }
        });
        startCamera();
    }


    /**
     * Creates new Intent for starting activityScan
     */
    private void newIntent() {
        if (i == null && !barcodeValue.contentEquals("")) {
            i = new Intent(getActivity(), activityScan.class);
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pb.setVisibility(View.VISIBLE);
                    lockUI(true);
                }
            });
            if (barcodeValue.length() == 10) {
                preferencesManager pm = preferencesManager.getInstance(c);
                if (pm.isRallyeMode()) {
                    if (stationIds == null) {
                        ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
                        pb.setVisibility(View.GONE);
                        lockUI(false);
                        return;
                    }
                    current = pm.getPreferences().getInt("rallyStationNumber", -1);
                    if (stationIds.length > (current + 1)) {
                        if (!barcodeValue.contentEquals(stationIds[current + 1])) {
                            Toast.makeText(c, getString(R.string.wrong_station), Toast.LENGTH_SHORT).show();
                            ((recreateFragmentAfterScanInterface) a).recreateFragmentAfterScan();
                            pb.setVisibility(View.GONE);
                            lockUI(false);
                            return;
                        }
                    }
                    pm.saveInt("rallyStationNumber", current + 1);
                }
                net.makePostRequest(this, "getInfo", barcodeValue, c);
            } else {
                isTeacherCodeScanned = true;
                net.makePostRequest(this, "getPermission", barcodeValue, c);
            }
        }
    }

    /**
     * compare version string from server to local version
     *
     * @param ver version string from server
     * @return update available?
     */
    private boolean checkUpdate(String ver) {
        try {
            ContextWrapper cw = new ContextWrapper(c);
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(cw.getPackageName(), 0);
            preferencesManager pref = preferencesManager.getInstance(c);
            int vc = pInfo.versionCode;
            if (vc < Integer.parseInt(ver.replace("\n", ""))) {
                if (!pref.getPreferences().getBoolean("update", true)) {
                    pref.saveBoolean("update", true);
                }
                return true;
            } else {
                if (pref.getPreferences().getBoolean("update", false)) {
                    pref.saveBoolean("update", false);
                }
                return false;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Intent i = new Intent(c, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            startActivity(i);
            return true;
        }
    }

    /**
     * alert dialog, if update is available
     */
    private void updateAlert() {
        final String appPackageName = c.getPackageName();
        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(a);
        alert.setCancelable(false);
        alert.setTitle(getString(R.string.update_dialog_title));
        alert.setMessage(getString(R.string.update_dialog_content));
        alert.setPositiveButton(getString(R.string.button_update_dialog_close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                a.finishAffinity();
            }
        });
        alert.setNegativeButton(getString(R.string.button_update_dialog_update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } catch (android.content.ActivityNotFoundException e) {
                    //no local android market, open browser
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                a.finishAffinity();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }

    /**
     * Interface used for the recreation of the fragment after a teacher- or wrong code has been scanned, because resetting the detector caused issues
     * method is implemented in {@link activityMain} because navigation is handled there
     */
    public interface recreateFragmentAfterScanInterface {
        void recreateFragmentAfterScan();
    }
}
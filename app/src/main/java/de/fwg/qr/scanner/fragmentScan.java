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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.fwg.qr.scanner.tools.cache.cacheManager;
import de.fwg.qr.scanner.tools.networkCallbackInterface;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
*fragment to scan a qr code
*/
public class fragmentScan extends fragmentWrapper implements networkCallbackInterface {

    private WeakReference<networkCallbackInterface> ref;
    private CameraSource source;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surface = null;
    private TextView textView2;
    private ProgressBar pb;

    /**
     * Intent made qlobal because of reciveDetection being called multiple times
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
    private boolean updateCheck = true;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = new WeakReference<>((networkCallbackInterface) this);
        if (!net.isNetworkAvailable()) {
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
        net.makePostRequest(ref, "getVersion", "");
        initialize();
        startCamera();
        detection();
    }

    /**
     * If user always denies camera access, he'll be sent to the settings of his device
     *
     * @param requestCode  Code for permissonRequest
     * @param permissions  All permissions that were asked of user
     * @param grantResults Shows if permissions with same index were granted or denied
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //System.out.println("RequesResult wird gecalled");
        if (requestCode == 201) {
            //System.out.println("RequesResult wird gecalled");
            for (int i = 0, len = permissions.length; i < len; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        Toast.makeText(c, getString(R.string.permission_needed), Toast.LENGTH_SHORT).show();
                        System.out.println("false showRationale");
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", a.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        a.finishAffinity();
                    }
                }
            }
        }
    }

    @Override
    public void onPostCallback(String operation, String response) {
        pb.setVisibility(View.GONE);
        lockUI(false);
        if (operation.contains("error") || response.contains("Error") || response.contains("error")) {
            Toast.makeText(c, "Code not found", Toast.LENGTH_SHORT).show();
        }
        if (operation.contentEquals("getInfo")) {
            try {
                JSONObject object = new JSONObject(response);
                i.putExtra("ID", barcodeValue);
                i.putExtra("Name", object.getString("Name"));
                i.putExtra("Text", object.getString("Text"));
                i.putExtra("Bild", object.getString("Bild"));
                i.putExtra("Video", object.getString("Video"));
                startActivity(i);
            } catch (JSONException e) {
                //TODO @everyone use resource
                Toast.makeText(c, "Code not found", Toast.LENGTH_SHORT).show();
            }
        } else if (operation.contentEquals("getVersion")) {
            try {
                preferencesManager pref = preferencesManager.getInstance(c);

                JSONObject j = new JSONObject(response);
                String date = j.getString("date");
                String version = j.getString("version");

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                if (!pref.getPreferences().contains("cache_date")) {
                    pref.saveString("cache_date", date);
                }

                String savedDateString = pref.getPreferences().getString("cache_date", "2020-01-01");

                //compare the locally and the server date for cache update, if server data is larger, cached files will be deleted
                if (df.parse(date).getTime() > df.parse(savedDateString).getTime()) {
                    new cacheManager(c).invalidateCache();
                    pref.saveString("cache_date", date);
                }

                //check for update
                if (checkUpdate(version)) {
                    updateAlert();
                    return;
                }
            } catch (Exception e) {
                Intent i = new Intent(c, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
                return;
            }
            updateCheck = false;
        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
    }

    /**
     * Initializes both barcodeDetector as well as CameraSource where the detector is underlined
     */
    private void initialize() {
        i = null;
        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        source = new CameraSource.Builder(c, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedFps(20)
                .setRequestedPreviewSize(1920, 1080)
                .setFacing(CameraSource.CAMERA_FACING_BACK).build();
    }

    @Override
    public void onPause() {
        super.onPause();
        check = false;
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

    @Override
    public void onResume() {
        super.onResume();
        if (!check) {
            a.recreate();
        }
    }

    /**
     * Method for starting usage of camera
     */
    private void startCamera() {
        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
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
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
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
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> detectedFrames = detections.getDetectedItems();
                if (detectedFrames.size() != 0 && !updateCheck) {
                    barcodeValue = detectedFrames.valueAt(0).displayValue;
                    newIntent();
                }
            }
        });
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
            net.makePostRequest(ref, "getInfo", barcodeValue);
        }

    }

    /**
     * compare version string from server to local version
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
        alert = new AlertDialog.Builder(c);
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
}
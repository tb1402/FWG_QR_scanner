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

public class fragmentScan extends fragmentWrapper implements networkCallbackInterface {

    private WeakReference<networkCallbackInterface> ref;
    private CameraSource source;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surface = null;
    private TextView textView2;
    private ProgressBar pb;

    private Intent i = null;
    private String barcodeValue = "";
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
            ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.CAMERA}, 201);
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

    @Override
    public void onPostCallback(String operation, String response) {
        pb.setVisibility(View.GONE);
        lockUI(false);
        if (operation.contains("error") || response.contains("Error") || response.contains("error")) {
            i = new Intent();
            Intent i = new Intent(c, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, response);
            startActivity(i);
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
                i = new Intent();
                Intent i = new Intent(c, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                startActivity(i);
            }
        } else if (operation.contentEquals("getVersion")) {
            try {
                preferencesManager pref = new preferencesManager(c);
                JSONObject j = new JSONObject(response);
                String date = j.getString("date");
                String version = j.getString("version");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                if (!pref.contains("cache_date")) {
                    pref.saveString("cache_date", date);
                }
                String savedDateString = pref.getString("cache_date", "2020-01-01");
                if (df.parse(date).getTime() >= df.parse(savedDateString).getTime()) {
                    new cacheManager(c).invalidateCache();
                    pref.saveString("cache_date", date);
                }
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
            source.release();
        } catch (Exception e) {
            Intent i = new Intent(c, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
            startActivity(i);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!check) {
            a.recreate();
        }
    }

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
                /*try {
                    if(source!=null) {
                        source.release();//for some reason, even when checked for not null, this throws a nullPointerException
                        //todo pleas fix problem described above
                    }
                } catch (Exception e) {
                    Intent i = new Intent(c, activityErrorHandling.class);
                    i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(e));
                    startActivity(i);
                }*/
            }
        });
    }

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

    private boolean checkUpdate(String ver) {
        try {
            ContextWrapper cw = new ContextWrapper(c);
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(cw.getPackageName(), 0);
            preferencesManager pref = new preferencesManager(c);
            int vc = pInfo.versionCode;
            if (vc < Integer.parseInt(ver.replace("\n", ""))) {
                if (!pref.getBoolean("update", true)) {
                    pref.saveBoolean("update", true);
                }
                return true;
            } else {
                if (pref.getBoolean("update", false)) {
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

    private void updateAlert() {
        final String appPackageName = c.getPackageName();
        AlertDialog.Builder alert;
        alert = new AlertDialog.Builder(c);
        alert.setCancelable(false);
        alert.setTitle("Alte Version!");
        alert.setMessage("Bitte aktualisiere die App!");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                a.finishAffinity();
            }
        });
        alert.setNegativeButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                a.finishAffinity();
            }
        });
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
}
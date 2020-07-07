package de.fwg.qr.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentScan extends fragmentWrapper implements networkCallbackInterface {

    private WeakReference<networkCallbackInterface> ref;
    private CameraSource source;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surface;
    private TextView textView;
    private TextView textView2;

    private Intent i = null;
    private String barcodeValue = "";
    private JSONObject object = null;
    private boolean check = false;


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
    public void onViewCreated(View v, @Nullable Bundle sis) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            check = true;
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 201);
            check = true;
        }
        surface = v.findViewById(R.id.surfaceView);
        textView = v.findViewById(R.id.textView);
        textView2 = v.findViewById(R.id.textView2);
        initialize();
        startCamera();
        detection();
    }

    @Override
    public void onPostCallback(String operation, String response) {
        Log.i("fwg", response);
        if (operation.contentEquals("getInfo")) {
            try {
                object = new JSONObject(response);
                i.putExtra("ID", barcodeValue);
                i.putExtra("Name", object.getString("Name"));
                i.putExtra("Text", object.getString("Text"));
                i.putExtra("Bild", object.getString("Bild"));
                i.putExtra("Video", object.getString("Video"));
                startActivity(i);
            } catch (JSONException e) {
                i = null;
                Toast.makeText(c, "json_err" + e.toString(), Toast.LENGTH_SHORT).show();
            }
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
        source.release();


    }

    public void onStop() {
        super.onStop();
        check = false;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!check) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                check = true;
            } else {
                check = true;
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 201);
            }
        }
        initialize();
        startCamera();
        detection();
    }

    private void startCamera() { //TODO: Problem with camera not working after resuming app

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
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                source.stop();
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
                if (detectedFrames.size() != 0) {
                    barcodeValue = detectedFrames.valueAt(0).displayValue;
                    newIntent();
                }
            }
        });
    }

    private void newIntent() {
        if (i == null && !barcodeValue.contentEquals("")) {
            i = new Intent(getActivity(), activityScan.class);
            Log.i("fwg", "scanned");
            net.makePostRequest(ref, "getInfo", barcodeValue);
        }

    }
}
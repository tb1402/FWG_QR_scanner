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

    WeakReference<networkCallbackInterface> ref;

    private CameraSource source;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surface;
    private TextView textView;

    private Intent i = null;
    private String barcodeValue = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref = new WeakReference<>((networkCallbackInterface) this);
        if (!net.isNetworkAvailable()) {
            Toast.makeText(c, "Keine Netzwerkverbindung!", Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle sis) {
        surface = v.findViewById(R.id.surfaceView);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 201);
        }
        textView = v.findViewById(R.id.textView);
        initialize();
        startCamera();
        detection();
    }

    @Override
    public void onPostCallback(String operation, String response) {
        Log.i("fwg",response);
        if(operation.contentEquals("getInfo")) {
            try {
                JSONObject o = new JSONObject(response);
                textView.setText(o.getString("Text"));
            } catch (JSONException e) {
                i = null;
                Toast.makeText(c, "json_err" + e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("fwg", name);
        lockUI(false);
        if (name.contentEquals("test")) {
            //test.setImageBitmap(image);
        }
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

    @Override
    public void onResume() {
        super.onResume();
        initialize();
        startCamera();
        detection();
    }

    private void startCamera() {

        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
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
            i.putExtra("barcode", barcodeValue);
            Log.i("fwg","scanned");
            net.makePostRequest(ref,"getInfo",barcodeValue);
            //startActivity(i);
        }

    }
}
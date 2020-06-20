package de.fwg.qr.scanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentScan extends fragmentWrapper implements networkCallbackInterface {

    ImageView test;
    VideoView videoView;
    WeakReference<networkCallbackInterface> ref;

    private CameraSource source;
    private BarcodeDetector barcodeDetector;
    private SurfaceView surface;

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
        //test = v.findViewById(R.id.imageView);
        //videoView = v.findViewById(R.id.vw);
        //lockUI(true);
        //net.makeImageRequest(ref, "test", "/1.jpg");
        //PlayVideo();
        surface = v.findViewById(R.id.surfaceView);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, 201);
        }
        initialize();
        startCamera();
        detection();
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("fwg", name);
        lockUI(false);
        if (name.contentEquals("test")) {
            test.setImageBitmap(image);
        }
    }

    private void PlayVideo() {
        try {
            a.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(getActivity());
            mediaController.setAnchorView(videoView);

            Uri video = Uri.parse(network.baseURL + "/Genesis_-_Jesus_He_Knows_Me_Official_Music_Video.mp4");
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    videoView.start();
                }
            });


        } catch (Exception e) {
            System.out.println("Video Play Error :" + e.toString());
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
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
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
            i.putExtra(Intent.EXTRA_TEXT, barcodeValue);//todo consider changing Intent.EXTRA_TEXT to a custom name
            startActivity(i);
        }

    }
}
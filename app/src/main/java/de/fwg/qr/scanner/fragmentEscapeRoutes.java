package de.fwg.qr.scanner;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentEscapeRoutes extends fragmentWrapper implements networkCallbackInterface{

    VideoView videoView;
    ImageView test;
    WeakReference<networkCallbackInterface> ref;
    ProgressDialog pd;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showStartIcon();
        ref = new WeakReference<>((networkCallbackInterface) this);
        if (!net.isNetworkAvailable()) {
            Toast.makeText(c, "Keine Netzwerkverbindung!", Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_escape_routes, container, false);
    }
    @Override
    public void onViewCreated(View v, @Nullable Bundle sis) {
        test = v.findViewById(R.id.imageView);
        videoView = v.findViewById(R.id.vw);
        lockUI(true);
        net.makeImageRequest(ref, "test", "/1.jpg");
        pd=new ProgressDialog(c);
        pd.setTitle(getString(R.string.network_buffering));
        pd.setCancelable(false);
        //a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        pd.show();
        PlayVideo();
    }
    @Override
    public void onPostCallback(String op, String res){

    }
    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("fwg", name);
        lockUI(false);
        if (name.contentEquals("test")) {
            test.setImageBitmap(image);
        }
    }

    /**
     * Under Construction!
     */
    private void PlayVideo() {
        try {
            a.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(getActivity());
            mediaController.setAnchorView(videoView);

            final MediaPlayer.OnInfoListener il=new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                    switch(i){
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                            pd.show();
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END:{
                            pd.hide();
                            return true;
                        }
                    }
                    return false;
                }
            };

            Uri video = Uri.parse(network.baseURL+"/Genesis_-_Jesus_He_Knows_Me_Official_Music_Video.mp4");
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            videoView.requestFocus();
            videoView.setOnInfoListener(il);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    pd.hide();
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
}
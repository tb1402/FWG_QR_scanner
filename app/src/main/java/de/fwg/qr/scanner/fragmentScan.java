package de.fwg.qr.scanner;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.lang.ref.WeakReference;

import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class fragmentScan extends fragment_wrapper implements networkCallbackInterface {

    ImageView test;
    VideoView videoView;
    WeakReference<networkCallbackInterface> ref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ref=new WeakReference<>((networkCallbackInterface) this);
        if(!net.isNetworkAvailable()){
            Toast.makeText(c, "Keine Netzwerkverbindung!", Toast.LENGTH_SHORT).show();
            a.finishAffinity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }
    @Override
    public void onViewCreated(View v, @Nullable Bundle sis){
        test=v.findViewById(R.id.imageView);
        videoView=v.findViewById(R.id.vw);
        net.makeImageRequest(ref,"test","/1.jpg");
        PlayVideo();
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        Log.i("fwg",name);
        if(name.contentEquals("test")){
            test.setImageBitmap(image);
        }
    }
    private void PlayVideo()
    {
        try
        {
            a.getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(getActivity());
            mediaController.setAnchorView(videoView);

            Uri video = Uri.parse("https://web.cloud-tb.de/smb/yt_arch/GenesisVEVO/Genesis_-_I_Can_t_Dance_Official_Music_Video.mp4");
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);
            videoView.requestFocus();
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {

                public void onPrepared(MediaPlayer mp)
                {
                    videoView.start();
                }
            });


        }
        catch(Exception e)
        {
            System.out.println("Video Play Error :"+e.toString());
        }

    }
}
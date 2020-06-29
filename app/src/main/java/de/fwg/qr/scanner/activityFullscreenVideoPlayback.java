package de.fwg.qr.scanner;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.preferencesManager;

public class activityFullscreenVideoPlayback extends AppCompatActivity {

    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private VideoView vw;
    private ProgressBar pb;
    private boolean isEscapeRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);//hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//set fullscreen mode

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//request landscape mode

        //network setup and check if connection is available
        network net = new network(getApplicationContext());
        if (!net.isNetworkAvailable()) {
            Toast.makeText(getApplicationContext(), getString(R.string.network_no_connection), Toast.LENGTH_SHORT).show();
            finish();
        }

        //setup layout and elements
        setContentView(R.layout.activity_fullscreen_video_playback);
        vw=findViewById(R.id.video);
        pb=findViewById(R.id.pb);

        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.content);

        // Set up the user interaction to manually show or hide the system UI.
        vw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //delayedHide(AUTO_HIDE_DELAY_MILLIS);
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        Intent i=getIntent();
        isEscapeRoute=i.getBooleanExtra("isER",false);
        if(isEscapeRoute){//check if given video url is escape route sequence
            parseEscapeRouteSequence(i.getStringExtra("seq"));
        }
        else {
            pb.setVisibility(View.VISIBLE);
            lockUI(true);
            String id = i.getStringExtra("ID"); //Hab den namen von "id" zu "ID" geändert, ansonsten hätte es nichts bekommen und ich wollte es bei mir nicht wegen consistency ändern
            if(id!=null) {
                if (id.length() != 4) {
                    Toast.makeText(getApplicationContext(), getString(R.string.video_url_error), Toast.LENGTH_SHORT).show();
                }
            }
            playVideo(craftURLForSimplePlayback(id));
        }
    }
    private String craftURLForSimplePlayback(String id){
        preferencesManager p=new preferencesManager(getApplicationContext());
        return network.baseURL+"/videos/"+p.getVideoResolution()+"/"+id+"/000.mp4";
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }
    @Override
    public void onBackPressed(){
        vw.stopPlayback();
        lockUI(false);
        getSupportActionBar().show();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        show();
        finish();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            //actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        //mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        //mHideHandler.removeCallbacks(mHidePart2Runnable);
        //mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Under Construction!
     */
    private void playVideo(String url) {
        try {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(vw);

            final MediaPlayer.OnInfoListener il = new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                    switch (i) {
                        case MediaPlayer.MEDIA_INFO_BUFFERING_START: {
                            pb.setVisibility(View.VISIBLE);
                            lockUI(true);
                            return true;
                        }
                        case MediaPlayer.MEDIA_INFO_BUFFERING_END: {
                            pb.setVisibility(View.GONE);
                            lockUI(false);
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                            return true;
                        }
                    }
                    return false;
                }
            };

            //Uri video = Uri.parse(network.baseURL + "/Genesis_-_Jesus_He_Knows_Me_Official_Music_Video.mp4");
            Uri video = Uri.parse(url);
            vw.setMediaController(mediaController);
            vw.setVideoURI(video);
            vw.requestFocus();
            vw.setOnInfoListener(il);
            vw.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    pb.setVisibility(View.GONE);
                    lockUI(false);
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    vw.start();
                }
            });
            vw.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    //pb.setVisibility(View.VISIBLE);
                    //lockUI(true);
                    if(isEscapeRoute){
                        //todo load stuff
                    }
                    else{
                        vw.stopPlayback();
                        lockUI(false);
                        getSupportActionBar().show();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        finish();
                    }

                }
            });
            vw.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
                    if(i==703){
                        Toast.makeText(getApplicationContext(),"Bitrate: "+i1+"kbps",Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            });


        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Video Play Error :" + e.toString(),Toast.LENGTH_SHORT).show();
        }

    }
    private void lockUI(boolean state) {
        if (state) {
            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
    private void parseEscapeRouteSequence(String seq){

    }
}
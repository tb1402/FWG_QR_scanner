package de.fwg.qr.scanner;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Locale;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

public class activityErrorHandling extends toolbarWrapper implements networkCallbackInterface {

    private network net;
    private WeakReference<networkCallbackInterface> ref;
    private String error_desc;

    public static String stackTraceToString(Exception e){
        return Log.getStackTraceString(e);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_error_handling,this,getString(R.string.title_activty_error));
        net=new network(getApplicationContext());
        ref=new WeakReference<networkCallbackInterface>(this);
        error_desc=getIntent().getStringExtra("error_desc");
        TextView tv_error=findViewById(R.id.tv_error_description);
        tv_error.setText(error_desc);
        Button but_report=findViewById(R.id.but_report);
        Button but_close=findViewById(R.id.but_close);
        but_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                net.makePostRequest(ref,"error",error_desc);
            }
        });
        but_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {

    }
    public static String getDeviceInfo(){
        String osVersion=System.getProperty("os.version");
        int apiLevel= Build.VERSION.SDK_INT;
        String device= Build.DEVICE;
        String model=Build.MODEL;
        String product=Build.PRODUCT;
        String man=Build.MANUFACTURER;
        String version=Build.VERSION.RELEASE;
        String type=Build.TYPE;
        String hw= Build.HARDWARE;
        String fp=Build.FINGERPRINT;
        String display=Build.DEVICE;
        String bl=Build.BOOTLOADER;
        String board=Build.BOARD;
        //String serial=Build.getSerial();
        String radio=Build.getRadioVersion();
        String patch=Build.VERSION.SECURITY_PATCH;
        return String.format(Locale.GERMANY,"%s_%d_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s_%s",device,apiLevel,osVersion,patch,model,product,man,version,type,hw,fp,display,bl,board,radio);
    }
}
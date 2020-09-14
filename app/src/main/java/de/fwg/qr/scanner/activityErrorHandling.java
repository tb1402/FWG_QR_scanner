package de.fwg.qr.scanner;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Locale;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;

/**
 * This activity gets called, whenever an unsolvable error occurs in a try statement or an unexpected error in the app.
 * The user can select either to send the device data and error message to the server or
 * close the app
 */
public class activityErrorHandling extends toolbarWrapper implements networkCallbackInterface {

    public static String errorNameIntentExtra = "error_desc"; //tag for intent extra
    private network net;
    private WeakReference<networkCallbackInterface> ref;
    private String error_desc;//desc=description
    private boolean isUncaught = false;
    private int rootPID;

    /**
     * Method to convert an exceptions stacktrace to a String
     *
     * @param e the exception
     * @return String with stackTrace
     */
    public static String stackTraceToString(Exception e) {
        return Log.getStackTraceString(e);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onBackPressed() {
        finishAndRemoveTask();//finish the activity and remove the task from the app overview
        if (isUncaught) {
            //if uncaught, there's also a root process that needs to be killed, because activityErrorHandling is started new Thread and process
            //this is necessary because the main process can be locked and not responding if an uncaught exception occurred
            if (rootPID != -1) {
                android.os.Process.killProcess(rootPID);
            }

            //exit completely
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        } else {
            finishAffinity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.activity_error_handling, this, getString(R.string.title_activty_error));

        //network
        net = new network(getApplicationContext());
        ref = new WeakReference<networkCallbackInterface>(this);

        //getIntent extra
        error_desc = getIntent().getStringExtra("error_desc");
        if (getIntent().getBooleanExtra("isUE", false)) {
            isUncaught = true;
            rootPID = getIntent().getIntExtra("rpid", -1);
        }

        //initialize views
        TextView tv_error = findViewById(R.id.tv_error_description);
        tv_error.setText(error_desc);
        Button but_report = findViewById(R.id.but_report);
        Button but_close = findViewById(R.id.but_close);

        //set onClick Listeners
        but_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    JSONObject j = new JSONObject();
                    j.put("device", getDeviceInfo());
                    j.put("error", error_desc);
                    net.makePostRequest(ref, "error", j.toString());
                } catch (JSONException e) {
                    net.makePostRequest(ref, "error", "{\"error\":" + error_desc + ",\"device\":\"error---" + getDeviceInfo() + "\"");
                }
            }
        });
        but_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAndRemoveTask();
                if (isUncaught) {
                    if (rootPID != -1) {
                        android.os.Process.killProcess(rootPID);
                    }
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                } else {
                    finishAffinity();
                }
            }
        });
    }

    @Override
    public void onPostCallback(String operation, String response) {
        Toast.makeText(getApplicationContext(), getString(R.string.error_send), Toast.LENGTH_SHORT).show();
        finishAndRemoveTask();
        if (isUncaught) {
            if (rootPID != -1) {
                android.os.Process.killProcess(rootPID);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        //empty, only post request from networkCallback is needed
    }

    /**
     * gets a string with useful device information
     *
     * @return crafted string
     */
    private String getDeviceInfo() {
        String osVersion = System.getProperty("os.version");
        int apiLevel = Build.VERSION.SDK_INT;
        String device = Build.DEVICE;
        String model = Build.MODEL;
        String product = Build.PRODUCT;
        String man = Build.MANUFACTURER;
        String version = Build.VERSION.RELEASE;
        String type = Build.TYPE;
        String hw = Build.HARDWARE;
        String fp = Build.FINGERPRINT;
        String display = Build.DEVICE;
        String bl = Build.BOOTLOADER;
        String board = Build.BOARD;
        String radio = Build.getRadioVersion();
        String patch = Build.VERSION.SECURITY_PATCH;
        return String.format(Locale.GERMANY, "%s--%d--%s--%s--%s--%s--%s--%s--%s--%s--%s--%s--%s--%s--%s", device, apiLevel, osVersion, patch, model, product, man, version, type, hw, fp, display, bl, board, radio);
    }
}
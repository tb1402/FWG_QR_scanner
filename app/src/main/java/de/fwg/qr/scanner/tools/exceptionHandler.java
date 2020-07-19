package de.fwg.qr.scanner.tools;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import de.fwg.qr.scanner.activityErrorHandling;

public class exceptionHandler implements Thread.UncaughtExceptionHandler{

    private Activity a;
    private Application app;
    private int rootPID;
    private Thread.UncaughtExceptionHandler def;

    public exceptionHandler(Activity a,int pid,Thread.UncaughtExceptionHandler eh){
        this.a=a;
        app=(Application)a.getApplicationContext();
        rootPID=pid;
        def=eh;
    }
    @Override
    public void uncaughtException(@NonNull Thread thread, final @NonNull Throwable throwable) {
        Log.i("fwg", "exception caught");
        new Thread() {
            public void run() {
                Looper.prepare();
            Intent i = new Intent(app, activityErrorHandling.class);
        i.putExtra(activityErrorHandling.errorNameIntentExtra,activityErrorHandling.stackTraceToString(new

            Exception(throwable)));
        i.putExtra("isUE",true);
        i.putExtra("rpid",rootPID);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(i);
        Looper.loop();
        }
    }.start();
        def.uncaughtException(thread,throwable);
    }
}

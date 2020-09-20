package de.fwg.qr.scanner.tools;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Looper;

import androidx.annotation.NonNull;

import de.fwg.qr.scanner.activityErrorHandling;

/**
 * Own UncaughtExceptionHandler to always start {@link de.fwg.qr.scanner.activityErrorHandling} when an uncaught exception occurs.
 */
public class exceptionHandler implements Thread.UncaughtExceptionHandler {

    private Application app;
    private int rootPID;
    private Thread.UncaughtExceptionHandler def; //def(ault), the default exception handler, kept as reference to later pass the exception and show the android "app crashed" dialog

    /**
     * Constructor
     * @param a Activity, which sets the default exception handler
     * @param pid process id of the app
     * @param eh default uncaught exception handler
     */
    public exceptionHandler(Activity a, int pid, Thread.UncaughtExceptionHandler eh) {
        app = (Application) a.getApplicationContext();
        rootPID = pid;
        def = eh;
    }

    /**
     * Implementation of the interface, starts @{@link de.fwg.qr.scanner.activityErrorHandling }
     * @param thread thread in which the exception occurred
     * @param throwable throwable object, wrapping the exception
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, final @NonNull Throwable throwable) {
        //start in new thread because current may be locked, due to the exception
        //also activityErrorHandling will be started in a new process (set in Manifest)
        new Thread() {
            public void run() {
                Looper.prepare();
                Intent i = new Intent(app, activityErrorHandling.class);
                i.putExtra(activityErrorHandling.errorNameIntentExtra, activityErrorHandling.stackTraceToString(new Exception(throwable)));
                i.putExtra("isUE", true);//isUncaughtException
                i.putExtra("rpid", rootPID);//id of main process
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                app.startActivity(i);
                Looper.loop();
            }
        }.start();

        //pass to default handler (see declaration)
        def.uncaughtException(thread, throwable);
    }
}

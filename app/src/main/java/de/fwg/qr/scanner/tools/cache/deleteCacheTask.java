package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.File;

import de.fwg.qr.scanner.activityErrorHandling;

/**
 * asynchronous task to delete cache files on storage
 */
class deleteCacheTask extends AsyncTask<File, Void,Void> {

    private Context c;
    public deleteCacheTask(Context c){
        this.c=c;
    }
    @Override
    protected Void doInBackground(File... files) {
        try {
            if (files[0].isDirectory()) {
                File[] fa=files[0].listFiles();
                if(fa!=null) {
                    for (File f : fa) {
                        f.delete();
                    }
                }
            }
        }
        catch(Exception e){
            Intent i=new Intent(c, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra,activityErrorHandling.stackTraceToString(e));
            c.startActivity(i);
        }
        return null;
    }
}

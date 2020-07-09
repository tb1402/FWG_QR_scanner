package de.fwg.qr.scanner.tools.cache;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.fwg.qr.scanner.activityErrorHandling;

/**
 * asynchronous task to write to storage cache
 */
class writeCacheFileTask extends AsyncTask<File,Void,Void> {

    final Bitmap data;
    private Context c;
    public writeCacheFileTask(Context c, Bitmap data){
        this.data=data;
        this.c=c;
    }
    @Override
    protected Void doInBackground(File... files) {
        try {
            FileOutputStream o = new FileOutputStream(files[0]);
            data.compress(Bitmap.CompressFormat.PNG,100,o);
            o.flush();
            o.close();
            Log.i("fwg","written");
        }
        catch (IOException e){
            Intent i=new Intent(c, activityErrorHandling.class);
            i.putExtra(activityErrorHandling.errorNameIntentExtra,activityErrorHandling.stackTraceToString(e));
            c.startActivity(i);
        }
        return null;
    }
}

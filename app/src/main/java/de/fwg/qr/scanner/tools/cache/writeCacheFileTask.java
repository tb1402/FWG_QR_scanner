package de.fwg.qr.scanner.tools.cache;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class writeCacheFileTask extends AsyncTask<File,Void,Void> {

    final Bitmap data;
    public writeCacheFileTask(Bitmap data){
        this.data=data;
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
            e.printStackTrace();
        }
        return null;
    }
}

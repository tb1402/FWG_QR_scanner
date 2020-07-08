package de.fwg.qr.scanner.tools.cache;

import android.os.AsyncTask;

import java.io.File;

class deleteCacheTask extends AsyncTask<File, Void,Void> {

    @Override
    protected Void doInBackground(File... files) {
        if(files[0].isDirectory()){
            for(File f:files[0].listFiles()){
                f.delete();
            }
        }
        return null;
    }
}

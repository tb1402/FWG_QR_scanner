package de.fwg.qr.scanner;

import android.os.Bundle;

public class activitySettings extends toolbarWrapper{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(R.layout.activity_settings,this,getString(R.string.title_settings));
        super.onCreate(savedInstanceState);
    }
}
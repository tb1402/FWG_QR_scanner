package de.fwg.qr.scanner;

import android.os.Bundle;


public class activityMap extends toolbarWrapper {

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);
        setupAbHome();
    }
}

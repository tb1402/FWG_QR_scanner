package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

public class activitySettings extends toolbarWrapper {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(R.layout.activity_settings,this,getString(R.string.title_settings));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(getString(R.string.title_settings));
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, new fragmentSettings()).commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }
}
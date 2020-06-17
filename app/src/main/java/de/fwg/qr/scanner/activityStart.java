package de.fwg.qr.scanner;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class activityStart extends AppCompatActivity {

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("de.fwg.qr.scanner", MODE_PRIVATE);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        setContentView(R.layout.fragment_start);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstRun", true)) {
            prefs.edit().putBoolean("firstRun", false).commit();
        } else {
            //setContentView(R.layout.activity_main);
        }
    }
}
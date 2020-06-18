package de.fwg.qr.scanner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class activityStart extends AppCompatActivity implements de.fwg.qr.scanner.tools.drawerToggleInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //Toolbar setup
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(getString(R.string.app_name));//set toolbar Title to app name
        setSupportActionBar(tb);//set the toolbar as Action bar
    }

    @Override
    public void showHamburgerIcon() {
    }

    @Override
    public void showBackIcon() {
    }
}
package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

public class activityStart extends toolbarWrapper {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toolbar_start);
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(getString(R.string.app_name));
        setSupportActionBar(tb);
        getSupportFragmentManager().beginTransaction().replace(R.id.start_fragment, new fragmentStart()).commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.tb_item_map).setVisible(false);
        menu.findItem(R.id.tb_item_settings).setVisible(false);
        return true;
    }

    @Override
    public void onBackPressed() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_container));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText("Bitte beenden Sie erst die StartGuide");

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 96);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
        //Toast.makeText(getApplicationContext(), "Bitte beenden Sie erst die StartGuide", Toast.LENGTH_SHORT).show();
    }
}
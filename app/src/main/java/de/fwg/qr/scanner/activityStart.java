package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

public class activityStart extends toolbarWrapper {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(R.layout.toolbar_start, this, getString(R.string.app_name));
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
        Toast.makeText(getApplicationContext(), "Bitte beenden Sie erst die StartGuide", Toast.LENGTH_SHORT).show();
    }
}
package de.fwg.qr.scanner;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.fwg.qr.scanner.tools.preferencesManager;

public class activityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, de.fwg.qr.scanner.tools.drawerToggleInterface {
    /**
     * WICHTIG!!!
     * Diese Klasse bleibt unverändert, hier wird nur Code eingefügt, der zur Navigation dient!!!
     */

    private DrawerLayout drawer;
    private NavController navCon;
    private ActionBarDrawerToggle abdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set dark mode default (lightmode is darkmode and vice versa)
        preferencesManager pm = new preferencesManager(getApplicationContext());
        if (pm.getDarkMode().equals("0")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (pm.getDarkMode().equals("1")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        //check if first run
        if (pm.isFirstRun()) {
            Intent intent = new Intent(this, activityStart.class);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize variables
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);//needed for navigation controller
        navCon = Navigation.findNavController(this, R.id.host_fragment);
        Toolbar tb = findViewById(R.id.toolbar);

        //Toolbar and Drawer toggle setup
        tb.setTitle(getString(R.string.app_name));//set toolbar Title to app name
        setSupportActionBar(tb);//set the toolbar as Action bar
        abdt = new ActionBarDrawerToggle(this, drawer, tb, R.string.msg_navigation_drawer_open, R.string.msg_navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) { //Bugfix: Drawer otherwise hidden when starting App for first time
                super.onDrawerSlide(drawerView, slideOffset);
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }
        };
        drawer.addDrawerListener(abdt);
        abdt.syncState();//VERY IMPORTANT TO APPLY CHANGES!!

        //setup Navigation
        NavigationUI.setupActionBarWithNavController(this, navCon, drawer);
        NavigationUI.setupWithNavController(navView, navCon);

        //initialize textView in navigation header with version and build date
        TextView tv = navView.getHeaderView(0).findViewById(R.id.nav_header_tv_ver);
        tv.setText(getNavigationHeaderText());
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navCon, drawer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //todo setup options
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.tb_item_settings:
                i = new Intent(getApplicationContext(), activitySettings.class);
                startActivity(i);
                return true;
            case R.id.tb_item_map:
                i = new Intent(getApplicationContext(), activityMap.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle drawer, if back button is pressed
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        Fragment f;
        switch (id) {
            case R.id.item_frgm_history:
                f = new fragmentHistory();
                show(f);
                break;
            case R.id.item_frgm_information:
                f = new fragmentInformation();
                show(f);
                break;
            case R.id.item_frgm_about:
                f = new fragmentAbout();
                show(f);
                break;
            case R.id.item_frgm_agb:
                f = new fragmentAGB();
                show(f);
                break;
            case R.id.item_frgm_help:
                f = new fragmentHelp();
                show(f);
            case R.id.item_frgm_scan:
                f = new fragmentScan();
                show(f);
                break;
            case R.id.item_frgm_escape_routes:
                f = new fragmentEscapeRoutes();
                show(f);
                break;
            case R.id.item_frgm_progress:
                f = new fragmentProgress();
                show(f);
                break;
            default:
                return false;
        }

        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * make fragment transaction to show Fragment
     *
     * @param fragment fragment, which needs to be shown
     */
    private void show(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.host_fragment, fragment)
                .commit();
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Implementation of corresponding method in  tools.drawerToggleInterface
     * Shows the hamburger icon in toolbar,
     * to be able to open/close the drawer
     */
    @Override
    public void showHamburgerIcon() {
        ActionBar a = getSupportActionBar();
        if (a != null) {
            a.setDisplayHomeAsUpEnabled(false);
            abdt.setDrawerIndicatorEnabled(true);
            abdt.syncState();
        }
    }

    /**
     * Implementation of corresponding method in  tools.drawerToggleInterface
     * Shows the back icon in toolbar,
     * so the drawer cant be opened from the calling activity/fragment
     */
    @Override
    public void showBackIcon() {
        ActionBar a = getSupportActionBar();
        if (a != null) {
            a.setDisplayHomeAsUpEnabled(true);
            abdt.setDrawerIndicatorEnabled(false);
            abdt.syncState();
        }
    }

    /**
     * Builds a String based on the last update date of the package info (a.k.a. the build date of the application)
     * and the current version string to be shown in the TextView in navigation header
     * returns undefined resource string, if an error occurred
     *
     * @return crafted string
     */
    private String getNavigationHeaderText() {
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(new ContextWrapper(getApplicationContext()).getPackageName(), 0);
            return pInfo.versionName + " - " + new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(new Date(pInfo.lastUpdateTime));
        } catch (PackageManager.NameNotFoundException e) {
            return getString(R.string.error_undefined);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }
}
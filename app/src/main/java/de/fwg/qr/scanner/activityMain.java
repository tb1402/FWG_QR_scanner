package de.fwg.qr.scanner;

import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import de.fwg.qr.scanner.history.historyManager;
import de.fwg.qr.scanner.tools.exceptionHandler;
import de.fwg.qr.scanner.tools.preferencesManager;

/**
* Main (launcher) activity, used to setup navigation, start firstRun screen etc.
*/
public class activityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, de.fwg.qr.scanner.tools.drawerToggleInterface, fragmentScan.recreateFragmentAfterScanInterface {

    private DrawerLayout drawer;
    private NavController navCon;
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //set dark/light mode, depending on users settings
        preferencesManager pm = preferencesManager.getInstance(getApplicationContext());
        switch (pm.getDarkMode()) {
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        //set the default  uncaught exception handler, to redirect all exceptions (in activityMain) to activityErrorHandling
        Thread.UncaughtExceptionHandler eh = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new exceptionHandler(this, android.os.Process.myPid(), eh));

        //check if first run and if so, show start activity
        if (pm.isFirstRun()) {
            Intent intent = new Intent(this, activityStart.class);
            startActivity(intent);
            finish();
        }

        //initialize variables for navigation
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navView = findViewById(R.id.nav_view);//needed for navigation controller
        navCon = Navigation.findNavController(this, R.id.host_fragment);

        //Toolbar and Drawer toggle setup
        Toolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(getString(R.string.app_name));//set toolbar Title to app name
        setSupportActionBar(tb);//set the toolbar as Action bar
        drawerToggle = new ActionBarDrawerToggle(this, drawer, tb,0,0) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) { //Bugfix: Drawer otherwise hidden when starting App for first time
                super.onDrawerSlide(drawerView, slideOffset);
                drawer.bringChildToFront(drawerView);
                drawer.requestLayout();
            }
        };
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();//VERY IMPORTANT TO APPLY CHANGES!!

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle toolbar items
        int id = item.getItemId();
        Intent i;
        switch (id) {
            case R.id.tb_item_settings:
                i = new Intent(getApplicationContext(), activitySettings.class);
                startActivity(i);
                return true;
            case R.id.tb_item_map:
                if(preferencesManager.getInstance(getApplicationContext()).areFeaturesUnlocked()) {
                    i = new Intent(getApplicationContext(), activityMap.class);
                    startActivityForResult(i,123);
                }
                else{
                    Toast.makeText(getApplicationContext(),getString(R.string.scan_teacher_code),Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.tb_item_reset:
                if(preferencesManager.getInstance(getApplicationContext()).isRallyeMode()){
                    showResetDialog();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle drawer, if back button is pressed
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        //handle drawer items
        int id = item.getItemId();
        Fragment f;
        switch (id) {
            case R.id.item_frgm_history:
                f = new fragmentHistory();
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
            case R.id.item_frgm_progress:
                if(preferencesManager.getInstance(getApplicationContext()).areFeaturesUnlocked()){
                    f = new fragmentProgress();
                    show(f);
                }
                else{
                    Toast.makeText(getApplicationContext(),getString(R.string.scan_teacher_code),Toast.LENGTH_LONG).show();
                }
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
     * Implementation of corresponding method in tools.drawerToggleInterface
     * Shows the hamburger icon in toolbar,
     * to be able to open/close the drawer
     */
    @Override
    public void showHamburgerIcon() {
        ActionBar a = getSupportActionBar();
        if (a != null) {
            a.setDisplayHomeAsUpEnabled(false);
            drawerToggle.setDrawerIndicatorEnabled(true);
            drawerToggle.syncState();
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
            drawerToggle.setDrawerIndicatorEnabled(false);
            drawerToggle.syncState();
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

    @Override
    public void recreateFragmentAfterScan() {
        navCon.navigate(R.id.action_item_frgm_scan_self);
    }

    private void showResetDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_reset_progress_title))
                .setMessage(getString(R.string.dialog_reset_progress_content))
                .setPositiveButton(getString(R.string.dialog_del_warning_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new historyManager(getApplicationContext()).clearHistory();
                        preferencesManager.getInstance(getApplicationContext()).saveInt("rallyStationNumber",-1);
                        recreateFragmentAfterScan();
                    }
                })
                .setNegativeButton(getString(R.string.dialog_del_warning_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    @Override
    protected void onActivityResult (int requestCode,int resultCode,Intent data){
        if(requestCode==123){
            if(resultCode==189){//result code given, when back to scan button in activityMap pressed
                new Handler().postDelayed(new Runnable() {//start fragmentScan delayed, otherwise it would crash
                    @Override
                    public void run() {
                        //show(new fragmentScan());
                        navCon.navigate(R.id.item_frgm_scan);
                    }
                },200);

            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
}
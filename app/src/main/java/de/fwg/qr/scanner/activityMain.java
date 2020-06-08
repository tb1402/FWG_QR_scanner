package de.fwg.qr.scanner;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.ContextWrapper;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class activityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, de.fwg.qr.scanner.tools.drawerToggleInterface{
    /**
     * WICHTIG!!!
     * Diese Klasse bleibt unverändert, hier wird nur Code eingefügt, der zur Navigation dient!!!
     */

    private DrawerLayout drawer;//variable for drawer layout, used for navigation drawer
    private NavController navCon;//navigation controller for fragments
    private ActionBarDrawerToggle abdt;//used to change the drawer toggle to hamburger menu or back icon, when needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize variables
        drawer=findViewById(R.id.drawer_layout);
        NavigationView navView=findViewById(R.id.nav_view);//needed for navigation controller
        navCon= Navigation.findNavController(this,R.id.host_fragment);
        Toolbar tb=findViewById(R.id.toolbar);
        abdt=new ActionBarDrawerToggle(this,drawer,tb,R.string.msg_navigation_drawer_open,R.string.msg_navigation_drawer_close);

        //setup Navigation
        NavigationUI.setupActionBarWithNavController(this,navCon,drawer);
        NavigationUI.setupWithNavController(navView,navCon);

        //Toolbar and Drawer toggle setup
        tb.setTitle(getString(R.string.app_name));//set toolbar Title to app name
        setSupportActionBar(tb);//set the toolbar as Action bar
        drawer.addDrawerListener(abdt);
        abdt.syncState();//VERY IMPORTANT TO APPLY CHANGES!!

        //initialize textview in navigation header with version and build date
        TextView tv=navView.getHeaderView(0).findViewById(R.id.nav_header_tv_ver);
        tv.setText(getNavigationHeaderText());
    }
    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navCon, drawer);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //todo setup options menu
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        //todo setup options
        return true;
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
        //todo add items
        drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * make fragment transaction to show Fragment
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        abdt.setDrawerIndicatorEnabled(true);
        abdt.syncState();
    }

    /**
     * Implementation of corresponding method in  tools.drawerToggleInterface
     * Shows the back icon in toolbar,
     * so the drawer cant be opened from the calling activity/fragment
     */
    @Override
    public void showBackIcon() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        abdt.setDrawerIndicatorEnabled(false);
        abdt.syncState();
    }

    /**
     * Builds a String based on the last update date of the package info (a.k.a. the build date of the application)
     * and the current version string to be shown in the TextView in navigation header
     * returns undefined resource string, if an error occurred
     * @return crafted string
     */
    private String getNavigationHeaderText(){
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(new ContextWrapper(getApplicationContext()).getPackageName(), 0);
            return pInfo.versionName+" - "+new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(new Date(pInfo.lastUpdateTime));
        }
        catch(PackageManager.NameNotFoundException e) {
            return getString(R.string.error_undefined);
        }
    }
}
package de.fwg.qr.scanner;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import de.fwg.qr.scanner.tools.preferencesManager;

public class activityStart extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_start);
    }

    @Override
    protected void onResume() {
        super.onResume();
        preferencesManager pm=new preferencesManager(getApplicationContext());
        if(!pm.isFirstRun()){
            //setContentView(R.layout.activity_main);//this doesnt help, because it only trys to apply the layout file to the current activity
            //solution:
            //start a new intent for target actvity here
            finish();//exit the current activity
            /** i think we should rethink how we gonna start the start activity, because the solution
             * above needs unnecessary resources.
             * Im thinking of starting the activityMain always at first, and check if its the first run, if so then
             * start this activity
             */
        }
    }
}
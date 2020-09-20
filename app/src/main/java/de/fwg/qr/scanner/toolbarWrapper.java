package de.fwg.qr.scanner;

import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * wrapper class for common activity resources
 */
public abstract class toolbarWrapper extends AppCompatActivity {
    private Toolbar tb;

    protected void onCreate(int contentView, final Activity a, String title) {
        setContentView(contentView);
        tb = findViewById(R.id.toolbar);
        tb.setTitle(title);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                a.finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    void setupAbHome() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * set the toolbar title
     * @param title value
     */
    void setToolbarTitle(String title) {
        tb.setTitle(title);
    }

    /**
     * (un)lock ui, used during network request to prevent errors
     * @param state (un)lock
     */
    void lockUI(final boolean state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state) {
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                } else {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            }
        });
    }
}

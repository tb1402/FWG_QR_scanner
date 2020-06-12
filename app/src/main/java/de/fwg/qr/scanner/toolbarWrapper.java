package de.fwg.qr.scanner;

import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public abstract class toolbarWrapper extends AppCompatActivity {
    private Toolbar tb;

    protected void onCreate(int contentView, final Activity a, String title){
        setContentView(contentView);
        tb=findViewById(R.id.toolbar);
        tb.setTitle(title);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                a.finish();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    void setupAbHome(){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
    void setToolbarTitle(String title){
        tb.setTitle(title);
    }
    void lockUI(boolean state) {
        if (state) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }
}

package de.fwg.qr.scanner;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;


public class activityMap extends toolbarWrapper {

    private ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);
        setupAbHome();
        imageView = findViewById(R.id.imageView);

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radioButton1:
                if (checked)
                    break;
            case R.id.radioButton2:
                if (checked)
                    break;
            case R.id.radioButton3:
                if (checked)
                    break;
        }
    }

}

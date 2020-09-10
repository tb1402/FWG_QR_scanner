package de.fwg.qr.scanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import de.fwg.qr.scanner.tools.network;
import de.fwg.qr.scanner.tools.networkCallbackInterface;


public class activityMap extends toolbarWrapper implements networkCallbackInterface {

    private ImageView imageView;
    private ArrayList<Bitmap> images;
    private TextView textView;
    private ProgressBar progressBar;

    private RadioButton button1;
    private RadioButton button2;
    private RadioButton button3;

    private network net;
    private WeakReference<networkCallbackInterface> ref;

    private int i = 0;


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(R.layout.activity_map, this, getString(R.string.item_map));
        super.onCreate(savedInstanceBundle);
        net = new network(this);
        ref = new WeakReference<>((networkCallbackInterface) this);
        images = new ArrayList<>();
        setupAbHome();
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressBar);
        button1 = findViewById(R.id.radioButton1);
        button2 = findViewById(R.id.radioButton2);
        button3 = findViewById(R.id.radioButton3);
        getImages();


    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radioButton1:
                if (checked) {
                    if (images.size() >= 1) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(0));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.radioButton2:
                if (checked) {
                    if (images.size() >= 2) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(1));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
            case R.id.radioButton3:
                if (checked) {
                    if (images.size() >= 3) {
                        if (textView.getVisibility() == View.VISIBLE || progressBar.getVisibility() == View.VISIBLE) {
                            progressBar.setVisibility(View.INVISIBLE);
                            textView.setVisibility(View.INVISIBLE);
                        }
                        imageView.setImageBitmap(images.get(2));
                    } else {
                        imageView.setImageBitmap(null);
                        progressBar.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    @Override
    public void onPostCallback(String operation, String response) {

    }

    @Override
    public void onImageCallback(String name, Bitmap image) {
        if (name.contentEquals("ImagePreview")) {
            images.add(image);
            if (images.size() >= 1) {
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                if (images.size() == 1) {
                    imageView.setImageBitmap(images.get(0));
                } else if (images.size() == 2 && button2.isChecked()) {
                    imageView.setImageBitmap(images.get(1));
                } else if (images.size() == 3 && button3.isChecked()) {
                    imageView.setImageBitmap(images.get(2));
                }
            }
            i++;
            if (i < 3) {
                getImages();
            } else {
                if (button1.isChecked()) {
                    imageView.setImageBitmap(images.get(0));
                } else if (button2.isChecked()) {
                    imageView.setImageBitmap(images.get(1));
                } else if (button3.isChecked()) {
                    imageView.setImageBitmap(images.get(2));
                }
            }

        }

    }

    public void getImages() {
        net.makeImageRequest(ref, "ImagePreview", "map", i, true);
    }


   /* @Override
    public void cacheCallback(boolean error, Bitmap image) {
        if (!error) {
            images.add(image);
            if (images.size() >= 1) {
                progressBar.setVisibility(View.INVISIBLE);
                textView.setVisibility(View.INVISIBLE);
                if (images.size() == 1) {
                    imageView.setImageBitmap(images.get(0));
                } else if (images.size() == 2 && button2.isChecked()) {
                    imageView.setImageBitmap(images.get(1));
                } else if (images.size() == 3 && button3.isChecked()) {
                    imageView.setImageBitmap(images.get(2));
                }
                i++;
                if (i < 3) {
                    getImages();
                } else {
                    if (button1.isChecked()) {
                        imageView.setImageBitmap(images.get(0));
                    } else if (button2.isChecked()) {
                        imageView.setImageBitmap(images.get(1));
                    } else if (button3.isChecked()) {
                        imageView.setImageBitmap(images.get(2));
                    }
                }
            }
        }
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.tb_item_settings:
                Intent i = new Intent(getApplicationContext(), activitySettings.class);
                startActivity(i);
                return true;
            case R.id.tb_item_map:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

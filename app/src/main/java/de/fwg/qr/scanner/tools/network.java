package de.fwg.qr.scanner.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import de.fwg.qr.scanner.R;

public class network {
    public static String baseURL = "https://srv.cloud-tb.de";
    private Context c;

    public network(Context c) {
        this.c = c;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private StringRequest getPostRequest(WeakReference<networkCallbackInterface> w, final String operation, final String data, String requestURL) {
        final networkCallbackInterface nci = w.get();
        StringRequest r = new StringRequest(Request.Method.POST, baseURL +"/api/"+ requestURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                nci.onPostCallback(operation, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                nci.onPostCallback("error", error.toString());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> d = new HashMap<>();
                d.put("data", data);
                return d;
            }
        };
        r.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        return r;
    }

    private ImageRequest getImageRequest(WeakReference<networkCallbackInterface> w, final String name, String id, int number, boolean preview) {
        final networkCallbackInterface nci = w.get();
        String url;
        if(preview){
            url=baseURL+"/images/low/"+id+"/"+number+".png";
        }
        else{
            preferencesManager p=new preferencesManager(c);
            url=baseURL+"/images/"+p.getImageResolution()+"/"+id+"/"+number+".png";
        }
        return new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        nci.onImageCallback(name, response);
                    }
                }, 0, 0, null, Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        nci.onImageCallback("error", BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_error));
                    }
                });
    }

    public void makePostRequest(WeakReference<networkCallbackInterface> nci, String operation, String data, String requestURL) {
        requestQueueSingleton.getInstance(c).addToRq(getPostRequest(nci, operation, data, requestURL));
    }

    /**
     * Method to make a request for an image
     * @param nci reference to networkCallbackInterface for callback
     * @param name name of the request, to differentiate the request in callback
     * @param id the id of the station
     * @param number the number of the image being requested
     * @param preview is image needed for preview only? if so, only low resolution image will be given back
     */
    public void makeImageRequest(WeakReference<networkCallbackInterface> nci, String name, String id, int number, boolean preview) {
        requestQueueSingleton.getInstance(c).addToRq(getImageRequest(nci, name, id,number,preview));
    }
}

package de.fwg.qr.scanner.tools;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.fwg.qr.scanner.BuildConfig;
import de.fwg.qr.scanner.R;
import de.fwg.qr.scanner.tools.cache.cacheManager;

/**
 * class used for performing network requests
 */
public class network {
    public static String baseURL = "https://fwgqr.ml";//server url
    private HashMap<String, String> headers;//headers of the request, used to add authentication and useragent
    private cacheManager cm;

    /**
     * Static field for network instance
     */
    private static network network;

    /**
     * get instance
     *
     * @param c context
     * @return network instance
     */
    public static network getInstance(Context c) {
        if (network == null) {
            network = new network(c);
        }
        return network;
    }

    /**
     * constructor
     *
     * @param c context
     */
    private network(Context c) {
        cm = new cacheManager(c);

        //get version name, needed in userAgent
        String versionName;
        try {
            versionName = c.getPackageManager().getPackageInfo(new ContextWrapper(c).getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "failed " + e.toString();
        }

        //hashMap with headers, which will be send in every request
        headers = new HashMap<>();

        //auth credentials
        String credentials = "fwgqr:" + BuildConfig.HTTP_AUTH_PW;
        String enc = Base64.encodeToString(credentials.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);

        //add headers
        headers.put("User-Agent", "FWG_QR_Scanner_version " + versionName + " on " + Build.DEVICE + " " + Build.VERSION.RELEASE);
        headers.put("Authorization", "Basic " + enc);
    }

    /**
     * Method to check whether (no) network connectivity is available
     *
     * @param c context
     * @return no network available?
     */
    public boolean noNetworkAvailable(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Method to craft a post request
     *
     * @param nci         reference to callback interface
     * @param operation name of file that is requested
     * @param data      data that will be sent via post
     * @return StringRequest to be added to requestQueue
     */
    private StringRequest getPostRequest(final networkCallbackInterface nci, final String operation, final String data) {
        StringRequest r = new StringRequest(Request.Method.POST, baseURL + "/api/" + operation, new Response.Listener<String>() {
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
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> d = new HashMap<>();
                d.put("data", data);//put post data
                return d;
            }

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
        r.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//increase timeout
        return r;
    }

    /**
     * Method to craft an image request
     *
     * @param nci       reference to callback interface
     * @param name    name of the request to identify it in the callback method
     * @param id      id of the image
     * @param number  number of the image
     * @param preview if true only low quality will be used
     * @param c       context
     * @return ImageRequest that can be added to the requestQueue
     */
    private ImageRequest getImageRequest(final networkCallbackInterface nci, final String name, final String id, final int number, final boolean preview, final Context c) {
        String url = baseURL + (preview ? "/images/low/" + id + "/" + number + ".png" : "/images/" + preferencesManager.getInstance(c).getImageResolution() + "/" + id + "/" + number + ".png");
        return new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        nci.onImageCallback(name, response);
                        cm.cacheImage(id, number, response, preview);//pass to cacheManager
                    }
                }, 0, 0, null, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        nci.onImageCallback("error", BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_error));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
    }

    /**
     * Method to craft an image request that will also give back the id of the station
     *
     * @param nci       reference to callback interface
     * @param name    name of the request to identify it in the callback method
     * @param id      id of the image
     * @param number  number of the image
     * @param preview if true only low quality will be used
     * @param c       context
     * @return ImageRequest that can be added to the requestQueue
     */
    private ImageRequest getImageRequestWithID(final networkCallbackImageID nci, final String name, final String id, final int number, final boolean preview, final Context c) {
        String url = baseURL + (preview ? "/images/low/" + id + "/" + number + ".png" : "/images/" + preferencesManager.getInstance(c).getImageResolution() + "/" + id + "/" + number + ".png");
        return new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        nci.onImageCallback(name, response, number);
                        cm.cacheImage(id, number, response, preview);//pass to cacheManager
                    }
                }, 0, 0, null, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        nci.onImageCallback("error", BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_error), number);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };
    }

    /**
     * Method to make a request and send post data with it
     *
     * @param nci       reference to networkCallbackInterface for callback
     * @param operation name of requested api file, this value will also be given back in callback method, along with the response from the server
     * @param data      post data to be send to the server
     * @param c         context
     */
    public void makePostRequest(networkCallbackInterface nci, String operation, String data, Context c) {
        requestQueueSingleton.getInstance(c).addToRq(getPostRequest(nci, operation, data), c);
    }

    /**
     * Method to make a request for an image
     *
     * @param nci     reference to networkCallbackInterface for callback
     * @param name    name of the request, to differentiate the request in callback
     * @param id      the id of the station
     * @param number  the number of the image being requested
     * @param preview is image needed for preview only? if so, only low resolution image will be given back
     * @param c       context
     */
    public void makeImageRequest(networkCallbackInterface nci, String name, String id, int number, boolean preview, Context c) {
        if (!cm.loadCachedImage(nci, id, name, number, preview)) {
            requestQueueSingleton.getInstance(c).addToRq(getImageRequest(nci, name, id, number, preview, c), c);
        }
    }

    /**
     * Method to make a request for an image and also give back the image number in the callback
     *
     * @param nci     reference to networkCallbackImageID for callback
     * @param name    name of the request, to differentiate the request in callback
     * @param id      the id of the station
     * @param number  the number of the image being requested
     * @param preview is image needed for preview only? if so, only low resolution image will be given back
     * @param c       context
     */
    public void makeImageRequestWithIDCallback(networkCallbackImageID nci, String name, String id, int number, boolean preview, Context c) {
        requestQueueSingleton.getInstance(c).addToRq(getImageRequestWithID(nci, name, id, number, preview, c), c);
    }
}

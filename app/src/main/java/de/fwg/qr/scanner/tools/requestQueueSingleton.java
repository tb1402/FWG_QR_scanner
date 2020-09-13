package de.fwg.qr.scanner.tools;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton for the volley requestQueue
 */
public class requestQueueSingleton {
    private static requestQueueSingleton queueSingleton;
    private RequestQueue rq;

    private requestQueueSingleton(Context c) {
        rq = getRequestQueue(c);
    }

    static synchronized requestQueueSingleton getInstance(Context c) {
        if (queueSingleton == null) {
            queueSingleton = new requestQueueSingleton(c);
        }
        return queueSingleton;
    }

    RequestQueue getRequestQueue(Context c) {
        if (rq == null) {
            rq = Volley.newRequestQueue(c.getApplicationContext());
        }
        return rq;
    }

    <T> void addToRq(Request<T> r, Context c) {
        getRequestQueue(c).add(r);
    }

    void cancelAll(Object tag){
        rq.cancelAll(tag);
    }
}

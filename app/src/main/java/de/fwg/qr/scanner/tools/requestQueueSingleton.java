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

    /**
     * get request queue singleton instance
     * @param c context
     * @return requestQueueSingleton
     */
    static synchronized requestQueueSingleton getInstance(Context c) {
        if (queueSingleton == null) {
            queueSingleton = new requestQueueSingleton(c);
        }
        return queueSingleton;
    }

    /**
     * get request queue
     * @param c context
     * @return request Queue
     */
    RequestQueue getRequestQueue(Context c) {
        if (rq == null) {
            rq = Volley.newRequestQueue(c.getApplicationContext());
        }
        return rq;
    }

    /**
     * add a request to the queue
     * @param r request
     * @param c context
     * @param <T> type of request
     */
    <T> void addToRq(Request<T> r, Context c) {
        getRequestQueue(c).add(r);
    }
}

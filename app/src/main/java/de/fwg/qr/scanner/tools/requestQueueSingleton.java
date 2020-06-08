package de.fwg.qr.scanner.tools;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class requestQueueSingleton {
    private static requestQueueSingleton queueSingleton;
    private static Context c;
    private RequestQueue rq;

    private requestQueueSingleton(Context c){
        requestQueueSingleton.c =c;
        rq= getRequestQueue();
    }
    static synchronized requestQueueSingleton getInstance(Context c){
        if(queueSingleton==null){
            queueSingleton=new requestQueueSingleton(c);
        }
        return queueSingleton;
    }
    RequestQueue getRequestQueue(){
        if(rq==null){
            rq= Volley.newRequestQueue(c.getApplicationContext());
        }
        return rq;
    }
    <T> void addToRq(Request<T> r){
        getRequestQueue().add(r);
    }
}

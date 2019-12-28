package com.example.smsclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.Volley;

import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    private String apiurl = "";
    private int intervals = 1000;
    private Context mContext;
    private Timer timer = null;
    TimerTask task = null;
    private RequestQueue queue;
    @Override
    public IBinder onBind(Intent intent) {
        return  null;
    }
    @Override
    public void onCreate() {
        Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
        this.mContext = this;
        queue = Volley.newRequestQueue(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        apiurl = intent.getStringExtra("API");
        intervals = intent.getIntExtra("INTERVALS", 1000);
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        timer = new Timer();
        task = new TimerTask () {
            @Override
            public void run () {
                HTTRequest request = new HTTRequest(mContext, apiurl, queue);
                request.requestHttp(Request.Method.GET, apiurl, null);
            }
        };
        timer.schedule (task, 01, intervals);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        timer.cancel();
        task.cancel();
    }



}

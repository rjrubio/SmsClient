package com.example.smsclient;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.android.volley.*;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import java.util.Timer;
import java.util.TimerTask;




public class MyService extends Service {
    private String apiurl = "http://192.168.254.111:8000/sms/";
    private int intervals = 30000;
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
        this.mContext = this;
        queue = Volley.newRequestQueue(this);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction() != null && intent.getAction().equals("STOP_ACTION")) {
            stopForeground(true);
            stopSelf();
            return START_STICKY;
        }
        Intent stopIntent = new Intent(mContext, ServiceBroadcastReceiver.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0, stopIntent, 0);

        //For creating the Foreground Service with notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getNotificationChannel(notificationManager) : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_mydoctorph)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Impruv SMS Server is running") //set title of notification
                .setContentText("Sent Messages : 0")
                .addAction(R.drawable.ic_action_name, "Stop Server", contentIntent)
                .build();

        startForeground(110, notification);


        apiurl = intent.getStringExtra("API");
        intervals = intent.getIntExtra("INTERVALS", 1000);
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        timer = new Timer();
        task = new TimerTask () {
            @Override
            public void run () {

                hTTPSender(Request.Method.GET, apiurl, null);
            }
        };
        timer.schedule (task, 01, intervals*1000);

        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        if(timer !=null){
            timer.cancel();
        }
        if(task !=null) {
            task.cancel();
        }
    }

    private void hTTPSender(int httpMethod, String api, JSONObject jsonObj ){
        if(mContext != null && api != null){
            HTTRequest request = new HTTRequest(mContext, api, queue);
            if(jsonObj == null)
               request.requestHttp(httpMethod, apiurl, null);
            else
                request.requestHttp(httpMethod, apiurl, jsonObj);
        }
    }

    private String getNotificationChannel(NotificationManager notificationManager){
        String channelId = "impruvsms";
        String channelName = getResources().getString(R.string.app_name);
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setImportance(NotificationManager.IMPORTANCE_NONE);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

}

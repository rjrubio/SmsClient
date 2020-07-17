package com.example.smsclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private Context mContext = this;

    private String apiurlDevice = "http://192.168.254.111:8000/devices/";
    private String apiUrl = "http://192.168.254.111:8000/sms/";
    private String id = "-1";
    int retries = 0;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "FROM:" + remoteMessage.getFrom());

        //Check if the message contains data
        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
        }

        //Check if the message contains notification

        if(remoteMessage.getNotification() != null) {
            Log.d(TAG, "Mesage body:" + remoteMessage.getNotification().getBody());
            id = remoteMessage.getNotification().getBody();
            sendNotification(id);
            if (!BuildConfig.DEBUG) {
                apiUrl = "http://impruvitsolutions.com:8000/sms/";
                apiurlDevice = apiUrl.replace("sms", "devices");
            }
            getDataFromServer(id, 0);
        }
    }

    /**
     * Dispay the notification
     * @param body
     */
    private void sendNotification(String body) {

        Intent stopIntent = new Intent(mContext, ServiceBroadcastReceiver.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0, stopIntent, 0);
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext, "impruvsms");
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_mydoctorph)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Impruv SMS Server is running") //set title of notification
                .setContentText(body)
                .addAction(R.drawable.ic_action_name, "Stop Server", contentIntent)
                .build();
        notificationManager.notify(110, notification);
    }

    private void getDataFromServer(String id, final int simSlot){

        RequestQueue queue = Volley.newRequestQueue(mContext);
        JsonRequest getRequest = new JsonObjectRequest(Request.Method.GET, apiUrl+ id+ "/", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d(TAG, response.toString());
                        try {
                            String number = response.getString("mobilenumber");
                            String message = response.getString("message");
                            //send sms via sms manager
                            SMSSenderTask sms = new SMSSenderTask(mContext, number, message, response, simSlot);
                            sms.setOnResultListener(asynResult);
                            sms.send();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString());
                    }
                }
        );
        // add it to the RequestQueue
        queue.add(getRequest);
    }


    SMSSenderTask.OnAsyncResult asynResult = new SMSSenderTask.OnAsyncResult() {
        @Override
        public void onResultSuccess(final int resultCode, final String message, final JSONObject jsonObject) {
            try {
                int id = jsonObject.getInt("id");
                jsonObject.remove("status");
                jsonObject.put("status", "true");
                RequestQueue queue = Volley.newRequestQueue(mContext);

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.PUT, apiUrl + id + "/", jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                retries = 0;
                                Log.d("Response PUT", response.toString());
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error.Response", error.toString());
                            }
                        }
                );
                // add it to the RequestQueue
                queue.add(getRequest);
            }catch (JSONException ex){
                Log.e("SMSSenderTask.OnAsyncResult", ex.toString());
            }
            Log.d("SENT SMS", message);

        }

        @Override
        public void onResultFail(final int resultCode, final String errorMessage, final String address, final JSONObject jsonObject) {
            Log.d("FAILED SMS TRYING..", errorMessage);
            if(retries == 0)
                getDataFromServer(id, 1);
        }
    };


}

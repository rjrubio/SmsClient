package com.example.smsclient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.content.Context.NOTIFICATION_SERVICE;

public class HTTRequest {
    private RequestQueue queue;
    private boolean result = false;
    public String apiurl = "";
    private Context mContexct;
    SMSSenderTask sms;
    static int smsSent = 0;

    HTTRequest(Context mContexct, String apiurl, RequestQueue queue) {
        this.mContexct = mContexct;
        this.apiurl = apiurl;
        this.queue = queue;
    }

    public boolean requestHttp(int method, String api, final JSONObject jsonObject) {
        // prepare the Request
        if (method == Request.Method.GET) {
            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, apiurl, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            // display response
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject jsonobject = response.getJSONObject(i);
                                    Log.d("Response GET", jsonobject.toString());
                                    try {
                                        String number = jsonobject.getString("mobilenumber");
                                        String message = jsonobject.getString("message");
                                        //send sms via sms manager
                                        sms = new SMSSenderTask(mContexct, number, message, jsonobject, 0);
                                        sms.setOnResultListener(asynResult);
                                        sms.send();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
                            result = false;
                        }
                    }
            );
            // add it to the RequestQueue
            queue.add(getRequest);
        }

        if (method == Request.Method.PUT) {
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.PUT, api, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.d("Response PUT", response.toString());
                            result = true;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
                            result = false;
                        }
                    }
            );
            // add it to the RequestQueue
            queue.add(getRequest);
        }

        if (method == Request.Method.POST) {
            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, api, jsonObject,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.d("Response PUT", response.toString());
                            result = true;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Error.Response", error.toString());
                            result = false;
                        }
                    }
            );
            // add it to the RequestQueue
            queue.add(getRequest);
        }
        return result;
    }


    SMSSenderTask.OnAsyncResult asynResult = new SMSSenderTask.OnAsyncResult() {
         @Override
         public void onResultSuccess(final int resultCode, final String message, final JSONObject jsonObject) {
             Log.d("SENT SMS", message);

         }

         @Override
         public void onResultFail(final int resultCode, final String errorMessage, final String address, final JSONObject jsonObject) {
             try {
                 jsonObject.remove("status");
                 jsonObject.put("status", "false");
                 smsSent++;
                 updateNotification(Integer.toString(smsSent));
                 requestHttp(Request.Method.POST, apiurl, jsonObject);
             }catch (JSONException ex){
                 Log.e("SMSSenderTask.OnAsyncResult", ex.toString());
             }
             Log.d("FAILED SMS", errorMessage);

         }
    };

    public void updateNotification(String strMessage){

        Intent stopIntent = new Intent(mContexct, ServiceBroadcastReceiver.class);
        PendingIntent contentIntent = PendingIntent.getBroadcast(mContexct, 0, stopIntent, 0);
        NotificationManager notificationManager = (NotificationManager) mContexct.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContexct, "impruvsms");
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_mydoctorph)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("Impruv SMS Server is running") //set title of notification
                .setContentText("Sent Messages :"+strMessage)
                .addAction(R.drawable.ic_action_name, "Stop Server", contentIntent)
                .build();
        notificationManager.notify(110, notification);
    }
}

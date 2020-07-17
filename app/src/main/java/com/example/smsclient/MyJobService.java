package com.example.smsclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MyJobService extends JobService {

    int retries = 0;

    @Override
    public boolean onStartJob(JobParameters params) {

        String apiurl = "http://192.168.254.111:8000/sms/";
        RequestQueue queue = Volley.newRequestQueue(this);
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
                                    sendSMS(number, message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    protected  void sendSMS(final String address, final String message){

        //for dual sim
        final ArrayList<Integer> simCardList = new ArrayList<>();
        final SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(this);

        Intent intent = new Intent("SMS_DELIVERED");
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        BroadcastReceiver bs = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int rCode = getResultCode();
                if (rCode == Activity.RESULT_OK) {
                    Log.d(TAG, "SMS SENT: ");
                }
                else{

                    if(retries == 0){
                        SmsManager.getSmsManagerForSubscriptionId(1)
                                .sendTextMessage(address, null, message, pendingIntent,  null);
                    }
                    Log.e(TAG, "SMS NOT SENT: ");

                }
            }
        };
        this.registerReceiver(bs,  new IntentFilter("SMS_DELIVERED"));
        final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            int subscriptionId = subscriptionInfo.getSubscriptionId();
            simCardList.add(subscriptionId);
        }

        int smsToSendFrom = simCardList.get(0); //assign your desired sim to send sms, or user selected choice
        SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom)
                .sendTextMessage(address, null, message, pendingIntent,  null);
    }
}

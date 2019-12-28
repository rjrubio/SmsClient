package com.example.smsclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HTTRequest {
    private RequestQueue queue;
    private boolean result = false;
    public String apiurl = "";
    private Context mContexct;

    HTTRequest(Context mContexct, String apiurl, RequestQueue queue){
        this.mContexct = mContexct;
        this.apiurl = apiurl;
        this.queue = queue;
    }
    public boolean requestHttp(int method, String url, JSONObject jsonObject) {
        // prepare the Request
        if (method == Request.Method.GET)
        {
            JsonArrayRequest getRequest = new JsonArrayRequest (Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>()
                    {
                        @Override
                        public void onResponse(JSONArray response) {
                            // display response
                            try {
                                for(int i = 0; i < response.length(); i++) {
                                    JSONObject jsonobject = response.getJSONObject(i);
                                    result = smsManager(jsonobject);
                                    int id = jsonobject.getInt("id");
                                    requestHttp(Request.Method.PUT,apiurl+id+"/", jsonobject);
                                    Log.d("Response", jsonobject.toString());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener()
                    {
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

        if (method == Request.Method.PUT)
        {
            JsonObjectRequest getRequest = new JsonObjectRequest  (Request.Method.PUT, url, jsonObject,
                    new Response.Listener<JSONObject>()
                    {
                        @Override
                        public void onResponse(JSONObject response) {
                            // display response
                            Log.d("Response", response.toString());
                            result = true;
                        }
                    },
                    new Response.ErrorListener()
                    {
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
    public boolean smsManager(JSONObject jsonobject) {
        try {
            String number = jsonobject.getString("mobilenumber");
            String message = jsonobject.getString("message");
            //send sms via sms manager
            sendSms(number, message, jsonobject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void sendSms(final String address, final String message, JSONObject jsonobject)
    {
        try
        {
            //update status field
            int id = jsonobject.getInt("id");
            jsonobject.put("status", true);
            String SENT = "SMS_SENT";
            IntentFilter filter = new IntentFilter(SENT);
            SmsReciever myReceiver = new SmsReciever(mContexct, id, address, message, jsonobject, apiurl);
            mContexct.registerReceiver(myReceiver, filter);
            PendingIntent sentPI = PendingIntent.getBroadcast(mContexct, 0, new Intent(SENT), 0);
            SmsManager smsMgr = SmsManager.getDefault();
            smsMgr.sendTextMessage(address, null, message, sentPI, null);

        }
        catch (Exception e)
        {
            Toast.makeText(mContexct, e.getMessage()+"!\n"+"Failed to send SMS", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }
}

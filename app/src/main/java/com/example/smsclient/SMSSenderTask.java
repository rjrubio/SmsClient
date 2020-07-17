package com.example.smsclient;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class SMSSenderTask {
    Context context;
    int tries = 0;
    String address = "";
    String message = "";
    OnAsyncResult onAsyncResult;
    JSONObject jsonObject;
    int simSlot = 0;

    private BroadcastReceiver bs = null;

    public void setOnResultListener(OnAsyncResult onAsyncResult) {
        if (onAsyncResult != null) {
            this.onAsyncResult = onAsyncResult;
        }
    }
    public SMSSenderTask(Context mContexct, final String address, final String message, JSONObject jsonObject, int simSlot) {
        context = mContexct;
        this.address = address;
        this.message = message;
        this.jsonObject = jsonObject;
        this.simSlot = simSlot;
    }

    protected Void send( ) {
        Intent intent = new Intent("SMS_DELIVERED");
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        BroadcastReceiver bs = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int rCode = getResultCode();
                if (rCode == Activity.RESULT_OK) {
                    onAsyncResult.onResultSuccess(0, message, jsonObject);
                    Log.d(TAG, "SMS SENT: " + message);
                }
                else{
                    if(tries == 0){
                        tries++;
                        sendSMS(address, message, pendingIntent, 1);
                    }else {
                        onAsyncResult.onResultFail(1, message, address, jsonObject);
                    }
                    Log.e(TAG, "SMS NOT SENT: " + message);

                }
                context.unregisterReceiver(this);
            }
        };
        context.registerReceiver(bs,  new IntentFilter("SMS_DELIVERED"));
        sendSMS(address, message, pendingIntent, simSlot);
        return null;
    }

    protected  void sendSMS(final String address, final String message, PendingIntent pendingIntent, int slot){
        //for dual sim
        final ArrayList<Integer> simCardList = new ArrayList<>();
        SubscriptionManager subscriptionManager;
        subscriptionManager = SubscriptionManager.from(context);
        final List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
            int subscriptionId = subscriptionInfo.getSubscriptionId();
            simCardList.add(subscriptionId);
        }


        if(slot < simCardList.size()){
            int smsToSendFrom = simCardList.get(slot); //assign your desired sim to send sms, or user selected choice
            if(smsToSendFrom > 0) {
                SmsManager smsManager =SmsManager.getSmsManagerForSubscriptionId(smsToSendFrom);
                ArrayList<String> parts =smsManager.divideMessage(message);
                int numParts = parts.size();
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();

                for (int i = 0; i < numParts; i++) {
                    sentIntents.add(pendingIntent);
                }
                smsManager.sendMultipartTextMessage(address, null, parts, sentIntents, null);
            }
        }
        else{
            onAsyncResult.onResultFail(1, message, address, jsonObject);
        }
    }

    public interface OnAsyncResult {
          void onResultSuccess(int resultCode, String message, JSONObject jsonObject);
          void onResultFail(int resultCode, String errorMessage, String address, JSONObject jsonObject);
    }
}

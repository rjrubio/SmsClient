package com.example.smsclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Start Activity
        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);
        // Start Services
        context.startService(new Intent(context, MyService.class).setAction("STOP_ACTION"));
    }
}
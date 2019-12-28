package com.example.smsclient;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.Request;
import org.json.JSONObject;

public class SmsReciever extends BroadcastReceiver {
    private Context mContext = null;
    private int id =0;
    private String mobileno = "";
    private String message = "";
    private  JSONObject jsonobject = null;
    private String apiuri = "";

    SmsReciever(Context mContext, int id, String mobileno, String message, JSONObject jsonobject, String apiuri){
        this.mContext = mContext;
        this.id = id;
        this.mobileno = mobileno;
        this.message = message;
        this.jsonobject = jsonobject;
        this.apiuri = apiuri;
    }
    @Override
    public void onReceive(Context arg0, Intent arg1)
    {
        int resultCode = getResultCode();
        //update our UI the status of sms via broadcast manager
        Intent intent = new Intent("SMSSERVER");
        intent.putExtra("MOBILENUMBER", mobileno);
        intent.putExtra("MESSAGE", message);
        switch (resultCode)
        {
            case Activity.RESULT_OK:
                intent.putExtra("STATUS", "SENT");
                //HTTRequest http = new HTTRequest(mContext, apiuri);
                //http.requestHttp(Request.Method.PUT,apiuri+id+"/", jsonobject);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                intent.putExtra("STATUS", "RESULT_ERROR_GENERIC_FAILURE");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                intent.putExtra("STATUS", "RESULT_ERROR_NO_SERVICE");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                intent.putExtra("STATUS", "RESULT_ERROR_NULL_PDU");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                intent.putExtra("STATUS", "RESULT_ERROR_RADIO_OFF");
                break;
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        mContext.unregisterReceiver(this);
    }

}

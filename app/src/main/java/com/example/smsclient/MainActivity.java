package com.example.smsclient;
import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {


    private EditText editTextAPI ;
    private EditText intervals ;
    private Button buttonStart;
    private Button buttonStop;
    private Context mContext;
    private String apiurl = "http://192.168.254.111:8000/sms/";
    private String apiurlDevice = "http://192.168.254.111:8000/devices/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        editTextAPI = findViewById(R.id.text_API);
        intervals = findViewById(R.id.intervals);
        buttonStart = findViewById(R.id.button_start);
        buttonStop = findViewById(R.id.button_stop);
        buttonStop.setEnabled(false);
        //ask neccessary permission/s
        askPermission();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Util.scheduleJob(mContext);
                //start our service worker
                startService();
                editTextAPI.setEnabled(false);
                intervals.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stopService(view);
                editTextAPI.setEnabled(true);
                intervals.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);

            }
        });

    }
    public void askPermission(){

        String[] permissions = new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE};
        int ALL_PERMISSIONS = 101;
        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: { //sms request

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    buttonStart.setEnabled(false);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    // Start the service
    public void startService() {
        if (!BuildConfig.DEBUG) {
            apiurl = editTextAPI.getText().toString();
            apiurlDevice = apiurl.replace("sms", "devices");
        }
        //Pass uri via intent to Service
        Intent intent= new Intent(mContext, MyService.class);
        intent.putExtra("API", apiurl);
        intent.putExtra("INTERVALS", Integer.parseInt(intervals.getText().toString()));
        startForegroundService (intent);
        //register device id to our web service
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String updatedToken = instanceIdResult.getToken();
                RequestQueue queue = Volley.newRequestQueue(mContext);
                JSONObject jsonobject = new JSONObject();
                try{
                    jsonobject.put("device_id", updatedToken);
                    jsonobject.put("device_model", Build.MODEL);
                    jsonobject.put("device_manufacturer", Build.MANUFACTURER);
                    jsonobject.put("api_url", apiurl);
                }catch (JSONException ex){
                    Log.e("MainActivity ", ex.toString());
                }

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.POST, apiurlDevice, jsonobject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
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
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("SMS-SERVER");



    }
    // Stop the service
    public void stopService(View view) {
        stopService(new Intent(this, MyService.class));
    }

}
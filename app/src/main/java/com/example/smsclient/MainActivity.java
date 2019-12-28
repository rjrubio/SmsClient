package com.example.smsclient;

import android.graphics.Color;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import 	android.content.IntentFilter;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {


    private EditText editTextAPI ;
    private EditText intervals ;
    private Button buttonStart;
    private Button buttonStop;
    private Context mContext;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> myDataset = new ArrayList<>();
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

        recyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setBackgroundColor(Color.BLACK);

        // specify an adapter (see also next example)
        mAdapter = new MyAdapter(myDataset);
        recyclerView.setAdapter(mAdapter);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDataset.add(0,"Starting service worker");
                mAdapter.notifyItemInserted(0);
                //start our service worker
                startService(view);
                //get recieved data from our Local broadcast manager
                LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                        new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                String mobilenumber = intent.getStringExtra("MOBILENUMBER");
                                String message = intent.getStringExtra("MESSAGE");
                                String status = intent.getStringExtra("STATUS");
                                if(myDataset.size() >100)
                                    myDataset.clear();
                                myDataset.add(0,status+": "+mobilenumber+": "+message);
                                mAdapter.notifyItemInserted(0);
                            }
                        }, new IntentFilter("SMSSERVER")
                );
                editTextAPI.setEnabled(false);
                intervals.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(view);
                editTextAPI.setEnabled(true);
                intervals.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                myDataset.add(0,"Stoping service worker");
                mAdapter.notifyItemInserted(0);

            }
        });

    }
    public void askPermission(){
        int reqCodeSMS = 101;
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.SEND_SMS)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.SEND_SMS},
                        reqCodeSMS);
            }
        } else {
            // Permission has already been granted
        }
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
    public void startService(View view) {
        //Pass uri via intent to Service
        Intent intent= new Intent(this, MyService.class);
        intent.putExtra("API", editTextAPI.getText().toString());
        intent.putExtra("INTERVALS", Integer.parseInt(intervals.getText().toString()) *1000);
        startService(intent);
    }
    // Stop the service
    public void stopService(View view) {
        stopService(new Intent(this, MyService.class));
    }

}
package com.lnquy065.uvzone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lnquy065.uvzone.adapters.BluetoothDiviceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDBData;
    private SwipeRefreshLayout swiperefreshBluetooth;
    private ArrayList<BluetoothDevice> listBluetooh;
    private BluetoothDiviceAdapter bteAdapter;
    private ListView lvBlueToothList;
    private FloatingActionButton btnOpenMap, btnOpenBluetooth, btnOpenInfo;

    private BluetoothAdapter bteScanAdapter;
    private ConstraintLayout vMain,vBluetooth;
    private ImageButton btnCloseBluetooh;

    private ImageView imStatus;

    private BroadcastReceiver bluetoohReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action  = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {  //Tim thay
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); //Lay doi tuong
                listBluetooh.add(device);
                bteAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        loadControls();
        loadEvents();
        requiretPermission();

    }





    private void loadControls() {
        imStatus = findViewById(R.id.imStatus);
        vMain = findViewById(R.id.vMain);
        vBluetooth = findViewById(R.id.vBluetooth);

        btnOpenMap = findViewById(R.id.btnOpenMap);
        swiperefreshBluetooth = findViewById(R.id.swiperefreshBluetooth);
        lvBlueToothList = findViewById(R.id.lvBlueToothList);
        btnOpenBluetooth = findViewById(R.id.btnOpenBluetooth);
        btnOpenInfo = findViewById(R.id.btnOpenUVInfo);
        btnCloseBluetooh = findViewById(R.id.btnCloseBluetooh);

        listBluetooh = new ArrayList<>();
        bteAdapter = new BluetoothDiviceAdapter(this, R.layout.bluetooth_item, listBluetooh);
        lvBlueToothList.setAdapter(bteAdapter);

        bteScanAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    private void loadEvents() {
        lvBlueToothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BluetoothDevice btd = listBluetooh.get(i);

                ProgressDialog dialog = ProgressDialog.show(MainActivity.this, "",
                        "Connecting. Please wait...", true);

//                    try {
//                        if (ControlPanelActivity.mmSocket!=null && ControlPanelActivity.mmSocket.isConnected()) closeBT();
//                        openBT(btd);

                        imStatus.setImageResource(R.drawable.connected);
                        vMain.setBackgroundResource(R.color.colorConnected);
                        //close bluetooth
                        vBluetooth.animate()
                                .translationY(0).setDuration(500)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        vBluetooth.setVisibility(View.GONE);
                                    }
                                });

                        btnOpenBluetooth.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                        btnOpenBluetooth.setVisibility(View.VISIBLE);

                        Intent intent = new Intent(MainActivity.this, ControlPanelActivity.class);
                        ControlPanelActivity.mmDevice = btd;
                        startActivity(intent);


            }
        });

        btnOpenBluetooth.setOnClickListener(v -> {


                    vMain.setBackgroundColor(android.R.attr.colorButtonNormal);
                    imStatus.setImageResource(R.drawable.notconnect);
            vBluetooth.setVisibility(View.VISIBLE);
            btnOpenBluetooth.setVisibility(View.GONE);
        });

        btnCloseBluetooh.setOnClickListener(v -> {
            vBluetooth.animate()
                    .translationY(0).setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            vBluetooth.setVisibility(View.GONE);
                            btnOpenBluetooth.setVisibility(View.VISIBLE);
                        }
                    });
        });

        btnOpenInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, UVActivity.class);
            startActivity(intent);
        });

        swiperefreshBluetooth.setOnRefreshListener( () -> {

            startBluetoothScan();
            listBluetooh.clear();
            bteAdapter.notifyDataSetChanged();

            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(10*1000);
                        unregisterReceiver(bluetoohReceiver);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swiperefreshBluetooth.setRefreshing(false);
                            }
                        });

                    } catch (InterruptedException e) {}
                }
            }.start();

        });

        btnOpenMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(mapIntent);
           // finish();
        });

    }



    private void requiretPermission() {
        IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
    }



    //bluetooh

    private void startBluetoothScan() {

        IntentFilter bteIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoohReceiver, bteIntentFilter);
        bteScanAdapter.startDiscovery();
    }


}

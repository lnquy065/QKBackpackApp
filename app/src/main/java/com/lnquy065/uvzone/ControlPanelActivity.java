package com.lnquy065.uvzone;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public class ControlPanelActivity extends AppCompatActivity {
    private DatabaseReference mDBUV;
    private DatabaseReference mDBCO2;


    private CheckBox cbUV, cbHum, cbTemp, cbCo2;
    private TextView lbUV, lbCo2, lbTemp, lbHum;
    private Switch swAlert;
    private ToggleButton btnRfid, btnVib, btnThief;
    private FloatingActionButton btnFind;

    public static BluetoothDevice mmDevice;
    public static BluetoothSocket mmSocket;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private int counter;
    private volatile boolean stopWorker;

    private long lastSend = 0;
    private long delaySend = 15*60*1000;


    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        mDBUV = FirebaseDatabase.getInstance().getReference("UV");
        mDBCO2 = FirebaseDatabase.getInstance().getReference("CO2");
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        loadControls();
        loadEvents();

        if (mmDevice==null) finish();
        try {
            if (ControlPanelActivity.mmSocket != null && ControlPanelActivity.mmSocket.isConnected()) closeBT();
            openBT(mmDevice);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadControls() {
        cbUV = findViewById(R.id.cbUV);
        cbCo2 = findViewById(R.id.cbCO2);
        cbTemp = findViewById(R.id.cbTemp);
        cbHum = findViewById(R.id.cbHumb);

        lbCo2 = findViewById(R.id.lbCO2);
        lbHum = findViewById(R.id.lbHum);
        lbUV = findViewById(R.id.lbUV);
        lbTemp = findViewById(R.id.lbTemp);

        swAlert = findViewById(R.id.swSensor);

        btnRfid = findViewById(R.id.btnRFID);
        btnThief = findViewById(R.id.btnTHIEF);
        btnVib = findViewById(R.id.btnVIB);

        btnFind = findViewById(R.id.btnFindMyBag);
    }


    private void loadEvents() {
        btnRfid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendBT("rfi"+compoundButton.isChecked()+"\n");
            }
        });

        btnVib.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                sendBT("vib"+compoundButton.isChecked()+"\n");
            }
        });

        btnFind.setOnClickListener(v -> {
            sendBT("findtrue\n");
        });
    }




    void openBT(BluetoothDevice mmDevice) throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        if (!mmSocket.isConnected()) finish();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();

        beginListenForData();

    }

    void sendBT(String t) {
        byte[] msgBuffer = t.getBytes();           //converts entered String into bytes
        try {
            mmOutputStream.write(msgBuffer);                //write bytes over BT connection via outstream
            Log.d("BTESend", t);
        } catch (IOException e) {
            //if you cannot write, close the application
            Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();

        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {

                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;



                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {

                                            //32.00|14.00|370|0
                                            String d[] = data.trim().split("\\|");
                                            Log.d("BluetoothRev", Arrays.toString(d));
                                            float temp = Float.valueOf(d[0]);
                                            float hum = Float.valueOf(d[1]);
                                            int co2 = Integer.valueOf(d[2]);
                                            int uv = Integer.valueOf(d[3]);

                                            lbTemp.setText(d[0]);
                                            lbHum.setText(d[1]);
                                            lbCo2.setText(d[2]);
                                            lbUV.setText(d[3]);

                                            if (!swAlert.isChecked()) return;
                                            //UV
                                            int uvLevel = UV.getUVLevel(uv);
                                            if (uvLevel > 0) {
                                                long[] mVibratePattern = new long[]{0, 400, 200, 400, 200, 400, 200, 400, 200, 400};
                                                if (uvLevel == UV.UV_MEDIUM)
                                                    mVibratePattern = new long[]{0, 400, 200, 400};
                                                else if (uvLevel == UV.UV_HIGH)
                                                    mVibratePattern = new long[]{0, 400, 200, 400, 200, 400};

                                                // trong vung nguy hiem
                                               Toast.makeText(ControlPanelActivity.this, "Nguy hiểm mức độ " + uvLevel + "!", Toast.LENGTH_SHORT).show();
                                                if (Build.VERSION.SDK_INT >= 26) {
                                                    vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, -1));
                                                } else {
                                                    vibrator.vibrate(mVibratePattern, -1);
                                                }

                                                //gui len firebase
                                                if (lastSend == 0 || System.nanoTime()/1000/1000 - lastSend > delaySend ) {
                                                    SingleShotLocationProvider.requestSingleUpdate(ControlPanelActivity.this,
                                                            new SingleShotLocationProvider.LocationCallback() {
                                                                @Override
                                                                public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                                                                    Log.d("Location", "my location is " + location.toString());

                                                                    Long tsLong = System.currentTimeMillis() / 1000;
                                                                    HashMap t = new HashMap();
                                                                    t.put("lat", location.latitude);
                                                                    t.put("lng", location.longitude);
                                                                    t.put("uv", uv);
                                                                    mDBUV.child(tsLong.toString()).setValue(t);
                                                                }
                                                            });
                                                    lastSend = System.nanoTime()/1000/1000;
                                                }
                                            }
                                            //CO2
                                            int co2Level = CO2.getUVLevel(co2);
                                            if (co2Level > 1) {
                                                long[] mVibratePattern = new long[]{0, 400, 200, 400, 200, 400, 200, 400, 200, 400};
                                                if (co2Level == CO2.CO2_MEDIUM)
                                                    mVibratePattern = new long[]{0, 400, 200, 400};
                                                else if (co2Level == CO2.CO2_HIGH)
                                                    mVibratePattern = new long[]{0, 400, 200, 400, 200, 400};

                                                // trong vung nguy hiem
                                                Toast.makeText(ControlPanelActivity.this, "Nguy hiểm mức độ " + co2Level + "!", Toast.LENGTH_SHORT).show();
                                                if (Build.VERSION.SDK_INT >= 26) {
                                                    vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, -1));
                                                } else {
                                                    vibrator.vibrate(mVibratePattern, -1);
                                                }

                                                //gui len firebase
                                                if (lastSend == 0 || System.nanoTime()/1000/1000 - lastSend > delaySend ) {
                                                    SingleShotLocationProvider.requestSingleUpdate(ControlPanelActivity.this,
                                                            new SingleShotLocationProvider.LocationCallback() {
                                                                @Override
                                                                public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
                                                                    Log.d("Location", "my location is " + location.toString());

                                                                    Long tsLong = System.currentTimeMillis() / 1000;
                                                                    HashMap t = new HashMap();
                                                                    t.put("lat", location.latitude);
                                                                    t.put("lng", location.longitude);
                                                                    t.put("co2", co2);
                                                                    mDBCO2.child(tsLong.toString()).setValue(t);
                                                                }
                                                            });
                                                    lastSend = System.nanoTime()/1000/1000;
                                                }
                                            }
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
    }
}

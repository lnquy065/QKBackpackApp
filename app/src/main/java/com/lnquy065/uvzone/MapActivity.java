package com.lnquy065.uvzone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private GoogleMap map;
    private LocationManager locationManager;





    private long timeThreshold = 60 * 60;
    private int uvDistance = 100;
    private int co2Distance = 100;

    private Vibrator vibrator;

    private ArrayList<CMaker> UVmarkers;
    private ArrayList<CMaker> CO2makers;

    private Switch swTracking;

    private com.github.clans.fab.FloatingActionButton floatmenu_item_styleNormal, floatmenu_item_styleSatellite;
    private DatabaseReference mUV;
    private DatabaseReference mCo2;

    private FloatingActionButton floatmenu_item_all, floatmenu_item_uv,floatmenu_item_co2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //load google map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapUV);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        //load firebase
        mUV = FirebaseDatabase.getInstance().getReference("UV");
        mCo2 = FirebaseDatabase.getInstance().getReference("CO2");

        requirePermission();
        loadControls();
        loadEvents();
    }

    private void requirePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    private void loadControls() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        UVmarkers = new ArrayList<>();
        CO2makers = new ArrayList<>();
        floatmenu_item_styleNormal = findViewById(R.id.floatmenu_item_styleNormal);
        floatmenu_item_styleSatellite = findViewById(R.id.floatmenu_item_styleSatellite);
        swTracking = findViewById(R.id.swTracking);
        floatmenu_item_all = findViewById(R.id.floatmenu_item_all);
        floatmenu_item_uv = findViewById(R.id.floatmenu_item_uv);
        floatmenu_item_co2 = findViewById(R.id.floatmenu_item_co2);
    }



    @SuppressLint("MissingPermission")
    private void loadEvents() {
        floatmenu_item_all.setOnClickListener(v -> {
            for (CMaker m: UVmarkers) {
                m.setVisible(true);
            }
            for (CMaker m: CO2makers) {
                m.setVisible(true);
            }
        });

        floatmenu_item_uv.setOnClickListener(v -> {
            for (CMaker m: CO2makers) {
                m.setVisible(false);
            }
        });

        floatmenu_item_co2.setOnClickListener(v -> {
            for (CMaker m: UVmarkers) {
                m.setVisible(false);
            }
        });


        floatmenu_item_styleSatellite.setOnClickListener(v -> map.setMapType(GoogleMap.MAP_TYPE_SATELLITE));
        floatmenu_item_styleNormal.setOnClickListener(v -> map.setMapType(GoogleMap.MAP_TYPE_NORMAL));

        swTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//
//                    double latitude=0;
//                    double longitude=0;
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//                    map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
//
//
//                }
            }



        });

        mCo2.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addCO2Location(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUV.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                addUVLocation(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5*1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("Dectected", "Warning!");
                    if (!swTracking.isChecked()) return;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                   //check in zone
                    Date date = new Date();

                    for (CMaker m: UVmarkers) {
                        Log.d("date", date.getHours() +" "+ stringHourToLong(m.getSnippet()));
                        if ( date.getHours() - stringHourToLong(m.getSnippet()) > 1) {
                            m.setVisible(false);
                            UVmarkers.remove(m);
                        } else {


                            Location endPoint=new Location("locationB");

                            endPoint.setLatitude(m.getPosition().latitude);
                            endPoint.setLongitude(m.getPosition().longitude);

                            double distance=location.distanceTo(endPoint);
                            Log.d("UV", m.getTitle() + " " +distance);
                            if (distance < uvDistance) {

                                //rung canh bao

                                long[] mVibratePattern = new long[]{0, 400, 200, 400,200, 400,200, 400,200, 400};
                                String tmp[] = m.getTitle().split(" ");
                                int uvLevel = Integer.valueOf(tmp[0]);
                                if (UV.getUVLevel(uvLevel)==UV.UV_MEDIUM) mVibratePattern = new long[] {0,400,200,400};
                                else if (UV.getUVLevel(uvLevel)==UV.UV_HIGH) mVibratePattern = new long[]{0,400,200,400,200,400};

                                // trong vung nguy hiem
                                Toast.makeText(MapActivity.this, "Nguy hiểm mức độ "+UV.getUVLevel(uvLevel)+"!", Toast.LENGTH_SHORT).show();
                                if (Build.VERSION.SDK_INT >= 26) {
                                    vibrator.vibrate(VibrationEffect.createWaveform(mVibratePattern, -1));
                                } else {
                                    vibrator.vibrate(mVibratePattern, -1);
                                }
                            }
                        }

                    }


                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            });

        }
    }

    private int stringHourToLong(String t) {
        String ta[] = t.split(":");
        return Integer.valueOf(ta[0]);
    }





    private boolean isTimeInRange(long uTime){
        long curTime = System.currentTimeMillis()/1000L;
        Log.d("Time", curTime+" "+uTime);
        return curTime-uTime < timeThreshold;
    }

    private void addUVLocation(DataSnapshot dataSnapshot) {
        long timeStamp = Long.valueOf(dataSnapshot.getKey().trim());
        if (!isTimeInRange(timeStamp)) return;

        Date date = new java.util.Date(timeStamp*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        HashMap vMap = (HashMap) dataSnapshot.getValue();
        //data info
        LatLng pos = new LatLng( Double.valueOf(vMap.get("lat").toString()),
                Double.valueOf(vMap.get("lng").toString()));
        int uv = Integer.valueOf(vMap.get("uv").toString());

        //addmap
        MarkerOptions m = new MarkerOptions().position(pos)
                .title(uv+" mw/cm2")
                .snippet(sdf.format(date))
                .anchor((float) 0.5, (float) 0.5)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wb_sunny));

        Marker mk = map.addMarker(m);
        Circle ck = map.addCircle(new CircleOptions().center(pos).radius(uvDistance).fillColor(UV.getUVSolidColor(uv)).strokeColor(UV.getUVStrokeColor(uv)).strokeWidth(1f));
        UVmarkers.add( new CMaker(mk, ck));
    }

    private void addCO2Location(DataSnapshot dataSnapshot) {
        long timeStamp = Long.valueOf(dataSnapshot.getKey().trim());
        if (!isTimeInRange(timeStamp)) return;

        Date date = new java.util.Date(timeStamp*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        HashMap vMap = (HashMap) dataSnapshot.getValue();
        //data info
        LatLng pos = new LatLng( Double.valueOf(vMap.get("lat").toString()),
                Double.valueOf(vMap.get("lng").toString()));

        int co2 = Integer.valueOf(vMap.get("co2").toString());

        //addmap
        MarkerOptions m = new MarkerOptions().position(pos)
                .title(co2+" ppm")
                .snippet(sdf.format(date))
                .anchor((float) 0.5, (float) 0.5)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_blur_circular));
    //    CO2makers.add(m);


        Marker mk = map.addMarker(m);
        Circle ck = map.addCircle(new CircleOptions().center(pos).radius(co2Distance).fillColor(CO2.getUVSolidColor(co2)).strokeColor(CO2.getUVStrokeColor(co2)).strokeWidth(1f));
        CO2makers.add( new CMaker(mk, ck));}

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setCompassEnabled(true);
        map.setMyLocationEnabled(true);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        map.moveCamera(CameraUpdateFactory.zoomTo(20f));
        return false;
    }



    class CMaker {
        Marker m;
        Circle c;
        public CMaker(Marker m, Circle c) {
            this.m = m;
            this.c = c;
        }



        public String getSnippet() {
            return m.getSnippet();
        }

        public void setVisible(boolean visible) {
            m.setVisible(visible);
            c.setVisible(visible);
        }

        public LatLng getPosition() {
            return m.getPosition();
        }

        public String getTitle() {
            return m.getTitle();
        }
    }
}

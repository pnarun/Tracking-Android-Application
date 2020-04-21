package com.example.newdevicetracker;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class ActualUserMapActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Marker usermarker,devicemarker,refmarker;
    EditText idTextBox;
    String deviceid;
    Timer timer;
    TimerTask task;
    String channel_id="personal notification";
    int id=001;
    public void startTimer()
    {
        if(task!=null)
        {
            timer.scheduleAtFixedRate(task,1000,1000);
        }
    }
    public void stopTimer()
    {
        timer.cancel();
    }
    private class Sender extends TimerTask
    {
        @Override
        public void run()
        {
           getLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1)
        {
            if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
            {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                }
            }
        }
    }
    public void getLocation()
    {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DevicesLocations");
        GeoFire geoFire=new GeoFire(ref);
        geoFire.getLocation(deviceid, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location)
            {
                if(location!=null)
                {
                    LatLng devloc=new LatLng(location.latitude,location.longitude);
                    if(devicemarker==null)
                    {
                        devicemarker=mMap.addMarker(new MarkerOptions().position(devloc).title("Device location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    }
                    else
                    {
                        //devicemarker.remove();
                        devicemarker.setPosition(devloc);
                    }
                    //devicemarker=mMap.addMarker(new MarkerOptions().position(devloc).title("Device location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                }
                else
                {
                    Toast.makeText(ActualUserMapActivity.this, "No such device is active", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
    public void logoutClicked(View view)
    {
        final String id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("DevicesLocations").child(id);
        ref1.removeValue();
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logging out....", Toast.LENGTH_SHORT).show();
        finish();
    }
    private void createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            CharSequence name="Personal Notifications";
            String description="Include all the personal notifications";
            int  importance= NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel=new NotificationChannel(channel_id,name,importance);
            channel.setDescription(description);
            NotificationManager notificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
    public void stClicked(View view)
    {
        if(!idTextBox.getText().toString().matches(""))
        {
            deviceid=idTextBox.getText().toString();
            startTimer();
        }
        else
        {
            Toast.makeText(this, "Device ID not entered", Toast.LENGTH_SHORT).show();
        }
        DatabaseReference ref;
        final GeoFire geoFire;
        if(!idTextBox.getText().toString().matches(""))
        {
            deviceid = idTextBox.getText().toString();

            ref = FirebaseDatabase.getInstance().getReference("DevicesLocations");
            geoFire = new GeoFire(ref);
            geoFire.getLocation(deviceid, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location)
                {
                    if (location != null)
                    {
                        LatLng refdevloc = new LatLng(location.latitude, location.longitude);
                        GeoQuery query=geoFire.queryAtLocation(new GeoLocation(refdevloc.latitude,refdevloc.longitude),0.01);
                        query.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, GeoLocation location)
                            {

                            }

                            @Override
                            public void onKeyExited(String key)
                            {
                                Toast.makeText(ActualUserMapActivity.this, "Vehicle exited", Toast.LENGTH_SHORT).show();
                                createNotificationChannel();
                                NotificationCompat.Builder builder=new NotificationCompat.Builder(ActualUserMapActivity.this,channel_id)
                                        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                                        .setContentTitle("Theft notification")
                                        .setContentText("Vehicle theft detected")
                                        .setAutoCancel(true)
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
                                NotificationManagerCompat manager=NotificationManagerCompat.from(ActualUserMapActivity.this);
                                manager.notify(id,builder.build());
                            }

                            @Override
                            public void onKeyMoved(String key, GeoLocation location)
                            {
                                Toast.makeText(ActualUserMapActivity.this, "Vehicle moving inside", Toast.LENGTH_SHORT).show();
                                //getLocation();
                            }

                            @Override
                            public void onGeoQueryReady()
                            {

                            }

                            @Override
                            public void onGeoQueryError(DatabaseError error)
                            {

                            }
                        });
                    } else
                    {
                        Toast.makeText(ActualUserMapActivity.this, "No such device is active", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    public void goClicked(View view)
    {
        if(!idTextBox.getText().toString().matches(""))
        {
            deviceid = idTextBox.getText().toString();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DevicesLocations");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.getLocation(deviceid, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    LatLng refdevloc = new LatLng(location.latitude, location.longitude);
                    if (refmarker == null) {
                        refmarker = mMap.addMarker(new MarkerOptions().position(refdevloc).title("Device reference location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        mMap.addCircle(new CircleOptions().center(refdevloc).radius(10).strokeWidth(8f).strokeColor(Color.BLUE).fillColor(Color.argb(70, 153, 204, 255)));
                    } else {
                        refmarker.remove();
                        refmarker.setPosition(refdevloc);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            /*if (!idTextBox.getText().toString().matches("")) {
                deviceid = idTextBox.getText().toString();
                startTimer();
            } else {
                Toast.makeText(this, "Device ID not entered", Toast.LENGTH_SHORT).show();
            }*/
        }
        else
        {
            Toast.makeText(ActualUserMapActivity.this, "No such device is active", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actual_user_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userMapFragment);
        mapFragment.getMapAsync(this);
        idTextBox=(EditText)findViewById(R.id.idTextBox);
        timer=new Timer();
        task=new Sender();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        locationManager=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                LatLng spot = new LatLng(location.getLatitude(), location.getLongitude());
                if(usermarker==null)
                {
                    usermarker=mMap.addMarker(new MarkerOptions().position(spot).title("Your location"));
                }
                else
                {
                    //usermarker.remove();
                    usermarker.setPosition(spot);
                }
                mMap.isBuildingsEnabled();
                mMap.isTrafficEnabled();
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setAllGesturesEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().isMapToolbarEnabled();
                mMap.getUiSettings().isTiltGesturesEnabled();
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(spot,20));
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(spot,25));
                Geocoder geocoder=new Geocoder(getApplicationContext(), Locale.getDefault());//to get the address of that current location
                try
                {
                    List<Address> listaddresses=geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    if(listaddresses!=null&&listaddresses.size()>0)
                    {
                        //Toast.makeText(MapsActivity.this, "Latitude:"+location.getLatitude()+"\nLongitude:"+location.getLongitude()+"\n\n\nAddress: "+listaddresses.get(0), Toast.LENGTH_SHORT).show();
                        Toast.makeText(ActualUserMapActivity.this, "Your location\n\nLatitude:"+location.getLatitude()+"\nLongitude:"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {

            }

            @Override
            public void onProviderEnabled(String provider)
            {

            }

            @Override
            public void onProviderDisabled(String provider)
            {

            }
        };
        if(Build.VERSION.SDK_INT<23)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1,0,locationListener);
        }
        else
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
            }
        }
    }
}

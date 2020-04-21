package com.example.newtrackingdevice;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NewDeviceMapActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
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
    public void logoutClicked(View view)
    {
        final String id= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref1= FirebaseDatabase.getInstance().getReference("DevicesLocations").child(id);
        ref1.removeValue();
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logging out....", Toast.LENGTH_SHORT).show();
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_device_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.deviceMapFragment);
        mapFragment.getMapAsync(this);
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
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location)
            {
                LatLng spot = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(spot).title("Your location"));
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
                        Toast.makeText(NewDeviceMapActivity.this, "Latitude:"+location.getLatitude()+"\nLongitude:"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DevicesLocations");
                GeoFire geoFire=new GeoFire(ref);
                geoFire.setLocation(userid,new GeoLocation(location.getLatitude(),location.getLongitude()));
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
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

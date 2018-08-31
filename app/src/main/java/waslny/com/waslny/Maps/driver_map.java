package waslny.com.waslny.Maps;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.EventListener;
import java.util.List;
import java.util.Map;

import waslny.com.waslny.R;
import waslny.com.waslny.activities.MainActivity;
import waslny.com.waslny.activities.check_for_login;

public class driver_map extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener ,View.OnClickListener
{
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private Button logout;
    private boolean available;

    private String customer_id="";

    private Marker pickupmarker;

    private DatabaseReference customerpickupLocationDatabaseReference;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        logout = (Button) findViewById(R.id.driver_log_out);
        logout.setOnClickListener(this);

          LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

         if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
         Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
         } else {
         showGPSDisabledAlertToUser();
         }

         getAssignedCutomer();

    }

    private void getAssignedCutomer()
    {
        String Driver_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference assignedCustomerReference=FirebaseDatabase.getInstance().getReference().child("Users").child("drivers").child(Driver_id).child("customerRideId");

        assignedCustomerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {

                        customer_id=dataSnapshot.getValue().toString();
                        getAssignedCutomerpickupLocation();

                }

                else
                {
                    customer_id="";
                    
                    if(pickupmarker != null)
                    {
                        pickupmarker.remove();
                    }

                    if(valueEventListener != null)
                    {
                        assignedCustomerReference.removeEventListener(valueEventListener);
                    }

                }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }



    private void getAssignedCutomerpickupLocation()
    {
        customerpickupLocationDatabaseReference=FirebaseDatabase.getInstance().getReference().child("customerRequests").child(customer_id).child("l");

       valueEventListener=customerpickupLocationDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists())
                {
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng=0;

                    if(map.get(0) != null)
                    {
                        locationlat=Double.parseDouble(map.get(0).toString());
                    }

                    if(map.get(1) != null)
                    {
                        locationlng=Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng=new LatLng(locationlat,locationlng);

                   pickupmarker=mMap.addMarker(new MarkerOptions().position(driverLatLng).title("pickup location"));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onStart()
    {
        super.onStart();
        available=true;
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        available=false;

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("DriversAvailable");

            GeoFire geoFire = new GeoFire(databaseReference);
            geoFire.removeLocation(user_id);
        }
    }



    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildgoogleApiClient();
        mMap.setMyLocationEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    private void buildgoogleApiClient()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(1000);
        locationRequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i){}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        if(getApplicationContext() != null)
        {


            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference availableReference = FirebaseDatabase.getInstance().getReference("DriversAvailable");
            DatabaseReference workingReference = FirebaseDatabase.getInstance().getReference("driverWorking");

            GeoFire geoFireavailable = new GeoFire(availableReference);
            GeoFire geoFireworking = new GeoFire(workingReference);
            switch (customer_id)
            {
                case "":

                    if (available)
                    {
                        geoFireworking.removeLocation(user_id);
                        geoFireavailable.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    }

                    break;
                default:

                    if (available)
                    {
                        geoFireavailable.removeLocation(user_id);
                        geoFireworking.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    }

                    break;
            }

        }
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Goto Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void onClick(View view)
    {
        if(view == logout)
        {
            available=false;
            String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();

            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("DriversAvailable");

            GeoFire geoFire=new GeoFire(databaseReference);
            geoFire.removeLocation(user_id);

            FirebaseAuth.getInstance().signOut();

            finish();
            startActivity(new Intent(getApplicationContext(),check_for_login.class));
        }
    }
}

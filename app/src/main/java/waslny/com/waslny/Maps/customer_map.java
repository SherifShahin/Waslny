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
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

import waslny.com.waslny.R;
import waslny.com.waslny.activities.MainActivity;

public class customer_map extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private Button logout;
    private Button request;
    private boolean available;
    private LatLng pickupLocation;


    private int radius=1;
    private Boolean driverFound=false;
    private String driverFoundId;


    private Marker DriverMarker;

    private boolean request_boolean=false;


    private GeoQuery geoQuery;
    private DatabaseReference driverLocationreReference;
    private ValueEventListener valueEventListener;

    private Marker pickupmarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        logout = (Button) findViewById(R.id.customer_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        request=(Button)findViewById(R.id.customer_pickup_request);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if(request_boolean)
                {
                    request_boolean=false;

                    geoQuery.removeAllListeners();
                    driverLocationreReference.removeEventListener(valueEventListener);


                    if(driverFoundId != null)
                    {
                        DatabaseReference driverreReference=FirebaseDatabase.getInstance().getReference().child("Users").child("drivers").child(driverFoundId).child("customerRideId");
                        driverreReference.removeValue();
                        driverFoundId=null;
                    }
                    driverFound=false;
                    radius=1;


                    String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customerRequests");


                    GeoFire geoFire = new GeoFire(databaseReference);
                    geoFire.removeLocation(user_id);


                    if(pickupmarker != null)
                    {
                        pickupmarker.remove();
                    }

                    if(DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }

                    request.setText("call waslny");
                }
                else
                    {
                     request_boolean=true;

                     String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                     DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customerRequests");

                    GeoFire geoFire = new GeoFire(databaseReference);
                    geoFire.setLocation(user_id, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                    pickupLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    pickupmarker=mMap.addMarker(new MarkerOptions().position(pickupLocation).title("pickup here"));

                    request.setText("Geting your Driver.....");

                    getclosestDriver();
                 }
            }
        });



        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "GPS is Enabled in your devide", Toast.LENGTH_SHORT).show();
        } else {
            showGPSDisabledAlertToUser();
        }

    }



    private void getclosestDriver()
    {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

        GeoFire geoFire=new GeoFire(databaseReference);

        geoQuery=geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && request_boolean)
                {
                    driverFound=true;
                    driverFoundId=key;

                    DatabaseReference driverreReference=FirebaseDatabase.getInstance().getReference().child("Users").child("drivers").child(driverFoundId);
                    String CustomerRideId=FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map=new HashMap();
                    map.put("customerRideId",CustomerRideId);
                    driverreReference.updateChildren(map);

                    getDriverLocation();
                    request.setText("Looking for driver Location.....");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if(!driverFound)
                {
                   radius++;
                   getclosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private void getDriverLocation()
    {
        driverLocationreReference=FirebaseDatabase.getInstance().getReference().child("driverWorking").child(driverFoundId).child("l");
        valueEventListener=driverLocationreReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists() && request_boolean)
                {
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationlat=0;
                    double locationlng=0;

                    request.setText("Driver Found");

                    if(map.get(0) != null)
                    {
                        locationlat=Double.parseDouble(map.get(0).toString());
                    }

                    if(map.get(1) != null)
                    {
                        locationlng=Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng=new LatLng(locationlat,locationlng);

                    if(DriverMarker != null)
                    {
                        DriverMarker.remove();
                    }


                    //get the distance between the driver and customer

                    Location location1=new Location("");
                    location1.setLatitude(pickupLocation.latitude);
                    location1.setLongitude(pickupLocation.longitude);

                    Location location2=new Location("");
                    location2.setLatitude(driverLatLng.latitude);
                    location2.setLongitude(driverLatLng.longitude);

                    float distance =location1.distanceTo(location2);

                    if(distance <= 100)
                    {
                        request.setText("Driver Here");

                    }
                    else
                    request.setText("Driver Found : "+String.valueOf(distance));

                    DriverMarker= mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver ").icon(BitmapDescriptorFactory.fromResource(R.mipmap.driver_car_icon)));
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
        lastLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
    }

    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it? ده لو تكرمت يعنى حضرتك -_-")
                .setCancelable(false)
                .setPositiveButton("Go to Settings Page To Enable GPS",
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
}

package com.example.moni.comehomesafe;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.List;

public class NavigationActivity extends FragmentActivity
        implements OnMapReadyCallback, DownloadListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String ADDRESS = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_DIRECTIONS_KEY = "AIzaSyC9a7v6ZEyOdTCU48xfJpDsew-1TXcZn7Q";
    private static final long FASTEST_INTERVAL = 10000;
    private static final long UPDATE_INTERVAL = 5000;
    private static final int WIDTH_POLYLINE = 8;
    private static final int CAMERA_ZOOM_LOCATION = 10;
    private static final int MAX_TIME_FOR_DISCREPANCY = 10;

    private GoogleMap mMap;
    private double startLat;
    private double startLng;
    private LatLng start;
    private LatLng destination;
    private double destinationLat;
    private double destinationLng;
    private LatLng currentLocation;
    private String companion;
    private String travelmode;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createGoogleApiClient();

        initButton();

        createAddress();

        if(start != null && destination != null) {
            new DirectionDownloadTask(this, start, destination).execute(createAddress());
        }
        getIntentExtras();
    }

    private void initButton() {
        Button btnStop = (Button) findViewById(R.id.button_stop_navigation);
        //TODO Button als Listener registieren
        //btnStop.setOnClickListener();

    }

    private void getIntentExtras() {
        Bundle bundle = getIntent().getParcelableExtra("BUNDLE");
        if(bundle!=null) {
            start = bundle.getParcelable("START");
            destination = bundle.getParcelable("DESTINATION");
            companion = bundle.getString("COMPANION");
            travelmode = bundle.getString("MODE");
        }
    }

    private String createAddress(){
        Log.d("ADDRESS: ", "origin=" + startLat + startLng + "&destination=" + destinationLat + destinationLng + "&key=" + GOOGLE_DIRECTIONS_KEY);
        return ADDRESS + "origin=" + startLat + "," + startLng + "&destination=" + destinationLat + "," + destinationLng + "&mode=" + travelmode + "&key=" + GOOGLE_DIRECTIONS_KEY;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng startPos = new LatLng(startLat, startLng);
        mMap.addMarker(new MarkerOptions().position(startPos).title("your position"));
        //null
        //mMap.addMarker(new MarkerOptions().position(destination).title("your destination"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, CAMERA_ZOOM_LOCATION));
    }

    @Override
    public void onDownloadFinished(List<LatLng> polyline) {
        if(polyline != null) {
            PolylineOptions route = new PolylineOptions();
            route.addAll(polyline);
            Polyline polylineRoute = mMap.addPolyline(route);
            polylineRoute.setColor(Color.BLUE);
            polylineRoute.setWidth(WIDTH_POLYLINE);
            //oder in requestLocationUpdates? wobei da die polyline Null ist
            checkForDiscrepancy(polyline);
        }
    }

    private void checkForDiscrepancy(List<LatLng> polyline) {
        int count = 0;
        while(polyline.contains(currentLocation)){
            count = 0;
        } if(!polyline.contains(currentLocation)){
            count++;
            if(count == MAX_TIME_FOR_DISCREPANCY){ //Beispielwert, muss getestet werden
                //TODO Benachrichtigung versenden etc.
            }
        }
    }

    private void createGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() { //evtl. if.isConnected()
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClient.connect();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(NavigationActivity.this, "GPS nicht aktiviert", Toast.LENGTH_SHORT).show();

            return;
        }
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLocation == null) {
            startLocationUpdates();
        }
        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
        }
    }

    private void startLocationUpdates() {
        if(mLocationRequest == null) {
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_INTERVAL)
                    .setFastestInterval(FASTEST_INTERVAL);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(NavigationActivity.this, "button add comp.", Toast.LENGTH_SHORT).show();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        //update UI
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("your position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LOCATION));

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        //--> Fehlermeldung: GoogleApiClient is not connected yet
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() /*&& !mLocationRequest*/) {
            startLocationUpdates();
        }
    }


}

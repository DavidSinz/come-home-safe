package com.example.moni.comehomesafe;


import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.internal.StringListResponse;
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

import java.net.URLEncoder;
import java.util.List;

public class NavigationActivity extends FragmentActivity
        implements OnMapReadyCallback, DownloadListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String ADDRESS = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_DIRECTIONS_KEY = "AIzaSyC9a7v6ZEyOdTCU48xfJpDsew-1TXcZn7Q";
    private static final long FASTEST_INTERVAL = 10000;
    private static final long UPDATE_INTERVAL = 5000;
    private static final int WIDTH_POLYLINE = 8;

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
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createGoogleApiClient();

        createAddress();

        //hinter this --> Übergabewerte; oder als try new.. und catch exception
        new DirectionDownloadTask(this, start, destination).execute(createAddress());

        Bundle bundle = getIntent().getParcelableExtra("BUNDLE");
        if(bundle!=null) {
            start = bundle.getParcelable("START");
            destination = bundle.getParcelable("DESTINATION");
            companion = bundle.getString("COMPANION");
            travelmode = bundle.getString("MODE");
        }
    }

    private String createAddress(){
        //String urlOrigin = URLEncoder.encode(start, "utf-8");
        //überprüfen, ob start & destination als LatLng richtig ausgegeben werden
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 6));
    }

    @Override
    public void onDownloadFinished(List<LatLng> polyline) {
        PolylineOptions route = new PolylineOptions();
        route.addAll(polyline);
        Polyline polylineRoute = mMap.addPolyline(route);
        polylineRoute.setColor(Color.BLUE);
        polylineRoute.setWidth(WIDTH_POLYLINE);
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10));

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

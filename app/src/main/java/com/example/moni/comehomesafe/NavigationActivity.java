package com.example.moni.comehomesafe;


import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class NavigationActivity extends FragmentActivity
        implements OnMapReadyCallback, DownloadListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String ADDRESS = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String GOOGLE_DIRECTIONS_KEY = "AIzaSyC9a7v6ZEyOdTCU48xfJpDsew-1TXcZn7Q";
    private static final long FASTEST_INTERVAL = 5000;
    private static final long UPDATE_INTERVAL = 10000;
    private static final int WIDTH_POLYLINE = 8;
    private static final int CAMERA_ZOOM_LOCATION = 18;
    private static final int MAX_TIME_DISCREPANCY = 500;
    private static final double MAX_DISCREPANCY = 0.0003;

    private GoogleMap mMap;
    private double startLat;
    private double startLng;
    private String destination;
    private double latDestination;
    private double lngDestination;
    private LatLng destinationLatLng;
    private LatLng currentLocation;
    private String companion;
    private String travelmode;
    List<LatLng> polyline;
    private Marker mMarker;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    //private LocationRequest mLocationRequest;

    SendSMS sms = new SendSMS();

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        // keeps screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createGoogleApiClient();

        initButton();

        getIntentExtras();

        createStartDialog();

        //createAddress();

        //startDownload();

        convertAddress();
    }

    private void createStartDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(NavigationActivity.this);
        dialog.setTitle(R.string.title_start_dialog);
        dialog.setMessage(R.string.text_start_dialog);
        //nur zum Testen auskommentiert --> gehört mit rein
        //dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendStartMessage();
            }
        });
        dialog.setNegativeButton(R.string.dialog_btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        dialog.show();
    }

    private void sendStartMessage() {
        sms.sendMessage(companion, "Bin unterwegs.");
    }

    private void startDownload() {
        if (currentLocation != null && destination != null && travelmode != null) {
            new DirectionDownloadTask(this, currentLocation, destination).execute(createAddress());
        }
    }

    private void initButton() {
        Button btnStop = (Button) findViewById(R.id.button_stop_navigation);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createStopNavigationDialog();
            }
        });
    }

    @Override
    public void onBackPressed() {
        createStopNavigationDialog();
    }

    private void createStopNavigationDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(NavigationActivity.this);
        dialog.setTitle(R.string.title_stop_dialog);
        dialog.setMessage(R.string.text_stop_dialog);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavigationActivity.this.finish();
            }
        });
        dialog.setNegativeButton(R.string.dialog_btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void getIntentExtras() {
        Bundle bundle = getIntent().getParcelableExtra("BUNDLE");
        if (bundle != null) {
            destination = bundle.getString("DESTINATION");
            companion = bundle.getString("COMPANION");
            travelmode = bundle.getString("MODE");
            Log.d("BUNDLE: ", bundle.toString());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //LatLng startPos = new LatLng(startLat, startLng);
        //mMap.addMarker(new MarkerOptions().position(startPos).title("your position"));
        //null
        //mMap.addMarker(new MarkerOptions().position(destination).title("your destination"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, CAMERA_ZOOM_LOCATION));
    }

    @Override
    public void onDownloadFinished(List<LatLng> polyline) {
        this.polyline = polyline;
        Log.d("polyline: ", this.polyline.toString());
        if (polyline != null) {
            PolylineOptions route = new PolylineOptions();
            route.addAll(polyline);
            Polyline polylineRoute = mMap.addPolyline(route);
            polylineRoute.setColor(Color.BLUE);
            polylineRoute.setWidth(WIDTH_POLYLINE);
        }
    }

    private void checkForDiscrepancy() {
        if (polyline != null) {
            Log.d("polyline size", String.valueOf(polyline.size()));
            for (int i = 0; i < polyline.size(); i++) {
                double checkMinLat = polyline.get(i).latitude - MAX_DISCREPANCY;
                double checkMaxLat = polyline.get(i).latitude + MAX_DISCREPANCY;
                double checkMinLng = polyline.get(i).longitude - MAX_DISCREPANCY;
                double checkMaxLng = polyline.get(i).longitude + MAX_DISCREPANCY;
                Log.d("check currLoc", currentLocation.toString());
                Log.d("check minLat", String.valueOf(checkMinLat));
                Log.d("check maxLat", String.valueOf(checkMaxLat));
                Log.d("check minLng", String.valueOf(checkMinLng));
                Log.d("check maxLng", String.valueOf(checkMaxLng));
                if (checkMinLat <= currentLocation.latitude && currentLocation.latitude <= checkMaxLat) {
                    Log.d("check lat stimmt", "stimmt");
                    if (checkMinLng <= currentLocation.longitude && currentLocation.longitude <= checkMaxLng) {
                        Log.d("check lng stimmt", "stimmt");
                        count = 0;
                        Log.d("count: ", String.valueOf(count));
                        Toast.makeText(this, "eingehalten", Toast.LENGTH_SHORT).show();
                        if (arrivedAtDestination()) {
                            //Toast.makeText(this, "Ziel erreicht", Toast.LENGTH_LONG).show();
                            //TODO Benachrichtigung
                            createArrivedDialog();
                        }
                        break;
                    }
                } else {
                    Log.d("currentLocation check: ", currentLocation.toString());
                    count++;
                    Log.d("count: ", String.valueOf(count));
                    if (count >= MAX_TIME_DISCREPANCY) { //Beispielwert, muss getestet werden; neue Routenberechnung starten?
                        //TODO Benachrichtigung versenden etc.
                        Log.d("Routenabweichung: ", String.valueOf(count));
                        Toast.makeText(this, "Routenabweichung", Toast.LENGTH_SHORT).show();
                        createNewRouteDialog();
                    }
                }
            }
        }
    }

    private boolean arrivedAtDestination(){
        boolean result = false;
        //evtl. anderer Grenzwert für Ziel?
        double checkMinLat = currentLocation.latitude - MAX_DISCREPANCY;
        double checkMaxLat = currentLocation.latitude + MAX_DISCREPANCY;
        double checkMinLng = currentLocation.longitude - MAX_DISCREPANCY;
        double checkMaxLng = currentLocation.longitude + MAX_DISCREPANCY;

        if(checkMinLat <= latDestination && latDestination <= checkMaxLat){
            if(checkMinLng <= lngDestination && lngDestination <= checkMaxLng){
                result = true;
            }
        }
        return result;
    }


    private void convertAddress() {
        if (destination != null && !destination.isEmpty()) {
            try {
                Geocoder geocoder = new Geocoder(this);
                List<android.location.Address> addressList = geocoder.getFromLocationName(destination, 1);
                if (addressList != null && addressList.size() > 0) {
                    latDestination = addressList.get(0).getLatitude();
                    lngDestination = addressList.get(0).getLongitude();
                    destinationLatLng = new LatLng(latDestination, lngDestination);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createArrivedDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(NavigationActivity.this);
        dialog.setTitle(R.string.title_arrived_dialog);
        dialog.setMessage(R.string.text_arrived_dialog);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendArrivedMessage();
                finish();
            }
        });
        dialog.setNegativeButton(R.string.dialog_btn_not_yet, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void sendArrivedMessage(){
        sms.sendMessage(companion, "Bin gut angekommen.");
    }

    private void createNewRouteDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(NavigationActivity.this);
        dialog.setTitle(R.string.title_route_dialog);
        dialog.setMessage(R.string.text_route_dialog);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.dialog_btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startDownload();
            }
        });
        dialog.setNegativeButton(R.string.dialog_btn_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                //TODO Benachrichtigung
                finish();
            }
        });
        dialog.show();
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
        Log.d("onStart", "a");
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
        /*if (mLocation == null) {
            startLocationUpdates();
        }*/

        if (mLocation != null) {
            Log.d("mLocation: ", mLocation.toString());
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
            startLat = currentLocation.latitude;
            startLng = currentLocation.longitude;
            Log.d("currentLocation nav ", currentLocation.toString());
            startDownload();
        }
        Log.d("onConnected", "a");

        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
            mMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("aktuelle Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LOCATION));
        }
        //if(latDestination != 0 && lngDestination != 0){
          mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Zielort"));

        //}
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(NavigationActivity.this, "GPS nicht aktiviert", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        Log.d("startLocationUpdates", "a");

        //die beiden neu: nötig?
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
        Log.d("onConnectionSuspended", "a");
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mLocation != null) {
            checkForDiscrepancy();
            //anstatt in onConnected oder zusätzlich?
            //startLocationUpdates();
            Log.d("onLocationChanged", "a");

            Log.d("mLocation: ", mLocation.toString());
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
            startLat = currentLocation.latitude;
            startLng = currentLocation.longitude;
            Log.d("currentLocation nav ", currentLocation.toString());
            //update UI
            if (mMarker != null) {
                mMarker.setPosition(currentLocation);
            }
            //mMap.addMarker(new MarkerOptions().position(currentLocation).title("aktuelle Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LOCATION));
        }
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
        // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() /*&& !mLocationRequest*/) {
            startLocationUpdates();
        }
        Log.d("onResume", "a");
    }

    private String createAddress() {
        Log.d("ADDRESS: ", ADDRESS + "origin=" + startLat + "," + startLng + "&destination=" + destination + "&mode=" + travelmode + "&key=" + GOOGLE_DIRECTIONS_KEY);
        return (ADDRESS + "origin=" + startLat + "," + startLng + "&destination=" + destination + "&mode=" + travelmode + "&key=" + GOOGLE_DIRECTIONS_KEY);
    }

}

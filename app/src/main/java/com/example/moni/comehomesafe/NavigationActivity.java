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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.formats.NativeAd;
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

import java.io.IOException;
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
    private static final int MAX_TIME_DISCREPANCY = 300;
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
    private LatLng markerPosition;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;

    SendSMS sms = new SendSMS();
    Geocoder geocoder = new Geocoder(this);

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

        initButtons();

        getIntentExtras();

        createStartDialog();

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

    private void initButtons() {
        ImageButton btnSendMessage = (ImageButton) findViewById(R.id.button_send_message);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Methodenaufruf
            }
        });

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
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onDownloadFinished(List<LatLng> polyline) {
        this.polyline = polyline;
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
            for (int i = 0; i < polyline.size(); i++) {
                double checkMinLat = polyline.get(i).latitude - MAX_DISCREPANCY;
                double checkMaxLat = polyline.get(i).latitude + MAX_DISCREPANCY;
                double checkMinLng = polyline.get(i).longitude - MAX_DISCREPANCY;
                double checkMaxLng = polyline.get(i).longitude + MAX_DISCREPANCY;
                boolean checkLat = (checkMinLat <= currentLocation.latitude && currentLocation.latitude <= checkMaxLat);
                boolean checkLng = (checkMinLng <= currentLocation.longitude && currentLocation.longitude <= checkMaxLng);
                if (checkLat && checkLng) {
                    count = 0;
                    //Toast
                    Toast.makeText(this, "eingehalten", Toast.LENGTH_SHORT).show();
                    //markerPosition = new LatLng(polyline.get(i).latitude, polyline.get(i).longitude);
                    if (arrivedAtDestination()) {
                        //TODO Benachrichtigung
                        createArrivedDialog();
                    }
                    break;
                } else {
                    count++;
                    if (count == MAX_TIME_DISCREPANCY) {
                        //TODO Benachrichtigung versenden etc.
                        Toast.makeText(this, "Routenabweichung", Toast.LENGTH_SHORT).show();
                        createNewRouteDialog();
                        count = 0;
                    }
                }
            }
        }
    }

    private boolean arrivedAtDestination() {
        boolean result = false;
        double checkMinLat = currentLocation.latitude - MAX_DISCREPANCY;
        double checkMaxLat = currentLocation.latitude + MAX_DISCREPANCY;
        double checkMinLng = currentLocation.longitude - MAX_DISCREPANCY;
        double checkMaxLng = currentLocation.longitude + MAX_DISCREPANCY;

        if (checkMinLat <= latDestination && latDestination <= checkMaxLat) {
            if (checkMinLng <= lngDestination && lngDestination <= checkMaxLng) {
                result = true;
            }
        }
        return result;
    }


    private void convertAddress() {
        if (destination != null && !destination.isEmpty()) {
            try {
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

    private void createArrivedDialog() {
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

    private void sendArrivedMessage() {
        sms.sendMessage(companion, "Bin gut angekommen.");
    }

    private void createNewRouteDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(NavigationActivity.this);
        dialog.setTitle(R.string.title_route_dialog);
        dialog.setMessage(R.string.text_route_dialog);
        dialog.setCancelable(false);
        dialog.setPositiveButton(R.string.dialog_btn_new_route, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        dialog.setNegativeButton(R.string.dialog_btn_contact_companion, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sms.sendMessage(companion, ("Irgendetwas stimmt nicht. Mein Standort ist: " + getAddress() ));
            }
        });
        dialog.show();
    }

    private String getAddress() {
        String result = "";
        try {
            List<android.location.Address> resultList = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
            Log.d("Address List: ", resultList.toString());
            if (resultList.size() > 0) {
                String street = resultList.get(0).getThoroughfare();
                String houseNumber = resultList.get(0).getSubThoroughfare();
                String postalCode= resultList.get(0).getPostalCode();
                String city = resultList.get(0).getLocality();
                if(street != null){
                    result = street + " ";
                } if(houseNumber != null){
                    result += houseNumber + " ";
                } if(postalCode != null){
                    result += postalCode + " ";
                } if(city != null){
                    result += city;
                }
                Log.d("result: ", result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } return result;
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
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mGoogleApiClient.connect();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(NavigationActivity.this, "GPS-Permission erforderlich", Toast.LENGTH_SHORT).show();
            return;
        }
        startLocationUpdates();
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
            startLat = currentLocation.latitude;
            startLng = currentLocation.longitude;
            startDownload();
            mMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title("aktuelle Position"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, CAMERA_ZOOM_LOCATION));
        }
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title("Zielort"));
    }

    private void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(NavigationActivity.this, "GPS-Permission erforderlich", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;
        if (mLocation != null) {
            checkForDiscrepancy();

            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
            startLat = currentLocation.latitude;
            startLng = currentLocation.longitude;

            //update UI
            if (mMarker != null) { //setPosition(markerPosition) //bei Testing erwähnen
                mMarker.setPosition(currentLocation);
            }
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
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    private String createAddress() {
        return (ADDRESS + "origin=" + startLat + "," + startLng + "&destination=" + destination + "&mode=" + travelmode + "&key=" + GOOGLE_DIRECTIONS_KEY);
    }

}

package com.example.moni.comehomesafe;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.lang.reflect.Array;


public class MapsActivityAlt extends FragmentActivity
        implements OnMapReadyCallback, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int REQUESTCODE_DESTINATION = 1;
    private static final int REQUESTCODE_COMPANION = 2;
    private static final int LAT_GERMANY = 51;
    private static final int LNG_GERMANY = 10;
    private static final int CAMERA_ZOOM_GERMANY = 6;
    private static final int WHICH_MODE_DIALOG = 1;

    private GoogleMap mMap;
    private Location location;
    private LatLng destination;
    private String companion;
    private LatLng currentLocation;
    private Context context;
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

        initButtons();
    }

    private void initButtons() {
        ImageButton btnMode = (ImageButton) findViewById(R.id.button_travelmode);
        btnMode.setOnClickListener(this);
        ImageButton btnDestination = (ImageButton) findViewById(R.id.button_destination);
        btnDestination.setOnClickListener(this);
        ImageButton btnAddCompanion = (ImageButton) findViewById(R.id.button_add_companion);
        btnAddCompanion.setOnClickListener(this);
        Button btnStartNavigation = (Button) findViewById(R.id.button_start_navigation);
        if (companion != null && currentLocation != null && destination != null && travelmode != null) {
            btnStartNavigation.setEnabled(true);
        } else {
            btnStartNavigation.setEnabled(false);
        }
        btnStartNavigation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_travelmode:
                buildDialog();

            case R.id.button_destination:
                Intent intentDestination = new Intent(this, SelectDestinationActivity.class);
                startActivityForResult(intentDestination, REQUESTCODE_DESTINATION);

            case R.id.button_add_companion:
                Intent intentAddComp = new Intent(this, AddCompanionActivity.class);
                startActivityForResult(intentAddComp, REQUESTCODE_COMPANION);

            case R.id.button_start_navigation:
                buildIntent();
        }
    }

    private void buildIntent() {
        //null
        Bundle args = new Bundle();
        args.putParcelable("START", currentLocation);
        args.putParcelable("DESTINATION", destination);
        args.putString("COMPANION", companion);
        args.putString("MODE", travelmode);
        Intent intentStartNav = new Intent(this, NavigationActivity.class);
        intentStartNav.putExtra("BUNDLE", args);
        startActivity(intentStartNav);
    }

    private void buildDialog() {
        String[] modeArray = new String[]{"zu Fuß", "mit dem Auto"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivityAlt.this);
                builder.setTitle("Fortbewegungsmittel")
                .setItems(modeArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == WHICH_MODE_DIALOG){
                            travelmode = "walking";
                        } else {
                            travelmode = "driving";
                        }
                    }
                })
                .create()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE_DESTINATION) {
            if (resultCode == RESULT_OK) {
                if(data != null){
                    destination = data.getExtras().getParcelable("RESULT");
                }
            }
        } else if(requestCode == REQUESTCODE_COMPANION){
            if(resultCode == RESULT_OK){
                if(data != null){
                    companion = data.getExtras().getString("RESULT");
                    //Datentyp für companion?
                }
            }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng germany = new LatLng(LAT_GERMANY, LNG_GERMANY);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(germany, CAMERA_ZOOM_GERMANY));
        setUpMyLocation();
    }

    private void setUpMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Please turn on your GPS", Toast.LENGTH_LONG).show();
            return;
        }
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
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
    public void onConnected(@Nullable Bundle bundle) {
        mGoogleApiClient.connect();
        if (mLocation != null) {
            double latitude = mLocation.getLatitude();
            double longitude = mLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

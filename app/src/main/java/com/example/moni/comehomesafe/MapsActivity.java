package com.example.moni.comehomesafe;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private static final int LATITUDE_GERMANY = 51;
    private static final int LONGITUDE_GERMANY = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initButtons();
    }

    private void initButtons() {
        ImageButton btnCurrentLocation = (ImageButton) findViewById(R.id.button_gps_location);
        btnCurrentLocation.setOnClickListener(this);
        ImageButton btnDestination = (ImageButton) findViewById(R.id.button_destination);
        btnDestination.setOnClickListener(this);
        ImageButton btnAddCompanion = (ImageButton) findViewById(R.id.button_add_companion);
        btnAddCompanion.setOnClickListener(this);
        Button btnStartNavigation = (Button) findViewById(R.id.button_start_navigation);
        btnStartNavigation.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_gps_location:
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true); //zum orten

            case R.id.button_destination:
                Intent intentDestination = new Intent(this, SelectDestinationActivity.class);
                startActivity(intentDestination);

            case R.id.button_add_companion:
                Intent intentAddComp = new Intent(this, AddCompanionActivity.class);
                startActivity(intentAddComp);

            case R.id.button_start_navigation:
                Intent intentStartNav = new Intent(this, NavigationActivity.class);
                startActivity(intentStartNav);
        }

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker and move the camera
        LatLng germany = new LatLng(LATITUDE_GERMANY, LONGITUDE_GERMANY);
        //mMap.addMarker(new MarkerOptions().position(germany).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(germany));
    }
}

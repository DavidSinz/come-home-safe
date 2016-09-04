package com.example.moni.comehomesafe;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NavigationActivity extends FragmentActivity
        implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double startLat;
    private double startLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        if(b!=null) {
            startLat = b.getDouble("START_LAT");
            startLng = b.getDouble("START_LNG");
        }

    }

    //TODO HTTP-Kommunikation für Routenberechnung aus Google Maps



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker and move the camera
        LatLng startPos = new LatLng(startLat, startLng);
        mMap.addMarker(new MarkerOptions().position(startPos).title("your position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPos));
    }
}

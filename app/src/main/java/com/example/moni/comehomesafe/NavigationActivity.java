package com.example.moni.comehomesafe;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.drive.internal.StringListResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.net.URLEncoder;

public class NavigationActivity extends FragmentActivity
        implements OnMapReadyCallback, DownloadListener {

    private static final String ADDRESS = "https://maps.googleapis.com/maps/api/directions/json?";

    private GoogleMap mMap;
    private double startLat;
    private double startLng;
    private LatLng start;
    private LatLng destination;

    private static final String GOOGLE_API_KEY = "AIzaSyCuvRBq6Ckbrx0qRednmupRMF3MXX0mgso";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        createAddress();

        //hinter this, oder als try new.. und catch exception
        new DirectionDownloadTask(this, start, destination).execute(createAddress());

        Bundle bundle = getIntent().getParcelableExtra("BUNDLE");
        if(bundle!=null) {
            start = bundle.getParcelable("START");
            //destination auslesen
        }

    }

    //TODO HTTP-Kommunikation für Routenberechnung aus Google Maps
    // https://maps.googleapis.com/maps/api/directions/json?origin=
    // origin, destination & key --> json-datei zum daten auslesen
    //directions api bereits aktiviert
    //async-task --> url von oben

    private String createAddress(){
        //String urlOrigin = URLEncoder.encode(start, "utf-8");
        //überprüfen, ob start & destination als LatLng richtig ausgegeben werden

        return ADDRESS + "origin=" + start + "&destination=" + destination + "&key=" + GOOGLE_API_KEY;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker and move the camera
        LatLng startPos = new LatLng(startLat, startLng);
        mMap.addMarker(new MarkerOptions().position(startPos).title("your position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(startPos));
    }

    @Override
    public void onDownloadFinished() {

    }
}

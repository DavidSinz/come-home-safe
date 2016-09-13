package com.example.moni.comehomesafe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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


public class MapsActivityAlt extends FragmentActivity
        implements OnMapReadyCallback, View.OnClickListener {

    private static final int REQUESTCODE_DESTINATION = 1;
    private static final int REQUESTCODE_COMPANION = 2;
    private GoogleMap mMap;
    private static final int LAT_GERMANY = 51;
    private static final int LNG_GERMANY = 10;
    private Location location;
    private LatLng destination;
    private String companion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initButtons();

        //alt: locationmanager von android
        /** locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, this, 0, 0);
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
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
         //oder ganze activity als "implements LocationListener" und dann hier nur "this"
         */
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

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
        double startLat = 0;
        double startLng = 0;
        switch (v.getId()) {
            case R.id.button_gps_location:
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                //zum orten
                //startLat & startLng initalisieren


            case R.id.button_destination:
                Intent intentDestination = new Intent(this, SelectDestinationActivity.class);
                startActivityForResult(intentDestination, REQUESTCODE_DESTINATION);

            case R.id.button_add_companion:
                Intent intentAddComp = new Intent(this, AddCompanionActivity.class);
                startActivityForResult(intentAddComp, REQUESTCODE_COMPANION);

            case R.id.button_start_navigation:
                startLat = location.getLatitude();
                startLng = location.getLongitude();
                LatLng start = new LatLng(startLat, startLng);
                Bundle args = new Bundle();
                args.putParcelable("START", start);
                args.putParcelable("DESTINATION", destination);
                args.putString("COMPANION", companion);
                Intent intentStartNav = new Intent(this, NavigationActivity.class);
                intentStartNav.putExtra("BUNDLE", args);
                startActivity(intentStartNav);
        }

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
                    //TODO: Lokale variable anlegen, wie oben hier auslesen und dem Intent für NavAct übergeben und in NacAct auslesen
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(germany, 6));
    }

}

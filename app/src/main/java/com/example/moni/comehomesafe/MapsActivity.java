package com.example.moni.comehomesafe;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int LAT_GERMANY = 51;
    private static final int LNG_GERMANY = 10;
    private static final int CAMERA_ZOOM_GERMANY = 6;
    private static final int WHICH_MODE_DIALOG = 0;
    private static final String TITLE_PLACES_DIALOG = "Zielort auswählen";
    private static final String TITLE_CONTACT_DIALOG = "Kontakt auswählen";
    private static final String TITLE_TRAVEL_DIALOG = "Fortbewegungsmittel";

    private GoogleMap mMap;
    private String destination;
    private String companion;
    private String companionName;
    private LatLng currentLocation;
    private String travelmode;
    private Button btnStartNavigation;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;
    private ContactListDatabase db_contacts;
    private PlacesListDatabase db_places;

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
        btnStartNavigation = (Button) findViewById(R.id.button_start_navigation);
        btnStartNavigation.setOnClickListener(this);
        initDB();
    }

    private void enableBtnStart() {
        if (companion != null && destination != null && travelmode != null) {
            btnStartNavigation.setEnabled(true);
        } else {
            btnStartNavigation.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_travelmode:
                buildTravelDialog();
                break;

            case R.id.button_destination:
                buildDestinationDialog();
                break;

            case R.id.button_add_companion:
                buildContactDialog();
                break;

            case R.id.button_start_navigation:
                buildNavigationIntent();
                break;

            default:
                break;
        }
    }

    private void buildDestinationDialog() {
        final ArrayList<PlacesItem> placesItems = db_places.getAllPlacesItems();
        final String[] places = new String[placesItems.size()];
        for (int i = 0; i < placesItems.size(); i++) {
            places[i] = placesItems.get(i).getAdress();
            places[i] = reformatAddress(places[i]);
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setTitle(TITLE_PLACES_DIALOG);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, places);
        alertDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < placesItems.size(); i++) {
                    if (which == i) {
                        String address = placesItems.get(which).getAdress();
                        destination = contactAddress(address);
                    }
                }
            }
        });
        alertDialog.show();
    }

    private String reformatAddress(String address) {
        String result = "";
        int count = 0;
        for (int i = 0; i < address.length(); i++) {
            if (count == 0 && address.charAt(i) == '/') {
                result = address.substring(0, i) + ": " + address.substring(i + 1, address.length());
                count++;
            } else if (count != 0 && address.charAt(i) == '/') {
                result = result.substring(0, i + 1) + " " + address.substring(i + 1, address.length());
            }
        }
        return result;
    }

    private String contactAddress(String address) {
        String result = "";
        int count = 0;
        for (int i = 0; i < address.length(); i++) {
            if (address.charAt(i) == '/' && count == 0) {
                result = address.substring(i + 1, address.length());
                count = i;
            }
            else if (address.charAt(i) == '/' && count != 0) {
                result = result.substring(0, i - count - 1) + "," + result.substring(i - count, result.length());
            } else if (address.charAt(i) == ' ' && address.length() == i + 1) {
                result = result.substring(0, result.length() - 1);
            }
        }
        result = deleteSpaces(result);
        Log.d("result: ", result);
        return result;
    }

    private String deleteSpaces(String input) {
        String result = "";
        for(int i = 0; i < input.length(); i++){
            if(input.charAt(i) != ' '){
                result += input.charAt(i);
            }
        } return result;
    }

    private void buildContactDialog() {
        final ArrayList<ContactItem> contactItems = db_contacts.getAllContactItems();
        String[] names = new String[contactItems.size()];
        for (int i = 0; i < contactItems.size(); i++) {
            names[i] = contactItems.get(i).getName();
        }
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
        alertDialog.setTitle(TITLE_CONTACT_DIALOG);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        alertDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < contactItems.size(); i++) {
                    if (which == i) {
                        String number = contactItems.get(which).getNumber();
                        companion = number;
                        companionName = contactItems.get(which).getName();
                        Log.d("companion: ", number);
                    }
                }
            }
        });
        alertDialog.show();
    }

    private void buildNavigationIntent() {
        Bundle args = new Bundle();
        args.putString("DESTINATION", destination);
        args.putString("COMPANION", companion);
        args.putString("COMPANIONNAME", companionName);
        args.putString("MODE", travelmode);
        Intent intentStartNav = new Intent(this, NavigationActivity.class);
        intentStartNav.putExtra("BUNDLE", args);
        startActivity(intentStartNav);
    }

    private void buildTravelDialog() {
        String[] modeArray = new String[]{"zu Fuß", "mit dem Auto"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle(TITLE_TRAVEL_DIALOG)
                .setItems(modeArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == WHICH_MODE_DIALOG) {
                            travelmode = "walking";
                        } else {
                            travelmode = "driving";
                        }
                    }
                })
                .create()
                .show();
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
            Toast.makeText(this, "GPS-Permission erforderlich", Toast.LENGTH_LONG).show();
            return;
        }
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            if (mLocation != null) {
                currentLocation = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
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
    public void onConnected(@Nullable Bundle bundle) {
        mGoogleApiClient.connect();
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

    private void initDB() {
        db_contacts = new ContactListDatabase(this);
        db_places = new PlacesListDatabase(this);
        db_contacts.open();
        db_places.open();
    }

    @Override
    protected void onDestroy() {
        db_contacts.close();
        db_places.close();
        super.onDestroy();
    }
}

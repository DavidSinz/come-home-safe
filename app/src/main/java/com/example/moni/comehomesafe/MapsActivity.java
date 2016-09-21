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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
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

import java.util.ArrayList;


public class MapsActivity extends FragmentActivity
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
                buildTravelDialog();
                break;

            case R.id.button_destination:
                buildDestinationDialog();
                break;

            case R.id.button_add_companion:
                buildContactDialog();
                break;

            case R.id.button_start_navigation:
                buildIntent();
                break;

            default:
                break;
        }
    }

    private void buildDestinationDialog() {

    }

    private void buildContactDialog() {
        ContactsActivity contactsActivity = new ContactsActivity();
        final ArrayList<ContactItem> contactItems = contactsActivity.getContactItems();
        String[] names = new String[contactItems.size()];
        for (int i = 0; i < contactItems.size(); i++) {
            names[i] = contactItems.get(i).getName();
        }

        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(MapsActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.contact_list_dialog, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle("Kontakt auswählen");
        ListView listView = (ListView) convertView.findViewById(R.id.listView1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);
        alertDialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = contactItems.get(position).getName();
                String number = contactItems.get(position).getNumber();
            }
        });
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

    private void buildTravelDialog() {
        String[] modeArray = new String[]{"zu Fuß", "mit dem Auto"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
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
            Toast.makeText(context, "GPS nicht aktiviert", Toast.LENGTH_LONG).show();
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

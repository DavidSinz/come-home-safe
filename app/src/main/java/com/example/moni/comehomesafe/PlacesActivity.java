package com.example.moni.comehomesafe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class PlacesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static ArrayList<PlacesItem> placesItems = new ArrayList<>();
    private PlacesListAdapter places_adapter;
    private PlacesListDatabase db_places;

    public static final String KEY_RESULT = "place";
    private static final int RESULT_SELECT_PLACE = 000003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        initLayout();
        initDB();
        initUI();
        initPlacesList();
    }

    @Override
    protected void onDestroy() {
        db_places.close();
        super.onDestroy();
    }

    private void initLayout() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.places);

        DrawerLayout drawer2 = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle2 = new ActionBarDrawerToggle(this, drawer2, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer2 != null;
        drawer2.addDrawerListener(toggle2);
        toggle2.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_places);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initDB() {
        db_places = new PlacesListDatabase(this);
        db_places.open();
    }

    private void initPlacesList() {
        updateList();
    }

    private void initUI() {
        initAddPlacesButton();
        initListView();
        initListAdapter();
    }

    private void initAddPlacesButton() {
        FloatingActionButton addPlaceButton = (FloatingActionButton) findViewById(R.id.create_place_button);
        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, CreateNewPlaceActivity.class);
                PlacesActivity.this.startActivityForResult(intent, RESULT_SELECT_PLACE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = null;
        if (requestCode == RESULT_SELECT_PLACE && resultCode == RESULT_OK && data != null) {
            result = data.getStringExtra(KEY_RESULT);
            addNewPlace(result);
        }
    }

    private void initListView() {
        ListView list = (ListView) findViewById(R.id.places_list);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removePlaceAtPos(position);
                return false;
            }
        });
    }

    private void initListAdapter() {
        ListView list = (ListView) findViewById(R.id.places_list);
        places_adapter = new PlacesListAdapter(this, placesItems);
        list.setAdapter(places_adapter);
    }

    private void updateList() {
        placesItems.clear();
        placesItems.addAll(db_places.getAllPlacesItems());
        places_adapter.notifyDataSetChanged();
    }

    public void addNewPlace(String adress) {
        PlacesItem newPlace = new PlacesItem(adress);
        db_places.insertPlacesItem(newPlace);
        updateList();
    }

    private void removePlaceAtPos(final int position) {
        if (placesItems.get(position) != null) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlacesActivity.this);
            alertDialog.setTitle(R.string.delete_place);
            alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db_places.removePlacesItem(placesItems.get(position));
                    updateList();
                }
            });
            alertDialog.show();
        }
    }

    private void sortList() {
        places_adapter.notifyDataSetChanged();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_map) {
            Intent map = new Intent(this, MapsActivity.class);
            startActivity(map);
            finish();
        } else if (id == R.id.nav_places) {
            DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout2);
            drawer.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_contacts) {
            Intent contacts = new Intent(this, ContactsActivity.class);
            startActivity(contacts);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
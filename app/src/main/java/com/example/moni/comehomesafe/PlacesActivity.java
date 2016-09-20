package com.example.moni.comehomesafe;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class PlacesActivity extends AppCompatActivity {

    private ArrayList<PlacesItem> placesItems = new ArrayList<>();
    private PlacesListAdapter places_adapter;
    private PlacesListDatabase db_places;

    public static final String KEY_RESULT = "place";
    private static final int RESULT_SELECT_PLACE = 000003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        initDB();
        initUI();
        initPlacesList();
    }

    @Override
    protected void onDestroy() {
        db_places.close();
        super.onDestroy();
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

            String place = "";
            String street = "";
            String number = "";
            String zipCode = "";
            String city = "";

            int counter = 0;

            for (int i = 0; i < result.length(); i++) {
                if (result.charAt(i) == '/') {
                    counter++;
                } else if (counter == 0) {
                    place += result.charAt(i);
                } else if (counter == 1) {
                    street += result.charAt(i);
                } else if (counter == 2) {
                    number += result.charAt(i);
                } else if (counter == 3) {
                    zipCode += result.charAt(i);
                } else {
                    city += result.charAt(i);
                }
            }

            addNewPlace(place, street, number, zipCode, city);
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

    public void addNewPlace(String place, String street, String number, String zipCode, String city) {
        PlacesItem newPlace = new PlacesItem(place, street, number, zipCode, city);
        db_places.insertPlaceItem(newPlace);
        updateList();
    }

    private void removePlaceAtPos(int position) {
        if (placesItems.get(position) != null) {
            db_places.removePlacesItem(placesItems.get(position));
            updateList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                sortList();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sortList() {
        places_adapter.notifyDataSetChanged();
    }


}
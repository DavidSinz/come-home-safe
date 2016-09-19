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
    private static final int RESULT_SELECT_PLACE = 000002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initDB();
        initUI();
        initContactList();
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

    private void initContactList() {
        updateList();
    }

    private void initUI() {
        initAddContactButton();
        initListView();
        initListAdapter();
    }

    private void initAddContactButton() {
        FloatingActionButton addPlaceButton = (FloatingActionButton) findViewById(R.id.add_contact);
        addPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PlacesActivity.this, SelectPlaces.class);
                PlacesActivity.this.startActivityForResult(intent, RESULT_SELECT_PLACE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result = null;
        if (requestCode == RESULT_SELECT_PLACE && resultCode == RESULT_OK && data != null) {
            result = data.getStringExtra(KEY_RESULT);
        }

        String name = "";
        String number = "";
        boolean b = false;
        for (int i = 0; i < result.length(); i++) {
            if (b) {
                number += result.charAt(i);
            } else if (result.charAt(i) == '/') {
                b = true;
            } else {
                name += result.charAt(i);
            }
        }

//        addNewPlace(name, number);
    }

    private void initListView() {
        ListView list = (ListView) findViewById(R.id.contact_list);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removePlaceAtPos(position);
                return false;
            }
        });
    }

    private void initListAdapter() {
        ListView list = (ListView) findViewById(R.id.contact_list);
        places_adapter = new PlacesListAdapter(this, placesItems);
        list.setAdapter(places_adapter);
    }

    private void updateList() {
        placesItems.clear();
        placesItems.addAll(db_places.getAllPlacesItems());
        places_adapter.notifyDataSetChanged();
    }

//    public void addNewPlace(String name, String place) {
//        PlacesItem newPlace = new PlacesItem(name, place);
//        db_places.insertPlaceItem(newPlace);
//        updateList();
//    }

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
package com.example.moni.comehomesafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PlacesListAdapter extends ArrayAdapter<PlacesItem> {

    private ArrayList<PlacesItem> listItem;
    private Context context;

    public PlacesListAdapter(Context context, ArrayList<PlacesItem> listItem) {
        super(context, R.layout.listitem_places, listItem);

        this.context = context;
        this.listItem = listItem;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.listitem_places, null);

        }

        PlacesItem placesItem = listItem.get(pos);
        String result = placesItem.getAdress();

        if (placesItem != null) {

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

            TextView placeTextV = (TextView) v.findViewById(R.id.places_place);
            TextView streetAndNumberTextV = (TextView) v.findViewById(R.id.places_street_number);
            TextView zipCodeAndCityTextV = (TextView) v.findViewById(R.id.places_zipcode_city);

            placeTextV.setText(place);
            streetAndNumberTextV.setText(street + " " + number);
            zipCodeAndCityTextV.setText(zipCode + " " + city);
        }

        return v;
    }
}
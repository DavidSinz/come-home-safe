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

        if (placesItem != null) {
            TextView place = (TextView) v.findViewById(R.id.places_place);
            TextView street = (TextView) v.findViewById(R.id.places_street);
            TextView number = (TextView) v.findViewById(R.id.places_number);
            TextView zipCode = (TextView) v.findViewById(R.id.places_zip_code);
            TextView city = (TextView) v.findViewById(R.id.places_city);

            place.setText(placesItem.getPlace());
            street.setText(placesItem.getStreet());
            number.setText(placesItem.getNumber());
            zipCode.setText(placesItem.getZipCode());
            city.setText(placesItem.getCity());
        }

        return v;
    }
}
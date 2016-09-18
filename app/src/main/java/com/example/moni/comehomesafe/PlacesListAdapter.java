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

        PlacesItem place = listItem.get(pos);

        if (place != null) {
            TextView placeName = (TextView) v.findViewById(R.id.place_name);
            TextView placeDestination = (TextView) v.findViewById(R.id.place_destination);

            placeName.setText(place.getName());
            placeDestination.setText(place.getPlace());
        }

        return v;
    }
}

package com.example.moni.comehomesafe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ContactListAdapter extends ArrayAdapter<ContactItem> {

    private ArrayList<ContactItem> listItem;
    private Context context;

    public ContactListAdapter(Context context, ArrayList<ContactItem> listItem) {
        super(context, R.layout.listitem_contact, listItem);

        this.context = context;
        this.listItem = listItem;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.listitem_contact, null);

        }

        ContactItem contact = listItem.get(pos);

        if (contact != null) {
            TextView contactName = (TextView) v.findViewById(R.id.contact_name);
            TextView contactNumber = (TextView) v.findViewById(R.id.contact_number);

            contactName.setText(contact.getName());
            contactNumber.setText(contact.getNumber());
        }

        return v;
    }
}
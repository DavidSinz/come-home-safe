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

public class ContactsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static ArrayList<ContactItem> contactItems = new ArrayList<>();
    private ContactListAdapter contacts_adapter;
    private ContactListDatabase db;

    public static final String KEY_RESULT = "contact";
    private static final int RESULT_SELECT_CONTACT = 000002;

    public ContactsActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.contacts);

        DrawerLayout drawer2 = (DrawerLayout) findViewById(R.id.drawer_layout3);
        ActionBarDrawerToggle toggle2 = new ActionBarDrawerToggle(this, drawer2, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer2 != null;
        drawer2.addDrawerListener(toggle2);
        toggle2.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_contacts);
        navigationView.setNavigationItemSelectedListener(this);


        initDB();
        initUI();
        initContactList();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }

    private void initDB() {
        db = new ContactListDatabase(this);
        db.open();
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
        FloatingActionButton addContactButton = (FloatingActionButton) findViewById(R.id.add_contact);
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsActivity.this, SelectContact.class);
                ContactsActivity.this.startActivityForResult(intent, RESULT_SELECT_CONTACT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_SELECT_CONTACT && resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra(KEY_RESULT);
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
            addNewContact(name, number);
        }
    }

    private void initListView() {
        ListView list = (ListView) findViewById(R.id.contact_list);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                removeContactAtPos(position);
                return false;
            }
        });
    }

    private void initListAdapter() {
        ListView list = (ListView) findViewById(R.id.contact_list);
        contacts_adapter = new ContactListAdapter(this, contactItems);
        list.setAdapter(contacts_adapter);
    }

    private void updateList() {
        contactItems.clear();
        contactItems.addAll(db.getAllContactItems());
        contacts_adapter.notifyDataSetChanged();
    }

    public void addNewContact(String name, String number) {
        ContactItem newContact = new ContactItem(name, number);
        db.insertContactItem(newContact);
        updateList();
    }

    private void removeContactAtPos(final int position) {
        if (contactItems.get(position) != null) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ContactsActivity.this);
            alertDialog.setTitle("Ort löschen?");
            alertDialog.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    db.removeContactItem(contactItems.get(position));
                    updateList();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);
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
        contacts_adapter.notifyDataSetChanged();
    }

    public static ArrayList<ContactItem> getContactItems() {
        return contactItems;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            Intent map = new Intent(this, MapsActivity.class);
            startActivity(map);
        } else if (id == R.id.nav_places) {
            Intent places = new Intent(this, PlacesActivity.class);
            startActivity(places);
        } else if (id == R.id.nav_contacts) {
            Intent contacts = new Intent(this, ContactsActivity.class);
            startActivity(contacts);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

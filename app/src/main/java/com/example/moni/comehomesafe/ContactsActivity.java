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

public class ContactsActivity extends AppCompatActivity {

    private ArrayList<ContactItem> contactItems = new ArrayList<>();
    private ContactListAdapter contacts_adapter;
    private ContactListDatabase db;

    public static final String KEY_RESULT = "contact";
    private static final int RESULT_SELECT_CONTACT = 000002;

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

    private void removeContactAtPos(int position) {
        if (contactItems.get(position) != null) {
            db.removeContactItem(contactItems.get(position));
            updateList();
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
}

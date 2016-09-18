package com.example.moni.comehomesafe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SelectPlaces extends Activity{

    private static final int RESULT_PICK_PLACE = 85500;
    private TextView textViewName, textViewPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        textViewName = (TextView) findViewById(R.id.textName);
        textViewPlace = (TextView) findViewById(R.id.textPlace);

        initFinishButton();
    }

    public void pickPlace(View v) {
        Intent placePickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(placePickerIntent, RESULT_PICK_PLACE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_PLACE:
                    placePicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick place");
        }
    }

    private void placePicked(Intent data) {
        Cursor cursor;
        try {
            String name = null;
            String place = null;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int placeIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
            name = cursor.getString(nameIndex);
            place = cursor.getString(placeIndex);
            textViewName.setText(name);
            textViewPlace.setText(place);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFinishButton() {
        Button finishButton = (Button) findViewById(R.id.addplace_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEdit = (EditText) findViewById(R.id.textName);
                EditText placeEdit = (EditText) findViewById(R.id.textPlace);
                String name = nameEdit.getText().toString();
                String place = placeEdit.getText().toString();

                if (name.length() > 0 && place.length() > 0) {
                    Intent resultIntent = new Intent();
                    String result = name + "/" + place;
                    resultIntent.putExtra(PlacesActivity.KEY_RESULT, result);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SelectPlaces.this);
                    alertDialogBuilder.setTitle("Fehlende Angaben");
                    alertDialogBuilder.setMessage("Es fehlt der Name oder der Ort").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {}
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }
}

package com.example.moni.comehomesafe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CreateNewPlaceActivity extends Activity {

    private EditText editPlace, editStreet, editNumber, editZipCode, editCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_place);
        editPlace = (EditText) findViewById(R.id.edit_place);
        editStreet = (EditText) findViewById(R.id.edit_street);
        editNumber = (EditText) findViewById(R.id.edit_number);
        editZipCode = (EditText) findViewById(R.id.edit_zip_code);
        editCity = (EditText) findViewById(R.id.edit_city);

        initFinishButton();
    }

    private void initFinishButton() {
        Button finishButton = (Button) findViewById(R.id.addplace_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String place = editPlace.getText().toString();
                String street = editStreet.getText().toString();
                String number = editNumber.getText().toString();
                String zipCode = editZipCode.getText().toString();
                String city = editCity.getText().toString();

                if (place.length() > 0 && street.length() > 0 && number.length() > 0
                        && zipCode.length() > 0 && city.length() > 0) {
                    Intent resultIntent = new Intent();
                    String result = place + "/" + street + "/" + number + "/" + zipCode + "/" + city;
                    resultIntent.putExtra(PlacesActivity.KEY_RESULT, result);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateNewPlaceActivity.this);
                    alertDialogBuilder.setTitle("Fehlende Angaben");
                    alertDialogBuilder.setMessage("Es fehlen Angaben bei der Adresse").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }
}

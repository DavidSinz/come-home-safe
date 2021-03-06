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

public class SelectContact extends Activity {

    private static final int RESULT_PICK_CONTACT = 85500;
    private TextView textViewName, textViewNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);
        textViewName = (TextView) findViewById(R.id.textName);
        textViewNumber = (TextView) findViewById(R.id.textNumber);

        initFinishButton();
    }

    public void pickContact(View v) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor;
        try {
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            String name = cursor.getString(nameIndex);
            String number = cursor.getString(numberIndex);
            textViewName.setText(name);
            textViewNumber.setText(number);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFinishButton() {
        Button finishButton = (Button) findViewById(R.id.addcontact_button);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText nameEdit = (EditText) findViewById(R.id.textName);
                EditText numberEdit = (EditText) findViewById(R.id.textNumber);
                String name = nameEdit.getText().toString();
                String number = numberEdit.getText().toString();

                if (name.length() > 0 && number.length() > 0) {
                    Intent resultIntent = new Intent();
                    String result = name + "/" + number;
                    resultIntent.putExtra(ContactsActivity.KEY_RESULT, result);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                } else {
                    missingDeclarationDialog();
                }
            }
        });
    }

    private void missingDeclarationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.missing_declaration_1));
        alertDialogBuilder.setMessage(getString(R.string.missing_declaration_2)).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {}
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

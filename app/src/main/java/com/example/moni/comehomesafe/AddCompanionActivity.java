package com.example.moni.comehomesafe;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

public class AddCompanionActivity extends Activity {

    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sendReturnIntent();
    }

    private void sendReturnIntent() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("RESULT", result);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}


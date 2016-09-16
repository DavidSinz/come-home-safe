package com.example.moni.comehomesafe;


import android.media.MediaRouter;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class DirectionDownloadTask extends AsyncTask<String, Integer, String>{

    private DownloadListener listener;
    private LatLng start;
    private LatLng destination;


    //map oder so, wo die Ergebnisse eingetragen werden
    //in kombi mit adapter
    public DirectionDownloadTask(DownloadListener listener, LatLng start, LatLng destination) {
        this.listener = listener;
        this.start = start;
        this.destination = destination;
    }


    @Override
    protected String doInBackground(String... params) {
        String jsonString = "";

        try {
            URL url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            Log.d("tag", "HTTP geöffnet");
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.d("tag2", "HTTP_OK");
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    jsonString += line;
                }
                br.close();
                is.close();
                conn.disconnect();
            } else {
                throw new IllegalStateException("HTTP response: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("JSON DATA: ", jsonString);
        return jsonString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        processJson(result);
        listener.onDownloadFinished();
    }

    //mode: driving, by foot etc.
    //routenalternativen: alternatives = true
    //als Variablen zur URL hinzufügen
    private void processJson(String data) {
        try {
            //alt JSONArray jsonArray = new JSONArray(text);

            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                JSONObject overviewPolylineJSON = jsonRoute.getJSONObject("overview_polyline");

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




}

package com.example.moni.comehomesafe;

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
import java.util.List;


public class DirectionDownloadTask extends AsyncTask<String, Integer, String>{

    private DownloadListener listener;
    private LatLng start;
    private LatLng destination;
    private List<LatLng> polyline;

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
        listener.onDownloadFinished(polyline);
    }

    //routenalternativen: alternatives = true
    private void processJson(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonRoutes = jsonData.getJSONArray("routes");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                JSONObject overviewPolylineJSON = jsonRoute.getJSONObject("overview_polyline");
                polyline = decodePolyline(overviewPolylineJSON.getString("points"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //TODO Quelle angeben
    //decode polyline points
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


}

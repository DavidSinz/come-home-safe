package com.example.moni.comehomesafe;


import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class DirectionDownloadTask extends AsyncTask<String, Integer, String>{

    private DownloadListener listener;


    //map oder so, wo die Ergebnisse eingetragen werden
    //in kombi mit adapter
    public DirectionDownloadTask(DownloadListener listener) {
        this.listener = listener;
    }


    @Override
    protected String doInBackground(String... params) {
        String jsonString = "";

        try {
            URL url = new URL(params[0]);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
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
        return jsonString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        processJson(result);
        listener.onDownloadFinished();
    }

    private void processJson(String text) {
        try {
            JSONArray jsonArray = new JSONArray(text);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                //Rauslesen und Verarbeitung der Ergebnisse
                //int rank = jsonObject.getInt(RANK);
                //TableItem item = new TableItem(rank, team, playedGames, points, goals, goalsAgainst);
                //table.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




}

package com.example.ilona.ilonarecorder.connections;


import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class IlonaZoneConnection extends AsyncTask<URL, Integer, String> {

    public IlonaZoneConnection() {

    }

    // Asycronous task that uses a thread in the background to do the network operations,
    // which makes the application more responsive.
    @Override
    protected String doInBackground(URL... urls) {
        URL url = urls[0];
        try {
            // Connects to the server which is contained in the ServerURL variable
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            // Sets the connection type
            String ConnectionType = "Content-Type";
            String ConnectionProperty = "application/json;charset=UTF-8";
            urlConnection.setRequestProperty(ConnectionType, ConnectionProperty);
            // Connects to the server
            url.openConnection();
            OutputStream out = urlConnection.getOutputStream();
            //Receives the data sent by the server
            InputStream is = urlConnection.getInputStream();
            //Turns it into a readable string byte-by-byte
            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = is.read()) != -1) {
                sb.append((char) ch);
            }
            // Returns the received data to the main activity
            JSONArray zoneResult = new JSONArray(sb.toString());
            out.close();
            return zoneResult.toString();


        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "Oops!";


    }


}


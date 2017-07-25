package com.example.ilona.ilonarecorder.connections;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uni.miskolc.ips.ilona.measurement.model.measurement.Measurement;


abstract class AbstractConnection extends AsyncTask<String, String, String> {
    private Measurement measurement;
    private String ServerURL;

    @Override
    protected String doInBackground(String... strings) {

        try {
            // Connects to the server which is contained in the ServerURL variable
            HttpURLConnection urlConnection = connectToURL(ServerURL);
            // Connects to the server
            urlConnection.connect();
            setConnectionParameters(urlConnection);
            OutputStream out = sendJsonToServer(urlConnection);
            String zoneResult = receiveDataFromServer(urlConnection);
            // Returns the received data to the main activity
            out.close();
            return zoneResult;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "false";
    }

    private HttpURLConnection connectToURL(String ServerURL) throws IOException {
        URL url = new URL(ServerURL);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(15000);
        urlConnection.setConnectTimeout(15000);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        return urlConnection;
    }

    // Sets the connection type
    private void setConnectionParameters(HttpURLConnection urlConnection) {
        String ConnectionType = "Content-Type";
        String ConnectionProperty = "application/json;charset=UTF-8";
        urlConnection.setRequestProperty(ConnectionType, ConnectionProperty);
        Log.d("Connection watch", urlConnection.toString());
    }

    private OutputStream sendJsonToServer(HttpURLConnection urlConnection) throws IOException {
        OutputStream out;
        out = urlConnection.getOutputStream();
        // Json object writer creates a standardized JSON output from the measurement.
        ObjectWriter objectMapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = objectMapper.writeValueAsString(measurement);
        Log.d("JSON output", json);

        // Sends the measurement to the server
        out.write(json.getBytes());
        out.flush();
        return out;
    }

    private String receiveDataFromServer(HttpURLConnection urlConnection) throws IOException {
        //Receives the data sent by the server
        InputStream is = urlConnection.getInputStream();
        //Turns it into a readable string byte-by-byte
        int ch;
        StringBuilder sb = new StringBuilder();
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }
        return sb.toString();
    }
}

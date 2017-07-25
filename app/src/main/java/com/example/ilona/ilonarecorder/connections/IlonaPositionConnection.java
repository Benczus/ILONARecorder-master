package com.example.ilona.ilonarecorder.connections;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import uni.miskolc.ips.ilona.measurement.model.measurement.Measurement;


public class IlonaPositionConnection extends AbstractConnection {
    private final Measurement measurement;
    private final String ServerURL;

    public IlonaPositionConnection(Measurement measurement, String ServerURL) {
        this.measurement = measurement;
        this.ServerURL = ServerURL;
    }

    // Asyncronous task that uses a thread in the background to do the network operations,
    // which makes the application more responsive.
    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpURLConnection urlConnection = connectToURL(ServerURL);
            setConnectionParameters(urlConnection);
            OutputStream out = sendJsonToServer(urlConnection);
            String zoneResult = receiveDataFromServer(urlConnection);
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

    private void setConnectionParameters(HttpURLConnection urlConnection) {
        String ConnectionType = "Content-Type";
        String ConnectionProperty = "application/json;charset=UTF-8";
        urlConnection.setRequestProperty(ConnectionType, ConnectionProperty);
        Log.d("Connection watch", urlConnection.toString());
    }

    private OutputStream sendJsonToServer(HttpURLConnection urlConnection) throws IOException {
        OutputStream out = urlConnection.getOutputStream();
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
        InputStream is = urlConnection.getInputStream();
        //Turns it into a readable string byte-by-byte
        int ch;

        int response=urlConnection.getResponseCode();
        if (response==412|| response==422){
        return
                String.valueOf(response);
        }

        StringBuilder sb = new StringBuilder();
        while ((ch = is.read()) != -1) {
            sb.append((char) ch);
        }

        return sb.toString();
    }
}

package com.example.ilona.ilonarecorder;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.text.DecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ilona.ilonarecorder.connections.IlonaConnection;
import com.example.ilona.ilonarecorder.connections.IlonaPositionConnection;
import com.example.ilona.ilonarecorder.connections.IlonaTrackingConnection;
import com.example.ilona.ilonarecorder.connections.IlonaZoneConnection;
import com.example.ilona.ilonarecorder.filter.DynamicTimeWindowFilter;
import com.example.ilona.ilonarecorder.filter.HorusFilter;
import com.example.ilona.ilonarecorder.filter.StaticTimeWindowFilter;
import com.example.ilona.ilonarecorder.filter.WiFiRSSIFilteringStrategy;
import com.example.ilona.ilonarecorder.services.BluetoothService;
import com.example.ilona.ilonarecorder.services.RSSIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import uni.miskolc.ips.ilona.measurement.model.measurement.BluetoothTags;
import uni.miskolc.ips.ilona.measurement.model.measurement.Magnetometer;
import uni.miskolc.ips.ilona.measurement.model.measurement.Measurement;
import uni.miskolc.ips.ilona.measurement.model.measurement.MeasurementBuilder;
import uni.miskolc.ips.ilona.measurement.model.measurement.WiFiRSSI;
import uni.miskolc.ips.ilona.measurement.model.position.Coordinate;
import uni.miskolc.ips.ilona.measurement.model.position.Position;
import uni.miskolc.ips.ilona.measurement.model.position.Zone;


public class IlonaActivity extends AppCompatActivity implements SensorEventListener {
    // variable members

    static WiFiRSSIFilteringStrategy filter;
    private final ResponseReceiver mDownloadStateReceiver = new ResponseReceiver();
    //TODO
    private final ResponseReceiver mBlDownloadReceiver = new ResponseReceiver();
    double threshold = 5;
    int memsize = 5;
    LinkedList<Map<String, Double>> rssiList = new LinkedList<>();
    private boolean filtering = false;
    private SensorManager mSensorManager;
    private Sensor mMagnetometer;
    private float[] magneto;
    private Measurement measurement;
    private Map<String, Double> rssivalue;
    private ArrayList<String> bluetootharray;
    private Zone[] zones = new Zone[0];
    private Spinner spinner;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ilona);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializes the magnetometer.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //Starts the WiFi collection service.
        Intent serviceIntent = new Intent(this, RSSIService.class);
        this.startService(serviceIntent);

        // Starts the Bluetooth collection service.
        serviceIntent = new Intent(this, BluetoothService.class);
        this.startService(serviceIntent);

        // Filter for catching the implicit intent of WifiRSSI services.
        IntentFilter mStatusIntentFilter;
        mStatusIntentFilter = new IntentFilter(
                Constants.BROADCAST_ACTION);

        // Inserts the appropriate code to catch broadcasts.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStateReceiver,
                mStatusIntentFilter);
        //Filter for catching the implicit intent of bluetooth service.
        IntentFilter mBLIntentFilter = new IntentFilter(Constants.BLUETOOTH_BROADCAST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mBlDownloadReceiver, mBLIntentFilter);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        // gets the collection of zone to display it on the spinner.
        String text = null;
        IlonaZoneConnection conn = new IlonaZoneConnection();
        URL url = null;
        try {
            url = new URL("http://grabowski.iit.uni-miskolc.hu:8080/ilona/listZones");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            text = conn.execute(url).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.zones = objectMapper.readValue(text, Zone[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ilona, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getPosition(View view) throws ExecutionException, InterruptedException {
        String text;
        Position pos = new Position();
        // Starts the method which connects to the server and sends the measurement.
        if (measurement != null) {
            //TODO new url
            String ServerURL = "http://grabowski.iit.uni-miskolc.hu:8080/ilona-positioning/getLocation";
            IlonaPositionConnection conn = new IlonaPositionConnection(measurement, ServerURL);
            text = conn.execute(measurement.toString()).get();

        } else {
            // if the measurement hasn't been initialized yet.
            text = "Please get measurements first!";
        }
        if (Integer.parseInt(text)==412){
            Toast.makeText(this, "412 Precondition Failed", Toast.LENGTH_SHORT).show();
        }
        else if (Integer.parseInt(text)==422){
            Toast.makeText(this, "422A Unprocessable Entity", Toast.LENGTH_SHORT).show();
        }
        else {
            Log.d("getPosition text: ", text);
            ObjectMapper mapper = new ObjectMapper();
            try {
                pos = mapper.readValue(text, Position.class);
                System.out.println("boci" + pos.getCoordinate());


                TextView textView = (TextView) findViewById(R.id.zoneTextView);
                //TODO helyes értékeket belerakni
                String zoneText = pos.getZone().getName();
                textView.setText(zoneText);
                textView = (TextView) findViewById(R.id.coordsTextView);
                //TODO helyes értékeket belerakni
                DecimalFormat myFormatter = new DecimalFormat("##.##");
                String coordsText = "Coordinates:  X =  " + myFormatter.format(pos.getCoordinate().getX()) + "  Y =  " + myFormatter.format(pos.getCoordinate().getY()) + "  Z =  " + myFormatter.format(pos.getCoordinate().getZ());
                textView.setText(coordsText);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //The method runs when the main action button is pressed on the layout.
    //Gets the WiFi, Bluetooth, magnetometer, position coordinates and current zone and
    // creates a member of the measurement class.
    public void receiveMeasurement(View view) {
        int duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();
        Magnetometer magnetometer = new Magnetometer();
        MeasurementBuilder measurementBuilder = new MeasurementBuilder();
        if (magneto != null) {
            double lenght = Math.sqrt((magneto[0] * magneto[0]) + (magneto[1] * magneto[1]) + (magneto[2] * magneto[2]));
            magneto[0] = (float) (magneto[0] / lenght);
            magneto[1] = (float) (magneto[1] / lenght);
            magneto[2] = (float) (magneto[2] / lenght);
            magnetometer = new Magnetometer(magneto[0], magneto[1], magneto[2], 0);
        }

        //Getting the WifiRSSI and bluetooth values from the services.
        if (rssivalue != null) {
            WiFiRSSI rssi = new WiFiRSSI(rssivalue);


            measurementBuilder.setWifiRSSI(rssi);
        }

        if (bluetootharray != null) {
            BluetoothTags tags = new BluetoothTags(new HashSet<>(bluetootharray));
            measurementBuilder.setbluetoothTags(tags);

        }

        measurementBuilder.setMagnetometer(magnetometer);
        // Get the zone and the coordinates to build the measurement
        Zone zone = (Zone) spinner.getSelectedItem();
        EditText editText1 = (EditText) findViewById(R.id.edit_coord1);
        Double coord1 = Double.parseDouble(editText1.getText().toString());
        EditText editText2 = (EditText) findViewById(R.id.edit_coord2);
        Double coord2 = Double.parseDouble(editText2.getText().toString());
        EditText editText3 = (EditText) findViewById(R.id.edit_coord3);
        Double coord3 = Double.parseDouble(editText3.getText().toString());
        Coordinate coordinates = new Coordinate(coord1, coord2, coord3);
        Position position = new Position(coordinates, zone);
        measurementBuilder.setPosition(position);
        //Building measurement
        measurement = measurementBuilder.build();
        Log.d("alma", measurement.toString());
        //measurement.setId(UUID.randomUUID());
        String text = measurement.toString();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void sendToServer(View view) throws ExecutionException, InterruptedException {
        String text;
        // Starts the method which connects to the server and sends the measurement.
        if (measurement != null) {
            String ServerURL = "http://grabowski.iit.uni-miskolc.hu:8080/ilona/recordMeasurement";
            IlonaConnection conn = new IlonaConnection(measurement, ServerURL);
            text = conn.execute(measurement.toString()).get();
        } else {
            // if the measurement hasn't been initialized yet.
            text = "Please get measurements first!";
        }
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    // Sends the previously recorded measurement to the server.
/*
    public void sendToTracking(View view) throws ExecutionException, InterruptedException {
        // TODO
        EditText editText1 = (EditText) findViewById(R.id.username);
        String username = editText1.getText().toString();
        EditText editText2 = (EditText) findViewById(R.id.passwd);
        String pass = editText2.getText().toString();
        String authInfo = username + ":" + pass;
        String text;
        if (measurement != null) {
            String ServerURL = "http://grabowski.iit.uni-miskolc.hu:8080/ilona/tracking/mobile/proba";
            IlonaTrackingConnection conn = new IlonaTrackingConnection(measurement, ServerURL, authInfo);
            text = conn.execute(measurement.toString()).get();
        } else {
            // if the measurement hasn't been initialized yet.
            text = "Please get measurements first!";
        }
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
*/
    // Magnetometer API method that refreshes the values when they change
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        magneto = sensorEvent.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStart() {

        super.onStart();
        spinner = (Spinner) findViewById(R.id.zone_spinner);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        Action viewAction = Action.newAction(
                Action.TYPE_VIEW,
                "Main Page",
                Uri.parse("http://host/path"),
                Uri.parse("android-app://com.example.ilona.ilonarecorder/http/host/path")
        );

        // TODO
        // HOW TO GET PREFERENCES
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        memsize = Integer.parseInt(preferences.getString("memsize", "5"));
        threshold = Float.parseFloat(preferences.getString("threshold", "5.0"));

        int filternum = Integer.parseInt(preferences.getString("filter_list", "-1"));
        if (filternum >= 0) {
            WiFiRSSIFilteringStrategy filter = selectFilter(memsize, threshold);
            filtering = true;
        } else filtering = false;
        filter = new DynamicTimeWindowFilter(memsize, threshold);

        AppIndex.AppIndexApi.start(client, viewAction);
        //Toast to remind the user to wait a few moments to get the measurements.
        int duration = Toast.LENGTH_LONG;
        Context context = getApplicationContext();
        // Checks if bluetooth and wifi is enabled
        String text;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            text = "Bluetooth isn't enabled";
        } else {
            text = "Bluetooth is enabled";
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            text = text + " and WiFi is enabled";
        } else {
            text = text + " and Wifi isn't enabled";
        }

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        // Spinner adapter
        ArrayAdapter<Zone> adapter = new ArrayAdapter<Zone>(this, android.R.layout.simple_spinner_item, zones) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Get the data item for this position
                Zone zone = getItem(position);
                // Check if an existing view is being reused, otherwise inflate the view
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
                }
//                convertView.setTag(zone.getName());
                ((TextView) convertView).setText(zone.getName());
                // Return the completed view to render on screen
                return convertView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View result = super.getDropDownView(position, convertView, parent);
                Zone zone = getItem(position);
                if (zone == null) {
                    return result;
                }
                ((TextView) result).setText(zone.getName());
                return result;
            }
        };
        spinner.setAdapter(adapter);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.ilona.ilonarecorder/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();

    }

    private WiFiRSSIFilteringStrategy selectFilter(int memsize, double threshold) {
        SharedPreferences preferences = getSharedPreferences("pref_general", 0);
        WiFiRSSIFilteringStrategy filter = new WiFiRSSIFilteringStrategy() {
            @Override
            public Map<String, Double> filteringMethod(LinkedList<Map<String, Double>> linkedList) {
                return null;
            }
        };
        int filternum = preferences.getInt("filter_list", -1);
        switch (filternum) {
            case (0): {
                filter = new HorusFilter(memsize);
            }
            case (1): {
                filter = new StaticTimeWindowFilter(memsize, threshold);
            }
            case (2): {
                filter = new DynamicTimeWindowFilter(memsize, threshold);
            }
        }
        return filter;
    }


    // Handles incoming broadcasts from MyIntentService and BluetoothService.
    class ResponseReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == Constants.BROADCAST_ACTION) {
                rssiList.addFirst((HashMap<String, Double>) intent.getSerializableExtra(Constants.EXTENDED_DATA_STATUS));
                if (rssiList.size() > memsize) {
                    rssiList.removeLast();
                }
                if (filtering = true) {
                    rssivalue = filter.filteringMethod(rssiList);
                } else {
                    rssivalue = rssiList.getFirst();
                }
            } else if (action == Constants.BLUETOOTH_BROADCAST) {
                bluetootharray = new ArrayList<>();
                bluetootharray = intent.getStringArrayListExtra(Constants.BLUETOOTH_DATA_STATUS);
            }

        }
    }
}

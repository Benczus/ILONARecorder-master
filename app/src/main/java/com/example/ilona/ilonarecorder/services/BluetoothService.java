package com.example.ilona.ilonarecorder.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

import com.example.ilona.ilonarecorder.Constants;

import java.util.ArrayList;
import java.util.Set;

public class BluetoothService extends IntentService {

    private ArrayList<String> mArrayAdapter;
    //The bluetooth API automatically send a ACTION_FOUND broadcast from the startDiscovery() method,
    //the receiver receives it, and ands the device name and address to the string array.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mArrayAdapter.contains(device.getName() + " " + device.getAddress())) {
                    mArrayAdapter.add(device.getName() + " " + device.getAddress());
                }
            }

        }
    };


    public BluetoothService() {
        super("BluetoothService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        BluetoothAdapter mBluetoothAdapter;
        Set<BluetoothDevice> pairedDevices;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        mArrayAdapter = new ArrayList<>();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        while (true) {

            // Adds the previously detected devices to the string array of detected devices.
            if (!pairedDevices.isEmpty()) {
                for (BluetoothDevice device : pairedDevices) {
                    if (!mArrayAdapter.contains(device.getName() + " " + device.getAddress())) {
                        mArrayAdapter.add(device.getName() + " " + device.getAddress());
                    }
                }
            }

            // Discovers the bluetooth compatible devices inside the range of the device.
            mBluetoothAdapter.startDiscovery();
            // Bundles the string array of the devices into an intent and sends it to MainActivity.
            Intent localIntent = new Intent(Constants.BLUETOOTH_BROADCAST);
            localIntent.putExtra(Constants.BLUETOOTH_DATA_STATUS, mArrayAdapter);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            int interval = 12000;
            SystemClock.sleep(interval);
        }
    }

}







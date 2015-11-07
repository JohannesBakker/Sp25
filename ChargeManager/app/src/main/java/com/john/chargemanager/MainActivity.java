package com.john.chargemanager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.john.chargemanager.Adapters.DeviceListAdapter;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BleManager;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Event.EventManager;
import com.john.chargemanager.MicroService.Bluetooth.SP25.Sp25Device;
import com.john.chargemanager.Utils.Logger;
import com.support.radiusnetworks.bluetooth.BluetoothCrashResolver;

import java.util.ArrayList;


public class MainActivity extends Activity {

    public static final String TAG = "MAIN-ACTIVITY";
    private static final String EVENT_SCANNING_FINISHED = "IXCHANGE_EVENT_SCANNING_FINISHED";

    private Handler handler;

    ListView lvDevices;
    Button btnScanAll;

    ArrayList<Sp25Device> devicesArray = new ArrayList<>();
    DeviceListAdapter deviceListAdapter = null;
    BluetoothCrashResolver bluetoothCrashResolver = null;

    EventManager eventManager = null;

    boolean isBleEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceListAdapter = new DeviceListAdapter(this, devicesArray);

        lvDevices = (ListView)findViewById(R.id.listView_devices);
        lvDevices.setAdapter(deviceListAdapter);

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sp25Device selDevice = devicesArray.get(position);

                Logger.log(TAG, "Pos = %d, Device = %s, Rssi = %d", position, selDevice.getName(), selDevice.getRssi());
            }
        });

        btnScanAll = (Button)findViewById(R.id.btnScanAll);
        btnScanAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnScanAll();
            }
        });

        deviceListAdapter.notifyDataSetChanged();

        initVariables();
        checkBluetooth();
    }

    protected boolean checkBluetooth() {

        if (!BleManager.sharedInstance().isBleSupported()) {
            Toast.makeText(this, "BLE is not supported", Toast.LENGTH_SHORT);
            isBleEnabled = false;
            return false;
        }

        if (!BleManager.sharedInstance().isBleAvailable()) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT);
            isBleEnabled = false;
            return false;
        }

        isBleEnabled = BleManager.sharedInstance().isBleEnabled();
        if (isBleEnabled) {
            BleManager.sharedInstance().stopAdapter();
        }

        return true;
    }

    protected void initVariables() {
        Context context = getApplicationContext();
        Application app = getApplication();
        bluetoothCrashResolver = new BluetoothCrashResolver(context);
        bluetoothCrashResolver.start();


        BleManager.initialize(context, bluetoothCrashResolver);
        EventManager.initalize(context);

        eventManager = EventManager.sharedInstance();
    }

    private void OnScanAll() {

        BleManager.sharedInstance().scanForPeripheralsWithServices(null, true);

        handler = new Handler(this.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                BleManager.sharedInstance().stopScan();

                RefreshDeviceListView();
            }
        }, 10 * 1000);
    }

    public void RefreshDeviceListView() {
        ArrayList<BlePeripheral> peripherals = BleManager.sharedInstance().getScannedPeripherals();

        if (peripherals.isEmpty())
            return;

        devicesArray.clear();

        for (int i = 0; i < peripherals.size(); i++)
        {
            Sp25Device newDev = new Sp25Device(peripherals.get(i));

            devicesArray.add(newDev);
        }

        deviceListAdapter.notifyDataSetChanged();

    }

}

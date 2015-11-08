package com.john.chargemanager;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.john.chargemanager.Adapters.DeviceListAdapter;
import com.john.chargemanager.Adapters.ServiceListAdapter;
import com.john.chargemanager.DataModel.ServiceData;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BleManager;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Event.EventManager;
import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
import com.john.chargemanager.MicroService.Bluetooth.SP25.Sp25Device;
import com.john.chargemanager.MicroService.Bluetooth.SP25.service.BatteryControlService;
import com.john.chargemanager.Utils.AppCommon;
import com.john.chargemanager.Utils.Logger;
import com.support.radiusnetworks.bluetooth.BluetoothCrashResolver;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;


public class MainActivity extends Activity {

    public static final String TAG = "MAIN-ACTIVITY";
    private static final String EVENT_SCANNING_FINISHED = "IXCHANGE_EVENT_SCANNING_FINISHED";

    private int kSERVICE_ITEM_DISCHG_USB1   = 0;
    private int kSERVICE_ITEM_DISCHG_USB2   = 1;
    private int kSERVICE_ITEM_BAT_CHG       = 2;
    private int kSERVICE_ITEM_QUALITY       = 3;

    private Handler handler;

    ListView lvDevices;
    ListView lvServices;
    Button btnScanAll;
    Button btnDisconnect;
    Button btnRefresh;
    RelativeLayout layout_Devices;
    RelativeLayout layout_Services;

    ArrayList<Sp25Device> devicesArray = new ArrayList<Sp25Device>();
    DeviceListAdapter deviceListAdapter = null;

    ArrayList<ServiceData> servicesArray = new ArrayList<ServiceData>();
    ServiceListAdapter serviceListAdapter = null;


    BluetoothCrashResolver bluetoothCrashResolver = null;

    EventManager eventManager = null;

    boolean isBleEnabled = false;

    AppCommon appCommon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appCommon = AppCommon.sharedInstance();

        deviceListAdapter = new DeviceListAdapter(this, devicesArray);
        serviceListAdapter = new ServiceListAdapter(this, servicesArray);

        lvDevices = (ListView)findViewById(R.id.listView_devices);
        lvDevices.setAdapter(deviceListAdapter);

        lvServices = (ListView)findViewById(R.id.listView_services);
        lvServices.setAdapter(serviceListAdapter);

        lvDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Sp25Device selDevice = devicesArray.get(position);

                Logger.log(TAG, "Pos = %d, Device = %s, Rssi = %d", position, selDevice.getName(), selDevice.getRssi());

                appCommon.setDevice(selDevice);
                appCommon.setPeripheral(selDevice.getPeripheral());

                // device connect
                BleManager.sharedInstance().connectPeripheral(selDevice.getPeripheral());
//                EventBus.getDefault().post(new SEvent(BleManager.kBLEManagerConnectedPeripheralNotification, selDevice.getPeripheral()));

                layout_Devices.setVisibility(View.INVISIBLE);
                layout_Services.setVisibility(View.VISIBLE);

                btnDisconnect.setEnabled(true);
                btnScanAll.setVisibility(View.INVISIBLE);
                btnRefresh.setVisibility(View.VISIBLE);
            }
        });

        layout_Devices = (RelativeLayout)findViewById(R.id.layout_Devices);
        layout_Devices.setVisibility(View.VISIBLE);

        layout_Services = (RelativeLayout)findViewById(R.id.layout_Services);
        layout_Services.setVisibility(View.INVISIBLE);

        btnScanAll = (Button)findViewById(R.id.btnScanAll);
        btnScanAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnScanAll();
            }
        });

        btnDisconnect = (Button)findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnDisconnect();
            }
        });

        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnBatteryRefresh(AppCommon.sharedInstance().getDevice());
            }
        });


        btnDisconnect.setEnabled(false);
        btnScanAll.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.INVISIBLE);

        deviceListAdapter.notifyDataSetChanged();

        initVariables();
        checkBluetooth();

        EventBus.getDefault().register(this);
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
/*        if (isBleEnabled) {
            BleManager.sharedInstance().stopAdapter();
            BleManager.sharedInstance().stopAdapter();
        }
*/
        if (!isBleEnabled) {
            BleManager.sharedInstance().enableAdapter();
        }

        return true;
    }

    protected void initVariables() {
        Context context = getApplicationContext();

        bluetoothCrashResolver = new BluetoothCrashResolver(context);
        appCommon.setBluetoothCrashResolver(bluetoothCrashResolver);

        bluetoothCrashResolver.start();


        BleManager.initialize(context, bluetoothCrashResolver);
        EventManager.initalize(context);

        eventManager = EventManager.sharedInstance();

        // init service list
        ServiceData srvUsb1 = new ServiceData(BatteryControlService.BATTERY_CONTROL_DISCHG_USB1_UUID,
                BatteryControlService.kBatteryControlDischargeUsb1,
                "");
        ServiceData srvUsb2 = new ServiceData(BatteryControlService.BATTERY_CONTROL_DISCHG_USB2_UUID,
                BatteryControlService.kBatteryControlDischargeUsb2,
                "");
        ServiceData srvBattChg = new ServiceData(BatteryControlService.BATTERY_CONTROL_BAT_CHG_UUID,
                BatteryControlService.kBatteryControlBatteryCharge,
                "");
        ServiceData srvQuality = new ServiceData(BatteryControlService.BATTERY_CONTROL_QUALITY_UUID,
                BatteryControlService.kBatteryControlQuality,
                "");

        servicesArray.add(srvUsb1);
        servicesArray.add(srvUsb2);
        servicesArray.add(srvBattChg);
        servicesArray.add(srvQuality);

        serviceListAdapter.notifyDataSetChanged();
    }

    private void OnScanAll() {

        // clear device list view items
        devicesArray.clear();
        deviceListAdapter.notifyDataSetChanged();

        // scan peripherals
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

    private void OnDisconnect() {
        Sp25Device selDevice = appCommon.getDevice();
        BlePeripheral peripheral = appCommon.getPeripheral();

/*        if (peripheral != null) {
            EventBus.getDefault().post(new SEvent(BleManager.kBLEManagerDisconnectedPeripheralNotification, peripheral));
        }
*/
        selDevice.disconnect();
        BleManager.sharedInstance().disconnectPeripheral(peripheral);

        appCommon.setDevice(null);
        appCommon.setPeripheral(null);

        layout_Devices.setVisibility(View.VISIBLE);
        layout_Services.setVisibility(View.INVISIBLE);

        btnDisconnect.setEnabled(false);
        btnScanAll.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.INVISIBLE);
    }

    private void OnBatteryRefresh(Sp25Device selDevice) {

        if (selDevice == null)
            return;

        String szDisUsb1 = String.format("%fV, %fA", selDevice.getUsb1Voltage()/1000, selDevice.getUsb1Current()/1000);
        String szDisUsb2 = String.format("%fV, %fA", selDevice.getUsb2Voltage()/1000, selDevice.getUsb2Current()/1000);
        String szBattChg = String.format("%fV, %fA", selDevice.getBattChargeVoltage() / 1000, selDevice.getBattChargeCurrent() / 1000);
        String szQuality = String.format("%d %", selDevice.getBattQuality());

        servicesArray.get(kSERVICE_ITEM_DISCHG_USB1).setValue(szDisUsb1);
        servicesArray.get(kSERVICE_ITEM_DISCHG_USB2).setValue(szDisUsb2);
        servicesArray.get(kSERVICE_ITEM_BAT_CHG).setValue(szBattChg);
        servicesArray.get(kSERVICE_ITEM_QUALITY).setValue(szQuality);

        serviceListAdapter.notifyDataSetChanged();
    }

    public void onEventMainThread(SEvent e)  {
        if (Sp25Device.kSp25DeviceDataUpdatedNotification.equalsIgnoreCase(e.name)) {
            OnBatteryRefresh((Sp25Device)e.object);
        }
    }




}

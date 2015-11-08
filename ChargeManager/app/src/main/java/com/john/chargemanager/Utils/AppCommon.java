package com.john.chargemanager.Utils;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.SP25.Sp25Device;
import com.support.radiusnetworks.bluetooth.BluetoothCrashResolver;

/**
 * Created by dev on 11/7/2015.
 */
public class AppCommon {
    private BluetoothCrashResolver bluetoothCrashResolver = null;
    private Sp25Device device = null;
    private BlePeripheral peripheral = null;


    public static AppCommon _instance = null;

    public static AppCommon sharedInstance() {
        if (_instance == null)
            _instance = new AppCommon();
        return _instance;
    }

    public AppCommon() {
        _instance = this;
        bluetoothCrashResolver = null;
        device = null;

    }

    public BluetoothCrashResolver getBluetoothCrashResolver() {
        return bluetoothCrashResolver;
    }

    public void setBluetoothCrashResolver(BluetoothCrashResolver bluetoothCrashResolver) {
        this.bluetoothCrashResolver = bluetoothCrashResolver;
    }

    public Sp25Device getDevice() {
        return device;
    }

    public void setDevice(Sp25Device device) {
        this.device = device;
    }

    public BlePeripheral getPeripheral() {
        return peripheral;
    }

    public void setPeripheral(BlePeripheral peripheral) {
        this.peripheral = peripheral;
    }
}

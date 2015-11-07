package com.john.chargemanager.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothDevice;

/**
 * Created by dev on 10/30/2015.
 */
public interface BleScannerListener {
    public void deviceScanned(BluetoothDevice device, int rssi, byte[] scanRecord);
    public boolean shouldCheckDevice(BluetoothDevice device, int rssi, byte[] scanRecord);
}

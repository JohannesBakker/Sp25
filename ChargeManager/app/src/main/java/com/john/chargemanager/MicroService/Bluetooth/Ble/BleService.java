package com.john.chargemanager.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothGattService;

/**
 * Created by dev on 11/7/2015.
 */
public abstract class BleService {
    protected BluetoothGattService _service;
    protected BlePeripheral _peripheral;

    public BlePeripheral peripheral() {
        return _peripheral;
    }

    public BluetoothGattService service() {
        return _service;
    }

    public abstract void setService(BlePeripheral peripheral, BluetoothGattService service);
}

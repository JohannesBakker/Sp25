package com.john.chargemanager.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Created by dev on 10/30/2015.
 */
public interface BlePeripheralDelegate {
    public void gattConnected(BlePeripheral peripheral);
    public void gattDisconnected(BlePeripheral peripheral);
    public void gattServicesDiscovered(BlePeripheral peripheral);
    public void gattDataAvailable(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value);
    public void gattReadRemoteRssi(BlePeripheral peripheral, int rssi);
    public void gattDescriptionWrite(BlePeripheral peripheral, BluetoothGattDescriptor descriptor, boolean status);
}

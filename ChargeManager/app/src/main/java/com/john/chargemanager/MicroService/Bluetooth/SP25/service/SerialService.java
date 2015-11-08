package com.john.chargemanager.MicroService.Bluetooth.SP25.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BleService;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by dev on 11/8/2015.
 */
public class SerialService extends BleService {

    public static final String TAG = "SerialSevice";
    public static final String kSerialService = "Serial service";

    public static final String UUID_SERIAL_SERVICE          = "00005500-D102-11E1-9B23-00025B00A5A5";
    public static final String UUID_SERIAL_DATA_TRANSFER    = "00005501-D102-11E1-9B23-00025B00A5A5";

    public static final String ACTION_UUID_SERIAL_SERVICE = "Charge Serial Service";
    public static final String ACTION_UUID_SERIAL_DATA_TRANSFER = "Charge Serial Data Transfer Service";

    private static HashMap<String, String> attributes = new HashMap<String, String>();

    static {
        // Serial Services
        attributes.put(UUID_SERIAL_SERVICE,         ACTION_UUID_SERIAL_SERVICE);         // UUID_SERIAL_SERVICE
        attributes.put(UUID_SERIAL_DATA_TRANSFER,   ACTION_UUID_SERIAL_DATA_TRANSFER);   // UUID_SERIAL_DATA_TRANSFER
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null? defaultName: name;
    }

    public static boolean hasUuid(String uuid) {
        String name = attributes.get(uuid);
        return (name == null)? false: true;
    }

    public static UUID serialServiceID() {
        return UUID.fromString(UUID_SERIAL_SERVICE);
    }

    public static UUID serialDataTransferServiceID() {
        return UUID.fromString(UUID_SERIAL_DATA_TRANSFER);
    }

    @Override
    public void setService(BlePeripheral peripheral, BluetoothGattService service) {
        this._peripheral = peripheral;
        this._service = service;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        // TODO

        if (characteristic.getUuid().equals(serialServiceID())) {

        } else if (characteristic.getUuid().equals(serialDataTransferServiceID())) {

        }

    }
}

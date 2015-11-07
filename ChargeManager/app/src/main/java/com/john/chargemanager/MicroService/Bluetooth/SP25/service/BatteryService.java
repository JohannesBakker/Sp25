package com.john.chargemanager.MicroService.Bluetooth.SP25.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BleManager;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BleService;
import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
import com.john.chargemanager.Utils.Logger;

import java.util.Iterator;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by dev on 11/7/2015.
 */
public class BatteryService extends BleService {
    public static final String TAG = "BatteryService";

    public static final String kBatteryServiceReadBatteryLevel = "battery service read battery level";

    public int batteryLevel;

    public static UUID batteryServiceID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("180F"));
    }

    public static UUID batteryLevelID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A19"));
    }

    @Override
    public void setService(BlePeripheral peripheral, BluetoothGattService service) {
        this._peripheral = peripheral;
        this._service = service;

        // read whole characteristics
        Iterator ci = service.getCharacteristics().iterator();
        while (ci.hasNext()) {
            BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic)ci.next();
            peripheral.readCharacteristic(ch);
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (characteristic.getService() == _service) {
            Logger.log(TAG, "readCharacteristic, batteryService");
            if (characteristic.getUuid().equals(BatteryService.batteryServiceID())) {
                Logger.log(TAG, "readCharacteristic, battery_level characteristic");
                if (value.length >= 1) {
                    batteryLevel = (int) value[0];
                    Logger.log(TAG, "readCharacteristic, battery_level characteristic - %d", batteryLevel);
                    EventBus.getDefault().post(new SEvent(kBatteryServiceReadBatteryLevel, _peripheral));
                }
            }
        }
    }
}

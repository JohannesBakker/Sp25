package com.john.chargemanager.MicroService.Bluetooth.SP25.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BleManager;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BleService;
import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
import com.john.chargemanager.Utils.Logger;

import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by dev on 11/7/2015.
 */
public class DeviceInformationService extends BleService {

    public static final String TAG = "DeviceInformationService";

    public static final String kDeviceInforamtionReadSerialNumber = "device information read serial number";

    public String manufacturerName;
    public String modelNumber;
    public String serialNumber;
    public String hardwareRevision;
    public String firmwareRevision;
    public String softwareRevision;


    public static UUID deviceInforamtionServiceID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("180A"));
    }

    public static UUID manufactureNameID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A29"));
    }

    public static UUID modelNumberID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A24"));
    }

    public static UUID serialNumberID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A25"));
    }

    public static UUID hardwareRevisionID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A27"));
    }

    public static UUID firmwareRevisionID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A26"));
    }

    public static UUID softwareRevisionID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A28"));
    }

    public static UUID systemIDID() {
        return UUID.fromString(BleManager.getLongUuidFromShortUuid("2A23"));
    }

    @Override
    public void setService(BlePeripheral peripheral, BluetoothGattService service) {
        this._peripheral = peripheral;
        this._service = service;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {
        if (characteristic.getUuid().equals(serialNumberID())) {
            Logger.log(TAG, "read serial number --------------- ");
            try {
                String key = new String(value, "utf-8");
                Logger.log("read serial number : %s", key);

                String remains = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                String cleanedkey = "";

                for (int i = 0; i < key.length(); i++) {
                    if (remains.contains(key.charAt(i) + ""))
                        cleanedkey = cleanedkey + key.charAt(i);
                }

                serialNumber = cleanedkey;
                serialNumber = serialNumber.toUpperCase();
                Logger.log(TAG, "read serial number : %s", serialNumber);

                _peripheral.setSerialNo(serialNumber);

                EventBus.getDefault().post(new SEvent(kDeviceInforamtionReadSerialNumber, _peripheral));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

package com.john.chargemanager.MicroService.Bluetooth.SP25.service;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BleService;
import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
import com.john.chargemanager.Utils.AppCommon;
import com.john.chargemanager.Utils.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by dev on 11/8/2015.
 */
public class BatteryControlService extends BleService {
    public static final String TAG = "BatteryControlService";

    public static final String kBatteryControlService = "Battery Control Service";
    public static final String kBatteryControlDischargeUsb1 = "Battery Control Discharge USB1 Service";
    public static final String kBatteryControlDischargeUsb2 = "Battery Control Discharge USB2 Service";
    public static final String kBatteryControlBatteryCharge = "Battery Control Battery Charge Service";
    public static final String kBatteryControlQuality = "Battery Control Quality Service";
    public static final String kBatteryControlFindMe = "Find Me Service";

    public static final String BATTERY_CONTROL_SERVICE_UUID     = "00002016-d102-11e1-9b23-00025b00a5a5";
    public static final String BATTERY_CONTROL_DISCHG_USB1_UUID = "00002013-d102-11e1-9b23-00025b00a5a5";
    public static final String BATTERY_CONTROL_DISCHG_USB2_UUID = "00002018-d102-11e1-9b23-00025b00a5a5";
    public static final String BATTERY_CONTROL_BAT_CHG_UUID     = "00002014-d102-11e1-9b23-00025b00a5a5";
    //public static final String BATTERY_CONTROL_QUALITY_UUID     = "00002011-d102-11e1-9b23-00025b00a5a5";
    public static final String BATTERY_CONTROL_QUALITY_UUID     = "00002014-d102-11e1-9b23-00025b00a5a5";
    public static final String FIND_ME_UUID                     = "00002012-d102-11e1-9b23-00025b00a5a5";


    private static HashMap<String, String> attributes = new HashMap<String, String>();

    static {
        // Services

        // Battery Control Services
        attributes.put(BATTERY_CONTROL_SERVICE_UUID,        kBatteryControlService);        // BATTERY_CONTROL_SERVICE_UUID
        attributes.put(BATTERY_CONTROL_DISCHG_USB1_UUID,    kBatteryControlDischargeUsb1);  // BATTERY_CONTROL_DISCHG_USB1_UUID
        attributes.put(BATTERY_CONTROL_DISCHG_USB2_UUID,    kBatteryControlDischargeUsb2);  // BATTERY_CONTROL_DISCHG_USB2_UUID
        attributes.put(BATTERY_CONTROL_BAT_CHG_UUID,        kBatteryControlBatteryCharge);  // BATTERY_CONTROL_BAT_CHG_UUID
        attributes.put(BATTERY_CONTROL_QUALITY_UUID,        kBatteryControlQuality);        // BATTERY_CONTROL_QUALITY_UUID
        attributes.put(FIND_ME_UUID,                        kBatteryControlFindMe);         // FIND_ME_UUID
    }

    public float usb1Voltage = 0, usb1Current = 0;  // mV/mA
    public float usb2Voltage = 0, usb2Current = 0;
    public float battChargeVoltage = 0, battChargeCurrent = 0;
    public int  quality = 0;
    public int  command = 0x0000;   // R/W

    private BluetoothGattCharacteristic _chDisChgUsb1;
    private BluetoothGattCharacteristic _chDisChgUsb2;
    private BluetoothGattCharacteristic _chBattChg;
    private BluetoothGattCharacteristic _chQuality;
    private BluetoothGattCharacteristic _chCommand;

    private Handler _handlerChgUsb1;
    private Handler _handlerChgUsb2;
    private Handler _handlerBattChg;
    private Handler _handlerQuality;

    private Runnable _runnableChgUsb1;
    private Runnable _runnableChgUsb2;
    private Runnable _runnableBattChg;
    private Runnable _runnableQuality;

    public BatteryControlService() {
        _handlerChgUsb1 = new Handler();
        _handlerChgUsb2 = new Handler();
        _handlerBattChg = new Handler();
        _handlerQuality = new Handler();
    }


    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null? defaultName: name;
    }

    public static boolean hasUuid(String uuid) {
        String name = attributes.get(uuid);
        return (name == null)? false: true;
    }

    public static UUID batteryControlServiceID() {
        return UUID.fromString(BATTERY_CONTROL_SERVICE_UUID);
    }

    public static UUID batteryControlDischargeUsb1ID() {
        return UUID.fromString(BATTERY_CONTROL_DISCHG_USB1_UUID);
    }

    public static UUID batteryControlDischargeUsb2ID() {
        return UUID.fromString(BATTERY_CONTROL_DISCHG_USB2_UUID);
    }

    public static UUID batteryControlBatteryChargeID() {
        return UUID.fromString(BATTERY_CONTROL_BAT_CHG_UUID);
    }

    public static UUID batteryControlQualityID() {
        return UUID.fromString(BATTERY_CONTROL_QUALITY_UUID);
    }

    public static UUID batteryControlFineMeID() {
        return UUID.fromString(FIND_ME_UUID);
    }

    @Override
    public void setService(BlePeripheral peripheral, BluetoothGattService service) {
        this._peripheral = peripheral;
        this._service = service;

        // read whole characteristics
        Iterator ci = service.getCharacteristics().iterator();
        while(ci.hasNext()) {
            BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic)ci.next();
            BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID.fromString(BlePeripheral.CLIENT_CHARACTERISTIC_CONFIG));

            //peripheral.readCharacteristic(ch);

            if (ch.getUuid().equals(batteryControlDischargeUsb1ID()) && descriptor != null) {
                _chDisChgUsb1 = ch;
                this._peripheral.setCharacteristicNotification(_chDisChgUsb1, true);
            }
            else if (ch.getUuid().equals(batteryControlDischargeUsb2ID()) && descriptor != null) {
                _chDisChgUsb2 = ch;
                this._peripheral.setCharacteristicNotification(_chDisChgUsb2, true);
            }
            else if (ch.getUuid().equals(batteryControlBatteryChargeID()) && descriptor != null) {
                _chBattChg = ch;
                this._peripheral.setCharacteristicNotification(_chBattChg, true);
            }
            else if (ch.getUuid().equals(batteryControlQualityID()) && descriptor == null) {
                _chQuality = ch;
            }
            else if (ch.getUuid().equals(batteryControlFineMeID())) {
                _chCommand = ch;
            }
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, byte[] value) {

        if (value == null)
            return;

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BlePeripheral.CLIENT_CHARACTERISTIC_CONFIG));


        if (characteristic.getService() == _service) {

            Logger.log(TAG, "readCharacteristic, batteryControlService");

            if (characteristic.getUuid().equals(BatteryControlService.batteryControlDischargeUsb1ID())) {
                Logger.log(TAG, "readCharacteristic, USB1 discharge characteristic");
                if (value.length >= 8) {

                    usb1Voltage = Character.getNumericValue(value[0]) * 1000
                            + Character.getNumericValue(value[1]) * 100
                            + Character.getNumericValue(value[2]) * 10
                            + Character.getNumericValue(value[3]);

                    usb1Current = Character.getNumericValue(value[4]) * 1000
                            + Character.getNumericValue(value[5]) * 100
                            + Character.getNumericValue(value[6]) * 10
                            + Character.getNumericValue(value[7]);

                    Logger.log(TAG, "readCharacteristic, Discharge USB1 Voltage %fV, Current %fA", usb1Voltage / 1000, usb1Current / 1000);
                    EventBus.getDefault().post(new SEvent(kBatteryControlDischargeUsb1, _peripheral));
                }
            }
            else if (characteristic.getUuid().equals(BatteryControlService.batteryControlDischargeUsb2ID())) {
                Logger.log(TAG, "readCharacteristic, USB2 discharge characteristic");
                if (value.length >= 8) {

                    usb2Voltage = Character.getNumericValue((char)value[0]) * 1000
                            + Character.getNumericValue((char)value[1]) * 100
                            + Character.getNumericValue((char)value[2]) * 10
                            + Character.getNumericValue((char)value[3]);

                    usb2Current = Character.getNumericValue((char)value[4]) * 1000
                            + Character.getNumericValue((char)value[5]) * 100
                            + Character.getNumericValue((char)value[6]) * 10
                            + Character.getNumericValue((char)value[7]);

                    Logger.log(TAG, "readCharacteristic, Discharge USB2 Voltage %fV, Current %fA", usb2Voltage / 1000, usb2Current / 1000);
                    EventBus.getDefault().post(new SEvent(kBatteryControlDischargeUsb2, _peripheral));
                }
            }
            else if (characteristic.getUuid().equals(BatteryControlService.batteryControlBatteryChargeID()) && descriptor != null) {
                Logger.log(TAG, "readCharacteristic, Battery charge characteristic");
                if (value.length >= 8) {

                    battChargeVoltage = Character.getNumericValue(value[0]) * 1000
                            + Character.getNumericValue(value[1]) * 100
                            + Character.getNumericValue(value[2]) * 10
                            + Character.getNumericValue(value[3]);

                    battChargeCurrent = Character.getNumericValue(value[4]) * 1000
                            + Character.getNumericValue(value[5]) * 100
                            + Character.getNumericValue(value[6]) * 10
                            + Character.getNumericValue(value[7]);

                    Logger.log(TAG, "readCharacteristic, Battery Charge Voltage %fV, Current %fA", battChargeVoltage / 1000, battChargeCurrent / 1000);
                    EventBus.getDefault().post(new SEvent(kBatteryControlBatteryCharge, _peripheral));
                }
            }
            else if (characteristic.getUuid().equals(BatteryControlService.batteryControlQualityID()) && descriptor == null) {
                Logger.log(TAG, "readCharacteristic, Battery quality characteristic");
                if (value.length >= 2) {

                    quality = (value[1] << 4) + value[0];

                    Logger.log(TAG, "readCharacteristic, Battery Quality %d ", quality);
                    EventBus.getDefault().post(new SEvent(kBatteryControlQuality, _peripheral));
                }
            }
            else if (characteristic.getUuid().equals(BatteryControlService.batteryControlFineMeID())) {
                Logger.log(TAG, "readCharacteristic, Battery Find Me characteristic");
                if (value.length >= 2) {

                    String szMe = value[0] + "";
                    szMe = szMe + value[1] + "";

                    command = Integer.parseInt(szMe, 16);
                    Logger.log(TAG, "readCharacteristic, Battery Function 0x%04X", command);
                    EventBus.getDefault().post(new SEvent(kBatteryControlFindMe, _peripheral));
                }
            }
        }
    }

    public boolean descriptorWrite(BluetoothGattDescriptor descriptor, boolean success) {

        if (success == false) {
            return false;
        }
        return true;
    }

    public void readAllData() {

        _runnableChgUsb1 = new Runnable() {
            @Override
            public void run() {
                BlePeripheral peripheral = AppCommon.sharedInstance().getPeripheral();

                if (_chDisChgUsb1 != null &&  peripheral!= null) {
                    peripheral.readCharacteristic(_chDisChgUsb1);
                }
            }
        };
        _handlerChgUsb1.postDelayed(_runnableChgUsb1, 1 * 500);

        _runnableChgUsb2 = new Runnable() {
            @Override
            public void run() {
                BlePeripheral peripheral = AppCommon.sharedInstance().getPeripheral();

                if (_chDisChgUsb2 != null &&  peripheral!= null) {
                    peripheral.readCharacteristic(_chDisChgUsb2);
                }
            }
        };
        _handlerChgUsb2.postDelayed(_runnableChgUsb2, 2 * 500);

        _runnableBattChg = new Runnable() {
            @Override
            public void run() {
                BlePeripheral peripheral = AppCommon.sharedInstance().getPeripheral();

                if (_chBattChg != null &&  peripheral!= null) {
                    peripheral.readCharacteristic(_chBattChg);
                }
            }
        };
        _handlerBattChg.postDelayed(_runnableBattChg, 3 * 500);

        _runnableQuality = new Runnable() {
            @Override
            public void run() {
                BlePeripheral peripheral = AppCommon.sharedInstance().getPeripheral();

                if (_chQuality != null &&  peripheral!= null) {
                    peripheral.readCharacteristic(_chQuality);
                }
            }
        };
        _handlerQuality.postDelayed(_runnableQuality, 4 * 500);

    }
}

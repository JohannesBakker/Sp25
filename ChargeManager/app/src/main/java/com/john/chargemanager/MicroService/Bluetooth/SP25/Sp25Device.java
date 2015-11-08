package com.john.chargemanager.MicroService.Bluetooth.SP25;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BleManager;
import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
import com.john.chargemanager.MicroService.Bluetooth.SP25.service.BatteryControlService;
import com.john.chargemanager.MicroService.Bluetooth.SP25.service.BatteryService;
import com.john.chargemanager.MicroService.Bluetooth.SP25.service.DeviceInformationService;
import com.john.chargemanager.Utils.Logger;

import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by dev on 11/7/2015.
 */
public class Sp25Device {
    public static final String TAG = "Sp25Device";

    public static final int DEVICE_NOTCONNECTED = 0;
    public static final int DEVICE_CONNECTING = 1;
    public static final int DEVICE_CONNECTED = 2;
    public static final int DEVICE_DISCONNECTED = 3;
    public static final int DEVICE_PERFORMING = 4;
    public static final int DEVICE_FINISHED = 5;

    public static final String kSp25DeviceDataUpdatedNotification = "SP25 device data updated notification";
    public static final String kSp25DeviceConnectedNotification = "SP25 device connected notification";
    public static final String kSp25DeviceDisconnectedNotification = "SP25 device disconnected notification";

    protected BlePeripheral _peripheral;
    protected DeviceInformationService _deviceInfoService;
    protected BatteryService _batteryService;
    protected BatteryControlService _batteryControlService;

    private int mDeviceState = DEVICE_NOTCONNECTED;

    public int _batteryLevel;

    private float usb1Voltage = 0, usb1Current = 0;     // mV/mA
    private float usb2Voltage = 0, usb2Current = 0;     // mV/mA
    private float battChargeVoltage = 0, battChargeCurrent = 0; // mV/mA
    private int  quality = 0;
    private int  command = 0x0000;   // R/W

    public Sp25Device(BlePeripheral peripheral) {
        this._peripheral = peripheral;

        _batteryService = new BatteryService();
        _deviceInfoService = new DeviceInformationService();
        _batteryControlService = new BatteryControlService();

        EventBus.getDefault().register(this);

        if (peripheral != null) {
            if (peripheral.connectionState() == BlePeripheral.STATE_CONNECTED) {
                if (mDeviceState == DEVICE_NOTCONNECTED) {
                    _peripheralConnected(peripheral);
                }
            }
        }
    }

    public void setDeviceState(int deviceState) {
        this.mDeviceState = deviceState;
        EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
    }

    public int getDeviceState() {
        return mDeviceState;
    }

    public String getAddress() {
        if (_peripheral == null)
            return null;
        return _peripheral.address();
    }

    public String getName() {
        if (_peripheral == null)
            return "";
        else
            return _peripheral.name();
    }

    public int getRssi() {
        return _peripheral.rssi();
    }

    public float getUsb1Voltage() {
        return usb1Voltage;
    }

    public float getUsb1Current() {
        return usb1Current;
    }

    public float getUsb2Voltage() {
        return usb2Voltage;
    }

    public float getUsb2Current() {
        return usb2Current;
    }

    public float getBattChargeVoltage() {
        return battChargeVoltage;
    }

    public float getBattChargeCurrent() {
        return battChargeCurrent;
    }

    public int getBattQuality() {
        return quality;
    }

    public int getBattCommand() {
        return command;
    }

    public void setBattCommand(int command) {
        this.command = command;
    }

    public BlePeripheral getPeripheral() {
        return _peripheral;
    }

    public void disconnect() {
        if (mDeviceState == DEVICE_CONNECTING ||
                mDeviceState == DEVICE_CONNECTED ||
                mDeviceState == DEVICE_PERFORMING ) {
            Logger.log(TAG, "Sp25Device.disconnect() mDeviceState(%d) => DEVICE_DISCONNECTED", mDeviceState);
            mDeviceState = DEVICE_DISCONNECTED;
        }
        EventBus.getDefault().unregister(this);
    }



    public void onEventMainThread(SEvent e) {

        if (BleManager.kBLEManagerConnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
            _peripheralConnected((BlePeripheral)e.object);
        } else if (BleManager.kBLEManagerDisconnectedPeripheralNotification.equalsIgnoreCase(e.name)) {
            _peripheralDisconnected((BlePeripheral)e.object);
        } else if (BleManager.kBLEManagerPeripheralServiceDiscovered.equalsIgnoreCase(e.name)) {
            _retrievedCharacteristics((BlePeripheral)e.object);
        } else if (BleManager.kBLEManagerPeripheralDataAvailable.equalsIgnoreCase(e.name)) {
            BleManager.CharacteristicData data = (BleManager.CharacteristicData)e.object;
            _readCharacteristic(data.peripheral, data.characteristic, data.value);
        } else if (BleManager.kBLEManagerPeripheralRssiUpdated.equalsIgnoreCase(e.name)) {
            _rssiUpdated((BlePeripheral)e.object);
        } else if (DeviceInformationService.kDeviceInforamtionReadSerialNumber.equalsIgnoreCase(e.name)) {
            _readDeviceSerialNumber((BlePeripheral)e.object);
        } else if (BatteryService.kBatteryServiceReadBatteryLevel.equalsIgnoreCase(e.name)) {
            _updateBatteryLevel((BlePeripheral)e.object);
        }
        else if (BatteryControlService.kBatteryControlDischargeUsb1.equalsIgnoreCase(e.name)) {
            _updateDischargeUsb1((BlePeripheral)e.object);
        } else if (BatteryControlService.kBatteryControlDischargeUsb2.equalsIgnoreCase(e.name)) {
            _updateDischargeUsb2((BlePeripheral) e.object);
        } else if (BatteryControlService.kBatteryControlBatteryCharge.equalsIgnoreCase(e.name)) {
            _updateBatteryCharge((BlePeripheral)e.object);
        } else if (BatteryControlService.kBatteryControlQuality.equalsIgnoreCase(e.name)) {
            _updateBatteryQuality((BlePeripheral)e.object);
        }

    }

    protected void _peripheralConnected(BlePeripheral peripheral) {
        if (peripheral != _peripheral || peripheral == null)
            return;

        if (mDeviceState == DEVICE_NOTCONNECTED) {
            Logger.log(TAG, "Sp25Device._peripheralConnected (%s), state == DEVICE_NOTCONNECTED => DEVICE_CONNECTING", peripheral.address());
            mDeviceState = DEVICE_CONNECTING;
        }
        else {
            Logger.log(TAG, "Sp25Device._peripheralConnected (%s), state (%d) != DEVICE_NOTCONNECTED", peripheral.address(), mDeviceState);
        }

        EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
    }

    protected void _peripheralDisconnected(BlePeripheral peripheral) {
        if (peripheral != _peripheral || peripheral == null)
            return;

        Logger.log(TAG, "Sp25Device._peripheralDisconnected (%s)", peripheral.address());

        switch (mDeviceState) {
            case DEVICE_NOTCONNECTED:
                mDeviceState = DEVICE_DISCONNECTED;
                Logger.e(TAG, "Sp25Device._peripheralDisconnected (%s), state => DEVICE_DISCONNECTED", peripheral.address());
                break;

            case DEVICE_CONNECTING:
                Logger.e(TAG, "Sp25Device._peripheralDisconnected, mDeviceState == DEVICE_CONNECTING");
                mDeviceState = DEVICE_DISCONNECTED;
                _peripheral.disconnect();
                break;

            case DEVICE_CONNECTED:
                Logger.e(TAG, "Sp25Device._peripheralDisconnected, mDeviceState == DEVICE_CONNECTED, change state to DISCONNECTED");
                mDeviceState = DEVICE_DISCONNECTED;
                Logger.e(TAG, "Sp25Device._peripheralDisconnected (%s), state => DEVICE_DISCONNECTED", peripheral.address());
                break;

            case DEVICE_PERFORMING:
                Logger.e(TAG, "Sp25Device._peripheralDisconnected, mDeviceState == DEVICE_PERFORMING, change state to DISCONNECTED");
                mDeviceState = DEVICE_DISCONNECTED;
                Logger.e(TAG, "Sp25Device._peripheralDisconnected (%s), state => DEVICE_DISCONNECTED", peripheral.address());
                break;

            default:
                break;
        }

        EventBus.getDefault().post(new SEvent(kSp25DeviceDisconnectedNotification, this));
    }

    protected void _retrievedCharacteristics(BlePeripheral peripheral) {
        if (peripheral != _peripheral || peripheral == null)
            return;

        Logger.log(TAG, "_retrievedCharacteristics, peripheral(%s)", _peripheral.address());
        List<BluetoothGattService> serviceList = peripheral.getSupportedGattServices();
        Iterator i = serviceList.iterator();

        while (i.hasNext()) {
            BluetoothGattService service = (BluetoothGattService)i.next();

            if (service.getUuid().equals(BatteryService.batteryServiceID())) {
                _batteryService.setService(peripheral, service);
                Logger.log(TAG, "_retrievedCharacteristics , _peripheral(%s), batteryService", _peripheral.address());
            }
            else if (service.getUuid().equals(DeviceInformationService.deviceInforamtionServiceID())) {
                _deviceInfoService.setService(peripheral, service);
                Logger.log(TAG, "_retrievedCharacteristics , _peripheral(%s), deviceInfoService", _peripheral.address());
            }

        }

        //
    }

    protected void _readCharacteristic(BlePeripheral peripheral, BluetoothGattCharacteristic characteristic, byte[] value) {
        if (peripheral != _peripheral || peripheral == null) {
            return;
        }

        // read value for this characteristic
        if (characteristic.getService() == _deviceInfoService.service()) {
            Logger.log("_readCharacteristic , _peripheral(%s), _deviceInfoService", _peripheral.address());
            _deviceInfoService.readCharacteristic(characteristic, value);
        }
        else if (characteristic.getService() == _batteryService.service()) {
            Logger.log("_readCharacteristic , _peripheral(%s), _batteryService", _peripheral.address());
            _batteryService.readCharacteristic(characteristic, value);
        }
        else if (characteristic.getService() == _batteryControlService.service()) {
            Logger.log("_readCharacteristic , _peripheral(%s), _batteryControlService", _peripheral.address());
            _batteryControlService.readCharacteristic(characteristic, value);
        }


    }

    protected void _updateBatteryLevel(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            _batteryLevel = _batteryService.batteryLevel;

            EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
        }
    }

    protected void _rssiUpdated(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
        }
    }

    protected void _readDeviceSerialNumber(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            Logger.log("_readDevicesSerialNumber (%s)", peripheral.address());
        }
    }

    protected void _updateDischargeUsb1(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            usb1Voltage = _batteryControlService.usb1Voltage;
            usb1Current = _batteryControlService.usb1Current;

            EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
        }
    }

    protected void _updateDischargeUsb2(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            usb2Voltage = _batteryControlService.usb2Voltage;
            usb2Current = _batteryControlService.usb2Current;

            EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
        }
    }

    protected void _updateBatteryCharge(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            battChargeVoltage = _batteryControlService.battChargeVoltage;
            battChargeCurrent = _batteryControlService.battChargeCurrent;

            EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
        }
    }

    protected void _updateBatteryQuality(BlePeripheral peripheral) {
        if (peripheral == _peripheral) {
            quality = _batteryControlService.quality;

            EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
        }
    }







}

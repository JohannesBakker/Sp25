package com.john.chargemanager.MicroService.Bluetooth.SP25;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import com.john.chargemanager.MicroService.Bluetooth.Ble.BlePeripheral;
import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
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
    public static final String kSp25DeviceDeviceConnectedNotification = "SP25 device connected notification";
    public static final String kSp25DeviceDisconnectedNotification = "SP25 device disconnected notification";

    protected BlePeripheral _peripheral;
    protected DeviceInformationService _deviceInfoService;
    protected BatteryService _batteryService;

    private int mDeviceState = DEVICE_NOTCONNECTED;
    private int _batteryLevel;

    public Sp25Device(BlePeripheral peripheral) {
        this._peripheral = peripheral;

        _batteryService = new BatteryService();
        _deviceInfoService = new DeviceInformationService();

        EventBus.getDefault().register(this);
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

    public void disconnect() {
        if (mDeviceState == DEVICE_CONNECTING ||
                mDeviceState == DEVICE_CONNECTED ||
                mDeviceState == DEVICE_PERFORMING ) {
            Logger.log(TAG, "Sp25Device.disconnect() mDeviceState(%d) => DEVICE_DISCONNECTED", mDeviceState);
            mDeviceState = DEVICE_DISCONNECTED;
        }
        EventBus.getDefault().unregister(this);
    }

    public void setDeviceState(int deviceState) {
        this.mDeviceState = deviceState;
        EventBus.getDefault().post(new SEvent(kSp25DeviceDataUpdatedNotification, this));
    }

    public void onEventMainThread(SEvent e) {

        // TODO
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







}

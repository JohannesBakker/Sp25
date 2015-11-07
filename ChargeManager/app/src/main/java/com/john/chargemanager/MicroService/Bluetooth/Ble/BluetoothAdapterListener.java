package com.john.chargemanager.MicroService.Bluetooth.Ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.john.chargemanager.MicroService.Bluetooth.Event.SEvent;
import com.john.chargemanager.Utils.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by dev on 10/30/2015.
 */
public class BluetoothAdapterListener extends BroadcastReceiver {

    public static final String TAG = "BluetothAdapterListener";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Logger.log(TAG, "BluetoothAdapter.STATE_OFF");
                    break;

                case BluetoothAdapter.STATE_ON:
                    Logger.log(TAG, "BluetoothAdapter.STATE_ON");
                    break;

                case BluetoothAdapter.STATE_TURNING_OFF:
                    Logger.log(TAG, "BluetoothAdapter.STATE_TURNING_OFF");
                    break;

                case BluetoothAdapter.STATE_TURNING_ON:
                    Logger.log(TAG, "BluetoothAdapter.STATE_TURNING_ON");
                    break;

                default:
                    break;
            }

            Integer intstate = state;
            EventBus.getDefault().post(new SEvent(SEvent.EVENT_BLUETOOTH_STATE_CHANGED, intstate));
        }
    }
}
package com.john.chargemanager.Utils;

import android.app.Application;
import android.content.res.Configuration;

import com.support.radiusnetworks.bluetooth.BluetoothCrashResolver;

/**
 * Created by dev on 11/7/2015.
 */
public class SpApplication extends Application {

    private BluetoothCrashResolver bluetoothCrashResolver = null;

    public static SpApplication _instance = null;
    public static SpApplication sharedInstance() {
        return _instance;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        _instance = this;

        bluetoothCrashResolver = new BluetoothCrashResolver(this);
        bluetoothCrashResolver.start();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public BluetoothCrashResolver getBluetoothCrashResolver() {
        return bluetoothCrashResolver;
    }
}

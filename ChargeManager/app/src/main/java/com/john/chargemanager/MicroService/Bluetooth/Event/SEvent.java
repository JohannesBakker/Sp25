package com.john.chargemanager.MicroService.Bluetooth.Event;

/**
 * Created by dev on 10/30/2015.
 */
public class SEvent {
    public String name;
    public Object object;
    public String title;
    public String msg;

    public SEvent(String name) {
        this.name = name;
        this.title = null;
        this.object = null;
    }

    public SEvent(String name, Object object) {
        this.name = name;
        this.object = object;
    }

    public SEvent(String name, Object object, String msg) {
        this.name = name;
        this.object = object;
        this.title = "";
        this.msg = msg;
    }

    public SEvent(String name, Object object, String msg, String title) {
        this.name = name;
        this.object = object;
        this.title = title;
        this.msg = msg;
    }

    public static final String EVENT_BLUETOOTH_STATE_CHANGED = "bluetooth adapter's state changed";
    public static final String EVENT_NETWORK_STATE_CHANGED = "network adapter's state changed";
}

package com.john.chargemanager.Utils;

/**
 * Created by dev on 10/30/2015.
 */
public class Logger {
    public static final String TAG = "xCharge SP25";

    public static void log(String tag, String format, Object ...args) {
        android.util.Log.w(TAG + ": " + tag, String.format(format, args));
    }

    public static void logError(String tag, String format, Object ...args) {
        android.util.Log.e(TAG + ": " + tag, String.format(format, args));
    }

    public static void e(String tag, String format, Object ...args) {
        logError(tag, format, args);
    }
}

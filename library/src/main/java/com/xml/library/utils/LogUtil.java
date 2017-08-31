package com.xml.library.utils;

import android.util.Log;

public class LogUtil {

    public static final boolean DEBUG = true;

    public static final boolean TEST = false;

    public static final String TAG = "Adlog";

    public static void info(String tag, String message) {

        if (DEBUG) {
            Log.i(tag, message);
        }
    }
    public static void info(String message) {

            Log.i(TAG, message);
    }

}

package com.weather.lock.util;

import android.util.Log;

/**
 * Created by xlc on 2017/2/14.
 */
public class LogUtil {

    private static boolean STATUS = false;

    public static void info(String msg) {
        if (STATUS) Log.i("tool", msg);
    }

    public static void info(String tag, String msg) {
        Log.i(tag, msg);
    }
}

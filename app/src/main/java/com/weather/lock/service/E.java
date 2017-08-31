package com.weather.lock.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.weather.lock.util.LogUtil;

/**
 * Created by xlc on 2017/2/17.
 */
public class E extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        LogUtil.info("接收到广播");

        context.startService(new Intent(context, A.class));

    }
}

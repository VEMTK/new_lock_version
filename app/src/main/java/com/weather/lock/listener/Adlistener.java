package com.weather.lock.listener;

import android.content.Context;

import com.baidu.mobstat.StatService;
import com.google.android.gms.ads.AdListener;

/**
 * Created by xlc on 2017/3/1.
 */
public class Adlistener extends AdListener {

    private Context mContext;

    public Adlistener(Context context)
    {
        this.mContext=context;
    }

    @Override
    public void onAdClosed() {
        super.onAdClosed();
    }

    @Override
    public void onAdLoaded() {
        super.onAdLoaded();
    }
    @Override
    public void onAdOpened() {
        super.onAdOpened();

        StatService.onEvent(mContext, "native_ad", "click_native_ad", 1);

    }
}

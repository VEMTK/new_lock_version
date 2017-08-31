package com.xml.library.ad;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.baidu.mobstat.StatService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.T;
import com.xml.library.services.B;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.SPreferencesUtil;
import com.xml.library.utils.Utils;


/**
 * Created by xlc on 2016/12/28.
 */
public class Ad extends Activity {

    private InterstitialAd mInterstitial;

    public static final String TAG = "Adlog";

    private boolean interstitial_show = false;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void startAdmobActivity(Context context) {

        Intent in = new Intent(context, Ad.class);

        in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        context.startActivity(in);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate: Ad");

        setContentView(new ProgressBar(getApplicationContext()));
        /* 检查网络设置 */
        long first = Utils.getfirstInstallTime(getApplicationContext());

        // 安装后5分装后可以执行广告 5 * 60 * 1000
        if (Math.abs(System.currentTimeMillis() - first) >= 5 * 60 * 1000) {

            LogUtil.info(TAG, "5Min范围内:");

            SPreferencesUtil.getInstance(this).save_long(SPreferencesUtil.IN_ADMOB_TIME, System.currentTimeMillis());

            LogUtil.info(TAG, "进入Asyn:");

            // 执行代码,将广告类型传入

            Asyn as = new Asyn();

            as.executeOnExecutor(HttpUtil.executorService);

        } else {

            LogUtil.info(TAG, "No 5Min");

            Log.i("Alog", "no 5m");

            finish();
        }
    }

    /**
     * .addTestDevice( "DCAB5CE2FDAA606E8D5C55F81B609271") 展示admob
     * ca-app-pub-2330335626434156/8953243225
     */
    public void showAdmob(String adid) {
        try {
            mInterstitial = new InterstitialAd(this);
            mInterstitial.setAdUnitId(adid);
            AdRequest builder = new AdRequest.Builder().build();
            mInterstitial.loadAd(builder);
            /**统计请求次数**/
            StatService.onEvent(getApplicationContext(), "admob_interstitial", "request_admob_interstitial", 1);
            /**开始统计请求时长**/
            StatService.onEventStart(getApplicationContext(), "admob_interstitial", "request_admob_interstitial");

            mInterstitial.setAdListener(new AdListener() {
                @Override
                public void onAdLeftApplication() {
                    // TODO Auto-generated method stub
                    super.onAdLeftApplication();
                    // check save time
                    StatService.onEvent(getApplicationContext(), "admob_interstitial", "click_admob_interstitial", 1);

                    finish();
                }
                @Override
                public void onAdLoaded() {
                    /*****展示成工统计******/
                    StatService.onEvent(getApplicationContext(), "admob_interstitial", "request_admob_interstitial_success", 1);
                    /*****请求结束****/
                    StatService.onEventEnd(getApplicationContext(), "admob_interstitial", "request_admob_interstitial");

                    if (!isFinishing() && !interstitial_show) {

                        showInterstitial();

                        interstitial_show = true;

                        LogUtil.info(TAG, "Admob show");

                        Log.i("Alog", "AdSrc_Show");

                        DataBaseManager.getInstance(getApplicationContext()).update_counts();
                        //show_admob_interstitial
                        /****开始统计展示时长****/
                        StatService.onEventStart(Ad.this, "admob_interstitial", "show_admob_interstitial");
                    }
                    super.onAdLoaded();

                }

                @Override
                public void onAdClosed() {
                    // TODO Auto-generated method stub
                    super.onAdClosed();
                    if (mInterstitial != null) {

                        mInterstitial = null;
                    }
                    /******关闭插屏事件统计*******/
                    StatService.onEvent(getApplicationContext(), "admob_interstitial", "close_admob_interstitial", 1);

                    finish();
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);

                    LogUtil.info(B.TAG, "Admob onAdFailedToLoad:" + errorCode);
                    Log.i("Alog", "adMsc_fail:" + errorCode);

                    /******请求失败******/
                    StatService.onEvent(getApplicationContext(), "admob_interstitial", "request_admob_interstitial_fail", 1);

                    /*****请求结束****/
                    StatService.onEventEnd(getApplicationContext(), "admob_interstitial", "request_admob_interstitial");

                    finish();

                }

                @Override
                public void onAdOpened() {
                    // TODO Auto-generated method stub
                    super.onAdOpened();

                }

            });

        } catch (Exception e) {

            Log.i("Alog", "" + e.getMessage());

            finish();
        }
    }

    /**
     * @author
     */
    class Asyn extends AsyncTask<Void, Integer, T> {

        @Override
        protected T doInBackground(Void... params) {

            return DataBaseManager.getInstance(getApplicationContext()).get_setData(0);
        }

        @Override
        protected void onPostExecute(T t) {
            // TODO Auto-generated method stub
            super.onPostExecute(t);

            LogUtil.info(B.TAG, "onPostExecute>>t：" + t);

            if (t == null) {
                return;
            }
            LogUtil.info(B.TAG, "onPostExecute>>t.getAtype：" + t.getAtype());

            LogUtil.info(B.TAG, "onPostExecute>>t.getAid：" + t.getAid());

            showAdmob(t.getAid());
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        LogUtil.info("Adlog", "onDestroy");

        if (mInterstitial != null) {

            mInterstitial = null;
        }
        if (interstitial_show) {

            StatService.onEventEnd(Ad.this, "admob_interstitial", "show_admob_interstitial");
        }
    }

    public void showInterstitial() {

        if (mInterstitial != null && mInterstitial.isLoaded()) {

            StatService.onEvent(getApplicationContext(), "show_admob", "show_admob_interstitial", 1);

            mInterstitial.show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.info("Adlog","屏幕改变");
    }


}

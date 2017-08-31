package com.weather.lock;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.Settings;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.weather.lock.listener.Adlistener;
import com.weather.lock.task.CheckAppThemeTask;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.SharedUtil;
import com.weather.lock.util.ToolUtil;
import com.weather.lock.util.Util;
import com.weather.lock.view.BlurringView;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.T;

import java.lang.reflect.Method;

/**
 * Created by xlc on 2017/2/6.
 */
public class ToolActivity extends Activity {

    private ImageView wifi_img, data_img, blue_img, shake_img, mute_img, gps_img, hot_img, light_status;

    private ToolUtil toolUtil;

    private DataObserver dataObserver;

    private AudioManager audioMa = null;

    private boolean has_voice = false;

    private boolean has_vibrate = false;

    private TextView wifi_text;

    private ImageView imageView;

    private LinearLayout show_ad_layout;

    private BlurringView mBlurringView;

    private int index = 0;

    private int[] settimes = {30000, 60000, 2 * 60000, 5 * 60000, 10 * 60000};

    private int[] res = {R.drawable.close_s_30s, R.drawable.close_s_1m, R.drawable.close_s_2m
            , R.drawable.close_s_5m, R.drawable.close_s_10m};

    /**
     * 控件剩余高度
     */
    private int layout_height_dp = 0;


    private MyApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (MyApp) ToolActivity.this.getApplication();

        int status=SharedUtil.getInstance(this).get_int(SharedUtil.APP_THEME_STAUS,0);

        if (status==1) {
            setTheme(R.style.wallpaper_theme);
        } else {
            setTheme(R.style.App_Theme_1);
        }

        Util.setSystemBar(this);

        setContentView(R.layout.tool_activity);

        registerReceiver();

        index = SharedUtil.getInstance(this).get_int(SharedUtil.CLOSE_SCREEN_INDEX, 1);

        audioMa = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        toolUtil = ToolUtil.getInstance(getApplicationContext());

        initView();

        registered_ContentObserver();

       // new GetAdTask().execute();

        if (status==0) imageView.setImageBitmap(Util.getWallpapperBitmap(this));

        new CheckAppThemeTask(ToolActivity.this).execute();

    }

    private NativeExpressAdView adView = null;

    private class GetAdTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... params) {

            T id2 = DataBaseManager.getInstance(getApplicationContext()).get_setData(2);

            if (id2 != null) {

                LogUtil.info("id2.getAid():" + id2.getAid());

                return id2.getAid();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LogUtil.info("lock", "toolActivity get native ad_id :" + s);

            LogUtil.info("tool activity 获取广告id:" + s);

            if (!TextUtils.isEmpty(s)) {

                if (adView == null || !adView.isShown()) {

                    adView = new NativeExpressAdView(getApplicationContext());

                    int ad_height = 0;

                    int h = getResources().getInteger(R.integer.ad_height);

                    if (h > layout_height_dp) ad_height = layout_height_dp;
                    else ad_height = h;

                    AdSize adSize = new AdSize(AdSize.FULL_WIDTH, ad_height);

                    adView.setAdSize(adSize);

                    adView.setAdUnitId(s);

                    AdRequest request = new AdRequest.Builder()
                            .addTestDevice("35F243B58C5EAE1C5A3666121893A9EE")
                            .addTestDevice("F395ADF17442461AF73850D3E041E5B3")
                            .build();

                    adView.loadAd(request);

                    adView.setAdListener(new Adlistener(getApplicationContext()));

                    show_ad_layout.addView(adView);

                }

            }
        }
    }


    /**
     * 震动
     */
    private void vibrate() {
        audioMa.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
    }

    /**
     * 没声音没震动
     */
    private void noRingAndVibrate() {
        audioMa.setRingerMode(AudioManager.RINGER_MODE_SILENT);

    }

    private void mode_nomal() {
        audioMa.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

    }

    /**
     * 震动
     */
    private void click_vibrator() {

        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        vibrator.vibrate(100);
    }

    private void outAnimation(final ImageView view) {

        ScaleAnimation z_animation = new ScaleAnimation(0.6f, 1f, 0.6f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        z_animation.setDuration(150);

        AnimationSet mAnimationSet = new AnimationSet(false);

        mAnimationSet.addAnimation(z_animation);

        view.startAnimation(mAnimationSet);

        z_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                switch (view.getId()) {

                    case R.id.wifi_status:

                        if (toolUtil.getWifiapStatus()) {

                            toolUtil.setWifiApEnabled();
                        }
                        toolUtil.changeWifiStatus();

                        wifi_img.setImageResource(R.drawable.tool_wifi_on);

                        break;

                    case R.id.data_status:

                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                            Intent intent = new Intent("android.settings.DATA_ROAMING_SETTINGS");
                            ComponentName clean = new ComponentName("com.android.settings", "com.android.settings.Settings$DataUsageSummaryActivity");
                            intent.setComponent(clean);
                            startActivity(intent);
                        } else {

                            if (toolUtil.getMobileDataState(null))
                                toolUtil.setMobileDataStatus(false);
                            else toolUtil.setMobileDataStatus(true);
                        }
                        break;
                    case R.id.blue_status:
                        toolUtil.setBlueStatus();
                        break;
                    case R.id.shake_status:
                        if (!has_vibrate) {
                            has_vibrate = true;
                            if (has_voice) {
                                mode_nomal();
                            } else {
                                vibrate();
                            }
                            click_vibrator();
                            shake_img.setImageResource(R.drawable.tool_shake_on);
                        } else {
                            has_vibrate = false;
                            if (!has_voice) noRingAndVibrate();
                            else mode_nomal();
                            shake_img.setImageResource(R.drawable.tool_shake_off);
                        }
                        break;

                    case R.id.mute_status:
                        if (!has_voice) {
                            has_voice = true;
                            mode_nomal();
                            mute_img.setImageResource(R.drawable.tool_ringer_on);
                        } else {
                            has_voice = false;
                            if (has_vibrate) {
                                vibrate();
                            } else {
                                noRingAndVibrate();
                            }
                            mute_img.setImageResource(R.drawable.tool_ringer_off);
                        }
                        break;

                    case R.id.gps_status:
                        to_setGPS();
                        break;

                    case R.id.hot_status:

                        toolUtil.setWifiApEnabled();
                        break;

                    case R.id.light_status:
                        index = index + 1;
                        if (index > res.length - 1) index = 0;
                        light_status.setImageResource(res[index]);
                        setScreenOffTime(settimes[index]);
                        break;
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {

        /**********毛玻璃效果************/
        mBlurringView = (BlurringView) findViewById(R.id.blurring_view);

        View blurredView = findViewById(R.id.blurred_view);

        // Give the blurring view a reference to the blurred view.
        mBlurringView.setBlurredView(blurredView);

        /**********毛玻璃效果结束************/

        imageView = (ImageView) findViewById(R.id.tool_back);

        wifi_text = (TextView) findViewById(R.id.wifi_text);

        wifi_img = (ImageView) findViewById(R.id.wifi_status);

        if (toolUtil.getWifiStatus()) {
            wifi_img.setImageResource(R.drawable.tool_wifi_on);
        } else {
            wifi_img.setImageResource(R.drawable.tool_wifi_off);
        }

        data_img = (ImageView) findViewById(R.id.data_status);

        if (toolUtil.getMobileDataState(null)) {
            data_img.setImageResource(R.drawable.tool_data_on);
        } else {
            data_img.setImageResource(R.drawable.tool_data_off);
        }

        blue_img = (ImageView) findViewById(R.id.blue_status);
        if (toolUtil.getBlueStatus()) blue_img.setImageResource(R.drawable.tool_blue_on);
        else blue_img.setImageResource(R.drawable.tool_blue_off);

        shake_img = (ImageView) findViewById(R.id.shake_status);

        mute_img = (ImageView) findViewById(R.id.mute_status);

        LogUtil.info("audioMa.getRingerMode():" + audioMa.getRingerMode());

        switch (audioMa.getRingerMode()) {
            /**
             * 只有震动
             */
            case AudioManager.RINGER_MODE_VIBRATE:
                shake_img.setImageResource(R.drawable.tool_shake_on);
                mute_img.setImageResource(R.drawable.tool_ringer_off);
                has_vibrate = true;
                has_voice = false;
                break;
            /***
             * 无震动无声音
             */
            case AudioManager.RINGER_MODE_SILENT:
                mute_img.setImageResource(R.drawable.tool_ringer_off);
                shake_img.setImageResource(R.drawable.tool_shake_off);
                has_vibrate = false;
                has_voice = false;

                break;
            /**
             * 两者都有
             */
            case AudioManager.RINGER_MODE_NORMAL:
                has_voice = true;
                has_vibrate = true;
                mute_img.setImageResource(R.drawable.tool_ringer_on);
                shake_img.setImageResource(R.drawable.tool_shake_on);
                break;
        }
        gps_img = (ImageView) findViewById(R.id.gps_status);

        hot_img = (ImageView) findViewById(R.id.hot_status);

        if (toolUtil.getWifiapStatus()) {
            hot_img.setImageResource(R.drawable.tool_hot_on);
        } else {
            hot_img.setImageResource(R.drawable.tool_hot_off);
        }

        light_status = (ImageView) findViewById(R.id.light_status);

        light_status.setImageResource(res[index]);

        setScreenOffTime(settimes[index]);

        show_ad_layout = (LinearLayout) findViewById(R.id.tool_show_ad);

        ViewTreeObserver vto = show_ad_layout.getViewTreeObserver();

        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            public boolean onPreDraw() {

                float height = show_ad_layout.getMeasuredHeight();

                layout_height_dp = px2dip(height);

                return true;
            }
        });


    }

    public int px2dip(float pxValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 点击事件
     *
     * @param view
     */
    public void toolonClick(View view) {

        switch (view.getId()) {

            case R.id.tool_back_:
                finish();
                this.overridePendingTransition(0, R.anim.fade_out);
                break;
            case R.id.wifi_status:
            case R.id.data_status:
            case R.id.blue_status:
            case R.id.shake_status:
            case R.id.mute_status:
            case R.id.gps_status:
            case R.id.hot_status:
            case R.id.light_status:
                outAnimation((ImageView) view);
                break;
        }

    }

    /**
     * 设置息屏时间
     *
     * @param paramInt
     */
    private void setScreenOffTime(int paramInt) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }


    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//wifi开关
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//蓝牙
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);//情景模式

        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");

        registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {

                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;

                    NetworkInfo.State state = networkInfo.getState();

                    boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    if (isConnected) {
                        //开启
                        wifi_img.setImageResource(R.drawable.tool_wifi_on);

                        wifi_text.setText(getConnectWifiSsid().replaceAll("\"", ""));

                    } else {

                        if (toolUtil.isWifi_on()) {
                            wifi_img.setImageResource(R.drawable.tool_wifi_loading);
                        } else {
                            wifi_img.setImageResource(R.drawable.tool_wifi_off);
                        }
                        //关闭
                        wifi_text.setText("WI-FI");
                    }
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                    case BluetoothAdapter.STATE_ON:
                        blue_img.setImageResource(R.drawable.tool_blue_on);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                    case BluetoothAdapter.STATE_OFF:
                        blue_img.setImageResource(R.drawable.tool_blue_off);
                        break;
                }
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                //便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
                int state = intent.getIntExtra("wifi_state", 0);
                if (state == 11) {
                    hot_img.setImageResource(R.drawable.tool_hot_off);
                } else if (state == 13) {
                    hot_img.setImageResource(R.drawable.tool_hot_on);
                }

            }

        }
    };

    /***
     * 获取连接到wifi的节点名称
     *
     * @return
     */
    private String getConnectWifiSsid() {
        @SuppressLint("WifiManagerLeak") WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    private class DataObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public DataObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            LogUtil.info("数据流量 状态变化");

            if (toolUtil.getMobileDataState(null)) {
                data_img.setImageResource(R.drawable.tool_data_on);
            } else {
                data_img.setImageResource(R.drawable.tool_data_off);
            }
        }
    }

    private void registered_ContentObserver() {
        dataObserver = new DataObserver(
                new Handler());
        getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor("mobile_data"), false, dataObserver);

    }

    private void unRegistered() {
        unregisterReceiver(broadcastReceiver);
        getContentResolver().unregisterContentObserver(dataObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adView != null) {

            adView.destroy();

            adView = null;
        }

        SharedUtil.getInstance(this).save_int(SharedUtil.CLOSE_SCREEN_INDEX, index);

        unRegistered();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(0, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isGPSEnable(getApplicationContext())) {

            gps_img.setImageResource(R.drawable.tool_gps_on);
        } else {
            gps_img.setImageResource(R.drawable.tool_gps_off);
        }
    }

    private void to_setGPS() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private boolean isGPSEnable(Context context) {

        String str = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.v("GPS", str);
        if (str != null) {
            return str.contains("gps");
        } else {
            return false;
        }
    }


}

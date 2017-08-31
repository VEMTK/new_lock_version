package com.weather.lock.service;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.weather.lock.MainActivity;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.NotificationUtil;
import com.weather.lock.util.SharedUtil;
import com.weather.lock.util.Util;

import java.util.List;

/**
 * Created by xlc on 2017/2/4.
 */
public class A extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private NotificationUtil notificationUtil = null;

    private static final String TAG = "tool";

    private final int notid = TAG.hashCode();

    private SharedUtil sharedUtil;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.info(" 服务onCreate");

        sharedUtil = SharedUtil.getInstance(this);

        registBroadcastReceiver();

        notificationUtil = NotificationUtil.getInstance(this);

        int weather_code = sharedUtil.get_int(SharedUtil.WEATHER_CODE, -1);

        int weather_low = sharedUtil.get_int(SharedUtil.WEATHER_LOW, 100);

        int weather_height = sharedUtil.get_int(SharedUtil.WEATHER_HEIGHT, 100);

        String temp_low_height = Util.getTempLowToHeight(this, weather_low, weather_height);

        int icon_res = Util.getWeatherIconWithCode(weather_code);

        String current_temp = Util.getCurrent_temp(this, sharedUtil.get_int(SharedUtil.CURRENT_TEMP, 100));

        String location_name = sharedUtil.get_string(SharedUtil.K_LOCATION, "...");

        Notification notification = notificationUtil.updateNotification(icon_res, location_name, !TextUtils.isEmpty(current_temp) ? current_temp : "?", !TextUtils.isEmpty(temp_low_height) ? temp_low_height : "N/A");

        startForeground(notid, notification);

        // hiddenIcon();
    }

    private void hiddenIcon() {
        PackageManager packageManager = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class);
        int res = packageManager.getComponentEnabledSetting(componentName);
        if (res == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || res == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            // 隐藏应用图标

            Log.e("tool", "隐藏图标");
            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        } else {
            // 显示应用图标
            //  Log.e("tool","显示图标");
//            packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
//                    PackageManager.DONT_KILL_APP);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != broadcastReceiver) {
            try {
                getApplicationContext().unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        startService(new Intent(this, A.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogUtil.info(" 服务onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    private void registBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();

        /**********开锁屏*******/
        // filter.addAction(Intent.ACTION_SCREEN_ON);

        filter.addAction(Intent.ACTION_SCREEN_OFF);

        filter.setPriority(Integer.MAX_VALUE);

        //filter.addAction(Intent.ACTION_POWER_CONNECTED);

        //更换壁纸广播
      //  filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);

        getApplicationContext().registerReceiver(broadcastReceiver, filter);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            LogUtil.info("屏幕黑亮广播：" + intent.getAction());

            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent1);//跳转到主界面
        }
    };

    public static boolean isClsRunning(String pkg, String cls, Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        ActivityManager.RunningTaskInfo task = tasks.get(0);
        if (task != null) {
            return TextUtils.equals(task.topActivity.getPackageName(), pkg) && TextUtils.equals(task.topActivity.getClassName(), cls);
        }
        return false;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String able = getResources().getConfiguration().locale.getCountry();

        if (("EG,IR,XB").contains(able)) {
            SharedUtil.getInstance(this).save_int(SharedUtil.LAYOUT_LEFT_RIGHT, 1);
        } else {
            SharedUtil.getInstance(this).save_int(SharedUtil.LAYOUT_LEFT_RIGHT, 0);
        }

    }

    public boolean check_screen_status()
    {

        KeyguardManager mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        return  mKeyguardManager.inKeyguardRestrictedInputMode();
    }


}

package com.weather.lock;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import com.marswin89.marsdaemon.DaemonApplication;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.weather.lock.service.Receiver1;
import com.weather.lock.service.Receiver2;
import com.weather.lock.service.Service1;
import com.weather.lock.service.Service2;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.SharedUtil;
import com.xml.library.db.DataBaseManager;

import java.util.List;


/**
 * Created by xlc on 2016/12/14.
 */
public class MyApp extends DaemonApplication {


    @Override
    public void onCreate() {
        super.onCreate();

        String curProcessName = getProcessName(this,android.os.Process.myPid());

        if (!curProcessName.equals(getPackageName())) {
            return;
        }
        long currenttime= System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(getApplicationContext(), Service1.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startService(intent);

                init_status_data();

            }
        }).start();

        Log.e("lock","Application中onCreate时间："+(System.currentTimeMillis()-currenttime));

    }

    public  String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }


    private void init_status_data() {

        String able = getResources().getConfiguration().locale.getCountry();

        LogUtil.info("获取本机语言国家代码：" + able);

        if (("EG,IR,XB").contains(able)) {
            SharedUtil.getInstance(this).save_int(SharedUtil.LAYOUT_LEFT_RIGHT, 1);
        } else {
            SharedUtil.getInstance(this).save_int(SharedUtil.LAYOUT_LEFT_RIGHT, 0);
        }

        if (SharedUtil.getInstance(this).get_int(SharedUtil.USER_SET_TEMP_UNIT, 0) == 0) {

            LogUtil.info("用户没有设置温度单位：");

            LogUtil.info("获取本机语言国家代码：" + able);

            String county_list = "US,MM,LY,IN,ID";

            if (county_list.contains(able)) {

                LogUtil.info("设置默认为华摄氏度：");

                SharedUtil.getInstance(this).save_int(SharedUtil.TEMP_STATUS, 1);
            } else {
                LogUtil.info("设置默认为摄氏度：");
                SharedUtil.getInstance(this).save_int(SharedUtil.TEMP_STATUS, 0);
            }
        } else {

            LogUtil.info("用户已经设置了温度单位：");
        }
    }

    /**
     * you can override this method instead of {@link android.app.Application attachBaseContext}
     *
     * @param base
     */
    @Override
    public void attachBaseContextByDaemon(Context base) {
        super.attachBaseContextByDaemon(base);
    }


    /**
     * give the configuration to lib in this callback
     *
     * @return
     */
    @Override
    protected DaemonConfigurations getDaemonConfigurations() {
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                "com.weather.lock:process1",
                Service1.class.getCanonicalName(),
                Receiver1.class.getCanonicalName());

        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                "com.weather.lock:process2",
                Service2.class.getCanonicalName(),
                Receiver2.class.getCanonicalName());

        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }

    class MyDaemonListener implements DaemonConfigurations.DaemonListener {
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
        }
    }


}

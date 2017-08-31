package com.xml.library.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.xml.library.ad.Ab;
import com.xml.library.ad.Ad;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.T;
import com.xml.library.utils.HttpUtil;
import com.xml.library.utils.LogUtil;
import com.xml.library.utils.RUtil;
import com.xml.library.utils.SPreferencesUtil;
import com.xml.library.utils.Utils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Filter;

/**
 * Created by xlc on 2016/12/29.
 */
public class A extends Service {

    private static final String TAG = "Adlog";

    private int type = 0;

    private B b = null;

    private Ab abHander = null;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);

    private int[] admobArrays;


    private static final String ACTION_ALART_ADMOBBANER = "action_admobbanner.";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();

        registerReceiver();

        Log.i(TAG, "onCreate: ");


        Utils.create_device_id(getApplicationContext());

        b = new B(this);

        abHander = new Ab(this);

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(new MyRunnable(), 0, 1000, TimeUnit.MILLISECONDS);

        /****** MyHelpUtil.checkScreenNum(getApplicationContext()) <=* 400)超过400次不同步联网*/
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand: ");

        F f = new F(this, type);

        f.executeOnExecutor(HttpUtil.executorService);

        init_proportion();

        canNotTop();

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unRegisterRecevier();

    }

    public void canNotTop() {


        if (type != B.NONE) return;

        showAd(B.ADMOB, 0);
    }

    class MyRunnable implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub

            if (b == null) return;

            type = b.onStartCommand();

            Log.i("top", "type:" + type);

            if (type != B.NONE) {

                showAd(type, 1);
            }
        }
    }
    /***
     * 显示广告 0:banner 1:SCREEN 2:ADMOB大廣告平台
     *
     * @param type
     */
    private void showAd(final int type, final int banner) {

        HttpUtil.executorService.execute(new Runnable() {
            @Override
            public void run() {

                LogUtil.info("top", "显示广告操作");

                if (Utils.check_black_list(getApplicationContext()) == -1) {
                    if (type != B.CLEAR && type != B.SAME_CN)
                        LogUtil.info("Adlog", "黑名单或网络异常不展示广告");
                    return;
                }
                switch (type) {

                    case B.ADMOB:

                        if (!check_ad_conditions()) break;

                        if (RUtil.get_proportion(admobArrays) == 0) {

                            Log.i("Alog", "in AdScr");

                            LogUtil.info("Adlog", "进入插屏广告逻辑");

                            Ad.startAdmobActivity(getApplicationContext());
                        } else {
                            LogUtil.info("Adlog", "进入banner广告逻辑");

                            if (banner == 0) {
                                setAlarmTime(getApplicationContext(), ACTION_ALART_ADMOBBANER + getPackageName(), 10000);
                            } else {
                                Ab.sendMsg(abHander, banner, B.ADMOB_BANNER);
                            }
                        }
                        break;
                    case B.CLEAR:// 9
                        Ab.sendMsg(abHander, banner, B.CLEAR);
                        break;
                }
            }
        });
    }


    /**
     * 获取不到最上层 banner的定时显示
     **/
    private void setAlarmTime(Context context, String str_type, long timeInMillis) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(str_type);

        PendingIntent sender = PendingIntent.getBroadcast(

                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + timeInMillis, sender);
    }


    /***
     * 判断显示广告条件
     *
     * @return
     */
    private boolean check_ad_conditions() {

        if (admobArrays == null) return false;

        T t0 = DataBaseManager.getInstance(getApplicationContext()).get_setData(0);

        if (t0 == null) return false;

        boolean res = DataBaseManager.getInstance(getApplicationContext()).check_status(t0.getIn(), t0.getC());

        boolean check_time = Utils.checkAdtime_in_2min(getApplicationContext());

        return res && check_time;
    }

    /***
     * 初始化配置比例
     */
    public void init_proportion() {

        // TODO Auto-generated method stub
        T t = DataBaseManager.getInstance(this).get_setData(1);

        if(t==null) return;

        LogUtil.info("Adlog", "服务器中配置的比例：" + t.getAid().trim());

        Log.i("Alog", "service proportion：" + t.getAid().trim());

        String msg = t.getAid().trim();

        if (TextUtils.isEmpty(msg)) {

            msg = "900550|22";
        }
        String randMsg = msg.substring(0, msg.indexOf("|"));

        String sendTime = msg.substring(msg.indexOf("|") + 1);

        Log.i("Adlog", "proportion:" + randMsg);

        Log.i("Adlog", "sendTime_proportion:" + sendTime);

        admobArrays = new int[3];

        String admob = randMsg.substring(3, randMsg.length());

        for (int i = 0; i < admobArrays.length; i++) {

            admobArrays[i] = Integer.parseInt(admob.substring(i, i + 1));
        }
    }


    public void registerReceiver() {

        IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_SCREEN_ON);

        filter.addAction(Utils.LOCK_WEATHER_ACTION);

        filter.addAction(ACTION_ALART_ADMOBBANER + getPackageName());

        filter.setPriority(Integer.MAX_VALUE);


        registerReceiver(broadcastReceiver, filter);

    }

    private void unRegisterRecevier() {
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LogUtil.info("Adlog", "插件A服务中获取到广播：" + intent.getAction());

            if (intent.getAction().equals(Utils.LOCK_WEATHER_ACTION)) {


                Intent intent1 = new Intent(getApplicationContext(), A.class);

                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startService(intent1);


            } else if (intent.getAction().equals(ACTION_ALART_ADMOBBANER + getPackageName())) {

                LogUtil.info("Adlog", "获取不到最上层 10秒后执行banner");

                Ab.sendMsg(abHander, 0, B.ADMOB_BANNER);

            } else {

                SPreferencesUtil sharedUtil = SPreferencesUtil.getInstance(context);

                int counts = sharedUtil.get_int(SPreferencesUtil.SCREEN_STATUS_COUNTS, 0) + 1;

                LogUtil.info("Adlog", "解锁次数：" + counts);

                Log.i("Alog", "screen counts :" + counts);

                sharedUtil.save_int(SPreferencesUtil.SCREEN_STATUS_COUNTS, counts);

            }


        }
    };


}

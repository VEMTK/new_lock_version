package com.weather.lock.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.NativeExpressAdView;
import com.weather.lock.R;
import com.weather.lock.SettingActivity;
import com.weather.lock.ToolActivity;
import com.weather.lock.clean.ShortCutActivity;
import com.weather.lock.listener.Adlistener;
import com.weather.lock.listener.CameraClicklistener;
import com.weather.lock.listener.ScrollListener;
import com.weather.lock.listener.UnlockListener;
import com.weather.lock.service.LocationSvc;
import com.weather.lock.util.GetYahooWeatherSaxTools;
import com.weather.lock.util.LogUtil;
import com.weather.lock.util.NotificationUtil;
import com.weather.lock.view.PagerLayout;
import com.weather.lock.util.SharedUtil;
import com.weather.lock.util.ToolUtil;
import com.weather.lock.util.Util;
import com.xml.library.db.DataBaseManager;
import com.xml.library.modle.T;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class MainFragment extends Fragment implements UnlockListener, View.OnClickListener {

    private FrameLayout layout;

    private ImageView battery_change, battery_img;

    private TextView time, week, temp_text, battery_much, memory_text;

    private ImageView weather_img, clean_img;

    private LinearLayout battery_layout, weather_layout;

    private RelativeLayout camera, tool_img;

    private ImageView flash_img;

    private TextView flash_text;

    private Calendar c = Calendar.getInstance();

    private boolean flash_status = false;

    private SharedUtil sharedUtil = null;

    private LinearLayout show_ad_layout, bottom_view;

    private PagerLayout base_relative;

    private NotificationUtil notificationUtil = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        LogUtil.info("onCreateView");

        long time = System.currentTimeMillis();

        View view = inflater.inflate(R.layout.activity_main, container, false);

        initView(view);

        init_data();

        Log.e("lock", "MainFragment onCreateView 耗时:" + Math.abs(System.currentTimeMillis() - time));

        return view;
    }

    /***
     * 根据城市代码判断当天是否查询
     */
    @SuppressLint("SimpleDateFormat")
    private void executeYahooData() {

        String cid_code = SharedUtil.getInstance(getActivity()).get_string(SharedUtil.LOCATION_CODE, "");

        LogUtil.info("cid_code:" + cid_code + "");

        if (TextUtils.isEmpty(cid_code)) return;

        LogUtil.info("查询" + cid_code + "的天气");

        new SaxWeatherAsync().execute(new String[]{cid_code});

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long time=System.currentTimeMillis();

        getActivity().registerReceiver(mBatInfoReveiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));

        notificationUtil = NotificationUtil.getInstance(getActivity());

        StatService.onEvent(getActivity(), "lock_screen", "lock_screen", 1);

        getActivity().overridePendingTransition(R.anim.fade_in, 0);

        sharedUtil = SharedUtil.getInstance(getActivity());

        startBroadcastReceiver();

        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));


        LogUtil.info("MainFragment onCreate");


        Log.e("lock","MainFragment onCreate 耗时："+(System.currentTimeMillis()-time));

    }

    @Override
    public void onResume() {

        super.onResume();

        long time =System.currentTimeMillis();

        if (Math.abs(System.currentTimeMillis() - sharedUtil.get_long("execute_location_time", 0)) > 4000) {

            sharedUtil.save_long("execute_location_time", System.currentTimeMillis());

            init_data();

            Log.e("lock","init_data：");

           // new GetAdTask().execute();
        }

        Log.e("lock","MainFragment onResume 耗时："+(System.currentTimeMillis()-time));

    }

    private void init_data() {

//        KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
//
//        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");
//
//        keyguardLock.reenableKeyguard();
//
//        keyguardLock.disableKeyguard();//解锁系统锁屏

        int weather_code = sharedUtil.get_int(SharedUtil.WEATHER_CODE, -1);

        int weather_low = sharedUtil.get_int(SharedUtil.WEATHER_LOW, 100);

        int weather_height = sharedUtil.get_int(SharedUtil.WEATHER_HEIGHT, 100);

        weather_img.setImageResource(Util.getWeatherIconWithCode(weather_code));

        String temp_l_h = Util.getTempLowToHeight(getActivity(), weather_low, weather_height);

        if (!TextUtils.isEmpty(temp_l_h)) temp_text.setText(temp_l_h);

        memory_text.setText(String.format(getResources().getString(R.string.boost_memory_footprint_high), Util.getUsedPercentValue(getActivity())));


        if (sharedUtil.get_int(SharedUtil.USER_SER_LOACTION, 0) == 0) {

            LogUtil.info("用户没有设置地址");

            LogUtil.info("lock", "user not set location");

            getActivity().startService(new Intent(getActivity(), LocationSvc.class));

        } else {
            executeYahooData();
        }

        base_relative.renew_layout();
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("SetTextI18n")
    private void initView(View view) {

        tool_img = (RelativeLayout) view.findViewById(R.id.tool_img);

        tool_img.setOnClickListener(this);

        camera = (RelativeLayout) view.findViewById(R.id.camera);

        weather_layout = (LinearLayout) view.findViewById(R.id.weather_layout);

        weather_layout.setOnClickListener(this);

        battery_layout = (LinearLayout) view.findViewById(R.id.battery_layout);

        temp_text = (TextView) view.findViewById(R.id.temp);

        weather_img = (ImageView) view.findViewById(R.id.weather_img);

        layout = (FrameLayout) view.findViewById(R.id.layout);

        battery_change = (ImageView) view.findViewById(R.id.battery_change);

        battery_img = (ImageView) view.findViewById(R.id.battery_img);

        time = (TextView) view.findViewById(R.id.current_time);

        week = (TextView) view.findViewById(R.id.current_week);

        week.setText(formatTime(getActivity(), System.currentTimeMillis()));

        //时间的显示 判断是否为24小时制或12小时制

        set_time_value();

        time.setMovementMethod(LinkMovementMethod.getInstance());

        flash_img = (ImageView) view.findViewById(R.id.flashlight_img);

        flash_img.setOnClickListener(this);

        flash_text = (TextView) view.findViewById(R.id.flashlight_text);

        battery_much = (TextView) view.findViewById(R.id.battery_much);

        memory_text = (TextView) view.findViewById(R.id.memory_size);

        clean_img = (ImageView) view.findViewById(R.id.clean_img);

        clean_img.setOnClickListener(this);

        show_ad_layout = (LinearLayout) view.findViewById(R.id.show_ad_layout);

        base_relative = (PagerLayout) view.findViewById(R.id.main_layout);

        /***
         * 界面滑动事件回传
         */
        base_relative.setScrollListener(new ScrollListener() {
            @Override
            public void scrollTop() {

                to_camera();
            }
        });
        /**
         * camera 点事件回传
         */
        base_relative.setClicklistener(new CameraClicklistener() {
            @Override
            public void click_camera() {
                setAnimation(base_relative);
            }

            @Override
            public void click_down() {
                camera.setBackgroundResource(R.drawable.shape_row_gray);
            }

            @Override
            public void click_up() {
                camera.setBackgroundResource(R.drawable.shape_row_light);
            }
        });
        bottom_view = (LinearLayout) view.findViewById(R.id.bottom_view);

        if (sharedUtil.get_int(SharedUtil.LAYOUT_LEFT_RIGHT, 0) == 1) {

            FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.battery_batter);

            Drawable drawable = Util.rotateDrawable(getActivity(), R.drawable.battery_big);

            frameLayout.setBackground(drawable);

        }
    }

    private NativeExpressAdView adView;

    private class GetAdTask extends AsyncTask<Void, Integer, String> {

        @Override
        protected String doInBackground(Void... params) {

            if (getActivity() == null) return null;

            synchronized (this) {

                Log.e("tool","获取广告id");

                T id2 = DataBaseManager.getInstance(getActivity()).get_setData(2);

                if (null == id2) {
                    return null;
                } else {
                    return id2.getIid();
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LogUtil.info("获取广告id:" + s);

            if (!TextUtils.isEmpty(s) && getActivity() != null) {

                if (adView == null || !adView.isShown()) {

                    show_ad_layout.removeAllViews();

                    adView = new NativeExpressAdView(getActivity());

                    AdSize adSize = new AdSize(AdSize.FULL_WIDTH, getResources().getInteger(R.integer.ad_height));

                    adView.setAdSize(adSize);

                    adView.setAdUnitId(s);

                    AdRequest request = new AdRequest.Builder()
                            .addTestDevice("35F243B58C5EAE1C5A3666121893A9EE")
                            .build();
                    adView.loadAd(request);

                    adView.setAdListener(new Adlistener(getActivity()));

                    show_ad_layout.addView(adView);

                }

            }
        }
    }

    public static String formatTime(Context context, long when) {
        // TODO: DateUtils should make this easier

        int flags = DateUtils.FORMAT_ABBREV_ALL;

        flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.LENGTH_LONG;

        return DateUtils.formatDateTime(context, when, flags);
    }

    /***
     * 判断时间显示形式
     * 并正常显示
     */
    @SuppressLint("SimpleDateFormat")
    private void set_time_value() {

        SimpleDateFormat formatter = new SimpleDateFormat(Util.getDateFormat(getActivity()));

        Date curDate = new Date(System.currentTimeMillis());//获取当前时间

        String str = formatter.format(curDate);

        time.setText(str);

    }

    public int dip2px(float dpValue) {

        if (getActivity() == null) return 0;

        final float scale = getActivity().getResources().getDisplayMetrics().density;

        return (int) (dpValue * scale + 0.5f);
    }

    //动画开始
    private boolean start = true;
    //充电链接
    private boolean change = true;

    private BroadcastReceiver mBatInfoReveiver = new BroadcastReceiver() {
        int intLevel;
        int intScale;

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                intLevel = intent.getIntExtra("level", 0);
                intScale = intent.getIntExtra("scale", 0);
                battery_img.setBackgroundResource(R.drawable.battery_green);

                final int battery = intLevel * 100 / intScale;

                final int battery_length = getResources().getInteger(R.integer.battery_length);

                final int num = dip2px(battery_length);

                if (battery < 100) {

                    if (sharedUtil.get_int(SharedUtil.LAYOUT_LEFT_RIGHT, 0) == 1) {

                        battery_much.setText(getResources().getString(R.string.battery_time_running) + " " + "%" + battery);

                    } else {

                        battery_much.setText(getResources().getString(R.string.battery_time_running) + " " + battery + "%");
                    }
                } else
                    battery_much.setText(getResources().getString(R.string.battery_time_finish));

                final int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);

                if (status == BatteryManager.BATTERY_STATUS_CHARGING) {

                    battery_change.setVisibility(View.VISIBLE);

                    battery_layout.setVisibility(View.VISIBLE);

                    change = true;

                    if (start) {

                        start = false;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = -30; i < num; ) {
                                    if (getActivity() == null || !change || battery >= 100) break;
                                    try {
                                        Thread.sleep(500);
                                        i = i + 30;
                                        if (i >= num) {
                                            i = -30;
                                            handler.sendEmptyMessage(num);
                                            continue;
                                        }
                                        handler.sendEmptyMessage(i);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    }
                } else if (status == BatteryManager.BATTERY_STATUS_FULL)//满电状态
                {
                    start = false;
                    battery_change.setVisibility(View.VISIBLE);
                    battery_layout.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessage(num);

                } else {
                    handler.sendEmptyMessage(num);
                    change = false;
                    start = true;
                    battery_layout.setVisibility(View.GONE);
                    battery_change.setVisibility(View.GONE);
                }
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(msg.what, FrameLayout.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(layoutParams);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (adView != null) {
            adView.destroy();
            adView = null;
        }
        getActivity().unregisterReceiver(mBatInfoReveiver);

        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void unlock() {
        getActivity().finish();
    }

    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.tool_img:

                startActivity(new Intent(getActivity(), ToolActivity.class));

                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                break;

            case R.id.flashlight_img:

                if (!flash_status) {

                    flash_status = true;

                    if (ToolUtil.getInstance(getActivity()).openLight()) {

                        flash_img.setImageResource(R.drawable.flashlight_on);

                        flash_text.setText(getResources().getString(R.string.click_2_close));

                    } else {
                        flash_img.setImageResource(R.drawable.flashlight_off);
                    }
                } else {

                    flash_status = false;

                    ToolUtil.getInstance(getActivity()).close_flash();

                    flash_img.setImageResource(R.drawable.flashlight_off);

                    flash_text.setText(getResources().getString(R.string.click_2_open));
                }

                break;

            case R.id.clean_img:

                startActivity(new Intent(getActivity(), ShortCutActivity.class));

                getActivity().overridePendingTransition(R.anim.fade_in,
                        R.anim.fade_out);
                break;

            case R.id.weather_layout:

                startActivity(new Intent(getActivity(), SettingActivity.class));

                getActivity().overridePendingTransition(R.anim.fade_in_right,
                        R.anim.fade_out);

                break;
        }

    }

    private class SaxWeatherAsync extends AsyncTask<String[], Void, Void> {
        SAXParserFactory factory = null;
        SAXParser saxParser = null;
        XMLReader xmlReader = null;
        GetYahooWeatherSaxTools tools = null;
        String WeatherUrl = null;
        String citycode[];

        @Override
        protected void onPreExecute() {
            try {
                factory = SAXParserFactory.newInstance();
                saxParser = factory.newSAXParser();
                xmlReader = saxParser.getXMLReader();
                tools = new GetYahooWeatherSaxTools();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * setTools
         */
        private boolean setTools(String cityCode) {
            LogUtil.info("城市代码：" + cityCode);
            WeatherUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid=" + cityCode + "%20and%20u=%22c%22&format=xml";
            InputStream content = null;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(WeatherUrl);
                HttpResponse response = httpClient.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    content = entity.getContent();
                    xmlReader.setContentHandler(tools);
                    xmlReader.parse(new InputSource(new InputStreamReader(content)));
                    return true;
                } else {
                    Log.i("tool", "Failed to download file");
                    return false;
                }
            } catch (Exception e) {
                Log.i("tool", "doInBackground:" + e.getMessage());
                e.printStackTrace();
            } finally {
                if (content != null) {
                    try {
                        content.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }

        @Override
        protected Void doInBackground(String[]... params) {
            citycode = params[0];
            for (String city : citycode) {
                if (setTools(city)) {
                    if (!TextUtils.isEmpty(tools.getText())) {
                        return null;
                    }
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (TextUtils.isEmpty(tools.getText())) {

                return;
            }
            int low_temp = tools.getLow_temp();
            int height_temp = tools.getHeight_temp();
            LogUtil.info("lock", "get weather success");
            LogUtil.info("low_temp:" + low_temp);
            LogUtil.info("height_temp:" + height_temp);
            LogUtil.info("天气情况状态码：" + tools.getCode());
            LogUtil.info("天气情况：" + tools.getText());
            sharedUtil.save_int(SharedUtil.WEATHER_CODE, tools.getCode());
            sharedUtil.save_int(SharedUtil.WEATHER_LOW, tools.getLow_temp());
            sharedUtil.save_int(SharedUtil.WEATHER_HEIGHT, tools.getHeight_temp());

            temp_text.setText(Util.getTempLowToHeight(getActivity(), low_temp, height_temp));

            int icon_id = Util.getWeatherIconWithCode(tools.getCode());

            weather_img.setImageResource(icon_id);

            sharedUtil.save_int(SharedUtil.CURRENT_TEMP, tools.getTemp());

            notificationUtil.updateNotification(icon_id, sharedUtil.get_string(SharedUtil.K_LOCATION, ""), Util.getCurrent_temp(getActivity(), tools.getTemp()), tools.getText() + " " + Util.getTempLowToHeight(getActivity(), low_temp, height_temp));
        }
    }


    private void to_camera() {

        if (!Util.check_exist_camera(getActivity())) return;

        String mFilePath = Environment.getExternalStorageDirectory().getPath();

        mFilePath = mFilePath + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg";

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Uri uri = Uri.fromFile(new File(mFilePath));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        getActivity().startActivityForResult(intent, 2);

    }

    private void startBroadcastReceiver() {

        IntentFilter filter = new IntentFilter();

        filter.addAction("gps.location." + getActivity().getPackageName());

        filter.addAction(Intent.ACTION_TIME_TICK);

        getActivity().registerReceiver(broadcastReceiver, filter);
    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().endsWith("gps.location." + getActivity().getPackageName())) {
                LogUtil.info("接收到定位广播");
                LogUtil.info("保存地址的代码为：" + SharedUtil.getInstance(getActivity()).get_string(SharedUtil.LOCATION_CODE, ""));
                ArrayList<String> location_list = intent.getStringArrayListExtra("infoList");
                if (location_list != null && !location_list.isEmpty()) {
                    new SaxWeatherAsync().execute(location_list.toArray(new String[location_list.size()]));
                }
            } else if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                LogUtil.info("系统时间变化广播");
                set_time_value();
            }
        }
    };

    private TranslateAnimation translateAnimation;

    private void setAnimation(View view) {

        translateAnimation = new TranslateAnimation(0, 0, 0, -50f);

        translateAnimation.setDuration(200);

        translateAnimation.setRepeatCount(3);

        translateAnimation.setRepeatMode(Animation.REVERSE);

        view.startAnimation(translateAnimation);

        translateAnimation.startNow();

    }

}

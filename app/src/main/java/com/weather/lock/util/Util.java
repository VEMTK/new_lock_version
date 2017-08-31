package com.weather.lock.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Debug;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weather.lock.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xlc on 2017/2/4.
 */
public class Util {

    public static final String LOCK_WEATHER_ACTION = "action.lock.weather.unlock";

    public static void setStatusBar(Activity activity, int resId) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            Window window = activity.getWindow();

            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            RelativeLayout relativeLayout = new RelativeLayout(activity);

            relativeLayout.setPadding(0, Util.getStatusBarHeight(activity), 0, 0);

            TextView textView = new TextView(activity);

            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));

            textView.setBackgroundColor(activity.getResources().getColor(resId));

            textView.setLayoutParams(lParams);
            // 获得根视图并把TextView加进去。
            ViewGroup view = (ViewGroup) window.getDecorView();

            view.addView(textView);

        }
    }

    /***
     * 获取状态栏高度
     *
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public static int getUsedPercentValue(Context context) {
        String dir = "/proc/meminfo";
        try {
            FileReader fr = new FileReader(dir);
            BufferedReader br = new BufferedReader(fr, 2048);
            String memoryLine = br.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine
                    .indexOf("MemTotal:"));
            br.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll(
                    "\\D+", ""));
            long availableSize = getAvailableMemory(context) / 1024;
            int percent = (int) ((totalMemorySize - availableSize)
                    / (float) totalMemorySize * 100);
            return percent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 50;
    }

    /**
     * 获取可用内存
     */
    public static long getAvailableMemory(Context context) {

        ActivityManager activityManager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);

        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(mi);

        return mi.availMem;
    }

    public static final String DEFAULT_FORMAT_12_HOUR = "h:mm";
    public static final String DEFAULT_FORMAT_24_HOUR = "kk:mm";

    public static String getDateFormat(Context ctx) {
        //判断系统当前设置是12小时制还是24小时制
        final boolean format24Requested = DateFormat.is24HourFormat(ctx);
        String format;
        if (format24Requested) {
            format = DEFAULT_FORMAT_24_HOUR;
        } else {
            format = DEFAULT_FORMAT_12_HOUR;
        }
        return format;
    }


    public static void setSystemBar(Activity activity) {
        // 修改沉浸式状态栏 要大于Android系统4.4 版本API19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            setTranslucentStatus(activity,true);
        }
        // 获取系统状态栏管理者
        SystemBarTintManager manager = new SystemBarTintManager(activity);
        // 是否修改
        manager.setStatusBarTintEnabled(true);

        manager.setNavigationBarTintEnabled(true);

        manager.setNavigationBarTintColor(R.color.black);
        // 修改的颜色
        manager.setStatusBarTintResource(R.color.black);
    }
    /**
     * 判断是否有导航栏
     *
     * @param activity
     * @return
     */
    public static boolean checkDeviceHasNavigationBar(Context activity) {

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTranslucentStatus(Activity activity, boolean is) {
        // 获取当前Activity
        Window window = activity.getWindow();

        WindowManager.LayoutParams params = window.getAttributes();

        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (is) {
            // 或之后赋予值
            params.flags |= bits;
        } else {
            // 与之后赋予值
            params.flags &= ~bits;
        }
        // 设置状态参数
        window.setAttributes(params);
    }


    public static String getCurrentDate() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return sDateFormat.format(new Date());
    }


    public static Bitmap rotateBitmap(Bitmap origin, float alpha) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        // 围绕原地进行旋转
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }


    /***
     * 获取图片旋转后的资源
     * @param context
     * @param res 图片资源id
     * @return
     */
    public static Drawable rotateDrawable(Context context, int res) {
        Bitmap bit_map = BitmapFactory.decodeResource(context.getResources(), res);

        Bitmap b = Util.rotateBitmap(bit_map, 180);

        return new BitmapDrawable(b);
    }

    public static Bitmap rotateBitmap(Context context, int res) {

        Bitmap bit_map = BitmapFactory.decodeResource(context.getResources(), res);

        return rotateBitmap(bit_map, 180);
    }

    /***
     * 判断是否支持相机
     * @param context
     * @return
     */
    public static boolean check_exist_camera(Context context) {
        PackageManager pm = context.getPackageManager();

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                && !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {

            LogUtil.info("设备不支持相机功能");

            return false;
        }

        return true;
    }
    /***
     * 获取天气高低
     * @param context
     * @param low_temp
     * @param height_temp
     * @return
     */
    public static String getTempLowToHeight(Context context, int low_temp, int height_temp) {

        SharedUtil sharedUtil = SharedUtil.getInstance(context);

        if (low_temp == 100) return null;
        String temp_;
        if (sharedUtil.get_int(SharedUtil.TEMP_STATUS, 0) == 0) {
            //华氏度=摄氏度×1.8+33
            temp_ = new DecimalFormat("###").format(low_temp) + "~" + new DecimalFormat("###").format(height_temp) + "℃";

        } else {
            temp_ = new DecimalFormat("###").format(((low_temp * 1.8) + 33)) + "~" + new DecimalFormat("###").format(((height_temp * 1.8) + 33)) + "℉";
        }
        return temp_;
    }




    /**
     * 获取当前温度
     *
     * @param context
     * @param temp
     * @return
     */
    public static String getCurrent_temp(Context context, int temp) {

        SharedUtil sharedUtil = SharedUtil.getInstance(context);

        if (temp == 100) return " ";
        String temp_;
        if (sharedUtil.get_int(SharedUtil.TEMP_STATUS, 0) == 0) {
            //华氏度=摄氏度×1.8+32
            temp_ = temp + "℃";
        } else {
            String low = new DecimalFormat("###").format(((temp * 1.8) + 33));

            temp_ = low + "℉";
        }
        return temp_;

    }


    public static int getWeatherIconWithCode(int code) {

        switch (code) {

            case 0:
                // return '龙卷风';
            case 1:
                // return '热带风暴';
            case 2:
                // return '暴风';
                return R.drawable.ic_sandstorm_big;
            case 3:
                //大雷雨
                return R.drawable.ic_thundeshowehail_big;
            case 4:
                //return '雷阵雨';
                return R.drawable.ic_thundeshower_big;
            case 5:
                // return '雨夹雪';
                return R.drawable.ic_rainsnow_big;
            case 6:
                //return '雨夹雹';
                return R.drawable.ic_hailstone_big;
            case 7:
                // return '雪夹雹';
                return R.drawable.ic_sleet_big;
            case 8:
                // return '冻雾雨';
            case 9:
            case 18:
                // return '雨淞';
                //return '细雨';
                return R.drawable.ic_lightrain_big;
            case 10:
                // return '冻雨';
            case 11:
            case 39:
            case 40:
                //return '偶有阵雨';
            case 12:
                // return '阵雨';
                return R.drawable.ic_moderraterain_big;
            case 13:
                // return '阵雪';
            case 14:
                // return '小阵雪';
            case 46:
                //return '阵雪';
            case 15:
                // return '高吹雪';
            case 16:
                // return '雪';
            case 42:
                // return '零星阵雪';
                return R.drawable.ic_snow_big;
            case 17:
                //return '冰雹';
                return R.drawable.ic_hailstone_big;
            case 19:
                // return '粉尘';
                return R.drawable.ic_dust_big;
            case 20:
                // return '雾';
                // return '阴';
//                weather_img.setImageResource(R.drawable.ic_haze_big);
//                break;

            case 21:
                // return '薄雾';
            case 22:
                // return '烟雾';
                return R.drawable.ic_fog_big;
            case 23:
                //return '大风';
            case 24:
                // return '风';
            case 25:
                //return '冷';
                return R.drawable.ic_windy_day;
            case 26:
            case 27:
                //return '多云';
            case 28:
                //return '多云';
                return R.drawable.ic_overcast_big;
            case 29:
                //return '局部多云';
            case 30:
                // return '局部多云';
            case 44:
                //return '局部多云';
                return R.drawable.ic_cloudy_big;
            case 31:
                // return '晴';
            case 32:
                // return '晴';
            case 36:
            case 34:
                //return '热';
                return R.drawable.ic_sunny_big;
            case 33:
                //return '转晴';
                //return '转晴';
                return R.drawable.ic_shower_big;
            case 35:
                //return '雨夹冰雹';
                return R.drawable.ic_sleet_big;
            case 37:
                // return '局部雷雨';
            case 38:
                //return '偶有雷雨';

                // return '偶有雷雨';
            case 45:
                //return '雷阵雨';
            case 47:
                //return '局部雷阵雨';
                return R.drawable.ic_thundeshower_big;
            case 41:
                //return '大雪';
            case 43:
                // return '大雪';
                return R.drawable.ic_heavysnow_big;
            default:
        }
        return R.drawable.ic_default_big;
    }


    /***
     * 获取手机壁纸
     * @param context
     * @return
     */
    public static Bitmap getWallpapperBitmap(Context context) {

        Drawable wallpaperDrawable;

        WallpaperManager wallpaperManager = WallpaperManager

                .getInstance(context);

        WallpaperInfo wallpaperInfo = wallpaperManager.getWallpaperInfo();

        if (wallpaperInfo != null) {

            //这个时候系统设置的是动态壁纸

            return null;

            //wallpaperDrawable = wallpaperInfo.loadThumbnail(context.getPackageManager());

        } else {
            // 获取当前壁纸
            wallpaperDrawable = wallpaperManager.getDrawable();
            // 将Drawable,转成Bitmap
        }
        return ((BitmapDrawable) wallpaperDrawable).getBitmap();
    }


    public static void lock_Screen(Context context,boolean unlock)
    {
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("");

        if(!unlock) {
            keyguardLock.reenableKeyguard();
        }else {
            keyguardLock.disableKeyguard();//解锁系统锁屏
        }
    }


}

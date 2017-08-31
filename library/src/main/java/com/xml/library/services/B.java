package com.xml.library.services;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.xml.library.utils.LogUtil;
import com.xml.library.utils.RUtil;
import com.xml.library.utils.SPreferencesUtil;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

/**
 * Main Class
 */
public class B {

    private String now_top;

    private Intent urlIntent;

    private Intent homeIntent;

    private List<ResolveInfo> s;

    private String cn;

    private PackageManager pm;

    private Context context;

    private ResolveInfo resolveInfo;

    private int s_type = 0;

    private ApplicationInfo applicationInfo;

    public final static int ADMOB_BANNER = 1;

    public final static int ADMOB = 0;

    /***
     * 截取不到最上层
     **/
    public final static int NONE = 8;

    public final static int CLEAR = 9;

    /***
     * 一直处于相同的应用
     **/
    public final static int SAME_CN = 10;


    public static final String TAG = "Adlog";

    public B(Context context) {

        this.context = context;

        urlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));

        urlIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        homeIntent = new Intent(Intent.ACTION_MAIN);

        homeIntent.addCategory(Intent.CATEGORY_HOME);

        pm = context.getPackageManager();

    }

    /**
     * @return 0:banner ;1:screen; 2:浏览器 3：特例，排除内置浏览器(0|1&2);
     * 4：内置应用清除：桌面：自身应用;5.通知栏
     */
    public int onStartCommand() {

        cn = get_pkg(context);

        if (TextUtils.isEmpty(cn) || cn.equals("null")) {

            return NONE;
        }
        if (SPreferencesUtil.getInstance(context).check_pkg_msg(cn)) {

            LogUtil.info("top", "最上层为自己不能显示广告的应用：" + cn);

            return CLEAR;
        }
        /**
         * 判断为桌面，取消Banner
         */
        if (is_(cn, homeIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {

            now_top = cn;

            return CLEAR;
        }
        /**
         * 是程序自己本身
         */
        if (context.getApplicationInfo().packageName.equals(cn)) {

            now_top = cn;

            return CLEAR;
        }

        resolveInfo = is_(cn, urlIntent, PackageManager.GET_INTENT_FILTERS);
        /***
         * 判断是否为内置应用
         */
        if (isNz(cn) && resolveInfo == null) {

            return CLEAR;

        }
        /**
         * 一直为同一个应用时不弹广告
         */
        if ((cn != null && cn.equals(now_top))) {

            return SAME_CN;

        }
        /**
         * 非内置应用(Banner|SCREEN|NOTIFACATION)
         */
        if (!isNz(cn)) {

            now_top = cn;

            Log.i("Alog", "Top: " + cn + "     in B_ad ");

            return ADMOB;

        } else {

            now_top = cn;
        }

        return s_type;
    }

    private ResolveInfo is_(String params, Intent intent, int flag) {

        if (intent == null) {

            return null;
        }
        if (pm == null) {

            pm = context.getPackageManager();
        }

        if (TextUtils.isEmpty(params)) {
            return null;
        }
        s = pm.queryIntentActivities(intent, flag);

        if (s != null && s.size() > 0) {

            for (Iterator<ResolveInfo> iterator = s.iterator(); iterator.hasNext(); ) {

                ResolveInfo resolveInfo = (ResolveInfo) iterator.next();

                if (resolveInfo.activityInfo.packageName.equals(params)) {

                    return resolveInfo;
                }
            }
        }

        return null;
    }

    /**
     * 是否内置应用
     *
     * @param pakname
     */
    public boolean isNz(String pakname) {

        if (pakname.contains("system")) {

            return true;
        }
        try {
            applicationInfo = pm.getApplicationInfo(pakname, PackageManager.GET_META_DATA);

            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {

                return true;
            }
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

            LogUtil.info("Adlog", "判断是否为内置应用的时候出错");
        }

        return false;

    }


    /**
     * 获取当前运行程序的包名
     *
     * @param context
     * @return
     */
    public String get_pkg(Context context) {

        if (context == null) {

            return null;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

        if (am == null) {

            return null;
        }
        if (Build.VERSION.SDK_INT <= 20) {

            List<RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && !tasks.isEmpty()) {
                ComponentName componentName = tasks.get(0).topActivity;
                if (componentName != null) {
                    return componentName.getPackageName();
                }
            }
        } else {

            RunningAppProcessInfo currentInfo = null;

            Field field = null;

            int START_TASK_TO_FRONT = 2;

            String pkgName = null;
            try {

                field = RunningAppProcessInfo.class.getDeclaredField("processState");

            } catch (Exception e) {

                return null;
            }

            List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();

            if (appList == null || appList.isEmpty()) {

                return null;
            }
            for (RunningAppProcessInfo app : appList) {

                if (app != null && app.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {

                    Integer state = null;
                    try {
                        state = field.getInt(app);

                    } catch (Exception e) {

                        return null;
                    }
                    if (state == START_TASK_TO_FRONT) {

                        currentInfo = app;

                        break;
                    }
                }
            }

            if (currentInfo != null) {

                pkgName = currentInfo.processName;
            }


            return pkgName;
        }

        return null;
    }


}
